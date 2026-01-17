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