<template>
  <el-card class="settings-card">
    <template #header>
      <div class="card-header">通知偏好</div>
    </template>
    <el-form :model="form" label-width="100px">
      <el-form-item label="站内通知">
        <el-switch v-model="form.inApp" />
      </el-form-item>
      <el-form-item label="邮件通知">
        <el-switch v-model="form.email" />
      </el-form-item>
      <el-form-item label="短信通知">
        <el-switch v-model="form.sms" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">保存</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref, watchEffect } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../../stores/user'

const store = useUserStore()
const loading = ref(false)
const form = reactive({ inApp: true, email: false, sms: false })

watchEffect(() => {
  const n = store.profile?.notifications
  if (n) {
    form.inApp = !!n.inApp
    form.email = !!n.email
    form.sms = !!n.sms
  }
})

const onSubmit = async () => {
  try {
    loading.value = true
    await store.updateNotifications({ inApp: form.inApp, email: form.email, sms: form.sms })
    ElMessage.success('通知偏好已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  const n = store.profile?.notifications
  if (n) {
    form.inApp = !!n.inApp
    form.email = !!n.email
    form.sms = !!n.sms
  } else {
    form.inApp = true
    form.email = false
    form.sms = false
  }
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
</style>

