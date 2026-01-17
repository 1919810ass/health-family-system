<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title-text">家庭管理</h2>
      <p class="subtitle-text">管理您的家庭成员和健康数据权限</p>
    </div>

    <!-- Actions Bar -->
    <div class="actions">
      <el-select
        v-model="currentFamilyId"
        placeholder="选择当前家庭"
        class="family-select"
        @change="handleFamilyChange"
      >
        <el-option
          v-for="item in familyStore.families"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>

      <el-button type="primary" class="action-btn" @click="showCreateDialog = true">
        <el-icon class="mr-2"><Plus /></el-icon>创建家庭
      </el-button>
      
      <el-button class="action-btn glass-btn" @click="showJoinDialog = true">
        <el-icon class="mr-2"><Connection /></el-icon>加入家庭
      </el-button>
    </div>

    <!-- Main Content -->
    <div v-if="currentFamily" class="content-grid">
      <!-- Family Info Card -->
      <div class="section-card glass-card family-info">
        <div class="card-header">
          <h3>{{ currentFamily.name }}</h3>
          <div class="invite-box" v-if="isAdmin">
            <span class="label">邀请码:</span>
            <span class="code-tag">{{ currentFamily.inviteCode }}</span>
            <el-button link type="primary" @click="copyInviteCode">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </div>
        </div>
        
        <div class="members-grid">
          <div 
            v-for="member in members" 
            :key="member.id"
            class="member-card glass-panel"
          >
            <el-avatar :size="64" :src="member.avatar || ''" class="member-avatar">
              {{ member.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="member-info">
              <h4>{{ member.nickname }}</h4>
              <div class="tags">
                <el-tag size="small" :type="member.role === 'ADMIN' ? 'warning' : 'info'">
                  {{ member.relation || '成员' }}
                </el-tag>
              </div>
            </div>
            <div class="member-actions" v-if="isAdmin && member.userId !== userStore.profile?.id">
              <el-popconfirm title="确定移除该成员吗？" @confirm="handleRemoveMember(member)">
                <template #reference>
                  <el-button link type="danger" size="small">移除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
      </div>

      <!-- Family Doctor Card -->
      <div class="section-card glass-card doctor-info mt-6">
        <div class="card-header">
          <h3><el-icon class="mr-2 icon-align"><FirstAidKit /></el-icon>家庭医生</h3>
          <el-button v-if="!currentDoctor && isAdmin" type="primary" plain size="small" @click="showBindDoctorDialog = true">
            签约医生
          </el-button>
        </div>
        
        <div v-if="currentDoctor" class="doctor-card">
          <el-avatar :size="80" :src="currentDoctor.avatar || ''" class="doctor-avatar">
            {{ currentDoctor.nickname?.charAt(0) || 'D' }}
          </el-avatar>
          <div class="doctor-detail">
            <div class="name-row">
              <h4>{{ currentDoctor.nickname }}</h4>
              <el-tag v-if="currentDoctor.title" size="small" effect="plain">{{ currentDoctor.title }}</el-tag>
            </div>
            <p class="hospital" v-if="currentDoctor.hospital">{{ currentDoctor.hospital }} · {{ currentDoctor.department }}</p>
            <p class="bio" v-if="currentDoctor.bio">{{ currentDoctor.bio }}</p>
            <div class="stats-row">
              <span class="stat-item">评分: {{ currentDoctor.rating || '5.0' }}</span>
              <span class="stat-item">服务数: {{ currentDoctor.serviceCount || 0 }}</span>
            </div>
          </div>
          <div class="doctor-actions" v-if="isAdmin">
             <el-button type="danger" plain size="small" @click="handleUnbindDoctor">解约</el-button>
          </div>
        </div>
        
        <div v-else class="empty-doctor">
          <p>暂无签约家庭医生</p>
          <p class="sub-hint" v-if="isAdmin">点击右上角"签约医生"按钮，输入医生邀请码即可绑定</p>
        </div>
      </div>
    </div>

    <el-empty v-else description="暂无家庭，请创建或加入" class="glass-card" />

    <!-- Dialogs -->
    <el-dialog v-model="showCreateDialog" title="创建家庭" width="400px" class="glass-dialog">
      <el-form :model="createForm" ref="createFormRef" :rules="rules">
        <el-form-item label="家庭名称" prop="name">
          <el-input v-model="createForm.name" placeholder="例如：幸福一家人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleCreate">创建</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showJoinDialog" title="加入家庭" width="400px" class="glass-dialog">
      <el-form :model="joinForm" ref="joinFormRef" :rules="rules">
        <el-form-item label="邀请码" prop="code">
          <el-input v-model="joinForm.code" placeholder="请输入8位邀请码" maxlength="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showJoinDialog = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleJoin">加入</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="showBindDoctorDialog" title="签约家庭医生" width="400px" class="glass-dialog">
      <el-form :model="bindDoctorForm" ref="bindDoctorFormRef" :rules="rules">
        <el-form-item label="医生邀请码" prop="inviteCode">
          <el-input v-model="bindDoctorForm.inviteCode" placeholder="请输入医生提供的邀请码" />
          <div class="form-tip">请向您的家庭医生索取邀请码</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBindDoctorDialog = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleBindDoctor">签约</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useFamilyStore } from '@/stores/family'
import { useUserStore } from '@/stores/user'
import { getFamilies, createFamily, joinFamily, getFamilyMembers, removeMember, getFamilyDoctor, bindFamilyDoctor, unbindFamilyDoctor } from '@/api/family'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Connection, CopyDocument, FirstAidKit } from '@element-plus/icons-vue'

const familyStore = useFamilyStore()
const userStore = useUserStore()

const currentFamilyId = ref(null)
const members = ref([])
const currentDoctor = ref(null)
const showCreateDialog = ref(false)
const showJoinDialog = ref(false)
const showBindDoctorDialog = ref(false)
const loading = ref(false)

const createForm = ref({ name: '' })
const joinForm = ref({ code: '' })
const bindDoctorForm = ref({ inviteCode: '' })
const createFormRef = ref(null)
const joinFormRef = ref(null)
const bindDoctorFormRef = ref(null)

const currentFamily = computed(() => 
  familyStore.families.find(f => f.id === currentFamilyId.value)
)

const isAdmin = computed(() => {
  if (!currentFamily.value || !userStore.profile) return false
  return currentFamily.value.ownerId === userStore.profile.id
})

const rules = {
  name: [{ required: true, message: '请输入家庭名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入邀请码', trigger: 'blur' }],
  inviteCode: [{ required: true, message: '请输入医生邀请码', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await getFamilies()
    familyStore.setFamilies(res.data)
    
    if (res.data.length > 0 && !currentFamilyId.value) {
      currentFamilyId.value = res.data[0].id
      handleFamilyChange(currentFamilyId.value)
    }
  } catch (error) {
    console.error(error)
  }
}

const loadDoctor = async (familyId) => {
  if (!familyId) return
  try {
    const res = await getFamilyDoctor(familyId)
    currentDoctor.value = res.data
  } catch (error) {
    currentDoctor.value = null
  }
}

const handleFamilyChange = async (val) => {
  if (!val) return
  try {
    const res = await getFamilyMembers(val)
    members.value = res.data
    const family = familyStore.families.find(f => f.id === val)
    familyStore.setCurrent(family)
    loadDoctor(val)
  } catch (error) {
    ElMessage.error('获取家庭数据失败')
  }
}

const handleBindDoctor = async () => {
  if (!bindDoctorFormRef.value) return
  await bindDoctorFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 使用邀请码（即医生UserID）进行绑定
        await bindFamilyDoctor(currentFamilyId.value, { 
          doctorUserId: bindDoctorForm.value.inviteCode 
        })
        ElMessage.success('签约成功')
        showBindDoctorDialog.value = false
        loadDoctor(currentFamilyId.value)
      } catch (error) {
        // Error handled by interceptor
      } finally {
        loading.value = false
      }
    }
  })
}

const handleUnbindDoctor = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要解除与该家庭医生的签约关系吗？解除后医生将无法查看您的健康数据。',
      '解除签约',
      { type: 'warning' }
    )
    await unbindFamilyDoctor(currentFamilyId.value)
    ElMessage.success('已解除签约')
    currentDoctor.value = null
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('解除失败')
    }
  }
}

const handleCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await createFamily(createForm.value)
        ElMessage.success('创建成功')
        showCreateDialog.value = false
        loadData()
      } catch (error) {
        // Error handled by interceptor
      } finally {
        loading.value = false
      }
    }
  })
}

const handleJoin = async () => {
  if (!joinFormRef.value) return
  await joinFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await joinFamily({ inviteCode: joinForm.value.code })
        ElMessage.success('加入成功')
        showJoinDialog.value = false
        loadData()
      } catch (error) {
        // Error handled by interceptor
      } finally {
        loading.value = false
      }
    }
  })
}

const copyInviteCode = () => {
  if (currentFamily.value?.inviteCode) {
    navigator.clipboard.writeText(currentFamily.value.inviteCode)
    ElMessage.success('邀请码已复制')
  }
}

const handleRemoveMember = async (member) => {
  try {
    await removeMember(currentFamilyId.value, member.id)
    ElMessage.success('移除成功')
    handleFamilyChange(currentFamilyId.value)
  } catch (error) {
    ElMessage.error('移除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.page-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  animation: fadeInDown 0.6s vars.$ease-spring;
  
  .title-text {
    font-size: 24px;
    font-weight: 600;
    color: vars.$text-primary-color;
    margin-bottom: 8px;
  }
  
  .subtitle-text {
    color: vars.$text-secondary-color;
  }
}

.actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
  animation: fadeInDown 0.6s vars.$ease-spring 0.1s backwards;
  
  .action-btn {
    height: 40px;
    padding: 0 20px;
    font-weight: 500;
    transition: all 0.3s vars.$ease-spring;
    border-radius: vars.$radius-md;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: vars.$shadow-sm;
    }
  }
  
  .glass-btn {
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.4);
    background: rgba(255, 255, 255, 0.3);
    color: vars.$text-primary-color;
    
    &:hover {
      background: rgba(255, 255, 255, 0.6);
    }
  }
  
  .family-select {
    width: 200px;
    
    :deep(.el-input__wrapper) {
      @include mixins.glass-effect;
      box-shadow: none !important;
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: vars.$radius-md;
      
      &:hover, &.is-focus {
        background: rgba(255, 255, 255, 0.8);
        border-color: vars.$primary-color;
      }
    }
  }
}

.invite-box {
  display: flex;
  align-items: center;
  padding: 4px 12px;
  background: rgba(vars.$primary-color, 0.1);
  border-radius: 20px;
  
  .label {
    font-size: 14px;
    color: vars.$text-secondary-color;
    margin-right: 8px;
  }
  
  .code-tag {
    font-family: monospace;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 1px;
    color: vars.$primary-color;
    margin-right: 4px;
  }
}

.glass-panel {
  @include mixins.glass-effect;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.section-card {
  border-radius: vars.$radius-lg;
  padding: 24px;
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s vars.$ease-spring 0.2s backwards;
  
  &.glass-card {
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.3);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h3 {
      font-size: 20px;
      font-weight: 600;
      color: vars.$text-primary-color;
    }
  }
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  
  .member-card {
    display: flex;
    align-items: center;
    padding: 16px;
    border-radius: vars.$radius-md;
    transition: all 0.3s vars.$ease-spring;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: vars.$shadow-md;
      background: rgba(255, 255, 255, 0.6);
    }
    
    .member-avatar {
      border: 2px solid white;
      box-shadow: vars.$shadow-sm;
      margin-right: 16px;
      font-size: 24px;
      background: vars.$gradient-primary;
    }
    
    .member-info {
      flex: 1;
      
      h4 {
        margin: 0 0 4px;
        font-size: 16px;
        color: vars.$text-primary-color;
      }
      
      .tags {
        display: flex;
        gap: 4px;
      }
    }
  }
}

/* Dialog Styles */
:deep(.glass-dialog) {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  
  .el-dialog__header {
    margin-right: 0;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    border-top: 1px solid rgba(0, 0, 0, 0.05);
    padding: 16px 24px;
  }
}

.mt-6 {
  margin-top: 24px;
}

.mr-2 {
  margin-right: 8px;
}

.icon-align {
  vertical-align: middle;
  position: relative;
  top: -2px;
}

.doctor-card {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding: 24px;
  background: linear-gradient(to right, rgba(255,255,255,0.6), rgba(255,255,255,0.3));
  border-radius: vars.$radius-md;
  border: 1px solid rgba(255, 255, 255, 0.4);

  .doctor-avatar {
    border: 4px solid rgba(255, 255, 255, 0.8);
    box-shadow: vars.$shadow-md;
  }

  .doctor-detail {
    flex: 1;

    .name-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      h4 {
        font-size: 18px;
        font-weight: 600;
        margin: 0;
        color: vars.$text-primary-color;
      }
    }

    .hospital {
      color: vars.$text-regular-color;
      font-size: 14px;
      margin-bottom: 8px;
      font-weight: 500;
    }

    .bio {
      color: vars.$text-secondary-color;
      font-size: 14px;
      line-height: 1.6;
      margin-bottom: 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .stats-row {
      display: flex;
      gap: 16px;

      .stat-item {
        font-size: 13px;
        color: vars.$text-secondary-color;
        background: rgba(0,0,0,0.03);
        padding: 2px 8px;
        border-radius: 4px;
      }
    }
  }
}

.empty-doctor {
  text-align: center;
  padding: 40px;
  color: vars.$text-secondary-color;
  background: rgba(0,0,0,0.02);
  border-radius: vars.$radius-md;
  border: 1px dashed rgba(0,0,0,0.1);

  .sub-hint {
    font-size: 13px;
    margin-top: 8px;
    color: vars.$text-placeholder-color;
  }
}

.form-tip {
  font-size: 12px;
  color: vars.$text-secondary-color;
  margin-top: 4px;
}
</style>
