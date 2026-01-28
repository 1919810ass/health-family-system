<template>
  <div class="health-inference-card glass-card">
    <div class="card-content">
      <!-- Left: Action Area -->
      <div class="action-area">
        <div class="illustration">
          <div class="ai-brain-icon">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="pulse-ring"></div>
        </div>
        <h3>AI 全域健康诊断</h3>
        <p>基于体质、日志、体征的多维深度分析</p>
        <el-button 
          type="primary" 
          size="large" 
          class="start-btn" 
          :loading="loading" 
          @click="startDiagnosis"
        >
          {{ loading ? '诊断中...' : '开始AI全域诊断' }}
        </el-button>
      </div>

      <!-- Right: Report Area -->
      <div class="report-area">
        <div v-if="loading" class="loading-state">
          <div class="typing-animation">
            <span></span><span></span><span></span>
          </div>
          <p class="loading-text">Qwen 正在查阅您的所有病历...</p>
        </div>
        
        <div v-else-if="reportHtml" class="report-paper">
          <div class="paper-texture"></div>
          <div class="report-header">
            <div class="hospital-logo">
              <el-icon><FirstAidKit /></el-icon>
            </div>
            <div class="title-group">
              <h2>多维健康关联推理报告</h2>
              <span class="sub-title">AI Cross-Domain Health Inference Report</span>
            </div>
            <div class="date-stamp">{{ currentDate }}</div>
          </div>
          <div class="markdown-body" v-html="reportHtml"></div>
          <div class="report-footer">
            <div class="signature">
              <span>诊断医师 (AI)：</span>
              <span class="ai-name">Qwen-Max</span>
            </div>
            <div class="disclaimer">
              * 本报告由 AI 生成，仅供参考，不作为最终医疗诊断依据。
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <el-empty description="暂无诊断报告" :image-size="100">
            <template #image>
              <el-icon :size="60" color="#E6E8EB"><Document /></el-icon>
            </template>
          </el-empty>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Cpu, FirstAidKit, Document } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const reportContent = ref('')
const md = new MarkdownIt()

const currentDate = computed(() => dayjs().format('YYYY-MM-DD'))

const reportHtml = computed(() => {
  return reportContent.value ? md.render(reportContent.value) : ''
})

const startDiagnosis = async () => {
  loading.value = true
  reportContent.value = ''
  
  try {
    const res = await request.post('/inference/analyze')
    if (res.code === 0 && res.data) {
      // Simulate typing effect if needed, but for now just show result
      reportContent.value = res.data
    } else {
      ElMessage.warning(res.message || '诊断生成失败，请重试')
    }
  } catch (error) {
    console.error('Inference error:', error)
    ElMessage.error('网络请求失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.health-inference-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: vars.$glass-border;
  overflow: hidden;
  margin-bottom: 24px;
  transition: all 0.3s vars.$ease-spring;

  &:hover {
    box-shadow: vars.$shadow-md;
  }
}

.card-content {
  display: flex;
  min-height: 400px;

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

/* Left: Action Area */
.action-area {
  width: 35%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.6) 0%, rgba(240, 242, 245, 0.6) 100%);
  padding: 40px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-right: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;

  @media (max-width: 768px) {
    width: 100%;
    padding: 32px 24px;
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }

  .illustration {
    position: relative;
    width: 100px;
    height: 100px;
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    justify-content: center;

    .ai-brain-icon {
      font-size: 48px;
      color: vars.$primary-color;
      z-index: 2;
    }

    .pulse-ring {
      position: absolute;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      border: 2px solid vars.$primary-color;
      opacity: 0;
      animation: pulse-ring 2s infinite;
    }
  }

  h3 {
    font-size: 20px;
    font-weight: 700;
    color: vars.$text-main-color;
    margin: 0 0 8px;
  }

  p {
    font-size: 14px;
    color: vars.$text-secondary-color;
    margin: 0 0 32px;
    max-width: 200px;
  }

  .start-btn {
    width: 100%;
    max-width: 200px;
    border-radius: 24px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(vars.$primary-color, 0.4);
    }
  }
}

/* Right: Report Area */
.report-area {
  flex: 1;
  background: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
  
  .empty-state {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .loading-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: vars.$text-secondary-color;

    .typing-animation {
      margin-bottom: 16px;
      span {
        display: inline-block;
        width: 8px;
        height: 8px;
        background: vars.$primary-color;
        border-radius: 50%;
        margin: 0 4px;
        animation: typing 1.4s infinite ease-in-out both;

        &:nth-child(1) { animation-delay: -0.32s; }
        &:nth-child(2) { animation-delay: -0.16s; }
      }
    }
    
    .loading-text {
      font-size: 15px;
      animation: fadeIn 1s infinite alternate;
    }
  }
}

/* Report Paper Style */
.report-paper {
  flex: 1;
  padding: 32px 40px;
  position: relative;
  background-color: #fdfbf7; /* Light yellow tint */
  overflow-y: auto;
  max-height: 600px;
  
  /* Scrollbar */
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 3px; }

  .paper-texture {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23d4c5a9' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
    pointer-events: none;
    z-index: 0;
  }

  .report-header {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    border-bottom: 2px solid #e8e1d5;
    padding-bottom: 20px;
    margin-bottom: 24px;

    .hospital-logo {
      font-size: 32px;
      color: #bf9e74;
      margin-right: 16px;
    }

    .title-group {
      flex: 1;
      h2 {
        font-size: 22px;
        font-weight: 700;
        color: #5c4e3c;
        margin: 0;
        font-family: "Songti SC", "SimSun", serif; /* Serif font for medical feel */
      }
      .sub-title {
        font-size: 12px;
        color: #9c8e7e;
        text-transform: uppercase;
        letter-spacing: 1px;
      }
    }

    .date-stamp {
      font-family: monospace;
      color: #9c8e7e;
      border: 1px solid #9c8e7e;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 14px;
    }
  }

  .markdown-body {
    position: relative;
    z-index: 1;
    font-family: "Songti SC", "SimSun", serif;
    color: #4a4a4a;
    line-height: 1.8;
    
    :deep(h3) {
      font-size: 18px;
      color: #8c6b45;
      border-bottom: 1px dashed #dcdfe6;
      padding-bottom: 8px;
      margin-top: 24px;
      margin-bottom: 16px;
    }

    :deep(strong) {
      color: #a65d3d;
      font-weight: 600;
    }

    :deep(ul) {
      padding-left: 20px;
    }

    :deep(li) {
      margin-bottom: 8px;
    }
  }

  .report-footer {
    position: relative;
    z-index: 1;
    margin-top: 40px;
    border-top: 1px solid #e8e1d5;
    padding-top: 16px;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;

    .signature {
      font-family: "Songti SC", "SimSun", serif;
      font-size: 16px;
      color: #5c4e3c;
      
      .ai-name {
        font-family: "Brush Script MT", cursive;
        font-size: 20px;
        color: #8c6b45;
        margin-left: 8px;
      }
    }

    .disclaimer {
      font-size: 12px;
      color: #9c8e7e;
      font-style: italic;
    }
  }
}

/* Animations */
@keyframes pulse-ring {
  0% { transform: scale(0.8); opacity: 0.5; }
  100% { transform: scale(1.5); opacity: 0; }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

@keyframes fadeIn {
  from { opacity: 0.5; }
  to { opacity: 1; }
}
</style>
