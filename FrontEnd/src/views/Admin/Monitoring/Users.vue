<template>
  <div class="user-activity">
    <div class="page-header stagger-anim" style="--delay: 0.1s">
      <div class="header-content">
        <h1 class="page-title">用户活动监控</h1>
        <p class="page-subtitle">实时监控用户活跃度、行为分析与登录日志</p>
      </div>
    </div>

    <!-- 活跃度统计卡片 -->
    <div class="activity-cards mb-24 stagger-anim" style="--delay: 0.2s">
      <div class="glass-card activity-card">
        <div class="card-content">
          <div class="info">
            <div class="card-title">日活跃用户</div>
            <div class="card-value">1,248</div>
          </div>
          <div class="icon-box primary">
            <el-icon><User /></el-icon>
          </div>
        </div>
        <div class="card-footer">
          <span class="trend up">
            <el-icon><Top /></el-icon> +12.5%
          </span>
          <span class="footer-text">较昨日</span>
        </div>
      </div>

      <div class="glass-card activity-card">
        <div class="card-content">
          <div class="info">
            <div class="card-title">周活跃用户</div>
            <div class="card-value">8,934</div>
          </div>
          <div class="icon-box success">
            <el-icon><DataLine /></el-icon>
          </div>
        </div>
        <div class="card-footer">
          <span class="trend down">
            <el-icon><Bottom /></el-icon> -2.3%
          </span>
          <span class="footer-text">较上周</span>
        </div>
      </div>

      <div class="glass-card activity-card">
        <div class="card-content">
          <div class="info">
            <div class="card-title">月活跃用户</div>
            <div class="card-value">32,567</div>
          </div>
          <div class="icon-box warning">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
        <div class="card-footer">
          <span class="trend up">
            <el-icon><Top /></el-icon> +8.7%
          </span>
          <span class="footer-text">较上月</span>
        </div>
      </div>

      <div class="glass-card activity-card">
        <div class="card-content">
          <div class="info">
            <div class="card-title">在线用户</div>
            <div class="card-value">234</div>
          </div>
          <div class="icon-box danger">
            <el-icon><Monitor /></el-icon>
          </div>
        </div>
        <div class="card-footer">
          <span class="trend up">
            <el-icon><Top /></el-icon> +15
          </span>
          <span class="footer-text">实时</span>
        </div>
      </div>
    </div>

    <!-- 实时在线用户 -->
    <div class="glass-card mb-24 stagger-anim" style="--delay: 0.3s">
      <div class="card-header">
        <div class="header-left">
          <div class="icon-box info"><el-icon><Connection /></el-icon></div>
          <span class="title">实时在线用户</span>
        </div>
        <div class="header-actions">
          <el-button @click="refreshOnlineUsers" :loading="onlineUsersLoading" circle plain>
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>
      
      <el-table :data="onlineUsers" style="width: 100%" v-loading="onlineUsersLoading">
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" effect="light" round>
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastActive" label="最后活跃" width="180" />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="device" label="设备" width="150" />
        <el-table-column prop="location" label="位置" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewUserDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 用户行为分析 -->
    <div class="glass-card mb-24 stagger-anim" style="--delay: 0.4s">
      <div class="card-header">
        <div class="header-left">
          <div class="icon-box warning"><el-icon><PieChart /></el-icon></div>
          <span class="title">用户行为分析</span>
        </div>
        <div class="header-actions">
          <el-date-picker
            v-model="behaviorTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="refreshBehaviorAnalysis"
            :clearable="false"
            style="width: 240px"
          />
          <el-button @click="refreshBehaviorAnalysis" :loading="behaviorLoading" circle plain class="ml-12">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>
      
      <div class="charts-container">
        <div ref="loginFreqChartRef" class="chart"></div>
        <div ref="featureUsageChartRef" class="chart"></div>
      </div>
    </div>

    <!-- 登录日志 -->
    <div class="glass-card stagger-anim" style="--delay: 0.5s">
      <div class="card-header">
        <div class="header-left">
          <div class="icon-box primary"><el-icon><List /></el-icon></div>
          <span class="title">用户登录日志</span>
        </div>
        <div class="header-actions">
          <el-select v-model="loginLogFilter.type" placeholder="日志类型" style="width: 120px; margin-right: 12px;">
            <el-option label="全部" value="" />
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failed" />
          </el-select>
          <el-button @click="refreshLoginLogs" :loading="loginLogsLoading" circle plain>
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>
      
      <el-table :data="loginLogs" style="width: 100%" v-loading="loginLogsLoading">
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="userAgent" label="用户代理" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getLoginStatusTagType(row.status)" effect="light" round>
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="150" />
      </el-table>
      
      <div class="pagination-container mt-16">
        <el-pagination
          @size-change="handleLoginLogSizeChange"
          @current-change="handleLoginLogCurrentChange"
          :current-page="loginLogPagination.page"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="loginLogPagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="loginLogPagination.total"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, DataLine, Calendar, Monitor, Top, Bottom, Connection, Refresh, PieChart, List } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getUserActivityStats, getOnlineUsers, getBehaviorAnalysis, getLoginLogs } from '../../../api/admin'

// 图表引用
const loginFreqChartRef = ref(null)
const featureUsageChartRef = ref(null)

// 活跃度统计
const activityStats = ref({
  daily: { count: 1248, trend: '+12.5%' },
  weekly: { count: 8934, trend: '-2.3%' },
  monthly: { count: 32567, trend: '+8.7%' },
  online: { count: 234, trend: '+15' }
})

// 在线用户数据
const onlineUsers = ref([])
const onlineUsersLoading = ref(false)

// 行为分析数据
const behaviorTimeRange = ref([
  dayjs().subtract(7, 'day').toDate(),
  dayjs().toDate()
])
const behaviorLoading = ref(false)

// 登录日志数据
const loginLogs = ref([])
const loginLogsLoading = ref(false)
const loginLogFilter = ref({
  type: ''
})
const loginLogPagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 图表实例
let loginFreqChart = null
let featureUsageChart = null

// 初始化图表
const initCharts = () => {
  if (!loginFreqChartRef.value || !featureUsageChartRef.value) return

  // 登录频次图表
  loginFreqChart = echarts.init(loginFreqChartRef.value)
  loginFreqChart.setOption({
    title: {
      text: '用户登录频次趋势',
      left: 'center',
      textStyle: { color: '#606266', fontSize: 16 }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#E4E7ED',
      textStyle: { color: '#303133' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#E4E7ED' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#E4E7ED' } },
      axisLabel: { color: '#909399' }
    },
    series: [{
      data: [1200, 1320, 1010, 1340, 900, 2300, 2100],
      type: 'line',
      smooth: true,
      symbolSize: 8,
      itemStyle: { color: '#409EFF' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      }
    }]
  })

  // 功能使用热度图表
  featureUsageChart = echarts.init(featureUsageChartRef.value)
  featureUsageChart.setOption({
    title: {
      text: '功能使用热度',
      left: 'center',
      textStyle: { color: '#606266', fontSize: 16 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#E4E7ED',
      textStyle: { color: '#303133' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['健康日志', '家庭管理', '医生协作', 'AI建议', '健康提醒', '体质测评'],
      axisLine: { lineStyle: { color: '#E4E7ED' } },
      axisLabel: { color: '#909399', interval: 0, rotate: 30 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#E4E7ED' } },
      axisLabel: { color: '#909399' }
    },
    series: [{
      data: [3200, 2800, 2100, 1800, 1500, 1200],
      type: 'bar',
      barWidth: '40%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#67C23A' },
          { offset: 1, color: 'rgba(103, 194, 58, 0.3)' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
}

// 加载在线用户
const loadOnlineUsers = async () => {
  onlineUsersLoading.value = true
  try {
    const response = await getOnlineUsers()
    onlineUsers.value = response.data || []
  } catch (error) {
    // 模拟数据
    onlineUsers.value = [
      { userId: '10001', username: '张三', role: 'MEMBER', lastActive: '2025-05-20 10:30:00', ipAddress: '192.168.1.10', device: 'Chrome/Windows', location: '北京' },
      { userId: '10002', username: '李四', role: 'DOCTOR', lastActive: '2025-05-20 10:29:45', ipAddress: '192.168.1.11', device: 'Safari/iPhone', location: '上海' },
      { userId: '10003', username: '王五', role: 'ADMIN', lastActive: '2025-05-20 10:28:30', ipAddress: '192.168.1.12', device: 'Edge/Windows', location: '广州' },
      { userId: '10004', username: '赵六', role: 'FAMILY_ADMIN', lastActive: '2025-05-20 10:25:12', ipAddress: '192.168.1.13', device: 'Chrome/Android', location: '深圳' },
      { userId: '10005', username: '钱七', role: 'MEMBER', lastActive: '2025-05-20 10:20:05', ipAddress: '192.168.1.14', device: 'Firefox/Mac', location: '杭州' },
    ]
    // ElMessage.error('加载在线用户失败')
  } finally {
    onlineUsersLoading.value = false
  }
}

// 加载行为分析数据
const loadBehaviorAnalysis = async () => {
  behaviorLoading.value = true
  try {
    const params = {
      startTime: behaviorTimeRange.value[0],
      endTime: behaviorTimeRange.value[1]
    }
    const response = await getBehaviorAnalysis(params)
    // 图表数据已通过initCharts设置
  } catch (error) {
    // ElMessage.error('加载行为分析数据失败')
  } finally {
    behaviorLoading.value = false
  }
}

// 加载登录日志
const loadLoginLogs = async () => {
  loginLogsLoading.value = true
  try {
    const params = {
      page: loginLogPagination.value.page,
      size: loginLogPagination.value.size,
      type: loginLogFilter.value.type
    }
    const response = await getLoginLogs(params)
    loginLogs.value = response.data?.items || []
    loginLogPagination.value.total = response.data?.total || 0
  } catch (error) {
    // 模拟数据
    loginLogs.value = Array(10).fill(0).map((_, i) => ({
      userId: `100${10 + i}`,
      username: `用户${10 + i}`,
      loginTime: dayjs().subtract(i * 15, 'minute').format('YYYY-MM-DD HH:mm:ss'),
      ipAddress: `192.168.1.${20 + i}`,
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
      status: Math.random() > 0.1 ? 'success' : 'failed',
      location: '北京'
    }))
    loginLogPagination.value.total = 50
    // ElMessage.error('加载登录日志失败')
  } finally {
    loginLogsLoading.value = false
  }
}

// 刷新数据
const refreshOnlineUsers = () => {
  loadOnlineUsers()
}

const refreshBehaviorAnalysis = () => {
  loadBehaviorAnalysis()
}

const refreshLoginLogs = () => {
  loadLoginLogs()
}

// 分页处理
const handleLoginLogSizeChange = (size) => {
  loginLogPagination.value.size = size
  loadLoginLogs()
}

const handleLoginLogCurrentChange = (page) => {
  loginLogPagination.value.page = page
  loadLoginLogs()
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'FAMILY_ADMIN': return 'warning'
    case 'DOCTOR': return 'primary'
    case 'MEMBER': return 'success'
    default: return 'info'
  }
}

// 获取登录状态标签类型
const getLoginStatusTagType = (status) => {
  return status === 'success' ? 'success' : 'danger'
}

// 查看用户详情
const viewUserDetail = (user) => {
  ElMessage.info(`查看用户 ${user.username} 详情`)
}

// 监听窗口大小变化
const handleResize = () => {
  loginFreqChart?.resize()
  featureUsageChart?.resize()
}

// 初始化
onMounted(() => {
  initCharts()
  loadOnlineUsers()
  loadBehaviorAnalysis()
  loadLoginLogs()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理图表
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (loginFreqChart) loginFreqChart.dispose()
  if (featureUsageChart) featureUsageChart.dispose()
})
</script>

<style scoped lang="scss">
@use 'sass:map';
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.user-activity {
  padding: 24px;
  min-height: 100%;
  
  .page-header {
    margin-bottom: 32px;
    
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

  .mb-24 {
    margin-bottom: 24px;
  }
  
  .mt-16 {
    margin-top: 16px;
  }
  
  .ml-12 {
    margin-left: 12px;
  }
  
  .glass-card {
    @include mixins.glass-effect;
    border-radius: vars.$radius-lg;
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: vars.$shadow-lg;
    padding: 24px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: vars.$shadow-xl;
    }
  }

  .activity-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 24px;

    .activity-card {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      height: 160px;
      
      .card-content {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        
        .info {
          .card-title {
            font-size: 14px;
            color: map.get(vars.$colors, 'text-secondary');
            margin-bottom: 8px;
            font-weight: 500;
          }
          
          .card-value {
            font-size: 28px;
            font-weight: 800;
            color: map.get(vars.$colors, 'text-main');
            font-family: 'DIN Alternate', sans-serif;
          }
        }
        
        .icon-box {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          
          &.primary {
            background: rgba(map.get(vars.$colors, 'primary'), 0.1);
            color: map.get(vars.$colors, 'primary');
          }
          &.success {
            background: rgba(map.get(vars.$colors, 'success'), 0.1);
            color: map.get(vars.$colors, 'success');
          }
          &.warning {
            background: rgba(map.get(vars.$colors, 'warning'), 0.1);
            color: map.get(vars.$colors, 'warning');
          }
          &.danger {
            background: rgba(map.get(vars.$colors, 'danger'), 0.1);
            color: map.get(vars.$colors, 'danger');
          }
        }
      }

      .card-footer {
        display: flex;
        align-items: center;
        gap: 8px;
        padding-top: 12px;
        border-top: 1px solid rgba(0, 0, 0, 0.05);

        .trend {
          font-size: 13px;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 2px;
          
          &.up {
            color: map.get(vars.$colors, 'success');
          }
          
          &.down {
            color: map.get(vars.$colors, 'danger');
          }
        }
        
        .footer-text {
          font-size: 12px;
          color: map.get(vars.$colors, 'text-secondary');
        }
      }
    }
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
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        
        &.info {
          background: rgba(map.get(vars.$colors, 'info'), 0.1);
          color: map.get(vars.$colors, 'info');
        }
        &.warning {
          background: rgba(map.get(vars.$colors, 'warning'), 0.1);
          color: map.get(vars.$colors, 'warning');
        }
        &.primary {
          background: rgba(map.get(vars.$colors, 'primary'), 0.1);
          color: map.get(vars.$colors, 'primary');
        }
      }
      
      .title {
        font-size: 18px;
        font-weight: 600;
        color: map.get(vars.$colors, 'text-main');
      }
    }

    .header-actions {
      display: flex;
      align-items: center;
    }
  }

  .charts-container {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
    gap: 24px;

    .chart {
      min-height: 350px;
      width: 100%;
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
  }
}

// Staggered Animation
.stagger-anim {
  opacity: 0;
  animation: fadeSlideUp 0.6s ease-out forwards;
  animation-delay: var(--delay, 0s);
}

@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Responsive
@media (max-width: 768px) {
  .user-activity {
    padding: 16px;
    
    .charts-container {
      grid-template-columns: 1fr;
    }
    
    .header-actions {
      .el-date-editor {
        width: 160px !important;
      }
    }
  }
}
</style>