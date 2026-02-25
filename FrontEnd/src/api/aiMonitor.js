import request from '@/utils/request'

export function getAiDashboardStats() {
  return request({
    url: '/admin/ai-monitor/dashboard',
    method: 'get'
  })
}
