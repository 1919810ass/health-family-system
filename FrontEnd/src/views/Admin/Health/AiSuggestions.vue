<template>
  <div class="ai-suggestions-container">
    <div class="page-header">
      <h1>AI健康建议</h1>
      <p class="subtitle">通过分析全平台用户的健康数据，为您提炼出可行动的运营洞察。</p>
    </div>

    <div v-if="loading" class="loading-indicator">
      <p>正在加载AI建议...</p>
    </div>

    <div v-if="error" class="error-message">
      <p>加载建议失败：{{ error }}</p>
    </div>

    <div v-if="!loading && !error" class="suggestions-grid">
      <el-card v-for="suggestion in suggestions" :key="suggestion.suggestionType" class="suggestion-card">
        <template #header>
          <div class="card-header">
            <span>{{ suggestion.title }}</span>
          </div>
        </template>
        <div class="card-body">
          <p class="description">{{ suggestion.description }}</p>
          <ul class="item-list">
            <li v-for="item in suggestion.items" :key="item.name">
              <span class="item-name">{{ item.name }}</span>
              <div>
                <span class="item-value">{{ item.value }}</span>
                <span v-if="item.remark" class="item-remark">({{ item.remark }})</span>
              </div>
            </li>
          </ul>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getAiSuggestions } from '../../../api/admin';

const loading = ref(true);
const error = ref(null);
const suggestions = ref([]);

onMounted(async () => {
  try {
    loading.value = true;
    const response = await getAiSuggestions();
    suggestions.value = response.data || response;
  } catch (err) {
    console.error("获取AI建议失败:", err);
    error.value = err.message || '未知错误';
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.ai-suggestions-container {
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
}
.suggestions-grid {
  margin-top: 2rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}
.card-header span {
  font-size: 1.1rem;
  font-weight: 600;
}
.description {
  color: #495057;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
}
.item-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.item-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f1f3f5;
}
.item-list li:last-child {
  border-bottom: none;
}
.item-name {
  font-weight: 500;
}
.item-value {
  font-weight: bold;
  color: #007bff;
}
.item-remark {
  font-size: 0.8rem;
  color: #6c757d;
  margin-left: 0.5rem;
}
.loading-indicator, .error-message {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
  color: #6c757d;
}
</style>
