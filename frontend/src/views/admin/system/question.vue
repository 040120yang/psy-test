<template>
  <div class="page-stack">
    <PageHeader eyebrow="Question Bank" title="题目管理" description="按量表维护题号、题干和反向计分设置。">
      <template #actions><el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>新增题目</el-button></template>
    </PageHeader>
    <section class="panel"><div class="panel-head"><el-select v-model="query.scaleId" placeholder="选择量表" style="width:200px" @change="load"><el-option v-for="s in scales" :key="s.scaleId" :label="s.scaleName" :value="s.scaleId"/></el-select><el-input v-model="query.content" clearable placeholder="题干关键词" style="width:240px"/><el-button type="primary" @click="load">查询</el-button></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe><el-table-column prop="questionId" label="ID" width="80"/><el-table-column prop="scaleName" label="所属量表" min-width="150"/><el-table-column prop="sortNo" label="题号" width="80"/><el-table-column prop="content" label="题目内容" min-width="360"/><el-table-column label="反向计分" width="100"><template #default="{row}"><el-tag v-if="row.reverseFlag===1" type="warning" effect="plain">是</el-tag><el-tag v-else effect="plain">否</el-tag></template></el-table-column><el-table-column label="操作" width="140"><template #default="{row}"><el-button text type="primary" @click="openEdit(row)">编辑</el-button><el-button text type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无题目"/></div></section>
    <el-dialog v-model="visible" :title="form.questionId?'编辑题目':'新增题目'" width="680px"><el-form :model="form" label-position="top"><div class="form-grid"><el-form-item label="所属量表"><el-select v-model="form.scaleId" style="width:100%"><el-option v-for="s in scales" :key="s.scaleId" :label="s.scaleName" :value="s.scaleId"/></el-select></el-form-item><el-form-item label="题号"><el-input-number v-model="form.sortNo" :min="1" style="width:100%"/></el-form-item><el-form-item label="反向计分"><el-switch v-model="form.reverseFlag" :active-value="1" :inactive-value="0"/></el-form-item></div><el-form-item label="题目内容"><el-input v-model="form.content" type="textarea" :rows="4"/></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listQuestion, addQuestion, updateQuestion, delQuestion, listScale } from '@/api/system'
const rows=ref([]); const scales=ref([]); const visible=ref(false); const query=reactive({pageNum:1,pageSize:100,scaleId:null,content:''}); const form=reactive(blank())
function blank(){return {questionId:null,scaleId:null,sortNo:1,content:'',reverseFlag:0}}
async function load(){const res=await listQuestion(query);rows.value=res.rows||[]}
function openAdd(){Object.assign(form,blank(),{scaleId:query.scaleId||scales.value[0]?.scaleId});visible.value=true}
function openEdit(row){Object.assign(form,blank(),row);visible.value=true}
async function save(){if(!form.content)return ElMessage.warning('请输入题目内容');form.questionId?await updateQuestion(form):await addQuestion(form);ElMessage.success('保存成功');visible.value=false;load()}
async function remove(row){await ElMessageBox.confirm('确认删除该题目吗？','删除确认',{type:'warning'});await delQuestion(row.questionId);ElMessage.success('已删除');load()}
onMounted(async()=>{const res=await listScale({pageNum:1,pageSize:100});scales.value=res.rows||[];if(scales.value.length)query.scaleId=scales.value[0].scaleId;load()})
</script>
<style scoped>
.form-grid{display:grid;grid-template-columns:2fr 1fr 1fr;gap:0 16px}@media(max-width:620px){.form-grid{grid-template-columns:1fr}}
</style>
