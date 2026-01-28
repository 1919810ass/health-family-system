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

  return { families, current, setFamilies, setCurrent, addFamily }
})