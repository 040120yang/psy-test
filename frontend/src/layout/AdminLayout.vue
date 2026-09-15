<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <div class="sidebar-container">
      <div class="sidebar-logo">
        <span class="logo-text">区域心理测试系统</span>
        <span class="logo-sub">管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        unique-opened
      >
        <template v-for="item in menuRoutes">
          <!-- 子菜单 -->
          <el-submenu
            v-if="!item.hidden && item.children && visibleChildren(item).length > 0"
            :key="item.path"
            :index="'/admin/' + item.path"
          >
            <template slot="title">
              <i :class="itemMeta(item).icon"></i>
              <span>{{ itemMeta(item).title }}</span>
            </template>
            <el-menu-item
              v-for="child in visibleChildren(item)"
              :key="child.path"
              :index="'/admin/' + item.path + '/' + child.path"
            >
              <i :class="child.meta && child.meta.icon"></i>
              <span slot="title">{{ child.meta && child.meta.title }}</span>
            </el-menu-item>
          </el-submenu>

          <!-- 直接菜单项 -->
          <el-menu-item
            v-else-if="!item.hidden && item.children && visibleChildren(item).length === 0"
            :key="item.path"
            :index="'/admin/' + item.path"
          >
            <i :class="itemMeta(item).icon"></i>
            <span slot="title">{{ itemMeta(item).title }}</span>
          </el-menu-item>
          <el-menu-item v-else-if="!item.hidden" :key="item.path" :index="'/admin/' + item.path">
            <i :class="itemMeta(item).icon"></i>
            <span slot="title">{{ itemMeta(item).title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </div>

    <!-- 主区域 -->
    <div class="main-container">
      <div class="navbar">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <el-dropdown class="user-dropdown" @command="handleCommand">
          <span class="user-info">
            <i class="el-icon-user-solid"></i>
            {{ user.nickname || user.username || '用户' }}
            <span class="role-tag">{{ roleName }}</span>
            <i class="el-icon-arrow-down"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>

      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { adminRoutes } from '@/router'
import { getUser, removeToken, removeUser } from '@/utils/auth'
import { logout } from '@/api/login'

export default {
  name: 'AdminLayout',
  data() {
    return {
      user: getUser() || {},
      roleKey: (getUser() && getUser().roles && getUser().roles[0]) || ''
    }
  },
  computed: {
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      if (meta && meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    menuRoutes() {
      // 取 /admin 路由下的子菜单项
      const admin = adminRoutes[0]
      return admin ? admin.children : []
    },
    roleName() {
      const map = { admin: '系统管理员', doctor: '临床医护人员', user: '公众用户' }
      return map[this.roleKey] || '未知角色'
    }
  },
  methods: {
    itemMeta(item) {
      if (item.meta) return item.meta
      if (item.children && item.children.length && item.children[0].meta) return item.children[0].meta
      return {}
    },
    visibleChildren(item) {
      if (!item.children) return []
      return item.children.filter(child => !child.hidden && this.hasPermission(child.meta && child.meta.roles))
    },
    hasPermission(roles) {
      if (!roles || roles.length === 0) return true
      return roles.indexOf(this.roleKey) !== -1
    },
    handleCommand(command) {
      if (command === 'logout') {
        logout().finally(() => {
          removeToken()
          removeUser()
          this.$router.push('/login')
        })
      }
    }
  }
}
</script>

<style scoped>
.app-wrapper {
  height: 100%;
  display: flex;
}
.sidebar-container {
  width: 210px;
  background-color: #304156;
  height: 100%;
  flex-shrink: 0;
  overflow-y: auto;
}
.sidebar-logo {
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4b;
}
.logo-text {
  color: #fff;
  font-size: 15px;
  font-weight: bold;
  letter-spacing: 1px;
}
.logo-sub {
  color: #8a97a8;
  font-size: 11px;
  margin-top: 2px;
}
.sidebar-container .el-menu {
  border-right: none;
}
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 5;
}
.user-info {
  cursor: pointer;
  color: #333;
  font-size: 14px;
}
.role-tag {
  margin-left: 6px;
  padding: 1px 8px;
  border-radius: 3px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 12px;
}
.app-main {
  flex: 1;
  padding: 16px;
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
