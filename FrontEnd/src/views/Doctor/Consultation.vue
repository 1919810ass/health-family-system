<template>
  <div class="doctor-consultation">
    <el-page-header content="在线咨询" />
    
    <div class="consultation-container">
      <!-- 左侧：患者列表 -->
      <div class="patient-list-panel">
        <div class="panel-header">
          <el-select 
            v-model="currentFamilyId" 
            placeholder="选择家庭" 
            style="width: 100%"
            @change="handleFamilyChange"
          >
            <el-option 
              v-for="f in families" 
              :key="f.id" 
              :label="f.name" 
              :value="String(f.id)" 
            />
          </el-select>
        </div>

        <div class="patient-list" v-loading="sessionsLoading">
          <div 
            v-for="session in sessions" 
            :key="session.id"
            class="patient-item"
            :class="{ active: currentSessionId === session.id }"
            @click="selectSession(session)"
          >
            <el-avatar :src="session.patientAvatar" :size="40" class="patient-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="patient-info">
              <div class="patient-name">{{ session.patientName }}</div>
              <div class="last-message">{{ getLastMessagePreview(session) }}</div>
            </div>
            <div class="patient-meta">
              <div class="message-time">{{ formatTime(session.lastMessageAt) }}</div>
              <el-badge 
                v-if="session.unreadCountDoctor > 0" 
                :value="session.unreadCountDoctor" 
                class="unread-badge"
              />
            </div>
          </div>

          <el-empty v-if="sessions.length === 0" description="暂无咨询会话" :image-size="80" />
        </div>
      </div>

      <!-- 右侧：聊天窗口 -->
      <div class="chat-panel">
        <div v-if="!currentSession" class="empty-chat">
          <el-empty description="请选择左侧患者开始咨询" :image-size="120" />
        </div>

        <div v-else class="chat-window">
          <!-- 患者信息栏 -->
          <div class="chat-header">
            <div class="patient-header-info">
              <el-avatar :src="currentSession.patientAvatar" :size="40">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="patient-details">
                <div class="patient-name">{{ currentSession.patientName }}</div>
                <div class="patient-meta-text">
                  <span class="family-tag">{{ currentSession.familyName }}</span>
                  <el-tag size="small" :type="healthStatus.type" effect="plain" class="health-tag">{{ healthStatus.label }}</el-tag>
                </div>
              </div>
            </div>
            <div class="chat-actions">
              <el-tooltip content="查看健康档案" placement="bottom">
                <el-button circle @click="toggleHealthProfile">
                  <el-icon><DataAnalysis /></el-icon>
                </el-button>
              </el-tooltip>
              <el-dropdown @command="handleActionCommand">
                <el-button type="primary" circle>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="createNote">生成随访病历</el-dropdown-item>
                    <el-dropdown-item command="createPlan">创建随访计划</el-dropdown-item>
                    <el-dropdown-item command="closeSession" divided>关闭会话</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 健康概览面板 (可折叠) -->
          <transition name="el-zoom-in-top">
            <div v-if="showHealthProfile" class="health-profile-panel" v-loading="loadingVitals">
              <div class="vitals-grid">
                <div class="vital-item">
                  <div class="vital-icon heart"><el-icon><Timer /></el-icon></div>
                  <div class="vital-data">
                    <span class="label">心率</span>
                    <span class="value">{{ patientVitals.heartRate || '--' }} <small>bpm</small></span>
                  </div>
                </div>
                <div class="vital-item">
                  <div class="vital-icon bp"><el-icon><Odometer /></el-icon></div>
                  <div class="vital-data">
                    <span class="label">血压</span>
                    <span class="value">{{ patientVitals.bloodPressure || '--/--' }} <small>mmHg</small></span>
                  </div>
                </div>
                <div class="vital-item">
                  <div class="vital-icon glucose"><el-icon><Dish /></el-icon></div>
                  <div class="vital-data">
                    <span class="label">血糖</span>
                    <span class="value">{{ patientVitals.bloodGlucose || '--' }} <small>mmol/L</small></span>
                  </div>
                </div>
                <div class="vital-item">
                  <div class="vital-icon temp"><el-icon><Sunny /></el-icon></div>
                  <div class="vital-data">
                    <span class="label">体温</span>
                    <span class="value">{{ patientVitals.temperature || '--' }} <small>°C</small></span>
                  </div>
                </div>
                <div class="vital-item">
                  <div class="vital-icon weight"><el-icon><ScaleToOriginal /></el-icon></div>
                  <div class="vital-data">
                    <span class="label">体重</span>
                    <span class="value">{{ patientVitals.weight || '--' }} <small>kg</small></span>
                  </div>
                </div>
              </div>
            </div>
          </transition>

          <!-- 消息列表 -->
          <div class="messages-container" ref="messagesContainerRef" v-loading="messagesLoading">
            <!-- 背景心电图装饰 -->
            <div class="ecg-bg"></div>
            <div 
              v-for="message in messages" 
              :key="message.id"
              class="message-item"
              :class="{ 'message-doctor': message.senderType === 'DOCTOR', 'message-patient': message.senderType !== 'DOCTOR' }"
            >
              <el-avatar 
                :src="getAvatar(message)" 
                :size="32"
                class="message-avatar"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="message-content">
                <div class="message-bubble">
                  <div class="message-text">{{ message.content }}</div>
                  <div class="message-time">{{ formatTime(message.createdAt) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-area">
            <!-- 模板回复快捷按钮 -->
            <div class="template-buttons" v-if="showTemplates">
              <el-button 
                v-for="template in messageTemplates" 
                :key="template.id"
                size="small"
                @click="useTemplate(template)"
              >
                {{ template.label }}
              </el-button>
            </div>

            <div class="input-toolbar">
              <el-button 
                text 
                :icon="showTemplates ? ArrowUp : ArrowDown"
                @click="showTemplates = !showTemplates"
              >
                模板回复
              </el-button>
            </div>

            <div class="input-box">
              <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="3"
                placeholder="输入消息..."
                @keydown.ctrl.enter="sendMessage"
                @keydown.meta.enter="sendMessage"
              />
              <div class="input-actions">
                <el-button type="primary" :loading="sending" @click="sendMessage">发送</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, MoreFilled, ArrowUp, ArrowDown, DataAnalysis, Timer, Odometer, Sunny, Dish, ScaleToOriginal } from '@element-plus/icons-vue'
import { useDoctorStore } from '../../stores/doctor'
import {
  listSessionsForDoctor,
  getSessionMessages,
  sendMessage as apiSendMessage,
  markMessagesAsRead,
  closeSession as apiCloseSession,
  getOrCreateSession
} from '../../api/consultation'
import { createDoctorNote } from '../../api/doctor'
import { getDoctorView } from '../../api/family'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const doctorStore = useDoctorStore()

// 状态
const currentFamilyId = ref(null)
const families = computed(() => doctorStore.families)
const sessions = ref([])
const sessionsLoading = ref(false)
const currentSessionId = ref(null)
const currentSession = ref(null)
const messages = ref([])
const messagesLoading = ref(false)
const inputMessage = ref('')
const sending = ref(false)
const showTemplates = ref(false)
const showHealthProfile = ref(false)
const messagesContainerRef = ref(null)

const loadingVitals = ref(false)
const patientVitals = ref({
  heartRate: '',
  bloodPressure: '',
  temperature: '',
  bloodGlucose: '',
  weight: ''
})

// 阈值定义
const THRESHOLDS = {
  heartRate: { min: 60, max: 100 },
  systolic: { min: 90, max: 140 },
  diastolic: { min: 60, max: 90 },
  temperature: { min: 36.0, max: 37.3 },
  bloodGlucose: { min: 3.9, max: 7.8 } // 宽泛范围，包含空腹和餐后
}

// 计算健康状态
const healthStatus = computed(() => {
  if (!patientVitals.value) return { label: '数据获取中', type: 'info' }
  
  const warnings = []
  
  // 1. 血压检查
  if (patientVitals.value.bloodPressure) {
    const bpParts = patientVitals.value.bloodPressure.split('/')
    if (bpParts.length === 2) {
      const systolic = parseFloat(bpParts[0])
      const diastolic = parseFloat(bpParts[1])
      if (!isNaN(systolic) && (systolic < THRESHOLDS.systolic.min || systolic > THRESHOLDS.systolic.max)) {
        warnings.push('血压异常')
      } else if (!isNaN(diastolic) && (diastolic < THRESHOLDS.diastolic.min || diastolic > THRESHOLDS.diastolic.max)) {
        warnings.push('血压异常')
      }
    }
  }
  
  // 2. 心率检查
  if (patientVitals.value.heartRate) {
    const hr = parseFloat(patientVitals.value.heartRate)
    if (!isNaN(hr) && (hr < THRESHOLDS.heartRate.min || hr > THRESHOLDS.heartRate.max)) {
      warnings.push('心率异常')
    }
  }
  
  // 3. 血糖检查
  if (patientVitals.value.bloodGlucose) {
    const bg = parseFloat(patientVitals.value.bloodGlucose)
    if (!isNaN(bg) && (bg < THRESHOLDS.bloodGlucose.min || bg > THRESHOLDS.bloodGlucose.max)) {
      warnings.push('血糖异常')
    }
  }
  
  // 4. 体温检查
  if (patientVitals.value.temperature) {
    const temp = parseFloat(patientVitals.value.temperature)
    if (!isNaN(temp) && (temp < THRESHOLDS.temperature.min || temp > THRESHOLDS.temperature.max)) {
      warnings.push('体温异常')
    }
  }
  
  if (warnings.length > 0) {
    // 去重
    const uniqueWarnings = [...new Set(warnings)]
    return {
      label: uniqueWarnings.join('、'),
      type: 'danger'
    }
  }
  
  // 如果所有数据都为空，显示暂无数据
  const hasData = Object.values(patientVitals.value).some(v => v !== '')
  if (!hasData) return { label: '暂无数据', type: 'info' }
  
  return { label: '健康状况良好', type: 'success' }
})

const toggleHealthProfile = async () => {
  showHealthProfile.value = !showHealthProfile.value
  // Vitals are now loaded when session is selected, so we might not need to reload
  // But reloading ensures freshness if the user toggles it open after a while
  if (showHealthProfile.value && currentSession.value) {
    await loadPatientVitals()
  }
}

const loadPatientVitals = async () => {
  if (!currentSession.value?.familyId) return
  
  loadingVitals.value = true
  try {
    const res = await getDoctorView(currentSession.value.familyId)
    if (res.data?.telemetry) {
      const telemetry = res.data.telemetry
      
      let patientData = telemetry[currentSession.value.patientName]
      
      // Fallback: try to find by userId if direct name match fails
      if (!patientData && currentSession.value.patientUserId) {
        const targetId = Number(currentSession.value.patientUserId)
        for (const key in telemetry) {
          const data = telemetry[key]
          if (data.items && data.items.length > 0) {
            const firstItem = data.items[0]
            // Try to match by userId or memberId
            if (firstItem.userId === targetId || firstItem.memberId === targetId) {
              patientData = data
              break
            }
          }
        }
      }
      
      if (patientData && patientData.items && patientData.items.length > 0) {
        // Initialize with empty values
        const newVitals = {
          heartRate: '',
          bloodPressure: '',
          temperature: '',
          bloodGlucose: '',
          weight: ''
        }
        
        // Iterate through items to find the latest value for each type
        // Assuming items are sorted by time desc (latest first)
        for (const item of patientData.items) {
          // Normalize type if present
          const type = item.type
          
          // Heart Rate
          if (!newVitals.heartRate) {
            if (type === '心率' && item.value) newVitals.heartRate = item.value
            else if (item.heartRate || item.heart_rate) newVitals.heartRate = item.heartRate || item.heart_rate
          }
          
          // Blood Pressure
          if (!newVitals.bloodPressure) {
            if (type === '血压') {
              if (item.systolic && item.diastolic) {
                newVitals.bloodPressure = `${item.systolic}/${item.diastolic}`
              } else if (item.value && item.value !== 'null') {
                newVitals.bloodPressure = item.value
              }
            } else if (item.bloodPressure || item.blood_pressure) {
              newVitals.bloodPressure = item.bloodPressure || item.blood_pressure
            } else if (item.systolic && item.diastolic) {
              newVitals.bloodPressure = `${item.systolic}/${item.diastolic}`
            }
          }
          
          // Temperature
          if (!newVitals.temperature) {
            if (type === '体温' && item.value) newVitals.temperature = item.value
            else if (item.temperature) newVitals.temperature = item.temperature
          }
          
          // Blood Glucose
          if (!newVitals.bloodGlucose) {
            if (type === '血糖' && item.value) newVitals.bloodGlucose = item.value
            else if (item.bloodGlucose || item.blood_glucose) newVitals.bloodGlucose = item.bloodGlucose || item.blood_glucose
          }
          
          // Weight
          if (!newVitals.weight) {
            // Match '体重' or '体重变化'
            if ((type === '体重' || type === '体重变化') && item.value) newVitals.weight = item.value
            else if (item.weight) newVitals.weight = item.weight
          }
          
          // Break if all found
          if (newVitals.heartRate && newVitals.bloodPressure && newVitals.temperature && newVitals.bloodGlucose && newVitals.weight) {
            break
          }
        }
        
        patientVitals.value = newVitals
      } else {
        // Reset if no data found
        patientVitals.value = { heartRate: '', bloodPressure: '', temperature: '', bloodGlucose: '', weight: '' }
      }
    }
  } catch (e) {
    console.error('Failed to load vitals', e)
  } finally {
    loadingVitals.value = false
  }
}

// 消息模板
const messageTemplates = ref([
  { id: 'greeting', label: '您好，有什么可以帮您的吗？', content: '您好，有什么可以帮您的吗？' },
  { id: 'symptom', label: '请详细描述一下您的症状', content: '请详细描述一下您的症状，包括出现时间、持续时间、严重程度等。' },
  { id: 'medication', label: '请告诉我您目前的用药情况', content: '请告诉我您目前的用药情况，包括药物名称、剂量、服用频率等。' },
  { id: 'lifestyle', label: '请描述一下您的生活方式', content: '请描述一下您的生活方式，包括饮食、运动、作息等方面的情况。' },
  { id: 'followup', label: '建议定期复查', content: '建议您定期复查，如有不适请及时就医。' },
  { id: 'rest', label: '注意休息，保持良好作息', content: '注意休息，保持良好作息，有助于身体恢复。' },
])

// 加载会话列表
const loadSessions = async () => {
  if (!currentFamilyId.value) return
  
  sessionsLoading.value = true
  try {
    const res = await listSessionsForDoctor(currentFamilyId.value)
    sessions.value = res?.data || []
    
    // 如果有未读消息的会话，自动选择第一个
    if (sessions.value.length > 0 && !currentSessionId.value) {
      const unreadSession = sessions.value.find(s => s.unreadCountDoctor > 0)
      if (unreadSession) {
        selectSession(unreadSession)
      } else {
        selectSession(sessions.value[0])
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  } finally {
    sessionsLoading.value = false
  }
}

// 监听家庭变化
watch(() => doctorStore.currentFamilyId, (newId) => {
  if (newId) {
    currentFamilyId.value = String(newId)
    loadSessions()
  }
}, { immediate: true })

// 选择会话
const selectSession = async (session) => {
  currentSessionId.value = session.id
  currentSession.value = session
  // Reset vitals and load them
  patientVitals.value = {
    heartRate: '',
    bloodPressure: '',
    temperature: '',
    bloodGlucose: '',
    weight: ''
  }
  // Ideally we should load vitals here to update the status immediately
  // even if the panel is not shown
  await loadPatientVitals()
  
  await loadMessages(session.id)
  // 标记为已读
  try {
    await markMessagesAsRead(session.id)
    // 更新本地未读数
    session.unreadCountDoctor = 0
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 加载消息
const loadMessages = async (sessionId) => {
  messagesLoading.value = true
  try {
    const res = await getSessionMessages(sessionId)
    messages.value = res?.data || []
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    messagesLoading.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSessionId.value || sending.value) return
  
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  sending.value = true
  
  try {
    await apiSendMessage({
      sessionId: currentSessionId.value,
      content,
      messageType: 'TEXT',
      senderType: 'DOCTOR'
    })
    
    // 重新加载消息
    await loadMessages(currentSessionId.value)
    // 重新加载会话列表以更新最后消息时间
    await loadSessions()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
    inputMessage.value = content // 恢复输入内容
  } finally {
    sending.value = false
  }
}

// 使用模板
const useTemplate = (template) => {
  inputMessage.value = template.content
  showTemplates.value = false
}

// 处理操作命令
const handleActionCommand = async (command) => {
  if (!currentSession.value) return
  
  switch (command) {
    case 'createNote':
      // 跳转到患者详情页面，并打开病历记录tab
      router.push(`/doctor/patients?patientUserId=${currentSession.value.patientUserId}&tab=notes`)
      break
    case 'createPlan':
      // 跳转到随访计划页面
      router.push(`/doctor/plans?patientUserId=${currentSession.value.patientUserId}`)
      break
    case 'closeSession':
      try {
        await ElMessageBox.confirm('确定要关闭此会话吗？', '确认关闭', {
          type: 'warning'
        })
        await apiCloseSession(currentSessionId.value)
        ElMessage.success('会话已关闭')
        currentSessionId.value = null
        currentSession.value = null
        messages.value = []
        await loadSessions()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('关闭会话失败:', error)
          ElMessage.error('关闭会话失败')
        }
      }
      break
  }
}

// 家庭切换
const handleFamilyChange = async (familyId) => {
  await doctorStore.setCurrentFamily(familyId)
  currentSessionId.value = null
  currentSession.value = null
  messages.value = []
  await loadSessions()
}

// 工具方法
const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const now = dayjs()
  if (date.isSame(now, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(now, 'year')) {
    return date.format('MM-DD HH:mm')
  } else {
    return date.format('YYYY-MM-DD HH:mm')
  }
}

const getLastMessagePreview = (session) => {
  // 这里可以显示最后一条消息的预览，暂时显示标题
  return session.title || '暂无消息'
}

const getAvatar = (message) => {
  // 根据发送者类型返回头像
  if (message.senderType === 'DOCTOR') {
    return currentSession.value?.doctorAvatar
  } else {
    return currentSession.value?.patientAvatar
  }
}

const scrollToBottom = () => {
  if (messagesContainerRef.value) {
    messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
  }
}

// 定时刷新会话列表（每30秒）
let refreshTimer = null

onMounted(async () => {
  if (doctorStore.currentFamilyId) {
    currentFamilyId.value = String(doctorStore.currentFamilyId)
    await loadSessions()
  }
  
  // 定时刷新
  refreshTimer = setInterval(() => {
    if (currentFamilyId.value) {
      loadSessions()
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 监听路由变化，确保在页面切换时状态正确
watch(() => route.path, (newPath, oldPath) => {
  // 当离开当前页面时，确保状态正确
  if (oldPath?.startsWith('/doctor/consultation') && !newPath?.startsWith('/doctor/consultation')) {
    // 离开咨询页面时的清理操作
    currentSessionId.value = null
    currentSession.value = null
    messages.value = []
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/mixins' as mixins;
@use '@/styles/variables' as vars;

.doctor-consultation {
  padding: 20px;
  height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color-page);
}

:deep(.el-page-header) {
  margin-bottom: 20px;
  
  .el-page-header__content {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.consultation-container {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

// 左侧患者列表
.patient-list-panel {
  width: 320px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  @include mixins.glass-effect;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.panel-header {
  padding: 20px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.patient-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
  }
}

.patient-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 8px;
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;

  &:hover {
    background-color: rgba(64, 158, 255, 0.08);
    transform: translateX(4px);
  }

  &.active {
    background: linear-gradient(90deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.02) 100%);
    border: 1px solid rgba(64, 158, 255, 0.2);
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: var(--el-color-primary);
      border-radius: 0 4px 4px 0;
    }
  }
}

.patient-avatar {
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.patient-info {
  flex: 1;
  min-width: 0;
}

.patient-name {
  font-weight: 600;
  font-size: 15px;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
}

.last-message {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.patient-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

// 右侧聊天窗口
.chat-panel {
  flex: 1;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  @include mixins.glass-effect;
  min-width: 0;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);
  font-size: 14px;
  background: rgba(255, 255, 255, 0.4);
}

.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  z-index: 10;
}

.patient-header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.patient-details {
  .patient-name {
    font-weight: 600;
    font-size: 16px;
    color: var(--el-text-color-primary);
  }

  .patient-meta-text {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
    display: flex;
    align-items: center;
    gap: 8px;
    
    .family-tag {
      background: rgba(0, 0, 0, 0.05);
      padding: 2px 6px;
      border-radius: 4px;
    }
    
    .health-tag {
      height: 20px;
      padding: 0 6px;
      font-size: 12px;
    }
  }
}

.health-profile-panel {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  padding: 12px 24px;
  animation: slideDown 0.3s ease-out;
}

.vitals-grid {
  display: flex;
  gap: 24px;
}

.vital-item {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.6);
  padding: 8px 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  
  .vital-icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.heart { background: rgba(245, 108, 108, 0.1); color: #F56C6C; }
    &.bp { background: rgba(64, 158, 255, 0.1); color: #409EFF; }
    &.glucose { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
    &.temp { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
    &.weight { background: rgba(144, 147, 153, 0.1); color: #909399; }
  }
  
  .vital-data {
    display: flex;
    flex-direction: column;
    
    .label { font-size: 12px; color: var(--el-text-color-secondary); }
    .value { 
      font-size: 16px; 
      font-weight: 600; 
      color: var(--el-text-color-primary);
      
      small { font-size: 12px; font-weight: normal; margin-left: 2px; }
    }
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: rgba(255, 255, 255, 0.4);
  position: relative; // For background overlay

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
  }
}

.ecg-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='300' height='100' viewBox='0 0 300 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 50 L30 50 L40 20 L50 80 L60 50 L300 50' stroke='rgba(64, 158, 255, 0.05)' stroke-width='2' fill='none'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 300px 100px;
  pointer-events: none;
  z-index: 0;
  opacity: 0.5;
}

.message-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  animation: messageSlideIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 1; // Above background

  &.message-doctor {
    flex-direction: row-reverse;

    .message-content {
      align-items: flex-end;
    }

    .message-bubble {
      background: linear-gradient(135deg, var(--el-color-primary) 0%, #3a8ee6 100%);
      color: white;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
      border-radius: 16px 16px 4px 16px;
    }
  }

  &.message-patient {
    .message-bubble {
      background: #ffffff;
      color: var(--el-text-color-primary);
      border: 1px solid rgba(0, 0, 0, 0.05);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      border-radius: 16px 16px 16px 4px;
    }
  }
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.message-avatar {
  flex-shrink: 0;
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-bubble {
  padding: 14px 18px;
  word-wrap: break-word;
  transition: all 0.3s;
  position: relative;
  font-size: 15px;
  line-height: 1.6;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  }
}

.message-text {
  margin-bottom: 6px;
}

.message-time {
  font-size: 12px;
  opacity: 0.8;
  text-align: right;
  margin-top: 4px;
}

// 输入区域
.input-area {
  padding: 20px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 10;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.02);
}

.template-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  animation: slideDown 0.3s ease-out;

  .el-button {
    border-radius: 20px;
    font-size: 13px;
    padding: 8px 16px;
    border: 1px solid rgba(255, 255, 255, 0.6);
    background: rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(4px);
    transition: all 0.3s vars.$ease-spring;
    
    &:hover {
      transform: translateY(-2px);
      background: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary-light-5);
      color: var(--el-color-primary);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    }
  }
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.input-toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}

.input-box {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  :deep(.el-textarea__inner) {
    border-radius: 16px;
    padding: 16px;
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(4px);
    border: 1px solid rgba(255, 255, 255, 0.3);
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
    transition: all 0.3s;
    font-size: 14px;
    line-height: 1.6;
    
    &:focus {
      background: #ffffff;
      box-shadow: 0 0 0 1px var(--el-color-primary) inset, 0 4px 12px rgba(64, 158, 255, 0.1);
      transform: translateY(-1px);
    }
    
    &::placeholder {
      color: var(--el-text-color-placeholder);
    }
  }
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  
  .el-button {
    padding: 10px 28px;
    border-radius: 20px;
    font-weight: 500;
    transition: all 0.3s vars.$ease-spring;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
    }
  }
}

// 响应式设计
@media (max-width: 1366px) {
  .doctor-consultation {
    padding: 16px;
  }

  .patient-list-panel {
    width: 280px;
  }
}

@media (max-width: 768px) {
  .consultation-container {
    flex-direction: column;
  }

  .patient-list-panel {
    width: 100%;
    max-height: 200px;
  }

  .message-content {
    max-width: 85%;
  }
  
  .message-bubble {
    padding: 10px 14px;
    font-size: 14px;
  }
}

</style>
