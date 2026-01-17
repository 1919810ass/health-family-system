import request from '../utils/request'

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
