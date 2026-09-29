<template>
  <div class="page-stack">
    <PageHeader eyebrow="Messages" title="消息中心" description="系统通知、随访提醒和测评结果消息。" />
    <section class="panel"><div class="panel-head"><div><div class="panel-title">全部消息</div><div class="panel-subtitle">{{ unread }} 条未读</div></div><el-button :disabled="!unread" @click="readAll">全部标记已读</el-button></div><div class="panel-body">
      <div v-if="rows.length" class="message-list"><article v-for="item in rows" :key="item.id" class="message-item" :class="{unread:!item.isRead}" @click="open(item)"><div class="message-icon"><el-icon><Bell /></el-icon></div><div class="message-main"><div class="message-title">{{ item.title }}<el-tag v-if="!item.isRead" size="small" type="danger" effect="plain">未读</el-tag></div><p>{{ item.content }}</p><span>{{ item.createTime }}</span></div></article></div>
      <EmptyPanel v-else title="暂无消息" description="系统通知会显示在这里" />
    </div></section>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listMessages, markMessageRead } from '@/api/business'
const rows=ref([]); const unread=computed(()=>rows.value.filter(i=>i.isRead===0).length)
async function load(){const res=await listMessages();rows.value=res.rows||[]}
async function open(item){if(item.isRead===0){await markMessageRead(item.id);item.isRead=1}ElMessageBox.alert(item.content,item.title,{confirmButtonText:'知道了'})}
async function readAll(){for(const item of rows.value.filter(i=>i.isRead===0))await markMessageRead(item.id);rows.value.forEach(i=>i.isRead=1)}
onMounted(load)
</script>
<style scoped>
.message-list{display:grid;gap:10px}.message-item{display:flex;gap:14px;padding:16px;border:1px solid var(--line);border-radius:14px;transition:.18s}.message-item:hover{border-color:#b8d8d5;background:#f8fcfb}.message-item.unread{background:#f6fbfa}.message-icon{display:grid;place-items:center;width:40px;height:40px;border-radius:12px;background:var(--primary-soft);color:var(--primary);flex-shrink:0}.message-main{min-width:0;flex:1}.message-title{display:flex;align-items:center;gap:8px;font-weight:700}.message-main p{margin:7px 0;color:var(--text-2);font-size:13px;line-height:1.7}.message-main span{color:var(--text-3);font-size:12px}
</style>
