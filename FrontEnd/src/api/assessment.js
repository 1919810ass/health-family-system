import request from '../utils/request'

export const getSchema = () => request.get('/assessments/schema')
export const submitAssessment = (payload) => request.post('/assessments', payload)
export const getResult = (assessmentId) => request.get(`/assessments/${assessmentId}`)
export const getHistory = () => request.get('/assessments/history')
export const getFamilyLatest = (familyId) => request.get(`/assessments/family/${familyId}/latest`)

// TCM相关API
export const getTcmPersonalizedPlan = () => request.get('/tcm-assessment/personalized-plan')
export const getConstitutionTrend = (lookbackDays = 90) => request.get('/tcm-assessment/trend', { params: { lookbackDays } })
export const getFamilyTcmHealthOverview = (familyId) => request.get(`/tcm-assessment/family-overview/${familyId}`)

// AI测评相关API
export const startAiAssessment = () => request.post('/ai-assessment/start')
export const processAiAnswer = (sessionId, answer) => request.post('/ai-assessment/answer', { answer }, { params: { sessionId } })
export const generateFinalAiAssessment = (sessionId, finalAnswers) => request.post('/ai-assessment/generate-result', { finalAnswers }, { params: { sessionId } })
