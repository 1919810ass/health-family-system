<template>
  <div class="register-page">
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>

    <div class="register-content">
      <div class="register-header">
        <div class="logo-box" aria-hidden="true">
          <el-icon class="logo-icon"><FirstAidKit /></el-icon>
        </div>
        <h1 class="app-name">智慧家庭医生</h1>
        <p class="app-slogan">创建账号 · 提交后等待管理员审核</p>
      </div>

      <el-card class="register-card" v-if="!submitted">
        <h2 class="form-title">账号注册</h2>

        <el-alert
          class="audit-tip"
          type="info"
          show-icon
          :closable="false"
          title="注册成功后需管理员审核通过才可登录"
        />

        <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          size="large"
          @submit.prevent="onSubmit"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              :prefix-icon="Iphone"
              autocomplete="tel"
              inputmode="tel"
              clearable
              :disabled="loading"
              aria-label="手机号"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="Lock"
              autocomplete="new-password"
              :disabled="loading"
              aria-label="密码"
            />
          </el-form-item>

          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称（可选）"
              :prefix-icon="User"
              maxlength="64"
              show-word-limit
              :disabled="loading"
              aria-label="昵称"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              native-type="submit"
              class="submit-btn"
              round
              v-particles
            >
              {{ loading ? '提交中...' : '注册' }}
            </el-button>
          </el-form-item>

          <el-form-item>
            <el-button class="secondary-btn" :disabled="loading" @click="goToDoctorRegister">我是医生，去注册申请</el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <el-link type="primary" :underline="false" @click="goLogin">返回登录</el-link>
        </div>
      </el-card>

      <el-card class="register-card success-card" v-else>
        <div class="success-content">
          <el-icon class="success-icon" color="#67C23A"><CircleCheckFilled /></el-icon>
          <h2 class="success-title">注册申请提交成功</h2>
          <p class="success-text">您的账号注册申请已提交，请等待管理员审核。</p>
          <p class="success-text">审核通过后，您将可以使用手机号和密码登录。</p>
          <el-button type="primary" @click="goLogin" class="submit-btn mt-16" round>返回登录页</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/auth'
import { CircleCheckFilled, Iphone, Lock, User, FirstAidKit } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const submitted = ref(false)

const form = reactive({ phone: '', password: '', nickname: '' })

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' },
  ],
  nickname: [
    { min: 2, max: 64, message: '昵称长度 2-64', trigger: 'blur' },
  ],
}

const onSubmit = async () => {
  try {
    loading.value = true
    await formRef.value.validate()
    const response = await register(form)
    
    // 检查响应，如果是等待审核，则显示成功页面
    // 后端现在返回 Result<?>，data可能是null或者TokenResponse
    // 我们的后端逻辑是：如果审核，返回 data: "注册成功，请等待管理员审核" 或者 data: null (但message有提示)
    // 之前的代码：Result.success("注册成功，请等待管理员审核") -> data 是字符串
    
    const data = response?.data || response;
    
    // 如果返回了token，说明不需要审核（可能是管理员注册或者逻辑变了），直接登录
    const token = data?.accessToken || data?.token;
    
    if (token) {
       localStorage.setItem('access_token', token)
       ElMessage.success('注册成功')
       router.push('/home');
       return;
    }

    // 没有token，视为需要审核
    submitted.value = true;
    
  } catch (error) {
    const data = error?.response?.data
    const msg = (typeof data === 'string' && data) || data?.message || error?.message || '注册失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const goToDoctorRegister = () => {
  router.push('/doctor/register')
}

const goLogin = () => router.push('/login')
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: vars.$gradient-bg;
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  z-index: 0;
  opacity: 0.6;
}

.circle-1 {
  width: 320px;
  height: 320px;
  background: rgba(vars.$primary-color, 0.4);
  top: -60px;
  left: -60px;
}

.circle-2 {
  width: 420px;
  height: 420px;
  background: rgba(vars.$success-color, 0.28);
  bottom: -120px;
  right: -120px;
}

.register-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: slideUp 0.8s vars.$ease-spring;
}

.register-header {
  text-align: center;
  margin-bottom: 22px;
  width: 100%;
}

.logo-box {
  width: 70px;
  height: 70px;
  margin: 0 auto 14px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.65);
  box-shadow: vars.$shadow-lg;
  @include mixins.glass-effect;

  .logo-icon {
    font-size: 34px;
    color: vars.$primary-color;
  }
}

.app-name {
  margin: 0;
  font-size: 26px;
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: 0.5px;
  background: linear-gradient(120deg, vars.$primary-color, vars.$success-color);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.app-slogan {
  margin: 10px 0 0;
  font-size: 13px;
  color: vars.$text-secondary-color;
}

:deep(.el-card) {
  border: vars.$glass-border;
  border-radius: vars.$radius-lg;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: vars.$shadow-xl;
}

:deep(.el-card__body) {
  padding: 24px 26px 18px;
}

.register-card {
  width: 100%;
  @include mixins.glass-effect;
}

.form-title {
  margin: 0 0 14px;
  font-size: 20px;
  font-weight: 700;
  color: vars.$text-main-color;
}

.audit-tip {
  margin-bottom: 16px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-input__wrapper) {
  border-radius: vars.$radius-md;
  box-shadow: 0 0 0 1px var(--el-border-color) inset;

  &.is-focus {
    box-shadow: 0 0 0 1px vars.$primary-color inset !important;
  }
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-weight: 700;
  letter-spacing: 0.2px;
  transition: transform 0.2s vars.$ease-spring, box-shadow 0.2s vars.$ease-spring;

  &:hover {
    transform: translateY(-1px);
    box-shadow: vars.$shadow-lg;
  }
}

.secondary-btn {
  width: 100%;
  height: 44px;
  border-radius: vars.$radius-round;
  transition: all 0.3s vars.$ease-spring;
  
  &:hover {
    background: rgba(vars.$primary-color, 0.05);
    color: vars.$primary-color;
    border-color: vars.$primary-color;
  }
}

.register-footer {
  margin-top: 10px;
  font-size: 13px;
  color: vars.$text-secondary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.mt-16 {
  margin-top: 16px;
}

.success-card {
  text-align: center;
  .success-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 0;
    
    .success-icon {
      font-size: 64px;
      margin-bottom: 24px;
      color: vars.$success-color;
    }
  }
}

.success-title {
  font-size: 20px;
  color: vars.$text-main-color;
  margin: 0 0 12px;
  font-weight: 700;
}

.success-text {
  color: vars.$text-secondary-color;
  font-size: 14px;
  line-height: 1.6;
  margin: 4px 0;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 480px) {
  .register-content {
    max-width: 380px;
  }

  :deep(.el-card__body) {
    padding: 20px 18px 14px;
  }

  .app-name {
    font-size: 22px;
  }
}
</style>
