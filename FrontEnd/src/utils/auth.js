/**
 * 前端工具模块：auth.js
 *
 * 提供通用工具方法或跨模块复用能力，避免业务代码重复。
 */

const TokenKey = 'HealthFamily-Token'

export const getToken = () => localStorage.getItem(TokenKey)
export const setToken = (token) => {
  localStorage.setItem(TokenKey, token)
}
export const removeToken = () => {
  localStorage.removeItem(TokenKey)
}