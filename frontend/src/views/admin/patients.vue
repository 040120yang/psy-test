<template>
  <div class="page-stack">
    <PageHeader eyebrow="Student Records" title="学生管理" description="维护学生档案、联系方式和心理关注标签。">
      <template #actions><el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>新增学生</el-button></template>
    </PageHeader>
    <section class="panel"><div class="panel-head"><el-input v-model="query.patientName" clearable placeholder="学生姓名" style="width:200px"/><el-input v-model="query.phone" clearable placeholder="联系电话" style="width:200px"/><el-button type="primary" @click="load">查询</el-button></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe><el-table-column prop="patientName" label="姓名" min-width="110"/><el-table-column label="性别" width="80"><template #default="{row}">{{ row.sex==='0'?'男':row.sex==='1'?'女':'未知' }}</template></el-table-column><el-table-column prop="age" label="年龄" width="80"/><el-table-column prop="phone" label="联系电话" min-width="130"/><el-table-column prop="tag" label="标签" min-width="110"><template #default="{row}"><el-tag v-if="row.tag" effect="plain">{{ row.tag }}</el-tag><span v-else class="text-muted">-</span></template></el-table-column><el-table-column prop="medicalHistory" label="既往经历/主要困扰" min-width="220" show-overflow-tooltip/><el-table-column label="操作" width="140" fixed="right"><template #default="{row}"><el-button text type="primary" @click="openEdit(row)">编辑</el-button><el-button text type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无学生档案"/><el-pagination v-if="total" v-model:current-page="query.pageNum" layout="total, prev, pager, next" :total="total" @current-change="load"/></div></section>
    <el-dialog v-model="visible" :title="form.patientId?'编辑学生':'新增学生'" width="720px"><el-form :model="form" label-position="top"><div class="form-grid"><el-form-item label="姓名"><el-input v-model="form.patientName"/></el-form-item><el-form-item label="性别"><el-select v-model="form.sex" style="width:100%"><el-option label="男" value="0"/><el-option label="女" value="1"/><el-option label="未知" value="2"/></el-select></el-form-item><el-form-item label="年龄"><el-input-number v-model="form.age" :min="1" :max="120" style="width:100%"/></el-form-item><el-form-item label="联系电话"><el-input v-model="form.phone"/></el-form-item><el-form-item label="学号/证件号"><el-input v-model="form.idCard"/></el-form-item><el-form-item label="标签"><el-input v-model="form.tag"/></el-form-item></div><el-form-item label="联系地址"><el-input v-model="form.address"/></el-form-item><el-form-item label="既往经历/主要困扰"><el-input v-model="form.medicalHistory" type="textarea" :rows="3"/></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listPatient, addPatient, updatePatient, delPatient } from '@/api/patient'
const rows=ref([]); const total=ref(0); const visible=ref(false); const query=reactive({pageNum:1,pageSize:10,patientName:'',phone:''}); const form=reactive({patientId:null,patientName:'',sex:'2',age:null,phone:'',idCard:'',address:'',medicalHistory:'',tag:''})
async function load(){const res=await listPatient(query);rows.value=res.rows||[];total.value=res.total||rows.value.length}
function reset(){Object.assign(form,{patientId:null,patientName:'',sex:'2',age:null,phone:'',idCard:'',address:'',medicalHistory:'',tag:''})}
function openAdd(){reset();visible.value=true}
function openEdit(row){Object.assign(form,row);visible.value=true}
async function save(){if(!form.patientName)return ElMessage.warning('请输入学生姓名');form.patientId?await updatePatient(form):await addPatient(form);ElMessage.success('保存成功');visible.value=false;load()}
async function remove(row){await ElMessageBox.confirm(`确认删除学生“${row.patientName}”吗？`,'删除确认',{type:'warning'});await delPatient(row.patientId);ElMessage.success('已删除');load()}
onMounted(load)
</script>
<style scoped>
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}@media(max-width:620px){.form-grid{grid-template-columns:1fr}}
</style>
