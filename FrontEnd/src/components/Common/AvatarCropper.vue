<template>
  <el-dialog
    v-model="dialogVisible"
    title="头像裁剪"
    width="600px"
    :close-on-click-modal="false"
    append-to-body
    @close="handleClose"
  >
    <div class="cropper-container">
      <vue-cropper
        ref="cropperRef"
        :img="imgSrc"
        :output-size="1"
        :output-type="'png'"
        :info="true"
        :can-scale="true"
        :auto-crop="true"
        :auto-crop-width="200"
        :auto-crop-height="200"
        :fixed="true"
        :fixed-number="[1, 1]"
        :center-box="true"
        :high="true"
        mode="cover"
      />
    </div>
    
    <div class="cropper-toolbar">
      <el-button-group>
        <el-button :icon="ZoomIn" @click="changeScale(1)">放大</el-button>
        <el-button :icon="ZoomOut" @click="changeScale(-1)">缩小</el-button>
        <el-button :icon="RefreshLeft" @click="rotateLeft">左旋转</el-button>
        <el-button :icon="RefreshRight" @click="rotateRight">右旋转</el-button>
      </el-button-group>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">保存头像</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ZoomIn, ZoomOut, RefreshLeft, RefreshRight } from '@element-plus/icons-vue'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  file: {
    type: [File, null],
    default: null
  }
})

const emit = defineEmits(['update:visible', 'confirm'])

const dialogVisible = ref(false)
const imgSrc = ref('')
const cropperRef = ref(null)
const loading = ref(false)

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      imgSrc.value = e.target.result
    }
    reader.readAsDataURL(props.file)
  }
})

watch(() => dialogVisible.value, (val) => {
  emit('update:visible', val)
})

const handleClose = () => {
  dialogVisible.value = false
}

const changeScale = (num) => {
  cropperRef.value?.changeScale(num)
}

const rotateLeft = () => {
  cropperRef.value?.rotateLeft()
}

const rotateRight = () => {
  cropperRef.value?.rotateRight()
}

const handleConfirm = () => {
  loading.value = true
  cropperRef.value?.getCropBlob((blob) => {
    loading.value = false
    if (blob) {
      // 创建一个新的 File 对象，保持原有文件名（如果需要）
      const fileName = props.file?.name || 'avatar.png'
      const newFile = new File([blob], fileName, { type: 'image/png' })
      emit('confirm', newFile)
      handleClose()
    }
  })
}
</script>

<style scoped lang="scss">
.cropper-container {
  height: 400px;
  width: 100%;
}

.cropper-toolbar {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
