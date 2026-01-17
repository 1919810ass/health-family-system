<template>
  <div class="admin-reports">
    <!-- Header with Staggered Animation -->
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">数据报告</h1>
        <p class="page-subtitle">系统核心数据概览与多维度趋势分析</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="glass-button" v-particles @click="refreshAll">
          <el-icon class="mr-2"><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 数据概览卡片 -->
    <div class="overview-cards stagger-anim" style="--delay: 0.2s">
      <div class="glass-card stat-card">
        <div class="card-icon user-bg">
          <el-icon><User /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">用户总数</div>
          <div class="card-value">12,847</div>
          <div class="card-trend up">
            <el-icon><Top /></el-icon>
            <span>+15.2%</span>
            <span class="trend-label">较上月</span>
          </div>
        </div>
      </div>

      <div class="glass-card stat-card">
        <div class="card-icon family-bg">
          <el-icon><HomeFilled /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">家庭总数</div>
          <div class="card-value">3,245</div>
          <div class="card-trend up">
            <el-icon><Top /></el-icon>
            <span>+8.7%</span>
            <span class="trend-label">较上月</span>
          </div>
        </div>
      </div>

      <div class="glass-card stat-card">
        <div class="card-icon log-bg">
          <el-icon><Files /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">健康记录</div>
          <div class="card-value">89,567</div>
          <div class="card-trend up">
            <el-icon><Top /></el-icon>
            <span>+22.3%</span>
            <span class="trend-label">较上月</span>
          </div>
        </div>
      </div>

      <div class="glass-card stat-card">
        <div class="card-icon doctor-bg">
          <el-icon><FirstAidKit /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">医生数量</div>
          <div class="card-value">127</div>
          <div class="card-trend up">
            <el-icon><Top /></el-icon>
            <span>+5.8%</span>
            <span class="trend-label">较上月</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据趋势分析 -->
    <div class="glass-card mb-24 stagger-anim" style="--delay: 0.3s">
      <div class="card-header-wrapper">
        <div class="card-title">
          <el-icon class="mr-2"><TrendCharts /></el-icon>
          数据趋势分析
        </div>
        <div class="header-actions">
          <el-date-picker
            v-model="trendTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :prefix-icon="Calendar"
            class="glass-input"
            @change="refreshTrendData"
          />
        </div>
      </div>
      
      <div class="charts-grid">
        <div class="chart-wrapper">
          <div ref="newUserChartRef" class="chart"></div>
        </div>
        <div class="chart-wrapper">
          <div ref="featureUsageChartRef" class="chart"></div>
        </div>
        <div class="chart-wrapper">
          <div ref="healthLogChartRef" class="chart"></div>
        </div>
        <div class="chart-wrapper">
          <div ref="familyGrowthChartRef" class="chart"></div>
        </div>
      </div>
    </div>

    <el-row :gutter="24" class="stagger-anim" style="--delay: 0.4s">
      <!-- 数据质量报告 -->
      <el-col :span="24" :lg="14">
        <div class="glass-card h-100">
          <div class="card-header-wrapper">
            <div class="card-title">
              <el-icon class="mr-2"><DataAnalysis /></el-icon>
              数据质量报告
            </div>
            <div class="header-actions">
              <el-button link type="primary" @click="generateQualityReport" :loading="qualityLoading">
                <el-icon class="mr-1"><Refresh /></el-icon>重新生成
              </el-button>
            </div>
          </div>
          
          <el-table :data="qualityReport" style="width: 100%" v-loading="qualityLoading" class="glass-table">
            <el-table-column prop="metric" label="指标" width="150" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="value" label="当前值" width="100" align="right">
              <template #default="{ row }">
                <span class="font-mono">{{ row.value }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getQualityStatusTagType(row.status)" effect="light" round size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="viewQualityDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 报告导出 -->
      <el-col :span="24" :lg="10">
        <div class="glass-card h-100">
          <div class="card-header-wrapper">
            <div class="card-title">
              <el-icon class="mr-2"><Download /></el-icon>
              报告导出
            </div>
          </div>
          
          <div class="export-form-container">
            <el-form :model="exportForm" label-position="top" class="glass-form">
              <el-form-item label="报告类型">
                <el-select v-model="exportForm.type" placeholder="请选择报告类型" class="w-100">
                  <template #prefix><el-icon><Document /></el-icon></template>
                  <el-option label="用户统计报告" value="user" />
                  <el-option label="健康数据报告" value="health" />
                  <el-option label="系统使用报告" value="system" />
                  <el-option label="综合分析报告" value="comprehensive" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="exportForm.timeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  class="w-100"
                  :prefix-icon="Calendar"
                />
              </el-form-item>
              
              <el-form-item label="报告格式">
                <div class="format-selector">
                  <div 
                    class="format-item" 
                    :class="{ active: exportForm.format === 'pdf' }"
                    @click="exportForm.format = 'pdf'"
                  >
                    <span class="format-icon">PDF</span>
                    <span class="format-name">Adobe PDF</span>
                  </div>
                  <div 
                    class="format-item" 
                    :class="{ active: exportForm.format === 'excel' }"
                    @click="exportForm.format = 'excel'"
                  >
                    <span class="format-icon excel">XLS</span>
                    <span class="format-name">Excel 表格</span>
                  </div>
                  <div 
                    class="format-item" 
                    :class="{ active: exportForm.format === 'csv' }"
                    @click="exportForm.format = 'csv'"
                  >
                    <span class="format-icon csv">CSV</span>
                    <span class="format-name">CSV 数据</span>
                  </div>
                </div>
              </el-form-item>
              
              <div class="form-actions mt-4">
                <el-button type="primary" class="w-100" size="large" v-particles @click="generateExport" :loading="exportLoading">
                  <el-icon class="mr-2"><Download /></el-icon>
                  立即导出
                </el-button>
              </div>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { 
  User, HomeFilled, Files, FirstAidKit, Top, Bottom, 
  TrendCharts, Calendar, DataAnalysis, Refresh, 
  Download, Document
} from '@element-plus/icons-vue'
import { getDataReports, getQualityReport } from '../../../api/admin'

// 图表引用
const newUserChartRef = ref(null)
const featureUsageChartRef = ref(null)
const healthLogChartRef = ref(null)
const familyGrowthChartRef = ref(null)

// 时间范围
const trendTimeRange = ref([
  dayjs().subtract(30, 'day').toDate(),
  dayjs().toDate()
])

// 加载状态
const trendLoading = ref(false)
const qualityLoading = ref(false)
const exportLoading = ref(false)

// 数据质量报告
const qualityReport = ref([])

// 导出表单
const exportForm = ref({
  type: 'comprehensive',
  timeRange: [
    dayjs().subtract(30, 'day').toDate(),
    dayjs().toDate()
  ],
  format: 'pdf'
})

// 图表实例
let newUserChart = null
let featureUsageChart = null
let healthLogChart = null
let familyGrowthChart = null

// 初始化图表
const initCharts = () => {
  const commonOption = {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#E4E7ED',
      textStyle: { color: '#1F2937' }
    }
  }

  // 新用户增长趋势
  if (newUserChartRef.value) {
    newUserChart = echarts.init(newUserChartRef.value)
    newUserChart.setOption({
      ...commonOption,
      title: { text: '新增用户趋势', left: 'center', textStyle: { fontSize: 14 } },
      xAxis: { type: 'category', data: ['1号', '5号', '10号', '15号', '20号', '25号', '30号'], axisLine: { show: false }, axisTick: { show: false } },
      yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#eee' } } },
      series: [{
        data: [120, 132, 101, 134, 90, 230, 210],
        type: 'line',
        smooth: true,
        symbol: 'none',
        areaStyle: {
          opacity: 0.2,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#7B61FF' },
            { offset: 1, color: 'rgba(123, 97, 255, 0)' }
          ])
        },
        itemStyle: { color: '#7B61FF' }
      }]
    })
  }

  // 功能使用统计
  if (featureUsageChartRef.value) {
    featureUsageChart = echarts.init(featureUsageChartRef.value)
    featureUsageChart.setOption({
      ...commonOption,
      title: { text: '功能使用统计', left: 'center', textStyle: { fontSize: 14 } },
      xAxis: { type: 'category', data: ['健康日志', '家庭', '医生', '建议', '提醒', '测评'], axisLine: { show: false }, axisTick: { show: false } },
      yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#eee' } } },
      series: [{
        data: [3200, 2800, 2100, 1800, 1500, 1200],
        type: 'bar',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00D2BA' },
            { offset: 1, color: '#48E5C2' }
          ])
        }
      }]
    })
  }

  // 健康记录趋势
  if (healthLogChartRef.value) {
    healthLogChart = echarts.init(healthLogChartRef.value)
    healthLogChart.setOption({
      ...commonOption,
      title: { text: '健康记录趋势', left: 'center', textStyle: { fontSize: 14 } },
      xAxis: { type: 'category', data: ['1号', '5号', '10号', '15号', '20号', '25号', '30号'], axisLine: { show: false }, axisTick: { show: false } },
      yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#eee' } } },
      series: [{
        data: [2400, 1398, 9800, 3908, 4800, 3800, 4300],
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 3 },
        itemStyle: { color: '#FFB84C' }
      }]
    })
  }

  // 家庭增长趋势
  if (familyGrowthChartRef.value) {
    familyGrowthChart = echarts.init(familyGrowthChartRef.value)
    familyGrowthChart.setOption({
      ...commonOption,
      title: { text: '家庭增长趋势', left: 'center', textStyle: { fontSize: 14 } },
      xAxis: { type: 'category', data: ['1号', '5号', '10号', '15号', '20号', '25号', '30号'], axisLine: { show: false }, axisTick: { show: false } },
      yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#eee' } } },
      series: [{
        data: [24, 13, 98, 39, 48, 38, 43],
        type: 'line',
        smooth: true,
        symbol: 'none',
        areaStyle: {
          opacity: 0.2,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF5C5C' },
            { offset: 1, color: 'rgba(255, 92, 92, 0)' }
          ])
        },
        itemStyle: { color: '#FF5C5C' }
      }]
    })
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  trendLoading.value = true
  try {
    // 这里应该调用实际的API
    await new Promise(resolve => setTimeout(resolve, 500))
  } catch (error) {
    ElMessage.error('加载趋势数据失败')
  } finally {
    trendLoading.value = false
  }
}

// 加载质量报告
const loadQualityReport = async () => {
  qualityLoading.value = true
  try {
    const response = await getQualityReport()
    qualityReport.value = response.data || []
  } catch (error) {
    ElMessage.error('加载质量报告失败')
  } finally {
    qualityLoading.value = false
  }
}

// 刷新所有数据
const refreshAll = () => {
  loadTrendData()
  loadQualityReport()
  ElMessage.success('数据已刷新')
}

// 刷新趋势数据
const refreshTrendData = () => {
  loadTrendData()
}

// 生成质量报告
const generateQualityReport = () => {
  loadQualityReport()
}

// 获取质量状态标签类型
const getQualityStatusTagType = (status) => {
  switch (status) {
    case '优秀': return 'success'
    case '良好': return 'primary'
    case '一般': return 'warning'
    case '较差': return 'danger'
    default: return 'info'
  }
}

// 查看质量详情
const viewQualityDetail = (item) => {
  ElMessage.info(`查看${item.metric}详情`)
}

// 导出报告
const exportReport = async (format) => {
  exportLoading.value = true
  try {
    await ElMessageBox.confirm(
      `确定要导出${format === 'pdf' ? 'PDF' : 'Excel'}格式的报告吗？`,
      '导出报告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(`报告已导出为${format === 'pdf' ? 'PDF' : 'Excel'}格式`)
  } catch {
    // 用户取消操作
  } finally {
    exportLoading.value = false
  }
}

// 生成导出
const generateExport = () => {
  exportReport(exportForm.value.format)
}

// 重置导出表单
const resetExportForm = () => {
  exportForm.value = {
    type: 'comprehensive',
    timeRange: [dayjs().subtract(30, 'day').toDate(), dayjs().toDate()],
    format: 'pdf'
  }
}

// 监听窗口大小变化
const handleResize = () => {
  newUserChart?.resize()
  featureUsageChart?.resize()
  healthLogChart?.resize()
  familyGrowthChart?.resize()
}

// 初始化
onMounted(() => {
  initCharts()
  loadTrendData()
  loadQualityReport()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理图表
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (newUserChart) newUserChart.dispose()
  if (featureUsageChart) featureUsageChart.dispose()
  if (healthLogChart) healthLogChart.dispose()
  if (familyGrowthChart) familyGrowthChart.dispose()
})
</script>

<style scoped lang="scss">
@use 'sass:map';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.admin-reports {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
    padding: 0 4px;

    .header-content {
      .page-title {
        font-size: 28px;
        font-weight: 800;
        margin: 0 0 8px 0;
        @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info')));
        letter-spacing: -0.5px;
      }

      .page-subtitle {
        font-size: 14px;
        color: map.get(vars.$colors, 'text-secondary');
        margin: 0;
      }
    }
  }

  .glass-button {
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.6);
    color: map.get(vars.$colors, 'primary');
    font-weight: 600;
    
    &:hover {
      background: map.get(vars.$colors, 'primary');
      color: white;
      border-color: map.get(vars.$colors, 'primary');
    }
  }

  .overview-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 24px;
    margin-bottom: 32px;

    .stat-card {
      display: flex;
      align-items: center;
      padding: 24px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        transform: translateY(-4px);
      }

      .card-icon {
        width: 64px;
        height: 64px;
        border-radius: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        margin-right: 20px;
        flex-shrink: 0;

        &.user-bg { background: linear-gradient(135deg, rgba(map.get(vars.$colors, 'primary'), 0.1), rgba(map.get(vars.$colors, 'primary'), 0.2)); color: map.get(vars.$colors, 'primary'); }
        &.family-bg { background: linear-gradient(135deg, rgba(map.get(vars.$colors, 'success'), 0.1), rgba(map.get(vars.$colors, 'success'), 0.2)); color: map.get(vars.$colors, 'success'); }
        &.log-bg { background: linear-gradient(135deg, rgba(map.get(vars.$colors, 'warning'), 0.1), rgba(map.get(vars.$colors, 'warning'), 0.2)); color: map.get(vars.$colors, 'warning'); }
        &.doctor-bg { background: linear-gradient(135deg, rgba(map.get(vars.$colors, 'danger'), 0.1), rgba(map.get(vars.$colors, 'danger'), 0.2)); color: map.get(vars.$colors, 'danger'); }
      }

      .card-content {
        flex: 1;

        .card-label {
          font-size: 14px;
          color: map.get(vars.$colors, 'text-secondary');
          margin-bottom: 4px;
        }

        .card-value {
          font-size: 28px;
          font-weight: 800;
          color: map.get(vars.$colors, 'text-main');
          line-height: 1.2;
          margin-bottom: 4px;
        }

        .card-trend {
          display: flex;
          align-items: center;
          font-size: 12px;
          font-weight: 600;
          gap: 4px;

          &.up { color: map.get(vars.$colors, 'success'); }
          &.down { color: map.get(vars.$colors, 'danger'); }

          .trend-label {
            color: map.get(vars.$colors, 'text-secondary');
            font-weight: 400;
            margin-left: 4px;
          }
        }
      }
    }
  }

  .glass-card {
    @include mixins.glass-effect;
    border-radius: 24px;
    padding: 24px;
    margin-bottom: 24px;
    border: 1px solid rgba(255, 255, 255, 0.4);
    box-shadow: 0 8px 32px rgba(31, 38, 135, 0.05);

    &.h-100 {
      height: calc(100% - 24px);
    }
  }

  .card-header-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .card-title {
      font-size: 18px;
      font-weight: 700;
      color: map.get(vars.$colors, 'text-main');
      display: flex;
      align-items: center;
    }
  }

  .charts-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
    gap: 24px;

    .chart-wrapper {
      background: rgba(255, 255, 255, 0.5);
      border-radius: 16px;
      padding: 16px;
      border: 1px solid rgba(255, 255, 255, 0.2);

      .chart {
        height: 300px;
        width: 100%;
      }
    }
  }

  .glass-table {
    background: transparent;
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: transparent;
    
    :deep(th.el-table__cell) {
      background-color: rgba(map.get(vars.$colors, 'primary'), 0.05);
      color: map.get(vars.$colors, 'text-main');
      font-weight: 700;
      border-bottom: none;
    }

    :deep(td.el-table__cell) {
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    }
    
    .font-mono {
      font-family: 'JetBrains Mono', monospace;
      font-weight: 600;
    }
  }

  .export-form-container {
    padding: 8px;

    .glass-form {
      :deep(.el-form-item__label) {
        font-weight: 600;
        color: map.get(vars.$colors, 'text-main');
      }
    }

    .format-selector {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
      width: 100%;

      .format-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 16px;
        border-radius: 12px;
        border: 2px solid rgba(0, 0, 0, 0.05);
        cursor: pointer;
        transition: all 0.2s;
        background: rgba(255, 255, 255, 0.5);

        &:hover {
          border-color: rgba(map.get(vars.$colors, 'primary'), 0.3);
          background: rgba(255, 255, 255, 0.8);
        }

        &.active {
          border-color: map.get(vars.$colors, 'primary');
          background: rgba(map.get(vars.$colors, 'primary'), 0.05);
          
          .format-icon {
            transform: scale(1.1);
          }
        }

        .format-icon {
          font-size: 14px;
          font-weight: 800;
          color: #FF5C5C; // PDF color
          margin-bottom: 8px;
          transition: transform 0.2s;
          
          &.excel { color: #217346; } // Excel color
          &.csv { color: #2D3047; }
        }

        .format-name {
          font-size: 12px;
          color: map.get(vars.$colors, 'text-secondary');
        }
      }
    }
  }

  // Staggered Animation
  .stagger-anim {
    opacity: 0;
    animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    animation-delay: var(--delay, 0s);
  }

  @keyframes slideUpFade {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}
</style>