<template>
  <div class="app-container">
    <div class="header">
      <h2>AI 服务监控仪表盘</h2>
      <el-button type="primary" @click="fetchData">刷新</el-button>
    </div>

    <!-- Top Cards -->
    <el-row :gutter="20" class="card-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日 Token 消耗</span>
            </div>
          </template>
          <div class="card-value">{{ stats.todayTokens }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>平均响应时间 (ms)</span>
            </div>
          </template>
          <div class="card-value">{{ stats.avgLatency }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>AI 错误率</span>
            </div>
          </template>
          <div class="card-value" :class="{ 'text-danger': stats.errorRate > 5 }">
            {{ stats.errorRate }}%
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>活跃 AI 用户数</span>
            </div>
          </template>
          <div class="card-value">{{ stats.activeUsers }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts -->
    <el-row :gutter="20" class="chart-row">
      <!-- 24h Trend -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>24小时请求与错误趋势</span>
            </div>
          </template>
          <v-chart class="chart" :option="trendChartOption" autoresize />
        </el-card>
      </el-col>

      <!-- Token Usage Ranking -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>模块 Token 消耗排行</span>
            </div>
          </template>
          <v-chart class="chart" :option="rankingChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- Latency Distribution -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>耗时分布 (P50, P90, P99)</span>
            </div>
          </template>
          <v-chart class="chart" :option="latencyChartOption" autoresize />
        </el-card>
      </el-col>
      
      <!-- Placeholder for more charts -->
      <el-col :span="12">
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAiDashboardStats } from '@/api/aiMonitor'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const stats = ref({
  todayTokens: 0,
  avgLatency: 0,
  errorRate: 0,
  activeUsers: 0,
  hourlyTrend: [],
  topEndpoints: [],
  latencyDistribution: { p50: 0, p90: 0, p99: 0 }
})

const trendChartOption = computed(() => {
  const hours = Array.from({ length: 24 }, (_, i) => i)
  const requests = new Array(24).fill(0)
  const errors = new Array(24).fill(0)

  if (stats.value.hourlyTrend && stats.value.hourlyTrend.length) {
    stats.value.hourlyTrend.forEach(item => {
      const h = item.hour
      if (h >= 0 && h < 24) {
        requests[h] = item.count
        errors[h] = item.errors
      }
    })
  }

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['请求量', '错误量'] },
    xAxis: { type: 'category', data: hours.map(h => `${h}:00`) },
    yAxis: { type: 'value' },
    series: [
      { name: '请求量', type: 'line', data: requests, smooth: true, itemStyle: { color: '#409EFF' } },
      { name: '错误量', type: 'line', data: errors, smooth: true, itemStyle: { color: '#F56C6C' } }
    ]
  }
})

const rankingChartOption = computed(() => {
  if (!stats.value.topEndpoints || !stats.value.topEndpoints.length) return {}
  
  const endpoints = stats.value.topEndpoints.map(item => (item.endpoint || 'Unknown').replace('Controller', ''))
  const tokens = stats.value.topEndpoints.map(item => item.tokens)

  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '35%', right: '10%' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: endpoints, axisLabel: { interval: 0 } },
    series: [
      { name: 'Token 消耗', type: 'bar', data: tokens, itemStyle: { color: '#67C23A' } }
    ]
  }
})

const latencyChartOption = computed(() => {
  if (!stats.value.latencyDistribution) return {}
  
  return {
    tooltip: { trigger: 'item' },
    xAxis: { type: 'category', data: ['P50', 'P90', 'P99'] },
    yAxis: { type: 'value', name: 'ms' },
    series: [
      {
        type: 'bar',
        data: [
          stats.value.latencyDistribution.p50 || 0,
          stats.value.latencyDistribution.p90 || 0,
          stats.value.latencyDistribution.p99 || 0
        ],
        itemStyle: {
          color: (params) => {
            if (params.dataIndex === 0) return '#409EFF'
            if (params.dataIndex === 1) return '#E6A23C'
            return '#F56C6C'
          }
        },
        label: { show: true, position: 'top' }
      }
    ]
  }
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  try {
    const res = await getAiDashboardStats()
    if (res.data) {
      stats.value = res.data
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取监控数据失败')
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.card-row {
  margin-bottom: 20px;
}
.card-header {
  font-weight: bold;
}
.card-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #303133;
}
.text-danger {
  color: #F56C6C;
}
.chart-row {
  margin-bottom: 20px;
}
.chart {
  height: 300px;
  width: 100%;
}
</style>
