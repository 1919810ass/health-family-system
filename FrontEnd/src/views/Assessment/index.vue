<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><DataAnalysis /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">体质测评</h2>
        <p class="subtitle">基于中医体质辨识，了解您的身体状况</p>
      </div>
    </div>
    
    <el-card v-if="!schema" v-loading="loadingSchema" element-loading-text="加载问卷中..." class="glass-card">
      <el-empty description="暂无测评问卷" />
    </el-card>

    <el-card v-else class="glass-card">
      <div class="assessment-header">
        <el-button type="primary" plain round @click="goToAiAssessment" style="margin-bottom: 20px;" v-particles>
          <el-icon><ChatDotRound /></el-icon>
          尝试AI对话式测评
        </el-button>
      </div>
      <el-steps v-if="schema && schema.steps" :active="currentStep" finish-status="success" align-center>
        <el-step v-for="(step, idx) in schema.steps" :key="idx" :title="step.title" />
      </el-steps>

      <div class="step-content">
        <div v-if="schema && schema.steps && currentStep < schema.steps.length" class="question-area">
          <h3 class="step-title">{{ schema.steps[currentStep].title }}</h3>
          <el-form label-position="top" @submit.prevent="handleNext">
            <div v-for="q in schema.steps[currentStep].questions" :key="q.id" class="question-item">
              <el-form-item :label="q.title" :rules="[{ required: true, message: '请选择' }]">
                <el-radio-group v-model="answers[q.id]">
                  <el-radio v-for="opt in q.options" :key="opt.value" :label="opt.value">
                    {{ opt.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
          </el-form>
        </div>

        <!-- 结果展示 -->
        <div v-if="result" class="result-area">
          <h3 class="step-title">测评结果</h3>
          <div class="result-chart">
            <div ref="radarRef" style="width: 100%; height: 320px" />
          </div>
          <div class="result-desc">
            <p>{{ result.report?.summary }}</p>
            <ul>
              <li v-for="(rec, idx) in (result.report?.recommendations || [])" :key="idx">{{ rec }}</li>
            </ul>
          </div>
        </div>
      </div>

      <div class="step-actions">
        <el-button v-if="currentStep > 0 && !result" round @click="handlePrev">上一步</el-button>
        <el-button v-if="schema && schema.steps && currentStep < schema.steps.length - 1 && !result" type="primary" round @click="handleNext" :loading="loadingNext" v-particles>下一步</el-button>
        <el-button v-if="schema && schema.steps && currentStep === schema.steps.length - 1 && !result" type="primary" round @click="handleSubmit" :loading="loadingSubmit" v-particles>提交测评</el-button>
        <el-button v-if="result" type="primary" round @click="handleHistory">查看历史</el-button>
        <el-button v-if="result" round @click="exportReport">导出报告图片</el-button>
        <el-button v-if="result" type="success" round @click="goToAiAssessment" v-particles>尝试AI测评</el-button>
      </div>
    </el-card>

    <!-- 历史对比弹窗 -->
    <el-dialog v-model="historyVisible" title="历史测评对比" width="800px" top="8vh">
      <div v-if="history.length" class="history-chart">
        <div ref="historyRef" style="width: 100%; height: 360px" />
      </div>
      <el-empty v-else description="暂无历史记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useAssessmentStore } from '../../stores'
import { submitAssessment, getResult, getHistory } from '../../api/assessment'
import { formatDate } from '../../utils/request'
import { ChatDotRound, DataAnalysis } from '@element-plus/icons-vue'
import { TCM_QUESTIONS, QUESTION_OPTIONS, calculateScore } from '../../utils/tcm-questionnaire'
import { getConstitutionName } from '../../utils/tcm-constants'

const router = useRouter()
const store = useAssessmentStore()
const loadingSchema = ref(false)
const loadingNext = ref(false)
const loadingSubmit = ref(false)
const historyVisible = ref(false)

const schema = ref(store.schema)
const currentStep = ref(store.currentStep)
const answers = ref(store.answers || {})
const result = ref(store.result)
const history = ref(store.history)

const radarRef = ref()
const historyRef = ref()

// 分页显示题目，每页5题
const QUESTIONS_PER_PAGE = 5
const totalPages = Math.ceil(TCM_QUESTIONS.length / QUESTIONS_PER_PAGE)

onMounted(async () => {
  if (!schema.value) {
    loadingSchema.value = true
    try {
      // 构造问卷结构
      const steps = []
      for (let i = 0; i < totalPages; i++) {
        const start = i * QUESTIONS_PER_PAGE
        const end = start + QUESTIONS_PER_PAGE
        const pageQuestions = TCM_QUESTIONS.slice(start, end)
        
        steps.push({
          title: `第 ${i + 1} 部分`,
          questions: pageQuestions.map(q => ({
            id: q.id,
            title: q.content,
            options: QUESTION_OPTIONS
          }))
        })
      }
      
      // 模拟后端返回 schema 结构
      schema.value = {
        type: 'standard_v1',
        dimensions: [], // 不再直接使用维度作为题目
        steps: steps
      }
      
      store.setSchema(schema.value)
      
      // 初始化答案
      if (!Object.keys(answers.value).length) {
        answers.value = {}
        TCM_QUESTIONS.forEach(q => { 
          answers.value[q.id] = null 
        })
      }
    } catch (e) {
      ElMessage.error('加载问卷失败')
    } finally {
      loadingSchema.value = false
    }
  }
})

function handlePrev() {
  if (currentStep.value > 0) {
    currentStep.value -= 1
    store.setStep(currentStep.value)
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

async function handleNext() {
  const step = schema.value.steps[currentStep.value]
  // 检查是否有未答题目
  const unanswered = step.questions.some(q => !answers.value[q.id])
  
  if (unanswered) {
    ElMessage.warning('请回答当前页所有问题')
    return
  }
  
  loadingNext.value = true
  setTimeout(() => {
    currentStep.value += 1
    store.setStep(currentStep.value)
    store.setAnswers(answers.value) // 保存中间状态
    loadingNext.value = false
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }, 300)
}

async function handleSubmit() {
  const step = schema.value.steps[currentStep.value]
  const unanswered = step.questions.some(q => !answers.value[q.id])
  if (unanswered) {
    ElMessage.warning('请回答完所有问题后再提交')
    return
  }

  loadingSubmit.value = true
  try {
    // 1. 计算各维度得分
    const dimensionScores = []
    const types = [
      'QI_DEFICIENCY', 'YANG_DEFICIENCY', 'YIN_DEFICIENCY', 'PHLEGM_DAMPNESS', 
      'DAMP_HEAT', 'BLOOD_STASIS', 'QI_STAGNATION', 'SPECIAL_DIATHESIS', 'BALANCED'
    ]
    
    types.forEach(type => {
      const typeQuestions = TCM_QUESTIONS.filter(q => q.type === type)
      const score = calculateScore(type, typeQuestions, answers.value)
      dimensionScores.push({ dimension: type, score: Math.round(score) })
    })

    // 2. 构造提交 payload
    const payload = {
      type: 'standard_v1',
      answers: dimensionScores, // 后端接收的是维度分
    }
    
    const res = await submitAssessment(payload)
    const id = res?.data?.id ?? res?.id
    const rRes = await getResult(id)
    result.value = rRes.data || rRes
    store.setResult(result.value)
    store.setAnswers({}) // 提交成功后清空暂存答案
    ElMessage.success('测评完成')
    nextTick(() => drawRadar())
  } catch (e) {
    console.error(e)
    ElMessage.error('提交失败')
  } finally {
    loadingSubmit.value = false
  }
}

async function handleHistory() {
  historyVisible.value = true
  if (!history.value.length) {
    try {
      const res = await getHistory()
      history.value = res.data
      store.setHistory(res.data)
      nextTick(() => drawHistory())
    } catch (e) {
      ElMessage.error('获取历史失败')
    }
  } else {
    nextTick(() => drawHistory())
  }
}

function goToAiAssessment() {
  router.push('/tcm/ai')
}

function drawRadar() {
  if (!result.value) return
  const chart = echarts.init(radarRef.value)
  const scores = result.value.report?.scores || {}
  const indicator = Object.keys(scores).map(code => {
    return { name: getConstitutionName(code), max: 100 }
  })
  const value = Object.values(scores)
  chart.setOption({
    radar: { indicator },
    series: [{
      type: 'radar',
      data: [{ value, name: '当前体质' }],
      areaStyle: { opacity: 0.3 }
    }]
  })
}

function drawHistory() {
  if (!history.value.length) return
  const chart = echarts.init(historyRef.value)
  const dates = history.value.map(h => formatDate(h.createdAt, 'YYYY-MM-DD HH:mm'))
  const series = []
  const types = Object.keys(history.value[0].scores)
  types.forEach(type => {
    series.push({
      name: getConstitutionName(type),
      type: 'line',
      data: history.value.map(h => h.scores[type])
    })
  })
  chart.setOption({
    legend: { data: types.map(getConstitutionName) },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', max: 100 },
    series
  })
}

async function exportReport() {
  if (!result.value) return
  const chart = echarts.getInstanceByDom(radarRef.value)
  const img = chart?.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#ffffff' })
  const canvas = document.createElement('canvas')
  const width = 900
  const height = 1200
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, width, height)
  ctx.fillStyle = '#333333'
  ctx.font = 'bold 26px system-ui'
  ctx.fillText('体质测评报告', 40, 60)
  ctx.font = '16px system-ui'
  ctx.fillText(`生成时间：${formatDate(new Date(), 'YYYY-MM-DD HH:mm')}`, 40, 90)
  const primary = result.value?.primaryType || '未知'
  ctx.fillText(`主导体质：${getConstitutionName(primary)}`, 40, 120)
  const conf = result.value?.report?.confidence
  if (conf != null) ctx.fillText(`置信度：${conf}`, 300, 120)
  if (img) {
    const image = new Image()
    image.src = img
    await new Promise(r => { image.onload = r })
    ctx.drawImage(image, 40, 150, 820, 420)
  }
  ctx.font = 'bold 18px system-ui'
  ctx.fillText('文字解读', 40, 600)
  ctx.font = '16px system-ui'
  const summary = String(result.value?.report?.summary || '')
  wrapText(ctx, summary, 40, 630, 820, 22)
  ctx.font = 'bold 18px system-ui'
  ctx.fillText('行动清单', 40, 740)
  ctx.font = '16px system-ui'
  const recs = result.value?.report?.recommendations || []
  let y = 770
  recs.slice(0, 8).forEach(text => {
    y = wrapText(ctx, `• ${text}`, 40, y, 820, 22) + 8
  })
  const a = document.createElement('a')
  a.href = canvas.toDataURL('image/png')
  a.download = `体质测评报告_${formatDate(new Date(), 'YYYYMMDD_HHmm')}.png`
  a.click()
}

function wrapText(ctx, text, x, y, maxWidth, lineHeight) {
  const words = String(text).split(' ')
  let line = ''
  for (let n = 0; n < words.length; n++) {
    const testLine = line + words[n] + ' '
    const metrics = ctx.measureText(testLine)
    const testWidth = metrics.width
    if (testWidth > maxWidth && n > 0) {
      ctx.fillText(line, x, y)
      line = words[n] + ' '
      y += lineHeight
    } else {
      line = testLine
    }
  }
  ctx.fillText(line, x, y)
  return y + lineHeight
}
</script>

<style scoped lang="scss">
@use "sass:map";
@use "sass:color";
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.page-container {
  min-height: 100%;
  padding: 24px;
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
.mb-24 { margin-bottom: 24px; }

.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  padding: 20px;
  transition: all 0.3s vars.$ease-spring;
}

.assessment-header {
  margin-bottom: 24px;
}

.step-content {
  margin: 32px 0;
  min-height: 300px;
  animation: slideInUp 0.6s vars.$ease-spring;
}

@keyframes slideInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 24px;
  color: map.get(vars.$colors, 'text-main');
  text-align: center;
}

.question-item {
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: vars.$radius-base;
  transition: all 0.3s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.8);
    transform: translateX(5px);
  }
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.result-area {
  animation: fadeIn 0.8s ease;
  
  .result-desc {
    margin-top: 24px;
    padding: 20px;
    background: rgba(vars.$primary-color, 0.05);
    border-radius: vars.$radius-base;
    line-height: 1.6;
    
    p {
      font-size: 16px;
      font-weight: 600;
      color: map.get(vars.$colors, 'text-main');
      margin-bottom: 12px;
    }
    
    ul {
      padding-left: 20px;
      color: map.get(vars.$colors, 'text-secondary');
    }
  }
}
</style>