<template>
  <div class="family-management">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <p class="page-subtitle">管理家庭账户、成员关系与数据权限</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" round v-particles @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增家庭
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="glass-card mb-24 stagger-anim" style="--delay: 0.2s">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="家庭ID">
          <el-input v-model="searchForm.familyId" placeholder="请输入家庭ID" clearable>
             <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="家庭名称">
          <el-input v-model="searchForm.name" placeholder="请输入家庭名称" clearable>
             <template #prefix><el-icon><House /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="所有者">
          <el-input v-model="searchForm.owner" placeholder="请输入所有者昵称" clearable>
             <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round v-particles @click="searchFamilies">
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

    <!-- 简单统计（仅数据统计模式显示） -->
    <div v-if="currentMode === 'stats'" class="family-stats mb-24 stagger-anim" style="--delay: 0.3s">
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper blue">
          <el-icon><House /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ familyStats.totalFamilies }}</div>
          <div class="stat-title">家庭总数</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper green">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ familyStats.totalMembers }}</div>
          <div class="stat-title">成员总数</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper orange">
          <el-icon><DataLine /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ familyStats.avgMembers }}</div>
          <div class="stat-title">平均成员数</div>
        </div>
      </div>
      <div class="glass-card stat-card">
        <div class="stat-icon-wrapper purple">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ familyStats.enabledFamilies }}</div>
          <div class="stat-title">启用家庭</div>
        </div>
      </div>
    </div>

    <!-- 家庭列表 -->
    <div class="glass-card stagger-anim" style="--delay: 0.4s">
      <div class="card-header">
        <span>家庭列表</span>
        <div class="header-actions">
          <el-button circle @click="refreshList" :loading="loading">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>

      <el-table 
        :data="families" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="家庭名称" min-width="140">
           <template #default="{ row }">
              <span class="family-name">{{ row.name }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="所有者" width="120">
           <template #default="{ row }">
             <div class="owner-info">
               <el-avatar :size="24" :src="row.ownerAvatar" class="mr-2">{{ (row.ownerName || '').charAt(0) }}</el-avatar>
               <span>{{ row.ownerName }}</span>
             </div>
           </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="成员数" width="100" align="center">
           <template #default="{ row }">
             <el-tag effect="plain" round>{{ row.memberCount }}人</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : (currentMode === 'audit' ? 'warning' : 'danger')" effect="dark" round>
              {{ row.status === 1 ? '已通过' : (currentMode === 'audit' ? '待审核' : '禁用') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="健康数据" width="180">
          <template #default="{ row }">
            <div class="health-stats-tags">
              <el-tag type="info" size="small" effect="plain">日志: {{ row.healthLogCount }}</el-tag>
              <el-tag type="warning" size="small" effect="plain" style="margin-left: 4px;">测评: {{ row.assessmentCount }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <!-- 审核模式下优先展示 审核相关操作 -->
            <template v-if="currentMode === 'audit'">
              <el-button size="small" type="success" round @click="approveFamily(row)">通过</el-button>
              <el-button size="small" type="danger" round @click="rejectFamily(row)">拒绝</el-button>
              <el-button size="small" circle @click="showDetailDialog(row)"><el-icon><View /></el-icon></el-button>
            </template>
            <!-- 成员管理模式下突出成员管理入口 -->
            <template v-else-if="currentMode === 'members'">
              <el-button size="small" type="primary" round @click="showMembers(row)">成员管理</el-button>
              <el-button size="small" circle @click="showDetailDialog(row)"><el-icon><View /></el-icon></el-button>
              <el-button size="small" circle @click="showHealthData(row)"><el-icon><DataAnalysis /></el-icon></el-button>
            </template>
            <!-- 其他模式保留完整家庭管理操作 -->
            <template v-else>
              <el-button size="small" text type="primary" @click="showEditDialog(row)">编辑</el-button>
              <el-button size="small" text @click="showDetailDialog(row)">详情</el-button>
              <el-dropdown trigger="click">
                <el-button size="small" text>
                  更多
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu class="glass-dropdown">
                    <el-dropdown-item @click="toggleStatus(row)">
                      <el-icon><component :is="row.status === 1 ? 'CircleClose' : 'CircleCheck'" /></el-icon>
                      {{ row.status === 1 ? '禁用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="showMembers(row)" divided><el-icon><User /></el-icon>成员管理</el-dropdown-item>
                    <el-dropdown-item @click="showHealthData(row)"><el-icon><DataAnalysis /></el-icon>健康数据</el-dropdown-item>
                    <el-dropdown-item @click="showInviteCode(row)"><el-icon><Key /></el-icon>邀请码</el-dropdown-item>
                    <el-dropdown-item @click="showFamilyReport(row)" divided><el-icon><Document /></el-icon>数据报告</el-dropdown-item>
                    <el-dropdown-item @click="handleDeleteFamily(row)" style="color: #f56c6c;"><el-icon><Delete /></el-icon>删除家庭</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
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
          background
        />
      </div>
    </div>

    <!-- 家庭编辑/创建对话框 -->
    <el-dialog 
      :title="dialog.title" 
      v-model="dialog.visible" 
      width="50%" 
      :before-close="handleDialogClose"
      class="glass-dialog"
    >
      <el-form 
        :model="dialog.form" 
        :rules="dialog.rules" 
        ref="dialogFormRef"
        label-width="100px"
        class="custom-form"
      >
        <el-form-item label="家庭名称" prop="name">
          <el-input v-model="dialog.form.name" placeholder="请输入家庭名称">
             <template #prefix><el-icon><House /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="所有者" prop="ownerId">
          <el-select 
            v-model="dialog.form.ownerId" 
            placeholder="请选择所有者" 
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            style="width: 100%"
          >
            <el-option 
              v-for="user in searchUsersResult" 
              :key="user.id" 
              :label="`${user.nickname} (${user.phone})`" 
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialog.form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="dialog.form.description" 
            type="textarea" 
            placeholder="请输入家庭描述" 
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogClose" round>取消</el-button>
        <el-button type="primary" @click="submitDialog" :loading="dialog.submitting" round v-particles>确定</el-button>
      </template>
    </el-dialog>

    <!-- 家庭详情对话框 -->
    <el-dialog title="家庭详情" v-model="detailDialog.visible" width="60%" class="glass-dialog">
      <el-descriptions :column="2" border class="glass-descriptions">
        <el-descriptions-item label="家庭ID">{{ detailDialog.family.id }}</el-descriptions-item>
        <el-descriptions-item label="家庭名称">{{ detailDialog.family.name }}</el-descriptions-item>
        <el-descriptions-item label="所有者">{{ detailDialog.family.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="成员数">{{ detailDialog.family.memberCount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailDialog.family.status === 1 ? 'success' : 'danger'" effect="dark">
            {{ detailDialog.family.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="健康日志数">{{ detailDialog.family.healthLogCount }}</el-descriptions-item>
        <el-descriptions-item label="体质测评数">{{ detailDialog.family.assessmentCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailDialog.family.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailDialog.family.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailDialog.family.description || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 成员管理对话框 -->
    <el-dialog title="成员管理" v-model="membersDialog.visible" width="70%" class="glass-dialog">
      <el-table :data="membersDialog.members" style="width: 100%" v-loading="membersDialog.loading" class="custom-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="昵称" width="120">
           <template #default="{ row }">
             <div class="user-cell">
               <el-avatar :size="24" class="mr-2">{{ (row.nickname || '').charAt(0) }}</el-avatar>
               <span>{{ row.nickname }}</span>
             </div>
           </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="relation" label="关系" width="100" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" effect="plain">
              {{ getRoleName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理员" width="100" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.admin" color="#67C23A"><Check /></el-icon>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" round @click="changeMemberRole(row)">修改角色</el-button>
            <el-button size="small" type="danger" round @click="removeMember(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="membersDialog.visible = false" round>关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Refresh, ArrowDown, House, Edit, View, 
  CircleCheck, CircleClose, User, Key, Search,
  DataLine, Check, DataAnalysis, Document, Delete
} from '@element-plus/icons-vue'
import { getFamilies, updateFamilyStatus, createFamily, updateFamily, deleteFamily } from '../../../api/admin'

const route = useRoute()
const pageTitle = ref('家庭管理')

// 搜索表单
const searchForm = reactive({
  familyId: '',
  name: '',
  owner: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 家庭列表
const families = ref([])
const loading = ref(false)
const selectedFamilies = ref([])
const familyStats = reactive({
  totalFamilies: 0,
  totalMembers: 0,
  avgMembers: 0,
  enabledFamilies: 0,
})

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '',
  type: '', // 'create' or 'edit'
  submitting: false,
  form: {
    id: null,
    name: '',
    ownerId: null,
    status: 1,
    description: ''
  },
  rules: {
    name: [
      { required: true, message: '请输入家庭名称', trigger: 'blur' },
      { min: 2, max: 20, message: '家庭名称长度在2-20个字符', trigger: 'blur' }
    ],
    ownerId: [
      { required: true, message: '请选择所有者', trigger: 'change' }
    ]
  }
})

// 详情对话框
const detailDialog = reactive({
  visible: false,
  family: {}
})

// 成员管理对话框
const membersDialog = reactive({
  visible: false,
  familyId: null,
  members: [],
  loading: false
})

// 用户搜索
const userSearchLoading = ref(false)
const searchUsersResult = ref([])

// 表单引用
const dialogFormRef = ref(null)

// 根据路由模式设置默认筛选/标题
const currentMode = computed(() => {
  const path = route.path || ''
  if (path.includes('/audit')) return 'audit'
  if (path.includes('/members')) return 'members'
  if (path.includes('/stats')) return 'stats'
  return 'list'
})

const applyModeDefaults = () => {
  switch (currentMode.value) {
    case 'audit':
      pageTitle.value = '家庭审核'
      if (searchForm.status === '') searchForm.status = 0
      break
    case 'members':
      pageTitle.value = '成员管理'
      break
    case 'stats':
      pageTitle.value = '数据统计'
      break
    default:
      pageTitle.value = '家庭管理'
      break
  }
}

// 加载家庭数据
onMounted(() => {
  applyModeDefaults()
  loadFamilies()
})

watch(
  () => route.path,
  () => {
    applyModeDefaults()
    pagination.page = 1
    loadFamilies()
  }
)

// 加载家庭列表（兼容后端 Result<T> 包装结构）
const loadFamilies = async () => {
  loading.value = true
  try {
    const keyword = [searchForm.name, searchForm.owner, searchForm.familyId].find(Boolean)
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword,
      status: searchForm.status === '' ? undefined : searchForm.status
    }

    // 后端返回结构为 Result<{ list, total, page, size, pages }>
    const res = await getFamilies(params)
    const data = res && typeof res === 'object' ? res.data || {} : {}

    // 兼容字段名：优先使用 list，其次 items
    const list = Array.isArray(data.list)
      ? data.list
      : Array.isArray(data.items)
        ? data.items
        : []

    families.value = list
    pagination.total = Number(data.total) || list.length || 0

    // 统计当前结果的家庭数据
    familyStats.totalFamilies = list.length
    familyStats.totalMembers = list.reduce((sum, f) => sum + (Number(f.memberCount) || 0), 0)
    familyStats.avgMembers = familyStats.totalFamilies
      ? (familyStats.totalMembers / familyStats.totalFamilies).toFixed(1)
      : 0
    familyStats.enabledFamilies = list.filter(f => f.status === 1).length
  } catch (error) {
    ElMessage.error('加载家庭列表失败')
    console.error('Error loading families:', error)
  } finally {
    loading.value = false
  }
}

// 搜索家庭
const searchFamilies = () => {
  pagination.page = 1
  loadFamilies()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  loadFamilies()
}

// 刷新列表
const refreshList = () => {
  loadFamilies()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  loadFamilies()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadFamilies()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedFamilies.value = selection
}

// 显示创建对话框
const showCreateDialog = () => {
  dialog.type = 'create'
  dialog.title = '新增家庭'
  dialog.form.id = null
  dialog.form.name = ''
  dialog.form.ownerId = null
  dialog.form.status = 1
  dialog.form.description = ''
  dialog.visible = true
}

// 显示编辑对话框
const showEditDialog = (family) => {
  dialog.type = 'edit'
  dialog.title = '编辑家庭'
  dialog.form.id = family.id
  dialog.form.name = family.name
  // 这里需要获取所有者ID，目前模拟
  dialog.form.ownerId = 1
  dialog.form.status = family.status
  dialog.form.description = family.description
  dialog.visible = true
}

// 显示详情对话框
const showDetailDialog = (family) => {
  detailDialog.family = { ...family }
  detailDialog.visible = true
}

// 提交对话框
const submitDialog = async () => {
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      dialog.submitting = true
      try {
        if (dialog.type === 'create') {
          await createFamily(dialog.form)
          ElMessage.success('家庭创建成功')
        } else {
          await updateFamily(dialog.form.id, dialog.form)
          ElMessage.success('家庭更新成功')
        }
        dialog.visible = false
        loadFamilies()
      } catch (error) {
        ElMessage.error(dialog.type === 'create' ? '创建家庭失败' : '更新家庭失败')
        console.error('Error submitting family:', error)
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

// 切换家庭状态
const toggleStatus = async (family) => {
  try {
    await ElMessageBox.confirm(
      `确定要${family.status === 1 ? '禁用' : '启用'}家庭 ${family.name} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateFamilyStatus(family.id, family.status === 1 ? 0 : 1)
    family.status = family.status === 1 ? 0 : 1
    ElMessage.success('家庭状态更新成功')
  } catch {
    // 用户取消操作
  }
}

// 显示成员管理
const showMembers = async (family) => {
  membersDialog.familyId = family.id
  membersDialog.loading = true
  try {
    // 这里应该调用实际的API来获取家庭成员
    // 目前使用模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    membersDialog.members = [
      { id: 1, nickname: '张三', phone: '13800138001', relation: '本人', role: 'FAMILY_ADMIN', admin: true },
      { id: 2, nickname: '张妻', phone: '13800138002', relation: '配偶', role: 'MEMBER', admin: false },
      { id: 3, nickname: '张子', phone: '13800138003', relation: '子女', role: 'MEMBER', admin: false },
      { id: 4, nickname: '张母', phone: '13800138004', relation: '母亲', role: 'VIEWER', admin: false }
    ]
  } catch (error) {
    ElMessage.error('获取家庭成员失败')
  } finally {
    membersDialog.loading = false
  }
  membersDialog.visible = true
}

// 修改成员角色
const changeMemberRole = (member) => {
  ElMessageBox.prompt(
    '请输入新角色:',
    '修改角色',
    {
      inputValue: member.role,
      inputPattern: /^(ADMIN|FAMILY_ADMIN|MEMBER|VIEWER)$/,
      inputErrorMessage: '角色不正确'
    }
  ).then(({ value }) => {
    member.role = value
    ElMessage.success('角色修改成功')
  }).catch(() => {
    // 用户取消操作
  })
}

// 移除成员
const removeMember = async (member) => {
  try {
    await ElMessageBox.confirm(
      `确定要将成员 ${member.nickname} 从家庭中移除吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里应该调用实际的API来移除成员
    // 目前使用模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const index = membersDialog.members.findIndex(m => m.id === member.id)
    if (index > -1) {
      membersDialog.members.splice(index, 1)
    }
    ElMessage.success('成员移除成功')
  } catch {
    // 用户取消操作
  }
}

// 显示健康数据
const showHealthData = (family) => {
  ElMessage.info(`查看家庭 ${family.name} 的健康数据`)
  // 这里可以跳转到健康数据页面
}

// 显示邀请码
const showInviteCode = (family) => {
  ElMessageBox.alert(
    `家庭 ${family.name} 的邀请码为: <strong>${'ABC123'}</strong>`,
    '邀请码',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '复制',
      callback: action => {
        if (action === 'confirm') {
          // 复制到剪贴板的逻辑
          navigator.clipboard.writeText('ABC123')
          ElMessage.success('邀请码已复制到剪贴板')
        }
      }
    }
  )
}

// 显示家庭报告
const showFamilyReport = (family) => {
  ElMessage.info(`生成家庭 ${family.name} 的数据报告`)
  // 这里可以跳转到报告页面
}

// 审核通过家庭（仅在审核模式使用）
const approveFamily = async (family) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过家庭 ${family.name}（ID: ${family.id}）的审核并启用吗？`,
      '通过审核',
      {
        confirmButtonText: '通过',
        cancelButtonText: '取消',
        type: 'success',
      },
    )

    await updateFamilyStatus(family.id, 1)
    family.status = 1
    ElMessage.success('家庭审核通过，已启用')
  } catch {
    // 用户取消
  }
}

// 审核拒绝家庭（仅在审核模式使用）
const rejectFamily = async (family) => {
  try {
    await ElMessageBox.confirm(
      `确定要拒绝家庭 ${family.name}（ID: ${family.id}）的审核并保持禁用状态吗？`,
      '拒绝审核',
      {
        confirmButtonText: '拒绝',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    await updateFamilyStatus(family.id, 0)
    family.status = 0
    ElMessage.success('已拒绝该家庭审核并保持禁用状态')
  } catch {
    // 用户取消
  }
}

// 搜索用户
const searchUsers = async (query) => {
  if (query) {
    userSearchLoading.value = true
    try {
      // 这里应该调用实际的API来搜索用户
      // 目前使用模拟数据
      await new Promise(resolve => setTimeout(resolve, 500))
      
      searchUsersResult.value = [
        { id: 1, nickname: '张三', phone: '13800138001' },
        { id: 2, nickname: '李四', phone: '13800138002' },
        { id: 3, nickname: '王五', phone: '13800138003' }
      ]
    } catch (error) {
      ElMessage.error('搜索用户失败')
    } finally {
      userSearchLoading.value = false
    }
  } else {
    searchUsersResult.value = []
  }
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'FAMILY_ADMIN': return 'warning'
    case 'DOCTOR': return 'primary'
    case 'MEMBER': return 'success'
    case 'VIEWER': return 'info'
    default: return 'info'
  }
}

// 获取角色名称
const getRoleName = (role) => {
  const roleMap = {
    ADMIN: '管理员',
    FAMILY_ADMIN: '家庭管理员',
    MEMBER: '普通成员',
    VIEWER: '访客',
    DOCTOR: '医生'
  }
  return roleMap[role] || role
}

// 删除家庭
const handleDeleteFamily = async (family) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除家庭 ${family.name}（ID: ${family.id}）吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'danger',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await deleteFamily(family.id)
    ElMessage.success('家庭已删除')
    loadFamilies() // 重新加载列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Error deleting family:', error)
      ElMessage.error('删除家庭失败')
    }
  }
}
</script>

<style scoped lang="scss">
@use "sass:map";
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.family-management {
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
        @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info')));
        letter-spacing: -0.5px;
      }

      .page-subtitle {
        font-size: 14px;
        color: map.get(vars.$colors, 'text-secondary');
        margin: 0;
      }
    }
  }

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

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      font-size: 18px;
      font-weight: 700;
      color: map.get(vars.$colors, 'text-main');
    }
  }

  .family-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      border: 1px solid rgba(255, 255, 255, 0.6);

      .stat-icon-wrapper {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        margin-right: 16px;
        
        &.blue { background: rgba(map.get(vars.$colors, 'info'), 0.1); color: map.get(vars.$colors, 'info'); }
        &.green { background: rgba(map.get(vars.$colors, 'success'), 0.1); color: map.get(vars.$colors, 'success'); }
        &.orange { background: rgba(map.get(vars.$colors, 'warning'), 0.1); color: map.get(vars.$colors, 'warning'); }
        &.purple { background: rgba(map.get(vars.$colors, 'primary'), 0.1); color: map.get(vars.$colors, 'primary'); }
      }

      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: 700;
          line-height: 1.2;
          margin-bottom: 4px;
          color: map.get(vars.$colors, 'text-main');
        }
        .stat-title {
          font-size: 12px;
          color: map.get(vars.$colors, 'text-secondary');
        }
      }
    }
  }

  .custom-table {
    background: transparent !important;
    
    :deep(th.el-table__cell) {
      background-color: rgba(255, 255, 255, 0.5);
      font-weight: 600;
      color: map.get(vars.$colors, 'text-main');
    }
    
    :deep(tr) {
      background-color: transparent;
      &:hover td.el-table__cell {
        background-color: rgba(64, 158, 255, 0.05) !important;
      }
    }

    .family-name {
      font-weight: 600;
      color: map.get(vars.$colors, 'primary');
    }

    .owner-info {
      display: flex;
      align-items: center;
    }

    .health-stats-tags {
      display: flex;
      align-items: center;
    }
    
    .user-cell {
      display: flex;
      align-items: center;
    }
  }

  .stagger-anim {
    opacity: 0;
    animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    animation-delay: var(--delay, 0s);
  }

  @keyframes slideUpFade {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // Responsive
  @media (max-width: 1024px) {
    .family-stats {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  @media (max-width: 768px) {
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .family-stats {
      grid-template-columns: 1fr;
    }
  }
}

:deep(.glass-dialog) {
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  
  .el-dialog__header {
    margin-right: 0;
    padding: 20px 24px;
    border-bottom: 1px solid rgba(0,0,0,0.05);
    
    .el-dialog__title {
      font-weight: 700;
    }
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    padding: 20px 24px;
    border-top: 1px solid rgba(0,0,0,0.05);
    background-color: rgba(250, 250, 250, 0.5);
  }
}

:deep(.glass-dropdown) {
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.6) !important;
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.1) !important;
}
</style>
