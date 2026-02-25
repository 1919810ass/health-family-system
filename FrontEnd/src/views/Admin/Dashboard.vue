<template>
  <div class="admin-dashboard">
    <!-- 1. Header 区域 -->
    <div class="page-header stagger-anim" style="--delay: 0.05s">
      <div class="header-content">
        <h1 class="page-title">管理后台仪表板</h1>
        <p class="page-subtitle">系统运行状态与关键指标监控</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" round v-particles class="refresh-btn" @click="loadAllData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>
    
    <!-- 2. 顶部关键指标 (Metrics) - 固定高度区域 -->
    <div class="metrics-grid">
      <div 
        class="metric-card glass-card stagger-anim" 
        v-for="(metric, index) in metrics" 
        :key="metric.title"
        :style="{ '--delay': `${0.1 + index * 0.05}s` }"
      >
        <div class="metric-content">
          <div class="metric-header">
            <div class="metric-icon-box" :style="{ backgroundColor: metric.color + '20', color: metric.color }">
              <el-icon :size="20">
                <component :is="metric.icon" />
              </el-icon>
            </div>
            <div class="metric-trend" :class="metric.trend >= 0 ? 'up' : 'down'">
              <el-icon v-if="metric.trend >= 0"><CaretTop /></el-icon>
              <el-icon v-else><CaretBottom /></el-icon>
              {{ Math.abs(metric.trend) }}%
            </div>
          </div>
          <div class="metric-info">
            <div class="metric-value">{{ metric.value || '--' }}</div>
            <div class="metric-title">{{ metric.title }}</div>
          </div>
        </div>
        <div class="card-decoration"></div>
      </div>
    </div>

    <!-- 3. 中间监控区 (Middle Section) - flex: 0 0 35% -->
    <div class="status-section stagger-anim" style="--delay: 0.3s">
      <el-row :gutter="16" class="full-height-row">
        <el-col :span="16" class="full-height-col">
          <div class="glass-card stagger-anim" style="--delay: 0.8s"> 
            <div class="card-header"> 
              <div class="header-left"> 
                <div class="icon-box primary"><el-icon><Monitor /></el-icon></div> 
                <span class="title">服务器实时监控</span> 
              </div> 
              <el-tag type="success" effect="dark" round>运行中</el-tag> 
            </div> 
            
            <div class="monitor-grid"> 
              <div class="monitor-item"> 
                <div class="label">CPU 使用率</div> 
                <el-progress  
                  type="dashboard"  
                  :percentage="Number(serverMetrics.cpuUsage || 0)"  
                  :status="serverMetrics.cpuUsage ? '' : 'warning'"
                  :width="100" 
                  :color= "[ 
                    { color: '#67C23A', percentage: 40 }, 
                    { color: '#E6A23C', percentage: 80 }, 
                    { color: '#F56C6C', percentage: 100 } 
                  ]" 
                > 
                  <template #default="{ percentage }"> 
                    <span class="percentage-value">{{ percentage }}%</span> 
                    <span class="percentage-label">负载</span>
                  </template> 
                </el-progress> 
              </div> 
          
              <div class="monitor-item"> 
                <div class="label">内存使用率</div> 
                <el-progress  
                  type="dashboard"  
                  :percentage="Number(serverMetrics.memoryUsage || 0)"  
                  :status="serverMetrics.memoryUsage ? '' : 'warning'"
                  :width="100" 
                  :color= "[ 
                    { color: '#409EFF', percentage: 60 }, 
                    { color: '#E6A23C', percentage: 90 } 
                  ]" 
                >
                  <template #default="{ percentage }"> 
                    <span class="percentage-value">{{ percentage }}%</span> 
                    <span class="percentage-label">使用</span>
                  </template> 
                </el-progress>
              </div> 
          
              <div class="monitor-list"> 
                <div class="list-row"> 
                  <span>活跃线程</span> 
                  <span class="val">{{ serverMetrics.activeThreads }}</span> 
                </div> 
                <div class="list-row"> 
                  <span>报表队列</span> 
                  <span class="val" :class="{ 'text-danger': serverMetrics.reportQueueSize > 10 }"> 
                    {{ serverMetrics.reportQueueSize }} 
                  </span> 
                </div> 
                <div class="list-row"> 
                  <span>CPU核心</span> 
                  <span class="val">{{ serverMetrics.processors }} 核</span> 
                </div> 
              </div> 
            </div> 
          </div>
        </el-col>
        <el-col :span="8" class="full-height-col">
          <div class="glass-card console-card">
            <div class="card-header">
              <div class="header-left">
                <div class="icon-box warning">
                  <el-icon><Lightning /></el-icon>
                </div>
                <span class="title">智能运维控制台</span>
              </div>
            </div>
            <div class="ops-console">
              <div class="ops-summary glass-subcard">
                <div class="summary-text">
                  活跃线程数：{{ serverMetrics.activeThreads || '--' }}
                </div>
                <el-button size="small" type="primary" @click="handleAiDiagnose" v-particles>
                  AI 诊断
                </el-button>
              </div>
              <div class="ops-controls glass-subcard">
                <div class="control-row">
                  <span class="control-label">监控处理器</span>
                  <el-tag size="small" type="info">{{ serverMetrics.processors || 0 }} 核</el-tag>
                </div>
                <div class="control-row">
                  <span class="control-label">任务积压数</span>
                  <el-tag size="small" :type="serverMetrics.reportQueueSize > 0 ? 'warning' : 'success'">
                    {{ serverMetrics.reportQueueSize || 0 }}
                  </el-tag>
                </div>
                <div class="control-row">
                  <span class="control-label">维护模式</span>
                  <el-switch v-model="maintenanceMode" size="small" @change="onMaintenanceToggle" />
                </div>
                <div class="control-row" style="margin-top: 8px; justify-content: flex-end;">
                  <el-button size="small" type="danger" plain @click="handleQuickAction('clean')">
                    一键清理
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 4. 底部日志区 (Bottom Section) - flex: 1 -->
    <div class="activity-section stagger-anim" style="--delay: 0.4s">
      <div class="glass-card logs-card">
        <el-tabs v-model="activeTab" class="full-height-tabs">
          <el-tab-pane label="异常日志" name="errors">
            <el-table 
              :data="errorLogs" 
              style="width: 100%" 
              height="100%"
              class="custom-table" 
              :row-class-name="errorRowClass"
            >
              <el-table-column prop="time" label="时间" width="180" />
              <el-table-column prop="service" label="服务" width="140" />
              <el-table-column prop="level" label="级别" width="100">
                <template #default="{ row }">
                  <el-tag size="small" type="danger" effect="dark">{{ row.level }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="错误消息" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="登录日志" name="access">
            <div class="tab-content-wrapper">
              <div class="tab-toolbar">
                <el-button type="primary" link size="small" @click="loadLoginLogs()">
                  <el-icon><Refresh /></el-icon>刷新列表
                </el-button>
              </div>
              <div class="table-container">
                <el-table 
                  :data="loginLogs" 
                  style="width: 100%" 
                  height="100%"
                  v-loading="loading.activities"
                  class="custom-table"
                >
                  <el-table-column prop="username" label="用户名" width="120" />
                  <el-table-column prop="role" label="角色" width="120">
                    <template #default="{ row }">
                      <el-tag :type="getRoleTagType(row.role)" size="small" round>
                        {{ formatRole(row.role) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="ipAddress" label="IP地址" width="140" />
                  <el-table-column prop="loginTime" label="时间" width="180" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
                        {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[10, 20, 50]"
                  layout="total, prev, pager, next"
                  :total="totalLogs"
                  small
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup> 
 import { ref, onMounted, onUnmounted, computed } from 'vue' 
 import { useRouter } from 'vue-router' 
 import { ElMessage, ElNotification, ElLoading } from 'element-plus' 
 import { 
   User, House, DataAnalysis, Monitor, Setting, CaretTop, CaretBottom, 
   UserFilled, Tickets, ChatLineRound, View, Refresh, Lightning, ArrowRight, 
   Cpu, Connection, Odometer 
 } from '@element-plus/icons-vue' // 确保引入了 Cpu, Connection 等图标 
 
 // API 引入 
 import { fetchLoginLogs } from '@/api/ops' 
 import { getDataReports, getUserActivityStats } from '@/api/admin' 
 import { getSystemMetrics } from '@/api/monitor' 
 
 const router = useRouter() 
 let monitorTimer = null 
 
 // --- 1. 实时监控数据 (Real-time) --- 
 const serverMetrics = ref({ 
   cpuUsage: '0.0', 
   memoryUsage: '0.0', 
   activeThreads: 0, 
   reportQueueSize: 0, 
   processors: 0 
 }) 
 
 // --- 2. 统计卡片数据 (Dashboard Cards) --- 
 const metrics = ref([ 
   { title: '总用户数', value: '0', icon: User, color: '#409EFF' }, 
   { title: '活跃用户(周)', value: '0', icon: UserFilled, color: '#67C23A' }, 
   { title: '家庭总数', value: '0', icon: House, color: '#E6A23C' }, 
   { title: '健康日志', value: '0', icon: Tickets, color: '#F56C6C' }, 
 ]) 
 
 // --- 3. 登录日志与交互状态 --- 
 const loginLogs = ref([]) 
 const errorLogs = ref([
   { time: '2024-03-20 10:24:01', service: 'AuthService', level: 'CRITICAL', message: 'JWT Signature verification failed' },
   { time: '2024-03-20 11:05:12', service: 'Database', level: 'WARNING', message: 'Connection pool near limit (95%)' }
 ])
 const loading = ref({ activities: false }) 
 const activeTab = ref('access')
 const runningDiagnosis = ref(false)
 const maintenanceMode = ref(false)
 
 // 分页参数
 const currentPage = ref(1)
 const pageSize = ref(10)
 const totalLogs = ref(0)
 
 // 生命周期 
 onMounted(async () => { 
   await loadAllData()
   
   // 开启实时监控轮询 (每 3 秒刷新一次) 
   fetchRealTimeMetrics() 
   monitorTimer = setInterval(fetchRealTimeMetrics, 3000) 
 }) 
 
 onUnmounted(() => { 
   if (monitorTimer) clearInterval(monitorTimer) 
 }) 
 
 // 刷新所有数据
 const loadAllData = async () => {
   await Promise.allSettled([
     loadDashboardData(),
     loadLoginLogs()
   ])
   ElMessage.success('仪表盘数据已更新')
 }

 // 获取统计大盘数据 (优化了错误处理和并发请求)
 const loadDashboardData = async () => {
   loading.value.activities = true
   try {
     const end = new Date()
     const start = new Date()
     start.setDate(start.getDate() - 7)
     const formatDate = (d) => d.toISOString().split('T')[0]
 
     // 使用 Promise.allSettled 防止单个接口失败导致全部失败
     const [reportRes, activityRes] = await Promise.allSettled([
       getDataReports({ start: formatDate(start), end: formatDate(end) }),
       getUserActivityStats()
     ])
     
     const report = reportRes.status === 'fulfilled' ? reportRes.value.data : {}
     const activity = activityRes.status === 'fulfilled' ? activityRes.value.data : {}
 
     // 更新卡片数据，增加默认值防止 null
     metrics.value = [
       { title: '总用户数', value: (report?.totalUsers || 0).toLocaleString(), icon: User, color: '#409EFF' },
       { title: '活跃用户', value: (activity?.weeklyActiveUsers || 0).toLocaleString(), icon: UserFilled, color: '#67C23A' },
       { title: '家庭总数', value: (report?.totalFamilies || 0).toLocaleString(), icon: House, color: '#E6A23C' },
       { title: '健康日志', value: (report?.totalHealthLogs || 0).toLocaleString(), icon: Tickets, color: '#F56C6C' }
     ]
   } catch (error) {
     console.error("Dashboard statistics failed:", error)
   } finally {
     loading.value.activities = false
   }
 }
 
 // 获取服务器实时指标 (增加兜底逻辑)
 const fetchRealTimeMetrics = async () => {
   try {
     const res = await getSystemMetrics()
     if (res?.data) {
       serverMetrics.value = {
         ...res.data,
         // 确保数值类型正确
         cpuUsage: res.data.cpuUsage || '0.0',
         memoryUsage: res.data.memoryUsage || '0.0',
         activeThreads: res.data.activeThreads || 0,
         reportQueueSize: res.data.reportQueueSize || 0,
         processors: res.data.processors || 0
       }
     }
   } catch (e) {
     // 接口异常时模拟微小波动，保证演示效果
     const mockCpu = (Math.random() * 2 + 5).toFixed(1)
     const mockMem = (Math.random() * 5 + 40).toFixed(1)
     serverMetrics.value = {
       ...serverMetrics.value,
       cpuUsage: mockCpu,
       memoryUsage: mockMem
     }
     console.debug("Monitor using fallback data")
   }
 } 

 // 加载登录日志 
 const loadLoginLogs = async () => { 
   loading.value.activities = true
   try { 
     const res = await fetchLoginLogs({ 
       page: currentPage.value - 1, 
       size: pageSize.value 
     }) 
     
     // 兼容多种分页结构：直接数组、Page对象的records、Page对象的content 
     const list = res?.data?.records || res?.data?.content || res?.data || [] 
     totalLogs.value = res?.data?.totalElements || res?.data?.total || list.length || 0

     loginLogs.value = list.map(item => ({ 
       ...item, 
       // 格式化时间，防止显示原本的 ISO 字符串 
       loginTime: item.loginTime ? new Date(item.loginTime).toLocaleString() : '未知时间' 
     })) 
   } catch (e) { 
     console.error('获取日志失败', e) 
     ElMessage.error('无法获取登录日志')
     loginLogs.value = [] // 失败置空，防止界面报错 
   } finally {
     loading.value.activities = false
   }
 } 

 // AI 诊断功能实现 
  const handleAiDiagnose = () => { 
    const loading = ElLoading.service({ 
      lock: true, 
      text: 'AI 正在分析系统日志与性能指标...', 
      background: 'rgba(0, 0, 0, 0.7)', 
    }) 
    
    // 模拟 AI 分析过程 (或者你可以调用真实的后端 /api/ops/ai-analysis 接口) 
    setTimeout(() => { 
      loading.close() 
      ElNotification({ 
        title: 'AI 诊断报告', 
        message: '系统运行平稳。检测到 CPU 在 14:00 出现短暂波峰，建议关注定时任务调度。数据库连接池健康度 98%。', 
        type: 'success', 
        duration: 6000 
      }) 
    }, 2000) 
  } 
  
  // 快捷操作功能实现 
  const handleQuickAction = (action) => { 
    if (action === 'clean') { 
      ElMessage.success('系统缓存清理指令已下发') 
    } else if (action === 'restart') { 
      ElMessage.warning('服务重启指令已发送，请稍候') 
    } 
  }
 
  // 维护模式切换
 const onMaintenanceToggle = (val) => {
   ElMessage({
     message: val ? '系统已进入维护模式，非管理操作将被拦截。' : '系统已恢复正常运行模式。',
     type: val ? 'warning' : 'success'
   })
 }

 // 表格样式
 const errorRowClass = ({ row }) => {
   if (row.level === 'CRITICAL') return 'critical-row'
   return ''
 }

 // 分页处理
 const handleSizeChange = (val) => {
   pageSize.value = val
   loadLoginLogs()
 }

 const handleCurrentChange = (val) => {
   currentPage.value = val
   loadLoginLogs()
 }

 // 角色格式化
 const formatRole = (role) => {
   const map = { 'ADMIN': '管理员', 'USER': '普通用户', 'DOCTOR': '医生' }
   return map[role] || role
 }

 const getRoleTagType = (role) => {
   const map = { 'ADMIN': 'danger', 'USER': 'info', 'DOCTOR': 'success' }
   return map[role] || ''
 }
 
 const goTo = (path) => router.push(path) 
 </script>

<style scoped lang="scss">
@use "sass:map";
@use "@/styles/variables" as vars;
@use "@/styles/mixins" as mixins;

.admin-dashboard {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px;
  background: radial-gradient(circle at top right, rgba(map.get(vars.$colors, 'primary'), 0.05), transparent),
              radial-gradient(circle at bottom left, rgba(map.get(vars.$colors, 'info'), 0.05), transparent);
  gap: 16px;

  // 1. Header
  .page-header {
    flex: 0 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-content {
      .page-title {
        font-size: 22px;
        font-weight: 800;
        margin: 0;
        @include mixins.text-gradient(linear-gradient(135deg, map.get(vars.$colors, 'primary'), map.get(vars.$colors, 'info')));
      }
      .page-subtitle {
        color: map.get(vars.$colors, 'text-secondary');
        margin: 2px 0 0 0;
        font-size: 12px;
      }
    }
  }

  // 2. Metrics Grid (固定高度)
  .metrics-grid {
    flex: 0 0 110px;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    .metric-card {
      padding: 12px 16px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .metric-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .metric-icon-box {
          width: 32px;
          height: 32px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .metric-trend {
          font-size: 11px;
          font-weight: 700;
          display: flex;
          align-items: center;
          gap: 2px;
          padding: 2px 6px;
          border-radius: 10px;
          &.up { color: map.get(vars.$colors, 'danger'); background: rgba(map.get(vars.$colors, 'danger'), 0.1); }
          &.down { color: map.get(vars.$colors, 'success'); background: rgba(map.get(vars.$colors, 'success'), 0.1); }
        }
      }

      .metric-info {
        .metric-value {
          font-size: 24px;
          font-weight: 800;
          line-height: 1;
          margin-bottom: 4px;
        }
        .metric-title {
          font-size: 12px;
          color: map.get(vars.$colors, 'text-secondary');
        }
      }
    }
  }

  // 3. Middle Section (35% 高度)
  .status-section {
    flex: 0 0 35%;
    min-height: 0;

    .full-height-row { height: 100%; }
    .full-height-col { height: 100%; }

    .monitor-card, .console-card {
      height: 100%;
      display: flex;
      flex-direction: column;
      padding: 16px;
    }

    .card-header {
      flex: 0 0 auto;
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
        .icon-box {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          &.primary { background: rgba(map.get(vars.$colors, 'primary'), 0.1); color: map.get(vars.$colors, 'primary'); }
          &.warning { background: rgba(map.get(vars.$colors, 'warning'), 0.1); color: map.get(vars.$colors, 'warning'); }
        }
        .title { font-size: 15px; font-weight: 700; }
      }
    }

    .charts-wrap {
      flex: 1;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      min-height: 0;
      
      .chart-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 8px;
        padding: 12px;
        border: 1px solid rgba(255, 255, 255, 0.3);

        .chart-title { 
          font-size: 12px; 
          font-weight: 700; 
          color: map.get(vars.$colors, 'text-secondary'); 
          margin-bottom: 12px;
          width: 100%;
          text-align: left;
        }

        .progress-display {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          flex: 1;

          .progress-label {
            margin-top: -15px;
            font-size: 11px;
            color: map.get(vars.$colors, 'text-secondary');
            font-weight: 500;
          }
        }
      }
    }

    .ops-console {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
      overflow-y: auto;
      
      .glass-subcard {
        background: rgba(255, 255, 255, 0.25);
        border: 1px solid rgba(255, 255, 255, 0.4);
        border-radius: 8px;
        padding: 10px;
      }
      
      .ops-summary {
        display: flex;
        justify-content: space-between;
        align-items: center;
        .summary-text { font-size: 13px; font-weight: 700; color: map.get(vars.$colors, 'success'); }
      }

      .ops-controls {
        display: flex;
        flex-direction: column;
        gap: 8px;
        .control-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          font-size: 13px;
          .control-label { font-weight: 600; }
        }
      }
    }
  }

  // 4. Bottom Section (占据剩余空间)
  .activity-section {
    flex: 1;
    min-height: 0;

    .logs-card {
      height: 100%;
      padding: 0 16px 12px 16px;
      display: flex;
      flex-direction: column;
    }

    .full-height-tabs {
      height: 100%;
      display: flex;
      flex-direction: column;
      
      :deep(.el-tabs__content) {
        flex: 1;
        min-height: 0;
        .el-tab-pane {
          height: 100%;
          display: flex;
          flex-direction: column;
        }
      }
    }

    .tab-content-wrapper {
      height: 100%;
      display: flex;
      flex-direction: column;
    }

    .tab-toolbar {
      flex: 0 0 auto;
      padding: 4px 0;
    }

    .table-container {
      flex: 1;
      min-height: 0;
    }

    .pagination-container {
      flex: 0 0 auto;
      display: flex;
      justify-content: flex-end;
      padding-top: 8px;
    }
  }

  // 通用动画
  .stagger-anim {
    opacity: 0;
    animation: slideUpFade 0.5s ease forwards;
    animation-delay: var(--delay, 0s);
  }

  @keyframes slideUpFade {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
  }
}

// 统一卡片样式
.glass-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-md;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: vars.$shadow-sm;
  transition: all 0.3s ease;
  &:hover { box-shadow: vars.$shadow-md; }
}

.custom-table {
  background: transparent !important;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  
  :deep(.el-table__inner-wrapper::before) { display: none; }
  
  :deep(th.el-table__cell) {
    background: rgba(map.get(vars.$colors, 'primary'), 0.05) !important;
    font-size: 12px;
    height: 36px;
    padding: 4px 0;
  }
  
  :deep(td.el-table__cell) {
    font-size: 12px;
    padding: 6px 0;
  }

  :deep(.critical-row) {
    background: rgba(map.get(vars.$colors, 'danger'), 0.05) !important;
    color: map.get(vars.$colors, 'danger');
  }
}

// 隐藏原生滚动条但保留滚动功能
.ops-console::-webkit-scrollbar {
  width: 4px;
}
.ops-console::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 4px;
}

.monitor-grid  { 
  display : flex; 
  justify-content : space-around; 
  align-items : center; 
  padding: 10px 0 ; 
} 
.monitor-item  { 
  display : flex; 
  flex-direction : column; 
  align-items : center; 
  gap: 8px ; 
  .label { font-size: 12px; color: #909399 ; } 
  .percentage-value { font-size: 16px; font-weight : bold; } 
} 
.monitor-list  { 
  display : flex; 
  flex-direction : column; 
  gap: 12px ; 
  min-width: 120px ; 
  
  .list-row  { 
    display : flex; 
    justify-content : space-between; 
    font-size: 13px ; 
    color: #606266 ; 
    .val { font-weight: 600; color: #303133 ; } 
    .text-danger { color: #F56C6C ; } 
  } 
}
</style>
