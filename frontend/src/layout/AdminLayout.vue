<template>
  <div class="admin-shell">
    <aside class="admin-aside" :class="{ collapsed }">
      <div class="aside-logo">
        <AppLogo light />
      </div>

      <el-menu
        :default-active="$route.path"
        router
        :collapse="collapsed"
        class="admin-menu"
        background-color="transparent"
        text-color="rgba(255,255,255,.72)"
        active-text-color="#fff"
      >
        <el-menu-item index="/admin/workbench">
          <el-icon><Tickets /></el-icon>
          <template #title>待办工作台</template>
        </el-menu-item>

        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>系统概览</template>
        </el-menu-item>

        <el-menu-item index="/admin/records">
          <el-icon><Document /></el-icon>
          <template #title>测评记录</template>
        </el-menu-item>

        <el-menu-item index="/admin/patients">
          <el-icon><User /></el-icon>
          <template #title>学生管理</template>
        </el-menu-item>

        <el-menu-item index="/admin/follow">
          <el-icon><Bell /></el-icon>
          <template #title>随访管理</template>
        </el-menu-item>

        <el-menu-item index="/admin/chat">
          <el-icon><ChatLineSquare /></el-icon>
          <template #title>
            沟通中心
            <span v-if="chatUnread" class="menu-count">{{ chatUnread }}</span>
          </template>
        </el-menu-item>

        <div class="menu-caption">内容管理</div>

        <el-menu-item index="/admin/knowledge">
          <el-icon><Reading /></el-icon>
          <template #title>知识库</template>
        </el-menu-item>

        <template v-if="isAdmin">
          <div class="menu-caption">系统维护</div>

          <el-sub-menu index="/admin/system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/system/user">用户管理</el-menu-item>
            <el-menu-item index="/admin/system/scale">量表管理</el-menu-item>
            <el-menu-item index="/admin/system/question">题目管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/admin/logs">
            <template #title>
              <el-icon><List /></el-icon>
              <span>日志管理</span>
            </template>
            <el-menu-item index="/admin/logs/login">登录日志</el-menu-item>
            <el-menu-item index="/admin/logs/oper">操作日志</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>

      <button
        class="collapse-btn"
        :title="collapsed ? '展开导航' : '收起导航'"
        @click="collapsed = !collapsed"
      >
        <el-icon>
          <Fold v-if="!collapsed" />
          <Expand v-else />
        </el-icon>
        <span v-if="!collapsed">收起导航</span>
      </button>
    </aside>

    <section class="admin-main" :class="{ collapsed }">
      <header class="admin-header">
        <div class="header-left">
          <div>
            <div class="crumb">
              管理后台 / {{ $route.meta.title || '系统概览' }}
            </div>
            <h2>{{ $route.meta.title || '系统概览' }}</h2>
          </div>
          <span class="header-status"><i></i>服务正常</span>
        </div>

        <div class="header-actions">
          <el-button
            class="refresh-btn"
            text
            circle
            title="刷新页面"
            @click="router.go(0)"
          >
            <el-icon><Refresh /></el-icon>
          </el-button>

          <el-dropdown trigger="click">
            <button class="admin-user">
              <el-avatar :size="34" class="avatar">{{ initial }}</el-avatar>
              <span class="admin-user-info">
                <strong>{{ user.nickname || user.username }}</strong>
                <small>{{ roleName }}</small>
              </span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLogo from '@/components/AppLogo.vue'
import { getUser, removeToken, removeUser } from '@/utils/auth'
import { logout } from '@/api/login'
import { getChatUnread } from '@/api/chat'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const user = ref(getUser() || {})

const isAdmin = computed(() => user.value.roles?.[0] === 'admin')

const initial = computed(() =>
  (user.value.nickname || user.value.username || '管').slice(0, 1)
)

const roleName = computed(() =>
  user.value.roles?.[0] === 'admin' ? '系统管理员' : '心理教师/辅导员'
)

const handleLogout = async () => {
  try {
    await logout()
  } finally {
    removeToken()
    removeUser()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}

// 沟通未读提醒：轮询未读数量，显示在「沟通中心」菜单上
const chatUnread = ref(0)
let timer = null

async function refreshChatUnread() {
  try {
    const res = await getChatUnread()
    chatUnread.value = Number(res.data || 0)
  } catch (e) {}
}

function onFocus() {
  refreshChatUnread()
}

onMounted(() => {
  refreshChatUnread()
  timer = setInterval(refreshChatUnread, 20000)
  window.addEventListener('focus', onFocus)
})

watch(() => route.path, () => refreshChatUnread())

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('focus', onFocus)
})
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #f4f8fb 0, #eef3f7 100%);
}

.admin-aside {
  position: fixed;
  inset: 0 auto 0 0;
  width: 264px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #102f49 0%, #143d58 55%, #12344c 100%);
  box-shadow: 8px 0 28px rgba(16, 47, 73, .08);
  transition: width .22s ease;
  z-index: 20;
}

.admin-aside.collapsed {
  width: 76px;
}

.aside-logo {
  height: 82px;
  display: grid;
  place-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, .1);
  overflow: hidden;
}

.admin-menu {
  flex: 1;
  border: 0;
  padding: 18px 12px;
  background: transparent;
}

.admin-menu:not(.el-menu--collapse) {
  width: 264px;
}

.admin-menu .el-menu-item,
.admin-menu :deep(.el-sub-menu__title) {
  height: 50px;
  margin: 5px 0;
  border-radius: 12px;
  font-size: 14px;
}

.admin-menu .el-menu-item:hover,
.admin-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, .1) !important;
}

.admin-menu .el-menu-item.is-active {
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, .2),
    rgba(255, 255, 255, .1)
  ) !important;
  box-shadow: inset 3px 0 0 #76d1c8;
}

.admin-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #fff !important;
}

.admin-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0, 0, 0, .1) !important;
  border-radius: 12px;
  margin: 3px 0;
  padding: 4px 0;
}

.admin-menu :deep(.el-sub-menu .el-menu-item) {
  height: 42px;
  margin: 2px 8px;
  padding-left: 52px !important;
  font-size: 13px;
}

.menu-caption {
  padding: 22px 14px 8px;
  color: rgba(255, 255, 255, .4);
  font-size: 11px;
  letter-spacing: 1.5px;
}

.menu-count {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  margin-left: 8px;
  padding: 0 5px;
  border-radius: 9px;
  background: #e5484d;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  text-align: center;
  vertical-align: 1px;
}

.collapse-btn {
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  border: 0;
  border-top: 1px solid rgba(255, 255, 255, .1);
  background: rgba(0, 0, 0, .08);
  color: rgba(255, 255, 255, .7);
  font-size: 12px;
  cursor: pointer;
}

.collapse-btn:hover {
  color: #fff;
  background: rgba(0, 0, 0, .18);
}

.admin-main {
  margin-left: 264px;
  min-height: 100vh;
  transition: margin-left .22s ease;
}

.admin-main.collapsed {
  margin-left: 76px;
}

.admin-header {
  height: 82px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 34px;
  background: rgba(255, 255, 255, .94);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(14px);
  position: sticky;
  top: 0;
  z-index: 15;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.crumb {
  color: var(--text-3);
  font-size: 12px;
}

.admin-header h2 {
  margin: 5px 0 0;
  font-size: 21px;
  letter-spacing: .2px;
}

.header-status {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 10px;
  border: 1px solid #d9eee5;
  border-radius: 999px;
  background: #f4fbf7;
  color: var(--success);
  font-size: 11px;
}

.header-status i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--success);
  box-shadow: 0 0 0 3px rgba(54, 162, 105, .12);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-btn {
  width: 36px;
  height: 36px;
  color: var(--text-3);
}

.refresh-btn:hover {
  color: var(--primary);
  background: var(--primary-soft);
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 11px;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  transition: .18s;
}

.admin-user:hover {
  border-color: #b8d8d5;
  box-shadow: var(--shadow-sm);
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: var(--primary);
  line-height: 1;
}

.admin-user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.2;
}

.admin-user-info small {
  color: var(--text-3);
  font-size: 11px;
  margin-top: 3px;
}

.admin-content {
  max-width: 1680px;
  margin: 0 auto;
  padding: 30px 34px 56px;
}

@media (max-width: 900px) {
  .admin-aside {
    width: 76px;
  }

  .admin-aside .copy,
  .admin-menu:not(.el-menu--collapse) .el-menu-item span,
  .admin-menu :deep(.el-sub-menu__title span),
  .menu-caption,
  .collapse-btn span,
  .header-status {
    display: none;
  }

  .admin-menu {
    width: 76px;
  }

  .admin-main {
    margin-left: 76px;
  }

  .admin-header,
  .admin-content {
    padding-left: 18px;
    padding-right: 18px;
  }

  .admin-header {
    height: 72px;
  }

  .admin-header h2 {
    font-size: 18px;
  }

  .header-left {
    gap: 10px;
  }
}

@media (max-width: 620px) {
  .admin-content {
    padding: 20px 12px 36px;
  }

  .admin-user-info,
  .admin-user > .el-icon {
    display: none;
  }

  .admin-user {
    padding: 5px;
  }

  .refresh-btn {
    display: none;
  }
}
</style>
