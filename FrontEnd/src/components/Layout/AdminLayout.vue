<template>
  <div v-if="loading" class="loading" style="display: flex; align-items: center; justify-content: center; height: 100vh;">
    <el-skeleton :rows="5" animated />
  </div>
  <div class="admin-layout" v-else-if="isAdminRole">
    <!-- 背景装饰圆 -->
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>
    
    <!-- 侧边栏 -->
    <el-aside class="sidebar" :width="collapsed ? '64px' : '200px'">
      <div class="logo">
        <el-icon v-if="collapsed"><Operation /></el-icon>
        <template v-else>
          <el-icon><Operation /></el-icon>
          <span>管理后台</span>
        </template>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :unique-opened="true"
        :router="true"
        class="menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><House /></el-icon>
          <template #title>仪表板</template>
        </el-menu-item>

        <!-- 用户管理 -->
        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">用户列表</el-menu-item>
          <el-menu-item index="/admin/users/audit">用户审核</el-menu-item>
          <el-menu-item index="/admin/users/roles">角色管理</el-menu-item>
          <el-menu-item index="/admin/users/logs">活动日志</el-menu-item>
        </el-sub-menu>

        <!-- 家庭管理 -->
        <el-sub-menu index="family">
          <template #title>
            <el-icon><House /></el-icon>
            <span>家庭管理</span>
          </template>
          <el-menu-item index="/admin/families">家庭列表</el-menu-item>
          <el-menu-item index="/admin/families/audit">家庭审核</el-menu-item>
          <el-menu-item index="/admin/families/members">成员管理</el-menu-item>
          <el-menu-item index="/admin/families/stats">数据统计</el-menu-item>
        </el-sub-menu>

        <!-- 健康数据管理 -->
        <el-sub-menu index="health">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>健康数据</span>
          </template>
          <el-menu-item index="/admin/health/logs">健康日志</el-menu-item>
          <el-menu-item index="/admin/health/assessments">体质测评</el-menu-item>
          <el-menu-item index="/admin/health/reminders">健康提醒</el-menu-item>
          <el-menu-item index="/admin/health/recommendations">AI建议</el-menu-item>
        </el-sub-menu>

        <!-- 医生管理 -->
        <el-sub-menu index="doctor">
          <template #title>
            <el-icon><User /></el-icon>
            <span>医生管理</span>
          </template>
          <el-menu-item index="/admin/doctors">医生列表</el-menu-item>
          <el-menu-item index="/admin/doctors/audit">资质审核</el-menu-item>
          <el-menu-item index="/admin/doctors/collaboration">协作监控</el-menu-item>
          <el-menu-item index="/admin/doctors/stats">服务统计</el-menu-item>
        </el-sub-menu>

        <!-- 系统监控 -->
        <el-sub-menu index="monitoring">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>系统监控</span>
          </template>
          <el-menu-item index="/admin/monitoring/system">系统监控</el-menu-item>
          <el-menu-item index="/admin/monitoring/users">用户活动</el-menu-item>
          <el-menu-item index="/admin/monitoring/reports">数据报告</el-menu-item>
          <el-menu-item index="/admin/monitoring/custom">自定义报告</el-menu-item>
        </el-sub-menu>

        <!-- 系统配置 -->
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </template>
          <el-menu-item index="/admin/system/config">系统配置</el-menu-item>
          <el-menu-item index="/admin/system/security">安全设置</el-menu-item>
          <el-menu-item index="/admin/system/audit">操作审计</el-menu-item>
          <el-menu-item index="/admin/system/backup">数据备份</el-menu-item>
        </el-sub-menu>

        <!-- 系统运维（管理员视角） -->
        <el-menu-item index="/admin/ops">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>系统运维</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button link @click="toggleSider">
            <el-icon>
              <Fold v-if="!collapsed" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
              {{ item }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown>
            <div class="user-info">
              <el-avatar :size="32" :icon="User" />
              <span class="nickname">{{ userStore.profile?.nickname || '管理员' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goProfile">个人资料</el-dropdown-item>
                <el-dropdown-item @click="goSettings">系统设置</el-dropdown-item>
                <el-dropdown-item @click="onLogout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
  <div v-else class="no-permission" style="display: flex; align-items: center; justify-content: center; height: 100vh;">
    <el-result icon="error" title="无权限访问" sub-title="您没有管理员权限，无法访问管理后台。请联系系统管理员。">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { 
  Operation, House, User, DataAnalysis, Monitor, Setting,
  Fold, Expand, ArrowDown, User as UserIcon, FirstAidKit, Cpu
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
const breadcrumbs = ref(['首页'])
const loading = ref(true)

// 检查用户是否具有ADMIN角色
const isAdminRole = computed(() => {
  return userStore.profile?.role === 'ADMIN'
})

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 更新面包屑
const updateBreadcrumbs = (path) => {
  const pathMap = {
    '/admin/dashboard': ['管理后台', '仪表板'],
    '/admin/users': ['管理后台', '用户管理', '用户列表'],
    '/admin/users/audit': ['管理后台', '用户管理', '用户审核'],
    '/admin/users/roles': ['管理后台', '用户管理', '角色管理'],
    '/admin/users/logs': ['管理后台', '用户管理', '活动日志'],
    '/admin/families': ['管理后台', '家庭管理', '家庭列表'],
    '/admin/families/audit': ['管理后台', '家庭管理', '家庭审核'],
    '/admin/families/members': ['管理后台', '家庭管理', '成员管理'],
    '/admin/families/stats': ['管理后台', '家庭管理', '数据统计'],
    '/admin/health/logs': ['管理后台', '健康数据', '健康日志'],
    '/admin/health/assessments': ['管理后台', '健康数据', '体质测评'],
    '/admin/health/reminders': ['管理后台', '健康数据', '健康提醒'],
    '/admin/health/recommendations': ['管理后台', '健康数据', 'AI建议'],
    '/admin/doctors': ['管理后台', '医生管理', '医生列表'],
    '/admin/doctors/audit': ['管理后台', '医生管理', '资质审核'],
    '/admin/doctors/collaboration': ['管理后台', '医生管理', '协作监控'],
    '/admin/doctors/stats': ['管理后台', '医生管理', '服务统计'],
    '/admin/monitoring/system': ['管理后台', '系统监控', '系统监控'],
    '/admin/monitoring/users': ['管理后台', '系统监控', '用户活动'],
    '/admin/monitoring/reports': ['管理后台', '系统监控', '数据报告'],
    '/admin/monitoring/custom': ['管理后台', '系统监控', '自定义报告'],
    '/admin/system/config': ['管理后台', '系统配置', '系统配置'],
    '/admin/system/security': ['管理后台', '系统配置', '安全设置'],
    '/admin/system/audit': ['管理后台', '系统配置', '操作审计'],
    '/admin/system/backup': ['管理后台', '系统配置', '数据备份'],
    '/admin/ops': ['管理后台', '系统运维']
  }
  
  // 确保 breadcrumbs.value 是数组类型
  breadcrumbs.value = Array.isArray(pathMap[path]) ? pathMap[path] : ['管理后台', '仪表板']
}

// 监听路由变化，更新面包屑
watch(
  () => route.path,
  (newPath) => {
    updateBreadcrumbs(newPath)
  },
  { immediate: true }
)

// 在组件挂载时确保用户信息已加载
onMounted(async () => {
  if (!userStore.profile) {
    try {
      await userStore.fetchProfile()
      console.log('[AdminLayout] User profile loaded:', userStore.profile)
    } catch (error) {
      console.error('[AdminLayout] Failed to fetch user profile:', error)
      ElMessage.error('获取用户信息失败，请重新登录')
      router.replace('/login')
    }
  }
  loading.value = false
})

// 切换侧边栏
const toggleSider = () => {
  collapsed.value = !collapsed.value
}

// 个人资料
const goProfile = () => {
  // 管理员个人资料页面
  router.push('/settings')
}

// 系统设置
const goSettings = () => {
  router.push('/settings')
}

// 返回首页
const goHome = () => {
  router.push('/home')
}

// 退出登录
const onLogout = () => {
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped lang="scss">
@use 'sass:map';
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.admin-layout {
  height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;

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

  .sidebar {
    /* Glassmorphism */
    @include mixins.glass-effect;
    border-right: 1px solid rgba(255, 255, 255, 0.3);
    margin: 16px 0 16px 16px;
    border-radius: vars.$radius-lg;
    height: calc(100vh - 32px);
    
    /* Ensure content is above glass */
    z-index: 10;
    
    transition: width 0.4s vars.$ease-spring;
    overflow-x: hidden;
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      padding: 0 16px 0 28px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.3);
      font-size: 18px;
      font-weight: bold;
      color: vars.$primary-color;

      .el-icon {
        margin-right: 8px;
        color: inherit;
        transition: transform 0.5s vars.$ease-spring;
      }
      
      &:hover .el-icon {
        transform: rotate(360deg);
      }
    }

    &.is-collapsed .logo {
      padding: 0;
      justify-content: center;
      
      .el-icon {
        margin-right: 0;
      }
    }

    .menu {
      border: none;
      height: calc(100vh - 92px); /* Adjust for logo and padding */
      background: transparent;
      
      /* Menu item wave animation */
      :deep(.el-menu-item) {
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
  }

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    
    /* Glassmorphism */
    @include mixins.glass-effect;
    margin: 16px 16px 0 16px;
    border-radius: vars.$radius-lg;
    height: 60px;
    z-index: 10;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .el-breadcrumb {
        margin-left: 16px;
      }
    }

    .header-right {
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;

        .nickname {
          margin-right: 8px;
        }
      }
    }
  }

  .main-content {
    background: transparent;
    padding: 16px;
    z-index: 1;
    overflow: auto;
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
</style>