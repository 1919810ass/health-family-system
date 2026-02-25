<template>
  <div class="home-container">
    <!-- Main Dashboard Grid -->
    <div class="dashboard-grid">
      <div class="grid-item seasonal-section">
         <SeasonalWellnessCard />
      </div>

      <div class="grid-item trend-chart">
        <HealthTrendChart />
      </div>

      <div class="grid-item ai-assistant">
        <AIAssistantCard />
      </div>
      
      <div class="grid-item family-status">
        <FamilyStatusCard />
      </div>

      <div class="grid-item ai-overview">
        <HealthInferenceCard />
      </div>
    </div>

    <!-- Keep Crisis Support accessible -->
    <div class="floating-crisis-btn" @click="showCrisisSupport">
      <el-tooltip content="紧急支持" placement="left">
        <el-button type="danger" circle size="large" class="pulse-btn">
          <el-icon><PhoneFilled /></el-icon>
        </el-button>
      </el-tooltip>
    </div>

    <!-- Crisis Dialog (Keep existing) -->
    <el-dialog 
      v-model="crisisVisible" 
      :show-close="false"
      width="440px" 
      class="crisis-dialog glass-dialog" 
      align-center
      append-to-body
    >
      <div class="crisis-header">
         <div class="pulse-icon">
            <el-icon><FirstAidKit /></el-icon>
         </div>
         <h2>紧急支持</h2>
         <p>你并不孤单，我们随时为你提供帮助</p>
      </div>

      <div class="crisis-content">
        <div class="emergency-contacts">
          <a href="tel:110" class="contact-card urgent">
            <div class="icon-box"><el-icon><Bell /></el-icon></div>
            <div class="info">
              <span class="label">生命安全急救</span>
              <span class="number">110</span>
            </div>
            <div class="action-btn">呼叫</div>
          </a>

          <a href="tel:12345" class="contact-card support">
            <div class="icon-box"><el-icon><Service /></el-icon></div>
            <div class="info">
              <span class="label">心理援助热线</span>
              <span class="number">12345</span>
            </div>
             <div class="action-btn">咨询</div>
          </a>
        </div>
        
        <div class="helper-actions">
           <div class="helper-btn" @click="startBreathing">
             <el-icon><Timer /></el-icon>
             <span>3分钟呼吸平复</span>
           </div>
        </div>
      </div>
      
      <div class="crisis-footer">
        <el-button @click="crisisVisible = false" round size="large" class="close-btn">
          我已平复，关闭窗口
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 组件：index.vue
 *
 * 业务说明：用于呈现对应页面/模块功能，并通过 API 层与后端进行数据交互。
 */

import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import SeasonalWellnessCard from './components/SeasonalWellnessCard.vue'
import HealthTrendChart from './components/HealthTrendChart.vue'
import HealthInferenceCard from './components/HealthInferenceCard.vue'
import AIAssistantCard from './components/AIAssistantCard.vue'
import FamilyStatusCard from './components/FamilyStatusCard.vue'
import { PhoneFilled, FirstAidKit, Bell, Service, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const crisisVisible = ref(false)

const showCrisisSupport = () => {
  crisisVisible.value = true
}

const startBreathing = () => {
  ElMessage.success({
    message: '保持深呼吸，吸气... 呼气... (功能演示)',
    duration: 3000
  })
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.home-container {
  height: calc(100vh - 60px);
  overflow: hidden; /* Prevent main scrollbar on large screens */
  padding: 16px;
  box-sizing: border-box;

  @media (max-width: 1200px) {
    height: auto;
    overflow-y: auto;
  }
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 65% 1fr;
  grid-template-rows: 15fr 22.5fr 22.5fr 40fr;
  gap: 16px;
  height: 100%;
  
  .grid-item {
    min-height: 0; /* Important for grid items to shrink/scroll internally */
    height: 100%;
    overflow: hidden;
  }

  .seasonal-section {
    grid-area: 1 / 1 / 2 / 3;
  }
  
  .trend-chart {
    grid-area: 2 / 1 / 4 / 2;
  }

  .ai-assistant {
    grid-area: 2 / 2 / 3 / 3;
  }
  
  .family-status {
    grid-area: 3 / 2 / 4 / 3;
  }

  .ai-overview {
    grid-area: 4 / 1 / 5 / 3;
  }

  /* Responsive fallback for smaller screens */
  @media (max-width: 1200px) {
    display: flex;
    flex-direction: column;
    height: auto;
    gap: 16px;
    
    .grid-item {
      height: auto;
      min-height: 320px;
      
      &.seasonal-section {
        min-height: auto;
      }
    }
  }
}

.floating-crisis-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 100;
  
  .pulse-btn {
    width: 56px;
    height: 56px;
    font-size: 24px;
    box-shadow: 0 4px 16px rgba(245, 108, 108, 0.4);
    animation: pulse-shadow 2s infinite;
  }
}

@keyframes pulse-shadow {
  0% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(245, 108, 108, 0); }
  100% { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}

/* Crisis Dialog Styles (reused) */
/* ... (Same as before, scoped styles handle this well) ... */
</style>

<style lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

/* Global overrides for this page if needed */
.crisis-dialog.glass-dialog {
  @include mixins.glass-effect;
  border-radius: 20px;
  overflow: hidden;
  
  .el-dialog__header { display: none; }
  .el-dialog__body { padding: 0; }
  
  .crisis-header {
    text-align: center;
    padding-top: 32px;
    margin-bottom: 24px;
    
    .pulse-icon {
      width: 64px;
      height: 64px;
      background: rgba(255, 111, 97, 0.1);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16px;
      color: #FF6F61;
      font-size: 32px;
      animation: pulse 2s infinite;
    }
    
    h2 { font-size: 24px; margin: 0 0 8px; }
    p { color: vars.$text-secondary-color; margin: 0; }
  }
  
  .crisis-content {
    padding: 0 24px;
    
    .emergency-contacts {
      display: grid;
      gap: 12px;
      margin-bottom: 24px;
      
      .contact-card {
        display: flex;
        align-items: center;
        padding: 16px;
        border-radius: 16px;
        text-decoration: none;
        transition: all 0.3s;
        
        .icon-box {
          width: 48px; height: 48px; border-radius: 12px;
          display: flex; align-items: center; justify-content: center;
          font-size: 24px; margin-right: 16px;
        }
        
        .info {
          flex: 1; display: flex; flex-direction: column;
          .label { font-size: 12px; color: vars.$text-secondary-color; }
          .number { font-size: 20px; font-weight: 700; color: vars.$text-main-color; }
        }
        
        .action-btn {
          padding: 6px 16px; border-radius: 20px; font-size: 14px; font-weight: 600;
        }
        
        &.urgent {
          background: rgba(245, 108, 108, 0.05);
          .icon-box { background: rgba(245, 108, 108, 0.1); color: #F56C6C; }
          .action-btn { background: #F56C6C; color: #fff; }
        }
        
        &.support {
          background: rgba(64, 158, 255, 0.05);
          .icon-box { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
          .action-btn { background: #409EFF; color: #fff; }
        }
      }
    }
    
    .helper-actions .helper-btn {
      width: 100%; padding: 12px; border-radius: 12px;
      background: #fff; border: 1px dashed #dcdfe6;
      color: vars.$text-secondary-color;
      display: flex; align-items: center; justify-content: center; gap: 8px;
      cursor: pointer;
      &:hover { border-color: vars.$primary-color; color: vars.$primary-color; }
    }
  }
  
  .crisis-footer {
    padding: 0 24px 32px; text-align: center;
    .close-btn { width: 100%; background: #f0f2f5; border: none; }
  }
}
</style>
