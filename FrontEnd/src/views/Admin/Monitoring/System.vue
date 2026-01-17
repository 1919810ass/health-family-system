<template>
  <div class="system-monitoring">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">系统监控</h1>
        <p class="page-subtitle">实时监控系统运行状态与性能指标</p>
      </div>
    </div>

    <!-- 系统状态卡片 -->
    <div class="status-cards mb-24 stagger-anim" style="--delay: 0.2s">
      <div class="glass-card status-card">
        <div class="status-header">
          <div class="header-left">
            <div class="icon-box primary"><el-icon><Connection /></el-icon></div>
            <span class="status-title">应用服务</span>
          </div>
          <el-tag type="success" effect="light" round>正常</el-tag>
        </div>
        <div class="status-info">
          <div class="info-item">
            <span class="label">响应时间</span>
            <span class="value">120ms</span>
          </div>
          <div class="info-item">
            <span class="label">运行时间</span>
            <span class="value">7天 12小时</span>
          </div>
          <div class="info-item">
            <span class="label">版本</span>
            <span class="value">v1.2.3</span>
          </div>
        </div>
      </div>

      <div class="glass-card status-card">
        <div class="status-header">
          <div class="header-left">
            <div class="icon-box success"><el-icon><DataLine /></el-icon></div>
            <span class="status-title">数据库</span>
          </div>
          <el-tag type="success" effect="light" round>正常</el-tag>
        </div>
        <div class="status-info">
          <div class="info-item">
            <span class="label">连接数</span>
            <span class="value">24/100</span>
          </div>
          <div class="info-item">
            <span class="label">查询QPS</span>
            <span class="value">156</span>
          </div>
          <div class="info-item">
            <span class="label">慢查询</span>
            <span class="value">0</span>
          </div>
        </div>
      </div>

      <div class="glass-card status-card">
        <div class="status-header">
          <div class="header-left">
            <div class="icon-box warning"><el-icon><Cpu /></el-icon></div>
            <span class="status-title">缓存服务</span>
          </div>
          <el-tag type="success" effect="light" round>正常</el-tag>
        </div>
        <div class="status-info">
          <div class="info-item">
            <span class="label">内存使用</span>
            <span class="value">1.2GB/4GB</span>
          </div>
          <div class="info-item">
            <span class="label">命中率</span>
            <span class="value">96.8%</span>
          </div>
          <div class="info-item">
            <span class="label">键数量</span>
            <span class="value">12,456</span>
          </div>
        </div>
      </div>

      <div class="glass-card status-card">
        <div class="status-header">
          <div class="header-left">
            <div class="icon-box info"><el-icon><Files /></el-icon></div>
            <span class="status-title">文件存储</span>
          </div>
          <el-tag type="success" effect="light" round>正常</el-tag>
        </div>
        <div class="status-info">
          <div class="info-item">
            <span class="label">存储使用</span>
            <span class="value">45.2GB/500GB</span>
          </div>
          <div class="info-item">
            <span class="label">上传速度</span>
            <span class="value">2.3MB/s</span>
          </div>
          <div class="info-item">
            <span class="label">文件数量</span>
            <span class="value">8,765</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 实时监控图表 -->
    <div class="glass-card chart-card mb-24 stagger-anim" style="--delay: 0.3s">
      <div class="card-header">
        <div class="header-left">
          <div class="icon-box primary"><el-icon><Monitor /></el-icon></div>
          <span class="title">实时性能监控</span>
        </div>
        <div class="header-actions">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :default-time="[new Date(2024, 0, 1, 0, 0, 0), new Date(2024, 0, 1, 23, 59, 59)]"
            @change="refreshCharts"
          />
        </div>
      </div>
      
      <div class="charts-container">
        <div ref="cpuChartRef" class="chart" style="height: 300px;"></div>
        <div ref="memoryChartRef" class="chart" style="height: 300px;"></div>
        <div ref="requestChartRef" class="chart" style="height: 300px;"></div>
        <div ref="responseChartRef" class="chart" style="height: 300px;"></div>
      </div>
    </div>

    <!-- API性能监控 -->
    <div class="glass-card api-monitor-card mb-24 stagger-anim" style="--delay: 0.4s">
      <div class="card-header">
        <div class="header-left">
          <div class="icon-box success"><el-icon><Link /></el-icon></div>
          <span class="title">API性能监控</span>
        </div>
        <div class="header-actions">
          <el-button @click="refreshApiStats" :loading="apiStatsLoading" circle plain>
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>
      
      <el-table :data="apiStats" style="width: 100%" v-loading="apiStatsLoading" class="custom-table">
        <el-table-column prop="endpoint" label="API端点" width="200" show-overflow-tooltip />
        <el-table-column prop="method" label="方法" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodTagType(row.method)" effect="light" round>
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="calls" label="调用次数" width="120" />
        <el-table-column prop="avgResponseTime" label="平均响应时间(ms)" width="160">
          <template #default="{ row }">
            <span :class="['response-time', getResponseTimeClass(row.avgResponseTime)]">
              {{ row.avgResponseTime }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="errorRate" label="错误率" width="120">
          <template #default="{ row }">
            <span :class="['error-rate', getErrorRateClass(row.errorRate)]">
              {{ row.errorRate }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="successRate" label="成功率" width="120">
          <template #default="{ row }">
            <div class="success-rate-wrapper">
              <el-progress 
                :percentage="row.successRate" 
                :color="getSuccessRateColor(row.successRate)"
                :show-text="false"
                :stroke-width="8"
                style="width: 60px;"
              />
              <span class="success-rate-text">{{ row.successRate }}%</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 系统告警 -->
    <div class="glass-card stagger-anim" style="--delay: 0.5s">
      <div class="card-header">
        <div class="header-left">
          <div class="icon-box danger"><el-icon><Bell /></el-icon></div>
          <span class="title">系统告警</span>
        </div>
        <div class="header-actions">
          <el-button @click="refreshAlerts" :loading="alertsLoading" circle plain>
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>
      
      <el-table :data="alerts" style="width: 100%" v-loading="alertsLoading" class="custom-table">
        <el-table-column prop="level" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getAlertLevelTagType(row.level)" effect="light" round>
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="告警信息" show-overflow-tooltip />
        <el-table-column prop="source" label="来源" width="150" />
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已处理' ? 'success' : 'warning'" effect="plain" round>
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" text type="primary" @click="handleAcknowledgeAlert(row)">确认</el-button>
              <el-button size="small" text type="danger" @click="handleResolveAlert(row)">解决</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, DataLine, Bell, Connection, Files, Cpu, Link, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getSystemStats, getApiPerformance, getSystemAlerts } from '../../../api/admin'

// 图表引用
const cpuChartRef = ref(null)
const memoryChartRef = ref(null)
const requestChartRef = ref(null)
const responseChartRef = ref(null)

// 时间范围
const timeRange = ref([
  dayjs().subtract(1, 'hour').toDate(),
  dayjs().toDate()
])

// API性能数据
const apiStats = ref([])
const apiStatsLoading = ref(false)

// 系统告警数据
const alerts = ref([])
const alertsLoading = ref(false)

// 图表实例
let cpuChart = null
let memoryChart = null
let requestChart = null
let responseChart = null

// 模拟加载数据
onMounted(() => {
  initCharts()
  loadSystemStats()
  loadApiStats()
  loadAlerts()
  
  // 定时刷新数据
  const refreshInterval = setInterval(() => {
    refreshCharts()
    loadSystemStats()
    loadApiStats()
    loadAlerts()
  }, 30000) // 每30秒刷新一次
  
  // 组件卸载时清理定时器
  onUnmounted(() => {
    clearInterval(refreshInterval)
    if (cpuChart) cpuChart.dispose()
    if (memoryChart) memoryChart.dispose()
    if (requestChart) requestChart.dispose()
    if (responseChart) responseChart.dispose()
  })
})

// 初始化图表
const initCharts = () => {
  // Common options
  const commonOptions = {
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    tooltip: { trigger: 'axis' }
  }

  // CPU使用率图表
  cpuChart = echarts.init(cpuChartRef.value)
  cpuChart.setOption({
    ...commonOptions,
    title: { text: 'CPU使用率 (%)', left: 'center', textStyle: { fontSize: 14, color: '#6B7280' } },
    xAxis: { type: 'category', data: generateTimeLabels(), axisLine: { lineStyle: { color: '#E5E7EB' } } },
    yAxis: { type: 'value', max: 100, splitLine: { lineStyle: { type: 'dashed', color: '#E5E7EB' } } },
    series: [{
      data: generateRandomData(20, 0, 80),
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(123, 97, 255, 0.3)' },
          { offset: 1, color: 'rgba(123, 97, 255, 0.05)' }
        ])
      },
      itemStyle: { color: '#7B61FF' }
    }]
  })

  // 内存使用率图表
  memoryChart = echarts.init(memoryChartRef.value)
  memoryChart.setOption({
    ...commonOptions,
    title: { text: '内存使用率 (%)', left: 'center', textStyle: { fontSize: 14, color: '#6B7280' } },
    xAxis: { type: 'category', data: generateTimeLabels(), axisLine: { lineStyle: { color: '#E5E7EB' } } },
    yAxis: { type: 'value', max: 100, splitLine: { lineStyle: { type: 'dashed', color: '#E5E7EB' } } },
    series: [{
      data: generateRandomData(20, 30, 70),
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 210, 186, 0.3)' },
          { offset: 1, color: 'rgba(0, 210, 186, 0.05)' }
        ])
      },
      itemStyle: { color: '#00D2BA' }
    }]
  })

  // API请求量图表
  requestChart = echarts.init(requestChartRef.value)
  requestChart.setOption({
    ...commonOptions,
    title: { text: 'API请求量 (次/分钟)', left: 'center', textStyle: { fontSize: 14, color: '#6B7280' } },
    xAxis: { type: 'category', data: generateTimeLabels(), axisLine: { lineStyle: { color: '#E5E7EB' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#E5E7EB' } } },
    series: [{
      data: generateRandomData(20, 50, 200),
      type: 'bar',
      itemStyle: { 
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#5D8BF4' },
          { offset: 1, color: '#7B61FF' }
        ])
      }
    }]
  })

  // 响应时间图表
  responseChart = echarts.init(responseChartRef.value)
  responseChart.setOption({
    ...commonOptions,
    title: { text: '平均响应时间 (ms)', left: 'center', textStyle: { fontSize: 14, color: '#6B7280' } },
    xAxis: { type: 'category', data: generateTimeLabels(), axisLine: { lineStyle: { color: '#E5E7EB' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#E5E7EB' } } },
    series: [{
      data: generateRandomData(20, 50, 300),
      type: 'line',
      smooth: true,
      lineStyle: { width: 3 },
      itemStyle: { color: '#FFB84C' }
    }]
  })
  
  // Resize charts on window resize
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  cpuChart && cpuChart.resize()
  memoryChart && memoryChart.resize()
  requestChart && requestChart.resize()
  responseChart && responseChart.resize()
}

// 生成时间标签
const generateTimeLabels = () => {
  const labels = []
  for (let i = 19; i >= 0; i--) {
    const time = dayjs().subtract(i, 'minute')
    labels.push(time.format('HH:mm'))
  }
  return labels
}

// 生成随机数据
const generateRandomData = (count, min, max) => {
  const data = []
  for (let i = 0; i < count; i++) {
    data.push(Math.floor(Math.random() * (max - min + 1)) + min)
  }
  return data
}

// 刷新图表
const refreshCharts = () => {
  const timeLabels = generateTimeLabels()
  
  cpuChart.setOption({
    xAxis: { data: timeLabels },
    series: [{ data: generateRandomData(20, 0, 80) }]
  })
  
  memoryChart.setOption({
    xAxis: { data: timeLabels },
    series: [{ data: generateRandomData(20, 30, 70) }]
  })
  
  requestChart.setOption({
    xAxis: { data: timeLabels },
    series: [{ data: generateRandomData(20, 50, 200) }]
  })
  
  responseChart.setOption({
    xAxis: { data: timeLabels },
    series: [{ data: generateRandomData(20, 50, 300) }]
  })
}

// 加载系统状态数据
const loadSystemStats = async () => {
  try {
    const response = await getSystemStats()
    if (response.data) {
      console.log('System stats:', response.data)
    }
  } catch (error) {
    ElMessage.error('加载系统状态数据失败')
    console.error('Error loading system stats:', error)
  }
}

// 加载API性能数据
const loadApiStats = async () => {
  apiStatsLoading.value = true
  try {
    const params = {
      startTime: timeRange.value[0],
      endTime: timeRange.value[1]
    }
    const response = await getApiPerformance(params)
    apiStats.value = response.data.items || response.data || []
  } catch (error) {
    ElMessage.error('加载API性能数据失败')
    console.error('Error loading API performance:', error)
  } finally {
    apiStatsLoading.value = false
  }
}

// 加载系统告警
const loadAlerts = async () => {
  alertsLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    
    alerts.value = [
      { level: '警告', message: '数据库连接池使用率超过80%', source: '数据库监控', time: '2024-01-15 14:30:22', status: '未处理' },
      { level: '信息', message: 'API响应时间超过阈值', source: '性能监控', time: '2024-01-15 14:25:15', status: '已处理' },
      { level: '错误', message: '文件存储服务异常', source: '存储监控', time: '2024-01-15 14:20:43', status: '未处理' },
      { level: '警告', message: '用户登录失败次数异常', source: '安全监控', time: '2024-01-15 14:15:33', status: '已处理' },
      { level: '信息', message: '系统负载正常', source: '系统监控', time: '2024-01-15 14:10:28', status: '已处理' }
    ]
  } catch (error) {
    ElMessage.error('加载系统告警失败')
  } finally {
    alertsLoading.value = false
  }
}

// 刷新API统计
const refreshApiStats = () => {
  loadApiStats()
}

// 刷新告警
const refreshAlerts = () => {
  loadAlerts()
}

// 获取方法标签类型
const getMethodTagType = (method) => {
  switch (method) {
    case 'GET': return 'success'
    case 'POST': return 'primary'
    case 'PUT': return 'warning'
    case 'DELETE': return 'danger'
    default: return 'info'
  }
}

// 获取响应时间样式类
const getResponseTimeClass = (time) => {
  if (time < 100) return 'good'
  if (time < 200) return 'normal'
  return 'slow'
}

// 获取错误率样式类
const getErrorRateClass = (rate) => {
  if (rate < 1) return 'low'
  if (rate < 5) return 'medium'
  return 'high'
}

// 获取成功率颜色
const getSuccessRateColor = (rate) => {
  if (rate >= 99) return '#67C23A'
  if (rate >= 95) return '#E6A23C'
  return '#F56C6C'
}

// 获取告警级别标签类型
const getAlertLevelTagType = (level) => {
  switch (level) {
    case '错误': return 'danger'
    case '警告': return 'warning'
    case '信息': return 'info'
    default: return 'info'
  }
}

// 确认告警
const handleAcknowledgeAlert = async (alert) => {
  try {
    await ElMessageBox.confirm(
      `确定要确认告警: ${alert.message} 吗？`,
      '确认告警',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    alert.status = '已处理'
    ElMessage.success('告警已确认')
  } catch {
    // 用户取消操作
  }
}

// 解决告警
const handleResolveAlert = async (alert) => {
  try {
    await ElMessageBox.confirm(
      `确定要解决告警: ${alert.message} 吗？`,
      '解决告警',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    alert.status = '已处理'
    ElMessage.success('告警已解决')
  } catch {
    // 用户取消操作
  }
}
</script>

<style scoped lang="scss">
@use 'sass:map';
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.system-monitoring {
  padding: 24px;
  min-height: 100%;

  .page-header {
    margin-bottom: 32px;

    .header-content {
      .page-title {
        font-size: 28px;
        font-weight: 800;
        margin: 0 0 8px 0;
        @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info')));
        letter-spacing: -0.5px;
      }

      .page-subtitle {
        color: map.get(vars.$colors, 'text-secondary');
        margin: 0;
        font-size: 14px;
      }
    }
  }

  .mb-24 {
    margin-bottom: 24px;
  }

  .status-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
    gap: 20px;

    .status-card {
      @include mixins.glass-effect;
      border-radius: vars.$radius-lg;
      padding: 20px;
      border: 1px solid rgba(255, 255, 255, 0.6);
      box-shadow: vars.$shadow-lg;
      transition: all 0.3s vars.$ease-spring;

      &:hover {
        transform: translateY(-4px);
      }

      .status-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        .header-left {
          display: flex;
          align-items: center;
          gap: 12px;

          .icon-box {
            width: 40px;
            height: 40px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            
            &.primary { background: rgba(map.get(vars.$colors, 'primary'), 0.1); color: map.get(vars.$colors, 'primary'); }
            &.success { background: rgba(map.get(vars.$colors, 'success'), 0.1); color: map.get(vars.$colors, 'success'); }
            &.warning { background: rgba(map.get(vars.$colors, 'warning'), 0.1); color: map.get(vars.$colors, 'warning'); }
            &.info { background: rgba(map.get(vars.$colors, 'info'), 0.1); color: map.get(vars.$colors, 'info'); }
            &.danger { background: rgba(map.get(vars.$colors, 'danger'), 0.1); color: map.get(vars.$colors, 'danger'); }
          }

          .status-title {
            font-size: 16px;
            font-weight: 700;
            color: map.get(vars.$colors, 'text-main');
          }
        }
      }

      .status-info {
        .info-item {
          display: flex;
          justify-content: space-between;
          margin-bottom: 12px;
          font-size: 14px;
          
          &:last-child {
            margin-bottom: 0;
          }

          .label {
            color: map.get(vars.$colors, 'text-secondary');
          }

          .value {
            font-weight: 600;
            color: map.get(vars.$colors, 'text-main');
          }
        }
      }
    }
  }

  .glass-card {
    @include mixins.glass-effect;
    border-radius: vars.$radius-lg;
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: vars.$shadow-lg;
    padding: 24px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid rgba(map.get(vars.$colors, 'text-main'), 0.05);

      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .icon-box {
          width: 40px;
          height: 40px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          &.primary { background: rgba(map.get(vars.$colors, 'primary'), 0.1); color: map.get(vars.$colors, 'primary'); }
          &.success { background: rgba(map.get(vars.$colors, 'success'), 0.1); color: map.get(vars.$colors, 'success'); }
          &.danger { background: rgba(map.get(vars.$colors, 'danger'), 0.1); color: map.get(vars.$colors, 'danger'); }
        }

        .title {
          font-size: 18px;
          font-weight: 700;
          color: map.get(vars.$colors, 'text-main');
        }
      }
    }
  }

  .chart-card {
    .charts-container {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
      gap: 20px;
      
      @media (max-width: 1024px) {
        grid-template-columns: 1fr;
      }
    }
  }

  .api-monitor-card {
    .success-rate-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .success-rate-text {
        font-size: 12px;
        color: map.get(vars.$colors, 'text-secondary');
      }
    }

    :deep(.response-time) {
      font-weight: 600;
      &.good { color: map.get(vars.$colors, 'success'); }
      &.normal { color: map.get(vars.$colors, 'warning'); }
      &.slow { color: map.get(vars.$colors, 'danger'); }
    }

    :deep(.error-rate) {
      font-weight: 600;
      &.low { color: map.get(vars.$colors, 'success'); }
      &.medium { color: map.get(vars.$colors, 'warning'); }
      &.high { color: map.get(vars.$colors, 'danger'); }
    }
  }

  .custom-table {
    background: transparent;
    
    :deep(tr), :deep(th.el-table__cell) {
      background: transparent;
    }
    
    :deep(.el-table__row:hover) {
      background-color: rgba(map.get(vars.$colors, 'primary'), 0.05) !important;
    }

    .action-buttons {
      display: flex;
      gap: 8px;
    }
  }

  // Animation Utilities
  .stagger-anim {
    opacity: 0;
    animation: slideUpFade 0.8s vars.$ease-spring forwards;
    animation-delay: var(--delay, 0s);
  }

  @keyframes slideUpFade {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // Responsive adjustments
  @media (max-width: 768px) {
    padding: 16px;

    .page-header {
      .header-content {
        .page-title { font-size: 24px; }
      }
    }

    .glass-card {
      padding: 16px;
      
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
        
        .header-actions {
          width: 100%;
          display: flex;
          justify-content: flex-end;
        }
      }
    }
  }
}
</style>