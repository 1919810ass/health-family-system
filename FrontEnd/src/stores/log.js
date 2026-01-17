import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLogStore = defineStore('log', () => {
  const activeTab = ref('diet') // diet / sleep / sport / mood / vital
  const logs = ref([]) // 当前选中日期下的日志列表
  const calendarMarks = ref({}) // 日期 -> 是否有记录
  const trends = ref({}) // 分类 -> 趋势数据

  function setActiveTab(tab) {
    activeTab.value = tab
  }

  function setLogs(list) {
    logs.value = list
  }

  function setCalendarMarks(marks) {
    calendarMarks.value = marks
  }

  function setTrends(data) {
    trends.value = data
  }

  return { activeTab, logs, calendarMarks, trends, setActiveTab, setLogs, setCalendarMarks, setTrends }
})