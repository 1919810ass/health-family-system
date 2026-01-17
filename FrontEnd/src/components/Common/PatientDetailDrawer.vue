<template>
  <el-drawer
    v-model="visible"
    title="患者详情"
    :size="drawerSize"
    :before-close="handleClose"
  >
    <el-tabs v-model="activeTab" class="patient-detail-tabs">
      <!-- 健康概览Tab -->
      <el-tab-pane label="健康概览" name="overview">
        <div v-loading="loading" class="patient-detail">
          <!-- 基础信息 -->
          <el-card class="section-card">
            <template #header>
              <div class="card-header">
                <span>基础信息</span>
                <el-button
                  :icon="isImportant ? StarFilled : Star"
                  :type="isImportant ? 'warning' : 'default'"
                  circle
                  size="small"
                  @click="toggleImportant"
                />
              </div>
            </template>
            <el-descriptions :column="2" border v-if="detail">
              <el-descriptions-item label="姓名">{{ detail.nickname }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ detail.age || '—' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ formatSex(detail.sex) }}</el-descriptions-item>
              <el-descriptions-item label="家庭">{{ detail.familyName }}</el-descriptions-item>
              <el-descriptions-item label="关系">{{ formatRelation(detail.relation) }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ formatRole(detail.role) }}</el-descriptions-item>
              <el-descriptions-item label="联系方式" :span="2">{{ detail.phone || '—' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 健康概览 -->
          <el-card class="section-card">
            <template #header>健康概览</template>
            
            <!-- 最近体质测评 -->
            <div v-if="detail?.latestAssessment" class="assessment-section">
              <h4>最近体质测评</h4>
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="体质类型">{{ getConstitutionName(detail.latestAssessment.primaryType) }}</el-descriptions-item>
                <el-descriptions-item label="置信度">{{ (detail.latestAssessment.confidence * 100).toFixed(1) }}%</el-descriptions-item>
                <el-descriptions-item label="测评时间" :span="2">
                  {{ formatTime(detail.latestAssessment.createdAt) }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="暂无体质测评记录" :image-size="80" />

            <!-- 日志统计 -->
            <div v-if="detail?.logStatistics" class="log-statistics-section">
              <h4>日志统计</h4>
              <el-row :gutter="16">
                <el-col :span="12">
                  <div class="stat-item">
                    <span class="stat-label">近7天：</span>
                    <span>饮食 {{ detail.logStatistics.diet_7d || 0 }} 条</span>
                    <span>运动 {{ detail.logStatistics.sport_7d || 0 }} 条</span>
                    <span>睡眠 {{ detail.logStatistics.sleep_7d || 0 }} 条</span>
                    <span>情绪 {{ detail.logStatistics.mood_7d || 0 }} 条</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="stat-item">
                    <span class="stat-label">近30天：</span>
                    <span>饮食 {{ detail.logStatistics.diet_30d || 0 }} 条</span>
                    <span>运动 {{ detail.logStatistics.sport_30d || 0 }} 条</span>
                    <span>睡眠 {{ detail.logStatistics.sleep_30d || 0 }} 条</span>
                    <span>情绪 {{ detail.logStatistics.mood_30d || 0 }} 条</span>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- 近期建议摘要 -->
            <div v-if="detail?.recentRecommendations && detail.recentRecommendations.length > 0" class="recommendations-section">
              <h4>近期建议摘要</h4>
              <el-timeline>
                <el-timeline-item
                  v-for="rec in detail.recentRecommendations.slice(0, 5)"
                  :key="rec.recommendationId"
                  :timestamp="formatTime(rec.createdAt)"
                  placement="top"
                >
                  <el-tag :type="getPriorityType(rec.priority)" size="small">{{ rec.category }}</el-tag>
                  <span class="recommendation-title">{{ rec.title }}</span>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-card>

          <!-- 关键风险 -->
          <el-card class="section-card">
            <template #header>关键风险</template>
            <div v-if="detail?.healthTags && detail.healthTags.length > 0" class="tags-section">
              <el-tag
                v-for="tag in detail.healthTags"
                :key="tag"
                :type="tag.includes('过敏') ? 'danger' : 'warning'"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="risk-level">
              <span class="risk-label">风险等级：</span>
              <el-tag :type="getRiskType(detail?.riskLevel)">
                {{ getRiskText(detail?.riskLevel) }}
              </el-tag>
            </div>
          </el-card>

          <!-- 快速操作 -->
          <el-card class="section-card">
            <template #header>快速操作</template>
            <div class="action-buttons">
              <el-button type="primary" :disabled="shareDisabled" @click="viewLogs">查看日志</el-button>
              <el-button type="success" :disabled="shareDisabled" @click="viewRecommendations">查看建议</el-button>
              <el-button type="warning" @click="createFollowupPlan">创建随访计划</el-button>
            </div>
          </el-card>

          <!-- 诊断工具 -->
          <el-card class="section-card">
            <template #header>诊断工具</template>
            <div class="diagnostic-tools">
              <el-button 
                type="primary" 
                :loading="generatingRecommendation"
                @click="handleGenerateRecommendation"
              >
                <el-icon><Refresh /></el-icon>
                重新生成健康建议
              </el-button>
              <p class="tooltip-text">基于患者最新体质测评和近期健康日志数据，重新生成个性化健康建议</p>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 病历记录Tab -->
      <el-tab-pane label="病历记录" name="notes">
        <div v-loading="notesLoading" class="notes-container">
          <div class="notes-header">
            <el-button type="primary" @click="handleCreateNote">
              <el-icon><Plus /></el-icon>
              新建病历
            </el-button>
          </div>

          <!-- 病历列表 -->
          <div v-if="notes.length > 0" class="notes-list">
            <el-card
              v-for="note in notes"
              :key="note.id"
              class="note-item"
              shadow="hover"
            >
              <template #header>
                <div class="note-header">
                  <span class="note-date">{{ formatDate(note.consultationDate) }}</span>
                  <div class="note-actions">
                    <el-button link type="primary" @click="handleEditNote(note)">编辑</el-button>
                    <el-button link type="danger" @click="handleDeleteNote(note)">删除</el-button>
                  </div>
                </div>
              </template>
              
              <div class="note-content">
                <div v-if="note.chiefComplaint" class="note-field">
                  <strong>主诉：</strong>
                  <p>{{ note.chiefComplaint }}</p>
                </div>
                <div v-if="note.pastHistory" class="note-field">
                  <strong>既往史：</strong>
                  <p>{{ note.pastHistory }}</p>
                </div>
                <div v-if="note.medication" class="note-field">
                  <strong>用药情况：</strong>
                  <p>{{ note.medication }}</p>
                </div>
                <div v-if="note.lifestyleAssessment" class="note-field">
                  <strong>生活方式评估：</strong>
                  <p>{{ note.lifestyleAssessment }}</p>
                </div>
                <div v-if="note.diagnosisOpinion" class="note-field">
                  <strong>诊疗意见：</strong>
                  <p>{{ note.diagnosisOpinion }}</p>
                </div>
                <div v-if="note.followupSuggestion" class="note-field">
                  <strong>随访建议：</strong>
                  <p>{{ note.followupSuggestion }}</p>
                </div>
                <div v-if="note.content" class="note-field">
                  <strong>备注：</strong>
                  <div class="note-content-text" v-html="formatMarkdown(note.content)"></div>
                </div>
              </div>
              
              <div class="note-footer">
                <span class="note-meta">创建时间：{{ formatTime(note.createdAt) }}</span>
                <span class="note-meta" v-if="note.updatedAt !== note.createdAt">
                  更新时间：{{ formatTime(note.updatedAt) }}
                </span>
              </div>
            </el-card>
          </div>

          <el-empty v-else description="暂无病历记录" :image-size="120" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 病历编辑对话框 -->
    <el-dialog
      v-model="noteDialogVisible"
      :title="editingNote ? '编辑病历' : '新建病历'"
      width="700px"
      :before-close="handleNoteDialogClose"
    >
      <el-form
        ref="noteFormRef"
        :model="noteForm"
        :rules="noteFormRules"
        label-width="120px"
      >
        <el-form-item label="问诊日期" prop="consultationDate">
          <el-date-picker
            v-model="noteForm.consultationDate"
            type="date"
            placeholder="选择问诊日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="主诉">
          <el-input
            v-model="noteForm.chiefComplaint"
            type="textarea"
            :rows="2"
            placeholder="患者主要症状和诉求"
          />
        </el-form-item>
        <el-form-item label="既往史">
          <el-input
            v-model="noteForm.pastHistory"
            type="textarea"
            :rows="3"
            placeholder="既往疾病史、手术史等"
          />
        </el-form-item>
        <el-form-item label="用药情况">
          <el-input
            v-model="noteForm.medication"
            type="textarea"
            :rows="2"
            placeholder="当前用药情况"
          />
        </el-form-item>
        <el-form-item label="生活方式评估">
          <el-input
            v-model="noteForm.lifestyleAssessment"
            type="textarea"
            :rows="3"
            placeholder="饮食、运动、作息等生活方式评估"
          />
        </el-form-item>
        <el-form-item label="诊疗意见">
          <el-input
            v-model="noteForm.diagnosisOpinion"
            type="textarea"
            :rows="3"
            placeholder="诊断和治疗意见"
          />
        </el-form-item>
        <el-form-item label="随访建议">
          <el-input
            v-model="noteForm.followupSuggestion"
            type="textarea"
            :rows="2"
            placeholder="后续随访和复查建议"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="noteForm.content"
            type="textarea"
            :rows="4"
            placeholder="其他备注信息（支持Markdown格式）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleNoteDialogClose">取消</el-button>
        <el-button type="primary" :loading="noteSubmitting" @click="handleSubmitNote">确定</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, Refresh, Plus } from '@element-plus/icons-vue'
import { 
  getPatientDetail, 
  togglePatientImportant,
  listDoctorNotes,
  createDoctorNote,
  updateDoctorNote,
  deleteDoctorNote,
  generateRecommendationForPatient
} from '../../api/doctor'
import { getConstitutionName } from '../../utils/tcm-constants'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  familyId: {
    type: String,
    required: true,
  },
  patientUserId: {
    type: [String, Number],
    required: true,
  },
  shareToFamily: {
    type: [Boolean, Number],
    required: false,
  },
})

const emit = defineEmits(['update:modelValue', 'viewLogs', 'viewRecommendations', 'createFollowupPlan'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

const drawerSize = computed(() => {
  return window.innerWidth > 768 ? '800px' : '90%'
})

const loading = ref(false)
const detail = ref(null)
const isImportant = ref(false)

// Tab管理
const activeTab = ref('overview')

// 病历记录相关
const notesLoading = ref(false)
const notes = ref([])
const noteDialogVisible = ref(false)
const editingNote = ref(null)
const noteSubmitting = ref(false)
const noteFormRef = ref(null)
const noteForm = ref({
  consultationDate: dayjs().format('YYYY-MM-DD'),
  chiefComplaint: '',
  pastHistory: '',
  medication: '',
  lifestyleAssessment: '',
  diagnosisOpinion: '',
  followupSuggestion: '',
  content: '',
})

const noteFormRules = {
  consultationDate: [
    { required: true, message: '请选择问诊日期', trigger: 'blur' }
  ],
}

// 诊断工具相关
const generatingRecommendation = ref(false)
const shareDisabled = computed(() => props.shareToFamily === false)

// 监听抽屉打开，加载数据
watch(visible, async (newVal) => {
  if (newVal && props.familyId && props.patientUserId) {
    await loadDetail()
    loadImportantStatus()
    if (activeTab.value === 'notes') {
      await loadNotes()
    }
  }
})

// 监听Tab切换
watch(activeTab, async (newTab) => {
  if (newTab === 'notes' && visible.value && props.familyId && props.patientUserId) {
    await loadNotes()
  }
})

const loadDetail = async () => {
  if (!props.familyId || !props.patientUserId) return
  
  loading.value = true
  try {
    const res = await getPatientDetail(props.familyId, props.patientUserId)
    detail.value = res?.data || null
  } catch (error) {
    console.error('加载患者详情失败:', error)
    ElMessage.error('加载患者详情失败')
  } finally {
    loading.value = false
  }
}

const loadImportantStatus = () => {
  const key = `doctor_patient_important_${props.patientUserId}`
  isImportant.value = localStorage.getItem(key) === 'true'
}

const loadNotes = async () => {
  if (!props.familyId || !props.patientUserId) return
  
  notesLoading.value = true
  try {
    const res = await listDoctorNotes(props.familyId, props.patientUserId)
    notes.value = res?.data || []
  } catch (error) {
    console.error('加载病历记录失败:', error)
    ElMessage.error('加载病历记录失败')
  } finally {
    notesLoading.value = false
  }
}

const toggleImportant = async () => {
  const newStatus = !isImportant.value
  try {
    await togglePatientImportant(props.familyId, props.patientUserId, newStatus)
    isImportant.value = newStatus
    const key = `doctor_patient_important_${props.patientUserId}`
    localStorage.setItem(key, String(newStatus))
    ElMessage.success(newStatus ? '已标记为重点管理' : '已取消重点管理')
  } catch (error) {
    console.error('切换重点管理标记失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleClose = () => {
  visible.value = false
}

const formatTime = (time) => {
  if (!time) return '—'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const formatDate = (date) => {
  if (!date) return '—'
  return dayjs(date).format('YYYY-MM-DD')
}

const formatMarkdown = (text) => {
  if (!text) return ''
  // 简单的Markdown转HTML（可以用marked库替换）
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
}

// 格式化辅助函数
const formatSex = (val) => {
  if (!val) return '—'
  const map = {
    'M': '男',
    'F': '女',
    'MALE': '男',
    'FEMALE': '女',
    1: '男',
    2: '女'
  }
  return map[String(val).toUpperCase()] || val
}

const formatRole = (val) => {
  if (!val) return '—'
  const map = {
    'MEMBER': '成员',
    'ADMIN': '管理员',
    'DOCTOR': '医生',
    'OWNER': '户主'
  }
  return map[String(val).toUpperCase()] || val
}

const formatRelation = (val) => {
  if (!val) return '—'
  const map = {
    'SELF': '本人',
    'SPOUSE': '配偶',
    'CHILD': '子女',
    'PARENT': '父母',
    'OTHER': '其他',
    'MEMBER': '成员'
  }
  return map[String(val).toUpperCase()] || val
}

const getPriorityType = (priority) => {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return map[priority] || 'info'
}

const getRiskType = (riskLevel) => {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'success' }
  return map[riskLevel] || 'info'
}

const getRiskText = (riskLevel) => {
  const map = { HIGH: '高风险', MEDIUM: '中等风险', LOW: '低风险' }
  return map[riskLevel] || '未知'
}

const viewLogs = () => {
  if (shareDisabled.value) {
    ElMessage.warning('患者未开启数据共享，无法查看健康日志')
    return
  }
  emit('viewLogs', props.patientUserId)
}

const viewRecommendations = () => {
  if (shareDisabled.value) {
    ElMessage.warning('患者未开启数据共享，无法查看健康建议')
    return
  }
  emit('viewRecommendations', props.patientUserId)
}

const createFollowupPlan = () => {
  emit('createFollowupPlan', props.patientUserId)
}

// 病历记录相关方法
const handleCreateNote = () => {
  editingNote.value = null
  noteForm.value = {
    consultationDate: dayjs().format('YYYY-MM-DD'),
    chiefComplaint: '',
    pastHistory: '',
    medication: '',
    lifestyleAssessment: '',
    diagnosisOpinion: '',
    followupSuggestion: '',
    content: '',
  }
  noteDialogVisible.value = true
}

const handleEditNote = (note) => {
  editingNote.value = note
  noteForm.value = {
    consultationDate: note.consultationDate,
    chiefComplaint: note.chiefComplaint || '',
    pastHistory: note.pastHistory || '',
    medication: note.medication || '',
    lifestyleAssessment: note.lifestyleAssessment || '',
    diagnosisOpinion: note.diagnosisOpinion || '',
    followupSuggestion: note.followupSuggestion || '',
    content: note.content || '',
  }
  noteDialogVisible.value = true
}

const handleDeleteNote = async (note) => {
  try {
    await ElMessageBox.confirm('确定要删除这条病历记录吗？', '确认删除', {
      type: 'warning',
    })
    
    await deleteDoctorNote(note.id)
    ElMessage.success('删除成功')
    await loadNotes()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除病历记录失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleNoteDialogClose = () => {
  noteDialogVisible.value = false
  editingNote.value = null
  noteFormRef.value?.resetFields()
}

const handleSubmitNote = async () => {
  if (!noteFormRef.value) return
  
  try {
    await noteFormRef.value.validate()
    
    noteSubmitting.value = true
    
    if (editingNote.value) {
      // 更新
      await updateDoctorNote(editingNote.value.id, noteForm.value)
      ElMessage.success('更新成功')
    } else {
      // 创建
      await createDoctorNote(props.familyId, props.patientUserId, noteForm.value)
      ElMessage.success('创建成功')
    }
    
    noteDialogVisible.value = false
    await loadNotes()
  } catch (error) {
    if (error !== false) { // 表单验证失败会返回false
      console.error('提交病历记录失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    noteSubmitting.value = false
  }
}

// 诊断工具相关方法
const handleGenerateRecommendation = async () => {
  try {
    generatingRecommendation.value = true
    
    const request = {
      date: dayjs().format('YYYY-MM-DD'),
      categories: null, // 生成所有类别
      maxItems: 5,
      strictMode: false,
    }
    
    await generateRecommendationForPatient(props.familyId, props.patientUserId, request)
    ElMessage.success('健康建议生成成功')
    
    // 刷新详情数据
    await loadDetail()
  } catch (error) {
    console.error('生成建议失败:', error)
    ElMessage.error('生成建议失败')
  } finally {
    generatingRecommendation.value = false
  }
}
</script>

<style scoped lang="scss">
.patient-detail-tabs {
  :deep(.el-tabs__content) {
    padding: 16px 0;
  }
}

.patient-detail {
  padding: 0;
}

.section-card {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assessment-section,
.log-statistics-section,
.recommendations-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }

  h4 {
    margin: 0 0 12px 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;

  .stat-label {
    font-weight: 600;
    margin-bottom: 4px;
  }

  span:not(.stat-label) {
    margin-right: 12px;
  }
}

.recommendation-title {
  margin-left: 8px;
  font-size: 13px;
}

.tags-section {
  margin-bottom: 16px;

  .tag-item {
    margin-right: 8px;
    margin-bottom: 8px;
  }
}

.risk-level {
  display: flex;
  align-items: center;
  gap: 8px;

  .risk-label {
    font-weight: 600;
  }
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;

  .el-button {
    flex: 1;
    min-width: 120px;
  }
}

.diagnostic-tools {
  .tooltip-text {
    margin-top: 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

// 病历记录样式
.notes-container {
  padding: 0;
}

.notes-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.notes-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.note-item {
  .note-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .note-date {
    font-weight: 600;
    font-size: 14px;
  }

  .note-actions {
    display: flex;
    gap: 8px;
  }

  .note-content {
    .note-field {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      strong {
        display: block;
        margin-bottom: 8px;
        color: var(--el-text-color-primary);
        font-weight: 600;
      }

      p {
        margin: 0;
        color: var(--el-text-color-regular);
        line-height: 1.6;
        white-space: pre-wrap;
      }

      .note-content-text {
        color: var(--el-text-color-regular);
        line-height: 1.6;
        white-space: pre-wrap;
      }
    }
  }

  .note-footer {
    margin-top: 16px;
    padding-top: 12px;
    border-top: 1px solid var(--el-border-color-lighter);
    display: flex;
    gap: 16px;
    font-size: 12px;
    color: var(--el-text-color-secondary);

    .note-meta {
      flex: 1;
    }
  }
}
</style>
