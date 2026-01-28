<template>
  <div class="ai-chat-container">
    <div class="page-header">
      <div class="header-icon">
        <el-icon><Cpu /></el-icon>
      </div>
      <div class="header-content">
        <h2 class="title">AI 健康助手</h2>
        <p class="subtitle">智能问答，您的私人健康顾问</p>
      </div>
      <div class="header-right">
        <el-tag type="success" size="small" effect="dark">RAG 增强模式</el-tag>
        <el-tag v-if="hasImage" type="warning" size="small" effect="dark" style="margin-left: 8px;">多模态开启</el-tag>
      </div>
    </div>

    <div class="chat-messages" ref="messagesRef">
      <div v-if="messages.length === 0" class="empty-state">
        <el-empty description="有什么健康问题想问我吗？" :image-size="120">
          <template #extra>
            <div class="suggestion-chips">
              <el-tag 
                v-for="q in suggestions" 
                :key="q" 
                class="chip" 
                @click="sendMessage(q)"
                effect="plain"
                round
              >
                {{ q }}
              </el-tag>
            </div>
          </template>
        </el-empty>
      </div>

      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        class="message-wrapper"
        :class="{ 'user-message': msg.role === 'user', 'ai-message': msg.role === 'ai' }"
      >
        <div class="avatar">
          <el-avatar v-if="msg.role === 'user'" :icon="UserFilled" class="user-avatar" />
          <el-avatar v-else class="ai-avatar">AI</el-avatar>
        </div>
        <div class="message-content">
          <div class="bubble">
            <div v-if="msg.image" class="message-image">
               <el-image :src="msg.image" fit="contain" :preview-src-list="[msg.image]" />
            </div>
            <div v-if="msg.role === 'ai'" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
            <div v-else>{{ msg.content }}</div>
            <span v-if="msg.role === 'ai' && msg.loading" class="typing-cursor"></span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <!-- Image Preview Area -->
      <div v-if="uploadImage" class="image-preview-area">
        <div class="preview-item">
          <img :src="uploadImage" alt="Preview" />
          <el-icon class="remove-icon" @click="clearImage"><CircleCloseFilled /></el-icon>
        </div>
      </div>

      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="输入您的问题，或者上传舌象/药品图片进行分析..."
        @keydown.enter.prevent="handleEnter"
        :disabled="isLoading"
        resize="none"
      />
      <div class="input-actions">
        <!-- Image Upload Button -->
        <el-upload
          class="upload-btn"
          action="#"
          :show-file-list="false"
          :auto-upload="false"
          :on-change="handleImageChange"
          accept="image/*"
        >
          <el-button :icon="Picture" circle plain title="上传图片" />
        </el-upload>

        <el-button type="primary" :loading="isLoading" @click="handleSend" :disabled="!inputMessage.trim() && !uploadImage">
          发送 <el-icon class="el-icon--right"><Position /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { UserFilled, Cpu, Position, Picture, CircleCloseFilled } from '@element-plus/icons-vue'
import { chatStream, chatImageStream } from '../../api/aiChat'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { ElMessage } from 'element-plus'

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesRef = ref(null)
const uploadImage = ref(null) // Base64 image string

const hasImage = computed(() => !!uploadImage.value)

const suggestions = [
  '我是什么体质？',
  '最近总是失眠怎么办？',
  '气虚体质适合吃什么？',
  '分析这张舌象图片（请先上传）'
]

const renderMarkdown = (text) => {
  const rawHtml = marked.parse(text || '')
  return DOMPurify.sanitize(rawHtml)
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const handleEnter = (e) => {
  if (!e.shiftKey) {
    handleSend()
  }
}

const sendMessage = (text) => {
  inputMessage.value = text
  handleSend()
}

// Handle Image Selection
const handleImageChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    uploadImage.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

const clearImage = () => {
  uploadImage.value = null
}

const handleSend = async () => {
  const content = inputMessage.value.trim()
  const image = uploadImage.value

  if ((!content && !image) || isLoading.value) return

  // 添加用户消息
  messages.value.push({ 
    role: 'user', 
    content: content || (image ? '[发送了一张图片]' : ''),
    image: image 
  })
  
  inputMessage.value = ''
  uploadImage.value = null // Clear image after sending
  isLoading.value = true
  scrollToBottom()

  // 添加 AI 占位消息
  const aiMsgIndex = messages.value.length
  messages.value.push({ role: 'ai', content: '', loading: true })

  const onChunk = (chunk) => {
    messages.value[aiMsgIndex].content += chunk
    scrollToBottom()
  }

  const onError = (error) => {
    messages.value[aiMsgIndex].content += '\n\n*(发生错误: ' + error.message + ')*'
    messages.value[aiMsgIndex].loading = false
  }

  const onComplete = () => {
    messages.value[aiMsgIndex].loading = false
  }

  try {
    if (image) {
      // Image Stream
      await chatImageStream(content || '请分析这张图片', image, onChunk, onError, onComplete)
    } else {
      // Text Stream (RAG)
      await chatStream(content, onChunk, onError, onComplete)
    }
  } catch (e) {
    messages.value[aiMsgIndex].loading = false
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables' as vars;
@use '@/styles/mixins' as mixins;

.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: vars.$shadow-sm;

  .page-header {
    display: flex;
    align-items: center;
    padding: 16px 24px;
    border-bottom: 1px solid #f0f0f0;
    gap: 16px;

    .header-icon {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: linear-gradient(135deg, vars.$primary-color, color.adjust(vars.$primary-color, $lightness: 15%));
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(vars.$primary-color, 0.3);

      .el-icon {
        font-size: 20px;
        color: #fff;
      }
    }

    .header-content {
      flex: 1;
      .title {
        font-size: 18px;
        font-weight: 700;
        color: vars.$text-main-color;
        margin: 0 0 2px 0;
        @include mixins.text-gradient(linear-gradient(to right, vars.$text-main-color, vars.$primary-color));
      }

      .subtitle {
        font-size: 12px;
        color: vars.$text-secondary-color;
        margin: 0;
      }
    }
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    background-color: #f9f9fa;
    scroll-behavior: smooth;

    .empty-state {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .suggestion-chips {
        margin-top: 20px;
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        justify-content: center;
        max-width: 500px;

        .chip {
          cursor: pointer;
          transition: all 0.2s;
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
          }
        }
      }
    }

    .message-wrapper {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      
      &.user-message {
        flex-direction: row-reverse;
        
        .bubble {
          background-color: var(--el-color-primary);
          color: #fff;
          border-top-right-radius: 2px;
          
          .message-image {
            margin-bottom: 8px;
            img {
               border-radius: 8px;
               max-width: 200px;
               border: 2px solid rgba(255,255,255,0.2);
            }
          }
        }
        
        .user-avatar {
          background-color: var(--el-color-primary-light-3);
        }
      }

      &.ai-message {
        .bubble {
          background-color: #fff;
          color: #333;
          border-top-left-radius: 2px;
          box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }
        
        .ai-avatar {
          background-color: var(--el-color-success);
        }
      }

      .message-content {
        max-width: 70%;
        
        .bubble {
          padding: 12px 16px;
          border-radius: 12px;
          line-height: 1.6;
          position: relative;
          word-break: break-word;

          .message-image {
            width: 100%;
            max-width: 360px;
            border-radius: 8px;
            overflow: hidden;
            background: #fff;
          }

          :deep(.el-image) {
            width: 100%;
            height: auto;
            border-radius: 8px;
            display: block;
          }

          :deep(.el-image__inner) {
            width: 100%;
            height: auto;
            object-fit: contain;
          }

          :deep(.markdown-body) {
            font-size: 15px;
            p { margin-bottom: 10px; &:last-child { margin-bottom: 0; } }
            ul, ol { padding-left: 20px; margin-bottom: 10px; }
            h3, h4 { margin-top: 15px; margin-bottom: 8px; font-weight: 600; }
            code { background-color: rgba(0,0,0,0.05); padding: 2px 4px; border-radius: 4px; }
            pre { background-color: #f4f4f4; padding: 12px; border-radius: 8px; overflow-x: auto; }
          }

          .typing-cursor {
            display: inline-block;
            width: 8px;
            height: 16px;
            background-color: currentColor;
            margin-left: 4px;
            vertical-align: middle;
            animation: blink 1s step-end infinite;
          }
        }
      }
    }
  }

  .chat-input-area {
    padding: 16px 24px;
    background-color: #fff;
    border-top: 1px solid #eee;
    position: relative;
    
    .image-preview-area {
      padding-bottom: 12px;
      .preview-item {
        position: relative;
        display: inline-block;
        img {
          max-width: 360px;
          max-height: 360px;
          width: auto;
          height: auto;
          border-radius: 8px;
          border: 1px solid #eee;
          object-fit: contain;
          background: #fff;
        }
        .remove-icon {
          position: absolute;
          top: -8px;
          right: -8px;
          cursor: pointer;
          color: #F56C6C;
          background: #fff;
          border-radius: 50%;
          font-size: 20px;
        }
      }
    }

    .input-actions {
      position: absolute;
      bottom: 24px;
      right: 32px;
      display: flex;
      gap: 12px;
      align-items: center;
      
      .upload-btn {
        display: inline-block;
      }
    }

    :deep(.el-textarea__inner) {
      padding-right: 140px; // Make space for buttons
      resize: none;
      box-shadow: none;
      background-color: #f5f7fa;
      border: none;
      border-radius: 8px;
      
      &:focus {
        background-color: #fff;
        box-shadow: 0 0 0 1px var(--el-color-primary) inset;
      }
    }
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
