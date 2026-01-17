import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAssessmentStore = defineStore('assessment', () => {
  const schema = ref(null) // 问卷结构
  const currentStep = ref(0) // 当前步骤
  const answers = ref({}) // 答案缓存
  const result = ref(null) // 测评结果
  const history = ref([]) // 历史记录

  function setSchema(data) {
    schema.value = data
  }

  function setStep(step) {
    currentStep.value = step
  }

  function saveAnswer(qId, value) {
    answers.value[qId] = value
  }

  function setResult(data) {
    result.value = data
  }

  function setHistory(list) {
    history.value = list
  }

  function setAnswers(data) {
    answers.value = data
  }

  function reset() {
    currentStep.value = 0
    answers.value = {}
    result.value = null
  }

  return {
    schema,
    currentStep,
    answers,
    result,
    history,
    setSchema,
    setStep,
    saveAnswer,
    setAnswers,
    setResult,
    setHistory,
    reset,
  }
})