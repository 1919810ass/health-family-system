import request from '../utils/request'

export const listMyFamilies = () => request.get('/user/families')
export const switchFamily = (familyId) => request.post(`/user/families/${familyId}/switch`)

// 医生端专用接口
export const getDoctorFamilies = () => request.get('/doctor/families')
export const getBoundFamilyMembers = (familyId) => request.get(`/doctor/families/${familyId}/members`)
export const getPatientDetail = (familyId, patientUserId) => request.get(`/doctor/families/${familyId}/patients/${patientUserId}/detail`)
export const togglePatientImportant = (familyId, patientUserId, isImportant) => request.post(`/doctor/families/${familyId}/patients/${patientUserId}/toggle-important`, { isImportant })
export const updatePatientTags = (familyId, patientUserId, tags) => request.post(`/doctor/families/${familyId}/patients/${patientUserId}/tags`, tags)

// 病历记录相关API
export const listDoctorNotes = (familyId, memberId) => request.get(`/doctor/families/${familyId}/members/${memberId}/notes`)
export const getDoctorNote = (noteId) => request.get(`/doctor/notes/${noteId}`)
export const createDoctorNote = (familyId, memberId, data) => request.post(`/doctor/families/${familyId}/members/${memberId}/notes`, data)
export const updateDoctorNote = (noteId, data) => request.put(`/doctor/notes/${noteId}`, data)
export const deleteDoctorNote = (noteId) => request.delete(`/doctor/notes/${noteId}`)

// 诊断工具相关API
export const generateRecommendationForPatient = (familyId, memberId, data) => request.post(`/doctor/families/${familyId}/members/${memberId}/recommendations/generate`, data)

// 健康计划相关API
export const listHealthPlans = (familyId, memberId, params = {}) => {
  const query = new URLSearchParams()
  if (params.status) query.append('status', params.status)
  if (params.type) query.append('type', params.type)
  const queryStr = query.toString()
  
  if (!memberId) {
    return request.get(`/doctor/families/${familyId}/plans${queryStr ? '?' + queryStr : ''}`)
  }
  return request.get(`/doctor/families/${familyId}/members/${memberId}/plans${queryStr ? '?' + queryStr : ''}`)
}
export const getHealthPlan = (planId) => request.get(`/doctor/plans/${planId}`)
export const createHealthPlan = (familyId, memberId, data) => request.post(`/doctor/families/${familyId}/members/${memberId}/plans`, data)
export const updateHealthPlan = (planId, data) => request.put(`/doctor/plans/${planId}`, data)
export const deleteHealthPlan = (planId) => request.delete(`/doctor/plans/${planId}`)
export const generateAiHealthPlan = (familyId, data) => request.post(`/doctor/families/${familyId}/plans/ai-generate`, data)
export const batchGenerateAiHealthPlans = (familyId) => request.post(`/doctor/families/${familyId}/plans/ai-batch-generate`)
export const getHealthPlansCalendar = (memberId, startDate, endDate) => request.get(`/doctor/members/${memberId}/plans/calendar`, { params: { startDate, endDate } })

// 随访任务相关API
export const listFollowUpTasks = (familyId, memberId, params = {}) => {
  if (!memberId) {
    return request.get(`/doctor/families/${familyId}/followups`, { params })
  }
  return request.get(`/doctor/families/${familyId}/members/${memberId}/followups`, { params })
}
export const createFollowUpTask = (familyId, memberId, data) => request.post(`/doctor/families/${familyId}/members/${memberId}/followups`, data)
export const updateFollowUpTask = (taskId, data) => request.put(`/doctor/followups/${taskId}`, data)
export const deleteFollowUpTask = (taskId) => request.delete(`/doctor/followups/${taskId}`)

// 医患沟通相关API
export const getDoctorPatientMessages = (familyId, memberId, params = {}) => request.get(`/doctor/families/${familyId}/members/${memberId}/messages`, { params })
export const sendDoctorPatientMessage = (familyId, memberId, data) => request.post(`/doctor/families/${familyId}/members/${memberId}/messages`, data)
export const markMessageRead = (messageId) => request.put(`/doctor/messages/${messageId}/read`)

// 数据统计相关API
export const getDoctorStats = (familyId, startDate, endDate) => request.get('/doctor/stats', { params: { familyId, startDate, endDate } })

// 上线咨询相关API
// 获取当前家庭绑定的医生信息
export const getOrCreateDoctorSession = () => {
  // 获取当前家庭 ID
  const familyId = localStorage.getItem('current_family_id')
  if (!familyId || familyId === 'null') {
    return Promise.reject(new Error('未找到家庭信息，请先选择或创建家庭'))
  }
  
  console.log(`[API] Calling /families/${familyId}/doctor`)
  return request.get(`/families/${familyId}/doctor`)
}

// 医生设置相关API
export const getDoctorSettings = () => request.get('/doctor/settings')
export const updateDoctorSettings = (data) => request.put('/doctor/settings', data)

// 阈值管理
export const getPatientThresholds = (userId) => request.get(`/doctor/thresholds/${userId}`)
export const savePatientThreshold = (userId, data) => request.post(`/doctor/thresholds/${userId}`, data)
