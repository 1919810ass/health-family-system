<template>
  <el-container class="layout" :class="{ 'theme-dark': appStore.theme === 'dark', 'theme-light': appStore.theme === 'light' }">
    <!-- Sidebar -->
    <Sidebar 
      :menu-items="currentMenuItems" 
      :collapsed="collapsed" 
      :title="layoutTitle"
      :logo-icon="logoIcon"
    />

    <el-container class="content-container">
      <!-- Header -->
      <el-header class="header">
        <div class="header-left">
          <el-button link @click="toggleSider" class="toggle-btn">
            <el-icon :size="20">
              <Fold v-if="!collapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>

          <h2 class="page-title">{{ layoutTitle }}</h2>

          <!-- Breadcrumbs for Admin -->
          <el-breadcrumb separator="/" v-if="isAdminMode && breadcrumbs.length > 0" class="breadcrumb">
            <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
              {{ item }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- Removed Message Popover -->
          
          <el-popover
            placement="bottom-end"
            width="360"
            trigger="click"
            v-model:visible="reminderPopoverVisible"
            @show="handleReminderShow"
            popper-class="reminder-popper"
          >
            <template #reference>
              <el-button link class="icon-btn">
                <el-badge :value="reminderCount" :hidden="!shouldShowReminderBadge" :max="99">
                  <el-icon :size="20"><Bell /></el-icon>
                </el-badge>
              </el-button>
            </template>
            <div class="popover-panel reminder-popover">
              <div class="popover-header">
                <div class="header-title">
                  <span class="header-icon">
                    <el-icon><Bell /></el-icon>
                  </span>
                  <span>提醒中心</span>
                </div>
                <el-button link type="primary" @click="goReminderCenter">查看全部</el-button>
              </div>
              <el-skeleton :loading="reminderLoading" animated>
                <template #default>
                  <div v-if="reminderItems.length === 0" class="popover-empty">
                    <el-empty description="暂无提醒" :image-size="80" />
                  </div>
                  <div v-else class="popover-list reminder-list">
                    <div v-for="item in reminderItems" :key="item.id" :class="['popover-item', 'reminder-item', `status-${(item.status || 'default').toLowerCase()}`]" @click="goReminderCenter">
                      <div class="reminder-top">
                        <div class="reminder-icon">
                          <el-icon><Bell /></el-icon>
                        </div>
                        <div class="reminder-main">
                          <div class="item-title">{{ item.title || '健康提醒' }}</div>
                          <div class="item-content">{{ item.content || item.remark || '点击查看详情' }}</div>
                        </div>
                      </div>
                      <div class="reminder-footer">
                        <span class="reminder-time">{{ formatReminderTime(item.scheduledTime || item.schedule || item.createdAt) }}</span>
                        <el-tag size="small" effect="light" round :type="getReminderStatusType(item.status)">
                          {{ getReminderStatusName(item.status) }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </template>
              </el-skeleton>
            </div>
          </el-popover>

          <el-tooltip content="设置" placement="bottom">
            <el-button link class="icon-btn" @click="goSettings">
              <el-icon :size="20"><Setting /></el-icon>
            </el-button>
          </el-tooltip>

          <div class="divider"></div>
          
          <el-dropdown trigger="click">
            <span class="user-entry">
              <el-avatar v-if="userStore.profile && !profileLoading" :size="32" :src="userStore.profile?.avatar" class="user-avatar">{{ avatarText }}</el-avatar>
              <el-avatar v-else :size="32" :icon="UserFilled" class="user-avatar" />
              <span class="username">{{ profileLoading ? '加载中...' : (userStore.profile?.nickname || userStore.profile?.phone || '用户') }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goSettings">账号设置</el-dropdown-item>
                <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="main" :class="{ 'doctor-main': isDoctorMode }">
        <div v-if="doctorHeader" class="doctor-header">
          <div class="header-icon">
            <el-icon><component :is="doctorHeader.icon" /></el-icon>
          </div>
          <div class="header-content">
            <h2 class="title">{{ doctorHeader.title }}</h2>
            <p class="subtitle">{{ doctorHeader.subtitle }}</p>
          </div>
        </div>
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useDoctorStore } from '../../stores'
import { useUserStore } from '../../stores/user'
import { 
  Sunny, Moon, Fold, Expand, UserFilled, House, FirstAidKit, 
  Operation, Cpu, Message, Bell, Setting, ArrowDown,
  Document, ChatDotRound, Monitor, Notebook, TrendCharts, StarFilled
} from '@element-plus/icons-vue'
import Sidebar from './Sidebar.vue'
import { MENU_CONFIG } from '../../config/menuConfig'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getUserTodoItems, getUserReminders } from '../../api/reminder'
import { getConsultationHistory, getOrCreateSession } from '../../api/consultation'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const doctorStore = useDoctorStore()

const errorBanner = ref(null)
const profileLoading = ref(false)
const breadcrumbs = ref([])
const messageLoading = ref(false)
const reminderLoading = ref(false)
const messageItems = ref([])
const reminderItems = ref([])
const messagePopoverVisible = ref(false)
const reminderPopoverVisible = ref(false)
const unreadConsultationCount = ref(0)

const collapsed = computed(() => appStore.collapsed)
const isDoctorMode = computed(() => route.path.startsWith('/doctor') && (userStore.profile?.role === 'DOCTOR'))
const isAdminMode = computed(() => userStore.profile?.role === 'ADMIN')

const hasReminderPermission = computed(() => {
  if (!userStore.profile) return false
  return ['ADMIN', 'DOCTOR', 'FAMILY_ADMIN', 'MEMBER'].includes(userStore.profile.role)
})

const isReminderVisible = computed(() => userStore.profile && hasReminderPermission.value)
const isAdminVisibleInUser = computed(() => userStore.profile && userStore.profile.role === 'ADMIN')
const isFamilyDoctorVisible = computed(() => userStore.profile && ['FAMILY_ADMIN', 'DOCTOR'].includes(userStore.profile.role))

// Compute menu items based on role
const currentMenuItems = computed(() => {
  if (isDoctorMode.value) {
    return MENU_CONFIG.DOCTOR
  }
  if (isAdminMode.value && route.path.startsWith('/admin')) {
    return MENU_CONFIG.ADMIN
  }
  // Default to USER menu (which might include admin/reminder items based on permission)
  return MENU_CONFIG.USER.filter(item => {
    if (item.permission === 'REMINDER_VISIBLE') {
      return isReminderVisible.value
    }
    if (item.permission === 'ADMIN_VISIBLE') {
      return isAdminVisibleInUser.value
    }
    if (item.permission === 'FAMILY_DOCTOR_VISIBLE') {
      return isFamilyDoctorVisible.value
    }
    return true
  }).map(item => {
    const newItem = { ...item }
    if (newItem.path === '/doctor-consultation') {
      newItem.badge = unreadConsultationCount.value
    }
    return newItem
  })
})

const layoutTitle = computed(() => {
  if (isDoctorMode.value) return '医生端'
  if (isAdminMode.value && route.path.startsWith('/admin')) return '管理后台'
  return '家庭健康智能服务平台'
})

const doctorHeaderMap = {
  '/doctor/workbench': {
    title: '医生工作台',
    subtitle: '快速概览今日待办与服务数据',
    icon: House
  },
  '/doctor/patients': {
    title: '患者管理',
    subtitle: '管理患者档案与重点关注人群',
    icon: UserFilled
  },
  '/doctor/reports': {
    title: '体检报告与点评',
    subtitle: '查看患者报告并提供专业点评',
    icon: Document
  },
  '/doctor/consultation': {
    title: '在线咨询',
    subtitle: '实时响应患者咨询与随访',
    icon: ChatDotRound
  },
  '/doctor/enhanced-monitoring': {
    title: '健康监测与预警',
    subtitle: '追踪异常事件并及时处置',
    icon: Monitor
  },
  '/doctor/plans': {
    title: '健康计划与随访',
    subtitle: '制定随访计划并管理执行情况',
    icon: Notebook
  },
  '/doctor/analysis': {
    title: '数据统计与分析',
    subtitle: '洞察患者健康趋势与工作负载',
    icon: TrendCharts
  },
  '/doctor/ratings': {
    title: '服务评价与统计',
    subtitle: '了解患者反馈与服务评分',
    icon: StarFilled
  },
  '/doctor/settings': {
    title: '医生设置',
    subtitle: '配置通知、工作时间与偏好',
    icon: Setting
  }
}

const doctorHeader = computed(() => {
  if (!isDoctorMode.value) return null
  if (route.path === '/doctor' || route.path === '/doctor/bind') return null
  return doctorHeaderMap[route.path] || null
})

const logoIcon = computed(() => {
  if (isDoctorMode.value) return 'FirstAidKit'
  if (isAdminMode.value && route.path.startsWith('/admin')) return 'Operation'
  return 'House'
})

const avatarText = computed(() => {
  const name = userStore.profile?.nickname || userStore.profile?.phone || 'U'
  return String(name).slice(0, 1).toUpperCase()
})

const messageCount = computed(() => messageItems.value.length)
const reminderCount = computed(() => reminderItems.value.length)

const lastViewedReminderId = ref(localStorage.getItem('last_viewed_reminder_id'))

const shouldShowReminderBadge = computed(() => {
  if (reminderItems.value.length === 0) return false
  const latestItem = reminderItems.value[0]
  // Only show badge if the latest item's ID is different from the last viewed one
  return String(latestItem.id) !== String(lastViewedReminderId.value)
})

const handleReminderShow = async () => {
  await loadReminderPreview()
  if (reminderItems.value.length > 0) {
    const latestId = String(reminderItems.value[0].id)
    lastViewedReminderId.value = latestId
    localStorage.setItem('last_viewed_reminder_id', latestId)
  }
}

const toggleSider = () => {
  appStore.toggleCollapsed()
}

const goSettings = () => {
  const path = isDoctorMode.value ? '/doctor/settings' : '/settings'
  router.push(path)
}

const onLogout = () => {
  userStore.logout()
  doctorStore.reset()
  const loginPath = isDoctorMode.value ? '/doctor/login' : '/login'
  router.replace(loginPath)
}

const loadMessagePreview = async () => {
  if (messageLoading.value) return
  messageLoading.value = true
  try {
    const params = {
      page: 1,
      size: 5,
      scope: userStore.profile?.role === 'ADMIN' ? 'family' : 'self',
      familyId: userStore.currentFamily?.id
    }
    const res = await getConsultationHistory(params)
    const items = Array.isArray(res?.data) ? res.data : (res?.data?.items || res?.items || [])
    const replies = items
      .filter(item => item && item.answer)
      .map((item, idx) => ({
        id: item.id || `${item.sessionId || 'msg'}-${idx}`,
        content: item.answer,
        createdAt: item.createdAt
      }))
    messageItems.value = replies.slice(0, 5)
  } catch (e) {
    messageItems.value = []
  } finally {
    messageLoading.value = false
  }
}

const loadReminderPreview = async () => {
  if (reminderLoading.value) return
  reminderLoading.value = true
  try {
    const res = await getUserTodoItems()
    const items = Array.isArray(res?.data) ? res.data : (res?.data?.items || res?.items || [])
    if (items.length > 0) {
      reminderItems.value = items.slice(0, 5)
    } else {
      const fallback = await getUserReminders({ page: 1, size: 5 })
      const list = Array.isArray(fallback?.data) ? fallback.data : (fallback?.data?.items || fallback?.items || [])
      reminderItems.value = list.slice(0, 5)
    }
  } catch (e) {
    reminderItems.value = []
  } finally {
    reminderLoading.value = false
  }
}

const goReminderCenter = () => {
  reminderPopoverVisible.value = false
  router.push('/reminders')
}

const goMessageCenter = () => {
  messagePopoverVisible.value = false
  const path = isDoctorMode.value ? '/doctor/consultation' : '/consultation'
  router.push(path)
}

// Breadcrumb logic for Admin
const updateBreadcrumbs = (path) => {
  if (route.meta.title) {
    breadcrumbs.value = ['管理后台', route.meta.title]
  } else {
    breadcrumbs.value = ['管理后台']
  }
}

watch(
  () => route.path,
  (newPath) => {
    if (isAdminMode.value && newPath.startsWith('/admin')) {
      updateBreadcrumbs(newPath)
    }
  },
  { immediate: true }
)

const checkConsultationUnread = async () => {
  if (isDoctorMode.value) return

  const currentUserId = userStore.profile?.id || userStore.profile?.userId
  const familyId = userStore.currentFamily?.id || localStorage.getItem('current_family_id')

  if (currentUserId && familyId) {
    try {
      const res = await getOrCreateSession({
        patientUserId: currentUserId,
        familyId: familyId
      })
      if (res.data) {
        unreadConsultationCount.value = res.data.unreadCountPatient || 0
      }
    } catch (e) {
      console.error('Check unread failed', e)
    }
  }
}

onMounted(async () => {
  if (!userStore.profile) {
    profileLoading.value = true
    try {
      await userStore.fetchProfile()
    } catch (error) {
      console.error('Failed to fetch user profile', error)
      if (error?.response?.status === 401) {
        userStore.logout()
        doctorStore.reset()
        router.replace('/login')
      }
    } finally {
      profileLoading.value = false
    }
  }

  // Error listener from AppLayout
  window.addEventListener('hf-error', (evt) => {
    errorBanner.value = evt.detail
    setTimeout(() => { errorBanner.value = null }, 6000)
  })

  // loadMessagePreview() // Removed message popover load
  loadReminderPreview()
  checkConsultationUnread() // Initial check
  
  // Optional: Poll for unread messages every 30 seconds
  const pollTimer = setInterval(checkConsultationUnread, 30000)

  // Listen for read events
  const onConsultationRead = () => {
    unreadConsultationCount.value = 0
  }
  window.addEventListener('hf-consultation-read', onConsultationRead)
  
  // Cleanup
  return () => {
    clearInterval(pollTimer)
    window.removeEventListener('hf-consultation-read', onConsultationRead)
  }
})

function formatDetail(detail) {
  try {
    const d = typeof detail === 'string' ? detail : JSON.stringify(detail)
    return `${d}｜${dayjs().format('HH:mm:ss')}`
  } catch {
    return String(detail || '')
  }
}

function formatTime(value) {
  if (!value) return '--'
  return dayjs(value).format('MM-DD HH:mm')
}

function formatReminderTime(value) {
  if (!value) return '--'
  return dayjs(value).format('MM-DD HH:mm')
}

function getReminderStatusType(status) {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'SENT':
      return 'success'
    case 'ACKNOWLEDGED':
      return 'info'
    default:
      return 'info'
  }
}

function getReminderStatusName(status) {
  switch (status) {
    case 'PENDING':
      return '待发送'
    case 'SENT':
      return '已发送'
    case 'ACKNOWLEDGED':
      return '已确认'
    default:
      return '提醒'
  }
}

function renderMarkdown(content) {
  if (!content) return ''
  try {
    return DOMPurify.sanitize(marked.parse(content))
  } catch (e) {
    return content
  }
}
</script>

<style scoped lang="scss">
@use 'sass:map';
@use 'sass:color';
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.layout {
  height: 100vh;
  width: 100vw;
  background-color: vars.$gradient-bg;
  display: flex;
}

.content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background: transparent;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .toggle-btn {
      color: vars.$text-secondary-color;
      &:hover { color: vars.$primary-color; }
    }

    .page-title {
      font-size: 20px;
      font-weight: 700;
      margin: 0;
      letter-spacing: -0.5px;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-main-color, vars.$primary-color));
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .icon-btn {
      color: vars.$text-secondary-color;
      padding: 8px;
      border-radius: 12px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: visible; // Allow badge to overflow if needed, but usually badge is absolute
      
      &:hover {
        background: rgba(78, 161, 255, 0.1);
        color: vars.$primary-color;
        transform: translateY(-1px);
      }

      &:active {
        transform: translateY(1px);
      }
      
      :deep(.el-badge__content) {
        border: 2px solid #fff;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        height: 18px;
        line-height: 14px;
        padding: 0 5px;
        top: 2px;
        right: 4px;
      }
    }

    .divider {
      width: 1px;
      height: 24px;
      background: rgba(0, 0, 0, 0.1);
      margin: 0 8px;
    }

    .user-entry {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 20px;
      transition: all 0.3s;

      &:hover {
        background: rgba(255, 255, 255, 0.5);
      }

      .username {
        font-size: 14px;
        font-weight: 600;
        color: vars.$text-main-color;
      }
      
      .el-icon--right {
        color: vars.$text-secondary-color;
        font-size: 12px;
      }
    }
  }
}

.main {
  flex: 1;
  padding: 0 32px 32px 32px; /* Consistent padding */
  overflow-y: auto;
  overflow-x: hidden;
}

.doctor-main {
  :deep(.el-page-header) {
    display: none;
  }

  .doctor-header {
    display: flex;
    align-items: center;
    margin: 8px 0 24px;
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
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Transitions */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>

<style lang="scss">
@use '../../styles/variables' as vars;

/* Global styles for Reminder Popover to override Element Plus defaults */
.el-popover.reminder-popper {
  --el-popover-border-radius: 16px;
  --el-popover-padding: 0px;
  border: 1px solid rgba(78, 161, 255, 0.1) !important;
  box-shadow: 0 10px 40px -10px rgba(31, 41, 55, 0.15) !important;
  
  .popover-panel.reminder-popover {
    background: #fff;
    overflow: hidden;
    border-radius: 16px;
    
    .popover-header {
      padding: 16px 20px;
      background: rgba(249, 250, 251, 0.8);
      border-bottom: 1px solid rgba(0, 0, 0, 0.04);
      display: flex;
      align-items: center;
      justify-content: space-between;

      .header-title {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 15px;
        font-weight: 600;
        color: vars.$text-main-color;

        .header-icon {
          width: 32px;
          height: 32px;
          border-radius: 10px;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, rgba(78, 161, 255, 0.1), rgba(78, 161, 255, 0.2));
          color: vars.$primary-color;
          box-shadow: 0 2px 6px rgba(78, 161, 255, 0.15);
        }
      }
    }
    
    .reminder-list {
      padding: 12px;
      background: #f9fafb;
      max-height: 400px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 12px;

      &::-webkit-scrollbar {
        width: 4px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.1);
        border-radius: 4px;
        
        &:hover {
          background: rgba(0, 0, 0, 0.2);
        }
      }
      
      .popover-item.reminder-item {
        background: #fff;
        border: 1px solid rgba(229, 231, 235, 0.5);
        padding: 12px 14px 12px 14px; /* Default padding */
        padding-left: 14px; /* Ensure left padding */
        border-radius: 12px;
        position: relative;
        cursor: pointer;
        transition: all 0.3s ease;
        overflow: hidden;

        /* Status strip */
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 4px;
          background: vars.$info-color;
          opacity: 0.6;
          transition: all 0.3s;
        }

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
          border-color: rgba(78, 161, 255, 0.2);

          &::before {
            opacity: 1;
            width: 6px;
          }
        }

        &.status-pending::before {
          background: vars.$warning-color;
        }

        &.status-sent::before {
          background: vars.$success-color;
        }

        &.status-acknowledged::before {
          background: vars.$primary-color;
        }

        .reminder-top {
          display: flex;
          align-items: flex-start;
          gap: 12px;
        }

        .reminder-icon {
          width: 36px;
          height: 36px;
          flex-shrink: 0;
          border-radius: 12px;
          background: rgba(255, 168, 102, 0.1);
          color: vars.$warning-color;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.3s;
        }
        
        &:hover .reminder-icon {
          background: rgba(255, 168, 102, 0.2);
          transform: scale(1.05);
        }

        .reminder-main {
          flex: 1;
          min-width: 0;
        }

        .item-title {
          font-size: 13px;
          font-weight: 600;
          color: vars.$text-main-color;
          margin-bottom: 4px;
          display: block;
          /* Remove truncation for title to show full context if needed, or keep single line but ensure it fits */
          white-space: normal; 
          line-height: 1.4;
        }

        .item-content {
          margin-top: 4px;
          font-size: 12px;
          color: vars.$text-secondary-color;
          line-height: 1.5;
          /* Remove clamping to show full content as requested */
          display: block; 
          overflow: visible;
        }

        .reminder-footer {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-top: 8px;
          padding-top: 8px;
          border-top: 1px dashed rgba(0,0,0,0.05);
        }

        .reminder-time {
          font-size: 12px;
          color: #9ca3af;
          background: transparent;
          padding: 0;
          border: none;
          display: flex;
          align-items: center;
          gap: 4px;
          
          &::before {
            content: '';
            display: inline-block;
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: #d1d5db;
          }
        }
      }
    }
  }
}

/* Removed message popper styles as it is no longer used in header */

</style>
