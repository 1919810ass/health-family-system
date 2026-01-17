import request from '../utils/request'

export const fetchLogs = (params) => request.get('/admin/ops/logs', { params })
export const analyzeLogs = (params) => request.post('/admin/ops/logs/ai-analysis', null, { params })
export const systemReport = (params) => request.get('/admin/ops/reports/system', { params })
export const familyTrendReport = (params) => request.get('/admin/ops/reports/family-trend', { params })
export const getSettings = () => request.get('/admin/ops/settings')
export const updateSettings = (data) => request.put('/admin/ops/settings', data)

// 新增：获取登录日志
export const fetchLoginLogs = (params) => request.get('/admin/logs/login', { params })
