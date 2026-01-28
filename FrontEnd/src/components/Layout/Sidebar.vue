<template>
  <el-aside :width="collapsed ? '80px' : '280px'" class="sider" :class="{ 'sider-collapsed': collapsed }">
    <div class="logo">
      <el-icon size="32" class="logo-icon">
        <component :is="logoIconComponent" />
      </el-icon>
      <transition name="fade">
        <span v-if="!collapsed" class="logo-title">{{ title }}</span>
      </transition>
    </div>
    
    <el-menu
      :default-active="activePath"
      class="menu"
      :collapse="collapsed"
      :unique-opened="true"
      router
    >
      <template v-for="item in menuItems" :key="item.path || item.title">
        <!-- Submenu -->
        <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path || item.title">
          <template #title>
            <el-icon v-if="item.icon">
              <component :is="getIcon(item.icon)" />
            </el-icon>
            <span>{{ item.title }}</span>
            <el-badge 
              v-if="item.badge > 0" 
              :value="item.badge" 
              :max="99" 
              class="menu-badge"
            />
          </template>
          <el-menu-item 
            v-for="child in item.children" 
            :key="child.path" 
            :index="child.path"
          >
            <el-icon v-if="child.icon">
              <component :is="getIcon(child.icon)" />
            </el-icon>
            <span v-else-if="!child.icon && item.icon" style="width: 24px; display: inline-block;"></span>
            <span>{{ child.title }}</span>
            <el-badge 
              v-if="child.badge > 0" 
              :value="child.badge" 
              :max="99" 
              class="menu-badge"
            />
          </el-menu-item>
        </el-sub-menu>

        <!-- Menu Item -->
        <el-menu-item v-else :index="item.path">
          <el-icon v-if="item.icon">
            <component :is="getIcon(item.icon)" />
          </el-icon>
          <template #title>
            <span>{{ item.title }}</span>
            <el-badge 
              v-if="item.badge > 0" 
              :value="item.badge" 
              :max="99" 
              class="menu-badge"
            />
          </template>
        </el-menu-item>
      </template>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import * as Icons from '@element-plus/icons-vue'

const props = defineProps({
  menuItems: {
    type: Array,
    required: true,
    default: () => []
  },
  collapsed: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '健康家庭'
  },
  logoIcon: {
    type: String,
    default: 'House'
  }
})

const route = useRoute()
const activePath = computed(() => route.path)

const logoIconComponent = computed(() => {
  return Icons[props.logoIcon] || Icons.House
})

const getIcon = (name) => {
  return Icons[name] || null
}
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.sider {
  transition: width 0.3s vars.$ease-spring;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  z-index: 10;
  background: #fff;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  height: 100vh;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
  
  &.sider-collapsed {
    .logo {
      padding: 0;
      justify-content: center;
      .logo-icon {
        margin-right: 0;
      }
    }
  }

  /* Menu Item Styling */
  :deep(.el-menu) {
    border-right: none;
    background: transparent;
    padding: 16px;
  }

  :deep(.menu-badge) {
    margin-left: auto;
    margin-right: 8px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    
    .el-badge__content {
      background-color: vars.$danger-color;
      border: none;
      box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
      position: static;
      transform: none;
      font-size: 12px;
      min-width: 18px;
      height: 18px;
      line-height: 18px;
      padding: 0 5px;
    }
  }

  :deep(.el-menu-item), :deep(.el-sub-menu__title) {
    border-radius: 12px;
    margin-bottom: 8px;
    height: 50px;
    line-height: 50px;
    color: vars.$text-secondary-color;
    font-weight: 500;
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(78, 161, 255, 0.08);
      color: vars.$primary-color;
      
      .el-icon {
        color: vars.$primary-color;
      }
    }
    
    &.is-active {
      background: rgba(78, 161, 255, 0.15); /* Light blue background */
      color: vars.$primary-color;
      font-weight: 600;
      
      .el-icon {
        color: vars.$primary-color;
      }

      /* Add the small blue indicator pill/bar if desired, but image shows full pill bg */
    }
    
    .el-icon {
      font-size: 20px;
      margin-right: 12px;
      color: #909399;
      transition: color 0.3s;
    }
  }
}

.logo {
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  font-size: 20px;
  font-weight: 700;
  color: vars.$text-main-color;
  white-space: nowrap;
  overflow: hidden;
  
  .logo-title {
    font-size: 18px; /* Slightly reduced font size */
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: inline-block;
    max-width: 200px; /* Ensure it fits */
    @include mixins.text-gradient(linear-gradient(to right, vars.$primary-color, vars.$success-color));
    font-weight: 800;
  }

  .logo-icon {
    margin-right: 12px;
    flex-shrink: 0;
    color: vars.$primary-color;
    /* Gradient on SVG requires defs or mask, keeping solid primary for visibility/compatibility */
    filter: drop-shadow(0 2px 4px rgba(vars.$primary-color, 0.3));
  }
}

.menu { 
  flex: 1; 
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(0,0,0,0.1);
    border-radius: 4px;
  }
}

/* Dark Theme Support via parent class */
:global(.theme-dark) .sider {
  background: #1e1e1e;
  border-right-color: rgba(255, 255, 255, 0.05);

  .logo {
    color: #fff;
  }

  :deep(.el-menu-item) {
    color: #a0a0a0;
    &:hover {
      background: rgba(255, 255, 255, 0.05);
      color: #fff;
    }
    &.is-active {
      background: vars.$primary-color;
      color: #fff;
      .el-icon { color: #fff; }
    }
  }
}
</style>
