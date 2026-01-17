<template>
  <div class="page-container">
    <el-page-header content="系统设置" icon="" class="page-header" />
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
@use '@/styles/variables' as vars;

.page-container {
  padding: 24px;
}

.page-header {
  animation: fadeInDown 0.6s vars.$ease-spring backwards;
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

