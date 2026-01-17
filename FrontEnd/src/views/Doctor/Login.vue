<template>
  <div class="doctor-login">
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>

    <div class="login-content">
      <div class="login-header">
        <div class="logo-box" aria-hidden="true">
          <img v-if="orgLogo" :src="orgLogo" alt="" />
          <el-icon v-else class="logo-icon"><FirstAidKit /></el-icon>
        </div>
        <h1 class="org-name">{{ orgName }}</h1>
        <p class="org-slogan">医生工作台 · 审核通过后可登录</p>
      </div>

      <el-card class="login-card">
        <h2 class="form-title">医生登录</h2>

        <el-alert
          class="audit-tip"
          type="info"
          show-icon
          :closable="false"
          title="医生账号需管理员审核通过后可登录"
        />

        <el-form
          :model="pwdForm"
          :rules="rulesPwd"
          ref="pwdRef"
          size="large"
          @submit.prevent="onPwdLogin"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="pwdForm.phone"
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
              v-model="pwdForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="Lock"
              autocomplete="current-password"
              :disabled="loading"
              aria-label="密码"
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
              {{ loading ? '登录中...' : '立即登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>还没有医生账号？</span>
          <el-link type="primary" :underline="false" @click="goRegister">立即注册</el-link>
          <span class="divider">|</span>
          <el-link type="primary" :underline="false" @click="goUserLogin">普通用户登录</el-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, FirstAidKit } from '@element-plus/icons-vue'
import { login } from '../../api/auth'
import { useUserStore } from '../../stores/user'
import { setToken } from '../../utils/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const orgName = '某医院/机构'
const orgLogo = null

const pwdRef = ref()
const pwdForm = ref({ phone: '', password: '' })
const rulesPwd = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
}

const onPwdLogin = async () => {
  await pwdRef.value.validate()
  loading.value = true
  try {
    const res = await login({ phone: pwdForm.value.phone, password: pwdForm.value.password })
    if (res?.code !== 0 && res?.code !== 200) {
      throw new Error(res?.message || '登录失败')
    }
    const token = res?.data?.accessToken || res?.data?.token || res?.accessToken || res?.token
    if (!token) {
      throw new Error('服务器响应异常：未获取到登录令牌')
    }
    setToken(token)

    const profile = await userStore.fetchProfile()
    if (profile?.role !== 'DOCTOR') {
      userStore.logout()
      ElMessage.error('该账号不是医生，请使用普通用户登录入口')
      router.replace('/login')
      return
    }

    ElMessage.success('登录成功')
    router.replace('/doctor/workbench')
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const goRegister = () => router.push('/doctor/register')
const goUserLogin = () => router.push('/login')
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.doctor-login {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.16) 0%, rgba(103, 194, 58, 0.12) 100%);
  position: relative;
  overflow: hidden;
  
  background-image: url("data:image/svg+xml,%3Csvg width='200' height='100' viewBox='0 0 200 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 50 L40 50 L50 20 L60 80 L70 50 L200 50' stroke='rgba(64, 158, 255, 0.05)' fill='none' stroke-width='2'/%3E%3C/svg%3E");
  background-size: 400px 200px;
  animation: bgScroll 20s linear infinite;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  z-index: 0;
  opacity: 0.55;
}

.circle-1 {
  width: 320px;
  height: 320px;
  background: rgba(64, 158, 255, 0.45);
  top: -60px;
  left: -60px;
}

.circle-2 {
  width: 420px;
  height: 420px;
  background: rgba(103, 194, 58, 0.35);
  bottom: -120px;
  right: -120px;
}

@keyframes bgScroll {
  from { background-position: 0 0; }
  to { background-position: -400px 0; }
}

.login-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: slideUp 0.8s vars.$ease-spring;
}

.login-header {
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
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
  @include mixins.glass-effect;

  img {
    width: 44px;
    height: 44px;
  }

  .logo-icon {
    font-size: 34px;
    color: var(--el-color-primary);
  }
}

.org-name {
  margin: 0;
  font-size: 26px;
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: 0.5px;
  background: linear-gradient(120deg, var(--el-color-primary), #67c23a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.org-slogan {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

:deep(.el-card) {
  border: none;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.10);
}

:deep(.el-card__body) {
  padding: 24px 26px 18px;
}

.login-card {
  width: 100%;
  @include mixins.glass-effect;
}

.form-title {
  margin: 0 0 14px;
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.audit-tip {
  margin-bottom: 16px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px var(--el-border-color) inset;
  transition: box-shadow 0.2s, transform 0.2s;

  &.is-focus {
    box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
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
    box-shadow: 0 10px 24px rgba(64, 158, 255, 0.28);
  }
}

.login-footer {
  margin-top: 10px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.divider {
  opacity: 0.55;
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
  .login-content {
    max-width: 380px;
  }

  :deep(.el-card__body) {
    padding: 20px 18px 14px;
  }

  .org-name {
    font-size: 22px;
  }
}
</style>
