<template>
  <div class="doctor-analysis">
    <el-page-header content="数据统计与分析" />
    
    <!-- 过滤工具栏 -->
    <div class="toolbar">
      <el-select :model-value="familyId" placeholder="选择家庭" style="width: 200px" @change="onSwitch">
        <el-option v-for="f in families" :key="f.id" :label="f.name" :value="String(f.id)" />
      </el-select>
      
      <el-select v-model="selectedMemberId" placeholder="全国家庭成员" style="width: 150px" clearable @change="loadStats">
        <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.realName" :value="m.userId" />
      </el-select>
      
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 240px"
        value-format="YYYY-MM-DD"
        @change="loadStats"
      />
      
      <el-button type="primary" :loading="loading" @click="loadStats">查询</el-button>
    </div>

    <div v-loading="loading" class="stats-content">
      <!-- 患者结构统计 -->
      <el-card class="stats-section" v-if="!selectedMemberId">
        <template #header>
          <span>患者结构</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="chart-container">
              <div class="chart-title">年龄段分布</div>
              <div ref="ageChartRef" class="chart"></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="chart-container">
              <div class="chart-title">性别分布</div>
              <div ref="genderChartRef" class="chart"></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="chart-container">
              <div class="chart-title">疾病类别分布</div>
              <div ref="diseaseChartRef" class="chart"></div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 管理效果统计 -->
      <el-card class="stats-section">
        <template #header>
          <span>管理效果</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="chart-container">
              <div class="chart-title">血压达标率趋势</div>
              <div ref="bloodPressureChartRef" class="chart"></div>
              <div v-if="stats?.managementEffect?.bloodPressure" class="chart-summary">
                <el-statistic title="达标率" :value="stats.managementEffect.bloodPressure.complianceRate" suffix="%" :precision="2" />
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-container">
              <div class="chart-title">体重变化趋势</div>
              <div ref="weightChartRef" class="chart"></div>
              <div v-if="stats?.managementEffect?.weight" class="chart-summary">
                <el-statistic title="平均变化" :value="stats.managementEffect.weight.averageWeightChange" suffix="kg" :precision="2" />
              </div>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" class="mt-16">
          <el-col :span="12">
            <div class="chart-container">
              <div class="chart-title">睡眠时长趋势</div>
              <div ref="sleepChartRef" class="chart"></div>
              <div v-if="stats?.managementEffect?.sleep" class="chart-summary">
                <el-statistic title="平均睡眠时长" :value="stats.managementEffect.sleep.averageSleepHours" suffix="小时" :precision="2" />
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 工作负载统计 -->
      <el-card class="stats-section" v-if="!selectedMemberId">
        <template #header>
          <span>工作负载</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="chart-container">
              <div class="chart-title">咨询量趋势</div>
              <div ref="consultationChartRef" class="chart"></div>
              <div v-if="stats?.workload?.consultation" class="chart-summary">
                <el-statistic title="总咨询次数" :value="stats.workload.consultation.totalCount" />
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="chart-container">
              <div class="chart-title">随访计划趋势</div>
              <div ref="followupChartRef" class="chart"></div>
              <div v-if="stats?.workload?.followup" class="chart-summary">
                <el-statistic title="总计划数" :value="stats.workload.followup.totalPlans" />
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="chart-container">
              <div class="chart-title">提醒发送趋势</div>
              <div ref="reminderChartRef" class="chart"></div>
              <div v-if="stats?.workload?.reminder" class="chart-summary">
                <el-statistic title="完成率" :value="stats.workload.reminder.completionRate" suffix="%" :precision="2" />
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useDoctorStore } from '../../stores/doctor'
import { getDoctorStats } from '../../api/doctor'
import { getDoctorView } from '../../api/family'
import dayjs from 'dayjs'

const route = useRoute()
const doctorStore = useDoctorStore()

// 使用 store 中的状态
const families = computed(() => doctorStore.families)
const familyId = computed(() => doctorStore.currentFamilyId)
const members = computed(() => doctorStore.boundMembers)

// 本地状态
const loading = ref(false)
const stats = ref(null)
const selectedMemberId = ref('')
const dateRange = ref([dayjs().subtract(29, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])

// 图表引用
const ageChartRef = ref(null)
const genderChartRef = ref(null)
const diseaseChartRef = ref(null)
const bloodPressureChartRef = ref(null)
const weightChartRef = ref(null)
const sleepChartRef = ref(null)
const consultationChartRef = ref(null)
const followupChartRef = ref(null)
const reminderChartRef = ref(null)

let chartInstances = {}

// 监听当前家庭变化
watch(() => doctorStore.currentFamilyId, async (newFamilyId) => {
  if (newFamilyId) {
    await loadStats()
  }
}, { immediate: false })

const onSwitch = async (id) => {
  await doctorStore.setCurrentFamily(id)
}

const loadStats = async () => {
  if (!familyId.value) {
    ElMessage.error('请选择家庭')
    return
  }
  
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.error('请选择日期范围')
    return
  }
  
  loading.value = true
  try {
    if (selectedMemberId.value) {
      // Load individual stats from telemetry
      const res = await getDoctorView(familyId.value)
      const telemetry = res?.data?.telemetry
      
      // Construct stats object for single patient
      // We only populate managementEffect
      const newStats = {
        patientStructure: {}, // Hide
        workload: {}, // Hide
        managementEffect: {
          bloodPressure: { trend: [], complianceRate: 0 },
          weight: { trend: [], averageWeightChange: 0 },
          sleep: { trend: [], averageSleepHours: 0 }
        }
      }
      
      // Find patient data
      let patientData = null
      for (const [key, data] of Object.entries(telemetry)) {
        if (data.items && data.items.length > 0) {
          const firstItem = data.items[0]
          if (firstItem.userId === selectedMemberId.value || firstItem.memberId === selectedMemberId.value) {
            patientData = data
            break
          }
        }
      }
      
      if (patientData && patientData.items) {
        // Filter items by date range
        const startDate = dayjs(dateRange.value[0])
        const endDate = dayjs(dateRange.value[1]).endOf('day')
        
        const items = patientData.items.filter(item => {
          const itemDate = dayjs(item.createdAt)
          return itemDate.isAfter(startDate) && itemDate.isBefore(endDate)
        }).sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
        
        // Process Blood Pressure
        const bpItems = items.filter(i => i.type === '血压' || (i.systolic && i.diastolic))
        newStats.managementEffect.bloodPressure.trend = bpItems.map(i => ({
          date: i.createdAt,
          value: i.systolic ? parseFloat(i.systolic) : 0 // Show systolic for trend
        }))
        // Calculate mock compliance (e.g., systolic < 140)
        const compliantCount = bpItems.filter(i => i.systolic && i.systolic < 140).length
        newStats.managementEffect.bloodPressure.complianceRate = bpItems.length > 0 ? ((compliantCount / bpItems.length) * 100).toFixed(1) : 0
        
        // Process Weight
        const weightItems = items.filter(i => i.type === '体重' || i.type === '体重变化' || i.weight)
        newStats.managementEffect.weight.trend = weightItems.map(i => ({
          date: i.createdAt,
          value: parseFloat(i.value || i.weight || 0)
        }))
        if (weightItems.length > 1) {
           const first = parseFloat(weightItems[0].value || weightItems[0].weight)
           const last = parseFloat(weightItems[weightItems.length - 1].value || weightItems[weightItems.length - 1].weight)
           newStats.managementEffect.weight.averageWeightChange = (last - first).toFixed(1)
        }
        
        // Process Sleep (Mock or real if available)
        // Assuming no real sleep data in typical logs, leave empty or mock
      }
      
      stats.value = newStats
    } else {
      // Load aggregate stats
      const res = await getDoctorStats(familyId.value, dateRange.value[0], dateRange.value[1])
      stats.value = res?.data
    }
    
    // 等待 DOM 更新后绘制图表
    await nextTick()
    drawAllCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const drawAllCharts = () => {
  if (!stats.value) return
  
  if (!selectedMemberId.value) {
    drawAgeChart()
    drawGenderChart()
    drawDiseaseChart()
    drawConsultationChart()
    drawFollowupChart()
    drawReminderChart()
  }
  
  drawBloodPressureChart()
  drawWeightChart()
  drawSleepChart()
}

// 年龄段分布（饼图）
const drawAgeChart = () => {
  if (!ageChartRef.value) return
  
  const data = stats.value?.patientStructure?.ageDistribution || {}
  const chartData = Object.keys(data).map(key => ({ name: key, value: data[key] }))
  
  if (!chartInstances.ageChart) {
    chartInstances.ageChart = echarts.init(ageChartRef.value)
  }
  
  chartInstances.ageChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: '60%',
      data: chartData,
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

// 性别分布（饼图）
const drawGenderChart = () => {
  if (!genderChartRef.value) return
  
  const data = stats.value?.patientStructure?.genderDistribution || {}
  const genderMap = { M: '男', F: '女', OTHER: '其他' }
  const chartData = Object.keys(data).map(key => ({ name: genderMap[key] || key, value: data[key] }))
  
  if (!chartInstances.genderChart) {
    chartInstances.genderChart = echarts.init(genderChartRef.value)
  }
  
  chartInstances.genderChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: '60%',
      data: chartData,
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

// 疾病类别分布（柱状图）
const drawDiseaseChart = () => {
  if (!diseaseChartRef.value) return
  
  const data = stats.value?.patientStructure?.diseaseDistribution || {}
  const categories = Object.keys(data)
  const values = Object.values(data)
  
  if (!chartInstances.diseaseChart) {
    chartInstances.diseaseChart = echarts.init(diseaseChartRef.value)
  }
  
  chartInstances.diseaseChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: { color: '#409EFF' }
    }],
    grid: { left: 60, right: 20, bottom: 60, top: 20 }
  })
}

// 血压达标率趋势（折线图）
const drawBloodPressureChart = () => {
  if (!bloodPressureChartRef.value) return
  
  const trend = stats.value?.managementEffect?.bloodPressure?.trend || []
  const dates = trend.map(item => dayjs(item.date).format('MM-DD'))
  const values = trend.map(item => item.value || 0)
  
  if (!chartInstances.bloodPressureChart) {
    chartInstances.bloodPressureChart = echarts.init(bloodPressureChartRef.value)
  }
  
  chartInstances.bloodPressureChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '达标率(%)', max: 100 },
    series: [{
      type: 'line',
      smooth: true,
      data: values,
      itemStyle: { color: '#67C23A' },
      areaStyle: { color: 'rgba(103, 194, 58, 0.1)' }
    }],
    grid: { left: 60, right: 20, bottom: 40, top: 20 }
  })
}

// 体重变化趋势（折线图）
const drawWeightChart = () => {
  if (!weightChartRef.value) return
  
  const trend = stats.value?.managementEffect?.weight?.trend || []
  const dates = trend.map(item => dayjs(item.date).format('MM-DD'))
  const values = trend.map(item => item.value || 0)
  
  if (!chartInstances.weightChart) {
    chartInstances.weightChart = echarts.init(weightChartRef.value)
  }
  
  chartInstances.weightChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '体重(kg)' },
    series: [{
      type: 'line',
      smooth: true,
      data: values,
      itemStyle: { color: '#E6A23C' }
    }],
    grid: { left: 60, right: 20, bottom: 40, top: 20 }
  })
}

// 睡眠时长趋势（折线图）
const drawSleepChart = () => {
  if (!sleepChartRef.value) return
  
  const trend = stats.value?.managementEffect?.sleep?.trend || []
  const dates = trend.map(item => dayjs(item.date).format('MM-DD'))
  const values = trend.map(item => item.value || 0)
  
  if (!chartInstances.sleepChart) {
    chartInstances.sleepChart = echarts.init(sleepChartRef.value)
  }
  
  chartInstances.sleepChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '睡眠时长(小时)' },
    series: [{
      type: 'line',
      smooth: true,
      data: values,
      itemStyle: { color: '#409EFF' },
      areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
    }],
    grid: { left: 80, right: 20, bottom: 40, top: 20 }
  })
}

// 咨询量趋势（柱状图）
const drawConsultationChart = () => {
  if (!consultationChartRef.value) return
  
  const trend = stats.value?.workload?.consultation?.trend || []
  const dates = trend.map(item => dayjs(item.date).format('MM-DD'))
  const values = trend.map(item => item.value || 0)
  
  if (!chartInstances.consultationChart) {
    chartInstances.consultationChart = echarts.init(consultationChartRef.value)
  }
  
  chartInstances.consultationChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '次数' },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: { color: '#409EFF' }
    }],
    grid: { left: 50, right: 20, bottom: 40, top: 20 }
  })
}

// 随访计划趋势（柱状图）
const drawFollowupChart = () => {
  if (!followupChartRef.value) return
  
  const trend = stats.value?.workload?.followup?.trend || []
  const dates = trend.map(item => dayjs(item.date).format('MM-DD'))
  const values = trend.map(item => item.value || 0)
  
  if (!chartInstances.followupChart) {
    chartInstances.followupChart = echarts.init(followupChartRef.value)
  }
  
  chartInstances.followupChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '计划数' },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: { color: '#67C23A' }
    }],
    grid: { left: 50, right: 20, bottom: 40, top: 20 }
  })
}

// 提醒发送趋势（折线图）
const drawReminderChart = () => {
  if (!reminderChartRef.value) return
  
  const trend = stats.value?.workload?.reminder?.trend || []
  const dates = trend.map(item => dayjs(item.date).format('MM-DD'))
  const values = trend.map(item => item.value || 0)
  
  if (!chartInstances.reminderChart) {
    chartInstances.reminderChart = echarts.init(reminderChartRef.value)
  }
  
  chartInstances.reminderChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '发送数' },
    series: [{
      type: 'line',
      smooth: true,
      data: values,
      itemStyle: { color: '#E6A23C' }
    }],
    grid: { left: 50, right: 20, bottom: 40, top: 20 }
  })
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  Object.values(chartInstances).forEach(chart => {
    if (chart) chart.resize()
  })
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  if (familyId.value) {
    await loadStats()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(chartInstances).forEach(chart => {
    if (chart) chart.dispose()
  })
  chartInstances = {}
})

// 监听路由变化，确保在页面切换时状态正确
watch(() => route.path, (newPath, oldPath) => {
  // 当离开当前页面时，确保状态正确
  if (oldPath?.startsWith('/doctor/analysis') && !newPath?.startsWith('/doctor/analysis')) {
    // 离开分析页面时的清理操作
    // 重置日期范围为最近30天
    dateRange.value = [dayjs().subtract(29, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
    stats.value = null
  }
})
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.doctor-analysis {
  padding: 24px;
  min-height: calc(100vh - 60px);
}

:deep(.el-page-header) {
  margin-bottom: 24px;
  
  .el-page-header__content {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.toolbar {
  display: flex;
  gap: 16px;
  margin-top: 20px;
  align-items: center;
  padding: 24px;
  border-radius: 16px;
  @include mixins.glass-effect;
  border: 1px solid rgba(255, 255, 255, 0.3);
  flex-wrap: wrap;
  transition: all 0.3s vars.$ease-spring;
  animation: slideDown 0.5s vars.$ease-spring;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }

  .el-select, .el-date-picker, .el-button {
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-1px);
    }
  }
}

.stats-content {
  margin-top: 24px;
}

.stats-section {
  margin-bottom: 24px;
  animation: fadeInUp 0.6s vars.$ease-spring backwards;
  
  @for $i from 1 through 3 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.1}s;
    }
  }
}

// 卡片统一样式
:deep(.el-card) {
  border-radius: 16px;
  border: none;
  @include mixins.glass-effect;
  transition: all 0.3s vars.$ease-spring;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  }

  .el-card__header {
    padding: 20px 24px;
    font-weight: 600;
    font-size: 16px;
    color: var(--el-text-color-primary);
    border-bottom: 1px solid rgba(var(--el-border-color-lighter-rgb), 0.3);
    background: transparent;
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      display: block;
      width: 4px;
      height: 16px;
      background: var(--el-color-primary);
      border-radius: 2px;
      margin-right: 12px;
    }
  }

  .el-card__body {
    padding: 24px;
  }
}

.mt-16 {
  margin-top: 24px;
}

.chart-container {
  position: relative;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.6);
  }
  
  .chart-title {
    font-weight: 600;
    margin-bottom: 20px;
    font-size: 15px;
    color: var(--el-text-color-primary);
    display: flex;
    align-items: center;
    gap: 8px;
    
    &::after {
      content: '';
      flex: 1;
      height: 1px;
      background: linear-gradient(to right, var(--el-border-color-lighter), transparent);
    }
  }
  
  .chart {
    width: 100%;
    height: 320px;
    padding: 10px 0;
  }
  
  .chart-summary {
    margin-top: 16px;
    text-align: center;
    padding-top: 16px;
    border-top: 1px dashed var(--el-border-color-lighter);
    
    :deep(.el-statistic) {
      .el-statistic__content {
        font-size: 24px;
        font-weight: 600;
        color: var(--el-color-primary);
      }
      
      .el-statistic__title {
        font-size: 13px;
        margin-bottom: 4px;
      }
    }
  }
}

// 动画关键帧
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 1366px) {
  .doctor-analysis {
    padding: 16px;
  }

  .chart-container .chart {
    height: 280px;
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
    
    .el-select, .el-date-picker, .el-button {
      width: 100% !important;
    }
  }

  .chart-container .chart {
    height: 240px;
  }
  
  .el-col {
    width: 100% !important;
    margin-bottom: 16px;
  }
}
</style>
