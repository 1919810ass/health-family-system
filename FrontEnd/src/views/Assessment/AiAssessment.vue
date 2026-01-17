<template>
  <div class="ai-assessment-container">
    <el-page-header content="AI中医体质测评" icon="" class="mb-24" @back="goBack" />
    
    <el-card class="assessment-card" v-loading="loading" element-loading-text="AI正在分析您的体质...">
      <!-- 进度条 -->
      <div class="progress-container" v-if="!isAssessmentComplete">
        <el-progress :percentage="progressPercentage" :stroke-width="8" :text-inside="true" status="success" />
        <div class="progress-text">已完成 {{ messages.filter(m => m.type === 'user').length }}/{{ totalQuestions }} 个问题</div>
      </div>
      
      <div class="chat-container" ref="chatContainer">
        <div v-for="(message, index) in messages" :key="index" class="message">
          <div v-if="message.type === 'ai'" class="ai-message">
            <div class="avatar ai-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="content">
              <p class="text">{{ message.content }}</p>
              <div v-if="message.type === 'ai' && !isAssessmentComplete" class="thinking-indicator" v-loading="message.thinking || index === messages.length - 1 && loadingNextQuestion"></div>
            </div>
          </div>
          
          <div v-else-if="message.type === 'user'" class="user-message">
            <div class="content">
              <p class="text">{{ message.content }}</p>
            </div>
            <div class="avatar user-avatar">
              <el-icon><User /></el-icon>
            </div>
          </div>
          
          <div v-else-if="message.type === 'result'" class="result-message">
            <div class="result-content">
              <div class="result-header">
                <h3>🎉 恭喜您完成体质测评！</h3>
                <p>根据AI分析，您的体质类型为：<span class="primary-type">{{ getConstitutionName(resultData?.primaryType) }}</span></p>
              </div>
              
              <div class="result-chart">
                <v-chart :option="chartOption" class="chart" v-if="chartOption" />
              </div>
              
              <div class="result-summary">
                <h4>🔍 体质分析</h4>
                <p>{{ resultData?.report?.summary }}</p>
                
                <h4>💡 调理建议</h4>
                <ul class="recommendations-list">
                  <li v-for="(rec, idx) in resultData?.report?.recommendations || []" :key="idx" class="recommendation-item">
                    <el-icon><Lightning /></el-icon>
                    <span>{{ rec }}</span>
                  </li>
                </ul>
                
                <div class="confidence">
                  <span class="confidence-label">可信度：</span>
                  <el-rate 
                    v-model="confidenceRate" 
                    :max="1" 
                    :allow-half="true" 
                    disabled 
                    show-text 
                    :texts="['较低', '一般', '较高']"
                    :score-template="'{value}'"
                  />
                </div>
              </div>
              
              <div class="result-actions">
                <el-button type="primary" size="large" @click="viewDetailedReport">
                  <el-icon><Document /></el-icon>
                  查看详细报告
                </el-button>
                <el-button size="large" @click="restartAssessment">
                  <el-icon><Refresh /></el-icon>
                  重新测评
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="input-area" v-if="!isAssessmentComplete">
        <el-input
          v-model="userInput"
          placeholder="请输入您的回答，按回车键发送..."
          :disabled="loadingNextQuestion"
          @keyup.enter="sendAnswer"
          clearable
          size="large"
        >
          <template #append>
            <el-button 
              :loading="loadingNextQuestion" 
              @click="sendAnswer" 
              :disabled="!userInput.trim() || loadingNextQuestion"
              size="large"
              type="primary"
            >
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </template>
        </el-input>
        <div class="input-actions">
          <el-button 
            class="skip-btn" 
            type="info" 
            plain 
            @click="skipQuestion"
            :disabled="loadingNextQuestion"
          >
            <el-icon><ArrowRight /></el-icon>
            跳过此题
          </el-button>
          <el-button 
            class="help-btn" 
            type="warning" 
            plain 
            @click="showHelp"
          >
            <el-icon><QuestionFilled /></el-icon>
            求助
          </el-button>
        </div>
      </div>
      
      <!-- 求助对话框 -->
      <el-dialog v-model="showHelpDialog" title="帮助提示" width="50%">
        <p>AI中医体质测评是通过与AI助手对话的方式，了解您的身体状况，从而判断您的体质类型。</p>
        <p>请根据您的实际情况，详细回答AI助手的问题。您可以：</p>
        <ul>
          <li>详细描述您的身体感受</li>
          <li>提及您平时的生活习惯</li>
          <li>告知您是否有特殊的体质表现</li>
        </ul>
        <p>回答越详细，AI分析结果越准确。</p>
      </el-dialog>
    </el-card>
    
    <!-- 详细报告弹窗 -->
    <el-dialog v-model="showDetailedReport" title="详细体质报告" width="80%" top="5vh" class="detailed-report-dialog">
      <div v-if="resultData" class="detailed-report">
        <div class="report-header">
          <h3>您的体质报告</h3>
          <div class="primary-constitution">
            主导体质：
            <span class="constitution-badge">{{ getConstitutionName(resultData.primaryType) }}</span>
          </div>
        </div>
        
        <div class="report-section">
          <h4>📊 体质分析</h4>
          <p>{{ resultData.report?.summary }}</p>
        </div>
        
        <div class="report-section">
          <h4>📈 各体质得分</h4>
          <el-table :data="scoreList" style="width: 100%" :default-sort="{ prop: 'score', order: 'descending' }" stripe>
            <el-table-column prop="name" label="体质类型" width="150" />
            <el-table-column prop="score" label="得分" width="100" sortable />
            <el-table-column label="占比" width="150">
              <template #default="scope">
                <div class="progress-cell">
                  <el-progress :percentage="Math.round(scope?.row?.score || 0)" :show-text="false" />
                  <span class="progress-text">{{ Math.round(scope?.row?.score || 0) }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="体质特征" width="200">
              <template #default="scope">
                <span class="constitution-desc">{{ getConstitutionDescription(scope?.row?.code) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="report-section">
          <h4>💡 调理建议</h4>
          <div class="recommendations-grid">
            <el-card v-for="(rec, idx) in resultData.report?.recommendations || []" :key="idx" class="recommendation-card">
              <el-icon class="recommendation-icon"><Lightning /></el-icon>
              <div class="recommendation-text">{{ rec }}</div>
            </el-card>
          </div>
        </div>
        
        <div class="report-section">
          <h4>🌱 生活指导</h4>
          <div class="lifestyle-guidance">
            <div class="guidance-item">
              <div class="guidance-header">
                <el-icon><Food /></el-icon>
                <h5>饮食建议</h5>
              </div>
              <p>根据您的体质特点，建议多食用温补、易消化的食物，如山药、红枣、小米等，避免生冷、油腻食物。</p>
            </div>
            <div class="guidance-item">
              <div class="guidance-header">
                <el-icon><VideoCamera /></el-icon>
                <h5>运动建议</h5>
              </div>
              <p>适合您的运动方式包括太极拳、八段锦、散步等温和运动，避免过度剧烈运动。</p>
            </div>
            <div class="guidance-item">
              <div class="guidance-header">
                <el-icon><Clock /></el-icon>
                <h5>作息建议</h5>
              </div>
              <p>建议保持规律作息，早睡早起，最佳睡眠时间为22:00-23:00，确保充足睡眠。</p>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, User, Lightning, Document, Refresh, Promotion, ArrowRight, QuestionFilled, Food, Clock, VideoCamera } from '@element-plus/icons-vue'
import * as assessmentApi from '@/api/assessment'

import { getConstitutionName, getConstitutionDescription } from '@/utils/tcm-constants'

// 引入 ECharts 选项类型
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { RadarChart } from 'echarts/charts'
import { 
  TitleComponent, 
  TooltipComponent, 
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

// 注册ECharts组件
use([
  CanvasRenderer,
  RadarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

const router = useRouter()

// 响应式数据
const loading = ref(false)
const loadingNextQuestion = ref(false)
const userInput = ref('')
const messages = ref([])
const sessionId = ref('')
const isAssessmentComplete = ref(false)
const resultData = ref(null)
const showDetailedReport = ref(false)
const showHelpDialog = ref(false)
const chatContainer = ref(null)
const totalQuestions = ref(10) // 预设问题总数

// 图表选项
const chartOption = ref(null)

// 计算属性
const progressPercentage = computed(() => {
  if (isAssessmentComplete.value) return 100
  const userMessages = messages.value.filter(m => m.type === 'user').length
  return Math.min(100, Math.round((userMessages / totalQuestions.value) * 100))
})

const confidenceRate = computed(() => {
  if (resultData.value?.report?.confidence) {
    return parseFloat(resultData.value.report.confidence)
  }
  return 0
})

const scoreList = computed(() => {
  if (!resultData.value?.report?.scores) return []
  
  return Object.entries(resultData.value.report.scores)
    .map(([code, score]) => ({
      code,
      name: getConstitutionName(code),
      score: Math.round(score * 100) / 100
    }))
    .sort((a, b) => b.score - a.score)
})

// 方法
const goBack = () => {
  router.push('/tcm/assessments')
}

const startAssessment = async () => {
  loading.value = true
  try {
    const response = await assessmentApi.startAiAssessment()
    sessionId.value = response.data.sessionId
    
    // 添加AI的初始问候
    messages.value.push({
      type: 'ai',
      content: '您好！我是中医体质辨识助手，将通过对话的方式为您进行体质测评。请根据您的实际情况回答问题。',
      thinking: false
    })
    
    // 添加AI的第一个问题
    messages.value.push({
      type: 'ai',
      content: response.data.question,
      thinking: false
    })
    
    scrollToBottom()
  } catch (error) {
    ElMessage.error('启动测评失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const sendAnswer = async () => {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入您的回答')
    return
  }
  
  // 添加用户回答到消息列表
  messages.value.push({
    type: 'user',
    content: userInput.value
  })
  
  const answer = userInput.value
  userInput.value = ''
  loadingNextQuestion.value = true
  
  try {
    const response = await assessmentApi.processAiAnswer(sessionId.value, answer)
    
    // 检查是否结束评估
    if (response.data.shouldEnd) {
      // 添加AI的结束语
      messages.value.push({
        type: 'ai',
        content: '感谢您的回答，正在为您分析体质特征...',
        thinking: true
      })
      
      scrollToBottom()
      
      try {
        // 调用最终评估API
        const finalResponse = await assessmentApi.generateFinalAiAssessment(sessionId.value, userInput.value || '用户已完成回答')
        
        resultData.value = finalResponse.data
        isAssessmentComplete.value = true
        
        // 移除最后的思考消息
        messages.value.pop()
        
        // 添加结果消息
        messages.value.push({
          type: 'result',
          content: '评估结果',
          thinking: false
        })
        
        // 准备图表数据
        prepareChartData()
      } catch (error) {
        ElMessage.error('生成评估结果失败: ' + error.message)
        // 移除思考消息并显示错误
        messages.value.pop()
        messages.value.push({
          type: 'ai',
          content: '生成评估结果时出现错误，请重试。',
          thinking: false
        })
      }
    } else {
      // 添加AI的下一个问题
      messages.value.push({
        type: 'ai',
        content: response.data.question,
        thinking: false
      })
    }
    
    scrollToBottom()
  } catch (error) {
    ElMessage.error('处理回答失败: ' + error.message)
    // 添加错误提示消息
    messages.value.push({
      type: 'ai',
      content: '抱歉，处理您的回答时出现了一些问题，请重新回答或跳过此题。',
      thinking: false
    })
  } finally {
    loadingNextQuestion.value = false
  }
}

const skipQuestion = async () => {
  try {
    // 使用一个通用回答来跳过当前问题
    const response = await assessmentApi.processAiAnswer(sessionId.value, "我不太确定这个问题的答案，可以问下一个问题吗？")
    
    // 添加AI的下一个问题
    messages.value.push({
      type: 'ai',
      content: response.data.question,
      thinking: false
    })
    
    scrollToBottom()
  } catch (error) {
    ElMessage.error('跳过问题失败: ' + error.message)
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

const prepareChartData = () => {
  if (!resultData.value?.report?.scores) return
  
  const scores = resultData.value.report.scores
  const indicator = Object.keys(scores).map(code => ({
    name: getConstitutionName(code),
    max: 100
  }))
  
  const value = Object.values(scores)
  
  chartOption.value = {
    tooltip: {},
    radar: {
      indicator,
      axisName: {
        color: '#333'
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(255,255,255,0.3)', 'rgba(255,255,255,0.3)']
        }
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(120,120,120,0.3)'
        }
      }
    },
    series: [{
      name: '体质得分',
      type: 'radar',
      data: [{
        value,
        name: '体质分析',
        areaStyle: { opacity: 0.3 }
      }]
    }]
  }
}

const viewDetailedReport = () => {
  showDetailedReport.value = true
}

const restartAssessment = () => {
  // 重新开始评估
  messages.value = []
  resultData.value = null
  isAssessmentComplete.value = false
  chartOption.value = null
  startAssessment()
}

const showHelp = () => {
  showHelpDialog.value = true
}

// 生命周期
onMounted(() => {
  startAssessment()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.ai-assessment-container {
  padding: 20px;
  /* Background handled by global layout */
  min-height: 100%;
}

.assessment-card {
  max-width: 900px;
  margin: 0 auto;
  @include mixins.glass-effect;
  border: none;
  border-radius: vars.$radius-lg;
  overflow: hidden;
}

.progress-container {
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.4);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  
  .el-progress {
    margin-bottom: 8px;
  }
  
  .progress-text {
    text-align: center;
    color: vars.$text-secondary-color;
    font-size: 14px;
  }
}

.chat-container {
  height: 500px;
  overflow-y: auto;
  padding: 24px;
  border-radius: vars.$radius-md;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.3);
  
  .message {
    margin-bottom: 24px;
    display: flex;
    animation: fadeInUp 0.4s vars.$ease-spring;
    
    .ai-message {
      display: flex;
      align-items: flex-start;
      
      .avatar {
        margin-right: 16px;
        width: 44px;
        height: 44px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 18px;
        box-shadow: vars.$shadow-sm;
        
        &.ai-avatar {
          background: vars.$gradient-primary;
        }
      }
      
      .content {
        flex: 1;
        max-width: 80%;
        
        .text {
          background: rgba(255, 255, 255, 0.8);
          padding: 16px;
          border-radius: 12px 12px 12px 4px;
          margin: 0;
          display: inline-block;
          color: vars.$text-main-color;
          line-height: 1.6;
          box-shadow: vars.$shadow-sm;
          border: vars.$glass-border;
        }
        
        .thinking-indicator {
          margin-top: 12px;
          height: 24px;
        }
      }
    }
    
    .user-message {
      display: flex;
      justify-content: flex-end;
      
      .content {
        .text {
          background: vars.$gradient-primary;
          padding: 16px;
          border-radius: 12px 12px 4px 12px;
          margin: 0;
          display: inline-block;
          color: #fff;
          line-height: 1.6;
          box-shadow: vars.$shadow-sm;
        }
      }
      
      .avatar {
        margin-left: 16px;
        width: 44px;
        height: 44px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 18px;
        background: vars.$gradient-success;
        box-shadow: vars.$shadow-sm;
      }
    }
    
    .result-message {
      width: 100%;
      text-align: center;
      
      .result-content {
        width: 100%;
        padding: 24px;
        background: rgba(255, 255, 255, 0.4);
        border: vars.$glass-border;
        border-radius: vars.$radius-lg;
        box-shadow: vars.$shadow-md;
        
        .result-header {
          margin-bottom: 24px;
          
          h3 {
            margin: 0 0 8px 0;
            color: vars.$text-main-color;
            font-size: 22px;
          }
          
          p {
            color: vars.$text-secondary-color;
            font-size: 16px;
            margin: 0;
            
            .primary-type {
              color: vars.$primary-color;
              font-weight: bold;
              font-size: 18px;
            }
          }
        }
        
        .result-chart {
          margin: 24px 0;
          height: 320px;
          
          .chart {
            width: 100%;
            height: 100%;
          }
        }
        
        .result-summary {
          text-align: left;
          margin: 24px 0;
          
          h4 {
            color: vars.$text-main-color;
            margin: 24px 0 12px 0;
            font-size: 18px;
            display: flex;
            align-items: center;
            
            &::before {
              content: '';
              display: inline-block;
              width: 4px;
              height: 20px;
              background: vars.$gradient-success;
              margin-right: 12px;
              border-radius: 2px;
            }
          }
          
          p {
            color: vars.$text-regular-color;
            line-height: 1.8;
            margin-bottom: 16px;
            font-size: 15px;
            background-color: rgba(255, 255, 255, 0.3);
            border-radius: vars.$radius-md;
            padding: 16px;
            border-left: 4px solid vars.$primary-color;
          }
          
          .recommendations-list {
            padding-left: 0;
            margin-bottom: 16px;
            
            .recommendation-item {
              list-style: none;
              margin-bottom: 12px;
              display: flex;
              align-items: flex-start;
              
              .el-icon {
                color: vars.$warning-color;
                margin-right: 8px;
                margin-top: 4px;
              }
              
              span {
                color: vars.$text-regular-color;
                line-height: 1.6;
              }
            }
          }
          
          .confidence {
            margin: 24px 0;
            padding: 16px;
            background-color: rgba(255, 255, 255, 0.4);
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: vars.$glass-border;
            
            .confidence-label {
              margin-right: 10px;
              color: vars.$text-regular-color;
              font-weight: 500;
            }
          }
        }
        
        .result-actions {
          margin-top: 32px;
          display: flex;
          justify-content: center;
          gap: 16px;
          flex-wrap: wrap;
          
          .el-button {
            min-width: 140px;
            transition: all 0.3s vars.$ease-spring;
            
            &:hover {
              transform: translateY(-2px);
              box-shadow: vars.$shadow-sm;
            }
          }
        }
      }
    }
  }
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .el-input {
    width: 100%;
    
    :deep(.el-input__wrapper) {
      background: rgba(255, 255, 255, 0.6);
      backdrop-filter: blur(10px);
      box-shadow: none;
      border: 1px solid rgba(255, 255, 255, 0.3);
      
      &.is-focus {
        background: rgba(255, 255, 255, 0.9);
        box-shadow: 0 0 0 1px vars.$primary-color inset;
      }
    }
  }
  
  .input-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    
    .el-button {
      margin: 0;
      transition: all 0.3s vars.$ease-spring;
      
      &:hover {
        transform: translateY(-2px);
      }
    }
  }
}

.detailed-report {
  .report-header {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    
    h3 {
      color: vars.$text-main-color;
      margin: 0 0 8px 0;
      font-size: 20px;
    }
    
    .primary-constitution {
      color: vars.$text-regular-color;
      
      .constitution-badge {
        display: inline-block;
        padding: 4px 12px;
        background: vars.$gradient-primary;
        color: white;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
  
  .report-section {
    margin-bottom: 32px;
    
    h4 {
      color: vars.$text-main-color;
      margin: 0 0 16px 0;
      font-size: 16px;
      display: flex;
      align-items: center;
      
      &::before {
        content: '';
        display: inline-block;
        width: 4px;
        height: 20px;
        background: vars.$gradient-success;
        margin-right: 12px;
        border-radius: 2px;
      }
    }
    
    p {
      color: vars.$text-regular-color;
      line-height: 1.8;
      margin-bottom: 16px;
    }
    
    ul {
      padding-left: 20px;
      color: vars.$text-regular-color;
      line-height: 1.8;
      
      li {
        margin-bottom: 8px;
      }
    }
    
    .progress-cell {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .progress-text {
        color: vars.$text-regular-color;
        font-size: 14px;
        min-width: 40px;
      }
    }
    
    .constitution-desc {
      color: vars.$text-secondary-color;
      font-size: 13px;
    }
    
    .recommendations-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;
      
      .recommendation-card {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 16px;
        background: rgba(255, 255, 255, 0.5);
        border: vars.$glass-border;
        border-radius: vars.$radius-md;
        transition: all 0.3s vars.$ease-spring;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: vars.$shadow-sm;
          background: rgba(255, 255, 255, 0.8);
        }
        
        .recommendation-icon {
          color: vars.$warning-color;
          font-size: 20px;
          margin-top: 2px;
        }
        
        .recommendation-text {
          color: vars.$text-regular-color;
          line-height: 1.6;
        }
      }
    }
  }
  
  .lifestyle-guidance {
    .guidance-item {
      margin-bottom: 20px;
      padding: 16px;
      border-left: 4px solid vars.$primary-color;
      background-color: rgba(255, 255, 255, 0.4);
      border: vars.$glass-border;
      border-radius: 0 8px 8px 0;
      transition: all 0.3s vars.$ease-spring;
      
      &:hover {
        transform: translateX(4px);
        background-color: rgba(255, 255, 255, 0.6);
        box-shadow: vars.$shadow-sm;
      }
      
      .guidance-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        
        .el-icon {
          color: vars.$primary-color;
          font-size: 20px;
        }
        
        h5 {
          color: vars.$text-main-color;
          margin: 0;
          font-size: 16px;
        }
      }
      
      p {
        color: vars.$text-regular-color;
        margin: 0;
        line-height: 1.6;
      }
    }
  }
}

.mb-24 {
  margin-bottom: 24px;
}

.detailed-report-dialog {
  :deep(.el-dialog__body) {
    padding: 24px;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
