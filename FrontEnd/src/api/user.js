import request from '../utils/request'

export const getProfile = () => request.get('/user/profile')
export const updateProfile = (payload) => request.put('/user/profile', payload)
export const getHealthProfile = () => request.get('/user/profile/health')
export const updateHealthProfile = (payload) => request.put('/user/profile/health', payload)
export const listFamilies = () => request.get('/user/families')
export const switchFamily = (familyId) => request.post(`/user/families/${familyId}/switch`)
export const updateAvatar = (formData) => request.post('/user/avatar', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const changePassword = (payload) => request.post('/user/password', payload)
export const updateNotifications = (payload) => request.put('/user/notifications', payload)
