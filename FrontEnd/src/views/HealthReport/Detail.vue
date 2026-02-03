<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <el-tooltip content="返回" placement="bottom">
        <el-button circle text class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </el-tooltip>
      <div class="header-icon">
        <el-icon><Document /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">报告详情</h2>
        <p class="subtitle">AI 智能分析报告详情，提供专业解读</p>
      </div>
    </div>

    <div v-if="report" class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Left: Image -->
        <el-card class="glass-card">
            <template #header>
                <div class="font-bold">原始图片</div>
            </template>
            <img :src="report.imageUrl" class="w-full rounded-lg" />
        </el-card>

        <!-- Right: Analysis -->
        <div class="space-y-6">
            <el-card class="glass-card">
                <template #header>
                    <div class="font-bold flex justify-between items-center">
                        <span>智能解读</span>
                        <el-tag :type="statusTagType">{{ getStatusText(statusValue) }}</el-tag>
                    </div>
                </template>
                
                <div v-if="statusValue === 'COMPLETED'">
                    <div v-if="interpretation" class="mb-6">
                        <h4 class="font-bold mb-2 text-primary">总体评价</h4>
                        <p class="text-gray-700 leading-relaxed">{{ summaryText }}</p>
                    </div>
                    
                    <div v-if="ocrData && ocrData.items">
                        <h4 class="font-bold mb-2 text-primary">详细指标</h4>
                        <el-table :data="ocrItems" style="width: 100%" stripe>
                            <el-table-column prop="name" label="项目" />
                            <el-table-column prop="value" label="结果" />
                            <el-table-column label="解读">
                                <template #default="scope">
                                    {{ getInterpretation(scope.row.name) }}
                                </template>
                            </el-table-column>
                        </el-table>
                    </div>
                </div>
                <div v-else-if="statusValue === 'PROCESSING' || statusValue === 'PENDING'" class="py-10 text-center">
                    <el-progress :percentage="progressValue" :stroke-width="6" status="success" />
                    <p class="mt-4">{{ progressStageText }}</p>
                </div>
                <div v-else-if="statusValue === 'FAILED'">
                    <el-result icon="error" title="分析失败" sub-title="请重试或上传清晰图片" />
                </div>
                <div v-else class="py-10 text-center">
                    <el-icon class="is-loading text-4xl text-primary mb-4"><Loading /></el-icon>
                    <p>报告正在处理中，请稍候...</p>
                </div>
            </el-card>

            <el-card class="glass-card">
                <template #header>
                    <div class="font-bold flex justify-between items-center">
                        <span>医生点评</span>
                        <span class="text-gray-500 text-sm">{{ report.doctorCommentTime ? formatDate(report.doctorCommentTime) : '暂无' }}</span>
                    </div>
                </template>
                <div v-if="report.doctorComment" class="text-gray-700 leading-relaxed">{{ report.doctorComment }}</div>
                <el-empty v-else description="暂无医生点评" :image-size="80" />
            </el-card>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getReportDetail, getReportStatus } from '@/api/report'
import { Document, ArrowLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const report = ref(null)
const statusValue = computed(() => (report.value?.status || '').toString().trim().toUpperCase())
const statusTagType = computed(() => {
    if (statusValue.value === 'COMPLETED') return 'success'
    if (statusValue.value === 'FAILED') return 'danger'
    return 'warning'
})
const streamSummary = ref('')
const streamDetails = ref({})
const streamActive = ref(false)
let streamInterval = null
let pollInterval = null
const progressValue = ref(0)
let progressTimer = null
const progressStageText = ref('AI正在努力分析您的报告，请稍候...')

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/report')
}

const ocrData = computed(() => {
    if (!report.value || !report.value.ocrData) return null
    try {
        return JSON.parse(report.value.ocrData)
    } catch (e) {
        return null
    }
})

const interpretation = computed(() => {
    if (!report.value || !report.value.interpretation) return null
    try {
        return JSON.parse(report.value.interpretation)
    } catch (e) {
        return null
    }
})

const ocrItems = computed(() => {
    if (!ocrData.value || !ocrData.value.items) return []
    // Convert map to array if needed, or if it is already map
    // The mock data returns items as Map<String, String>
    // Frontend needs to handle it.
    const items = ocrData.value.items
    if (Array.isArray(items)) return items
    if (typeof items === 'object') {
        return Object.keys(items).map(key => ({
            name: key,
            value: items[key]
        }))
    }
    return []
})

const getInterpretation = (itemName) => {
    if (streamActive.value && streamDetails.value[itemName]) return streamDetails.value[itemName]
    if (!interpretation.value || !interpretation.value.details) return '待分析'
    return interpretation.value.details[itemName] || '待分析'
}

const getStatusText = (status) => {
    const map = {
        'PENDING': '待处理',
        'PROCESSING': '分析中',
        'COMPLETED': '已完成',
        'FAILED': '失败'
    }
    return map[status] || status
}

const summaryText = computed(() => {
    if (streamActive.value && streamSummary.value) return streamSummary.value
    return interpretation.value?.summary || ''
})

const normalizeStatus = (status) => (status || '').toString().trim().toUpperCase()

const stopStream = () => {
    if (streamInterval) {
        clearInterval(streamInterval)
        streamInterval = null
    }
    streamActive.value = false
}
const startProgress = () => {
    if (progressTimer) clearInterval(progressTimer)
    const start = Date.now()
    progressValue.value = 5
    progressTimer = setInterval(() => {
        const elapsed = (Date.now() - start) / 1000
        const next = Math.min(95, Math.floor(5 + elapsed * 8))
        if (next > progressValue.value) progressValue.value = next
    }, 1000)
}
const stopProgress = (finalValue = null) => {
    if (progressTimer) {
        clearInterval(progressTimer)
        progressTimer = null
    }
    if (finalValue !== null) {
        progressValue.value = finalValue
    }
}

const startStream = () => {
    stopStream()
    if (!interpretation.value) return
    const summary = interpretation.value?.summary || ''
    const detailsMap = interpretation.value?.details || {}
    const detailList = ocrItems.value.map(item => ({
        name: item.name,
        text: detailsMap[item.name] || '待分析'
    }))
    streamSummary.value = ''
    streamDetails.value = {}
    streamActive.value = true
    let phase = 'summary'
    let idx = 0
    let charIndex = 0
    streamInterval = setInterval(() => {
        if (phase === 'summary') {
            if (charIndex < summary.length) {
                streamSummary.value += summary.charAt(charIndex++)
            } else {
                phase = 'detail'
                idx = 0
                charIndex = 0
                if (detailList.length === 0) {
                    stopStream()
                }
            }
            return
        }
        if (idx >= detailList.length) {
            stopStream()
            return
        }
        const item = detailList[idx]
        const current = streamDetails.value[item.name] || ''
        if (charIndex < item.text.length) {
            streamDetails.value = {
                ...streamDetails.value,
                [item.name]: current + item.text.charAt(charIndex++)
            }
        } else {
            idx += 1
            charIndex = 0
        }
    }, 20)
}

const stopPolling = () => {
    if (pollInterval) {
        clearInterval(pollInterval)
        pollInterval = null
    }
}

const startPolling = () => {
    stopPolling()
    pollInterval = setInterval(async () => {
        try {
            const userId = userStore.profile?.id
            const statusRes = await getReportStatus(route.params.id, userId)
            const statusData = statusRes?.data ?? statusRes
            if (!statusData) return
            const status = normalizeStatus(statusData?.status)
            if (typeof statusData?.progressPercent === 'number') {
                progressValue.value = statusData.progressPercent
            }
            if (statusData?.progressStage) {
                progressStageText.value = statusData.progressStage
            }
            if (status === 'COMPLETED' || status === 'FAILED') {
                const res = await getReportDetail(route.params.id, userId)
                const detail = res?.data ?? res
                if (detail) {
                    report.value = { ...detail, status: status || detail?.status }
                }
                stopPolling()
            }
        } catch (e) {
        }
    }, 2000)
}

watch([statusValue, interpretation, ocrItems], ([status]) => {
    if (status === 'COMPLETED' && interpretation.value) {
        startStream()
    } else {
        stopStream()
    }
})

const formatDate = (date) => {
    return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(async () => {
    loading.value = true
    try {
        const userId = userStore.profile?.id
        const res = await getReportDetail(route.params.id, userId)
        const isResult = res && typeof res === 'object' && 'code' in res
        if (isResult && res.code !== 0) {
            ElMessage.error(res.message || '加载报告失败')
            return
        }
        const data = res?.data ?? res
        report.value = data || null
        const status = normalizeStatus(report.value?.status)
        if (report.value && status !== 'COMPLETED' && status !== 'FAILED') {
            startProgress()
            startPolling()
        }
    } catch (error) {
        ElMessage.error(error?.message || '加载报告失败')
    } finally {
        loading.value = false
    }
})

onUnmounted(() => {
    stopPolling()
    stopStream()
    stopProgress()
})

watch(statusValue, (status) => {
    if (status === 'COMPLETED') {
        stopProgress(100)
    } else if (status === 'FAILED') {
        stopProgress(0)
    } else if (status === 'PROCESSING' || status === 'PENDING') {
        if (!progressTimer) startProgress()
    }
})
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.page-header {
  .back-btn {
    width: 44px;
    height: 44px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.6);
    border: 1px solid rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10px);
    transition: transform 0.25s vars.$ease-spring, background 0.25s vars.$ease-spring;
    flex-shrink: 0;

    &:hover {
      transform: translateX(-2px);
      background: rgba(255, 255, 255, 0.85);
    }
  }
}

.page-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  animation: fadeInDown 0.6s vars.$ease-spring;
  gap: 16px;
  
  .header-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    background: linear-gradient(135deg, vars.$primary-color, color.adjust(vars.$primary-color, $lightness: 15%));
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);

    .el-icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .header-content {
    flex: 1;
    .title {
      font-size: 24px;
      font-weight: 700;
      color: vars.$text-main-color;
      margin: 0 0 4px 0;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-main-color, vars.$primary-color));
    }

    .subtitle {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

.glass-card {
  @include mixins.glass-effect;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  animation: fadeInUp 0.6s vars.$ease-spring;
  animation-fill-mode: both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
