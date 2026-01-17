<template>
  <el-card class="settings-card">
    <template #header>
      <div class="card-header">密码安全</div>
    </template>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password class="glass-input" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password class="glass-input" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password class="glass-input" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit" v-particles>修改密码</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../../stores/user'

const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码至少 8 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_, v, cb) => {
        if (v !== form.newPassword) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur',
    },
  ],
}

const onSubmit = async () => {
  await formRef.value.validate()
  try {
    loading.value = true
    await store.changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
    ElMessage.success('密码已修改')
    onReset()
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
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

.glass-input {
  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.5);
    box-shadow: none;
    border: 1px solid rgba(255, 255, 255, 0.4);
    border-radius: vars.$radius-base;
    transition: all 0.3s;
    
    &:hover, &.is-focus {
      background: rgba(255, 255, 255, 0.9);
      border-color: vars.$primary-color;
      box-shadow: 0 0 0 1px vars.$primary-color inset;
    }
  }
  
  :deep(.el-input__inner) {
    color: vars.$text-main-color;
  }
}
</style>

