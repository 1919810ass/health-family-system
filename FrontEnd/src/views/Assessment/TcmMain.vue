<template>
  <div class="tcm-main-layout">
    <el-page-header 
      :content="pageTitle" 
      icon="" 
      class="mb-24"
      @back="goBack" 
    />
    
    <el-card class="main-card">
      <router-view />
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const pageTitle = ref('中医体质')

// 监听路由变化，更新页面标题
watch(() => route.meta.title, (newTitle) => {
  if (newTitle) {
    pageTitle.value = newTitle
  }
}, { immediate: true })

const goBack = () => {
  router.push('/home')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.tcm-main-layout {
  padding: 24px;
  background: transparent;
  min-height: 100%;
}

.main-card {
  max-width: 1200px;
  margin: 0 auto;
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: vars.$glass-border;
  box-shadow: vars.$shadow-md;
  animation: fadeInUp 0.6s vars.$ease-spring;
}

.mb-24 {
  margin-bottom: 24px;
}
</style>