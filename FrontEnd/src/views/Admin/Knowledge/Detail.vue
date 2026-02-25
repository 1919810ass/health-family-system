<template>
  <div class="app-container">
    <div class="header">
      <h2>文档详情: {{ documentTitle }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="chunks"
      border
      fit
      highlight-current-row
      style="width: 100%; margin-top: 20px;"
    >
      <el-table-column label="Chunk Index" prop="chunkIndex" align="center" width="100" />
      
      <el-table-column label="内容预览" min-width="400px">
        <template #default="{ row }">
          <div class="chunk-content">{{ truncate(row.content, 100) }}</div>
        </template>
      </el-table-column>

      <el-table-column label="Embedding Status" width="150px" align="center">
        <template #default="{ row }">
          <el-tag :type="row.embedding ? 'success' : 'warning'">
            {{ row.embedding ? '已生成' : '未生成' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            编辑内容
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Edit Dialog -->
    <el-dialog
      title="编辑切片内容"
      v-model="dialogVisible"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form :model="currentChunk" label-width="80px">
        <el-form-item label="内容">
          <el-input
            v-model="currentChunk.content"
            type="textarea"
            :rows="10"
            placeholder="请输入切片内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDocumentChunks, updateChunk } from '@/api/knowledge'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const documentId = route.params.id
const documentTitle = ref('') // We might need to fetch document details separately if we want title here, or just infer from chunks
const chunks = ref([])
const loading = ref(true)

const dialogVisible = ref(false)
const submitting = ref(false)
const currentChunk = ref({ id: null, content: '' })

onMounted(() => {
  fetchChunks()
})

async function fetchChunks() {
  loading.value = true
  try {
    const response = await getDocumentChunks(documentId)
    // Assuming response.data is List<KnowledgeDocument>
    chunks.value = response.data || []
    if (chunks.value.length > 0) {
      // Assuming chunks have access to parent title, but parent is LAZY loaded in backend entity
      // In JSON serialization, parent might be null or partial.
      // If we need title, we might need a separate API call to get document details, 
      // or rely on what's available.
      // Let's assume the backend might not return parent object in chunks list to avoid recursion/performance.
      // We can get title from the first chunk's title if formatted as "Filename - Chunk X"
      const firstTitle = chunks.value[0].title
      if (firstTitle && firstTitle.includes(' - Chunk')) {
        documentTitle.value = firstTitle.split(' - Chunk')[0]
      } else {
        documentTitle.value = firstTitle
      }
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取切片失败')
  } finally {
    loading.value = false
  }
}

function truncate(str, n) {
  return (str.length > n) ? str.substr(0, n-1) + '...' : str;
}

function handleEdit(row) {
  currentChunk.value = { ...row } // Copy object
  dialogVisible.value = true
}

async function submitEdit() {
  if (!currentChunk.value.content) {
    ElMessage.warning('内容不能为空')
    return
  }
  
  submitting.value = true
  try {
    await updateChunk(currentChunk.value.id, currentChunk.value.content)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    fetchChunks() // Refresh list
  } catch (error) {
    console.error(error)
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.chunk-content {
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
