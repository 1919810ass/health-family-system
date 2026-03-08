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
          <el-form-item label="个人简介">
            <el-input 
              v-model="profileForm.bio" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入个人简介，将向患者展示" 
              maxlength="500"
              show-word-limit
            />
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
  </div>
</template>

<script setup>
/**
 * 组件：Settings.vue
 *
 * 业务说明：用于呈现对应页面/模块功能，并通过 API 层与后端进行数据交互。
 */

import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { getDoctorSettings, updateDoctorSettings } from '../../api/doctor'
import { useUserStore } from '../../stores/user'
import AvatarCropper from '@/components/Common/AvatarCropper.vue'

const userStore = useUserStore()
const savingProfile = ref(false)
const avatarLoading = ref(false)
const cropperVisible = ref(false)
const selectedFile = ref(null)

// 个人信息表单
const profileForm = ref({
  nickname: '',
  phone: '',
  bio: ''
})

// 同步用户信息
watch(() => userStore.profile, (newVal) => {
  if (newVal) {
    profileForm.value.nickname = newVal.nickname || ''
    profileForm.value.phone = newVal.phone || ''
    // bio 将在 loadSettings 中单独加载
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
    // 1. 更新基本信息
    await userStore.updateProfile({
      nickname: profileForm.value.nickname
    })
    
    // 2. 更新医生简介
    await updateDoctorSettings({
      notifications: null,
      workingHours: null,
      bio: profileForm.value.bio
    })
    
    ElMessage.success('个人信息保存成功')
  } catch (error) {
    console.error('保存个人信息失败', error)
    ElMessage.error('保存失败')
  } finally {
    savingProfile.value = false
  }
}

// 加载设置
const loadSettings = async () => {
  try {
    const res = await getDoctorSettings()
    const data = res?.data
    
    if (data?.bio !== undefined) {
      profileForm.value.bio = data.bio || ''
    }
  } catch (error) {
    console.error('加载医生设置失败:', error)
    ElMessage.error('加载设置失败')
  }
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
