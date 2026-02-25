<template>
  <div class="doctor-workbench-v2">
    <!-- 顶部欢迎区 -->
    <div class="welcome-header">
      <div class="welcome-main">
        <div class="welcome-badge-row">
          <span class="welcome-badge">医生端 · 健康监测中心</span>
        </div>
        <h1 class="welcome-title">
          {{ greetingText }}，{{ doctorName }}
        </h1>
        <p class="welcome-subtitle">
          今天是 {{ todayText }}，当前已服务
          <span class="welcome-highlight">{{ dashboardData.managedFamilies || 0 }}</span>
          个家庭、{{ dashboardData.totalPatients || 0 }} 位患者。
        </p>
      </div>
      <div class="welcome-meta">
        <div class="welcome-meta-card">
          <div class="meta-label">待处理异常</div>
          <div class="meta-value">{{ dashboardData.pendingAlerts || 0 }}</div>
        </div>
        <div class="welcome-meta-card">
          <div class="meta-label">本月生成报告</div>
          <div class="meta-value small">{{ dashboardData.monthlyReports || 0 }}</div>
        </div>
        <el-button type="primary" round class="welcome-refresh-btn" @click="loadDashboard">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="stat-card glass-card">
          <div class="stat-content">
            <div class="stat-icon-wrapper blue">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-label">管理家庭</div>
              <div class="stat-value">{{ dashboardData.managedFamilies || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="stat-card glass-card">
          <div class="stat-content">
            <div class="stat-icon-wrapper green">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-label">患者总数</div>
              <div class="stat-value">{{ dashboardData.totalPatients || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="stat-card glass-card">
          <div class="stat-content">
            <div class="stat-icon-wrapper orange">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-label">待处理异常</div>
              <div class="stat-value">{{ dashboardData.pendingAlerts || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" :xs="24" :sm="12" :md="12" :lg="6">
        <el-card shadow="hover" class="stat-card glass-card">
          <div class="stat-content">
            <div class="stat-icon-wrapper red">
              <el-icon><DocumentChecked /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-label">本月报告</div>
              <div class="stat-value">{{ dashboardData.monthlyReports || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主体内容区 -->
    <el-row :gutter="16" class="mt-16 main-grid">
      <!-- 左侧主栏 -->
      <el-col :span="17" :xs="24" :md="16" :lg="17">
        <!-- 高风险患者雷达 -->
        <el-card shadow="hover" class="section-card glass-card high-risk-card">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon class="header-icon"><FirstAidKit /></el-icon>
                高风险患者雷达
              </span>
              <span 
                class="header-link text-xs text-gray-400 hover:text-blue-500 cursor-pointer transition-colors duration-200"
                @click="goToPatients"
              >
                查看全部 <el-icon class="ml-1"><ArrowRight /></el-icon>
              </span>
            </div>
          </template>
          <!-- 新的高风险患者表格 -->
          <el-table
            class="high-risk-table"
            :data="dashboardData.criticalPatients"
            style="width: 100%"
            v-loading="loading"
            highlight-current-row
            @row-click="handleOpenPatientDetail"
          >
            <el-table-column label="患者信息" width="180">
              <template #default="scope">
                <div class="flex items-center">
                  <el-avatar :src="scope.row.avatar" />
                  <span class="ml-2 font-bold">{{ scope.row.nickname || scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="风险提示">
              <template #default="scope">
                <div class="risk-cell">
                  <el-tag
                    v-if="scope.row.riskType"
                    :type="riskTagType(scope.row)"
                    size="small"
                    class="risk-pill-tag"
                  >
                    {{ riskTypeLabel(scope.row.riskType) }}
                  </el-tag>
                  <span class="risk-text" v-if="scope.row.riskDescription">
                    {{ scope.row.riskDescription }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="最近异常时间" width="180">
              <template #default="{ row }">{{ formatTime(row.riskTime || row.lastAbnormalTime) }}</template>
            </el-table-column>
            <el-table-column label="近7日趋势" width="140">
              <template #default="scope">
                <MiniTrend
                  :data="scope.row.recentTrend || []"
                  :color="scope.row.riskLevel === 'CRITICAL' ? '#ef4444' : '#f59e0b'"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-tooltip content="查看健康档案" placement="top">
                  <el-button
                    text
                    type="primary"
                    class="op-button table-action-icon"
                    @click.stop="handleOpenPatientDetail(row)"
                  >
                    <el-icon class="view-icon">
                      <View />
                    </el-icon>
                  </el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!dashboardData.criticalPatients || dashboardData.criticalPatients.length === 0" class="empty-hint">
            暂无高风险患者
          </div>
        </el-card>

        <!-- 患者详情抽屉 -->
        <PatientDetailDrawer
          ref="detailDrawerRef"
          v-model="detailDrawerVisible"
          :family-id="currentFamilyId"
          :patient-user-id="currentPatientId"
          @refresh="handleDrawerRefresh"
        />

        <!-- 待接诊咨询 -->
        <el-card shadow="hover" class="mt-16 section-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><ChatDotRound /></el-icon> 待接诊咨询</span>
              <span 
                              class="header-link text-xs text-gray-400 hover:text-blue-500 cursor-pointer transition-colors duration-200"
                              @click="goToConsultation"
                            >
                              处理更多 <el-icon class="ml-1"><ArrowRight /></el-icon>
                            </span>
            </div>
          </template>
          <!-- 待接诊列表 -->
          <div class="task-list">
            <div v-for="task in dashboardData.pendingConsultations" :key="task.sessionId" class="task-item">
              <div class="task-patient-info">
                <el-avatar :src="task.avatarUrl" :size="32">
                  {{ task.patientName?.charAt(0) || '患' }}
                </el-avatar>
                <span class="patient-name">{{ task.patientName }}</span>
              </div>
              <div class="task-summary">{{ task.requestSummary }}</div>
              <div class="task-extra-info">
                <el-tag size="small" type="info">{{ task.suggestedDepartment }}</el-tag>
                <span>等待 {{ task.waitingTime }}</span>
              </div>
              <el-button 
                              type="primary" 
                              link 
                              class="text-sm"
                              @click="startConsultation(task.sessionId)">
                              立即接诊
                            </el-button>
            </div>
          </div>
          <div v-if="!dashboardData.pendingConsultations || dashboardData.pendingConsultations.length === 0" class="empty-hint">
            当前无待处理咨询
          </div>
        </el-card>
      </el-col>

      <!-- 右侧侧栏 -->
      <el-col :span="7" :xs="24" :md="8" :lg="7">
        <!-- 时令小贴士 -->
        <el-card shadow="hover" class="seasonal-card section-card" :style="seasonalCardStyle">
          <div class="seasonal-content-new">
            <div class="seasonal-term">{{ dashboardData.seasonalSolarTerm || '节气' }}</div>
            <p class="seasonal-advice">{{ dashboardData.seasonalAdvice }}</p>
            <el-button
              type="primary"
              link
              class="seasonal-send-btn"
              :loading="sendingSeasonal"
              @click="onSendSeasonalAdvice"
            >
              发送给患者
            </el-button>
          </div>
        </el-card>

        <!-- 异常动态时间轴 -->
        <el-card shadow="hover" class="mt-16 section-card glass-card">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon class="header-icon"><Bell /></el-icon>
                异常动态
              </span>
            </div>
          </template>
          <el-timeline class="px-10">
            <el-timeline-item
              v-for="(activity, index) in abnormalEvents"
              :key="index"
              :timestamp="formatTime(activity.time)"
              :type="activity.type === 'ALERT' ? 'danger' : 'warning'"
            >
              {{ activity.message }}
            </el-timeline-item>
          </el-timeline>
           <div v-if="!abnormalEvents || abnormalEvents.length === 0" class="empty-hint">
            暂无异常动态
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 组件：Workbench.vue
 *
 * 业务说明：用于呈现对应页面/模块功能，并通过 API 层与后端进行数据交互。
 */

import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWorkbenchDashboard, sendSeasonalAdviceToPatients } from '../../api/doctor'
import { 
  OfficeBuilding, 
  User, 
  Warning, 
  DocumentChecked, 
  FirstAidKit, 
  ChatDotRound, 
  Sunny, 
  Bell,
  Refresh,
  View,
  ArrowRight
} from '@element-plus/icons-vue'
import PatientDetailDrawer from '@/components/Common/PatientDetailDrawer.vue'
import MiniTrend from '@/components/Common/MiniTrend.vue'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const sendingSeasonal = ref(false)
const dashboardData = ref({})

const solarTermImages = import.meta.glob('@/assets/solar-terms/*.png', { eager: true, import: 'default' })

const seasonalCardStyle = computed(() => {
  const term = dashboardData.value.seasonalSolarTerm?.trim()
  if (!term) return {}
  const matchedKey = Object.keys(solarTermImages).find((path) => path.endsWith(`/${term}.png`))
  const imageUrl = matchedKey ? solarTermImages[matchedKey] : ''
  if (imageUrl) {
    return {
      backgroundImage: `linear-gradient(to top, rgba(0,0,0,0.7), transparent), url(${imageUrl})`,
    }
  }
  return {}
})

const doctorName = ref(localStorage.getItem('doctor_name') || '医生')
const greetingText = computed(() => {
  const hour = dayjs().hour()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})
const todayText = computed(() => dayjs().format('YYYY年MM月DD日'))

// 患者详情抽屉控制
const detailDrawerVisible = ref(false)
const currentPatientId = ref(null)
const currentFamilyId = ref(null)
const detailDrawerRef = ref(null)

// 异常动态：来自工作台聚合接口
const abnormalEvents = computed(() => dashboardData.value.abnormalEvents || [])

// 导航方法
const goToPatients = () => router.push('/doctor/patients')
const goToConsultation = () => router.push('/doctor/consultation')

// 格式化时间
const formatTime = (time) => {
  if (!time) return '—'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 风险类型显示文案
const riskTypeLabel = (riskType) => {
  if (!riskType) return '高风险'
  if (riskType === 'VITALS_WARNING') return '体征异常'
  if (riskType === 'TCM_IMBALANCE') return '体质偏颇'
  return '高风险'
}

// 根据风险等级决定标签颜色
const riskTagType = (row) => {
  const level = row?.riskLevel
  if (level === 'CRITICAL') return 'danger'
  if (level === 'WARNING') return 'warning'
  return 'info'
}

// 打开患者详情抽屉
const handleOpenPatientDetail = (row) => {
  const userId = row?.userId || row?.patientId
  const familyId = row?.familyId
  if (!userId || !familyId) {
    return
  }
  currentPatientId.value = userId
  currentFamilyId.value = String(familyId)
  detailDrawerVisible.value = true

  // 如果后续 PatientDetailDrawer 增加 open 方法，这里兼容调用
  nextTick(() => {
    if (detailDrawerRef.value && typeof detailDrawerRef.value.open === 'function') {
      detailDrawerRef.value.open(userId, familyId)
    }
  })
}

// 加载工作台数据
const loadDashboard = async () => {
  loading.value = true
  try {
    const response = await getWorkbenchDashboard()
    // Mock 数据增强
    const enhancedPatients = response.data.criticalPatients.map(p => ({
      ...p,
      recentTrend: Array.from({ length: 7 }, () => 120 + Math.floor(Math.random() * 40)),
    }));
    response.data.criticalPatients = enhancedPatients;
    dashboardData.value = response.data;
  } catch (e) {
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

// 抽屉内部标记已处理后，刷新工作台数据
const handleDrawerRefresh = () => {
  loadDashboard()
}

// 发送今日时令小贴士给所有绑定患者
const onSendSeasonalAdvice = async () => {
  if (!dashboardData.value.seasonalAdvice) {
    ElMessage.warning('暂无可发送的时令小贴士')
    return
  }
  sendingSeasonal.value = true
  try {
    await sendSeasonalAdviceToPatients()
    ElMessage.success('已将今日时令小贴士发送给患者')
  } catch (e) {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    sendingSeasonal.value = false
  }
}

// 接诊
const startConsultation = (sessionId) => {
  router.push(`/doctor/consultation?sessionId=${sessionId}`)
}

// 查看患者详情
const viewPatientDetail = (patientId) => {
  router.push(`/doctor/patients?memberId=${patientId}`)
}

onMounted(() => {
  loadDashboard();
});
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

// 旧版样式保留（部分页面仍可能引用）
.doctor-workbench {
  padding: 24px;
  background: transparent;
  min-height: calc(100vh - 60px);
  position: relative;
  z-index: 1;
  
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
  transition: transform 0.3s, background-color 0.3s;
  cursor: pointer;
  border-radius: 6px;
  
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

// 新版工作台 V2 样式（2025 Clean Medical 风格）
.doctor-workbench-v2 {
  padding: 24px 24px 32px;
  min-height: calc(100vh - 60px);
  background: radial-gradient(circle at top left, #ecf5ff 0%, #f8fafc 40%, #ffffff 100%);

  .welcome-header {
    display: flex;
    align-items: stretch;
    justify-content: space-between;
    gap: 24px;
    padding: 18px 24px;
    border-radius: 20px;
    background: linear-gradient(120deg, rgba(64,158,255,0.16), rgba(103,194,58,0.06));
    box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
    margin-bottom: 20px;
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      right: -40px;
      top: -40px;
      width: 160px;
      height: 160px;
      background: radial-gradient(circle, rgba(255,255,255,0.5) 0%, transparent 70%);
      opacity: 0.9;
    }

    .welcome-main {
      display: flex;
      flex-direction: column;
      gap: 8px;
      position: relative;
      z-index: 1;
    }

    .welcome-badge-row {
      margin-bottom: 4px;
    }

    .welcome-badge {
      display: inline-flex;
      align-items: center;
      padding: 2px 10px;
      border-radius: 999px;
      font-size: 12px;
      color: #1d4ed8;
      background: rgba(255,255,255,0.7);
      border: 1px solid rgba(191,219,254,0.8);
      backdrop-filter: blur(6px);
    }

    .welcome-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      letter-spacing: 0.02em;
      color: #0f172a;
    }

    .welcome-subtitle {
      margin: 0;
      font-size: 13px;
      color: #0f172a;
      opacity: 0.9;
    }

    .welcome-highlight {
      font-weight: 600;
      color: #1d4ed8;
    }

    .welcome-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      position: relative;
      z-index: 1;
    }

    .welcome-meta-card {
      min-width: 96px;
      padding: 10px 12px;
      border-radius: 14px;
      background: rgba(255,255,255,0.9);
      box-shadow: 0 10px 30px rgba(15,23,42,0.12);
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      gap: 4px;
    }

    .meta-label {
      font-size: 11px;
      color: #64748b;
    }

    .meta-value {
      font-size: 22px;
      font-weight: 700;
      color: #0f172a;

      &.small {
        font-size: 18px;
      }
    }

    .welcome-refresh-btn {
      align-self: flex-end;
      background: rgba(255,255,255,0.8);
      border-color: transparent;
      color: #0f172a;
      backdrop-filter: blur(8px);

      &:hover, &:focus {
        background: #fff;
        border-color: transparent;
        color: #0284c7;
      }
    }
  }

  .stats-row {
    .stat-card {
      border: none;
      box-shadow: 0 10px 25px -3px rgba(15,23,42,0.05), 0 4px 6px -4px rgba(15,23,42,0.05);

      &:hover {
        border: none;
        box-shadow: 0 20px 25px -5px rgba(15,23,42,0.1), 0 8px 10px -6px rgba(15,23,42,0.1);
      }

      &.glass-card {
        background: rgba(255,255,255,0.6);
        backdrop-filter: blur(12px);
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
      }

      .stat-icon-wrapper {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;

        &.blue { background: #dbeafe; color: #2563eb; }
        &.green { background: #dcfce7; color: #16a34a; }
        &.orange { background: #ffedd5; color: #f97316; }
        &.red { background: #fee2e2; color: #dc2626; }
      }

      .stat-text {
        display: flex;
        flex-direction: column;
        gap: 2px;
      }

      .stat-label {
        font-size: 12px;
        color: #475569;
      }

      .stat-value {
        font-size: 22px;
        font-weight: 700;
        color: #0f172a;
      }
    }
  }

  .main-grid {
    .section-card {
      border: none;
      box-shadow: 0 10px 25px -3px rgba(15,23,42,0.05), 0 4px 6px -4px rgba(15,23,42,0.05);

      &:hover {
        border: none;
        box-shadow: 0 20px 25px -5px rgba(15,23,42,0.1), 0 8px 10px -6px rgba(15,23,42,0.1);
      }

      &.glass-card {
        background: rgba(255,255,255,0.6);
        backdrop-filter: blur(12px);
      }

      :deep(.el-card__header) {
        background: transparent;
        border-bottom: 1px solid #e2e8f0;
        padding: 14px 20px;
      }

      :deep(.el-card__body) {
        padding: 20px;
      }

      .card-header {
        font-size: 15px;

        .header-icon {
          background: none;
          color: #3b82f6;
          font-size: 18px;
          padding: 0;
        }

        .header-link {
          font-size: 13px;
        }
      }
      
      // 为卡片头部的链接添加过渡效果
      .header-link {
        transition: color 0.2s ease;
      }
      
      

      &:hover .header-icon {
        background: none;
        color: #2563eb;
        transform: none;
      }
    }

    .high-risk-card {
      :deep(.el-card__body) {
        padding: 0;
      }

      .high-risk-table {
        .el-table__cell {
          padding: 10px 12px;
        }

        .risk-cell {
          display: flex;
          flex-direction: column;
          gap: 4px;
          align-items: flex-start;
        }

        .risk-pill-tag {
          border-radius: 999px;
          padding: 0 10px;
          height: 22px;
          line-height: 22px;
          border: none;
        }

        .risk-text {
          font-size: 12px;
          color: #475569;
        }

        .op-button {
          font-size: 13px;
        }
        
        // 表格行悬停时的操作图标样式
        :deep(.el-table__row:hover) {
          .table-action-icon {
            .view-icon {
              color: #2563eb !important; // 蓝色
              transform: scale(1.1); // 放大效果
              transition: all 0.3s ease;
            }
          }
        }
        
        :deep(.el-table__row) {
          .table-action-icon {
            .view-icon {
              color: #d1d5db !important; // 浅灰色
              transition: all 0.3s ease;
            }
          }
        }
      }
    }

    .task-list {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .task-item {
        display: grid;
        grid-template-columns: auto 1fr auto auto;
        align-items: center;
        gap: 12px;
        padding: 12px;
        border-radius: 12px;
        background: #f8fafc;
        border: 1px solid #f1f5f9;
        transition: all 0.2s;

        &:hover {
          background: #f1f5f9;
          border-color: #e2e8f0;
        }
      }

      .task-patient-info {
        display: flex;
        align-items: center;
        gap: 8px;

        .patient-name {
          font-weight: 600;
          font-size: 14px;
        }
      }

      .task-summary {
        font-size: 13px;
        color: #475569;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .task-extra-info {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: #64748b;
      }
    }

    .seasonal-card {
      background-size: cover;
      background-position: center;
      color: #fff;
      min-height: 220px;
      display: flex;
      flex-direction: column;

      :deep(.el-card__body) {
        padding: 0;
        flex: 1;
        display: flex;
      }
    }

    .seasonal-content-new {
      padding: 20px;
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      flex: 1;
      text-shadow: 0 2px 4px rgba(0,0,0,0.5);

      .seasonal-term {
        font-size: 28px;
        font-weight: bold;
        font-family: "KaiTi", "STKaiti", serif;
      }

      .seasonal-advice {
        margin: 8px 0 12px;
        font-size: 14px;
        line-height: 1.6;
        flex-grow: 1;
      }

      .seasonal-send-btn {
        color: #fff;
        background: rgba(255,255,255,0.2);
        border-radius: 20px;
        padding: 4px 12px;
        align-self: flex-start;

        &:hover {
          background: rgba(255,255,255,0.3);
        }
      }
    }

    .px-10 {
      padding-left: 10px;
      padding-right: 10px;
    }
  }
}
</style>
