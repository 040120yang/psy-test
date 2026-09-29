import NProgress from 'nprogress'
import router from '@/router'
import { getToken, getUser, removeToken, removeUser } from '@/utils/auth'

NProgress.configure({ showSpinner: false })
const whiteList = ['/login', '/register', '/404']

router.beforeEach((to) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - 校园心理健康智能测评与随访系统` : '校园心理健康智能测评与随访系统'
  const token = getToken()
  if (!token) {
    if (whiteList.includes(to.path)) return true
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  if (to.path === '/login') return '/'
  const user = getUser()
  if (!user) {
    removeToken()
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  const roles = to.matched.flatMap(item => item.meta?.roles || [])
  const current = user.roles?.[0]
  if (roles.length && current && !roles.includes(current)) return '/'
  return true
})

router.afterEach(() => NProgress.done())
router.onError(() => NProgress.done())
