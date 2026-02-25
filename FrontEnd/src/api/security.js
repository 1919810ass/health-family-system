/**
 * 前端接口封装：security.js
 *
 * 统一封装与后端 /api 路径下接口的调用，供页面与状态管理层复用。
 */

import request from '../utils/request'

export const exportData = () => request.get('/security/export')
export const deleteData = () => request.delete('/security/data')

export const getPrivacySettings = () => request.get('/security/privacy')
export const updatePrivacySettings = (data) => request.put('/security/privacy', data)

export const getSecurityActivities = (params) => request.get('/security/activities', { params })
