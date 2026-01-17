<template>
  <div class="health-logs-management">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">健康日志管理</h1>
        <p class="page-subtitle">监控全平台用户健康数据录入情况</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" round v-particles @click="exportLogs">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="glass-card mb-24 stagger-anim" style="--delay: 0.2s">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable>
             <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="家庭ID">
          <el-input v-model="searchForm.familyId" placeholder="请输入家庭ID" clearable>
             <template #prefix><el-icon><House /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="日志类型">
          <el-select v-model="searchForm.type" placeholder="请选择日志类型" clearable>
            <template #prefix><el-icon><DataAnalysis /></el-icon></template>
            <el-option label="饮食" value="diet"><span style="float: left">饮食</span><el-icon style="float: right; color: #E6A23C"><Food /></el-icon></el-option>
            <el-option label="睡眠" value="sleep"><span style="float: left">睡眠</span><el-icon style="float: right; color: #409EFF"><Moon /></el-icon></el-option>
            <el-option label="运动" value="exercise"><span style="float: left">运动</span><el-icon style="float: right; color: #67C23A"><Bicycle /></el-icon></el-option>
            <el-option label="情绪" value="mood"><span style="float: left">情绪</span><el-icon style="float: right; color: #E91E63"><Sunny /></el-icon></el-option>
            <el-option label="体征" value="vital_signs"><span style="float: left">体征</span><el-icon style="float: right; color: #F56C6C"><FirstAidKit /></el-icon></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round v-particles @click="searchLogs">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button round @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards mb-24 stagger-anim" style="--delay: 0.3s">
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper blue">
          <el-icon><Tickets /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalLogs }}</div>
          <div class="stat-title">总日志数</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper green">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeUsers }}</div>
          <div class="stat-title">活跃用户</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper orange">
          <el-icon><House /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeFamilies }}</div>
          <div class="stat-title">活跃家庭</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper red">
          <el-icon><DataAnalysis /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayNew }}</div>
          <div class="stat-title">今日新增</div>
        </div>
      </div>
    </div>

    <!-- 日志列表 -->
    <div class="glass-card stagger-anim" style="--delay: 0.4s">
      <div class="card-header">
        <span>健康日志列表</span>
        <div class="header-actions">
          <el-button circle @click="refreshList" :loading="loading">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>

      <el-table 
        :data="logs" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100">
           <template #default="{ row }">
              <el-tag size="small" effect="plain" type="info">{{ row.userId }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="familyId" label="家庭ID" width="100">
           <template #default="{ row }">
              <el-tag size="small" effect="plain" type="info">{{ row.familyId || '-' }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getLogTypeTagType(row.type)" effect="light" round class="icon-tag">
              <el-icon class="mr-1">
                <component :is="getLogTypeIcon(row.type)" />
              </el-icon>
              {{ getLogTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容摘要" show-overflow-tooltip min-width="200" />
        <el-table-column prop="date" label="日期" width="120" sortable />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" round @click="showDetailDialog(row)">详情</el-button>
            <el-button size="small" type="danger" round @click="deleteLog(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>

    <!-- 日志详情对话框 -->
    <el-dialog title="日志详情" v-model="detailDialog.visible" width="60%" class="glass-dialog">
      <el-descriptions :column="2" border class="glass-descriptions">
        <el-descriptions-item label="日志ID">{{ detailDialog.log.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailDialog.log.userId }}</el-descriptions-item>
        <el-descriptions-item label="家庭ID">{{ detailDialog.log.familyId }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getLogTypeTagType(detailDialog.log.type)" effect="dark" round>
            {{ getLogTypeName(detailDialog.log.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="日期">{{ detailDialog.log.date }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailDialog.log.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailDialog.log.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div class="log-content-box">{{ detailDialog.log.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog.visible = false" round>关闭</el-button>
        <el-button type="primary" @click="editLog" round>编辑</el-button>
        <el-button type="danger" @click="deleteLog(detailDialog.log)" round>删除</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑日志" v-model="editDialog.visible" width="50%" class="glass-dialog">
      <el-form :model="editDialog.form" :rules="editDialog.rules" ref="editFormRef" label-width="100px" class="custom-form">
        <el-form-item label="类型" prop="type">
          <el-select v-model="editDialog.form.type" placeholder="请选择日志类型" style="width: 100%">
            <template #prefix><el-icon><DataAnalysis /></el-icon></template>
            <el-option label="饮食" value="diet"><span style="float: left">饮食</span><el-icon style="float: right; color: #E6A23C"><Food /></el-icon></el-option>
            <el-option label="睡眠" value="sleep"><span style="float: left">睡眠</span><el-icon style="float: right; color: #409EFF"><Moon /></el-icon></el-option>
            <el-option label="运动" value="exercise"><span style="float: left">运动</span><el-icon style="float: right; color: #67C23A"><Bicycle /></el-icon></el-option>
            <el-option label="情绪" value="mood"><span style="float: left">情绪</span><el-icon style="float: right; color: #E91E63"><Sunny /></el-icon></el-option>
            <el-option label="体征" value="vital_signs"><span style="float: left">体征</span><el-icon style="float: right; color: #F56C6C"><FirstAidKit /></el-icon></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="editDialog.form.date"
            type="date"
            placeholder="请选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="editDialog.form.content" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入日志内容" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false" round>取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editDialog.submitting" round v-particles>确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Download, Refresh, Tickets, User, House, DataAnalysis, 
  Edit, Delete, Search, Food, Moon, Bicycle, Sunny, FirstAidKit
} from '@element-plus/icons-vue'
import { getHealthLogs, getHealthLogStats, updateHealthLog, deleteHealthLog } from '../../../api/admin'

// 搜索表单
const searchForm = reactive({
  userId: '',
  familyId: '',
  type: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 统计信息
const stats = reactive({
  totalLogs: 0,
  activeUsers: 0,
  activeFamilies: 0,
  todayNew: 0
})

// 日志列表
const logs = ref([])
const loading = ref(false)
const selectedLogs = ref([])

// 详情对话框
const detailDialog = reactive({
  visible: false,
  log: {}
})

// 编辑对话框
const editDialog = reactive({
  visible: false,
  submitting: false,
  form: {
    id: null,
    type: '',
    date: '',
    content: ''
  },
  rules: {
    type: [
      { required: true, message: '请选择日志类型', trigger: 'change' }
    ],
    date: [
      { required: true, message: '请选择日期', trigger: 'change' }
    ],
    content: [
      { required: true, message: '请输入日志内容', trigger: 'blur' },
      { min: 5, max: 1000, message: '内容长度在5-1000个字符', trigger: 'blur' }
    ]
  }
})

// 表单引用
const editFormRef = ref(null)

// 加载健康日志数据
onMounted(() => {
  loadLogs()
  loadStats()
})

// 加载日志列表（兼容后端 Result<T> 包装结构）
const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }

    const res = await getHealthLogs(params)
    const data = res && typeof res === 'object' ? res.data || {} : {}

    const list = Array.isArray(data.list)
      ? data.list
      : Array.isArray(data.items)
        ? data.items
        : []

    logs.value = list
    pagination.total = Number(data.total) || list.length || 0
  } catch (error) {
    ElMessage.error('加载健康日志失败')
    console.error('Error loading health logs:', error)
  } finally {
    loading.value = false
  }
}

// 加载统计信息（兼容后端 Result<T> 包装结构）
const loadStats = async () => {
  try {
    const res = await getHealthLogStats()
    const data = res && typeof res === 'object' ? res.data || {} : {}
    stats.totalLogs = Number(data.totalLogs) || 0
    stats.activeUsers = Number(data.activeUsers) || 0
    stats.activeFamilies = Number(data.activeFamilies) || 0
    stats.todayNew = Number(data.todayNew) || 0
  } catch (error) {
    ElMessage.error('加载统计信息失败')
    console.error('Error loading stats:', error)
  }
}

// 搜索日志
const searchLogs = () => {
  pagination.page = 1
  loadLogs()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  loadLogs()
}

// 刷新列表
const refreshList = () => {
  loadLogs()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  loadLogs()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadLogs()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedLogs.value = selection
}

// 导出日志
const exportLogs = () => {
  ElMessage.success('开始导出健康日志数据')
  // 这里应该实现实际的导出逻辑
}

// 显示详情对话框
const showDetailDialog = (log) => {
  detailDialog.log = { ...log }
  detailDialog.visible = true
}

// 编辑日志
const editLog = () => {
  editDialog.form.id = detailDialog.log.id
  editDialog.form.type = detailDialog.log.type
  editDialog.form.date = detailDialog.log.date
  editDialog.form.content = detailDialog.log.content
  editDialog.visible = true
  detailDialog.visible = false
}

// 提交编辑
const submitEdit = async () => {
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editDialog.submitting = true
      try {
        await updateHealthLog(editDialog.form.id, editDialog.form)
        ElMessage.success('日志更新成功')
        editDialog.visible = false
        loadLogs()
      } catch (error) {
        ElMessage.error('更新日志失败')
        console.error('Error updating health log:', error)
      } finally {
        editDialog.submitting = false
      }
    }
  })
}

// 删除日志
const deleteLog = async (log) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除日志ID为 ${log.id} 的健康日志吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteHealthLog(log.id)
    const index = logs.value.findIndex(item => item.id === log.id)
    if (index > -1) {
      logs.value.splice(index, 1)
    }
    ElMessage.success('日志删除成功')
  } catch {
    // 用户取消操作
  }
}

// 获取日志类型标签类型
const getLogTypeTagType = (type) => {
  switch (type) {
    case 'diet': return 'warning'
    case 'sleep': return 'info'
    case 'exercise': return 'success'
    case 'mood': return 'primary'
    case 'vital_signs': return 'danger'
    default: return 'info'
  }
}

// 获取日志类型名称
const getLogTypeName = (type) => {
  const typeMap = {
    diet: '饮食',
    sleep: '睡眠',
    exercise: '运动',
    mood: '情绪',
    vital_signs: '体征'
  }
  return typeMap[type] || type
}

// 获取日志类型图标
const getLogTypeIcon = (type) => {
  const iconMap = {
    diet: Food,
    sleep: Moon,
    exercise: Bicycle,
    mood: Sunny,
    vital_signs: FirstAidKit
  }
  return iconMap[type] || DataAnalysis
}
</script>

<style scoped lang="scss">
@use "sass:map";
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.health-logs-management {
  // Page Header
  .page-header {
    margin-bottom: 32px;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    flex-wrap: wrap;
    gap: 20px;

    .header-content {
      .page-title {
        font-size: 28px;
        font-weight: 800;
        margin: 0 0 8px 0;
        @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info')));
        letter-spacing: -0.5px;
      }

      .page-subtitle {
        font-size: 14px;
        color: map.get(vars.$colors, 'text-secondary');
        margin: 0;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  // Search Form
  .search-form {
    padding: 24px;
    
    :deep(.el-form-item) {
      margin-bottom: 16px;
      margin-right: 24px;

      &:last-child {
        margin-right: 0;
      }
    }
  }

  // Stats Cards
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 24px;

    .stat-card {
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 20px;
      transition: all 0.3s vars.$ease-spring;

      &:hover {
        transform: translateY(-5px);
        box-shadow: vars.$shadow-lg;
      }

      .stat-icon-wrapper {
        width: 64px;
        height: 64px;
        border-radius: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: white;
        box-shadow: vars.$shadow-sm;

        &.blue { background: linear-gradient(135deg, #409EFF, #36D1DC); }
        &.green { background: linear-gradient(135deg, #67C23A, #81FBB8); }
        &.orange { background: linear-gradient(135deg, #E6A23C, #FDC830); }
        &.red { background: linear-gradient(135deg, #F56C6C, #FF416C); }
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 32px;
          font-weight: 800;
          color: map.get(vars.$colors, 'text-main');
          line-height: 1.2;
          margin-bottom: 4px;
        }

        .stat-title {
          font-size: 14px;
          color: map.get(vars.$colors, 'text-secondary');
          font-weight: 500;
        }
      }
    }
  }

  // Card Header
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);

    span {
      font-size: 18px;
      font-weight: 700;
      color: map.get(vars.$colors, 'text-main');
    }
  }

  // Custom Table
  .custom-table {
    :deep(.el-table__inner-wrapper) {
      &::before { display: none; }
    }

    :deep(th.el-table__cell) {
      background-color: rgba(map.get(vars.$colors, 'primary'), 0.05);
      color: map.get(vars.$colors, 'text-main');
      font-weight: 700;
      border-bottom: none;
    }

    :deep(td.el-table__cell) {
      border-bottom: 1px solid rgba(0, 0, 0, 0.03);
    }
    
    .icon-tag {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  // Pagination
  .pagination {
    padding: 24px;
    display: flex;
    justify-content: center;
  }
}

// Dialog Styles
.glass-dialog {
  :deep(.el-dialog__body) {
    padding: 24px;
  }
}

.glass-descriptions {
  :deep(.el-descriptions__body) {
    background: transparent;
  }
  
  :deep(.el-descriptions__label) {
    background: rgba(map.get(vars.$colors, 'primary'), 0.05);
    color: map.get(vars.$colors, 'text-main');
    font-weight: 600;
  }
  
  :deep(.el-descriptions__content) {
    color: map.get(vars.$colors, 'text-main');
  }
}

.log-content-box {
  white-space: pre-wrap;
  line-height: 1.6;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
}

// Stagger Animation
.stagger-anim {
  opacity: 0;
  animation: slideUpFade 0.6s vars.$ease-spring forwards;
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
@media (max-width: 768px) {
  .health-logs-management {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      
      .header-actions {
        width: 100%;
        
        .el-button {
          flex: 1;
        }
      }
    }

    .stats-cards {
      grid-template-columns: 1fr;
    }
  }
}
</style>