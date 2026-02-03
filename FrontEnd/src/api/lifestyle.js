import request from '../utils/request'

export const ingestDiet = (payload) => request.post('/lifestyle/diet/ingest', payload)
export const uploadDietImage = (formData) => request.post('/lifestyle/diet/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const recommendRecipes = (payload) => request.post('/lifestyle/recipes/recommend', payload)
export const weeklyDietReport = (params) => request.get('/lifestyle/diet/report/weekly', { params })
export const recordExercise = (payload) => request.post('/lifestyle/exercise/record', payload)
export const suggestExercise = () => request.get('/lifestyle/exercise/suggest')
export const recordSleep = (payload) => request.post('/lifestyle/sleep/record', payload)
export const analyzeSleep = () => request.get('/lifestyle/sleep/analyze')
export const recordMood = (payload) => request.post('/lifestyle/mood/record', payload)
export const recordVitals = (payload) => request.post('/lifestyle/vitals/record', payload)
