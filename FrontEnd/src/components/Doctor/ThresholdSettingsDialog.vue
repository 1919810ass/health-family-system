<template>
  <el-dialog
    v-model="visible"
    title="设置健康阈值"
    width="600px"
    @open="loadThresholds"
    destroy-on-close
  >
    <div v-loading="loading">
      <el-alert
        title="设置患者的各项指标正常范围，超出范围将在用户端和医生端显示为异常。"
        type="info"
        show-icon
        class="mb-16"
      />
      
      <div class="threshold-group">
        <div class="group-title">血压 (mmHg)</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收缩压">
              <div class="range-inputs">
                <el-input-number v-model="form['血压_收缩压'].min" :controls="false" placeholder="下限" />
                <span class="separator">-</span>
                <el-input-number v-model="form['血压_收缩压'].max" :controls="false" placeholder="上限" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="舒张压">
              <div class="range-inputs">
                <el-input-number v-model="form['血压_舒张压'].min" :controls="false" placeholder="下限" />
                <span class="separator">-</span>
                <el-input-number v-model="form['血压_舒张压'].max" :controls="false" placeholder="上限" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="threshold-group">
        <div class="group-title">血糖 (mmol/L)</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="空腹血糖">
              <div class="range-inputs">
                <el-input-number v-model="form['血糖_空腹'].min" :step="0.1" :precision="1" :controls="false" placeholder="下限" />
                <span class="separator">-</span>
                <el-input-number v-model="form['血糖_空腹'].max" :step="0.1" :precision="1" :controls="false" placeholder="上限" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="餐后血糖">
              <div class="range-inputs">
                <el-input-number v-model="form['血糖_餐后'].min" :step="0.1" :precision="1" :controls="false" placeholder="下限" />
                <span class="separator">-</span>
                <el-input-number v-model="form['血糖_餐后'].max" :step="0.1" :precision="1" :controls="false" placeholder="上限" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="threshold-group">
        <div class="group-title">其他指标</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="心率 (bpm)">
              <div class="range-inputs">
                <el-input-number v-model="form['心率'].min" :controls="false" placeholder="下限" />
                <span class="separator">-</span>
                <el-input-number v-model="form['心率'].max" :controls="false" placeholder="上限" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体温 (°C)">
              <div class="range-inputs">
                <el-input-number v-model="form['体温'].min" :step="0.1" :precision="1" :controls="false" placeholder="下限" />
                <span class="separator">-</span>
                <el-input-number v-model="form['体温'].max" :step="0.1" :precision="1" :controls="false" placeholder="上限" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="mt-16">
            <el-form-item label="体重 (kg)">
              <div class="range-inputs">
                <el-input-number v-model="form['体重'].min" :step="0.1" :precision="1" :controls="false" placeholder="下限" />
                <span class="separator">-</span>
                <el-input-number v-model="form['体重'].max" :step="0.1" :precision="1" :controls="false" placeholder="上限" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存设置</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getPatientThresholds, savePatientThreshold } from '../../api/doctor'

const props = defineProps({
  userId: {
    type: [String, Number],
    required: true
  },
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const saving = ref(false)

// 默认值参考
const defaultRanges = {
  '血压_收缩压': { min: 90, max: 140 },
  '血压_舒张压': { min: 60, max: 90 },
  '血糖_空腹': { min: 3.9, max: 6.1 },
  '血糖_餐后': { min: 4.4, max: 7.8 },
  '心率': { min: 60, max: 100 },
  '体温': { min: 36.0, max: 37.3 },
  '体重': { min: 40, max: 100 }
}

const form = reactive(JSON.parse(JSON.stringify(defaultRanges)))

async function loadThresholds() {
  if (!props.userId) return
  loading.value = true
  try {
    const res = await getPatientThresholds(props.userId)
    if (res && res.data) {
      // 合并后端数据
      res.data.forEach(item => {
        if (form[item.metric]) {
          form[item.metric].min = item.lowerBound
          form[item.metric].max = item.upperBound
        }
      })
    }
  } catch (e) {
    console.error('加载阈值失败', e)
    ElMessage.error('加载阈值配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const promises = []
    for (const [metric, range] of Object.entries(form)) {
      // 只有当值有变化或不为空时才保存? 
      // 简单起见，保存所有有效值
      if (range.min != null && range.max != null) {
        promises.push(savePatientThreshold(props.userId, {
          metric,
          lowerBound: range.min,
          upperBound: range.max
        }))
      }
    }
    
    await Promise.all(promises)
    ElMessage.success('阈值设置已保存')
    visible.value = false
    emit('saved')
  } catch (e) {
    console.error('保存失败', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.mb-16 { margin-bottom: 16px; }
.mt-16 { margin-top: 16px; }

.threshold-group {
  margin-bottom: 24px;
  
  .group-title {
    font-weight: 600;
    margin-bottom: 12px;
    padding-left: 8px;
    border-left: 3px solid var(--el-color-primary);
  }
}

.range-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .separator {
    color: var(--el-text-color-secondary);
  }
  
  :deep(.el-input-number) {
    width: 100%;
  }
}
</style>
