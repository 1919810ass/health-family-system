import request from '@/utils/request'

export function getAuditSessions(params) {
  return request({
    url: '/admin/audit/chat/sessions',
    method: 'get',
    params
  })
}

export function getSessionMessages(sessionId) {
  return request({
    url: `/admin/audit/chat/sessions/${sessionId}/messages`,
    method: 'get'
  })
}

export function flagRisk(data) {
  return request({
    url: '/admin/audit/chat/flag-risk',
    method: 'post',
    data
  })
}
