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
                  :percentage="Number(serverMetrics.cpuUsage)"  
                  :width="100" 
                  :color= "[ 
                    { color: '#67C23A', percentage: 40 }, 
                    { color: '#E6A23C', percentage: 80 }, 
                    { color: '#F56C6C', percentage: 100 } 
                  ]" 
                > 
                  <template #default="{ percentage }"> 
                    <span class="percentage-value">{{ percentage }}%</span> 
                  </template> 
                </el-progress> 
              </div> 
          
              <div class="monitor-item"> 
                <div class="label">内存使用率</div> 
                <el-progress  
                  type="dashboard"  
                  :percentage="Number(serverMetrics.memoryUsage)"  
                  :width="100" 
                  :color= "[ 
                    { color: '#409EFF', percentage: 60 }, 
                    { color: '#E6A23C', percentage: 90 } 
                  ]" 
                /> 
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
                <el-button size="small" type="primary" :loading="runningDiagnosis" @click="runAIDiagnosis" v-particles>
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
 import { ElMessage } from 'element-plus' 
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
 
 // --- 3. 登录日志数据 --- 
 const loginLogs = ref([]) 
 const loading = ref({ activities: false }) 
 
 // 生命周期 
 onMounted(async () => { 
   await loadDashboardData() // 加载一次性统计数据 
   await loadLoginLogs()     // 加载日志 
   
   // 开启实时监控轮询 (每 3 秒刷新一次) 
   fetchRealTimeMetrics() 
   monitorTimer = setInterval(fetchRealTimeMetrics, 3000) 
 }) 
 
 onUnmounted(() => { 
   if (monitorTimer) clearInterval(monitorTimer) 
 }) 
 
 // 获取统计大盘数据 (修复了日期参数缺失的问题) 
 const loadDashboardData = async () => { 
   loading.value.activities = true 
   try { 
     // 构造日期范围：过去7天 
     const end = new Date() 
     const start = new Date() 
     start.setDate(start.getDate() - 7) 
     const formatDate = (d) => d.toISOString().split('T')[0] 
 
     const [reportRes, activityRes] = await Promise.all([ 
       getDataReports({ start: formatDate(start), end: formatDate(end) }), 
       getUserActivityStats() 
     ]) 
     
     const report = reportRes?.data || {} 
     const activity = activityRes?.data || {} 
 
     // 更新卡片数据 
     metrics.value = [ 
       { title: '总用户数', value: (report.totalUsers || 0).toLocaleString(), icon: User, color: '#409EFF' }, 
       { title: '活跃用户', value: (activity.weeklyActiveUsers || 0).toLocaleString(), icon: UserFilled, color: '#67C23A' }, 
       { title: '家庭总数', value: (report.totalFamilies || 0).toLocaleString(), icon: House, color: '#E6A23C' }, 
       { title: '健康日志', value: (report.totalHealthLogs || 0).toLocaleString(), icon: Tickets, color: '#F56C6C' } 
     ] 
   } catch (error) { 
     console.error(error) 
     ElMessage.warning('统计数据加载部分失败') 
   } finally { 
     loading.value.activities = false 
   } 
 } 
 
 // 获取服务器实时指标 
 const fetchRealTimeMetrics = async () => { 
   try { 
     const res = await getSystemMetrics() 
     if (res.data) { 
       serverMetrics.value = res.data 
     } 
   } catch (e) { 
     // 监控接口失败通常不弹窗打扰用户，仅控制台输出 
     console.debug("Monitor update skipped") 
   } 
 } 
 
 // 加载登录日志 
 const loadLoginLogs = async () => { 
   try { 
     const res = await fetchLoginLogs({ page: 0, size: 10 }) 
     loginLogs.value = res.data?.content || [] 
   } catch (e) { 
     console.error(e) 
   } 
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
