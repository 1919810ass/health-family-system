<template>
  <div class="consultation-page">
    <!-- 移动端历史记录抽屉 -->
    <el-drawer
      v-model="historyDrawerVisible"
      title="咨询历史"
      direction="ltr"
      size="80%"
      class="mobile-history-drawer"
    >
      <div class="history-list">
        <div 
          v-for="(item, index) in historySessions" 
          :key="index"
          class="history-session-item"
          @click="loadSession(item.sessionId)"
        >
          <div class="session-title">{{ item.title || '无标题会话' }}</div>
          <div class="session-time">{{ formatTime(item.createdAt) }}</div>
        </div>
        <el-empty v-if="historySessions.length === 0" description="暂无历史记录" />
      </div>
    </el-drawer>

    <div class="main-layout glass-effect">
      <!-- 已移除桌面端侧边栏，历史入口迁移至顶部操作条 -->

      <!-- 主聊天区域 -->
      <main class="chat-container">
        <!-- 头部导航 -->
        <div class="page-header">
          <el-button class="hidden-md-and-up mr-2" link @click="historyDrawerVisible = true" style="margin-right: 12px">
            <el-icon :size="24"><Menu /></el-icon>
          </el-button>
          
          <div class="header-icon">
            <el-icon><Service /></el-icon>
          </div>
          <div class="header-content">
            <div style="display: flex; align-items: center; gap: 8px;">
              <h2 class="title">智能健康咨询助手</h2>
              <el-tag size="small" effect="light" round type="success" class="status-tag">
                <span class="dot"></span> 在线
              </el-tag>
            </div>
            <p class="subtitle">24小时为您解答健康疑问，提供专业建议</p>
          </div>

          <div class="header-actions">
            <el-tooltip content="清空当前对话" placement="bottom">
              <el-button circle text @click="clearCurrentDialog" class="hover-rotate">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
            <el-dropdown>
              <el-button circle text class="hover-rotate">
                <el-icon><Menu /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="exportHistoryCSV">导出历史为Excel(CSV)</el-dropdown-item>
                  <el-dropdown-item @click="printCurrentSession">导出当前会话为PDF</el-dropdown-item>
                  <el-dropdown-item @click="statsVisible = true">查看咨询统计</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div class="action-bar">
          <div class="action-left">
            <el-button class="md-button md-green" @click="onNewConsult" v-particles round>
              <el-icon class="mr-6"><Plus /></el-icon>新增咨询
            </el-button>
          </div>
          <div class="action-right">
            <el-popover
              placement="bottom-end"
              trigger="click"
              :width="520"
              v-model:visible="historyPanelVisible"
              popper-class="history-popover glass-popover"
            >
              <template #reference>
                <el-button class="md-button md-blue" v-particles round>
                  <el-icon class="mr-6"><Clock /></el-icon>查看历史
                </el-button>
              </template>
              <div class="history-panel">
                <div class="panel-header">
                  <span>咨询历史</span>
                  <el-button text size="small" @click="refreshHistoryPanel">刷新</el-button>
                </div>
                <el-timeline>
                  <el-timeline-item
                    v-for="(item, idx) in historySessions"
                    :key="idx"
                    :timestamp="formatDateTime(item.createdAt)"
                    placement="top"
                    class="history-item-anim"
                    :style="{ '--delay': idx * 0.05 + 's' }"
                  >
                    <el-card shadow="hover" class="history-card glass-card-sm" @click="openHistoryItem(item)">
                      <div class="card-row">
                        <div class="card-title">{{ item.title || item.question?.slice(0,20) || '新的咨询' }}</div>
                        <el-tag v-if="item.type" size="small" effect="plain" round>{{ item.type }}</el-tag>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
                <el-empty v-if="historySessions.length === 0" description="暂无历史记录" />
              </div>
            </el-popover>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-area custom-scrollbar" ref="chatHistoryRef">
          <div v-if="history.length === 0" class="welcome-screen">
            <div class="welcome-icon pulse-anim">
              <el-icon :size="60" class="welcome-icon-svg"><Service /></el-icon>
            </div>
            <h2 class="gradient-text">您好，我是您的健康助手</h2>
            <p>我可以为您提供健康建议、疾病咨询和生活指导。</p>
            
            <div class="quick-questions">
              <p class="hint">您可以试着问我：</p>
              <div class="chips">
                <el-tag 
                  v-for="(q, idx) in quickQuestions" 
                  :key="q" 
                  class="question-chip" 
                  effect="light" 
                  round
                  @click="useQuickQuestion(q)"
                  :style="{ animationDelay: idx * 0.1 + 's' }"
                >
                  {{ q }}
                </el-tag>
              </div>
            </div>
          </div>

          <div
            v-for="(item, index) in history"
            :key="index"
            class="message-row"
            :class="{ 'user-row': item.question, 'ai-row': item.answer }"
          >
            <!-- 用户消息 -->
            <div v-if="item.question" class="message-wrapper user slide-in-right">
              <div class="bubble user-bubble">
                <div class="text">{{ item.question }}</div>
              </div>
              <el-avatar :size="40" :icon="User" class="avatar user-avatar" />
            </div>

            <!-- AI消息 -->
            <div v-if="item.answer" class="message-wrapper ai slide-in-left">
              <el-avatar :size="40" :icon="ChatDotRound" class="avatar ai-avatar" />
              <div class="content-group">
                <div class="bubble ai-bubble glass-bubble">
                  <div class="markdown-body" v-html="formatContent(item.answer)"></div>
                  <div class="validate" v-if="item.mismatch">
                    <el-tag type="warning" size="small" round>回答可能未对应上一个问题</el-tag>
                    <el-button link size="small" @click="reportMismatch(item)">报错纠正</el-button>
                  </div>
                  <div class="sources" v-if="item.sources && item.sources.length">
                    <span class="label">参考来源:</span>
                    <span v-for="(source, idx) in item.sources" :key="idx" class="source-item">{{ source }}</span>
                  </div>
                </div>
                <div class="message-footer">
                  <span class="time">{{ formatTime(item.createdAt) }}</span>
                  <div class="feedback-actions">
                    <el-tooltip content="有帮助" placement="top">
                      <el-button 
                        size="small" 
                        circle 
                        :type="item.feedback === 1 ? 'success' : ''" 
                        text
                        @click="feedbackConsultation(item.id, 1)"
                        class="hover-scale"
                      >
                        <el-icon><CircleCheck /></el-icon>
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="无帮助" placement="top">
                      <el-button 
                        size="small" 
                        circle 
                        :type="item.feedback === 0 ? 'danger' : ''" 
                        text
                        @click="feedbackConsultation(item.id, 0)"
                        class="hover-scale"
                      >
                        <el-icon><CircleClose /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Loading 状态 -->
          <div v-if="loading && !streamingAnswerId" class="message-row ai-row">
            <div class="message-wrapper ai slide-in-left">
              <el-avatar :size="40" :icon="ChatDotRound" class="avatar ai-avatar" />
              <div class="bubble ai-bubble loading-bubble glass-bubble">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
                <span class="text">正在思考中...</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <footer class="input-area glass-panel-bottom">
          <div class="input-container glass-input">
            <el-button circle text class="voice-btn hover-rotate" @click="handleVoiceInput">
              <el-icon><Microphone /></el-icon>
            </el-button>
            <el-input
              v-model="question"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              placeholder="请输入您的健康问题..."
              class="chat-input-field"
              @keydown.enter.exact.prevent="handleSubmit"
              resize="none"
            />
            <el-button type="primary" circle class="send-btn" :loading="loading" @click="handleSubmit" :disabled="!question.trim()" v-particles>
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
          <div class="disclaimer">
            AI建议仅供参考，不作为医疗诊断依据。如有不适请及时就医。
          </div>
        </footer>
      </main>
    </div>
  </div>

  <el-dialog v-model="statsVisible" title="咨询量统计" width="800px" top="8vh" class="glass-dialog">
    <el-form :inline="true" class="mb-12">
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="statsRange"
          type="daterange"
          unlink-panels
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadStats">生成</el-button>
      </el-form-item>
    </el-form>
    <div ref="statsRef" style="width: 100%; height: 360px" />
    <el-empty v-if="!statsData.length" description="暂无统计数据" />
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { 
  User, ChatDotRound, Loading, Promotion, 
  Menu, Plus, Delete, Service, Microphone, Clock,
  CircleCheck, CircleClose, Headset
} from '@element-plus/icons-vue'
import { consultStream, getConsultationHistory, feedbackConsultation as feedbackConsultationApi, getSessionDetail, getConsultationStats, saveSessionMeta } from '@/api/consultation'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores'
// import { marked } from 'marked' // Assuming marked is available or use simple formatting
import { info as logInfo, error as logError } from '@/utils/logger'

const question = ref('')
const history = ref([])
const loading = ref(false)
const streamingAnswerId = ref(null)
const currentSessionId = ref(null)
const chatHistoryRef = ref(null)
const historyDrawerVisible = ref(false)
const historySessions = ref([])
const pagination = ref({ page: 1, size: 10, total: 0 })
const filters = ref({ keyword: '', type: '', range: [] })
const types = [
  { label: '全部', value: '' },
  { label: '常规咨询', value: 'general' },
  { label: '饮食', value: 'diet' },
  { label: '睡眠', value: 'sleep' },
  { label: '运动', value: 'exercise' },
  { label: '情绪', value: 'emotion' },
  { label: '疾病', value: 'disease' },
]
const sessionsCache = new Map()
const sessionMsgCache = new Map()
const store = useUserStore()
const statsVisible = ref(false)
const statsRef = ref(null)
const statsRange = ref([])
const statsData = ref([])
const enableSessionMetaSave = (import.meta.env?.VITE_ENABLE_SESSION_META_SAVE === 'true')
const historyPanelVisible = ref(false)

const quickQuestions = [
  '如何科学减肥？',
  '经常失眠怎么办？',
  '高血压患者的饮食禁忌',
  '儿童发烧物理降温方法'
]

onMounted(() => {
  // Initialize session
  createNewSession()
  logInfo('ConsultationHistory', 'MOUNT_REFRESH', { filters: filters.value, page: pagination.value.page, size: pagination.value.size })
  loadHistorySessions(true)
})

watch(() => historyPanelVisible.value, (val) => {
  if (val && historySessions.value.length === 0) {
    loadHistorySessions(true)
  }
})

const createNewSession = () => {
  currentSessionId.value = `session-${Date.now()}`
  history.value = []
  historyDrawerVisible.value = false
  ensureSessionMeta({ title: '新的咨询', type: filters.value.type || 'general', createdAt: new Date() })
  loadHistorySessions(true)
}

const onNewConsult = () => {
  createNewSession()
}

const refreshHistoryPanel = () => {
  const start = performance.now()
  loadHistorySessions(true).then(() => {
    const cost = performance.now() - start
    logInfo('ConsultationUI', 'HISTORY_PANEL_REFRESH', { costMs: Math.round(cost) })
  })
}

const openHistoryItem = (item) => {
  loadSession(item.sessionId)
  historyPanelVisible.value = false
}

const handlePageSizeChange = () => {
  pagination.value.page = 1
  loadHistorySessions()
}

const getLocalSessions = () => {
  try {
    const raw = localStorage.getItem('hf_consult_sessions') || '[]'
    return JSON.parse(raw)
  } catch {
    return []
  }
}

const setLocalSessions = (list) => {
  try {
    localStorage.setItem('hf_consult_sessions', JSON.stringify(list || []))
  } catch {}
}

const ensureSessionMeta = (metaPatch = {}) => {
  try {
    const all = getLocalSessions()
    const idx = all.findIndex(s => s.id === currentSessionId.value)
    const base = { id: currentSessionId.value, title: '新的咨询', type: filters.value.type || 'general', createdAt: new Date() }
    const next = Object.assign({}, base, metaPatch)
    if (idx >= 0) all[idx] = Object.assign({}, all[idx], next)
    else all.unshift(next)
    setLocalSessions(all)
  } catch {}
  if (enableSessionMetaSave) {
    saveSessionMeta({ sessionId: currentSessionId.value, title: metaPatch.title || '新的咨询', type: metaPatch.type || (filters.value.type || 'general') }).catch(() => {})
  }
}

const loadHistorySessions = async (force = false) => {
  const traceId = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
  const key = JSON.stringify({ ...filters.value, page: pagination.value.page, size: pagination.value.size })
  const cached = sessionsCache.get(key)
  if (!force && cached && Date.now() - cached.ts < 5 * 60 * 1000) {
    logInfo('ConsultationHistory', 'USE_CACHE', { traceId, key, count: cached.data.items.length })
    historySessions.value = cached.data.items
    pagination.value.total = cached.data.total || cached.data.items.length
    return
  }
  if (force && cached) {
    sessionsCache.delete(key)
    logInfo('ConsultationHistory', 'BYPASS_CACHE', { traceId, key })
  }
  const params = {
    page: pagination.value.page,
    size: pagination.value.size,
    keyword: filters.value.keyword || undefined,
    type: filters.value.type || undefined,
    start: filters.value.range?.[0] || undefined,
    end: filters.value.range?.[1] || undefined,
    scope: store.profile?.role === 'ADMIN' ? 'family' : 'self',
    familyId: store.currentFamily?.id,
  }
  try {
    logInfo('ConsultationHistory', 'REQUEST_START', { traceId, url: '/consultation/history', params })
    const res = await getConsultationHistory(params)
    const items = Array.isArray(res?.data) ? res.data : (res?.data?.items || res?.items || [])
    const total = Array.isArray(res?.data) ? res.data.length : (res?.data?.total ?? res?.total ?? items.length)
    historySessions.value = items
    pagination.value.total = total
    sessionsCache.set(key, { ts: Date.now(), data: { items, total } })
    logInfo('ConsultationHistory', 'RESPONSE_OK', { traceId, count: items.length, total, shape: Array.isArray(res?.data) ? 'array' : typeof res })
    logInfo('ConsultationHistory', 'RENDER_STATE_UPDATED', { traceId, page: pagination.value.page, size: pagination.value.size, total: pagination.value.total })
  } catch (e) {
    logError('ConsultationHistory', 'RESPONSE_ERROR', { traceId, message: e?.message, params })
    const all = getLocalSessions()
    const filtered = all.filter(s => {
      const okKeyword = !filters.value.keyword || String(s.title || '').includes(filters.value.keyword)
      const okType = !filters.value.type || s.type === filters.value.type
      const d = dayjs(s.createdAt)
      const okStart = !filters.value.range?.[0] || d.isAfter(dayjs(filters.value.range[0]).startOf('day')) || d.isSame(dayjs(filters.value.range[0]).startOf('day'))
      const okEnd = !filters.value.range?.[1] || d.isBefore(dayjs(filters.value.range[1]).endOf('day')) || d.isSame(dayjs(filters.value.range[1]).endOf('day'))
      return okKeyword && okType && okStart && okEnd
    })
    pagination.value.total = filtered.length
    const startIdx = (pagination.value.page - 1) * pagination.value.size
    const pageItems = filtered.slice(startIdx, startIdx + pagination.value.size)
    historySessions.value = pageItems
    logInfo('ConsultationHistory', 'FALLBACK_LOCAL', { traceId, count: pageItems.length, total: filtered.length })
  }
}

const loadSession = (sessionId) => {
  currentSessionId.value = sessionId
  const cached = sessionMsgCache.get(sessionId)
  if (cached && Date.now() - cached.ts < 10 * 60 * 1000) {
    history.value = cached.data
    historyDrawerVisible.value = false
    scrollToBottom()
    return
  }
  fetchSessionMessages(sessionId)
  historyDrawerVisible.value = false
  ElMessage.info('加载会话历史: ' + sessionId)
}

const fetchSessionMessages = async (sessionId) => {
  try {
    const res = await getConsultationHistory({ sessionId })
    const items = Array.isArray(res?.data) ? res.data : (res?.data?.items || res?.items || [])
    const msgs = []
    items.forEach(m => {
      msgs.push({
        id: m.id,
        question: m.question,
        createdAt: m.createdAt || new Date()
      })
      msgs.push({
        id: m.id,
        answer: m.answer,
        sources: m.sources || [],
        feedback: m.feedback ?? -1,
        createdAt: m.createdAt || new Date()
      })
    })
    for (let i = 0; i < msgs.length; i += 2) {
      const q = msgs[i]
      const a = msgs[i + 1]
      if (a) a.mismatch = !isAnswerMatchingQuestion(a.answer, q?.question)
    }
    history.value = msgs
    sessionMsgCache.set(sessionId, { ts: Date.now(), data: history.value })
    nextTick(scrollToBottom)
  } catch (e) {
    history.value = []
  }
}

const useQuickQuestion = (q) => {
  question.value = q
  handleSubmit()
}

const handleSubmit = async () => {
  if (!question.value.trim() || loading.value) return

  const userQ = question.value
  question.value = ''
  ensureSessionMeta({ title: userQ.slice(0, 20), createdAt: new Date() })
  
  const tempId = Date.now()
  history.value.push({
    id: tempId,
    question: userQ,
    createdAt: new Date()
  })
  
  scrollToBottom()
  loading.value = true

  try {
    const answerMsg = {
      id: tempId + 1,
      answer: '',
      sources: [],
      feedback: -1,
      createdAt: new Date()
    }
    history.value.push(answerMsg)
    streamingAnswerId.value = answerMsg.id
    const typingQueue = []
    let typewriterTimer = null

    const flushTypewriter = () => {
      if (typewriterTimer) return
      typewriterTimer = setInterval(() => {
        if (typingQueue.length === 0) {
          clearInterval(typewriterTimer)
          typewriterTimer = null
          return
        }
        answerMsg.answer += typingQueue.shift()
        scrollToBottom()
      }, 20)
    }

    const onChunk = (chunk) => {
      if (!chunk) return
      for (const ch of chunk) {
        typingQueue.push(ch)
      }
      flushTypewriter()
    }

    const onError = (error) => {
      if (typewriterTimer) {
        clearInterval(typewriterTimer)
        typewriterTimer = null
      }
      typingQueue.length = 0
      streamingAnswerId.value = null
      const msg = String(error?.message || '')
      if (msg.includes('CUDA') || msg.includes('llama runner')) {
        ElNotification({
          title: 'AI服务故障',
          message: 'GPU计算服务异常（CUDA错误），已通知运维，请稍后再试',
          type: 'error',
          duration: 4000
        })
      } else {
        ElMessage.error('网络请求失败，请稍后重试')
      }
    }

    const onComplete = async () => {
      if (typewriterTimer || typingQueue.length) {
        await new Promise(resolve => {
          const checker = setInterval(() => {
            if (!typewriterTimer && typingQueue.length === 0) {
              clearInterval(checker)
              resolve()
            }
          }, 20)
        })
      }
      streamingAnswerId.value = null
      const lastQIndex = [...history.value].reverse().findIndex(m => !!m.question)
      const lastQ = lastQIndex >= 0 ? history.value[history.value.length - 1 - lastQIndex] : null
      answerMsg.mismatch = !isAnswerMatchingQuestion(answerMsg.answer, lastQ?.question)
      if (enableSessionMetaSave) {
        await saveSessionMeta({ sessionId: currentSessionId.value, title: history.value[0]?.question?.slice(0, 20) || userQ.slice(0, 20) || '新的咨询', type: filters.value.type || 'general' }).catch(() => {})
        loadHistorySessions(true)
      } else {
        ensureSessionMeta({ title: history.value[0]?.question?.slice(0, 20) || userQ.slice(0, 20) || '新的咨询', createdAt: new Date() })
        loadHistorySessions(true)
      }
      if (currentSessionId.value) {
        await fetchSessionMessages(currentSessionId.value)
      }
    }

    await consultStream({
      question: userQ,
      sessionId: currentSessionId.value
    }, onChunk, onError, onComplete)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const clearCurrentDialog = () => {
  history.value = []
}

const feedbackConsultation = async (id, type) => {
  try {
    const res = await feedbackConsultationApi(id, { feedback: type })
    if (res.code === 0) {
      const item = history.value.find(h => h.id === id)
      if (item) {
        item.feedback = type
      }
      ElMessage.success('感谢您的反馈')
    }
  } catch (e) {
    // Silent fail or notify
  }
}

const handleVoiceInput = () => {
  ElNotification({
    title: '功能开发中',
    message: '语音输入功能即将上线，敬请期待',
    type: 'info',
    duration: 2000
  })
}

const formatTime = (time) => {
  return dayjs(time).format('HH:mm')
}

const formatDateTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const formatContent = (content) => {
  // Simple formatting: convert newlines to <br>
  // In a real app, use a markdown library
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

const normalize = (s) => {
  return String(s || '')
    .replace(/[^\p{L}\p{N}\s]/gu, ' ')
    .toLowerCase()
    .split(/\s+/)
    .filter(Boolean)
}

const isAnswerMatchingQuestion = (answer, question) => {
  const qTokens = normalize(question)
  const aTokens = normalize(answer)
  if (!qTokens.length || !aTokens.length) return true
  const setA = new Set(aTokens)
  let hit = 0
  qTokens.forEach(t => { if (setA.has(t)) hit++ })
  const ratio = hit / Math.max(3, qTokens.length)
  return ratio >= 0.4
}

const reportMismatch = (item) => {
  if (!item?.id) return
  feedbackConsultation(item.id, 0)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatHistoryRef.value) {
      chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight
    }
  })
}

const exportHistoryCSV = () => {
  const rows = [['咨询时间', '类型', '标题']]
  historySessions.value.forEach(s => {
    rows.push([formatDateTime(s.createdAt), s.type || '', s.title || ''])
  })
  const csvContent = rows.map(r => r.map(v => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\r\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `consult_history_${dayjs().format('YYYYMMDD_HHmm')}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const printCurrentSession = () => {
  const el = document.querySelector('.messages-area')
  if (!el) return
  const w = window.open('', '_blank')
  const css = `
    <style>
      body { font-family: system-ui; padding: 24px; }
      .bubble { border: 1px solid #eee; border-radius: 8px; padding: 12px; margin-bottom: 8px; }
      .user { background: #f0f7ff }
      .ai { background: #fff }
      .time { color: #999; font-size: 12px; }
    </style>
  `
  w.document.write('<html><head><title>咨询记录</title>' + css + '</head><body>' + el.innerHTML + '</body></html>')
  w.document.close()
  w.focus()
  w.print()
  w.close()
}

const loadStats = async () => {
  const params = { start: statsRange.value?.[0], end: statsRange.value?.[1] }
  try {
    const res = await getConsultationStats(params)
    statsData.value = res?.data?.series || res?.series || []
  } catch {
    // fallback from current sessions
    const map = new Map()
    historySessions.value.forEach(s => {
      const d = dayjs(s.createdAt).format('YYYY-MM-DD')
      map.set(d, (map.get(d) || 0) + 1)
    })
    statsData.value = Array.from(map.entries()).map(([date, count]) => ({ date, count })).sort((a,b) => a.date.localeCompare(b.date))
  }
  drawStats()
}

const drawStats = () => {
  if (!statsRef.value) return
  const chart = echarts.getInstanceByDom(statsRef.value) || echarts.init(statsRef.value)
  const dates = statsData.value.map(d => d.date)
  const counts = statsData.value.map(d => d.count)
  chart.setOption({
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: counts, smooth: true }],
    tooltip: { trigger: 'axis' }
  })
}
</script>

<style lang="scss" scoped>
@use "sass:color";
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.consultation-page {
  height: calc(100vh - 84px);
  background-color: transparent;
  display: flex;
  flex-direction: column;
}

.main-layout {
  display: flex;
  height: 100%;
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  overflow: hidden;
  
  @media (min-width: 1200px) {
    border-radius: vars.$radius-lg;
    margin: 20px auto;
    height: calc(100% - 40px);
  }
  
  &.glass-effect {
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.5);
  }
}

/* Chat Area */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: transparent;
  position: relative;
  
  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    gap: 16px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: stretch;
    }
    .action-left, .action-right {
      display: flex;
      align-items: center;
    }
    .md-button {
      width: 120px;
      height: 40px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 600;
      display: inline-flex;
      justify-content: center;
      align-items: center;
      gap: 6px;
      box-shadow: vars.$shadow-sm;
      transition: all 0.3s vars.$ease-spring;
      
      &:hover {
        transform: translateY(-2px) scale(1.02);
        box-shadow: vars.$shadow-md;
      }
      
      &:active {
        transform: scale(0.95);
      }
    }
    .md-green {
      background: linear-gradient(135deg, vars.$success-color 0%, color.adjust(vars.$success-color, $lightness: 10%) 100%);
      color: #fff;
      border: none;
    }
    .md-blue {
      background: linear-gradient(135deg, vars.$primary-color 0%, color.adjust(vars.$primary-color, $lightness: 10%) 100%);
      color: #fff;
      border: none;
    }
  }
  
  .page-header {
    height: 64px;
    padding: 0 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(0,0,0,0.05);
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10px);
    z-index: 10;
    
    .header-icon {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: linear-gradient(135deg, vars.$success-color, color.adjust(vars.$success-color, $lightness: 15%));
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(vars.$success-color, 0.3);
      margin-right: 12px;

      .el-icon {
        font-size: 20px;
        color: #fff;
      }
    }

    .header-content {
      flex: 1;
      min-width: 0;
      
      .title {
        font-size: 18px;
        font-weight: 700;
        color: vars.$text-main-color;
        margin: 0;
        @include mixins.text-gradient(linear-gradient(to right, vars.$text-main-color, vars.$success-color));
      }

      .subtitle {
        font-size: 12px;
        color: vars.$text-secondary-color;
        margin: 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
      margin-left: 16px;
    }
  }
  
  .messages-area {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background-color: transparent;
    
    .welcome-screen {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: vars.$text-secondary-color;
      animation: fadeInUp 0.6s vars.$ease-spring;
      
      .welcome-icon {
        margin-bottom: 20px;
        padding: 24px;
        background: rgba(vars.$primary-color, 0.1);
        border-radius: 50%;
        backdrop-filter: blur(5px);
        
        &.pulse-anim {
          animation: pulse 2s infinite;
        }
        
        .welcome-icon-svg {
          color: vars.$primary-color;
        }
      }
      
      h2 {
        font-size: 24px;
        margin-bottom: 12px;
        background: linear-gradient(120deg, vars.$primary-color, vars.$secondary-color);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        font-weight: 700;
      }
      
      .quick-questions {
        margin-top: 40px;
        text-align: center;
        
        .hint {
          font-size: 14px;
          margin-bottom: 16px;
          color: vars.$text-secondary-color;
        }
        
        .chips {
          display: flex;
          flex-wrap: wrap;
          justify-content: center;
          gap: 12px;
          max-width: 600px;
          
          .question-chip {
            cursor: pointer;
            padding: 8px 20px;
            font-size: 14px;
            border-radius: 20px;
            background: rgba(255, 255, 255, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.8);
            transition: all 0.3s vars.$ease-spring;
            animation: fadeInUp 0.5s vars.$ease-spring backwards;
            
            &:hover {
              transform: translateY(-2px) scale(1.05);
              box-shadow: vars.$shadow-sm;
              background: #fff;
              color: vars.$primary-color;
              border-color: vars.$primary-color;
            }
          }
        }
      }
    }
  }
}

.history-popover {
  &.glass-popover {
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.6);
  }
  
  .history-panel {
    max-height: 420px;
    overflow-y: auto;
    padding: 12px;
  }
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 4px 12px;
    font-weight: 600;
    color: vars.$text-primary-color;
  }
  .history-card {
    transition: all 0.3s vars.$ease-spring;
    border: none;
    
    &.glass-card-sm {
      background: rgba(255, 255, 255, 0.5);
      backdrop-filter: blur(4px);
      border: 1px solid rgba(255, 255, 255, 0.3);
    }
    
    .card-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
    }
    .card-title {
      font-size: 15px;
      color: vars.$text-primary-color;
      font-weight: 500;
    }
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(0,0,0,0.08);
      background: rgba(255, 255, 255, 0.8);
    }
  }
  
  .history-item-anim {
    animation: slideInRight 0.4s vars.$ease-spring backwards;
    animation-delay: var(--delay);
  }
}

/* Message Bubbles */
.message-row {
  display: flex;
  margin-bottom: 24px;
  
  &.user-row {
    justify-content: flex-end;
  }
  
  .message-wrapper {
    display: flex;
    max-width: 85%;
    
    &.slide-in-right {
      animation: slideInRight 0.4s vars.$ease-spring;
    }
    
    &.slide-in-left {
      animation: slideInLeft 0.4s vars.$ease-spring;
    }
    
    &.user {
      flex-direction: row;
      align-items: flex-start;
      
      .bubble {
        background: linear-gradient(135deg, vars.$primary-color 0%, color.adjust(vars.$primary-color, $lightness: 10%) 100%);
        color: #fff;
        border-radius: 18px 18px 4px 18px;
        margin-right: 12px;
        box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);
      }
      
      .user-avatar {
        background: rgba(vars.$primary-color, 0.1);
        color: vars.$primary-color;
        border: 2px solid #fff;
        box-shadow: vars.$shadow-sm;
      }
    }
    
    &.ai {
      flex-direction: row;
      align-items: flex-start;
      
      .ai-avatar {
        margin-right: 12px;
        background: linear-gradient(135deg, #fff 0%, #f0f2f5 100%);
        color: vars.$primary-color;
        border: 2px solid #fff;
        box-shadow: vars.$shadow-sm;
      }
      
      .bubble {
        background: rgba(255, 255, 255, 0.8);
        color: vars.$text-primary-color;
        border-radius: 4px 18px 18px 18px;
        box-shadow: 0 2px 12px rgba(0,0,0,0.05);
        border: 1px solid rgba(255, 255, 255, 0.6);
        
        &.glass-bubble {
          backdrop-filter: blur(8px);
        }
      }
    }
    
    .bubble {
      padding: 14px 18px;
      font-size: 15px;
      line-height: 1.6;
      position: relative;
      word-break: break-word;
      
      .text {
        white-space: pre-wrap;
      }
    }
    
    .content-group {
      display: flex;
      flex-direction: column;
    }
    
    .message-footer {
      margin-top: 6px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 4px;
      
      .time {
        font-size: 12px;
        color: vars.$text-secondary-color;
      }
      
      .feedback-actions {
        opacity: 0;
        transition: opacity 0.2s;
        transform: scale(0.9);
      }
    }
    
    &:hover .feedback-actions {
      opacity: 1;
      transform: scale(1);
    }
  }
}

/* Input Area */
.input-area {
  padding: 20px;
  background-color: transparent;
  
  &.glass-panel-bottom {
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    border-top: 1px solid rgba(255, 255, 255, 0.5);
  }
  
  .input-container {
    display: flex;
    align-items: flex-end;
    background-color: rgba(255, 255, 255, 0.7);
    border-radius: 24px;
    padding: 8px 12px;
    border: 1px solid rgba(0, 0, 0, 0.05);
    transition: all 0.3s;
    box-shadow: vars.$shadow-sm;
    
    &.glass-input {
      backdrop-filter: blur(4px);
    }
    
    &:focus-within {
      background-color: #fff;
      border-color: vars.$primary-color;
      box-shadow: 0 0 0 3px rgba(vars.$primary-color, 0.15);
      transform: translateY(-2px);
    }
    
    .chat-input-field {
      flex: 1;
      margin: 0 8px;
      
      :deep(.el-textarea__inner) {
        box-shadow: none;
        background: transparent;
        padding: 8px 0;
        min-height: 24px !important;
        color: vars.$text-primary-color;
        
        &::placeholder {
          color: vars.$text-secondary-color;
        }
      }
    }
    
    .voice-btn {
      color: vars.$text-secondary-color;
      transition: all 0.3s;
      
      &:hover {
        color: vars.$primary-color;
        background-color: rgba(vars.$primary-color, 0.1);
      }
    }
    
    .send-btn {
      flex-shrink: 0;
      background: linear-gradient(135deg, vars.$primary-color 0%, color.adjust(vars.$primary-color, $lightness: 10%) 100%);
      border: none;
      box-shadow: 0 4px 10px rgba(vars.$primary-color, 0.3);
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      
      &:hover {
        transform: scale(1.1) rotate(-10deg);
        box-shadow: 0 6px 14px rgba(vars.$primary-color, 0.4);
      }
      
      &:active {
        transform: scale(0.95);
      }
      
      &:disabled {
        background: #ccc;
        box-shadow: none;
        transform: none;
      }
    }
  }
  
  .disclaimer {
    margin-top: 12px;
    text-align: center;
    font-size: 12px;
    color: vars.$text-secondary-color;
    opacity: 0.8;
  }
}

/* Utilities & Animations */
.custom-scrollbar {
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
    &:hover {
      background-color: rgba(0, 0, 0, 0.2);
    }
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
}

.typing-indicator {
  display: inline-flex;
  align-items: center;
  margin-bottom: 4px;
  
  span {
    height: 6px;
    width: 6px;
    margin: 0 2px;
    background-color: vars.$primary-color;
    border-radius: 50%;
    animation: typing 1s infinite ease-in-out;
    
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0% { transform: translateY(0); opacity: 0.5; }
  50% { transform: translateY(-4px); opacity: 1; }
  100% { transform: translateY(0); opacity: 0.5; }
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(vars.$primary-color, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(vars.$primary-color, 0); }
  100% { box-shadow: 0 0 0 0 rgba(vars.$primary-color, 0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInRight {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes fadeInLeft {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

.hover-rotate {
  transition: transform 0.3s;
  &:hover {
    transform: rotate(90deg);
  }
}

.hover-scale {
  transition: transform 0.2s;
  &:hover {
    transform: scale(1.1);
  }
}

/* Helper classes for responsiveness */
.new-session-btn {
  font-weight: 600;
}
.hidden-sm-and-down {
  @media (max-width: 768px) {
    display: none !important;
  }
}

.hidden-md-and-up {
  @media (min-width: 769px) {
    display: none !important;
  }
}

.text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
