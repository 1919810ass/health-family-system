<template>
  <div class="system-config">
    <!-- Header with Staggered Animation -->
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">系统配置</h1>
        <p class="page-subtitle">管理平台全局参数与功能开关</p>
      </div>
    </div>

    <div class="stagger-anim" style="--delay: 0.2s">
      <el-tabs v-model="activeTab" type="border-card" class="glass-tabs">
        <!-- 基础配置 -->
        <el-tab-pane name="basic">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Operation /></el-icon>
              <span>基础配置</span>
            </span>
          </template>
          
          <div class="tab-content">
            <div class="section-header">
              <h3 class="section-title">基础信息设置</h3>
              <el-button type="primary" round v-particles @click="saveConfig('basic')">
                <el-icon class="mr-2"><Check /></el-icon> 保存配置
              </el-button>
            </div>
            
            <el-form 
              :model="config.basic" 
              :rules="configRules.basic" 
              ref="basicFormRef"
              label-width="120px"
              label-position="top"
              class="custom-form two-column-form"
            >
              <el-form-item label="系统名称" prop="systemName">
                <el-input v-model="config.basic.systemName" placeholder="请输入系统名称">
                  <template #prefix><el-icon><Monitor /></el-icon></template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="联系邮箱" prop="contactEmail">
                <el-input v-model="config.basic.contactEmail" placeholder="请输入联系邮箱">
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="config.basic.contactPhone" placeholder="请输入联系电话">
                  <template #prefix><el-icon><Phone /></el-icon></template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="系统状态">
                <div class="switch-wrapper">
                  <el-switch
                    v-model="config.basic.systemStatus"
                    active-text="正常运行"
                    inactive-text="维护模式"
                    inline-prompt
                    style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
                  />
                </div>
              </el-form-item>
              
              <el-form-item label="系统描述" prop="systemDesc" class="full-width">
                <el-input 
                  v-model="config.basic.systemDesc" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请输入系统描述" 
                />
              </el-form-item>
              
              <el-form-item label="维护模式说明" class="full-width">
                <el-input 
                  v-model="config.basic.maintenanceDesc" 
                  type="textarea" 
                  :rows="3" 
                  placeholder="请输入维护模式说明" 
                  :disabled="config.basic.systemStatus"
                />
              </el-form-item>
              
              <el-form-item label="系统Logo" class="full-width">
                <el-upload
                  class="logo-uploader glass-uploader"
                  action="/api/upload"
                  :show-file-list="false"
                  :on-success="handleLogoSuccess"
                  :before-upload="beforeLogoUpload"
                >
                  <img v-if="config.basic.logo" :src="config.basic.logo" class="logo">
                  <div v-else class="uploader-content">
                    <el-icon class="uploader-icon"><Plus /></el-icon>
                    <div class="uploader-text">点击上传Logo</div>
                  </div>
                </el-upload>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 功能开关 -->
        <el-tab-pane name="features">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Switch /></el-icon>
              <span>功能开关</span>
            </span>
          </template>
          
          <div class="tab-content">
            <div class="section-header">
              <h3 class="section-title">系统功能模块控制</h3>
              <el-button type="primary" round v-particles @click="saveConfig('features')">
                <el-icon class="mr-2"><Check /></el-icon> 保存配置
              </el-button>
            </div>
            
            <div class="features-grid">
              <div v-for="(value, key) in config.features" :key="key" class="feature-item glass-panel">
                <div class="feature-info">
                  <span class="feature-name">{{ getFeatureName(key) }}</span>
                  <span class="feature-desc">{{ getFeatureDesc(key) }}</span>
                </div>
                <el-switch v-model="config.features[key]" />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 业务规则 -->
        <el-tab-pane name="rules">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><List /></el-icon>
              <span>业务规则</span>
            </span>
          </template>
          
          <div class="tab-content">
            <div class="section-header">
              <h3 class="section-title">业务逻辑参数设置</h3>
              <el-button type="primary" round v-particles @click="saveConfig('rules')">
                <el-icon class="mr-2"><Check /></el-icon> 保存配置
              </el-button>
            </div>
            
            <el-form 
              :model="config.rules" 
              :rules="configRules.rules" 
              ref="rulesFormRef"
              label-width="160px"
              class="custom-form"
            >
              <el-form-item label="最大家庭成员数" prop="maxFamilyMembers">
                <el-input-number v-model="config.rules.maxFamilyMembers" :min="1" :max="20" controls-position="right" />
                <span class="form-help">单个家庭最多可包含的成员数量</span>
              </el-form-item>
              
              <el-form-item label="日志保留天数" prop="logRetentionDays">
                <el-input-number v-model="config.rules.logRetentionDays" :min="30" :max="365" controls-position="right" />
                <span class="form-help">健康日志数据保留的天数</span>
              </el-form-item>
              
              <el-form-item label="提醒频率限制" prop="reminderFrequency">
                <el-input-number v-model="config.rules.reminderFrequency" :min="1" :max="24" controls-position="right" />
                <span class="form-help">每小时最多可发送的提醒数量</span>
              </el-form-item>
              
              <el-form-item label="密码有效期(天)" prop="passwordExpiryDays">
                <el-input-number v-model="config.rules.passwordExpiryDays" :min="30" :max="365" controls-position="right" />
                <span class="form-help">用户密码的有效期</span>
              </el-form-item>
              
              <el-form-item label="会话超时(分钟)" prop="sessionTimeout">
                <el-input-number v-model="config.rules.sessionTimeout" :min="15" :max="1440" controls-position="right" />
                <span class="form-help">用户会话的超时时间</span>
              </el-form-item>
              
              <el-form-item label="数据备份频率" prop="backupFrequency">
                <el-select v-model="config.rules.backupFrequency" placeholder="请选择备份频率">
                  <el-option label="每天" value="daily" />
                  <el-option label="每周" value="weekly" />
                  <el-option label="每月" value="monthly" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- API配置 -->
        <el-tab-pane name="api">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Connection /></el-icon>
              <span>API配置</span>
            </span>
          </template>
          
          <div class="tab-content">
            <div class="section-header">
              <h3 class="section-title">接口与传输限制</h3>
              <el-button type="primary" round v-particles @click="saveConfig('api')">
                <el-icon class="mr-2"><Check /></el-icon> 保存配置
              </el-button>
            </div>
            
            <el-form 
              :model="config.api" 
              :rules="configRules.api" 
              ref="apiFormRef"
              label-width="160px"
              class="custom-form"
            >
              <el-form-item label="请求频率限制" prop="rateLimit">
                <el-input-number v-model="config.api.rateLimit" :min="1" :max="10000" controls-position="right" />
                <span class="form-help">每分钟每个IP的请求限制</span>
              </el-form-item>
              
              <el-form-item label="请求大小限制(MB)" prop="requestSizeLimit">
                <el-input-number v-model="config.api.requestSizeLimit" :min="1" :max="100" controls-position="right" />
                <span class="form-help">单个请求的最大大小限制</span>
              </el-form-item>
              
              <el-form-item label="文件上传限制(MB)" prop="fileUploadLimit">
                <el-input-number v-model="config.api.fileUploadLimit" :min="1" :max="100" controls-position="right" />
                <span class="form-help">单个文件上传的最大大小限制</span>
              </el-form-item>
              
              <el-form-item label="API超时时间(秒)" prop="apiTimeout">
                <el-input-number v-model="config.api.apiTimeout" :min="5" :max="300" controls-position="right" />
                <span class="form-help">API请求的超时时间</span>
              </el-form-item>
              
              <el-form-item label="启用API日志">
                <el-switch v-model="config.api.enableApiLog" />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 通知配置 -->
        <el-tab-pane name="notifications">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Bell /></el-icon>
              <span>通知配置</span>
            </span>
          </template>
          
          <div class="tab-content">
            <div class="section-header">
              <h3 class="section-title">邮件与短信通知服务</h3>
              <el-button type="primary" round v-particles @click="saveConfig('notifications')">
                <el-icon class="mr-2"><Check /></el-icon> 保存配置
              </el-button>
            </div>
            
            <el-form 
              :model="config.notifications" 
              label-width="120px"
              label-position="top"
              class="custom-form two-column-form"
            >
              <!-- 邮件配置 -->
              <div class="form-section-title full-width">
                <el-icon><Message /></el-icon> 邮件服务
              </div>
              
              <el-form-item label="启用邮件通知" class="full-width">
                <el-switch v-model="config.notifications.emailEnabled" />
              </el-form-item>
              
              <template v-if="config.notifications.emailEnabled">
                <el-form-item label="SMTP服务器">
                  <el-input v-model="config.notifications.smtpServer" placeholder="例如: smtp.example.com" />
                </el-form-item>
                
                <el-form-item label="SMTP端口">
                  <el-input-number v-model="config.notifications.smtpPort" :min="1" :max="65535" controls-position="right" class="w-100" />
                </el-form-item>
                
                <el-form-item label="邮箱账号">
                  <el-input v-model="config.notifications.emailAccount" placeholder="请输入邮箱账号" />
                </el-form-item>
                
                <el-form-item label="邮箱密码">
                  <el-input v-model="config.notifications.emailPassword" type="password" show-password placeholder="请输入邮箱密码" />
                </el-form-item>
              </template>

              <!-- 短信配置 -->
              <div class="form-section-title full-width mt-4">
                <el-icon><ChatDotSquare /></el-icon> 短信服务
              </div>

              <el-form-item label="启用短信通知" class="full-width">
                <el-switch v-model="config.notifications.smsEnabled" />
              </el-form-item>
              
              <template v-if="config.notifications.smsEnabled">
                <el-form-item label="短信服务商">
                  <el-select v-model="config.notifications.smsProvider" placeholder="请选择短信服务商" class="w-100">
                    <el-option label="阿里云" value="aliyun" />
                    <el-option label="腾讯云" value="tencent" />
                    <el-option label="华为云" value="huawei" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="API密钥 (AccessKey)">
                  <el-input v-model="config.notifications.smsApiKey" placeholder="请输入API密钥" />
                </el-form-item>
                
                <el-form-item label="API密钥 (Secret)" class="full-width">
                  <el-input v-model="config.notifications.smsApiSecret" type="password" show-password placeholder="请输入API密钥Secret" />
                </el-form-item>
              </template>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Download, Upload, RefreshLeft, Operation, Switch, 
  List, Connection, Bell, Check, Monitor, Message, Phone,
  ChatDotSquare, Refresh
} from '@element-plus/icons-vue'
import { getSystemConfig, updateSystemConfig, backupSystemConfig, restoreSystemConfig, resetSystemConfig } from '../../../api/admin'

// 活跃标签页
const activeTab = ref('basic')

// 配置数据
const config = reactive({
  basic: {
    systemName: '家庭健康管理系统',
    systemDesc: '一个专注于家庭健康管理的系统，提供健康日志、体质测评、医生协作等功能',
    logo: '',
    contactEmail: 'admin@healthfamily.com',
    contactPhone: '400-123-4567',
    systemStatus: true,
    maintenanceDesc: '系统维护中，请稍后再试'
  },
  features: {
    userRegistration: true,
    familyCreation: true,
    healthLogs: true,
    assessment: true,
    healthReminders: true,
    aiRecommendations: true,
    doctorCollaboration: true,
    dataExport: true
  },
  rules: {
    maxFamilyMembers: 10,
    logRetentionDays: 180,
    reminderFrequency: 5,
    passwordExpiryDays: 90,
    sessionTimeout: 120,
    backupFrequency: 'daily'
  },
  api: {
    rateLimit: 1000,
    requestSizeLimit: 10,
    fileUploadLimit: 10,
    apiTimeout: 30,
    enableApiLog: true
  },
  notifications: {
    emailEnabled: true,
    smtpServer: 'smtp.example.com',
    smtpPort: 587,
    emailAccount: 'noreply@healthfamily.com',
    emailPassword: '',
    smsEnabled: false,
    smsProvider: 'aliyun',
    smsApiKey: '',
    smsApiSecret: ''
  }
})

// 配置验证规则
const configRules = {
  basic: {
    systemName: [
      { required: true, message: '请输入系统名称', trigger: 'blur' },
      { min: 2, max: 50, message: '系统名称长度在2-50个字符', trigger: 'blur' }
    ],
    contactEmail: [
      { required: true, message: '请输入联系邮箱', trigger: 'blur' },
      { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
    ],
    contactPhone: [
      { required: true, message: '请输入联系电话', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/, message: '电话格式不正确', trigger: 'blur' }
    ]
  },
  rules: {
    maxFamilyMembers: [
      { required: true, message: '请输入最大家庭成员数', trigger: 'blur' }
    ],
    logRetentionDays: [
      { required: true, message: '请输入日志保留天数', trigger: 'blur' }
    ],
    reminderFrequency: [
      { required: true, message: '请输入提醒频率限制', trigger: 'blur' }
    ],
    passwordExpiryDays: [
      { required: true, message: '请输入密码有效期', trigger: 'blur' }
    ],
    sessionTimeout: [
      { required: true, message: '请输入会话超时时间', trigger: 'blur' }
    ]
  },
  api: {
    rateLimit: [
      { required: true, message: '请输入请求频率限制', trigger: 'blur' }
    ],
    requestSizeLimit: [
      { required: true, message: '请输入请求大小限制', trigger: 'blur' }
    ],
    fileUploadLimit: [
      { required: true, message: '请输入文件上传限制', trigger: 'blur' }
    ],
    apiTimeout: [
      { required: true, message: '请输入API超时时间', trigger: 'blur' }
    ]
  }
}

// 表单引用
const basicFormRef = ref(null)
const rulesFormRef = ref(null)
const apiFormRef = ref(null)

// 上传Logo
const handleLogoSuccess = (response, file) => {
  config.basic.logo = URL.createObjectURL(file.raw)
}

const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 保存配置
const saveConfig = async (tab) => {
  let formRef = null
  let configSection = null
  
  switch(tab) {
    case 'basic':
      formRef = basicFormRef.value
      configSection = 'basic'
      break
    case 'rules':
      formRef = rulesFormRef.value
      configSection = 'rules'
      break
    case 'api':
      formRef = apiFormRef.value
      configSection = 'api'
      break
    default:
      configSection = tab
  }
  
  if (formRef) {
    await formRef.validate(async (valid) => {
      if (valid) {
        await submitConfig(configSection)
      }
    })
  } else {
    await submitConfig(configSection)
  }
}

// 提交配置
const submitConfig = async (section) => {
  try {
    await updateSystemConfig(config)
    ElMessage.success(`${getSectionName(section)}配置保存成功`)
  } catch (error) {
    ElMessage.error(`${getSectionName(section)}配置保存失败`)
    console.error('Error submitting config:', error)
  }
}

// 获取配置区域名称
const getSectionName = (section) => {
  const sectionMap = {
    basic: '基础',
    features: '功能开关',
    rules: '业务规则',
    api: 'API',
    notifications: '通知'
  }
  return sectionMap[section] || section
}

// 获取功能名称
const getFeatureName = (key) => {
  const nameMap = {
    userRegistration: '用户注册',
    familyCreation: '家庭创建',
    healthLogs: '健康日志',
    assessment: '体质测评',
    healthReminders: '健康提醒',
    aiRecommendations: '智能建议',
    doctorCollaboration: '医生协作',
    dataExport: '数据导出'
  }
  return nameMap[key] || key
}

// 获取功能描述
const getFeatureDesc = (key) => {
  const descMap = {
    userRegistration: '允许新用户注册账号',
    familyCreation: '允许用户创建新的家庭组',
    healthLogs: '开启健康日志记录功能',
    assessment: '开启中医体质测评功能',
    healthReminders: '开启健康打卡提醒功能',
    aiRecommendations: '开启AI健康建议功能',
    doctorCollaboration: '允许医生介入管理',
    dataExport: '允许用户导出健康数据'
  }
  return descMap[key] || '暂无描述'
}

// 加载系统配置
const loadSystemConfig = async () => {
  try {
    const response = await getSystemConfig()
    // 更新配置数据
    Object.assign(config, response.data)
  } catch (error) {
    console.error('加载系统配置失败:', error)
    ElMessage.error('加载系统配置失败')
  }
}

onMounted(() => {
  loadSystemConfig()
})
</script>

<style scoped lang="scss">
@use 'sass:map';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.system-config {
  min-height: 100%;
  padding-bottom: 2rem;

  .page-header {
    margin-bottom: 2rem;
    
    .page-title {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
      @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info')));
    }
    
    .page-subtitle {
      color: map.get(vars.$colors, 'text-secondary');
      font-size: 1rem;
    }
  }

  // 玻璃态Tabs样式
  :deep(.glass-tabs) {
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 16px;
    padding: 1rem;
    
    .el-tabs__header {
      background: rgba(255, 255, 255, 0.5);
      border-radius: 12px;
      padding: 0.5rem;
      border: none;
      margin-bottom: 2rem;
    }

    .el-tabs__item {
      border: none;
      border-radius: 8px;
      margin-right: 0.5rem;
      transition: all 0.3s ease;
      height: 44px;
      color: map.get(vars.$colors, 'text-secondary');
      
      &.is-active {
        background: #fff;
        color: map.get(vars.$colors, 'primary');
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        font-weight: 600;
      }

      &:hover:not(.is-active) {
        background: rgba(255, 255, 255, 0.6);
        color: map.get(vars.$colors, 'primary');
      }
    }

    .el-tabs__content {
      padding: 1rem;
    }
  }

  .custom-tab-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-weight: 500;
    
    .el-icon {
      font-size: 1.1em;
    }
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 1px dashed rgba(0, 0, 0, 0.1);

    .section-title {
      font-size: 1.25rem;
      font-weight: 600;
      color: map.get(vars.$colors, 'text-main');
      position: relative;
      padding-left: 1rem;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 1.2em;
        background: map.get(vars.$colors, 'primary');
        border-radius: 2px;
      }
    }
  }

  .custom-form {
    &.two-column-form {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1.5rem;

      .full-width {
        grid-column: span 2;
      }
    }

    .form-section-title {
      font-size: 1rem;
      font-weight: 600;
      color: map.get(vars.$colors, 'text-secondary');
      margin: 1.5rem 0 1rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      
      .el-icon {
        color: map.get(vars.$colors, 'primary');
      }
    }

    .form-help {
      display: block;
      font-size: 0.85rem;
      color: map.get(vars.$colors, 'text-secondary');
      margin-top: 0.25rem;
      line-height: 1.4;
    }

    :deep(.el-form-item__label) {
      font-weight: 500;
      color: map.get(vars.$colors, 'text-main');
    }
  }

  // 功能开关网格
  .features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.5rem;

    .feature-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-radius: 12px;
      transition: all 0.3s ease;

      &.glass-panel {
        background: rgba(255, 255, 255, 0.4);
        border: 1px solid rgba(255, 255, 255, 0.2);

        &:hover {
          background: rgba(255, 255, 255, 0.7);
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        }
      }

      .feature-info {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;

        .feature-name {
          font-weight: 600;
          color: map.get(vars.$colors, 'text-main');
        }

        .feature-desc {
          font-size: 0.85rem;
          color: map.get(vars.$colors, 'text-secondary');
        }
      }
    }
  }

  // 上传组件样式
  .glass-uploader {
    :deep(.el-upload) {
      width: 100%;
      border: 2px dashed rgba(0, 0, 0, 0.1);
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.3);
      transition: all 0.3s ease;

      &:hover {
        border-color: map.get(vars.$colors, 'primary');
        background: rgba(255, 255, 255, 0.5);
      }
    }

    .uploader-content {
      padding: 2rem;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      color: map.get(vars.$colors, 'text-secondary');

      .uploader-icon {
        font-size: 2rem;
        transition: transform 0.3s ease;
      }

      &:hover .uploader-icon {
        transform: scale(1.1);
        color: map.get(vars.$colors, 'primary');
      }
    }

    .logo {
      width: 100%;
      max-width: 200px;
      height: auto;
      border-radius: 8px;
    }
  }

  // 备份卡片
  .glass-card {
    @include mixins.glass-effect;
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 16px;

    .card-header {
      display: flex;
      align-items: center;
      
      .header-title {
        font-size: 1.1rem;
        font-weight: 600;
        display: flex;
        align-items: center;
        color: map.get(vars.$colors, 'text-main');
      }
    }

    .backup-actions {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
      padding: 1rem 0;
    }
  }

  // 动画
  .stagger-anim {
    opacity: 0;
    animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    animation-delay: var(--delay, 0s);
  }

  @keyframes slideUpFade {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // 响应式
  @media (max-width: 768px) {
    .custom-form.two-column-form {
      grid-template-columns: 1fr;
      
      .full-width {
        grid-column: span 1;
      }
    }

    .glass-tabs {
      :deep(.el-tabs__item) {
        padding: 0 1rem;
      }
    }
  }
}
</style>