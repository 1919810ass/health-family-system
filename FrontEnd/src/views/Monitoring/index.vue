<template>
  <div class="monitor-container" v-loading="loading">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-icon">
        <el-icon><Monitor /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">健康监测与预警</h2>
        <p class="subtitle">实时监控健康指标，智能预警潜在风险</p>
      </div>
    </div>

    <el-alert 
      class="info-alert stagger-anim" 
      style="--delay: 0.2s"
      type="info" 
      :closable="false"
      show-icon
    >
      <template #title>
        <span class="alert-title">使用提示</span>
      </template>
      <template #default>
        需有有效的用户ID与家庭ID；若数值未超过阈值不会产生预警。
      </template>
    </el-alert>

    <el-row :gutter="24" class="content-row">
      <el-col :xs="24" :lg="10" class="mb-24">
        <div class="glass-card stagger-anim" style="--delay: 0.3s">
          <div class="card-header">
            <div class="header-left">
              <div class="icon-box warning">
                <el-icon><BellFilled /></el-icon>
              </div>
              <span class="title">实时预警</span>
            </div>
            <el-tag type="danger" effect="dark" round size="small" v-if="alerts.length">{{ alerts.length }}</el-tag>
          </div>
          
          <div class="card-content">
            <el-table 
              :data="alerts" 
              style="width: 100%" 
              v-loading="alertsLoading"
              class="glass-table"
            >
              <el-table-column prop="metric" label="指标" min-width="80">
                <template #default="{ row }">
                  <span class="metric-tag">{{ row.metric }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="value" label="数值" min-width="70">
                <template #default="{ row }">
                  <span class="value-text danger">{{ row.value }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="severity" label="级别" min-width="70">
                <template #default="{ row }">
                  <el-tag :type="getSeverityType(row.severity)" size="small" effect="light" round>
                    {{ row.severity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button 
                    size="small" 
                    type="primary" 
                    circle
                    @click="ack(row)" 
                    :disabled="row.status==='ACKED'"
                    v-particles
                  >
                    <el-icon><Check /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="empty-state" v-if="!alerts.length && !alertsLoading">
              <el-empty description="暂无预警信息" :image-size="80" />
            </div>
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="14">
        <div class="glass-card stagger-anim mb-24" style="--delay: 0.4s">
          <div class="card-header">
            <div class="header-left">
              <div class="icon-box primary">
                <el-icon><Operation /></el-icon>
              </div>
              <span class="title">阈值与优化</span>
            </div>
            <el-button type="primary" link :loading="optLoading" @click="optimize" class="opt-btn" v-particles>
              <el-icon class="mr-4"><MagicStick /></el-icon>
              AI智能优化
            </el-button>
          </div>
          
          <div class="card-content">
            <el-table 
              :data="thresholds" 
              style="width: 100%" 
              v-loading="thresholdsLoading"
              class="glass-table"
            >
              <el-table-column prop="metric" label="指标" min-width="100">
                <template #default="{ row }">
                  <div class="metric-item">
                    <div class="dot"></div>
                    {{ row.metric }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="lowerBound" label="下限" min-width="100">
                <template #default="{ row }">
                  <span class="threshold-value">{{ row.lowerBound }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="upperBound" label="上限" min-width="100">
                <template #default="{ row }">
                  <span class="threshold-value">{{ row.upperBound }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <div class="glass-card stagger-anim" style="--delay: 0.5s">
          <div class="card-header">
            <div class="header-left">
              <div class="icon-box success">
                <el-icon><DataLine /></el-icon>
              </div>
              <span class="title">数据接入模拟</span>
            </div>
          </div>
          
          <div class="card-content">
            <el-form :model="form" label-position="top" class="custom-form">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户ID">
                    <el-input v-model="form.userId" placeholder="输入用户ID">
                      <template #prefix><el-icon><User /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="家庭ID">
                    <el-input v-model="form.familyId" placeholder="输入家庭ID">
                      <template #prefix><el-icon><House /></el-icon></template>
                      <template #append>
                        <el-button @click="saveFamily">保存</el-button>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="监测指标">
                    <el-select v-model="form.metric" style="width: 100%" placeholder="选择指标">
                      <template #prefix><el-icon><Odometer /></el-icon></template>
                      <el-option label="心率 (HR)" value="HR" />
                      <el-option label="收缩压 (BP_SYS)" value="BP_SYS" />
                      <el-option label="舒张压 (BP_DIA)" value="BP_DIA" />
                      <el-option label="血糖 (GLU)" value="GLU" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="监测数值">
                    <el-input-number v-model="form.value" :min="0" :max="300" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions">
                <el-button @click="fillCritical" class="action-btn" plain>
                  <el-icon class="mr-4"><Warning /></el-icon>
                  生成危急值
                </el-button>
                <el-button type="primary" :loading="ingestLoading" @click="ingest" class="action-btn" v-particles>
                  <el-icon class="mr-4"><Upload /></el-icon>
                  发送数据
                </el-button>
              </div>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor, BellFilled, Operation, MagicStick, Check, DataLine, User, House, Odometer, Upload, Warning } from '@element-plus/icons-vue'
import { getAlerts, ackAlert, getThresholds, optimizeThresholds, ingestTelemetry } from '../../api/monitor'
import request from '../../utils/request'

const loading = ref(false)
const alertsLoading = ref(false)
const thresholdsLoading = ref(false)
const optLoading = ref(false)
const ingestLoading = ref(false)

const alerts = ref([])
const thresholds = ref([])

const form = ref({
  userId: '',
  familyId: localStorage.getItem('current_family_id'),
  metric: 'HR',
  value: 80,
})

const getSeverityType = (severity) => {
  const map = {
    'CRITICAL': 'danger',
    'HIGH': 'warning',
    'MEDIUM': 'warning',
    'LOW': 'info'
  }
  return map[severity] || 'info'
}

const loadAlerts = async () => {
  alertsLoading.value = true
  try {
    const familyId = form.value.familyId
    const params = familyId && familyId !== 'null' ? { familyId } : {}
    const res = await getAlerts(params)
    alerts.value = res?.data || []
  } catch (e) {
    ElMessage.error('加载预警失败')
  } finally {
    alertsLoading.value = false
  }
}

const loadThresholds = async () => {
  thresholdsLoading.value = true
  try {
    const res = await getThresholds()
    thresholds.value = res?.data || []
  } catch (e) {
    ElMessage.error('加载阈值失败')
  } finally {
    thresholdsLoading.value = false
  }
}

const optimize = async () => {
  optLoading.value = true
  try {
    await optimizeThresholds()
    ElMessage.success('已优化阈值')
    await loadThresholds()
  } catch (e) {
    ElMessage.error('优化失败')
  } finally {
    optLoading.value = false
  }
}

const ack = async (row) => {
  try {
    await ackAlert(row.id)
    ElMessage.success('已确认')
    await loadAlerts()
  } catch (e) {
    ElMessage.error('确认失败')
  }
}

const ingest = async () => {
  ingestLoading.value = true
  try {
    if (!form.value.userId) {
      ElMessage.warning('请先填写用户ID')
      ingestLoading.value = false
      return
    }
    const payload = { ...form.value, value: Number(form.value.value) }
    await ingestTelemetry(payload)
    ElMessage.success('已接入')
    await loadAlerts()
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    ingestLoading.value = false
  }
}

const saveFamily = () => {
  try { 
    localStorage.setItem('current_family_id', String(form.value.familyId || ''))
    ElMessage.success('家庭ID已保存')
  } catch {}
}

const fillCritical = () => {
  if (form.value.metric === 'GLU') form.value.value = 15
  else if (form.value.metric === 'HR') form.value.value = 160
  else if (form.value.metric === 'BP_SYS') form.value.value = 200
  else if (form.value.metric === 'BP_DIA') form.value.value = 120
}

onMounted(async () => {
  loading.value = true
  try {
    try {
      const prof = await request.get('/user/profile')
      if (prof?.data?.id) form.value.userId = String(prof.data.id)
      if (!form.value.familyId || form.value.familyId === 'null') {
        const stored = localStorage.getItem('current_family_id')
        if (stored) form.value.familyId = stored
      }
    } catch {}
    await Promise.all([loadAlerts(), loadThresholds()])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.monitor-container {
  min-height: 100%;
  padding: 24px;
  background: transparent;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;

  .header-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    background: linear-gradient(135deg, vars.$primary-color, vars.$secondary-color);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);

    .el-icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .header-content {
    .title {
      font-size: 24px;
      font-weight: 700;
      color: vars.$text-primary-color;
      margin: 0 0 4px 0;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-primary-color, vars.$primary-color));
    }

    .subtitle {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

.info-alert {
  border-radius: vars.$radius-md;
  margin-bottom: 24px;
  border: 1px solid rgba(vars.$primary-color, 0.1);
  background: rgba(vars.$primary-color, 0.05);

  .alert-title {
    font-weight: 600;
  }
}

.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 20px;
  height: 100%;
  transition: all 0.3s vars.$ease-spring;

  &:hover {
    transform: translateY(-4px);
    box-shadow: vars.$shadow-lg;
    border-color: rgba(255, 255, 255, 0.9);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid rgba(vars.$text-primary-color, 0.05);

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .icon-box {
        width: 36px;
        height: 36px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.warning {
          background: rgba(vars.$danger-color, 0.1);
          color: vars.$danger-color;
        }
        
        &.primary {
          background: rgba(vars.$primary-color, 0.1);
          color: vars.$primary-color;
        }

        &.success {
          background: rgba(vars.$success-color, 0.1);
          color: vars.$success-color;
        }

        .el-icon {
          font-size: 18px;
        }
      }

      .title {
        font-size: 16px;
        font-weight: 600;
        color: vars.$text-primary-color;
      }
    }
  }
}

.glass-table {
  background: transparent !important;
  
  :deep(th.el-table__cell) {
    background: transparent;
    color: vars.$text-secondary-color;
    font-weight: 500;
  }
  
  :deep(tr) {
    background: transparent;
    
    &:hover td.el-table__cell {
      background: rgba(vars.$primary-color, 0.05) !important;
    }
  }

  :deep(td.el-table__cell) {
    border-bottom: 1px solid rgba(vars.$text-primary-color, 0.05);
  }
}

.metric-tag {
  display: inline-block;
  padding: 2px 8px;
  background: rgba(vars.$primary-color, 0.1);
  color: vars.$primary-color;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.value-text {
  font-weight: 600;
  font-family: 'Roboto Mono', monospace;
  
  &.danger {
    color: vars.$danger-color;
  }
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: vars.$text-primary-color;
  
  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: vars.$primary-color;
  }
}

.threshold-value {
  font-family: 'Roboto Mono', monospace;
  color: vars.$text-secondary-color;
  background: rgba(vars.$text-primary-color, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.custom-form {
  :deep(.el-form-item__label) {
    color: vars.$text-secondary-color;
    padding-bottom: 8px;
  }
}

.form-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  
  .action-btn {
    flex: 1;
    height: 40px;
    border-radius: vars.$radius-md;
    font-weight: 600;
  }
}

.mr-4 { margin-right: 4px; }
.mb-24 { margin-bottom: 24px; }

.stagger-anim {
  opacity: 0;
  animation: fadeInUp 0.6s vars.$ease-spring forwards;
  animation-delay: var(--delay, 0s);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .monitor-container {
    padding: 16px;
  }
  
  .glass-card {
    padding: 16px;
  }
}
</style>
  align-items: center;
  gap: 8px;
  
  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: vars.$primary-color;
  }
}

.threshold-value {
  font-family: 'Roboto Mono', monospace;
  color: vars.$text-primary-color;
  background: rgba(255, 255, 255, 0.5);
  padding: 2px 6px;
  border-radius: 4px;
}

.custom-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: vars.$text-secondary-color;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper),
  :deep(.el-input-number__wrapper) {
    background: rgba(255, 255, 255, 0.5);
    box-shadow: none;
    border: 1px solid rgba(vars.$text-primary-color, 0.1);
    border-radius: vars.$radius-md;
    transition: all 0.3s ease;

    &:hover, &.is-focus {
      background: #fff;
      border-color: vars.$primary-color;
      box-shadow: 0 0 0 3px rgba(vars.$primary-color, 0.1);
    }
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;

  .action-btn {
    border-radius: vars.$radius-md;
    padding: 10px 20px;
    height: auto;
    font-weight: 500;
    
    &:not(.el-button--primary) {
      &:hover {
        color: vars.$primary-color;
        border-color: vars.$primary-color;
        background: rgba(vars.$primary-color, 0.05);
      }
    }
  }
}

.mr-4 { margin-right: 4px; }
.mb-24 { margin-bottom: 24px; }

.stagger-anim {
  opacity: 0;
  animation: slideInUp 0.6s vars.$ease-spring forwards;
  animation-delay: var(--delay);
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .monitor-container {
    padding: 16px;
  }
  
  .content-row {
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  
  .glass-card {
    margin-bottom: 16px;
  }
}
</style>
