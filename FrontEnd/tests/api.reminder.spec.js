import { describe, it, expect, vi, beforeEach } from 'vitest'

// Use vi.hoisted to make the mock available for the hoisted vi.mock call
const requestMock = vi.hoisted(() => ({
  post: vi.fn(() => Promise.resolve({ code: 0, data: {} })),
  get: vi.fn(() => Promise.resolve({ code: 0, data: [] })),
  put: vi.fn(() => Promise.resolve({ code: 0, data: {} })),
  delete: vi.fn(() => Promise.resolve({ code: 0, data: {} }))
}))

vi.mock('../src/utils/request', () => ({
  default: requestMock
}))

import { 
  createReminder, 
  generateSmartRemindersForUser 
} from '../src/api/reminder'

describe('Reminder API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('createReminder should send correct payload with assignedToUserId', async () => {
    const payload = {
      title: 'Test Reminder',
      content: 'Test Content',
      scheduledTime: '2025-12-31 10:00:00',
      type: 'MEDICATION',
      priority: 'HIGH',
      assignedToUserId: 123
    }

    await createReminder(payload)

    expect(requestMock.post).toHaveBeenCalledTimes(1)
    expect(requestMock.post).toHaveBeenCalledWith('/reminders', payload)
    
    // Verify assignedToUserId is passed as is (could be number or string)
    const callArg = requestMock.post.mock.calls[0][1]
    expect(callArg.assignedToUserId).toBe(123)
  })

  it('createReminder should handle null assignedToUserId', async () => {
    const payload = {
      title: 'Test Reminder',
      assignedToUserId: null
    }

    await createReminder(payload)

    expect(requestMock.post).toHaveBeenCalledTimes(1)
    const callArg = requestMock.post.mock.calls[0][1]
    expect(callArg.assignedToUserId).toBeNull()
  })

  it('generateSmartRemindersForUser should call correct endpoint with userId', async () => {
    const userId = 456
    await generateSmartRemindersForUser(userId)

    expect(requestMock.post).toHaveBeenCalledTimes(1)
    expect(requestMock.post).toHaveBeenCalledWith(`/reminders/generate/user/${userId}`, {})
  })
})
