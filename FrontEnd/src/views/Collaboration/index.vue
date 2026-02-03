<template>
  <div class="collab glass-effect">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Connection /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">家庭健康协作</h2>
        <p class="subtitle">家庭成员健康互助，共享健康生活</p>
      </div>
    </div>

    <el-card class="mt-16 glass-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>家庭健康看板</span>
          <el-switch v-model="onlyAbnormal" active-text="仅看异常" />
        </div>
      </template>
      <el-row :gutter="16">
        <el-col v-for="m in filteredMembers" :key="m.userId" :xs="24" :sm="12" :md="8" :lg="6" class="mb-16">
          <div class="member-card" :class="{ 'abnormal': m.abnormal }">
            <div class="member">
              <el-avatar :size="48" :src="m.avatar" class="member-avatar" />
              <div class="info">
                <div class="name">{{ m.nickname }}</div>
                
                <!-- 新增：多指标展示 -->
                <div v-if="m.metrics && m.metrics.length > 0" class="metrics-list">
                  <div v-for="item in m.metrics" :key="item.label" class="metric-item">
                    <span class="metric-label">{{ item.label }}:</span>
                    <span class="metric-val" :class="item.abnormal ? 'text-danger' : 'text-success'">
                      {{ item.value }} {{ item.unit }}
                    </span>
                  </div>
                </div>
                <div v-else class="summary" :class="m.abnormal ? 'text-danger' : 'text-success'">{{ m.summary }}</div>
                
                <div class="date">{{ formatDate(m.logDate) }}</div>

                <div class="actions mt-2" v-if="m.userId !== currentUserId">
                  <el-tooltip content="点赞鼓励" placement="top">
                    <el-button circle size="small" type="primary" plain @click="handleLike(m)">
                      <el-icon><Star /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="拍一拍提醒" placement="top">
                    <el-button circle size="small" type="warning" plain @click="handleNudge(m)">
                      <el-icon><Bell /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="写寄语" placement="top">
                    <el-button circle size="small" type="success" plain @click="openMessageDialog(m)">
                      <el-icon><ChatLineSquare /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="filteredMembers.length === 0" description="暂无家庭成员数据" />
    </el-card>

    <el-card class="mt-16 glass-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>家庭寄语 & 互动</span>
          <el-button type="text" @click="loadInteractions" :loading="interactionsLoading">刷新</el-button>
        </div>
      </template>
      <div class="interactions-list">
        <div v-for="item in interactions" :key="item.id" class="interaction-item">
          <el-avatar :size="32" :src="item.senderAvatar" class="sender-avatar">
             {{ item.senderName?.charAt(0) }}
          </el-avatar>
          <div class="interaction-content">
            <div class="interaction-header">
              <span class="sender-name">{{ item.senderName }}</span>
              <span class="action-text">{{ getActionText(item.type) }}</span>
              <span class="target-name">{{ item.targetUserName }}</span>
              <span class="time">{{ formatDateTime(item.createdAt) }}</span>
            </div>
            <div v-if="item.content" class="interaction-message">
              {{ item.content }}
            </div>
          </div>
        </div>
        <el-empty v-if="interactions.length === 0" description="暂无互动，快去给家人点个赞吧~" />
      </div>
    </el-card>

    <el-card class="mt-16 glass-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>协作任务</span>
          <div>
            <el-button type="primary" :loading="genLoading" @click="generateCollab" round class="glass-button" v-particles>智能生成协作提醒</el-button>
          </div>
        </div>
      </template>
      <el-table :data="familyReminders" v-loading="remindersLoading" style="width:100%" class="glass-table">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="240" />
        <el-table-column prop="assignedToUserId" label="指派给" min-width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" round>{{ userName(row.assignedToUserId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scheduledTime" label="计划时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.scheduledTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
             <el-tag :type="getTaskStatusType(row.status)" size="small" round>{{ getTaskStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canComplete(row)"
              size="small"
              type="success"
              plain
              :loading="completingId === row.id"
              @click="completeTask(row)"
              v-particles
            >
              完成
            </el-button>
            <el-button
              v-if="canUrge(row)"
              size="small"
              type="warning"
              plain
              @click="urgeTask(row)"
              v-particles
            >
              督促
            </el-button>
            <el-popconfirm
              v-if="canDelete(row)"
              title="确定删除该任务吗？"
              @confirm="deleteTask(row)"
            >
              <template #reference>
                <el-button size="small" type="danger" plain :loading="deletingId === row.id">删除</el-button>
              </template>
            </el-popconfirm>
            <span v-if="!canComplete(row) && !canDelete(row) && !canUrge(row)" class="text-secondary">—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="messageDialogVisible" title="发送健康寄语" width="400px" custom-class="glass-dialog">
      <el-input
        v-model="messageContent"
        type="textarea"
        :rows="3"
        placeholder="写下对家人的鼓励（例如：爷爷今天血压很稳，继续保持！）..."
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="messageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="sendMessage" :loading="sendingMessage">发送</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Connection, Star, Bell, ChatLineSquare } from '@element-plus/icons-vue'
import { useFamilyStore } from '../../stores/family'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getFamilyMembers, getFamilyDashboard, sendInteraction, getInteractions } from '../../api/family'
import { getFamilyReminders, generateCollaborationReminders, updateReminderStatus, deleteReminder as deleteReminderApi } from '../../api/reminder'

const famStore = useFamilyStore()
const userStore = useUserStore()
const familyId = ref()
const members = ref([])
const dashboard = ref({ members: [] })
const remindersLoading = ref(false)
const familyReminders = ref([])
const onlyAbnormal = ref(false)
const genLoading = ref(false)
const completingId = ref(null)
const deletingId = ref(null)

const interactions = ref([])
const interactionsLoading = ref(false)
const messageDialogVisible = ref(false)
const messageContent = ref('')
const messageTarget = ref(null)
const sendingMessage = ref(false)

const filteredMembers = computed(() => {
  const list = dashboard.value.members || []
  return onlyAbnormal.value ? list.filter(m => m.abnormal) : list
})

const userMap = ref({})
const userName = uid => userMap.value[uid] || '—'

const formatDate = d => (d ? dayjs(d).format('YYYY-MM-DD') : '')
const formatDateTime = d => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const normalizeStatus = (value) => String(value || '').trim().toUpperCase()

const currentUserId = computed(() => {
  return userStore.profile?.id || userStore.profile?.userId
})

const getTaskStatusName = (status) => {
  const s = normalizeStatus(status)
  if (s === 'COMPLETED' || s === 'ACKNOWLEDGED') return '已完成'
  if (s === 'CANCELLED') return '已取消'
  if (s === 'SKIPPED') return '已跳过'
  if (s === 'SENT') return '已发送'
  if (s === 'PENDING') return '待处理'
  return status ? String(status) : '待处理'
}

const getTaskStatusType = (status) => {
  const s = normalizeStatus(status)
  if (s === 'COMPLETED' || s === 'ACKNOWLEDGED') return 'success'
  if (s === 'CANCELLED') return 'info'
  if (s === 'SKIPPED') return 'info'
  if (s === 'SENT') return 'warning'
  return 'warning'
}

const canComplete = (row) => {
  const me = String(currentUserId.value ?? '')
  const assigned = String(row?.assignedToUserId ?? '')
  const s = normalizeStatus(row?.status)
  if (!me) return false
  if (assigned !== me) return false
  return !['COMPLETED', 'ACKNOWLEDGED', 'CANCELLED', 'SKIPPED'].includes(s)
}

const canDelete = (row) => {
  const me = String(currentUserId.value ?? '')
  const assigned = String(row?.assignedToUserId ?? '')
  if (!me) return false
  return assigned === me
}

const canUrge = (row) => {
  const me = String(currentUserId.value ?? '')
  const assigned = String(row?.assignedToUserId ?? '')
  const s = normalizeStatus(row?.status)
  if (!me) return false
  if (assigned === me) return false // 不能督促自己
  return ['PENDING', 'SENT'].includes(s)
}

const urgeTask = (row) => {
  ElMessage.success(`已发送提醒给 ${userName(row.assignedToUserId)}，请他们尽快完成！`)
}

const isPlaceholder111 = (row) => {
  const title = String(row?.title || '').trim()
  const content = String(row?.content || row?.remark || '').trim()
  if (!title) return false
  if (!/^111\s*-\s*提醒$/.test(title)) return false
  return /健康计划.*111/.test(content) || content.includes('：111') || content.includes(':111')
}

const loadInteractions = async () => {
  if (!familyId.value) return
  interactionsLoading.value = true
  try {
    const res = await getInteractions(familyId.value)
    interactions.value = res.data || []
  } catch (e) {
    // ignore
  } finally {
    interactionsLoading.value = false
  }
}

const handleLike = async (member) => {
  try {
    await sendInteraction(familyId.value, {
      targetUserId: member.userId,
      type: 'LIKE',
      content: '为你点赞'
    })
    ElMessage.success(`已为 ${member.nickname} 点赞`)
    loadInteractions()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleNudge = async (member) => {
  try {
    await sendInteraction(familyId.value, {
      targetUserId: member.userId,
      type: 'NUDGE',
      content: '拍了拍你'
    })
    ElMessage.success(`已提醒 ${member.nickname}`)
    loadInteractions()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const openMessageDialog = (member) => {
  messageTarget.value = member
  messageContent.value = ''
  messageDialogVisible.value = true
}

const sendMessage = async () => {
  if (!messageContent.value.trim()) return
  sendingMessage.value = true
  try {
    await sendInteraction(familyId.value, {
      targetUserId: messageTarget.value.userId,
      type: 'MESSAGE',
      content: messageContent.value
    })
    ElMessage.success('寄语已发送')
    messageDialogVisible.value = false
    loadInteractions()
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sendingMessage.value = false
  }
}

const getActionText = (type) => {
  if (type === 'LIKE') return '点赞了'
  if (type === 'NUDGE') return '拍了拍'
  if (type === 'MESSAGE') return '留言给'
  return '互动了'
}

const loadFamily = async () => {
  try {
    // 简化：取第一个家庭成员的familyId
    const families = await getFamilyMembers(await resolveDefaultFamilyId())
  } catch {}
}

const resolveDefaultFamilyId = async () => {
  if (famStore.current?.id) return famStore.current.id
  const fid = localStorage.getItem('current_family_id')
  return fid
}

const init = async () => {
  try {
    familyId.value = await resolveDefaultFamilyId()
    if (!familyId.value) {
      ElMessage.warning('请先在家庭管理中选择当前家庭')
      return
    }
    const [membersRes, boardRes] = await Promise.all([
      getFamilyMembers(familyId.value),
      getFamilyDashboard(familyId.value),
    ])
    members.value = membersRes.data || []
    userMap.value = Object.fromEntries(members.value.map(m => [m.userId, m.nickname]))
    dashboard.value = boardRes.data || { members: [] }
    await Promise.all([loadReminders(), loadInteractions()])
  } catch (e) {
    // Silent error or retry logic could be added
  }
}

const loadReminders = async () => {
  remindersLoading.value = true
  try {
    const res = await getFamilyReminders(familyId.value)
    const list = res.data || []
    const parseTs = (row) => {
      const v = row?.scheduledTime || row?.schedule || row?.createdAt || row?.updatedAt
      const ts = v ? dayjs(v).valueOf() : 0
      return Number.isFinite(ts) ? ts : 0
    }
    const me = String(currentUserId.value ?? '')
    const placeholders = Array.isArray(list)
      ? list.filter((i) => me && String(i?.assignedToUserId ?? '') === me && isPlaceholder111(i) && i?.id != null)
      : []
    if (placeholders.length > 0) {
      await Promise.allSettled(placeholders.map((i) => deleteReminderApi(i.id)))
    }

    familyReminders.value = [...list]
      .filter((i) => !isPlaceholder111(i))
      .sort((a, b) => {
      const diff = parseTs(b) - parseTs(a)
      if (diff !== 0) return diff
      const bid = Number(b?.id)
      const aid = Number(a?.id)
      if (Number.isFinite(bid) && Number.isFinite(aid) && bid !== aid) return bid - aid
      return 0
    })
  } catch (e) {
    ElMessage.error('加载协作任务失败')
  } finally {
    remindersLoading.value = false
  }
}

const completeTask = async (row) => {
  if (!row?.id) return
  completingId.value = row.id
  try {
    const res = await updateReminderStatus(row.id, { status: 'COMPLETED' })
    if (res?.code === 0) {
      ElMessage.success('已完成')
      await loadReminders()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    completingId.value = null
  }
}

const deleteTask = async (row) => {
  if (!row?.id) return
  deletingId.value = row.id
  try {
    const res = await deleteReminderApi(row.id)
    if (res?.code === 0) {
      ElMessage.success('已删除')
      await loadReminders()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  } finally {
    deletingId.value = null
  }
}

const generateCollab = async () => {
  genLoading.value = true
  try {
    await generateCollaborationReminders(familyId.value)
    ElMessage.success('已生成协作提醒')
    await loadReminders()
  } catch (e) {
    ElMessage.error('生成协作提醒失败')
  } finally {
    genLoading.value = false
  }
}

onMounted(init)
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables.scss' as vars;
@use '@/styles/mixins.scss' as mixins;

.collab {
  padding: 24px;
  min-height: 100%;
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

.glass-header {
  margin-bottom: 24px;
  :deep(.el-page-header__content) {
    font-weight: 600;
    color: vars.$text-main-color;
  }
}

.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  transition: all 0.4s vars.$ease-spring;
  margin-bottom: 24px;
  animation: fadeInUp 0.8s vars.$ease-spring;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: vars.$shadow-md;
  }
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
    padding: 16px 20px;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-header { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  font-weight: 600;
  color: vars.$text-main-color;
  font-size: 16px;
}

.member-card {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: vars.$radius-base;
  padding: 16px;
  transition: all 0.3s;
  height: 100%;
  cursor: default;
  
  &:hover {
    background: rgba(255, 255, 255, 0.8);
    transform: translateY(-2px);
    box-shadow: vars.$shadow-sm;
    border-color: vars.$primary-color;
  }
  
  &.abnormal {
    border-color: rgba(vars.$danger-color, 0.5);
    background: rgba(vars.$danger-color, 0.05);
    
    &:hover {
      border-color: vars.$danger-color;
      background: rgba(vars.$danger-color, 0.1);
    }
  }
}

.member { 
  display: flex; 
  gap: 16px; 
  align-items: center; 
  
  .member-avatar {
    border: 2px solid #fff;
    box-shadow: vars.$shadow-sm;
  }
  
  .info { 
    display: flex; 
    flex-direction: column; 
    gap: 4px;
    flex: 1;
    overflow: hidden;
  }
  
  .name { 
    font-weight: 600; 
    color: vars.$text-main-color;
    font-size: 16px;
  }
  
  .summary { 
    font-size: 14px;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .date { 
    color: vars.$text-secondary-color; 
    font-size: 12px; 
  }
}

.metrics-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 4px 0;
  
  .metric-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 13px;
    
    .metric-label {
      color: vars.$text-secondary-color;
      margin-right: 8px;
    }
    
    .metric-val {
      font-weight: 500;
    }
  }
}

.text-danger { color: vars.$danger-color; }
.text-success { color: vars.$success-color; }

.glass-table {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.3);
  --el-table-row-hover-bg-color: rgba(var(--el-color-primary-rgb), 0.1);
  --el-table-border-color: rgba(255, 255, 255, 0.3);
  
  :deep(th), :deep(td) {
    background: transparent !important;
  }
}

.glass-button {
  transition: all 0.3s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);
  }
}

.mt-16 { margin-top: 16px; }
.mb-16 { margin-bottom: 16px; }

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Media Queries */
@media (max-width: 768px) {
  .collab {
    padding: 12px;
  }
}

.interactions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.interaction-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: vars.$radius-base;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.6);
    border-color: rgba(vars.$primary-color, 0.3);
  }
}

.interaction-content {
  flex: 1;
}

.interaction-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-bottom: 4px;
  flex-wrap: wrap;
  
  .sender-name { font-weight: 600; color: vars.$text-main-color; }
  .action-text { color: vars.$text-secondary-color; font-size: 13px; }
  .target-name { font-weight: 600; color: vars.$text-main-color; }
  .time { color: vars.$text-placeholder-color; font-size: 12px; margin-left: auto; }
}

.interaction-message {
  font-size: 14px;
  color: vars.$text-main-color;
  background: rgba(255, 255, 255, 0.5);
  padding: 8px 12px;
  border-radius: 0 12px 12px 12px;
  margin-top: 4px;
  display: inline-block;
}

.actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
