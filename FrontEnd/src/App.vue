<template>
  <router-view />
  </template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from './stores/user'

const userStore = useUserStore()

onMounted(async () => {
  // 在应用启动时尝试加载用户信息
  const token = localStorage.getItem('access_token')
  if (token && userStore.profile === null) {
    try {
      console.log('[App] Loading user profile on app mount...')
      await userStore.fetchProfile()
      console.log('[App] User profile loaded:', userStore.profile)
    } catch (error) {
      console.error('[App] Failed to load user profile:', error)
      const status = error?.response?.status
      if (status === 403) {
        console.error('[App] 403 Forbidden - User may not have permission to access /user/profile')
      } else if (status === 401) {
        console.log('[App] 401 Unauthorized - Token may be expired or invalid')
        localStorage.removeItem('access_token')
      }
      console.log('[App] User profile not loaded - user may not be logged in or token is invalid')
    }
  }
})
</script>

<style>
html, body, #app { height: 100%; }
</style>

