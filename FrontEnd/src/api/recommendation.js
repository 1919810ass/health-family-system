/**
 * 前端接口封装：recommendation.js
 *
 * 统一封装与后端 /api 路径下接口的调用，供页面与状态管理层复用。
 */

import request from '../utils/request'

// 统一用户ID头的注入逻辑：优先使用本地开发配置，其次回退到 4
const withUser = (config = {}, userId) => {
  const devUserId = localStorage.getItem('dev_user_id') || '4'
  const finalUserId = userId || devUserId
  const headers = Object.assign({ 'X-User-Id': finalUserId }, config.headers || {})
  return { ...config, headers }
}

export const fetchRecommendations = (params = {}) => {
  const { userId, ...rest } = params
  return request.get('/recommendations', withUser({ params: rest }, userId))
}

export const generateRecommendations = (payload, userId) =>
  request.post('/recommendations/generate', payload, withUser({}, userId))

export const sendFeedback = (id, payload, userId) =>
  request.post(`/recommendations/${id}/feedback`, payload, withUser({}, userId))
