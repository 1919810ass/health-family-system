<template>
  <div class="tcm-personalized-plan">
    <el-page-header @back="goBack" content="个性化中医养生方案" />
    
    <el-card class="plan-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>个性化中医养生方案</span>
          <el-button type="primary" @click="generatePlan" :disabled="loading">
            {{ plan ? '重新生成方案' : '生成养生方案' }}
          </el-button>
        </div>
      </template>
      
      <div v-if="plan" class="plan-content">
        <!-- 基础信息 -->
        <div class="basic-info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="主导体质">{{ getConstitutionName(plan.primaryConstitution) }}</el-descriptions-item>
            <el-descriptions-item label="生成日期">{{ formatDate(plan.planDate) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 体质调理优先级 -->
        <div class="priority-section">
          <h3>调理优先级建议</h3>
          <el-tag 
            v-for="(item, index) in plan.priorityRecommendations" 
            :key="index" 
            type="warning" 
            class="priority-tag"
          >
            {{ item }}
          </el-tag>
        </div>
        
        <!-- 分类养生建议 -->
        <div class="plan-sections">
          <el-tabs v-model="activeTab">
            <el-tab-pane 
              v-for="tab in planTabs" 
              :key="tab.name" 
              :label="tab.label" 
              :name="tab.name"
            >
              <div class="plan-items">
                <el-card 
                  v-for="item in (plan.planItems[tab.name] || [])" 
                  :key="item.title" 
                  class="item-card"
                  :class="getDifficultyClass(item.difficulty)"
                >
                  <template #header>
                    <div class="item-header">
                      <span class="item-title">{{ item.title }}</span>
                      <el-tag :type="getDifficultyTagType(item.difficulty)">
                        {{ getDifficultyText(item.difficulty) }}
                      </el-tag>
                    </div>
                  </template>
                  <div class="item-content">{{ item.content }}</div>
                  <div class="item-tags" v-if="item.tags && item.tags.length">
                    <el-tag 
                      v-for="tag in item.tags" 
                      :key="tag" 
                      size="small" 
                      type="info"
                      class="tag"
                    >
                      {{ tag }}
                    </el-tag>
                  </div>
                  <div class="item-contraindications" v-if="item.contraindications && item.contraindications.length">
                    <el-alert 
                      title="注意事项" 
                      type="warning" 
                      :closable="false" 
                      class="contraindication-alert"
                    >
                      <div v-for="contraindication in item.contraindications" :key="contraindication">
                        {{ contraindication }}
                      </div>
                    </el-alert>
                  </div>
                  <div class="item-actions mt-16" style="display: flex; justify-content: flex-end;">
                    <el-button size="small" type="primary" plain @click="checkIn(item)" :disabled="item.checked">
                      <el-icon class="mr-4"><Check /></el-icon>
                      {{ item.checked ? '今日已打卡' : '完成打卡' }}
                    </el-button>
                  </div>
                </el-card>
                <el-empty v-if="!plan.planItems[tab.name] || plan.planItems[tab.name].length === 0" description="暂无相关建议" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <!-- 季节性建议 -->
        <div class="seasonal-section" v-if="hasSeasonalRecommendations">
          <h3>季节性养生建议</h3>
          <el-table :data="seasonalRecommendations" style="width: 100%">
            <el-table-column prop="season" label="季节" width="120" />
            <el-table-column prop="advice" label="建议" />
          </el-table>
        </div>
      </div>
      
      <div v-else class="no-data">
        <el-empty description="暂无养生方案，请先完成体质测评" />
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
import dayjs from 'dayjs'
import { getConstitutionName } from '@/utils/tcm-constants'
import { Check } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const plan = ref(null)
const activeTab = ref('DIET')

// 方案标签页配置
const planTabs = [
  { label: '饮食调理', name: 'DIET' },
  { label: '茶饮调理', name: 'TEA' },
  { label: '穴位按摩', name: 'ACUPUNCTURE' },
  { label: '运动调理', name: 'EXERCISE' },
  { label: '情志调理', name: 'EMOTION' },
  { label: '生活调理', name: 'LIFESTYLE' }
]

const checkIn = (item) => {
  item.checked = true
  ElMessage.success(`已完成"${item.title}"打卡`)
}

const goBack = () => {
  router.push('/tcm/assessments')
}

const generatePlan = async () => {
  try {
    loading.value = true
    const response = await assessmentApi.getTcmPersonalizedPlan()
    plan.value = response.data
    
    // 如果没有数据，提示用户
    if (!plan.value) {
      ElMessage.warning('暂无个性化方案数据')
    } else {
      ElMessage.success('方案已刷新')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取养生方案失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

// 季节性建议计算属性
const hasSeasonalRecommendations = computed(() => {
  return plan.value && plan.value.seasonalRecommendations && 
         Object.keys(plan.value.seasonalRecommendations).length > 0
})

const seasonalRecommendations = computed(() => {
  if (!plan.value || !plan.value.seasonalRecommendations) return []
  
  return Object.entries(plan.value.seasonalRecommendations).map(([season, advice]) => ({
    season,
    advice
  }))
})

// 难度相关辅助函数
const getDifficultyClass = (difficulty) => {
  return difficulty ? `difficulty-${difficulty.toLowerCase()}` : ''
}

const getDifficultyTagType = (difficulty) => {
  return {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }[difficulty] || 'info'
}

const getDifficultyText = (difficulty) => {
  return {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }[difficulty] || '未知'
}

onMounted(() => {
  generatePlan()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.tcm-personalized-plan {
  padding: 20px;
  /* Background handled by global layout */
  min-height: 100%;
}

.plan-card {
  margin-top: 20px;
  @include mixins.glass-effect;
  border: vars.$glass-border;
  border-radius: vars.$radius-lg;
  animation: fadeInUp 0.6s vars.$ease-spring;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: vars.$text-main-color;
}

.basic-info {
  margin-bottom: 24px;
  
  :deep(.el-descriptions__cell) {
    background: transparent;
  }
}

.priority-section {
  margin: 24px 0;
  
  h3 {
    margin-bottom: 16px;
    font-size: 18px;
    color: vars.$text-main-color;
  }
}

.priority-tag {
  margin: 5px 12px 5px 0;
  padding: 16px 20px;
  font-size: 14px;
  border-radius: 8px;
  box-shadow: vars.$shadow-sm;
  border: none;
}

.plan-sections {
  margin: 24px 0;
  
  :deep(.el-tabs__item) {
    font-size: 16px;
    &.is-active {
      font-weight: 600;
    }
  }
}

.seasonal-section {
  margin-top: 24px;
  
  h3 {
    margin-bottom: 16px;
  }
}

.item-card {
  margin-bottom: 20px;
  border: none;
  background: rgba(255, 255, 255, 0.5);
  border-radius: vars.$radius-md;
  transition: all 0.3s vars.$ease-spring;
  animation: fadeInUp 0.6s vars.$ease-spring backwards;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: vars.$shadow-md;
    background: rgba(255, 255, 255, 0.8);
  }
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-title {
  font-weight: 600;
  font-size: 16px;
  color: vars.$text-main-color;
}

.item-content {
  color: vars.$text-regular-color;
  line-height: 1.8;
  font-size: 15px;
}

.item-tags {
  margin-top: 16px;
}

.tag {
  margin: 0 8px 8px 0;
}

.item-contraindications {
  margin-top: 16px;
}

.contraindication-alert {
  padding: 12px;
  border-radius: 8px;
}

.difficulty-easy {
  border-left: 4px solid vars.$success-color;
}

.difficulty-medium {
  border-left: 4px solid vars.$warning-color;
}

.difficulty-hard {
  border-left: 4px solid vars.$danger-color;
}

.no-data {
  text-align: center;
  padding: 60px 0;
  
  .el-empty {
    margin-bottom: 24px;
  }
}

.mt-16 { margin-top: 16px; }
.mr-4 { margin-right: 4px; }
</style>