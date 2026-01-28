<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Setting /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">系统设置</h2>
        <p class="subtitle">个性化您的应用体验与偏好</p>
      </div>
    </div>

    <div class="grid mt-24">
      <div class="grid-item" style="--delay: 0.1s"><ProfileCard /></div>
      <div class="grid-item" style="--delay: 0.2s"><AvatarCard /></div>
      <div class="grid-item full-width" style="--delay: 0.3s"><HealthProfileCard /></div>
      <div class="grid-item" style="--delay: 0.4s"><PasswordCard /></div>
      <div class="grid-item" style="--delay: 0.5s"><NotificationCard /></div>
    </div>
  </div>
</template>

<script setup>
import { Setting } from '@element-plus/icons-vue'
import ProfileCard from './components/ProfileCard.vue'
import AvatarCard from './components/AvatarCard.vue'
import HealthProfileCard from './components/HealthProfileCard.vue'
import PasswordCard from './components/PasswordCard.vue'
import NotificationCard from './components/NotificationCard.vue'
import { onMounted } from 'vue'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
onMounted(() => {
  console.log('[Settings] mounted, fetching profile')
  store.fetchProfile().catch(() => {})
})
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.page-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
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
    background: linear-gradient(135deg, vars.$info-color, color.adjust(vars.$info-color, $lightness: 15%));
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(vars.$info-color, 0.3);

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
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-main-color, vars.$info-color));
    }

    .subtitle {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

.mt-24 { margin-top: 24px }
.grid { 
  display: grid; 
  grid-template-columns: repeat(2, 1fr); 
  gap: 24px;
}

.grid-item {
  animation: fadeInUp 0.6s vars.$ease-spring backwards;
  animation-delay: var(--delay);
  
  &.full-width {
    grid-column: 1 / -1;
  }
}

@media (max-width: 768px) { 
  .grid { grid-template-columns: 1fr } 
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>

