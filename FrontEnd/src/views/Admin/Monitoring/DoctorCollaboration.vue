<template>
  <div class="doctor-collaboration-container">
    <div class="page-header">
      <h1>医生协作监控</h1>
      <p class="subtitle">实时监控全平台医生的工作负荷、协作效率及活跃状态。</p>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="stats-overview">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>医生总数</template>
          <div class="stat-value">{{ doctors.length }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>活跃会话总数</template>
          <div class="stat-value">{{ totalActiveConsultations }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>高负载医生</template>
          <div class="stat-value busy">{{ highLoadDoctorsCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>平均响应活跃度</template>
          <div class="stat-value success">良好</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-card class="doctor-list-card">
      <el-table :data="doctors" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="医生姓名" width="150" />
        <el-table-column prop="phone" label="联系方式" width="150" />
        <el-table-column label="当前协作状态" width="150">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.collaborationStatus)">
              {{ row.collaborationStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="activeConsultations" label="进行中咨询" sortable width="150" />
        <el-table-column prop="totalHealthPlans" label="负责计划数" sortable width="150" />
        <el-table-column prop="lastActivityAt" label="最后活跃" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastActivityAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="viewDetail(row)">查看明细</el-button>
            <el-button size="small" type="warning" plain @click="assignTask(row)">调度分配</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 医生详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" :title="`${detailDialog.data?.nickname || ''} 的工作明细`" width="800px">
      <div v-loading="detailDialog.loading">
        <div v-if="detailDialog.data">
          <h3>活跃咨询会话 (Top 10)</h3>
          <el-table :data="detailDialog.data.activeSessions" style="width: 100%; margin-bottom: 20px" size="small">
            <el-table-column prop="sessionId" label="ID" width="60" />
            <el-table-column prop="patientName" label="患者" width="120" />
            <el-table-column prop="title" label="咨询主题" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastMessageAt" label="最后消息" width="160">
              <template #default="{ row }">
                {{ formatTime(row.lastMessageAt) }}
              </template>
            </el-table-column>
          </el-table>

          <h3>负责的健康计划 (Top 10)</h3>
          <el-table :data="detailDialog.data.activePlans" style="width: 100%" size="small">
            <el-table-column prop="planId" label="ID" width="60" />
            <el-table-column prop="patientName" label="患者" width="120" />
            <el-table-column prop="title" label="计划名称" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="status" label="状态" width="100">
               <template #default="{ row }">
                <el-tag size="small" type="success">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 任务分配弹窗 -->
    <el-dialog v-model="assignDialog.visible" title="任务调度分配" width="500px">
      <el-form :model="assignDialog.form" label-width="100px">
        <el-form-item label="当前医生">
          <el-input :value="assignDialog.sourceDoctorName" disabled />
        </el-form-item>
        <el-form-item label="目标医生" required>
          <el-select v-model="assignDialog.form.targetDoctorId" placeholder="请选择目标医生">
            <el-option
              v-for="doc in availableDoctors"
              :key="doc.id"
              :label="`${doc.nickname} (${doc.collaborationStatus})`"
              :value="doc.id"
              :disabled="doc.id === assignDialog.form.sourceDoctorId"
            />
          </el-select>
          <div class="form-tip">
            将该医生最近的一个活跃会话转移给目标医生。
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign" :loading="assignDialog.submitting">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import dayjs from 'dayjs';
import { getDoctorCollaborationStats, getDoctorCollaborationDetail, assignDoctorTask } from '../../../api/admin';

const loading = ref(false);
const doctors = ref([]);

// 详情弹窗状态
const detailDialog = reactive({
  visible: false,
  loading: false,
  data: null
});

// 分配弹窗状态
const assignDialog = reactive({
  visible: false,
  submitting: false,
  sourceDoctorName: '',
  form: {
    sourceDoctorId: null,
    targetDoctorId: null
  }
});

// 可用于分配的目标医生列表 (排除自己)
const availableDoctors = computed(() => {
  return doctors.value;
});

const fetchStats = async () => {
  loading.value = true;
  try {
    const res = await getDoctorCollaborationStats();
    doctors.value = res.data || res;
  } catch (error) {
    ElMessage.error('加载监控数据失败');
  } finally {
    loading.value = false;
  }
};

const totalActiveConsultations = computed(() => {
  return doctors.value.reduce((acc, curr) => acc + (curr.activeConsultations || 0), 0);
});

const highLoadDoctorsCount = computed(() => {
  return doctors.value.filter(d => d.collaborationStatus === '高负载').length;
});

const getStatusType = (status) => {
  switch (status) {
    case '高负载': return 'danger';
    case '忙碌': return 'warning';
    case '空闲/在线': return 'success';
    default: return 'info';
  }
};

const formatTime = (time) => {
  if (!time) return '无记录';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// 查看明细
const viewDetail = async (row) => {
  detailDialog.visible = true;
  detailDialog.loading = true;
  detailDialog.data = null;
  
  try {
    const res = await getDoctorCollaborationDetail(row.id);
    detailDialog.data = res.data || res;
  } catch (error) {
    ElMessage.error('获取医生详情失败');
  } finally {
    detailDialog.loading = false;
  }
};

// 打开分配弹窗
const assignTask = (row) => {
  assignDialog.sourceDoctorName = row.nickname;
  assignDialog.form.sourceDoctorId = row.id;
  assignDialog.form.targetDoctorId = null;
  assignDialog.visible = true;
};

// 提交分配
const confirmAssign = async () => {
  if (!assignDialog.form.targetDoctorId) {
    ElMessage.warning('请选择目标医生');
    return;
  }
  
  assignDialog.submitting = true;
  try {
    await assignDoctorTask({
      sourceDoctorId: assignDialog.form.sourceDoctorId,
      targetDoctorId: assignDialog.form.targetDoctorId
    });
    ElMessage.success('任务分配成功');
    assignDialog.visible = false;
    fetchStats(); // 刷新列表数据
  } catch (error) {
    ElMessage.error('任务分配失败');
  } finally {
    assignDialog.submitting = false;
  }
};

onMounted(fetchStats);
</script>

<style scoped>
.doctor-collaboration-container {
  padding: 2rem;
}
.page-header h1 {
  font-size: 2rem;
  font-weight: bold;
}
.subtitle {
  font-size: 1rem;
  color: #6c757d;
  margin-top: 0.5rem;
  margin-bottom: 2rem;
}
.stats-overview {
  margin-bottom: 2rem;
}
.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
  text-align: center;
}
.stat-value.busy { color: #f56c6c; }
.stat-value.success { color: #67c23a; }
.doctor-list-card {
  margin-top: 1rem;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
