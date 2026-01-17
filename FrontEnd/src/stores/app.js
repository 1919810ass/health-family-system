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
