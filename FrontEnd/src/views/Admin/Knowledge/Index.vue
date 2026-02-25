<template>
  <div class="app-container">
    <div class="filter-container">
      <el-upload
        class="upload-demo"
        action="#"
        :http-request="handleUpload"
        :show-file-list="false"
        accept=".md,.txt"
        :disabled="uploading"
      >
        <el-button type="primary" :loading="uploading">
          <el-icon class="el-icon--left"><Upload /></el-icon>上传文档
        </el-button>
      </el-upload>
      <el-button type="warning" @click="handleSync" class="sync-btn" :loading="syncing">
        <el-icon class="el-icon--left"><Refresh /></el-icon>同步到向量库
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%; margin-top: 20px;"
    >
      <el-table-column label="ID" prop="id" align="center" width="80" />
      
      <el-table-column label="标题" min-width="200px">
        <template #default="{ row }">
          <router-link :to="'/admin/knowledge/' + row.id" class="link-type">
            <span>{{ row.title }}</span>
          </router-link>
        </template>
      </el-table-column>

      <el-table-column label="上传时间" width="180px" align="center">
        <template #default="{ row }">
          <span>{{ formatDateTime(row.createdAt) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="版本" width="100px" align="center">
        <template #default="{ row }">
          <el-tag>{{ row.version }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100px" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">
            {{ row.enabled ? '已启用' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="$router.push('/admin/knowledge/' + row.id)">
            详情
          </el-button>
          <el-button size="small" type="warning" @click="handleReEmbed(row)">
            重建索引
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-show="total > 0"
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Upload, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDocuments, uploadDocument, deleteDocument, syncVectorStore, reEmbedDocument } from '@/api/knowledge'
import dayjs from 'dayjs'

const list = ref([])
const total = ref(0)
const loading = ref(true)
const uploading = ref(false)
const syncing = ref(false)

const queryParams = reactive({
  page: 1,
  size: 10
})

onMounted(() => {
  getList()
})

async function getList() {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1,
      size: queryParams.size
    }
    const response = await getDocuments(params)
    list.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载文档失败')
  } finally {
    loading.value = false
  }
}

async function handleUpload(option) {
  uploading.value = true
  const formData = new FormData()
  formData.append('file', option.file)
  
  try {
    await uploadDocument(formData)
    ElMessage.success('上传成功')
    getList()
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确认删除该文档?', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDocument(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

async function handleReEmbed(row) {
  try {
    await reEmbedDocument(row.id)
    ElMessage.success('重建索引请求已发送')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function handleSync() {
  syncing.value = true
  try {
    await syncVectorStore()
    ElMessage.success('向量库同步已触发')
  } catch (error) {
    ElMessage.error('同步失败')
  } finally {
    syncing.value = false
  }
}

function formatDateTime(time) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : ''
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.filter-container {
  padding-bottom: 10px;
  display: flex;
  gap: 10px;
}
.sync-btn {
  margin-left: 10px;
}
.link-type {
  color: #409EFF;
  cursor: pointer;
  text-decoration: none;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
