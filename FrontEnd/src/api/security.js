import request from '../utils/request'

export const exportData = () => request.get('/security/export')
export const deleteData = () => request.delete('/security/data')

export const getPrivacySettings = () => request.get('/security/privacy')
export const updatePrivacySettings = (data) => request.put('/security/privacy', data)

export const getSecurityActivities = (params) => request.get('/security/activities', { params })
