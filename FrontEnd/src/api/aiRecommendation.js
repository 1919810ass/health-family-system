import request from '../utils/request'

export const generateRecommendation = (payload) => request.post('/ai-recommendations', payload)
export const getUserRecommendations = (params) => request.get('/ai-recommendations', { params })
export const feedbackRecommendation = (id, payload) => request.post(`/ai-recommendations/${id}/feedback`, payload)

