<template>
  <div class="page-stack">
    <PageHeader eyebrow="Access Control" title="用户管理" description="维护账号、角色、状态和登录密码，支持批量导入建号与批量操作。">
      <template #actions>
        <el-button @click="downloadTemplate"><el-icon><Download /></el-icon>下载模板</el-button>
        <el-button @click="openImport"><el-icon><Upload /></el-icon>批量导入用户</el-button>
        <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>新增用户</el-button>
      </template>
    </PageHeader>
    <section class="panel"><div class="panel-head"><el-input v-model="query.username" clearable placeholder="用户名" style="width:180px"/><el-input v-model="query.nickname" clearable placeholder="昵称" style="width:180px"/><el-button type="primary" @click="search">查询</el-button><span class="batch-bar" v-if="selected.length"><el-button type="warning" plain size="small" @click="openBatchReset">批量重置密码</el-button><el-button type="primary" plain size="small" @click="openBatchAssign">批量分配负责教师</el-button><span class="selected-count">已选 {{ selected.length }} 人</span></span></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe @selection-change="onSelect"><el-table-column type="selection" width="50"/><el-table-column prop="username" label="用户名" min-width="120"/><el-table-column prop="nickname" label="昵称" min-width="120"/><el-table-column prop="roleName" label="角色" min-width="130"/><el-table-column prop="phone" label="手机号" min-width="130"/><el-table-column label="状态" width="100"><template #default="{row}"><StatusTag :text="row.status==='0'?'启用':'停用'"/></template></el-table-column><el-table-column prop="createTime" label="创建时间" width="180"/><el-table-column label="操作" width="220" fixed="right"><template #default="{row}"><el-button text type="primary" @click="openEdit(row)">编辑</el-button><el-button text @click="openReset(row)">重置密码</el-button><el-button text type="danger" :disabled="row.userId===1" @click="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无用户"/><el-pagination v-if="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" :total="total" @current-change="load" @size-change="search"/></div></section>
    <el-dialog v-model="visible" :title="form.userId?'编辑用户':'新增用户'" width="680px"><el-form :model="form" label-position="top"><div class="form-grid"><el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!form.userId"/></el-form-item><el-form-item label="昵称"><el-input v-model="form.nickname"/></el-form-item><el-form-item v-if="!form.userId" label="初始密码"><el-input v-model="form.password" type="password" show-password/></el-form-item><el-form-item label="角色"><el-select v-model="form.roleId" style="width:100%"><el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId"/></el-select></el-form-item><el-form-item label="手机号"><el-input v-model="form.phone"/></el-form-item><el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option label="正常" value="0"/><el-option label="停用" value="1"/></el-select></el-form-item></div></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
    <el-dialog v-model="resetVisible" title="重置密码" width="460px"><el-form label-position="top"><el-form-item label="新密码"><el-input v-model="newPassword" type="password" show-password/></el-form-item></el-form><template #footer><el-button @click="resetVisible=false">取消</el-button><el-button type="primary" @click="resetPwd">确认重置</el-button></template></el-dialog>
    <el-dialog v-model="importVisible" title="批量导入用户" width="560px"><el-form label-position="top"><el-form-item label="CSV 文件"><el-upload :auto-upload="false" :limit="1" accept=".csv" :on-change="onImportFileChange" :file-list="importFileList" drag><el-icon><UploadFilled /></el-icon><div class="el-upload__text">拖拽 CSV 文件到此处，或<em>点击上传</em></div><template #tip><div class="el-upload__tip">格式：用户名,姓名,性别,角色,手机号,初始密码<br/>性别填 男/女；角色填 学生用户 / 心理教师/辅导员 / 系统管理员（留空默认学生）；初始密码留空默认 123456</div></template></el-upload></el-form-item></el-form><template #footer><el-button @click="importVisible=false">取消</el-button><el-button type="primary" :disabled="!importFile" @click="doImport">开始导入</el-button></template></el-dialog>
    <el-dialog v-model="batchResetVisible" title="批量重置密码" width="460px"><el-form label-position="top"><el-form-item :label="`为选中的 ${selected.length} 名用户设置新密码`"><el-input v-model="batchPassword" type="password" show-password/></el-form-item></el-form><template #footer><el-button @click="batchResetVisible=false">取消</el-button><el-button type="primary" @click="doBatchReset">确认重置</el-button></template></el-dialog>
    <el-dialog v-model="batchAssignVisible" title="批量分配负责教师" width="460px"><el-form label-position="top"><el-form-item :label="`为选中的 ${selected.length} 名学生分配负责教师`"><el-select v-model="assignTeacherId" style="width:100%" placeholder="选择心理教师"><el-option v-for="d in doctors" :key="d.userId" :label="d.nickname || d.username" :value="d.userId"/></el-select></el-form-item></el-form><template #footer><el-button @click="batchAssignVisible=false">取消</el-button><el-button type="primary" @click="doBatchAssign">确认分配</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listUser, listRoles, addUser, updateUser, delUser, resetUserPwd, listDoctors, batchImportUser, batchResetPwd, batchAssignTeacher } from '@/api/system'
import request from '@/utils/request'
const rows=ref([]); const roles=ref([]); const total=ref(0); const visible=ref(false); const resetVisible=ref(false); const newPassword=ref('123456'); const resetUser=ref(null); const selected=ref([]); const doctors=ref([]); const importVisible=ref(false); const importFile=ref(null); const importFileList=ref([]); const batchResetVisible=ref(false); const batchPassword=ref('123456'); const batchAssignVisible=ref(false); const assignTeacherId=ref(null); const query=reactive({pageNum:1,pageSize:10,username:'',nickname:''}); const form=reactive(blank())
function blank(){return {userId:null,username:'',nickname:'',password:'123456',roleId:null,phone:'',status:'0'}}
async function load(){const res=await listUser(query);rows.value=res.rows||[];total.value=res.total||rows.value.length;selected.value=[]}
function search(){query.pageNum=1;load()}
function openAdd(){Object.assign(form,blank());visible.value=true}
function openEdit(row){Object.assign(form,blank(),row);visible.value=true}
async function save(){if(!form.username)return ElMessage.warning('请输入用户名');form.userId?await updateUser(form):await addUser(form);ElMessage.success('保存成功');visible.value=false;load()}
function openReset(row){resetUser.value=row;newPassword.value='123456';resetVisible.value=true}
async function resetPwd(){await resetUserPwd({userId:resetUser.value.userId,password:newPassword.value});ElMessage.success('密码已重置');resetVisible.value=false}
async function remove(row){await ElMessageBox.confirm(`确认删除用户“${row.username}”吗？`,'删除确认',{type:'warning'});await delUser(row.userId);ElMessage.success('已删除');load()}
function onSelect(rows){selected.value=rows}
function downloadTemplate(){request({url:'/system/user/importTemplate',method:'get',responseType:'blob'}).then(res=>{const blob=new Blob([res],{type:'text/csv;charset=utf-8'});const link=document.createElement('a');link.href=URL.createObjectURL(blob);link.download='student_import_template.csv';link.click();URL.revokeObjectURL(link.href)}).catch(()=>{})}
function openImport(){importFile.value=null;importFileList.value=[];importVisible.value=true}
function onImportFileChange(file){importFile.value=file.raw;importFileList.value=[file]}
async function doImport(){if(!importFile.value)return ElMessage.warning('请选择 CSV 文件');const res=await batchImportUser(importFile.value);const data=res.data||{};ElMessage.success(`导入完成：成功 ${data.success} 人，失败 ${data.fail} 人`);if(data.errors&&data.errors.length){ElMessageBox.alert(data.errors.join('\n'),'导入失败明细',{type:'warning'})}importVisible.value=false;load()}
function openBatchReset(){batchPassword.value='123456';batchResetVisible.value=true}
async function doBatchReset(){if(!batchPassword.value)return ElMessage.warning('请输入新密码');await batchResetPwd({userIds:selected.value.map(u=>u.userId),password:batchPassword.value});ElMessage.success('密码已批量重置');batchResetVisible.value=false;load()}
function openBatchAssign(){assignTeacherId.value=null;batchAssignVisible.value=true}
async function doBatchAssign(){if(!assignTeacherId.value)return ElMessage.warning('请选择负责教师');await batchAssignTeacher({userIds:selected.value.map(u=>u.userId),teacherId:assignTeacherId.value});ElMessage.success('已批量分配');batchAssignVisible.value=false;load()}
onMounted(async()=>{await load();const res=await listRoles();roles.value=res.data||[];try{const d=await listDoctors();doctors.value=d.data||[]}catch(e){}})
</script>
<style scoped>
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}@media(max-width:620px){.form-grid{grid-template-columns:1fr}}
.batch-bar{display:inline-flex;align-items:center;gap:8px;margin-left:12px}
.selected-count{font-size:13px;color:#5b6b7b}
</style>
