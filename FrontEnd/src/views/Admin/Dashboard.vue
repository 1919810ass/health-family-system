<template>
  <div class="admin-dashboard">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">管理后台仪表板</h1>
        <p class="page-subtitle">系统运行状态与关键指标监控</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" round v-particles class="refresh-btn" @click="loadDashboardData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <!-- 关键指标卡片 -->
    <div class="metrics-grid">
      <div 
        class="metric-card glass-card stagger-anim" 
        v-for="(metric, index) in metrics" 
        :key="metric.title"
        :style="{ '--delay': `${0.2 + index * 0.1}s` }"
      >
        <div class="metric-content">
          <div class="metric-header">
            <div class="metric-icon-box" :style="{ backgroundColor: metric.color + '20', color: metric.color }">
              <el-icon :size="24">
                <component :is="metric.icon" />
              </el-icon>
            </div>
            <div class="metric-trend" :class="metric.trend > 0 ? 'up' : 'down'">
              <el-icon v-if="metric.trend > 0"><CaretTop /></el-icon>
              <el-icon v-else><CaretBottom /></el-icon>
              {{ Math.abs(metric.trend) }}%
            </div>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ metric.value }}</div>
            <div class="metric-title">{{ metric.title }}</div>
          </div>
        </div>
        <div class="card-decoration"></div>
      </div>
    </div>

    <!-- 系统状态和快速操作 -->
    <div class="status-section">
      <el-row :gutter="24">
        <el-col :span="16" :xs="24" :sm="24" :md="16">
          <div class="glass-card stagger-anim" style="--delay: 0.8s">
            <div class="card-header">
              <div class="header-left">
                <div class="icon-box primary">
                  <el-icon><Monitor /></el-icon>
                </div>
                <span class="title">系统状态</span>
              </div>
              <el-tag type="success" effect="dark" round>运行正常</el-tag>
            </div>
            <div class="system-status-grid">
              <div class="status-item" v-for="status in systemStatus" :key="status.name">
                <div class="status-icon">
                  <div class="dot" :class="status.status === '正常' ? 'success' : 'danger'"></div>
                </div>
                <div class="status-info">
                  <div class="status-name">{{ status.name }}</div>
                  <div class="status-val" :class="status.status === '正常' ? 'text-success' : 'text-danger'">
                    {{ status.status }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8" :xs="24" :sm="24" :md="8">
          <div class="glass-card stagger-anim" style="--delay: 0.9s">
            <div class="card-header">
              <div class="header-left">
                <div class="icon-box warning">
                  <el-icon><Lightning /></el-icon>
                </div>
                <span class="title">快速操作</span>
              </div>
            </div>
            <div class="quick-actions">
              <el-button type="primary" plain class="action-btn" @click="goTo('/admin/users')" v-particles>
                <el-icon><User /></el-icon>
                <span>用户管理</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </el-button>
              <el-button type="success" plain class="action-btn" @click="goTo('/admin/families')" v-particles>
                <el-icon><House /></el-icon>
                <span>家庭管理</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </el-button>
              <el-button type="warning" plain class="action-btn" @click="goTo('/admin/health/logs')" v-particles>
                <el-icon><DataAnalysis /></el-icon>
                <span>健康数据</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </el-button>
              <el-button type="info" plain class="action-btn" @click="goTo('/ops')" v-particles>
                <el-icon><Setting /></el-icon>
                <span>系统运维</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 用户登录日志 -->
    <div class="activity-section stagger-anim" style="--delay: 1.0s">
      <div class="glass-card">
        <div class="card-header">
          <div class="header-left">
            <div class="icon-box info">
              <el-icon><Tickets /></el-icon>
            </div>
            <span class="title">用户登录日志</span>
          </div>
          <el-button type="primary" link @click="loadLoginLogs()">
            <el-icon><Refresh /></el-icon>
            刷新列表
          </el-button>
        </div>
        
        <el-table 
          :data="loginLogs" 
          style="width: 100%" 
          v-loading="loading.activities"
          class="custom-table"
        >
          <el-table-column prop="username" label="用户名" width="150">
            <template #default="{ row }">
              <span class="username-cell">{{ row.username }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="用户类型" width="140">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)" effect="light" round>
                {{ formatRole(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ipAddress" label="IP地址" width="160">
            <template #default="{ row }">
              <span class="ip-cell">{{ row.ipAddress }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="loginTime" label="登录时间" width="200" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <div class="status-cell">
                <div class="status-dot" :class="row.status === 'SUCCESS' ? 'success' : 'danger'"></div>
                <span>{{ row.status === 'SUCCESS' ? '成功' : '失败' }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalLogs"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  User, House, DataAnalysis, Monitor, Setting, CaretTop, CaretBottom,
  UserFilled, Tickets, ChatLineRound, View, Refresh, Lightning, ArrowRight
} from '@element-plus/icons-vue'
import { fetchLogs, fetchLoginLogs } from '../../api/ops' // 使用现有的ops API

const router = useRouter()

// 关键指标数据
const metrics = ref([
  {
    title: '总用户数',
    value: '1,234',
    trend: 5.2,
    trendText: '较上周',
    icon: User,
    color: '#409EFF'
  },
  {
    title: '活跃用户',
    value: '892',
    trend: 3.1,
    trendText: '较上周',
    icon: UserFilled,
    color: '#67C23A'
  },
  {
    title: '家庭总数',
    value: '345',
    trend: 8.7,
    trendText: '较上周',
    icon: House,
    color: '#E6A23C'
  },
  {
    title: '健康日志',
    value: '2,456',
    trend: 12.3,
    trendText: '较上周',
    icon: Tickets,
    color: '#F56C6C'
  },
  {
    title: '医生数量',
    value: '42',
    trend: 2.5,
    trendText: '较上周',
    icon: ChatLineRound,
    color: '#909399'
  },
  {
    title: '今日访问',
    value: '1,876',
    trend: -1.2,
    trendText: '较上周',
    icon: View,
    color: '#409EFF'
  }
])

// 系统状态
const systemStatus = ref([
  { name: '应用服务', status: '正常' },
  { name: '数据库', status: '正常' },
  { name: '缓存服务', status: '正常' },
  { name: '文件存储', status: '正常' },
  { name: '消息队列', status: '正常' },
  { name: '定时任务', status: '正常' }
])

// 登录日志相关数据
const loginLogs = ref([])
const totalLogs = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 加载状态
const loading = ref({
  activities: false
})

// 模拟加载数据
onMounted(async () => {
  await loadDashboardData()
  await loadLoginLogs()
})

// 加载仪表板数据
const loadDashboardData = async () => {
  try {
    // 这里可以调用实际的API来获取仪表板数据
    // 目前使用模拟数据
    loading.value.activities = true
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    loading.value.activities = false
  } catch (error) {
    ElMessage.error('加载仪表板数据失败')
    loading.value.activities = false
  }
}

// 跳转到指定页面
const goTo = (path) => {
  router.push(path)
}

// 加载登录日志
const loadLoginLogs = async () => {
  try {
    loading.value.activities = true;
    const response = await fetchLoginLogs({
      page: currentPage.value - 1,
      size: pageSize.value
    });
    
    loginLogs.value = response.data.content || [];
    totalLogs.value = response.data.totalElements || 0;
  } catch (error) {
    ElMessage.error('加载登录日志失败');
    console.error('加载登录日志失败:', error);
  } finally {
    loading.value.activities = false;
  }
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1; // 重置到第一页
  loadLoginLogs();
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page;
  loadLoginLogs();
}

// 根据角色获取标签类型
const getRoleTagType = (role) => {
  switch (role) {
    case 'ADMIN':
      return 'danger';
    case 'DOCTOR':
      return 'warning';
    case 'MEMBER':
    case 'FAMILY_ADMIN':
      return 'primary';
    case 'VIEWER':
      return 'info';
    default:
      return 'info';
  }
}

// 格式化角色显示
const formatRole = (role) => {
  switch (role) {
    case 'ADMIN':
      return '管理员';
    case 'DOCTOR':
      return '医生';
    case 'MEMBER':
      return '患者';
    case 'FAMILY_ADMIN':
      return '家庭管理员';
    case 'VIEWER':
      return '查看者';
    default:
      return role || '未知';
  }
}
</script>

<style scoped lang="scss">
@use "sass:map";
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.admin-dashboard {
  padding: 24px;
  min-height: 100%;
  
  .page-header {
    margin-bottom: 32px;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;

    .header-content {
      .page-title {
        font-size: 28px;
        font-weight: 800;
        margin: 0 0 8px 0;
        @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info')));
        letter-spacing: -0.5px;
      }

      .page-subtitle {
        color: map.get(vars.$colors, 'text-secondary');
        margin: 0;
        font-size: 14px;
      }
    }
  }

  .metrics-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
    gap: 24px;
    margin-bottom: 32px;

    .metric-card {
      padding: 24px;
      position: relative;
      overflow: hidden;
      
      .metric-content {
        position: relative;
        z-index: 2;
        
        .metric-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 20px;

          .metric-icon-box {
            width: 48px;
            height: 48px;
            border-radius: 16px;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: transform 0.3s vars.$ease-spring;
          }

          .metric-trend {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 13px;
            font-weight: 600;
            padding: 4px 10px;
            border-radius: 20px;
            background: rgba(255, 255, 255, 0.5);

            &.up {
              color: map.get(vars.$colors, 'danger');
              background: rgba(map.get(vars.$colors, 'danger'), 0.1);
            }

            &.down {
              color: map.get(vars.$colors, 'success');
              background: rgba(map.get(vars.$colors, 'success'), 0.1);
            }
          }
        }

        .metric-info {
          .metric-value {
            font-size: 32px;
            font-weight: 800;
            color: map.get(vars.$colors, 'text-main');
            margin-bottom: 4px;
            letter-spacing: -1px;
          }

          .metric-title {
            color: map.get(vars.$colors, 'text-secondary');
            font-size: 14px;
            font-weight: 500;
          }
        }
      }

      &:hover {
        .metric-icon-box {
          transform: scale(1.1) rotate(5deg);
        }
      }
    }
  }

  .status-section {
    margin-bottom: 32px;

    .glass-card {
      height: 100%;
      padding: 24px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .icon-box {
          width: 40px;
          height: 40px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          &.primary {
            background: rgba(map.get(vars.$colors, 'primary'), 0.1);
            color: map.get(vars.$colors, 'primary');
          }
          
          &.warning {
            background: rgba(map.get(vars.$colors, 'warning'), 0.1);
            color: map.get(vars.$colors, 'warning');
          }
        }

        .title {
          font-size: 18px;
          font-weight: 700;
          color: map.get(vars.$colors, 'text-main');
        }
      }
    }

    .system-status-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
      gap: 16px;

      .status-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        border-radius: vars.$radius-md;
        background: rgba(255, 255, 255, 0.3);
        border: 1px solid rgba(255, 255, 255, 0.4);
        transition: all 0.3s ease;

        &:hover {
          background: rgba(255, 255, 255, 0.6);
          transform: translateY(-2px);
        }

        .status-icon {
          width: 12px;
          height: 12px;
          
          .dot {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            
            &.success {
              background: map.get(vars.$colors, 'success');
              box-shadow: 0 0 10px rgba(map.get(vars.$colors, 'success'), 0.5);
            }
            
            &.danger {
              background: map.get(vars.$colors, 'danger');
              box-shadow: 0 0 10px rgba(map.get(vars.$colors, 'danger'), 0.5);
            }
          }
        }

        .status-info {
          .status-name {
            font-size: 13px;
            color: map.get(vars.$colors, 'text-secondary');
            margin-bottom: 2px;
          }

          .status-val {
            font-size: 14px;
            font-weight: 600;

            &.text-success { color: map.get(vars.$colors, 'success'); }
            &.text-danger { color: map.get(vars.$colors, 'danger'); }
          }
        }
      }
    }

    .quick-actions {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .action-btn {
        width: 100%;
        height: 56px;
        justify-content: space-between;
        margin: 0;
        border-radius: vars.$radius-md;
        border: none;
        padding: 0 20px;
        font-size: 15px;
        font-weight: 600;
        transition: all 0.3s vars.$ease-spring;

        .el-icon {
          font-size: 20px;
        }

        .arrow {
          font-size: 16px;
          opacity: 0.5;
          transition: all 0.3s ease;
        }

        &:hover {
          transform: translateX(4px);
          
          .arrow {
            opacity: 1;
            transform: translateX(4px);
          }
        }
      }
    }
  }

  .activity-section {
    .glass-card {
      padding: 24px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .icon-box {
          width: 40px;
          height: 40px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          &.info {
            background: rgba(map.get(vars.$colors, 'info'), 0.1);
            color: map.get(vars.$colors, 'info');
          }
        }

        .title {
          font-size: 18px;
          font-weight: 700;
          color: map.get(vars.$colors, 'text-main');
        }
      }
    }
  }

  // Animation Utilities
  .stagger-anim {
    opacity: 0;
    animation: slideUpFade 0.8s vars.$ease-spring forwards;
    animation-delay: var(--delay, 0s);
  }

  @keyframes slideUpFade {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // Responsive adjustments
  @media (max-width: 768px) {
    padding: 16px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
      
      .header-actions {
        width: 100%;
        
        .refresh-btn {
          width: 100%;
        }
      }
    }
  }
}

// Glass effect class
.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: vars.$shadow-lg;
  transition: all 0.3s vars.$ease-spring;

  &:hover {
    box-shadow: vars.$shadow-xl;
    border-color: rgba(255, 255, 255, 0.9);
  }
}

.custom-table {
  background: transparent;
  
  :deep(tr), :deep(th.el-table__cell) {
    background: transparent;
  }
  
  :deep(.el-table__row:hover) {
    background-color: rgba(map.get(vars.$colors, 'primary'), 0.05) !important;
  }

  .username-cell {
    font-weight: 600;
    color: map.get(vars.$colors, 'text-main');
  }

  .status-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      
      &.success { background: map.get(vars.$colors, 'success'); }
      &.danger { background: map.get(vars.$colors, 'danger'); }
    }
  }
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>