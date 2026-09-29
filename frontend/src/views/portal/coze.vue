<template>
  <div class="page-stack">
    <PageHeader eyebrow="AI Companion" title="AI 心理助手" description="用于心理陪伴与健康科普，不提供医学诊断。如有危机情况，请优先联系专业人员。" />
    <section class="chat-shell panel">
      <div class="chat-head"><div class="ai-avatar"><el-icon><MagicStick /></el-icon></div><div><strong>心理陪伴助手</strong><p>可咨询情绪、压力、睡眠和人际困扰</p></div><div class="online"><span :class="{ local: mode==='local' }"></span>{{ mode==='local' ? '本地陪伴' : mode==='cloud' ? '云端在线' : '智能在线' }}</div></div>
      <div ref="chatBody" class="chat-body">
        <div v-if="!messages.length" class="chat-empty"><el-icon :size="52"><ChatDotRound /></el-icon><h3>你好，我在这里</h3><p>可以试着问我：“最近总是失眠怎么办？”<br>“考试前焦虑如何缓解？”</p></div>
        <div v-for="(item,index) in messages" :key="index" class="message-row" :class="item.role"><div v-if="item.role==='assistant'" class="bubble-avatar"><el-icon><MagicStick /></el-icon></div><div class="bubble" :class="item.role">{{ item.content }}</div></div>
        <div v-if="loading" class="message-row assistant"><div class="bubble-avatar"><el-icon><MagicStick /></el-icon></div><div class="bubble assistant typing"><span></span><span></span><span></span></div></div>
      </div>
      <div class="chat-input"><el-input v-model="input" type="textarea" :rows="2" resize="none" placeholder="请输入问题，Enter 发送，Shift+Enter 换行" @keydown.enter.exact.prevent="send" /><el-button type="primary" size="large" :loading="loading" @click="send">发送</el-button></div>
    </section>
  </div>
</template>
<script setup>
import { ref, onMounted, nextTick } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { ElMessage } from 'element-plus'
import { chatWithCoze } from '@/api/coze'
import { getUser } from '@/utils/auth'
const user=getUser()||{}; const input=ref(''); const loading=ref(false); const messages=ref([]); const conversationId=ref(''); const mode=ref('auto'); const chatBody=ref()
const msgKey=`psy-v3-ai-messages-${user.username||'guest'}`; const chatKey=`psy-v3-ai-chat-${user.username||'guest'}`
function persist(){localStorage.setItem(msgKey,JSON.stringify(messages.value.slice(-50)));localStorage.setItem(chatKey,conversationId.value)}
async function scrollBottom(){await nextTick();if(chatBody.value)chatBody.value.scrollTop=chatBody.value.scrollHeight}
async function send(){const text=input.value.trim();if(!text||loading.value)return;messages.value.push({role:'user',content:text,time:Date.now()});input.value='';persist();scrollBottom();loading.value=true;try{const res=await chatWithCoze({message:text,conversationId:conversationId.value||undefined});messages.value.push({role:'assistant',content:res.reply||'暂时没有生成回答。',time:Date.now()});conversationId.value=res.conversationId||'';mode.value=res.mode||'cloud';persist()}catch(e){ElMessage.error(e.message||'AI 服务暂不可用');messages.value.push({role:'assistant',content:'服务暂时不可用，请稍后再试。如遇紧急情况，请联系专业人员或拨打 12356。',time:Date.now()})}finally{loading.value=false;scrollBottom()}}
onMounted(()=>{try{messages.value=JSON.parse(localStorage.getItem(msgKey)||'[]');conversationId.value=localStorage.getItem(chatKey)||''}catch{};scrollBottom()})
</script>
<style scoped>
.chat-shell{display:flex;flex-direction:column;height:calc(100vh - 230px);min-height:560px;overflow:hidden}.chat-head{display:flex;align-items:center;gap:12px;padding:18px 22px;border-bottom:1px solid var(--line)}.ai-avatar,.bubble-avatar{display:grid;place-items:center;width:42px;height:42px;border-radius:13px;color:#fff;background:linear-gradient(135deg,var(--primary),#4f7cf0);flex-shrink:0}.chat-head strong{font-size:16px}.chat-head p{margin:4px 0 0;color:var(--text-3);font-size:12px}.online{margin-left:auto;color:var(--success);font-size:12px}.online span{display:inline-block;width:8px;height:8px;margin-right:6px;border-radius:50%;background:var(--success)}.online span.local{background:var(--warning)}.chat-body{flex:1;overflow:auto;padding:24px;background:linear-gradient(180deg,#f8fbfb,#f6f8fb)}.chat-empty{display:grid;place-items:center;padding-top:80px;color:var(--text-3);text-align:center}.chat-empty h3{margin:16px 0 8px;color:var(--text)}.chat-empty p{line-height:1.9;font-size:13px}.message-row{display:flex;gap:10px;margin-bottom:16px}.message-row.user{justify-content:flex-end}.bubble{max-width:min(680px,78%);padding:13px 16px;border-radius:16px 16px 16px 4px;background:#fff;box-shadow:var(--shadow-sm);line-height:1.8;white-space:pre-wrap}.bubble.user{border-radius:16px 16px 4px 16px;background:var(--primary);color:#fff;box-shadow:0 8px 18px rgba(47,125,122,.2)}.typing span{display:inline-block;width:6px;height:6px;margin-right:5px;border-radius:50%;background:#9bb8b5;animation:blink 1s infinite}.typing span:nth-child(2){animation-delay:.2s}.typing span:nth-child(3){animation-delay:.4s}@keyframes blink{50%{opacity:.25}}.chat-input{display:grid;grid-template-columns:1fr auto;gap:12px;padding:16px 18px;border-top:1px solid var(--line);background:#fff}
</style>
