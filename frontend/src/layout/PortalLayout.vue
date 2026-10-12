<template>
  <div class="portal-shell">
    <header class="portal-header">
      <div class="page-shell header-inner">
        <AppLogo />
        <nav class="portal-nav">
          <router-link v-for="item in menus" :key="item.path" :to="item.path" class="nav-link">{{ item.title }}<span v-if="item.path==='/portal/chat' && chatUnread" class="nav-count">{{ chatUnread }}</span></router-link>
        </nav>
        <div class="header-actions">
          <el-badge :value="unread" :hidden="!unread" class="msg-badge">
            <el-button circle text @click="$router.push('/portal/message')"><el-icon><Bell /></el-icon></el-button>
          </el-badge>
          <el-dropdown trigger="click" @command="handleCommand">
            <button class="user-chip"><el-avatar :size="30">{{ initial }}</el-avatar><span>{{ user.nickname || user.username }}</span><el-icon><ArrowDown /></el-icon></button>
            <template #dropdown><el-dropdown-menu><el-dropdown-item command="center">个人中心</el-dropdown-item><el-dropdown-item divided command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </div>
    </header>
    <main class="portal-main page-shell"><router-view /></main>
    <footer class="portal-footer">本系统测评结果仅用于健康筛查参考，不能替代专业心理评估</footer>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import AppLogo from '@/components/AppLogo.vue'
import { getUser, removeToken, removeUser } from '@/utils/auth'
import { logout } from '@/api/login'
import { getUnreadCount, listMessages } from '@/api/business'
import { getChatUnread, getMyChat } from '@/api/chat'

const router=useRouter(); const route=useRoute()
const user=ref(getUser() || {})
const unread=ref(0)
const chatUnread=ref(0)
const initial=computed(()=>(user.value.nickname || user.value.username || '用户').slice(0,1))
const menus=[{path:'/portal/home',title:'工作台'},{path:'/portal/scales',title:'心理测评'},{path:'/portal/records',title:'我的记录'},{path:'/portal/follow',title:'我的随访'},{path:'/portal/chat',title:'我的沟通'},{path:'/portal/knowledge',title:'心理知识'},{path:'/portal/coze',title:'AI 助手'}]
const handleCommand=async command=>{if(command==='center')return router.push('/portal/personal-center');if(command==='logout'){try{await logout()}finally{removeToken();removeUser();ElMessage.success('已退出登录');router.replace('/login')}}}

// ===== 消息通知提醒 =====
// 轮询未读数量，发现新消息时弹出桌面通知，并同步更新顶栏铃铛角标
const POLL_MS=20000
const notifiedIds=new Set()   // 已弹过通知的消息ID，避免重复提醒
let knownMaxId=0              // 首屏已存在的消息不提醒
let ready=false
let timer=null
// 沟通（学生-教师会话）未读提醒
const notifiedChatIds=new Set()
let knownChatId=0

function popNotify(item){
  const content=item.content||''
  ElNotification({
    title: item.title || '您有新的消息',
    message: content.length>80 ? content.slice(0,80)+'…' : content,
    type: item.type==='result' ? 'success' : (item.type==='follow' ? 'warning' : 'info'),
    duration: 8000,
    position: 'bottom-right',
    onClick: ()=>{ router.push('/portal/message') }
  })
}

async function refreshUnread(withNotify){
  let count
  try{ const res=await getUnreadCount(); count=Number(res.data||0) }catch{ return }
  const grew = count > unread.value
  unread.value = count
  if(!withNotify || !ready || !grew) return
  try{
    const res=await listMessages()
    const list=(res.rows||[]).filter(i=>i.isRead===0).sort((a,b)=>(a.id||0)-(b.id||0))
    list.forEach(m=>{
      if((m.id||0)<=knownMaxId || notifiedIds.has(m.id)) return
      notifiedIds.add(m.id)
      popNotify(m)
    })
    const maxId=list.length ? list[list.length-1].id : knownMaxId
    if(maxId>knownMaxId) knownMaxId=maxId
  }catch{}
}

async function initUnread(){
  try{ const res=await getUnreadCount(); unread.value=Number(res.data||0) }catch{}
  try{
    const res=await listMessages()
    knownMaxId=(res.rows||[]).filter(i=>i.isRead===0).reduce((m,i)=>Math.max(m,i.id||0),0)
  }catch{}
  ready=true
}

// ===== 沟通消息提醒（学生端）=====
async function chatMessages(){
  const res=await getMyChat({ limit: 50 })
  return ((res.data && res.data.messages) || []).filter(m=>m.senderRole==='teacher')
}

async function refreshChatUnread(withNotify){
  let count
  try{ const res=await getChatUnread(); count=Number(res.data||0) }catch{ return }
  const grew = count > chatUnread.value
  chatUnread.value = count
  if(!withNotify || !ready || !grew) return
  try{
    const list=await chatMessages()
    list.forEach(m=>{
      if((m.id||0)<=knownChatId || notifiedChatIds.has(m.id)) return
      notifiedChatIds.add(m.id)
      ElNotification({
        title: '心理教师回复了你的消息',
        message: (m.content||'').length>80 ? m.content.slice(0,80)+'…' : (m.content||''),
        type: 'success', duration: 8000, position: 'bottom-right',
        onClick: ()=>{ router.push('/portal/chat') }
      })
    })
    const maxId=list.length ? list[list.length-1].id : knownChatId
    if(maxId>knownChatId) knownChatId=maxId
  }catch{}
}

async function initChatUnread(){
  try{ const res=await getChatUnread(); chatUnread.value=Number(res.data||0) }catch{}
  try{ const list=await chatMessages(); knownChatId=list.reduce((m,i)=>Math.max(m,i.id||0),0) }catch{}
}

function onFocusOrVisible(){ refreshUnread(true); refreshChatUnread(true) }
function onVisibility(){ if(document.visibilityState==='visible'){ refreshUnread(true); refreshChatUnread(true) } }
function onMsgRead(){ refreshUnread(false) }
function onChatRead(){ refreshChatUnread(false) }

onMounted(()=>{
  initUnread()
  initChatUnread()
  timer=setInterval(()=>{ refreshUnread(true); refreshChatUnread(true) }, POLL_MS)
  window.addEventListener('focus', onFocusOrVisible)
  document.addEventListener('visibilitychange', onVisibility)
  window.addEventListener('portal-msg-read', onMsgRead)
  window.addEventListener('portal-chat-read', onChatRead)
})
// 切换页面（如从消息中心返回）时刷新角标，保证已读后角标及时消失
watch(()=>route.path, ()=>{ refreshUnread(false); refreshChatUnread(false) })
onUnmounted(()=>{
  if(timer) clearInterval(timer)
  window.removeEventListener('focus', onFocusOrVisible)
  document.removeEventListener('visibilitychange', onVisibility)
  window.removeEventListener('portal-msg-read', onMsgRead)
  window.removeEventListener('portal-chat-read', onChatRead)
})
</script>
<style scoped>
.portal-shell{min-height:100vh;background:linear-gradient(180deg,#f6faf9 0,#f4f7fa 260px)}.portal-header{position:sticky;top:0;z-index:20;height:68px;background:rgba(255,255,255,.92);border-bottom:1px solid rgba(231,236,242,.9);backdrop-filter:blur(14px)}.header-inner{height:100%;display:flex;align-items:center;gap:28px}.portal-nav{display:flex;align-items:center;gap:6px;flex:1}.nav-link{padding:10px 14px;border-radius:10px;color:var(--text-2);font-size:14px;transition:.18s}.nav-link:hover,.nav-link.router-link-active{color:var(--primary);background:var(--primary-soft);font-weight:700}.header-actions{display:flex;align-items:center;gap:12px}.user-chip{display:flex;align-items:center;gap:8px;padding:5px 9px;border:1px solid var(--line);border-radius:999px;background:#fff;color:var(--text);cursor:pointer}.msg-badge{margin-right:2px}.nav-count{display:inline-block;min-width:16px;height:16px;line-height:16px;margin-left:5px;padding:0 4px;border-radius:8px;background:#f56c6c;color:#fff;font-size:11px;font-weight:700;text-align:center;vertical-align:1px}.portal-main{padding:28px 24px 48px}.portal-footer{padding:24px;color:var(--text-3);font-size:12px;text-align:center}@media(max-width:1050px){.portal-nav{gap:0}.nav-link{padding:9px 8px;font-size:13px}.copy small{display:none}}@media(max-width:760px){.portal-nav{display:none}.header-inner{gap:12px}.user-chip span{display:none}.portal-main{padding:20px 14px 36px}}
</style>
