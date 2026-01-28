<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Document /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">智能报告解读</h2>
        <p class="subtitle">AI 深度分析医疗报告，提供专业解读与建议</p>
      </div>
    </div>

    <el-card class="glass-card">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-bold">我的报告</h3>
        <el-button type="primary" @click="$router.push('/report/upload')">上传报告</el-button>
      </div>

      <el-table :data="reports" v-loading="loading" style="width: 100%">
        <el-table-column prop="reportName" label="报告名称" />
        <el-table-column prop="reportType" label="类型">
            <template #default="scope">
                {{ formatReportType(scope.row.reportType) }}
            </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间">
            <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
            </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
            <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="$router.push(`/report/detail/${scope.row.id}`)">查看解读</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { getUserReports } from '@/api/report'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const reports = ref([])
const loading = ref(false)

const loadReports = async () => {
  loading.value = true
  try {
    // 显式传入当前登录用户的ID，避免使用测试账号ID
    const userId = userStore.profile?.id
    const res = await getUserReports(userId)
    const isResult = res && typeof res === 'object' && 'code' in res
    if (isResult && res.code !== 0) {
      reports.value = []
      return
    }
    const data = res?.data ?? res
    reports.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

const formatReportType = (type) => {
    const map = {
        'LAB_REPORT': '化验单',
        'EXAM_REPORT': '体检报告',
        'PRESCRIPTION': '处方单',
        'OTHER': '其他'
    }
    return map[type] || type
}

const formatDate = (date) => {
    return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getStatusType = (status) => {
    const map = {
        'PENDING': 'info',
        'PROCESSING': 'warning',
        'COMPLETED': 'success',
        'FAILED': 'danger'
    }
    return map[status] || 'info'
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

onMounted(() => {
  loadReports()
})
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

.mb-24 {
  margin-bottom: 24px;
}
</style>
