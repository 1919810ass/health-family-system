/**
 * 前端接口封装：monitor.js
 *
 * 统一封装与后端 /api 路径下接口的调用，供页面与状态管理层复用。
 */

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

// 获取服务器实时监控数据 (CPU, 内存, 线程)
export function getSystemMetrics() {
  return request({
    url: '/api/monitor/metrics',
    method: 'get'
  })
}

