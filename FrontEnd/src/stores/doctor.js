import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getDoctorFamilies, getBoundFamilyMembers } from '../api/doctor'
import { getDoctorView } from '../api/family'
import { ElMessage } from 'element-plus'

// 本地存储键名
const STORAGE_KEY_CURRENT_FAMILY_ID = 'doctor_current_family_id'
const STORAGE_KEY_CURRENT_FAMILY_ID_COMMON = 'current_family_id'

export const useDoctorStore = defineStore('doctor', () => {
  // 状态定义
  const families = ref([]) // 医生绑定的家庭列表
  const currentFamilyId = ref(null) // 当前选中的家庭ID
  const boundMembers = ref([]) // 当前家庭的绑定成员列表
  const loadingStates = ref({
    families: false,
    members: false,
    summary: false,
  })
  const summary = ref('') // 当前家庭的健康摘要
  const telemetry = ref({}) // 当前家庭的遥测数据
  const aiEnabled = ref(false) // AI生成功能开关
  const pendingData = ref({
    consultations: 0,
    plans: 0,
    followups: 0,
  }) // 待办事项统计
  const abnormalEvents = ref([]) // 异常事件列表
  const highRiskMembers = ref([]) // 高风险患者列表

  // 计算属性
  const currentFamily = computed(() => {
    if (!currentFamilyId.value) return null
    return families.value.find(f => String(f.id) === String(currentFamilyId.value))
  })

  // 初始化当前家庭ID（从本地存储读取）
  function initCurrentFamilyId() {
    const storedDoctor = localStorage.getItem(STORAGE_KEY_CURRENT_FAMILY_ID)
    if (storedDoctor && storedDoctor !== 'null') {
      currentFamilyId.value = storedDoctor
      return
    }
    const storedCommon = localStorage.getItem(STORAGE_KEY_CURRENT_FAMILY_ID_COMMON)
    if (storedCommon && storedCommon !== 'null') {
      currentFamilyId.value = storedCommon
    }
  }

  // 获取医生绑定的家庭列表
  async function fetchFamilies() {
    loadingStates.value.families = true
    try {
      const res = await getDoctorFamilies()
      families.value = res?.data || []
      
      // 如果列表不为空且当前没有选中家庭，自动选择第一个
      if (families.value.length > 0 && !currentFamilyId.value) {
        const firstFamilyId = String(families.value[0].id)
        await setCurrentFamily(firstFamilyId)
      } else if (currentFamilyId.value) {
        // 验证当前家庭ID是否还在列表中
        const exists = families.value.some(f => String(f.id) === String(currentFamilyId.value))
        if (!exists && families.value.length > 0) {
          // 当前家庭不存在，选择第一个
          await setCurrentFamily(String(families.value[0].id))
        }
      }
      
      return families.value
    } catch (error) {
      console.error('获取医生家庭列表失败:', error)
      ElMessage.error('获取家庭列表失败')
      throw error
    } finally {
      loadingStates.value.families = false
    }
  }

  // 设置当前家庭（会更新本地存储并触发相关数据刷新）
  async function setCurrentFamily(familyId) {
    if (!familyId || familyId === 'null') {
      ElMessage.error('请选择家庭')
      return
    }

    const familyIdStr = String(familyId)
    
    // 验证家庭ID是否在列表中
    const exists = families.value.some(f => String(f.id) === familyIdStr)
    if (!exists && families.value.length > 0) {
      ElMessage.warning('所选家庭不在绑定列表中')
      return
    }

    currentFamilyId.value = familyIdStr
    
    // 保存到本地存储
    try {
      localStorage.setItem(STORAGE_KEY_CURRENT_FAMILY_ID, familyIdStr)
      localStorage.setItem(STORAGE_KEY_CURRENT_FAMILY_ID_COMMON, familyIdStr)
    } catch (error) {
      console.error('保存当前家庭ID到本地存储失败:', error)
    }

    // 刷新相关数据（不自动启用AI）
    await Promise.all([
      fetchMembers(familyIdStr),
      fetchSummary(familyIdStr, false),
    ])
  }

  // 获取指定家庭的绑定成员
  async function fetchMembers(familyId) {
    if (!familyId || familyId === 'null') {
      boundMembers.value = []
      return []
    }

    loadingStates.value.members = true
    try {
      const res = await getBoundFamilyMembers(familyId)
      boundMembers.value = res?.data || []
      return boundMembers.value
    } catch (error) {
      console.error('获取家庭成员列表失败:', error)
      ElMessage.error('加载患者失败')
      boundMembers.value = []
      throw error
    } finally {
      loadingStates.value.members = false
    }
  }

  // 获取当前家庭的健康摘要和遥测数据
  async function fetchSummary(familyId, useAi = false) {
    if (!familyId || familyId === 'null') {
      summary.value = ''
      telemetry.value = {}
      pendingData.value = { consultations: 0, plans: 0, followups: 0 }
      abnormalEvents.value = []
      highRiskMembers.value = []
      return
    }

    loadingStates.value.summary = true
    try {
      const res = await getDoctorView(familyId, useAi)
      summary.value = res?.data?.summary || ''
      telemetry.value = res?.data?.telemetry || {}
      pendingData.value = {
        consultations: res?.data?.pendingConsultationsCount || 0,
        plans: res?.data?.pendingPlansCount || 0,
        followups: res?.data?.todayFollowupsCount || 0,
      }
      abnormalEvents.value = res?.data?.abnormalEvents || []
      highRiskMembers.value = res?.data?.highRiskMembers || []
      return {
        summary: summary.value,
        telemetry: telemetry.value,
        pendingData: pendingData.value,
        abnormalEvents: abnormalEvents.value,
        highRiskMembers: highRiskMembers.value,
      }
    } catch (error) {
      console.error('获取健康摘要失败:', error)
      summary.value = ''
      telemetry.value = {}
      pendingData.value = { consultations: 0, plans: 0, followups: 0 }
      abnormalEvents.value = []
      highRiskMembers.value = []
      throw error
    } finally {
      loadingStates.value.summary = false
    }
  }

  // 切换AI功能
  function toggleAi(enabled) {
    aiEnabled.value = enabled
  }

  // 刷新所有数据
  async function refreshAll() {
    if (!currentFamilyId.value) {
      ElMessage.error('请先选择家庭')
      return
    }
    await Promise.all([
      fetchFamilies(),
      fetchMembers(currentFamilyId.value),
      fetchSummary(currentFamilyId.value, aiEnabled.value),
    ])
  }

  // 重置状态（退出登录时调用）
  function reset() {
    families.value = []
    currentFamilyId.value = null
    boundMembers.value = []
    summary.value = ''
    telemetry.value = {}
    pendingData.value = { consultations: 0, plans: 0, followups: 0 }
    abnormalEvents.value = []
    highRiskMembers.value = []
    aiEnabled.value = false
    try {
      localStorage.removeItem(STORAGE_KEY_CURRENT_FAMILY_ID)
      localStorage.removeItem(STORAGE_KEY_CURRENT_FAMILY_ID_COMMON)
    } catch (error) {
      console.error('清除本地存储失败:', error)
    }
  }

  // 初始化：从本地存储恢复当前家庭ID
  initCurrentFamilyId()

  return {
    // 状态
    families,
    currentFamilyId,
    boundMembers,
    loadingStates,
    summary,
    telemetry,
    pendingData,
    abnormalEvents,
    highRiskMembers,
    aiEnabled,
    // 计算属性
    currentFamily,
    // 方法
    fetchFamilies,
    setCurrentFamily,
    fetchMembers,
    fetchSummary,
    refreshAll,
    reset,
    toggleAi,
  }
})

