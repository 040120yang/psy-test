<template>
  <div class="page-stack">
    <PageHeader eyebrow="Follow-up Management" title="随访管理" description="查看随访任务、完成随访并设置下次随访日期。">
      <template #actions><el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>新增随访</el-button></template>
    </PageHeader>
    <section class="panel"><div class="panel-head"><el-select v-model="status" clearable placeholder="全部状态" style="width:180px" @change="load"><el-option label="待随访" value="待随访"/><el-option label="已完成" value="已完成"/></el-select><el-button @click="load">查询</el-button></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe><el-table-column prop="followDate" label="随访日期" width="120"/><el-table-column prop="userId" label="用户ID" width="90"/><el-table-column prop="followType" label="方式" width="90"/><el-table-column label="状态" width="110"><template #default="{row}"><StatusTag :text="row.status"/></template></el-table-column><el-table-column prop="symptomScore" label="症状评分" width="100"/><el-table-column prop="doctorNote" label="医生记录" min-width="220" show-overflow-tooltip/><el-table-column prop="nextFollowDate" label="下次随访" width="120"/><el-table-column label="操作" width="180" fixed="right"><template #default="{row}"><el-button v-if="row.status!=='已完成'" text type="primary" @click="openFinish(row)">完成随访</el-button><el-button text @click="openEdit(row)">编辑</el-button><el-button text type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无随访任务"/></div></section>
    <el-dialog v-model="visible" :title="form.id?'编辑随访':'新增随访'" width="680px"><el-form :model="form" label-position="top"><div class="form-grid"><el-form-item label="用户ID"><el-input-number v-model="form.userId" :min="1" style="width:100%"/></el-form-item><el-form-item label="测评记录ID"><el-input-number v-model="form.recordId" :min="1" style="width:100%"/></el-form-item><el-form-item label="随访日期"><el-date-picker v-model="form.followDate" type="date" value-format="YYYY-MM-DD" style="width:100%"/></el-form-item><el-form-item label="随访方式"><el-select v-model="form.followType" style="width:100%"><el-option label="线上" value="线上"/><el-option label="电话" value="电话"/><el-option label="面诊" value="面诊"/></el-select></el-form-item><el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option label="待随访" value="待随访"/><el-option label="已完成" value="已完成"/></el-select></el-form-item><el-form-item label="症状评分"><el-input-number v-model="form.symptomScore" :min="0" :max="10" style="width:100%"/></el-form-item></div><el-form-item label="医生记录"><el-input v-model="form.doctorNote" type="textarea" :rows="3"/></el-form-item><el-form-item label="下次随访日期"><el-date-picker v-model="form.nextFollowDate" type="date" value-format="YYYY-MM-DD" style="width:100%"/></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
    <el-dialog v-model="finishVisible" title="完成随访" width="620px"><el-form label-position="top"><el-form-item label="症状评分（0-10）"><el-input-number v-model="finishForm.symptomScore" :min="0" :max="10"/></el-form-item><el-form-item label="随访记录"><el-input v-model="finishForm.doctorNote" type="textarea" :rows="4"/></el-form-item><el-form-item label="下次随访日期"><el-date-picker v-model="finishForm.nextFollowDate" type="date" value-format="YYYY-MM-DD" style="width:100%"/></el-form-item></el-form><template #footer><el-button @click="finishVisible=false">取消</el-button><el-button type="primary" @click="finish">确认完成</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listAdminFollows, addFollow, updateFollow, finishFollow, delFollow } from '@/api/follow'
const rows=ref([]); const status=ref(''); const visible=ref(false); const finishVisible=ref(false); const form=reactive(blank()); const finishForm=reactive({id:null,doctorNote:'',symptomScore:0,nextFollowDate:''})
function blank(){return {id:null,userId:null,recordId:null,followDate:'',followType:'线上',status:'待随访',symptomScore:null,doctorNote:'',nextFollowDate:''}}
async function load(){const res=await listAdminFollows({status:status.value});rows.value=res.rows||[]}
function openAdd(){Object.assign(form,blank());visible.value=true}
function openEdit(row){Object.assign(form,blank(),row);visible.value=true}
function openFinish(row){Object.assign(finishForm,{id:row.id,doctorNote:'',symptomScore:row.symptomScore||0,nextFollowDate:''});finishVisible.value=true}
async function save(){form.id?await updateFollow(form):await addFollow(form);ElMessage.success('保存成功');visible.value=false;load()}
async function finish(){await finishFollow(finishForm);ElMessage.success('随访已完成');finishVisible.value=false;load()}
async function remove(row){await ElMessageBox.confirm('确认删除该随访任务吗？','删除确认',{type:'warning'});await delFollow(row.id);ElMessage.success('已删除');load()}
onMounted(load)
</script>
<style scoped>
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}@media(max-width:620px){.form-grid{grid-template-columns:1fr}}
</style>
