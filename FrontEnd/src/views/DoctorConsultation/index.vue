<template>
  <div class="patient-consultation-page">
    <el-page-header content="在线咨询" />

    <!-- 医生信息卡片 -->
    <div class="doctor-info-section">
      <el-card class="doctor-card" v-if="boundDoctor">
        <template #header>
          <div class="card-header">
            <span>我的家庭医生</span>
          </div>
        </template>
        <div class="doctor-detail">
          <el-avatar :src="boundDoctor.avatar" :size="60" class="doctor-avatar">
            {{ boundDoctor.nickname?.charAt(0) || 'D' }}
          </el-avatar>
          <div class="doctor-info">
            <div class="doctor-name">{{ boundDoctor.nickname || '家庭医生' }}</div>
            <div class="doctor-title">{{ boundDoctor.title || '主治医生' }}</div>
            <div class="doctor-status">
              <el-tag type="success" size="small">
                <span class="status-dot"></span>在线
              </el-tag>
            </div>
          </div>
          <el-button type="primary" @click="startConsultation" v-if="!currentSession" size="large">
            <el-icon><ChatDotRound /></el-icon>
            开始咨询
          </el-button>
          <el-button type="danger" @click="endConsultation" v-else size="large">
            <el-icon><Close /></el-icon>
            结束咨询
          </el-button>
        </div>
      </el-card>
      <el-empty v-else description="暂无绑定的家庭医生" class="empty-doctor">
        <template #extra>
          <el-button type="primary" @click="router.push('/family-doctor')">
            去绑定医生
          </el-button>
        </template>
      </el-empty>
    </div>

    <!-- 聊天区域 -->
    <div v-if="currentSession" class="chat-section">
      <el-card class="chat-card">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="header-left">
            <el-avatar :src="boundDoctor.avatar" :size="40">
              {{ boundDoctor.nickname?.charAt(0) || 'D' }}
            </el-avatar>
            <div class="header-info">
              <div class="doctor-name">{{ boundDoctor.nickname || '家庭医生' }}</div>
              <div class="status-text">
                <el-tag type="success" size="small" effect="plain">
                  <span class="status-dot"></span>在线
                </el-tag>
              </div>
            </div>
          </div>
          <div class="header-right">
            <el-button text circle @click="showSessionInfo = true" title="会话信息">
              <el-icon><InfoFilled /></el-icon>
            </el-button>
          </div>
        </div>

        <el-divider />

        <!-- 消息列表 -->
        <div class="messages-container custom-scrollbar" ref="messagesRef" v-loading="loading">
          <div v-if="messages.length === 0" class="empty-messages">
            <el-empty description="暂无消息，主动开始交流吧" />
          </div>

          <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-item"
              :class="{ 'is-patient': msg.senderType === 'PATIENT' || msg.senderType === 'MEMBER', 'is-doctor': msg.senderType === 'DOCTOR' }"
          >
            <el-avatar
                :src="(msg.senderType === 'PATIENT' || msg.senderType === 'MEMBER') ? currentUserAvatar : boundDoctor.avatar"
                :size="32"
                class="message-avatar"
            >
              {{ (msg.senderType === 'PATIENT' || msg.senderType === 'MEMBER') ? '我' : boundDoctor.nickname?.charAt(0) }}
            </el-avatar>
            <div class="message-content">
              <div class="message-bubble">
                {{ msg.content }}
              </div>
              <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <el-divider />
        <div class="input-section">
          <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入您的问题或需求..."
              maxlength="500"
              show-word-limit
              @keydown.ctrl.enter="sendMessage"
              @keydown.meta.enter="sendMessage"
          />
          <div class="input-actions">
            <el-button type="primary" :loading="sending" @click="sendMessage" size="large">
              <el-icon><Promotion /></el-icon>
              发送消息
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 会话信息抽屉 -->
    <el-drawer
        v-model="showSessionInfo"
        title="会话信息"
        direction="rtl"
        size="350px"
    >
      <div class="session-info-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="咨询医生">
            {{ boundDoctor.nickname || '家庭医生' }}
          </el-descriptions-item>
          <el-descriptions-item label="会话开始时间">
            {{ formatDateTime(currentSession?.createdAt) || '刚刚' }}
          </el-descriptions-item>
          <el-descriptions-item label="会话状态">
            <el-tag type="success" v-if="currentSession?.status === 'OPEN'">进行中</el-tag>
            <el-tag type="info" v-else>已结束</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="消息数量">
            {{ messages.length }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Close, Promotion, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import {
  listSessionsForPatient,
  getOrCreateSession,
  getSession,
  getSessionMessages,
  sendMessage as apiSendMessage,
  closeSession as apiCloseSession
} from '../../api/consultation'
import { getOrCreateDoctorSession } from '../../api/doctor'
import dayjs from 'dayjs'

const userStore = useUserStore()
const router = useRouter()
const messagesRef = ref(null)

// 状态
const boundDoctor = ref(null)
const currentSession = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const showSessionInfo = ref(false)
const currentUserAvatar = ref('')

// 获取当前用户信息
const getCurrentUserInfo = async () => {
  if (userStore.profile) {
    currentUserAvatar.value = userStore.profile.avatar
  }
}

// 获取家庭医生信息
const fetchBoundDoctor = async () => {
  try {
    // 检查用户角色，如果是医生角色，提示并重定向
    if (userStore.profile && userStore.profile.role === 'DOCTOR') {
      ElMessage.warning('您是医生用户，请前往医生端使用相关功能')
      router.push('/doctor/consultation')
      return
    }

    // 获取当前家庭 ID：优先使用 localStorage 中的 current_family_id，其次使用 profile 中的 familyId
    let familyId = localStorage.getItem('current_family_id')
    if (!familyId || familyId === 'null') {
      familyId = userStore.profile ? userStore.profile.familyId : undefined
    }

    if (!familyId || familyId === 'null') {
      console.warn('[DoctorConsultation] No family selected')
      return
    }

    const result = await getOrCreateDoctorSession()
    console.log('[DoctorConsultation] fetchBoundDoctor result:', result)
    if (result?.data) {
      boundDoctor.value = result.data
      console.log('[DoctorConsultation] Bound doctor info:', boundDoctor.value)
    } else {
      console.warn('[DoctorConsultation] No bound doctor data returned')
    }
  } catch (error) {
    console.error('[DoctorConsultation] Failed to fetch bound doctor:', error)
    const errorMsg = error?.response?.data?.message || error?.message || '未知错误'
    console.error('[DoctorConsultation] Error details:', errorMsg)
    ElMessage.error('获取家庭医生信息失败：' + errorMsg)
  }
}

// 开始咨询（创建或获取会话）
const startConsultation = async () => {
  try {
    // 检查用户角色，如果是医生角色，提示并重定向
    if (userStore.profile && userStore.profile.role === 'DOCTOR') {
      ElMessage.warning('您是医生用户，请前往医生端使用相关功能')
      router.push('/doctor/consultation')
      return
    }

    // 修复：兼容 userId 和 doctorUserId 字段，并确保医生已绑定
    const doctorId = boundDoctor.value?.doctorUserId || boundDoctor.value?.userId

    if (!boundDoctor.value || !doctorId) {
      await ElMessageBox.confirm(
          '您还未绑定家庭医生，需要先去绑定吗？',
          '提示',
          {
            confirmButtonText: '去绑定',
            cancelButtonText: '取消',
            type: 'warning'
          }
      ).then(() => {
        router.push('/family-doctor')
      }).catch(() => {
        // 用户取消
      })
      return
    }

    loading.value = true

    // 获取家庭 ID：优先使用 localStorage 中的 current_family_id，其次使用 profile 中的 familyId
    let familyId = localStorage.getItem('current_family_id')
    if (!familyId || familyId === 'null') {
      familyId = userStore.profile ? userStore.profile.familyId : undefined
    }
    if (!familyId || familyId === 'null') {
      ElMessage.error('未找到家庭信息，请先进入家庭')
      return
    }

    // 修复：应该传入当前用户ID作为 patientUserId，而不是医生ID
    const currentUserId = userStore.profile?.id || userStore.profile?.userId
    if (!currentUserId) {
      ElMessage.error('无法获取当前用户信息')
      return
    }

    // 获取或创建会话
    const result = await getOrCreateSession(currentUserId, familyId)
    if (result.data) {
      currentSession.value = result.data
      messages.value = []
      await loadMessages()
      await nextTick(() => {
        if (messagesRef.value) {
          messagesRef.value.scrollTop = messagesRef.value.scrollHeight
        }
      })
      ElMessage.success('咨询已开启')
    }
  } catch (error) {
    console.error('Failed to start consultation:', error)
    ElMessage.error('开启咨询失败：' + (error?.response?.data?.message || error?.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 加载消息
const loadMessages = async () => {
  if (!currentSession.value?.id) return

  try {
    loading.value = true
    const result = await getSessionMessages(currentSession.value.id)
    if (result.data && Array.isArray(result.data)) {
      messages.value = result.data
    }
  } catch (error) {
    console.error('Failed to load messages:', error)
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  if (!currentSession.value?.id) {
    ElMessage.error('会话不存在，请重新开启')
    return
  }

  try {
    sending.value = true
    const payload = {
      sessionId: currentSession.value.id,
      content: inputMessage.value.trim(),
      senderType: 'PATIENT'
    }

    const result = await apiSendMessage(payload)
    if (result.data) {
      messages.value.push({
        id: result.data.id,
        content: inputMessage.value.trim(),
        senderType: 'PATIENT',
        createdAt: new Date().toISOString()
      })
      inputMessage.value = ''

      await nextTick(() => {
        if (messagesRef.value) {
          messagesRef.value.scrollTop = messagesRef.value.scrollHeight
        }
      })
    }
  } catch (error) {
    console.error('Failed to send message:', error)
    ElMessage.error('消息发送失败')
  } finally {
    sending.value = false
  }
}

// 结束咨询
const endConsultation = async () => {
  if (!currentSession.value?.id) return

  try {
    await ElMessageBox.confirm('确定要结束这次咨询吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await apiCloseSession(currentSession.value.id)
    currentSession.value = null
    messages.value = []
    inputMessage.value = ''
    ElMessage.success('咨询已结束')
  } catch (error) {
    if (error.response?.status !== 401) {
      console.error('Failed to close session:', error)
      ElMessage.error('关闭会话失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  if (now.isSame(date, 'day')) {
    return date.format('HH:mm')
  }
  return date.format('MM-DD HH:mm')
}

// 格式化完整日期时间
const formatDateTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 初始化
onMounted(async () => {
  try {
    // 确保用户信息已加载
    if (!userStore.profile) {
      console.log('[DoctorConsultation] Fetching user profile...')
      await userStore.fetchProfile()
      console.log('[DoctorConsultation] User profile loaded:', userStore.profile)
    }

    // 检查用户角色，如果是医生角色，提示并重定向
    if (userStore.profile && userStore.profile.role === 'DOCTOR') {
      ElMessage.warning('您是医生用户，请前往医生端使用相关功能')
      router.push('/doctor/consultation')
      return
    }

    await getCurrentUserInfo()
    await fetchBoundDoctor()
  } catch (error) {
    console.error('[DoctorConsultation] Error during initialization:', error)
    ElMessage.error('页面初始化失败')
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.patient-consultation-page {
  padding: 24px;
  background: transparent;
  min-height: calc(100vh - 60px);

  :deep(.el-page-header) {
    margin-bottom: 24px;

    .el-page-header__content {
      font-size: 20px;
      font-weight: 600;
      color: vars.$text-main-color;
    }
    
    .el-page-header__left {
      color: vars.$text-main-color;
    }
  }
}

// 医生信息卡片
.doctor-info-section {
    margin-bottom: 24px;
    animation: fadeInDown 0.6s vars.$ease-spring;

    :deep(.el-card) {
      @include mixins.glass-effect;
      border-radius: vars.$radius-lg;
      border: vars.$glass-border;
      box-shadow: vars.$shadow-sm;
      transition: all 0.3s vars.$ease-spring;

      &:hover {
        box-shadow: vars.$shadow-md;
        transform: translateY(-2px);
      }

      .el-card__header {
        padding: 16px 20px;
        border-bottom: 1px solid rgba(0, 0, 0, 0.05);
        
        span {
          font-weight: 600;
          color: vars.$text-main-color;
        }
      }

      .el-card__body {
        padding: 24px;
      }
    }

  .doctor-detail {
    display: flex;
    align-items: center;
    gap: 20px;

    .doctor-avatar {
      flex-shrink: 0;
      border: 2px solid rgba(255, 255, 255, 0.5);
      box-shadow: vars.$shadow-sm;
    }

    .doctor-info {
      flex: 1;

      .doctor-name {
        font-size: 18px;
        font-weight: 600;
        color: vars.$text-main-color;
        margin-bottom: 4px;
      }

      .doctor-title {
        font-size: 14px;
        color: vars.$text-secondary-color;
        margin-bottom: 8px;
      }

      .doctor-status {
        .status-dot {
          display: inline-block;
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: vars.$success-color;
          margin-right: 4px;
          animation: pulse 2s infinite;
        }
      }
    }

    .el-button {
      flex-shrink: 0;
      padding: 12px 28px;
      height: auto;
      border-radius: vars.$radius-md;
      font-weight: 500;
      box-shadow: vars.$shadow-sm;
      transition: all 0.3s vars.$ease-spring;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: vars.$shadow-md;
      }
    }
  }

  .empty-doctor {
    padding: 40px 20px;
  }
}

// 聊天区域
.chat-section {
  margin-bottom: 24px;
  animation: fadeInUp 0.6s vars.$ease-spring 0.2s backwards;

  .chat-card {
    @include mixins.glass-effect;
    border-radius: vars.$radius-lg;
    border: vars.$glass-border;
    box-shadow: vars.$shadow-sm;
    height: calc(100vh - 380px);
    min-height: 500px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    :deep(.el-card__body) {
      padding: 0;
      flex: 1;
      display: flex;
      flex-direction: column;
      background: transparent;
    }
  }

  .chat-header {
    padding: 16px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid rgba(vars.$text-main-color, 0.05);
    background: rgba(255, 255, 255, 0.3);

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;

      .header-info {
        .doctor-name {
          font-size: 15px;
          font-weight: 600;
          color: vars.$text-main-color;
        }

        .status-text {
          margin-top: 2px;

          .status-dot {
            display: inline-block;
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: vars.$success-color;
            margin-right: 4px;
            animation: pulse 2s infinite;
          }
        }
      }
    }

    .header-right {
      .el-button {
        color: vars.$primary-color;
        
        &:hover {
          color: vars.$info-color;
          background: rgba(vars.$primary-color, 0.1);
        }
      }
    }
  }

  :deep(.el-divider) {
    margin: 0;
    border-color: rgba(vars.$text-main-color, 0.05);
  }

  // 消息容器
  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .empty-messages {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100%;
    }

    .message-item {
      display: flex;
      gap: 12px;
      animation: fadeInUp 0.4s vars.$ease-spring;

      &.is-patient {
        flex-direction: row-reverse;

        .message-avatar {
          flex-shrink: 0;
          border: 2px solid rgba(255, 255, 255, 0.5);
          box-shadow: vars.$shadow-sm;
        }

        .message-content {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          max-width: 70%;

          .message-bubble {
            background: vars.$gradient-primary;
            color: white;
            padding: 12px 18px;
            border-radius: 16px 16px 4px 16px;
            word-wrap: break-word;
            word-break: break-all;
            box-shadow: vars.$shadow-sm;
            font-size: 14px;
            line-height: 1.5;
          }

          .message-time {
            font-size: 12px;
            color: vars.$text-secondary-color;
            margin-top: 6px;
            padding-right: 4px;
          }
        }
      }

      &.is-doctor {
        .message-avatar {
          flex-shrink: 0;
          border: 2px solid rgba(255, 255, 255, 0.5);
          box-shadow: vars.$shadow-sm;
        }

        .message-content {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
          max-width: 70%;

          .message-bubble {
            background: rgba(255, 255, 255, 0.8);
            color: vars.$text-main-color;
            padding: 12px 18px;
            border-radius: 16px 16px 16px 4px;
            word-wrap: break-word;
            word-break: break-all;
            border: vars.$glass-border;
            box-shadow: vars.$shadow-sm;
            font-size: 14px;
            line-height: 1.5;
          }

          .message-time {
            font-size: 12px;
            color: vars.$text-secondary-color;
            margin-top: 6px;
            padding-left: 4px;
          }
        }
      }
    }
  }

  // 输入区域
  .input-section {
    padding: 20px 24px;
    border-top: 1px solid rgba(vars.$text-main-color, 0.05);
    background: rgba(255, 255, 255, 0.3);
    display: flex;
    flex-direction: column;
    gap: 16px;

    :deep(.el-textarea__inner) {
      border-radius: vars.$radius-md;
      background: rgba(255, 255, 255, 0.6);
      border: vars.$glass-border;
      box-shadow: none;
      padding: 12px;
      transition: all 0.3s;
      
      &:focus {
        background: #fff;
        border-color: vars.$primary-color;
        box-shadow: 0 0 0 3px rgba(vars.$primary-color, 0.1);
      }
    }

    .input-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;

      .el-button {
        min-width: 120px;
        border-radius: vars.$radius-md;
        font-weight: 500;
        transition: all 0.3s vars.$ease-spring;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: vars.$shadow-sm;
        }
      }
    }
  }
}

// 会话信息抽屉
.session-info-content {
  padding: 16px 0;

  :deep(.el-descriptions) {
    margin: 0;
    --el-descriptions-item-bordered-label-background: rgba(vars.$primary-color, 0.05);
  }
}

// 自定义滚动条
.custom-scrollbar {
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(vars.$text-main-color, 0.1);
    border-radius: 3px;

    &:hover {
      background: rgba(vars.$text-main-color, 0.2);
    }
  }
}

// 动画
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

// 响应式设计
@media (max-width: 1366px) {
  .patient-consultation-page {
    padding: 16px;
  }

  .doctor-info-section :deep(.el-card__body) {
    padding: 20px;
  }

  .chat-section {
    .chat-card {
      height: calc(100vh - 420px);
      min-height: 400px;
    }
  }
}

@media (max-width: 768px) {
  .patient-consultation-page {
    padding: 16px;
  }

  .doctor-info-section {
    .doctor-detail {
      flex-direction: column;
      text-align: center;
      gap: 16px;
      
      .doctor-info {
        width: 100%;
      }
      
      .el-button {
        width: 100%;
      }
    }
  }
}
</style>