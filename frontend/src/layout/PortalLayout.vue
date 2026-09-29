<template>
  <div class="portal-shell">
    <header class="portal-header">
      <div class="page-shell header-inner">
        <AppLogo />
        <nav class="portal-nav">
          <router-link v-for="item in menus" :key="item.path" :to="item.path" class="nav-link">{{ item.title }}</router-link>
        </nav>
        <div class="header-actions">
          <el-badge :value="unread" :hidden="!unread" class="msg-badge">
            <el-button circle text @click="$router.push('/portal/message')"><el-icon><Bell /></el-icon></el-button>
          </el-badge>
          <el-dropdown trigger="click" @command="handleCommand">
            <button class="user-chip"><el-avatar :size="30">{{ initial }}</el-avatar><span>{{ user.nickname || user.username }}</span><el-icon><ArrowDown /></el-icon></button>
            <template #dropdown><el-dropdown-menu><el-dropdown-item command="center">个人中心</el-dropdown-item><el-dropdown-item divided command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </div>
    </header>
    <main class="portal-main page-shell"><router-view /></main>
    <footer class="portal-footer">本系统测评结果仅用于健康筛查参考，不能替代专业心理评估</footer>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLogo from '@/components/AppLogo.vue'
import { getUser, removeToken, removeUser } from '@/utils/auth'
import { logout } from '@/api/login'
import request from '@/utils/request'

const router=useRouter()
const user=ref(getUser() || {})
const unread=ref(0)
const initial=computed(()=>(user.value.nickname || user.value.username || '用户').slice(0,1))
const menus=[{path:'/portal/home',title:'工作台'},{path:'/portal/scales',title:'心理测评'},{path:'/portal/records',title:'我的记录'},{path:'/portal/follow',title:'我的随访'},{path:'/portal/knowledge',title:'心理知识'},{path:'/portal/coze',title:'AI 助手'}]
const handleCommand=async command=>{if(command==='center')return router.push('/portal/personal-center');if(command==='logout'){try{await logout()}finally{removeToken();removeUser();ElMessage.success('已退出登录');router.replace('/login')}}}
onMounted(async()=>{try{const res=await request({url:'/business/message/unread',method:'get'});unread.value=res.data||0}catch{}})
</script>
<style scoped>
.portal-shell{min-height:100vh;background:linear-gradient(180deg,#f6faf9 0,#f4f7fa 260px)}.portal-header{position:sticky;top:0;z-index:20;height:68px;background:rgba(255,255,255,.92);border-bottom:1px solid rgba(231,236,242,.9);backdrop-filter:blur(14px)}.header-inner{height:100%;display:flex;align-items:center;gap:28px}.portal-nav{display:flex;align-items:center;gap:6px;flex:1}.nav-link{padding:10px 14px;border-radius:10px;color:var(--text-2);font-size:14px;transition:.18s}.nav-link:hover,.nav-link.router-link-active{color:var(--primary);background:var(--primary-soft);font-weight:700}.header-actions{display:flex;align-items:center;gap:12px}.user-chip{display:flex;align-items:center;gap:8px;padding:5px 9px;border:1px solid var(--line);border-radius:999px;background:#fff;color:var(--text);cursor:pointer}.msg-badge{margin-right:2px}.portal-main{padding:28px 24px 48px}.portal-footer{padding:24px;color:var(--text-3);font-size:12px;text-align:center}@media(max-width:1050px){.portal-nav{gap:0}.nav-link{padding:9px 8px;font-size:13px}.copy small{display:none}}@media(max-width:760px){.portal-nav{display:none}.header-inner{gap:12px}.user-chip span{display:none}.portal-main{padding:20px 14px 36px}}
</style>
