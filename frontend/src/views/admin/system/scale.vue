<template>
  <div class="page-stack">
    <PageHeader eyebrow="Scale Management" title="量表管理" description="维护量表名称、编码、评分制和启用状态。">
      <template #actions><el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>新增量表</el-button></template>
    </PageHeader>
    <section class="panel"><div class="panel-head"><el-input v-model="query.scaleName" clearable placeholder="量表名称" style="width:200px"/><el-button type="primary" @click="load">查询</el-button></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe><el-table-column prop="scaleName" label="量表名称" min-width="180"/><el-table-column prop="scaleCode" label="编码" width="110"/><el-table-column prop="description" label="说明" min-width="240" show-overflow-tooltip/><el-table-column label="评分制" width="110"><template #default="{row}">{{ row.optionType===5?'五级评分':'四级评分' }}</template></el-table-column><el-table-column label="状态" width="100"><template #default="{row}"><StatusTag :text="row.status==='0'?'启用':'停用'"/></template></el-table-column><el-table-column prop="sortNo" label="排序" width="80"/><el-table-column label="操作" width="140"><template #default="{row}"><el-button text type="primary" @click="openEdit(row)">编辑</el-button><el-button text type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无量表"/></div></section>
    <el-dialog v-model="visible" :title="form.scaleId?'编辑量表':'新增量表'" width="640px"><el-form :model="form" label-position="top"><div class="form-grid"><el-form-item label="量表名称"><el-input v-model="form.scaleName"/></el-form-item><el-form-item label="量表编码"><el-input v-model="form.scaleCode"/></el-form-item><el-form-item label="评分制"><el-select v-model="form.optionType" style="width:100%"><el-option label="四级评分" :value="4"/><el-option label="五级评分" :value="5"/></el-select></el-form-item><el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option label="启用" value="0"/><el-option label="停用" value="1"/></el-select></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sortNo" :min="0" style="width:100%"/></el-form-item></div><el-form-item label="量表说明"><el-input v-model="form.description" type="textarea" :rows="3"/></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listScale, addScale, updateScale, delScale } from '@/api/system'
const rows=ref([]); const visible=ref(false); const query=reactive({pageNum:1,pageSize:50,scaleName:''}); const form=reactive(blank())
function blank(){return {scaleId:null,scaleName:'',scaleCode:'',description:'',optionType:4,status:'0',sortNo:0}}
async function load(){const res=await listScale(query);rows.value=res.rows||[]}
function openAdd(){Object.assign(form,blank());visible.value=true}
function openEdit(row){Object.assign(form,blank(),row);visible.value=true}
async function save(){form.scaleId?await updateScale(form):await addScale(form);ElMessage.success('保存成功');visible.value=false;load()}
async function remove(row){await ElMessageBox.confirm(`确认删除量表“${row.scaleName}”及其题目吗？`,'删除确认',{type:'warning'});await delScale(row.scaleId);ElMessage.success('已删除');load()}
onMounted(load)
</script>
<style scoped>
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}@media(max-width:620px){.form-grid{grid-template-columns:1fr}}
</style>
