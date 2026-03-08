/**
 * 前端接口封装：lifestyle.js
 *
 * 统一封装与后端 /api 路径下接口的调用，供页面与状态管理层复用。
 */

import request from '../utils/request'

export const ingestDiet = (payload) => request.post('/lifestyle/diet/ingest', payload)
// 不手动设置 Content-Type，让 axios 自动加 boundary，否则后端可能解析不到文件
export const uploadDietImage = (formData) => request.post('/lifestyle/diet/upload', formData)
export const recommendRecipes = (payload) => request.post('/lifestyle/recipes/recommend', payload)
export const weeklyDietReport = (params) => request.get('/lifestyle/diet/report/weekly', { params })
export const recordExercise = (payload) => request.post('/lifestyle/exercise/record', payload)
export const suggestExercise = () => request.get('/lifestyle/exercise/suggest')
export const recordSleep = (payload) => request.post('/lifestyle/sleep/record', payload)
export const analyzeSleep = () => request.get('/lifestyle/sleep/analyze')
export const recordMood = (payload) => request.post('/lifestyle/mood/record', payload)
export const analyzeMood = () => request.get('/lifestyle/mood/analyze')
export const recordVitals = (payload) => request.post('/lifestyle/vitals/record', payload)
export const analyzeVitals = () => request.get('/lifestyle/vitals/analyze')
