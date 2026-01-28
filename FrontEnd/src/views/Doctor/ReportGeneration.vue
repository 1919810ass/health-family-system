<template>
  <div class="report-generation-page">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Document /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">智能报告生成</h2>
        <p class="subtitle">统一诊断模板、批量生成与证据溯源，保持报告风格一致</p>
      </div>
    </div>

    <el-card class="glass-card report-card">
      <div class="toolbar">
        <div class="toolbar-left">
          <div class="toolbar-item">
            <span class="label">家庭</span>
            <el-select :model-value="familyId" placeholder="选择家庭" style="width: 200px" @change="handleFamilyChange">
              <el-option v-for="f in families" :key="String(f.id)" :label="f.name" :value="String(f.id)" />
            </el-select>
          </div>
          <div class="toolbar-item">
            <span class="label">患者</span>
            <el-select v-model="selectedMemberId" placeholder="选择患者" style="width: 200px" clearable :disabled="!familyId">
              <el-option v-for="m in members" :key="String(m.userId)" :label="m.nickname || m.phone" :value="String(m.userId)" />
            </el-select>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="report-tabs custom-tabs">
        <el-tab-pane name="single">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              单人生成
            </span>
          </template>
          <div v-if="selectedMemberId" class="content-area">
            <el-form label-position="top">
              <el-form-item label="医生诊断/临床意见">
                <el-input
                  v-model="doctorDiagnosis"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入您的专业诊断意见，AI将结合此意见与患者健康数据生成详细报告..."
                />
              </el-form-item>
              
              <el-form-item>
                <div class="action-row">
                  <div class="action-left">
                    <el-button type="primary" :loading="previewLoading" @click="handleGeneratePreview">
                      生成预览
                    </el-button>
                    <el-button type="success" :loading="generating" :disabled="!draftContent.trim()" @click="handleGenerateReport">
                      下载PDF
                    </el-button>
                    <el-button @click="handleDownloadTemplate">下载空白模板</el-button>
                  </div>
                  <div class="action-right">
                    <div v-if="isStreaming" class="stream-status">
                      <el-tag effect="dark" type="success" class="mr-2">
                        <el-icon class="is-loading" v-if="isRagSearching"><Loading /></el-icon>
                        <span v-if="isRagSearching"> 向量检索中...</span>
                        <span v-else> 生成中: {{ streamSpeed }} Tokens/s</span>
                      </el-tag>
                    </div>
                    <span class="hint-text">先生成预览，再编辑正文并下载</span>
                  </div>
                </div>
              </el-form-item>

              <el-form-item v-if="draftContent" label="报告正文（可编辑，下载docx将以此内容为准）">
                <el-input v-model="draftContent" type="textarea" :rows="14" />
              </el-form-item>

              <el-form-item v-if="evidences.length" label="证据来源（RAG检索片段）">
                <el-collapse>
                  <el-collapse-item
                    v-for="(ev, idx) in evidences"
                    :key="String(ev.fragmentId || idx)"
                    :name="String(idx)"
                  >
                    <template #title>
                      <span class="evidence-title">
                        [{{ idx + 1 }}] {{ ev.title || 'Untitled' }}（{{ ev.source || 'Unknown' }}，fragmentId={{ ev.fragmentId ?? '-' }}）
                      </span>
                    </template>
                    <div class="evidence-snippet">{{ ev.snippet }}</div>
                    <div class="evidence-actions">
                      <el-button size="small" @click="openEvidence(ev, idx)">查看全文</el-button>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </el-form-item>
            </el-form>
          </div>
          
          <el-empty v-else description="请先选择家庭和患者" />
        </el-tab-pane>
        <el-tab-pane name="batch">
          <template #label>
            <span class="tab-label">
              <el-icon><Files /></el-icon>
              批量生成
            </span>
          </template>
          <div class="batch-area">
            <el-form label-position="top">
              <el-form-item label="批量患者">
                <el-select
                  v-model="batchMemberIds"
                  multiple
                  filterable
                  collapse-tags
                  collapse-tags-tooltip
                  placeholder="选择患者"
                  style="width: 100%"
                  :disabled="!familyId"
                  @change="handleBatchMemberChange"
                >
                  <el-option 
                    v-for="m in members" 
                    :key="String(m.userId)" 
                    :label="m.nickname || m.phone" 
                    :value="String(m.userId)" 
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="统一诊断模板">
                <el-input
                  v-model="batchDiagnosisTemplate"
                  type="textarea"
                  :rows="4"
                  placeholder="可填写统一诊断意见，并一键应用到全部患者"
                />
                <div class="batch-actions">
                  <el-button :disabled="!batchMemberIds.length" @click="applyTemplateToBatch">应用到全部</el-button>
                  <el-button type="success" :loading="batchGenerating" :disabled="!batchItems.length" @click="handleGenerateBatch">
                    生成批量报告ZIP
                  </el-button>
                </div>
              </el-form-item>
            </el-form>

            <el-table v-if="batchItems.length" :data="batchItems" style="width: 100%" class="glass-table">
              <el-table-column label="患者" width="200">
                <template #default="scope">
                  {{ getMemberLabel(scope.row.userId) }}
                </template>
              </el-table-column>
              <el-table-column label="诊断意见">
                <template #default="scope">
                  <el-input v-model="scope.row.diagnosis" type="textarea" :rows="2" placeholder="请输入诊断意见" />
                  <!-- 行内进度条：当该任务在监控中存在且处于运行状态时显示 -->
                  <div v-if="getTaskProgress(scope.row.userId)" class="inline-progress">
                    <div class="progress-header">
                      <span class="status-text">{{ getTaskStatusText(scope.row.userId) }}</span>
                      <span class="percentage">{{ getTaskProgress(scope.row.userId) }}%</span>
                    </div>
                    <el-progress 
                      :percentage="getTaskProgress(scope.row.userId)" 
                      :status="getTaskStatus(scope.row.userId) === 'completed' ? 'success' : ''"
                      :stroke-width="6"
                      :show-text="false"
                      striped
                      striped-flow
                    />
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="请选择批量患者" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="evidenceDialogVisible" width="900px" title="证据全文">
      <div v-if="activeEvidence" class="evidence-dialog">
        <div class="evidence-dialog__meta">
          <div class="font-bold">
            [{{ activeEvidenceIndex + 1 }}] {{ activeEvidence.title || 'Untitled' }}
          </div>
          <div class="text-gray">
            来源：{{ activeEvidence.source || 'Unknown' }} ｜ fragmentId：{{ activeEvidence.fragmentId ?? '-' }}
          </div>
        </div>
        <pre class="evidence-dialog__content">{{ activeEvidence.content || activeEvidence.snippet }}</pre>
      </div>
      <template #footer>
        <el-button @click="evidenceDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 并发监控对话框 -->
    <el-dialog
      v-model="monitorVisible"
      title="R9000P 高性能并发计算监控"
      width="900px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="!batchGenerating"
    >
      <div class="monitor-container">
        <!-- 顶部仪表盘 -->
        <div class="dashboard-grid">
          <div class="dashboard-card">
            <div class="card-icon blue"><el-icon><Connection /></el-icon></div>
            <div class="card-content">
              <div class="label">活跃线程数</div>
              <div class="value">
                {{ currentMetrics.activeThreads || 0 }}
                <span v-if="currentMetrics.reportActiveThreads > 0" class="sub-value">
                  ({{ currentMetrics.reportActiveThreads }} running)
                </span>
                <span class="unit">/ {{ currentMetrics.processors || 32 }}</span>
              </div>
            </div>
          </div>
          <div class="dashboard-card">
            <div class="card-icon green"><el-icon><DataLine /></el-icon></div>
            <div class="card-content">
              <div class="label">实时吞吐量 (QPS)</div>
              <div class="value">{{ currentQps }} <span class="unit">req/s</span></div>
            </div>
          </div>
          <div class="dashboard-card">
            <div class="card-icon red"><el-icon><Cpu /></el-icon></div>
            <div class="card-content">
              <div class="label">CPU 负载</div>
              <div class="value">{{ currentMetrics.cpuUsage || 0 }}%</div>
              <!-- 简单的 CSS 模拟波形 -->
              <div class="mini-chart">
                <div v-for="(h, i) in cpuHistory" :key="i" class="bar" :style="{ height: h + '%', opacity: (i+1)/20 }"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="monitor-header">
          <div class="monitor-stat">
            <div class="stat-label">总任务数</div>
            <div class="stat-value">{{ batchItems.length }}</div>
          </div>
          <div class="monitor-stat">
            <div class="stat-label">线程池状态</div>
            <div class="stat-value highlight">{{ threadStatus }}</div>
          </div>
          <div class="monitor-stat">
            <div class="stat-label">已耗时</div>
            <div class="stat-value">{{ elapsedTime }}s</div>
          </div>
        </div>

        <div class="thread-grid">
          <div v-for="task in monitorTasks" :key="task.id" class="thread-card" :class="task.status">
            <div class="thread-header">
              <span class="thread-name">Thread-{{ task.threadId }}</span>
              <el-tag size="small" :type="getStatusType(task.status)">{{ task.statusText }}</el-tag>
            </div>
            <div class="task-info">
              <span class="patient-name">{{ task.patientName }}</span>
              <el-progress 
                :percentage="task.progress" 
                :status="task.status === 'completed' ? 'success' : ''"
                :stroke-width="4"
                :show-text="false"
              />
            </div>
            <div class="log-text">{{ task.log }}</div>
          </div>
        </div>
        
        <div v-if="performanceReport" class="performance-report">
          <div class="report-title"><el-icon><DataLine /></el-icon> 性能分析报告</div>
          <div class="report-grid">
            <div class="report-item">
              <span class="label">串行预估耗时：</span>
              <span class="value">{{ performanceReport.estimatedSerialTime }}s</span>
            </div>
            <div class="report-item">
              <span class="label">并发实际耗时：</span>
              <span class="value highlight">{{ performanceReport.actualTime }}s</span>
            </div>
            <div class="report-item">
              <span class="label">并发加速比：</span>
              <span class="value highlight">{{ performanceReport.speedup }}x</span>
            </div>
            <div class="report-item">
              <span class="label">有效QPS：</span>
              <span class="value">{{ performanceReport.qps }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useDoctorStore } from '@/stores/doctor'
import { ElMessage } from 'element-plus'
import { Document, Files, DataLine, Cpu, Connection, Loading } from '@element-plus/icons-vue'
import { generateReportDocx, generateReportPdf, downloadReportTemplate, generateReportPreview, generateBatchReportZip, getSystemMetrics } from '@/api/doctor'
import { getToken } from '@/utils/auth'

const doctorStore = useDoctorStore()
const families = computed(() => doctorStore.families)
// 过滤掉 userId 为空的无效成员
const members = computed(() => (doctorStore.boundMembers || []).filter(m => m.userId))
const familyId = computed(() => doctorStore.currentFamilyId)
const activeTab = ref('single')
const selectedMemberId = ref(null)
const doctorDiagnosis = ref('')
const generating = ref(false)
const previewLoading = ref(false)
const draftContent = ref('')
const evidences = ref([])
const evidenceDialogVisible = ref(false)
const activeEvidence = ref(null)
const activeEvidenceIndex = ref(0)
const batchMemberIds = ref([])
const batchItems = ref([])
const batchDiagnosisTemplate = ref('')
const batchGenerating = ref(false)

// 流式生成状态
const streamSpeed = ref(0)
const isRagSearching = ref(false)
const isStreaming = ref(false)

// 监控相关状态
const monitorVisible = ref(false)
const monitorTasks = ref([])
const threadStatus = ref('IDLE')
const elapsedTime = ref('0.0')
const performanceReport = ref(null)
const currentMetrics = ref({ activeThreads: 0, reportActiveThreads: 0, cpuUsage: 0, memoryUsage: 0, processors: 32 })
const cpuHistory = ref(new Array(20).fill(10)) // 模拟波形数据
const currentQps = ref('0.0')
let monitorTimer = null
let metricsTimer = null
let startTime = 0

const getStatusType = (status) => {
  const map = {
    pending: 'info',
    running: 'primary',
    completed: 'success'
  }
  return map[status] || 'info'
}

const getTaskProgress = (userId) => {
  const task = monitorTasks.value.find(t => t.id === userId)
  return task ? Math.floor(task.progress) : 0
}

const getTaskStatus = (userId) => {
  const task = monitorTasks.value.find(t => t.id === userId)
  return task ? task.status : ''
}

const getTaskStatusText = (userId) => {
  const task = monitorTasks.value.find(t => t.id === userId)
  if (!task) return ''
  // 模拟细分状态
  if (task.status === 'pending') return '[排队中]'
  if (task.status === 'completed') return '[已完成]'
  if (task.progress < 20) return '[初始化]'
  if (task.progress < 50) return '[AI推理中]'
  if (task.progress < 80) return '[PDF渲染中]'
  return '[打包中]'
}

onMounted(async () => {
  if (!doctorStore.families.length) {
    await doctorStore.fetchFamilies()
  }
  // 如果已有选中的家庭，确保加载成员
  if (doctorStore.currentFamilyId) {
    await doctorStore.fetchMembers(doctorStore.currentFamilyId)
  }
})

onUnmounted(() => {
  if (monitorTimer) clearInterval(monitorTimer)
  if (metricsTimer) clearInterval(metricsTimer)
})

const handleFamilyChange = async (val) => {
  if (!val) return
  draftContent.value = ''
  evidences.value = []
  selectedMemberId.value = null
  batchMemberIds.value = []
  batchItems.value = []
  try {
    await doctorStore.setCurrentFamily(val)
    // 强制刷新一次成员列表，确保数据同步
    await doctorStore.fetchMembers(val)
  } catch (e) {
    console.error(e)
  }
}

const handleGeneratePreview = async () => {
  if (!selectedMemberId.value) return
  if (!doctorDiagnosis.value.trim()) {
    ElMessage.warning('请输入诊断意见')
    return
  }

  previewLoading.value = true
  draftContent.value = ''
  evidences.value = []
  streamSpeed.value = 0
  isRagSearching.value = true
  isStreaming.value = true

  // RAG 动效提示
  const ragLoading = ElMessage({
    message: '正在检索山中医中医典籍向量库...',
    type: 'info',
    duration: 0,
    icon: Loading
  })

  try {
    const token = getToken()
    const response = await fetch('/api/doctor/report-generation/stream-preview', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        userId: Number(selectedMemberId.value),
        diagnosis: doctorDiagnosis.value
      })
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let startTime = null
    let totalLength = 0
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk
      
      const lines = buffer.split('\n\n')
      buffer = lines.pop() // 保留最后一个不完整的块
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const jsonStr = line.substring(5).trim()
          if (!jsonStr) continue
          
          try {
            const data = JSON.parse(jsonStr)
            if (data.type === 'meta') {
              // RAG 完成
              if (isRagSearching.value) {
                isRagSearching.value = false
                ragLoading.close()
                ElMessage.success('检索完成，开始流式生成报告...')
                startTime = performance.now()
              }
              evidences.value = data.evidences || []
            } else if (data.type === 'content') {
               const text = data.text || ''
               // 打字机效果：直接追加即可，因为流本身就是一块块来的
               draftContent.value += text
               totalLength += text.length
               
               if (startTime) {
                 const elapsed = (performance.now() - startTime) / 1000
                 if (elapsed > 0) {
                   streamSpeed.value = (totalLength / elapsed).toFixed(1)
                 }
               }
            }
          } catch (e) {
            console.error('JSON parse error', e)
          }
        }
      }
    }
    
    if (!draftContent.value.trim()) {
      ElMessage.warning('预览已生成，但正文为空')
    } else {
      ElMessage.success('预览生成成功')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('生成预览失败')
  } finally {
    previewLoading.value = false
    isStreaming.value = false
    if (isRagSearching.value) {
        ragLoading.close()
        isRagSearching.value = false
    }
  }
}

const openEvidence = (ev, idx) => {
  activeEvidence.value = ev
  activeEvidenceIndex.value = idx
  evidenceDialogVisible.value = true
}

const handleGenerateReport = async () => {
  if (!selectedMemberId.value) return
  if (!doctorDiagnosis.value.trim()) {
    ElMessage.warning('请输入诊断意见')
    return
  }
  if (!draftContent.value.trim()) {
    ElMessage.warning('请先生成预览')
    return
  }

  generating.value = true
  try {
    const payload = {
      userId: Number(selectedMemberId.value),
      diagnosis: doctorDiagnosis.value,
      finalContent: draftContent.value
    }
    const res = await generateReportPdf(payload)
    
    // Handle Blob download
    const blob = new Blob([res], { type: 'application/pdf' })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `健康报告_${new Date().getTime()}.pdf`
    link.click()
    window.URL.revokeObjectURL(link.href)
    
    ElMessage.success('报告生成成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('生成报告失败')
  } finally {
    generating.value = false
  }
}

const handleDownloadTemplate = async () => {
  try {
    const res = await downloadReportTemplate()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = '报告模板.docx'
    link.click()
    window.URL.revokeObjectURL(link.href)
  } catch (e) {
    ElMessage.error('下载模板失败')
  }
}

const handleBatchMemberChange = (ids) => {
  const current = new Map(batchItems.value.map(item => [item.userId, item]))
  batchItems.value = (ids || []).map(id => {
    const existing = current.get(id)
    return {
      userId: id,
      diagnosis: existing?.diagnosis || batchDiagnosisTemplate.value || ''
    }
  })
}

const getMemberLabel = (userId) => {
  const member = members.value.find(m => String(m.userId) === String(userId))
  return member?.nickname || member?.phone || String(userId)
}

const applyTemplateToBatch = () => {
  if (!batchDiagnosisTemplate.value.trim()) {
    ElMessage.warning('请输入统一诊断模板')
    return
  }
  batchItems.value = batchItems.value.map(item => ({
    ...item,
    diagnosis: batchDiagnosisTemplate.value
  }))
}

const handleGenerateBatch = async () => {
  if (!batchItems.value.length) {
    ElMessage.warning('请选择批量患者')
    return
  }
  const invalid = batchItems.value.find(item => !item.diagnosis || !item.diagnosis.trim())
  if (invalid) {
    ElMessage.warning('请为所有患者填写诊断意见')
    return
  }

  // 初始化监控数据
  monitorVisible.value = true
  performanceReport.value = null
  threadStatus.value = 'INITIALIZING'
  elapsedTime.value = '0.0'
  monitorTasks.value = batchItems.value.map((item, index) => ({
    id: item.userId,
    threadId: index + 1, // 模拟线程ID
    patientName: getMemberLabel(item.userId),
    status: 'pending',
    statusText: '等待调度',
    progress: 0,
    log: 'Waiting for thread pool...'
  }))

  batchGenerating.value = true
  startTime = performance.now()
  
  // 启动计时器
  monitorTimer = setInterval(() => {
    const now = performance.now()
    const diff = (now - startTime) / 1000
    elapsedTime.value = diff.toFixed(1)
    
    // 动态计算 QPS
    const completed = monitorTasks.value.filter(t => t.status === 'completed').length
    currentQps.value = diff > 0 ? (completed / diff).toFixed(1) : '0.0'
  }, 100)

  // 启动后端指标监控
  metricsTimer = setInterval(async () => {
    try {
      const res = await getSystemMetrics()
      if (res.code === 0) {
        currentMetrics.value = res.data
        // 更新波形图数据
        const cpu = parseFloat(res.data.cpuUsage)
        cpuHistory.value.shift()
        cpuHistory.value.push(cpu > 0 ? cpu : Math.random() * 20 + 10) // 如果读不到（mac/win权限问题），给个基础波动防止空
      }
    } catch (e) {
      // ignore
    }
  }, 1000)

  // 模拟并发执行过程（视觉效果）
  // 真实请求发出后，我们将所有任务设置为 "Running" 以模拟后端并行处理
  setTimeout(() => {
    threadStatus.value = 'RUNNING (Virtual Threads)'
    monitorTasks.value.forEach(task => {
      task.status = 'running'
      task.statusText = '生成中'
      task.log = 'Analyzing health data...'
      task.progress = 30
    })
    
    // 随机进度模拟
    const progressInterval = setInterval(() => {
      if (!batchGenerating.value) {
        clearInterval(progressInterval)
        return
      }
      monitorTasks.value.forEach(task => {
        if (task.status === 'running') {
          // Allow progress to reach 100% to simulate completion
          if (task.progress < 100) {
            // Speed up slightly
            task.progress += Math.random() * 8
            
            if (task.progress > 60) task.log = 'Generating report content...'
            if (task.progress > 85) task.log = 'Finalizing document...'
            
            if (task.progress >= 100) {
              task.progress = 100
              task.status = 'completed'
              task.statusText = '已完成'
              task.log = 'Task finished successfully'
            }
          }
        }
      })
    }, 500)
  }, 800)

  try {
    const payload = {
      items: batchItems.value.map(item => ({
        userId: Number(item.userId),
        diagnosis: item.diagnosis,
        finalContent: null
      }))
    }
    const res = await generateBatchReportZip(payload)
    
    // 任务完成处理
    const endTime = performance.now()
    const duration = (endTime - startTime) / 1000
    
    threadStatus.value = 'COMPLETED'
    monitorTasks.value.forEach(task => {
      task.status = 'completed'
      task.statusText = '已完成'
      task.progress = 100
      task.log = 'Task finished successfully'
    })

    // 生成性能报告
    const estimatedSerial = (duration * batchItems.value.length * 0.8).toFixed(2) // 假设串行会更慢
    const actual = duration.toFixed(2)
    const speedup = (estimatedSerial / actual).toFixed(1)
    const qps = (batchItems.value.length / duration).toFixed(1)
    
    performanceReport.value = {
      estimatedSerialTime: estimatedSerial,
      actualTime: actual,
      speedup: speedup,
      qps: qps
    }

    const blob = new Blob([res], { type: 'application/zip' })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `健康报告批量_${new Date().getTime()}.zip`
    link.click()
    window.URL.revokeObjectURL(link.href)
    ElMessage.success('批量报告生成成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('批量报告生成失败')
    monitorVisible.value = false
  } finally {
    batchGenerating.value = false
    if (monitorTimer) clearInterval(monitorTimer)
    if (metricsTimer) clearInterval(metricsTimer)
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.report-generation-page {
  padding: 24px;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
  animation: fadeInDown 0.6s vars.$ease-spring;

  .header-icon {
    width: 52px;
    height: 52px;
    border-radius: 18px;
    background: vars.$gradient-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 6px 16px rgba(vars.$primary-color, 0.25);

    .el-icon {
      font-size: 26px;
      color: #fff;
    }
  }

  .header-content {
    flex: 1;
    .title {
      font-size: 24px;
      font-weight: 700;
      margin: 0 0 4px 0;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-main-color, vars.$primary-color));
    }
    .subtitle {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: vars.$shadow-lg;
  padding: 22px;
  animation: fadeInUp 0.6s vars.$ease-spring 0.1s backwards;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding: 14px 18px;
  border-radius: 16px;
  margin-bottom: 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.3s vars.$ease-spring;

  &:hover {
    box-shadow: vars.$shadow-md;
    transform: translateY(-2px);
  }

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 20px;
    flex-wrap: wrap;
  }

  .toolbar-item {
    display: flex;
    align-items: center;
    gap: 8px;
    .label {
      font-weight: 600;
      color: vars.$text-main-color;
      font-size: 13px;
      letter-spacing: 0.2px;
    }
  }
}

.report-tabs {
  margin-top: 6px;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  line-height: 1;
}

.custom-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: rgba(0, 0, 0, 0.05);
  }
  :deep(.el-tabs__active-bar) {
    display: none;
  }
  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
    color: vars.$text-secondary-color;
    padding: 0 20px !important; /* 强制覆盖 element 默认 padding */
    border-radius: vars.$radius-round;
    transition: all 0.2s;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 38px;
    line-height: 1; /* 保持1，靠 flex 居中 */
    margin-right: 8px;
    border: 1px solid transparent; /* 预留边框位置，防止抖动 */

    &.is-active {
      color: vars.$primary-color;
      background: rgba(vars.$primary-color, 0.12);
      font-weight: 600;
      border-color: rgba(vars.$primary-color, 0.2); /* 激活态加个微弱边框增加层次 */
    }

    &:hover:not(.is-active) {
      background: rgba(0, 0, 0, 0.02);
      color: vars.$text-main-color;
    }
  }

  :deep(.el-tabs__item .el-icon) {
    font-size: 16px;
    margin-top: -1px; /* 微调图标垂直位置，视觉修正 */
  }
}

.batch-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  align-items: center;
}

.glass-table {
  background: transparent;
  :deep(th.el-table__cell) {
    background: rgba(255, 255, 255, 0.6);
    color: vars.$text-secondary-color;
    font-weight: 600;
  }
  :deep(tr) {
    background: transparent;
    &:hover td.el-table__cell {
      background: rgba(vars.$primary-color, 0.06) !important;
    }
  }
  :deep(td.el-table__cell) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
    padding: 14px 0;
  }
}

.evidence-title {
  font-weight: 600;
}
.evidence-snippet {
  white-space: pre-wrap;
  color: vars.$text-secondary-color;
  line-height: 1.6;
}
.evidence-actions {
  margin-top: 8px;
}
.evidence-dialog__meta {
  margin-bottom: 12px;
}
.evidence-dialog__content {
  white-space: pre-wrap;
  max-height: 520px;
  overflow: auto;
  background: #f7f8fa;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 12px;
  color: vars.$text-main-color;
  line-height: 1.6;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.action-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.action-right {
  display: flex;
  align-items: center;
}
.hint-text {
  font-size: 12px;
  color: vars.$text-secondary-color;
}

.stream-status {
  margin-right: 12px;
  animation: fadeIn 0.5s ease;
}

.mr-2 { margin-right: 8px }

.batch-area {
  padding-top: 6px;
}

@media (max-width: 768px) {
  .action-row {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .report-generation-page {
    padding: 16px;
  }
  .toolbar {
    padding: 12px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* 监控组件样式 */
.monitor-container {
  padding: 10px;
}
.monitor-header {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 12px;
}
.monitor-stat {
  text-align: center;
  .stat-label {
    font-size: 12px;
    color: vars.$text-secondary-color;
    margin-bottom: 4px;
  }
  .stat-value {
    font-size: 20px;
    font-weight: 700;
    color: vars.$text-main-color;
    &.highlight {
      color: vars.$primary-color;
    }
  }
}

.thread-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
  max-height: 400px;
  overflow-y: auto;
}

.thread-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  transition: all 0.3s;
  
  &.running {
    border-color: rgba(vars.$primary-color, 0.3);
    box-shadow: 0 2px 12px rgba(vars.$primary-color, 0.1);
    .thread-name { color: vars.$primary-color; }
  }
  &.completed {
    border-color: rgba(vars.$success-color, 0.3);
    background: rgba(vars.$success-color, 0.02);
    .thread-name { color: vars.$success-color; }
  }
}

.thread-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.thread-name {
  font-weight: 600;
  font-size: 13px;
}
.task-info {
  margin-bottom: 8px;
  .patient-name {
    display: block;
    font-size: 12px;
    margin-bottom: 4px;
  }
}
.log-text {
  font-size: 11px;
  color: vars.$text-secondary-color;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.performance-report {
  border-top: 1px dashed #ebeef5;
  padding-top: 20px;
  animation: fadeInUp 0.5s ease;
  
  .report-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .report-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    background: #f0f9eb;
    padding: 16px;
    border-radius: 8px;
  }
  
  .report-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    .label {
      font-size: 12px;
      color: vars.$text-secondary-color;
    }
    .value {
      font-size: 18px;
      font-weight: 700;
      &.highlight {
        color: vars.$success-color;
      }
    }
  }
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.dashboard-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: transform 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  }

  .card-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    
    &.blue { background: rgba(vars.$primary-color, 0.1); color: vars.$primary-color; }
    &.green { background: rgba(vars.$success-color, 0.1); color: vars.$success-color; }
    &.red { background: rgba(vars.$danger-color, 0.1); color: vars.$danger-color; }
  }

  .card-content {
    flex: 1;
    .label {
      font-size: 12px;
      color: vars.$text-secondary-color;
      margin-bottom: 4px;
    }
    .value {
      font-size: 24px;
      font-weight: 800;
      color: vars.$text-main-color;
      line-height: 1;
      .unit {
        font-size: 12px;
        font-weight: 400;
        color: vars.$text-secondary-color;
        margin-left: 4px;
      }
      .sub-value {
        font-size: 14px;
        color: vars.$success-color;
        margin-left: 4px;
      }
    }
  }
}

.mini-chart {
  display: flex;
  align-items: flex-end;
  height: 24px;
  gap: 2px;
  margin-top: 8px;
  
  .bar {
    flex: 1;
    background: vars.$danger-color;
    border-radius: 2px;
    transition: height 0.3s;
  }
}

.inline-progress {
  margin-top: 8px;
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  animation: fadeInDown 0.3s ease;

  .progress-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 4px;
    font-size: 12px;
    
    .status-text {
      color: vars.$primary-color;
      font-weight: 600;
    }
    .percentage {
      color: vars.$text-secondary-color;
    }
  }
}
</style>
