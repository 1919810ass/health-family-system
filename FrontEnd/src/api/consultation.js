import request from '../utils/request'
import { getToken } from '../utils/auth'

export const getSession = (sessionId) => request.get(`/consultations/sessions/${sessionId}`)
export const getOrCreateSession = (params) => request.get('/consultations/sessions', { params })
export const createSession = (payload) => request.post('/consultations/sessions', payload)

export const getSessionMessages = (sessionId) => request.get(`/consultations/sessions/${sessionId}/messages`)
export const getMessages = getSessionMessages

export const sendMessage = (payload) => request.post('/consultations/messages', payload)

export const markMessagesAsRead = (sessionId) => request.post(`/consultations/sessions/${sessionId}/read`)
export const markRead = markMessagesAsRead

export const listSessionsForDoctor = (familyId) => request.get('/consultations/sessions/doctor', { params: { familyId } })

export const closeSession = (sessionId) => request.post(`/consultations/sessions/${sessionId}/close`)

// AI Consultation (Health Consultant)
export const consult = (data) => request.post('/consultation', data)
export const consultStream = async (data, onMessage, onError, onComplete) => {
  try {
    const token = getToken()
    const response = await fetch('/api/consultation/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        'Accept': 'text/event-stream'
      },
      cache: 'no-store',
      body: JSON.stringify(data)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      buffer += chunk

      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.trim() === '') continue

        if (line.startsWith('data:')) {
          let content = line.replace(/^data:\s*/, '')
          if (content.startsWith('data:')) {
            content = content.replace(/^data:\s*/, '')
          }
          if (content.trim() === '[DONE]') continue
          if (content) {
            onMessage(content)
          }
        }
      }
    }

    if (buffer && buffer.startsWith('data:')) {
      const content = buffer.substring(5)
      if (content) onMessage(content)
    }

    if (onComplete) onComplete()
  } catch (error) {
    if (onError) onError(error)
  }
}
export const getConsultationHistory = (params) => request.get('/consultation/history', { params })
export const feedbackConsultation = (id, data) => request.post(`/consultation/${id}/feedback`, data)
export const getSessionDetail = (sessionId) => request.get('/consultation/history', { params: { sessionId } })
export const getConsultationStats = (params) => Promise.reject(new Error('Not implemented'))
export const saveSessionMeta = (data) => Promise.resolve()

// AI Triage
export const triageChat = (data) => request.post('/consultation/triage/chat', data)
export const finishTriage = (data) => request.post('/consultation/triage/finish', data)
