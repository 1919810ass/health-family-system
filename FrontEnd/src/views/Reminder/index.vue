<template>
  <div class="reminder-container">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-icon">
        <el-icon><Bell /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">智能健康提醒</h2>
        <p class="subtitle">个性化健康日程管理，不错过每一个重要时刻</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog" class="create-btn" v-particles>
          <el-icon class="mr-4"><Plus /></el-icon>
          创建提醒
        </el-button>
        <el-button type="success" @click="generateSmartReminders" class="smart-btn" v-particles>
          <el-icon class="mr-4"><MagicStick /></el-icon>
          生成智能提醒
        </el-button>
      </div>
    </div>

    <div class="glass-card stagger-anim" style="--delay: 0.2s">
      <div class="tabs-header">
        <el-tabs v-model="activeTab" @tab-change="loadReminders" class="custom-tabs">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待发送" name="PENDING" />
          <el-tab-pane label="已发送" name="SENT" />
          <el-tab-pane label="已确认" name="ACKNOWLEDGED" />
        </el-tabs>
      </div>

      <div class="card-content">
        <el-table 
          :data="reminders" 
          v-loading="loading" 
          style="width: 100%"
          class="glass-table"
        >
          <el-table-column prop="title" label="标题" min-width="150">
            <template #default="{ row }">
              <div class="title-cell">
                <div class="icon-wrapper" :class="getPriorityClass(row.priority)">
                  <el-icon><component :is="getTypeIcon(row.type)" /></el-icon>
                </div>
                <span class="title-text">{{ row.title }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="type" label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" effect="light" round size="small">
                {{ getTypeName(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="content" label="内容" show-overflow-tooltip min-width="200" />
          
          <el-table-column prop="assignedToUserName" label="分配给" width="120">
            <template #default="{ row }">
              <div class="user-cell" v-if="row.assignedToUserName">
                <el-avatar :size="24" class="user-avatar">{{ row.assignedToUserName.charAt(0) }}</el-avatar>
                <span class="user-name">{{ row.assignedToUserName }}</span>
              </div>
              <span v-else class="text-secondary">未分配</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="scheduledTime" label="计划时间" width="180">
            <template #default="{ row }">
              <span class="time-text">{{ formatTime(row.scheduledTime) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small" effect="plain" round>
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  v-if="row.status === 'PENDING'"
                  size="small"
                  type="success"
                  circle
                  @click="updateStatus(row.id, 'SENT')"
                  v-particles
                >
                  <el-icon><Promotion /></el-icon>
                </el-button>
                <el-button
                  v-if="row.status === 'SENT'"
                  size="small"
                  type="primary"
                  circle
                  @click="updateStatus(row.id, 'ACKNOWLEDGED')"
                  v-particles
                >
                  <el-icon><Check /></el-icon>
                </el-button>
                <el-button size="small" type="danger" circle @click="deleteReminder(row.id)" plain>
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="empty-state" v-if="!reminders.length && !loading">
          <el-empty description="暂无提醒事项" :image-size="120" />
        </div>
      </div>
    </div>

    <!-- 创建提醒对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="创建提醒" 
      width="600px"
      class="glass-dialog"
      align-center
    >
      <el-form :model="reminderForm" label-position="top" class="custom-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="提醒类型" required>
              <el-select v-model="reminderForm.type" placeholder="请选择类型" style="width: 100%">
                <template #prefix><el-icon><PriceTag /></el-icon></template>
                <el-option label="用药提醒" value="MEDICATION" />
                <el-option label="测量提醒" value="MEASUREMENT" />
                <el-option label="疫苗接种" value="VACCINE" />
                <el-option label="生活方式" value="LIFESTYLE" />
                <el-option label="异常数据" value="ABNORMAL" />
                <el-option label="常规提醒" value="ROUTINE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="reminderForm.priority" placeholder="请选择优先级" style="width: 100%">
                <template #prefix><el-icon><Flag /></el-icon></template>
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="URGENT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="提醒标题" required>
          <el-input v-model="reminderForm.title" placeholder="例如：吃降压药">
            <template #prefix><el-icon><Edit /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item label="提醒内容" required>
          <el-input
            v-model="reminderForm.content"
            type="textarea"
            :rows="3"
            placeholder="例如：明天早上7点吃降压药"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划时间" required>
              <el-date-picker
                v-model="reminderForm.scheduledTime"
                type="datetime"
                placeholder="选择提醒时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="提醒渠道">
              <el-select v-model="reminderForm.channel" placeholder="请选择渠道" style="width: 100%">
                <template #prefix><el-icon><Bell /></el-icon></template>
                <el-option label="APP推送" value="APP" />
                <el-option label="短信" value="SMS" />
                <el-option label="语音" value="VOICE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="分配给">
          <div style="display: flex; gap: 8px; align-items: center; width: 100%">
            <el-select 
              v-model="reminderForm.assignedToUserId" 
              placeholder="请选择要分配的用户" 
              clearable 
              filterable
              :loading="loadingMembers"
              :disabled="loadingMembers || familyMembers.length === 0"
              style="flex: 1;"
            >
              <template #prefix><el-icon><User /></el-icon></template>
              <el-option 
                v-for="member in familyMembers" 
                :key="member.id" 
                :label="member.nickname" 
                :value="member.id" 
              />
            </el-select>
            <el-button 
              type="primary" 
              link 
              @click="loadFamilyMembers" 
              :loading="loadingMembers"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCreateDialog = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" :loading="creating" @click="handleCreate" class="submit-btn" v-particles>创建</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 选择用户生成智能提醒对话框 -->
    <el-dialog 
      v-model="showSmartReminderDialog" 
      title="选择用户生成智能提醒" 
      width="500px"
      class="glass-dialog"
      align-center
    >
      <el-form label-position="top" class="custom-form">
        <el-form-item label="选择用户" required>
          <div style="display: flex; gap: 8px; align-items: center; width: 100%">
            <el-select 
              v-model="selectedUserForSmartReminder" 
              placeholder="请选择用户" 
              clearable 
              filterable
              :loading="loadingMembers"
              :disabled="loadingMembers || familyMembers.length === 0"
              style="flex: 1;"
            >
              <template #prefix><el-icon><User /></el-icon></template>
              <el-option 
                v-for="member in familyMembers" 
                :key="member.id" 
                :label="member.nickname" 
                :value="member.id" 
              />
            </el-select>
            <el-button 
              type="primary" 
              link 
              @click="loadFamilyMembers" 
              :loading="loadingMembers"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showSmartReminderDialog = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" :loading="loading" @click="generateSmartRemindersForSelectedUser" class="submit-btn" v-particles>生成智能提醒</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, MagicStick, Refresh, Bell, PriceTag, Flag, 
  Edit, User, Promotion, Check, Delete, FirstAidKit,
  Timer, DataLine, Sunny, Warning
} from '@element-plus/icons-vue'
import {
  createReminder,
  getUserReminders,
  updateReminderStatus,
  deleteReminder as deleteReminderApi,
  generateSmartReminders as generateSmartRemindersApi,
  generateSmartRemindersForUser
} from '@/api/reminder'
import { getFamilyMembers } from '@/api/family'
import { listFamilies } from '@/api/user'
import dayjs from 'dayjs'

const activeTab = ref('all')
const loading = ref(false)
const reminders = ref([])
const showCreateDialog = ref(false)

const openCreateDialog = async () => {
  // 打开对话框前先重置表单
  resetForm()
  // 然后加载家庭成员
  await loadFamilyMembers()
  showCreateDialog.value = true
}
const creating = ref(false)

const familyMembers = ref([])
const loadingMembers = ref(false)

const currentFamilyId = ref(null)

const reminderForm = ref({
  type: 'MEDICATION',
  title: '',
  content: '',
  scheduledTime: null,
  priority: 'MEDIUM',
  channel: 'APP',
  assignedToUserId: null
})

const getTypeIcon = (type) => {
  const map = {
    MEDICATION: FirstAidKit,
    MEASUREMENT: DataLine,
    VACCINE: FirstAidKit,
    LIFESTYLE: Sunny,
    ABNORMAL: Warning,
    ROUTINE: Timer
  }
  return map[type] || Bell
}

const getPriorityClass = (priority) => {
  const map = {
    LOW: 'priority-low',
    MEDIUM: 'priority-medium',
    HIGH: 'priority-high',
    URGENT: 'priority-urgent'
  }
  return map[priority] || 'priority-medium'
}

const loadFamilyMembers = async () => {
  loadingMembers.value = true
  try {
    // 尝试从localStorage获取当前家庭ID
    let familyId = localStorage.getItem('current_family_id')
    
    // 如果localStorage中没有家庭ID，尝试从用户API获取
    if (!familyId) {
      const familyRes = await listFamilies()
      if (familyRes && familyRes.code === 0 && familyRes.data && familyRes.data.length > 0) {
        // 使用第一个家庭作为当前家庭
        familyId = familyRes.data[0].id
        localStorage.setItem('current_family_id', familyId)
      }
    }
    
    if (!familyId) {
      ElMessage.warning('您还没有加入任何家庭，请先加入或创建家庭')
      familyMembers.value = []
      return
    }
    
    currentFamilyId.value = familyId
    
    const res = await getFamilyMembers(familyId)
    if (res && res.code === 0 && res.data) {
      // 映射并过滤成员数据，确保id(userId)和nickname存在
      const rawMembers = res.data || []
      familyMembers.value = rawMembers
        .filter(m => m && m.userId)
        .map(m => ({
          ...m,
          id: m.userId, // 使用userId作为唯一标识和值
          nickname: m.nickname || m.name || m.phone || '家庭成员' // 确保有显示名称
        }))
      
      console.log('Loaded family members:', familyMembers.value);
      // 检查是否有家庭成员
      if (familyMembers.value.length === 0) {
        ElMessage.info('当前家庭暂无成员')
      }
    } else {
      familyMembers.value = []
      ElMessage.warning('当前家庭暂无成员')
    }
  } catch (error) {
    console.error('加载家庭成员失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '加载家庭成员失败'
    ElMessage.error(errorMsg)
    familyMembers.value = []
  } finally {
    loadingMembers.value = false
  }
}

const loadReminders = async () => {
  loading.value = true
  try {
    const params = activeTab.value === 'all' ? {} : { status: activeTab.value }
    
    // 如果有当前家庭ID，传入familyId参数以便获取整个家庭的提醒
    if (currentFamilyId.value) {
      params.familyId = currentFamilyId.value
    }
    
    const res = await getUserReminders(params)
    if (res && res.code === 0 && res.data) {
      reminders.value = res.data || []
    } else {
      reminders.value = []
    }
  } catch (error) {
    console.error('加载提醒失败:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '加载提醒失败'
    ElMessage.error(errorMsg)
    reminders.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // 先加载家庭信息，获取currentFamilyId
  await loadFamilyMembers()
  loadReminders()
})

const handleCreate = async () => {
  if (!reminderForm.value.title || !reminderForm.value.content || !reminderForm.value.scheduledTime) {
    ElMessage.warning('请填写完整信息')
    return
  }

  creating.value = true
  try {
    // 准备请求数据
    const requestData = {
      ...reminderForm.value,
      scheduledTime: dayjs(reminderForm.value.scheduledTime).format('YYYY-MM-DD HH:mm:ss'),
      assignedToUserId: reminderForm.value.assignedToUserId || null
    };
    
    console.log('创建提醒请求数据:', requestData);
    
    const res = await createReminder(requestData)
    if (res.code === 0) {
      ElMessage.success('创建成功')
      showCreateDialog.value = false
      resetForm()
      loadReminders()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建提醒失败:', error)
    ElMessage.error('创建提醒失败')
  } finally {
    creating.value = false
  }
}

const updateStatus = async (id, status) => {
  try {
    const res = await updateReminderStatus(id, { status })
    if (res.code === 0) {
      ElMessage.success('更新成功')
      loadReminders()
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

const deleteReminder = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条提醒吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteReminderApi(id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadReminders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除提醒失败:', error)
      ElMessage.error('删除提醒失败')
    }
  }
}

const showSmartReminderDialog = ref(false);
const selectedUserForSmartReminder = ref(null);

const generateSmartReminders = async () => {
  // 先加载家庭成员，然后打开选择用户对话框
  await loadFamilyMembers();
  
  // 检查是否有家庭成员
  if (familyMembers.value.length === 0) {
    ElMessage.warning('当前家庭暂无其他成员，无法为特定用户生成智能提醒');
    return;
  }
  
  showSmartReminderDialog.value = true;
  // 重置选择的用户
  selectedUserForSmartReminder.value = null;
};

const generateSmartRemindersForSelectedUser = async () => {
  if (!selectedUserForSmartReminder.value) {
    ElMessage.warning('请选择要生成智能提醒的用户');
    return;
  }
  
  loading.value = true;
  try {
    const userId = selectedUserForSmartReminder.value;
    console.log('Generating smart reminders for user:', userId);
    const res = await generateSmartRemindersForUser(userId);
    if (res && res.code === 0 && res.data) {
      const count = res.data.length || 0;
      if (count > 0) {
        ElMessage.success(`成功为用户生成 ${count} 条智能提醒`);
      } else {
        ElMessage.info('暂无可生成的智能提醒');
      }
      loadReminders();
      showSmartReminderDialog.value = false;
      selectedUserForSmartReminder.value = null;
    } else {
      ElMessage.warning(res?.message || '生成智能提醒失败');
    }
  } catch (error) {
    console.error('生成智能提醒失败:', error);
    const errorMsg = error?.response?.data?.message || error?.message || '生成智能提醒失败';
    ElMessage.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  reminderForm.value = {
    type: 'MEDICATION',
    title: '',
    content: '',
    scheduledTime: null,
    priority: 'MEDIUM',
    channel: 'APP',
    assignedToUserId: null
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('MM-DD HH:mm')
}

const getTypeName = (type) => {
  const map = {
    MEDICATION: '用药提醒',
    MEASUREMENT: '测量提醒',
    VACCINE: '疫苗接种',
    LIFESTYLE: '生活方式',
    ABNORMAL: '异常数据',
    ROUTINE: '常规提醒'
  }
  return map[type] || type
}

const getTypeTagType = (type) => {
  const map = {
    MEDICATION: 'primary',
    MEASUREMENT: 'success',
    VACCINE: 'warning',
    LIFESTYLE: 'info',
    ABNORMAL: 'danger',
    ROUTINE: ''
  }
  return map[type] || ''
}

const getStatusName = (status) => {
  const map = {
    PENDING: '待发送',
    SENT: '已发送',
    ACKNOWLEDGED: '已确认',
    SKIPPED: '已跳过',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    PENDING: 'warning',
    SENT: 'success',
    ACKNOWLEDGED: 'primary',
    SKIPPED: 'info',
    CANCELLED: 'danger'
  }
  return map[status] || ''
}
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.reminder-container {
  min-height: 100%;
  padding: 24px;
  background: transparent;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;

  .header-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    background: linear-gradient(135deg, vars.$warning-color, color.adjust(vars.$warning-color, $lightness: 15%));
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(vars.$warning-color, 0.3);

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
      color: vars.$text-primary-color;
      margin: 0 0 4px 0;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-primary-color, vars.$warning-color));
    }

    .subtitle {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;

    .create-btn, .smart-btn {
      border-radius: 20px;
      padding: 10px 20px;
      height: auto;
      font-weight: 500;
      transition: all 0.3s vars.$ease-spring;
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
      }
    }
  }
}

.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: vars.$glass-border;
  overflow: hidden;
  transition: all 0.3s vars.$ease-spring;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: vars.$shadow-lg;

  .tabs-header {
    padding: 12px 20px;
    border-bottom: 1px solid rgba(vars.$text-primary-color, 0.06);
    background: rgba(255, 255, 255, 0.6);
  }
  
  .card-content {
    padding: 20px;
  }
}

.custom-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
  
  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
    color: vars.$text-secondary-color;
    padding: 16px 0;
    margin-right: 12px;
    border-radius: 999px;
    padding: 8px 16px;
    transition: all 0.2s;
    
    &.is-active {
      color: vars.$primary-color;
      font-weight: 600;
      background: rgba(78, 161, 255, 0.12);
    }
  }
}

.glass-table {
  background: transparent !important;
  
  :deep(th.el-table__cell) {
    background: transparent;
    color: vars.$text-secondary-color;
    font-weight: 500;
    border-bottom: 1px solid rgba(vars.$text-primary-color, 0.08);
  }
  
  :deep(tr) {
    background: transparent;
    
    &:hover td.el-table__cell {
      background: rgba(78, 161, 255, 0.06) !important;
    }
  }

  :deep(td.el-table__cell) {
    border-bottom: 1px solid rgba(vars.$text-primary-color, 0.06);
    padding: 18px 0;
  }

  :deep(.el-table__row) {
    background: rgba(255, 255, 255, 0.7);
  }
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 12px;

  .icon-wrapper {
    width: 32px;
    height: 32px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.priority-low { background: rgba(vars.$info-color, 0.1); color: vars.$info-color; }
    &.priority-medium { background: rgba(vars.$primary-color, 0.1); color: vars.$primary-color; }
    &.priority-high { background: rgba(vars.$warning-color, 0.1); color: vars.$warning-color; }
    &.priority-urgent { background: rgba(vars.$danger-color, 0.1); color: vars.$danger-color; }
  }

  .title-text {
    font-weight: 600;
    color: vars.$text-primary-color;
    font-size: 15px;
  }
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;

  .user-avatar {
    background: vars.$primary-color;
    color: #fff;
    font-size: 12px;
  }

  .user-name {
    font-size: 13px;
    color: vars.$text-regular-color;
  }
}

.time-text {
  font-family: 'Roboto Mono', monospace;
  color: vars.$text-regular-color;
  background: rgba(255, 255, 255, 0.5);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  border: vars.$glass-border;
}

.action-buttons {
  display: flex;
  gap: 8px;

  :deep(.el-button) {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  }
}

.glass-dialog {
  :deep(.el-dialog) {
    @include mixins.glass-effect;
    border-radius: vars.$radius-lg;
    box-shadow: vars.$shadow-2xl;
    
    .el-dialog__header {
      border-bottom: 1px solid rgba(vars.$text-primary-color, 0.05);
      margin-right: 0;
      padding: 20px;
    }
    
    .el-dialog__footer {
      border-top: 1px solid rgba(vars.$text-primary-color, 0.05);
      padding: 20px;
    }
  }
}

.custom-form {
  padding: 10px 0;

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: vars.$text-secondary-color;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-textarea__inner) {
    background: rgba(255, 255, 255, 0.5);
    box-shadow: none;
    border: 1px solid rgba(vars.$text-primary-color, 0.1);
    border-radius: vars.$radius-md;
    transition: all 0.3s ease;

    &:hover, &.is-focus {
      background: #fff;
      border-color: vars.$primary-color;
      box-shadow: 0 0 0 3px rgba(vars.$primary-color, 0.1);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .cancel-btn {
    border-radius: vars.$radius-md;
    &:hover {
      background: rgba(vars.$text-primary-color, 0.05);
    }
  }

  .submit-btn {
    border-radius: vars.$radius-md;
    padding: 10px 24px;
  }
}

.mr-4 { margin-right: 4px; }
.text-secondary { color: vars.$text-secondary-color; font-size: 13px; }

.stagger-anim {
  opacity: 0;
  animation: fadeInUp 0.6s vars.$ease-spring forwards;
  animation-delay: var(--delay);
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

@media (max-width: 768px) {
  .reminder-container {
    padding: 16px;
  }
  
  .page-header {
    .header-actions {
      width: 100%;
      justify-content: space-between;
      
      .el-button {
        flex: 1;
      }
    }
  }
}
</style>
