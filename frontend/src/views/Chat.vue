<template>
  <div class="chat-container">
    <el-card class="box-card chat-card">
      <template #header>
        <div class="card-header">
          <span>智能问答助手</span>
          <el-button text bg size="small" @click="clearHistory">清空对话</el-button>
        </div>
      </template>

      <div class="message-area" ref="msgRef">
        <div v-for="(item, index) in msgList" :key="index" :class="['message-row', item.role === 'user' ? 'row-right' : 'row-left']">
          
          <el-avatar :icon="item.role === 'user' ? UserFilled : Service" :size="36" 
            :style="{ backgroundColor: item.role === 'user' ? '#409EFF' : '#67C23A' }" 
          />
          
          <div class="message-content">
            <div class="bubble">{{ item.content }}</div>
            
            <div v-if="item.role === 'ai' && item.citations && item.citations.length > 0" class="citations">
              <div class="citation-title">引用来源:</div>
              <el-tag 
                v-for="(cite, cIndex) in item.citations" 
                :key="cIndex" 
                size="small" 
                type="info" 
                class="citation-tag"
                @click="viewDoc(cite)"
              >
                {{ typeof cite === 'string' ? cite : (cite.docName || '相关文档') }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="inputQuery"
          type="textarea"
          :rows="3"
          placeholder="请输入您的问题，按 Enter 发送..."
          @keyup.enter.ctrl="handleSend"
        />
        <div class="btn-group">
          <el-button type="primary" @click="handleSend" :loading="loading">发送</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { UserFilled, Service } from '@element-plus/icons-vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

// 定义消息类型
interface Message {
  role: 'user' | 'ai'
  content: string
  citations?: any[] // 放宽类型以适应后端返回
}

const inputQuery = ref('')
const loading = ref(false)
const msgRef = ref<HTMLElement | null>(null)
const currentSessionId = ref('') // 【新增】用于维持多轮对话的会话ID

// 初始欢迎语
const msgList = ref<Message[]>([
  { role: 'ai', content: '您好！我是产业知识助手，有什么可以帮您？' }
])

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (msgRef.value) {
      msgRef.value.scrollTop = msgRef.value.scrollHeight
    }
  })
}

// 发送消息
const handleSend = async () => {
  const content = inputQuery.value.trim()
  if (!content) return

  // 1. 添加用户消息到界面
  msgList.value.push({ role: 'user', content })
  inputQuery.value = ''
  scrollToBottom()
  loading.value = true

  try {
    // 2. 调用后端接口
    // 请求头中的 X-User-Id 和 X-Tenant-Id 会由 request.ts 拦截器自动添加
    const res: any = await request.post('/chat/completions', {
      question: content,
      sessionId: currentSessionId.value // 带上当前的会话ID
    })

    if (res.code === 200) {
      const data = res.data
      
      // 【关键】更新本地的 sessionId，以便下一轮对话接上上下文
      if (data.sessionId) {
        currentSessionId.value = data.sessionId
      }

      // 3. 接收 AI 回复并显示
      msgList.value.push({
        role: 'ai',
        content: data.answer || '暂无回复',
        citations: data.citations || [] // 后端返回的引用列表
      })
    } else {
      msgList.value.push({ role: 'ai', content: '系统响应异常: ' + res.msg })
    }
  } catch (error) {
    console.error(error)
    msgList.value.push({ role: 'ai', content: '抱歉，服务暂时不可用，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

// 清空历史
const clearHistory = () => {
  msgList.value = [
    { role: 'ai', content: '您好！我是产业知识助手，有什么可以帮您？' }
  ]
  currentSessionId.value = '' // 【关键】清空会话ID，开启新会话
  ElMessage.success('对话已清空')
}

// 查看文档引用（预留功能）
const viewDoc = (docInfo: any) => {
  console.log('查看文档:', docInfo)
  // 如果是对象且有 ID，可以跳转
  // if (typeof docInfo !== 'string' && docInfo.docId) { ... }
}
</script>

<style scoped>
.chat-container { height: 100%; display: flex; flex-direction: column; }
.chat-card { display: flex; flex-direction: column; height: 100%; }
/* 深度选择器修改 element-plus card body 高度 */
:deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; overflow: hidden; padding: 0; }

.card-header { display: flex; justify-content: space-between; align-items: center; }

.message-area { flex: 1; overflow-y: auto; padding: 20px; background-color: #f9f9f9; }

.message-row { display: flex; margin-bottom: 20px; }
.row-left { justify-content: flex-start; }
.row-right { justify-content: flex-end; flex-direction: row-reverse; }

.message-content { max-width: 70%; margin: 0 12px; display: flex; flex-direction: column; }
.row-right .message-content { align-items: flex-end; }

.bubble { padding: 10px 14px; border-radius: 8px; font-size: 14px; line-height: 1.5; white-space: pre-wrap; }
.row-left .bubble { background-color: #fff; border: 1px solid #e4e7ed; color: #303133; border-top-left-radius: 0; }
.row-right .bubble { background-color: #409EFF; color: #fff; border-top-right-radius: 0; }

.citations { margin-top: 8px; padding: 8px; background-color: #f0f2f5; border-radius: 4px; font-size: 12px; width: 100%; }
.citation-title { color: #909399; margin-bottom: 4px; }
.citation-tag { margin-right: 5px; margin-bottom: 5px; cursor: pointer; display: inline-block; }

.input-area { padding: 20px; border-top: 1px solid #ebeef5; background-color: #fff; }
.btn-group { margin-top: 10px; display: flex; justify-content: flex-end; }
</style>