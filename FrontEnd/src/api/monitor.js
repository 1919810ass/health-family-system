import request from '@/utils/request'

export function getSystemMetrics() {
  return request({
    url: '/monitor/metrics',
    method: 'get'
  })
}

export function getEnhancedMonitoringData(familyId) {
  return request({
    url: '/doctor/monitoring',
    method: 'get',
    params: { familyId }
  })
}

export function handleAlert(alertId, data) {
  return request({
    url: `/doctor/monitoring/alerts/${alertId}/handle`,
    method: 'post',
    data
  })
}

export function batchHandleAlerts(data) {
  return request({
    url: '/doctor/monitoring/alerts/batch-handle',
    method: 'post',
    data
  })
}
