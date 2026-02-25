/**
 * 前端接口封装：report.js
 *
 * 统一封装与后端 /api 路径下接口的调用，供页面与状态管理层复用。
 */

import request from '../utils/request'

const withUser = (config = {}, userId) => {
  const devUserId = localStorage.getItem('dev_user_id') || '4'
  const finalUserId = userId || devUserId
  const headers = Object.assign({ 'X-User-Id': finalUserId }, config.headers || {})
  return { ...config, headers }
}

export const submitReport = (data, userId) => {
  return request.post('/reports', data, withUser({}, userId))
}

export const getUserReports = (userId) => {
  return request.get('/reports', withUser({}, userId))
}

export const getReportDetail = (id, userId) => {
  return request.get(`/reports/${id}`, withUser({}, userId))
}

export const getReportStatus = (id, userId) => {
  return request.get(`/reports/${id}/status`, withUser({}, userId))
}

export const uploadReportImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
