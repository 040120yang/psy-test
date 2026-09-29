<template>
  <div class="admin-shell">
    <aside class="admin-aside" :class="{ collapsed }">
      <div class="aside-logo"><AppLogo light /></div>
      <el-menu :default-active="$route.path" router :collapse="collapsed" class="admin-menu" background-color="transparent" text-color="rgba(255,255,255,.72)" active-text-color="#fff">
        <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon><template #title>系统概览</template></el-menu-item>
        <el-menu-item index="/admin/records"><el-icon><Document /></el-icon><template #title>测评记录</template></el-menu-item>
        <el-menu-item index="/admin/patients"><el-icon><User /></el-icon><template #title>患者管理</template></el-menu-item>
        <el-menu-item index="/admin/follow"><el-icon><Bell /></el-icon><template #title>随访管理</template></el-menu-item>
        <template v-if="isAdmin">
          <div class="menu-caption">系统维护</div>
          <el-menu-item index="/admin/knowledge"><el-icon><Reading /></el-icon><template #title>知识库</template></el-menu-item>
          <el-sub-menu index="/admin/system"><template #title><el-icon><Setting /></el-icon><span>系统管理</span></template><el-menu-item index="/admin/system/user">用户管理</el-menu-item><el-menu-item index="/admin/system/scale">量表管理</el-menu-item><el-menu-item index="/admin/system/question">题目管理</el-menu-item></el-sub-menu>
          <el-sub-menu index="/admin/logs"><template #title><el-icon><List /></el-icon><span>日志管理</span></template><el-menu-item index="/admin/logs/login">登录日志</el-menu-item><el-menu-item index="/admin/logs/oper">操作日志</el-menu-item></el-sub-menu>
        </template>
      </el-menu>
      <button class="collapse-btn" @click="collapsed=!collapsed"><el-icon><Fold v-if="!collapsed"/><Expand v-else/></el-icon></button>
    </aside>
    <section class="admin-main" :class="{ collapsed }">
      <header class="admin-header">
        <div><div class="crumb">管理后台 / {{ $route.meta.title || '系统概览' }}</div><h2>{{ $route.meta.title || '系统概览' }}</h2></div>
        <el-dropdown trigger="click" @command="handleCommand">
          <button class="admin-user"><el-avatar :size="34" class="avatar">{{ initial }}</el-avatar><span><strong>{{ user.nickname || user.username }}</strong><small>{{ roleName }}</small></span><el-icon><ArrowDown /></el-icon></button>
          <template #dropdown><el-dropdown-menu><el-dropdown-item command="portal" v-if="isAdmin">切换用户端</el-dropdown-item><el-dropdown-item divided command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown>
      </header>
      <main class="admin-content"><router-view /></main>
    </section>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLogo from '@/components/AppLogo.vue'
import { getUser, removeToken, removeUser } from '@/utils/auth'
import { logout } from '@/api/login'
const router=useRouter(); const collapsed=ref(false); const user=ref(getUser()||{})
const isAdmin=computed(()=>user.value.roles?.[0]==='admin')
const initial=computed(()=>(user.value.nickname||user.value.username||'管').slice(0,1))
const roleName=computed(()=>user.value.roles?.[0]==='admin'?'系统管理员':'临床医护人员')
const handleCommand=async command=>{if(command==='portal')return router.push('/portal/home');if(command==='logout'){try{await logout()}finally{removeToken();removeUser();ElMessage.success('已退出登录');router.replace('/login')}}}
</script>
<style scoped>
.admin-shell{min-height:100vh;background:var(--bg)}.admin-aside{position:fixed;inset:0 auto 0 0;width:248px;display:flex;flex-direction:column;background:linear-gradient(180deg,#102a43,#173f56);transition:width .22s ease;z-index:20}.admin-aside.collapsed{width:76px}.aside-logo{height:74px;display:grid;place-items:center;border-bottom:1px solid rgba(255,255,255,.08);overflow:hidden}.admin-menu{flex:1;border:0;padding:14px 10px}.admin-menu:not(.el-menu--collapse){width:248px}.admin-menu .el-menu-item,.admin-menu :deep(.el-sub-menu__title){height:46px;margin:3px 0;border-radius:10px}.admin-menu .el-menu-item:hover,.admin-menu :deep(.el-sub-menu__title:hover){background:rgba(255,255,255,.08)!important}.admin-menu .el-menu-item.is-active{background:rgba(255,255,255,.15)!important;box-shadow:inset 3px 0 0 #79c7c0}.menu-caption{padding:18px 14px 6px;color:rgba(255,255,255,.35);font-size:11px;letter-spacing:1px}.collapse-btn{height:48px;border:0;border-top:1px solid rgba(255,255,255,.08);background:transparent;color:rgba(255,255,255,.7);cursor:pointer}.admin-main{margin-left:248px;min-height:100vh;transition:margin-left .22s ease}.admin-main.collapsed{margin-left:76px}.admin-header{height:78px;display:flex;align-items:center;justify-content:space-between;padding:0 28px;background:rgba(255,255,255,.9);border-bottom:1px solid var(--line);backdrop-filter:blur(12px);position:sticky;top:0;z-index:15}.crumb{color:var(--text-3);font-size:12px}.admin-header h2{margin:4px 0 0;font-size:19px}.admin-user{display:flex;align-items:center;gap:10px;padding:6px 10px;border:1px solid var(--line);border-radius:12px;background:#fff;cursor:pointer}.avatar{background:var(--primary)}.admin-user span{display:flex;flex-direction:column;align-items:flex-start;line-height:1.2}.admin-user small{color:var(--text-3);font-size:11px}.admin-content{padding:26px 28px 50px}@media(max-width:900px){.admin-aside{width:76px}.admin-aside .copy,.admin-menu:not(.el-menu--collapse) .el-menu-item span,.menu-caption{display:none}.admin-menu{width:76px}.admin-main{margin-left:76px}.admin-header,.admin-content{padding-left:16px;padding-right:16px}}
</style>
