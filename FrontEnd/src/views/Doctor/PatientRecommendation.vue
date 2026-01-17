<template>
  <div class="page-container recommendation-page">
    <el-page-header @back="goBack" content="患者健康建议" class="mb-24" />

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
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-area" v-loading="loading" element-loading-text="加载中..." />
      <div v-else-if="!recommendations.length" class="empty-box">
        <el-empty description="暂无建议" />
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
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { markRaw, onMounted, ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { fetchRecommendations } from '../../api/recommendation'
import { Food, Clock, Bicycle, Orange, MagicStick } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const targetUserId = computed(() => route.params.userId)

const activeCategory = ref('DIET')
const recommendations = ref([])
const loading = ref(false)
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))

const categories = [
  { key: 'DIET', name: '饮食', desc: '合理膳食建议', color: '#409EFF', icon: markRaw(Food) },
  { key: 'REST', name: '作息', desc: '睡眠与休息', color: '#67C23A', icon: markRaw(Clock) },
  { key: 'SPORT', name: '运动', desc: '运动与活动', color: '#E6A23C', icon: markRaw(Bicycle) },
  { key: 'EMOTION', name: '情绪', desc: '心理与情绪', color: '#F56C6C', icon: markRaw(Orange) }
]

onMounted(() => {
  if (!targetUserId.value) {
    ElMessage.error('未指定患者')
    router.back()
    return
  }
  loadRecommendations()
})

watch(selectedDate, async () => {
  await loadRecommendations()
})

const handleCategoryClick = (key) => {
  if (activeCategory.value === key) {
    return
  }
  activeCategory.value = key
  loadRecommendations()
}

const goBack = () => {
  router.back()
}

const loadRecommendations = async () => {
  loading.value = true
  try {
    const resp = await fetchRecommendations({
      date: selectedDate.value,
      category: activeCategory.value,
      userId: targetUserId.value
    })
    const list = Array.isArray(resp?.data) ? resp.data : (Array.isArray(resp) ? resp : [])
    recommendations.value = list || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载建议失败')
  } finally {
    loading.value = false
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
</style>
