<template>
  <div class="family-status-card">
    <div class="card-header">
      <h3>家庭成员状态</h3>
    </div>
    <div class="card-content" v-loading="loading">
      <div v-for="member in members" :key="member.id" class="member-item">
        <el-avatar :size="48" :src="member.avatar" class="avatar">
          {{ member.name.charAt(0) }}
        </el-avatar>
        
        <div class="member-info">
          <div class="top-row">
            <span class="name">{{ member.name }}</span>
            <span class="tag" :class="member.roleTagColor">{{ member.roleTagName }}</span>
          </div>
          <div class="bottom-row">
            {{ member.statusText }}
          </div>
        </div>
        
        <div class="status-indicator" :class="{ offline: !member.isOnline }">
          <span class="dot"></span>
          {{ member.isOnline ? '已上线' : '离线' }}
        </div>
      </div>
      
      <!-- Empty state if no members -->
      <div v-if="!loading && members.length === 0" class="empty-members">
        暂无家庭成员
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getFamilyMembers } from '@/api/family'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const members = ref([])
const loading = ref(false)

const roleMap = {
  ADMIN: '管理员',
  FAMILY_ADMIN: '家庭管理员',
  MEMBER: '成员',
  VIEWER: '访客',
  DOCTOR: '医生'
}

const currentFamilyId = computed(() => {
  return userStore.currentFamily?.id || localStorage.getItem('current_family_id')
})

const mapMember = (m, idx) => {
  const name = m?.nickname || m?.name || m?.phone || '家庭成员'
  const relation = m?.relation || m?.relationship || ''
  const role = m?.role || m?.roleCode || ''
  const roleTagName = relation || roleMap[role] || '成员'
  const roleTagColor = role === 'FAMILY_ADMIN' || relation === '本人' ? 'blue' : 'orange'
  const statusText = relation ? `${relation}成员` : '家庭成员'

  return {
    id: m?.userId || m?.id || idx,
    name,
    avatar: m?.avatar || m?.headImg || '',
    roleTagName,
    roleTagColor,
    statusText,
    isOnline: m?.online ?? true
  }
}

const loadMembers = async () => {
  const familyId = currentFamilyId.value
  if (!familyId) {
    members.value = []
    return
  }
  loading.value = true
  try {
    const res = await getFamilyMembers(familyId)
    const list = Array.isArray(res?.data) ? res.data : (res?.data?.items || res?.items || [])
    members.value = list.map(mapMember)
  } catch {
    members.value = []
  } finally {
    loading.value = false
  }
}

watch(currentFamilyId, () => {
  loadMembers()
})

onMounted(() => {
  loadMembers()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;

.family-status-card {
  background: #fff;
  border-radius: vars.$radius-lg;
  padding: 24px;
  height: 100%;
  box-shadow: vars.$shadow-sm;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: vars.$shadow-md;
  }
}

.card-header {
  h3 {
    margin: 0;
    font-size: 18px;
    color: vars.$text-main-color;
    margin-bottom: 24px;
  }
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
  
  .member-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 12px;
    transition: background 0.2s;
    
    &:hover {
      background: #F8FAFC;
    }
    
    .avatar {
      background: #EBF5FF;
      color: vars.$primary-color;
      font-weight: 600;
      margin-right: 12px;
    }
    
    .member-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .top-row {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .name {
          font-weight: 600;
          font-size: 15px;
          color: vars.$text-main-color;
        }
        
        .tag {
          font-size: 10px;
          padding: 2px 8px;
          border-radius: 10px;
          font-weight: 500;
          
          &.blue {
            background: rgba(78, 161, 255, 0.1);
            color: vars.$primary-color;
          }
          
          &.orange {
            background: rgba(255, 168, 102, 0.1);
            color: vars.$warning-color;
          }
        }
      }
      
      .bottom-row {
        font-size: 12px;
        color: vars.$text-secondary-color;
      }
    }
    
    .status-indicator {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px;
      background: rgba(53, 199, 122, 0.1);
      color: vars.$success-color;
      font-size: 12px;
      border-radius: 20px;
      font-weight: 500;
      
      .dot {
        width: 6px;
        height: 6px;
        background: vars.$success-color;
        border-radius: 50%;
      }

      &.offline {
        background: rgba(144, 147, 153, 0.12);
        color: vars.$text-secondary-color;

        .dot {
          background: vars.$text-secondary-color;
        }
      }
    }
  }
}

.empty-members {
  text-align: center;
  color: vars.$text-secondary-color;
  padding: 20px;
}
</style>
