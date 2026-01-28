<template>
  <div class="collab glass-effect">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Connection /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">家庭健康协作</h2>
        <p class="subtitle">家庭成员健康互助，共享健康生活</p>
      </div>
    </div>

    <el-card class="mt-16 glass-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>家庭健康看板</span>
          <el-switch v-model="onlyAbnormal" active-text="仅看异常" />
        </div>
      </template>
      <el-row :gutter="16">
        <el-col v-for="m in filteredMembers" :key="m.userId" :xs="24" :sm="12" :md="8" :lg="6" class="mb-16">
          <div class="member-card" :class="{ 'abnormal': m.abnormal }">
            <div class="member">
              <el-avatar :size="48" :src="m.avatar" class="member-avatar" />
              <div class="info">
                <div class="name">{{ m.nickname }}</div>
                
                <!-- 新增：多指标展示 -->
                <div v-if="m.metrics && m.metrics.length > 0" class="metrics-list">
                  <div v-for="item in m.metrics" :key="item.label" class="metric-item">
                    <span class="metric-label">{{ item.label }}:</span>
                    <span class="metric-val" :class="item.abnormal ? 'text-danger' : 'text-success'">
                      {{ item.value }} {{ item.unit }}
                    </span>
                  </div>
                </div>
                <div v-else class="summary" :class="m.abnormal ? 'text-danger' : 'text-success'">{{ m.summary }}</div>
                
                <div class="date">{{ formatDate(m.logDate) }}</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="filteredMembers.length === 0" description="暂无家庭成员数据" />
    </el-card>

    <el-card class="mt-16 glass-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>协作任务</span>
          <div>
            <el-button type="primary" :loading="genLoading" @click="generateCollab" round class="glass-button" v-particles>智能生成协作提醒</el-button>
          </div>
        </div>
      </template>
      <el-table :data="familyReminders" v-loading="remindersLoading" style="width:100%" class="glass-table">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="240" />
        <el-table-column prop="assignedToUserId" label="指派给" min-width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" round>{{ userName(row.assignedToUserId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scheduledTime" label="计划时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.scheduledTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
             <el-tag :type="row.status === 'completed' ? 'success' : 'warning'" size="small" round>{{ row.status === 'completed' ? '已完成' : '待处理' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Connection } from '@element-plus/icons-vue'
import { useFamilyStore } from '../../stores/family'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getFamilyMembers, getFamilyDashboard } from '../../api/family'
import { getFamilyReminders, generateCollaborationReminders } from '../../api/reminder'

const famStore = useFamilyStore()
const familyId = ref()
const members = ref([])
const dashboard = ref({ members: [] })
const remindersLoading = ref(false)
const familyReminders = ref([])
const onlyAbnormal = ref(false)
const genLoading = ref(false)

const filteredMembers = computed(() => {
  const list = dashboard.value.members || []
  return onlyAbnormal.value ? list.filter(m => m.abnormal) : list
})

const userMap = ref({})
const userName = uid => userMap.value[uid] || '—'

const formatDate = d => (d ? dayjs(d).format('YYYY-MM-DD') : '')
const formatDateTime = d => (d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '')

const loadFamily = async () => {
  try {
    // 简化：取第一个家庭成员的familyId
    const families = await getFamilyMembers(await resolveDefaultFamilyId())
  } catch {}
}

const resolveDefaultFamilyId = async () => {
  if (famStore.current?.id) return famStore.current.id
  const fid = localStorage.getItem('current_family_id')
  return fid
}

const init = async () => {
  try {
    familyId.value = await resolveDefaultFamilyId()
    if (!familyId.value) {
      ElMessage.warning('请先在家庭管理中选择当前家庭')
      return
    }
    const [membersRes, boardRes] = await Promise.all([
      getFamilyMembers(familyId.value),
      getFamilyDashboard(familyId.value),
    ])
    members.value = membersRes.data || []
    userMap.value = Object.fromEntries(members.value.map(m => [m.userId, m.nickname]))
    dashboard.value = boardRes.data || { members: [] }
    await loadReminders()
  } catch (e) {
    // Silent error or retry logic could be added
  }
}

const loadReminders = async () => {
  remindersLoading.value = true
  try {
    const res = await getFamilyReminders(familyId.value)
    familyReminders.value = res.data || []
  } catch (e) {
    ElMessage.error('加载协作任务失败')
  } finally {
    remindersLoading.value = false
  }
}

const generateCollab = async () => {
  genLoading.value = true
  try {
    await generateCollaborationReminders(familyId.value)
    ElMessage.success('已生成协作提醒')
    await loadReminders()
  } catch (e) {
    ElMessage.error('生成协作提醒失败')
  } finally {
    genLoading.value = false
  }
}

onMounted(init)
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables.scss' as vars;
@use '@/styles/mixins.scss' as mixins;

.collab {
  padding: 24px;
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  animation: fadeInDown 0.6s vars.$ease-spring;
  gap: 16px;
  
  .header-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    background: linear-gradient(135deg, vars.$primary-color, color.adjust(vars.$primary-color, $lightness: 15%));
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);

    .el-icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .header-content {
    flex: 1;
    .title {
      font-size: 24px;
      font-weight: 700;
      color: vars.$text-main-color;
      margin: 0 0 4px 0;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-main-color, vars.$primary-color));
    }

    .subtitle {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

.glass-header {
  margin-bottom: 24px;
  :deep(.el-page-header__content) {
    font-weight: 600;
    color: vars.$text-main-color;
  }
}

.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  transition: all 0.4s vars.$ease-spring;
  margin-bottom: 24px;
  animation: fadeInUp 0.8s vars.$ease-spring;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: vars.$shadow-md;
  }
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
    padding: 16px 20px;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-header { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  font-weight: 600;
  color: vars.$text-main-color;
  font-size: 16px;
}

.member-card {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: vars.$radius-base;
  padding: 16px;
  transition: all 0.3s;
  height: 100%;
  cursor: default;
  
  &:hover {
    background: rgba(255, 255, 255, 0.8);
    transform: translateY(-2px);
    box-shadow: vars.$shadow-sm;
    border-color: vars.$primary-color;
  }
  
  &.abnormal {
    border-color: rgba(vars.$danger-color, 0.5);
    background: rgba(vars.$danger-color, 0.05);
    
    &:hover {
      border-color: vars.$danger-color;
      background: rgba(vars.$danger-color, 0.1);
    }
  }
}

.member { 
  display: flex; 
  gap: 16px; 
  align-items: center; 
  
  .member-avatar {
    border: 2px solid #fff;
    box-shadow: vars.$shadow-sm;
  }
  
  .info { 
    display: flex; 
    flex-direction: column; 
    gap: 4px;
    flex: 1;
    overflow: hidden;
  }
  
  .name { 
    font-weight: 600; 
    color: vars.$text-main-color;
    font-size: 16px;
  }
  
  .summary { 
    font-size: 14px;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .date { 
    color: vars.$text-secondary-color; 
    font-size: 12px; 
  }
}

.metrics-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin: 4px 0;
  
  .metric-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 13px;
    
    .metric-label {
      color: vars.$text-secondary-color;
      margin-right: 8px;
    }
    
    .metric-val {
      font-weight: 500;
    }
  }
}

.text-danger { color: vars.$danger-color; }
.text-success { color: vars.$success-color; }

.glass-table {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.3);
  --el-table-row-hover-bg-color: rgba(var(--el-color-primary-rgb), 0.1);
  --el-table-border-color: rgba(255, 255, 255, 0.3);
  
  :deep(th), :deep(td) {
    background: transparent !important;
  }
}

.glass-button {
  transition: all 0.3s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);
  }
}

.mt-16 { margin-top: 16px; }
.mb-16 { margin-bottom: 16px; }

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Media Queries */
@media (max-width: 768px) {
  .collab {
    padding: 12px;
  }
}
</style>
