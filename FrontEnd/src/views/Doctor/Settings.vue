<template>
  <div class="doctor-settings">
    <el-page-header content="医生系统设置" />
    
    <!-- 通知设置 -->
    <el-card class="mt-16">
      <template #header>
        <span>消息通知设置</span>
        <el-text type="info" size="small" style="margin-left: 12px">可针对不同通知类型设置接收渠道</el-text>
      </template>
      
      <el-form :model="notifications" label-width="150px">
        <!-- 系统通知 -->
        <el-divider content-position="left">系统通知</el-divider>
        <el-form-item label="APP 内通知">
          <el-switch v-model="notifications.system.inApp" />
        </el-form-item>
        <el-form-item label="邮件通知">
          <el-switch v-model="notifications.system.email" />
        </el-form-item>
        <el-form-item label="短信通知">
          <el-switch v-model="notifications.system.sms" />
        </el-form-item>
        
        <!-- 随访提醒 -->
        <el-divider content-position="left">随访提醒</el-divider>
        <el-form-item label="APP 内通知">
          <el-switch v-model="notifications.followup.inApp" />
        </el-form-item>
        <el-form-item label="邮件通知">
          <el-switch v-model="notifications.followup.email" />
        </el-form-item>
        <el-form-item label="短信通知">
          <el-switch v-model="notifications.followup.sms" />
        </el-form-item>
        
        <!-- 预警通知 -->
        <el-divider content-position="left">预警通知</el-divider>
        <el-form-item label="APP 内通知">
          <el-switch v-model="notifications.alert.inApp" />
        </el-form-item>
        <el-form-item label="邮件通知">
          <el-switch v-model="notifications.alert.email" />
        </el-form-item>
        <el-form-item label="短信通知">
          <el-switch v-model="notifications.alert.sms" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="saveNotifications">保存通知设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 工作时间设置 -->
    <el-card class="mt-16">
      <template #header>
        <span>工作时间设置</span>
        <el-text type="info" size="small" style="margin-left: 12px">设置的工作时间会影响随访计划生成和推送发送窗口</el-text>
      </template>
      
      <el-form :model="workingHours" label-width="150px">
        <el-form-item label="工作日">
          <el-select v-model="workingHours.workDays" multiple placeholder="选择工作日" style="width: 400px">
            <el-option v-for="d in daysList" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作时间段">
          <el-time-picker
            v-model="timeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 400px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingWork" @click="saveWorkingHours">保存工作时间</el-button>
          <el-button @click="resetWorkingHours">重置为默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDoctorSettings, updateDoctorSettings } from '../../api/doctor'
import dayjs from 'dayjs'

const saving = ref(false)
const savingWork = ref(false)

// 通知设置
const notifications = ref({
  system: { inApp: true, email: false, sms: false },
  followup: { inApp: true, email: false, sms: false },
  alert: { inApp: true, email: false, sms: false }
})

// 工作时间设置
const daysList = [
  { label: '周一', value: 'MON' },
  { label: '周二', value: 'TUE' },
  { label: '周三', value: 'WED' },
  { label: '周四', value: 'THU' },
  { label: '周五', value: 'FRI' },
  { label: '周六', value: 'SAT' },
  { label: '周日', value: 'SUN' },
]

const workingHours = ref({
  workDays: ['MON', 'TUE', 'WED', 'THU', 'FRI'],
  startTime: '09:00',
  endTime: '18:00'
})

// 时间范围（用于时间选择器）
const timeRange = ref([])

// 同步时间范围到工作时间
watch(timeRange, (val) => {
  if (val && val.length === 2) {
    workingHours.value.startTime = val[0]
    workingHours.value.endTime = val[1]
  }
}, { immediate: true })

// 同步工作时间到时间范围
watch(() => [workingHours.value.startTime, workingHours.value.endTime], ([start, end]) => {
  if (start && end) {
    timeRange.value = [start, end]
  }
}, { immediate: true })

// 加载设置
const loadSettings = async () => {
  try {
    const res = await getDoctorSettings()
    const data = res?.data
    
    if (data?.notifications) {
      notifications.value = {
        system: {
          inApp: data.notifications.system?.inApp ?? true,
          email: data.notifications.system?.email ?? false,
          sms: data.notifications.system?.sms ?? false
        },
        followup: {
          inApp: data.notifications.followup?.inApp ?? true,
          email: data.notifications.followup?.email ?? false,
          sms: data.notifications.followup?.sms ?? false
        },
        alert: {
          inApp: data.notifications.alert?.inApp ?? true,
          email: data.notifications.alert?.email ?? false,
          sms: data.notifications.alert?.sms ?? false
        }
      }
    }
    
    if (data?.workingHours) {
      workingHours.value = {
        workDays: data.workingHours.workDays || ['MON', 'TUE', 'WED', 'THU', 'FRI'],
        startTime: data.workingHours.startTime || '09:00',
        endTime: data.workingHours.endTime || '18:00'
      }
    }
  } catch (error) {
    console.error('加载医生设置失败:', error)
    ElMessage.error('加载设置失败')
  }
}

// 保存通知设置
const saveNotifications = async () => {
  saving.value = true
  try {
    await updateDoctorSettings({
      notifications: notifications.value,
      workingHours: null  // 不更新工作时间
    })
    ElMessage.success('通知设置已保存')
  } catch (error) {
    console.error('保存通知设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存工作时间
const saveWorkingHours = async () => {
  if (!workingHours.value.workDays || workingHours.value.workDays.length === 0) {
    ElMessage.warning('请至少选择一个工作日')
    return
  }
  
  if (!workingHours.value.startTime || !workingHours.value.endTime) {
    ElMessage.warning('请设置工作时间段')
    return
  }
  
  savingWork.value = true
  try {
    await updateDoctorSettings({
      notifications: null,  // 不更新通知设置
      workingHours: workingHours.value
    })
    ElMessage.success('工作时间设置已保存')
  } catch (error) {
    console.error('保存工作时间失败:', error)
    ElMessage.error('保存失败')
  } finally {
    savingWork.value = false
  }
}

// 重置工作时间
const resetWorkingHours = () => {
  workingHours.value = {
    workDays: ['MON', 'TUE', 'WED', 'THU', 'FRI'],
    startTime: '09:00',
    endTime: '18:00'
  }
  ElMessage.info('已重置为默认工作时间（周一至周五 09:00-18:00）')
}

onMounted(async () => {
  await loadSettings()
})
</script>

<style scoped lang="scss">
@use '../../styles/variables' as vars;
@use '../../styles/mixins' as mixins;

.doctor-settings {
  padding: 24px;
  min-height: calc(100vh - 60px);
}

:deep(.el-page-header) {
  margin-bottom: 24px;
  
  .el-page-header__content {
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.mt-16 {
  margin-top: 24px;
}

// 卡片统一样式
:deep(.el-card) {
  border-radius: 16px;
  border: none;
  @include mixins.glass-effect;
  transition: all 0.3s vars.$ease-spring;
  animation: fadeInUp 0.6s vars.$ease-spring backwards;

  @for $i from 1 through 3 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.1}s;
    }
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  }

  .el-card__header {
    padding: 20px 24px;
    font-weight: 600;
    font-size: 16px;
    color: var(--el-text-color-primary);
    border-bottom: 1px solid rgba(var(--el-border-color-lighter-rgb), 0.3);
    background: transparent;
    display: flex;
    align-items: center;

    &::before {
      content: '';
      display: block;
      width: 4px;
      height: 16px;
      background: var(--el-color-primary);
      border-radius: 2px;
      margin-right: 12px;
    }

    .el-text {
      margin-left: 12px;
      font-weight: 400;
      font-size: 13px;
    }
  }

  .el-card__body {
    padding: 24px;
  }
}

// 表单优化
:deep(.el-form) {
  .el-form-item {
    margin-bottom: 24px;

    .el-form-item__label {
      font-weight: 500;
      font-size: 14px;
    }

    .el-switch {
      --el-switch-on-color: var(--el-color-success);
    }

    .el-select, .el-time-picker {
      transition: all 0.3s;

      &:hover {
        transform: translateY(-1px);
      }
    }
  }

  .el-button {
    padding: 12px 28px;
    font-weight: 500;
    transition: all 0.3s vars.$ease-spring;
    border-radius: 8px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

// 分割线优化
:deep(.el-divider) {
  margin: 24px 0;
  border-top-color: rgba(64, 158, 255, 0.2);

  .el-divider__text {
    font-weight: 600;
    font-size: 15px;
    color: var(--el-color-primary);
    background: transparent; // Make it transparent for glass effect
    padding: 0 16px;
  }
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

// 响应式设计
@media (max-width: 1366px) {
  .doctor-settings {
    padding: 16px;
  }

  :deep(.el-card .el-card__body) {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  :deep(.el-form) {
    .el-form-item {
      .el-select, .el-time-picker {
        width: 100% !important;
      }
    }
  }
}
</style>
