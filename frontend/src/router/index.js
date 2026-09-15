import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

const PortalLayout = () => import('@/layout/PortalLayout')
const AdminLayout = () => import('@/layout/AdminLayout')

/**
 * 用户端路由（公众用户：心理测评 + 我的测评记录）
 */
export const portalRoutes = [
  {
    path: '/portal',
    component: PortalLayout,
    redirect: '/portal/home',
    meta: { title: '心理测评', icon: 'el-icon-notebook-2', roles: ['user'] },
    children: [
      {
        path: 'home',
        name: 'PortalHome',
        component: () => import('@/views/portal/home'),
        meta: { title: '首页', icon: 'el-icon-house' }
      },
      {
        path: 'scales',
        name: 'PortalScales',
        component: () => import('@/views/portal/scales'),
        meta: { title: '心理测评', icon: 'el-icon-notebook-2' }
      },
      {
        path: 'answer',
        name: 'PortalAnswer',
        component: () => import('@/views/portal/answer'),
        hidden: true,
        meta: { title: '在线答题', activeMenu: '/portal/scales' }
      },
      {
        path: 'result',
        name: 'PortalResult',
        component: () => import('@/views/portal/result'),
        hidden: true,
        meta: { title: '测评报告', activeMenu: '/portal/scales' }
      },
      {
        path: 'records',
        name: 'PortalRecords',
        component: () => import('@/views/portal/records'),
        meta: { title: '我的测评记录', icon: 'el-icon-document' }
      },
      {
        path: 'coze',
        name: 'PortalCoze',
        component: () => import('@/views/portal/coze'),
        meta: { title: 'AI 智能体', icon: 'el-icon-chat-dot-round' }
      }
    ]
  }
]

/**
 * 管理端路由（系统管理员 / 临床医护人员）
 */
export const adminRoutes = [
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { title: '管理后台', roles: ['admin', 'doctor'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard'),
        meta: { title: '系统概览', icon: 'el-icon-data-analysis', roles: ['admin', 'doctor'] }
      },
      {
        path: 'records',
        name: 'AdminRecords',
        component: () => import('@/views/admin/records'),
        meta: { title: '测评记录', icon: 'el-icon-document', roles: ['admin', 'doctor'] }
      },
      {
        path: 'patients',
        name: 'AdminPatients',
        component: () => import('@/views/admin/patients'),
        meta: { title: '患者管理', icon: 'el-icon-user', roles: ['admin', 'doctor'] }
      },
      {
        path: 'system',
        // vue-router 3 嵌套父节点必须提供组件，否则子路由空白
        component: { render: h => h('router-view') },
        meta: { title: '系统管理', icon: 'el-icon-setting', roles: ['admin'] },
        children: [
          {
            path: 'user',
            name: 'AdminSystemUser',
            component: () => import('@/views/admin/system/user'),
            meta: { title: '用户管理', icon: 'el-icon-user-solid' }
          },
          {
            path: 'scale',
            name: 'AdminSystemScale',
            component: () => import('@/views/admin/system/scale'),
            meta: { title: '量表管理', icon: 'el-icon-notebook-1' }
          },
          {
            path: 'question',
            name: 'AdminSystemQuestion',
            component: () => import('@/views/admin/system/question'),
            meta: { title: '题目管理', icon: 'el-icon-tickets' }
          }
        ]
      }
    ]
  }
]

/**
 * 路由表
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/login/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/',
    redirect: '/portal/home',
    hidden: true
  },
  ...portalRoutes,
  ...adminRoutes,
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

const router = new Router({
  mode: 'history',
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

export default router
