import axios from 'axios'
import dayjs from 'dayjs'
import { error as logError, info as logInfo } from './logger'
import { getToken, removeToken } from './auth'

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

const request = axios.create({
  baseURL: '/api',
  timeout: 120000,
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    if (!token) {
      const devUserId = localStorage.getItem('dev_user_id') || '4'
      if (!config.headers['X-User-Id']) {
        config.headers['X-User-Id'] = devUserId
      }
    }
    if (!config.headers['Accept']) {
      config.headers['Accept'] = 'application/json'
    }
    config.headers['X-Client-Env'] = import.meta.env.MODE || 'unknown'
    logInfo('HTTP', 'REQUEST', { url: config.url, method: config.method, ts: dayjs().toISOString() })
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (response) => {
    const ct = response?.headers?.['content-type'] || ''
    const isHtml = typeof response?.data === 'string' && response.data.startsWith('<!doctype html')
    if (ct.includes('text/html') || isHtml) {
      const err = new Error('后端未返回JSON，可能未启动或被重定向到/login')
      err.response = response
       logError('HTTP', 'INVALID_CONTENT_TYPE', { url: response?.config?.url, ct, status: response?.status })
      return Promise.reject(err)
    }
    return response.data
  },
  async (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        removeToken()
        const redirected = error?.response?.request?.responseURL?.includes('/login')
        if (redirected) {
          const wrapped = new Error('后端重定向到 /login，疑似未启用JWT或表单登录未禁用')
          wrapped.cause = error
           logError('AUTH', 'REDIRECT_LOGIN', { url: error?.config?.url, status })
          return Promise.reject(wrapped)
        }
      }
      logError('HTTP', 'RESPONSE_ERROR', { url: error?.config?.url, status, data: error?.response?.data })
      if (status >= 500) {
        try {
          window.dispatchEvent(new CustomEvent('hf-error', { detail: `接口错误(${status}): ${error?.config?.url}` }))
        } catch {}
        const method = String(error?.config?.method || 'get').toLowerCase()
        if (method === 'get') {
          const retry = error?.config?.__retryCount || 0
          if (retry < 2) {
            error.config.__retryCount = retry + 1
            const delay = 300 * Math.pow(2, retry)
            await sleep(delay)
            return request(error.config)
          }
        }
      }
    }
    if (!error.response) {
      // 代理连接被拒绝（后端未启动或端口不通）
      if (error.code === 'ECONNREFUSED' || /ECONNREFUSED/i.test(error.message)) {
        const wrapped = new Error('无法连接后端服务（ECONNREFUSED），请确认 http://localhost:8080 已启动')
        wrapped.cause = error
        logError('HTTP', 'ECONNREFUSED', { url: error?.config?.url, hint: '检查Vite代理与后端服务', target: 'http://localhost:8080' })
        return Promise.reject(wrapped)
      }
      const redirected = error?.request?.responseURL?.includes('/login')
      if (redirected) {
        const wrapped = new Error('后端重定向到 /login，疑似未启用JWT或表单登录未禁用')
        wrapped.cause = error
        logError('HTTP', 'NETWORK_REDIRECT_LOGIN', { url: error?.config?.url })
        return Promise.reject(wrapped)
      }
    }
    logError('HTTP', 'NETWORK_ERROR', { url: error?.config?.url, message: error.message })
    return Promise.reject(error)
  },
)

export const formatDate = (date, pattern = 'YYYY-MM-DD') => dayjs(date).format(pattern)

export default request
