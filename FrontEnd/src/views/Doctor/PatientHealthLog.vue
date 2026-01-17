<template>
  <div class="page-container">
    <el-page-header @back="goBack" content="患者健康日志" class="mb-24" />

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
                <div class="log-content-wrapper">
                  <p class="content">{{ log.content }}</p>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getLogs, getStatistics } from '../../api/log'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.tz.setDefault('Asia/Shanghai')

const route = useRoute()
const router = useRouter()
const activeTab = ref('diet')
const selectedDate = ref(new Date())
const selectedDay = computed(() => dayjs(selectedDate.value).format('YYYY-MM-DD'))
const targetUserId = computed(() => route.params.userId)
const logs = ref([])
const calendarMarks = ref({})
const trends = ref([])

const trendRef = ref()
let chartInstance = null

const activeTabName = computed(() => {
  const map = { diet: '饮食', sleep: '睡眠', sport: '运动', mood: '情绪', vital: '体征' }
  return map[activeTab.value]
})

function dateKey(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(async () => {
  if (!targetUserId.value) {
    ElMessage.error('未指定患者')
    router.back()
    return
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

async function handleTabChange() {
  await loadLogs()
  await loadTrends()
}

async function handleDateChange() {
  await loadLogs()
}

function goBack() {
  router.back()
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
    }
  } catch (e) {
    console.error('加载日历标记失败:', e)
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
        
        if (l.content && typeof l.content === 'object') {
          time = l.content.time || time
          content = l.content.note || l.content.text || l.content.content || ''
          if (!content) {
            try {
              content = JSON.stringify(l.content)
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
        }
      })
      logs.value = list
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
    
    let series = []
    if (res && res.data) {
      if (res.data.last7Days && res.data.last7Days.typeSeries) {
        series = res.data.last7Days.typeSeries[type] || []
      } else if (res.data.last_7_days && res.data.last_7_days.typeSeries) {
        series = res.data.last_7_days.typeSeries[type] || []
      }
    }
    
    const data = series.map(v => {
      const date = v.date || v.logDate || v.dateStr
      const value = v.average ?? v.averageScore ?? v.value ?? 0
      return { date: dayjs(date).format('MM-DD'), value: Number(value) }
    })
    
    trends.value = data
    
    await nextTick()
    drawTrend()
  } catch (e) {
    console.error('加载趋势失败:', e)
    ElMessage.error('加载趋势失败')
    trends.value = []
    await nextTick()
    drawTrend()
  }
}

function drawTrend() {
  if (!trendRef.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  if (!trends.value || !trends.value.length) {
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
        min: 0
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
  padding: 20px;
  min-height: 100%;
}

.mb-24 { margin-bottom: 24px; }
.mt-16 { margin-top: 16px; }

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
