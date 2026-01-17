import request from '../utils/request'

// 医生端健康监测API
export const getMonitoringData = (familyId) => request.get('/doctor/monitoring', { params: { familyId } })
export const markAlertAsHandled = (alertId) => request.post(`/doctor/monitoring/alerts/${alertId}/handle`)

// 增强的医生端监测API
export const getEnhancedMonitoringData = (familyId) => request.get('/doctor/monitoring', { params: { familyId } })
export const handleAlert = (alertId, data) => request.post(`/doctor/monitoring/alerts/${alertId}/handle`, data)
export const batchHandleAlerts = (data) => request.post('/doctor/monitoring/alerts/batch-handle', data)
export const sendPatientNotification = (data) => request.post('/doctor/monitoring/notifications/send', data)
export const getHandlingHistory = (familyId, userId) => {
  const params = { familyId };
  if (userId) params.userId = userId;
  return request.get('/doctor/monitoring/handling-history', { params });
}

export const ingestTelemetry = (payload) => request.post('/monitor/ingest', payload)
export const getAlerts = (params) => request.get('/monitor/alerts', { params })
export const ackAlert = (id) => request.put(`/monitor/alerts/${id}/ack`)
export const getThresholds = () => request.get('/monitor/thresholds')
export const optimizeThresholds = () => request.post('/monitor/thresholds/optimize', {})

