<template>
  <div class="register-page">
    <div class="register-card">
      <AppLogo />
      <div class="title"><h2>创建学生账号</h2><p>注册后即可使用心理测评、报告记录与随访服务</p></div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid"><el-form-item label="用户名" prop="username"><el-input v-model="form.username" placeholder="请输入学号/用户名" /></el-form-item><el-form-item label="昵称" prop="nickname"><el-input v-model="form.nickname" placeholder="选填" /></el-form-item></div>
        <div class="form-grid"><el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password placeholder="不少于 6 位" /></el-form-item><el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" /></el-form-item></div>
        <div class="form-grid"><el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="选填" /></el-form-item><el-form-item label="年龄" prop="age"><el-input-number v-model="form.age" :min="1" :max="120" controls-position="right" style="width:100%" /></el-form-item></div>
        <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">注册账号</el-button>
      </el-form>
      <div class="footer">已有账号？<el-link type="primary" underline="never" @click="$router.push('/login')">返回登录</el-link></div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLogo from '@/components/AppLogo.vue'
import { register } from '@/api/login'
const router=useRouter(); const formRef=ref(); const loading=ref(false)
const form=reactive({username:'',nickname:'',password:'',confirmPassword:'',phone:'',age:null,sex:'2'})
const validateConfirm=(rule,value,callback)=>value===form.password?callback():callback(new Error('两次输入的密码不一致'))
const rules={username:[{required:true,message:'请输入学号/用户名',trigger:'blur'},{min:3,max:20,message:'用户名长度 3-20 个字符',trigger:'blur'}],password:[{required:true,message:'请输入密码',trigger:'blur'},{min:6,max:20,message:'密码长度 6-20 位',trigger:'blur'}],confirmPassword:[{required:true,message:'请再次输入密码',trigger:'blur'},{validator:validateConfirm,trigger:'blur'}],phone:[{pattern:/^1[3-9]\d{9}$/,message:'请输入正确的 11 位手机号',trigger:'blur'}]}
async function submit(){await formRef.value.validate();loading.value=true;try{await register({username:form.username,nickname:form.nickname,password:form.password,phone:form.phone,age:form.age,sex:'2'});ElMessage.success('注册成功，请登录');router.replace('/login')}finally{loading.value=false}}
</script>
<style scoped>
.register-page{min-height:100vh;display:grid;place-items:center;padding:30px;background:radial-gradient(circle at 18% 10%,#d9efec,transparent 34%),radial-gradient(circle at 90% 90%,#e5ecff,transparent 36%),#f4f7fa}.register-card{width:min(680px,100%);padding:38px;border:1px solid rgba(255,255,255,.9);border-radius:26px;background:#fff;box-shadow:0 24px 70px rgba(31,61,91,.12)}.title{margin:30px 0 24px}.title h2{margin:0;font-size:27px}.title p{margin:8px 0 0;color:var(--text-3);font-size:13px}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:16px}.submit{width:100%;height:46px;margin-top:6px}.footer{margin-top:20px;color:var(--text-3);font-size:13px;text-align:center}@media(max-width:620px){.register-card{padding:28px 22px}.form-grid{grid-template-columns:1fr;gap:0}}
</style>
