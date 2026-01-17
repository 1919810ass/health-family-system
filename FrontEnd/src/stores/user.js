import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProfile as apiGetProfile, updateProfile as apiUpdateProfile, updateAvatar as apiUpdateAvatar, changePassword as apiChangePassword, updateNotifications as apiUpdateNotifications } from '../api/user'
import { removeToken } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const profile = ref(null)
  const currentFamily = ref(null) // { id, name, inviteCode }

  function setProfile(data) {
    profile.value = data
  }

  function setCurrentFamily(family) {
    currentFamily.value = family
  }

  function logout() {
    profile.value = null
    currentFamily.value = null
    removeToken()
  }

  async function fetchProfile() {
    const { data } = await apiGetProfile()
    setProfile(data)
    return data
  }

  async function updateProfile(payload) {
    const { data } = await apiUpdateProfile(payload)
    setProfile({ ...profile.value, ...data })
    return data
  }

  async function updateAvatar(formData) {
    console.log('[UserStore] call apiUpdateAvatar')
    const res = await apiUpdateAvatar(formData)
    const url = res?.data?.avatar ?? res?.avatar ?? null
    console.log('[UserStore] apiUpdateAvatar response', res)
    if (!url) {
      throw new Error('上传返回缺少 avatar 字段')
    }
    setProfile({ ...profile.value, avatar: url })
    return url
  }

  async function changePassword(payload) {
    await apiChangePassword(payload)
  }

  async function updateNotifications(payload) {
    const { data } = await apiUpdateNotifications(payload)
    setProfile({ ...profile.value, notifications: data })
    return data
  }

  return { profile, currentFamily, setProfile, setCurrentFamily, logout, fetchProfile, updateProfile, updateAvatar, changePassword, updateNotifications }
})
