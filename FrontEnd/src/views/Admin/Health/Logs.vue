<template>
  <div class="logs-management-container">
    <div class="page-header">
      <h1>健康日志管理</h1>
      <p class="subtitle">搜索、筛选并审核全平台用户的健康日志记录。</p>
    </div>

    <el-card class="box-card">
      <!-- 搜索栏 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="用户">
          <el-input v-model="searchForm.userKeyword" placeholder="ID/昵称/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="日志类型">
          <el-select v-model="searchForm.logType" placeholder="选择类型" clearable>
            <el-option label="饮食" value="DIET" />
            <el-option label="睡眠" value="SLEEP" />
            <el-option label="运动" value="SPORT" />
            <el-option label="心情" value="MOOD" />
            <el-option label="体征" value="VITALS" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="内容关键字">
          <el-input v-model="searchForm.contentKeyword" placeholder="搜索日志内容" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <!-- 数据表格 -->
      <el-table :data="logs" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="日志ID" width="100" />
        <el-table-column prop="userNickname" label="用户昵称" width="150" />
        <el-table-column prop="type" label="日志类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ formatLogType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contentJson" label="日志内容 (摘要)" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="记录时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination-container"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog title="日志详情" v-model="detailDialog.visible" width="600px">
      <pre>{{ detailDialog.content }}</pre>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getHealthLogs } from '../../../api/admin';

const loading = ref(false);
const logs = ref([]);
const searchForm = reactive({
  userKeyword: '',
  logType: '',
  dateRange: [],
  contentKeyword: ''
});
const pagination = reactive({ page: 1, size: 10, total: 0 });

const detailDialog = reactive({ visible: false, content: '' });

const fetchLogs = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page - 1, // Spring Data Pageable is 0-indexed
      size: pagination.size,
      userKeyword: searchForm.userKeyword || null,
      logType: searchForm.logType || null,
      startDate: searchForm.dateRange ? searchForm.dateRange[0] : null,
      endDate: searchForm.dateRange ? searchForm.dateRange[1] : null,
      contentKeyword: searchForm.contentKeyword || null,
    };
    const res = await getHealthLogs(params);
    const data = res.data || res;
    logs.value = data.content || [];
    pagination.total = data.totalElements || 0;
  } catch (error) {
    ElMessage.error('加载日志失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onMounted(fetchLogs);

const handleSearch = () => {
  pagination.page = 1;
  fetchLogs();
};

const resetSearch = () => {
  searchForm.userKeyword = '';
  searchForm.logType = '';
  searchForm.dateRange = [];
  searchForm.contentKeyword = '';
  handleSearch();
};

const handleSizeChange = (size) => {
  pagination.size = size;
  fetchLogs();
};

const handleCurrentChange = (page) => {
  pagination.page = page;
  fetchLogs();
};

const showDetail = (row) => {
  try {
    const formattedJson = JSON.stringify(JSON.parse(row.contentJson), null, 2);
    detailDialog.content = formattedJson;
  } catch (e) {
    detailDialog.content = row.contentJson; // if not a valid JSON, show raw
  }
  detailDialog.visible = true;
};

const formatLogType = (type) => {
  const map = { DIET: '饮食', SLEEP: '睡眠', SPORT: '运动', MOOD: '心情', VITALS: '体征' };
  return map[type] || type;
};

</script>

<style scoped>
.logs-management-container {
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
  margin-bottom: 1.5rem;
}
.search-form .el-form-item {
  margin-bottom: 10px;
}
.pagination-container {
  margin-top: 1.5rem;
  justify-content: flex-end;
}
pre {
  background-color: #f5f5f5;
  padding: 1rem;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
