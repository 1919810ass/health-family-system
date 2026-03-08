<template>
  <div class="report-page glass-effect">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><DataAnalysis /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title-text">家庭健康周报</h2>
        <p class="subtitle-text">通过 AI 生成家庭成员的健康周报</p>
      </div>
    </div>

    <div class="report-controls" v-if="adminFamilies.length > 0">
      <el-select
        v-model="selectedFamilyId"
        placeholder="请选择要生成报告的家庭"
        class="family-select"
        @change="handleFamilyChange"
      >
        <el-option
          v-for="item in adminFamilies"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" @click="generateReport" :loading="loading" v-particles :disabled="!selectedFamilyId">
        <el-icon><MagicStick /></el-icon>
        生成本周报告
      </el-button>
    </div>
    
    <el-empty v-else description="您目前不是任何家庭的管理员，无法生成周报"></el-empty>

    <el-card class="report-card" v-if="reportContent || loading">
      <template #header>
        <div class="card-header">
          <span>AI 分析报告</span>
        </div>
      </template>
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      <div v-else class="report-content" v-html="renderedReport"></div>
    </el-card>
    
    <el-empty v-if="!reportContent && !loading && adminFamilies.length > 0" description="点击按钮生成您的家庭健康周报"></el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { MagicStick, DataAnalysis } from '@element-plus/icons-vue';
import { useFamilyStore } from '../../stores/family';
import { useUserStore } from '../../stores/user';
import { generateWeeklyReport } from '../../api/report';
import { getFamilies } from '../../api/family';
import { marked } from 'marked';

const router = useRouter();
const familyStore = useFamilyStore();
const userStore = useUserStore();
const loading = ref(false);
const reportContent = ref('');
const selectedFamilyId = ref(null);

const adminFamilies = computed(() => {
  return familyStore.families.filter(f => f.isAdmin);
});

onMounted(async () => {
  if (familyStore.families.length === 0) {
    try {
      const res = await getFamilies();
      familyStore.setFamilies(res.data);
    } catch (error) {
      console.error('获取家庭列表失败', error);
    }
  }

  // 权限校验：如果用户不是任何家庭的管理员，且不是系统管理员，则无权访问此页面
  const isPlatformAdmin = userStore.profile?.role === 'ADMIN';
  if (!isPlatformAdmin && adminFamilies.value.length === 0) {
    ElMessage.error('您没有权限访问此页面');
    router.replace('/home');
    return;
  }
  
  // 如果当前 store 中已有选中的家庭，且用户是该家庭管理员，则默认选中
  if (familyStore.current && adminFamilies.value.some(f => f.id === familyStore.current.id)) {
    selectedFamilyId.value = familyStore.current.id;
  } else if (adminFamilies.value.length > 0) {
    // 否则默认选中第一个管理的家庭
    selectedFamilyId.value = adminFamilies.value[0].id;
  }
});

const handleFamilyChange = (val) => {
  const family = familyStore.families.find(f => f.id === val);
  if (family) {
    familyStore.setCurrent(family);
  }
};

const renderedReport = computed(() => {
  if (reportContent.value) {
    return marked(reportContent.value);
  }
  return '';
});

const goBack = () => {
  router.back();
};

const generateReport = async () => {
  const familyId = selectedFamilyId.value;
  if (!familyId) {
    ElMessage.warning('请先选择一个家庭');
    return;
  }

  loading.value = true;
  reportContent.value = '';
  try {
    const res = await generateWeeklyReport(familyId);
    // 这里根据后端返回格式处理，如果是 ResponseEntity<String>，res 可能是直接的字符串
    // 但在 request.js 拦截器中通常会处理成 { code, data, message }
    if (res.code === 0 || typeof res === 'string') {
      reportContent.value = typeof res === 'string' ? res : res.data;
      ElMessage.success('周报生成成功！');
    } else {
      ElMessage.error(res.message || '生成失败');
    }
  } catch (error) {
    ElMessage.error('请求失败，请稍后再试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables.scss' as vars;
@use '@/styles/mixins.scss' as mixins;

.report-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  animation: fadeInDown 0.6s vars.$ease-spring;
  gap: 16px;
  
  .header-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    background: linear-gradient(135deg, vars.$primary-color, color.adjust(vars.$primary-color, $lightness: 15%));
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);

    .el-icon {
      font-size: 24px;
      color: #fff;
    }
  }

  .header-content {
    flex: 1;
    
    .title-text {
      font-size: 24px;
      font-weight: 700;
      color: vars.$text-primary-color;
      margin: 0 0 4px 0;
      @include mixins.text-gradient(linear-gradient(to right, vars.$text-primary-color, vars.$primary-color));
    }
    
    .subtitle-text {
      font-size: 14px;
      color: vars.$text-secondary-color;
      margin: 0;
    }
  }
}

.report-controls {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
  gap: 16px;
  
  .family-select {
    width: 240px;
  }
}

.report-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  
  .card-header {
    font-weight: 600;
  }
  
  .report-content {
    line-height: 1.8;
    :deep(h2) {
      font-size: 20px;
      margin-bottom: 16px;
      border-bottom: 1px solid #eee;
      padding-bottom: 8px;
    }
    :deep(h3) {
      font-size: 18px;
      margin-top: 24px;
      margin-bottom: 12px;
    }
    :deep(ul) {
      padding-left: 20px;
    }
    :deep(strong) {
      color: vars.$primary-color;
    }
  }
}
</style>
