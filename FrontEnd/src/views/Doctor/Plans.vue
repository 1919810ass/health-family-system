<template>
  <div class="doctor-plans">
    <el-page-header content="健康计划与随访" />
    
    <!-- 公共工具栏 (家庭与患者选择) -->
    <div class="common-toolbar">
      <el-select :model-value="familyId" placeholder="选择家庭" style="width: 200px" @change="onSwitch">
        <el-option v-for="f in families" :key="f.id" :label="f.name" :value="String(f.id)" />
      </el-select>
      
      <el-select v-model="selectedMemberId" placeholder="选择患者" style="width: 200px" clearable @change="handleMemberChange">
        <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.phone" :value="m.userId" />
      </el-select>

      <div class="patient-info" v-if="selectedMemberId">
        <el-tag type="info">当前患者: {{ getMemberName(selectedMemberId) }}</el-tag>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="main-tabs" @tab-change="handleTabChange">
      
      <!-- 1. 健康计划 -->
      <el-tab-pane label="健康计划" name="plans">
        <template #label>
          <span class="custom-tab-label">
            <el-icon><Notebook /></el-icon>
            <span>健康计划</span>
          </span>
        </template>

        <!-- 计划过滤工具栏 -->
        <div class="toolbar">
          <el-select v-model="filterStatus" placeholder="按状态筛选" style="width: 150px" clearable @change="loadPlans">
            <el-option label="全部" value="" />
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="逾期" value="OVERDUE" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已暂停" value="PAUSED" />
          </el-select>
          
          <el-select v-model="filterType" placeholder="按类型筛选" style="width: 180px" clearable @change="loadPlans">
            <el-option label="全部" value="" />
            <el-option label="血压随访" value="BLOOD_PRESSURE_FOLLOWUP" />
            <el-option label="饮食管理" value="DIET_MANAGEMENT" />
            <el-option label="运动处方" value="EXERCISE_PRESCRIPTION" />
            <el-option label="用药管理" value="MEDICATION_MANAGEMENT" />
            <el-option label="体重管理" value="WEIGHT_MANAGEMENT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
          
          <el-button type="primary" :loading="loadingPlans" @click="loadPlans">查询</el-button>
          <el-button type="warning" plain @click="handleSmartGenerate">
            <el-icon><MagicStick /></el-icon> AI 智能生成
          </el-button>
          <el-button type="success" @click="handleCreatePlan">+ 新建计划</el-button>

          <!-- 视图切换 -->
          <div class="view-switch">
            <el-radio-group v-model="viewMode" @change="handleViewChange" size="small">
              <el-radio-button label="list"><el-icon><List /></el-icon> 列表</el-radio-button>
              <el-radio-button label="calendar"><el-icon><Calendar /></el-icon> 日历</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 计划列表视图 -->
        <el-card v-if="viewMode === 'list'" class="mt-16 content-card" v-loading="loadingPlans">
          <el-table :data="plans" height="520">
            <el-table-column prop="title" label="计划标题" width="200" />
            <el-table-column label="类型" width="140">
              <template #default="{ row }">
                <div class="plan-type-cell">
                  <el-icon class="type-icon" :class="getPlanTypeIconClass(row.type)">
                    <component :is="getPlanTypeIcon(row.type)" />
                  </el-icon>
                  <span>{{ row.typeLabel }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="日期范围" width="200">
              <template #default="{ row }">
                {{ formatDate(row.startDate) }} ~ {{ row.endDate ? formatDate(row.endDate) : '无结束日期' }}
              </template>
            </el-table-column>
            <el-table-column prop="frequencyTypeLabel" label="频率" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">{{ row.statusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="完成度" width="120">
              <template #default="{ row }">
                <el-progress :percentage="row.completionRate || 0" :status="getProgressStatus(row.completionRate)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditPlan(row)">编辑</el-button>
                <el-button link type="warning" @click="handleViewPlanDetail(row)">详情</el-button>
                <el-button link type="danger" @click="handleDeletePlan(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loadingPlans && plans.length === 0" description="暂无健康计划" />
        </el-card>

        <!-- 计划日历视图 -->
        <el-card v-if="viewMode === 'calendar'" class="mt-16 content-card" v-loading="loadingPlans">
          <el-calendar v-model="calendarDate">
            <template #date-cell="{ data }">
              <div class="calendar-cell">
                <span>{{ data.day.split('-')[2] }}</span>
                <div v-if="calendarPlans[data.day]" class="calendar-plans">
                  <el-tag
                    v-for="plan in calendarPlans[data.day]"
                    :key="plan.id"
                    :type="getStatusTagType(plan.status)"
                    size="small"
                    class="plan-tag"
                  >
                    {{ plan.title }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-calendar>
        </el-card>
      </el-tab-pane>

      <!-- 2. 随访任务 -->
      <el-tab-pane label="随访任务" name="followups">
        <template #label>
          <span class="custom-tab-label">
            <el-icon><Timer /></el-icon>
            <span>随访任务</span>
          </span>
        </template>
        
        <div class="toolbar">
           <el-select v-model="filterFollowupStatus" placeholder="状态筛选" style="width: 150px" clearable @change="loadFollowups">
             <el-option label="全部" value="" />
             <el-option label="待处理" value="PENDING" />
             <el-option label="已完成" value="COMPLETED" />
             <el-option label="已取消" value="CANCELLED" />
           </el-select>
           <el-button type="primary" @click="loadFollowups">刷新</el-button>
           <el-button type="success" @click="handleCreateFollowup">+ 新建任务</el-button>
        </div>

        <el-card class="mt-16 content-card" v-loading="loadingFollowups">
          <el-table :data="followups" height="520">
            <el-table-column prop="title" label="任务标题" width="180" show-overflow-tooltip />
            <el-table-column prop="content" label="任务内容" show-overflow-tooltip />
            <el-table-column label="截止日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.scheduledTime || row.dueDate) }}
              </template>
            </el-table-column>
            <el-table-column label="优先级" width="100">
              <template #default="{ row }">
                 <el-tag :type="row.priority === 'HIGH' ? 'danger' : (row.priority === 'MEDIUM' ? 'warning' : 'info')">
                   {{ row.priority === 'HIGH' ? '高' : (row.priority === 'MEDIUM' ? '中' : '低') }}
                 </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'COMPLETED' ? 'success' : (row.status === 'PENDING' ? 'primary' : 'info')">
                  {{ row.status === 'COMPLETED' ? '已完成' : (row.status === 'PENDING' ? '待处理' : '已取消') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="result" label="随访结果" show-overflow-tooltip />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditFollowup(row)">编辑</el-button>
                <el-button link type="success" v-if="row.status !== 'COMPLETED'" @click="handleCompleteFollowup(row)">完成</el-button>
                <el-button link type="danger" @click="handleDeleteFollowup(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loadingFollowups && followups.length === 0" description="暂无随访任务" />
        </el-card>
      </el-tab-pane>

      <!-- 4. 健康数据 -->
      <el-tab-pane label="健康数据" name="healthData">
        <template #label>
          <span class="custom-tab-label">
            <el-icon><TrendCharts /></el-icon>
            <span>健康数据</span>
          </span>
        </template>
        
        <el-card class="mt-16 content-card" v-loading="healthDataLoading">
          <div ref="healthChartRef" style="width: 100%; height: 400px;"></div>
          <div class="chart-actions" style="text-align: center; margin-top: 20px;">
            <el-button-group>
              <el-button :type="currentMetric === 'systolic' ? 'primary' : ''" @click="initChart('systolic')">血压趋势</el-button>
              <el-button :type="currentMetric === 'weight' ? 'primary' : ''" @click="initChart('weight')">体重趋势</el-button>
              <el-button :type="currentMetric === 'heartRate' ? 'primary' : ''" @click="initChart('heartRate')">心率趋势</el-button>
            </el-button-group>
          </div>
        </el-card>
      </el-tab-pane>

    </el-tabs>

    <!-- 计划编辑/创建弹窗 -->
    <el-dialog
      v-model="planDialogVisible"
      :title="planDialogMode === 'create' ? '新建健康计划' : '编辑健康计划'"
      width="800px"
      @close="handlePlanDialogClose"
    >


      <el-form :model="planFormData" :rules="planFormRules" ref="planFormRef" label-width="120px">
        <el-form-item label="选择患者" prop="patientUserId">
          <el-select v-model="planFormData.patientUserId" placeholder="请选择患者" style="width: 100%" :disabled="planDialogMode === 'edit'">
            <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.phone" :value="m.userId" />
          </el-select>
        </el-form-item>

        <el-form-item label="计划类型" prop="type">
          <el-select v-model="planFormData.type" placeholder="选择计划类型" style="width: 100%">
            <el-option label="血压随访" value="BLOOD_PRESSURE_FOLLOWUP" />
            <el-option label="饮食管理" value="DIET_MANAGEMENT" />
            <el-option label="运动处方" value="EXERCISE_PRESCRIPTION" />
            <el-option label="用药管理" value="MEDICATION_MANAGEMENT" />
            <el-option label="体重管理" value="WEIGHT_MANAGEMENT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="计划标题" prop="title">
          <el-input v-model="planFormData.title" placeholder="请输入计划标题" />
        </el-form-item>
        
        <el-form-item label="计划描述">
          <el-input v-model="planFormData.description" type="textarea" :rows="3" placeholder="请输入计划描述" />
        </el-form-item>
        
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="planFormData.startDate" type="date" placeholder="选择开始日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        
        <el-form-item label="结束日期">
          <el-date-picker v-model="planFormData.endDate" type="date" placeholder="选择结束日期（可选）" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        
        <el-form-item label="执行频率" prop="frequencyType">
          <el-select v-model="planFormData.frequencyType" placeholder="选择频率类型" style="width: 100%">
            <el-option label="每日" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="频率值" v-if="planFormData.frequencyType === 'WEEKLY' || planFormData.frequencyType === 'MONTHLY'">
          <el-input-number v-model="planFormData.frequencyValue" :min="1" placeholder="如每周3次，每2周1次等" />
        </el-form-item>
        
        <el-form-item label="目标指标">
          <el-input v-model="planFormData.targetIndicators" type="textarea" :rows="2" placeholder='JSON格式，如：{"systolic": "<140", "diastolic": "<90"}' />
        </el-form-item>
        
        <el-form-item label="提醒策略">
          <el-input v-model="planFormData.reminderStrategy" type="textarea" :rows="2" placeholder='JSON格式，如：{"time": "09:00", "advanceMinutes": 30, "channels": ["APP"]}' />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingPlan" @click="handleSubmitPlan">确定</el-button>
      </template>
    </el-dialog>

    <!-- 随访任务弹窗 -->
    <el-dialog
      v-model="followupDialogVisible"
      :title="followupDialogMode === 'create' ? '新建随访任务' : '编辑随访任务'"
      width="600px"
      @close="handleFollowupDialogClose"
    >
      <el-form :model="followupFormData" :rules="followupFormRules" ref="followupFormRef" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="followupFormData.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务内容" prop="content">
          <el-input v-model="followupFormData.content" type="textarea" :rows="3" placeholder="请输入随访任务内容" />
        </el-form-item>
        
        <el-form-item label="截止日期" prop="scheduledTime">
          <el-date-picker v-model="followupFormData.scheduledTime" type="datetime" placeholder="选择截止时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="followupFormData.priority" placeholder="选择优先级" style="width: 100%">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status" v-if="followupDialogMode === 'edit'">
          <el-select v-model="followupFormData.status" placeholder="选择状态" style="width: 100%">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="随访结果" v-if="followupDialogMode === 'edit' || followupFormData.status === 'COMPLETED'">
          <el-input v-model="followupFormData.result" type="textarea" :rows="3" placeholder="请输入随访结果记录" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="followupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingFollowup" @click="handleSubmitFollowup">确定</el-button>
      </template>
    </el-dialog>

    <!-- AI生成弹窗 -->
    <el-dialog v-model="aiDialogVisible" title="AI 智能生成健康计划" width="500px">
      <el-form :model="aiFormData" label-width="100px">
        <el-form-item label="关注领域">
          <el-input v-model="aiFormData.focusArea" placeholder="例如：降血压、减重、改善睡眠" />
        </el-form-item>
        <el-form-item label="额外要求">
          <el-input v-model="aiFormData.additionalRequirements" type="textarea" :rows="3" placeholder="例如：患者膝盖有旧伤，不适合剧烈运动；饮食偏好清淡" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generatingAiPlan" @click="submitAiGeneration">开始生成</el-button>
      </template>
    </el-dialog>

    <!-- 批量生成结果弹窗 -->
    <el-dialog v-model="batchResultDialogVisible" title="AI 智能生成结果 (智能分析)" width="800px">
      <el-alert title="AI已分析全家健康数据，根据成员健康状况建议以下计划：" type="info" show-icon :closable="false" class="mb-4" />
      <div v-loading="batchGenerating">
        <div v-for="item in batchResults" :key="item.userId" class="batch-plan-item" style="margin-top: 12px; border: 1px solid #ebeef5; padding: 16px; border-radius: 8px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
            <h4 style="margin: 0; color: #409EFF;">{{ item.userName }} - {{ item.plan.title }}</h4>
            <el-button type="success" size="small" @click="applyBatchPlan(item)">采纳并创建</el-button>
          </div>
          <p style="margin: 4px 0; font-size: 13px; color: #666;">{{ item.plan.description }}</p>
          <div style="margin-top: 8px; font-size: 12px; color: #999;">
            <el-tag size="small" type="warning">{{ item.plan.type }}</el-tag>
            <span style="margin-left: 8px;">频率: {{ item.plan.frequencyType }} ({{ item.plan.frequencyValue }}次)</span>
          </div>
        </div>
        <el-empty v-if="!batchGenerating && batchResults.length === 0" description="未发现需要新建计划的成员" />
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDoctorStore } from '../../stores/doctor'
import { 
  listHealthPlans, createHealthPlan, updateHealthPlan, deleteHealthPlan, getHealthPlansCalendar,
  listFollowUpTasks, createFollowUpTask, updateFollowUpTask, deleteFollowUpTask, generateAiHealthPlan, batchGenerateAiHealthPlans
} from '../../api/doctor'
import { getDoctorView } from '../../api/family'
import { FirstAidKit, KnifeFork, Timer, Trophy, Notebook, WarnTriangleFilled, TrendCharts, Calendar, List, MagicStick } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'

const route = useRoute()
const doctorStore = useDoctorStore()

// Store Data
const families = computed(() => doctorStore.families)
const familyId = computed(() => doctorStore.currentFamilyId)
const members = computed(() => doctorStore.boundMembers)

// Local State
const selectedMemberId = ref(null)
const activeTab = ref('plans')

// --- Health Plans State ---
const filterStatus = ref('')
const filterType = ref('')
const viewMode = ref('list')
const plans = ref([])
const loadingPlans = ref(false)
const calendarDate = ref(new Date())
const calendarPlans = ref({})

const planDialogVisible = ref(false)
const planDialogMode = ref('create')
const planFormRef = ref(null)
const submittingPlan = ref(false)
const currentPlanId = ref(null)
const planFormData = ref({
  type: '',
  title: '',
  description: '',
  startDate: '',
  endDate: '',
  frequencyType: '',
  frequencyValue: null,
  frequencyDetail: '',
  targetIndicators: '{}',
  reminderStrategy: '{}'
})

const planFormRules = {
  patientUserId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  type: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入计划标题', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  frequencyType: [{ required: true, message: '请选择执行频率', trigger: 'change' }]
}

// --- AI Generation State ---
const aiDialogVisible = ref(false)
const generatingAiPlan = ref(false)
const aiFormData = ref({ focusArea: '', additionalRequirements: '' })

const handleAiGenerate = () => {
  if (!planFormData.value.patientUserId) {
    ElMessage.warning('请先选择患者')
    return
  }
  aiFormData.value = { focusArea: '', additionalRequirements: '' }
  aiDialogVisible.value = true
}

const submitAiGeneration = async () => {
  generatingAiPlan.value = true
  try {
    const res = await generateAiHealthPlan(familyId.value, {
      patientUserId: planFormData.value.patientUserId,
      focusArea: aiFormData.value.focusArea,
      additionalRequirements: aiFormData.value.additionalRequirements
    })
    
    const generated = res.data
    // Fill form
    planFormData.value.title = generated.title
    planFormData.value.description = generated.description
    planFormData.value.type = generated.type
    planFormData.value.frequencyType = generated.frequencyType
    planFormData.value.frequencyValue = generated.frequencyValue
    planFormData.value.targetIndicators = generated.targetIndicators
    planFormData.value.reminderStrategy = generated.reminderStrategy
    
    // Suggest dates if not set
    if (!planFormData.value.startDate) {
        planFormData.value.startDate = dayjs().format('YYYY-MM-DD')
    }
    if (!planFormData.value.endDate) {
        planFormData.value.endDate = dayjs().add(1, 'month').format('YYYY-MM-DD')
    }

    ElMessage.success('AI生成成功，请检查并确认')
    aiDialogVisible.value = false
  } catch (e) {
    console.error(e)
    ElMessage.error('生成失败: ' + (e.message || '未知错误'))
  } finally {
    generatingAiPlan.value = false
  }
}

// --- Batch AI Generation Logic ---
const batchResultDialogVisible = ref(false)
const batchGenerating = ref(false)
const batchResults = ref([])

const handleSmartGenerate = async () => {
  if (selectedMemberId.value) {
    // 单人模式：直接打开创建弹窗并唤起AI
    handleCreatePlan()
    nextTick(() => {
      handleAiGenerate()
    })
    return
  }
  
  // 批量模式
  if (!familyId.value) {
    ElMessage.warning('请先选择家庭')
    return
  }
  
  batchResultDialogVisible.value = true
  batchGenerating.value = true
  batchResults.value = []
  
  try {
    const res = await batchGenerateAiHealthPlans(familyId.value)
    batchResults.value = res.data || []
    if (batchResults.value.length === 0) {
        // ElMessage.info('未发现需要紧急干预的高风险成员')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('批量生成失败: ' + (e.message || '未知错误'))
  } finally {
    batchGenerating.value = false
  }
}

const applyBatchPlan = async (item) => {
    try {
        const planData = {
            patientUserId: item.userId, // 显式添加 patientUserId
            title: item.plan.title,
            description: item.plan.description,
            type: item.plan.type,
            frequencyType: item.plan.frequencyType,
            frequencyValue: item.plan.frequencyValue,
            targetIndicators: item.plan.targetIndicators,
            reminderStrategy: item.plan.reminderStrategy,
            startDate: dayjs().format('YYYY-MM-DD'),
            endDate: dayjs().add(1, 'month').format('YYYY-MM-DD')
        }
        
        await createHealthPlan(familyId.value, item.userId, planData)
        ElMessage.success(`已为 ${item.userName} 创建计划`)
        
        // Remove from list
        batchResults.value = batchResults.value.filter(i => i.userId !== item.userId)
        
        // 智能重置筛选条件以确保新计划可见
        let filtersChanged = false
        
        // 1. 如果当前选了特定患者且不是该计划患者，切换到全部患者
        if (selectedMemberId.value && selectedMemberId.value !== item.userId) {
            selectedMemberId.value = null
            filtersChanged = true
        }
        
        // 2. 如果当前有状态筛选且不是"进行中"，重置状态筛选（新计划默认是进行中）
        if (filterStatus.value && filterStatus.value !== 'ACTIVE') {
            filterStatus.value = ''
            filtersChanged = true
        }
        
        if (filtersChanged) {
            ElMessage.info('已自动调整筛选条件以显示新生成的计划')
        }

        // Refresh main list
        loadPlans()
        
        if (batchResults.value.length === 0) {
            batchResultDialogVisible.value = false
        }
    } catch (e) {
        console.error(e)
        ElMessage.error('创建计划失败')
    }
}

// --- Follow-up Tasks State ---
const filterFollowupStatus = ref('') // Default all
const followups = ref([])
const loadingFollowups = ref(false)
const followupDialogVisible = ref(false)
const followupDialogMode = ref('create')
const followupFormRef = ref(null)
const submittingFollowup = ref(false)
const currentFollowupId = ref(null)
const followupFormData = ref({
  title: '',
  content: '',
  scheduledTime: '',
  priority: 'MEDIUM',
  status: 'PENDING',
  result: ''
})

const followupFormRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入任务内容', trigger: 'blur' }],
  scheduledTime: [{ required: true, message: '请选择截止时间', trigger: 'change' }]
}

// --- Health Data State ---
const healthChartRef = ref(null)
let healthChart = null
const healthDataLoading = ref(false)
const currentMetric = ref('systolic')
const currentMetricLabel = ref('血压趋势')

// --- Common Logic ---
const onSwitch = async (id) => {
  await doctorStore.setCurrentFamily(id)
}

const handleMemberChange = () => {
  if (activeTab.value === 'plans') loadPlans()
  if (activeTab.value === 'followups') loadFollowups()
  if (activeTab.value === 'healthData') {
    // Reset chart first to show loading state or clear previous data
    healthDataLoading.value = true
    nextTick(() => initChart(currentMetric.value))
  }
}

const getMemberName = (id) => {
  const m = members.value.find(m => m.userId === id)
  return m ? (m.nickname || m.phone) : id
}

const handleTabChange = (tab) => {
  const tabName = typeof tab === 'string' ? tab : (tab?.props?.name || tab?.paneName)
  if (tabName === 'plans') loadPlans()
  if (tabName === 'followups') loadFollowups()
  if (tabName === 'healthData') nextTick(() => initChart())
}

// --- Health Plans Logic ---
const handleViewChange = () => {
  if (viewMode.value === 'calendar') loadCalendarPlans()
  else loadPlans()
}

const loadPlans = async () => {
  if (!familyId.value) return // 只需要 familyId 即可
  loadingPlans.value = true
  try {
    const res = await listHealthPlans(familyId.value, selectedMemberId.value, {
      status: filterStatus.value || undefined,
      type: filterType.value || undefined
    })
    plans.value = res?.data || []
  } catch (error) {
    console.error('加载计划失败:', error)
    ElMessage.error('加载计划列表失败')
  } finally {
    loadingPlans.value = false
  }
}

const loadCalendarPlans = async () => {
  if (!selectedMemberId.value) return
  loadingPlans.value = true
  try {
    const monthStart = dayjs(calendarDate.value).startOf('month').format('YYYY-MM-DD')
    const monthEnd = dayjs(calendarDate.value).endOf('month').format('YYYY-MM-DD')
    const res = await getHealthPlansCalendar(selectedMemberId.value, monthStart, monthEnd)
    // Process calendar data similar to before
    const plansList = res?.data || []
    const plansByDate = {}
    plansList.forEach(plan => {
      const start = dayjs(plan.startDate)
      const end = plan.endDate ? dayjs(plan.endDate) : dayjs().add(1, 'year')
      let current = start
      while (current.isBefore(end) || current.isSame(end, 'day')) {
        const dateKey = current.format('YYYY-MM-DD')
        if (!plansByDate[dateKey]) plansByDate[dateKey] = []
        plansByDate[dateKey].push(plan)
        current = current.add(1, 'day')
      }
    })
    calendarPlans.value = plansByDate
  } catch (error) {
    ElMessage.error('加载日历计划失败')
  } finally {
    loadingPlans.value = false
  }
}

const handleCreatePlan = () => {
  // if (!selectedMemberId.value) return ElMessage.warning('请先选择患者') // Allow create without pre-selection
  planDialogMode.value = 'create'
  resetPlanForm()
  if (selectedMemberId.value) {
      planFormData.value.patientUserId = selectedMemberId.value
  }
  planDialogVisible.value = true
}

const handleEditPlan = (row) => {
  planDialogMode.value = 'edit'
  resetPlanForm()
  planFormData.value = { ...row, patientUserId: row.patientUserId || selectedMemberId.value } // Simplify mapping
  currentPlanId.value = row.id
  planDialogVisible.value = true
}

const handleDeletePlan = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该计划吗？', '提示', { type: 'warning' })
    await deleteHealthPlan(row.id)
    ElMessage.success('删除成功')
    loadPlans()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleSubmitPlan = async () => {
  if (!planFormRef.value) return
  await planFormRef.value.validate()
  submittingPlan.value = true
  try {
    const data = { ...planFormData.value }
    // 确保有 patientUserId
    if (!data.patientUserId) {
        ElMessage.warning('请选择患者')
        submittingPlan.value = false
        return
    }

    if (planDialogMode.value === 'create') {
      await createHealthPlan(familyId.value, data.patientUserId, data)
      ElMessage.success('创建成功')
    } else {
      await updateHealthPlan(currentPlanId.value, data)
      ElMessage.success('更新成功')
    }
    planDialogVisible.value = false
    // 如果当前选中的就是该患者，刷新列表；否则可能需要切换或者提示
    if (selectedMemberId.value === data.patientUserId) {
        loadPlans()
    } else {
        // 可选：切换到该患者
        selectedMemberId.value = data.patientUserId
        loadPlans()
    }
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submittingPlan.value = false
  }
}

const resetPlanForm = () => {
  planFormData.value = {
    patientUserId: null,
    type: '', title: '', description: '', startDate: '', endDate: '',
    frequencyType: '', frequencyValue: null, frequencyDetail: null,
    targetIndicators: '{}', reminderStrategy: '{}'
  }
  if (planFormRef.value) planFormRef.value.resetFields()
}

// --- Follow-up Logic ---
const loadFollowups = async () => {
  if (!familyId.value) return // 只需要 familyId 即可
  loadingFollowups.value = true
  try {
    const res = await listFollowUpTasks(familyId.value, selectedMemberId.value, {
        status: filterFollowupStatus.value || undefined
    })
    followups.value = res?.data || []
  } catch (error) {
    console.error('加载随访任务失败:', error)
    // ElMessage.error('加载随访任务失败') // Suppress for now if API not ready
    // Mock data for demo
    // followups.value = [
    //   { id: 1, title: '高血压随访', content: '电话随访：了解血压控制情况', scheduledTime: '2024-06-20', priority: 'HIGH', status: 'PENDING' },
    //   { id: 2, title: '复诊提醒', content: '提醒复诊', scheduledTime: '2024-06-25', priority: 'MEDIUM', status: 'PENDING' }
    // ]
    followups.value = []
  } finally {
    loadingFollowups.value = false
  }
}

const handleCreateFollowup = () => {
  if (!selectedMemberId.value) return ElMessage.warning('请先选择患者')
  followupDialogMode.value = 'create'
  resetFollowupForm()
  followupDialogVisible.value = true
}

const handleEditFollowup = (row) => {
  followupDialogMode.value = 'edit'
  resetFollowupForm()
  // Ensure fields match
  const time = row.scheduledTime || row.dueDate
  followupFormData.value = { 
      ...row,
      scheduledTime: time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : ''
  }
  currentFollowupId.value = row.id
  followupDialogVisible.value = true
}

const handleCompleteFollowup = (row) => {
  followupDialogMode.value = 'edit'
  resetFollowupForm()
  const time = row.scheduledTime || row.dueDate
  followupFormData.value = { 
      ...row, 
      status: 'COMPLETED',
      scheduledTime: time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '' 
  }
  currentFollowupId.value = row.id
  followupDialogVisible.value = true
}

const handleDeleteFollowup = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该随访任务吗？', '提示', { type: 'warning' })
    await deleteFollowUpTask(row.id)
    ElMessage.success('删除成功')
    loadFollowups()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleSubmitFollowup = async () => {
  if (!followupFormRef.value) return
  await followupFormRef.value.validate()
  submittingFollowup.value = true
  try {
    const data = { ...followupFormData.value }
    if (followupDialogMode.value === 'create') {
      await createFollowUpTask(familyId.value, selectedMemberId.value, data)
      ElMessage.success('创建成功')
    } else {
      await updateFollowUpTask(currentFollowupId.value, data)
      ElMessage.success('更新成功')
    }
    followupDialogVisible.value = false
    loadFollowups()
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submittingFollowup.value = false
  }
}

const resetFollowupForm = () => {
  followupFormData.value = { title: '', content: '', scheduledTime: '', priority: 'MEDIUM', status: 'PENDING', result: '' }
  if (followupFormRef.value) followupFormRef.value.resetFields()
}

// --- Health Data Logic ---
const initChart = async (type = 'systolic') => {
  if (!healthChartRef.value) return
  
  // Update current metric state
  currentMetric.value = type
  if (type === 'systolic') currentMetricLabel.value = '血压趋势'
  else if (type === 'weight') currentMetricLabel.value = '体重趋势'
  else if (type === 'heartRate') currentMetricLabel.value = '心率趋势'
  
  if (healthChart) {
    healthChart.dispose()
  }
  
  healthChart = echarts.init(healthChartRef.value)
  healthDataLoading.value = true
  
  try {
    // 默认空数据
    let dates = []
    let values = []
    let values2 = [] // For diastolic pressure
    
    // 如果已选择患者，从后端获取真实数据
    if (familyId.value && selectedMemberId.value) {
      const res = await getDoctorView(familyId.value)
      if (res.data?.telemetry) {
        // Find patient data
        const memberName = getMemberName(selectedMemberId.value)
        // Telemetry is keyed by nickname, we need to be careful matching
        // Try to find matching data entry
        let patientData = null
        
        // Strategy: Iterate all entries and check memberId or userId in items
        for (const [key, data] of Object.entries(res.data.telemetry)) {
          if (data.items && data.items.length > 0) {
            const firstItem = data.items[0]
            if (firstItem.userId === selectedMemberId.value || firstItem.memberId === selectedMemberId.value) {
              patientData = data
              break
            }
          }
        }
        
        // If not found by ID, try name match as fallback
        if (!patientData && res.data.telemetry[memberName]) {
          patientData = res.data.telemetry[memberName]
        }
        
        if (patientData && patientData.items) {
          // Filter items by type and sort by date
          const items = [...patientData.items].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
          
          items.forEach(item => {
            const dateStr = dayjs(item.createdAt).format('MM-DD HH:mm')
            
            if (type === 'systolic') {
              if (item.type === '血压' || item.systolic) {
                if (item.systolic && item.diastolic) {
                  dates.push(dateStr)
                  values.push(item.systolic)
                  values2.push(item.diastolic)
                }
              }
            } else if (type === 'weight') {
              if (item.type === '体重' || item.type === '体重变化' || item.weight) {
                const val = item.value || item.weight
                if (val) {
                  dates.push(dateStr)
                  values.push(parseFloat(val))
                }
              }
            } else if (type === 'heartRate') {
              if (item.type === '心率' || item.heartRate || item.heart_rate) {
                const val = item.value || item.heartRate || item.heart_rate
                if (val) {
                  dates.push(dateStr)
                  values.push(parseFloat(val))
                }
              }
            }
          })
        }
      }
    }
    
    // 如果没有真实数据，使用最近7天的空数据占位，或者显示暂无数据
    if (dates.length === 0) {
       // Optional: Use mock data if no real data found for demonstration?
       // For now, let's show empty chart or keep previous mock behavior if preferred.
       // User requested "Show his indicator data trends", implies real data.
       // Let's generate a placeholder date axis so chart isn't broken
       for (let i = 0; i < 7; i++) {
         dates.push(dayjs().subtract(6-i, 'day').format('MM-DD'))
         values.push(null)
         if (type === 'systolic') values2.push(null)
       }
    }
    
    const option = {
      title: { text: currentMetricLabel.value },
      tooltip: { trigger: 'axis' },
      legend: { data: type === 'systolic' ? ['收缩压', '舒张压'] : [currentMetricLabel.value] },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: []
    }
    
    if (type === 'systolic') {
      option.series = [
        {
          name: '收缩压',
          data: values,
          type: 'line',
          smooth: true,
          itemStyle: { color: '#F56C6C' }
        },
        {
          name: '舒张压',
          data: values2,
          type: 'line',
          smooth: true,
          itemStyle: { color: '#409EFF' }
        }
      ]
    } else {
      option.series = [
        {
          name: currentMetricLabel.value,
          data: values,
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.2 },
          itemStyle: { color: '#409EFF' }
        }
      ]
    }
    
    healthChart.setOption(option)
  } catch (error) {
    console.error('Failed to load health data', error)
    ElMessage.error('加载健康数据失败')
  } finally {
    healthDataLoading.value = false
  }
}

// Helpers
const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD') : ''
const formatDateTime = (date) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : ''
const getPlanTypeIcon = (type) => ({ BLOOD_PRESSURE_FOLLOWUP: FirstAidKit, DIET_MANAGEMENT: KnifeFork, EXERCISE_PRESCRIPTION: Trophy }[type] || Notebook)
const getPlanTypeIconClass = (type) => '' // simplify
const getStatusTagType = (status) => ({ ACTIVE: 'success', COMPLETED: 'info', OVERDUE: 'danger', PAUSED: 'warning' }[status] || '')
const getProgressStatus = (rate) => rate >= 80 ? 'success' : (rate >= 50 ? 'warning' : 'exception')
const handlePlanDialogClose = () => { resetPlanForm(); currentPlanId.value = null }
const handleFollowupDialogClose = () => { resetFollowupForm(); currentFollowupId.value = null }
const handleViewPlanDetail = (row) => {
  ElMessageBox.alert(
    `<div style="text-align: left;">
      <p><strong>计划标题：</strong>${row.title}</p>
      <p><strong>患者：</strong>${row.patientName || '-'}</p>
      <p><strong>类型：</strong>${row.typeLabel || row.type}</p>
      <p><strong>描述：</strong>${row.description || '无'}</p>
      <p><strong>日期范围：</strong>${formatDate(row.startDate)} ~ ${row.endDate ? formatDate(row.endDate) : '无结束日期'}</p>
      <p><strong>频率：</strong>${row.frequencyTypeLabel || row.frequencyType}</p>
      <p><strong>状态：</strong>${row.statusLabel || row.status}</p>
      <p><strong>完成度：</strong>${row.completionRate || 0}%</p>
    </div>`,
    '计划详情',
    { dangerouslyUseHTMLString: true }
  )
}

onMounted(async () => {
  const tabFromRoute = route.query?.tab
  if (tabFromRoute && ['plans', 'followups', 'healthData'].includes(String(tabFromRoute))) {
    activeTab.value = String(tabFromRoute)
  }

  if (!families.value || families.value.length === 0) {
    await doctorStore.fetchFamilies()
  }

  if (familyId.value) {
    await doctorStore.fetchMembers(familyId.value)
    handleTabChange(activeTab.value)
  }
})

// 监听家庭ID变化，重新加载数据
watch(() => familyId.value, async (newVal) => {
  if (newVal) {
    await doctorStore.fetchMembers(newVal)
    if (activeTab.value === 'plans') loadPlans()
    if (activeTab.value === 'followups') loadFollowups()
    if (activeTab.value === 'healthData') initChart()
  } else {
    // 清空数据
    plans.value = []
    followups.value = []
  }
})

watch(() => calendarDate.value, () => { if (viewMode.value === 'calendar') loadCalendarPlans() })

</script>

<style scoped lang="scss">
@use '@/styles/mixins' as mixins;
@use '@/styles/variables' as vars;

.doctor-plans {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: var(--el-bg-color-page);
}

.common-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  align-items: center;
}

.main-tabs {
  background: white;
  padding: 16px;
  border-radius: 12px;
  min-height: 600px;
}

.custom-tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.view-switch {
  margin-left: auto;
}

.content-card {
  border: none;
  box-shadow: none;
}
</style>
