<template>
  <div class="ai-assistant-card">
    <div class="card-header">
      <h3>AI 健康助手</h3>
    </div>
    <div class="card-content" @click="startChat">
      <div class="icon-wrapper">
        <el-icon><ChatDotRound /></el-icon>
      </div>
      <p>您好，有什么健康问题可以问我</p>
    </div>
  </div>
</template>

<script setup>
import { ChatDotRound } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const startChat = () => {
  router.push('/ai-chat')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;

.ai-assistant-card {
  background: #fff;
  border-radius: vars.$radius-lg;
  padding: 24px;
  height: 100%;
  box-shadow: vars.$shadow-sm;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  cursor: pointer;
  
  &:hover {
    box-shadow: vars.$shadow-md;
    transform: translateY(-2px);
    
    .icon-wrapper {
      transform: scale(1.1);
      background: vars.$primary-color;
      color: #fff;
    }
  }
}

.card-header {
  h3 {
    margin: 0;
    font-size: 18px;
    color: vars.$text-main-color;
    margin-bottom: 24px;
  }
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  
  .icon-wrapper {
    width: 64px;
    height: 64px;
    border-radius: 20px;
    background: rgba(78, 161, 255, 0.1);
    color: vars.$primary-color;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    margin-bottom: 16px;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  
  p {
    margin: 0;
    color: vars.$text-secondary-color;
    font-size: 14px;
  }
}
</style>
