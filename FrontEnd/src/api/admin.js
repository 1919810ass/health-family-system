import request from '../utils/request'
import { ElMessage } from 'element-plus'
// 用户管理相关API
export const getUsers = (params) => request.get('/admin/users', { params })
export const getUserById = (id) => request.get(`/admin/users/${id}`)
export const createUser = (data) => request.post('/admin/users', data)
export const updateUser = (id, data) => request.put(`/admin/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/admin/users/${id}`)
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, { status })
export const resetUserPassword = (id, password) => request.put(`/admin/users/${id}/password`, { password })
export const forceLogout = (id) => request.post(`/admin/users/${id}/logout`)
export const lockUser = (id) => request.post(`/admin/users/${id}/lock`)
export const unlockUser = (id) => request.post(`/admin/users/${id}/unlock`)
export const approveUser = (id) => request.post(`/admin/users/${id}/audit/approve`)
export const rejectUser = (id, reason) => request.post(`/admin/users/${id}/audit/reject`, { reason })

// 家庭管理相关API
export const getFamilies = (params) => request.get('/admin/families', { params })
export const getFamilyById = (id) => request.get(`/admin/families/${id}`)
export const createFamily = (data) => request.post('/admin/families', data)
export const updateFamily = (id, data) => request.put(`/admin/families/${id}`, data)
export const deleteFamily = (id) => request.delete(`/admin/families/${id}`)
export const updateFamilyStatus = (id, status) => request.put(`/admin/families/${id}/status`, { status })

// 健康日志管理相关API
export const getHealthLogs = (params) => request.get('/admin/health/logs', { params })
export const getHealthLogById = (id) => request.get(`/admin/health/logs/${id}`)
export const updateHealthLog = (id, data) => request.put(`/admin/health/logs/${id}`, data)
export const deleteHealthLog = (id) => request.delete(`/admin/health/logs/${id}`)
export const getHealthLogStats = () => request.get('/admin/health/logs/stats')

// 健康提醒管理相关API
export const getHealthReminders = (params) => request.get('/admin/health/reminders', { params })
export const getHealthReminderById = (id) => request.get(`/admin/health/reminders/${id}`)
export const createHealthReminder = (data) => request.post('/admin/health/reminders', data)
export const updateHealthReminder = (id, data) => request.put(`/admin/health/reminders/${id}`, data)
export const deleteHealthReminder = (id) => request.delete(`/admin/health/reminders/${id}`)
export const updateHealthReminderStatus = (id, status) => request.put(`/admin/health/reminders/${id}/status`, { status })
export const sendTestReminder = (id) => request.post(`/admin/health/reminders/${id}/test`)

// 医生管理相关API
export const getDoctors = (params) => request.get('/admin/doctors', { params })
export const getDoctorById = (id) => request.get(`/admin/doctors/${id}`)
export const getDoctorStats = () => request.get('/admin/doctors/stats')
export const getPendingDoctors = (params) => request.get('/admin/doctors/pending', { params })
export const approveDoctor = (id) => request.post(`/admin/doctors/${id}/approve`)
export const rejectDoctor = (id, rejectReason) => request.post(`/admin/doctors/${id}/reject`, { rejectReason })
export const createDoctor = (data) => request.post('/admin/doctors', data)
export const updateDoctor = (id, data) => request.put(`/admin/doctors/${id}`, data)
export const deleteDoctor = (id) => request.delete(`/admin/doctors/${id}`)
export const updateDoctorStatus = (id, status) => request.put(`/admin/doctors/${id}/status`, null, { params: { status } })
export const updateDoctorCertification = (id, certified) => request.put(`/admin/doctors/${id}/certification`, null, { params: { certified } })

// 系统配置相关API
export const getSystemConfig = () => request.get('/admin/system/config')
export const updateSystemConfig = (data) => request.put('/admin/system/config', data)
export const backupSystemConfig = () => request.get('/admin/system/config/backup')
export const restoreSystemConfig = (data) => request.post('/admin/system/config/restore', data)
export const resetSystemConfig = () => request.delete('/admin/system/config/reset')

// 系统监控相关API
export const getSystemStats = () => request.get('/admin/monitoring/system/stats')
export const getApiPerformance = (params) => request.get('/admin/monitoring/api/performance', { params })
export const getSystemAlerts = (params) => request.get('/admin/monitoring/alerts', { params })
export const acknowledgeAlert = (id) => request.put(`/admin/monitoring/alerts/${id}/acknowledge`)
export const resolveAlert = (id) => request.put(`/admin/monitoring/alerts/${id}/resolve`)

// 用户活动监控相关API
export const getUserActivityStats = (params) => request.get('/admin/monitoring/user/activity/stats', { params })
export const getOnlineUsers = () => request.get('/admin/monitoring/user/online')
export const getBehaviorAnalysis = (params) => request.get('/admin/monitoring/user/behavior', { params })
export const getLoginLogs = (params) => request.get('/admin/monitoring/user/login-logs', { params })

// 数据报告相关API
export const getDataReports = (params) => request.get('/admin/monitoring/data/reports', { params })
export const getQualityReport = () => request.get('/admin/monitoring/data/quality')

// 自定义报告相关API
export const getCustomReport = (config) => request.post('/admin/monitoring/custom-reports/generate', config)
export const saveReportTemplate = (name, config) => request.post('/admin/monitoring/custom-reports/templates', config, { params: { name } })
export const getSavedTemplates = () => request.get('/admin/monitoring/custom-reports/templates')
export const getTemplate = (id) => request.get(`/admin/monitoring/custom-reports/templates/${id}`)

// 审计日志相关API
export const getAuditLogs = (params) => request.get('/admin/audit/logs', { params })
