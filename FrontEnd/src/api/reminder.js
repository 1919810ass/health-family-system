import request from '../utils/request'

export const createReminder = (payload) => request.post('/reminders', payload)
export const getUserReminders = (params) => request.get('/reminders', { params })
export const updateReminderStatus = (id, payload) => request.put(`/reminders/${id}/status`, payload)
export const deleteReminder = (id) => request.delete(`/reminders/${id}`)
export const generateSmartReminders = () => request.post('/reminders/generate', {})
export const generateCollaborationReminders = (familyId) => request.post('/reminders/generate/collaboration', {}, { params: { familyId } })
export const getFamilyReminders = (familyId) => request.get('/reminders', { params: { familyId } })
export const getUserTodoItems = () => request.get('/reminders/todo')
export const generateSmartRemindersForUser = (targetUserId) => request.post(`/reminders/generate/user/${targetUserId}`, {})

