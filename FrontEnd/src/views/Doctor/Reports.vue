<template>
  <div class="page-container">
    <el-page-header content="体检报告与点评" class="mb-24" />

    <el-card class="glass-card">
      <div class="common-toolbar">
        <el-select :model-value="familyId" placeholder="选择家庭" style="width: 200px" @change="onSwitch">
          <el-option v-for="f in families" :key="f.id" :label="f.name" :value="String(f.id)" />
        </el-select>

        <el-select v-model="selectedMemberId" placeholder="选择患者" style="width: 200px" clearable @change="handleMemberChange">
          <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.phone" :value="m.userId" />
        </el-select>

        <el-button type="primary" @click="loadReports" :disabled="!selectedMemberId">刷新</el-button>
      </div>

      <el-table :data="reports" v-loading="loading" style="width: 100%">
        <el-table-column prop="reportName" label="报告名称" min-width="180" />
        <el-table-column prop="reportType" label="类型" width="120">
          <template #default="scope">
            {{ formatReportType(scope.row.reportType) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="doctorCommentTime" label="点评时间" width="180">
          <template #default="scope">
            {{ scope.row.doctorCommentTime ? formatDate(scope.row.doctorCommentTime) : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <div class="flex items-center justify-center gap-2">
              <el-tooltip content="查看详情" placement="top">
                <el-button link type="primary" :icon="View" @click="openDetail(scope.row)" />
              </el-tooltip>

              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
                <el-button link type="info" :icon="MoreFilled" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="review">
                      <el-icon><EditPen /></el-icon>医生点评
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && reports.length === 0" description="暂无报告" :image-size="100" />
    </el-card>

    <el-dialog 
      v-model="detailVisible" 
      width="1100px" 
      title="报告详情与点评"
      top="5vh" 
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-container" style="height: 75vh; display: flex; overflow: hidden;">
        <div class="left-viewer" style="flex: 1.5; background: #000; display: flex; align-items: center; justify-content: center;">
          <el-image
            v-if="activeReport"
            :src="activeReport.imageUrl"
            fit="contain"
            :preview-src-list="[activeReport.imageUrl]"
            style="width: 100%; height: 100%;"
          >
            <template #error>
              <div class="text-white flex justify-center items-center h-full">
                <el-icon><Picture /></el-icon> 图片加载失败
              </div>
            </template>
          </el-image>
        </div>
        <div class="right-panel custom-scrollbar" style="flex: 1; overflow-y: auto; padding: 20px;">
          <div v-if="activeReport">
            <el-card class="box-card mb-4 shadow-sm border-none bg-blue-50/50">
              <template #header>
                <div class="flex justify-between items-center">
                  <span class="font-bold text-gray-800 flex items-center">
                    <el-icon class="mr-1 text-blue-500"><DataAnalysis /></el-icon> 智能解读
                  </span>
                  <el-tag :type="getStatusType(activeReport.status)" effect="dark" size="small">
                     {{ getStatusText(activeReport.status) }} 
                  </el-tag>
                </div>
              </template>
              
              <div v-if="activeReport.status === 'COMPLETED'">
                <div v-if="interpretation" class="text-sm text-gray-700 leading-relaxed">
                  <div class="font-bold mb-1 text-blue-700">总体评价:</div>
                  <p class="mb-3">{{ interpretation.summary }}</p>
                </div>
                <div v-if="ocrData && ocrData.items">
                  <h4 class="font-bold mb-2 text-blue-700">详细指标</h4>
                  <el-table :data="ocrItems" style="width: 100%" stripe>
                    <el-table-column prop="name" label="项目" />
                    <el-table-column prop="value" label="结果" />
                    <el-table-column label="解读">
                      <template #default="scope">
                        {{ getInterpretation(scope.row.name) }}
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
              <div v-else-if="activeReport.status === 'PROCESSING'" class="py-10 text-center">
                <el-icon class="is-loading text-4xl text-primary mb-4"><Loading /></el-icon>
                <p>AI正在分析报告，请稍候...</p>
              </div>
              <div v-else>
                <el-result icon="error" title="分析失败" sub-title="请等待患者重新上传清晰图片" />
              </div>
            </el-card>

            <div class="comment-section">
              <h4 class="font-bold mb-2">医生点评</h4>
              <div class="quick-tags mb-2">
                <el-tag 
                  v-for="tag in quickTags" 
                  :key="tag" 
                  class="cursor-pointer mr-1 mb-1" 
                  type="info"
                  effect="light"
                  @click="appendPhrase(tag + ' ')">
                  {{ tag }}
                </el-tag>
              </div>
              <el-input 
                ref="commentRef"
                v-model="commentText" 
                type="textarea" 
                :rows="6" 
                placeholder="请输入专业的医疗建议与指导..." 
                resize="none"
                class="text-base"
              />
              <div class="mt-2 text-xs text-gray-400 text-right">
                建议结合患者既往病史进行综合评价
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer pt-4 border-t border-gray-100">
          <el-button @click="detailVisible = false">取消</el-button>
          <el-button type="primary" :loading="commentLoading" @click="submitComment" :disabled="!activeReport">
            保存点评
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 组件：Reports.vue
 *
 * 业务说明：用于呈现对应页面/模块功能，并通过 API 层与后端进行数据交互。
 */

import { ref, computed, onMounted, watch } from 'vue'
import { useDoctorStore } from '@/stores/doctor'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { Loading, View, MoreFilled, EditPen, Picture, DataAnalysis } from '@element-plus/icons-vue'
import { getDoctorReports, getDoctorReportDetail, commentDoctorReport } from '@/api/doctor'

const doctorStore = useDoctorStore()
const families = computed(() => doctorStore.families)
const familyId = computed(() => doctorStore.currentFamilyId)
const members = computed(() => doctorStore.boundMembers)

const selectedMemberId = ref(null)
const reports = ref([])
const loading = ref(false)

const detailVisible = ref(false)
const detailLoading = ref(false)
const commentLoading = ref(false)
const activeReport = ref(null)
const commentText = ref('')

const quickTags = [
  '指标正常',
  '建议复查',
  '忌烟酒',
  '清淡饮食',
  '多休息',
  '适量运动',
  '定期随访',
  '遵医嘱服药'
]

const ocrData = computed(() => {
  if (!activeReport.value || !activeReport.value.ocrData) return null
  try {
    return JSON.parse(activeReport.value.ocrData)
  } catch (e) {
    return null
  }
})

const interpretation = computed(() => {
  if (!activeReport.value || !activeReport.value.interpretation) return null
  try {
    return JSON.parse(activeReport.value.interpretation)
  } catch (e) {
    return null
  }
})

const ocrItems = computed(() => {
  if (!ocrData.value || !ocrData.value.items) return []
  const items = ocrData.value.items
  if (Array.isArray(items)) return items
  if (typeof items === 'object') {
    return Object.keys(items).map(key => ({
      name: key,
      value: items[key]
    }))
  }
  return []
})

const getInterpretation = (itemName) => {
  if (!interpretation.value || !interpretation.value.details) return '-'
  return interpretation.value.details[itemName] || '正常'
}

const formatReportType = (type) => {
  const map = {
    LAB_REPORT: '化验单',
    EXAM_REPORT: '体检报告',
    PRESCRIPTION: '处方单',
    OTHER: '其他'
  }
  return map[type] || type
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'info',
    PROCESSING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '分析中',
    COMPLETED: '已完成',
    FAILED: '失败'
  }
  return map[status] || status
}

const onSwitch = async (id) => {
  await doctorStore.setCurrentFamily(id)
  selectedMemberId.value = null
  reports.value = []
}

const handleMemberChange = async () => {
  await loadReports()
}

const loadReports = async () => {
  if (!selectedMemberId.value) {
    reports.value = []
    return
  }
  loading.value = true
  try {
    const res = await getDoctorReports(selectedMemberId.value)
    if (res.code === 0) {
      reports.value = res.data || []
    } else {
      reports.value = []
    }
  } catch (e) {
    reports.value = []
    ElMessage.error('加载报告失败')
  } finally {
    loading.value = false
  }
}

const openDetail = async (row, mode = 'view') => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getDoctorReportDetail(row.id)
    if (res.code === 0) {
      activeReport.value = res.data
      commentText.value = res.data?.doctorComment || ''
      
      // 等待弹窗渲染完成后根据模式执行不同操作
      setTimeout(() => {
        if (mode === 'review') {
          // 点评模式：滚动到底部并聚焦输入框
          scrollToBottom();
          focusCommentInput();
        } else {
          // 查看模式：滚动到顶部
          scrollToTop();
        }
      }, 100);
    }
  } catch (e) {
    ElMessage.error('加载报告详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  const rightPanel = document.querySelector('.right-panel');
  if (rightPanel) {
    rightPanel.scrollTop = rightPanel.scrollHeight;
  }
};

// 滚动到顶部
const scrollToTop = () => {
  const rightPanel = document.querySelector('.right-panel');
  if (rightPanel) {
    rightPanel.scrollTop = 0;
  }
};

// 聚焦评论输入框
const commentRef = ref(null);
const focusCommentInput = () => {
  if (commentRef.value) {
    commentRef.value.focus();
  }
};

// 追加快捷短语
const appendPhrase = (text) => {
  commentText.value += text;
};

const submitComment = async () => {
  if (!activeReport.value) return
  commentLoading.value = true
  try {
    const res = await commentDoctorReport(activeReport.value.id, commentText.value)
    if (res.code === 0) {
      activeReport.value = res.data
      reports.value = reports.value.map(r => (r.id === res.data.id ? res.data : r))
      ElMessage.success('点评已保存')
    }
  } catch (e) {
    ElMessage.error('保存点评失败')
  } finally {
    commentLoading.value = false
  }
}

const handleCommand = (command, row) => {
  if (command === 'review') {
    openDetail(row, 'review');
  }
}

watch(detailVisible, (val) => {
  if (!val) {
    activeReport.value = null
    commentText.value = ''
  }
})

onMounted(async () => {
  if (!families.value.length) {
    await doctorStore.fetchFamilies()
  }
  if (familyId.value) {
    await doctorStore.fetchMembers(familyId.value)
  }
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 24px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.common-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  height: 75vh;
  overflow: hidden;
}

.detail-left {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px solid #e4e7ed;
}

.detail-right {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: auto;
  padding-right: 8px;
}

.custom-scrollbar {
  /* 滚动条整体部分 */
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  /* 滚动条滑块 */
  &::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 4px;
    
    &:hover {
      background: #909399;
    }
  }
  
  /* 滚动条轨道 */
  &::-webkit-scrollbar-track {
    background: #f4f4f5;
    border-radius: 4px;
  }
}

.h-full {
  height: 100%;
}

.overflow-hidden {
  overflow: hidden;
}

.overflow-y-auto {
  overflow-y: auto;
}

.bg-gray-100 {
  background-color: #f3f4f6;
}

.rounded-lg {
  border-radius: 8px;
}

.p-2 {
  padding: 0.5rem;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.relative {
  position: relative;
}

.px-2 {
  padding-left: 0.5rem;
  padding-right: 0.5rem;
}

.grid-template-columns-1fr-1fr {
  grid-template-columns: 1fr 1fr;
}

.gap-16 {
  gap: 4rem;
}

@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
    height: 80vh;
  }
  
  .detail-left {
    min-height: 300px;
  }
}

// 定义常用的flex布局类
.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.gap-2 {
  gap: 0.5rem;
}

// 为操作列按钮提供合适的内边距
.el-table .cell {
  padding: 0 !important;
}

.border {
  border: 1px solid #dcdfe6;
}

.border-gray-200 {
  border-color: #e4e7ed;
}

.mb-4 {
  margin-bottom: 1rem;
}

.shadow-sm {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.border-blue-200 {
  border-color: #dee3f9;
}

.left-panel {
  flex: 1.2;
  height: 100%;
  overflow: hidden;
}

.right-panel {
  flex: 0.8;
  height: 100%;
  overflow-y: auto;
}

.dialog-content-wrapper {
  height: 75vh;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

.box-card {
  border-radius: 12px;
}

.bg-blue-50\/50 {
  background-color: rgba(239, 246, 255, 0.5);
}

.text-blue-500 {
  color: #3b82f6;
}

.text-blue-700 {
  color: #1d4ed8;
}

.text-blue-800 {
  color: #1e40af;
}

.mr-1 {
  margin-right: 0.25rem;
}

.mb-1 {
  margin-bottom: 0.25rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-3 {
  margin-bottom: 0.75rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.mt-2 {
  margin-top: 0.5rem;
}

.pt-4 {
  padding-top: 1rem;
}

.border-t {
  border-top: 1px solid #e5e7eb;
}

.border-gray-100 {
  border-color: #f3f4f6;
}

.pointer-events-none {
  pointer-events: none;
}

.bg-black\/50 {
  background-color: rgba(0, 0, 0, 0.5);
}

.px-3 {
  padding-left: 0.75rem;
  padding-right: 0.75rem;
}

.py-1 {
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
}

.text-xs {
  font-size: 0.75rem;
}

.text-sm {
  font-size: 0.875rem;
}

.text-base {
  font-size: 1rem;
}

.leading-relaxed {
  line-height: 1.625;
}

.items-center {
  align-items: center;
}

.justify-between {
  justify-content: space-between;
}

.shadow-md {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.shadow-sm {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.border-none {
  border: none;
}

.gap-20px {
  gap: 20px;
}

.overflow-hidden {
  overflow: hidden;
}

.bg-gray-900 {
  background-color: #111827;
}

.rounded-lg {
  border-radius: 0.5rem;
}

.flex {
  display: flex;
}

.relative {
  position: relative;
}

.absolute {
  position: absolute;
}

.bottom-4 {
  bottom: 1rem;
}

.text-white {
  color: #fff;
}

.rounded-full {
  border-radius: 9999px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
