<template>
  <div class="system-audit">
    <!-- Header -->
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">操作审计</h1>
        <p class="page-subtitle">查看系统操作日志，追踪用户行为与敏感操作</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleSearch">
          <el-icon class="mr-2"><Refresh /></el-icon> 刷新
        </el-button>
        <el-button @click="handleExport">
          <el-icon class="mr-2"><Download /></el-icon> 导出
        </el-button>
      </div>
    </div>

    <!-- Search Form -->
    <div class="filter-card stagger-anim" style="--delay: 0.2s">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="操作/资源/详情" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="queryParams.userId" placeholder="用户ID" clearable @keyup.enter="handleSearch" style="width: 120px" />
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select v-model="queryParams.result" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILURE" />
            <el-option label="未知" value="UNKNOWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="敏感度">
          <el-select v-model="queryParams.sensitivity" placeholder="全部" clearable style="width: 120px">
            <el-option label="普通" value="NORMAL" />
            <el-option label="高" value="HIGH" />
            <el-option label="极高" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :default-time="defaultTime"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Data Table -->
    <div class="table-card stagger-anim" style="--delay: 0.3s">
      <el-table
        v-loading="loading"
        :data="logList"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="用户" min-width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="username">{{ row.username || 'Unknown' }}</span>
              <el-tag size="small" type="info" v-if="row.userRole">{{ row.userRole }}</el-tag>
              <span class="user-id" v-if="row.userId">(ID: {{ row.userId }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="action" label="动作" width="150">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.action)">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resource" label="资源" width="150" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" effect="dark">
              {{ row.result }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sensitivityLevel" label="敏感度" width="100">
          <template #default="{ row }">
            <el-tag :type="getSensitivityTagType(row.sensitivityLevel)" effect="plain">
              {{ row.sensitivityLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
          background
        />
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="审计日志详情"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentLog" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ formatTime(currentLog.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="操作用户">{{ currentLog.username }} (ID: {{ currentLog.userId }})</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
          <el-descriptions-item label="User Agent">{{ currentLog.userAgent }}</el-descriptions-item>
          <el-descriptions-item label="操作">{{ currentLog.action }}</el-descriptions-item>
          <el-descriptions-item label="资源">{{ currentLog.resource }}</el-descriptions-item>
          <el-descriptions-item label="结果">
            <el-tag :type="currentLog.result === 'SUCCESS' ? 'success' : 'danger'">{{ currentLog.result }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="json-viewer mt-4">
          <h4>扩展信息 (Extra JSON)</h4>
          <pre class="json-pre">{{ formatJson(currentLog.extraJson) }}</pre>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogs } from '@/api/admin'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { Refresh, Download } from '@element-plus/icons-vue'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const currentLog = ref(null)

const defaultTime = [
  new Date(2000, 1, 1, 0, 0, 0),
  new Date(2000, 1, 1, 23, 59, 59),
]

const queryParams = reactive({
  page: 1,
  size: 20,
  keyword: '',
  userId: '',
  result: '',
  sensitivity: '',
  startTime: '',
  endTime: ''
})

const handleSearch = async () => {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }

    const res = await getAuditLogs(queryParams)
    if (res.data) {
      logList.value = res.data.content
      total.value = res.data.totalElements
    }
  } catch (error) {
    console.error('Failed to fetch audit logs:', error)
    ElMessage.error('获取审计日志失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.userId = ''
  queryParams.result = ''
  queryParams.sensitivity = ''
  dateRange.value = []
  queryParams.page = 1
  handleSearch()
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const showDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const formatJson = (jsonStr) => {
  if (!jsonStr) return '无'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

const getActionTagType = (action) => {
  if (action.includes('DELETE')) return 'danger'
  if (action.includes('UPDATE') || action.includes('RESET')) return 'warning'
  if (action.includes('CREATE') || action.includes('REGISTER')) return 'success'
  return ''
}

const getSensitivityTagType = (level) => {
  if (level === 'CRITICAL') return 'danger'
  if (level === 'HIGH') return 'warning'
  return 'info'
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
@use 'sass:map';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.system-audit {
  min-height: 100%;
  padding-bottom: 2rem;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 2rem;
    
    .page-title {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
      @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info')));
    }
    
    .page-subtitle {
      color: map.get(vars.$colors, 'text-secondary');
      font-size: 1rem;
    }
  }

  .filter-card {
    background: map.get(vars.$colors, 'bg-card');
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 1.5rem;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  }

  .table-card {
    background: map.get(vars.$colors, 'bg-card');
    border-radius: 12px;
    padding: 1.5rem;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  }

  .pagination-container {
    margin-top: 1.5rem;
    display: flex;
    justify-content: flex-end;
  }

  .user-cell {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .username {
      font-weight: 500;
    }
    
    .user-id {
      font-size: 12px;
      color: map.get(vars.$colors, 'text-secondary');
    }
  }

  .json-pre {
    background: #f5f7fa;
    padding: 1rem;
    border-radius: 4px;
    overflow-x: auto;
    font-family: monospace;
    font-size: 12px;
    white-space: pre-wrap;
    word-wrap: break-word;
  }

  .stagger-anim {
    opacity: 0;
    animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    animation-delay: var(--delay, 0s);
  }

  @keyframes slideUpFade {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}
</style>
