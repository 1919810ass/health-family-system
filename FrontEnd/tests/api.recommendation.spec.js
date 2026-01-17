import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../src/utils/request', () => {
  return {
    default: {
      post: vi.fn((url, payload, config) => {
        return Promise.resolve({ url, payload, config })
      }),
      get: vi.fn((url, config) => {
        return Promise.resolve({ url, config })
      }),
    },
  }
})

// 使用真实模块以测试 withUser 头部注入逻辑
import { generateRecommendations, fetchRecommendations } from '../src/api/recommendation'
import request from '../src/utils/request'

describe('recommendation api user header', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    localStorage.setItem('dev_user_id', '5')
  })

  it('should inject X-User-Id from localStorage in POST /recommendations/generate', async () => {
    const payload = { date: '2025-12-27' }
    const res = await generateRecommendations(payload)
    expect(request.post).toHaveBeenCalledTimes(1)
    const call = request.post.mock.calls[0]
    expect(call[0]).toBe('/recommendations/generate')
    expect(call[1]).toEqual(payload)
    expect(call[2]).toBeDefined()
    expect(call[2].headers['X-User-Id']).toBe('5')
  })

  it('should inject X-User-Id from localStorage in GET /recommendations', async () => {
    const res = await fetchRecommendations({ date: '2025-12-27' })
    expect(request.get).toHaveBeenCalledTimes(1)
    const call = request.get.mock.calls[0]
    expect(call[0]).toBe('/recommendations')
    expect(call[1]).toBeDefined()
    expect(call[1].headers['X-User-Id']).toBe('5')
  })
})

