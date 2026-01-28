<template>
  <div class="doctor-settings">
    <el-page-header content="医生系统设置" />
    
    <!-- 个人信息设置 -->
    <el-card class="mt-16">
      <template #header>
        <span>个人信息</span>
        <el-text type="info" size="small" style="margin-left: 12px">管理您的基本信息和头像</el-text>
      </template>
      
      <div class="profile-setting-container">
        <!-- 头像部分 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="onFileChange"
          >
            <div class="avatar-wrapper" v-loading="avatarLoading">
              <el-avatar :size="100" :src="userStore.profile?.avatar" class="avatar-img">
                {{ userStore.profile?.nickname?.charAt(0) || '医' }}
              </el-avatar>
              <div class="avatar-mask">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
          </el-upload>
          <div class="avatar-tip">点击头像进行更换</div>
        </div>

        <AvatarCropper
          v-model:visible="cropperVisible"
          :file="selectedFile"
          @confirm="handleCropConfirm"
        />

        <!-- 表单部分 -->
        <el-form :model="profileForm" label-width="100px" class="profile-form">
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" disabled>
              <template #append>
                <el-button link>修改</el-button>
              </template>
            </el-input>
            <div class="form-tip">手机号修改请联系管理员</div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存个人信息</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 通知设置 -->
    <el-card class="mt-16">
      <template #header>
        <span>消息通知设置</span>
        <el-text type="info" size="small" style="margin-left: 12px">可针对不同通知类型设置接收渠道</el-text>
      </template>
      
      <el-form :model="notifications" label-width="150px">
        <!-- 系统通知 -->
        <el-divider content-position="left">系统通知</el-divider>
        <el-form-item label="APP 内通知">
          <el-switch v-model="notifications.system.inApp" />
        </el-form-item>
        <el-form-item label="邮件通知">
          <el-switch v-model="notifications.system.email" />
        </el-form-item>
        <el-form-item label="短信通知">
          <el-switch v-model="notifications.system.sms" />
        </el-form-item>
        
        <!-- 随访提醒 -->
        <el-divider content-position="left">随访提醒</el-divider>
        <el-form-item label="APP 内通知">
          <el-switch v-model="notifications.followup.inApp" />
        </el-form-item>
        <el-form-item label="邮件通知">
          <el-switch v-model="notifications.followup.email" />
        </el-form-item>
        <el-form-item label="短信通知">
          <el-switch v-model="notifications.followup.sms" />
        </el-form-item>
        
        <!-- 预警通知 -->
        <el-divider content-position="left">预警通知</el-divider>
        <el-form-item label="APP 内通知">
          <el-switch v-model="notifications.alert.inApp" />
        </el-form-item>
        <el-form-item label="邮件通知">
          <el-switch v-model="notifications.alert.email" />
        </el-form-item>
        <el-form-item label="短信通知">
          <el-switch v-model="notifications.alert.sms" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveNotifications">保存通知设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 工作时间设置 -->
    <el-card class="mt-16">
      <template #header>
        <span>工作时间设置</span>
        <el-text type="info" size="small" style="margin-left: 12px">设置的工作时间会影响随访计划生成和推送发送窗口</el-text>
      </template>
      
      <el-form :model="workingHours" label-width="150px">
        <el-form-item label="工作日">
          <el-select v-model="workingHours.workDays" multiple placeholder="选择工作日" style="width: 400px">
            <el-option v-for="d in daysList" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作时间段">
          <el-time-picker
            v-model="timeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 400px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingWork" @click="saveWorkingHours">保存工作时间</el-button>
          <el-button @click="resetWorkingHours">重置为默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { getDoctorSettings, updateDoctorSettings } from '../../api/doctor'
import { useUserStore } from '../../stores/user'
import dayjs from 'dayjs'
import AvatarCropper from '@/components/Common/AvatarCropper.vue'

const userStore = useUserStore()
const saving = ref(false)
const savingWork = ref(false)
const savingProfile = ref(false)
const avatarLoading = ref(false)
const cropperVisible = ref(false)
const selectedFile = ref(null)

// 个人信息表单
const profileForm = ref({
  nickname: '',
  phone: ''
})

// 同步用户信息
watch(() => userStore.profile, (newVal) => {
  if (newVal) {
    profileForm.value.nickname = newVal.nickname || ''
    profileForm.value.phone = newVal.phone || ''
  }
}, { immediate: true })

// 头像上传
const onFileChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return

  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG/PNG/WEBP 格式!')
    return
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return
  }
  
  selectedFile.value = file
  cropperVisible.value = true
}

const handleCropConfirm = async (file) => {
  avatarLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    await userStore.updateAvatar(formData)
    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('上传头像失败', error)
    ElMessage.error('上传头像失败')
  } finally {
    avatarLoading.value = false
    selectedFile.value = null
  }
}

// 保存个人信息
const saveProfile = async () => {
  savingProfile.value = true
  try {
    await userStore.updateProfile({
      nickname: profileForm.value.nickname
    })
    ElMessage.success('个人信息保存成功')
  } catch (error) {
    console.error('保存个人信息失败', error)
    ElMessage.error('保存失败')
  } finally {
    savingProfile.value = false
  }
}

// 通知设置
const notifications = ref({
  system: { inApp: true, email: false, sms: false },
  followup: { inApp: true, email: false, sms: false },
  alert: { inApp: true, email: false, sms: false }
})

// 工作时间设置
const daysList = [
  { label: '周一', value: 'MON' },
  { label: '周二', value: 'TUE' },
  { label: '周三', value: 'WED' },
  { label: '周四', value: 'THU' },
  { label: '周五', value: 'FRI' },
  { label: '周六', value: 'SAT' },
  { label: '周日', value: 'SUN' },
]

const workingHours = ref({
  workDays: ['MON', 'TUE', 'WED', 'THU', 'FRI'],
  startTime: '09:00',
  endTime: '18:00'
})

// 时间范围（用于时间选择器）
const timeRange = ref([])

// 同步时间范围到工作时间
watch(timeRange, (val) => {
  if (val && val.length === 2) {
    workingHours.value.startTime = val[0]
    workingHours.value.endTime = val[1]
  }
}, { immediate: true })

// 同步工作时间到时间范围
watch(() => [workingHours.value.startTime, workingHours.value.endTime], ([start, end]) => {
  if (start && end) {
    timeRange.value = [start, end]
  }
}, { immediate: true })

// 加载设置
const loadSettings = async () => {
  try {
    const res = await getDoctorSettings()
    const data = res?.data
    
    if (data?.notifications) {
      notifications.value = {
        system: {
          inApp: data.notifications.system?.inApp ?? true,
          email: data.notifications.system?.email ?? false,
          sms: data.notifications.system?.sms ?? false
        },
        followup: {
          inApp: data.notifications.followup?.inApp ?? true,
          email: data.notifications.followup?.email ?? false,
          sms: data.notifications.followup?.sms ?? false
        },
        alert: {
          inApp: data.notifications.alert?.inApp ?? true,
          email: data.notifications.alert?.email ?? false,
          sms: data.notifications.alert?.sms ?? false
        }
      }
    }
    
    if (data?.workingHours) {
      workingHours.value = {
        workDays: data.workingHours.workDays || ['MON', 'TUE', 'WED', 'THU', 'FRI'],
        startTime: data.workingHours.startTime || '09:00',
        endTime: data.workingHours.endTime || '18:00'
      }
    }
  } catch (error) {
    console.error('加载医生设置失败:', error)
    ElMessage.error('加载设置失败')
  }
}

// 保存通知设置
const saveNotifications = async () => {
  saving.value = true
  try {
    await updateDoctorSettings({
      notifications: notifications.value,
      workingHours: null  // 不更新工作时间
    })
    ElMessage.success('通知设置已保存')
  } catch (error) {
    console.error('保存通知设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存工作时间
const saveWorkingHours = async () => {
  if (!workingHours.value.workDays || workingHours.value.workDays.length === 0) {
    ElMessage.warning('请至少选择一个工作日')
    return
  }
  
  if (!workingHours.value.startTime || !workingHours.value.endTime) {
    ElMessage.warning('请设置工作时间段')
    return
  }
  
  savingWork.value = true
  try {
    await updateDoctorSettings({
      notifications: null,  // 不更新通知设置
      workingHours: workingHours.value
    })
    ElMessage.success('工作时间设置已保存')
  } catch (error) {
    console.error('保存工作时间失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingWork.value = false
  }
}

// 重置工作时间
const resetWorkingHours = () => {
  workingHours.value = {
    workDays: ['MON', 'TUE', 'WED', 'THU', 'FRI'],
    startTime: '09:00',
    endTime: '18:00'
  }
  ElMessage.info('已重置为默认工作时间（周一至周五 09:00-18:00）')
}

onMounted(async () => {
  await loadSettings()
})
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.doctor-settings {
  padding: 24px;
  min-height: calc(100vh - 60px);
}

:deep(.el-page-header) {
  margin-bottom: 24px;
  
  .el-page-header__content {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.mt-16 {
  margin-top: 24px;
}

// 卡片统一样式
:deep(.el-card) {
  border-radius: 16px;
  border: none;
  @include mixins.glass-effect;
  transition: all 0.3s vars.$ease-spring;
  animation: fadeInUp 0.6s vars.$ease-spring backwards;

  @for $i from 1 through 3 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.1}s;
    }
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  }

  .el-card__header {
    padding: 20px 24px;
    font-weight: 600;
    font-size: 16px;
    color: var(--el-text-color-primary);
    border-bottom: 1px solid rgba(var(--el-border-color-lighter-rgb), 0.3);
    background: transparent;
    display: flex;
    align-items: center;

    &::before {
      content: '';
      display: block;
      width: 4px;
      height: 16px;
      background: var(--el-color-primary);
      border-radius: 2px;
      margin-right: 12px;
    }

    .el-text {
      margin-left: 12px;
      font-weight: 400;
      font-size: 13px;
    }
  }

  .el-card__body {
    padding: 24px;
  }
}

.profile-setting-container {
  display: flex;
  gap: 40px;
  align-items: flex-start;
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: center;
    gap: 24px;
  }
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 120px;
  
  .avatar-uploader {
    cursor: pointer;
    
    .avatar-wrapper {
      position: relative;
      border-radius: 50%;
      overflow: hidden;
      transition: all 0.3s;
      
      &:hover .avatar-mask {
        opacity: 1;
      }
    }
    
    .avatar-img {
      border: 4px solid rgba(var(--el-color-primary-rgb), 0.1);
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      font-size: 32px;
      font-weight: bold;
    }
    
    .avatar-mask {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      opacity: 0;
      transition: opacity 0.3s;
      gap: 4px;
      
      .el-icon {
        font-size: 24px;
      }
      
      span {
        font-size: 12px;
      }
    }
  }
  
  .avatar-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.profile-form {
  flex: 1;
  max-width: 500px;
  width: 100%;
  
  .form-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
    line-height: 1.4;
  }
}

// 表单优化
:deep(.el-form) {
  .el-form-item {
    margin-bottom: 24px;

    .el-form-item__label {
      font-weight: 500;
      font-size: 14px;
    }

    .el-switch {
      --el-switch-on-color: var(--el-color-success);
    }

    .el-select, .el-time-picker {
      transition: all 0.3s;

      &:hover {
        transform: translateY(-1px);
      }
    }
  }

  .el-button {
    padding: 12px 28px;
    font-weight: 500;
    transition: all 0.3s vars.$ease-spring;
    border-radius: 8px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

// 分割线优化
:deep(.el-divider) {
  margin: 24px 0;
  border-top-color: rgba(64, 158, 255, 0.2);

  .el-divider__text {
    font-weight: 600;
    font-size: 15px;
    color: var(--el-color-primary);
    background: transparent; // Make it transparent for glass effect
    padding: 0 16px;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 1366px) {
  .doctor-settings {
    padding: 16px;
  }

  :deep(.el-card .el-card__body) {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  :deep(.el-form) {
    .el-form-item {
      .el-select, .el-time-picker {
        width: 100% !important;
      }
    }
  }
}
</style>
