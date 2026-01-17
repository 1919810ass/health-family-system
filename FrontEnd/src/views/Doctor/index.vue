<template>
  <div class="doctor-page">
    <el-page-header :content="role === 'DOCTOR' ? '家庭健康管理' : '家庭医生对接'" />

    <div class="main-content mt-4">
      <!-- 1. Patient View: Bind Doctor -->
      <template v-if="role !== 'DOCTOR'">
        <!-- Unbound State -->
        <el-card v-if="!doctor && !bindLoading && !checkingBind" class="bind-card">
          <template #header>
            <div class="card-header">
              <span>绑定家庭医生</span>
            </div>
          </template>
          <div class="bind-form">
            <el-alert
              title="您尚未绑定家庭医生"
              type="info"
              description="绑定医生后，您可以享受智能健康周报、在线咨询等专属服务。"
              show-icon
              :closable="false"
              class="mb-4"
            />
            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
              <el-form-item label="医生ID" prop="doctorUserId">
                <el-input v-model.number="form.doctorUserId" placeholder="请输入医生的用户ID" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="bindLoading" @click="bind">立即绑定</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>

        <!-- Loading State -->
        <div v-else-if="checkingBind" class="p-8 text-center">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- Bound State -->
        <div v-else-if="doctor" class="doctor-dashboard">
          <!-- Doctor Info Header -->
          <el-card class="doctor-info-card mb-4" shadow="hover">
            <div class="doctor-profile">
              <el-avatar :size="64" :src="doctor.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <div class="info ml-4">
                <h3 class="text-lg font-bold">{{ doctor.nickname }} <el-tag size="small" type="success" effect="dark">已签约</el-tag></h3>
                <p class="text-gray-500 mt-1">联系电话: {{ doctor.phone || '未公开' }}</p>
              </div>
              <div class="actions ml-auto">
                <el-popconfirm title="确定要解除绑定吗？" @confirm="unbind">
                  <template #reference>
                    <el-button type="danger" plain size="small">解绑医生</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </el-card>

          <el-tabs v-model="activeTab" class="doctor-tabs" type="border-card">
            <!-- Tab 1: Smart Report -->
            <el-tab-pane label="智能健康周报" name="report">
              <!-- Generate Button -->
              <div class="report-actions mb-4 flex-row-between">
                <div>
                   <el-button type="primary" :loading="viewLoading" @click="loadView(true)">
                      <el-icon class="mr-1"><MagicStick /></el-icon> AI 生成最新周报
                   </el-button>
                   <span class="ml-2 text-gray text-sm">基于家庭成员近7日健康数据智能分析</span>
                </div>
                <div class="text-gray text-xs">上次更新: {{ lastReportTime || '无' }}</div>
              </div>

              <!-- Report Content -->
              <div v-if="doctorView" class="report-content">
                <!-- AI Summary -->
                <el-alert
                  v-if="doctorView.summary"
                  :title="doctorView.summary"
                  type="success"
                  :closable="false"
                  show-icon
                  class="mb-4 summary-alert"
                />
                
                <!-- Abnormal Events -->
                <div v-if="doctorView.abnormalEvents && doctorView.abnormalEvents.length > 0" class="section mb-4">
                  <h4 class="section-title mb-2 warning-border">异常事件预警</h4>
                  <el-table :data="doctorView.abnormalEvents" stripe style="width: 100%" size="small">
                    <el-table-column prop="time" label="时间" width="160">
                      <template #default="{ row }">{{ formatTime(row.time) }}</template>
                    </el-table-column>
                    <el-table-column prop="title" label="事件" width="150" />
                    <el-table-column prop="description" label="详情" show-overflow-tooltip />
                    <el-table-column prop="severity" label="等级" width="100">
                      <template #default="{ row }">
                        <el-tag :type="row.severity === 'CRITICAL' ? 'danger' : 'warning'" size="small">{{ row.severity }}</el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>

                <!-- High Risk Members -->
                <div v-if="doctorView.highRiskMembers && doctorView.highRiskMembers.length > 0" class="section mb-4">
                  <h4 class="section-title mb-2 risk-border">重点关注成员</h4>
                  <div class="risk-grid">
                    <el-card v-for="member in doctorView.highRiskMembers" :key="member.userId" shadow="hover" class="member-card">
                      <div class="flex-center">
                        <el-avatar :src="member.avatar" size="small" />
                        <span class="ml-2 font-bold">{{ member.nickname }}</span>
                      </div>
                      <div class="mt-2">
                        <el-tag v-for="tag in member.healthTags" :key="tag" size="small" type="info" class="mr-1 mb-1">{{ tag }}</el-tag>
                      </div>
                      <div class="text-xs text-gray mt-2">最近异常: {{ formatTime(member.lastAbnormalTime) }}</div>
                    </el-card>
                  </div>
                </div>

                <!-- Data Overview (Telemetry) -->
                <div class="section">
                  <h4 class="section-title mb-2 info-border">数据概览</h4>
                  <el-row :gutter="20">
                      <el-col :span="12" :md="8" v-for="(val, key) in doctorView.telemetry" :key="key" class="mb-4">
                          <el-card shadow="never" class="telemetry-card">
                              <template #header>
                                  <div class="font-bold">{{ key }}</div>
                              </template>
                              <div class="telemetry-count">{{ val.count }}</div>
                              <div class="text-center text-xs text-gray">近7日记录数</div>
                              <div class="text-xs text-gray mt-2 border-t pt-2" v-if="val.items && val.items.length">
                                  最近: {{ val.items[0].date.split('T')[0] }}
                              </div>
                          </el-card>
                      </el-col>
                  </el-row>
                </div>
              </div>
              <el-empty v-else description="点击上方按钮生成报告" />
            </el-tab-pane>

            <!-- Tab 2: Online Consultation -->
            <el-tab-pane label="在线咨询" name="chat">
              <div class="chat-layout">
                  <!-- Sidebar: Select Member -->
                  <div class="chat-sidebar">
                      <div class="sidebar-header">咨询成员</div>
                      <div class="member-list">
                          <div 
                              v-for="m in members" 
                              :key="m.userId" 
                              class="member-item"
                              :class="{ 'active': currentChatMemberId === m.userId }"
                              @click="handleChatMemberSelect(m.userId)"
                          >
                              <el-avatar :size="32" :src="m.avatar" class="mr-2" />
                              <div class="member-name">{{ m.nickname }}</div>
                          </div>
                      </div>
                  </div>

                  <!-- Chat Area -->
                  <div class="chat-main">
                      <div class="chat-header">
                          <span class="font-bold">{{ getMemberName(currentChatMemberId) }} 与 {{ doctor.nickname }} 的对话</span>
                          <el-button size="small" circle @click="refreshMessages"><el-icon><Refresh /></el-icon></el-button>
                      </div>
                      
                      <div class="chat-messages" ref="messagesRef">
                          <div v-if="loadingMessages" class="loading-spinner"><el-spinner /></div>
                          <div v-else-if="messages.length === 0" class="empty-chat">
                              <p>暂无消息，发送一条消息开始咨询</p>
                          </div>
                          <div v-for="msg in messages" :key="msg.id" class="message-row" :class="{ 'message-right': msg.senderId === currentUserId }">
                              <div class="message-bubble" 
                                   :class="msg.senderId === currentUserId ? 'bubble-me' : 'bubble-other'">
                                  <div class="message-text">{{ msg.content }}</div>
                                  <div class="message-time">
                                      {{ formatTimeShort(msg.createdAt) }}
                                  </div>
                              </div>
                          </div>
                      </div>
                      
                      <div class="chat-input-area">
                          <el-input
                              v-model="inputMessage"
                              type="textarea"
                              :rows="3"
                              placeholder="请输入咨询内容..."
                              @keyup.enter.ctrl="sendMsg"
                          />
                          <div class="chat-actions">
                              <span class="text-xs text-gray">Ctrl + Enter 发送</span>
                              <el-button type="primary" :loading="sending" @click="sendMsg" :disabled="!inputMessage.trim()">发送</el-button>
                          </div>
                      </div>
                  </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </template>

      <!-- 2. Doctor View: Manage Families -->
      <template v-else>
        <div class="doctor-dashboard">
          <!-- Doctor Header -->
          <el-card class="mb-4" shadow="hover">
            <div class="flex-row-between">
              <div class="flex-center">
                 <el-avatar :size="50" :src="myAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                 <div class="ml-4">
                   <h2 class="text-lg font-bold">欢迎您，{{ myNickname }}医生</h2>
                   <div class="text-gray text-sm mt-1">
                     我的医生ID: <span class="text-primary font-bold">{{ currentUserId }}</span>
                     <el-tag size="small" type="info" class="ml-2">请将ID告知患者进行绑定</el-tag>
                   </div>
                 </div>
              </div>
              <div>
                <span class="mr-2 text-sm text-gray">选择家庭:</span>
                <el-select v-model="selectedFamilyId" placeholder="请选择要查看的家庭" style="width: 220px" @change="onDoctorFamilyChange">
                  <el-option v-for="f in boundFamilies" :key="f.id" :label="f.name" :value="f.id" />
                </el-select>
              </div>
            </div>
          </el-card>

          <!-- Main Content Area (Similar to Patient View but from Doctor perspective) -->
          <div v-if="selectedFamilyId">
            <el-tabs v-model="activeTab" class="doctor-tabs" type="border-card">
              <!-- Tab 1: Family Health Report -->
              <el-tab-pane label="家庭健康档案" name="report">
                <div class="report-actions mb-4 flex-row-between">
                  <div>
                     <el-button type="primary" :loading="viewLoading" @click="loadView(true)">
                        <el-icon class="mr-1"><MagicStick /></el-icon> AI 生成家庭分析
                     </el-button>
                     <span class="ml-2 text-gray text-sm">查看该家庭成员的近期健康状况</span>
                  </div>
                  <div class="text-gray text-xs">上次更新: {{ lastReportTime || '无' }}</div>
                </div>

                <div v-if="doctorView" class="report-content">
                  <!-- AI Summary -->
                  <el-alert
                    v-if="doctorView.summary"
                    :title="doctorView.summary"
                    type="success"
                    :closable="false"
                    show-icon
                    class="mb-4 summary-alert"
                  />
                  
                  <!-- Abnormal Events -->
                  <div v-if="doctorView.abnormalEvents && doctorView.abnormalEvents.length > 0" class="section mb-4">
                    <h4 class="section-title mb-2 warning-border">近期异常事件</h4>
                    <el-table :data="doctorView.abnormalEvents" stripe style="width: 100%" size="small">
                      <el-table-column prop="time" label="时间" width="160">
                        <template #default="{ row }">{{ formatTime(row.time) }}</template>
                      </el-table-column>
                      <el-table-column prop="title" label="事件" width="150" />
                      <el-table-column prop="description" label="详情" show-overflow-tooltip />
                      <el-table-column prop="severity" label="等级" width="100">
                        <template #default="{ row }">
                          <el-tag :type="row.severity === 'CRITICAL' ? 'danger' : 'warning'" size="small">{{ row.severity }}</el-tag>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>

                  <!-- High Risk Members -->
                  <div v-if="doctorView.highRiskMembers && doctorView.highRiskMembers.length > 0" class="section mb-4">
                    <h4 class="section-title mb-2 risk-border">家庭成员风险概览</h4>
                    <div class="risk-grid">
                      <el-card v-for="member in doctorView.highRiskMembers" :key="member.userId" shadow="hover" class="member-card">
                        <div class="flex-center">
                          <el-avatar :src="member.avatar" size="small" />
                          <span class="ml-2 font-bold">{{ member.nickname }}</span>
                        </div>
                        <div class="mt-2">
                          <el-tag v-for="tag in member.healthTags" :key="tag" size="small" type="info" class="mr-1 mb-1">{{ tag }}</el-tag>
                        </div>
                        <div class="text-xs text-gray mt-2">最近异常: {{ formatTime(member.lastAbnormalTime) }}</div>
                      </el-card>
                    </div>
                  </div>

                  <!-- Data Overview -->
                  <div class="section">
                    <h4 class="section-title mb-2 info-border">数据监测概览</h4>
                    <el-row :gutter="20">
                        <el-col :span="12" :md="8" v-for="(val, key) in doctorView.telemetry" :key="key" class="mb-4">
                            <el-card shadow="never" class="telemetry-card">
                                <template #header>
                                    <div class="font-bold">{{ key }}</div>
                                </template>
                                <div class="telemetry-count">{{ val.count }}</div>
                                <div class="text-center text-xs text-gray">近7日记录数</div>
                                <div class="text-xs text-gray mt-2 border-t pt-2" v-if="val.items && val.items.length">
                                    最近: {{ val.items[0].date.split('T')[0] }}
                                </div>
                            </el-card>
                        </el-col>
                    </el-row>
                  </div>
                </div>
                <el-empty v-else description="点击上方按钮生成报告" />
              </el-tab-pane>

              <!-- Tab 2: Online Consultation (Doctor Side) - REMOVED per user request -->
              <!-- <el-tab-pane label="患者咨询" name="chat"> -->
              <!-- ... content removed ... -->
              <!-- </el-tab-pane> -->
            </el-tabs>
          </div>
          
          <div v-else class="p-10 text-center text-gray">
             <el-empty description="请先在上方选择一个家庭进行管理" />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Refresh } from '@element-plus/icons-vue'
import { bindFamilyDoctor, getFamilyDoctor, getDoctorView, unbindFamilyDoctor, getFamilyMembers, getDoctorFamilies } from '../../api/family'
import { getOrCreateSession, getMessages, sendMessage, markRead } from '../../api/consultation'
import request from '../../utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const familyId = ref(localStorage.getItem('current_family_id'))
const checkingBind = ref(true)
const bindLoading = ref(false)
const viewLoading = ref(false)
const doctor = ref(null)
const doctorView = ref(null)
const lastReportTime = ref('')
const activeTab = ref('report')

// Doctor View Refs
const role = ref('')
const myNickname = ref('')
const myAvatar = ref('')
const boundFamilies = ref([])
const selectedFamilyId = ref(null)

// Form
const formRef = ref()
const form = ref({ doctorUserId: null })
const rules = {
  doctorUserId: [{ required: true, message: '请输入医生用户ID', trigger: 'blur' }],
}

// Chat
const members = ref([])
const currentChatMemberId = ref(null)
const currentSessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const loadingMessages = ref(false)
const messagesRef = ref(null)
const currentUserId = ref(null)
let pollTimer = null

// Initialize
onMounted(async () => {
  try {
    const prof = await request.get('/user/profile')
    currentUserId.value = prof.data.id
    role.value = prof.data.role
    myNickname.value = prof.data.nickname
    try {
      // Parse avatar from preferences
      const prefs = JSON.parse(prof.data.preferences || '{}')
      myAvatar.value = prefs.avatar
    } catch {}

    if (role.value === 'DOCTOR') {
      await loadBoundFamilies()
      checkingBind.value = false
    } else {
      if (familyId.value) {
         await checkDoctor()
         if (doctor.value) {
           await loadMembers()
           // If bound, load report automatically if not exists
           loadView(false) 
         }
      }
      checkingBind.value = false
    }
  } catch (e) {
    console.error(e)
    checkingBind.value = false
  }
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

const loadBoundFamilies = async () => {
  try {
    const res = await getDoctorFamilies()
    boundFamilies.value = res.data || []
    if (boundFamilies.value.length > 0) {
      // Auto select first family? No, let doctor choose
    }
  } catch (e) {
    console.error('Failed to load families', e)
  }
}

const onDoctorFamilyChange = async (val) => {
  familyId.value = String(val) // Update context
  doctorView.value = null
  members.value = []
  currentChatMemberId.value = null
  messages.value = []
  
  if (val) {
    await loadMembers()
    if (activeTab.value === 'report') {
      loadView(false)
    }
  }
}

const checkDoctor = async () => {
  try {
    const res = await getFamilyDoctor(familyId.value)
    doctor.value = res.data
  } catch {
    doctor.value = null
  }
}

const bind = async () => {
  if (!familyId.value) return ElMessage.error('请先选择家庭')
  bindLoading.value = true
  try {
    await bindFamilyDoctor(familyId.value, form.value)
    await checkDoctor()
    ElMessage.success('绑定成功')
    await loadMembers()
  } catch (e) {
    ElMessage.error('绑定失败：' + (e.response?.data?.message || '医生不存在'))
  } finally {
    bindLoading.value = false
  }
}

const unbind = async () => {
  try {
    await unbindFamilyDoctor(familyId.value)
    doctor.value = null
    doctorView.value = null
    ElMessage.success('已解绑')
  } catch (e) {
    ElMessage.error('解绑失败')
  }
}

const loadView = async (useAi) => {
  if (!familyId.value) return
  viewLoading.value = true
  try {
    const res = await getDoctorView(familyId.value, useAi)
    doctorView.value = res.data
    lastReportTime.value = dayjs().format('MM-DD HH:mm')
  } catch (e) {
    ElMessage.error('获取周报失败')
  } finally {
    viewLoading.value = false
  }
}

// Chat Logic
const loadMembers = async () => {
    if (!familyId.value) return
    try {
        const res = await getFamilyMembers(familyId.value)
        members.value = res.data
        
        // If doctor view, don't auto select current user (doctor is not in family list usually)
        // Auto select the first member
        if (members.value.length > 0) {
            currentChatMemberId.value = members.value[0].userId
        }
    } catch {}
}

const handleChatMemberSelect = (userId) => {
    currentChatMemberId.value = userId
}

watch([activeTab, currentChatMemberId], async ([tab, memberId]) => {
    // For Doctor: check selectedFamilyId
    // For Patient: check doctor.value
    const isReady = (role.value === 'DOCTOR' && selectedFamilyId.value) || (role.value !== 'DOCTOR' && doctor.value)
    
    if (tab === 'chat' && memberId && isReady) {
        await loadSessionAndMessages(memberId)
    } else {
        if (pollTimer) clearInterval(pollTimer)
    }
})

const loadSessionAndMessages = async (patientId) => {
    loadingMessages.value = true
    if (pollTimer) clearInterval(pollTimer)
    
    try {
        const res = await getOrCreateSession({ 
            patientUserId: patientId, 
            familyId: familyId.value 
        })
        currentSessionId.value = res.data.id
        await refreshMessages()
        
        // Start polling
        pollTimer = setInterval(refreshMessages, 5000)
    } catch (e) {
        ElMessage.error('无法连接到咨询服务')
    } finally {
        loadingMessages.value = false
    }
}

const refreshMessages = async () => {
    if (!currentSessionId.value) return
    try {
        const res = await getMessages(currentSessionId.value)
        messages.value = res.data
        scrollToBottom()
    } catch {}
}

const sendMsg = async () => {
    if (!inputMessage.value.trim() || !currentSessionId.value) return
    sending.value = true
    try {
        await sendMessage({
            sessionId: currentSessionId.value,
            content: inputMessage.value,
            type: 'TEXT'
        })
        inputMessage.value = ''
        await refreshMessages()
    } catch (e) {
        ElMessage.error('发送失败')
    } finally {
        sending.value = false
    }
}

const scrollToBottom = () => {
    nextTick(() => {
        if (messagesRef.value) {
            messagesRef.value.scrollTop = messagesRef.value.scrollHeight
        }
    })
}

const getMemberName = (id) => {
    const m = members.value.find(m => m.userId === id)
    return m ? m.nickname : '成员'
}

const formatTime = (t) => dayjs(t).format('YYYY-MM-DD HH:mm')
const formatTimeShort = (t) => dayjs(t).format('MM-DD HH:mm')
</script>

<style scoped>
.doctor-page { padding: 16px; }
.doctor-dashboard { max-width: 1200px; margin: 0 auto; }
.doctor-profile { display: flex; align-items: center; }
.report-content { margin-top: 16px; }
.summary-alert :deep(.el-alert__content) { font-size: 14px; line-height: 1.6; }

/* Utilities */
.flex-center { display: flex; align-items: center; }
.flex-row-between { display: flex; justify-content: space-between; align-items: center; }
.text-gray { color: #909399; }
.text-xs { font-size: 12px; }
.text-sm { font-size: 14px; }
.text-lg { font-size: 18px; }
.font-bold { font-weight: bold; }
.mb-2 { margin-bottom: 8px; }
.mb-4 { margin-bottom: 16px; }
.mt-2 { margin-top: 8px; }
.mt-4 { margin-top: 16px; }
.ml-2 { margin-left: 8px; }
.ml-4 { margin-left: 16px; }
.mr-1 { margin-right: 4px; }
.mr-2 { margin-right: 8px; }
.text-center { text-align: center; }

/* Report Styles */
.section-title { padding-left: 8px; border-left-width: 4px; border-left-style: solid; }
.warning-border { border-left-color: #f56c6c; }
.risk-border { border-left-color: #e6a23c; }
.info-border { border-left-color: #409eff; }

.risk-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 16px; }
.telemetry-card { height: 100%; }
.telemetry-count { font-size: 24px; font-weight: bold; color: #409eff; text-align: center; margin: 8px 0; }

/* Chat Styles */
.chat-layout { display: flex; height: 600px; border: 1px solid #dcdfe6; border-radius: 4px; background: #fff; }

.chat-sidebar { width: 260px; border-right: 1px solid #dcdfe6; background: #f5f7fa; display: flex; flex-direction: column; }
.sidebar-header { padding: 12px; border-bottom: 1px solid #ebeef5; font-weight: bold; background: #fff; }
.member-list { flex: 1; overflow-y: auto; }
.member-item { padding: 12px; display: flex; align-items: center; cursor: pointer; transition: background 0.3s; }
.member-item:hover { background: #e6e8eb; }
.member-item.active { background: #ecf5ff; border-left: 4px solid #409eff; }
.member-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 14px; }

.chat-main { flex: 1; display: flex; flex-direction: column; }
.chat-header { padding: 12px; border-bottom: 1px solid #ebeef5; display: flex; justify-content: space-between; align-items: center; background: #fff; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; background: #f5f7fa; display: flex; flex-direction: column; }
.loading-spinner { text-align: center; padding: 16px; }
.empty-chat { text-align: center; color: #909399; padding-top: 40px; }

.message-row { margin-bottom: 16px; display: flex; width: 100%; }
.message-right { justify-content: flex-end; }
.message-bubble { max-width: 70%; padding: 10px 14px; border-radius: 8px; position: relative; word-break: break-word; }
.bubble-me { background: #409eff; color: #fff; border-top-right-radius: 2px; }
.bubble-other { background: #fff; color: #303133; border: 1px solid #ebeef5; border-top-left-radius: 2px; }
.message-text { white-space: pre-wrap; line-height: 1.5; }
.message-time { font-size: 12px; margin-top: 4px; opacity: 0.8; }
.bubble-other .message-time { color: #909399; }
.bubble-me .message-time { color: #d9ecff; text-align: right; }

.chat-input-area { padding: 16px; border-top: 1px solid #ebeef5; background: #fff; }
.chat-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
</style>
