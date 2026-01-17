import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import AiAssessment from '../src/views/Assessment/AiAssessment.vue'
import * as assessmentApi from '../src/api/assessment'

// 模拟Element Plus组件
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn()
  }
}))

// 模拟assessment API
vi.mock('../src/api/assessment', () => ({
  startAiAssessment: vi.fn(),
  processAiAnswer: vi.fn(),
  generateFinalAiAssessment: vi.fn()
}))

// 创建模拟路由
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/assessment/ai', component: AiAssessment }
  ]
})

describe('AiAssessment Component', () => {
  beforeEach(() => {
    // 清除所有模拟调用
    vi.clearAllMocks()
  })

  it('should render correctly', async () => {
    // 模拟API调用
    assessmentApi.startAiAssessment.mockResolvedValue({
      data: {
        sessionId: 'test_session_123',
        question: '您好！请告诉我您的基本情况，比如年龄和性别。'
      }
    })

    const wrapper = mount(AiAssessment, {
      global: {
        plugins: [router]
      }
    })

    // 等待组件挂载和API调用完成
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(wrapper.find('.ai-assessment-container').exists()).toBe(true)
    expect(wrapper.find('.chat-container').exists()).toBe(true)
  })

  it('should start assessment on mount', async () => {
    const mockResponse = {
      data: {
        sessionId: 'test_session_123',
        question: '您好！请告诉我您的基本情况，比如年龄和性别。'
      }
    }
    
    assessmentApi.startAiAssessment.mockResolvedValue(mockResponse)

    const wrapper = mount(AiAssessment, {
      global: {
        plugins: [router]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(assessmentApi.startAiAssessment).toHaveBeenCalled()
    expect(wrapper.vm.messages).toHaveLength(2) // 初始问候 + 第一个问题
  })

  it('should process user answer correctly', async () => {
    const mockStartResponse = {
      data: {
        sessionId: 'test_session_123',
        question: '您好！请告诉我您的基本情况，比如年龄和性别。'
      }
    }
    
    const mockProcessResponse = {
      data: {
        sessionId: 'test_session_123',
        question: '了解了，那您平时容易疲劳吗？',
        shouldEnd: false
      }
    }
    
    assessmentApi.startAiAssessment.mockResolvedValue(mockStartResponse)
    assessmentApi.processAiAnswer.mockResolvedValue(mockProcessResponse)

    const wrapper = mount(AiAssessment, {
      global: {
        plugins: [router]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 模拟用户输入和发送
    wrapper.vm.userInput = '我今年30岁，女性。'
    await wrapper.vm.sendAnswer()

    expect(assessmentApi.processAiAnswer).toHaveBeenCalledWith('test_session_123', '我今年30岁，女性。')
    expect(wrapper.vm.messages).toHaveLength(4) // 初始问候 + 第一个问题 + 用户回答 + 新问题
  })

  it('should handle assessment completion', async () => {
    const mockStartResponse = {
      data: {
        sessionId: 'test_session_123',
        question: '您好！请告诉我您的基本情况，比如年龄和性别。'
      }
    }
    
    const mockProcessResponse = {
      data: {
        sessionId: 'test_session_123',
        question: '感谢您的回答，正在为您分析体质特征...',
        shouldEnd: true
      }
    }
    
    const mockFinalResponse = {
      data: {
        primaryType: 'BALANCED',
        scores: { BALANCED: 60.0, QI_DEFICIENCY: 20.0 },
        confidence: 0.8,
        summary: '体质平衡，保持良好生活方式。',
        recommendations: ['保持规律作息', '坚持适量运动'],
        assessmentId: 123
      }
    }
    
    assessmentApi.startAiAssessment.mockResolvedValue(mockStartResponse)
    assessmentApi.processAiAnswer.mockResolvedValue(mockProcessResponse)
    assessmentApi.generateFinalAiAssessment.mockResolvedValue(mockFinalResponse)

    const wrapper = mount(AiAssessment, {
      global: {
        plugins: [router]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 模拟触发完成评估
    wrapper.vm.userInput = '我感觉身体还不错。'
    await wrapper.vm.sendAnswer()

    // 等待异步操作完成
    await new Promise(resolve => setTimeout(resolve, 200))

    expect(wrapper.vm.isAssessmentComplete).toBe(true)
    expect(wrapper.vm.resultData).not.toBeNull()
    expect(wrapper.vm.resultData.primaryType).toBe('BALANCED')
  })

  it('should handle skip question functionality', async () => {
    const mockStartResponse = {
      data: {
        sessionId: 'test_session_123',
        question: '您好！请告诉我您的基本情况，比如年龄和性别。'
      }
    }
    
    const mockProcessResponse = {
      data: {
        sessionId: 'test_session_123',
        question: '了解了，那您平时容易疲劳吗？',
        shouldEnd: false
      }
    }
    
    assessmentApi.startAiAssessment.mockResolvedValue(mockStartResponse)
    assessmentApi.processAiAnswer.mockResolvedValue(mockProcessResponse)

    const wrapper = mount(AiAssessment, {
      global: {
        plugins: [router]
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 模拟跳过问题
    await wrapper.vm.skipQuestion()

    expect(assessmentApi.processAiAnswer).toHaveBeenCalledWith('test_session_123', '我不太确定这个问题的答案，可以问下一个问题吗？')
  })
})
