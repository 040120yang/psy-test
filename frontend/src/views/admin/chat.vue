<template>
  <div class="page-stack">
    <PageHeader
      eyebrow="Communication"
      title="沟通中心"
      description="与学生在线沟通。仅可查看自己负责的学生会话；标记为「待分配」的会话可由你回复后接手。"
    >
      <template #actions>
        <el-button @click="refreshAll">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </template>
    </PageHeader>

    <section class="panel chat-panel">
      <aside class="conv-pane">
        <div class="conv-head">
          <el-input
            v-model="keyword"
            clearable
            placeholder="搜索学生姓名或账号"
          />
          <el-select
            v-model="statusFilter"
            size="small"
            style="width: 104px"
            @change="noop"
          >
            <el-option label="全部" value="" />
            <el-option label="待处理" value="待处理" />
            <el-option label="已处理" value="已处理" />
            <el-option label="需转介" value="需转介" />
          </el-select>
        </div>

        <el-scrollbar class="conv-scroll" always>
          <div v-loading="listLoading" class="conv-list">
            <div
              v-if="!filtered.length && !listLoading"
              class="conv-empty"
            >
              暂无可沟通的学生
            </div>

            <button
              v-for="c in filtered"
              :key="c.studentId"
              class="conv-item"
              :class="{ active: c.studentId === activeId }"
              @click="openConv(c)"
            >
              <div class="conv-avatar">
                {{ (c.studentName || c.studentUsername || '学').slice(0, 1) }}
              </div>
              <div class="conv-main">
                <div class="conv-title">
                  <span class="conv-name">
                    {{ c.studentName || c.studentUsername }}
                  </span>
                  <el-tag
                    v-for="t in tags(c)"
                    :key="t.text"
                    size="small"
                    :type="t.type"
                    effect="plain"
                  >
                    {{ t.text }}
                  </el-tag>
                </div>
                <div class="conv-sub">
                  <span class="conv-msg">{{ lastText(c) }}</span>
                  <el-badge v-if="c.unread" :value="c.unread" />
                </div>
              </div>
            </button>
          </div>
        </el-scrollbar>
      </aside>

      <section class="chat-pane">
        <template v-if="activeId">
          <div class="chat-head">
            <div class="chat-head-main">
              <strong>
                {{ activeStudent.studentName || activeStudent.studentUsername }}
              </strong>
              <small>
                {{ activeStudent.studentUsername }}
                <template v-if="activeStudent.teacherName">
                  · 负责教师：{{ activeStudent.teacherName }}
                </template>
                <template v-else>
                  · 尚未指定负责教师，你回复后将自动接手
                </template>
              </small>
            </div>

            <div class="chat-head-actions">
              <el-select
                v-model="convStatus"
                size="small"
                style="width: 104px"
                @change="onStatusChange"
              >
                <el-option label="待处理" value="待处理" />
                <el-option label="已处理" value="已处理" />
                <el-option label="需转介" value="需转介" />
              </el-select>
              <el-button
                size="small"
                :type="convPinned ? 'primary' : 'default'"
                @click="togglePin"
              >
                {{ convPinned ? '取消置顶' : '置顶' }}
              </el-button>
              <el-button size="small" @click="openProfile">
                个案档案
              </el-button>
            </div>
          </div>

          <div ref="listRef" v-loading="msgLoading" class="chat-list">
            <div
              v-if="!messages.length && !msgLoading"
              class="chat-empty"
            >
              暂无沟通记录，可在下方发起首次沟通
            </div>

            <div
              v-for="m in messages"
              :key="m.id"
              class="chat-row"
              :class="{ mine: m.senderRole === 'teacher' }"
            >
              <div class="chat-avatar">
                {{ m.senderRole === 'teacher' ? '师' : '学' }}
              </div>
              <div class="chat-body">
                <div class="chat-meta">
                  {{
                    m.senderRole === 'teacher'
                      ? (m.senderName || '我')
                      : (m.senderName || '学生')
                  }} ·
                  {{ m.createTime }}
                </div>
                <div class="chat-bubble">{{ m.content }}</div>
              </div>
            </div>
          </div>

          <div class="chat-editor">
            <div class="quick-replies">
              <span class="quick-label">快捷回复</span>
              <el-button
                v-for="(q, i) in QUICK_REPLIES"
                :key="i"
                size="small"
                text
                @click="applyQuick(q)"
              >
                {{ q.length > 16 ? q.slice(0, 16) + '…' : q }}
              </el-button>
            </div>

            <el-input
              v-model="draft"
              type="textarea"
              :rows="3"
              maxlength="2000"
              show-word-limit
              resize="none"
              placeholder="输入回复内容（Ctrl + Enter 发送）"
              @keydown.ctrl.enter="send"
            />

            <div class="chat-actions">
              <el-button
                type="primary"
                :loading="sending"
                :disabled="sending"
                @click="send"
              >
                发送
              </el-button>
            </div>
          </div>
        </template>

        <div v-else class="chat-placeholder">
          请从左侧选择一位学生开始沟通
        </div>
      </section>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import {
  listConversations,
  getChatDetail,
  sendChat,
  markChatRead,
  updateConversation
} from '@/api/chat'

const POLL_MS = 10000
const QUICK_REPLIES = [
  '你好，我是心理老师，已看到你的留言，我们一起来梳理一下。',
  '谢谢你愿意说出来，这本身就很不容易。',
  '我先了解一下：这种情况大概从什么时候开始的？',
  '建议先固定作息、保证睡眠，我们在下次随访时再一起看看变化。',
  '如果情绪持续加重，或出现伤害自己的想法，请立即联系家人，或拨打全国心理援助热线 12356。'
]

const router = useRouter()
const conversations = ref([])
const messages = ref([])
const activeId = ref(null)
const keyword = ref('')
const statusFilter = ref('')
const draft = ref('')
const convStatus = ref('待处理')
const convPinned = ref(0)
const listLoading = ref(false)
const msgLoading = ref(false)
const sending = ref(false)
const listRef = ref(null)
let timer = null

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  let list = conversations.value
  if (statusFilter.value) {
    list = list.filter(
      c => (c.status || '待处理') === statusFilter.value
    )
  }
  if (k) {
    list = list.filter(
      c =>
        (c.studentName || '').toLowerCase().includes(k) ||
        (c.studentUsername || '').toLowerCase().includes(k)
    )
  }
  return list
})

const activeStudent = computed(
  () =>
    conversations.value.find(c => c.studentId === activeId.value) || {}
)

function lastText(c) {
  if (!c.lastMessage) return c.studentUsername || '暂无消息'
  const prefix = c.lastSenderRole === 'teacher' ? '我：' : ''
  const text = prefix + c.lastMessage
  return text.length > 24 ? text.slice(0, 24) + '…' : text
}

function tags(c) {
  const list = []
  if (c.pinned) list.push({ text: '置顶', type: 'danger' })
  if (!c.teacherId) {
    list.push({ text: '待分配', type: 'warning' })
  }
  if (c.status && c.status !== '待处理') {
    list.push({
      text: c.status,
      type: c.status === '需转介' ? 'danger' : 'success'
    })
  }
  return list
}

function syncConv(c) {
  convStatus.value = (c && c.status) || '待处理'
  convPinned.value = (c && c.pinned) || 0
}

function noop() {
  // 仅触发筛选
}

function scrollToBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

async function loadConversations(keepActive) {
  listLoading.value = conversations.value.length === 0
  try {
    const res = await listConversations()
    conversations.value = res.rows || []
    if (
      !keepActive &&
      !conversations.value.some(c => c.studentId === activeId.value)
    ) {
      activeId.value = null
    }
    if (activeId.value) syncConv(activeStudent.value)
  } catch (e) {
    // 已统一提示
  } finally {
    listLoading.value = false
  }
}

async function loadMessages(scroll) {
  if (!activeId.value) {
    messages.value = []
    return
  }
  msgLoading.value = messages.value.length === 0
  try {
    const res = await getChatDetail(activeId.value)
    messages.value = (res.data && res.data.messages) || []

    const hasUnread = messages.value.some(
      m => m.senderRole === 'student' && m.isRead === 0
    )
    if (hasUnread) {
      await markChatRead({ studentId: activeId.value })
      messages.value.forEach(m => {
        if (m.senderRole === 'student') m.isRead = 1
      })
      const item = conversations.value.find(
        c => c.studentId === activeId.value
      )
      if (item) item.unread = 0
    }

    if (scroll) scrollToBottom()
  } catch (e) {
    // 已统一提示
  } finally {
    msgLoading.value = false
  }
}

async function openConv(c) {
  if (activeId.value === c.studentId) return
  activeId.value = c.studentId
  syncConv(c)
  messages.value = []
  await loadMessages(true)
}

function applyQuick(text) {
  draft.value = text
}

async function onStatusChange(value) {
  try {
    await updateConversation({
      studentId: activeId.value,
      status: value
    })
    ElMessage.success('会话状态已更新为「' + value + '」')
    await loadConversations(true)
  } catch (e) {
    syncConv(activeStudent.value)
  }
}

async function togglePin() {
  const next = convPinned.value ? 0 : 1
  try {
    await updateConversation({
      studentId: activeId.value,
      pinned: next
    })
    ElMessage.success(next ? '已置顶该会话' : '已取消置顶')
    await loadConversations(true)
  } catch (e) {
    // 已统一提示
  }
}

function openProfile() {
  router.push({
    path: '/admin/student-profile',
    query: { studentId: activeId.value }
  })
}

async function send() {
  const content = draft.value.trim()
  if (!activeId.value) return
  if (!content) {
    ElMessage.warning('消息内容不能为空')
    return
  }
  sending.value = true
  try {
    await sendChat({
      studentId: activeId.value,
      content
    })
    draft.value = ''
    await loadMessages(true)
    await loadConversations(true)
    ElMessage.success('已发送')
  } catch (e) {
    // 已统一提示
  } finally {
    sending.value = false
  }
}

async function refreshAll() {
  await loadConversations(true)
  await loadMessages(true)
}

onMounted(() => {
  refreshAll()
  timer = setInterval(() => {
    loadConversations(true)
    loadMessages(false)
  }, POLL_MS)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.chat-panel {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 0;
  overflow: hidden;
  height: calc(100vh - 274px);
  min-height: 420px;
}

.conv-pane {
  display: flex;
  flex-direction: column;
  min-height: 0;
  border-right: 1px solid var(--line);
}

.conv-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 14px 10px;
  border-bottom: 1px solid var(--line);
}

.conv-scroll {
  flex: 1;
  min-height: 0;
}

.conv-list {
  padding: 8px;
}

.chat-list::-webkit-scrollbar {
  width: 6px;
}

.chat-list::-webkit-scrollbar-track {
  background: transparent;
}

.chat-list::-webkit-scrollbar-thumb {
  background: #d3dce4;
  border-radius: 3px;
}

.chat-list::-webkit-scrollbar-thumb:hover {
  background: #bcc9d6;
}

.conv-empty {
  padding: 30px 10px;
  color: var(--text-3);
  font-size: 13px;
  text-align: center;
}

.conv-item {
  width: 100%;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 11px 10px;
  border: 0;
  border-radius: 12px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: 0.16s;
}

.conv-item:hover {
  background: #f5f9f8;
}

.conv-item.active {
  background: var(--primary-soft);
}

.conv-avatar {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: #e8eef4;
  color: var(--text-2);
  font-size: 13px;
  font-weight: 700;
}

.conv-item.active .conv-avatar {
  background: #fff;
  color: var(--primary);
}

.conv-main {
  flex: 1;
  min-width: 0;
}

.conv-title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.conv-name {
  max-width: 96px;
  overflow: hidden;
  color: var(--text);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.conv-sub {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 4px;
}

.conv-msg {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  color: var(--text-3);
  font-size: 12px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.chat-pane {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 18px 20px;
}

.chat-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.chat-head-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-head-main strong {
  font-size: 15px;
}

.chat-head-main small {
  color: var(--text-3);
  font-size: 12px;
}

.chat-head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-placeholder {
  flex: 1;
  display: grid;
  place-items: center;
  color: var(--text-3);
  font-size: 13px;
}

.chat-list {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 2px;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.chat-empty {
  margin: auto;
  color: var(--text-3);
  font-size: 13px;
}

.chat-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.chat-row.mine {
  flex-direction: row-reverse;
}

.chat-avatar {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--primary);
  font-size: 13px;
  font-weight: 700;
}

.chat-row.mine .chat-avatar {
  background: #e8eef4;
  color: var(--text-2);
}

.chat-body {
  max-width: min(560px, 74%);
}

.chat-row.mine .chat-body {
  text-align: right;
}

.chat-meta {
  margin-bottom: 5px;
  color: var(--text-3);
  font-size: 12px;
}

.chat-bubble {
  display: inline-block;
  padding: 11px 14px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--text);
  font-size: 13px;
  line-height: 1.85;
  white-space: pre-wrap;
  word-break: break-word;
  text-align: left;
}

.chat-row.mine .chat-bubble {
  border-color: transparent;
  background: var(--primary-soft);
}

.chat-editor {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

.quick-replies {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.quick-label {
  margin-right: 4px;
  color: var(--text-3);
  font-size: 12px;
}

.chat-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

@media (max-width: 1100px) {
  .chat-panel {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 0;
  }

  .conv-pane {
    max-height: 340px;
    border-right: 0;
    border-bottom: 1px solid var(--line);
  }

  .chat-pane {
    min-height: 460px;
  }
}
</style>
