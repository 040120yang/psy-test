<template>
  <div class="auth-page">
    <section class="auth-visual">
      <div class="visual-orb orb-a"></div><div class="visual-orb orb-b"></div>
      <div class="visual-content">
        <div class="visual-badge">Regional Psychological Care</div>
        <h1>让心理测评、专业评估与持续随访连接起来</h1>
        <p>面向公众用户的在线量表测评，以及面向医护人员的患者档案、诊断与随访管理。</p>
        <div class="visual-features">
          <div><el-icon><CircleCheck /></el-icon><span>SAS / SDS / SCL-90 / SRSS 在线测评</span></div>
          <div><el-icon><CircleCheck /></el-icon><span>自动计分、风险分级与健康建议</span></div>
          <div><el-icon><CircleCheck /></el-icon><span>医生随访与个案闭环管理</span></div>
        </div>
      </div>
      <div class="hotline-card"><span>全国心理援助热线</span><strong>12356</strong><small>24 小时提供支持</small></div>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <AppLogo />
        <div class="auth-title"><h2>欢迎登录</h2><p>使用系统账号进入对应工作台</p></div>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="submit">
          <el-form-item prop="username"><el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" /></el-form-item>
          <el-form-item prop="password"><el-input v-model="form.password" type="password" show-password placeholder="密码" :prefix-icon="Lock" /></el-form-item>
          <el-form-item prop="code">
            <div class="captcha-row"><el-input v-model="form.code" placeholder="验证码" :prefix-icon="Key" /><button type="button" class="captcha-button" @click="loadCaptcha"><img v-if="captchaImg" :src="captchaImg" alt="验证码" /><span v-else>刷新</span></button></div>
          </el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="submit">登录系统</el-button>
        </el-form>
        <div class="demo-tip"><strong>演示账号</strong><span>admin / doctor / user，密码均为 123456</span></div>
        <div class="auth-footer">还没有账号？<el-link type="primary" underline="never" @click="$router.push('/register')">立即注册</el-link></div>
      </div>
    </section>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import AppLogo from '@/components/AppLogo.vue'
import { getCaptcha, login, getInfo } from '@/api/login'
import { setToken, setUser } from '@/utils/auth'
const route=useRoute(); const router=useRouter(); const formRef=ref(); const captchaImg=ref(''); const loading=ref(false)
const form=reactive({username:'',password:'',code:'',uuid:''})
const rules={username:[{required:true,message:'请输入用户名',trigger:'blur'}],password:[{required:true,message:'请输入密码',trigger:'blur'}],code:[{required:true,message:'请输入验证码',trigger:'blur'}]}
async function loadCaptcha(){const res=await getCaptcha();captchaImg.value=`data:image/png;base64,${res.data.img}`;form.uuid=res.data.uuid;form.code=''}
async function submit(){await formRef.value.validate();loading.value=true;try{const res=await login(form);setToken(res.data.token);const info=await getInfo();const data=info.data;const user={userId:data.user.userId,username:data.user.username,nickname:data.user.nickname,roles:data.roles,roleName:data.roleName};setUser(user);ElMessage.success('登录成功');const role=user.roles?.[0]||'user';const target=role==='user'?'/portal/home':'/admin/dashboard';router.replace(route.query.redirect||target)}catch{await loadCaptcha()}finally{loading.value=false}}
onMounted(loadCaptcha)
</script>
<style scoped>
.auth-page{min-height:100vh;display:grid;grid-template-columns:1.15fr .85fr;background:#edf4f5}.auth-visual{position:relative;overflow:hidden;display:flex;flex-direction:column;justify-content:center;padding:8vw 7vw;color:#fff;background:linear-gradient(145deg,#123b4a 0,#1f625f 52%,#3c75bd 145%)}.visual-content{position:relative;z-index:2;max-width:660px}.visual-badge{display:inline-block;padding:7px 12px;border:1px solid rgba(255,255,255,.25);border-radius:999px;color:rgba(255,255,255,.74);font-size:11px;letter-spacing:1.5px}.auth-visual h1{max-width:620px;margin:24px 0 18px;font-size:46px;line-height:1.25;letter-spacing:-1px}.auth-visual p{max-width:590px;margin:0;color:rgba(255,255,255,.76);font-size:16px;line-height:1.9}.visual-features{display:grid;gap:14px;margin-top:34px;color:rgba(255,255,255,.86);font-size:14px}.visual-features div{display:flex;align-items:center;gap:10px}.hotline-card{position:relative;z-index:2;width:fit-content;margin-top:52px;padding:18px 24px;border:1px solid rgba(255,255,255,.18);border-radius:18px;background:rgba(255,255,255,.1);backdrop-filter:blur(10px)}.hotline-card span,.hotline-card small{display:block;color:rgba(255,255,255,.68);font-size:12px}.hotline-card strong{display:block;margin:4px 0;font-size:32px;letter-spacing:2px}.visual-orb{position:absolute;border-radius:50%;background:rgba(255,255,255,.08)}.orb-a{width:360px;height:360px;right:-110px;top:-100px}.orb-b{width:240px;height:240px;left:-90px;bottom:-100px}.auth-panel{display:grid;place-items:center;padding:40px}.auth-card{width:min(430px,100%);padding:40px;border:1px solid rgba(255,255,255,.8);border-radius:26px;background:rgba(255,255,255,.94);box-shadow:0 24px 70px rgba(31,61,91,.14)}.auth-title{margin:32px 0 26px}.auth-title h2{margin:0;font-size:27px}.auth-title p{margin:8px 0 0;color:var(--text-3);font-size:13px}.captcha-row{display:flex;gap:10px;width:100%}.captcha-row .el-input{flex:1}.captcha-button{width:130px;height:40px;padding:0;border:1px solid var(--line);border-radius:10px;overflow:hidden;background:#f7fafc;cursor:pointer}.captcha-button img{width:100%;height:100%;object-fit:cover}.submit-btn{width:100%;height:46px;margin-top:4px}.demo-tip{display:flex;gap:10px;margin-top:22px;padding:12px 14px;border-radius:12px;background:var(--primary-soft);color:var(--primary-deep);font-size:12px}.demo-tip span{color:var(--text-2)}.auth-footer{margin-top:22px;color:var(--text-3);font-size:13px;text-align:center}@media(max-width:900px){.auth-page{grid-template-columns:1fr}.auth-visual{display:none}.auth-panel{padding:22px;background:linear-gradient(160deg,#e7f3f1,#f4f7fa)}.auth-card{padding:30px 24px}}
</style>
