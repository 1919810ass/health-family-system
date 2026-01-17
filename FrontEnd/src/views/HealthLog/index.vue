<template>
  <div class="page-container">
    <el-page-header content="健康日志" icon="" class="mb-24" />

    <el-card class="glass-card">
      <!-- 类型切换 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="custom-tabs">
        <el-tab-pane label="饮食" name="diet" />
        <el-tab-pane label="睡眠" name="sleep" />
        <el-tab-pane label="运动" name="sport" />
        <el-tab-pane label="情绪" name="mood" />
        <el-tab-pane label="体征" name="vital" />
      </el-tabs>

      <div class="log-body">
        <!-- 左侧日历 -->
        <div class="calendar-wrap">
          <el-calendar v-model="selectedDate" @input="handleDateChange" class="custom-calendar">
            <template #date-cell="{ date, data }">
              <div :class="{ 'has-log': calendarMarks[dateKey(date)] }">
                <span>{{ data.day.split('-')[2] }}</span>
                <el-icon v-if="calendarMarks[dateKey(date)]" size="12" class="mark-icon"><Check /></el-icon>
              </div>
            </template>
          </el-calendar>
          <el-button type="primary" round class="add-btn mt-16" @click="handleAdd" v-particles>+ 添加记录</el-button>
          <el-button 
            v-if="offlineDrafts.length > 0" 
            type="warning" 
            round
            :icon="Connection" 
            class="add-btn mt-8" 
            style="margin-left: 0" 
            @click="syncDrafts"
          >
            同步 {{ offlineDrafts.length }} 条草稿
          </el-button>
        </div>

        <!-- 右侧记录 & 趋势 -->
        <div class="right">
          <!-- 当日记录 -->
          <el-card shadow="never" class="day-logs inner-card">
            <template #header>
              <span class="card-title">{{ selectedDay }} 记录</span>
            </template>
            <div v-if="logs.length" class="log-list">
              <div v-for="(log, index) in logs" :key="log.id" class="log-item" :style="{ '--delay': index * 0.1 + 's' }">
                <div class="log-meta">
                  <span class="time">{{ log.time }}</span>
                  <el-tag size="small" effect="light" round>{{ log.category }}</el-tag>
                </div>
                <div class="log-content-wrapper" :class="{ 'is-abnormal': log.isAbnormal }">
                  <div class="content-text">
                    <p class="content">{{ log.content }}</p>
                    <div v-if="log.isAbnormal" class="abnormal-tag">
                      <el-tag type="danger" size="small" effect="dark">异常</el-tag>
                      <span class="reason" v-if="log.abnormalReason">{{ log.abnormalReason }}</span>
                    </div>
                  </div>
                  <el-button 
                    type="danger" 
                    link 
                    :icon="Delete" 
                    class="delete-btn"
                    @click="handleDelete(log.id)"
                  >删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="当日暂无记录" />
          </el-card>

          <!-- 趋势图表 -->
          <el-card shadow="never" class="trend-card inner-card mt-16">
            <template #header>
              <span class="card-title">{{ activeTabName }}趋势（近7天）</span>
            </template>
            <div ref="trendRef" style="width: 100%; height: 260px" />
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog v-model="addVisible" :title="`添加${activeTabName}记录`" width="600px" class="custom-dialog">
      <el-tabs v-model="inputMode" type="card">
        <el-tab-pane label="手动录入" name="manual">
          <el-form :model="addForm" label-width="100px" style="margin-top: 20px">
            <el-form-item label="时间">
              <el-time-picker v-model="addForm.time" format="HH:mm" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
            <el-alert
              v-if="activeTab !== 'vital'"
              :title="entryTips[activeTab]"
              type="info"
              :closable="false"
              style="margin-bottom:12px"
            />
            <el-form-item v-if="activeTab === 'vital'" label="体征数据">
              <div v-for="(item, index) in addForm.vitalItems" :key="index" class="vital-item-row">
                <el-select v-model="item.type" placeholder="类型" style="width: 120px">
                  <el-option label="血压" value="血压" />
                  <el-option label="血糖" value="血糖" />
                  <el-option label="体温" value="体温" />
                  <el-option label="心率" value="心率" />
                  <el-option label="体重" value="体重" />
                </el-select>
                
                <template v-if="item.type === '血压'">
                  <el-input v-model="item.value" placeholder="120/80" style="width: 140px" />
                  <span class="unit">mmHg</span>
                </template>
                <template v-else>
                  <el-input-number v-model="item.value" :min="0" :precision="1" :controls="false" placeholder="数值" style="width: 100px" />
                  <el-input v-model="item.unit" placeholder="单位" style="width: 80px" />
                </template>

                <el-button type="danger" link :icon="Delete" @click="removeVitalItem(index)" v-if="addForm.vitalItems.length > 1" />
              </div>
              <el-button type="primary" link :icon="Plus" @click="addVitalItem" class="mt-8">添加指标</el-button>
            </el-form-item>

            <el-form-item v-if="activeTab === 'vital'" label="文本内容（可选）">
              <el-input v-model="addForm.content" type="textarea" :rows="3" placeholder="例如：今天早上血压130/85，心率75 或 血糖6.2mmol/L" />
              <div style="margin-top: 8px; display: flex; gap: 8px">
                <el-button size="small" type="primary" :loading="optLoading" @click="optimizeAny">优化输入内容</el-button>
                <el-button size="small" @click="clearOptimizedAny" :disabled="!hasOptimized">清空优化</el-button>
              </div>
            </el-form-item>
            <el-form-item v-else label="内容">
              <el-input v-model="addForm.content" type="textarea" :rows="4" placeholder="请输入记录内容" />
              <div v-if="activeTab !== 'vital'" style="margin-top: 8px; display: flex; gap: 8px">
                <el-button size="small" type="primary" :loading="optLoading" @click="optimizeAny">优化输入内容</el-button>
                <el-button size="small" @click="clearOptimizedAny" :disabled="!hasOptimized">清空优化</el-button>
              </div>
              <el-table v-if="activeTab === 'diet' && dietItems.length" :data="dietItems" size="small" class="mt-12">
                <el-table-column prop="name" label="食物" />
                <el-table-column prop="quantity" label="数量" />
                <el-table-column prop="unit" label="单位" />
                <el-table-column prop="calories" label="热量" />
              </el-table>
              <div v-if="activeTab === 'diet' && dietTotal !== null" class="mt-12">总热量：{{ Math.round(dietTotal || 0) }} 千卡</div>
              <el-descriptions v-else-if="activeTab === 'sport' && optimizedData" :column="2" class="mt-12">
                <el-descriptions-item label="类型">{{ optimizedData.type }}</el-descriptions-item>
                <el-descriptions-item label="时长(分钟)">{{ optimizedData.durationMinutes }}</el-descriptions-item>
                <el-descriptions-item label="强度">{{ optimizedData.intensity }}</el-descriptions-item>
                <el-descriptions-item label="距离(公里)">{{ optimizedData.distanceKm }}</el-descriptions-item>
              </el-descriptions>
              <el-descriptions v-else-if="activeTab === 'sleep' && optimizedData" :column="2" class="mt-12">
                <el-descriptions-item label="入睡时间">{{ optimizedData.bedtime }}</el-descriptions-item>
                <el-descriptions-item label="起床时间">{{ optimizedData.wakeTime }}</el-descriptions-item>
                <el-descriptions-item label="时长(小时)">{{ optimizedData.durationHours }}</el-descriptions-item>
                <el-descriptions-item label="质量">{{ optimizedData.quality }}</el-descriptions-item>
              </el-descriptions>
              <el-descriptions v-else-if="activeTab === 'mood' && optimizedData" :column="2" class="mt-12">
                <el-descriptions-item label="情绪">{{ optimizedData.emotion }}</el-descriptions-item>
                <el-descriptions-item label="强度(1-5)">{{ optimizedData.level }}</el-descriptions-item>
                <el-descriptions-item label="备注">{{ optimizedData.note }}</el-descriptions-item>
              </el-descriptions>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="语音录入" name="voice">
          <div style="margin-top: 20px">
            <el-button :disabled="recording" @click="startVoiceInput" type="primary" size="large" style="width: 100%">
              <el-icon v-if="!recording"><Microphone /></el-icon>
              <span v-if="!recording">点击开始语音录入</span>
              <span v-else>正在录音...</span>
            </el-button>
            <el-input
              v-model="voiceText"
              type="textarea"
              :rows="3"
              placeholder="或直接输入语音转文字后的文本，如：记录今天血压 130/85"
              style="margin-top: 16px"
            />
            <el-button @click="parseVoice" :loading="parsingVoice" style="margin-top: 12px" type="success">解析语音</el-button>
            <div v-if="parsedVoiceData" style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px">
              <div>解析结果：{{ parsedVoiceData.type }} - {{ parsedVoiceData.value }}{{ parsedVoiceData.unit }}</div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="OCR识别" name="ocr">
          <div style="margin-top: 20px">
            <el-upload
              :auto-upload="false"
              :on-change="handleImageSelect"
              :show-file-list="false"
              accept="image/*"
              drag
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">将医疗报告图片拖到此处，或<em>点击上传</em></div>
            </el-upload>
            <el-button v-if="selectedImage" @click="parseOcr" :loading="parsingOcr" type="primary" style="margin-top: 16px; width: 100%">
              识别图片
            </el-button>
            <div v-if="ocrResult" style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px; max-height: 300px; overflow-y: auto">
              <div v-for="(item, idx) in ocrResult.items" :key="idx" style="margin-bottom: 8px">
                <el-tag :type="item.isAbnormal ? 'danger' : 'success'">{{ item.name }}</el-tag>
                <span style="margin-left: 8px">{{ item.value }} {{ item.unit }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="设备同步" name="device">
          <div style="margin-top: 20px">
            <el-alert type="info" :closable="false" style="margin-bottom: 16px">
              模拟设备数据同步（实际使用时需要对接真实设备）
            </el-alert>
            <el-form :model="deviceForm" label-width="100px">
              <el-form-item label="设备类型">
                <el-select v-model="deviceForm.deviceType" placeholder="选择设备" style="width: 100%">
                  <el-option label="苹果 Watch" value="APPLE_WATCH" />
                  <el-option label="华为手环" value="HUAWEI_BAND" />
                  <el-option label="血压计" value="BLOOD_PRESSURE_MONITOR" />
                  <el-option label="血糖仪" value="GLUCOSE_METER" />
                  <el-option label="体脂秤" value="BODY_SCALE" />
                </el-select>
              </el-form-item>
              <el-form-item label="设备ID">
                <el-input v-model="deviceForm.deviceId" placeholder="设备唯一标识" />
              </el-form-item>
              <el-form-item label="同步日期">
                <el-date-picker v-model="deviceForm.logDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
              <el-form-item label="数据">
                <el-input v-model="deviceForm.dataJson" type="textarea" :rows="4" placeholder='JSON格式，如：{"value": 130, "unit": "mmHg"}' />
        </el-form-item>
      </el-form>
            <el-button @click="syncDevice" :loading="syncingDevice" type="primary" style="width: 100%">同步数据</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button v-if="inputMode === 'manual'" type="primary" @click="handleSave" :loading="saving">保存</el-button>
        <el-button v-else-if="inputMode === 'voice' && parsedVoiceData" type="primary" @click="saveVoiceData" :loading="saving">保存</el-button>
        <el-button v-else-if="inputMode === 'ocr' && ocrResult" type="primary" @click="saveOcrData" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Microphone, UploadFilled, Delete, Plus, Connection } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useLogStore } from '../../stores'
import { getLogs, createLog, getStatistics, parseVoiceInput, parseOcrData, syncDeviceData, optimizeDietText, optimizeInput, deleteLog } from '../../api/log'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.tz.setDefault('Asia/Shanghai')

const route = useRoute()
const store = useLogStore()
const activeTab = ref(store.activeTab || 'diet')
const selectedDate = ref(new Date())
const selectedDay = computed(() => dayjs(selectedDate.value).format('YYYY-MM-DD'))
const targetUserId = computed(() => route.query.userId || route.query.patientUserId)
const logs = ref([])
const calendarMarks = ref({})
const trends = ref([])
const offlineDrafts = ref([]) // Added for offline storage

const addVisible = ref(false)
const saving = ref(false)
const inputMode = ref('manual')
const addForm = ref({ 
  time: new Date(), 
  content: '',
  vitalType: '',
  systolic: null,
  diastolic: null,
  value: null
})
const entryTips = {
  diet: '推荐格式：食物+数量+单位，如“鸡蛋1个、米饭1碗”。点击“优化输入内容”可自动估算热量',
  sleep: '填写睡眠时长(小时)、就寝时间与起床时间，质量可选，如“23:00-07:00 8小时 质量良好”',
  sport: '填写运动类型与时长(分钟)，可选距离(公里)，如“跑步 30 分钟 距离 5 公里”',
  mood: '填写情绪类型与强度(1-5)，如“焦虑 3”，可补充触发事件简述'
}

// 语音录入相关
const recording = ref(false)
const voiceText = ref('')
const parsingVoice = ref(false)
const parsedVoiceData = ref(null)

// OCR相关
const selectedImage = ref(null)
const parsingOcr = ref(false)
const ocrResult = ref(null)

// 设备同步相关
const syncingDevice = ref(false)
const deviceForm = ref({
  deviceType: '',
  deviceId: '',
  logDate: dayjs().format('YYYY-MM-DD'),
  dataJson: ''
})

// 饮食优化相关
const dietItems = ref([])
const dietTotal = ref(null)
const optLoading = ref(false)
const optimizedData = ref(null)
const hasOptimized = computed(() => {
  if (activeTab.value === 'diet') return dietItems.value.length > 0
  return !!optimizedData.value
})

const trendRef = ref()
let chartInstance = null

const activeTabName = computed(() => {
  const map = { diet: '饮食', sleep: '睡眠', sport: '运动', mood: '情绪', vital: '体征' }
  return map[activeTab.value]
})

function formatDate(date) {
  return dayjs(date).tz().format('YYYY-MM-DD')
}

function dateKey(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(async () => {
  // Load drafts
  const drafts = localStorage.getItem('health_log_drafts')
  if (drafts) {
    try {
      offlineDrafts.value = JSON.parse(drafts)
      if (offlineDrafts.value.length > 0) {
        ElMessage.info(`您有 ${offlineDrafts.value.length} 条离线草稿待同步`)
      }
    } catch (e) {
      console.error('Failed to parse drafts', e)
    }
  }

  // Handle Route Query
  if (route.query.mode === 'create') {
    if (route.query.type) {
      activeTab.value = route.query.type
      store.setActiveTab(route.query.type)
    }
    handleAdd()
  }

  await loadCalendarMarks()
  await loadLogs()
  await loadTrends()
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

// 监听 tab 变化，更新 store
watch(activeTab, (newTab) => {
  store.setActiveTab(newTab)
})

async function handleTabChange() {
  await loadLogs()
  await loadTrends()
}

async function handleDateChange() {
  await loadLogs()
}

async function loadCalendarMarks() {
  try {
    const type = mapType(activeTab.value)
    const startDate = dayjs(selectedDate.value).startOf('month').format('YYYY-MM-DD')
    const endDate = dayjs(selectedDate.value).endOf('month').format('YYYY-MM-DD')
    const res = await getLogs({ type, startDate, endDate, userId: targetUserId.value })
    if (res && res.data) {
    const marks = {}
      res.data.forEach(l => {
        if (l.logDate) {
          const dateKey = dayjs(l.logDate).format('YYYY-MM-DD')
          marks[dateKey] = true
        }
      })
    calendarMarks.value = marks
    store.setCalendarMarks(marks)
    }
  } catch (e) {
    console.error('加载日历标记失败:', e)
    // 静默失败，不影响其他功能
  }
}

function mapType(tab) {
  const map = { diet: 'DIET', sleep: 'SLEEP', sport: 'SPORT', mood: 'MOOD', vital: 'VITALS' }
  return map[tab]
}

async function loadLogs() {
  try {
    const type = mapType(activeTab.value)
    const day = selectedDay.value
    const res = await getLogs({ type, startDate: day, endDate: day, userId: targetUserId.value })
    if (res && res.data) {
      const list = res.data.map(l => {
        let content = ''
        let time = '--:--'
        
        // 后端返回的 content 已经是 Map，不需要再解析
        if (l.content && typeof l.content === 'object') {
          time = l.content.time || time
          content = l.content.note || l.content.text || l.content.content || ''
          // 如果没有找到内容，尝试序列化整个对象
          if (!content) {
            try {
              const { _isAbnormal, _metadata, _dataSource, ...rest } = l.content
              content = JSON.stringify(rest)
            } catch (e) {
              content = String(l.content)
            }
          }
        }
        
        return {
          id: l.id,
          time,
          category: activeTabName.value,
          content,
          isAbnormal: l.content?._isAbnormal === true,
          abnormalReason: l.content?._metadata?.anomaly?.reason || ''
        }
      })
      logs.value = list
      store.setLogs(list)
    } else {
      logs.value = []
    }
  } catch (e) {
    console.error('加载日志失败:', e)
    ElMessage.error('加载日志失败')
    logs.value = []
  }
}

async function loadTrends() {
  try {
    const res = await getStatistics(targetUserId.value)
    const type = mapType(activeTab.value)
    
    // 处理不同的数据结构
    let series = []
    if (res && res.data) {
      // 后端返回格式：{ last7Days: { label: "last_7_days", typeSeries: { "DIET": [{ date, averageScore, count }] } } }
      if (res.data.last7Days && res.data.last7Days.typeSeries) {
        series = res.data.last7Days.typeSeries[type] || []
      } else if (res.data.last_7_days && res.data.last_7_days.typeSeries) {
        // 兼容其他可能的格式
        series = res.data.last_7_days.typeSeries[type] || []
      }
    }
    
    const data = series.map(v => {
      const date = v.date || v.logDate || v.dateStr
      const value = v.average ?? v.averageScore ?? v.value ?? 0
      return { date: dayjs(date).format('MM-DD'), value: Number(value) }
    })
    
    trends.value = data
    store.setTrends(data)
    
    await nextTick()
    drawTrend()
  } catch (e) {
    console.error('加载趋势失败:', e)
    ElMessage.error('加载趋势失败')
    trends.value = []
    // 即使失败也要尝试绘制空图表
    await nextTick()
    drawTrend()
  }
}

function saveToOffline() {
  const draft = {
    form: JSON.parse(JSON.stringify(addForm.value)),
    activeTab: activeTab.value,
    timestamp: Date.now(),
    dateStr: selectedDay.value,
    dietItems: JSON.parse(JSON.stringify(dietItems.value)),
    dietTotal: dietTotal.value,
    id: Date.now()
  }
  offlineDrafts.value.push(draft)
  localStorage.setItem('health_log_drafts', JSON.stringify(offlineDrafts.value))
  ElMessage.success('已保存为离线草稿')
  addVisible.value = false
}

async function syncDrafts() {
  if (offlineDrafts.value.length === 0) return
  
  const loading = ElMessage.loading({
    message: '正在同步草稿...',
    duration: 0
  })
  
  let successCount = 0
  const remainingDrafts = []
  
  for (const draft of offlineDrafts.value) {
    try {
      const type = mapType(draft.activeTab)
      let logDate = draft.dateStr
      const timeStr = draft.form.time ? dayjs(draft.form.time).format('HH:mm') : dayjs().format('HH:mm')
      
      if (draft.activeTab === 'vital') {
         if (draft.form.vitalItems && draft.form.vitalItems.length) {
            for (const item of draft.form.vitalItems) {
                 await createLog({
                    logDate,
                    type: 'VITALS',
                    content: {
                        type: item.type,
                        value: item.value,
                        unit: item.unit,
                        time: timeStr
                },
                score: null
             }, targetUserId.value)
        }
     }
  } else {
     let content = {
        time: timeStr,
        note: draft.form.content
     }
     if (draft.activeTab === 'diet' && draft.dietItems && draft.dietItems.length) {
        content.items = draft.dietItems
        content.totalCalories = draft.dietTotal
     }
     
     await createLog({
        logDate,
        type,
        content,
        score: null
     }, targetUserId.value)
  }
  successCount++
    } catch (e) {
      console.error('Sync failed for draft', draft, e)
      remainingDrafts.push(draft)
    }
  }
  
  offlineDrafts.value = remainingDrafts
  localStorage.setItem('health_log_drafts', JSON.stringify(remainingDrafts))
  
  loading.close()
  if (successCount > 0) {
    ElMessage.success(`成功同步 ${successCount} 条草稿`)
    loadLogs()
    loadTrends()
  } else if (remainingDrafts.length > 0) {
    ElMessage.warning('部分草稿同步失败，请稍后重试')
  }
}

function handleAdd() {
  addVisible.value = true
  inputMode.value = 'manual'
  addForm.value = { 
    time: new Date(), 
    content: '',
    vitalItems: [{ type: '血压', value: null, unit: 'mmHg' }],
    vitalType: '',
    systolic: null,
    diastolic: null,
    value: null
  }
  voiceText.value = ''
  parsedVoiceData.value = null
  selectedImage.value = null
  ocrResult.value = null
  dietItems.value = []
  dietTotal.value = null
}

// 语音录入
function startVoiceInput() {
  // 这里可以集成Web Speech API
  ElMessage.info('语音录入功能需要浏览器支持，请直接输入文本')
}

async function optimizeDiet() {
  if (activeTab.value !== 'diet') return
  if (!addForm.value.content || !addForm.value.content.trim()) {
    ElMessage.warning('请输入饮食内容')
    return
  }
  optLoading.value = true
  try {
    const res = await optimizeDietText({ text: addForm.value.content.trim() }, targetUserId.value)
    const items = res?.data?.items || []
    const total = res?.data?.totalCalories ?? null
    dietItems.value = items
    dietTotal.value = typeof total === 'number' ? total : null
    // 将优化后的结构化内容替换原文本内容
    if (items.length) {
      const parts = items.map(i => {
        const n = i?.name || ''
        const q = i?.quantity != null ? String(i.quantity) : ''
        const u = i?.unit || ''
        return [n, q, u].filter(Boolean).join('')
      })
      const summary = parts.join('、')
      const totalText = typeof dietTotal.value === 'number' ? `；总热量：${Math.round(dietTotal.value)} 千卡` : ''
      addForm.value.content = summary + totalText
    }
    ElMessage.success('优化成功')
  } catch (e) {
    ElMessage.error('优化失败：' + (e.response?.data?.message || e.message))
  } finally {
    optLoading.value = false
  }
}

function clearOptimizedDiet() {
  dietItems.value = []
  dietTotal.value = null
}

async function optimizeAny() {
  const txt = addForm.value.content?.trim()
  if (!txt) {
    ElMessage.warning('请输入内容')
    return
  }
  if (activeTab.value === 'diet') {
    await optimizeDiet()
    return
  }
  optLoading.value = true
  try {
    const res = await optimizeInput({ text: txt, type: mapType(activeTab.value) })
    const data = res?.data || {}
    optimizedData.value = data
    let summary = ''
    if (activeTab.value === 'sport') {
      const t = data.type || ''
      const d = data.durationMinutes != null ? `${data.durationMinutes}分钟` : ''
      const dist = data.distanceKm != null ? `距离 ${data.distanceKm} 公里` : ''
      const inten = data.intensity || ''
      summary = [t, d, dist, inten].filter(Boolean).join(' ')
      // 实时优化建议：如果内容有变化，提示用户
      if (summary && summary !== txt) {
        ElMessage.success('已根据输入内容自动优化格式')
      }
    } else if (activeTab.value === 'sleep') {
      const bt = data.bedtime || ''
      const wt = data.wakeTime || ''
      const dur = data.durationHours != null ? `${data.durationHours}小时` : ''
      const q = data.quality || ''
      summary = [bt && wt ? `${bt}-${wt}` : bt || wt, dur, q].filter(Boolean).join(' ')
       if (summary && summary !== txt) {
        ElMessage.success('已优化睡眠记录格式')
      }
    } else if (activeTab.value === 'mood') {
      const e = data.emotion || ''
      const l = data.level != null ? `强度 ${data.level}` : ''
      const n = data.note || ''
      summary = [e, l, n].filter(Boolean).join(' ')
    } else if (activeTab.value === 'vital') {
      const items = data.items || (data.type ? [data] : [])
      
      if (items.length) {
        addForm.value.vitalItems = items.map(d => {
           let type = d.type || ''
           let val = d.value
           let unit = d.unit || ''
           
           if (!type && d.systolic != null) type = '血压'
           
           const typeMap = { 'BLOOD_PRESSURE': '血压', 'BP': '血压', 'GLUCOSE': '血糖', 'BLOOD_SUGAR': '血糖', 'HEART_RATE': '心率', 'HR': '心率', 'TEMPERATURE': '体温', 'TEMP': '体温', 'WEIGHT': '体重' }
           type = typeMap[type.toUpperCase()] || type
           
           if (type === '血压' && d.systolic != null && d.diastolic != null) {
             val = `${d.systolic}/${d.diastolic}`
           }
           
           return {
             type: type,
             value: val,
             unit: unit
           }
        })
        ElMessage.success(`已识别 ${items.length} 条数据`)
      } else {
        applyVitalOptimized(data)
        ElMessage.success('已自动识别并填充数据')
      }
      return
    }
    if (summary) addForm.value.content = summary
    if (activeTab.value !== 'vital') ElMessage.success('优化成功')
  } catch (e) {
    ElMessage.error('优化失败：' + (e.response?.data?.message || e.message))
  } finally {
    optLoading.value = false
  }
}

function clearOptimizedAny() {
  optimizedData.value = null
  clearOptimizedDiet()
}

function applyVitalOptimized() {
  if (!optimizedData.value) return
  const d = optimizedData.value
  if (d.systolic != null && d.diastolic != null) {
    addForm.value.vitalType = '血压'
    addForm.value.systolic = d.systolic
    addForm.value.diastolic = d.diastolic
  } else if (d.value != null) {
    addForm.value.value = d.value
    const u = d.unit || ''
    if (u) {
      const typeMap = { 'mmol/L': '血糖', '°C': '体温', 'bpm': '心率', 'kg': '体重' }
      addForm.value.vitalType = typeMap[u] || addForm.value.vitalType
    }
  }
}

async function parseVoice() {
  if (!voiceText.value.trim()) {
    ElMessage.warning('请输入语音文本')
    return
  }
  
  parsingVoice.value = true
  try {
    const res = await parseVoiceInput({ voiceText: voiceText.value })
    if (res && res.data) {
      parsedVoiceData.value = res.data
      ElMessage.success('解析成功')
    }
  } catch (e) {
    console.error('解析语音失败:', e)
    ElMessage.error('解析失败：' + (e.response?.data?.message || e.message))
  } finally {
    parsingVoice.value = false
  }
}

async function saveVoiceData() {
  if (!parsedVoiceData.value) {
    ElMessage.warning('请先解析语音数据')
    return
  }
  
  saving.value = true
  try {
    const content = {}
    if (parsedVoiceData.value.type === '血压' && parsedVoiceData.value.systolic) {
      content.systolic = parsedVoiceData.value.systolic
      content.diastolic = parsedVoiceData.value.diastolic
    } else {
      content.value = parsedVoiceData.value.value
      content.unit = parsedVoiceData.value.unit
    }
    content._dataSource = 'VOICE'
    
    await createLog({
      logDate: parsedVoiceData.value.date || selectedDay.value,
      type: 'VITALS',
      content,
      score: null
    })
    ElMessage.success('保存成功')
    addVisible.value = false
    await loadLogs()
    await loadCalendarMarks()
    await loadTrends()
  } catch (e) {
    console.error('保存失败:', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// OCR识别
function handleImageSelect(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    const base64 = e.target.result.split(',')[1] // 移除data:image/...;base64,前缀
    selectedImage.value = base64
  }
  reader.readAsDataURL(file.raw)
}

async function parseOcr() {
  if (!selectedImage.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  parsingOcr.value = true
  try {
    const res = await parseOcrData({ imageBase64: selectedImage.value }, targetUserId.value)
    if (res && res.data) {
      ocrResult.value = res.data
      ElMessage.success('识别成功')
    }
  } catch (e) {
    console.error('OCR识别失败:', e)
    ElMessage.error('识别失败：' + (e.response?.data?.message || e.message))
  } finally {
    parsingOcr.value = false
  }
}

async function saveOcrData() {
  if (!ocrResult.value || !ocrResult.value.items || ocrResult.value.items.length === 0) {
    ElMessage.warning('没有可保存的数据')
    return
  }
  
  saving.value = true
  try {
    // 保存每个检查项
    for (const item of ocrResult.value.items) {
      const content = {
        name: item.name,
        value: item.value,
        unit: item.unit,
        normalRange: item.normalRange,
        _dataSource: 'OCR'
      }
      
      await createLog({
        logDate: ocrResult.value.checkDate || selectedDay.value,
        type: 'VITALS',
        content,
        score: null
      }, targetUserId.value)
    }
    ElMessage.success('保存成功')
    addVisible.value = false
    await loadLogs()
    await loadCalendarMarks()
    await loadTrends()
  } catch (e) {
    console.error('保存失败:', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 设备同步
async function syncDevice() {
  if (!deviceForm.value.deviceType || !deviceForm.value.deviceId) {
    ElMessage.warning('请填写设备类型和设备ID')
    return
  }
  
  let data = {}
  try {
    if (deviceForm.value.dataJson) {
      data = JSON.parse(deviceForm.value.dataJson)
    }
  } catch (e) {
    ElMessage.error('数据格式错误，请输入有效的JSON')
    return
  }
  
  syncingDevice.value = true
  try {
    await syncDeviceData({
      deviceId: deviceForm.value.deviceId,
      deviceType: deviceForm.value.deviceType,
      logDate: deviceForm.value.logDate,
      data
    })
    ElMessage.success('同步成功')
    addVisible.value = false
    await loadLogs()
    await loadCalendarMarks()
    await loadTrends()
  } catch (e) {
    console.error('同步失败:', e)
    ElMessage.error('同步失败：' + (e.response?.data?.message || e.message))
  } finally {
    syncingDevice.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    
    await deleteLog(id, targetUserId.value)
    ElMessage.success('删除成功')
    loadLogs()
    loadTrends()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败：' + (e.response?.data?.message || e.message))
    }
  }
}

function addVitalItem() {
  addForm.value.vitalItems.push({ type: '', value: null, unit: '' })
}

function removeVitalItem(index) {
  addForm.value.vitalItems.splice(index, 1)
}

async function handleSave() {
  if (activeTab.value === 'vital') {
    if (!addForm.value.vitalItems.length) {
      ElMessage.warning('请至少添加一条体征记录')
      return
    }
    for (const item of addForm.value.vitalItems) {
      if (!item.type || !item.value) {
          ElMessage.warning('请补全数据类型和数值')
          return
      }
    }
    
    saving.value = true
    try {
      const timeStr = addForm.value.time ? dayjs(addForm.value.time).format('HH:mm') : dayjs().format('HH:mm')
      let successCount = 0
      for (const item of addForm.value.vitalItems) {
          const logData = {
            logDate: selectedDay.value,
            type: 'VITALS',
            content: {
              type: item.type,
              value: item.value,
              unit: item.unit,
              time: timeStr
            },
            score: null
          }
          
          if (item.type === '血压' && String(item.value).includes('/')) {
            const [sys, dia] = String(item.value).split('/')
            logData.content.systolic = Number(sys)
            logData.content.diastolic = Number(dia)
            logData.content.value = null
            logData.content.unit = 'mmHg'
          } else {
            logData.content.unit = logData.content.unit || getUnitForVitalType(item.type)
          }
          
          await createLog(logData, targetUserId.value)
          successCount++
      }
      ElMessage.success(`成功保存 ${successCount} 条记录`)
      addVisible.value = false
      addForm.value = { 
        time: new Date(), 
        content: '',
        vitalItems: [],
        vitalType: '',
        systolic: null,
        diastolic: null,
        value: null
      }
      loadLogs()
      loadTrends()
    } catch (e) {
      ElMessageBox.confirm('保存失败，是否保存为离线草稿？', '提示', {
        confirmButtonText: '保存草稿',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        saveToOffline()
      }).catch(() => {
        ElMessage.error('保存失败：' + (e.response?.data?.message || e.message))
      })
    } finally {
      saving.value = false
    }
    return
  }

  saving.value = true
  try {
    const timeStr = addForm.value.time ? dayjs(addForm.value.time).format('HH:mm') : dayjs().format('HH:mm')
    let content = {}
    
    if (!addForm.value.content || !addForm.value.content.trim()) {
      ElMessage.warning('请输入记录内容')
      saving.value = false
      return
    }
    content = {
      time: timeStr,
      note: addForm.value.content.trim()
    }
    if (activeTab.value === 'diet' && dietItems.value.length) {
      content.items = dietItems.value
      if (dietTotal.value !== null) content.totalCalories = dietTotal.value
    }
    
    await createLog({
      logDate: selectedDay.value,
      type: mapType(activeTab.value),
      content,
      score: null,
    }, targetUserId.value)
    ElMessage.success('记录成功')
    addVisible.value = false
    addForm.value = { 
      time: new Date(), 
      content: '',
      vitalItems: [],
      vitalType: '',
      systolic: null,
      diastolic: null,
      value: null
    }
    dietItems.value = []
    dietTotal.value = null
    
    // 依次加载，避免并发问题
    await loadLogs()
    await loadCalendarMarks()
    await loadTrends()
  } catch (e) {
    console.error('保存失败:', e)
    ElMessageBox.confirm('保存失败，是否保存为离线草稿？', '提示', {
      confirmButtonText: '保存草稿',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      saveToOffline()
    }).catch(() => {
      ElMessage.error('保存失败：' + (e.response?.data?.message || e.message || '未知错误'))
    })
  } finally {
    saving.value = false
  }
}

function getUnitForVitalType(type) {
  const map = {
    '血糖': 'mmol/L',
    '体温': '°C',
    '心率': 'bpm',
    '体重': 'kg'
  }
  return map[type] || ''
}

function drawTrend() {
  if (!trendRef.value) return
  
  // 如果已有实例，先销毁
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  if (!trends.value || !trends.value.length) {
    // 如果没有数据，显示空图表
    chartInstance = echarts.init(trendRef.value)
    chartInstance.setOption({
      grid: { left: 40, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value', min: 0, max: 1 },
      series: [{
        data: [],
        type: 'line',
        smooth: true
      }],
      graphic: [{
        type: 'text',
        left: 'center',
        top: 'middle',
        style: {
          text: '暂无数据',
          textAlign: 'center',
          fill: '#999',
          fontSize: 14
        }
      }]
    })
    return
  }
  
  try {
    chartInstance = echarts.init(trendRef.value)
  const dates = trends.value.map(t => t.date)
  const values = trends.value.map(t => t.value)
    
    chartInstance.setOption({
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
      xAxis: { 
        type: 'category', 
        data: dates,
        boundaryGap: false
      },
      yAxis: { 
        type: 'value',
        min: 0,
        max: 1
      },
    series: [{
      data: values,
      type: 'line',
      smooth: true,
        areaStyle: { opacity: 0.2 },
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2
        }
      }],
      tooltip: {
        trigger: 'axis',
        formatter: (params) => {
          const param = params[0]
          return `${param.name}<br/>${param.seriesName}: ${param.value}`
        }
      }
    })
  } catch (e) {
    console.error('绘制图表失败:', e)
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.page-container {
  min-height: 100%;
  /* padding is handled by global style, but we might need some specific adjustments if any */
}

.mb-24 { margin-bottom: 24px; }
.mt-16 { margin-top: 16px; }
.mt-8 { margin-top: 8px; }
.mt-12 { margin-top: 12px; }

.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.3s vars.$ease-spring;
  
  &:hover {
    box-shadow: vars.$shadow-md;
  }
}

.custom-tabs {
  :deep(.el-tabs__item) {
    font-size: 16px;
    transition: color 0.3s;
    
    &.is-active {
      color: vars.$primary-color;
      font-weight: 600;
    }
    
    &:hover {
      color: vars.$primary-color;
    }
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: rgba(0,0,0,0.05);
  }
}

.log-body {
  display: flex;
  margin-top: 20px;
  gap: 20px;
}

.calendar-wrap {
  width: 320px;
  flex-shrink: 0;
  
  .custom-calendar {
    border-radius: vars.$radius-md;
    border: none;
    box-shadow: vars.$shadow-sm;
    background: rgba(255, 255, 255, 0.8);
    
    :deep(.el-calendar-table td.is-selected) {
      background-color: rgba(vars.$primary-color, 0.1);
      border-radius: 8px;
    }
    
    :deep(.el-calendar-table .el-calendar-day) {
      height: 40px;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      transition: all 0.3s;
      margin: 2px;
      border: none;
      
      &:hover {
        background-color: rgba(vars.$primary-color, 0.05);
      }
    }
    
    :deep(.el-calendar__header) {
      padding: 12px;
      border-bottom: 1px solid rgba(0,0,0,0.05);
    }
  }
}

.mark-icon {
  color: vars.$success-color;
  margin-left: 2px;
}

.add-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  box-shadow: vars.$shadow-sm;
  transition: all 0.3s vars.$ease-spring;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: vars.$shadow-md;
  }
}

.right {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.inner-card {
  border: none;
  background: rgba(255, 255, 255, 0.5);
  border-radius: vars.$radius-md;
  backdrop-filter: blur(8px);
  
  .card-title {
    font-weight: 600;
    color: vars.$text-main-color;
    border-left: 4px solid vars.$primary-color;
    padding-left: 12px;
    font-size: 16px;
  }
  
  :deep(.el-card__header) {
    padding: 16px;
    border-bottom: 1px solid rgba(0,0,0,0.05);
  }
}

.log-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  animation: fadeInUp 0.5s vars.$ease-spring backwards;
  animation-delay: var(--delay);
  
  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.log-meta {
  width: 100px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  
  .time {
    font-size: 16px;
    font-weight: bold;
    color: vars.$text-main-color;
    margin-bottom: 4px;
  }
}

.log-content-wrapper {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: rgba(255, 255, 255, 0.6);
  padding: 12px;
  border-radius: 8px;
  transition: background 0.3s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.8);
  }
  
  .content {
    margin: 0;
    line-height: 1.6;
    color: vars.$text-main-color;
  }
}

.log-content-wrapper.is-abnormal {
  background: rgba(255, 235, 238, 0.8);
  border: 1px solid rgba(245, 108, 108, 0.3);
}

.content-text {
  flex: 1;
}

.abnormal-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  
  .reason {
    font-size: 12px;
    color: vars.$danger-color;
  }
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.3s;
}

.log-item:hover .delete-btn {
  opacity: 1;
}

.has-log {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1;
  
  span {
    margin-bottom: 2px;
    font-weight: 600;
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .log-body {
    flex-direction: column;
  }
  .calendar-wrap {
    width: 100%;
  }
}
</style>
