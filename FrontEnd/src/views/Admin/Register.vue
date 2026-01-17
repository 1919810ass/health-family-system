<template>
  <div class="register-page">
    <div class="register-container stagger-anim">
      <div class="glass-card register-card">
        <div class="card-header">
          <div class="logo-area">
            <div class="logo-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <h2 class="title">管理员注册</h2>
          </div>
          <p class="subtitle">创建新的管理员账号以管理系统</p>
        </div>
        
        <el-form 
          :model="form" 
          :rules="rules" 
          ref="formRef" 
          label-position="top" 
          @submit.prevent="onSubmit"
          class="register-form"
          size="large"
        >
          <el-form-item label="手机号" prop="phone">
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号" 
              :prefix-icon="Iphone"
            />
          </el-form-item>
          
          <el-form-item label="昵称" prop="nickname">
            <el-input 
              v-model="form.nickname" 
              placeholder="请输入昵称" 
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              show-password 
              placeholder="请输入密码" 
              :prefix-icon="Lock"
            />
          </el-form-item>
          
          <el-form-item class="actions-item">
            <el-button 
              type="primary" 
              :loading="loading" 
              native-type="submit" 
              class="submit-btn" 
              v-particles
            >
              注册管理员
            </el-button>
            <div class="links">
              <el-button link type="info" @click="goLogin">返回登录</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="page-footer">
        <p>&copy; {{ new Date().getFullYear() }} 家庭健康管理系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Monitor, Iphone, Lock, User } from '@element-plus/icons-vue'
import { registerAdmin } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

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
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 64, message: '昵称长度 2-64', trigger: 'blur' },
  ],
}

const onSubmit = async () => {
  try {
    loading.value = true
    await formRef.value.validate()
    
    const response = await registerAdmin(form)
    const token = response?.data?.accessToken ?? response?.accessToken
    if (!token) {
      ElMessage.error('注册返回缺少令牌')
      return
    }
    localStorage.setItem('access_token', token)
    ElMessage.success('管理员注册成功')
    
    // 尝试加载用户信息以确定角色并相应重定向
    try {
      const userStore = useUserStore();
      await userStore.fetchProfile();
      const role = userStore.profile?.role;
      
      if (role === 'ADMIN') {
        router.push('/admin');
      } else if (role === 'DOCTOR') {
        router.push('/doctor');
      } else {
        router.push('/home');
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 如果获取用户信息失败，跳转到首页
      router.push('/home');
    }
  } catch (error) {
    const data = error?.response?.data
    const msg = (typeof data === 'string' && data) || data?.message || error?.message || '注册失败，请稍后重试'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const goLogin = () => router.push('/login')
</script>

<style scoped lang="scss">
@use 'sass:map';
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.register-page { 
  min-height: 100vh; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  background: radial-gradient(circle at top right, rgba(map.get(vars.$colors, 'primary'), 0.1), transparent 40%),
              radial-gradient(circle at bottom left, rgba(map.get(vars.$colors, 'success'), 0.1), transparent 40%),
              #f5f7fa;
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.glass-card {
  @include mixins.glass-effect;
  width: 100%;
  padding: 40px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  
  .card-header {
    text-align: center;
    margin-bottom: 32px;
    
    .logo-area {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      margin-bottom: 12px;
      
      .logo-icon {
        width: 64px;
        height: 64px;
        background: linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info'));
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 32px;
        color: white;
        box-shadow: 0 10px 20px rgba(map.get(vars.$colors, 'primary'), 0.3);
      }
      
      .title {
        font-size: 24px;
        font-weight: 700;
        margin: 0;
        color: map.get(vars.$colors, 'text-main');
        letter-spacing: -0.5px;
      }
    }
    
    .subtitle {
      color: map.get(vars.$colors, 'text-secondary');
      margin: 0;
      font-size: 14px;
    }
  }
}

.register-form {
  .actions-item {
    margin-top: 32px;
    margin-bottom: 0;
    
    :deep(.el-form-item__content) {
      flex-direction: column;
      gap: 16px;
    }
  }
  
  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
    background: linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info'));
    border: none;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(map.get(vars.$colors, 'primary'), 0.3);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
  
  .links {
    display: flex;
    justify-content: center;
    width: 100%;
  }
}

.page-footer {
  text-align: center;
  color: map.get(vars.$colors, 'text-secondary');
  font-size: 12px;
  opacity: 0.6;
}

// Staggered Animation
.stagger-anim {
  opacity: 0;
  animation: fadeSlideUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>