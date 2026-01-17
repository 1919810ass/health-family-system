<template>
  <div>
    <div class="ops-page">
      <el-tabs v-model="active">
        <el-tab-pane label="日志管理" name="logs">
          <div class="card">
            <el-form :inline="true" :model="logQuery">
              <el-form-item label="类型">
                <el-select v-model="logQuery.type" style="width: 160px">
                  <el-option label="OPERATION" value="OPERATION" />
                  <el-option label="SYSTEM" value="SYSTEM" />
                  <el-option label="AI" value="AI" />
                </el-select>
              </el-form-item>
              <el-form-item label="关键词">
                <el-input v-model="logQuery.keyword" placeholder="如：CUDA、llama runner" style="width: 220px" clearable />
              </el-form-item>
              <el-form-item label="开始">
                <el-date-picker v-model="logQuery.start" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
              </el-form-item>
              <el-form-item label="结束">
                <el-date-picker v-model="logQuery.end" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
              </el-form-item>
              <el-form-item label="数量">
                <el-input-number v-model="logQuery.limit" :min="1" :max="500" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loading.logs" @click="loadLogs">查询</el-button>
                <el-button :loading="loading.analysis" @click="doAnalyze">AI分析</el-button>
                <el-button @click="quickCuda" :disabled="loading.logs">CUDA快速检查</el-button>
              </el-form-item>
            </el-form>
            <el-table :data="logs" v-loading="loading.logs" style="width: 100%">
              <el-table-column prop="createdAt" label="时间" width="180" />
              <el-table-column prop="level" label="级别" width="100" />
              <el-table-column prop="module" label="模块" width="140" />
              <el-table-column prop="action" label="动作" width="180" />
              <el-table-column prop="detail" label="详情" />
            </el-table>
            <el-dialog v-model="analysisVisible" title="AI分析">
              <pre class="analysis">{{ analysis }}</pre>
            </el-dialog>
          </div>
        </el-tab-pane>
        <el-tab-pane label="系统报表" name="system">
          <div class="card">
            <el-form :inline="true" :model="sysQuery">
              <el-form-item label="开始">
                <el-date-picker v-model="sysQuery.start" type="date" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item label="结束">
                <el-date-picker v-model="sysQuery.end" type="date" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loading.system" @click="loadSystem">生成</el-button>
              </el-form-item>
            </el-form>
            <div class="cards" v-loading="loading.system">
              <el-card class="metric"><div class="k">活跃用户</div><div class="v">{{ systemReport.activeUsers }}</div></el-card>
              <el-card class="metric"><div class="k">总用户</div><div class="v">{{ systemReport.totalUsers }}</div></el-card>
              <el-card class="metric"><div class="k">日志量</div><div class="v">{{ systemReport.healthLogCount }}</div></el-card>
              <el-card class="metric"><div class="k">设备同步率</div><div class="v">{{ (systemReport.deviceSyncRate * 100).toFixed(1) }}%</div></el-card>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="家庭趋势" name="family">
          <div class="card">
            <el-form :inline="true" :model="famQuery">
              <el-form-item label="家庭ID">
                <el-input v-model="famQuery.familyId" style="width: 160px" />
              </el-form-item>
              <el-form-item label="开始">
                <el-date-picker v-model="famQuery.start" type="date" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item label="结束">
                <el-date-picker v-model="famQuery.end" type="date" value-format="YYYY-MM-DD" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loading.family" @click="loadFamily">生成</el-button>
              </el-form-item>
            </el-form>
            <el-table :data="familySeries" v-loading="loading.family" style="width: 100%">
              <el-table-column prop="month" label="月份" width="120" />
              <el-table-column prop="total" label="总量" width="120" />
              <el-table-column prop="abnormal" label="异常" width="120" />
              <el-table-column prop="controlRate" label="控制率" :formatter="pctFmt" />
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="系统设置" name="settings">
          <div class="card">
            <el-form :model="settings" label-width="140px" :rules="rules" ref="formRef">
              <el-form-item label="AI 温度" prop="ai.temperature">
                <el-input-number v-model="settings.ai.temperature" :min="0" :max="1" :step="0.1" />
              </el-form-item>
              <el-form-item label="RAG 阈值" prop="ai.ragThreshold">
                <el-input-number v-model="settings.ai.ragThreshold" :min="0" :max="1" :step="0.05" />
              </el-form-item>
              <el-form-item label="设备协议" prop="device.protocols">
                <el-select v-model="settings.device.protocols" multiple style="width: 240px">
                  <el-option label="BLE" value="BLE" />
                  <el-option label="MQTT" value="MQTT" />
                </el-select>
              </el-form-item>
              <el-form-item label="通知渠道" prop="notify.channels">
                <el-select v-model="settings.notify.channels" multiple style="width: 240px">
                  <el-option label="SMS" value="SMS" />
                  <el-option label="EMAIL" value="EMAIL" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loading.settings" @click="saveSettings">保存</el-button>
                <el-button @click="loadSettings">刷新</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <el-backtop :right="20" :bottom="20" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchLogs, analyzeLogs, systemReport as fetchSystemReport, familyTrendReport, getSettings, updateSettings } from '../../api/ops'
import dayjs from 'dayjs'

const active = ref('logs')
const loading = reactive({ logs: false, analysis: false, system: false, family: false, settings: false })

const logQuery = reactive({ type: 'SYSTEM', keyword: '', start: dayjs().subtract(1, 'day').format('YYYY-MM-DDTHH:mm:ss'), end: dayjs().format('YYYY-MM-DDTHH:mm:ss'), limit: 50 })
const logs = ref([])
const analysisVisible = ref(false)
const analysis = ref('')

const sysQuery = reactive({ start: dayjs().subtract(7, 'day').format('YYYY-MM-DD'), end: dayjs().format('YYYY-MM-DD') })
const systemReport = reactive({ activeUsers: 0, totalUsers: 0, healthLogCount: 0, deviceSyncRate: 0 })

const famQuery = reactive({ familyId: '', start: dayjs().subtract(2, 'month').startOf('month').format('YYYY-MM-DD'), end: dayjs().endOf('month').format('YYYY-MM-DD') })
const familySeries = ref([])

const formRef = ref()
const settings = reactive({ ai: { temperature: 0.7, ragThreshold: 0.35 }, device: { protocols: [] }, notify: { channels: [] } })
const rules = { 'ai.temperature': [{ required: true, message: '必填', trigger: 'blur' }], 'ai.ragThreshold': [{ required: true, message: '必填', trigger: 'blur' }], 'device.protocols': [{ type: 'array', required: true, message: '至少选择一项', trigger: 'change' }], 'notify.channels': [{ type: 'array', required: true, message: '至少选择一项', trigger: 'change' }] }

const loadLogs = async () => {
  loading.logs = true
  try {
    const res = await fetchLogs({ ...logQuery })
    logs.value = res.data || res
  } catch (e) {
    ElMessage.error(String(e.message || e))
  } finally {
    loading.logs = false
  }
}

const quickCuda = async () => {
  logQuery.type = 'AI'
  logQuery.keyword = 'CUDA'
  await loadLogs()
  const count = (logs.value || []).filter(l => String(l.detail || '').toUpperCase().includes('CUDA')).length
  if (count > 0) {
    ElMessage.error(`检测到 ${count} 条 CUDA 相关错误，请及时检查GPU与驱动`)
  } else {
    ElMessage.success('未检测到近期CUDA错误')
  }
}
const doAnalyze = async () => {
  loading.analysis = true
  try {
    const res = await analyzeLogs({ ...logQuery })
    analysis.value = res.data || res
    analysisVisible.value = true
  } catch (e) {
    ElMessage.error(String(e.message || e))
  } finally {
    loading.analysis = false
  }
}

const loadSystem = async () => {
  loading.system = true
  try {
    const res = await fetchSystemReport({ ...sysQuery })
    const data = res.data || res
    systemReport.activeUsers = data.activeUsers || 0
    systemReport.totalUsers = data.totalUsers || 0
    systemReport.healthLogCount = data.healthLogCount || 0
    systemReport.deviceSyncRate = data.deviceSyncRate || 0
  } catch (e) {
    ElMessage.error(String(e.message || e))
  } finally {
    loading.system = false
  }
}

const pctFmt = (_row, _col, val) => `${(Number(val || 0) * 100).toFixed(1)}%`

const loadFamily = async () => {
  loading.family = true
  try {
    const res = await familyTrendReport({ ...famQuery })
    const data = res.data || res
    familySeries.value = data.series || []
  } catch (e) {
    ElMessage.error(String(e.message || e))
  } finally {
    loading.family = false
  }
}

const loadSettings = async () => {
  loading.settings = true
  try {
    const res = await getSettings()
    const data = res.data || res
    settings.ai = Object.assign({ temperature: 0.7, ragThreshold: 0.35 }, data.ai || {})
    settings.device = Object.assign({ protocols: [] }, data.device || {})
    settings.notify = Object.assign({ channels: [] }, data.notify || {})
  } catch (e) {
    ElMessage.error(String(e.message || e))
  } finally {
    loading.settings = false
  }
}

const saveSettings = async () => {
  await formRef.value.validate()
  loading.settings = true
  try {
    await updateSettings(settings)
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(String(e.message || e))
  } finally {
    loading.settings = false
  }
}

onMounted(() => {
  loadLogs()
  loadSystem()
  loadSettings()
})
</script>

<style scoped>
.ops-page { padding: 16px }
.card { background: var(--el-bg-color); padding: 12px; border-radius: 8px }
.cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; margin-top: 12px }
.metric { text-align: center }
.metric .k { font-size: 13px; color: var(--el-text-color-secondary) }
.metric .v { font-size: 20px; font-weight: 600; margin-top: 6px }
.analysis { white-space: pre-wrap; line-height: 1.6 }
@media (max-width: 768px) { .ops-page { padding: 8px } }
</style>
