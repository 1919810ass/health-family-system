<template>
  <div class="triage-chat-window">
    <div class="chat-header">
      <div class="header-info">
        <el-avatar :size="36" src="https://api.dicebear.com/7.x/bottts/svg?seed=HealthAI" />
        <span class="title">AI 预问诊助手 (小Q)</span>
      </div>
      <el-button v-if="!isFinished" size="small" type="danger" plain @click="handleFinish">结束问诊</el-button>
    </div>

    <div class="chat-content" ref="chatContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
        <el-avatar v-if="msg.role === 'ai'" :size="32" src="https://api.dicebear.com/7.x/bottts/svg?seed=HealthAI" class="avatar" />
        <div class="message-bubble">
          <div class="message-text" v-html="renderMessage(msg.content)"></div>
        </div>
        <el-avatar v-if="msg.role === 'user'" :size="32" icon="el-icon-user" class="avatar" />
      </div>
      <div v-if="loading" class="message-item ai">
         <el-avatar :size="32" src="https://api.dicebear.com/7.x/bottts/svg?seed=HealthAI" class="avatar" />
         <div class="message-bubble typing">
            <span></span><span></span><span></span>
         </div>
      </div>
    </div>

    <div class="chat-input" v-if="!isFinished">
      <el-input
        v-model="inputMessage"
        placeholder="请描述您的症状..."
        @keyup.enter="sendMessage"
        :disabled="loading"
      >
        <template #append>
          <el-button @click="sendMessage" :loading="loading">发送</el-button>
        </template>
      </el-input>
    </div>

    <div v-else class="summary-card">
       <el-alert title="预问诊已结束" type="success" :closable="false" show-icon>
         <template #default>
           <div class="summary-content" v-html="renderMarkdown(summary)"></div>
           <div class="actions">
             <el-button type="primary" @click="$emit('triage-complete', summary)">提交给医生</el-button>
           </div>
         </template>
       </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { triageChat, finishTriage } from '../../../api/consultation'
import MarkdownIt from 'markdown-it'

const props = defineProps({
  sessionId: {
    type: [Number, String],
    required: true
  }
})

const emit = defineEmits(['triage-complete'])

const md = new MarkdownIt()
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const isFinished = ref(false)
const summary = ref('')
const chatContainer = ref(null)

const renderMessage = (text) => {
  return text ? text.replace(/\n/g, '<br>') : ''
}

const renderMarkdown = (text) => {
  return text ? md.render(text) : ''
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) return

  messages.value.push({ role: 'user', content })
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()

  try {
    const res = await triageChat({
      sessionId: props.sessionId,
      userMessage: content
    })
    
    // Check if res is already the data or wrapped
    const reply = res.data ? res.data : res
    
    // If response format is { code, message, data: "..." }
    const replyText = (typeof reply === 'string') ? reply : (reply.data || JSON.stringify(reply))

    messages.value.push({ role: 'ai', content: replyText })
    
    if (replyText.includes('生成病历摘要') || replyText.includes('结束提问')) {
        handleFinish()
    }
  } catch (error) {
    console.error('Chat error:', error)
    messages.value.push({ role: 'ai', content: '抱歉，我遇到了一些问题，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const handleFinish = async () => {
  loading.value = true
  try {
    const res = await finishTriage({ sessionId: props.sessionId })
    const data = res.data ? res.data : res
    summary.value = (typeof data === 'string') ? data : (data.data || JSON.stringify(data))
    isFinished.value = true
  } catch (error) {
    console.error('Finish error:', error)
  } finally {
    loading.value = false
  }
}

// Initial greeting
onMounted(() => {
  messages.value.push({ 
    role: 'ai', 
    content: '您好，我是您的AI预问诊助手小Q。请问您哪里不舒服？(例如：头痛、发烧、胃痛等)' 
  })
})
</script>

<style scoped lang="scss">
.triage-chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;

  .chat-header {
    padding: 12px 20px;
    background: #fff;
    border-bottom: 1px solid #ebeef5;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-info {
        display: flex;
        align-items: center;
        gap: 10px;
        font-weight: 600;
        .title {
            color: #303133;
        }
    }
  }

  .chat-content {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 15px;

    .message-item {
      display: flex;
      gap: 10px;
      max-width: 80%;

      &.user {
        align-self: flex-end;
        flex-direction: row-reverse;
        
        .message-bubble {
            background: #409EFF;
            color: #fff;
            border-radius: 12px 12px 0 12px;
        }
      }

      &.ai {
        align-self: flex-start;
        
        .message-bubble {
            background: #fff;
            color: #303133;
            border-radius: 12px 12px 12px 0;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
        }
      }

      .message-bubble {
        padding: 10px 15px;
        font-size: 14px;
        line-height: 1.5;
        
        &.typing {
            display: flex;
            gap: 4px;
            padding: 15px;
            
            span {
                width: 6px;
                height: 6px;
                background: #909399;
                border-radius: 50%;
                animation: typing 1s infinite ease-in-out;
                
                &:nth-child(1) { animation-delay: 0s; }
                &:nth-child(2) { animation-delay: 0.2s; }
                &:nth-child(3) { animation-delay: 0.4s; }
            }
        }
      }
    }
  }

  .chat-input {
    padding: 20px;
    background: #fff;
    border-top: 1px solid #ebeef5;
  }
  
  .summary-card {
      padding: 20px;
      background: #fff;
      height: 100%;
      overflow-y: auto;
      
      .summary-content {
          margin: 15px 0;
          
          :deep(h3) {
              margin-top: 0;
              color: #409EFF;
          }
          
          :deep(ul) {
              padding-left: 20px;
          }
          
          :deep(strong) {
              color: #303133;
          }
      }
      
      .actions {
          margin-top: 20px;
          text-align: right;
      }
  }
}

@keyframes typing {
    0%, 100% { transform: translateY(0); opacity: 0.6; }
    50% { transform: translateY(-4px); opacity: 1; }
}
</style>
