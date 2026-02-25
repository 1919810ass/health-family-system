<template>
  <div class="maintenance-container">
    <div class="content">
      <div class="illustration">
        <el-icon class="maintenance-icon"><Tools /></el-icon>
        <div class="gears">
          <el-icon class="gear gear-1"><Setting /></el-icon>
          <el-icon class="gear gear-2"><Setting /></el-icon>
        </div>
      </div>
      <h1>系统维护升级中</h1>
      <p class="subtitle">我们正在对系统进行必要的维护和升级，以提供更好的服务。</p>
      <p class="message">预计恢复时间：待定。请稍后再试。</p>
      <el-button type="primary" @click="checkStatus" :loading="loading" round>
        检查状态
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Tools, Setting } from '@element-plus/icons-vue'
import { getMaintenanceMode } from '@/api/ops'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

const checkStatus = async () => {
  loading.value = true
  try {
    const res = await getMaintenanceMode()
    // If we get a response (even false), it means we can access the API
    // If the API returns true, we are still in maintenance (for admins they might see true but can access)
    // But for normal users, if they can access getMaintenanceMode, it means interceptor allowed it?
    // Wait, getMaintenanceMode is an API. If maintenance is ON, and user is NOT admin, 
    // fetch('/api/admin/ops/maintenance') will return 503!
    // So if we get 503, we stay here.
    // If we get 200, it means either maintenance is OFF, OR we are ADMIN.
    
    if (res.data === false) {
      ElMessage.success('系统已恢复运行')
      router.push('/')
    } else {
       // Response is 200, but data is true.
       // This happens if we are ADMIN.
       ElMessage.warning('系统仍处于维护模式 (管理员可见)')
       router.push('/')
    }
  } catch (error) {
    // If 503, stay here
    if (error.response && error.response.status === 503) {
       ElMessage.info('系统维护中...')
    } else {
       // Other errors
       ElMessage.error('无法连接服务器')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.maintenance-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  
  .content {
    text-align: center;
    background: white;
    padding: 40px 60px;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    max-width: 500px;
    width: 90%;
    
    .illustration {
      position: relative;
      height: 120px;
      margin-bottom: 24px;
      color: #409EFF;
      
      .maintenance-icon {
        font-size: 80px;
        color: #409EFF;
      }
      
      .gears {
        position: absolute;
        top: 0;
        right: 120px; // Adjust based on layout
        width: 100%;
        height: 100%;
        pointer-events: none;
        
        .gear {
          position: absolute;
          color: #909399;
          opacity: 0.6;
        }
        
        .gear-1 {
          font-size: 40px;
          top: 10px;
          right: 35%;
          animation: spin 4s linear infinite;
        }
        
        .gear-2 {
          font-size: 24px;
          top: 45px;
          right: 28%;
          animation: spin-reverse 3s linear infinite;
        }
      }
    }
    
    h1 {
      font-size: 24px;
      color: #303133;
      margin-bottom: 12px;
    }
    
    .subtitle {
      font-size: 16px;
      color: #606266;
      margin-bottom: 8px;
    }
    
    .message {
      font-size: 14px;
      color: #909399;
      margin-bottom: 30px;
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes spin-reverse {
  from { transform: rotate(0deg); }
  to { transform: rotate(-360deg); }
}
</style>
