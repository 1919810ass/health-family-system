<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Document /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">上传报告</h2>
        <p class="subtitle">上传医疗报告图片，获取 AI 智能解读</p>
      </div>
    </div>

    <el-card class="glass-card">
      <el-form :model="form" label-width="100px">
        <el-form-item label="报告名称">
            <el-input v-model="form.reportName" placeholder="例如：2023年度体检报告" />
        </el-form-item>
        <el-form-item label="报告图片">
            <el-upload
              class="report-uploader"
              action="#"
              :http-request="handleUpload"
              :show-file-list="false"
              :before-upload="beforeUpload"
            >
              <img v-if="form.imageUrl" :src="form.imageUrl" class="report-image" />
              <el-icon v-else class="report-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="mt-2 text-gray-400 text-sm">支持 JPG/PNG 格式，大小不超过 5MB</div>
            <div class="mt-2">
                <el-button size="small" @click="fillDemoImage">使用示例图片</el-button>
            </div>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交分析</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="reportDetail" class="glass-card mt-6">
      <template #header>
        <div class="font-bold flex justify-between items-center">
          <span>智能解读结果</span>
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
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { submitReport, uploadReportImage, getReportDetail, getReportStatus } from '@/api/report'
import { ElMessage } from 'element-plus'
import { Plus, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const submitting = ref(false)
const form = ref({
    reportName: '',
    reportType: 'LAB_REPORT',
    imageUrl: ''
})
const reportDetail = ref(null)
const statusValue = computed(() => (reportDetail.value?.status || '').toString().trim().toUpperCase())
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

const ocrData = computed(() => {
    if (!reportDetail.value || !reportDetail.value.ocrData) return null
    try {
        return JSON.parse(reportDetail.value.ocrData)
    } catch (e) {
        return null
    }
})

const interpretation = computed(() => {
    if (!reportDetail.value || !reportDetail.value.interpretation) return null
    try {
        return JSON.parse(reportDetail.value.interpretation)
    } catch (e) {
        return null
    }
})

const ocrItems = computed(() => {
    if (!ocrData.value || !ocrData.value.items) return []
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

watch([statusValue, interpretation, ocrItems], ([status]) => {
    if (status === 'COMPLETED' && interpretation.value) {
        startStream()
    } else {
        stopStream()
    }
})

onUnmounted(() => {
    stopStream()
    stopProgress()
    if (pollInterval) {
        clearInterval(pollInterval)
        pollInterval = null
    }
})

const beforeUpload = (file) => {
    const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
    const isLt5M = file.size / 1024 / 1024 < 5

    if (!isJPG) {
        ElMessage.error('上传图片只能是 JPG/PNG 格式!')
    }
    if (!isLt5M) {
        ElMessage.error('上传图片大小不能超过 5MB!')
    }
    return isJPG && isLt5M
}

const handleUpload = async (options) => {
    try {
        const res = await uploadReportImage(options.file)
        const isResult = res && typeof res === 'object' && 'code' in res
        if (isResult && res.code !== 0) {
            ElMessage.error(res.message || '图片上传失败')
            return
        }
        const data = res?.data ?? res
        if (!data) {
            ElMessage.error('图片上传失败')
            return
        }
        form.value.imageUrl = data
        ElMessage.success('图片上传成功')
    } catch (error) {
        console.error('上传出错', error)
        ElMessage.error('图片上传失败')
    }
}

const fillDemoImage = () => {
    form.value.imageUrl = 'https://img.freepik.com/free-photo/medical-report-test-chart_53876-13398.jpg' // Sample image
}

const handleSubmit = async () => {
    if (!form.value.imageUrl) {
        ElMessage.warning('请提供图片')
        return
    }
    submitting.value = true
    try {
        const userId = userStore.profile?.id
        const res = await submitReport(form.value, userId)
        const isResult = res && typeof res === 'object' && 'code' in res
        if (isResult && res.code !== 0) {
            ElMessage.error(res.message || '提交失败')
            submitting.value = false
            return
        }
        const data = res?.data ?? res
        if (!data || !data.id) {
            ElMessage.error('提交失败')
            submitting.value = false
            return
        }
        
        const reportId = data.id
        reportDetail.value = { ...data, status: 'PROCESSING' }
        progressStageText.value = 'AI正在努力分析您的报告，请稍候...'
        startProgress()
        // 轮询检查状态
        if (pollInterval) {
            clearInterval(pollInterval)
        }
        pollInterval = setInterval(async () => {
            try {
                const statusRes = await getReportStatus(reportId, userId)
                const statusData = statusRes?.data ?? statusRes
                const status = normalizeStatus(statusData?.status)
                if (typeof statusData?.progressPercent === 'number') {
                    progressValue.value = statusData.progressPercent
                }
                if (statusData?.progressStage) {
                    progressStageText.value = statusData.progressStage
                }
                if (status === 'COMPLETED') {
                    const detailRes = await getReportDetail(reportId, userId)
                    const detail = detailRes?.data ?? detailRes
                    clearInterval(pollInterval)
                    pollInterval = null
                    ElMessage.success('分析完成')
                    stopProgress(100)
                    reportDetail.value = { ...detail, status: status || detail?.status }
                    submitting.value = false
                } else if (status === 'FAILED') {
                    const detailRes = await getReportDetail(reportId, userId)
                    const detail = detailRes?.data ?? detailRes
                    clearInterval(pollInterval)
                    pollInterval = null
                    ElMessage.error('分析失败，请重试')
                    stopProgress(0)
                    submitting.value = false
                    reportDetail.value = { ...detail, status: status || detail?.status }
                }
            } catch (e) {
                // ignore polling errors
            }
        }, 2000)

        // 设置超时保护 (例如 60秒)
        setTimeout(() => {
            if (statusValue.value !== 'COMPLETED' && statusValue.value !== 'FAILED') {
                ElMessage.warning('分析时间较长，结果会自动刷新，请稍候')
                submitting.value = false
            }
        }, 60000)

    } catch (error) {
        ElMessage.error(error?.message || '提交失败')
        submitting.value = false
        stopProgress(0)
    }
}
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

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

.report-uploader {
    :deep(.el-upload) {
      border: 2px dashed var(--el-border-color-lighter);
      border-radius: 12px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
      width: 100%;
      min-height: 300px;
      display: flex;
      justify-content: center;
      align-items: center;
      background-color: #fafafa;

      &:hover {
        border-color: var(--el-color-primary);
        background-color: rgba(var(--el-color-primary-rgb), 0.02);
      }
      
      .el-upload-dragger {
        width: 100%;
        height: 100%;
        border: none;
        background: transparent;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 40px;
      }
    }
  }

  .uploader-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: vars.$text-secondary-color;
  }

  .report-uploader-icon {
    font-size: 48px;
    color: #cbd5e1;
    margin-bottom: 16px;
    transition: all 0.3s;
  }

  .report-uploader:hover .report-uploader-icon {
      color: vars.$primary-color;
      transform: scale(1.1);
  }
  
  .uploader-text {
      font-size: 14px;
      line-height: 1.6;
      text-align: center;
      
      .highlight {
          color: vars.$primary-color;
          font-weight: 500;
      }
      
      .sub-text {
          font-size: 12px;
          color: #9ca3af;
          margin-top: 8px;
      }
  }

  .report-image {
    max-width: 100%;
    max-height: 500px;
    display: block;
    object-fit: contain;
  }
  
  .pill-input {
      :deep(.el-input__wrapper) {
          border-radius: 20px;
          background-color: #f5f7fa;
          box-shadow: none;
          padding-left: 16px;
          border: 1px solid transparent;
          transition: all 0.3s;
          
          &:hover, &.is-focus {
              background-color: #fff;
              border-color: vars.$primary-color;
              box-shadow: 0 0 0 1px vars.$primary-color inset;
          }
      }
  }
  
  .custom-label {
      :deep(.el-form-item__label) {
          font-weight: 600;
          color: vars.$text-main-color;
          margin-bottom: 8px;
      }
  }
  
  .submit-btn {
      padding-left: 40px;
      padding-right: 40px;
      font-weight: 600;
      box-shadow: 0 4px 14px rgba(vars.$primary-color, 0.3);
      transition: all 0.3s;
      
      &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(vars.$primary-color, 0.4);
      }
  }
  
  .mt-4 { margin-top: 16px; }
  .mt-8 { margin-top: 32px; }
</style>
