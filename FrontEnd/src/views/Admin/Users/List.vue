<template>
  <div class="user-management">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">{{ pageTitle }}</h1>
        <p class="page-subtitle">管理系统用户信息与权限</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" round v-particles @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="glass-card stagger-anim mb-24" style="--delay: 0.2s">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable>
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable>
            <template #prefix><el-icon><Iphone /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <template #prefix><el-icon><UserFilled /></el-icon></template>
            <el-option label="ADMIN" value="ADMIN" />
            <el-option label="FAMILY_ADMIN" value="FAMILY_ADMIN" />
            <el-option label="MEMBER" value="MEMBER" />
            <el-option label="VIEWER" value="VIEWER" />
            <el-option label="DOCTOR" value="DOCTOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <template #prefix><el-icon><CircleCheck /></el-icon></template>
            <el-option label="启用" :value="1" />
            <el-option label="待审核" :value="2" />
            <el-option label="已拒绝" :value="3" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUsers" v-particles>搜索</el-button>
          <el-button @click="resetSearch" class="reset-btn">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 角色统计（仅角色管理模式显示） -->
    <div v-if="currentMode === 'roles'" class="role-stats stagger-anim mb-24" style="--delay: 0.3s">
      <div class="stat-card glass-card">
        <div class="stat-icon admin"><el-icon><Management /></el-icon></div>
        <div class="stat-info">
          <div class="stat-title">管理员</div>
          <div class="stat-value">{{ roleStats.ADMIN }}</div>
        </div>
      </div>
      <div class="stat-card glass-card">
        <div class="stat-icon family"><el-icon><House /></el-icon></div>
        <div class="stat-info">
          <div class="stat-title">家庭管理员</div>
          <div class="stat-value">{{ roleStats.FAMILY_ADMIN }}</div>
        </div>
      </div>
      <div class="stat-card glass-card">
        <div class="stat-icon doctor"><el-icon><FirstAidKit /></el-icon></div>
        <div class="stat-info">
          <div class="stat-title">医生</div>
          <div class="stat-value">{{ roleStats.DOCTOR }}</div>
        </div>
      </div>
      <div class="stat-card glass-card">
        <div class="stat-icon member"><el-icon><User /></el-icon></div>
        <div class="stat-info">
          <div class="stat-title">普通成员</div>
          <div class="stat-value">{{ roleStats.MEMBER }}</div>
        </div>
      </div>
      <div class="stat-card glass-card">
        <div class="stat-icon viewer"><el-icon><View /></el-icon></div>
        <div class="stat-info">
          <div class="stat-title">访客</div>
          <div class="stat-value">{{ roleStats.VIEWER }}</div>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="glass-card stagger-anim" style="--delay: 0.4s">
      <div class="card-header">
        <div class="header-left">
          <div class="icon-box primary">
            <el-icon><List /></el-icon>
          </div>
          <span class="title">用户列表</span>
        </div>
        <div class="header-actions">
          <el-button @click="refreshList" :loading="loading" plain round>
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <el-table 
        :data="users" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="nickname" label="昵称" width="120">
          <template #default="{ row }">
            <span class="nickname-cell">{{ row.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="160">
          <template #default="{ row }">
            <!-- 角色管理模式下支持直接修改角色 -->
            <template v-if="currentMode === 'roles'">
              <el-select
                v-model="row.role"
                size="small"
                style="width: 130px"
                @change="val => changeUserRole(row, val)"
                class="role-select"
              >
                <el-option label="管理员" value="ADMIN" />
                <el-option label="家庭管理员" value="FAMILY_ADMIN" />
                <el-option label="普通成员" value="MEMBER" />
                <el-option label="访客" value="VIEWER" />
                <el-option label="医生" value="DOCTOR" />
              </el-select>
            </template>
            <template v-else>
              <el-tag :type="getRoleTagType(row.role)" effect="light" round>
                {{ getRoleName(row.role) }}
              </el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <div class="status-cell">
              <div class="status-dot" :class="getStatusClass(row.status)"></div>
              <span>{{ getStatusLabel(row.status) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="160" />
        <el-table-column prop="createdAt" label="注册时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <!-- 审核模式下优先展示 审核相关操作 -->
            <div class="action-buttons">
              <template v-if="currentMode === 'audit'">
                <el-button size="small" type="success" plain @click="approveUser(row)">通过</el-button>
                <el-button size="small" type="danger" plain @click="rejectUser(row)">拒绝</el-button>
                <el-button size="small" text @click="showDetailDialog(row)">详情</el-button>
              </template>
              <!-- 其他模式使用完整的用户管理操作 -->
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
                      <el-dropdown-item v-if="row.status !== 0" @click="handleLockUser(row)">
                        <el-icon><Lock /></el-icon>锁定用户
                      </el-dropdown-item>
                      <el-dropdown-item v-else @click="handleUnlockUser(row)">
                        <el-icon><Unlock /></el-icon>解锁用户
                      </el-dropdown-item>
                      <el-dropdown-item @click="handleForceLogout(row)">
                        <el-icon><SwitchButton /></el-icon>强制下线
                      </el-dropdown-item>
                      <el-dropdown-item @click="resetPassword(row)">重置密码</el-dropdown-item>
                      <el-dropdown-item @click="showHealthProfile(row)" divided>健康档案</el-dropdown-item>
                      <el-dropdown-item @click="showFamilyRelation(row)">家庭关系</el-dropdown-item>
                      <el-dropdown-item @click="handleDeleteUser(row)" divided style="color: #f56c6c;">
                        <el-icon><Delete /></el-icon>删除用户
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
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

    <!-- 用户编辑/创建对话框 -->
    <el-dialog 
      :title="dialog.title" 
      v-model="dialog.visible" 
      width="50%" 
      :before-close="handleDialogClose"
      class="glass-dialog"
      align-center
    >
      <el-form 
        :model="dialog.form" 
        :rules="dialog.rules" 
        ref="dialogFormRef"
        label-width="100px"
        class="custom-form"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="dialog.form.nickname" placeholder="请输入昵称">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="dialog.form.phone" placeholder="请输入手机号">
            <template #prefix><el-icon><Iphone /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="dialog.form.role" placeholder="请选择角色" style="width: 100%">
            <template #prefix><el-icon><UserFilled /></el-icon></template>
            <el-option label="ADMIN" value="ADMIN" />
            <el-option label="FAMILY_ADMIN" value="FAMILY_ADMIN" />
            <el-option label="MEMBER" value="MEMBER" />
            <el-option label="VIEWER" value="VIEWER" />
            <el-option label="DOCTOR" value="DOCTOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialog.form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">待审核</el-radio>
            <el-radio :label="3">已拒绝</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="密码" v-if="dialog.type === 'create'" prop="password">
          <el-input 
            v-model="dialog.form.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
          >
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="submitDialog" :loading="dialog.submitting" class="submit-btn" v-particles>确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog title="用户详情" v-model="detailDialog.visible" width="60%" class="glass-dialog" align-center>
      <el-descriptions :column="2" border class="custom-descriptions">
        <el-descriptions-item label="用户ID">{{ detailDialog.user.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailDialog.user.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailDialog.user.phone }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="getRoleTagType(detailDialog.user.role)" effect="light" round>
            {{ getRoleName(detailDialog.user.role) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(detailDialog.user.status)" effect="dark" round>
            {{ getStatusLabel(detailDialog.user.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ detailDialog.user.lastLoginAt || '从未登录' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detailDialog.user.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailDialog.user.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Refresh, ArrowDown, User, Edit, View, 
  CircleCheck, CircleClose, Lock, Key, Iphone, UserFilled,
  Management, House, FirstAidKit, List, SwitchButton, Unlock, Delete
} from '@element-plus/icons-vue'
import { 
  getUsers, updateUserStatus, resetUserPassword, createUser, updateUser,
  forceLogout, lockUser, unlockUser, approveUser as apiApproveUser, rejectUser as apiRejectUser, deleteUser
} from '../../../api/admin'

const route = useRoute()
const pageTitle = ref('用户管理')

// 搜索表单
const searchForm = reactive({
  userId: '',
  nickname: '',
  phone: '',
  role: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 用户列表
const users = ref([])
const loading = ref(false)
const selectedUsers = ref([])
const roleStats = reactive({
  ADMIN: 0,
  FAMILY_ADMIN: 0,
  DOCTOR: 0,
  MEMBER: 0,
  VIEWER: 0,
})

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '',
  type: '', // 'create' or 'edit'
  submitting: false,
  form: {
    id: null,
    nickname: '',
    phone: '',
    role: 'MEMBER',
    status: 1,
    password: ''
  },
  rules: {
    nickname: [
      { required: true, message: '请输入昵称', trigger: 'blur' },
      { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
    ],
    role: [
      { required: true, message: '请选择角色', trigger: 'change' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
    ]
  }
})

// 详情对话框
const detailDialog = reactive({
  visible: false,
  user: {}
})

// 表单引用
const dialogFormRef = ref(null)

// 根据路由模式设置默认筛选/标题
const currentMode = computed(() => {
  const path = route.path || ''
  if (path.includes('/audit')) return 'audit'
  if (path.includes('/roles')) return 'roles'
  if (path.includes('/logs')) return 'logs'
  return 'list'
})

const applyModeDefaults = () => {
  switch (currentMode.value) {
    case 'audit':
      pageTitle.value = '用户审核'
      if (searchForm.status === '') searchForm.status = 2
      break
    case 'roles':
      pageTitle.value = '角色管理'
      break
    case 'logs':
      pageTitle.value = '活动日志'
      break
    default:
      pageTitle.value = '用户管理'
      break
  }
}

// 加载用户数据
onMounted(() => {
  applyModeDefaults()
  loadUsers()
})

watch(
  () => route.path,
  () => {
    applyModeDefaults()
    pagination.page = 1
    loadUsers()
  }
)

// 加载用户列表（兼容后端 Result<T> 包装结构）
const loadUsers = async () => {
  loading.value = true
  try {
    // 将前端搜索字段映射到后端接口 (keyword/role/status)
    const keyword = [searchForm.nickname, searchForm.phone, searchForm.userId].find(Boolean)
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword,
      role: searchForm.role || undefined,
      status: searchForm.status === '' ? undefined : searchForm.status
    }

    // 后端返回结构为 Result<{ list, total, page, size, pages }>
    const res = await getUsers(params)
    // 兼容不同的响应结构：处理 res.data 可能不存在或直接返回数据的情况
    const responseData = res && res.data ? res.data : res
    const data = responseData || {}

    // 兼容字段名：优先使用 list，其次 items，或者直接是数组
    const list = Array.isArray(data.list)
      ? data.list
      : Array.isArray(data.items)
        ? data.items
        : Array.isArray(data)
          ? data
          : []

    users.value = list

    // 重新统计当前结果中的角色分布
    Object.keys(roleStats).forEach(key => { roleStats[key] = 0 })
    list.forEach((u) => {
      const r = u.role
      if (r && roleStats[r] !== undefined) {
        roleStats[r] += 1
      }
    })
    pagination.total = Number(data.total) || list.length || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error('Error loading users:', error)
    users.value = [] // 出错时清空列表
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索用户
const searchUsers = () => {
  pagination.page = 1
  loadUsers()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    if (typeof searchForm[key] === 'string') {
      searchForm[key] = ''
    } else if (typeof searchForm[key] === 'number') {
      searchForm[key] = 0
    } else {
      searchForm[key] = ''
    }
  })
  pagination.page = 1
  loadUsers()
}

// 刷新列表
const refreshList = () => {
  loadUsers()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  loadUsers()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadUsers()
}

// 选择变化
const handleSelectionChange = (selection) => {
  // 确保 selection 是数组类型
  selectedUsers.value = Array.isArray(selection) ? selection : []
}

// 显示创建对话框
const showCreateDialog = () => {
  dialog.type = 'create'
  dialog.title = '新增用户'
  dialog.form.id = null
  dialog.form.nickname = ''
  dialog.form.phone = ''
  dialog.form.role = 'MEMBER'
  dialog.form.status = 1
  dialog.form.password = ''
  dialog.visible = true
}

// 显示编辑对话框
const showEditDialog = (user) => {
  dialog.type = 'edit'
  dialog.title = '编辑用户'
  dialog.form.id = user.id
  dialog.form.nickname = user.nickname
  dialog.form.phone = user.phone
  dialog.form.role = user.role
  dialog.form.status = user.status
  dialog.form.password = ''
  dialog.visible = true
}

// 显示详情对话框
const showDetailDialog = (user) => {
  detailDialog.user = user && typeof user === 'object' ? { ...user } : {}
  detailDialog.visible = true
}

// 提交对话框
const submitDialog = async () => {
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      dialog.submitting = true
      try {
        if (dialog.type === 'create') {
          await createUser(dialog.form)
          ElMessage.success('用户创建成功')
        } else {
          await updateUser(dialog.form.id, dialog.form)
          ElMessage.success('用户更新成功')
        }
        dialog.visible = false
        loadUsers()
      } catch (error) {
        ElMessage.error(dialog.type === 'create' ? '创建用户失败' : '更新用户失败')
        console.error('Error submitting user:', error)
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

// 切换用户状态
const toggleStatus = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要${user.status === 1 ? '禁用' : '启用'}用户 ${user.nickname} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateUserStatus(user.id, user.status === 1 ? 0 : 1)
    user.status = user.status === 1 ? 0 : 1
    ElMessage.success('用户状态更新成功')
  } catch {
    // 用户取消操作
  }
}

// 锁定用户
const handleLockUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要锁定用户 ${user.nickname} 吗？锁定后用户将无法登录。`,
      '锁定确认',
      { confirmButtonText: '锁定', cancelButtonText: '取消', type: 'warning' }
    )
    await lockUser(user.id)
    user.status = 0
    ElMessage.success('用户已锁定')
  } catch {}
}

// 解锁用户
const handleUnlockUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要解锁用户 ${user.nickname} 吗？`,
      '解锁确认',
      { confirmButtonText: '解锁', cancelButtonText: '取消', type: 'success' }
    )
    await unlockUser(user.id)
    user.status = 1
    ElMessage.success('用户已解锁')
  } catch {}
}

// 强制下线
const handleForceLogout = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要强制用户 ${user.nickname} 下线吗？`,
      '下线确认',
      { confirmButtonText: '强制下线', cancelButtonText: '取消', type: 'danger' }
    )
    await forceLogout(user.id)
    ElMessage.success('用户已强制下线')
  } catch {}
}

// 重置密码
const resetPassword = async (user) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `请输入新密码：`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^.{6,20}$/,
        inputErrorMessage: '密码长度在6-20个字符'
      }
    )
    
    await resetUserPassword(user.id, value)
    ElMessage.success('密码重置成功')
  } catch {
    // 用户取消操作
  }
}

// 显示健康档案
const showHealthProfile = (user) => {
  ElMessage.info(`查看用户 ${user.nickname} 的健康档案`)
  // 这里可以跳转到健康档案页面
}

// 显示家庭关系
const showFamilyRelation = (user) => {
  ElMessage.info(`查看用户 ${user.nickname} 的家庭关系`)
  // 这里可以跳转到家庭关系页面
}

// 修改用户角色（仅角色管理模式下使用）
const changeUserRole = async (user, newRole) => {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 ${user.nickname}（ID: ${user.id}）的角色调整为 ${getRoleName(newRole)} 吗？`,
      '调整角色',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    // 仅提交必要字段，避免覆盖其他信息
    await updateUser(user.id, {
      id: user.id,
      nickname: user.nickname,
      phone: user.phone,
      role: newRole,
      status: user.status,
    })

    ElMessage.success('用户角色已更新')
    // 更新统计信息
    await loadUsers()
  } catch {
    // 用户取消或失败，重新加载以恢复原值
    await loadUsers()
  }
}

// 审核通过用户（仅在审核模式使用）
const approveUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要通过用户 ${user.nickname}（ID: ${user.id}）的审核并启用账号吗？`,
      '通过审核',
      {
        confirmButtonText: '通过',
        cancelButtonText: '取消',
        type: 'success',
      },
    )

    await apiApproveUser(user.id)
    ElMessage.success('用户审核通过，已启用账号')
    await loadUsers()
  } catch {
    // 用户取消
  }
}

// 审核拒绝用户（仅在审核模式使用）
const rejectUser = async (user) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `请输入拒绝原因（可选）：`,
      `拒绝审核：${user.nickname}（ID: ${user.id}）`,
      {
        confirmButtonText: '拒绝',
        cancelButtonText: '取消',
        inputPlaceholder: '例如：资料不完整/信息不匹配',
        inputType: 'textarea',
      },
    )

    await apiRejectUser(user.id, value || '')
    ElMessage.success('已拒绝该用户审核')
    await loadUsers()
  } catch {
    // 用户取消
  }
}

// 删除用户
const handleDeleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.nickname}（ID: ${user.id}）吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'danger',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await deleteUser(user.id)
    ElMessage.success('用户已删除')
    loadUsers() // 重新加载列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Error deleting user:', error)
      ElMessage.error('删除用户失败')
    }
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

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger' // 已拒绝也用红色
    case 0: return 'danger'
    default: return 'info'
  }
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  switch (status) {
    case 1: return '启用'
    case 2: return '待审核'
    case 3: return '已拒绝'
    case 0: return '禁用'
    default: return '未知'
  }
}

// 获取状态Tag类型
const getStatusTagType = (status) => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    case 0: return 'info'
    default: return 'info'
  }
}
</script>

<style scoped lang="scss">
@use 'sass:map';
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.user-management {
  padding: 24px;
  min-height: 100%;

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
        color: map.get(vars.$colors, 'text-secondary');
        margin: 0;
        font-size: 14px;
      }
    }
  }

  .mb-24 {
    margin-bottom: 24px;
  }

  .role-stats {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 20px;

    .stat-card {
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      transition: all 0.3s vars.$ease-spring;
      
      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        
        &.admin { background: rgba(map.get(vars.$colors, 'danger'), 0.1); color: map.get(vars.$colors, 'danger'); }
        &.family { background: rgba(map.get(vars.$colors, 'warning'), 0.1); color: map.get(vars.$colors, 'warning'); }
        &.doctor { background: rgba(map.get(vars.$colors, 'primary'), 0.1); color: map.get(vars.$colors, 'primary'); }
        &.member { background: rgba(map.get(vars.$colors, 'success'), 0.1); color: map.get(vars.$colors, 'success'); }
        &.viewer { background: rgba(map.get(vars.$colors, 'info'), 0.1); color: map.get(vars.$colors, 'info'); }
      }

      .stat-info {
        .stat-title {
          font-size: 13px;
          color: map.get(vars.$colors, 'text-secondary');
          margin-bottom: 4px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: map.get(vars.$colors, 'text-main');
        }
      }

      &:hover {
        transform: translateY(-4px);
        box-shadow: vars.$shadow-lg;
      }
    }
  }

  .glass-card {
    @include mixins.glass-effect;
    border-radius: vars.$radius-lg;
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: vars.$shadow-lg;
    padding: 24px;
    
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
          color: map.get(vars.$colors, 'text-main');
        }
      }
    }
  }

  .custom-table {
    background: transparent;
    
    :deep(tr), :deep(th.el-table__cell) {
      background: transparent;
    }
    
    :deep(.el-table__row:hover) {
      background-color: rgba(map.get(vars.$colors, 'primary'), 0.05) !important;
    }

    .nickname-cell {
      font-weight: 600;
      color: map.get(vars.$colors, 'text-main');
    }

    .status-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        
        &.success { background: map.get(vars.$colors, 'success'); }
        &.warning { background: map.get(vars.$colors, 'warning'); }
        &.danger { background: map.get(vars.$colors, 'danger'); }
        &.info { background: map.get(vars.$colors, 'info'); }
      }
    }

    .action-buttons {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .pagination-wrapper {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
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

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    
    .cancel-btn {
      border-radius: vars.$radius-md;
      &:hover {
        background: rgba(map.get(vars.$colors, 'text-main'), 0.05);
      }
    }
    
    .submit-btn {
      border-radius: vars.$radius-md;
      padding: 10px 24px;
    }
  }

  // Responsive adjustments
  @media (max-width: 768px) {
    padding: 16px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      
      .header-actions {
        width: 100%;
        .el-button { width: 100%; }
      }
    }

    .search-form {
      .el-form-item {
        margin-right: 0;
        width: 100%;
        
        :deep(.el-form-item__content) {
          width: 100%;
        }
      }
    }
  }
}

.glass-dialog {
  :deep(.el-dialog) {
    @include mixins.glass-effect;
    border-radius: vars.$radius-lg;
    box-shadow: vars.$shadow-2xl;

    .el-dialog__header {
      border-bottom: 1px solid rgba(map.get(vars.$colors, 'text-main'), 0.05);
      margin-right: 0;
      padding: 20px;
    }

    .el-dialog__footer {
      border-top: 1px solid rgba(map.get(vars.$colors, 'text-main'), 0.05);
      padding: 20px;
    }
  }
}
</style>
