<template>
  <div class="doctor-patients">
    <el-page-header content="患者管理" />
    
    <!-- 家庭选择和统计信息 -->
    <div class="family-selector">
      <div class="family-info">
        <span class="label">当前家庭：</span>
        <el-select :model-value="familyId" placeholder="选择家庭" style="width: 240px" @change="onSwitch">
          <el-option v-for="f in families" :key="f.id" :label="f.name" :value="String(f.id)" />
        </el-select>
        <span v-if="currentFamilyName" class="current-family-name">{{ currentFamilyName }}</span>
      </div>
      <div class="stats-info">
        <div class="stat-item">
          <span class="stat-label">总患者数：</span>
          <span class="stat-value">{{ members.length }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">筛选结果：</span>
          <span class="stat-value primary">{{ filtered.length }}</span>
        </div>
      </div>
    </div>
    
    <!-- 过滤工具栏 -->
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索姓名/电话" style="width: 200px" clearable prefix-icon="Search" />
      
      <el-select v-model="disease" placeholder="疾病类型" style="width: 150px" clearable>
        <el-option label="全部" value="" />
        <el-option label="高血压" value="hypertension" />
        <el-option label="糖尿病" value="diabetes" />
      </el-select>
      
      <el-select v-model="risk" placeholder="风险等级" style="width: 150px" clearable>
        <el-option label="全部" value="" />
        <el-option label="低" value="low" />
        <el-option label="中" value="mid" />
        <el-option label="高" value="high" />
      </el-select>
      
      <el-select v-model="importantFilter" placeholder="重点管理" style="width: 120px" clearable>
        <el-option label="全部" value="" />
        <el-option label="重点管理" :value="true" />
        <el-option label="非重点" :value="false" />
      </el-select>
      
      <el-date-picker
        v-model="activeTimeRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 240px"
        clearable
      />
      
      <el-button type="primary" :loading="loading" @click="loadMembers" icon="Search">查询</el-button>
      <el-button @click="resetFilters" icon="RefreshLeft">重置</el-button>
    </div>

    <!-- 患者列表 -->
    <el-table :data="filtered" class="mt-16" height="auto" v-loading="loading" @row-click="handleRowClick" style="width: 100%">
      <el-table-column width="60" align="center" label="重点" resizable="false">
        <template #default="{ row }">
          <div class="star-cell">
            <el-icon
              :style="{ color: isPatientImportant(row.userId) ? '#E6A23C' : '#C0C4CC', cursor: 'pointer' }"
              :size="20"
              @click.stop="toggleImportant(row.userId)"
            >
              <StarFilled v-if="isPatientImportant(row.userId)" />
              <Star v-else />
            </el-icon>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="姓名" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="name-cell">
            <el-avatar :size="32" :src="row.avatar">
              {{ row.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="text-ellipsis">{{ row.nickname }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" min-width="140" show-overflow-tooltip />
      <el-table-column prop="role" label="角色" min-width="130">
        <template #default="{ row }">
          <div class="role-cell">
            <el-icon :class="['role-icon', getRoleType(row.role)]">
              <UserFilled v-if="row.role === 'ADMIN' || row.role === 'DOCTOR'" />
              <User v-else />
            </el-icon>
            <el-tag size="small" :type="getRoleType(row.role)" effect="plain" class="role-tag">
              {{ getRoleLabel(row.role) }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="疾病标签" min-width="180">
        <template #default="{ row }">
          <div class="tags-wrapper">
            <el-tag 
              v-for="tag in (row.tags || []).slice(0, 2)" 
              :key="tag" 
              size="small" 
              :type="tag.includes('高血压') ? 'danger' : (tag.includes('糖尿病') ? 'warning' : 'info')"
              class="disease-tag"
              effect="light"
            >
              <el-icon class="mr-1"><component :is="getDiseaseIcon(tag)" /></el-icon>
              {{ tag }}
            </el-tag>
            <span v-if="!row.tags || row.tags.length === 0" class="placeholder-text">—</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="最近活跃" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          {{ formatLastActive(row) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="{ row }">
          <div class="operation-buttons">
            <el-tooltip content="在线咨询" placement="top">
              <el-button circle type="primary" @click.stop="openConsult(row)" :icon="ChatDotRound" />
            </el-tooltip>
            <el-tooltip content="查看详情" placement="top">
              <el-button circle @click.stop="viewDetail(row)" :icon="View" />
            </el-tooltip>
            
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, row)">
              <el-button circle :icon="MoreFilled" class="more-btn" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logs" :icon="Document" :disabled="isShareOffByUserId(row.userId)">健康日志</el-dropdown-item>
                  <el-dropdown-item command="recommendations" :icon="Memo" :disabled="isShareOffByUserId(row.userId)">健康建议</el-dropdown-item>
                  <el-dropdown-item command="tags" :icon="Edit">管理标签</el-dropdown-item>
                  <el-dropdown-item command="plan" :icon="Calendar">随访计划</el-dropdown-item>
                  <el-dropdown-item command="threshold" :icon="Operation">健康阈值</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 患者详情抽屉 -->
    <PatientDetailDrawer
      v-model="drawerVisible"
      :family-id="familyId"
      :patient-user-id="currentPatientUserId"
      :share-to-family="currentPatientShareToFamily"
      @view-logs="handleViewLogs"
      @view-recommendations="handleViewRecommendations"
      @create-followup-plan="handleCreateFollowupPlan"
    />
    
    <!-- 标签编辑弹窗 -->
    <el-dialog v-model="tagDialogVisible" title="管理患者标签" width="500px">
      <el-form label-position="top">
        <el-form-item label="疾病/风险标签">
          <el-select
            v-model="editingTags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入标签"
            style="width: 100%"
          >
            <el-option v-for="tag in predefinedTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="tagDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTags">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 阈值设置弹窗 -->
    <ThresholdSettingsDialog
      v-model="thresholdVisible"
      :user-id="currentThresholdUserId"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled, Search, RefreshLeft, View, Document, Memo, Calendar, ChatDotRound, MoreFilled, FirstAidKit, Sugar, Timer, User, UserFilled, Operation, Edit } from '@element-plus/icons-vue'
import { useDoctorStore } from '../../stores/doctor'
import { togglePatientImportant, updatePatientTags } from '../../api/doctor'
import PatientDetailDrawer from '../../components/Common/PatientDetailDrawer.vue'
import ThresholdSettingsDialog from '../../components/Doctor/ThresholdSettingsDialog.vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const doctorStore = useDoctorStore()

// 疾病图标映射
const getDiseaseIcon = (tag) => {
  if (tag.includes('高血压')) return Timer
  if (tag.includes('糖尿病')) return Sugar
  return FirstAidKit
}

// 使用 store 中的状态
const families = computed(() => doctorStore.families)
const familyId = computed(() => doctorStore.currentFamilyId)
const currentFamilyName = computed(() => {
  if (!familyId.value) return ''
  const family = families.value.find(f => String(f.id) === String(familyId.value))
  return family?.name || ''
})
const loading = computed(() => doctorStore.loadingStates.members)
const members = computed(() => doctorStore.boundMembers)

// 搜索过滤条件
const keyword = ref('')
const disease = ref('')
const risk = ref('')
const importantFilter = ref('')
const activeTimeRange = ref(null)

// 抽屉状态
const drawerVisible = ref(false)
const currentPatientUserId = ref(null)
const currentPatientShareToFamily = ref(undefined)

const getShareFlag = (member) => {
  const val = member?.shareToFamily ?? member?.share_to_family ?? member?.dataShare ?? member?.data_share
  if (val === undefined || val === null) return undefined
  return !!val
}

const isShareOffByUserId = (userId) => {
  if (!userId) return false
  const member = members.value.find(m => String(m.userId) === String(userId))
  return getShareFlag(member) === false
}

// 过滤后的成员列表
const filtered = computed(() => {
  return members.value.filter((m) => {
    // 关键词搜索
    const kw = keyword.value?.trim()
    const okKw = !kw || 
      (m.nickname && m.nickname.includes(kw)) || 
      (m.phone && String(m.phone).includes(kw))
    
    // 疾病类型过滤
    const okDis = !disease.value || (m.tags && m.tags.includes(disease.value))
    
    // 风险等级过滤
    const okRisk = !risk.value || m.risk === risk.value
    
    // 重点管理过滤
    const okImportant = importantFilter.value === '' || 
      isPatientImportant(m.userId) === (importantFilter.value === true)
    
    // 活跃时间区间过滤
    let okActiveTime = true
    if (activeTimeRange.value && activeTimeRange.value.length === 2 && m.lastActive) {
      const lastActive = dayjs(m.lastActive)
      const start = dayjs(activeTimeRange.value[0])
      const end = dayjs(activeTimeRange.value[1])
      okActiveTime = lastActive.isAfter(start) && lastActive.isBefore(end)
    }
    
    return okKw && okDis && okRisk && okImportant && okActiveTime
  })
})

// 检查患者是否为重点管理
const isPatientImportant = (userId) => {
  if (!userId) return false
  const key = `doctor_patient_important_${userId}`
  return localStorage.getItem(key) === 'true'
}

// 切换重点管理状态
const toggleImportant = async (userId) => {
  if (!userId || !familyId.value) return
  const key = `doctor_patient_important_${userId}`
  const current = localStorage.getItem(key) === 'true'
  const newValue = !current
  
  try {
    // 乐观更新
    localStorage.setItem(key, String(newValue))
    ElMessage.success(newValue ? '已设为重点管理' : '已取消重点管理')
    
    // 同步到服务器（虽然目前后端只是占位，但保持接口调用习惯）
    await togglePatientImportant(familyId.value, userId, newValue)
  } catch(e) {
    // 失败回滚
    localStorage.setItem(key, String(current))
    ElMessage.error('操作失败')
  }
}

// 重置筛选条件
const resetFilters = () => {
  keyword.value = ''
  disease.value = ''
  risk.value = ''
  importantFilter.value = ''
  activeTimeRange.value = null
  ElMessage.success('筛选条件已重置')
}

// 获取角色类型（用于标签颜色）
const getRoleType = (role) => {
  const typeMap = {
    'ADMIN': 'danger',
    'FAMILY_ADMIN': 'warning',
    'MEMBER': '',
    'DOCTOR': 'success'
  }
  return typeMap[role] || ''
}

// 获取角色显示名称
const getRoleLabel = (role) => {
  const labelMap = {
    'ADMIN': '管理员',
    'FAMILY_ADMIN': '家庭管理员',
    'MEMBER': '成员',
    'DOCTOR': '医生'
  }
  return labelMap[role] || role
}

// 监听当前家庭变化，自动加载成员
watch(() => doctorStore.currentFamilyId, async (newFamilyId) => {
  if (newFamilyId) {
    await doctorStore.fetchMembers(newFamilyId)
  }
}, { immediate: false })

const loadMembers = async () => {
  if (!familyId.value) {
    ElMessage.error('请选择家庭')
    return
  }
  await doctorStore.fetchMembers(familyId.value)
}

const onSwitch = async (id) => {
  await doctorStore.setCurrentFamily(id)
}

const handleRowClick = (row) => {
  // 点击行也可以打开详情，但优先级低于按钮点击
}

const viewDetail = (row) => {
  currentPatientUserId.value = String(row.userId)
  currentPatientShareToFamily.value = getShareFlag(row)
  drawerVisible.value = true
}

const viewLogs = (row) => {
  if (isShareOffByUserId(row?.userId)) {
    ElMessage.warning('患者未开启数据共享，无法查看健康日志')
    return
  }
  // 跳转到医生端的健康日志页面，保持在医生布局内
  router.push(`/doctor/patients/${row.userId}/logs`)
}

const viewRecommendations = (row) => {
  if (isShareOffByUserId(row?.userId)) {
    ElMessage.warning('患者未开启数据共享，无法查看健康建议')
    return
  }
  // 跳转到医生端的健康建议页面，保持在医生布局内
  router.push(`/doctor/patients/${row.userId}/recommendations`)
}

const createFollowupPlan = (row) => {
  // 跳转到计划页面，通过查询参数指定患者ID
  router.push(`/doctor/plans?patientUserId=${row.userId}`)
}

const openConsult = (row) => {
  router.push(`/doctor/consultation?memberId=${row.userId}`)
}

// 阈值设置弹窗状态
const thresholdVisible = ref(false)
const currentThresholdUserId = ref(null)

const openThresholdSettings = (row) => {
  currentThresholdUserId.value = String(row.userId)
  thresholdVisible.value = true
}

// 标签编辑相关
const tagDialogVisible = ref(false)
const editingTags = ref([])
const currentEditingUserId = ref(null)
const predefinedTags = ['高血压', '糖尿病', '高血脂', '心脏病', '哮喘', '过敏体质', '孕产妇', '儿童', '老年人', '慢阻肺', '脑卒中康复']

const openTagEdit = (row) => {
  currentEditingUserId.value = row.userId
  // copy tags
  editingTags.value = row.tags ? [...row.tags] : []
  tagDialogVisible.value = true
}

const saveTags = async () => {
  if (!currentEditingUserId.value || !familyId.value) return
  try {
    await updatePatientTags(familyId.value, currentEditingUserId.value, editingTags.value)
    ElMessage.success('标签已更新')
    tagDialogVisible.value = false
    // Refresh list
    await loadMembers()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const handleCommand = (command, row) => {
  switch (command) {
    case 'logs':
      viewLogs(row)
      break
    case 'recommendations':
      viewRecommendations(row)
      break
    case 'tags':
      openTagEdit(row)
      break
    case 'plan':
      createFollowupPlan(row)
      break
    case 'threshold':
      openThresholdSettings(row)
      break
  }
}

// 处理抽屉中的操作事件
const handleViewLogs = (userId) => {
  drawerVisible.value = false
  viewLogs({ userId })
}

const handleViewRecommendations = (userId) => {
  drawerVisible.value = false
  viewRecommendations({ userId })
}

const handleCreateFollowupPlan = (userId) => {
  drawerVisible.value = false
  createFollowupPlan({ userId })
}

const formatLastActive = (row) => {
  // 暂时显示 —，后续可以从日志中获取最后活跃时间
  return row.lastActive || '—'
}

onMounted(async () => {
  try {
    // 加载家庭列表（会自动选择默认家庭）
    await doctorStore.fetchFamilies()
    // 如果已有当前家庭，加载成员
    if (doctorStore.currentFamilyId) {
      await loadMembers()
    }
  } catch (error) {
    console.error('初始化失败:', error)
  }
})

// 监听路由变化，确保在页面切换时状态正确
watch(() => route.path, (newPath, oldPath) => {
  // 当离开当前页面时，确保状态正确
  if (oldPath?.startsWith('/doctor/patients') && !newPath?.startsWith('/doctor/patients')) {
    // 离开患者管理页面时的操作
  }
})
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.doctor-patients {
  padding: 20px;
  background: transparent;
  min-height: calc(100vh - 60px);
}

:deep(.el-page-header) {
  margin-bottom: 24px;
  
  .el-page-header__content {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

// 家庭选择器区域（与工作台保持一致）
.family-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  @include mixins.glass-effect;
  border-radius: 12px;
  margin-bottom: 20px;
  transition: all 0.4s vars.$ease-spring;

  &:hover {
    box-shadow: vars.$shadow-md;
    transform: translateY(-2px);
  }

  .family-info {
    display: flex;
    align-items: center;
    gap: 16px;

    .label {
      font-weight: 600;
      font-size: 15px;
      color: var(--el-text-color-primary);
    }

    .current-family-name {
      color: var(--el-color-primary);
      font-weight: 600;
      font-size: 15px;
      background: rgba(var(--el-color-primary-rgb), 0.1);
      padding: 4px 12px;
      border-radius: 20px;
      border: 1px solid rgba(var(--el-color-primary-rgb), 0.2);
    }
  }

  .stats-info {
    display: flex;
    gap: 32px;
    align-items: center;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;

      .stat-label {
        font-size: 14px;
        color: var(--el-text-color-regular);
        font-weight: 500;
      }

      .stat-value {
        font-size: 20px;
        font-weight: 700;
        color: var(--el-text-color-primary);
        letter-spacing: 0.5px;
        font-family: 'DIN Alternate', sans-serif;

        &.primary {
          color: var(--el-color-primary);
        }
      }
    }
  }
}

// 工具栏区域
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  padding: 20px;
  @include mixins.glass-effect;
  border-radius: 12px;
  margin-bottom: 20px;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: vars.$shadow-md;
  }

  .el-input, .el-select, .el-date-picker {
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
    }

    &:focus-within {
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
    }
  }

  .el-button {
    padding: 10px 20px;
    font-weight: 500;
    font-size: 14px;
    height: 36px;
    transition: all 0.3s;
    border-radius: 6px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }


    &--primary {
      background: var(--el-color-primary);
      border-color: var(--el-color-primary);

      &:hover {
        background: var(--el-color-primary);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
      }
    }
  }
}

.mt-16 {
  margin-top: 20px;
  transition: margin-top 0.3s;
}

.mr-1 {
  margin-right: 4px;
}

.role-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .role-icon {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    
    &.success { color: var(--el-color-success); }
    &.warning { color: var(--el-color-warning); }
    &.danger { color: var(--el-color-danger); }
  }
  
  .role-tag {
    border: none;
    background: transparent;
    padding: 0;
    height: auto;
    font-size: 13px;
  }
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  
  .disease-tag {
    display: inline-flex;
    align-items: center;
    border-radius: 12px;
    padding: 0 8px;
    height: 22px;
    line-height: 20px;
    border: none;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
  }
}

// 星标单元格整齐
.star-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 4px 0;

  .el-icon {
    transition: transform 0.3s, color 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      transform: scale(1.3);
    }
  }
}

// 操作按颁整齐布局
.operation-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
  width: 100%;
  row-gap: 6px;

  .el-button {
    &.is-link {
      padding: 4px 8px;
      font-size: 12px;
      height: 28px;
      line-height: 20px;
      transition: all 0.2s;
      border-radius: 4px;
      font-weight: 500;
      white-space: nowrap;
      min-width: 60px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      &.is-disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      :deep(.el-icon) {
        margin-right: 2px;
      }
    }
  }
}

// 全局 loading 样式
:deep(.el-loading-mask) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.7);
}

// 全局模态框样式
:deep(.el-message-box) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

// 全局消息提示样式
:deep(.el-message) {
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

// 表格优化
:deep(.el-table) {
  @include mixins.glass-effect;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.3);
  --el-table-row-hover-bg-color: rgba(var(--el-color-primary-rgb), 0.1);

  &:hover {
    box-shadow: vars.$shadow-lg;
  }

  .el-table__inner-wrapper::before {
    display: none; // remove bottom border
  }

  .el-table__header {
    th {
      background: rgba(255, 255, 255, 0.5);
      font-weight: 600;
      font-size: 14px;
      color: var(--el-text-color-primary);
      padding: 12px 8px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      letter-spacing: 0.3px;
      height: 48px;
      line-height: 24px;
    }
  }

  .el-table__body {
    tr {
      transition: background-color 0.3s, transform 0.3s;
      cursor: pointer;
      height: 64px; // Slightly taller rows for better touch target

      &:hover {
        background-color: var(--el-table-row-hover-bg-color) !important;
        transform: scale(1.002); // Micro-interaction
        z-index: 1;
        position: relative;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
      }

      td {
        padding: 12px 8px;
        font-size: 14px;
        color: var(--el-text-color-primary);
        line-height: 1.5;
        vertical-align: middle;
        border-bottom: 1px solid rgba(0, 0, 0, 0.03);

        .name-cell {
          display: flex;
          align-items: center;
          gap: 12px;
          height: 40px;

          :deep(.el-avatar) {
            flex-shrink: 0;
            border: 2px solid #fff;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
          }

          span {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            font-weight: 500;
            font-size: 15px;
          }
        }

        .placeholder-text {
          color: var(--el-text-color-placeholder);
          font-size: 13px;
        }
      }
    }
  }

  .el-button {
    &.is-link {
      padding: 6px 12px;
      font-size: 13px;
      height: auto;
      transition: all 0.2s;
      border-radius: 4px;
      font-weight: 500;

      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      &.is-disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }

  .el-icon {
    transition: transform 0.3s, color 0.3s;
    cursor: pointer;

    &:hover {
      transform: scale(1.25);
    }
  }

  .el-tag {
    transition: all 0.2s;
    border-radius: 4px;
    font-size: 13px;
    padding: 4px 10px;

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
    }
  }

  .el-avatar {
    flex-shrink: 0;
  }
}

// 响应式设计
@media (max-width: 1366px) {
  .doctor-patients {
    padding: 16px;
  }

  .family-selector,
  .toolbar {
    padding: 16px;
  }

  .family-selector {
    .family-info {
      .label {
        font-size: 14px;
      }

      .current-family-name {
        font-size: 14px;
      }
    }

    .stats-info {
      gap: 20px;

      .stat-item {
        .stat-label {
          font-size: 13px;
        }

        .stat-value {
          font-size: 16px;
        }
      }
    }
  }

  :deep(.el-table) {
    .el-table__header th,
    .el-table__body td {
      padding: 12px 8px;
      font-size: 13px;
    }
  }
}

@media (max-width: 768px) {
  .doctor-patients {
    padding: 12px;
  }

  .family-selector {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    padding: 12px;

    .family-info {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;

      .label {
        font-size: 13px;
      }

      .current-family-name {
        font-size: 13px;
      }
    }

    .stats-info {
      gap: 16px;
      flex-wrap: wrap;

      .stat-item {
        flex: 1;
        min-width: 120px;

        .stat-label {
          font-size: 12px;
        }

        .stat-value {
          font-size: 14px;
        }
      }
    }
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
    padding: 12px;
    gap: 8px;

    .el-input, .el-select, .el-date-picker, .el-button {
      width: 100% !important;
      height: 36px;
    }
  }

  :deep(.el-table) {
    .el-table__header th {
      padding: 10px 6px;
      font-size: 12px;
    }

    .el-table__body td {
      padding: 12px 6px;
      font-size: 12px;

      .name-cell {
        :deep(.el-avatar) {
          width: 28px !important;
          height: 28px !important;
          font-size: 11px;
        }
      }
    }

    .el-button.is-link {
      padding: 4px 6px;
      font-size: 12px;
    }
  }

  .mt-16 {
    margin-top: 12px;
  }
}
</style>
