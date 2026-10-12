<template>
  <div class="page-stack">
    <PageHeader eyebrow="Communication" title="我的沟通" description="与心理教师在线沟通，消息会保留在会话中，便于持续跟进。">
      <template #actions><el-button @click="load(true)"><el-icon><Refresh /></el-icon> 刷新</el-button></template>
    </PageHeader>
    <section class="panel">
      <div class="panel-head">
        <div>
          <div class="panel-title">与心理教师沟通</div>
          <div class="panel-subtitle">
            <template v-if="teacherName">负责教师：{{ teacherName }}</template>
            <template v-else>暂未指定负责教师，你现在发送的消息会由心理教师团队接收</template>
          </div>
        </div>
        <el-tag :type="unread ? 'danger' : 'info'" effect="plain">{{ unread ? unread + ' 条未读' : '均已是读' }}</el-tag>
      </div>
      <div class="panel-body">
        <div ref="listRef" v-loading="loading" class="chat-list">
          <div v-if="!messages.length && !loading" class="chat-empty">
            <p>还没有沟通记录</p>
            <span>可以在下方输入你的困扰、疑问或想反馈的情况，心理教师会看到并回复。</span>
          </div>
          <div v-for="m in messages" :key="m.id" class="chat-row" :class="{ mine: isMine(m) }">
            <div class="chat-avatar">{{ isMine(m) ? '我' : '师' }}</div>
            <div class="chat-body">
              <div class="chat-meta">{{ isMine(m) ? '我' : (m.senderName || '心理教师') }} · {{ m.createTime }}</div>
              <div class="chat-bubble">{{ m.content }}</div>
            </div>
          </div>
        </div>
        <div class="chat-editor">
          <el-input v-model="draft" type="textarea" :rows="3" maxlength="2000" show-word-limit resize="none" placeholder="输入想对心理教师说的话（Ctrl + Enter 发送）" @keydown.ctrl.enter="send" />
          <div class="chat-actions">
            <el-button type="primary" :loading="sending" :disabled="!draft.trim()" @click="send">发送</el-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getMyChat, sendChat, markChatRead } from '@/api/chat'

const POLL_MS = 10000
const messages = ref([])
const teacherName = ref('')
const draft = ref('')
const loading = ref(false)
const sending = ref(false)
const listRef = ref(null)
let timer = null
let firstLoad = true

const unread = computed(() => messages.value.filter(m => m.senderRole === 'teacher' && m.isRead === 0).length)
const isMine = m => m.senderRole === 'student'

function scrollToBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

async function markReadLocal() {
  if (!messages.value.some(m => m.senderRole === 'teacher' && m.isRead === 0)) return
  try {
    await markChatRead()
    messages.value.forEach(m => { if (m.senderRole === 'teacher') m.isRead = 1 })
    window.dispatchEvent(new CustomEvent('portal-chat-read'))
  } catch (e) { /* 标记失败不影响阅读 */ }
}

async function load(manual) {
  if (firstLoad) loading.value = true
  try {
    const res = await getMyChat({ limit: 200 })
    const data = res.data || {}
    messages.value = data.messages || []
    teacherName.value = data.teacher && data.teacher.teacherName ? data.teacher.teacherName : ''
    await markReadLocal()
    if (manual || firstLoad) scrollToBottom()
  } catch (e) { /* 提示已由请求拦截器统一处理 */ } finally {
    loading.value = false
    firstLoad = false
  }
}

async function send() {
  const content = draft.value.trim()
  if (!content) return
  sending.value = true
  try {
    await sendChat({ content })
    draft.value = ''
    await load(true)
    ElMessage.success('已发送')
  } catch (e) { /* 提示已由请求拦截器统一处理 */ } finally {
    sending.value = false
  }
}

onMounted(() => {
  load(true)
  timer = setInterval(() => load(false), POLL_MS)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
<style scoped>
.chat-list{height:52vh;min-height:300px;overflow-y:auto;padding:4px 2px;display:flex;flex-direction:column;gap:16px}
.chat-empty{display:grid;place-items:center;gap:6px;height:100%;color:var(--text-3);text-align:center}
.chat-empty p{margin:0;font-size:14px;font-weight:600;color:var(--text-2)}
.chat-empty span{font-size:12px;max-width:420px;line-height:1.8}
.chat-row{display:flex;gap:10px;align-items:flex-start}
.chat-row.mine{flex-direction:row-reverse}
.chat-avatar{flex-shrink:0;width:36px;height:36px;display:grid;place-items:center;border-radius:50%;background:var(--primary-soft);color:var(--primary);font-size:13px;font-weight:700}
.chat-row.mine .chat-avatar{background:#e8eef4;color:var(--text-2)}
.chat-body{max-width:min(560px,76%)}
.chat-row.mine .chat-body{text-align:right}
.chat-meta{font-size:12px;color:var(--text-3);margin-bottom:5px}
.chat-bubble{display:inline-block;text-align:left;padding:11px 14px;border-radius:14px;background:#fff;border:1px solid var(--line);color:var(--text);font-size:13px;line-height:1.85;white-space:pre-wrap;word-break:break-word}
.chat-row.mine .chat-bubble{background:var(--primary-soft);border-color:transparent}
.chat-editor{margin-top:18px;border-top:1px solid var(--line);padding-top:16px}
.chat-actions{display:flex;justify-content:flex-end;margin-top:10px}
</style>
