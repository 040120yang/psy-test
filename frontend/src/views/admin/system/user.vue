<template>
  <div class="page-stack">
    <PageHeader eyebrow="Access Control" title="用户管理" description="维护账号、角色、状态和登录密码。">
      <template #actions><el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon>新增用户</el-button></template>
    </PageHeader>
    <section class="panel"><div class="panel-head"><el-input v-model="query.username" clearable placeholder="用户名" style="width:180px"/><el-input v-model="query.nickname" clearable placeholder="昵称" style="width:180px"/><el-button type="primary" @click="load">查询</el-button></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe><el-table-column prop="username" label="用户名" min-width="120"/><el-table-column prop="nickname" label="昵称" min-width="120"/><el-table-column prop="roleName" label="角色" min-width="130"/><el-table-column prop="phone" label="手机号" min-width="130"/><el-table-column label="状态" width="100"><template #default="{row}"><StatusTag :text="row.status==='0'?'启用':'停用'"/></template></el-table-column><el-table-column prop="createTime" label="创建时间" width="180"/><el-table-column label="操作" width="220" fixed="right"><template #default="{row}"><el-button text type="primary" @click="openEdit(row)">编辑</el-button><el-button text @click="openReset(row)">重置密码</el-button><el-button text type="danger" :disabled="row.userId===1" @click="remove(row)">删除</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无用户"/><el-pagination v-if="total" v-model:current-page="query.pageNum" layout="total, prev, pager, next" :total="total" @current-change="load"/></div></section>
    <el-dialog v-model="visible" :title="form.userId?'编辑用户':'新增用户'" width="680px"><el-form :model="form" label-position="top"><div class="form-grid"><el-form-item label="用户名"><el-input v-model="form.username" :disabled="!!form.userId"/></el-form-item><el-form-item label="昵称"><el-input v-model="form.nickname"/></el-form-item><el-form-item v-if="!form.userId" label="初始密码"><el-input v-model="form.password" type="password" show-password/></el-form-item><el-form-item label="角色"><el-select v-model="form.roleId" style="width:100%"><el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId"/></el-select></el-form-item><el-form-item label="手机号"><el-input v-model="form.phone"/></el-form-item><el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option label="正常" value="0"/><el-option label="停用" value="1"/></el-select></el-form-item></div></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
    <el-dialog v-model="resetVisible" title="重置密码" width="460px"><el-form label-position="top"><el-form-item label="新密码"><el-input v-model="newPassword" type="password" show-password/></el-form-item></el-form><template #footer><el-button @click="resetVisible=false">取消</el-button><el-button type="primary" @click="resetPwd">确认重置</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listUser, listRoles, addUser, updateUser, delUser, resetUserPwd } from '@/api/system'
const rows=ref([]); const roles=ref([]); const total=ref(0); const visible=ref(false); const resetVisible=ref(false); const newPassword=ref('123456'); const resetUser=ref(null); const query=reactive({pageNum:1,pageSize:10,username:'',nickname:''}); const form=reactive(blank())
function blank(){return {userId:null,username:'',nickname:'',password:'123456',roleId:null,phone:'',status:'0'}}
async function load(){const res=await listUser(query);rows.value=res.rows||[];total.value=res.total||rows.value.length}
function openAdd(){Object.assign(form,blank());visible.value=true}
function openEdit(row){Object.assign(form,blank(),row);visible.value=true}
async function save(){if(!form.username)return ElMessage.warning('请输入用户名');form.userId?await updateUser(form):await addUser(form);ElMessage.success('保存成功');visible.value=false;load()}
function openReset(row){resetUser.value=row;newPassword.value='123456';resetVisible.value=true}
async function resetPwd(){await resetUserPwd({userId:resetUser.value.userId,password:newPassword.value});ElMessage.success('密码已重置');resetVisible.value=false}
async function remove(row){await ElMessageBox.confirm(`确认删除用户“${row.username}”吗？`,'删除确认',{type:'warning'});await delUser(row.userId);ElMessage.success('已删除');load()}
onMounted(async()=>{await load();const res=await listRoles();roles.value=res.data||[]})
</script>
<style scoped>
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}@media(max-width:620px){.form-grid{grid-template-columns:1fr}}
</style>
