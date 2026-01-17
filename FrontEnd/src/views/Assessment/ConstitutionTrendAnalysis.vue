<template>
  <div class="constitution-trend-analysis">
    <el-page-header @back="goBack" content="体质变化趋势分析" />
    
    <el-card class="trend-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>体质变化趋势分析</span>
          <el-button type="primary" @click="loadTrendData" :disabled="loading">
            更新趋势
          </el-button>
        </div>
      </template>
      
      <div v-if="trendData && trendData.hasData" class="trend-content">
        <!-- 趋势摘要 -->
        <div class="trend-summary">
          <el-alert 
            :title="getChineseSummary(trendData?.summary) || '暂无分析'" 
            :type="trendData?.hasData ? 'success' : 'info'" 
            :closable="false" 
            show-icon 
          />
        </div>

        <!-- AI 洞察分析 -->
        <div v-if="insights" class="ai-insights mb-24">
          <div class="insights-header">
            <el-icon class="ai-icon"><MagicStick /></el-icon>
            <h3>AI 健康洞察</h3>
          </div>
          <el-card shadow="hover" class="insight-card">
            <div class="insight-summary mb-16">
              <strong>{{ getChineseSummary(insights.summary) }}</strong>
            </div>
            <div class="insight-grid">
              <div class="insight-col">
                <h4><el-icon><List /></el-icon> 关键依据</h4>
                <ul>
                  <li v-for="(ev, idx) in insights.evidence" :key="idx">{{ getChineseContent(ev) }}</li>
                </ul>
              </div>
              <div class="insight-col">
                <h4><el-icon><TrendCharts /></el-icon> 改进建议</h4>
                <ul>
                  <li v-for="(sug, idx) in insights.suggestions" :key="idx">{{ sug }}</li>
                </ul>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 趋势图表 -->
        <div class="trend-charts">
          <h3>体质得分变化趋势</h3>
          <div class="chart-container">
            <v-chart :option="trendChartOption" autoresize />
          </div>
        </div>
        
        <!-- 体质变化详情 -->
        <div class="trend-details">
          <h3>体质变化详情</h3>
          <el-table :data="trendItems" style="width: 100%">
            <el-table-column prop="name" label="体质类型" width="120" />
            <el-table-column label="变化趋势" width="120">
              <template #default="{ row }">
                <el-tag :type="getTrendTagType(row.trend)">
                  {{ row.trend }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" />
          </el-table>
        </div>
        
        <!-- 评估记录统计 -->
        <div class="assessment-stats" v-if="trendData?.assessmentCount">
          <h3>评估记录统计</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="评估次数">{{ trendData.assessmentCount }}</el-descriptions-item>
            <el-descriptions-item label="评估时间范围">
              {{ formatDate(trendData.dateRange?.from) }} 至 {{ formatDate(trendData.dateRange?.to) }}
            </el-descriptions-item>
            <el-descriptions-item label="当前主导体质">
              <el-tag type="success">{{ getConstitutionName(trendData.currentPrimaryType) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <div v-else class="no-data">
        <el-empty description="暂无体质趋势数据，请先完成多次体质测评" />
        <el-button type="primary" @click="$router.push('/tcm/assessments')">前往体质测评</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as assessmentApi from '@/api/assessment'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getConstitutionName, getConstitutionColor } from '@/utils/tcm-constants'
import { MagicStick, List, TrendCharts } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const trendData = ref(null)

const goBack = () => {
  router.go(-1)
}

const loadTrendData = async () => {
  try {
    loading.value = true
    const response = await assessmentApi.getConstitutionTrend(90) // 获取最近90天的趋势
    trendData.value = response.data
  } catch (error) {
    ElMessage.error('获取体质趋势数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const getChineseSummary = (text) => {
  if (!text) return ''
  // 替换体质英文代码为中文名
  let result = text
  const codes = [
    'BALANCED', 'QI_DEFICIENCY', 'YANG_DEFICIENCY', 'YIN_DEFICIENCY', 
    'PHLEGM_DAMPNESS', 'DAMP_HEAT', 'BLOOD_STASIS', 'QI_STAGNATION', 'SPECIAL_DIATHESIS'
  ]
  codes.forEach(code => {
    const name = getConstitutionName(code)
    result = result.replace(new RegExp(code, 'g'), name)
  })
  return result
}

const getChineseContent = (text) => {
  if (!text) return ''
  return getChineseSummary(text)
}

const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

const getTrendTagType = (trend) => {
  switch (trend) {
    case '上升':
      return 'danger'
    case '下降':
      return 'primary'
    case '稳定':
      return 'success'
    default:
      return 'info'
  }
}

// 趋势图表选项
const trendChartOption = computed(() => {
  if (!trendData.value || !trendData.value.hasData || !trendData.value.historyScores) {
    return {}
  }
  
  const xAxisData = trendData.value.dates || []
  const seriesData = []
  
  Object.entries(trendData.value.historyScores).forEach(([type, scores]) => {
    seriesData.push({
      name: getConstitutionName(type),
      data: scores,
      color: getConstitutionColor(type)
    })
  })
  
  return {
    title: {
      text: '体质得分变化趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      top: '10%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    series: seriesData.map(item => ({
      name: item.name,
      type: 'line',
      smooth: true,
      data: item.data,
      itemStyle: {
        color: item.color
      }
    }))
  }
})

// 趋势项目计算属性
const trendItems = computed(() => {
  if (!trendData.value || !trendData.value.trends) {
    return []
  }
  
  return Object.entries(trendData.value.trends).map(([type, trend]) => ({
    name: getConstitutionName(type),
    trend: trend,
    description: `${getConstitutionName(type)}体质${trend}趋势`
  }))
})

const insights = computed(() => trendData.value?.insights || null)

onMounted(() => {
  loadTrendData()
})
</script>

<style scoped lang="scss">
@use "sass:map";
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.constitution-trend-analysis {
  padding: 24px;
  min-height: 100%;
  background-color: transparent; /* Use global background */
}

.trend-card {
  margin-top: 20px;
  @include mixins.glass-effect;
  border: vars.$glass-border;
  border-radius: vars.$radius-lg;
  animation: fadeInUp 0.6s vars.$ease-spring;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(0,0,0,0.05);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  span {
    font-size: 18px;
    font-weight: 600;
    color: vars.$text-main-color;
  }
}

.trend-summary {
  margin-bottom: 24px;
  animation: fadeInUp 0.6s vars.$ease-spring 0.1s backwards;
}

.ai-insights {
  margin: 24px 0;
  animation: fadeInUp 0.6s vars.$ease-spring 0.2s backwards;
  
  .insights-header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    color: vars.$primary-color;
    
    .ai-icon {
      margin-right: 8px;
      font-size: 20px;
    }
    
    h3 {
      margin: 0;
      font-size: 16px;
    }
  }
  
  .insight-card {
    background: rgba(255, 255, 255, 0.4);
    border: vars.$glass-border;
    border-radius: vars.$radius-md;
    transition: all 0.3s vars.$ease-spring;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: vars.$shadow-md;
    }
    
    .insight-summary {
      font-size: 15px;
      color: vars.$text-main-color;
      padding-bottom: 12px;
      border-bottom: 1px dashed rgba(0,0,0,0.1);
    }
    
    .insight-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      margin-top: 12px;
      
      .insight-col {
        h4 {
          display: flex;
          align-items: center;
          color: vars.$text-secondary-color;
          margin-bottom: 8px;
          font-size: 14px;
          
          .el-icon {
            margin-right: 4px;
          }
        }
        
        ul {
          padding-left: 20px;
          margin: 0;
          color: vars.$text-regular-color;
          font-size: 13px;
          
          li {
            margin-bottom: 4px;
          }
        }
      }
    }
  }
}

.trend-charts {
  margin: 32px 0;
  animation: fadeInUp 0.6s vars.$ease-spring 0.3s backwards;
  
  h3 {
    color: vars.$text-main-color;
    margin-bottom: 16px;
    font-size: 16px;
  }
}

.chart-container {
  height: 400px;
  padding: 16px;
  background: rgba(255,255,255,0.4);
  border-radius: vars.$radius-lg;
  border: vars.$glass-border;
}

.trend-details {
  margin: 32px 0;
  animation: fadeInUp 0.6s vars.$ease-spring 0.4s backwards;
  
  h3 {
    color: vars.$text-main-color;
    margin-bottom: 16px;
    font-size: 16px;
  }
}

.assessment-stats {
  margin: 32px 0;
  animation: fadeInUp 0.6s vars.$ease-spring 0.5s backwards;
  
  h3 {
    color: vars.$text-main-color;
    margin-bottom: 16px;
    font-size: 16px;
  }
}

.no-data {
  text-align: center;
  padding: 60px 0;
}

.mb-16 { margin-bottom: 16px; }
.mb-24 { margin-bottom: 24px; }
</style>