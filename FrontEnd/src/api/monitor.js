import request from '@/utils/request'

export function getSystemMetrics() {
  return request({
    url: '/monitor/metrics',
    method: 'get'
  })
}

