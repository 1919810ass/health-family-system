/**
 * 前端状态管理模块：recommendation.js
 *
 * 存储与管理全局/模块级状态，并封装与接口交互的动作。
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRecommendationStore = defineStore('recommendation', () => {
  const items = ref([])
  const activeCategory = ref('DIET')

  function setItems(data) {
    items.value = data
  }

  function setActiveCategory(key) {
    activeCategory.value = key
  }

  return { items, activeCategory, setItems, setActiveCategory }
})