<template>
  <el-card class="settings-card">
    <template #header>
      <div class="card-header">基础资料</div>
    </template>
    <el-form :model="form" :rules="rules" ref="formRef" label-width="88px">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" class="glass-input" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" class="glass-input" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" class="glass-input" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit" v-particles>保存</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, watchEffect } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../../stores/user'

const store = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ nickname: '', phone: '', email: '' })

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }, { min: 2, max: 20, message: '长度 2-20', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { pattern: /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/, message: '邮箱格式错误', trigger: 'blur' }],
}

watchEffect(() => {
  const p = store.profile
  if (p) {
    form.nickname = p.nickname || ''
    form.phone = p.phone || ''
    form.email = p.email || ''
  }
})

const onSubmit = async () => {
  await formRef.value.validate()
  try {
    loading.value = true
    await store.updateProfile({ nickname: form.nickname, phone: form.phone, email: form.email })
    ElMessage.success('资料已更新')
  } catch (e) {
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  const p = store.profile
  if (p) {
    form.nickname = p.nickname || ''
    form.phone = p.phone || ''
    form.email = p.email || ''
  } else {
    form.nickname = ''
    form.phone = ''
    form.email = ''
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

