<template>
  <div class="constitution-trend-analysis">
    <el-page-header @back="goBack" content="体质变化趋势分析" />
    
    <el-card class="trend-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>体质变化趋势分析</span>
          <el-button type="primary" @click="loadTrendData" :disabled="loading">
            更新趋势
          </el-button>
        </div>
      </template>
      
      <div v-if="trendData && trendData.hasData" class="trend-content">
        <!-- 趋势摘要 -->
        <div class="trend-summary">
          <el-alert 
            :title="getChineseSummary(trendData?.summary) || '暂无分析'" 
            :type="trendData?.hasData ? 'success' : 'info'" 
            :closable="false" 
            show-icon 
          />
        </div>

        <!-- AI 洞察 -->
        <div v-if="trendData.hasData" class="ai-insights mb-24">
          <div class="insights-header">
            <el-icon class="ai-icon"><MagicStick /></el-icon>
            <h3>AI 智能健康洞察</h3>
          </div>
          
          <el-card class="insight-card">
             <div class="streaming-content markdown-body" v-html="renderMarkdown(streamingInsights)"></div>
             <div v-if="isStreaming" class="typing-cursor"></div>
          </el-card>
        </div>
        
        <!-- 趋势图表 -->
        <div class="trend-charts">
          <h3>体质得分变化趋势</h3>
          <div class="chart-container">
            <v-chart :option="trendChartOption" autoresize />
          </div>
        </div>
        
        <!-- 体质变化详情 -->
        <div class="trend-details">
          <h3>体质变化详情</h3>
          <el-table :data="trendItems" style="width: 100%">
            <el-table-column prop="name" label="体质类型" width="120" />
            <el-table-column label="变化趋势" width="120">
              <template #default="{ row }">
                <el-tag :type="getTrendTagType(row.trend)">
                  {{ row.trend }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" />
          </el-table>
        </div>
        
        <!-- 评估记录统计 -->
        <div class="assessment-stats" v-if="trendData?.assessmentCount">
          <h3>评估记录统计</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="评估次数">{{ trendData.assessmentCount }}</el-descriptions-item>
            <el-descriptions-item label="评估时间范围">
              {{ formatDate(trendData.dateRange?.from) }} 至 {{ formatDate(trendData.dateRange?.to) }}
            </el-descriptions-item>
            <el-descriptions-item label="当前主导体质">
              <el-tag type="success">{{ getConstitutionName(trendData.currentPrimaryType) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <div v-else class="no-data">
        <el-empty description="暂无体质趋势数据，请先完成多次体质测评" />
        <el-button type="primary" @click="$router.push('/tcm/assessments')">前往体质测评</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as assessmentApi from '@/api/assessment'
import { getToken } from '@/utils/auth'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getConstitutionName, getConstitutionColor } from '@/utils/tcm-constants'
import { MagicStick, List, TrendCharts } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const trendData = ref(null)
const streamingInsights = ref('')
const isStreaming = ref(false)
const md = new MarkdownIt({
  html: true,
  breaks: true,
  linkify: true
})

const replaceConstitutionCodes = (text) => {
  if (!text) return ''
  let result = text
  const codes = [
    'BALANCED', 'QI_DEFICIENCY', 'YANG_DEFICIENCY', 'YIN_DEFICIENCY', 
    'PHLEGM_DAMPNESS', 'DAMP_HEAT', 'BLOOD_STASIS', 'QI_STAGNATION', 'SPECIAL_DIATHESIS'
  ]
  codes.forEach(code => {
    const name = getConstitutionName(code)
    result = result.replace(new RegExp(code, 'g'), name)
  })
  return result
}

const renderMarkdown = (text) => {
  if (!text) return ''
  // 1. Replace constitution codes with Chinese names
  let processed = replaceConstitutionCodes(text)
  
  // 2. Pre-processing for Markdown structure
  
  // Ensure headers have a newline before them (unless at start) and a space after #
  // First, ensure space after #: "###Title" -> "### Title"
  processed = processed.replace(/(#{1,6})([^#\s])/g, '$1 $2')
  // Second, ensure newline before headers: "End### Title" -> "End\n\n### Title"
  processed = processed.replace(/([^\n])\s*(#{1,6}\s)/g, '$1\n\n$2')
  
  // Ensure lists have a newline before them
  // "Text1. Item" -> "Text\n1. Item"
  processed = processed.replace(/([^\n])\s*(\d+\.\s)/g, '$1\n$2')
  // "Text- Item" -> "Text\n- Item"
  processed = processed.replace(/([^\n])\s*(-\s)/g, '$1\n$2')
  
  // 3. Render Markdown
  const html = md.render(processed)
  
  // 4. Sanitize HTML
  return DOMPurify.sanitize(html)
}

const goBack = () => {
  router.go(-1)
}

const loadInsightsStream = async () => {
  try {
    isStreaming.value = true
    streamingInsights.value = ''
    
    const token = getToken()
    const headers = { 'Accept': 'text/event-stream' }
    if (token) headers['Authorization'] = `Bearer ${token}`
    
    // Dev fallback
    if (!token) {
       const devUserId = localStorage.getItem('dev_user_id') || '4'
       headers['X-User-Id'] = devUserId
    }

    const response = await fetch('/api/tcm-assessment/trend/stream?lookbackDays=90', { headers })
    
    if (!response.ok) throw new Error(response.statusText)
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true })
      const lines = chunk.split('\n')
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data) streamingInsights.value += data
        }
      }
    }
  } catch (e) {
    console.error('Stream error', e)
  } finally {
    isStreaming.value = false
  }
}

const loadTrendData = async () => {
  try {
    loading.value = true
    const response = await assessmentApi.getConstitutionTrend(90) // 获取最近90天的趋势
    trendData.value = response.data
    
    // Start streaming insights after data load
    if (trendData.value && trendData.value.hasData) {
        loadInsightsStream()
    }
  } catch (error) {
    ElMessage.error('获取体质趋势数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const getChineseSummary = (text) => {
  if (!text) return ''
  // 替换体质英文代码为中文名
  let result = text
  const codes = [
    'BALANCED', 'QI_DEFICIENCY', 'YANG_DEFICIENCY', 'YIN_DEFICIENCY', 
    'PHLEGM_DAMPNESS', 'DAMP_HEAT', 'BLOOD_STASIS', 'QI_STAGNATION', 'SPECIAL_DIATHESIS'
  ]
  codes.forEach(code => {
    const name = getConstitutionName(code)
    result = result.replace(new RegExp(code, 'g'), name)
  })
  return result
}

const getChineseContent = (text) => {
  if (!text) return ''
  return getChineseSummary(text)
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

const getTrendTagType = (trend) => {
  switch (trend) {
    case '上升':
      return 'danger'
    case '下降':
      return 'primary'
    case '稳定':
      return 'success'
    default:
      return 'info'
  }
}

// 趋势图表选项
const trendChartOption = computed(() => {
  if (!trendData.value || !trendData.value.hasData || !trendData.value.historyScores) {
    return {}
  }
  
  const xAxisData = trendData.value.dates || []
  const seriesData = []
  
  Object.entries(trendData.value.historyScores).forEach(([type, scores]) => {
    seriesData.push({
      name: getConstitutionName(type),
      data: scores,
      color: getConstitutionColor(type)
    })
  })
  
  return {
    title: {
      text: '体质得分变化趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      top: '10%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    series: seriesData.map(item => ({
      name: item.name,
      type: 'line',
      smooth: true,
      data: item.data,
      itemStyle: {
        color: item.color
      }
    }))
  }
})

// 趋势项目计算属性
const trendItems = computed(() => {
  if (!trendData.value || !trendData.value.trends) {
    return []
  }
  
  return Object.entries(trendData.value.trends).map(([type, trend]) => ({
    name: getConstitutionName(type),
    trend: trend,
    description: `${getConstitutionName(type)}体质${trend}趋势`
  }))
})

const insights = computed(() => trendData.value?.insights || null)

onMounted(() => {
  loadTrendData()
})
</script>

<style scoped lang="scss">
@use "sass:map";
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.constitution-trend-analysis {
  padding: 24px;
  min-height: 100%;
  background-color: transparent; /* Use global background */
}

.trend-card {
  margin-top: 20px;
  @include mixins.glass-effect;
  border: vars.$glass-border;
  border-radius: vars.$radius-lg;
  animation: fadeInUp 0.6s vars.$ease-spring;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(0,0,0,0.05);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  span {
    font-size: 18px;
    font-weight: 600;
    color: vars.$text-main-color;
  }
}

.trend-summary {
  margin-bottom: 24px;
  animation: fadeInUp 0.6s vars.$ease-spring 0.1s backwards;
}

.ai-insights {
  margin: 24px 0;
  animation: fadeInUp 0.6s vars.$ease-spring 0.2s backwards;
  
  .insights-header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    color: vars.$primary-color;
    
    .ai-icon {
      margin-right: 8px;
      font-size: 20px;
    }
    
    h3 {
      margin: 0;
      font-size: 16px;
    }
  }
  
  .insight-card {
    background: rgba(255, 255, 255, 0.4);
    border: vars.$glass-border;
    border-radius: vars.$radius-md;
    transition: all 0.3s vars.$ease-spring;
    min-height: 120px;
    
    .streaming-content {
        padding: 16px;
        font-size: 15px;
        line-height: 1.8;
        color: vars.$text-main-color;
        
        :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
            margin-top: 20px;
            margin-bottom: 12px;
            font-weight: 600;
            color: vars.$primary-color;
            line-height: 1.4;
            &:first-child { margin-top: 0; }
        }

        :deep(h3) { font-size: 18px; }
        :deep(h4) { font-size: 16px; }
        
        :deep(p) { margin-bottom: 12px; text-align: justify; }
        :deep(ul), :deep(ol) { padding-left: 24px; margin-bottom: 12px; }
        :deep(li) { margin-bottom: 6px; }
        :deep(strong) { color: vars.$primary-color; font-weight: 600; }
        :deep(blockquote) {
            margin: 16px 0;
            padding: 8px 16px;
            background-color: rgba(vars.$primary-color, 0.05);
            border-left: 4px solid vars.$primary-color;
            border-radius: 4px;
            color: vars.$text-secondary-color;
        }
        :deep(code) {
            background-color: rgba(0,0,0,0.05);
            padding: 2px 5px;
            border-radius: 4px;
            font-family: monospace;
            font-size: 0.9em;
        }
    }
    
    .typing-cursor {
        display: inline-block;
        width: 8px;
        height: 16px;
        background-color: vars.$primary-color;
        animation: blink 1s infinite;
        vertical-align: middle;
        margin-left: 4px;
    }
  }
}

@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

.trend-charts {
  margin: 32px 0;
  animation: fadeInUp 0.6s vars.$ease-spring 0.3s backwards;
  
  h3 {
    color: vars.$text-main-color;
    margin-bottom: 16px;
    font-size: 16px;
  }
}

.chart-container {
  height: 400px;
  padding: 16px;
  background: rgba(255,255,255,0.4);
  border-radius: vars.$radius-lg;
  border: vars.$glass-border;
}

.trend-details {
  margin: 32px 0;
  animation: fadeInUp 0.6s vars.$ease-spring 0.4s backwards;
  
  h3 {
    color: vars.$text-main-color;
    margin-bottom: 16px;
    font-size: 16px;
  }
}

.assessment-stats {
  margin: 32px 0;
  animation: fadeInUp 0.6s vars.$ease-spring 0.5s backwards;
  
  h3 {
    color: vars.$text-main-color;
    margin-bottom: 16px;
    font-size: 16px;
  }
}

.no-data {
  text-align: center;
  padding: 60px 0;
}

.mb-16 { margin-bottom: 16px; }
.mb-24 { margin-bottom: 24px; }
</style>