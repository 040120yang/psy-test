<template>
  <div class="page-stack">
    <PageHeader eyebrow="Account" title="个人中心" description="维护个人资料并修改登录密码。" />
    <section class="soft-grid cols-2">
      <div class="panel"><div class="panel-head"><div><div class="panel-title">基本资料</div><div class="panel-subtitle">这些信息会用于报告和随访联系</div></div></div><div class="panel-body"><el-form :model="profile" label-position="top"><el-form-item label="用户名"><el-input v-model="profile.username" disabled /></el-form-item><el-form-item label="昵称"><el-input v-model="profile.nickname" /></el-form-item><div class="two-cols"><el-form-item label="性别"><el-select v-model="profile.sex" style="width:100%"><el-option label="男" value="0"/><el-option label="女" value="1"/><el-option label="未知" value="2"/></el-select></el-form-item><el-form-item label="年龄"><el-input-number v-model="profile.age" :min="1" :max="120" style="width:100%" /></el-form-item></div><el-form-item label="手机号"><el-input v-model="profile.phone" /></el-form-item><el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button></el-form></div></div>
      <div class="panel"><div class="panel-head"><div><div class="panel-title">修改密码</div><div class="panel-subtitle">新密码不少于 6 位</div></div></div><div class="panel-body"><el-form :model="pwdForm" label-position="top"><el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item><el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item><el-form-item label="确认新密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item><el-button type="primary" :loading="pwdSaving" @click="savePwd">修改密码</el-button></el-form><div class="notice mt-20">为了账号安全，建议不要使用与其他网站相同的密码。</div></div></div>
    </section>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getInfo, updateProfile, updatePwd } from '@/api/login'
import { getUser, setUser } from '@/utils/auth'
const profile=reactive({userId:null,username:'',nickname:'',sex:'2',age:null,phone:''}); const pwdForm=reactive({oldPassword:'',newPassword:'',confirmPassword:''}); const saving=ref(false); const pwdSaving=ref(false)
async function saveProfile(){saving.value=true;try{await updateProfile(profile);const old=getUser()||{};setUser({...old,nickname:profile.nickname});ElMessage.success('资料已保存')}finally{saving.value=false}}
async function savePwd(){if(pwdForm.newPassword!==pwdForm.confirmPassword)return ElMessage.warning('两次输入的新密码不一致');pwdSaving.value=true;try{await updatePwd({oldPassword:pwdForm.oldPassword,newPassword:pwdForm.newPassword});ElMessage.success('密码修改成功');Object.assign(pwdForm,{oldPassword:'',newPassword:'',confirmPassword:''})}finally{pwdSaving.value=false}}
onMounted(async()=>{const res=await getInfo();Object.assign(profile,res.data.user)})
</script>
<style scoped>
.two-cols{display:grid;grid-template-columns:1fr 1fr;gap:14px}@media(max-width:620px){.two-cols{grid-template-columns:1fr}}
</style>
