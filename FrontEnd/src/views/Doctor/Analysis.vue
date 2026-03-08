<template>
  <div class="doctor-analysis">
    <!-- 顶部工具栏 (更紧凑) -->
    <div class="header-toolbar">
      <div class="page-title">数据分析</div>
      <div class="filters">
        <el-select :model-value="familyId" placeholder="选择家庭" size="small" style="width: 160px" @change="onSwitch">
          <el-option v-for="f in families" :key="f.id" :label="f.name" :value="String(f.id)" />
        </el-select>
        

        
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          size="small"
          range-separator=":"
          start-placeholder="开始"
          end-placeholder="结束"
          style="width: 220px"
          value-format="YYYY-MM-DD"
          @change="loadStats"
        />
        
        <el-button type="primary" size="small" :loading="loading" @click="loadStats">刷新</el-button>
      </div>
    </div>

    <div v-loading="loading" class="dashboard-container">
      <!-- Top Row: 关键指标 (15%) -->
      <div class="dashboard-row top-row">
        <el-row :gutter="12" class="fill-height">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-label">总患者数</div>
                <div class="stat-value">{{ topStats.totalPatients }}</div>
                <div class="stat-trend">
                  <span>--</span> 同比上周
                </div>
              </div>
              <div class="stat-icon bg-blue"><el-icon><User /></el-icon></div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-label">血压达标率</div>
                <div class="stat-value">{{ topStats.complianceRate }}%</div>
                <div class="stat-trend">
                  <span>--</span> 较上月
                </div>
              </div>
              <div class="stat-icon bg-green"><el-icon><Timer /></el-icon></div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-label">平均睡眠</div>
                <div class="stat-value">{{ topStats.averageSleepHours }}h</div>
                <div class="stat-trend">
                  <span>--</span> 需关注
                </div>
              </div>
              <div class="stat-icon bg-orange"><el-icon><Moon /></el-icon></div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-content">
                <div class="stat-label">本期咨询</div>
                <div class="stat-value">{{ topStats.totalConsultations }}</div>
                <div class="stat-trend">
                  <span>{{ topStats.reminderCompletionRate }}%</span> 提醒完成率
                </div>
              </div>
              <div class="stat-icon bg-purple"><el-icon><ChatDotRound /></el-icon></div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- Middle Row: 核心图表 (50%) -->
      <div class="dashboard-row middle-row">
        <el-row :gutter="12" class="fill-height">
          <el-col :span="12" class="fill-height">
            <el-card shadow="hover" class="chart-card fill-height" :body-style="{ padding: '10px', height: '100%', boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }">
              <div class="card-header">
                <span class="title">疾病分布分析 (雷达图)</span>
              </div>
              <div ref="diseaseChartRef" class="chart-box"></div>
            </el-card>
          </el-col>
          <el-col :span="12" class="fill-height">
            <el-card shadow="hover" class="chart-card fill-height" :body-style="{ padding: '10px', height: '100%', boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }">
              <div class="card-header">
                <span class="title">健康趋势监控 (血压/体重)</span>
                <el-select v-model="trendChartPatientId" placeholder="选择患者" size="small" style="width: 140px; margin-left: auto;" @change="drawBloodPressureChart" clearable>
                  <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.realName" :value="m.userId" />
                </el-select>
              </div>
              <div ref="bloodPressureChartRef" class="chart-box"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- Bottom Row: 次要指标/列表 (35%) -->
      <div class="dashboard-row bottom-row">
        <el-row :gutter="12" class="fill-height">
          <el-col :span="8" class="fill-height">
            <el-card shadow="hover" class="chart-card fill-height" :body-style="{ padding: '10px', height: '100%', boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }">
              <div class="card-header"><span class="title">健康计划与随访</span></div>
              <div class="health-plan-container">
                <div class="plan-header">
                  <p class="plan-title">{{ activePlan?.title || '暂无进行中的健康计划' }}</p>
                  <el-progress :percentage="planCompletion" :stroke-width="10" striped />
                </div>
                <p class="plan-content">
                  {{ activePlan?.description || '当前统计周期内未查询到可展示的计划信息。' }}
                </p>
                <div class="task-list-container">
                  <div v-for="task in followUpTasks" :key="task.id" class="task-item">
                    <span :class="{ completed: task.status === 'COMPLETED' }">
                      {{ task.title }}
                      <span v-if="task.scheduledTime" style="margin-left: 6px; color: #909399; font-size: 12px;">
                        {{ dayjs(task.scheduledTime).format('MM-DD HH:mm') }}
                      </span>
                    </span>
                    <el-button
                      :type="task.status === 'COMPLETED' ? 'success' : 'primary'"
                      size="small"
                      :disabled="task.status === 'COMPLETED'"
                      @click="completeFollowUp(task)"
                    >
                      {{ task.status === 'COMPLETED' ? '已完成' : '完成' }}
                    </el-button>
                  </div>
                  <div v-if="!followUpTasks.length" class="empty-hint" style="padding: 10px 0; font-size: 13px;">
                    当前统计区间内暂无待随访任务
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :span="8" class="fill-height">
            <el-card shadow="hover" class="chart-card fill-height" :body-style="{ padding: '10px', height: '100%', boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }">
              <div class="card-header"><span class="title">工作负载 (咨询趋势)</span></div>
              <div ref="consultationChartRef" class="chart-box"></div>
            </el-card>
          </el-col>

          <el-col :span="8" class="fill-height">
            <el-card shadow="hover" class="chart-card fill-height" :body-style="{ padding: '10px', height: '100%', boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }">
              <div class="card-header"><span class="title">近期预警/建议</span></div>
              <div class="alert-list">
                <div v-for="(alert, index) in alertsData" :key="index" class="alert-item" :class="alert.level">
                  <el-icon><component :is="alertIcons[alert.level]" /></el-icon>
                  <span class="text">{{ alert.text }}</span>
                  <span class="time">{{ alert.time }}</span>
                </div>
                <div v-if="!alertsData.length" class="empty-hint" style="padding: 20px 0; font-size: 13px;">
                  暂无预警信息
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 组件：Analysis.vue
 * 
 * 优化：一屏无滚动布局
 */

import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { User, Timer, Moon, ChatDotRound, Warning, WarnTriangleFilled, InfoFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import { useDoctorStore } from '../../stores/doctor'
import { getDoctorStats, getWorkbenchDashboard, listHealthPlans, listFollowUpTasks, updateFollowUpTask } from '../../api/doctor'
import { getLogs } from '../../api/log'
import dayjs from 'dayjs'

const doctorStore = useDoctorStore()

// 使用 store 中的状态
const families = computed(() => doctorStore.families)
const familyId = computed(() => doctorStore.currentFamilyId)
const members = computed(() => doctorStore.boundMembers)

// 动态计算的顶部卡片数据
const topStats = computed(() => {
  if (stats.value) {
    // 家庭模式
    const sleepHours = stats.value.managementEffect?.sleep?.averageSleepHours || 0;
    const reminderRate = stats.value.workload?.reminder?.completionRate || 0;
    return {
      totalPatients: members.value.length,
      complianceRate: stats.value.managementEffect?.bloodPressure?.complianceRate || 0,
      averageSleepHours: parseFloat(sleepHours).toFixed(1),
      totalConsultations: stats.value.workload?.consultation?.totalCount || 0,
      reminderCompletionRate: parseFloat(reminderRate).toFixed(1)
    };
  } 
  // 默认值
  return {
    totalPatients: 0,
    complianceRate: 0,
    averageSleepHours: 0,
    totalConsultations: 0,
    reminderCompletionRate: 0
  };
});

// 本地状态
const loading = ref(false)
const stats = ref(null)
const alertsData = ref([])
const trendChartPatientId = ref('')

// 健康计划与随访（必须来自系统真实数据）
const activePlan = ref(null)
const followUpTasks = ref([])

const planCompletion = computed(() => {
  const rate = activePlan.value?.completionRate
  if (rate === null || rate === undefined) return 0
  const n = Number(rate)
  if (!Number.isFinite(n)) return 0
  // 兼容 0-1 与 0-100 两种后端口径
  const pct = n <= 1 ? n * 100 : n
  return Math.max(0, Math.min(100, Math.round(pct)))
});

const completeFollowUp = async (task) => {
  if (!task?.id) return
  try {
    await updateFollowUpTask(task.id, { status: 'COMPLETED' })
    ElMessage.success('已标记完成')
    await loadStats()
  } catch (e) {
    ElMessage.error('标记失败：' + (e.response?.data?.message || e.message))
  }
}

const dateRange = ref([dayjs().subtract(29, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')])

const alertIcons = {
  warning: Warning,
  danger: WarnTriangleFilled,
  info: InfoFilled,
  success: CircleCheckFilled
};

// 图表引用
const diseaseChartRef = ref(null)
const bloodPressureChartRef = ref(null)
const consultationChartRef = ref(null)


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
  loading.value = true
  try {
    const currentFid = familyId.value || (families.value.length > 0 ? families.value[0].id : null)
    if (!currentFid) {
      stats.value = null
      alertsData.value = []
      activePlan.value = null
      followUpTasks.value = []
      return
    }

    const startDate = dateRange.value ? dateRange.value[0] : dayjs().subtract(29, 'day').format('YYYY-MM-DD')
    const endDate = dateRange.value ? dateRange.value[1] : dayjs().format('YYYY-MM-DD')

    const res = await getDoctorStats(currentFid, startDate, endDate)
    stats.value = res?.data || null
    await loadPlansAndFollowUps()
    
    // 优化渲染时序：先移除 loading，等待 DOM 更新，再渲染图表
    loading.value = false
    await nextTick()
    drawAllCharts()

    // 额外获取工作台数据以填充预警列表
    const workbenchRes = await getWorkbenchDashboard()
    if (workbenchRes.data?.criticalPatients) {
      alertsData.value = workbenchRes.data.criticalPatients.map(p => ({
        level: p.riskLevel === 'CRITICAL' ? 'danger' : 'warning',
        text: `${p.nickname || p.name}: ${p.riskDescription}`,
        time: dayjs(p.riskTime).format('YYYY-MM-DD HH:mm')
      }));
    }

  } catch (error) {
    ElMessage.error('加载失败：' + (error.response?.data?.message || error.message))
    loading.value = false
  }
}

const loadPlansAndFollowUps = async () => {
  const currentFid = familyId.value || (families.value.length > 0 ? families.value[0].id : null)
  if (!currentFid) return
  try {
    const startDate = dateRange.value?.[0] || dayjs().subtract(29, 'day').format('YYYY-MM-DD')
    const endDate = dateRange.value?.[1] || dayjs().format('YYYY-MM-DD')
    const [plansRes, followupsRes] = await Promise.all([
      listHealthPlans(currentFid, null, { status: 'ACTIVE', startDate, endDate }),
      listFollowUpTasks(currentFid, null, { status: 'PENDING', startDate, endDate })
    ])
    const plans = plansRes?.data || []
    const sortedPlans = [...plans].sort((a, b) => {
      const ar = a?.completionRate === null || a?.completionRate === undefined ? Infinity : Number(a.completionRate)
      const br = b?.completionRate === null || b?.completionRate === undefined ? Infinity : Number(b.completionRate)
      if (ar !== br) return ar - br
      const at = a?.createdAt ? dayjs(a.createdAt).valueOf() : 0
      const bt = b?.createdAt ? dayjs(b.createdAt).valueOf() : 0
      return bt - at
    })
    activePlan.value = sortedPlans.length ? sortedPlans[0] : null
    followUpTasks.value = (followupsRes?.data || []).slice(0, 5)
  } catch (e) {
    activePlan.value = null
    followUpTasks.value = []
  }
}

const drawAllCharts = () => {
  if (!stats.value) return
  
  drawDiseaseChart()
  drawBloodPressureChart()
  drawConsultationChart()

}

// 疾病分布 - 改为雷达图
const drawDiseaseChart = () => {
  if (!diseaseChartRef.value) return
  
  const data = stats.value?.patientStructure?.diseaseDistribution || {}
  const keys = Object.keys(data)
  
  if (!chartInstances.diseaseChart) {
    chartInstances.diseaseChart = echarts.init(diseaseChartRef.value)
  }

  if (!keys.length) {
    chartInstances.diseaseChart.clear()
    chartInstances.diseaseChart.setOption({
      title: {
        text: '暂无疾病分布数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#999', fontSize: 14, fontWeight: 500 }
      }
    })
    return
  }

  const maxVal = Math.max(...Object.values(data))
  const indicators = keys.map(key => ({ name: key, max: maxVal + 1 }))
  const values = keys.map(k => data[k])
  
  chartInstances.diseaseChart.setOption({
    tooltip: {},
    radar: {
      indicator: indicators,
      radius: '65%',
      center: ['50%', '55%'],
      splitNumber: 4,
      axisName: { color: '#666' }
    },
    series: [{
      type: 'radar',
      data: [{
        value: values,
        name: '疾病分布',
        areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
        lineStyle: { color: '#409EFF' },
        itemStyle: { color: '#409EFF' }
      }]
    }]
  })
}

// 血压与体重趋势 - 最终修复版，使用 getLogs
const drawBloodPressureChart = async () => {
  if (!bloodPressureChartRef.value) return;

  if (!chartInstances.bloodPressureChart) {
    chartInstances.bloodPressureChart = echarts.init(bloodPressureChartRef.value);
  }

  const patientId = trendChartPatientId.value;

  if (!patientId) {
    chartInstances.bloodPressureChart.clear();
    chartInstances.bloodPressureChart.setOption({
      title: {
        text: '请选择一位患者以查看其健康趋势',
        left: 'center',
        top: 'center',
        textStyle: { color: '#999', fontSize: 14 }
      }
    });
    return;
  }

  chartInstances.bloodPressureChart.showLoading();

  try {
    // 1. 直接获取最近7天的原始体征日志
    const startDate = dayjs().subtract(6, 'day').format('YYYY-MM-DD');
    const endDate = dayjs().format('YYYY-MM-DD');
    const res = await getLogs({ 
      type: 'VITALS', 
      startDate, 
      endDate, 
      userId: patientId 
    });
    const rawLogs = res.data || [];

    // 2. 将原始日志处理成按天聚合的数据 (取当天最后一条记录)
    const dailyVitals = new Map();
    for (const log of rawLogs) {
      const logDate = dayjs(log.logDate).format('YYYY-MM-DD');
      let content = log.content;
      if (typeof content === 'string') {
        try { content = JSON.parse(content); } catch (e) { continue; }
      }
      if (typeof content !== 'object' || content === null) continue;

      const dayData = dailyVitals.get(logDate) || {};
      if (content.type === '血压' || content.type === 'blood_pressure') {
        if (content.systolic) dayData.systolic = content.systolic;
        if (content.diastolic) dayData.diastolic = content.diastolic;
      } else if (content.type === '体重' || content.type === 'weight') {
        if (content.value) dayData.weight = content.value;
      }
      dailyVitals.set(logDate, dayData);
    }

    // 3. 创建完整7天时间轴，并用聚合数据填充
    const last7Days = Array.from({ length: 7 }, (_, i) => dayjs().subtract(6 - i, 'day').format('YYYY-MM-DD'));
    const displayDates = last7Days.map(d => dayjs(d).format('MM-DD'));
    const systolicData = last7Days.map(day => dailyVitals.get(day)?.systolic || null);
    const diastolicData = last7Days.map(day => dailyVitals.get(day)?.diastolic || null);
    const weightData = last7Days.map(day => dailyVitals.get(day)?.weight || null);

    chartInstances.bloodPressureChart.hideLoading();
    chartInstances.bloodPressureChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
      grid: { left: '10%', right: '10%', bottom: '10%' },
      legend: { data: ['收缩压', '舒张压', '体重'], top: '5%' },
      xAxis: { type: 'category', boundaryGap: false, data: displayDates },
      yAxis: [
        { type: 'value', name: '血压 (mmHg)', position: 'left', min: 60, max: 220, axisLine: { show: true, lineStyle: { color: '#409EFF' } } },
        { type: 'value', name: '体重 (kg)', position: 'right', min: 40, max: 220, axisLine: { show: true, lineStyle: { color: '#67C23A' } } }
      ],
      series: [
        { name: '收缩压', type: 'line', smooth: true, connectNulls: true, data: systolicData, itemStyle: { color: '#F56C6C' } },
        { name: '舒张压', type: 'line', smooth: true, connectNulls: true, data: diastolicData, itemStyle: { color: '#409EFF' } },
        { name: '体重', type: 'line', yAxisIndex: 1, smooth: true, connectNulls: true, data: weightData, itemStyle: { color: '#67C23A' } }
      ]
    }, true);
  } catch (error) {
    console.error('加载单人健康趋势失败:', error);
    chartInstances.bloodPressureChart.hideLoading();
    chartInstances.bloodPressureChart.setOption({
      title: {
        text: '数据加载失败，请稍后重试',
        left: 'center',
        top: 'center',
        textStyle: { color: '#F56C6C', fontSize: 14 }
      }
    });
  }
}

// 咨询趋势
const drawConsultationChart = () => {
  if (!consultationChartRef.value) return
  
  if (!chartInstances.consultationChart) {
    chartInstances.consultationChart = echarts.init(consultationChartRef.value)
  }

  const startDate = dateRange.value?.[0]
  const endDate = dateRange.value?.[1]
  const trendList = stats.value?.workload?.consultation?.trend || []
  const trendMap = new Map(
    trendList
      .filter(dv => dv?.date)
      .map(dv => [dayjs(dv.date).format('YYYY-MM-DD'), Number(dv.value || 0)])
  )

  let days = []
  if (startDate && endDate) {
    const start = dayjs(startDate)
    const end = dayjs(endDate)
    const diff = end.diff(start, 'day')
    if (diff >= 0 && diff <= 365) {
      days = Array.from({ length: diff + 1 }, (_, i) => start.add(i, 'day').format('YYYY-MM-DD'))
    }
  }

  const xAxisData = days.length ? days.map(d => dayjs(d).format('MM-DD')) : trendList.map(dv => dayjs(dv.date).format('MM-DD'))
  const seriesData = days.length ? days.map(d => trendMap.get(d) ?? 0) : trendList.map(dv => Number(dv.value || 0))

  if (!xAxisData.length) {
    chartInstances.consultationChart.clear()
    chartInstances.consultationChart.setOption({
      title: {
        text: '暂无咨询趋势数据',
        left: 'center',
        top: 'center',
        textStyle: { color: '#999', fontSize: 14, fontWeight: 500 }
      }
    })
    return
  }
  
  chartInstances.consultationChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: xAxisData },
    yAxis: { type: 'value' },
    series: [{
      data: seriesData,
      type: 'bar',
      barWidth: '40%',
      itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] }
    }]
  })
}



const handleResize = () => {
  Object.values(chartInstances).forEach(chart => {
    if (chart) chart.resize()
  })
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  
  // 确保家庭数据已加载
  if (!doctorStore.families.length) {
     await doctorStore.fetchFamilies()
  }
  
  // 尝试自动选择第一个家庭
  if (!familyId.value && families.value.length > 0) {
    await doctorStore.setCurrentFamily(families.value[0].id)
  }

  // 加载真实统计数据（无 mock 兜底）
  await loadStats()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(chartInstances).forEach(chart => {
    if (chart) chart.dispose()
  })
  chartInstances = {}
})
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;

.doctor-analysis {
  height: calc(100vh - 60px); /* 锁定高度，假设Navbar 60px */
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  background-color: #f6f8fa;
  box-sizing: border-box;
}

.header-toolbar {
  height: 40px;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  
  .page-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--el-text-color-primary);
  }
  
  .filters {
    display: flex;
    gap: 12px;
  }
}

.dashboard-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden; /* 防止滚动 */
}

.dashboard-row {
  width: 100%;
}

.top-row {
  height: 15%;
  min-height: 100px;
}

.middle-row {
  height: 50%;
  flex: 1; /* 自适应剩余空间 */
}

.bottom-row {
  height: 35%;
  min-height: 200px;
}

.fill-height {
  height: 100%;
}

/* 卡片样式优化 */
.stat-card {
  height: 100%;
  border: none;
  border-radius: 8px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  
  :deep(.el-card__body) {
    padding: 16px;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.stat-content {
  z-index: 1;
  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 4px;
  }
  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
  }
  .stat-trend {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    display: flex;
    align-items: center;
    gap: 4px;
    
    .up { color: #f56c6c; display: flex; align-items: center; }
    .down { color: #67c23a; display: flex; align-items: center; }
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  opacity: 0.9;
  
  &.bg-blue { background: linear-gradient(135deg, #409EFF, #337ecc); }
  &.bg-green { background: linear-gradient(135deg, #67C23A, #529b2e); }
  &.bg-orange { background: linear-gradient(135deg, #E6A23C, #b88230); }
  &.bg-purple { background: linear-gradient(135deg, #a0cfff, #8cc5ff); }
}

/* 图表卡片 */
.chart-card {
  height: 100%;
  border: none;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  
  .card-header {
    height: 32px;
    display: flex;
    align-items: center;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 8px;
    flex-shrink: 0;
    
    .title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      border-left: 3px solid #409EFF;
      padding-left: 8px;
    }
  }
  
  .chart-box {
    flex: 1;
    width: 100%;
    min-height: 0; /* 关键：允许flex子项收缩 */
  }
}

/* 预警列表 */
.alert-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
  
  .alert-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    border-radius: 4px;
    margin-bottom: 8px;
    background: #fdf6ec;
    border: 1px solid #faecd8;
    
    &.warning { color: #e6a23c; }
    &.danger { background: #fef0f0; border-color: #fde2e2; color: #f56c6c; }
    &.info { background: #f4f4f5; border-color: #e9e9eb; color: #909399; }
    &.success { background: #f0f9eb; border-color: #e1f3d8; color: #67c23a; }
    
    .text {
      flex: 1;
      font-size: 12px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .time {
      font-size: 11px;
      color: #909399;
    }
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 4px;
}
::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}

/* 新增：健康计划与随访模块样式 */
.health-plan-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.plan-header {
  margin-bottom: 8px;
}

.plan-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 8px;
}

.plan-content {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 12px;
}

.task-list-container {
  flex: 1;
  overflow-y: auto;
  padding-right: 5px; /* for scrollbar */
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 4px;
  font-size: 13px;
  border-bottom: 1px solid #f0f2f5;
}

.task-item:last-child {
  border-bottom: none;
}

.task-item span.completed {
  text-decoration: line-through;
  color: #999;
}
</style>