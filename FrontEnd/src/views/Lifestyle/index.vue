<template>
  <div class="lifestyle glass-effect">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Sunrise /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">生活方式管理</h2>
        <p class="subtitle">饮食、运动、睡眠全方位健康管理</p>
      </div>
    </div>

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
                <el-form-item label="份量/数量">
                  <el-input v-model="dietForm.quantity" placeholder="例如：200g, 1碗, 半份" class="glass-input" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="dietLoading || uploadLoading" @click="ingestDietAction" round>
                    {{ dietLoading ? '正在记录...' : (uploadLoading ? '正在识别图片...' : '识别并记录') }}
                  </el-button>
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
                <el-form-item label="上床时间">
                   <el-time-picker v-model="sleepForm.bedtime" format="HH:mm" value-format="HH:mm" placeholder="选择时间" class="glass-input" style="width: 100%" />
                </el-form-item>
                <el-form-item label="起床时间">
                   <el-time-picker v-model="sleepForm.wakeTime" format="HH:mm" value-format="HH:mm" placeholder="选择时间" class="glass-input" style="width: 100%" />
                </el-form-item>
                <el-form-item label="入睡耗时(分钟)">
                   <el-input-number v-model="sleepForm.sleepLatency" :min="0" :max="120" placeholder="上床后多久入睡" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="醒来赖床(分钟)">
                   <el-input-number v-model="sleepForm.wakeUpLatency" :min="0" :max="120" placeholder="醒来后多久起床" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="深度睡眠(小时)">
                  <el-input-number v-model="sleepForm.deepHours" :min="0" :max="16" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="醒来次数">
                  <el-input-number v-model="sleepForm.wakeCount" :min="0" :max="20" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                
                <div v-if="calculatedDuration" style="margin-bottom: 24px; padding: 12px; background: rgba(64, 158, 255, 0.1); border-radius: 8px; text-align: center">
                   <span style="color: #606266">预计总睡眠时长: </span>
                   <span style="font-size: 18px; font-weight: bold; color: #409EFF; margin: 0 4px">{{ calculatedDuration }}</span>
                   <span style="color: #409EFF">小时</span>
                </div>

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

      <el-tab-pane label="情绪管理">
        <el-row justify="center">
          <el-col :xs="24" :md="16" class="stagger-anim" style="--delay: 0.1s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><ChatDotRound /></el-icon> 记录情绪</span></template>
              <el-form :model="moodForm" label-width="120px">
                <el-form-item label="情绪">
                  <el-select v-model="moodForm.emotion" placeholder="选择情绪" style="width: 100%" class="glass-select">
                    <el-option label="开心" value="开心" />
                    <el-option label="平静" value="平静" />
                    <el-option label="焦虑" value="焦虑" />
                    <el-option label="低落" value="低落" />
                    <el-option label="烦躁" value="烦躁" />
                    <el-option label="疲惫" value="疲惫" />
                  </el-select>
                </el-form-item>
                <el-form-item label="强度(1-5)">
                  <el-rate v-model="moodForm.level" :max="5" />
                </el-form-item>
                <el-form-item label="压力(0-10)">
                  <el-slider v-model="moodForm.stress" :min="0" :max="10" show-input />
                </el-form-item>
                <el-form-item label="精力(0-10)">
                  <el-slider v-model="moodForm.energy" :min="0" :max="10" show-input />
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="moodForm.note" type="textarea" :rows="3" placeholder="例如：工作压力大，下午有点焦虑" class="glass-input" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="moodLoading" @click="recordMoodAction" round v-particles>保存到健康日志</el-button>
                  <el-button @click="goToLogs('mood')" round class="glass-button">去健康日志查看</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="体征管理">
        <el-row :gutter="20">
          <el-col :xs="24" :md="12" class="stagger-anim" style="--delay: 0.1s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><Monitor /></el-icon> 血压</span></template>
              <el-form :model="bpForm" label-width="120px">
                <el-form-item label="测量时间">
                  <el-time-picker v-model="bpForm.time" format="HH:mm" value-format="HH:mm" placeholder="HH:mm" class="glass-input" style="width: 100%" />
                </el-form-item>
                <el-form-item label="收缩压(mmHg)">
                  <el-input-number v-model="bpForm.systolic" :min="60" :max="260" :precision="0" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="舒张压(mmHg)">
                  <el-input-number v-model="bpForm.diastolic" :min="40" :max="160" :precision="0" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="bpForm.note" placeholder="可选" class="glass-input" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="vitalsLoading.bp" @click="recordVitalsAction('血压')" round v-particles>保存到健康日志</el-button>
                  <el-button @click="goToLogs('vital')" round class="glass-button">去健康日志查看</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <el-col :xs="24" :md="12" class="stagger-anim" style="--delay: 0.2s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><Monitor /></el-icon> 血糖</span></template>
              <el-form :model="glucoseForm" label-width="120px">
                <el-form-item label="测量时间">
                  <el-time-picker v-model="glucoseForm.time" format="HH:mm" value-format="HH:mm" placeholder="HH:mm" class="glass-input" style="width: 100%" />
                </el-form-item>
                <el-form-item label="数值">
                  <el-input-number v-model="glucoseForm.value" :min="0" :max="30" :precision="1" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="单位">
                  <el-input v-model="glucoseForm.unit" placeholder="mmol/L" class="glass-input" />
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="glucoseForm.note" placeholder="可选，例如：空腹/餐后" class="glass-input" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="vitalsLoading.glucose" @click="recordVitalsAction('血糖')" round v-particles>保存到健康日志</el-button>
                  <el-button @click="goToLogs('vital')" round class="glass-button">去健康日志查看</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <el-col :xs="24" :md="12" class="stagger-anim" style="--delay: 0.3s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><Monitor /></el-icon> 心率</span></template>
              <el-form :model="hrForm" label-width="120px">
                <el-form-item label="测量时间">
                  <el-time-picker v-model="hrForm.time" format="HH:mm" value-format="HH:mm" placeholder="HH:mm" class="glass-input" style="width: 100%" />
                </el-form-item>
                <el-form-item label="数值">
                  <el-input-number v-model="hrForm.value" :min="20" :max="240" :precision="0" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="单位">
                  <el-input v-model="hrForm.unit" placeholder="bpm" class="glass-input" />
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="hrForm.note" placeholder="可选" class="glass-input" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="vitalsLoading.hr" @click="recordVitalsAction('心率')" round v-particles>保存到健康日志</el-button>
                  <el-button @click="goToLogs('vital')" round class="glass-button">去健康日志查看</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <el-col :xs="24" :md="12" class="stagger-anim" style="--delay: 0.4s">
            <el-card class="glass-card" shadow="hover">
              <template #header><span class="card-title"><el-icon><Monitor /></el-icon> 体温 / 体重</span></template>
              <el-form :model="tempWeightForm" label-width="120px">
                <el-form-item label="测量时间">
                  <el-time-picker v-model="tempWeightForm.time" format="HH:mm" value-format="HH:mm" placeholder="HH:mm" class="glass-input" style="width: 100%" />
                </el-form-item>
                <el-form-item label="体温(°C)">
                  <el-input-number v-model="tempWeightForm.temperature" :min="30" :max="45" :precision="1" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="体重(kg)">
                  <el-input-number v-model="tempWeightForm.weight" :min="20" :max="300" :precision="1" class="glass-input-number" style="width: 100%" />
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="tempWeightForm.note" placeholder="可选" class="glass-input" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="vitalsLoading.tempWeight" @click="recordTempWeight" round v-particles>保存到健康日志</el-button>
                  <el-button @click="goToLogs('vital')" round class="glass-button">去健康日志查看</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Camera, DataLine, Food, Bicycle, Moon, Sunrise, ChatDotRound, Monitor } from '@element-plus/icons-vue'
import { ingestDiet, weeklyDietReport, recommendRecipes, recordExercise, suggestExercise, recordSleep, analyzeSleep, uploadDietImage, recordMood, recordVitals } from '../../api/lifestyle'
import dayjs from 'dayjs'
import { useLogStore } from '../../stores'

const router = useRouter()
const logStore = useLogStore()

const dietForm = ref({ imageUrl: '', description: '', quantity: '' })
const dietLoading = ref(false)
const uploadLoading = ref(false)
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
  uploadLoading.value = true
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
  } finally {
    uploadLoading.value = false
  }
}

const sportForm = ref({ type: 'run', durationMinutes: 30, distanceKm: 3, steps: 5000 })
const sportLoading = ref(false)
const sportSuggestLoading = ref(false)
const sportSuggest = ref('')

const sleepForm = ref({ hours: 7, deepHours: 2, wakeCount: 1, bedtime: null, wakeTime: null, sleepLatency: 0, wakeUpLatency: 0 })
const sleepLoading = ref(false)
const sleepAnalyzeLoading = ref(false)
const sleepAnalyze = ref('')
const calculatedDuration = ref(null)

const moodForm = ref({ emotion: '平静', level: 3, stress: 3, energy: 7, note: '' })
const moodLoading = ref(false)

const bpForm = ref({ time: null, systolic: 120, diastolic: 80, note: '' })
const glucoseForm = ref({ time: null, value: 5.6, unit: 'mmol/L', note: '' })
const hrForm = ref({ time: null, value: 75, unit: 'bpm', note: '' })
const tempWeightForm = ref({ time: null, temperature: 36.5, weight: 60, note: '' })
const vitalsLoading = ref({ bp: false, glucose: false, hr: false, tempWeight: false })

// 自动计算睡眠时长
watch([() => sleepForm.value.bedtime, () => sleepForm.value.wakeTime], ([bed, wake]) => {
  if (bed && wake) {
    const today = dayjs().format('YYYY-MM-DD')
    let start = dayjs(`${today} ${bed}`)
    let end = dayjs(`${today} ${wake}`)
    
    // 如果起床时间小于上床时间，说明跨天了
    if (end.isBefore(start)) {
      end = end.add(1, 'day')
    }
    
    const diffHours = end.diff(start, 'hour', true)
    const val = Number(diffHours.toFixed(1))
    calculatedDuration.value = val
    sleepForm.value.hours = val
  } else {
    calculatedDuration.value = null
  }
})

const ingestDietAction = async () => {
  dietLoading.value = true
  try {
    const payload = { ...dietForm.value }
    const res = await ingestDiet(payload)
    if (res.code === 0) {
      dietItems.value = res?.data?.items || []
      dietTotal.value = res?.data?.totalCalories || 0
      ElMessage.success('已记录饮食')
      // 移除自动刷新报告，改为用户手动刷新
      // await loadWeeklyReport()
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

const goToLogs = (tab) => {
  logStore.setActiveTab(tab)
  router.push('/logs')
}

const recordMoodAction = async () => {
  if (!moodForm.value.level) {
    ElMessage.warning('请设置情绪强度')
    return
  }
  moodLoading.value = true
  try {
    const payload = {
      emotion: moodForm.value.emotion,
      level: moodForm.value.level,
      stress: moodForm.value.stress,
      energy: moodForm.value.energy,
      note: moodForm.value.note,
      time: dayjs().format('HH:mm')
    }
    const res = await recordMood(payload)
    if (res.code === 0) {
      ElMessage.success('已保存到健康日志')
      moodForm.value.note = ''
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    moodLoading.value = false
  }
}

const recordVitalsAction = async (type) => {
  const loadingKeyMap = { 血压: 'bp', 血糖: 'glucose', 心率: 'hr' }
  const key = loadingKeyMap[type]
  if (key) vitalsLoading.value[key] = true
  try {
    let payload = { type }
    if (type === '血压') {
      payload = { ...payload, systolic: bpForm.value.systolic, diastolic: bpForm.value.diastolic, unit: 'mmHg', note: bpForm.value.note, time: bpForm.value.time }
    } else if (type === '血糖') {
      payload = { ...payload, value: glucoseForm.value.value, unit: glucoseForm.value.unit, note: glucoseForm.value.note, time: glucoseForm.value.time }
    } else if (type === '心率') {
      payload = { ...payload, value: hrForm.value.value, unit: hrForm.value.unit, note: hrForm.value.note, time: hrForm.value.time }
    }
    const res = await recordVitals(payload)
    if (res.code === 0) {
      ElMessage.success('已保存到健康日志')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    if (key) vitalsLoading.value[key] = false
  }
}

const recordTempWeight = async () => {
  if (tempWeightForm.value.temperature == null && tempWeightForm.value.weight == null) {
    ElMessage.warning('请至少填写体温或体重')
    return
  }
  vitalsLoading.value.tempWeight = true
  try {
    const time = tempWeightForm.value.time
    const tasks = []
    if (tempWeightForm.value.temperature != null) {
      tasks.push(recordVitals({ type: '体温', value: tempWeightForm.value.temperature, unit: '°C', note: tempWeightForm.value.note, time }))
    }
    if (tempWeightForm.value.weight != null) {
      tasks.push(recordVitals({ type: '体重', value: tempWeightForm.value.weight, unit: 'kg', note: tempWeightForm.value.note, time }))
    }
    const results = await Promise.all(tasks)
    const ok = results.every(r => r.code === 0)
    if (ok) {
      ElMessage.success('已保存到健康日志')
    } else {
      ElMessage.error('部分保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    vitalsLoading.value.tempWeight = false
  }
}
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.lifestyle {
  padding: 24px;
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  animation: fadeInDown 0.6s vars.$ease-spring;
  gap: 16px;
  
  .header-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    background: linear-gradient(135deg, vars.$warning-color, color.adjust(vars.$warning-color, $lightness: 15%));
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(vars.$warning-color, 0.3);

    .el-icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .header-content {
    flex: 1;
    .title {
      font-size: 24px;
      font-weight: 700;
      color: vars.$text-main-color;
      margin: 0 0 4px 0;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-main-color, vars.$warning-color));
    }

    .subtitle {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

.glass-header {
  margin-bottom: 24px;
  :deep(.el-page-header__content) {
    font-weight: 600;
    color: vars.$text-main-color;
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
