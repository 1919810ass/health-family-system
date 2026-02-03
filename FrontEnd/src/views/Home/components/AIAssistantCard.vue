<template>
  <div class="ai-assistant-card">
    <div class="card-header">
      <h3>协作待办</h3>
      <div class="header-actions">
        <el-button class="header-btn" link type="primary" @click="goCollaboration">查看全部</el-button>
        <el-button class="header-btn" link type="primary" @click="startChat">问 AI</el-button>
      </div>
    </div>

    <div class="card-content">
      <div v-if="loading" class="state">加载中...</div>
      <div v-else-if="todos.length === 0" class="empty">
        <div class="empty-title">暂无待办协作任务</div>
        <div class="empty-actions">
          <el-button type="primary" plain size="small" :loading="generating" @click="generateTodos">智能生成</el-button>
          <el-button size="small" @click="goCollaboration">去协作中心</el-button>
        </div>
      </div>
      <div v-else class="todo-list">
        <div class="todo-scroll">
          <div v-for="item in todos" :key="item.id" class="todo-item">
            <div class="todo-main">
              <div class="todo-title">{{ item.title || '协作任务' }}</div>
              <div class="todo-meta">
                <span class="time">{{ formatTime(item.scheduledTime || item.schedule || item.createdAt) }}</span>
                <el-tag v-if="item.assignedToUserName" size="small" effect="plain" round>{{ item.assignedToUserName }}</el-tag>
              </div>
            </div>
            <div class="todo-actions">
              <el-button
                class="complete-btn"
                size="small"
                type="success"
                plain
                :loading="completingId === item.id"
                @click="completeTodo(item)"
              >
                完成
              </el-button>
              <el-button
                class="delete-btn"
                size="small"
                link
                type="danger"
                :loading="deletingId === item.id"
                @click="deleteTodo(item)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
        <div class="footer-actions">
          <el-button class="footer-btn" size="small" @click="refresh">刷新</el-button>
          <el-button class="footer-btn footer-primary" size="small" type="primary" plain :loading="generating" @click="generateTodos">智能生成</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getFamilyReminders, generateCollaborationReminders, updateReminderStatus, deleteReminder as deleteReminderApi } from '@/api/reminder'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const generating = ref(false)
const completingId = ref(null)
const deletingId = ref(null)
const todos = ref([])

const familyId = computed(() => {
  return userStore.currentFamily?.id || localStorage.getItem('current_family_id')
})

const currentUserId = computed(() => {
  return userStore.profile?.id || userStore.profile?.userId
})

const normalizeStatus = (value) => String(value || '').trim().toUpperCase()

const parseTs = (row) => {
  const v = row?.scheduledTime || row?.schedule || row?.createdAt || row?.updatedAt
  const ts = v ? dayjs(v).valueOf() : 0
  return Number.isFinite(ts) ? ts : 0
}

const isPlaceholder111 = (row) => {
  const title = String(row?.title || '').trim()
  const content = String(row?.content || row?.remark || '').trim()
  if (!title) return false
  if (!/^111\s*-\s*提醒$/.test(title)) return false
  return /健康计划.*111/.test(content) || content.includes('：111') || content.includes(':111')
}

const loadTodos = async () => {
  if (!familyId.value) {
    todos.value = []
    return
  }
  loading.value = true
  try {
    const res = await getFamilyReminders(familyId.value)
    const list = Array.isArray(res?.data) ? res.data : []
    const mine = list.filter((i) => {
      const st = normalizeStatus(i?.status)
      const assigned = String(i?.assignedToUserId ?? '')
      const me = String(currentUserId.value ?? '')
      if (!me) return false
      return assigned === me && !['COMPLETED', 'ACKNOWLEDGED', 'CANCELLED'].includes(st)
    })
    const placeholders = mine.filter(isPlaceholder111).filter((i) => i?.id != null)
    if (placeholders.length > 0) {
      await Promise.allSettled(placeholders.map((i) => deleteReminderApi(i.id)))
    }

    todos.value = mine
      .filter((i) => !isPlaceholder111(i))
      .sort((a, b) => {
        const diff = parseTs(b) - parseTs(a)
        if (diff !== 0) return diff
        const bid = Number(b?.id)
        const aid = Number(a?.id)
        if (Number.isFinite(bid) && Number.isFinite(aid) && bid !== aid) return bid - aid
        return 0
      })
      .slice(0, 5)
  } catch (e) {
    todos.value = []
  } finally {
    loading.value = false
  }
}

const startChat = () => {
  router.push('/ai-chat')
}

const goCollaboration = () => {
  router.push('/collaboration')
}

const refresh = async () => {
  await loadTodos()
}

const generateTodos = async () => {
  if (!familyId.value) {
    ElMessage.warning('请先在家庭管理中选择当前家庭')
    return
  }
  generating.value = true
  try {
    await generateCollaborationReminders(familyId.value)
    await loadTodos()
    if (todos.value.length > 0) {
      ElMessage.success('已生成协作待办')
    } else {
      ElMessage.info('暂无可生成的协作待办')
    }
  } catch (e) {
    ElMessage.error('生成失败')
  } finally {
    generating.value = false
  }
}

const completeTodo = async (item) => {
  if (!item?.id) return
  completingId.value = item.id
  try {
    const res = await updateReminderStatus(item.id, { status: 'COMPLETED' })
    if (res?.code === 0) {
      ElMessage.success('已完成')
      await loadTodos()
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    completingId.value = null
  }
}

const deleteTodo = async (item) => {
  if (!item?.id) return
  deletingId.value = item.id
  try {
    const res = await deleteReminderApi(item.id)
    if (res?.code === 0) {
      ElMessage.success('已删除')
      await loadTodos()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  } finally {
    deletingId.value = null
  }
}

const formatTime = (value) => {
  if (!value) return '--'
  return dayjs(value).format('MM-DD HH:mm')
}

onMounted(() => {
  loadTodos()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;

.ai-assistant-card {
  background: #fff;
  border-radius: 20px;
  padding: 16px 16px 12px;
  height: 100%;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.08);
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 12px 32px rgba(64, 158, 255, 0.12);
    transform: translateY(-2px);
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 800;
    letter-spacing: 0.3px;
    color: vars.$text-main-color;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  :deep(.el-button.header-btn) {
    height: 26px;
    border-radius: 999px;
    padding: 0 10px;
    background: rgba(64, 158, 255, 0.12);
    border: 1px solid rgba(64, 158, 255, 0.18);
    color: rgba(44, 106, 229, 0.95);
    font-weight: 700;
  }
  :deep(.el-button.header-btn:hover) {
    background: rgba(64, 158, 255, 0.16);
    border-color: rgba(64, 158, 255, 0.22);
    color: rgba(44, 106, 229, 1);
  }
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-top: 8px;
  min-height: 0;
}

.state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: vars.$text-secondary-color;
  font-size: 14px;
}

.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 10px;
  padding: 4px 0;
}

.empty-title {
  color: vars.$text-secondary-color;
  font-size: 14px;
}

.empty-actions {
  display: flex;
  gap: 8px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  min-height: 0;
}

.todo-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 14px;
  background: #f6f9ff;
  border: 1px solid rgba(64, 158, 255, 0.12);
}

.todo-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  :deep(.el-button) {
    height: 28px;
    border-radius: 999px;
    padding: 0 14px;
    font-weight: 700;
  }
  :deep(.el-button.complete-btn) {
    background: rgba(103, 194, 58, 0.14);
    border-color: rgba(103, 194, 58, 0.26);
    color: rgba(36, 106, 54, 0.95);
  }
  :deep(.el-button.complete-btn:hover) {
    background: rgba(103, 194, 58, 0.18);
    border-color: rgba(103, 194, 58, 0.34);
    color: rgba(36, 106, 54, 1);
  }
  :deep(.el-button.delete-btn) {
    height: 28px;
    border-radius: 999px;
    padding: 0 10px;
    background: rgba(245, 108, 108, 0.1);
    border: 1px solid rgba(245, 108, 108, 0.18);
    color: rgba(190, 53, 53, 0.95);
  }
  :deep(.el-button.delete-btn:hover) {
    background: rgba(245, 108, 108, 0.14);
    border-color: rgba(245, 108, 108, 0.24);
    color: rgba(190, 53, 53, 1);
  }
}

.todo-main {
  min-width: 0;
  flex: 1;
}

.todo-title {
  font-size: 13px;
  font-weight: 700;
  color: vars.$text-main-color;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-meta {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: vars.$text-secondary-color;
  font-size: 12px;
  .time {
    font-weight: 600;
  }
  :deep(.el-tag--small) {
    border-radius: 10px;
    padding: 0 8px;
  }
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  :deep(.el-button.footer-btn) {
    height: 30px;
    border-radius: 999px;
    padding: 0 14px;
    font-weight: 700;
  }
  :deep(.el-button.footer-btn:not(.footer-primary)) {
    background: rgba(0, 0, 0, 0.03);
    border-color: rgba(0, 0, 0, 0.08);
    color: rgba(48, 49, 51, 0.9);
  }
  :deep(.el-button.footer-primary) {
    background: rgba(64, 158, 255, 0.12);
    border-color: rgba(64, 158, 255, 0.22);
    color: rgba(44, 106, 229, 0.98);
  }
  :deep(.el-button.footer-primary:hover) {
    background: rgba(64, 158, 255, 0.16);
    border-color: rgba(64, 158, 255, 0.28);
  }
}
</style>
