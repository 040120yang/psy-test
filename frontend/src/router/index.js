import { createRouter, createWebHistory } from 'vue-router'
import { getUserRole } from '@/utils/auth'

const homeByRole = () => getUserRole() === 'user' ? '/portal/home' : '/admin/dashboard'

const portalRoutes = {
  path: '/portal',
  component: () => import('@/layout/PortalLayout.vue'),
  redirect: '/portal/home',
  meta: { roles: ['user'] },
  children: [
    { path: 'home', component: () => import('@/views/portal/home.vue'), meta: { title: '工作台', icon: 'HomeFilled' } },
    { path: 'scales', component: () => import('@/views/portal/scales.vue'), meta: { title: '心理测评', icon: 'Notebook' } },
    { path: 'answer', component: () => import('@/views/portal/answer.vue'), meta: { title: '在线答题', hidden: true } },
    { path: 'result', component: () => import('@/views/portal/result.vue'), meta: { title: '测评报告', hidden: true } },
    { path: 'records', component: () => import('@/views/portal/records.vue'), meta: { title: '我的记录', icon: 'Document' } },
    { path: 'follow', component: () => import('@/views/portal/follow.vue'), meta: { title: '我的随访', icon: 'Bell' } },
    { path: 'message', component: () => import('@/views/portal/message.vue'), meta: { title: '消息中心', icon: 'ChatDotRound' } },
    { path: 'knowledge', component: () => import('@/views/portal/knowledge.vue'), meta: { title: '心理知识', icon: 'Reading' } },
    { path: 'coze', component: () => import('@/views/portal/coze.vue'), meta: { title: 'AI 助手', icon: 'MagicStick' } },
    { path: 'personal-center', component: () => import('@/views/portal/personal_center.vue'), meta: { title: '个人中心', hidden: true } }
  ]
}

const adminRoutes = {
  path: '/admin',
  component: () => import('@/layout/AdminLayout.vue'),
  redirect: '/admin/dashboard',
  meta: { roles: ['admin', 'doctor'] },
  children: [
    { path: 'dashboard', component: () => import('@/views/admin/dashboard.vue'), meta: { title: '系统概览', icon: 'DataAnalysis' } },
    { path: 'records', component: () => import('@/views/admin/records.vue'), meta: { title: '测评记录', icon: 'Document' } },
    { path: 'patients', component: () => import('@/views/admin/patients.vue'), meta: { title: '患者管理', icon: 'User' } },
    { path: 'follow', component: () => import('@/views/admin/follow.vue'), meta: { title: '随访管理', icon: 'Bell' } },
    { path: 'knowledge', component: () => import('@/views/admin/knowledge.vue'), meta: { title: '知识库', icon: 'Reading', roles: ['admin'] } },
    { path: 'logs/login', component: () => import('@/views/admin/log/login.vue'), meta: { title: '登录日志', icon: 'Clock', roles: ['admin'] } },
    { path: 'logs/oper', component: () => import('@/views/admin/log/oper.vue'), meta: { title: '操作日志', icon: 'List', roles: ['admin'] } },
    { path: 'system/user', component: () => import('@/views/admin/system/user.vue'), meta: { title: '用户管理', icon: 'UserFilled', roles: ['admin'] } },
    { path: 'system/scale', component: () => import('@/views/admin/system/scale.vue'), meta: { title: '量表管理', icon: 'Notebook', roles: ['admin'] } },
    { path: 'system/question', component: () => import('@/views/admin/system/question.vue'), meta: { title: '题目管理', icon: 'Tickets', roles: ['admin'] } }
  ]
}

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/login', component: () => import('@/views/login/index.vue'), meta: { public: true, title: '登录' } },
    { path: '/register', component: () => import('@/views/login/register.vue'), meta: { public: true, title: '注册' } },
    { path: '/404', component: () => import('@/views/error/404.vue'), meta: { public: true, title: '页面不存在' } },
    { path: '/', redirect: homeByRole },
    portalRoutes,
    adminRoutes,
    { path: '/:pathMatch(.*)*', redirect: '/404' }
  ]
})

export default router
