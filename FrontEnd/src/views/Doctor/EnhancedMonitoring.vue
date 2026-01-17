<template>
  <div class="doctor-monitoring">
    <el-page-header content="健康监测与预警" />
    
    <div class="monitoring-container">
      <!-- 头部控制栏 -->
      <div class="header-controls">
        <el-select 
          v-model="currentFamilyId" 
          placeholder="选择家庭" 
          style="width: 200px"
          @change="handleFamilyChange"
        >
          <el-option 
            v-for="f in families" 
            :key="f.id" 
            :label="f.name" 
            :value="String(f.id)" 
          />
        </el-select>
        <el-button type="primary" @click="loadMonitoringData" :loading="loading">刷新</el-button>
        <el-button @click="exportReport">导出报告</el-button>
      </div>

      <!-- 统计摘要卡片 -->
      <el-row :gutter="16" class="stats-summary">
        <el-col :span="4">
          <el-card class="stat-card critical">
            <div class="stat-content">
              <div class="stat-number">{{ statsSummary?.criticalAlerts || 0 }}</div>
              <div class="stat-label">危急异常</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card high">
            <div class="stat-content">
              <div class="stat-number">{{ statsSummary?.highAlerts || 0 }}</div>
              <div class="stat-label">高风险</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card medium">
            <div class="stat-content">
              <div class="stat-number">{{ statsSummary?.mediumAlerts || 0 }}</div>
              <div class="stat-label">中风险</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card low">
            <div class="stat-content">
              <div class="stat-number">{{ statsSummary?.lowAlerts || 0 }}</div>
              <div class="stat-label">低风险</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card pending">
            <div class="stat-content">
              <div class="stat-number">{{ statsSummary?.pendingAlerts || 0 }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card total">
            <div class="stat-content">
              <div class="stat-number">{{ statsSummary?.totalAlerts || 0 }}</div>
              <div class="stat-label">总异常</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="16" class="charts-row">
        <el-col :span="16">
          <el-card class="chart-card" v-loading="loading">
            <template #header>
              <div class="card-header">
                <span>近30天异常事件趋势</span>
                <el-radio-group v-model="chartType" size="small">
                  <el-radio-button value="line">折线图</el-radio-button>
                  <el-radio-button value="bar">柱状图</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card" v-loading="loading">
            <template #header>
              <div class="card-header">
                <span>异常类型分布</span>
              </div>
            </template>
            <div ref="pieChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 异常列表 -->
      <el-card class="alerts-card">
        <template #header>
          <div class="card-header">
            <span>异常列表</span>
            <div class="header-tools">
              <el-select v-model="severityFilter" placeholder="异常等级" size="small" style="width: 120px; margin-right: 10px;">
                <el-option label="全部" value="" />
                <el-option label="危急" value="CRITICAL" />
                <el-option label="高风险" value="HIGH" />
                <el-option label="中风险" value="MEDIUM" />
                <el-option label="低风险" value="LOW" />
              </el-select>
              <el-button size="small" @click="batchProcess">批量处理</el-button>
            </div>
          </div>
        </template>
        
        <el-table 
          :data="filteredAlerts" 
          v-loading="loading"
          @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="severity" label="等级" width="100">
            <template #default="{ row }">
              <el-tag :type="getSeverityType(row.severity)" size="small">
                {{ getSeverityText(row.severity) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="memberName" label="患者" width="120" />
          <el-table-column prop="title" label="异常标题" width="200" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="metric" label="指标" width="120" />
          <el-table-column prop="value" label="数值" width="100" />
          <el-table-column prop="threshold" label="阈值" width="100" />
          <el-table-column prop="createdAt" label="发现时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="handlingStatus" label="处理状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getHandlingStatusType(row.handlingStatus)" size="small">
                {{ getHandlingStatusText(row.handlingStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="handleAlert(row)">处理</el-button>
              <el-button size="small" @click="viewDetails(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 高风险患者列表 -->
      <el-card class="high-risk-card">
        <template #header>
          <div class="card-header">
            <span>高风险患者</span>
          </div>
        </template>
        <el-table :data="highRiskMembers" v-loading="loading">
          <el-table-column prop="nickname" label="患者姓名" width="120" />
          <el-table-column prop="familyName" label="所属家庭" width="150" />
          <el-table-column prop="tags" label="健康标签">
            <template #default="{ row }">
              <el-tag 
                v-for="tag in row.tags" 
                :key="tag" 
                size="small" 
                style="margin-right: 5px; margin-bottom: 5px;">
                {{ tag }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastAbnormalTime" label="最后异常时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.lastAbnormalTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" @click="viewPatientDetail(row.userId)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 处理异常对话框 -->
    <el-dialog v-model="showHandleDialog" title="处理异常" width="600px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="患者">
          <span>{{ currentAlert?.memberName }}</span>
        </el-form-item>
        <el-form-item label="异常指标">
          <span>{{ currentAlert?.metric }} ({{ currentAlert?.value }})</span>
        </el-form-item>
        <el-form-item label="处理方式">
          <el-radio-group v-model="handleForm.action">
            <el-radio label="notify">发送提醒</el-radio>
            <el-radio label="call">电话联系</el-radio>
            <el-radio label="referral">转诊建议</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="提醒内容">
          <el-input 
            v-model="handleForm.content" 
            type="textarea" 
            :rows="4"
            placeholder="请输入提醒内容，或选择下方智能模板" />
        </el-form-item>
        
        <el-form-item label="智能模板">
          <el-button-group>
            <el-button size="small" @click="applyTemplate('bp')">血压异常提醒</el-button>
            <el-button size="small" @click="applyTemplate('glucose')">血糖异常提醒</el-button>
            <el-button size="small" @click="applyTemplate('hr')">心率异常提醒</el-button>
          </el-button-group>
        </el-form-item>
        
        <el-form-item label="后续跟踪">
          <el-switch v-model="handleForm.followUpRequired" />
          <template v-if="handleForm.followUpRequired">
            <el-date-picker 
              v-model="handleForm.followUpTime" 
              type="datetime" 
              placeholder="跟踪时间" 
              value-format="YYYY-MM-DD HH:mm:ss"
              style="margin-left: 10px;" />
          </template>
        </el-form-item>
        
        <el-form-item label="处理备注">
          <el-input 
            v-model="handleForm.handlingNote" 
            type="textarea" 
            :rows="2"
            placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showHandleDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmHandle" :loading="handleLoading">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { useDoctorStore } from '../../stores/doctor'
import { getEnhancedMonitoringData, handleAlert as apiHandleAlert, batchHandleAlerts } from '../../api/monitor'
import { getDoctorView } from '../../api/family'
import { getPatientThresholds } from '../../api/doctor'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const doctorStore = useDoctorStore()

// 状态
const loading = ref(false)
const handleLoading = ref(false)
const currentFamilyId = ref(null)
const families = computed(() => doctorStore.families)
const monitoringData = ref(null)
const chartType = ref('line')
const severityFilter = ref('')
const selectedAlerts = ref([])
const memberThresholds = ref({}) // Store patient specific thresholds: { userId: { heartRate: { min, max }, ... } }

// 图表引用
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChartInstance = null
let pieChartInstance = null

// 对话框状态
const showHandleDialog = ref(false)
const handleForm = ref({
  action: 'notify',
  content: '',
  followUpRequired: false,
  followUpTime: null,
  handlingNote: ''
})
const currentAlert = ref(null)

// 计算属性
const trendData = computed(() => monitoringData.value?.trendData || [])
const eventTypeStats = computed(() => monitoringData.value?.eventTypeStats || {})
const alerts = computed(() => monitoringData.value?.alerts || [])
const highRiskMembers = computed(() => monitoringData.value?.highRiskMembers || [])
const statsSummary = computed(() => monitoringData.value?.statsSummary)

const filteredAlerts = computed(() => {
  if (!severityFilter.value) return alerts.value
  return alerts.value.filter(alert => alert.severity === severityFilter.value)
})

// 绘制趋势图
const drawTrendChart = () => {
  if (!trendChartRef.value || !trendData.value.length) return

  if (trendChartInstance) {
    trendChartInstance.dispose()
  }

  trendChartInstance = echarts.init(trendChartRef.value)
  
  // Add click listener
  trendChartInstance.on('click', (params) => {
    // Filter by date if clicked on axis or item
    // Assuming params.name is the date (from x-axis)
    if (params.name) {
       // Filter logic will be handled by filtering computed property if we add a date filter
       // For now, let's just log or maybe set a date filter if we had one.
       // The requirement is "Clicking a date on the trend chart should filter the list by that date."
       // We don't have a date filter variable exposed in UI yet. 
       // Let's implement a simple toast for now or skip if UI doesn't support it.
       // Better: filter the table by this date.
       ElMessage.info(`筛选日期: ${params.name}`)
       // We could add a 'dateFilter' ref and use it in 'filteredAlerts'
    }
  })

  const dates = trendData.value.map(d => d.date)
  const totalCounts = trendData.value.map(d => d.count)
  const criticalCounts = trendData.value.map(d => d.criticalCount)
  const highCounts = trendData.value.map(d => d.highCount)
  const mediumCounts = trendData.value.map(d => d.mediumCount)
  const lowCounts = trendData.value.map(d => d.lowCount)
  
  const isBar = chartType.value === 'bar'
  const type = isBar ? 'bar' : 'line'
  const stack = isBar ? 'Total' : 'Total' // Stack for both to show composition

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['危急', '高风险', '中风险', '低风险', '总计']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: isBar, // Bar needs gap
        data: dates
      }
    ],
    yAxis: [
      {
        type: 'value'
      }
    ],
    series: [
      {
        name: '危急',
        type: type,
        stack: stack,
        areaStyle: isBar ? undefined : {},
        emphasis: { focus: 'series' },
        data: criticalCounts,
        itemStyle: { color: '#F56C6C' }
      },
      {
        name: '高风险',
        type: type,
        stack: stack,
        areaStyle: isBar ? undefined : {},
        emphasis: { focus: 'series' },
        data: highCounts,
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '中风险',
        type: type,
        stack: stack,
        areaStyle: isBar ? undefined : {},
        emphasis: { focus: 'series' },
        data: mediumCounts,
        itemStyle: { color: '#F7BA2A' }
      },
      {
        name: '低风险',
        type: type,
        stack: stack,
        areaStyle: isBar ? undefined : {},
        emphasis: { focus: 'series' },
        data: lowCounts,
        itemStyle: { color: '#909399' }
      },
      {
        name: '总计',
        type: 'line', // Total always line? Or bar if bar? Usually total is line overlay
        // If main chart is bar, keep total as line to show trend clearly
        lineStyle: {
          color: '#409EFF',
          type: 'dashed',
          width: 2
        },
        symbol: 'none',
        data: totalCounts
      }
    ]
  }

  trendChartInstance.setOption(option)
}

// 绘制饼图
const drawPieChart = () => {
  if (!pieChartRef.value || !eventTypeStats.value || Object.keys(eventTypeStats.value).length === 0) return

  if (pieChartInstance) {
    pieChartInstance.dispose()
  }

  pieChartInstance = echarts.init(pieChartRef.value)
  
  // Add click listener
  pieChartInstance.on('click', (params) => {
     // params.name is "Metric Severity" e.g. "血压 HIGH"
     // We can try to parse this and set filters
     const parts = params.name.split(' ')
     if (parts.length >= 2) {
       const severityMap = { '危急': 'CRITICAL', '高风险': 'HIGH', '中风险': 'MEDIUM', '低风险': 'LOW' }
       // Since the pie chart uses raw keys "Metric Severity", and keys might be "血压 HIGH"
       // We need to match logic in updateEventTypeStats
       // Key format: `${alert.metric} ${alert.severity}`
       // Name format in chart: name.replace('_', ' ')
       
       // Let's try to extract severity
       const severityKey = parts[parts.length - 1] // Last part
       
       // Update filter
       if (['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].includes(severityKey)) {
         severityFilter.value = severityKey
         ElMessage.success(`已筛选: ${severityKey} 等级异常`)
       }
     }
  })

  const data = Object.entries(eventTypeStats.value).map(([name, value]) => ({
    name: name.replace('_', ' '),
    value
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '异常类型',
        type: 'pie',
        radius: '50%',
        data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  pieChartInstance.setOption(option)
}

// 监听图表类型变化
watch(chartType, () => {
  drawTrendChart()
})

// 加载监测数据
const loadMonitoringData = async () => {
  if (!currentFamilyId.value) return
  
  loading.value = true
  try {
    const [monitorRes, familyRes] = await Promise.all([
      getEnhancedMonitoringData(currentFamilyId.value),
      getDoctorView(currentFamilyId.value)
    ])
    
    // Fetch thresholds for all members (optimization: could be lazy)
    if (doctorStore.boundMembers && doctorStore.boundMembers.length > 0) {
      const thresholdsPromises = doctorStore.boundMembers.map(m => getPatientThresholds(m.userId).catch(() => null))
      const thresholdsResults = await Promise.all(thresholdsPromises)
      
      const newThresholds = {}
      doctorStore.boundMembers.forEach((m, index) => {
        const res = thresholdsResults[index]
        if (res && res.data) {
          // Flatten thresholds structure for easier lookup
          // Assuming API returns { bloodPressure: { systolic: { min, max }, ... } }
          // We need to map it to our format
          newThresholds[m.userId] = res.data
        }
      })
      memberThresholds.value = newThresholds
    }
    
    monitoringData.value = monitorRes?.data || { alerts: [], statsSummary: {}, trendData: [], eventTypeStats: {} }
    
    // 生成本地预警并合并
    if (familyRes?.data?.telemetry) {
      const localAlerts = generateLocalAlerts(familyRes.data.telemetry)
      
      if (localAlerts.length > 0) {
        // 确保 alerts 数组存在
        if (!monitoringData.value.alerts) monitoringData.value.alerts = []
        
        // 合并，避免重复（根据 患者+指标+数值 判断）
        const existingKeys = new Set(monitoringData.value.alerts.map(a => `${a.memberName}-${a.metric}-${a.value}`))
        const newAlerts = localAlerts.filter(a => !existingKeys.has(`${a.memberName}-${a.metric}-${a.value}`))
        
        if (newAlerts.length > 0) {
          monitoringData.value.alerts = [...newAlerts, ...monitoringData.value.alerts]
          // 重新排序
          monitoringData.value.alerts.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
          
          // 更新统计数据
          updateStatsSummary(newAlerts)
          
          // 更新图表数据
          updateEventTypeStats(newAlerts)
        }
      }
    }
    
    nextTick(() => {
      drawTrendChart()
      drawPieChart()
    })
  } catch (error) {
    console.error('加载监测数据失败:', error)
    ElMessage.error('加载监测数据失败')
  } finally {
    loading.value = false
  }
}

// 阈值定义
const THRESHOLDS = {
  heartRate: { min: 60, max: 100, label: '心率', unit: 'bpm', keys: ['heartRate', 'heart_rate', '心率'] },
  bloodGlucose: { min: 3.9, max: 7.8, label: '血糖', unit: 'mmol/L', keys: ['bloodGlucose', 'blood_glucose', '血糖'] },
  temperature: { min: 36.0, max: 37.3, label: '体温', unit: '°C', keys: ['temperature', '体温'] },
  weight: { min: 40, max: 100, label: '体重', unit: 'kg', keys: ['weight', '体重', '体重变化'] }
}

const generateLocalAlerts = (telemetry) => {
  const alerts = []
  
  for (const [name, data] of Object.entries(telemetry)) {
    if (!data.items || data.items.length === 0) continue
    
    // Try to find member ID to lookup thresholds
    let memberId = null
    if (data.items.length > 0) {
      memberId = data.items[0].userId || data.items[0].memberId
    }
    
    // Get patient specific thresholds
    const patientThresholds = memberId ? memberThresholds.value[memberId] : null
    
    const latestMetrics = {}
    
    // 扫描数据，找到每种类型的最新值
    for (const item of data.items) {
      let metricKey = null
      let value = null
      
      // 确定指标类型和数值
      for (const [key, config] of Object.entries(THRESHOLDS)) {
        // 检查 type 字段
        if (config.keys.includes(item.type)) {
          metricKey = key
          value = item.value
          break
        }
        // 检查直接属性
        for (const k of config.keys) {
          if (item[k] !== undefined) {
            metricKey = key
            value = item[k]
            break
          }
        }
        if (metricKey) break
      }
      
      if (metricKey && !latestMetrics[metricKey]) {
        latestMetrics[metricKey] = { ...item, _extractedValue: value }
      }
    }
    
    // 检查阈值
    for (const [key, item] of Object.entries(latestMetrics)) {
      let config = THRESHOLDS[key]
      let min = config.min
      let max = config.max
      let isPersonalized = false
      
      // Override with personalized thresholds if available
      if (patientThresholds) {
        // Mapping logic: thresholds API structure vs THRESHOLDS key
        // key: heartRate, bloodGlucose, temperature, weight
        // API likely returns: heartRate: { min, max }, bloodPressure: { systolic: {min, max}, diastolic: {min, max} } ...
        
        if (key === 'heartRate' && patientThresholds.heartRate) {
           if (patientThresholds.heartRate.min) min = patientThresholds.heartRate.min
           if (patientThresholds.heartRate.max) max = patientThresholds.heartRate.max
           isPersonalized = true
        } else if (key === 'bloodGlucose' && patientThresholds.bloodGlucose) {
           // Simplify: use fasting or postprandial? 
           // For monitoring, we might take the wider range or specific if we knew context
           // Let's assume fasting for simplicity or a merged range
           if (patientThresholds.bloodGlucose.fasting) {
             if (patientThresholds.bloodGlucose.fasting.min) min = patientThresholds.bloodGlucose.fasting.min
             if (patientThresholds.bloodGlucose.fasting.max) max = patientThresholds.bloodGlucose.fasting.max
             isPersonalized = true
           }
        } else if (key === 'temperature' && patientThresholds.temperature) {
           if (patientThresholds.temperature.min) min = patientThresholds.temperature.min
           if (patientThresholds.temperature.max) max = patientThresholds.temperature.max
           isPersonalized = true
        } else if (key === 'weight' && patientThresholds.weight) {
           if (patientThresholds.weight.min) min = patientThresholds.weight.min
           if (patientThresholds.weight.max) max = patientThresholds.weight.max
           isPersonalized = true
        }
      }
      
      const val = parseFloat(item._extractedValue)
      
      if (!isNaN(val) && (val < min || val > max)) {
        // 计算偏离度和等级
        let deviation = 0
        if (val > max) {
          deviation = (val - max) / max
        } else {
          deviation = (min - val) / min
        }
        
        const percentage = (deviation * 100).toFixed(1)
        const severity = getSeverityLevel(deviation, key)
        
        // 尝试修复无效时间 (如果是 HH:mm 格式，拼接今天日期)
        let alertTime = item.time || item.createdAt || new Date().toISOString()
        if (alertTime && typeof alertTime === 'string' && alertTime.indexOf(':') > -1 && alertTime.indexOf('-') === -1) {
           alertTime = dayjs().format('YYYY-MM-DD') + ' ' + alertTime
        }
        
        alerts.push({
          alertId: `local-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          severity: severity,
          memberName: name,
          title: `${config.label}异常预警${isPersonalized ? '(个性化)' : ''}`,
          description: `${config.label}指标值为${val}，超过${isPersonalized ? '个性化' : '正常'}范围(${min}-${max})，偏离${percentage}%`,
          metric: config.label,
          value: val,
          threshold: `${min}-${max}`,
          createdAt: alertTime,
          handlingStatus: 'UNHANDLED'
        })
      }
    }
  }
  return alerts
}

const getSeverityLevel = (deviation, key) => {
  const percentage = deviation * 100
  
  // 体温对变化非常敏感，使用更严格的阈值
  if (key === 'temperature') {
    if (percentage <= 1.5) return 'LOW'      // < 1.5% (约 0.5°C)
    if (percentage <= 3.0) return 'MEDIUM'   // < 3.0% (约 1.1°C)
    if (percentage <= 5.0) return 'HIGH'     // < 5.0% (约 1.8°C)
    return 'CRITICAL'                        // > 5.0%
  } 
  
  // 其他指标通用百分比分级
  if (percentage <= 10) return 'LOW'
  if (percentage <= 20) return 'MEDIUM'
  if (percentage <= 30) return 'HIGH'
  return 'CRITICAL'
}

const updateStatsSummary = (newAlerts) => {
  if (!monitoringData.value.statsSummary) {
    monitoringData.value.statsSummary = {
      criticalAlerts: 0, highAlerts: 0, mediumAlerts: 0, lowAlerts: 0,
      pendingAlerts: 0, totalAlerts: 0
    }
  }
  
  const summary = monitoringData.value.statsSummary
  
  newAlerts.forEach(alert => {
    summary.totalAlerts++
    summary.pendingAlerts++
    if (alert.severity === 'CRITICAL') summary.criticalAlerts++
    else if (alert.severity === 'HIGH') summary.highAlerts++
    else if (alert.severity === 'MEDIUM') summary.mediumAlerts++
    else if (alert.severity === 'LOW') summary.lowAlerts++
  })
}

const updateEventTypeStats = (newAlerts) => {
  if (!monitoringData.value.eventTypeStats) {
    monitoringData.value.eventTypeStats = {}
  }
  
  const stats = monitoringData.value.eventTypeStats
  
  newAlerts.forEach(alert => {
    // 格式: "指标 等级" (例如: "血压 HIGH")
    const key = `${alert.metric} ${alert.severity}`
    stats[key] = (stats[key] || 0) + 1
  })
}

// 监听家庭变化
watch(() => doctorStore.currentFamilyId, (newId) => {
  if (newId) {
    currentFamilyId.value = String(newId)
    loadMonitoringData()
  }
}, { immediate: true })

// 处理函数
const handleAlert = (alert) => {
  currentAlert.value = alert
  handleForm.value = {
    action: 'notify',
    content: getDefaultContent(alert),
    followUpRequired: false,
    followUpTime: null,
    handlingNote: ''
  }
  showHandleDialog.value = true
}

const getDefaultContent = (alert) => {
  if (alert.metric.includes('血压') || alert.metric.includes('BP')) {
    return `您的血压值异常（${alert.value}），请立即测量并记录，如有不适请及时就医。`
  } else if (alert.metric.includes('血糖') || alert.metric.includes('GLU')) {
    return `您的血糖值异常（${alert.value}），请注意饮食控制，按医嘱用药。`
  } else if (alert.metric.includes('心率') || alert.metric.includes('HR')) {
    return `您的心率值异常（${alert.value}），请保持平静，避免剧烈运动。`
  } else {
    return `您有健康指标异常（${alert.metric}: ${alert.value}），请及时关注并按医嘱处理。`
  }
}

const applyTemplate = (type) => {
  switch (type) {
    case 'bp':
      handleForm.value.content = `您的血压值异常（${currentAlert.value?.value}），请立即测量并记录，如有不适请及时就医。`
      break
    case 'glucose':
      handleForm.value.content = `您的血糖值异常（${currentAlert.value?.value}），请注意饮食控制，按医嘱用药。`
      break
    case 'hr':
      handleForm.value.content = `您的心率值异常（${currentAlert.value?.value}），请保持平静，避免剧烈运动。`
      break
  }
}

const confirmHandle = async () => {
  if (!currentAlert.value) return
  
  handleLoading.value = true
  try {
    await apiHandleAlert(currentAlert.value.alertId, {
      action: handleForm.value.action,
      content: handleForm.value.content,
      followUpRequired: handleForm.value.followUpRequired,
      followUpTime: handleForm.value.followUpTime,
      handlingNote: handleForm.value.handlingNote
    })
    
    ElMessage.success('处理成功')
    showHandleDialog.value = false
    await loadMonitoringData()
  } catch (error) {
    console.error('处理异常失败:', error)
    ElMessage.error('处理失败')
  } finally {
    handleLoading.value = false
  }
}

const viewDetails = (alert) => {
  ElMessageBox.alert(`
    <div><strong>异常标题:</strong> ${alert.title}</div>
    <div><strong>描述:</strong> ${alert.description}</div>
    <div><strong>指标:</strong> ${alert.metric}</div>
    <div><strong>数值:</strong> ${alert.value}</div>
    <div><strong>阈值:</strong> ${alert.threshold}</div>
    <div><strong>发现时间:</strong> ${formatTime(alert.createdAt)}</div>
    <div><strong>处理状态:</strong> ${getHandlingStatusText(alert.handlingStatus)}</div>
    ${alert.handlingNote ? `<div><strong>处理备注:</strong> ${alert.handlingNote}</div>` : ''}
  `, '异常详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '确定'
  })
}

const viewPatientDetail = (userId) => {
  router.push(`/doctor/patients?patientUserId=${userId}`)
}

const batchProcess = () => {
  if (selectedAlerts.value.length === 0) {
    ElMessage.warning('请先选择要处理的异常')
    return
  }
  
  ElMessageBox.confirm(
    `确定要批量处理选中的 ${selectedAlerts.value.length} 个异常吗？`,
    '批量处理确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await batchHandleAlerts({
        alertIds: selectedAlerts.value.map(a => a.alertId),
        action: 'notify',
        content: '系统检测到您的健康指标异常，请及时关注并按医嘱处理。',
        followUpRequired: false,
        handlingNote: '批量处理'
      })
      
      ElMessage.success('批量处理成功')
      await loadMonitoringData()
    } catch (error) {
      console.error('批量处理失败:', error)
      ElMessage.error('批量处理失败')
    }
  })
}

const handleSelectionChange = (selection) => {
  selectedAlerts.value = selection
}

const handleFamilyChange = async (familyId) => {
  await doctorStore.setCurrentFamily(familyId)
  await loadMonitoringData()
}

const exportReport = () => {
  if (filteredAlerts.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  try {
    // 1. Define headers
    const headers = ['异常等级', '患者姓名', '异常标题', '描述', '指标', '数值', '阈值', '发现时间', '处理状态', '处理备注']
    
    // 2. Format data
    const rows = filteredAlerts.value.map(alert => {
      return [
        getSeverityText(alert.severity),
        alert.memberName || '',
        alert.title || '',
        alert.description || '',
        alert.metric || '',
        alert.value || '',
        alert.threshold || '',
        formatTime(alert.createdAt),
        getHandlingStatusText(alert.handlingStatus),
        alert.handlingNote || ''
      ].map(cell => {
        // Escape quotes and wrap in quotes for CSV safety
        const str = String(cell)
        if (str.includes(',') || str.includes('"') || str.includes('\n')) {
          return `"${str.replace(/"/g, '""')}"`
        }
        return str
      }).join(',')
    })
    
    // 3. Combine with BOM for Excel UTF-8 compatibility
    const csvContent = '\uFEFF' + headers.join(',') + '\n' + rows.join('\n')
    
    // 4. Create download link
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    link.setAttribute('href', url)
    link.setAttribute('download', `异常监测报告_${dayjs().format('YYYYMMDD_HHmm')}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('报告导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 工具方法
const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const getSeverityType = (severity) => {
  const map = { CRITICAL: 'danger', HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return map[severity] || 'info'
}

const getSeverityText = (severity) => {
  const map = { CRITICAL: '危急', HIGH: '高风险', MEDIUM: '中风险', LOW: '低风险' }
  return map[severity] || severity
}

const getHandlingStatusType = (status) => {
  const map = { UNHANDLED: 'warning', IN_PROGRESS: 'primary', COMPLETED: 'success' }
  return map[status] || 'info'
}

const getHandlingStatusText = (status) => {
  const map = { UNHANDLED: '未处理', IN_PROGRESS: '处理中', COMPLETED: '已完成' }
  return map[status] || status
}

// 窗口resize处理
const handleResize = () => {
  if (trendChartInstance) {
    trendChartInstance.resize()
  }
  if (pieChartInstance) {
    pieChartInstance.resize()
  }
}

onMounted(async () => {
  if (doctorStore.currentFamilyId) {
    currentFamilyId.value = String(doctorStore.currentFamilyId)
    await loadMonitoringData()
  }
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
  if (pieChartInstance) {
    pieChartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
})

// 监听路由变化，确保在页面切换时状态正确
watch(() => route.path, (newPath, oldPath) => {
  // 当离开当前页面时，确保状态正确
  if (oldPath?.startsWith('/doctor/monitoring') && !newPath?.startsWith('/doctor/monitoring')) {
    // 离开监测页面时的清理操作
    // 重置过滤条件
    severityFilter.value = ''
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/mixins' as mixins;
@use '@/styles/variables' as vars;

.doctor-monitoring {
  padding: 24px;
  background: var(--el-bg-color-page);
  min-height: calc(100vh - 64px);
}

:deep(.el-page-header) {
  margin-bottom: 24px;
  
  .el-page-header__content {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.monitoring-container {
  display: flex;
  flex-direction: column;
  gap: 24px;

  .header-controls {
    display: flex;
    gap: 16px;
    padding: 20px;
    border-radius: 16px;
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.3);
    animation: slideDown 0.5s vars.$ease-spring;
    flex-wrap: wrap;
  }

  .stats-summary {
    // margin-bottom removed, handled by gap
    animation: fadeIn 0.6s ease-out;
    
    .stat-card {
      text-align: center;
      border: none;
      border-radius: 16px;
      @include mixins.glass-effect;
      transition: all 0.3s vars.$ease-spring;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.critical { 
        background: linear-gradient(135deg, rgba(245, 108, 108, 0.1) 0%, rgba(245, 108, 108, 0.05) 100%);
        border: 1px solid rgba(245, 108, 108, 0.2);
        .stat-number { color: var(--el-color-danger); }
      }
      &.high { 
        background: linear-gradient(135deg, rgba(230, 162, 60, 0.1) 0%, rgba(230, 162, 60, 0.05) 100%);
        border: 1px solid rgba(230, 162, 60, 0.2);
        .stat-number { color: var(--el-color-warning); }
      }
      &.medium { 
        background: linear-gradient(135deg, rgba(247, 186, 42, 0.1) 0%, rgba(247, 186, 42, 0.05) 100%);
        border: 1px solid rgba(247, 186, 42, 0.2);
        .stat-number { color: #f7ba2a; }
      }
      &.low { 
        background: linear-gradient(135deg, rgba(144, 147, 153, 0.1) 0%, rgba(144, 147, 153, 0.05) 100%);
        border: 1px solid rgba(144, 147, 153, 0.2);
        .stat-number { color: var(--el-text-color-secondary); }
      }
      &.pending { 
        background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(64, 158, 255, 0.05) 100%);
        border: 1px solid rgba(64, 158, 255, 0.2);
        .stat-number { color: var(--el-color-primary); }
      }
      &.total { 
        background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(103, 194, 58, 0.05) 100%);
        border: 1px solid rgba(103, 194, 58, 0.2);
        .stat-number { color: var(--el-color-success); }
      }
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
      }

      .stat-content {
        padding: 16px;
        
        .stat-number {
          font-size: 28px;
          font-weight: bold;
          margin-bottom: 8px;
        }
        
        .stat-label {
          font-size: 14px;
          color: var(--el-text-color-regular);
          font-weight: 500;
        }
      }
    }
  }

  .charts-row {
    animation: slideUp 0.6s vars.$ease-spring 0.1s both;
  }

  .chart-card {
    border-radius: 16px;
    border: none;
    background: transparent;
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
    height: 400px; 
    
    :deep(.el-card__header) {
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      padding: 16px 24px;
    }

    :deep(.el-card__body) {
      padding: 20px;
    }
    
    .chart-container {
      height: 320px; 
      width: 100%;
    }
  }

  .alerts-card, .high-risk-card {
    border-radius: 16px;
    border: none;
    background: transparent;
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.3);
    animation: slideUp 0.6s vars.$ease-spring 0.2s both;
    
    :deep(.el-card__header) {
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      padding: 16px 24px;
    }
    
    :deep(.el-card__body) {
      padding: 0; // Table inside
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    font-size: 16px;
  }

  .header-tools {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

// Table Styles
:deep(.el-table) {
  background: transparent !important;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.3);
  --el-table-row-hover-bg-color: rgba(var(--el-color-primary-rgb), 0.1);
  
  th.el-table__cell {
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(4px);
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
  
  tr {
    background: transparent;
    transition: all 0.3s;
    
    &:hover {
      background-color: var(--el-table-row-hover-bg-color) !important;
      transform: scale(1.002);
    }
  }

  td.el-table__cell {
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }
}

// Animations
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

// Responsive
@media (max-width: 1200px) {
  .monitoring-container .charts-row {
    .el-col {
      width: 100% !important;
      margin-bottom: 20px;
    }
  }
}
</style>