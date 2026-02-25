/**
 * 前端状态管理模块：app.js
 *
 * 存储与管理全局/模块级状态，并封装与接口交互的动作。
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const collapsed = ref(false)
  const theme = ref('light')

  function toggleCollapsed() {
    collapsed.value = !collapsed.value
  }

  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
  }

  return { collapsed, theme, toggleCollapsed, toggleTheme }
})
