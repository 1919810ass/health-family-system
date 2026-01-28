<template>
  <el-card class="seasonal-wellness-card" :body-style="{ padding: '0px' }" shadow="hover">
    <div class="wellness-container" v-loading="loading">
      <!-- 左侧/背景区域：节气展示 -->
      <div class="solar-term-section" :style="sectionStyle">
        <div class="term-name">{{ wellnessData.solarTerm || '节气' }}</div>
        <div class="term-date">{{ currentDate }}</div>
      </div>
      
      <!-- 右侧区域：养生建议 -->
      <div class="advice-section">
        <div class="header">
          <div class="title-wrapper">
            <span class="title">今日养生</span>
            <el-tag v-if="isStreaming" type="success" size="small" class="ai-tag" effect="dark">
              <el-icon class="is-loading"><Loading /></el-icon> AI 生成中
            </el-tag>
            <el-tag v-else-if="wellnessData.advice" type="info" size="small" class="ai-tag" effect="light">
              AI 智能建议
            </el-tag>
          </div>
          <el-tag v-if="wellnessData.constitution" type="warning" size="small" effect="plain">
            针对：{{ getConstitutionName(wellnessData.constitution) }}
          </el-tag>
        </div>
        
        <div class="content">
          <p class="advice-text">
            {{ formattedAdvice || '正在生成个性化养生建议...' }}
            <span v-if="isStreaming" class="cursor">|</span>
          </p>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getToken } from '@/utils/auth'
import { Loading } from '@element-plus/icons-vue'
import { getConstitutionName, CONSTITUTION_NAMES } from '@/utils/tcm-constants'
import dayjs from 'dayjs'

const loading = ref(true)
const isStreaming = ref(false)
const wellnessData = ref({
  solarTerm: '',
  constitution: '',
  advice: '',
  imageUrl: ''
})

const solarTermImages = import.meta.glob('@/assets/solar-terms/*.png', { eager: true, import: 'default' })

const currentDate = computed(() => dayjs().format('MM月DD日'))

const localImageUrl = computed(() => {
  const term = wellnessData.value.solarTerm?.trim()
  if (!term) return ''
  const matchedKey = Object.keys(solarTermImages).find((path) => path.endsWith(`/${term}.png`))
  return matchedKey ? solarTermImages[matchedKey] : ''
})

const resolvedImageUrl = computed(() => localImageUrl.value || wellnessData.value.imageUrl || '')

const sectionStyle = computed(() => {
  if (resolvedImageUrl.value) {
    return {
      backgroundImage: `linear-gradient(rgba(0,0,0,0.3), rgba(0,0,0,0.3)), url(${resolvedImageUrl.value})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {
    background: 'linear-gradient(135deg, #e6b980 0%, #eacda3 100%)'
  }
})

const formattedAdvice = computed(() => {
  let text = wellnessData.value.advice
  if (!text) return ''
  
  // Replace all English constitution codes with Chinese names
  // Also handle spaces instead of underscores just in case (e.g. QI DEFICIENCY)
  Object.entries(CONSTITUTION_NAMES).forEach(([code, name]) => {
    // Replace standard code (e.g., QI_DEFICIENCY)
    text = text.replace(new RegExp(code, 'g'), name)
    // Replace space-separated code (e.g., QI DEFICIENCY)
    const spaceCode = code.replace(/_/g, ' ')
    text = text.replace(new RegExp(spaceCode, 'g'), name)
  })
  
  return text
})

const fetchWellnessData = async () => {
  try {
    loading.value = true
    isStreaming.value = false
    wellnessData.value.advice = '' // 清空旧数据
    
    const token = getToken()
    const headers = {
        'Accept': 'text/event-stream'
    }
    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    } else {
        const devUserId = localStorage.getItem('dev_user_id') || '4'
        headers['X-User-Id'] = devUserId
    }

    const response = await fetch('/api/wellness/today/stream', { headers })

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
    }

    loading.value = false // 收到响应后关闭大的 loading
    isStreaming.value = true // 开始流式输出

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
        const { done, value } = await reader.read()
        if (done) break
        
        buffer += decoder.decode(value, { stream: true })
        const chunks = buffer.split('\n\n')
        buffer = chunks.pop()
        
        for (const chunk of chunks) {
            const lines = chunk.split('\n')
            let eventType = 'message'
            let data = ''
            
            for (const line of lines) {
                if (line.startsWith('event:')) {
                    eventType = line.substring(6).trim()
                } else if (line.startsWith('data:')) {
                    data = line.substring(5).trim()
                }
            }
            
            if (eventType === 'meta') {
                try {
                    const meta = JSON.parse(data)
                    wellnessData.value.solarTerm = meta.solarTerm
                    wellnessData.value.constitution = meta.constitution
                    wellnessData.value.imageUrl = meta.imageUrl
                } catch (e) {
                    console.error('Failed to parse meta', e)
                }
            } else if (eventType === 'data') {
                // 如果 data 是空的，不要追加
                if (data) {
                    wellnessData.value.advice += data
                }
            } else if (eventType === 'complete') {
                isStreaming.value = false
            }
        }
    }
  } catch (error) {
    console.error('Failed to fetch seasonal wellness data', error)
    if (!wellnessData.value.advice) {
        wellnessData.value.advice = '暂无建议，请稍后再试。'
    }
    isStreaming.value = false
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchWellnessData()
})
</script>

<style scoped lang="scss">
.seasonal-wellness-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  border: none;
  
  .wellness-container {
    display: flex;
    min-height: 140px;
    
    @media (max-width: 768px) {
      flex-direction: column;
    }
  }
  
  .solar-term-section {
    width: 30%;
    min-width: 120px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
    text-shadow: 0 2px 4px rgba(0,0,0,0.3);
    
    @media (max-width: 768px) {
      width: 100%;
      padding: 20px 0;
    }
    
    .term-name {
      font-size: 2.5rem;
      font-weight: bold;
      font-family: "KaiTi", "STKaiti", serif;
      letter-spacing: 4px;
    }
    
    .term-date {
      font-size: 1rem;
      margin-top: 8px;
      opacity: 0.9;
    }
  }
  
  .advice-section {
    flex: 1;
    padding: 20px;
    background-color: #fdfbf7;
    display: flex;
    flex-direction: column;
    
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .title-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .title {
        font-size: 1.2rem;
        font-weight: 600;
        color: #5c4033;
      }

      .ai-tag {
        font-size: 10px;
        height: 20px;
        padding: 0 6px;
        display: flex;
        align-items: center;
        gap: 2px;
      }
    }
    
    .content {
      flex: 1;
      
      .advice-text {
        color: #666;
        font-size: 0.95rem;
        line-height: 1.6;
        text-align: justify;
        white-space: pre-wrap;
        position: relative;
      }

      .cursor {
        display: inline-block;
        width: 2px;
        animation: blink 1s infinite;
        color: #409EFF;
        font-weight: bold;
        margin-left: 2px;
        vertical-align: middle;
      }
    }
  }
}

@keyframes blink {
  from, to { opacity: 0; }
  50% { opacity: 1; }
}
</style>
