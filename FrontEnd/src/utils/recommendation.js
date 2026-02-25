/**
 * 前端工具模块：recommendation.js
 *
 * 提供通用工具方法或跨模块复用能力，避免业务代码重复。
 */

export const mapToBackendCategories = (arr) => {
  const M = { DIET: 'DIET', SLEEP: 'REST', SPORT: 'SPORT', MOOD: 'EMOTION' }
  return (arr || []).map(k => M[k] || k)
}

