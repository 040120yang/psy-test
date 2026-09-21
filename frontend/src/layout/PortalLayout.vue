<template>
  <div class="portal-wrapper">
    <!-- 用户端顶栏 -->
    <div class="portal-header">
      <div class="portal-brand">
        <span class="brand-title">区域智能诊疗辅助诊断系统</span>
        <span class="brand-sub">智能诊疗辅助诊断服务平台</span>
      </div>
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        router
        background-color="#ffffff"
        text-color="#606266"
        active-text-color="#409EFF"
        class="portal-menu"
      >
        <el-menu-item index="/portal/home">
          <i class="el-icon-house"></i>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/portal/scales">
          <i class="el-icon-notebook-2"></i>
          <span>心理测评</span>
        </el-menu-item>
        <el-menu-item index="/portal/records">
          <i class="el-icon-document"></i>
          <span>我的测评记录</span>
        </el-menu-item>
        <el-menu-item index="/portal/coze">
          <i class="el-icon-chat-dot-round"></i>
          <span>AI 心理助手</span>
        </el-menu-item>
        <el-menu-item index="/portal/personal_center">
          <i class="el-icon-user-solid"></i>
          <span>个人中心</span>
        </el-menu-item>
        <el-menu-item index="/portal/follow">
          <i class="el-icon-bell"></i>
          <span>我的随访</span>
        </el-menu-item>
      </el-menu>
      <div class="portal-user">
        <img v-if="user.avatar" :src="user.avatar" class="portal-avatar" alt="头像" />
        <i v-else class="el-icon-user-solid"></i>
        <span class="user-name">{{ user.nickname || user.username }}</span>
        <el-button type="text" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <!-- 用户端内容区 -->
    <div class="portal-main">
      <router-view />
    </div>
  </div>
</template>

<script>
import { getUser, setUser, removeToken, removeUser } from '@/utils/auth'
import { logout, getInfo } from '@/api/login'

export default {
  name: 'PortalLayout',
  data() {
    return {
      user: getUser() || {}
    }
  },
  created() {
    this.loadUser()
  },
  methods: {
    loadUser() {
      getInfo().then(res => {
        if (res.data && res.data.user) {
          this.user = res.data.user
          setUser(res.data.user)
        }
      })
    },
    handleLogout() {
      logout().finally(() => {
        removeToken()
        removeUser()
        this.$router.push('/login')
      })
    }
  }
}
</script>

<style scoped>
.portal-wrapper {
  min-height: 100%;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}
.portal-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 10;
}
.portal-brand {
  display: flex;
  flex-direction: column;
  margin-right: 32px;
}
.brand-title {
  font-size: 17px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}
.brand-sub {
  font-size: 11px;
  color: #909399;
}
.portal-menu {
  border-bottom: none;
  flex: 1;
}
.portal-user {
  display: flex;
  align-items: center;
  color: #606266;
  font-size: 14px;
}
.user-name {
  margin: 0 8px;
}
.portal-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}
.portal-main {
  flex: 1;
  padding: 20px 32px;
  width: 100%;
  box-sizing: border-box;
}
</style>
