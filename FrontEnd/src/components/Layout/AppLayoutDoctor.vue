<template>
  <el-container class="layout" :class="{ 'theme-dark': appStore.theme === 'dark', 'theme-light': appStore.theme === 'light' }">
    <!-- 背景装饰圆 -->
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>
    
    <el-aside :width="collapsed ? '0' : '220px'" class="sider" :class="{ 'sider-collapsed': collapsed }">
      <div class="logo">
        <el-icon size="28" class="logo-icon"><FirstAidKit /></el-icon>
        <transition name="fade"><span v-if="!collapsed" class="logo-title">医生端</span></transition>
      </div>
      <el-menu :default-active="activePath" class="menu" :router="false" @select="onMenuSelect">
        <el-menu-item index="/doctor/workbench"><el-icon><HomeFilled /></el-icon><span>医生工作台</span></el-menu-item>
        <el-menu-item index="/doctor/patients"><el-icon><UserFilled /></el-icon><span>患者管理</span></el-menu-item>
        <el-menu-item index="/doctor/consultation"><el-icon><ChatDotRound /></el-icon><span>在线咨询</span></el-menu-item>
        <el-menu-item index="/doctor/enhanced-monitoring"><el-icon><DataAnalysis /></el-icon><span>健康监测与预警</span></el-menu-item>
        <el-menu-item index="/doctor/plans"><el-icon><Notebook /></el-icon><span>健康计划与随访</span></el-menu-item>
        <el-menu-item index="/doctor/analysis"><el-icon><DataAnalysis /></el-icon><span>数据统计与分析</span></el-menu-item>
        <el-menu-item index="/doctor/settings"><el-icon><Setting /></el-icon><span>系统设置</span></el-menu-item>
        <el-menu-item index="/doctor/bind"><el-icon><UserFilled /></el-icon><span>家庭医生对接</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <el-button link @click="toggleSider"><el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon></el-button>
        <el-button link @click="appStore.toggleTheme()" class="theme-btn"><el-icon><Sunny v-if="appStore.theme === 'light'" /><Moon v-else /></el-icon></el-button>
        <div class="spacer" />
        <el-dropdown>
          <span class="user-entry">
            <el-avatar size="small" :src="userStore.profile?.avatar">{{ avatarText }}</el-avatar>
            <span class="username">{{ userStore.profile?.nickname || userStore.profile?.phone || '医生用户' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goSettings">系统设置</el-dropdown-item>
              <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore, useDoctorStore } from '../../stores'
import { useUserStore } from '../../stores/user'
import { Sunny, Moon } from '@element-plus/icons-vue'
import { HomeFilled, UserFilled, DataAnalysis, Notebook, Setting, Fold, Expand, House, ChatDotRound, FirstAidKit } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const doctorStore = useDoctorStore()

const collapsed = computed(() => appStore.collapsed)
const activePath = computed(() => route.path)
const avatarText = computed(() => {
  const name = userStore.profile?.nickname || userStore.profile?.phone || 'U'
  return String(name).slice(0, 1).toUpperCase()
})

const toggleSider = () => { appStore.toggleCollapsed() }
const goSettings = () => { router.push('/doctor/settings') }
const onLogout = () => {
  userStore.logout()
  doctorStore.reset() // 重置医生端状态
  router.replace('/doctor/login')
}

// 菜单选择处理，确保路由正确跳转
const onMenuSelect = (index) => {
  if (route.path !== index) {
    router.push(index)
  }
}

onMounted(() => { if (!userStore.profile) { userStore.fetchProfile().catch(() => {}) } })
</script>
<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.layout {
  height: 100vh;
  position: relative;
  overflow: hidden;
  background-image: 
    radial-gradient(var(--el-color-primary-light-9) 1px, transparent 1px),
    radial-gradient(var(--el-color-primary-light-9) 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
}

/* 背景装饰圆 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.2), rgba(64, 158, 255, 0));
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(103, 194, 58, 0.15), rgba(103, 194, 58, 0));
  bottom: -50px;
  left: -50px;
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, 20px); }
}

.sider {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.3);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.02);

  &.sider-collapsed {
    .logo {
      padding: 0;
      justify-content: center;
      
      .logo-icon {
        margin-right: 0;
      }
    }
  }
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  color: var(--el-color-primary);
  font-weight: bold;
  font-size: 18px;
  white-space: nowrap;
  overflow: hidden;
  transition: all 0.3s;
  border-bottom: 1px solid rgba(0, 0, 0, 0.03);

  .logo-icon {
    margin-right: 12px;
    transition: all 0.3s;
    filter: drop-shadow(0 2px 4px rgba(64, 158, 255, 0.3));
  }
  
  .logo-title {
    font-family: 'PingFang SC', sans-serif;
    letter-spacing: 1px;
    background: linear-gradient(120deg, var(--el-color-primary), #67c23a);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

.menu {
  border-right: none;
  background: transparent;
  flex: 1;
  padding: 12px 0;
  
  :deep(.el-menu-item) {
    margin: 4px 12px;
    border-radius: 8px;
    height: 48px;
    line-height: 48px;
    color: var(--el-text-color-regular);
    transition: all 0.3s vars.$ease-spring;
    
    &:hover {
      background: rgba(64, 158, 255, 0.08);
      color: var(--el-color-primary);
      transform: translateX(4px);
    }
    
    &.is-active {
      background: linear-gradient(90deg, rgba(64, 158, 255, 0.15), rgba(64, 158, 255, 0.05));
      color: var(--el-color-primary);
      font-weight: 600;
      box-shadow: inset 3px 0 0 var(--el-color-primary);
      
      .el-icon {
        color: var(--el-color-primary);
        transform: scale(1.1);
      }
    }
    
    .el-icon {
      transition: all 0.3s;
    }
  }
}

.header {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  z-index: 9;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
  
  .el-button {
    font-size: 20px;
    color: var(--el-text-color-primary);
    padding: 8px;
    border-radius: 8px;
    transition: all 0.3s;
    
    &:hover {
      background: rgba(0, 0, 0, 0.03);
      color: var(--el-color-primary);
    }
  }
}

.spacer {
  flex: 1;
}

.user-entry {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s;
  
  &:hover {
    background: rgba(0, 0, 0, 0.03);
  }

  .el-avatar {
    background: linear-gradient(135deg, var(--el-color-primary), #66b1ff);
    border: 2px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  }

  .username {
    margin-left: 10px;
    font-size: 14px;
    color: var(--el-text-color-primary);
    font-weight: 500;
  }
}

.main {
  padding: 0;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  z-index: 1;
  
  /* 自定义滚动条 */
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(144, 147, 153, 0.3);
    border-radius: 3px;
    
    &:hover {
      background: rgba(144, 147, 153, 0.5);
    }
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
}

/* 深色模式适配 */
.theme-dark {
  .layout {
    background: linear-gradient(135deg, #1a1a1a 0%, #0d1117 100%);
  }
  
  .sider {
    background: rgba(30, 30, 30, 0.8);
    border-right-color: rgba(255, 255, 255, 0.05);
  }
  
  .header {
    background: rgba(30, 30, 30, 0.7);
    border-bottom-color: rgba(255, 255, 255, 0.05);
    
    .el-button:hover {
      background: rgba(255, 255, 255, 0.05);
    }
  }
  
  .menu :deep(.el-menu-item):hover {
    background: rgba(255, 255, 255, 0.05);
  }
  
  .user-entry:hover {
    background: rgba(255, 255, 255, 0.05);
  }
}
</style>
