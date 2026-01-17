<template>
  <el-container class="layout" :class="{ 'theme-dark': appStore.theme === 'dark', 'theme-light': appStore.theme === 'light' }">
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>
    <el-aside :width="collapsed ? '0' : '220px'" class="sider" :class="{ 'sider-collapsed': collapsed }">
      <div class="logo">
        <el-icon size="28">
          <House />
        </el-icon>
        <transition name="fade">
          <span v-if="!collapsed" class="logo-title">{{ isDoctorMode ? '医生端' : '健康家庭' }}</span>
        </transition>
      </div>
      <el-menu v-if="isDoctorMode" :default-active="activePath" class="menu" router>
        <el-menu-item index="/doctor/workbench">
          <el-icon><HomeFilled /></el-icon>
          <span>医生工作台</span>
        </el-menu-item>
        <el-menu-item index="/doctor/patients">
          <el-icon><UserFilled /></el-icon>
          <span>患者管理</span>
        </el-menu-item>
        <el-menu-item index="/doctor/consultation">
          <el-icon><ChatDotRound /></el-icon>
          <span>在线咨询</span>
        </el-menu-item>
        <el-menu-item index="/doctor/monitoring">
          <el-icon><DataAnalysis /></el-icon>
          <span>健康监测与预警</span>
        </el-menu-item>
        <el-menu-item index="/doctor/plans">
          <el-icon><Notebook /></el-icon>
          <span>健康计划与随访</span>
        </el-menu-item>
        <el-menu-item index="/doctor/analysis">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计与分析</span>
        </el-menu-item>
        <el-menu-item index="/doctor/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
        <el-menu-item index="/doctor/bind">
          <el-icon><UserFilled /></el-icon>
          <span>家庭医生对接</span>
        </el-menu-item>
      </el-menu>
      <el-menu v-else :default-active="activePath" class="menu" router>
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/families">
          <el-icon><UserFilled /></el-icon>
          <span>家庭管理</span>
        </el-menu-item>
        <el-sub-menu>
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>中医体质</span>
          </template>
          <el-menu-item index="/tcm/assessments">
            <el-icon><DataAnalysis /></el-icon>
            <span>体质测评</span>
          </el-menu-item>
          <el-menu-item index="/tcm/trend">
            <el-icon><TrendCharts /></el-icon>
            <span>体质趋势分析</span>
          </el-menu-item>
          <el-menu-item index="/tcm/ai">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI体质测评</span>
          </el-menu-item>
          <el-menu-item index="/tcm/personalized-plan">
            <el-icon><DataAnalysis /></el-icon>
            <span>个性化方案</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/logs">
          <el-icon><Notebook /></el-icon>
          <span>健康日志</span>
        </el-menu-item>
        <el-menu-item index="/recommendations">
          <el-icon><MagicStick /></el-icon>
          <span>个性化建议</span>
        </el-menu-item>
        <el-menu-item index="/lifestyle">
          <el-icon><DataAnalysis /></el-icon>
          <span>生活方式</span>
        </el-menu-item>
        <el-menu-item index="/doctor-consultation">
          <el-icon><ChatDotRound /></el-icon>
          <span>在线咨询</span>
        </el-menu-item>
        <el-menu-item index="/consultation">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能咨询</span>
        </el-menu-item>
        <el-menu-item index="/reminders" v-if="isReminderVisible">
          <el-icon><Bell /></el-icon>
          <span>健康提醒</span>
        </el-menu-item>
        <el-menu-item index="/collaboration">
          <el-icon><Notebook /></el-icon>
          <span>家庭协作</span>
        </el-menu-item>

        <el-menu-item index="/family-doctor">
          <el-icon><UserFilled /></el-icon>
          <span>家庭医生对接</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
        <el-menu-item index="/security">
          <el-icon><Setting /></el-icon>
          <span>数据安全与隐私</span>
        </el-menu-item>
        <el-menu-item index="/ops" v-if="isAdminVisible">
          <el-icon><DataAnalysis /></el-icon>
          <span>系统运维</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div v-if="errorBanner" class="error-banner">
          <el-alert :title="errorBanner.action" :description="formatDetail(errorBanner.detail)" type="error" show-icon closable @close="errorBanner=null" />
        </div>
        <el-button link @click="toggleSider">
          <el-icon>
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
        </el-button>
        <el-button link @click="appStore.toggleTheme()" class="theme-btn">
          <el-icon>
            <Sunny v-if="appStore.theme === 'light'" />
            <Moon v-else />
          </el-icon>
        </el-button>
        <div class="spacer" />
        <el-dropdown>
          <span class="user-entry">
            <el-avatar v-if="userStore.profile && !profileLoading" size="small" :src="userStore.profile?.avatar">{{ avatarText }}</el-avatar>
            <el-avatar v-else size="small" :icon="UserFilled" />
            <span class="username">{{ profileLoading ? '加载中...' : (userStore.profile?.nickname || userStore.profile?.phone || '健康家庭用户') }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goSettings">账号设置</el-dropdown-item>
              <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useDoctorStore } from '../../stores'
import { useUserStore } from '../../stores/user'
import { Sunny, Moon } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  HomeFilled,
  UserFilled,
  DataAnalysis,
  Notebook,
  MagicStick,
  Setting,
  Fold,
  Expand,
  House,
  ChatDotRound,
  Bell,
  TrendCharts,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const doctorStore = useDoctorStore()
const errorBanner = ref(null)
const profileLoading = ref(false)

const collapsed = computed(() => appStore.collapsed)
const activePath = computed(() => route.path)
const isDoctorMode = computed(() => route.path.startsWith('/doctor') && (userStore.profile?.role === 'DOCTOR'))
const hasReminderPermission = computed(() => {
  if (!userStore.profile) {
    return false
  }
  return ['ADMIN', 'DOCTOR', 'FAMILY_ADMIN'].includes(userStore.profile.role)
})
const isReminderVisible = computed(() => {
  // 只有当用户信息已加载且有权限时才显示
  return userStore.profile && hasReminderPermission.value
})

const isAdminVisible = computed(() => {
  // 只有ADMIN角色的用户才能看到系统运维菜单
  return userStore.profile && userStore.profile.role === 'ADMIN'
})
const avatarText = computed(() => {
  const name = userStore.profile?.nickname || userStore.profile?.phone || 'U'
  return String(name).slice(0, 1).toUpperCase()
})

const toggleSider = () => {
  appStore.toggleCollapsed()
}

const goSettings = () => {
  router.push('/settings')
}

const onLogout = () => {
  userStore.logout()
  doctorStore.reset() // 重置医生端状态
  router.replace('/login')
}

onMounted(async () => {
  // 检查是否已经有用户信息，如果没有则加载
  if (!userStore.profile) {
    profileLoading.value = true
    try {
      await userStore.fetchProfile()
    } catch (error) {
      console.error('Failed to fetch user profile', error)
      // 如果获取用户信息失败，可能需要重新登录
      if (error?.response?.status === 401) {
        userStore.logout()
        doctorStore.reset() // 重置医生端状态
        router.replace('/login')
      }
    } finally {
      profileLoading.value = false
    }
  }
  window.addEventListener('hf-error', (evt) => {
    errorBanner.value = evt.detail
    setTimeout(() => { errorBanner.value = null }, 6000)
  })
})

function formatDetail(detail) {
  try {
    const d = typeof detail === 'string' ? detail : JSON.stringify(detail)
    return `${d}｜${dayjs().format('HH:mm:ss')}`
  } catch {
    return String(detail || '')
  }
}
</script>

<style scoped lang="scss">
@use 'sass:map';
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.layout {
  height: 100vh;
  position: relative;
  overflow: hidden;
}

/* 背景装饰圆 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
  pointer-events: none;
}
.circle-1 {
  width: 400px;
  height: 400px;
  background: rgba(64, 158, 255, 0.2);
  top: -100px;
  left: -100px;
}
.circle-2 {
  width: 500px;
  height: 500px;
  background: rgba(54, 207, 201, 0.15);
  bottom: -150px;
  right: -150px;
}

.sider {
  transition: width 0.4s vars.$ease-spring;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  z-index: 10;
  /* Glassmorphism for sider */
  @include mixins.glass-effect;
  border-right: 1px solid rgba(255, 255, 255, 0.3);
  margin: 16px 0 16px 16px;
  border-radius: vars.$radius-lg;
  height: calc(100vh - 32px);
  
  /* Menu item wave animation */
  :deep(.el-menu-item), :deep(.el-sub-menu__title) {
    border-radius: 8px;
    margin: 4px 8px;
    transition: all 0.3s vars.$ease-spring;
    height: 48px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.5);
      transform: translateX(4px);
      
      .el-icon {
        transform: rotate(15deg) scale(1.1);
      }
    }
    
    &.is-active {
      background: vars.$gradient-primary;
      color: #fff;
      box-shadow: vars.$shadow-sm;
      
      .el-icon {
        color: #fff;
        animation: bounce 0.5s;
      }
    }
    
    .el-icon {
      transition: transform 0.3s ease;
    }
  }
  
  /* Staggered animation for menu items */
  @for $i from 1 through 20 {
    :deep(.el-menu-item:nth-child(#{$i})) {
      animation: slideIn 0.4s ease backwards;
      animation-delay: #{$i * 0.05}s;
    }
  }
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  font-size: 18px;
  font-weight: 700;
  color: vars.$primary-color;
  
  .el-icon {
    transition: transform 0.5s vars.$ease-spring;
  }
  
  &:hover .el-icon {
    transform: rotate(360deg);
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.logo-title {
  white-space: nowrap;
}

.menu { border-right: none; background: transparent; flex: 1; }

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 10;
  /* Glassmorphism for header */
  @include mixins.glass-effect;
  margin: 16px 16px 0 16px;
  border-radius: 20px;
  height: 60px;
  padding: 0 20px;
}

.error-banner { position: fixed; left: 240px; right: 16px; top: 8px; z-index: 1000 }

.spacer {
  flex: 1;
}

.user-entry { display: inline-flex; align-items: center; gap: 8px; cursor: pointer; }

.username {
  font-size: 14px;
}

.main {
  overflow: auto;
  z-index: 1;
  padding: 0; /* Remove padding here, let child handle it or add margin */
}

.theme-btn { margin-left: 8px }

/* Light Theme Overrides */
.theme-light .sider {
  /* Background handled by mixin, but ensure text color */
  color: vars.$text-main-color;
}
.theme-light .menu, .theme-light .username { color: vars.$text-regular-color }
.theme-light .logo, .theme-light .logo .el-icon { color: vars.$primary-color }
.theme-light .header {
  /* Background handled by mixin */
}
.theme-light .main {
  background: transparent; /* Allow body gradient to show */
}

.theme-dark .sider { background: #001529; color: #ffffff }
.theme-dark .menu, .theme-dark .username { color: #cfd3dc }
.theme-dark .logo, .theme-dark .logo .el-icon { color: #ffffff }
.theme-dark .header { background: #0f172a; border-bottom: 1px solid #1f2937 }
.theme-dark .main { background: #0b1220 }

.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.5s vars.$ease-spring;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.user-profile-compact {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.8);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .user-name {
    font-size: 14px;
    font-weight: 600;
    color: vars.$text-main-color;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .user-role {
    font-size: 12px;
    color: vars.$text-secondary-color;
  }
}

.sidebar-avatar {
  border: 2px solid #fff;
  background: vars.$gradient-primary;
}

.more-icon {
  color: vars.$text-secondary-color;
}
</style>

.sider-collapsed { border-right: none }
.sider-collapsed .menu, .sider-collapsed .logo { display: none }
