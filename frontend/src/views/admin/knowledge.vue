<template>
  <div class="page-stack">
    <PageHeader eyebrow="Knowledge Management" title="知识库管理" description="维护心理科普文章和分类内容。">
      <template #actions><el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>新增文章</el-button></template>
    </PageHeader>
    <section class="panel"><div class="panel-head"><div><div class="panel-title">文章列表</div><div class="panel-subtitle">共 {{ rows.length }} 篇</div></div><el-select v-model="category" clearable placeholder="全部分类" style="width:180px" @change="load"><el-option v-for="item in categories" :key="item" :label="item" :value="item"/></el-select></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe><el-table-column prop="title" label="标题" min-width="220"/><el-table-column prop="category" label="分类" width="120"/><el-table-column prop="author" label="作者" width="120"/><el-table-column prop="viewCount" label="阅读量" width="90"/><el-table-column prop="createTime" label="创建时间" width="170"/><el-table-column label="操作" width="140"><template #default="{row}"><el-button text type="primary" @click="openEdit(row)">编辑</el-button><el-button text type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无知识文章" description="当前数据库尚未完成知识库表或没有文章"/></div></section>
    <el-dialog v-model="visible" :title="form.id?'编辑文章':'新增文章'" width="760px"><el-form :model="form" label-position="top"><div class="form-grid"><el-form-item label="标题"><el-input v-model="form.title"/></el-form-item><el-form-item label="分类"><el-select v-model="form.category" style="width:100%"><el-option v-for="item in categories" :key="item" :label="item" :value="item"/></el-select></el-form-item><el-form-item label="作者"><el-input v-model="form.author"/></el-form-item></div><el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="2"/></el-form-item><el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="10"/></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listKnowledge, addKnowledge, updateKnowledge, delKnowledge } from '@/api/business'
const rows=ref([]); const category=ref(''); const categories=['情绪管理','压力调适','睡眠健康','人际交往','亲子关系','心理科普','危机支持','自我关怀']; const visible=ref(false); const form=reactive(blank())
function blank(){return {id:null,title:'',category:'心理科普',author:'系统管理员',summary:'',content:''}}
async function load(){try{const res=await listKnowledge({category:category.value});rows.value=res.rows||[]}catch{rows.value=[]}}
function openAdd(){Object.assign(form,blank());visible.value=true}
function openEdit(row){Object.assign(form,blank(),row);visible.value=true}
async function save(){form.id?await updateKnowledge(form):await addKnowledge(form);ElMessage.success('保存成功');visible.value=false;load()}
async function remove(row){await ElMessageBox.confirm('确认删除这篇文章吗？','删除确认',{type:'warning'});await delKnowledge(row.id);ElMessage.success('已删除');load()}
onMounted(load)
</script>
<style scoped>
.form-grid{display:grid;grid-template-columns:2fr 1fr 1fr;gap:0 16px}@media(max-width:620px){.form-grid{grid-template-columns:1fr}}
</style>
