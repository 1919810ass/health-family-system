<template>
  <div class="health-reminders-management">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">健康提醒管理</h1>
        <p class="page-subtitle">设置和管理全平台用户的健康提醒任务</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" round v-particles @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新建提醒
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
        <el-form-item label="提醒类型">
          <el-select v-model="searchForm.type" placeholder="请选择提醒类型" clearable>
            <template #prefix><el-icon><Bell /></el-icon></template>
            <el-option label="饮食提醒" value="diet"><span style="float: left">饮食提醒</span><el-icon style="float: right; color: #E6A23C"><Food /></el-icon></el-option>
            <el-option label="运动提醒" value="exercise"><span style="float: left">运动提醒</span><el-icon style="float: right; color: #67C23A"><Bicycle /></el-icon></el-option>
            <el-option label="用药提醒" value="medication"><span style="float: left">用药提醒</span><el-icon style="float: right; color: #F56C6C"><FirstAidKit /></el-icon></el-option>
            <el-option label="体检提醒" value="checkup"><span style="float: left">体检提醒</span><el-icon style="float: right; color: #409EFF"><Monitor /></el-icon></el-option>
            <el-option label="睡眠提醒" value="sleep"><span style="float: left">睡眠提醒</span><el-icon style="float: right; color: #909399"><Moon /></el-icon></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <template #prefix><el-icon><InfoFilled /></el-icon></template>
            <el-option label="启用" value="active" />
            <el-option label="暂停" value="paused" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round v-particles @click="searchReminders">
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
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalReminders }}</div>
          <div class="stat-title">总提醒数</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper green">
          <el-icon><VideoPlay /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeReminders }}</div>
          <div class="stat-title">启用中</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper orange">
          <el-icon><VideoPause /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pausedReminders }}</div>
          <div class="stat-title">已暂停</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper red">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todaySent }}</div>
          <div class="stat-title">今日发送</div>
        </div>
      </div>
    </div>

    <!-- 提醒列表 -->
    <div class="glass-card stagger-anim" style="--delay: 0.4s">
      <div class="card-header">
        <span>健康提醒列表</span>
        <div class="header-actions">
          <el-button circle @click="refreshList" :loading="loading">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>

      <el-table 
        :data="reminders" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100">
           <template #default="{ row }">
              <el-tag size="small" effect="plain" type="info">{{ row.userId }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="title" label="提醒标题" width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="140">
          <template #default="{ row }">
            <el-tag :type="getReminderTypeTagType(row.type)" effect="light" round class="icon-tag">
              <el-icon class="mr-1"><component :is="getReminderTypeIcon(row.type)" /></el-icon>
              {{ getReminderTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="schedule" label="提醒时间" width="120">
           <template #default="{ row }">
              <span class="schedule-time">{{ row.schedule }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="frequency" label="频率" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ getFrequencyName(row.frequency) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getReminderStatusTagType(row.status)" effect="dark" round>
              {{ getReminderStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastSent" label="最后发送" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" round @click="showEditDialog(row)">编辑</el-button>
            <el-dropdown trigger="click" style="margin-left: 12px">
              <el-button size="small" type="primary" round>
                更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="showDetailDialog(row)">详情</el-dropdown-item>
                  <el-dropdown-item @click="toggleStatus(row)">
                    {{ row.status === 'active' ? '暂停' : '启用' }}
                  </el-dropdown-item>
                  <el-dropdown-item @click="sendTestReminder(row)" divided>发送测试</el-dropdown-item>
                  <el-dropdown-item @click="deleteReminder(row)" style="color: #F56C6C">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 提醒编辑/创建对话框 -->
    <el-dialog 
      :title="dialog.title" 
      v-model="dialog.visible" 
      width="50%" 
      class="glass-dialog"
      :before-close="handleDialogClose"
    >
      <el-form 
        :model="dialog.form" 
        :rules="dialog.rules" 
        ref="dialogFormRef"
        label-width="100px"
        class="custom-form"
      >
        <el-form-item label="提醒标题" prop="title">
          <el-input v-model="dialog.form.title" placeholder="请输入提醒标题" />
        </el-form-item>
        <el-form-item label="提醒类型" prop="type">
          <el-select v-model="dialog.form.type" placeholder="请选择提醒类型" style="width: 100%">
            <template #prefix><el-icon><Bell /></el-icon></template>
            <el-option label="饮食提醒" value="diet"><span style="float: left">饮食提醒</span><el-icon style="float: right; color: #E6A23C"><Food /></el-icon></el-option>
            <el-option label="运动提醒" value="exercise"><span style="float: left">运动提醒</span><el-icon style="float: right; color: #67C23A"><Bicycle /></el-icon></el-option>
            <el-option label="用药提醒" value="medication"><span style="float: left">用药提醒</span><el-icon style="float: right; color: #F56C6C"><FirstAidKit /></el-icon></el-option>
            <el-option label="体检提醒" value="checkup"><span style="float: left">体检提醒</span><el-icon style="float: right; color: #409EFF"><Monitor /></el-icon></el-option>
            <el-option label="睡眠提醒" value="sleep"><span style="float: left">睡眠提醒</span><el-icon style="float: right; color: #909399"><Moon /></el-icon></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="提醒内容" prop="content">
          <el-input 
            v-model="dialog.form.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入提醒内容" 
          />
        </el-form-item>
        <el-form-item label="提醒时间" prop="schedule">
          <el-time-select
            v-model="dialog.form.schedule"
            start="00:00"
            step="00:15"
            end="23:45"
            placeholder="请选择提醒时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="提醒频率" prop="frequency">
          <el-select v-model="dialog.form.frequency" placeholder="请选择提醒频率" style="width: 100%">
            <el-option label="每天" value="daily" />
            <el-option label="每周" value="weekly" />
            <el-option label="每月" value="monthly" />
            <el-option label="工作日" value="workdays" />
            <el-option label="周末" value="weekends" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="dialog.form.userId" placeholder="请输入用户ID">
             <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialog.form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="paused">暂停</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="dialog.form.remarks" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注信息" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogClose" round>取消</el-button>
        <el-button type="primary" @click="submitDialog" :loading="dialog.submitting" round v-particles>确定</el-button>
      </template>
    </el-dialog>

    <!-- 提醒详情对话框 -->
    <el-dialog title="提醒详情" v-model="detailDialog.visible" width="60%" class="glass-dialog">
      <el-descriptions :column="2" border class="glass-descriptions">
        <el-descriptions-item label="提醒ID">{{ detailDialog.reminder.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailDialog.reminder.userId }}</el-descriptions-item>
        <el-descriptions-item label="提醒标题">{{ detailDialog.reminder.title }}</el-descriptions-item>
        <el-descriptions-item label="提醒类型">
          <el-tag :type="getReminderTypeTagType(detailDialog.reminder.type)">
            {{ getReminderTypeName(detailDialog.reminder.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提醒内容">{{ detailDialog.reminder.content }}</el-descriptions-item>
        <el-descriptions-item label="提醒时间">{{ detailDialog.reminder.schedule }}</el-descriptions-item>
        <el-descriptions-item label="提醒频率">
          <el-tag type="info">{{ getFrequencyName(detailDialog.reminder.frequency) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getReminderStatusTagType(detailDialog.reminder.status)">
            {{ getReminderStatusName(detailDialog.reminder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后发送">{{ detailDialog.reminder.lastSent || '从未发送' }}</el-descriptions-item>
        <el-descriptions-item label="下次发送">{{ detailDialog.reminder.nextSend || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailDialog.reminder.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailDialog.reminder.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailDialog.reminder.remarks || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog.visible = false" round>关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Refresh, ArrowDown, Bell, VideoPlay, VideoPause, CircleCheck, 
  Edit, Delete, User, InfoFilled, Search,
  Food, Bicycle, FirstAidKit, Monitor, Moon
} from '@element-plus/icons-vue'
import { getHealthReminders, updateHealthReminderStatus, createHealthReminder, updateHealthReminder, deleteHealthReminder, sendTestReminder as apiSendTestReminder } from '../../../api/admin'

// 搜索表单
const searchForm = reactive({
  userId: '',
  type: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 统计信息
const stats = reactive({
  totalReminders: 0,
  activeReminders: 0,
  pausedReminders: 0,
  todaySent: 0
})

// 提醒列表
const reminders = ref([])
const loading = ref(false)
const selectedReminders = ref([])

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '',
  type: '', // 'create' or 'edit'
  submitting: false,
  form: {
    id: null,
    userId: '',
    title: '',
    type: 'diet',
    content: '',
    schedule: '08:00',
    frequency: 'daily',
    status: 'active',
    remarks: ''
  },
  rules: {
    title: [
      { required: true, message: '请输入提醒标题', trigger: 'blur' },
      { min: 2, max: 50, message: '标题长度在2-50个字符', trigger: 'blur' }
    ],
    type: [
      { required: true, message: '请选择提醒类型', trigger: 'change' }
    ],
    content: [
      { required: true, message: '请输入提醒内容', trigger: 'blur' },
      { min: 5, max: 200, message: '内容长度在5-200个字符', trigger: 'blur' }
    ],
    schedule: [
      { required: true, message: '请选择提醒时间', trigger: 'change' }
    ],
    frequency: [
      { required: true, message: '请选择提醒频率', trigger: 'change' }
    ],
    userId: [
      { required: true, message: '请输入用户ID', trigger: 'blur' },
      { pattern: /^\d+$/, message: '用户ID必须为数字', trigger: 'blur' }
    ]
  }
})

// 详情对话框
const detailDialog = reactive({
  visible: false,
  reminder: {}
})

// 表单引用
const dialogFormRef = ref(null)

// 加载健康提醒数据
onMounted(() => {
  loadReminders()
  loadStats()
})

// 加载提醒列表（兼容后端 Result<T> 包装结构）
const loadReminders = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }

    const res = await getHealthReminders(params)
    const data = res && typeof res === 'object' ? res.data || {} : {}

    const list = Array.isArray(data.list)
      ? data.list
      : Array.isArray(data.items)
        ? data.items
        : []

    reminders.value = list
    pagination.total = Number(data.total) || list.length || 0
  } catch (error) {
    ElMessage.error('加载健康提醒失败')
    console.error('Error loading health reminders:', error)
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStats = async () => {
  try {
    // 这里应该调用实际的API
    // 目前使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    stats.totalReminders = 156
    stats.activeReminders = 123
    stats.pausedReminders = 23
    stats.todaySent = 45
  } catch (error) {
    ElMessage.error('加载统计信息失败')
  }
}

// 搜索提醒
const searchReminders = () => {
  pagination.page = 1
  loadReminders()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  loadReminders()
}

// 刷新列表
const refreshList = () => {
  loadReminders()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  loadReminders()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadReminders()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedReminders.value = selection
}

// 显示创建对话框
const showCreateDialog = () => {
  dialog.type = 'create'
  dialog.title = '新建健康提醒'
  dialog.form.id = null
  dialog.form.userId = ''
  dialog.form.title = ''
  dialog.form.type = 'diet'
  dialog.form.content = ''
  dialog.form.schedule = '08:00'
  dialog.form.frequency = 'daily'
  dialog.form.status = 'active'
  dialog.form.remarks = ''
  dialog.visible = true
}

// 显示编辑对话框
const showEditDialog = (reminder) => {
  dialog.type = 'edit'
  dialog.title = '编辑健康提醒'
  dialog.form.id = reminder.id
  dialog.form.userId = reminder.userId
  dialog.form.title = reminder.title
  dialog.form.type = reminder.type
  dialog.form.content = reminder.content
  dialog.form.schedule = reminder.schedule
  dialog.form.frequency = reminder.frequency
  dialog.form.status = reminder.status
  dialog.form.remarks = reminder.remarks
  dialog.visible = true
}

// 显示详情对话框
const showDetailDialog = (reminder) => {
  detailDialog.reminder = { ...reminder }
  detailDialog.visible = true
}

// 提交对话框
const submitDialog = async () => {
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      dialog.submitting = true
      try {
        if (dialog.type === 'create') {
          await createHealthReminder(dialog.form)
          ElMessage.success('提醒创建成功')
        } else {
          await updateHealthReminder(dialog.form.id, dialog.form)
          ElMessage.success('提醒更新成功')
        }
        dialog.visible = false
        loadReminders()
      } catch (error) {
        ElMessage.error(dialog.type === 'create' ? '创建提醒失败' : '更新提醒失败')
        console.error('Error submitting health reminder:', error)
      } finally {
        dialog.submitting = false
      }
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  dialog.visible = false
  if (dialogFormRef.value) {
    dialogFormRef.value.clearValidate()
  }
}

// 切换提醒状态
const toggleStatus = async (reminder) => {
  try {
    await ElMessageBox.confirm(
      `确定要${reminder.status === 'active' ? '暂停' : '启用'}提醒 "${reminder.title}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateHealthReminderStatus(reminder.id, reminder.status === 'active' ? 'paused' : 'active')
    reminder.status = reminder.status === 'active' ? 'paused' : 'active'
    ElMessage.success('提醒状态更新成功')
  } catch {
    // 用户取消操作
  }
}

// 发送测试提醒
const sendTestReminder = async (reminder) => {
  try {
    await ElMessageBox.confirm(
      `确定要向用户 ${reminder.userId} 发送测试提醒吗？`,
      '发送测试提醒',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await apiSendTestReminder(reminder.id)
    ElMessage.success('测试提醒发送成功')
  } catch {
    // 用户取消操作
  }
}

// 删除提醒
const deleteReminder = async (reminder) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除提醒 "${reminder.title}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteHealthReminder(reminder.id)
    const index = reminders.value.findIndex(item => item.id === reminder.id)
    if (index > -1) {
      reminders.value.splice(index, 1)
    }
    ElMessage.success('提醒删除成功')
  } catch {
    // 用户取消操作
  }
}

// 获取提醒类型标签类型
const getReminderTypeTagType = (type) => {
  switch (type) {
    case 'diet': return 'warning'
    case 'exercise': return 'success'
    case 'medication': return 'danger'
    case 'checkup': return 'primary'
    case 'sleep': return 'info'
    default: return 'info'
  }
}

// 获取提醒类型名称
const getReminderTypeName = (type) => {
  const typeMap = {
    diet: '饮食提醒',
    exercise: '运动提醒',
    medication: '用药提醒',
    checkup: '体检提醒',
    sleep: '睡眠提醒'
  }
  return typeMap[type] || type
}

// 获取提醒类型图标
const getReminderTypeIcon = (type) => {
  const iconMap = {
    diet: Food,
    exercise: Bicycle,
    medication: FirstAidKit,
    checkup: Monitor,
    sleep: Moon
  }
  return iconMap[type] || Bell
}

// 获取频率名称
const getFrequencyName = (frequency) => {
  const frequencyMap = {
    daily: '每天',
    weekly: '每周',
    monthly: '每月',
    workdays: '工作日',
    weekends: '周末',
    once: '一次性'
  }
  return frequencyMap[frequency] || frequency
}

// 获取提醒状态标签类型
const getReminderStatusTagType = (status) => {
  switch (status) {
    case 'active': return 'success'
    case 'paused': return 'warning'
    case 'completed': return 'info'
    default: return 'info'
  }
}

// 获取提醒状态名称
const getReminderStatusName = (status) => {
  const statusMap = {
    active: '启用',
    paused: '暂停',
    completed: '已完成'
  }
  return statusMap[status] || status
}
</script>

<style scoped lang="scss">
@use "sass:map";
@use "sass:color";
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.health-reminders-management {
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

        &.blue { background: linear-gradient(135deg, map.get(vars.$colors, 'info'), color.adjust(map.get(vars.$colors, 'info'), $lightness: 20%)); }
        &.green { background: linear-gradient(135deg, map.get(vars.$colors, 'success'), color.adjust(map.get(vars.$colors, 'success'), $lightness: 20%)); }
        &.orange { background: linear-gradient(135deg, map.get(vars.$colors, 'warning'), color.adjust(map.get(vars.$colors, 'warning'), $lightness: 20%)); }
        &.red { background: linear-gradient(135deg, map.get(vars.$colors, 'danger'), color.adjust(map.get(vars.$colors, 'danger'), $lightness: 20%)); }
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
    
    .schedule-time {
      font-family: monospace;
      font-weight: 600;
      color: map.get(vars.$colors, 'primary');
      background: rgba(map.get(vars.$colors, 'primary'), 0.1);
      padding: 2px 6px;
      border-radius: 4px;
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

.custom-form {
  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1) inset;
    
    &:hover, &.is-focus {
      box-shadow: 0 0 0 1px map.get(vars.$colors, 'primary') inset;
    }
  }
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
  .health-reminders-management {
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