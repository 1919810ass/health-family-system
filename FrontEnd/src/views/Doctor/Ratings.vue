<template>
  <div class="doctor-ratings-container">
    <div class="page-header">
      <div class="header-content">
        <h2 class="title">我的评价</h2>
        <p class="subtitle">查看患者对您的服务评价</p>
      </div>
      <div class="header-stats" v-if="stats">
        <div class="stat-item">
          <span class="value">{{ stats.averageRating }}</span>
          <span class="label">平均评分</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="value">{{ stats.totalRatings }}</span>
          <span class="label">累计评价</span>
        </div>
      </div>
    </div>

    <div v-loading="loading" class="ratings-list">
      <el-empty v-if="!loading && ratings.length === 0" description="暂无评价数据" />
      
      <div v-else class="rating-cards">
        <div v-for="rating in ratings" :key="rating.id" class="rating-card glass-panel">
          <div class="card-header">
            <div class="user-info">
              <el-avatar :size="40" :src="rating.userAvatar || ''" class="user-avatar">
                {{ rating.userName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="user-meta">
                <span class="username">{{ rating.userName }}</span>
                <span class="date">{{ formatDate(rating.createdAt) }}</span>
              </div>
            </div>
            <div class="rating-score">
              <el-rate
                v-model="rating.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value} 分"
              />
            </div>
          </div>
          <div class="card-content">
            <p class="comment-text">{{ rating.comment || '用户未填写评价内容' }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyRatings } from '@/api/doctor'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const ratings = ref([])

const stats = computed(() => {
  if (ratings.value.length === 0) return { averageRating: '0.0', totalRatings: 0 }
  const total = ratings.value.length
  const sum = ratings.value.reduce((acc, curr) => acc + curr.rating, 0)
  return {
    averageRating: (sum / total).toFixed(1),
    totalRatings: total
  }
})

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const fetchRatings = async () => {
  loading.value = true
  try {
    const res = await getMyRatings()
    ratings.value = res.data
  } catch (error) {
    console.error(error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRatings()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.doctor-ratings-container {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  background: white;
  padding: 24px;
  border-radius: vars.$radius-lg;
  box-shadow: vars.$shadow-sm;

  .title {
    font-size: 24px;
    font-weight: 600;
    color: vars.$text-primary-color;
    margin-bottom: 8px;
  }

  .subtitle {
    color: vars.$text-secondary-color;
    font-size: 14px;
  }

  .header-stats {
    display: flex;
    align-items: center;
    gap: 24px;

    .stat-item {
      text-align: center;
      
      .value {
        display: block;
        font-size: 28px;
        font-weight: 700;
        color: vars.$primary-color;
        line-height: 1.2;
      }

      .label {
        font-size: 12px;
        color: vars.$text-secondary-color;
      }
    }

    .stat-divider {
      width: 1px;
      height: 40px;
      background: rgba(0,0,0,0.05);
    }
  }
}

.rating-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rating-card {
  @include mixins.glass-effect;
  background: white;
  border-radius: vars.$radius-md;
  padding: 20px;
  transition: all 0.3s ease;
  border: 1px solid rgba(0,0,0,0.05);

  &:hover {
    transform: translateY(-2px);
    box-shadow: vars.$shadow-md;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-avatar {
        background: vars.$gradient-primary;
        color: white;
        font-weight: 600;
      }

      .user-meta {
        display: flex;
        flex-direction: column;
        
        .username {
          font-weight: 500;
          color: vars.$text-primary-color;
        }

        .date {
          font-size: 12px;
          color: vars.$text-placeholder-color;
          margin-top: 2px;
        }
      }
    }
  }

  .card-content {
    padding-left: 52px; // Align with username

    .comment-text {
      color: vars.$text-regular-color;
      line-height: 1.6;
      font-size: 14px;
      margin: 0;
      background: rgba(0,0,0,0.02);
      padding: 12px;
      border-radius: 8px;
    }
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    
    .header-stats {
      width: 100%;
      justify-content: space-around;
      background: rgba(0,0,0,0.02);
      padding: 16px;
      border-radius: vars.$radius-md;
    }
  }

  .card-content {
    padding-left: 0 !important;
    margin-top: 12px;
  }
}
</style>