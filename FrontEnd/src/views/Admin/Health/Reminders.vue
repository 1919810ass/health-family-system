<template>
  <div class="reminders-management-container">
    <div class="page-header">
      <h1>健康提醒管理</h1>
      <p class="subtitle">设置和管理全平台用户可选择的健康提醒模板。</p>
    </div>

    <!-- 搜索和操作 -->
    <el-card class="box-card">
      <div class="card-header">
        <el-form :model="searchForm" inline>
          <el-form-item label="提醒内容">
            <el-input v-model="searchForm.content" placeholder="输入关键字搜索" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.category" placeholder="选择分类" clearable @change="handleSearch">
              <el-option label="用药提醒" value="用药提醒" />
              <el-option label="运动提醒" value="运动提醒" />
              <el-option label="饮食提醒" value="饮食提醒" />
              <el-option label="作息提醒" value="作息提醒" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="handleCreate">新增模板</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="templates" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="提醒内容" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="userCount" label="设置用户数" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除这个模板吗?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px">
      <el-form :model="dialog.form" :rules="dialog.rules" ref="dialogFormRef" label-width="80px">
        <el-form-item label="提醒内容" prop="content">
          <el-input v-model="dialog.form.content" type="textarea" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="dialog.form.category" placeholder="选择分类">
            <el-option label="用药提醒" value="用药提醒" />
            <el-option label="运动提醒" value="运动提醒" />
            <el-option label="饮食提醒" value="饮食提醒" />
            <el-option label="作息提醒" value="作息提醒" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="dialog.form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getReminderTemplates, createReminderTemplate, updateReminderTemplate, deleteReminderTemplate } from '../../../api/admin';

const loading = ref(true);
const templates = ref([]);
const searchForm = reactive({ content: '', category: '' });
const pagination = reactive({ page: 1, size: 10, total: 0 });

const dialog = reactive({
  visible: false,
  title: '',
  form: { id: null, content: '', category: '', status: 1, userCount: 0 },
  rules: {
    content: [{ required: true, message: '请输入提醒内容', trigger: 'blur' }],
    category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  },
});
const dialogFormRef = ref(null);

const fetchTemplates = async () => {
  loading.value = true;
  try {
    const params = { ...searchForm, page: pagination.page, size: pagination.size };
    const res = await getReminderTemplates(params);
    const data = res.data || res;
    // Spring Data Page 返回的数据结构是 content 和 totalElements
    templates.value = data.content || [];
    pagination.total = data.totalElements || 0;
  } catch (error) {
    ElMessage.error('加载提醒模板失败');
  } finally {
    loading.value = false;
  }
};

onMounted(fetchTemplates);

const handleSearch = () => {
  pagination.page = 1;
  fetchTemplates();
};

const handleReset = () => {
  searchForm.content = '';
  searchForm.category = '';
  handleSearch();
};

const handleCreate = () => {
  dialog.title = '新增提醒模板';
  dialog.form = { id: null, content: '', category: '', status: 1, userCount: 0 };
  dialog.visible = true;
};

const handleEdit = (row) => {
  dialog.title = '编辑提醒模板';
  dialog.form = { ...row };
  dialog.visible = true;
};

const handleDelete = async (id) => {
  try {
    await deleteReminderTemplate(id);
    ElMessage.success('删除成功');
    fetchTemplates();
  } catch (error) {
    ElMessage.error('删除失败');
  }
};

const submitForm = async () => {
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialog.form.id) {
          await updateReminderTemplate(dialog.form.id, dialog.form);
          ElMessage.success('更新成功');
        } else {
          await createReminderTemplate(dialog.form);
          ElMessage.success('创建成功');
        }
        dialog.visible = false;
        fetchTemplates();
      } catch (error) {
        ElMessage.error('操作失败');
      }
    }
  });
};

const handleSizeChange = (size) => {
  pagination.size = size;
  fetchTemplates();
};

const handleCurrentChange = (page) => {
  pagination.page = page;
  fetchTemplates();
};
</script>

<style scoped>
.reminders-management-container {
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.pagination-container {
  margin-top: 1.5rem;
  justify-content: flex-end;
}
</style>
