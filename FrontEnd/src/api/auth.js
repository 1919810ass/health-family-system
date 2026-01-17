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

