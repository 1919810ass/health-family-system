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
                <div class="stat-label">本月咨询</div>
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
                  <p class="plan-title">{{ healthPlan.title }}</p>
                  <el-progress :percentage="planCompletion" :stroke-width="10" striped />
                </div>
                <p class="plan-content">{{ healthPlan.content }}</p>
                <div class="task-list-container">
                  <div v-for="task in healthPlan.tasks" :key="task.id" class="task-item">
                    <span :class="{ completed: task.completed }">{{ task.text }}</span>
                    <el-button 
                      :type="task.completed ? 'success' : 'primary'" 
                      size="small" 
                      :disabled="task.completed"
                      @click="completeTask(task)"
                    >
                      {{ task.completed ? '已完成' : '完成' }}
                    </el-button>
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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { User, Timer, Moon, ChatDotRound, Top, Bottom, Warning, WarnTriangleFilled, InfoFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import { useDoctorStore } from '../../stores/doctor'
import { getDoctorStats, getWorkbenchDashboard } from '../../api/doctor'
import { getLogs } from '../../api/log'
import { getDoctorView } from '../../api/family'
import dayjs from 'dayjs'

const route = useRoute()
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

// 新增：健康计划与随访任务
const healthPlan = ref({
  title: 'AI 生成的春季养生计划',
  content: '本计划旨在通过调整生活方式，增强春季体质，预防季节性不适。请按时完成以下随访任务。',
  tasks: [
    { id: 1, text: '提醒患者注意保暖，避免风寒', completed: false },
    { id: 2, text: '建议患者增加户外活动，每日至少30分钟', completed: true },
    { id: 3, text: '发送春季饮食指南，强调多食甘味、少食酸味', completed: false },
    { id: 4, text: '跟进患者睡眠情况，确保每晚7-8小时睡眠', completed: false },
    { id: 5, text: '检查患者过敏药物储备情况', completed: false },
  ],
});

const planCompletion = computed(() => {
  const total = healthPlan.value.tasks.length;
  if (total === 0) return 0;
  const completed = healthPlan.value.tasks.filter(t => t.completed).length;
  return Math.round((completed / total) * 100);
});

const completeTask = (taskToComplete) => {
  const taskIndex = healthPlan.value.tasks.findIndex(t => t.id === taskToComplete.id);
  if (taskIndex !== -1 && !healthPlan.value.tasks[taskIndex].completed) {
    // 创建一个新对象来替换，以确保响应性
    const updatedTask = { ...healthPlan.value.tasks[taskIndex], completed: true };
    healthPlan.value.tasks.splice(taskIndex, 1, updatedTask);
    ElMessage.success(`任务 "${updatedTask.text}" 已完成`);
  }
};

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
  console.log('Analysis: 开始获取统计数据...');
  loading.value = true;
  try {
    const currentFid = familyId.value || (families.value.length > 0 ? families.value[0].id : null);
    let res = null;

    if (currentFid) {
      const startDate = dateRange.value ? dateRange.value[0] : dayjs().subtract(29, 'day').format('YYYY-MM-DD');
      const endDate = dateRange.value ? dateRange.value[1] : dayjs().format('YYYY-MM-DD');
      
      console.log('Analysis: 获取家庭统计数据', currentFid);
      res = await getDoctorStats(currentFid, startDate, endDate);
    }

    if (!res?.data || Object.keys(res.data).length === 0) {
      console.warn('API returned empty stats or no family selected, using mock data');
      stats.value = {
        patientStructure: {
          diseaseDistribution: { '高血压': 12, '糖尿病': 8, '冠心病': 5, '慢阻肺': 3, '脑卒中': 2, '其他': 6 },
          ageDistribution: { '60以下': 5, '60-70': 12, '70-80': 8, '80以上': 3 },
          genderDistribution: { 'M': 15, 'F': 13 }
        },
        managementEffect: {
          bloodPressure: { 
            trend: Array.from({length: 7}, (_, i) => ({
              userId: members.value[i % members.value.length]?.userId || 1, // 模拟不同用户的数据
              date: dayjs().subtract(6-i, 'day').format('YYYY-MM-DD'),
              systolic: Math.floor(120 + Math.random() * 15), // 收缩压
              diastolic: Math.floor(75 + Math.random() * 10) // 舒张压
            })),
            complianceRate: 85.5 
          },
          weight: { 
            trend: Array.from({length: 7}, (_, i) => ({
              date: dayjs().subtract(6-i, 'day').format('YYYY-MM-DD'),
              value: parseFloat((65 + Math.random() * 2 - 1).toFixed(1))
            })),
            averageWeightChange: -1.2 
          },
          sleep: { 
            trend: Array.from({length: 7}, (_, i) => ({
              date: dayjs().subtract(6-i, 'day').format('YYYY-MM-DD'),
              value: (6 + Math.random() * 2).toFixed(1)
            })),
            averageSleepHours: 7.2 
          }
        },
        workload: {
          consultation: {
            totalCount: 42,
            trend: Array.from({length: 7}, (_, i) => ({
              date: dayjs().subtract(6-i, 'day').format('YYYY-MM-DD'),
              value: Math.floor(Math.random() * 10)
            }))
          },
          followup: {
            totalPlans: 15,
            completion: { completed: 12, pending: 3 }, // For Pie chart
            trend: Array.from({length: 7}, (_, i) => ({
              date: dayjs().subtract(6-i, 'day').format('YYYY-MM-DD'),
              value: Math.floor(Math.random() * 5)
            }))
          },
          reminder: {
              completionRate: 92.5,
              trend: Array.from({length: 7}, (_, i) => ({
                date: dayjs().subtract(6-i, 'day').format('YYYY-MM-DD'),
                value: Math.floor(Math.random() * 8)
              }))
            }
          },
          recentAlerts: [
            { level: 'danger', text: '患者 [李四] 血糖触发红色预警', time: '昨天' },
            { level: 'warning', text: '患者 [张三] 血压连续3天偏高', time: '10:23' },
            { level: 'info', text: '本周随访计划完成率低于 80%', time: '周一' },
            { level: 'success', text: '患者 [王五] 体重管理目标达成', time: '周日' },
          ]
        };
    } else {
      stats.value = res.data;
    }
    
    // 优化渲染时序：先移除 loading，等待 DOM 更新，再渲染图表
    loading.value = false;
    await nextTick();
    drawAllCharts();

    // 额外获取工作台数据以填充预警列表
    const workbenchRes = await getWorkbenchDashboard();
    if (workbenchRes.data?.criticalPatients) {
      alertsData.value = workbenchRes.data.criticalPatients.map(p => ({
        level: p.riskLevel === 'CRITICAL' ? 'danger' : 'warning',
        text: `${p.nickname || p.name}: ${p.riskDescription}`,
        time: dayjs(p.riskTime).format('YYYY-MM-DD HH:mm')
      }));
    }

  } catch (error) {
    console.error('Analysis: 加载失败', error);
    ElMessage.error('加载失败');
    loading.value = false; // 确保异常时也关闭 loading
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
  
  const data = stats.value?.patientStructure?.diseaseDistribution || { '高血压': 12, '糖尿病': 8, '冠心病': 5, '慢阻肺': 3, '脑卒中': 2, '其他': 6 }
  const indicators = Object.keys(data).map(key => ({ name: key, max: Math.max(...Object.values(data)) + 5 }))
  const values = Object.values(data)
  
  if (!chartInstances.diseaseChart) {
    chartInstances.diseaseChart = echarts.init(diseaseChartRef.value)
  }
  
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
  
  chartInstances.consultationChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
    yAxis: { type: 'value' },
    series: [{
      data: [12, 15, 8, 22, 18, 10, 14], // Mock
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
  console.log('Analysis: Component Mounted')
  window.addEventListener('resize', handleResize)
  
  // 确保家庭数据已加载
  if (!doctorStore.families.length) {
     console.log('Analysis: 尝试加载家庭列表...')
     await doctorStore.fetchFamilies()
  }
  
  // 尝试自动选择第一个家庭
  if (!familyId.value && families.value.length > 0) {
    console.log('Analysis: 自动选择第一个家庭', families.value[0].id)
    await doctorStore.setCurrentFamily(families.value[0].id)
  }

  // 无论如何尝试加载一次数据（内部有mock兜底）
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