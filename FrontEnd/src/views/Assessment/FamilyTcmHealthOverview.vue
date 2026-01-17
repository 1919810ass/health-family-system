<template>
  <div class="family-tcm-health-overview">
    <el-page-header @back="goBack" content="家庭中医健康概览" class="page-header" />
    
    <el-card class="overview-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>{{ familyName }} - 中医健康概览</span>
          <el-button type="primary" @click="loadOverview" :disabled="loading" class="action-btn">
            刷新数据
          </el-button>
        </div>
      </template>
      
      <div v-if="overview" class="overview-content">
        <!-- 家庭基本信息 -->
        <div class="family-info animate-item">
          <el-descriptions :column="3" border class="glass-descriptions">
            <el-descriptions-item label="家庭名称">{{ overview.familyName }}</el-descriptions-item>
            <el-descriptions-item label="成员数量">{{ overview.memberCount }}</el-descriptions-item>
            <el-descriptions-item label="生成日期">{{ formatDate(overview.generatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 体质分布统计 -->
        <div class="constitution-distribution animate-item" style="animation-delay: 0.1s">
          <h3>家庭体质分布</h3>
          <el-row :gutter="20">
            <el-col :span="16" :xs="24">
              <div class="chart-container glass-panel">
                <v-chart :option="chartOption" autoresize />
              </div>
            </el-col>
            <el-col :span="8" :xs="24" class="mt-xs-20">
              <el-card class="stat-card glass-panel">
                <template #header>
                  <span>体质统计</span>
                </template>
                <div v-for="(count, type) in overview.constitutionDistribution" :key="type" class="stat-item">
                  <span class="type-name">{{ getConstitutionName(type) }}</span>
                  <el-progress :percentage="getPercentage(count, overview.memberCount)" :color="getProgressColor(type)" />
                  <span class="count">{{ count }}人</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <!-- 家庭成员详情 -->
        <div class="members-section animate-item" style="animation-delay: 0.2s">
          <h3>家庭成员详情</h3>
          <el-table :data="overview.members" style="width: 100%" class="glass-table">
            <el-table-column prop="userName" label="姓名" width="120" />
            <el-table-column prop="relationship" label="关系" width="120" />
            <el-table-column label="主导体质" width="120">
              <template #default="{ row }">
                <el-tag :type="getConstitutionTagType(row.primaryConstitution)" v-if="row.hasConstitutionData">
                  {{ row.primaryConstitution ? getConstitutionName(row.primaryConstitution) : '未测评' }}
                </el-tag>
                <span v-else>未测评</span>
              </template>
            </el-table-column>
            <el-table-column label="体质得分" min-width="200">
              <template #default="{ row }">
                <div v-if="row.hasConstitutionData" class="scores-container">
                  <el-progress 
                    v-for="(score, type) in row.scores" 
                    :key="type" 
                    :percentage="Math.round(score)" 
                    :color="getScoreColor(type)"
                    :stroke-width="8"
                    :format="() => getConstitutionShortName(type)"
                  />
                </div>
                <span v-else>暂无数据</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button 
                  size="small" 
                  :disabled="!row.hasConstitutionData"
                  @click="viewMemberPlan(row.userId)"
                  type="primary"
                  plain
                >
                  查看方案
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 家庭健康建议 -->
        <div class="family-recommendation animate-item" style="animation-delay: 0.3s">
          <h3>家庭健康建议</h3>
          <el-alert :title="overview.familyRecommendation" type="info" :closable="false" show-icon class="glass-alert" />
        </div>
      </div>
      
      <div v-else class="no-data">
        <el-empty description="暂无家庭健康数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as assessmentApi from '@/api/assessment'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const overview = ref(null)
const familyId = ref(null)

// 从路由参数获取家庭ID
familyId.value = parseInt(route.params.familyId)

const goBack = () => {
  router.go(-1)
}

const loadOverview = async () => {
  if (!familyId.value) {
    ElMessage.error('未指定家庭ID')
    return
  }
  
  try {
    loading.value = true
    const response = await assessmentApi.getFamilyTcmHealthOverview(familyId.value)
    overview.value = response.data
  } catch (error) {
    ElMessage.error('获取家庭健康概览失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const getConstitutionName = (code) => {
  const names = {
    'QI_DEFICIENCY': '气虚质',
    'YANG_DEFICIENCY': '阳虚质',
    'YIN_DEFICIENCY': '阴虚质',
    'PHLEGM_DAMPNESS': '痰湿质',
    'DAMP_HEAT': '湿热质',
    'BLOOD_STASIS': '血瘀质',
    'QI_STAGNATION': '气郁质',
    'SPECIAL_DIATHESIS': '特禀质',
    'BALANCED': '平和质'
  }
  return names[code] || code
}

const getConstitutionShortName = (code) => {
  const names = {
    'QI_DEFICIENCY': '气虚',
    'YANG_DEFICIENCY': '阳虚',
    'YIN_DEFICIENCY': '阴虚',
    'PHLEGM_DAMPNESS': '痰湿',
    'DAMP_HEAT': '湿热',
    'BLOOD_STASIS': '血瘀',
    'QI_STAGNATION': '气郁',
    'SPECIAL_DIATHESIS': '特禀',
    'BALANCED': '平和'
  }
  return names[code] || code
}

const getConstitutionTagType = (code) => {
  const types = {
    'QI_DEFICIENCY': 'warning',
    'YANG_DEFICIENCY': 'danger',
    'YIN_DEFICIENCY': 'info',
    'PHLEGM_DAMPNESS': 'primary',
    'DAMP_HEAT': 'danger',
    'BLOOD_STASIS': 'danger',
    'QI_STAGNATION': 'warning',
    'SPECIAL_DIATHESIS': 'info',
    'BALANCED': 'success'
  }
  return types[code] || 'info'
}

const getPercentage = (count, total) => {
  return Math.round((count / total) * 100)
}

const getProgressColor = (type) => {
  const colors = {
    'QI_DEFICIENCY': '#e6a23c',
    'YANG_DEFICIENCY': '#f56c6c',
    'YIN_DEFICIENCY': '#909399',
    'PHLEGM_DAMPNESS': '#409eff',
    'DAMP_HEAT': '#f56c6c',
    'BLOOD_STASIS': '#f56c6c',
    'QI_STAGNATION': '#e6a23c',
    'SPECIAL_DIATHESIS': '#909399',
    'BALANCED': '#67c23a'
  }
  return colors[type] || '#409eff'
}

const getScoreColor = (type) => {
  const colors = {
    'QI_DEFICIENCY': '#e6a23c',
    'YANG_DEFICIENCY': '#f56c6c',
    'YIN_DEFICIENCY': '#909399',
    'PHLEGM_DAMPNESS': '#409eff',
    'DAMP_HEAT': '#f56c6c',
    'BLOOD_STASIS': '#f56c6c',
    'QI_STAGNATION': '#e6a23c',
    'SPECIAL_DIATHESIS': '#909399',
    'BALANCED': '#67c23a'
  }
  return colors[type] || '#409eff'
}

const viewMemberPlan = (userId) => {
  // 这里可以跳转到查看成员个性化方案的页面
  router.push({ path: '/tcm/personalized-plan', query: { userId } })
}

// 图表选项
const chartOption = computed(() => {
  if (!overview.value || !overview.value.constitutionDistribution) {
    return {}
  }
  
  const data = Object.entries(overview.value.constitutionDistribution).map(([type, count]) => ({
    name: getConstitutionName(type),
    value: count
  }))
  
  return {
    title: {
      text: '家庭体质分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '体质分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
})

const familyName = computed(() => {
  return overview.value ? overview.value.familyName : '家庭健康概览'
})

onMounted(() => {
  loadOverview()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.family-tcm-health-overview {
  padding: 24px;
  /* Background handled by global layout */
  min-height: 100%;
}

.page-header {
  margin-bottom: 24px;
  animation: fadeInDown 0.6s vars.$ease-spring;
}

.overview-card {
  @include mixins.glass-effect;
  border: vars.$glass-border;
  border-radius: vars.$radius-lg;
  animation: fadeInUp 0.6s vars.$ease-spring;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    padding: 16px 24px;
  }
  
  :deep(.el-card__body) {
    padding: 24px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 18px;
  color: vars.$text-main-color;
  
  .action-btn {
    transition: all 0.3s vars.$ease-spring;
    &:hover {
      transform: translateY(-2px);
      box-shadow: vars.$shadow-sm;
    }
  }
}

.animate-item {
  animation: fadeInUp 0.6s vars.$ease-spring backwards;
}

.family-info {
  margin-bottom: 32px;
  
  .glass-descriptions {
    :deep(.el-descriptions__cell) {
      background: transparent;
    }
    :deep(.el-descriptions__label) {
      background: rgba(255, 255, 255, 0.5);
      color: vars.$text-secondary-color;
      font-weight: 500;
    }
    :deep(.el-descriptions__content) {
      color: vars.$text-main-color;
    }
  }
}

.constitution-distribution {
  margin-bottom: 32px;
  
  h3 {
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: 600;
    color: vars.$text-main-color;
    padding-left: 12px;
    border-left: 4px solid vars.$primary-color;
  }
}

.glass-panel {
  @include mixins.glass-effect;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.4);
}

.chart-container {
  height: 400px;
  border-radius: vars.$radius-lg;
  padding: 16px;
  transition: all 0.3s vars.$ease-spring;
  
  &:hover {
    background: rgba(255, 255, 255, 0.6);
    box-shadow: vars.$shadow-sm;
    transform: translateY(-2px);
  }
}

.stat-card {
  border-radius: vars.$radius-lg;
  border: none;
  height: 100%;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(0,0,0,0.05);
    padding: 12px 20px;
    font-weight: 600;
  }
}

.stat-item {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .el-progress {
    flex: 1;
    margin: 0 16px;
  }
}

.type-name {
  display: inline-block;
  width: 70px;
  font-size: 14px;
  color: vars.$text-main-color;
  font-weight: 500;
}

.count {
  width: 40px;
  text-align: right;
  font-size: 14px;
  color: vars.$text-secondary-color;
  font-weight: 500;
}

.scores-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
}

.members-section {
  margin-bottom: 32px;
  
  h3 {
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: 600;
    color: vars.$text-main-color;
    padding-left: 12px;
    border-left: 4px solid vars.$primary-color;
  }
  
  .glass-table {
    background: transparent !important;
    border-radius: vars.$radius-lg;
    overflow: hidden;
    
    :deep(.el-table__inner-wrapper), :deep(tr), :deep(th.el-table__cell), :deep(td.el-table__cell) {
      background: transparent !important;
    }
    
    :deep(th.el-table__cell) {
      background: rgba(255, 255, 255, 0.5) !important;
      color: vars.$text-secondary-color;
      font-weight: 600;
    }
    
    :deep(.el-table__row:hover > td.el-table__cell) {
      background: rgba(255, 255, 255, 0.3) !important;
    }
  }
}

.family-recommendation {
  h3 {
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: 600;
    color: vars.$text-main-color;
    padding-left: 12px;
    border-left: 4px solid vars.$primary-color;
  }

  .glass-alert {
    background: rgba(vars.$info-color, 0.1);
    border: 1px solid rgba(vars.$info-color, 0.2);
    border-radius: vars.$radius-md;
    
    :deep(.el-alert__title) {
      font-size: 15px;
      line-height: 1.6;
      color: vars.$text-main-color;
    }
    
    :deep(.el-alert__icon) {
      font-size: 20px;
      color: vars.$info-color;
    }
  }
}

.no-data {
  text-align: center;
  padding: 60px 0;
  
  .el-empty {
    margin-bottom: 24px;
  }
}

@media (max-width: 768px) {
  .mt-xs-20 {
    margin-top: 20px;
  }
  
  .chart-container {
    height: 300px;
  }
}
</style>