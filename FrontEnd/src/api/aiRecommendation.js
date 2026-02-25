/**
 * 前端接口封装：aiRecommendation.js
 *
 * 统一封装与后端 /api 路径下接口的调用，供页面与状态管理层复用。
 */

import request from '../utils/request'

export const generateRecommendation = (payload) => request.post('/ai-recommendations', payload)
export const getUserRecommendations = (params) => request.get('/ai-recommendations', { params })
export const feedbackRecommendation = (id, payload) => request.post(`/ai-recommendations/${id}/feedback`, payload)

