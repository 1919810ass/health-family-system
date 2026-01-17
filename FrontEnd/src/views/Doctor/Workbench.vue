<template>
  <div class="doctor-workbench">
    <!-- 固定的家庭选择器 -->
    <div class="family-selector">
      <div class="family-info">
        <span class="label">当前家庭：</span>
        <el-select :model-value="familyId" placeholder="选择家庭" style="width: 240px" @change="onSwitch">
          <el-option v-for="f in families" :key="f.id" :label="f.name" :value="String(f.id)" />
        </el-select>
        <span v-if="currentFamilyName" class="current-family-name">{{ currentFamilyName }}</span>
      </div>
      <div class="action-group">
        <el-button type="success" :icon="Plus" @click="showInviteCode">邀请家庭</el-button>
        <el-switch
          v-model="aiEnabled"
          active-text="AI生成"
          inactive-text="普通模式"
          @change="handleAiToggle"
          class="ai-switch"
        />
        <el-button type="primary" @click="loadAll" :loading="loading" :icon="Refresh">刷新数据</el-button>
      </div>
    </div>

    <!-- 快捷入口区 -->
    <div class="quick-actions mt-16">
       <div class="quick-item" @click="goToPatients">
         <div class="icon-box blue">
           <el-icon><UserFilled /></el-icon>
         </div>
         <span>患者档案</span>
       </div>
       <div class="quick-item" @click="goToConsultation">
         <div class="icon-box green">
           <el-icon><ChatDotRound /></el-icon>
         </div>
         <span>在线问诊</span>
       </div>
       <div class="quick-item" @click="goToPlans">
         <div class="icon-box orange">
           <el-icon><Document /></el-icon>
         </div>
         <span>健康计划</span>
       </div>
       <div class="quick-item" @click="goToMonitoring">
         <div class="icon-box red">
           <el-icon><Monitor /></el-icon>
         </div>
         <span>预警监测</span>
       </div>
    </div>

    <el-row :gutter="16" class="mt-16">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span><el-icon class="header-icon"><List /></el-icon> 待处理事项</span>
              <el-tag type="danger" effect="dark" round v-if="totalPending > 0">{{ totalPending }}</el-tag>
            </div>
          </template>
          <div class="pending-item">
            <span>待回复咨询：</span>
            <el-link v-if="pendingData.consultations > 0" type="primary" @click="goToConsultation">{{ pendingData.consultations }}</el-link>
            <span v-else class="text-gray">0</span>
          </div>
          <div class="pending-item">
            <span>待审核计划：</span>
            <el-link v-if="pendingData.plans > 0" type="primary" @click="goToPlans">{{ pendingData.plans }}</el-link>
            <span v-else class="text-gray">0</span>
          </div>
          <div class="pending-item">
            <span>今日需随访：</span>
            <el-link v-if="pendingData.followups > 0" type="primary" @click="goToFollowups">{{ pendingData.followups }}</el-link>
            <span v-else class="text-gray">0</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span><el-icon class="header-icon"><TrendCharts /></el-icon> 患者健康概览</span>
            </div>
          </template>
          <div v-for="(v,k) in status" :key="k" class="status-item">
            <span>{{ k }}：</span>
            <span class="status-value">{{ v }}</span>
          </div>
          <div v-if="Object.keys(status).length === 0" class="empty-hint">暂无数据</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span><el-icon class="header-icon"><DataLine /></el-icon> 数据简报</span>
            </div>
          </template>
          <div class="brief-item">
            <span>本周咨询量</span>
            <span class="brief-value">{{ brief.consults }}</span>
          </div>
          <div class="brief-item">
            <span>新增患者数</span>
            <span class="brief-value">{{ brief.newPatients }}</span>
          </div>
          <div class="brief-item">
            <span>改善率</span>
            <span class="brief-value highlight">{{ brief.improveRate }}%</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt-16" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon class="header-icon"><Bell /></el-icon> 异常提醒</span>
        </div>
      </template>
      <div v-if="abnormalEvents && abnormalEvents.length > 0" class="abnormal-list">
        <div
          v-for="(event, index) in abnormalEvents"
          :key="index"
          class="abnormal-item"
          @click="handleAbnormalClick(event)"
        >
          <div class="abnormal-icon-wrapper" :class="{ 'is-alert': event.type === 'ALERT' }">
             <el-icon><Warning /></el-icon>
          </div>
          <div class="abnormal-content">
            <div class="abnormal-title-row">
               <el-tag :type="event.type === 'ALERT' ? 'danger' : 'warning'" size="small" effect="dark" class="mr-8">
                 {{ event.type === 'ALERT' ? '预警' : '提醒' }}
               </el-tag>
               <span class="abnormal-title">{{ event.title }}</span>
            </div>
            <span class="abnormal-time">{{ formatTime(event.createdAt) }}</span>
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>
      <div v-else class="empty-hint">暂无异常</div>
    </el-card>

    <!-- 高风险患者区块 -->
    <el-card class="mt-16" shadow="hover">
      <template #header>
        <div class="card-header">
          <span><el-icon class="header-icon"><FirstAidKit /></el-icon> 高风险患者</span>
        </div>
      </template>
      <el-table :data="highRiskMembers" style="width: 100%" v-loading="loading">
        <el-table-column prop="nickname" label="姓名" width="120" />
        <el-table-column prop="familyName" label="家庭" width="150" />
        <el-table-column label="疾病/风险标签" min-width="200">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tags" :key="tag" type="warning" size="small" class="mr-4">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近异常时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastAbnormalTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewPatientDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!highRiskMembers || highRiskMembers.length === 0" class="empty-hint">暂无高风险患者</div>
    </el-card>

    <el-alert v-if="summary" type="info" :closable="false" class="mt-16" :title="summary" />
    
    <!-- 邀请码弹窗 -->
    <el-dialog
      v-model="inviteDialogVisible"
      title="邀请家庭签约"
      width="400px"
      append-to-body
      class="invite-dialog"
    >
      <div class="invite-content">
        <p class="invite-tip">请将以下邀请码发送给家庭管理员，<br>他们在"家庭设置"中输入即可完成签约绑定。</p>
        <div class="invite-code-box" @click="copyInviteCode">
          <span class="code">{{ userStore.profile?.id }}</span>
          <el-icon class="copy-icon"><CopyDocument /></el-icon>
        </div>
        <p class="copy-hint">点击复制邀请码</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useDoctorStore } from '../../stores/doctor'
import { useUserStore } from '../../stores/user'
import { List, TrendCharts, DataLine, Bell, Warning, FirstAidKit, ArrowRight, Refresh, UserFilled, ChatDotRound, Document, Monitor, Plus, CopyDocument } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const doctorStore = useDoctorStore()
const userStore = useUserStore()

// 邀请码相关
const inviteDialogVisible = ref(false)
const showInviteCode = () => {
  inviteDialogVisible.value = true
}

const copyInviteCode = async () => {
  try {
    await navigator.clipboard.writeText(String(userStore.profile?.id))
    ElMessage.success('邀请码已复制')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

// 导航方法
const goToPatients = () => router.push('/doctor/patients')
const goToConsultation = () => router.push('/doctor/consultation')
const goToPlans = () => router.push('/doctor/plans')
const goToFollowups = () => router.push('/doctor/plans?tab=followups')
const goToMonitoring = () => router.push('/doctor/enhanced-monitoring')

// 使用 store 中的状态
const families = computed(() => doctorStore.families)
const familyId = computed(() => doctorStore.currentFamilyId)
const currentFamilyName = computed(() => {
  if (!familyId.value) return ''
  const family = families.value.find(f => String(f.id) === String(familyId.value))
  return family?.name || ''
})
const loading = computed(() => doctorStore.loadingStates.summary)
const summary = computed(() => doctorStore.summary)
const telemetry = computed(() => doctorStore.telemetry)
const pendingData = computed(() => doctorStore.pendingData)
const totalPending = computed(() => {
  return (pendingData.value.consultations || 0) + (pendingData.value.plans || 0) + (pendingData.value.followups || 0)
})
const abnormalEvents = computed(() => doctorStore.abnormalEvents)
const highRiskMembers = computed(() => doctorStore.highRiskMembers)
const aiEnabled = computed({
  get: () => doctorStore.aiEnabled,
  set: (value) => doctorStore.toggleAi(value)
})

// 本地状态
const status = ref({})
const brief = ref({ consults: 0, newPatients: 0, improveRate: 0 })

// 从遥测数据计算状态
watch(telemetry, (tel) => {
  status.value = Object.keys(tel).reduce((acc, key) => {
    const item = tel[key]
    acc[key] = item?.count ?? 0
    return acc
  }, {})
}, { immediate: true })

// 格式化时间
const formatTime = (time) => {
  if (!time) return '—'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 加载所有数据
const loadAll = async () => {
  if (!familyId.value) {
    ElMessage.error('请选择家庭')
    return
  }
  try {
    await doctorStore.fetchSummary(familyId.value, aiEnabled.value)
    // 数据简报可以在这里扩展（基于实际业务需求）
    brief.value = { consults: 0, newPatients: 0, improveRate: 0 }
  } catch (e) {
    ElMessage.error('数据加载失败')
  }
}

// 处理AI开关切换
const handleAiToggle = async (enabled) => {
  if (!familyId.value) {
    ElMessage.warning('请先选择家庭')
    return
  }
  try {
    await doctorStore.fetchSummary(familyId.value, enabled)
    ElMessage.success(enabled ? 'AI生成已启用' : '已切换到普通模式')
  } catch (e) {
    ElMessage.error('切换失败')
  }
}

// 切换家庭
const onSwitch = async (id) => {
  await doctorStore.setCurrentFamily(id)
  // 切换家庭后自动刷新数据
  ElMessage.success(`已切换到家庭：${currentFamilyName.value}`)
}





// 处理异常事件点击
const handleAbnormalClick = (event) => {
  if (event.memberUserId) {
    // 跳转到患者详情页
    router.push(`/doctor/patients?memberId=${event.memberUserId}`)
  } else {
    // 可以跳转到提醒列表
    ElMessage.info('功能开发中')
  }
}

// 查看患者详情
const viewPatientDetail = (row) => {
  router.push(`/doctor/patients?memberId=${row.userId}`)
}

onMounted(async () => {
  try {
    // 加载家庭列表（会自动选择默认家庭）
    await doctorStore.fetchFamilies()
    // 如果已有当前家庭，加载基础数据（不自动启用AI）
    if (doctorStore.currentFamilyId) {
      // 只加载基础数据，AI功能需要用户手动启用
      await doctorStore.fetchSummary(doctorStore.currentFamilyId, false)
    }
  } catch (error) {
    console.error('初始化失败:', error)
  }
})
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.doctor-workbench {
  padding: 24px;
  background: transparent;
  min-height: calc(100vh - 60px);
  position: relative;
  z-index: 1;
  
  // Medical Background Pattern (Crosses)
  background-image: radial-gradient(rgba(64, 158, 255, 0.08) 2px, transparent 2px);
  background-size: 32px 32px;
}

.quick-actions {
  display: flex;
  gap: 16px;
  
  .quick-item {
    flex: 1;
    @include mixins.glass-effect;
    padding: 20px;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.3s vars.$ease-spring;
    border: 1px solid rgba(255, 255, 255, 0.4);
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
      background: rgba(255, 255, 255, 0.9);
      
      .icon-box {
        transform: scale(1.1) rotate(10deg);
      }
    }
    
    .icon-box {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      transition: all 0.4s vars.$ease-spring;
      
      &.blue { background: rgba(64, 158, 255, 0.1); color: var(--el-color-primary); }
      &.green { background: rgba(103, 194, 58, 0.1); color: var(--el-color-success); }
      &.orange { background: rgba(230, 162, 60, 0.1); color: var(--el-color-warning); }
      &.red { background: rgba(245, 108, 108, 0.1); color: var(--el-color-danger); }
    }
    
    span {
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }
}

.family-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  @include mixins.glass-effect;
  border-radius: 16px;
  margin-bottom: 24px;
  transition: all 0.4s vars.$ease-spring;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: vars.$shadow-md;
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
      font-weight: 700;
      font-size: 16px;
      background: rgba(var(--el-color-primary-rgb), 0.1);
      padding: 4px 12px;
      border-radius: 20px;
      border: 1px solid rgba(var(--el-color-primary-rgb), 0.2);
    }
  }

  .action-group {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .ai-switch {
    --el-switch-on-color: var(--el-color-success);
  }
}

// 卡片统一样式优化
.stat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  
  // 装饰性背景图标
  &::after {
    content: '';
    position: absolute;
    right: -20px;
    bottom: -20px;
    width: 100px;
    height: 100px;
    background: radial-gradient(circle, var(--el-color-primary-light-9) 0%, transparent 70%);
    border-radius: 50%;
    opacity: 0.5;
    z-index: 0;
    pointer-events: none;
  }

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    position: relative;
    z-index: 1;
  }
}

:deep(.el-card) {
  @include mixins.glass-effect;
  border-radius: 16px;
  transition: all 0.4s vars.$ease-spring;

  &:hover {
    transform: translateY(-4px);
    box-shadow: vars.$shadow-lg;
    border-color: rgba(var(--el-color-primary-rgb), 0.3);
  }

  .el-card__header {
    padding: 16px 24px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    background: rgba(255, 255, 255, 0.3);
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 16px;
  color: var(--el-text-color-primary);

  span {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .header-icon {
    font-size: 20px;
    padding: 6px;
    background: var(--el-color-primary-light-9);
    border-radius: 8px;
    color: var(--el-color-primary);
    transition: all 0.3s;
  }
}

// 悬停时图标动画
:deep(.el-card:hover) .header-icon {
  background: var(--el-color-primary);
  color: #fff;
  transform: scale(1.1) rotate(5deg);
}

.pending-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
  transition: background 0.3s;
  border-radius: 4px;
  padding-left: 8px;
  padding-right: 8px;

  &:hover {
    background: rgba(0,0,0,0.02);
  }

  &:last-child {
    border-bottom: none;
  }

  span:first-child {
    font-size: 14px;
    color: var(--el-text-color-regular);
  }

  .el-link {
    font-size: 20px;
    font-weight: 600;
    font-family: 'DIN Alternate', sans-serif;
  }
  
  .text-gray {
    color: var(--el-text-color-placeholder);
    font-family: 'DIN Alternate', sans-serif;
    font-size: 20px;
  }
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 8px;
  transition: transform 0.3s;
  
  &:hover {
    transform: translateX(4px);
  }
  
  span:first-child {
    color: var(--el-text-color-regular);
  }

  .status-value {
    font-weight: 600;
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    padding: 2px 10px;
    border-radius: 10px;
    font-size: 13px;
  }
}

.brief-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  
  .brief-value {
    font-weight: 700;
    font-size: 18px;
    font-family: 'DIN Alternate', sans-serif;
    color: var(--el-text-color-primary);
    
    &.highlight {
      color: var(--el-color-success);
    }
  }
}

.empty-hint {
  color: var(--el-text-color-placeholder);
  text-align: center;
  padding: 32px 20px;
  font-size: 14px;
}

.abnormal-list {
  .abnormal-item {
    display: flex;
    align-items: center;
    padding: 16px;
    margin-bottom: 12px;
    background: #fff;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid transparent;
    box-shadow: 0 2px 8px rgba(0,0,0,0.02);

    &:hover {
      background: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary-light-7);
      transform: translateX(4px);
      
      .arrow-icon {
        opacity: 1;
        transform: translateX(0);
      }
    }

    .abnormal-icon-wrapper {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: var(--el-color-warning-light-9);
      color: var(--el-color-warning);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      margin-right: 16px;
      flex-shrink: 0;
      
      &.is-alert {
        background: var(--el-color-danger-light-9);
        color: var(--el-color-danger);
      }
    }

    .abnormal-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .abnormal-title-row {
        display: flex;
        align-items: center;
      }
      
      .abnormal-title {
        font-weight: 600;
        font-size: 15px;
        color: var(--el-text-color-primary);
      }

      .abnormal-time {
        color: var(--el-text-color-secondary);
        font-size: 12px;
      }
    }
    
    .arrow-icon {
      opacity: 0;
      transform: translateX(-10px);
      transition: all 0.3s;
      color: var(--el-text-color-placeholder);
    }
  }
}

// 表格优化
:deep(.el-table) {
  --el-table-header-bg-color: var(--el-fill-color-light);
  border-radius: 8px;
  overflow: hidden;

  th {
    font-weight: 600;
    color: var(--el-text-color-primary);
    height: 50px;
  }

  td {
    height: 60px;
  }
}

.mt-16 {
  margin-top: 24px;
}

.mr-8 {
  margin-right: 8px;
}

// 响应式设计
@media (max-width: 1200px) {
  .doctor-workbench {
    padding: 16px;
  }
  
  :deep(.el-col) {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .family-selector {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;

    .family-info {
      flex-direction: column;
      align-items: flex-start;
      
      .el-select {
        width: 100% !important;
      }
    }
    
    .action-group {
      justify-content: space-between;
    }
  }

  :deep(.el-col-8) {
    max-width: 100%;
    flex: 0 0 100%;
  }
}

.invite-content {
  text-align: center;
  padding: 10px 0 20px;

  .invite-tip {
    color: var(--el-text-color-regular);
    margin-bottom: 24px;
    line-height: 1.6;
  }

  .invite-code-box {
    background: var(--el-color-primary-light-9);
    border: 1px dashed var(--el-color-primary);
    border-radius: 12px;
    padding: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.3s;
    margin-bottom: 8px;

    &:hover {
      background: var(--el-color-primary-light-8);
      transform: scale(1.02);
    }

    .code {
      font-size: 32px;
      font-weight: bold;
      color: var(--el-color-primary);
      font-family: monospace;
      letter-spacing: 2px;
    }

    .copy-icon {
      font-size: 20px;
      color: var(--el-text-color-secondary);
    }
  }

  .copy-hint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}
</style>
