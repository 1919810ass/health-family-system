<template>
  <div class="page-container recommendation-page">
    <el-page-header content="个性化建议" icon="" class="mb-24" />

    <el-row :gutter="20" class="category-row">
      <el-col v-for="(cat, index) in categories" :key="cat.key" :xs="24" :sm="12" :md="6">
        <el-card
          shadow="hover"
          :class="['category-card', activeCategory === cat.key ? 'is-active' : '']"
          @click="handleCategoryClick(cat.key)"
          :style="{ '--delay': index * 0.1 + 's' }"
        >
          <div class="card-icon" :style="{ background: cat.color + '15', color: cat.color }">
            <el-icon size="32"><component :is="cat.icon" /></el-icon>
          </div>
          <div class="card-title">{{ cat.name }}</div>
          <div class="card-desc">{{ cat.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="list-card glass-card">
      <template #header>
        <div class="list-header">
          <span class="list-title">
            <el-icon class="mr-8"><MagicStick /></el-icon>
            每日建议
          </span>
          <div class="toolbar-inline">
            <el-date-picker v-model="selectedDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 150px" />
            <el-button type="primary" round :loading="generating" @click="handleGenerate" v-particles>生成建议</el-button>
          </div>
        </div>
      </template>
      <div style="margin-bottom:24px">
        <el-alert
          v-if="requiredTypes.every(t => completion[t])"
          type="success"
          :closable="false"
          title="数据完整，正在生成建议"
          show-icon
          class="mb-16"
        />
        <div v-else>
          <el-alert
            type="warning"
            :closable="false"
            title="当前数据不完整，请先完成以下录入"
            show-icon
            class="mb-16"
          />
          <div class="completion-tips">
            <div v-for="t in requiredTypes.filter(t => !completion[t])" :key="t" class="completion-item">
               <el-tag size="small" type="warning" class="mr-8">{{ typeMap[t] }}</el-tag>
               <span class="text-secondary">{{ instructions[t] }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-area" v-loading="loading" element-loading-text="加载中..." />
      <div v-else-if="!recommendations.length" class="empty-box">
        <el-empty description="暂无建议，请点击生成" />
      </div>
      <div v-else class="recommendation-list">
        <el-card v-for="item in recommendations" :key="item.id" shadow="never" class="rec-item">
          <template #header>
            <div class="rec-header">
              <div class="rec-headline">
                <el-tag size="small" :type="item.categoryTagType">{{ item.categoryLabel }}</el-tag>
                <span class="rec-date">{{ item.forDate }}</span>
              </div>
              <el-tag v-if="item.safety?.refuse" size="small" type="danger">建议复查</el-tag>
            </div>
          </template>

          <div class="rec-body">
            <div v-for="sub in item.items" :key="sub.title" class="rec-sub-item">
              <div class="rec-sub-header">
                <h4 class="rec-title">{{ sub.title }}</h4>
                <el-tag v-if="sub.priority" size="small" effect="plain">{{ sub.priority }}</el-tag>
              </div>
              <p class="rec-content">{{ sub.content }}</p>
              <div class="rec-tags" v-if="sub.sourceTags?.length">
                <el-tag v-for="tag in sub.sourceTags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              </div>
            </div>
            <el-collapse accordion>
              <el-collapse-item title="查看依据">
                <ul class="evidence-list">
                  <li v-for="e in item.evidence" :key="e">{{ e }}</li>
                </ul>
              </el-collapse-item>
            </el-collapse>
          </div>

          <div class="rec-footer">
            <el-button-group>
              <el-button size="small" :type="item.accepted === true ? 'success' : 'default'" @click="handleFeedback(item, true)">
                👍 有用
              </el-button>
              <el-button size="small" :type="item.accepted === false ? 'danger' : 'default'" @click="handleFeedback(item, false)">
                👎 无用
              </el-button>
            </el-button-group>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { markRaw, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useRecommendationStore } from '../../stores'
import { fetchRecommendations, generateRecommendations, sendFeedback } from '../../api/recommendation'
import { getLogs } from '../../api/log'
import { Food, Clock, Bicycle, Orange } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { mapToBackendCategories } from '../../utils/recommendation'

const store = useRecommendationStore()
const { activeCategory, items: recommendations } = storeToRefs(store)
const loading = ref(false)
const generating = ref(false)
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const completion = ref({ DIET: false, SLEEP: false, SPORT: false, MOOD: false })
const checking = ref(false)

const categories = [
  { key: 'DIET', name: '饮食', desc: '合理膳食建议', color: '#409EFF', icon: markRaw(Food) },
  { key: 'REST', name: '作息', desc: '睡眠与休息', color: '#67C23A', icon: markRaw(Clock) },
  { key: 'SPORT', name: '运动', desc: '运动与活动', color: '#E6A23C', icon: markRaw(Bicycle) },
  { key: 'EMOTION', name: '情绪', desc: '心理与情绪', color: '#F56C6C', icon: markRaw(Orange) }
]

// refs are from storeToRefs above

onMounted(() => {
  loadRecommendations()
  checkCompletion()
})
watch(selectedDate, async () => {
  await checkCompletion()
  await loadRecommendations()
})

const handleCategoryClick = (key) => {
  if (activeCategory.value === key) {
    return
  }
  store.setActiveCategory(key)
  loadRecommendations()
}

const requiredTypes = ['DIET', 'SLEEP', 'SPORT', 'MOOD']
const typeMap = { DIET: '饮食', SLEEP: '睡眠', SPORT: '运动', MOOD: '情绪' }
const instructions = {
  DIET: '填写食物、数量、单位，如“鸡蛋1个、米饭1碗”，可点击“优化输入内容”获得结构化结果',
  SLEEP: '填写睡眠时长(小时)、就寝与起床时间，质量可选',
  SPORT: '填写运动类型、时长(分钟)，可选距离(公里)',
  MOOD: '填写情绪类型(开心/焦虑等)与强度(1-5)'
}

const loadRecommendations = async () => {
  loading.value = true
  try {
    const resp = await fetchRecommendations({
      date: selectedDate.value,
      category: activeCategory.value
    })
    const list = Array.isArray(resp?.data) ? resp.data : (Array.isArray(resp) ? resp : [])
    store.setItems(list || [])
  } catch (error) {
    console.error(error)
    ElMessage.error('加载建议失败')
  } finally {
    loading.value = false
  }
}

const checkCompletion = async () => {
  checking.value = true
  try {
    const results = await Promise.all(requiredTypes.map(t => getLogs({ type: t, startDate: selectedDate.value, endDate: selectedDate.value })))
    requiredTypes.forEach((t, i) => {
      const list = results[i]?.data || []
      completion.value[t] = Array.isArray(list) && list.length > 0
    })
    const complete = requiredTypes.every(t => completion.value[t])
    if (complete) {
      ElMessage.success('数据完整，可生成建议')
    }
  } catch (e) {
    console.error(e)
  } finally {
    checking.value = false
  }
}

const handleGenerate = async () => {
  generating.value = true
  try {
    await generateRecommendations({
      date: selectedDate.value,
      categories: [activeCategory.value],
      maxItems: 3,
      strictMode: false
    })
    ElMessage.success('正在为您生成建议')
    await loadRecommendations()
  } catch (error) {
    console.error(error)
    ElMessage.error('生成建议失败')
  } finally {
    generating.value = false
  }
}

const handleGenerateAll = async () => {
  generating.value = true
  try {
    await generateRecommendations({
      date: selectedDate.value,
      categories: mapToBackendCategories(requiredTypes),
      maxItems: 8,
      strictMode: true
    })
  } catch (e) {
    console.error(e)
  } finally {
    generating.value = false
  }
}

const handleFeedback = async (item, accepted) => {
  if (item.accepted === accepted) {
    return
  }
  try {
    await sendFeedback(item.id, { accepted })
    item.accepted = accepted
    ElMessage.success('反馈已记录')
  } catch (error) {
    console.error(error)
    ElMessage.error('反馈失败')
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.recommendation-page {
  padding: 16px;
  min-height: 100%;
  
  .category-row {
    margin-bottom: 24px;
  }
}
.mb-24 { margin-bottom: 24px; }
.mb-16 { margin-bottom: 16px; }
.mr-8 { margin-right: 8px; }

.category-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s vars.$ease-spring;
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  animation: fadeInUp 0.6s vars.$ease-spring backwards;
  animation-delay: var(--delay);
  
  &.is-active {
    border-color: vars.$primary-color;
    background-color: rgba(vars.$primary-color, 0.05);
    transform: translateY(-4px);
    box-shadow: vars.$shadow-md;
  }
  &:hover {
    transform: translateY(-6px);
    box-shadow: vars.$shadow-lg;
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-icon {
  margin-bottom: 16px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.category-card:hover .card-icon {
  transform: scale(1.1) rotate(5deg);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: vars.$text-main-color;
}
.card-desc {
  font-size: 14px;
  color: vars.$text-secondary-color;
}

.list-card {
  margin-top: 16px;
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: 1px solid rgba(255, 255, 255, 0.6);
  
  :deep(.el-card__header) {
    padding: 16px 24px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.list-title {
  font-size: 18px;
  font-weight: 600;
  color: vars.$text-main-color;
  display: flex;
  align-items: center;
}
.toolbar-inline {
  display: flex;
  gap: 12px;
}

.completion-tips {
  background-color: rgba(vars.$warning-color, 0.1);
  padding: 16px;
  border-radius: vars.$radius-base;
  border: 1px solid rgba(vars.$warning-color, 0.2);
}
.completion-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  &:last-child { margin-bottom: 0; }
}
.text-secondary {
  color: vars.$text-secondary-color;
  font-size: 14px;
}

.loading-area { min-height: 200px; }
.empty-box {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.rec-item {
  border-radius: vars.$radius-md;
  border: 1px solid rgba(0,0,0,0.05);
  transition: all 0.3s;
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(5px);
  animation: fadeInUp 0.5s ease-out backwards;
  
  &:hover {
    box-shadow: vars.$shadow-md;
    transform: translateY(-2px);
    background: rgba(255,255,255,0.8);
  }
  
  :deep(.el-card__header) {
    background-color: rgba(250, 250, 250, 0.5);
    padding: 16px 24px;
    border-bottom: 1px solid rgba(0,0,0,0.05);
  }
}
.rec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.rec-headline {
  display: flex;
  align-items: center;
  gap: 12px;
}
.rec-date {
  font-size: 13px;
  color: vars.$text-secondary-color;
}

.rec-body {
  padding: 8px 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.rec-sub-item {
  background: rgba(248, 250, 252, 0.6);
  border-radius: vars.$radius-base;
  padding: 20px;
  border: 1px solid rgba(0,0,0,0.05);
  transition: all 0.3s;
  
  &:hover {
    background: rgba(248, 250, 252, 0.9);
    box-shadow: vars.$shadow-sm;
  }
}
.rec-sub-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.rec-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: vars.$text-main-color;
}
.rec-content {
  margin: 0;
  color: vars.$text-main-color;
  line-height: 1.8;
  font-size: 15px;
  text-align: justify;
}
.rec-tags {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
.evidence-list {
  margin: 0;
  padding-left: 20px;
  color: vars.$text-secondary-color;
  font-size: 14px;
  line-height: 1.6;
}
.rec-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0,0,0,0.05);
}
</style>
