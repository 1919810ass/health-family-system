<template>
  <div class="system-backup">
    <!-- Header -->
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">数据备份</h1>
        <p class="page-subtitle">管理系统数据备份与恢复，确保数据安全</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="large" round @click="handleCreateBackup">
          <el-icon class="mr-2"><Plus /></el-icon> 新建备份
        </el-button>
        <el-upload
          class="upload-demo inline-block ml-3"
          action="/api/system/backup/upload"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :before-upload="beforeUpload"
        >
          <el-button size="large" round plain>
            <el-icon class="mr-2"><Upload /></el-icon> 上传备份
          </el-button>
        </el-upload>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-row stagger-anim" style="--delay: 0.2s">
      <div class="stat-card glass-panel">
        <div class="stat-icon primary">
          <el-icon><Files /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ backupList.length }}</div>
          <div class="stat-label">备份文件总数</div>
        </div>
      </div>
      <div class="stat-card glass-panel">
        <div class="stat-icon success">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ lastBackupTime }}</div>
          <div class="stat-label">最近备份时间</div>
        </div>
      </div>
      <div class="stat-card glass-panel">
        <div class="stat-icon warning">
          <el-icon><DataLine /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ totalSize }}</div>
          <div class="stat-label">占用存储空间</div>
        </div>
      </div>
    </div>

    <!-- Backup List -->
    <div class="content-section stagger-anim" style="--delay: 0.3s">
      <div class="glass-container">
        <div class="table-toolbar">
          <div class="left">
            <el-input
              v-model="searchQuery"
              placeholder="搜索备份文件..."
              prefix-icon="Search"
              clearable
              class="search-input"
            />
            <el-select v-model="filterType" placeholder="备份类型" clearable class="filter-select">
              <el-option label="自动备份" value="auto" />
              <el-option label="手动备份" value="manual" />
            </el-select>
          </div>
          <div class="right">
            <el-button circle plain @click="refreshList">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>

        <el-table :data="filteredList" style="width: 100%" v-loading="loading">
          <el-table-column prop="filename" label="备份文件名称" min-width="250">
            <template #default="{ row }">
              <div class="filename-cell">
                <el-icon class="file-icon"><Document /></el-icon>
                <span>{{ row.filename }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="size" label="文件大小" width="120" />
          <el-table-column prop="type" label="备份类型" width="120">
            <template #default="{ row }">
              <el-tag :type="row.type === 'auto' ? 'info' : 'primary'" effect="light" round>
                {{ row.type === 'auto' ? '自动备份' : '手动备份' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" sortable />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'success' ? 'success' : 'danger'" effect="plain" round size="small">
                {{ row.status === 'success' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleDownload(row)">
                下载
              </el-button>
              <el-button link type="warning" @click="handleRestore(row)">
                恢复
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- Create Backup Dialog -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建备份"
      width="500px"
      class="glass-dialog"
      append-to-body
    >
      <el-form :model="createForm" label-position="top">
        <el-form-item label="备注信息">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备份备注信息（可选）"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="createForm.includeFiles">包含上传的文件（图片、文档等）</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="creating" @click="submitCreateBackup">
            开始备份
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Upload, Refresh, Search, Document,
  Files, Timer, DataLine
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// State
const loading = ref(false)
const searchQuery = ref('')
const filterType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const createDialogVisible = ref(false)
const creating = ref(false)
const createForm = ref({
  remark: '',
  includeFiles: true
})

// Mock Data
const backupList = ref([
  { id: 1, filename: 'backup-20260114-120000.sql', size: '15.2 MB', type: 'auto', createTime: '2026-01-14 12:00:00', status: 'success' },
  { id: 2, filename: 'backup-20260113-120000.sql', size: '15.1 MB', type: 'auto', createTime: '2026-01-13 12:00:00', status: 'success' },
  { id: 3, filename: 'manual-backup-v2.0.sql', size: '15.0 MB', type: 'manual', createTime: '2026-01-12 15:30:00', status: 'success' },
  { id: 4, filename: 'backup-20260112-120000.sql', size: '14.8 MB', type: 'auto', createTime: '2026-01-12 12:00:00', status: 'success' },
  { id: 5, filename: 'backup-20260111-120000.sql', size: '14.5 MB', type: 'auto', createTime: '2026-01-11 12:00:00', status: 'success' },
])

// Computed
const filteredList = computed(() => {
  let result = backupList.value
  
  if (searchQuery.value) {
    result = result.filter(item => item.filename.toLowerCase().includes(searchQuery.value.toLowerCase()))
  }
  
  if (filterType.value) {
    result = result.filter(item => item.type === filterType.value)
  }
  
  // Pagination logic (simple client-side for demo)
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

const lastBackupTime = computed(() => {
  if (backupList.value.length === 0) return '-'
  return backupList.value[0].createTime
})

const totalSize = computed(() => {
  // Simple mock calculation
  return '74.6 MB'
})

// Lifecycle
onMounted(() => {
  total.value = backupList.value.length
  // In real app, fetch data here
})

// Actions
const refreshList = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

const handleCreateBackup = () => {
  createForm.value = { remark: '', includeFiles: true }
  createDialogVisible.value = true
}

const submitCreateBackup = () => {
  creating.value = true
  setTimeout(() => {
    creating.value = false
    createDialogVisible.value = false
    const newBackup = {
      id: Date.now(),
      filename: `manual-backup-${dayjs().format('YYYYMMDD-HHmmss')}.sql`,
      size: '15.3 MB',
      type: 'manual',
      createTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
      status: 'success'
    }
    backupList.value.unshift(newBackup)
    total.value = backupList.value.length
    ElMessage.success('备份创建成功')
  }, 1500)
}

const handleUploadSuccess = () => {
  ElMessage.success('备份文件上传成功')
  refreshList()
}

const beforeUpload = (file) => {
  const isValidType = file.name.endsWith('.sql') || file.name.endsWith('.zip')
  if (!isValidType) {
    ElMessage.error('只能上传 .sql 或 .zip 格式的备份文件')
  }
  return isValidType
}

const handleDownload = (row) => {
  ElMessage.success(`开始下载: ${row.filename}`)
  // Implement actual download logic
}

const handleRestore = (row) => {
  ElMessageBox.confirm(
    `确定要将系统恢复到备份点 [${row.filename}] 吗？恢复过程中系统将无法访问。`,
    '系统恢复确认',
    {
      confirmButtonText: '确定恢复',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    const loadingInstance = ElMessage.loading({
      message: '正在恢复系统数据，请勿关闭页面...',
      duration: 0
    })
    setTimeout(() => {
      loadingInstance.close()
      ElMessage.success('系统恢复成功，即将重新登录')
      // Redirect to login or reload
    }, 2000)
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该备份文件吗？此操作无法撤销。',
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'danger',
    }
  ).then(() => {
    backupList.value = backupList.value.filter(item => item.id !== row.id)
    total.value = backupList.value.length
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleSizeChange = (val) => {
  pageSize.value = val
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}
</script>

<style scoped lang="scss">
@use 'sass:map';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.system-backup {
  min-height: 100%;
  padding-bottom: 2rem;

  .page-header {
    margin-bottom: 2rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-content {
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

    .header-actions {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
  }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
    margin-bottom: 2rem;

    .stat-card {
      @include mixins.glass-effect;
      padding: 1.5rem;
      border-radius: 16px;
      display: flex;
      align-items: center;
      gap: 1.5rem;
      transition: transform 0.3s ease;

      &:hover {
        transform: translateY(-4px);
      }

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.8rem;

        &.primary {
          background: rgba(map.get(vars.$colors, 'primary'), 0.1);
          color: map.get(vars.$colors, 'primary');
        }
        &.success {
          background: rgba(map.get(vars.$colors, 'success'), 0.1);
          color: map.get(vars.$colors, 'success');
        }
        &.warning {
          background: rgba(map.get(vars.$colors, 'warning'), 0.1);
          color: map.get(vars.$colors, 'warning');
        }
      }

      .stat-info {
        .stat-value {
          font-size: 1.8rem;
          font-weight: 700;
          color: map.get(vars.$colors, 'text-main');
          line-height: 1.2;
        }
        .stat-label {
          color: map.get(vars.$colors, 'text-secondary');
          font-size: 0.9rem;
          margin-top: 0.25rem;
        }
      }
    }
  }

  .content-section {
    .glass-container {
      @include mixins.glass-effect;
      border-radius: 16px;
      padding: 1.5rem;
      border: 1px solid rgba(255, 255, 255, 0.3);

      .table-toolbar {
        display: flex;
        justify-content: space-between;
        margin-bottom: 1.5rem;

        .left {
          display: flex;
          gap: 1rem;

          .search-input {
            width: 240px;
          }
          .filter-select {
            width: 160px;
          }
        }
      }

      .filename-cell {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-weight: 500;

        .file-icon {
          color: map.get(vars.$colors, 'primary');
          font-size: 1.2rem;
        }
      }

      .pagination-wrapper {
        margin-top: 1.5rem;
        display: flex;
        justify-content: flex-end;
      }
    }
  }

  // Animation
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

  // Responsive
  @media (max-width: 1024px) {
    .stats-row {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 768px) {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
      
      .header-actions {
        width: 100%;
        justify-content: space-between;
      }
    }

    .stats-row {
      grid-template-columns: 1fr;
    }

    .table-toolbar {
      flex-direction: column;
      gap: 1rem;
      
      .left {
        flex-direction: column;
        width: 100%;
        
        .search-input, .filter-select {
          width: 100%;
        }
      }
    }
  }
}
</style>