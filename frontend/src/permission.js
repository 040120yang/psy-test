import { getToken, removeToken, removeUser } from '@/utils/auth'
import router from './router'

/**
 * 路由守卫：未登录跳转登录页
 */
const whiteList = ['/login', '/register']

router.beforeEach((to, from, next) => {
  if (getToken()) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`)
    }
  }
})

router.afterEach(() => {
  // 预留：记录历史
})
