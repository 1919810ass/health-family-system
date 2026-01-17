<template>
  <div class="doctor-management">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <p class="page-subtitle">管理医生资质、执业信息与服务数据</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" round v-particles @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增医生
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div v-if="currentMode !== 'stats'" class="glass-card mb-24 stagger-anim" style="--delay: 0.2s">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="医生ID">
          <el-input v-model="searchForm.doctorId" placeholder="请输入医生ID" clearable>
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入医生姓名" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="医院">
          <el-input v-model="searchForm.hospital" placeholder="请输入医院名称" clearable>
            <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="searchForm.specialty" placeholder="请输入专业领域" clearable>
            <template #prefix><el-icon><Medal /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="认证状态" v-if="currentMode === 'list'">
          <el-select v-model="searchForm.certified" placeholder="请选择认证状态" clearable>
            <el-option label="已认证" :value="true" />
            <el-option label="未认证" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round v-particles @click="searchDoctors">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button round @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid mb-24 stagger-anim" style="--delay: 0.3s">
      <div class="glass-card stat-card">
        <div class="stat-icon primary">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">医生总数</div>
          <div class="stat-value">{{ stats.totalDoctors }}</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon success">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">已认证</div>
          <div class="stat-value">{{ stats.certifiedDoctors }}</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon warning">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">待审核</div>
          <div class="stat-value">{{ stats.pendingDoctors }}</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon danger">
          <el-icon><ChatLineRound /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">服务用户</div>
          <div class="stat-value">{{ stats.serviceUsers }}</div>
        </div>
      </div>
    </div>

    <!-- 统计图表（仅统计模式显示） -->
    <div v-if="currentMode === 'stats'" class="glass-card stagger-anim" style="--delay: 0.4s; min-height: 400px; display: flex; align-items: center; justify-content: center;">
      <el-empty description="服务趋势图表开发中..." />
    </div>

    <!-- 医生列表 -->
    <div v-if="currentMode !== 'stats'" class="glass-card stagger-anim" style="--delay: 0.4s">
      <div class="card-header">
        <div class="header-left">
          <div class="icon-box primary">
            <el-icon><List /></el-icon>
          </div>
          <span class="title">医生列表</span>
        </div>
        <div class="header-actions">
          <el-button circle @click="refreshList" :loading="loading">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>

      <el-table 
        :data="doctors" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="hospital" label="执业医院" width="200" show-overflow-tooltip />
        <el-table-column prop="specialty" label="专业领域" width="150" show-overflow-tooltip />
        <el-table-column prop="department" label="科室" width="120" />
        <el-table-column label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="row.certified ? 'success' : (row.certificationStatus === 'REJECTED' ? 'danger' : 'warning')"
            >
              {{ row.certified ? '已认证' : (row.certificationStatus === 'REJECTED' ? '已拒绝' : '待审核') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="120" v-if="currentMode === 'collaboration' || currentMode === 'list'">
          <template #default="{ row }">
            <el-rate 
              :model-value="row.rating ? Number(row.rating) : 0" 
              disabled 
              show-score 
              text-color="#ff9900" 
              :score-template="row.rating ? row.rating.toFixed(1) : '0.0'" 
            />
          </template>
        </el-table-column>
        <el-table-column prop="serviceCount" label="服务次数" width="100" v-if="currentMode === 'collaboration'" />
        <el-table-column prop="createdAt" label="注册时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <!-- 审核模式 -->
              <template v-if="currentMode === 'audit'">
                <el-button size="small" type="success" plain @click="handleAudit(row, 'approve')">通过</el-button>
                <el-button size="small" type="danger" plain @click="handleAudit(row, 'reject')">拒绝</el-button>
              </template>
              
              <!-- 其他模式 -->
              <template v-else>
                <el-button size="small" text type="primary" @click="showEditDialog(row)">编辑</el-button>
                <el-button size="small" text @click="showDetailDialog(row)">详情</el-button>
                <el-dropdown trigger="click">
                  <el-button size="small" text type="info">
                    更多
                    <el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu class="custom-dropdown">
                      <el-dropdown-item @click="toggleStatus(row)">
                        {{ row.status === 1 ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="toggleCertification(row)" divided>
                        {{ row.certified ? '撤销认证' : '认证通过' }}
                      </el-dropdown-item>
                      <el-dropdown-item @click="showCollaboration(row)">协作监控</el-dropdown-item>
                      <el-dropdown-item @click="showServiceStats(row)">服务统计</el-dropdown-item>
                      <el-dropdown-item @click="handleDeleteDoctor(row)" divided style="color: #f56c6c;">
                        <el-icon><Delete /></el-icon>删除医生
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 医生编辑/创建对话框 -->
    <el-dialog 
      :title="dialog.title" 
      v-model="dialog.visible" 
      width="50%" 
      :before-close="handleDialogClose"
      >
      <el-form
        :model="dialog.form" 
        :rules="dialog.rules" 
        ref="dialogFormRef"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="dialog.form.name" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="执业医院" prop="hospital">
          <el-input v-model="dialog.form.hospital" placeholder="请输入执业医院" />
        </el-form-item>
        <el-form-item label="科室" prop="department">
          <el-input v-model="dialog.form.department" placeholder="请输入科室" />
        </el-form-item>
        <el-form-item label="专业领域" prop="specialty">
          <el-input v-model="dialog.form.specialty" placeholder="请输入专业领域" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="dialog.form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="dialog.form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="认证状态" prop="certified">
          <el-switch
            v-model="dialog.form.certified"
            active-text="已认证"
            inactive-text="未认证"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialog.form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="简介">
          <el-input 
            v-model="dialog.form.bio" 
            type="textarea" 
            placeholder="请输入医生简介" 
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="submitDialog" :loading="dialog.submitting" class="submit-btn" v-particles>确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 医生详情对话框 -->
    <el-dialog title="医生详情" v-model="detailDialog.visible" width="60%" class="glass-dialog" align-center>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="医生ID">{{ detailDialog.doctor.id }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailDialog.doctor.name }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ detailDialog.doctor.title || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="执业医院">{{ detailDialog.doctor.hospital }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ detailDialog.doctor.department }}</el-descriptions-item>
        <el-descriptions-item label="专业领域">{{ detailDialog.doctor.specialty }}</el-descriptions-item>
        <el-descriptions-item label="执业证书编号">{{ detailDialog.doctor.licenseNumber || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailDialog.doctor.idCard || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailDialog.doctor.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detailDialog.doctor.email }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">
          <el-tag :type="detailDialog.doctor.certified ? 'success' : 'warning'">
            {{ detailDialog.doctor.certified ? '已认证' : '未认证' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailDialog.doctor.status === 1 ? 'success' : 'danger'">
            {{ detailDialog.doctor.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="评分">{{ detailDialog.doctor.rating }}分</el-descriptions-item>
        <el-descriptions-item label="服务用户数">{{ detailDialog.doctor.serviceCount }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detailDialog.doctor.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailDialog.doctor.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ detailDialog.doctor.bio || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Refresh, ArrowDown, User, Edit, View, 
  CircleCheck, CircleClose, ChatLineRound,
  Key, OfficeBuilding, Medal, Search, List, Delete
} from '@element-plus/icons-vue'
import { getDoctors, getDoctorStats, getPendingDoctors, updateDoctorStatus, updateDoctorCertification, createDoctor, updateDoctor, approveDoctor, rejectDoctor, deleteDoctor } from '../../../api/admin'

const route = useRoute()
const pageTitle = ref('医生管理')

// 根据路由模式设置默认筛选/标题
const currentMode = computed(() => {
  const path = route.path || ''
  if (path.includes('/audit')) return 'audit'
  if (path.includes('/collaboration')) return 'collaboration'
  if (path.includes('/stats')) return 'stats'
  return 'list'
})

const applyModeDefaults = () => {
  switch (currentMode.value) {
    case 'audit':
      pageTitle.value = '资质审核'
      searchForm.certified = false // 默认查未认证
      break
    case 'collaboration':
      pageTitle.value = '协作监控'
      break
    case 'stats':
      pageTitle.value = '服务统计'
      break
    default:
      pageTitle.value = '医生管理'
      searchForm.certified = ''
      break
  }
}

watch(
  () => route.path,
  () => {
    applyModeDefaults()
    pagination.page = 1
    loadDoctors()
    if (currentMode.value === 'stats') {
      loadStats()
    }
  }
)

// 搜索表单
const searchForm = reactive({
  doctorId: '',
  name: '',
  hospital: '',
  specialty: '',
  certified: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 统计信息
const stats = reactive({
  totalDoctors: 0,
  certifiedDoctors: 0,
  pendingDoctors: 0,
  serviceUsers: 0
})

// 医生列表
const doctors = ref([])
const loading = ref(false)
const selectedDoctors = ref([])

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '',
  type: '', // 'create' or 'edit'
  submitting: false,
  form: {
    id: null,
    name: '',
    hospital: '',
    department: '',
    specialty: '',
    phone: '',
    email: '',
    certified: false,
    status: 1,
    bio: ''
  },
  rules: {
    name: [
      { required: true, message: '请输入医生姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
    ],
    hospital: [
      { required: true, message: '请输入执业医院', trigger: 'blur' },
      { min: 2, max: 50, message: '医院名称长度在2-50个字符', trigger: 'blur' }
    ],
    department: [
      { required: true, message: '请输入科室', trigger: 'blur' },
      { min: 2, max: 20, message: '科室名称长度在2-20个字符', trigger: 'blur' }
    ],
    specialty: [
      { required: true, message: '请输入专业领域', trigger: 'blur' },
      { min: 2, max: 30, message: '专业领域长度在2-30个字符', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
    ]
  }
})

// 详情对话框
const detailDialog = reactive({
  visible: false,
  doctor: {}
})

// 表单引用
const dialogFormRef = ref(null)

// 加载医生数据
onMounted(() => {
  applyModeDefaults()
  loadDoctors()
  loadStats()
})

// 加载医生列表（兼容后端 Result<T> 包装结构）
const loadDoctors = async () => {
  loading.value = true
  try {
    let res
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }

    if (currentMode.value === 'audit') {
      // 审核模式下调用待审核列表接口
      res = await getPendingDoctors({ page: pagination.page, size: pagination.size })
    } else {
      // 普通模式
      res = await getDoctors(params)
    }

    const data = res && typeof res === 'object' ? res.data || {} : {}

    const list = Array.isArray(data.list)
      ? data.list
      : Array.isArray(data.items)
        ? data.items
        : []

    doctors.value = list
    pagination.total = Number(data.total) || list.length || 0
  } catch (error) {
    ElMessage.error('加载医生列表失败')
    console.error('Error loading doctors:', error)
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStats = async () => {
  try {
    const res = await getDoctorStats()
    const data = res && typeof res === 'object' ? res.data || {} : {}
    stats.totalDoctors = Number(data.totalDoctors) || 0
    stats.certifiedDoctors = Number(data.certifiedDoctors) || 0
    stats.pendingDoctors = Number(data.pendingDoctors) || 0
    stats.serviceUsers = Number(data.serviceUsers) || 0
  } catch (error) {
    ElMessage.error('加载统计信息失败')
    console.error('Error loading doctor stats:', error)
  }
}

// 搜索医生
const searchDoctors = () => {
  pagination.page = 1
  loadDoctors()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  loadDoctors()
}

// 刷新列表
const refreshList = () => {
  loadDoctors()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  loadDoctors()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadDoctors()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedDoctors.value = selection
}

// 显示创建对话框
const showCreateDialog = () => {
  dialog.type = 'create'
  dialog.title = '新增医生'
  dialog.form.id = null
  dialog.form.name = ''
  dialog.form.hospital = ''
  dialog.form.department = ''
  dialog.form.specialty = ''
  dialog.form.phone = ''
  dialog.form.email = ''
  dialog.form.certified = false
  dialog.form.status = 1
  dialog.form.bio = ''
  dialog.visible = true
}

// 显示编辑对话框
const showEditDialog = (doctor) => {
  dialog.type = 'edit'
  dialog.title = '编辑医生'
  dialog.form.id = doctor.id
  dialog.form.name = doctor.name
  dialog.form.hospital = doctor.hospital
  dialog.form.department = doctor.department
  dialog.form.specialty = doctor.specialty
  dialog.form.phone = doctor.phone
  dialog.form.email = doctor.email
  dialog.form.certified = doctor.certified
  dialog.form.status = doctor.status
  dialog.form.bio = doctor.bio
  dialog.visible = true
}

// 显示详情对话框
const showDetailDialog = (doctor) => {
  detailDialog.doctor = { ...doctor }
  detailDialog.visible = true
}

// 提交对话框
const submitDialog = async () => {
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      dialog.submitting = true
      try {
        if (dialog.type === 'create') {
          await createDoctor(dialog.form)
          ElMessage.success('医生创建成功')
        } else {
          await updateDoctor(dialog.form.id, dialog.form)
          ElMessage.success('医生更新成功')
        }
        dialog.visible = false
        loadDoctors()
      } catch (error) {
        ElMessage.error(dialog.type === 'create' ? '创建医生失败' : '更新医生失败')
        console.error('Error submitting doctor:', error)
      } finally {
        dialog.submitting = false
      }
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  dialog.visible = false
  if (dialogFormRef.value) {
    dialogFormRef.value.clearValidate()
  }
}

// 切换医生状态
const toggleStatus = async (doctor) => {
  try {
    await ElMessageBox.confirm(
      `确定要${doctor.status === 1 ? '禁用' : '启用'}医生 ${doctor.name} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateDoctorStatus(doctor.id, doctor.status === 1 ? 0 : 1)
    doctor.status = doctor.status === 1 ? 0 : 1
    ElMessage.success('医生状态更新成功')
  } catch {
    // 用户取消操作
  }
}

// 审核操作
const handleAudit = async (doctor, action) => {
  try {
    if (action === 'approve') {
      await ElMessageBox.confirm(
        `确定要认证通过医生 ${doctor.name} 吗？`,
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'primary'
        }
      )
      await approveDoctor(doctor.id)
      doctor.certified = true
      doctor.certificationStatus = 'APPROVED'
      ElMessage.success('医生认证成功')
    } else if (action === 'reject') {
      const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝认证', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /\S+/,
        inputErrorMessage: '拒绝原因不能为空'
      })
      await rejectDoctor(doctor.id, value)
      doctor.certified = false
      doctor.certificationStatus = 'REJECTED'
      ElMessage.success('医生认证已拒绝')
    }
    loadDoctors() // 重新加载列表
    loadStats() // 重新加载统计
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Error handling audit:', error)
    }
  }
}

// 切换认证状态（兼容旧逻辑）
const toggleCertification = (doctor) => {
  if (doctor.certified) {
    // 撤销认证 -> 视为拒绝？或者单独的撤销逻辑
    handleAudit(doctor, 'reject') 
  } else {
    handleAudit(doctor, 'approve')
  }
}

// 显示协作监控
const showCollaboration = (doctor) => {
  ElMessage.info(`查看医生 ${doctor.name} 的协作监控`)
  // 这里可以跳转到协作监控页面
}

// 显示服务统计
const showServiceStats = (doctor) => {
  ElMessage.info(`查看医生 ${doctor.name} 的服务统计`)
  // 这里可以跳转到服务统计页面
}

// 删除医生
const handleDeleteDoctor = async (doctor) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除医生 ${doctor.name}（ID: ${doctor.id}）吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'danger',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await deleteDoctor(doctor.id)
    ElMessage.success('医生已删除')
    loadDoctors() // 重新加载列表
    loadStats()   // 重新加载统计
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Error deleting doctor:', error)
      ElMessage.error('删除医生失败')
    }
  }
}
</script>

<style scoped lang="scss">
@use 'sass:map';
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.doctor-management {
  .page-header {
    margin-bottom: 32px;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;

    .header-content {
      .page-title {
        font-size: 28px;
        font-weight: 800;
        margin: 0 0 8px 0;
        @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'success')));
        letter-spacing: -0.5px;
      }

      .page-subtitle {
        font-size: 14px;
        color: var(--el-text-color-secondary);
        margin: 0;
      }
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 20px;

    .stat-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 24px;
      transition: all 0.3s vars.$ease-spring;

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;

        &.primary {
          background: rgba(map.get(vars.$colors, 'primary'), 0.1);
          color: map.get(vars.$colors, 'primary');
        }
        
        &.success {
          background: rgba(map.get(vars.$colors, 'success'), 0.1);
          color: map.get(vars.$colors, 'success');
        }
        
        &.warning {
          background: rgba(map.get(vars.$colors, 'warning'), 0.1);
          color: map.get(vars.$colors, 'warning');
        }
        
        &.danger {
          background: rgba(map.get(vars.$colors, 'danger'), 0.1);
          color: map.get(vars.$colors, 'danger');
        }
      }

      .stat-info {
        .stat-title {
          font-size: 14px;
          color: var(--el-text-color-secondary);
          margin-bottom: 4px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: var(--el-text-color-primary);
        }
      }

      &:hover {
        transform: translateY(-5px);
        .stat-icon {
          transform: scale(1.1) rotate(5deg);
        }
      }
    }
  }

  // Animation Utilities
  .stagger-anim {
    opacity: 0;
    animation: slideUpFade 0.8s vars.$ease-spring forwards;
    animation-delay: var(--delay, 0s);
  }

  @keyframes slideUpFade {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // Responsive adjustments
  @media (max-width: 768px) {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      
      .header-actions {
        width: 100%;
        .el-button {
          width: 100%;
        }
      }
    }
  }
}

// Glass effect class
.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: vars.$shadow-lg;
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: vars.$shadow-xl;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .icon-box {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.primary {
        background: rgba(map.get(vars.$colors, 'primary'), 0.1);
        color: map.get(vars.$colors, 'primary');
      }
    }

    .title {
      font-size: 18px;
      font-weight: 700;
      color: var(--el-text-color-primary);
    }
  }
}

.custom-table {
  background: transparent;
  
  :deep(tr), :deep(th.el-table__cell) {
    background: transparent;
  }
  
  :deep(th.el-table__cell) {
    background-color: rgba(255, 255, 255, 0.5);
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
  
  :deep(.el-table__row:hover) {
    background-color: rgba(map.get(vars.$colors, 'primary'), 0.05) !important;
  }

  :deep(.el-table__row:hover > td.el-table__cell) {
    background-color: transparent !important;
  }
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

// Dialog styles
:deep(.glass-dialog) {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: vars.$shadow-xl;
  
  .el-dialog__header {
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    margin-right: 0;
    padding: 20px 24px;
    
    .el-dialog__title {
      font-weight: 700;
      color: var(--el-text-color-primary);
    }
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    border-top: 1px solid rgba(0, 0, 0, 0.05);
    padding: 20px 24px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  
  .cancel-btn {
    border-radius: vars.$radius-md;
    &:hover {
      background: rgba(map.get(vars.$colors, 'primary'), 0.05);
    }
  }
  
  .submit-btn {
    border-radius: vars.$radius-md;
    padding: 10px 24px;
  }
}
</style>