<template>
  <div class="home-container">
    <!-- Welcome Header -->
    <div class="welcome-section">
      <div class="greeting">
        <h1>你好，{{ userStore.profile?.nickname || '朋友' }}</h1>
        <p>今天感觉怎么样？让我们开始关注内心的一天。</p>
      </div>
      <div class="avatar-wrapper">
        <el-avatar :size="64" :src="userStore.profile?.avatar" class="soft-avatar">
          {{ (userStore.profile?.nickname || 'U').charAt(0).toUpperCase() }}
        </el-avatar>
      </div>
    </div>

    <!-- Mood Tracker Preview -->
    <div class="section-title">情绪追踪</div>
    <div class="mood-tracker-card glass-card">
      <div class="mood-options">
        <div class="mood-item" @click="quickLogMood('GREAT')">
          <div class="mood-icon icon-great">😄</div>
          <span>开心</span>
        </div>
        <div class="mood-item" @click="quickLogMood('GOOD')">
          <div class="mood-icon icon-good">🙂</div>
          <span>平静</span>
        </div>
        <div class="mood-item" @click="quickLogMood('BAD')">
          <div class="mood-icon icon-bad">😔</div>
          <span>低落</span>
        </div>
        <div class="mood-item" @click="quickLogMood('ANXIOUS')">
          <div class="mood-icon icon-anxious">😰</div>
          <span>焦虑</span>
        </div>
      </div>
      <div class="mood-chart-preview" ref="moodChartRef"></div>
    </div>

    <!-- Main Features Grid -->
    <div class="features-grid">
      <!-- Treatment Appointment -->
      <div class="feature-card glass-card" @click="$router.push('/doctor-consultation')">
        <div class="card-icon icon-appointment"><el-icon><Calendar /></el-icon></div>
        <h3>治疗咨询</h3>
        <p>预约专业心理咨询师</p>
      </div>

      <!-- Resource Library -->
      <div class="feature-card glass-card" @click="$router.push('/recommendations')">
        <div class="card-icon icon-resource"><el-icon><Reading /></el-icon></div>
        <h3>个性化建议</h3>
        <p>冥想音频与心理文章</p>
      </div>
      
      <!-- Crisis Support -->
      <div class="feature-card glass-card warning-card" @click="showCrisisSupport">
        <div class="card-icon icon-crisis"><el-icon><PhoneFilled /></el-icon></div>
        <h3>危机支持</h3>
        <p>24小时紧急援助</p>
      </div>

      <!-- Constitution Analysis (Keep existing feature) -->
      <div class="feature-card glass-card" @click="$router.push('/tcm/trend')">
        <div class="card-icon icon-trend"><el-icon><DataLine /></el-icon></div>
        <h3>身心趋势</h3>
        <p>查看体质与情绪变化</p>
      </div>
    </div>

    <!-- Daily Insight / Todo -->
    <div class="section-title">每日关怀</div>
    <div class="insight-card glass-card">
      <el-empty v-if="todoItems.length === 0" description="今天没有待办事项，好好休息吧" image-size="80" />
      <div v-else class="todo-list">
        <div v-for="item in todoItems.slice(0, 3)" :key="item.id" class="todo-item">
          <div class="todo-check">
            <el-checkbox v-model="item.checked" @change="markAsRead(item)" />
          </div>
          <div class="todo-content">
            <span class="todo-title">{{ item.title }}</span>
            <span class="todo-time">{{ formatDate(item.scheduledTime, 'HH:mm') }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Crisis Dialog -->
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
import { ref, onMounted, nextTick } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { Calendar, Reading, PhoneFilled, DataLine, ChatDotRound, MagicStick } from '@element-plus/icons-vue'
import { getUserTodoItems, updateReminderStatus } from '../../api/reminder'
import { getMetricTrends } from '../../api/family'
import { formatDate } from '../../utils/request'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const userStore = useUserStore()
const router = useRouter()
const todoItems = ref([])
const crisisVisible = ref(false)
const moodChartRef = ref(null)

const quickLogMood = (mood) => {
  // Navigate to log creation with pre-filled mood
  router.push({ path: '/logs', query: { type: 'mood', value: mood, mode: 'create' } })
}

const showCrisisSupport = () => {
  crisisVisible.value = true
}

const startBreathing = () => {
  ElMessage.success({
    message: '保持深呼吸，吸气... 呼气... (功能演示)',
    duration: 3000
  })
}

const markAsRead = async (item) => {
  try {
    await updateReminderStatus(item.id, { status: 'ACKNOWLEDGED' })
    ElMessage.success('已完成')
    // Remove from list
    todoItems.value = todoItems.value.filter(i => i.id !== item.id)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const initMoodChart = async () => {
  if (!moodChartRef.value) return
  // Mock data for preview or fetch real data
  const chart = echarts.init(moodChartRef.value)
  const option = {
    grid: { top: 10, bottom: 20, left: 0, right: 0 },
    xAxis: { show: false, type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
    yAxis: { show: false, type: 'value' },
    series: [{
      data: [3, 4, 3, 5, 4, 2, 4], // Mock mood levels
      type: 'line',
      smooth: true,
      lineStyle: { color: '#FFB7B2', width: 3 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255, 183, 178, 0.5)' },
          { offset: 1, color: 'rgba(255, 183, 178, 0.0)' }
        ])
      },
      symbol: 'none'
    }]
  }
  chart.setOption(option)
}

onMounted(async () => {
  try {
    const todoRes = await getUserTodoItems()
    todoItems.value = todoRes.data || []
    nextTick(() => initMoodChart())
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.home-container {
  padding: 24px;
  min-height: 100vh;
  /* Background is handled by global layout */
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  animation: fadeInDown 0.8s vars.$ease-spring;

  .greeting {
    h1 {
      font-size: 28px;
      font-weight: 700;
      color: vars.$text-main-color;
      margin: 0 0 8px 0;
    }
    p {
      color: vars.$text-secondary-color;
      font-size: 16px;
      margin: 0;
    }
  }

  .avatar-wrapper {
    .soft-avatar {
      background: vars.$gradient-primary;
      font-size: 24px;
      font-weight: 600;
      color: #fff;
      box-shadow: vars.$shadow-sm;
      border: 2px solid rgba(255, 255, 255, 0.5);
      transition: transform 0.3s vars.$ease-spring;
      
      &:hover {
        transform: scale(1.05);
      }
    }
  }
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: vars.$text-main-color;
  margin-bottom: 16px;
  padding-left: 8px;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 16px;
    background: vars.$primary-color;
    border-radius: 2px;
  }
}

/* Unified Card Style */
.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: vars.$glass-border;
  margin-bottom: 24px;
  transition: all 0.4s vars.$ease-spring;

  &:hover {
    transform: translateY(-4px);
    box-shadow: vars.$shadow-md;
    background: rgba(255, 255, 255, 0.8);
  }
  
  &.warning-card {
    border-color: rgba(vars.$warning-color, 0.3);
    &:hover {
      background: rgba(vars.$warning-color, 0.05);
    }
  }
}

/* Mood Tracker */
.mood-tracker-card {
  padding: 20px;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s vars.$ease-spring 0.2s backwards;

  .mood-options {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
    position: relative;
    z-index: 2;

    .mood-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      cursor: pointer;
      transition: all 0.3s ease;
      
      .mood-icon {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        margin-bottom: 8px;
        transition: transform 0.3s vars.$ease-spring;
        
        &.icon-great { background: #FFB7B2; }
        &.icon-good { background: #E2F0CB; }
        &.icon-bad { background: #B5EAD7; }
        &.icon-anxious { background: #C7CEEA; }
      }
      
      span {
        font-size: 12px;
        color: vars.$text-secondary-color;
      }

      &:hover .mood-icon {
        transform: scale(1.15) rotate(5deg);
      }
    }
  }

  .mood-chart-preview {
    height: 60px;
    opacity: 0.6;
    pointer-events: none;
    margin: 0 -20px -20px -20px;
  }
}

/* Features Grid */
.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  animation: fadeInUp 0.6s vars.$ease-spring 0.3s backwards;

  .feature-card {
    padding: 20px;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;

    .card-icon {
      font-size: 28px;
      margin-bottom: 12px;
      
      &.icon-appointment { color: #FF9AA2; }
      &.icon-resource { color: #B5EAD7; }
      &.icon-crisis { color: #FF6F61; }
      &.icon-trend { color: #C7CEEA; }
    }

    h3 {
      font-size: 16px;
      margin: 0 0 4px 0;
      color: vars.$text-main-color;
    }

    p {
      font-size: 12px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

/* Daily Insight */
.insight-card {
  padding: 20px;
  animation: fadeInUp 0.6s vars.$ease-spring 0.4s backwards;

  .todo-list {
    .todo-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);

      &:last-child {
        border-bottom: none;
      }

      .todo-check {
        margin-right: 12px;
      }

      .todo-content {
        flex: 1;
        display: flex;
        flex-direction: column;

        .todo-title {
          font-size: 14px;
          color: vars.$text-main-color;
        }

        .todo-time {
          font-size: 12px;
          color: vars.$text-secondary-color;
          margin-top: 2px;
        }
      }
    }
  }
}

</style>

<style lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.crisis-dialog.glass-dialog {
  @include mixins.glass-effect;
  border-radius: 20px;
  overflow: hidden; /* Ensure rounded corners clip content */
  
  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0; /* Remove default padding */
  }

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

    h2 {
      font-size: 24px;
      color: vars.$text-main-color;
      margin: 0 0 8px;
    }

    p {
      color: vars.$text-secondary-color;
      margin: 0;
      font-size: 14px;
    }
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
        border: 1px solid transparent;

        .icon-box {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          margin-right: 16px;
        }

        .info {
          flex: 1;
          display: flex;
          flex-direction: column;

          .label {
            font-size: 12px;
            color: vars.$text-secondary-color;
            margin-bottom: 2px;
          }
          
          .number {
            font-size: 20px;
            font-weight: 700;
            color: vars.$text-main-color;
            font-family: monospace;
          }
        }

        .action-btn {
          padding: 6px 16px;
          border-radius: 20px;
          font-size: 14px;
          font-weight: 600;
        }

        &.urgent {
          background: rgba(245, 108, 108, 0.05);
          .icon-box { background: rgba(245, 108, 108, 0.1); color: #F56C6C; }
          .action-btn { background: #F56C6C; color: #fff; }
          &:hover { background: rgba(245, 108, 108, 0.1); transform: translateY(-2px); }
        }

        &.support {
          background: rgba(64, 158, 255, 0.05);
          .icon-box { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
          .action-btn { background: #409EFF; color: #fff; }
          &:hover { background: rgba(64, 158, 255, 0.1); transform: translateY(-2px); }
        }
      }
    }

    .helper-actions {
      margin-bottom: 24px;
      
      .helper-btn {
        width: 100%;
        padding: 12px;
        border-radius: 12px;
        background: #fff;
        border: 1px dashed #dcdfe6;
        color: vars.$text-secondary-color;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          border-color: vars.$primary-color;
          color: vars.$primary-color;
          background: rgba(vars.$primary-color, 0.05);
        }
      }
    }
  }

  .crisis-footer {
    padding: 0 24px 32px;
    text-align: center;
    
    .close-btn {
      width: 100%;
      background: #f0f2f5;
      border: none;
      color: vars.$text-secondary-color;
      
      &:hover {
        background: #e6e8eb;
        color: vars.$text-main-color;
      }
    }
  }
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 111, 97, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(255, 111, 97, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 111, 97, 0); }
}
</style>
