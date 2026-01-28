<template>
  <div class="health-trend-card">
    <div class="card-header">
      <h3>今日健康概览</h3>
      <div class="legend">
        <span class="legend-item"><span class="dot blue"></span>生命体征</span>
        <span class="legend-item"><span class="dot green"></span>运动状态</span>
        <span class="legend-item"><span class="dot orange"></span>睡眠质量</span>
      </div>
    </div>
    <div class="chart-container" ref="chartRef"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getStatistics } from '@/api/log'
import { useUserStore } from '@/stores/user'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)
const hasData = ref(false)
const userStore = useUserStore()

const initChart = (xData = [], vitals = [], sport = [], sleep = []) => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  
  const option = {
    grid: {
      top: 40,
      right: 20,
      bottom: 20,
      left: 40,
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#f0f0f0',
      textStyle: {
        color: '#333'
      }
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#909399' },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      min: 0,
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#E6EBF5'
        }
      },
      axisLabel: { color: '#909399' }
    },
    series: [
      {
        name: '生命体征',
        type: 'line',
        smooth: true,
        showSymbol: false,
        symbol: 'circle',
        symbolSize: 8,
        data: vitals,
        itemStyle: { color: '#4EA1FF' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(78, 161, 255, 0.2)' },
            { offset: 1, color: 'rgba(78, 161, 255, 0)' }
          ])
        }
      },
      {
        name: '运动状态',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: sport,
        itemStyle: { color: '#35C77A' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(53, 199, 122, 0.2)' },
            { offset: 1, color: 'rgba(53, 199, 122, 0)' }
          ])
        }
      },
      {
        name: '睡眠质量',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: sleep,
        itemStyle: { color: '#FFA866' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 168, 102, 0.2)' },
            { offset: 1, color: 'rgba(255, 168, 102, 0)' }
          ])
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const drawEmpty = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    grid: { left: 40, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value', min: 0 },
    series: [{ data: [], type: 'line', smooth: true }],
    graphic: [{
      type: 'text',
      left: 'center',
      top: 'middle',
      style: { text: '暂无数据', textAlign: 'center', fill: '#999', fontSize: 14 }
    }]
  })
}

const fetchAndRender = async () => {
  loading.value = true
  try {
    const userId = userStore?.profile?.id
    const res = await getStatistics(userId)
    const data = res?.data || {}
    const range = data.last7Days || data.last_7_days
    if (!range || !range.typeSeries) {
      hasData.value = false
      await nextTick()
      drawEmpty()
      return
    }
    const series = range.typeSeries
    const vitalsArr = (series.VITALS || []).map(v => ({
      date: v.date || v.logDate || v.dateStr,
      value: Number(v.average ?? v.averageScore ?? v.value ?? 0)
    }))
    const sportArr = (series.SPORT || []).map(v => ({
      date: v.date || v.logDate || v.dateStr,
      value: Number(v.average ?? v.averageScore ?? v.value ?? 0)
    }))
    const sleepArr = (series.SLEEP || []).map(v => ({
      date: v.date || v.logDate || v.dateStr,
      value: Number(v.average ?? v.averageScore ?? v.value ?? 0)
    }))
    const datesSet = new Set([
      ...vitalsArr.map(d => d.date),
      ...sportArr.map(d => d.date),
      ...sleepArr.map(d => d.date)
    ].filter(Boolean))
    const dates = Array.from(datesSet).sort().map(d => dayjs(d).format('MM-DD'))
    const mapByDate = (arr) => {
      const m = new Map()
      arr.forEach(i => {
        if (!i.date) return
        m.set(dayjs(i.date).format('MM-DD'), i.value)
      })
      return dates.map(d => (m.has(d) ? m.get(d) : null))
    }
    const vitals = mapByDate(vitalsArr)
    const sport = mapByDate(sportArr)
    const sleep = mapByDate(sleepArr)
    hasData.value = dates.length > 0
    await nextTick()
    if (hasData.value) {
      initChart(dates, vitals, sport, sleep)
    } else {
      drawEmpty()
    }
  } catch (e) {
    hasData.value = false
    await nextTick()
    drawEmpty()
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  nextTick(() => {
    fetchAndRender()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;

.health-trend-card {
  background: #fff;
  border-radius: vars.$radius-lg;
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: vars.$shadow-sm;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: vars.$shadow-md;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    margin: 0;
    font-size: 18px;
    color: vars.$text-main-color;
  }
  
  .legend {
    display: flex;
    gap: 16px;
    
    .legend-item {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: vars.$text-secondary-color;
      
      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        margin-right: 6px;
        
        &.blue { background: #4EA1FF; }
        &.green { background: #35C77A; }
        &.orange { background: #FFA866; }
      }
    }
  }
}

.chart-container {
  flex: 1;
  min-height: 250px;
  width: 100%;
}
</style>
