<template>
  <div class="custom-reports">
    <!-- Header with Staggered Animation -->
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">自定义报告</h1>
        <p class="page-subtitle">灵活配置并生成多维度系统数据分析报告</p>
      </div>
    </div>

    <el-row :gutter="24">
      <!-- Config Panel -->
      <el-col :span="24" :lg="8" class="stagger-anim" style="--delay: 0.2s">
        <el-card class="glass-card config-panel">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon class="mr-2"><Setting /></el-icon>
                报告配置
              </span>
            </div>
          </template>
          
          <el-form :model="reportConfig" label-width="80px" label-position="top" class="custom-form">
            <el-form-item label="报告名称">
              <el-input 
                v-model="reportConfig.name" 
                placeholder="请输入报告名称"
              >
                <template #prefix>
                  <el-icon><Document /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="reportConfig.timeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%;"
                :prefix-icon="Calendar"
              />
            </el-form-item>
            
            <el-form-item label="数据维度">
              <el-checkbox-group v-model="reportConfig.dimensions" class="custom-checkbox-group">
                <el-checkbox label="users" border>用户数据</el-checkbox>
                <el-checkbox label="families" border>家庭数据</el-checkbox>
                <el-checkbox label="healthLogs" border>健康日志</el-checkbox>
                <el-checkbox label="doctors" border>医生数据</el-checkbox>
                <el-checkbox label="reminders" border>健康提醒</el-checkbox>
                <el-checkbox label="assessments" border>体质测评</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="图表类型">
              <el-radio-group v-model="reportConfig.chartType" class="custom-radio-group">
                <el-radio-button label="line">
                  <el-icon><TrendCharts /></el-icon> 折线图
                </el-radio-button>
                <el-radio-button label="bar">
                  <el-icon><Histogram /></el-icon> 柱状图
                </el-radio-button>
                <el-radio-button label="pie">
                  <el-icon><PieChart /></el-icon> 饼图
                </el-radio-button>
                <el-radio-button label="area">
                  <el-icon><DataAnalysis /></el-icon> 面积图
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="报告模板">
              <el-select v-model="reportConfig.template" placeholder="选择模板" style="width: 100%;">
                <template #prefix>
                  <el-icon><Collection /></el-icon>
                </template>
                <el-option label="默认模板" value="default" />
                <el-option label="用户分析" value="userAnalysis" />
                <el-option label="健康趋势" value="healthTrend" />
                <el-option label="系统使用" value="systemUsage" />
              </el-select>
            </el-form-item>
            
            <div class="form-actions">
              <el-button type="primary" round v-particles @click="generateReport" :loading="generating" class="w-100 mb-12">
                <el-icon class="mr-2"><VideoPlay /></el-icon>
                生成报告
              </el-button>
              <div class="flex-row">
                <el-button round @click="saveTemplate" class="flex-1">保存模板</el-button>
                <el-button round @click="resetConfig" class="flex-1">重置</el-button>
              </div>
            </div>
          </el-form>
        </el-card>
        
        <el-card class="glass-card saved-templates mt-24 stagger-anim" style="--delay: 0.3s">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon class="mr-2"><Files /></el-icon>
                已保存模板
              </span>
            </div>
          </template>
          
          <el-table :data="savedTemplates" style="width: 100%" class="glass-table">
            <el-table-column prop="name" label="模板名称" />
            <el-table-column prop="updatedAt" label="更新时间" width="100">
              <template #default="{ row }">
                <span class="text-xs text-secondary">{{ row.updatedAt.split(' ')[0] }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="loadTemplate(row)">加载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <!-- Report Preview -->
      <el-col :span="24" :lg="16" class="stagger-anim" style="--delay: 0.3s">
        <el-card class="glass-card preview-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon class="mr-2"><Monitor /></el-icon>
                报告预览
              </span>
              <div class="header-actions">
                <el-button type="primary" link @click="exportReport" :loading="exporting">
                  <el-icon class="mr-1"><Download /></el-icon> 导出
                </el-button>
                <el-button link @click="refreshPreview">
                  <el-icon class="mr-1"><Refresh /></el-icon> 刷新
                </el-button>
              </div>
            </div>
          </template>
          
          <div v-if="reportData.length > 0" class="report-content">
            <div class="chart-wrapper glass-panel">
              <div ref="chartContainerRef" class="chart-container">
                <div ref="chartRef" class="chart" style="height: 400px;"></div>
              </div>
            </div>
            
            <div class="report-table mt-24">
              <h3 class="section-title">详细数据</h3>
              <el-table :data="reportData" style="width: 100%" class="glass-table" border>
                <el-table-column prop="name" label="指标">
                  <template #default="{ row }">
                    <div class="indicator-cell">
                      <span class="indicator-dot"></span>
                      {{ row.name }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="value" label="数值" align="right">
                  <template #default="{ row }">
                    <span class="value-text">{{ row.value }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="trend" label="趋势" align="center">
                  <template #default="{ row }">
                    <el-tag :type="getTrendType(row.trend)" effect="light" round size="small">
                      {{ row.trend }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          
          <div v-else class="no-data">
            <el-empty description="请配置参数并点击生成报告" :image-size="200" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { 
  Document, Calendar, Setting, TrendCharts, Histogram, 
  PieChart, DataAnalysis, Collection, VideoPlay, Files, 
  Monitor, Download, Refresh 
} from '@element-plus/icons-vue'
import { getCustomReport, saveReportTemplate, getSavedTemplates } from '../../../api/admin'

// 图表引用
const chartRef = ref(null)
const chartContainerRef = ref(null)

// 配置数据
const reportConfig = ref({
  name: '',
  timeRange: [
    dayjs().subtract(30, 'day').toDate(),
    dayjs().toDate()
  ],
  dimensions: ['users', 'healthLogs'],
  chartType: 'line',
  template: 'default'
})

// 状态
const generating = ref(false)
const exporting = ref(false)

// 报告数据
const reportData = ref([])
const savedTemplates = ref([])

// 图表实例
let chart = null

// 初始化图表
const initChart = () => {
  if (chartRef.value) {
    chart = echarts.init(chartRef.value)
    updateChart()
  }
}

// 更新图表
const updateChart = () => {
  if (!chart) return
  
  let option = {}
  
  if (reportConfig.value.chartType === 'line') {
    option = {
      title: {
        text: reportConfig.value.name || '自定义报告',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: ['1号', '5号', '10号', '15号', '20号', '25号', '30号']
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: [120, 132, 101, 134, 90, 230, 210],
        type: 'line',
        smooth: true
      }]
    }
  } else if (reportConfig.value.chartType === 'bar') {
    option = {
      title: {
        text: reportConfig.value.name || '自定义报告',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: ['用户', '家庭', '日志', '医生', '提醒', '测评']
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: [1200, 2000, 3600, 1800, 1500, 1200],
        type: 'bar'
      }]
    }
  } else if (reportConfig.value.chartType === 'pie') {
    option = {
      title: {
        text: reportConfig.value.name || '自定义报告',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      series: [{
        name: '占比',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 1048, name: '用户' },
          { value: 735, name: '家庭' },
          { value: 580, name: '日志' },
          { value: 484, name: '医生' },
          { value: 300, name: '提醒' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
  }
  
  chart.setOption(option, true)
}

// 生成报告
const generateReport = async () => {
  generating.value = true
  try {
    const response = await getCustomReport(reportConfig.value)
    reportData.value = response.data?.reportData || []
    
    // 更新图表
    updateChart()
    
    ElMessage.success('报告生成成功')
  } catch (error) {
    ElMessage.error('报告生成失败')
  } finally {
    generating.value = false
  }
}

// 保存模板
const saveTemplate = async () => {
  try {
    await ElMessageBox.prompt('请输入模板名称', '保存模板', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '模板名称不能为空'
    }).then(async ({ value }) => {
      const template = {
        name: value,
        config: { ...reportConfig.value },
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      
      // 这里应该调用实际的API
      // 目前使用模拟数据
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // 添加到本地模板列表
      savedTemplates.value.push(template)
      
      ElMessage.success('模板保存成功')
    })
  } catch {
    // 用户取消操作
  }
}

// 加载模板
const loadTemplate = (template) => {
  reportConfig.value = { ...template.config }
  ElMessage.success('模板加载成功')
}

// 刷新预览
const refreshPreview = () => {
  if (reportData.value.length > 0) {
    updateChart()
  }
}

// 导出报告
const exportReport = async () => {
  exporting.value = true
  try {
    await ElMessageBox.confirm(
      '确定要导出当前报告吗？',
      '导出报告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // 实际导出API调用
    const params = new URLSearchParams({
      ...reportConfig.value,
      format: 'pdf'
    }).toString()
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = `/api/admin/monitoring/custom-reports/export?${params}`
    link.download = 'report.pdf'
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('报告已导出')
  } catch {
    // 用户取消操作
  } finally {
    exporting.value = false
  }
}

// 重置配置
const resetConfig = () => {
  reportConfig.value = {
    name: '',
    timeRange: [
      dayjs().subtract(30, 'day').toDate(),
      dayjs().toDate()
    ],
    dimensions: ['users', 'healthLogs'],
    chartType: 'line',
    template: 'default'
  }
  reportData.value = []
}

// 加载已保存模板
const loadSavedTemplates = async () => {
  try {
    // 这里应该调用实际的API
    // 目前使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    savedTemplates.value = [
      { name: '用户增长分析', updatedAt: '2024-01-15 10:30:22' },
      { name: '健康趋势报告', updatedAt: '2024-01-14 15:45:18' },
      { name: '系统使用统计', updatedAt: '2024-01-13 09:20:33' }
    ]
  } catch (error) {
    ElMessage.error('加载模板失败')
  }
}

// 监听配置变化，自动更新图表
watch(reportConfig, (newVal, oldVal) => {
  if (newVal.chartType !== oldVal.chartType && reportData.value.length > 0) {
    updateChart()
  }
}, { deep: true })

// 辅助函数：获取趋势标签类型
const getTrendType = (trend) => {
  if (trend === 'up' || trend > 0 || String(trend).includes('+')) return 'success'
  if (trend === 'down' || trend < 0 || String(trend).includes('-')) return 'danger'
  return 'info'
}

// 初始化
onMounted(() => {
  initChart()
  loadSavedTemplates()
})

// 组件卸载时清理图表
onUnmounted(() => {
  if (chart) chart.dispose()
})
</script>

<style scoped lang="scss">
@use 'sass:map';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.custom-reports {
  .page-header {
    margin-bottom: 24px;
    padding: 24px;
    background: linear-gradient(135deg, rgba(map.get(vars.$colors, 'primary'), 0.1) 0%, rgba(255, 255, 255, 0) 100%);
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.6);
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10px);
      z-index: -1;
    }

    .header-content {
      position: relative;
      z-index: 1;

      .page-title {
        font-size: 28px;
        font-weight: 800;
        margin: 0 0 8px;
        @include mixins.text-gradient(linear-gradient(45deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'success')));
        letter-spacing: -0.5px;
      }

      .page-subtitle {
        font-size: 14px;
        color: map.get(vars.$colors, 'text-secondary');
        margin: 0;
        opacity: 0.8;
      }
    }
  }

  .glass-card {
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 16px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    margin-bottom: 24px;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.05);
      border-color: rgba(255, 255, 255, 0.5);
    }

    :deep(.el-card__header) {
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      padding: 16px 20px;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .header-title {
          font-size: 16px;
          font-weight: 700;
          color: map.get(vars.$colors, 'text-main');
          display: flex;
          align-items: center;
        }

        .header-actions {
          display: flex;
          gap: 12px;
        }
      }
    }
  }

  .custom-form {
    padding: 10px;

    :deep(.el-form-item__label) {
      font-weight: 600;
      color: map.get(vars.$colors, 'text-main');
    }

    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper) {
      box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.1) inset;
      border-radius: 8px;
      transition: all 0.2s;
      
      &:hover, &.is-focus {
        box-shadow: 0 0 0 1px map.get(vars.$colors, 'primary') inset;
      }
    }

    .custom-checkbox-group {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      :deep(.el-checkbox) {
        margin-right: 0;
        border-radius: 8px;
        
        &.is-bordered.is-checked {
          background-color: rgba(map.get(vars.$colors, 'primary'), 0.1);
          border-color: map.get(vars.$colors, 'primary');
        }
      }
    }

    .custom-radio-group {
      :deep(.el-radio-button__inner) {
        border-radius: 0;
        display: flex;
        align-items: center;
        gap: 4px;
        
        .el-icon {
          margin-right: 2px;
        }
      }
      
      :deep(.el-radio-button:first-child .el-radio-button__inner) {
        border-radius: 8px 0 0 8px;
      }
      
      :deep(.el-radio-button:last-child .el-radio-button__inner) {
        border-radius: 0 8px 8px 0;
      }
    }

    .form-actions {
      margin-top: 24px;
      
      .w-100 {
        width: 100%;
      }
      
      .flex-row {
        display: flex;
        gap: 12px;
      }
      
      .flex-1 {
        flex: 1;
      }
      
      .mb-12 {
        margin-bottom: 12px;
      }
    }
  }

  .glass-table {
    background: transparent;
    
    :deep(th.el-table__cell) {
      background-color: rgba(map.get(vars.$colors, 'primary'), 0.05);
      color: map.get(vars.$colors, 'text-main');
      font-weight: 600;
    }
    
    :deep(tr) {
      background-color: transparent;
      
      &:hover > td.el-table__cell {
        background-color: rgba(map.get(vars.$colors, 'primary'), 0.05);
      }
    }
    
    :deep(td.el-table__cell) {
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    }

    .text-secondary {
      color: map.get(vars.$colors, 'text-secondary');
    }
  }

  .preview-card {
    min-height: 600px;
    
    .chart-wrapper {
      background: white;
      border-radius: 12px;
      padding: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
      border: 1px solid rgba(0, 0, 0, 0.05);
      
      .chart-container {
        width: 100%;
        overflow: hidden;
      }
    }
    
    .section-title {
      font-size: 16px;
      font-weight: 700;
      margin: 0 0 16px;
      color: map.get(vars.$colors, 'text-main');
      display: flex;
      align-items: center;
      
      &::before {
        content: '';
        display: inline-block;
        width: 4px;
        height: 16px;
        background: map.get(vars.$colors, 'primary');
        margin-right: 8px;
        border-radius: 2px;
      }
    }
    
    .indicator-cell {
      display: flex;
      align-items: center;
      font-weight: 500;
      
      .indicator-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background-color: map.get(vars.$colors, 'primary');
        margin-right: 8px;
      }
    }
    
    .value-text {
      font-family: monospace;
      font-weight: 600;
      color: map.get(vars.$colors, 'text-main');
    }
  }

  .no-data {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
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