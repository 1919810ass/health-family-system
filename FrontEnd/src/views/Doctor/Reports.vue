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
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row)">查看</el-button>
            <el-button link type="success" @click="openDetail(scope.row)">点评</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && reports.length === 0" description="暂无报告" :image-size="100" />
    </el-card>

    <el-dialog v-model="detailVisible" width="1000px" title="报告详情与点评">
      <div v-loading="detailLoading">
        <div v-if="activeReport" class="detail-grid">
          <el-card class="glass-card">
            <template #header>
              <div class="font-bold">原始图片</div>
            </template>
            <img :src="activeReport.imageUrl" class="w-full rounded-lg" />
          </el-card>

          <div class="detail-right">
            <el-card class="glass-card">
              <template #header>
                <div class="font-bold flex justify-between items-center">
                  <span>智能解读</span>
                  <el-tag :type="activeReport.status === 'COMPLETED' ? 'success' : 'warning'">{{ getStatusText(activeReport.status) }}</el-tag>
                </div>
              </template>
              <div v-if="activeReport.status === 'COMPLETED'">
                <div v-if="interpretation" class="mb-6">
                  <h4 class="font-bold mb-2 text-primary">总体评价</h4>
                  <p class="text-gray-700 leading-relaxed">{{ interpretation.summary }}</p>
                </div>
                <div v-if="ocrData && ocrData.items">
                  <h4 class="font-bold mb-2 text-primary">详细指标</h4>
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

            <el-card class="glass-card mt-16">
              <template #header>
                <div class="font-bold">医生点评</div>
              </template>
              <el-input v-model="commentText" type="textarea" :rows="5" placeholder="请输入点评内容" />
            </el-card>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" :loading="commentLoading" @click="submitComment" :disabled="!activeReport">保存点评</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useDoctorStore } from '@/stores/doctor'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { Loading } from '@element-plus/icons-vue'
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

const openDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getDoctorReportDetail(row.id)
    if (res.code === 0) {
      activeReport.value = res.data
      commentText.value = res.data?.doctorComment || ''
    }
  } catch (e) {
    ElMessage.error('加载报告详情失败')
  } finally {
    detailLoading.value = false
  }
}

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
  gap: 16px;
}

.detail-right {
  display: flex;
  flex-direction: column;
}

@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
