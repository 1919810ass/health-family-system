<template>
  <div class="lifestyle glass-effect">
    <el-page-header content="生活方式管理" class="glass-header" />

    <el-tabs class="mt-16 glass-tabs" type="border-card">
      <el-tab-pane label="饮食管理">
        <el-row :gutter="20">
          <el-col :xs="24" :md="12" class="stagger-anim" style="--delay: 0.1s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><Camera /></el-icon> 拍照/描述记录</span></template>
              <el-form :model="dietForm" label-width="100px">
                <el-form-item label="上传图片">
                  <el-upload
                    class="avatar-uploader glass-upload"
                    action="#"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleUpload"
                  >
                    <img v-if="dietForm.imageUrl" :src="dietForm.imageUrl" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
                <el-form-item label="饮食描述">
                  <el-input v-model="dietForm.description" type="textarea" placeholder="午餐：米饭、青菜、鱼" class="glass-input" :rows="3" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="dietLoading" @click="ingestDietAction" round>识别并记录</el-button>
                </el-form-item>
              </el-form>
              <el-table v-if="dietItems.length" :data="dietItems" size="small" class="mt-12 glass-table">
                <el-table-column prop="name" label="食物" />
                <el-table-column prop="calories" label="热量" />
              </el-table>
              <div v-if="dietTotal" class="total-calories">总热量：<span>{{ Math.round(dietTotal) }}</span> 千卡</div>
            </el-card>
          </el-col>
          <el-col :xs="24" :md="12" class="stagger-anim" style="--delay: 0.2s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><DataLine /></el-icon> 周营养报告</span></template>
              <div class="report ai-content" v-loading="reportLoading" v-html="weeklyReport || '暂无报告，请先记录饮食'"></div>
              <div class="mt-12">
                <el-button @click="loadWeeklyReport" round class="glass-button">刷新报告</el-button>
              </div>
            </el-card>
            <el-card class="mt-16 glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><Food /></el-icon> 个性化食谱推荐</span></template>
              <el-select v-model="tags" multiple placeholder="选择标签" style="width:100%" class="glass-select">
                <el-option label="糖尿病" value="diabetes" />
                <el-option label="素食" value="vegan" />
                <el-option label="低脂" value="low-fat" />
                <el-option label="高蛋白" value="high-protein" />
              </el-select>
              <div class="mt-12">
                <el-button type="primary" :loading="recipeLoading" @click="loadRecipes" round v-particles>生成食谱</el-button>
              </div>
              <el-descriptions v-if="recipes.length" :column="1" class="mt-12 glass-desc">
                <el-descriptions-item v-for="r in recipes" :key="r.title" :label="r.title">{{ r.items.join('、') }}（{{ r.note }}）</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="运动管理">
        <el-row justify="center">
          <el-col :xs="24" :md="16" class="stagger-anim" style="--delay: 0.1s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><Bicycle /></el-icon> 记录运动</span></template>
              <el-form :model="sportForm" label-width="100px">
                <el-form-item label="类型">
                  <el-select v-model="sportForm.type" style="width:100%" class="glass-select">
                    <el-option label="跑步" value="run" />
                    <el-option label="游泳" value="swim" />
                    <el-option label="步行" value="walk" />
                  </el-select>
                </el-form-item>
                <el-form-item label="时长(分钟)">
                  <el-input-number v-model="sportForm.durationMinutes" :min="10" :max="240" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="距离(公里)">
                  <el-input-number v-model="sportForm.distanceKm" :min="0" :max="50" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="步数">
                  <el-input-number v-model="sportForm.steps" :min="0" :max="50000" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="sportLoading" @click="recordSport" round v-particles>保存</el-button>
                  <el-button @click="loadSportSuggest" :loading="sportSuggestLoading" round class="glass-button">获取运动建议</el-button>
                </el-form-item>
              </el-form>
              <el-alert v-if="sportSuggest" :closable="false" type="info" class="mt-12 glass-alert">
                <template #default>
                  <div class="ai-content" v-html="sportSuggest"></div>
                </template>
              </el-alert>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="睡眠管理">
        <el-row justify="center">
          <el-col :xs="24" :md="16" class="stagger-anim" style="--delay: 0.1s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><Moon /></el-icon> 记录睡眠</span></template>
              <el-form :model="sleepForm" label-width="120px">
                <el-form-item label="睡眠时长(小时)">
                  <el-input-number v-model="sleepForm.hours" :min="0" :max="16" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="深度睡眠(小时)">
                  <el-input-number v-model="sleepForm.deepHours" :min="0" :max="16" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="醒来次数">
                  <el-input-number v-model="sleepForm.wakeCount" :min="0" :max="20" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="sleepLoading" @click="recordSleepAction" round v-particles>保存</el-button>
                  <el-button @click="analyzeSleepAction" :loading="sleepAnalyzeLoading" round class="glass-button">分析睡眠</el-button>
                </el-form-item>
              </el-form>
              <el-alert v-if="sleepAnalyze" :closable="false" type="warning" class="mt-12 glass-alert">
                <template #default>
                  <div class="ai-content" v-html="sleepAnalyze"></div>
                </template>
              </el-alert>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Camera, DataLine, Food, Bicycle, Moon } from '@element-plus/icons-vue'
import { ingestDiet, weeklyDietReport, recommendRecipes, recordExercise, suggestExercise, recordSleep, analyzeSleep, uploadDietImage } from '../../api/lifestyle'

const dietForm = ref({ imageUrl: '', description: '' })
const dietLoading = ref(false)
const dietItems = ref([])
const dietTotal = ref(0)
const reportLoading = ref(false)
const weeklyReport = ref('')
const tags = ref([])
const recipes = ref([])
const recipeLoading = ref(false)

const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png' && rawFile.type !== 'image/gif') {
    ElMessage.error('Avatar picture must be JPG/PNG/GIF format!')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('Avatar picture size can not exceed 5MB!')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await uploadDietImage(formData)
    if (res.code === 0) {
        // Handle new response format: { url, recognizedFood, confidence }
        const data = res.data
        if (typeof data === 'string') {
           dietForm.value.imageUrl = data
        } else {
           dietForm.value.imageUrl = data.url
           if (data.recognizedFood) {
               dietForm.value.description = `识别结果: ${data.recognizedFood} (置信度: ${(data.confidence * 100).toFixed(0)}%)`
               ElMessage.success(`已识别为：${data.recognizedFood}`)
           }
        }
        ElMessage.success('上传成功')
    } else {
        ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const sportForm = ref({ type: 'run', durationMinutes: 30, distanceKm: 3, steps: 5000 })
const sportLoading = ref(false)
const sportSuggestLoading = ref(false)
const sportSuggest = ref('')

const sleepForm = ref({ hours: 7, deepHours: 2, wakeCount: 1 })
const sleepLoading = ref(false)
const sleepAnalyzeLoading = ref(false)
const sleepAnalyze = ref('')

const ingestDietAction = async () => {
  dietLoading.value = true
  try {
    const payload = { ...dietForm.value }
    const res = await ingestDiet(payload)
    if (res.code === 0) {
      dietItems.value = res?.data?.items || []
      dietTotal.value = res?.data?.totalCalories || 0
      ElMessage.success('已记录饮食')
      await loadWeeklyReport()
    } else {
      ElMessage.error(res.message || '识别失败')
    }
  } catch (e) {
    ElMessage.error('识别失败')
  } finally {
    dietLoading.value = false
  }
}

const loadWeeklyReport = async () => {
  reportLoading.value = true
  try {
    const res = await weeklyDietReport({})
    if (res.code === 0) {
      weeklyReport.value = res.data || ''
    } else {
      weeklyReport.value = ''
    }
  } catch (e) {
    weeklyReport.value = ''
  } finally {
    reportLoading.value = false
  }
}

const loadRecipes = async () => {
  recipeLoading.value = true
  try {
    const res = await recommendRecipes({ tags: tags.value })
    if (res.code === 0) {
      recipes.value = res.data || []
    } else {
      recipes.value = []
    }
  } catch (e) {
    recipes.value = []
  } finally {
    recipeLoading.value = false
  }
}

const recordSport = async () => {
  sportLoading.value = true
  try {
    const res = await recordExercise(sportForm.value)
    if (res.code === 0) {
      ElMessage.success('已保存')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    sportLoading.value = false
  }
}

const loadSportSuggest = async () => {
  sportSuggestLoading.value = true
  try {
    const res = await suggestExercise()
    if (res.code === 0) {
      sportSuggest.value = res.data || ''
    } else {
      sportSuggest.value = ''
    }
  } catch (e) {
    sportSuggest.value = ''
  } finally {
    sportSuggestLoading.value = false
  }
}

const recordSleepAction = async () => {
  sleepLoading.value = true
  try {
    const res = await recordSleep(sleepForm.value)
    if (res.code === 0) {
      ElMessage.success('已保存')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    sleepLoading.value = false
  }
}

const analyzeSleepAction = async () => {
  sleepAnalyzeLoading.value = true
  try {
    const res = await analyzeSleep()
    if (res.code === 0) {
      sleepAnalyze.value = res.data || ''
    } else {
      sleepAnalyze.value = ''
    }
  } catch (e) {
    sleepAnalyze.value = ''
  } finally {
    sleepAnalyzeLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as vars;
@use '@/styles/mixins.scss' as mixins;

.lifestyle {
  padding: 20px;
  min-height: 100%;
  
  &.glass-effect {
    background: transparent;
  }
}

.glass-header {
  margin-bottom: 24px;
  animation: fadeInDown 0.6s vars.$ease-spring;
  
  :deep(.el-page-header__content) {
    color: vars.$text-main-color;
    font-weight: 600;
  }
}

.glass-tabs {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  animation: fadeInUp 0.8s vars.$ease-spring;
  
  :deep(.el-tabs__header) {
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(12px);
    border-radius: vars.$radius-lg vars.$radius-lg 0 0;
    border: 1px solid rgba(255, 255, 255, 0.4);
    margin: 0;
    
    .el-tabs__item {
      color: vars.$text-secondary-color;
      font-weight: 500;
      transition: all 0.3s;
      
      &.is-active {
        color: vars.$primary-color;
        font-weight: 600;
        background: rgba(255, 255, 255, 0.8);
        border-radius: vars.$radius-lg vars.$radius-lg 0 0;
      }
      
      &:hover:not(.is-active) {
        color: vars.$primary-color;
      }
    }
  }
  
  :deep(.el-tabs__content) {
    @include mixins.glass-effect;
    border-radius: 0 0 vars.$radius-lg vars.$radius-lg;
    padding: 24px;
    border-top: none;
    overflow: visible;
  }
}

.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  transition: all 0.4s vars.$ease-spring;
  margin-bottom: 24px;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: vars.$shadow-md;
    background: rgba(255, 255, 255, 0.8);
  }
  
  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: vars.$text-main-color;
    font-size: 16px;
    
    .el-icon {
      color: vars.$primary-color;
      font-size: 18px;
    }
  }
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
    padding: 16px 20px;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.glass-button {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(4px);
  color: vars.$text-main-color;
  transition: all 0.3s;
  
  &:hover {
    background: vars.$primary-color;
    border-color: vars.$primary-color;
    color: #fff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);
  }
}

/* Glass Form Elements */
.glass-input, .glass-select, .glass-input-number {
  :deep(.el-input__wrapper), :deep(.el-textarea__inner) {
    background: rgba(255, 255, 255, 0.5);
    box-shadow: none;
    border: 1px solid rgba(255, 255, 255, 0.4);
    border-radius: vars.$radius-base;
    transition: all 0.3s;
    
    &:hover, &.is-focus {
      background: rgba(255, 255, 255, 0.9);
      border-color: vars.$primary-color;
      box-shadow: 0 0 0 1px vars.$primary-color inset;
    }
  }
  
  :deep(.el-input__inner) {
    color: vars.$text-main-color;
  }
  
  /* Input Number Buttons */
  :deep(.el-input-number__decrease), :deep(.el-input-number__increase) {
    background: rgba(255, 255, 255, 0.5);
    border-color: rgba(255, 255, 255, 0.4);
    color: vars.$text-main-color;
    transition: all 0.3s;
    
    &:hover {
      background: vars.$primary-color;
      color: #fff;
      border-color: vars.$primary-color;
    }
  }
}

.glass-upload {
  :deep(.el-upload) {
    border: 2px dashed rgba(var(--el-color-primary-rgb), 0.2);
    border-radius: vars.$radius-lg;
    background: rgba(255, 255, 255, 0.3);
    transition: all 0.3s;
    width: 100%;
    max-width: 300px;
    
    &:hover {
      border-color: vars.$primary-color;
      background: rgba(255, 255, 255, 0.6);
      transform: scale(1.02);
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: vars.$text-secondary-color;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }
  
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
    object-fit: cover;
    border-radius: vars.$radius-lg;
  }
}

.glass-table {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.3);
  --el-table-row-hover-bg-color: rgba(var(--el-color-primary-rgb), 0.1);
  --el-table-border-color: rgba(255, 255, 255, 0.3);
  
  :deep(th), :deep(td) {
    background: transparent !important;
  }
}

.glass-desc {
  :deep(.el-descriptions__body) {
    background: transparent;
  }
  :deep(.el-descriptions__label) {
    color: vars.$text-secondary-color;
  }
  :deep(.el-descriptions__content) {
    color: vars.$text-main-color;
    font-weight: 500;
  }
}

.glass-alert {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(4px);
  
  &.el-alert--info {
    --el-alert-text-color: vars.$text-main-color;
  }
}

.mt-16 { margin-top: 16px }
.mt-12 { margin-top: 12px }
.report { min-height: 80px }

.total-calories {
  margin-top: 16px;
  font-size: 16px;
  color: vars.$text-main-color;
  text-align: right;
  font-weight: 500;
  
  span {
    color: vars.$danger-color;
    font-weight: 700;
    font-size: 24px;
    margin: 0 4px;
  }
}

/* Animations */
.stagger-anim {
  opacity: 0;
  animation: fadeInUp 0.8s vars.$ease-spring forwards;
  animation-delay: var(--delay, 0s);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* AI Content Styling */
.ai-content {
  line-height: 1.8;
  font-size: 15px;
  color: vars.$text-main-color;
  
  :deep(h3) {
    font-size: 18px;
    font-weight: 600;
    margin: 16px 0 12px;
    color: vars.$primary-color;
    display: flex;
    align-items: center;
    gap: 8px;
    
    &::before {
      content: '';
      width: 4px;
      height: 18px;
      background: vars.$primary-color;
      border-radius: 2px;
      display: inline-block;
    }
  }
  
  :deep(ul) {
    padding-left: 20px;
    margin: 12px 0;
  }
  
  :deep(li) {
    margin-bottom: 8px;
    list-style-type: none;
    position: relative;
    padding-left: 16px;
    
    &::before {
      content: '•';
      color: vars.$primary-color;
      position: absolute;
      left: 0;
      font-weight: bold;
    }
  }
  
  :deep(p) {
    margin: 10px 0;
    color: vars.$text-secondary-color;
  }
  
  :deep(strong) {
    color: vars.$primary-color;
    font-weight: 600;
  }
}

/* Media Queries */
@media (max-width: 768px) {
  .lifestyle {
    padding: 12px;
  }
  
  .glass-tabs {
    :deep(.el-tabs__header) {
      .el-tabs__item {
        padding: 0 12px;
        font-size: 14px;
      }
    }
    
    :deep(.el-tabs__content) {
      padding: 16px;
    }
  }
}
</style>
