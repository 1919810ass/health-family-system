<template>
  <div class="login-container">
    <!-- 背景装饰圆 -->
    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>

    <div class="login-content">
      <!-- 左侧/顶部：欢迎语与Logo (增加项目辨识度) -->
      <div class="login-header">
        <div class="logo-box">
          <!-- 这里可以用 img 标签替换为你的 logo 图片 -->
          <el-icon class="logo-icon"><FirstAidKit /></el-icon>
        </div>
        <h1 class="app-name">智慧家庭医生</h1>
        <p class="app-slogan">基于 Spring AI 的家庭健康智能服务平台</p>
      </div>

      <!-- 右侧/下部：登录卡片 -->
      <el-card class="login-card">
        <h2 class="form-title">账号登录</h2>

        <el-form
            :model="form"
            :rules="rules"
            ref="loginFormRef"
            size="large"
            @submit.prevent="onSubmit"
        >
          <el-form-item prop="phone">
            <el-input
                v-model="form.phone"
                placeholder="请输入手机号"
                :prefix-icon="Iphone"
                clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="请输入密码"
                :prefix-icon="Lock"
            />
          </el-form-item>

          <div class="form-options">
            <!-- 即使后端没做，前端放个checkbox看起来更完善 -->
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false" class="forgot-pwd">忘记密码？</el-link>
          </div>

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
          <span>还没有账号？</span>
          <el-link type="primary" @click="goRegister" :underline="false">立即注册</el-link>
          <span class="divider">|</span>
          <el-link type="primary" @click="goDoctorLogin" :underline="false">医生登录</el-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, FirstAidKit } from '@element-plus/icons-vue' // 引入图标
import { login } from '../../api/auth'
import { useUserStore } from '../../stores/user'
import { setToken, removeToken } from '../../utils/auth'

const router = useRouter()
const store = useUserStore()
const loginFormRef = ref()
const loading = ref(false)
const rememberMe = ref(false) // 模拟记住我功能

// 进入登录页时，自动清除登录状态
onMounted(() => {
  removeToken()
  store.logout && store.logout()
})

const form = reactive({
  phone: '',
  password: '',
})

// 验证规则优化
const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
}

const onSubmit = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 1. 执行登录
    const response = await login(form)
    
    // 优先检查业务状态码
    if (response?.code !== 0 && response?.code !== 200) {
      throw new Error(response?.message || '登录失败')
    }

    // 兼容不同后端的返回结构 (data.accessToken 或 直接accessToken)
    const token = response?.data?.accessToken || response?.accessToken || response?.token

    if (!token) {
      throw new Error('服务器响应异常：未获取到登录令牌')
    }

    setToken(token)

    // 2. 获取用户信息
    try {
      await store.fetchProfile()
    } catch (profileError) {
      console.error('获取用户信息失败', profileError)
      removeToken() // 获取失败则回滚
      throw new Error('获取用户信息失败，请重试')
    }

    // 3. 登录成功提示
    ElMessage.success('欢迎回来，登录成功！')

    // 4. 根据角色跳转 (逻辑保持不变)
    const role = store?.profile?.role
    if (role === 'ADMIN') {
      router.replace('/admin')
    } else if (role === 'DOCTOR') {
      router.replace('/doctor/workbench')
    } else {
      router.replace('/home')
    }

  } catch (error) {
    console.error(error)
    const errorMsg = error?.response?.data?.message || error?.message || '登录服务暂不可用'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.push('/register')
}

const goDoctorLogin = () => {
  router.push('/doctor/login')
}
</script>

<style scoped lang="scss">
@use "sass:map";
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: vars.$gradient-bg;
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 背景装饰圆：增加层次感 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
}
.circle-1 {
  width: 300px;
  height: 300px;
  background: rgba(vars.$primary-color, 0.4);
  top: -50px;
  left: -50px;
}
.circle-2 {
  width: 400px;
  height: 400px;
  background: rgba(vars.$success-color, 0.3);
  bottom: -100px;
  right: -100px;
}

.login-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 440px;

  /* 入场动画 */
  animation: slideUp 0.8s vars.$ease-spring;
}

.login-header {
  text-align: center;
  margin-bottom: 22px;

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
}

.login-card {
  width: 100%;
  @include mixins.glass-effect;
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

.form-title {
  margin: 0 0 14px;
  font-size: 20px;
  font-weight: 700;
  color: vars.$text-main-color;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .forgot-pwd {
    font-size: 14px;
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

.login-footer {
  margin-top: 10px;
  font-size: 13px;
  color: vars.$text-secondary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.divider {
  opacity: 0.55;
}

:deep(.el-input__wrapper) {
  border-radius: vars.$radius-md;
  box-shadow: 0 0 0 1px var(--el-border-color) inset;

  &.is-focus {
    box-shadow: 0 0 0 1px vars.$primary-color inset !important;
  }
}

/* 动画定义 */
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

/* 响应式适配 */
@media (max-width: 480px) {
  .login-content {
    max-width: 100%;
  }
  .bg-circle {
    display: none;
  }

  :deep(.el-card__body) {
    padding: 20px 18px 14px;
  }

  .login-header {
    margin-bottom: 18px;
  }

  .login-header .app-name {
    font-size: 22px;
  }
}
</style>
