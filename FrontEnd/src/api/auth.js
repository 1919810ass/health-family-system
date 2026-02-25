/**
 * 前端接口封装：auth.js
 *
 * 统一封装与后端 /api 路径下接口的调用，供页面与状态管理层复用。
 */

import request from '../utils/request'

export const login = (payload) => {
  return request.post('/auth/login', payload)
}


export const register = (payload) => {
  return request.post('/auth/register', payload)
}

export const registerDoctor = (payload) => {
  return request.post('/auth/register-doctor', payload)
}

export const registerAdmin = (payload) => {
  return request.post('/auth/register-admin', payload)
}

export const refreshToken = (payload) => {
  return request.post('/auth/refresh', payload)
}

