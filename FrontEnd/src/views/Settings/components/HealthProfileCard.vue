<template>
  <el-card class="settings-card">
    <template #header>
      <div class="card-header">健康画像</div>
      <div class="card-subtitle">完善您的健康信息，AI将为您提供更精准的个性化建议</div>
    </template>
    <el-form :model="form" ref="formRef" label-width="120px">
      <el-divider content-position="left">基础信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别">
            <el-select v-model="form.sex" placeholder="请选择性别" class="glass-select">
              <el-option label="男" value="M" />
              <el-option label="女" value="F" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生日">
            <el-date-picker
              v-model="form.birthday"
              type="date"
              placeholder="选择生日"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="glass-date-picker"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="身高 (cm)">
            <el-input-number v-model="form.heightCm" :min="50" :max="250" :precision="1" class="glass-input-number" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体重 (kg)">
            <el-input-number v-model="form.weightKg" :min="10" :max="300" :precision="1" class="glass-input-number" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">健康标签</el-divider>
      <el-form-item label="慢性疾病">
        <el-select
          v-model="form.healthTags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入健康标签（如：糖尿病、高血压、高血脂等）"
          class="glass-select"
        >
          <el-option label="糖尿病" value="糖尿病" />
          <el-option label="高血压" value="高血压" />
          <el-option label="高血脂" value="高血脂" />
          <el-option label="心脏病" value="心脏病" />
          <el-option label="肾病" value="肾病" />
          <el-option label="肝病" value="肝病" />
          <el-option label="哮喘" value="哮喘" />
          <el-option label="关节炎" value="关节炎" />
          <el-option label="骨质疏松" value="骨质疏松" />
        </el-select>
        <div class="form-tip">选择您已确诊的慢性疾病，帮助AI提供更精准的建议</div>
      </el-form-item>

      <el-form-item label="过敏史">
        <el-select
          v-model="form.allergies"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入过敏原（如：花生、海鲜、花粉等）"
          class="glass-select"
        >
          <el-option label="花生" value="花生" />
          <el-option label="海鲜" value="海鲜" />
          <el-option label="花粉" value="花粉" />
          <el-option label="牛奶" value="牛奶" />
          <el-option label="鸡蛋" value="鸡蛋" />
          <el-option label="药物过敏" value="药物过敏" />
        </el-select>
        <div class="form-tip">记录您的过敏史，确保建议的安全性</div>
      </el-form-item>

      <el-divider content-position="left">生活习惯</el-divider>
      <el-form-item label="饮食习惯">
        <el-input
          v-model="form.lifestyle.diet"
          type="textarea"
          :rows="3"
          placeholder="描述您的饮食习惯，如：素食为主、偏好清淡、喜欢辛辣等"
          class="glass-input"
        />
      </el-form-item>
      <el-form-item label="运动习惯">
        <el-input
          v-model="form.lifestyle.exercise"
          type="textarea"
          :rows="3"
          placeholder="描述您的运动习惯，如：每周跑步3次、喜欢瑜伽、很少运动等"
          class="glass-input"
        />
      </el-form-item>
      <el-form-item label="睡眠习惯">
        <el-input
          v-model="form.lifestyle.sleep"
          type="textarea"
          :rows="3"
          placeholder="描述您的睡眠习惯，如：通常23:00睡觉、睡眠质量一般、经常失眠等"
          class="glass-input"
        />
      </el-form-item>
      <el-form-item label="其他习惯">
        <el-input
          v-model="form.lifestyle.other"
          type="textarea"
          :rows="2"
          placeholder="其他生活习惯，如：吸烟、饮酒、工作压力等"
          class="glass-input"
        />
      </el-form-item>

      <el-divider content-position="left">健康目标</el-divider>
      <el-form-item label="目标">
        <el-select
          v-model="form.goals"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入您的健康目标"
          class="glass-select"
        >
          <el-option label="减重" value="减重" />
          <el-option label="增重" value="增重" />
          <el-option label="改善睡眠" value="改善睡眠" />
          <el-option label="增强体质" value="增强体质" />
          <el-option label="控制血糖" value="控制血糖" />
          <el-option label="控制血压" value="控制血压" />
          <el-option label="提高免疫力" value="提高免疫力" />
          <el-option label="缓解压力" value="缓解压力" />
        </el-select>
        <div class="form-tip">设定您的健康目标，AI将为您制定针对性建议</div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit" v-particles>保存健康画像</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHealthProfile, updateHealthProfile } from '../../../api/user'
import { useUserStore } from '../../../stores/user'

const store = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  sex: '',
  birthday: null,
  heightCm: null,
  weightKg: null,
  healthTags: [],
  allergies: [],
  lifestyle: {
    diet: '',
    exercise: '',
    sleep: '',
    other: ''
  },
  goals: []
})

// 从后端获取现有数据
const loadProfile = async () => {
  try {
    const { data } = await getHealthProfile()
    if (data) {
      form.sex = data.sex || ''
      form.birthday = data.birthday || null
      form.heightCm = data.heightCm ? Number(data.heightCm) : null
      form.weightKg = data.weightKg ? Number(data.weightKg) : null
      form.healthTags = data.healthTags || []
      form.allergies = data.allergies || []
      form.lifestyle = {
        diet: data.lifestyle?.diet || '',
        exercise: data.lifestyle?.exercise || '',
        sleep: data.lifestyle?.sleep || '',
        other: data.lifestyle?.other || ''
      }
      // goals 从对象转为数组
      if (data.goals && typeof data.goals === 'object') {
        form.goals = Object.keys(data.goals).filter(k => data.goals[k])
      } else {
        form.goals = []
      }
    }
  } catch (e) {
    console.error('加载健康画像失败', e)
  }
}

const onSubmit = async () => {
  try {
    loading.value = true
    
    // 构建请求数据
    const payload = {
      sex: form.sex || null,
      birthday: form.birthday || null,
      heightCm: form.heightCm || null,
      weightKg: form.weightKg || null,
      healthTags: form.healthTags.length > 0 ? form.healthTags : null,
      allergies: form.allergies.length > 0 ? form.allergies : null,
      lifestyle: Object.values(form.lifestyle).some(v => v) ? form.lifestyle : null,
      goals: form.goals.length > 0 ? Object.fromEntries(form.goals.map(g => [g, true])) : null
    }

    await updateHealthProfile(payload)
    ElMessage.success('健康画像已保存，AI将根据您的信息提供个性化建议')
  } catch (e) {
    ElMessage.error('保存失败：' + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  form.sex = ''
  form.birthday = null
  form.heightCm = null
  form.weightKg = null
  form.healthTags = []
  form.allergies = []
  form.lifestyle = {
    diet: '',
    exercise: '',
    sleep: '',
    other: ''
  }
  form.goals = []
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.settings-card {
  @include mixins.glass-effect;
  border-radius: vars.$radius-lg;
  transition: all 0.3s vars.$ease-spring;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: vars.$shadow-md;
  }
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  color: vars.$text-main-color;
}

.card-subtitle {
  font-size: 12px;
  color: vars.$text-secondary-color;
  margin-top: 4px;
}

.form-tip {
  font-size: 12px;
  color: vars.$text-secondary-color;
  margin-top: 4px;
}

:deep(.el-divider__text) {
  font-weight: 500;
  color: vars.$text-main-color;
  background-color: transparent !important; /* Ensure divider text bg is transparent on glass card if needed, but usually it matches card bg */
}

/* Glass Form Elements */
.glass-input, .glass-select, .glass-input-number, .glass-date-picker {
  width: 100%;
  
  :deep(.el-input__wrapper), :deep(.el-textarea__inner) {
    background: rgba(255, 255, 255, 0.5);
    box-shadow: none;
    border: 1px solid rgba(255, 255, 255, 0.4);
    border-radius: vars.$radius-base;
    transition: all 0.3s;
    
    &:hover, &.is-focus {
      background: rgba(255, 255, 255, 0.9);
      border-color: vars.$primary-color;
      box-shadow: 0 0 0 1px vars.$primary-color inset;
    }
  }
  
  :deep(.el-input__inner) {
    color: vars.$text-main-color;
  }
  
  /* Input Number Buttons */
  :deep(.el-input-number__decrease), :deep(.el-input-number__increase) {
    background: rgba(255, 255, 255, 0.5);
    border-color: rgba(255, 255, 255, 0.4);
    color: vars.$text-main-color;
    transition: all 0.3s;
    
    &:hover {
      background: vars.$primary-color;
      color: #fff;
      border-color: vars.$primary-color;
    }
  }
}
</style>

