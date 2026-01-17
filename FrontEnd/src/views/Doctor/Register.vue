<template>
  <div class="register-page">
    <div class="register-container stagger-anim">
      <el-card class="register-card" v-if="!submitted">
        <template #header>
          <div class="card-header">
            <div class="logo">
              <el-icon class="logo-icon"><FirstAidKit /></el-icon>
              <span>医生注册申请</span>
            </div>
            <p class="subtitle">加入家庭健康平台，开启智慧医疗服务</p>
          </div>
        </template>
        
        <el-scrollbar max-height="600px" class="form-scrollbar">
          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" label-position="top">
            <!-- 基本信息 -->
            <div class="form-section">
              <div class="section-title">基本信息</div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="form.name" placeholder="请输入真实姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="form.phone" placeholder="请输入手机号" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="身份证号" prop="idCard">
                    <el-input v-model="form.idCard" placeholder="请输入身份证号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="form.email" placeholder="请输入邮箱 (可选)" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="登录密码" prop="password">
                <el-input v-model="form.password" type="password" placeholder="请设置登录密码" show-password />
              </el-form-item>
            </div>

            <!-- 执业信息 -->
            <div class="form-section">
              <div class="section-title">执业信息</div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="执业医院" prop="hospital">
                    <el-input v-model="form.hospital" placeholder="请输入执业医院全称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属科室" prop="department">
                    <el-input v-model="form.department" placeholder="例如：心内科" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="职称" prop="title">
                    <el-select v-model="form.title" placeholder="请选择职称" style="width: 100%">
                      <el-option label="主任医师" value="主任医师" />
                      <el-option label="副主任医师" value="副主任医师" />
                      <el-option label="主治医师" value="主治医师" />
                      <el-option label="住院医师" value="住院医师" />
                      <el-option label="医士" value="医士" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="专业领域" prop="specialty">
                    <el-input v-model="form.specialty" placeholder="例如：高血压、糖尿病管理" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="执业证书编号" prop="licenseNumber">
                <el-input v-model="form.licenseNumber" placeholder="请输入医师执业证书编码" />
              </el-form-item>
              <el-form-item label="个人简介" prop="bio">
                <el-input v-model="form.bio" type="textarea" rows="3" placeholder="请输入个人从业经历和擅长领域" />
              </el-form-item>
            </div>
            
            <div class="form-actions">
              <el-button type="primary" :loading="loading" @click="onRegister" size="large" style="width: 100%">提交审核</el-button>
              <div class="login-link">
                已有账号？ <el-button link type="primary" @click="goLogin">去登录</el-button>
              </div>
            </div>
          </el-form>
        </el-scrollbar>
      </el-card>

      <!-- 提交成功状态 -->
      <el-card class="register-card success-card" v-else>
        <div class="success-content">
          <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
          <h2>申请提交成功</h2>
          <p>您的注册申请已提交，管理员将在 1-3 个工作日内完成审核。</p>
          <p>审核结果将通过短信通知您，审核通过后即可登录使用。</p>
          <el-button type="primary" @click="goLogin" class="back-btn">返回登录页</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerDoctor } from '../../api/auth'
import { FirstAidKit, CircleCheckFilled } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const submitted = ref(false)

const form = reactive({
  phone: '',
  password: '',
  name: '',
  idCard: '',
  email: '',
  hospital: '',
  department: '',
  title: '',
  specialty: '',
  licenseNumber: '',
  bio: '',
  // 暂时保留图片字段为空，后续可对接上传
  licenseImage: '',
  idCardFront: '',
  idCardBack: ''
})

const rules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' }, 
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请设置登录密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证格式错误', trigger: 'blur' }
  ],
  hospital: [{ required: true, message: '请输入执业医院', trigger: 'blur' }],
  department: [{ required: true, message: '请输入所属科室', trigger: 'blur' }],
  specialty: [{ required: true, message: '请输入专业领域', trigger: 'blur' }],
  licenseNumber: [{ required: true, message: '请输入执业证书编号', trigger: 'blur' }]
}

const onRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await registerDoctor(form)
    // 注册成功，后端不返回token，而是提示等待审核
    submitted.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error(e.response?.data?.message || '提交失败，请重试')
  } finally {
    loading.value = false
  }
}

const goLogin = () => router.push('/doctor/login')
</script>

<style scoped lang="scss">
.register-page { 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  min-height: 100vh; 
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 600px;
}

.register-card {
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  :deep(.el-card__header) {
    padding: 0;
    border-bottom: none;
  }
}

.card-header {
  background: linear-gradient(135deg, #409EFF, #36D1DC);
  padding: 30px 20px;
  text-align: center;
  color: white;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 8px;

    .logo-icon {
      font-size: 28px;
    }
  }

  .subtitle {
    margin: 0;
    opacity: 0.9;
    font-size: 14px;
  }
}

.form-scrollbar {
  padding: 0 20px;
}

.form-section {
  margin-bottom: 24px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
    padding-left: 10px;
    border-left: 4px solid #409EFF;
    line-height: 1;
  }
}

.form-actions {
  margin-top: 30px;
  margin-bottom: 20px;
  text-align: center;

  .login-link {
    margin-top: 16px;
    font-size: 14px;
    color: #606266;
  }
}

.success-card {
  text-align: center;
  padding: 40px 20px;

  .success-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .success-icon {
      font-size: 64px;
      color: #67C23A;
      margin-bottom: 20px;
    }

    h2 {
      font-size: 24px;
      color: #303133;
      margin: 0 0 16px;
    }

    p {
      color: #606266;
      margin: 0 0 8px;
      line-height: 1.6;
    }

    .back-btn {
      margin-top: 30px;
      width: 200px;
    }
  }
}

.stagger-anim {
  animation: slideUp 0.6s cubic-bezier(0.2, 0.8, 0.2, 1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>

