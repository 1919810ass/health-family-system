/**
 * 前端状态管理模块：family.js
 *
 * 存储与管理全局/模块级状态，并封装与接口交互的动作。
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useFamilyStore = defineStore('family', () => {
  const families = ref([]) // 当前用户可切换的家庭列表
  const current = ref(null) // 当前选中的家庭对象

  function setFamilies(list) {
    families.value = list
  }

  function setCurrent(family) {
    current.value = family
    if (family && family.id) {
      localStorage.setItem('current_family_id', family.id)
    } else {
      localStorage.removeItem('current_family_id')
    }
  }

  function addFamily(family) {
    families.value.push(family)
  }

  function reset() {
    families.value = []
    current.value = null
    localStorage.removeItem('current_family_id')
  }

  return { families, current, setFamilies, setCurrent, addFamily, reset }
})