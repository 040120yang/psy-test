<template>
  <div class="page-stack">
    <PageHeader eyebrow="Communication" title="沟通中心" description="与学生在线沟通。仅可查看自己负责的学生会话；标记为「待分配」的会话可由你回复后接手。">
      <template #actions><el-button @click="refreshAll"><el-icon><Refresh /></el-icon> 刷新</el-button></template>
    </PageHeader>
    <section class="panel chat-panel">
      <aside class="conv-pane">
        <div class="conv-head"><el-input v-model="keyword" clearable placeholder="搜索学生姓名或账号" /></div>
        <el-scrollbar class="conv-scroll" always>
          <div v-loading="listLoading" class="conv-list">
            <div v-if="!filtered.length && !listLoading" class="conv-empty">暂无可沟通的学生</div>
            <button v-for="c in filtered" :key="c.studentId" class="conv-item" :class="{ active: c.studentId === activeId }" @click="openConv(c)">
              <div class="conv-avatar">{{ (c.studentName || c.studentUsername || '学').slice(0,1) }}</div>
              <div class="conv-main">
                <div class="conv-title"><span class="conv-name">{{ c.studentName || c.studentUsername }}</span><el-tag v-if="!c.teacherId" size="small" type="warning" effect="plain">待分配</el-tag></div>
                <div class="conv-sub"><span class="conv-msg">{{ lastText(c) }}</span><el-badge v-if="c.unread" :value="c.unread" /></div>
              </div>
            </button>
          </div>
        </el-scrollbar>
      </aside>
      <section class="chat-pane">
        <template v-if="activeId">
          <div class="chat-head">
            <div class="chat-head-main"><strong>{{ activeStudent.studentName || activeStudent.studentUsername }}</strong><small>{{ activeStudent.studentUsername }}<template v-if="activeStudent.teacherName"> · 负责教师：{{ activeStudent.teacherName }}</template><template v-else> · 尚未指定负责教师，你回复后将自动接手</template></small></div>
          </div>
          <div ref="listRef" v-loading="msgLoading" class="chat-list">
            <div v-if="!messages.length && !msgLoading" class="chat-empty">暂无沟通记录，可在下方发起首次沟通</div>
            <div v-for="m in messages" :key="m.id" class="chat-row" :class="{ mine: m.senderRole === 'teacher' }">
              <div class="chat-avatar">{{ m.senderRole === 'teacher' ? '师' : '学' }}</div>
              <div class="chat-body"><div class="chat-meta">{{ m.senderRole === 'teacher' ? (m.senderName || '我') : (m.senderName || '学生') }} · {{ m.createTime }}</div><div class="chat-bubble">{{ m.content }}</div></div>
            </div>
          </div>
          <div class="chat-editor">
            <el-input v-model="draft" type="textarea" :rows="3" maxlength="2000" show-word-limit resize="none" placeholder="输入回复内容（Ctrl + Enter 发送）" @keydown.ctrl.enter="send" />
            <div class="chat-actions"><el-button type="primary" :loading="sending" :disabled="!draft.trim()" @click="send">发送</el-button></div>
          </div>
        </template>
        <div v-else class="chat-placeholder">请从左侧选择一位学生开始沟通</div>
      </section>
    </section>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { listConversations, getChatDetail, sendChat, markChatRead } from '@/api/chat'

const POLL_MS = 10000
const conversations = ref([])
const messages = ref([])
const activeId = ref(null)
const keyword = ref('')
const draft = ref('')
const listLoading = ref(false)
const msgLoading = ref(false)
const sending = ref(false)
const listRef = ref(null)
let timer = null

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return conversations.value
  return conversations.value.filter(c => (c.studentName || '').toLowerCase().includes(k) || (c.studentUsername || '').toLowerCase().includes(k))
})
const activeStudent = computed(() => conversations.value.find(c => c.studentId === activeId.value) || {})

function lastText(c) {
  if (!c.lastMessage) return c.studentUsername || '暂无消息'
  const prefix = c.lastSenderRole === 'teacher' ? '我：' : ''
  const text = prefix + c.lastMessage
  return text.length > 24 ? text.slice(0, 24) + '…' : text
}

function scrollToBottom() {
  nextTick(() => { const el = listRef.value; if (el) el.scrollTop = el.scrollHeight })
}

async function loadConversations(keepActive) {
  listLoading.value = conversations.value.length === 0
  try {
    const res = await listConversations()
    conversations.value = res.rows || []
    if (!keepActive && !conversations.value.some(c => c.studentId === activeId.value)) activeId.value = null
  } catch (e) { /* 已统一提示 */ } finally { listLoading.value = false }
}

async function loadMessages(scroll) {
  if (!activeId.value) { messages.value = []; return }
  msgLoading.value = messages.value.length === 0
  try {
    const res = await getChatDetail(activeId.value)
    messages.value = (res.data && res.data.messages) || []
    const hasUnread = messages.value.some(m => m.senderRole === 'student' && m.isRead === 0)
    if (hasUnread) {
      await markChatRead({ studentId: activeId.value })
      messages.value.forEach(m => { if (m.senderRole === 'student') m.isRead = 1 })
      const item = conversations.value.find(c => c.studentId === activeId.value)
      if (item) item.unread = 0
    }
    if (scroll) scrollToBottom()
  } catch (e) { /* 已统一提示 */ } finally { msgLoading.value = false }
}

async function openConv(c) {
  if (activeId.value === c.studentId) return
  activeId.value = c.studentId
  messages.value = []
  await loadMessages(true)
}

async function send() {
  const content = draft.value.trim()
  if (!content || !activeId.value) return
  sending.value = true
  try {
    await sendChat({ studentId: activeId.value, content })
    draft.value = ''
    await loadMessages(true)
    await loadConversations(true)
    ElMessage.success('已发送')
  } catch (e) { /* 已统一提示 */ } finally { sending.value = false }
}

async function refreshAll() {
  await loadConversations(true)
  await loadMessages(true)
}

onMounted(() => {
  refreshAll()
  timer = setInterval(() => { loadConversations(true); loadMessages(false) }, POLL_MS)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
<style scoped>
.chat-panel{display:grid;grid-template-columns:320px 1fr;gap:0;overflow:hidden;height:calc(100vh - 274px);min-height:420px}
.conv-pane{border-right:1px solid var(--line);display:flex;flex-direction:column;min-height:0}
.conv-head{padding:16px 16px 12px;border-bottom:1px solid var(--line)}
.conv-scroll{flex:1;min-height:0}
.conv-list{padding:8px}
.chat-list::-webkit-scrollbar{width:6px}
.chat-list::-webkit-scrollbar-track{background:transparent}
.chat-list::-webkit-scrollbar-thumb{background:#d3dce4;border-radius:3px}
.chat-list::-webkit-scrollbar-thumb:hover{background:#bcc9d6}
.conv-empty{padding:30px 10px;text-align:center;color:var(--text-3);font-size:13px}
.conv-item{width:100%;display:flex;gap:10px;align-items:flex-start;padding:11px 10px;border:0;border-radius:12px;background:transparent;text-align:left;cursor:pointer;transition:.16s}
.conv-item:hover{background:#f5f9f8}
.conv-item.active{background:var(--primary-soft)}
.conv-avatar{flex-shrink:0;width:34px;height:34px;display:grid;place-items:center;border-radius:10px;background:#e8eef4;color:var(--text-2);font-size:13px;font-weight:700}
.conv-item.active .conv-avatar{background:#fff;color:var(--primary)}
.conv-main{flex:1;min-width:0}
.conv-title{display:flex;align-items:center;gap:6px}
.conv-name{font-size:13px;font-weight:700;color:var(--text)}
.conv-sub{display:flex;align-items:center;justify-content:space-between;gap:8px;margin-top:4px}
.conv-msg{flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:var(--text-3);font-size:12px}
.chat-pane{display:flex;flex-direction:column;padding:18px 20px;min-height:0}
.chat-head{padding-bottom:14px;border-bottom:1px solid var(--line)}
.chat-head-main{display:flex;flex-direction:column;gap:4px}
.chat-head-main strong{font-size:15px}
.chat-head-main small{color:var(--text-3);font-size:12px}
.chat-placeholder{flex:1;display:grid;place-items:center;color:var(--text-3);font-size:13px}
.chat-list{flex:1;min-height:0;overflow-y:auto;overscroll-behavior:contain;padding:16px 2px;display:flex;flex-direction:column;gap:16px}
.chat-empty{margin:auto;color:var(--text-3);font-size:13px}
.chat-row{display:flex;gap:10px;align-items:flex-start}
.chat-row.mine{flex-direction:row-reverse}
.chat-avatar{flex-shrink:0;width:34px;height:34px;display:grid;place-items:center;border-radius:50%;background:var(--primary-soft);color:var(--primary);font-size:13px;font-weight:700}
.chat-row.mine .chat-avatar{background:#e8eef4;color:var(--text-2)}
.chat-body{max-width:min(560px,74%)}
.chat-row.mine .chat-body{text-align:right}
.chat-meta{font-size:12px;color:var(--text-3);margin-bottom:5px}
.chat-bubble{display:inline-block;text-align:left;padding:11px 14px;border-radius:14px;background:#fff;border:1px solid var(--line);color:var(--text);font-size:13px;line-height:1.85;white-space:pre-wrap;word-break:break-word}
.chat-row.mine .chat-bubble{background:var(--primary-soft);border-color:transparent}
.chat-editor{border-top:1px solid var(--line);padding-top:14px;margin-top:12px}
.chat-actions{display:flex;justify-content:flex-end;margin-top:10px}
@media(max-width:1100px){.chat-panel{grid-template-columns:1fr;height:auto;min-height:0}.conv-pane{border-right:0;border-bottom:1px solid var(--line);max-height:340px}.chat-pane{min-height:460px}}
</style>
