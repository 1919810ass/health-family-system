<template>
  <div class="security-config">
    <!-- Header -->
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">安全设置</h1>
        <p class="page-subtitle">配置系统安全策略、访问控制与AI治理规则</p>
      </div>
    </div>

    <div class="stagger-anim" style="--delay: 0.2s">
      <el-tabs v-model="activeTab" type="border-card" class="glass-tabs">
        
        <!-- 账号安全 -->
        <el-tab-pane name="account">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Lock /></el-icon>
              <span>账号安全</span>
            </span>
          </template>
          
          <div class="tab-content">
            <div class="section-header">
              <h3 class="section-title">登录与密码策略</h3>
              <el-button type="primary" round @click="saveConfig('account')">
                <el-icon class="mr-2"><Check /></el-icon> 保存配置
              </el-button>
            </div>
            
            <el-form :model="config.account" label-width="160px" class="custom-form">
              <el-form-item label="登录失败最大重试次数">
                <el-input-number v-model="config.account.loginMaxRetries" :min="3" :max="10" />
                <span class="form-help">超过次数后账号将被临时锁定</span>
              </el-form-item>
              
              <el-form-item label="账号锁定时间(分钟)">
                <el-input-number v-model="config.account.loginLockMinutes" :min="5" :max="1440" :step="5" />
                <span class="form-help">触发锁定后的自动解锁时间</span>
              </el-form-item>
              
              <el-form-item label="密码最小长度">
                <el-input-number v-model="config.account.passwordMinLength" :min="6" :max="32" />
              </el-form-item>

              <el-form-item label="强制定期修改密码">
                <el-switch v-model="config.account.passwordExpiryEnabled" />
                <span class="ml-2 form-help">每90天强制用户修改密码</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- AI安全治理 -->
        <el-tab-pane name="ai">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Cpu /></el-icon>
              <span>AI安全治理</span>
            </span>
          </template>
          
          <div class="tab-content">
            <div class="section-header">
              <h3 class="section-title">大模型输出控制</h3>
              <el-button type="primary" round @click="saveConfig('ai')">
                <el-icon class="mr-2"><Check /></el-icon> 保存配置
              </el-button>
            </div>
            
            <el-form :model="config.ai" label-width="160px" class="custom-form">
              <el-form-item label="启用AI建议功能">
                <el-switch v-model="config.ai.safetyEnabled" active-text="开启" inactive-text="关闭" />
                <span class="form-help">全局控制是否允许调用AI模型生成建议</span>
              </el-form-item>
              
              <el-form-item label="每日调用上限(次/人)">
                <el-input-number v-model="config.ai.dailyLimit" :min="10" :max="1000" :step="10" />
                <span class="form-help">限制单个用户每日可调用的AI次数，防止滥用</span>
              </el-form-item>
              
              <el-form-item label="敏感内容过滤等级">
                <el-select v-model="config.ai.contentFilterLevel">
                  <el-option label="宽松" value="low" />
                  <el-option label="标准" value="medium" />
                  <el-option label="严格" value="high" />
                </el-select>
                <span class="form-help">控制AI输出医疗建议的保守程度</span>
              </el-form-item>

              <el-alert
                title="安全提示"
                type="warning"
                description="AI生成的健康建议仅供参考，系统会自动在输出中附加免责声明。请确保'严格'模式已启用以减少幻觉风险。"
                show-icon
                :closable="false"
                class="mt-4"
              />
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 审计日志 -->
        <el-tab-pane name="audit">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Document /></el-icon>
              <span>审计合规</span>
            </span>
          </template>
          
          <div class="tab-content">
            <div class="section-header">
              <h3 class="section-title">日志与留存策略</h3>
              <el-button type="primary" round @click="saveConfig('audit')">
                <el-icon class="mr-2"><Check /></el-icon> 保存配置
              </el-button>
            </div>
            
            <el-form :model="config.audit" label-width="160px" class="custom-form">
               <el-form-item label="开启操作审计">
                <el-switch v-model="config.audit.enabled" />
              </el-form-item>
              
              <el-form-item label="日志保留时间(月)">
                <el-input-number v-model="config.audit.retentionMonths" :min="3" :max="36" />
                <span class="form-help">满足网络安全法要求的日志留存时间（建议至少6个月）</span>
              </el-form-item>
              
              <el-form-item label="记录敏感操作详情">
                <el-switch v-model="config.audit.recordDetail" />
                <span class="form-help">是否记录修改前后的具体数据快照（占用较多存储）</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, Cpu, Document, Check } from '@element-plus/icons-vue'
import { getSystemConfig, updateSystemConfig } from '../../../api/admin'

const activeTab = ref('account')

// 本地状态结构
const config = reactive({
  account: {
    loginMaxRetries: 5,
    loginLockMinutes: 30,
    passwordMinLength: 8,
    passwordExpiryEnabled: false
  },
  ai: {
    safetyEnabled: true,
    dailyLimit: 100,
    contentFilterLevel: 'medium'
  },
  audit: {
    enabled: true,
    retentionMonths: 6,
    recordDetail: true
  }
})

// 键名映射表：前端Key -> 后端Key
const keyMapping = {
  // Account
  'config.account.loginMaxRetries': 'security.login.max_retries',
  'config.account.loginLockMinutes': 'security.login.lock_minutes',
  'config.account.passwordMinLength': 'security.password.min_length',
  'config.account.passwordExpiryEnabled': 'security.password.expiry_enabled',
  // AI
  'config.ai.safetyEnabled': 'ai.safety.enabled',
  'config.ai.dailyLimit': 'ai.safety.daily_limit',
  'config.ai.contentFilterLevel': 'ai.safety.content_filter_level',
  // Audit
  'config.audit.enabled': 'audit.config.enabled',
  'config.audit.retentionMonths': 'audit.config.retention_months',
  'config.audit.recordDetail': 'audit.config.record_detail'
}

// 加载配置
const loadConfig = async () => {
  try {
    const res = await getSystemConfig()
    const serverData = res.data || {}
    
    // 反向映射：后端Key -> 前端State
    // 这里需要遍历 mapping，如果 serverData 有值则更新
    for (const [uiKey, serverKey] of Object.entries(keyMapping)) {
      if (serverData[serverKey] !== undefined) {
        // uiKey like 'config.account.loginMaxRetries'
        const parts = uiKey.split('.')
        // parts[0] is 'config', parts[1] is section, parts[2] is field
        if (parts.length === 3) {
          // 处理类型转换，后端全是字符串
          let val = serverData[serverKey]
          
          // 尝试解析 JSON 字符串 (因为 OpsService 有 parseJsonSafe)
          // 但 AuthServiceImpl 里我们用 replace("\"", "") 简单处理了
          // 这里如果是纯数字字符串，直接转换
          if (!isNaN(val)) {
            val = Number(val)
          } else if (val === 'true') {
            val = true
          } else if (val === 'false') {
            val = false
          }
          
          config[parts[1]][parts[2]] = val
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载安全配置失败')
  }
}

// 保存配置
const saveConfig = async (section) => {
  try {
    const payload = {}
    // 只收集当前 section 的配置
    for (const [uiKey, serverKey] of Object.entries(keyMapping)) {
      if (uiKey.startsWith(`config.${section}.`)) {
        const parts = uiKey.split('.')
        const val = config[parts[1]][parts[2]]
        // 转为字符串发送，保持后端一致性
        payload[serverKey] = String(val)
      }
    }
    
    await updateSystemConfig(payload)
    ElMessage.success('配置已保存并生效')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.security-config {
  min-height: 100%;
  padding-bottom: 2rem;

  .page-header {
    margin-bottom: 2rem;
    .page-title {
      font-size: 1.8rem;
      font-weight: 700;
      color: #303133;
    }
    .page-subtitle {
      color: #909399;
      margin-top: 0.5rem;
    }
  }

  :deep(.glass-tabs) {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    
    .el-tabs__header {
      background: #f5f7fa;
      border-radius: 12px 12px 0 0;
      padding: 0.5rem 1rem;
    }
    
    .el-tabs__content {
      padding: 2rem;
    }
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #ebeef5;
    
    .section-title {
      font-size: 1.2rem;
      font-weight: 600;
      color: #303133;
      border-left: 4px solid #409EFF;
      padding-left: 12px;
    }
  }

  .custom-form {
    max-width: 800px;
    
    .form-help {
      font-size: 12px;
      color: #909399;
      margin-left: 10px;
    }
  }
  
  .custom-tab-label {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  
  .stagger-anim {
    animation: fadeInUp 0.5s ease forwards;
    opacity: 0;
    animation-delay: var(--delay);
  }
  
  @keyframes fadeInUp {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
  }
}
</style>