/**
 * 前端接口封装：ops.js
 *
 * 统一封装与后端 /api 路径下接口的调用，供页面与状态管理层复用。
 */

import request from '../utils/request'

export const fetchLogs = (params) => request.get('/admin/ops/logs', { params })
export const analyzeLogs = (params) => request.post('/admin/ops/logs/ai-analysis', null, { params })
export const systemReport = (params) => request.get('/admin/ops/reports/system', { params })
export const familyTrendReport = (params) => request.get('/admin/ops/reports/family-trend', { params })
export const getSettings = () => request.get('/admin/ops/settings')
export const updateSettings = (data) => request.put('/admin/ops/settings', data)
export const aiSystemDiagnose = () => request.get('/admin/ops/ai-diagnose')
export const fetchErrorLogs = () => request.get('/admin/ops/logs/errors')
export const getMaintenanceMode = () => request.get('/admin/ops/maintenance')
export const setMaintenanceMode = (enable) => request.post('/admin/ops/maintenance', { enable })

// 新增：获取登录日志
export const fetchLoginLogs = (params) => request.get('/admin/logs/login', { params })
