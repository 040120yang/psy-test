<template>
  <div class="page-stack">
    <PageHeader eyebrow="Knowledge Base" title="心理知识库" description="了解情绪、压力、睡眠和心理调适方法。" />
    <div class="notice">本知识库内容用于健康科普，不构成诊断或治疗建议。如有持续不适，请及时咨询专业人员。</div>
    <section class="panel"><div class="panel-head"><div><div class="panel-title">文章列表</div><div class="panel-subtitle">共 {{ rows.length }} 篇</div></div><el-select v-model="category" clearable placeholder="全部分类" style="width:180px" @change="load"><el-option v-for="item in categories" :key="item" :label="item" :value="item" /></el-select></div><div class="panel-body">
      <div v-if="rows.length" class="knowledge-grid"><article v-for="item in rows" :key="item.id" class="knowledge-card" @click="open(item)"><div class="knowledge-category">{{ item.category || '心理健康' }}</div><h3>{{ item.title }}</h3><p>{{ item.summary || '点击查看文章详情' }}</p><div><span>{{ item.author || '系统知识库' }}</span><span>{{ item.viewCount || 0 }} 次阅读</span></div></article></div>
      <EmptyPanel v-else title="暂无可展示文章" description="知识库暂时没有符合条件的文章" />
    </div></section>
    <el-dialog v-model="visible" :title="detail?.title" width="720px"><div style="line-height:2;color:var(--text-2);white-space:pre-wrap">{{ detail?.content }}</div></el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listKnowledge, getKnowledge } from '@/api/business'
const rows=ref([]); const category=ref(''); const categories=['情绪管理','压力调适','睡眠健康','人际交往','亲子关系','心理科普','危机支持','自我关怀']; const visible=ref(false); const detail=ref(null)
async function load(){try{const res=await listKnowledge({category:category.value});rows.value=res.rows||[]}catch{rows.value=[]}}
async function open(item){const res=await getKnowledge(item.id);detail.value=res.data;visible.value=true}
onMounted(load)
</script>
<style scoped>
.knowledge-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}.knowledge-card{padding:20px;border:1px solid var(--line);border-radius:16px;cursor:pointer;transition:.18s}.knowledge-card:hover{transform:translateY(-3px);box-shadow:var(--shadow-md);border-color:#b8d8d5}.knowledge-category{color:var(--primary);font-size:12px;font-weight:700}.knowledge-card h3{margin:10px 0 8px;font-size:17px}.knowledge-card p{margin:0;min-height:52px;color:var(--text-3);font-size:13px;line-height:1.8}.knowledge-card div:last-child{display:flex;justify-content:space-between;margin-top:18px;color:var(--text-3);font-size:11px}@media(max-width:900px){.knowledge-grid{grid-template-columns:1fr 1fr}}@media(max-width:620px){.knowledge-grid{grid-template-columns:1fr}}
</style>
