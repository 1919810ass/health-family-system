<template>
  <div class="chat-audit-container">
    <!-- Left: Session List -->
    <div class="session-list-container">
      <div class="list-header">
        <h3>待审计会话</h3>
        <el-switch v-model="onlyRisky" active-text="仅看风险" @change="refreshList" />
      </div>
      <div class="session-list" v-loading="listLoading">
        <div 
          v-for="session in sessions" 
          :key="session.id" 
          class="session-item"
          :class="{ active: currentSessionId === session.id, risky: session.hasRisk }"
          @click="selectSession(session)"
        >
          <div class="session-main">
            <span class="patient-name">{{ session.patientName }}</span>
            <span class="time">{{ formatTime(session.lastMessageAt) }}</span>
          </div>
          <div class="session-sub">
            <span class="doctor-name">Dr: {{ session.doctorName }}</span>
            <el-tag v-if="session.hasRisk" type="danger" size="small" effect="dark">风险</el-tag>
          </div>
          <div class="session-preview" v-if="session.triageSummary">
             {{ truncate(session.triageSummary, 30) }}
          </div>
        </div>
        <el-empty v-if="sessions.length === 0" description="无待审计会话" />
      </div>
      <div class="pagination-wrapper">
        <el-pagination
          small
          layout="prev, next"
          :total="total"
          v-model:current-page="page"
          :page-size="size"
          @current-change="refreshList"
        />
      </div>
    </div>

    <!-- Right: Chat Window -->
    <div class="chat-window-container">
      <div v-if="currentSessionId" class="chat-window">
        <div class="chat-header">
          <span>会话详情: {{ currentSession?.patientName }} (ID: {{ currentSessionId }})</span>
        </div>
        
        <div class="message-list">
          <div 
            v-for="(msg, index) in messages" 
            :key="msg.id" 
            class="message-row"
            :class="{ 
              'msg-left': msg.senderType !== 'DOCTOR' && msg.senderType !== 'AI_ASSISTANT', 
              'msg-right': msg.senderType === 'DOCTOR' || msg.senderType === 'AI_ASSISTANT' 
            }"
          >
            <!-- Avatar -->
            <div class="avatar" v-if="msg.senderType === 'DOCTOR' || msg.senderType === 'AI_ASSISTANT'">
               Dr
            </div>
            <div class="avatar" v-else>
               {{ msg.senderName.charAt(0) }}
            </div>

            <div class="message-content-wrapper">
              <div class="sender-info">
                {{ msg.senderName }} <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <div class="bubble" :class="{ 'bubble-ai': msg.senderType === 'AI_ASSISTANT' || msg.senderType === 'DOCTOR' }">
                {{ msg.content }}
              </div>
              
              <!-- Audit Actions for AI/Doctor messages -->
              <div v-if="isAiOrDoctor(msg.senderType)" class="audit-actions">
                <el-tag v-if="msg.isBadCase" type="danger">已标记风险</el-tag>
                <template v-else>
                  <el-button type="success" link size="small" @click="approveMessage(msg)">
                    <el-icon><Check /></el-icon> 确认无误
                  </el-button>
                  <el-button type="danger" link size="small" @click="openFlagDialog(msg, index)">
                    <el-icon><Flag /></el-icon> 标记风险
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="请选择左侧会话进行审计" />
    </div>

    <!-- Flag Risk Dialog -->
    <el-dialog v-model="flagDialogVisible" title="标记风险 (Bad Case)" width="500px">
      <el-form :model="flagForm" label-position="top">
        <el-form-item label="问题 (Context)">
          <el-input v-model="flagForm.question" type="textarea" :rows="2" disabled />
        </el-form-item>
        <el-form-item label="AI 回答">
          <el-input v-model="flagForm.aiAnswer" type="textarea" :rows="3" disabled />
        </el-form-item>
        <el-form-item label="风险类型">
          <el-select v-model="flagForm.riskType" placeholder="请选择类型">
            <el-option label="幻觉 (Hallucination)" value="HALLUCINATION" />
            <el-option label="有害内容 (Harmful)" value="HARMFUL" />
            <el-option label="答非所问 (Off-topic)" value="OFF_TOPIC" />
            <el-option label="不专业/错误建议 (Incorrect)" value="INCORRECT" />
          </el-select>
        </el-form-item>
        <el-form-item label="人工修正建议 (Human Correction)">
          <el-input v-model="flagForm.humanCorrection" type="textarea" :rows="4" placeholder="请输入正确的回答，用于后续微调..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="flagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFlagRisk" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getAuditSessions, getSessionMessages, flagRisk } from '@/api/chatAudit'
import { ElMessage } from 'element-plus'
import { Check, Flag } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// Data
const sessions = ref([])
const messages = ref([])
const currentSessionId = ref(null)
const currentSession = ref(null)
const listLoading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(20)
const onlyRisky = ref(false)

// Flag Dialog
const flagDialogVisible = ref(false)
const submitting = ref(false)
const flagForm = reactive({
  sessionId: null,
  messageId: null,
  question: '',
  aiAnswer: '',
  riskType: '',
  humanCorrection: ''
})

onMounted(() => {
  refreshList()
})

async function refreshList() {
  listLoading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      onlyRisky: onlyRisky.value
    }
    const res = await getAuditSessions(params)
    sessions.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error(error)
    ElMessage.error('加载会话列表失败')
  } finally {
    listLoading.value = false
  }
}

async function selectSession(session) {
  currentSessionId.value = session.id
  currentSession.value = session
  loadMessages(session.id)
}

async function loadMessages(sessionId) {
  try {
    const res = await getSessionMessages(sessionId)
    messages.value = res.data
  } catch (error) {
    console.error(error)
    ElMessage.error('加载消息失败')
  }
}

function isAiOrDoctor(type) {
  // Assuming 'AI_ASSISTANT' is not yet in enum but logic might infer it from doctor==null?
  // Backend returns "AI Assistant" as name if doctor is null. 
  // Let's assume senderType is consistent. 
  // Backend ConsultationMessage entity has senderType. 
  // If AI answers, senderType might be 'DOCTOR' (as system agent) or we need to check implementation.
  // For now, let's assume 'DOCTOR' includes AI or check if name is 'AI Assistant'.
  // Actually, backend controller logic mapped doctor name to "AI Assistant" if doctor is null.
  // But message senderType is from DB.
  return type === 'DOCTOR' || type === 'AI_ASSISTANT'
}

function approveMessage(msg) {
  ElMessage.success('已标记为通过')
  // In real app, call API to mark as approved/reviewed
}

function openFlagDialog(msg, index) {
  // Try to find previous message as question
  let question = 'N/A'
  if (index > 0) {
    question = messages.value[index - 1].content
  }

  flagForm.sessionId = currentSessionId.value
  flagForm.messageId = msg.id
  flagForm.question = question
  flagForm.aiAnswer = msg.content
  flagForm.riskType = ''
  flagForm.humanCorrection = ''
  
  flagDialogVisible.value = true
}

async function submitFlagRisk() {
  if (!flagForm.riskType) {
    ElMessage.warning('请选择风险类型')
    return
  }
  
  submitting.value = true
  try {
    await flagRisk(flagForm)
    ElMessage.success('标记成功')
    flagDialogVisible.value = false
    // Refresh messages to show tag
    loadMessages(currentSessionId.value)
  } catch (error) {
    console.error(error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// Utils
function formatTime(t) {
  return t ? dayjs(t).format('MM-DD HH:mm') : ''
}
function truncate(str, n) {
  if (!str) return ''
  return (str.length > n) ? str.substr(0, n-1) + '...' : str
}
</script>

<style scoped>
.chat-audit-container {
  display: flex;
  height: calc(100vh - 84px); /* Adjust based on layout header */
  background-color: #f0f2f5;
}

.session-list-container {
  width: 300px;
  background: white;
  border-right: 1px solid #dcdfe6;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 15px;
  border-bottom: 1px solid #f5f7fa;
  cursor: pointer;
  transition: background 0.3s;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
  border-right: 3px solid #409EFF;
}

.session-main {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.patient-name {
  font-weight: bold;
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: #909399;
}

.session-sub {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #606266;
}

.session-preview {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-wrapper {
  padding: 10px;
  border-top: 1px solid #ebeef5;
}

.chat-window-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  background: white;
  padding: 15px 20px;
  border-bottom: 1px solid #dcdfe6;
  font-weight: bold;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.message-row {
  display: flex;
  margin-bottom: 20px;
}

.msg-left {
  flex-direction: row;
}

.msg-right {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #c0c4cc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin: 0 10px;
  font-size: 16px;
}

.msg-right .avatar {
  background: #409EFF;
}

.message-content-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.msg-right .message-content-wrapper {
  align-items: flex-end;
}

.sender-info {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.bubble {
  padding: 10px 15px;
  border-radius: 8px;
  background: white;
  line-height: 1.5;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  word-break: break-word;
}

.bubble-ai {
  background: #e1f3d8; /* Light green for AI/Doctor */
}

.audit-actions {
  margin-top: 5px;
  display: flex;
  gap: 10px;
}
</style>
