<template>
  <div class="page-stack">
    <PageHeader eyebrow="AI Companion" title="AI 心理助手" description="用于心理陪伴和健康科普，不提供医学诊断。遇到危机情况请优先联系专业人员或拨打 12356。">
      <template #actions><el-button @click="clearChat">清空对话</el-button></template>
    </PageHeader>
    <section class="chat-shell panel">
      <div class="chat-head">
        <div class="ai-avatar"><el-icon><MagicStick /></el-icon></div>
        <div class="ai-meta"><strong>心理陪伴助手</strong><p>{{ modeText }} · {{ mode==='local' ? '本地陪伴' : '云端在线' }}</p></div>
        <el-radio-group v-model="chatMode" size="small" class="mode-group">
          <el-radio-button value="listen">倾听</el-radio-button><el-radio-button value="advice">建议</el-radio-button><el-radio-button value="science">科普</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="chatBody" class="chat-body">
        <div v-if="!messages.length" class="chat-empty"><el-icon :size="52"><ChatDotRound /></el-icon><h3>你可以慢慢说</h3><p>不用一下子把问题讲清楚。先告诉我最近最困扰你的一件事。</p><div class="quick-prompts"><button v-for="item in quickPrompts" :key="item" @click="send(item)">{{ item }}</button></div></div>
        <div v-for="(item,index) in messages" :key="index" class="message-row" :class="item.role">
          <div v-if="item.role==='assistant'" class="bubble-avatar"><el-icon><MagicStick /></el-icon></div>
          <div class="bubble" :class="item.role"><span style="white-space:pre-wrap">{{ item.content }}</span><i v-if="item.streaming" class="cursor"></i></div>
        </div>
        <div v-if="loading && !messages[messages.length-1]?.streaming" class="message-row assistant"><div class="bubble-avatar"><el-icon><MagicStick /></el-icon></div><div class="bubble typing"><span></span><span></span><span></span></div></div>
      </div>
      <div class="chat-input"><el-input v-model="input" type="textarea" :rows="2" resize="none" placeholder="写下你现在的感受，Enter 发送，Shift+Enter 换行" @keydown.enter.exact.prevent="send()" /><el-button type="primary" size="large" :loading="loading" @click="send()">发送</el-button></div>
      <div class="chat-footnote">AI 回复仅用于心理陪伴和健康教育，不能替代医生诊断。如有自伤或轻生想法，请立即求助身边人员并拨打 12356。</div>
    </section>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { ElMessageBox } from 'element-plus'
import { chatWithCoze } from '@/api/coze'
import { getUser } from '@/utils/auth'
const user=getUser()||{}; const input=ref(''); const loading=ref(false); const messages=ref([]); const conversationId=ref(''); const mode=ref('auto'); const chatMode=ref('listen'); const chatBody=ref()
const quickPrompts=['最近总是失眠','考试前很紧张','我觉得自己不够好','和同学相处很累']
const modeText=computed(()=>({listen:'陪伴倾听',advice:'实用建议',science:'心理科普'}[chatMode.value]))
const msgKey=`psy-v4-ai-messages-${user.username||'guest'}`; const chatKey=`psy-v4-ai-chat-${user.username||'guest'}`
const sleep=ms=>new Promise(resolve=>setTimeout(resolve,ms))
function persist(){localStorage.setItem(msgKey,JSON.stringify(messages.value.slice(-40)));localStorage.setItem(chatKey,conversationId.value)}
async function scrollBottom(){await nextTick();if(chatBody.value)chatBody.value.scrollTop=chatBody.value.scrollHeight}
async function send(preset){const text=(preset||input.value).trim();if(!text||loading.value)return;const history=messages.value.filter(i=>!i.streaming).slice(-8).map(i=>({role:i.role,content:i.content}));messages.value.push({role:'user',content:text,time:Date.now()});input.value='';loading.value=true;persist();scrollBottom();let assistant=null;try{const res=await chatWithCoze({message:text,conversationId:conversationId.value||undefined,history,mode:chatMode.value});conversationId.value=res.conversationId||'';mode.value=res.mode||'cloud';const reply=res.reply||'我暂时没有想好怎么回应，可以再告诉我一点吗？';assistant={role:'assistant',content:'',time:Date.now(),streaming:true};messages.value.push(assistant);for(let i=0;i<reply.length;i+=3){assistant.content+=reply.slice(i,i+3);scrollBottom();await sleep(12)}assistant.streaming=false;persist()}catch(e){if(assistant){assistant.streaming=false;assistant.content='服务暂时不可用，请稍后再试。如果情况紧急，请联系身边可信任的人或拨打 12356。'}else{messages.value.push({role:'assistant',content:'服务暂时不可用，请稍后再试。如果情况紧急，请联系身边可信任的人或拨打 12356。',time:Date.now()})}}finally{loading.value=false;persist();scrollBottom()}}
async function clearChat(){if(messages.value.length){await ElMessageBox.confirm('确认清空当前对话记录吗？','清空对话',{type:'warning'})}messages.value=[];conversationId.value='';mode.value='auto';localStorage.removeItem(msgKey);localStorage.removeItem(chatKey)}
onMounted(()=>{try{messages.value=JSON.parse(localStorage.getItem(msgKey)||'[]').map(i=>({...i,streaming:false}));conversationId.value=localStorage.getItem(chatKey)||''}catch{};scrollBottom()})
</script>
<style scoped>
.chat-shell{display:flex;flex-direction:column;height:calc(100vh - 210px);min-height:620px;overflow:hidden}.chat-head{display:flex;align-items:center;gap:12px;padding:16px 22px;border-bottom:1px solid var(--line)}.ai-avatar,.bubble-avatar{display:grid;place-items:center;width:42px;height:42px;border-radius:13px;color:#fff;background:linear-gradient(135deg,var(--primary),#4f7cf0);flex-shrink:0}.ai-meta{flex:1}.ai-meta strong{font-size:16px}.ai-meta p{margin:4px 0 0;color:var(--text-3);font-size:12px}.mode-group{margin-left:auto}.chat-body{flex:1;overflow:auto;padding:24px;background:linear-gradient(180deg,#f8fbfb,#f6f8fb)}.chat-empty{display:grid;place-items:center;padding-top:58px;color:var(--text-3);text-align:center}.chat-empty h3{margin:16px 0 8px;color:var(--text)}.chat-empty p{max-width:520px;line-height:1.9;font-size:13px}.quick-prompts{display:flex;flex-wrap:wrap;justify-content:center;gap:10px;margin-top:20px}.quick-prompts button{padding:9px 14px;border:1px solid var(--line);border-radius:999px;background:#fff;color:var(--text-2);cursor:pointer}.quick-prompts button:hover{border-color:var(--primary);color:var(--primary)}.message-row{display:flex;gap:10px;margin-bottom:18px}.message-row.user{justify-content:flex-end}.bubble{max-width:min(720px,78%);padding:13px 16px;border-radius:17px 17px 17px 5px;background:#fff;box-shadow:var(--shadow-sm);line-height:1.85}.bubble.user{border-radius:17px 17px 5px 17px;background:var(--primary);color:#fff;box-shadow:0 8px 18px rgba(47,125,122,.2)}.cursor{display:inline-block;width:2px;height:1em;margin-left:3px;background:var(--primary);vertical-align:-2px;animation:blink .8s infinite}.typing span{display:inline-block;width:6px;height:6px;margin-right:5px;border-radius:50%;background:#9bb8b5;animation:blink 1s infinite}.typing span:nth-child(2){animation-delay:.2s}.typing span:nth-child(3){animation-delay:.4s}@keyframes blink{50%{opacity:.2}}.chat-input{display:grid;grid-template-columns:1fr auto;gap:12px;padding:14px 18px 8px;border-top:1px solid var(--line);background:#fff}.chat-footnote{padding:0 18px 12px;color:var(--text-3);font-size:11px;text-align:center}@media(max-width:720px){.mode-group{margin-left:0}.chat-head{flex-wrap:wrap}.chat-shell{height:auto;min-height:620px}.bubble{max-width:88%}}
</style>
