<template>
  <el-card class="settings-card">
    <template #header>
      <div class="card-header">头像设置</div>
    </template>
    <div class="avatar-row">
      <el-avatar :src="previewUrl || store.profile?.avatar" size="large" class="avatar-preview" />
      <el-upload
        class="ml-16"
        action="/api/user/avatar"
        :show-file-list="false"
        :accept="'image/*'"
        :before-upload="beforeUpload"
        :http-request="doUpload"
        :on-change="onFileChange"
      >
        <el-button type="primary" :loading="loading" v-particles>选择图片并上传</el-button>
      </el-upload>
    </div>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../../stores/user'

const store = useUserStore()
const loading = ref(false)
const previewUrl = ref('')

const beforeUpload = (file) => {
  console.log('[Avatar] beforeUpload', file?.name, file?.size)
  const isImage = /\.(png|jpg|jpeg|webp)$/i.test(file.name)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('仅支持 png/jpg/jpeg/webp')
  if (!isLt2M) ElMessage.error('图片大小不超过 2MB')
  return isImage && isLt2M
}

const doUpload = async ({ file }) => {
  try {
    console.log('[Avatar] doUpload start')
    loading.value = true
    const formData = new FormData()
    formData.append('file', file)
    const url = await store.updateAvatar(formData)
    console.log('[Avatar] uploaded url', url)
    if (url) {
      previewUrl.value = url
      ElMessage.success('头像已更新')
    }
  } catch (e) {
    console.error('[Avatar] upload error', e)
    ElMessage.error('上传失败')
  } finally {
    loading.value = false
  }
}

const onFileChange = (file) => {
  console.log('[Avatar] file selected', file?.name)
}
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.settings-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  transition: all 0.3s vars.$ease-spring;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: vars.$shadow-md;
  }
}

.card-header { 
  font-weight: 600;
  color: vars.$text-main-color;
}

.avatar-row { 
  display: flex; 
  align-items: center;
}

.avatar-preview {
  box-shadow: vars.$shadow-sm;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.ml-16 { margin-left: 16px }
</style>

