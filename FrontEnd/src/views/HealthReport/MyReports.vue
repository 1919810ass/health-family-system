<template>
  <div class="my-reports-page">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Document /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title-text">我的健康报告</h2>
        <p class="subtitle-text">查看和管理您的个人健康报告</p>
      </div>
    </div>
    
    <div class="reports-container" v-loading="loading">
      <el-empty v-if="!loading && reports.length === 0" description="暂无健康报告" />
      
      <div v-else class="report-list">
        <el-card 
          v-for="report in reports" 
          :key="report.id" 
          class="report-card"
          shadow="hover"
          @click="viewReport(report)"
        >
          <div class="report-header">
            <h3 class="report-title">{{ report.title }}</h3>
            <el-tag :type="report.isRead ? 'info' : 'danger'" size="small">
              {{ report.isRead ? '已读' : '未读' }}
            </el-tag>
          </div>
          <div class="report-meta">
            <span class="date">{{ formatDate(report.createdAt) }}</span>
            <span class="doctor">医生: {{ report.doctorName || report.doctorId }}</span>
          </div>
          <div class="report-diagnosis text-ellipsis">
            诊断意见: {{ report.diagnosis }}
          </div>
        </el-card>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="报告详情"
      width="800px"
      fullscreen
      class="report-detail-dialog"
    >
      <div v-if="currentReport" class="report-detail">
        <div class="detail-header">
          <h2>{{ currentReport.title }}</h2>
          <div class="detail-meta">
            <span>生成时间: {{ formatDate(currentReport.createdAt) }}</span>
            <span>医生: {{ currentReport.doctorName || currentReport.doctorId }}</span>
          </div>
        </div>
        
        <el-divider content-position="left">诊断意见</el-divider>
        <div class="diagnosis-section">
          {{ currentReport.diagnosis }}
        </div>

        <el-divider content-position="left">报告正文</el-divider>
        <div class="content-section markdown-body" v-html="renderMarkdown(currentReport.content)"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyDoctorReports, getMyDoctorReportDetail } from '@/api/report'
import dayjs from 'dayjs'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { Document } from '@element-plus/icons-vue'

const loading = ref(false)
const reports = ref([])
const dialogVisible = ref(false)
const currentReport = ref(null)

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const loadReports = async () => {
  loading.value = true
  try {
    const res = await getMyDoctorReports()
    reports.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const viewReport = async (report) => {
  try {
    const res = await getMyDoctorReportDetail(report.id)
    currentReport.value = res.data
    dialogVisible.value = true
    // Update read status locally
    report.isRead = true
  } catch (e) {
    console.error(e)
  }
}

const renderMarkdown = (content) => {
  if (!content) return ''
  return DOMPurify.sanitize(marked.parse(content))
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.my-reports-page {
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
    
    .title-text {
      font-size: 24px;
      font-weight: 700;
      color: vars.$text-primary-color;
      margin: 0 0 4px 0;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-primary-color, vars.$primary-color));
    }
    
    .subtitle-text {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

.reports-container {
  margin-top: 20px;
}

.report-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.report-card {
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-5px);
  }
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.report-title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
}

.report-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
}

.report-diagnosis {
  font-size: 14px;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.report-detail {
  padding: 20px;
}

.detail-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    margin-bottom: 10px;
  }
  
  .detail-meta {
    color: #666;
    font-size: 14px;
    display: flex;
    justify-content: center;
    gap: 20px;
  }
}

.diagnosis-section {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.content-section {
  line-height: 1.8;
  font-size: 15px;
}
</style>
