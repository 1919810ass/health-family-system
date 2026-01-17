import request from '../utils/request'

const withUser = (config = {}, userId) => {
  const devUserId = localStorage.getItem('dev_user_id') || '4'
  // 优先使用传入的 userId，否则使用 devUserId
  const finalUserId = userId || devUserId
  const headers = Object.assign({ 'X-User-Id': finalUserId }, config.headers || {})
  return { ...config, headers }
}

export const getLogs = (params = {}) => {
  const { userId, ...rest } = params
  return request.get('/logs', withUser({ params: rest }, userId))
}

export const createLog = (payload, userId) => request.post('/logs', payload, withUser({}, userId))

export const getStatistics = (userId) => request.get('/logs/statistics', withUser({}, userId))

export const parseVoiceInput = (payload, userId) => request.post('/logs/voice', payload, withUser({}, userId))

export const parseOcrData = (payload, userId) => request.post('/logs/ocr', payload, withUser({}, userId))

export const syncDeviceData = (payload, userId) => request.post('/logs/device/sync', payload, withUser({}, userId))

export const getAbnormalLogs = (userId) => request.get('/logs/abnormal', withUser({}, userId))

export const optimizeDietText = (payload, userId) => request.post('/logs/diet/optimize', payload, withUser({}, userId))

export const optimizeInput = (payload, userId) => request.post('/logs/optimize', payload, withUser({}, userId))

export const deleteLog = (id, userId) => request.delete(`/logs/${id}`, withUser({}, userId))
