<template>
  <div class="coze-page">
    <!-- 头部 -->
    <div class="coze-header">
      <div class="coze-title">
        <div class="coze-avatar"><i class="el-icon-chat-dot-round"></i></div>
        <div>
          <div class="coze-name">AI 心理助手</div>
          <div class="coze-desc">基于扣子智能体的心理健康问答助手，可随时咨询情绪、压力、睡眠等问题</div>
        </div>
      </div>
    </div>

    <!-- 消息区 -->
    <div class="chat-body" ref="chatBody">
      <div class="chat-empty" v-if="!messages.length">
        <i class="el-icon-chat-dot-round"></i>
        <p>您好，我是 AI 心理助手，欢迎随时向我倾诉～<br>您可以问我：怎么缓解考前焦虑？最近失眠怎么办？</p>
      </div>
      <div v-for="(m, idx) in messages" :key="idx" class="msg-row" :class="m.role">
        <div class="msg-avatar" v-if="m.role === 'assistant'"><i class="el-icon-chat-dot-round"></i></div>
        <div class="msg-bubble">{{ m.content }}</div>
        <div class="msg-avatar user-avatar" v-if="m.role === 'user'"><i class="el-icon-user"></i></div>
      </div>
      <div v-if="loading" class="msg-row assistant">
        <div class="msg-avatar"><i class="el-icon-chat-dot-round"></i></div>
        <div class="msg-bubble typing"><span></span><span></span><span></span></div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="chat-input">
      <el-input
        v-model="input"
        type="textarea"
        :rows="2"
        placeholder="请输入您想咨询的问题，Enter 发送，Shift+Enter 换行"
        resize="none"
        @keydown.enter.native.prevent="handleSend"
      />
      <el-button type="primary" :loading="loading" @click="handleSend">发送</el-button>
    </div>
  </div>
</template>

<script>
import { chatWithCoze } from '@/api/coze'
import { getUser } from '@/utils/auth'

export default {
  name: 'PortalCoze',
  data() {
    return {
      input: '',
      loading: false,
      messages: [],
      conversationId: ''
    }
  },
  mounted() {
    // 恢复当前用户的对话记录（按用户名隔离，跨会话保留）
    try {
      const saved = localStorage.getItem(this.msgKey())
      if (saved) {
        const arr = JSON.parse(saved)
        if (Array.isArray(arr)) this.messages = arr
      }
    } catch (e) {
      // 存储数据异常时忽略，从空会话开始
    }
    this.conversationId = localStorage.getItem(this.chatKey()) || ''
    this.scrollBottom()
  },
  methods: {
    // 存储 key 按当前登录用户隔离
    msgKey() {
      const u = getUser()
      return 'PsyTest-Coze-Messages-' + (u && u.username ? u.username : 'guest')
    },
    chatKey() {
      const u = getUser()
      return 'PsyTest-Coze-Chat-' + (u && u.username ? u.username : 'guest')
    },
    // 持久化消息与上下文（最多保留最近 50 条，避免超出浏览器存储上限）
    persist() {
      const arr = this.messages.slice(-50)
      localStorage.setItem(this.msgKey(), JSON.stringify(arr))
      if (this.conversationId) {
        localStorage.setItem(this.chatKey(), this.conversationId)
      }
    },
    handleSend() {
      const text = (this.input || '').trim()
      if (!text || this.loading) return
      this.messages.push({ role: 'user', content: text })
      this.input = ''
      this.scrollBottom()
      this.persist()
      this.loading = true
      chatWithCoze({ message: text, conversationId: this.conversationId || undefined })
        .then(res => {
          this.messages.push({ role: 'assistant', content: res.reply })
          this.conversationId = res.conversationId || ''
          this.persist()
          this.scrollBottom()
        })
        .catch(err => {
          this.$message.error((err && err.message) || 'AI 服务暂时不可用，请稍后重试')
          this.messages.push({ role: 'assistant', content: '抱歉，我刚才走神了，请稍后再试一次。' })
        })
        .finally(() => {
          this.loading = false
          this.scrollBottom()
        })
    },
    scrollBottom() {
      this.$nextTick(() => {
        const el = this.$refs.chatBody
        if (el) el.scrollTop = el.scrollHeight
      })
    }
  }
}
</script>

<style scoped>
.coze-page {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  min-height: 480px;
}
.coze-header {
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
}
.coze-title {
  display: flex;
  align-items: center;
}
.coze-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(120deg, #667eea, #764ba2);
  color: #fff;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
}
.coze-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.coze-desc {
  margin-top: 2px;
  color: #909399;
  font-size: 12px;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f7f8fa;
}
.chat-empty {
  text-align: center;
  color: #c0c4cc;
  padding-top: 80px;
  font-size: 13px;
  line-height: 2;
}
.chat-empty i {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}
.msg-row {
  display: flex;
  margin-bottom: 16px;
}
.msg-row.user {
  justify-content: flex-end;
}
.msg-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(120deg, #667eea, #764ba2);
  color: #fff;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.user-avatar {
  background: #409eff;
}
.msg-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
}
.msg-row.assistant .msg-bubble {
  background: #fff;
  color: #303133;
  margin-left: 10px;
  border: 1px solid #ebeef5;
}
.msg-row.user .msg-bubble {
  background: #409eff;
  color: #fff;
  margin-right: 10px;
}
.msg-row.assistant .msg-avatar {
  margin-right: 0;
}
.typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 4px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: blink 1.2s infinite;
}
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink {
  0%, 80%, 100% { opacity: 0.2; }
  40% { opacity: 1; }
}
.chat-input {
  padding: 14px 24px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: flex-end;
}
.chat-input .el-textarea {
  flex: 1;
}
.chat-input .el-button {
  margin-left: 12px;
  height: 54px;
}
</style>
