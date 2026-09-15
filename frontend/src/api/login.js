import request from '@/utils/request'

/** 获取验证码 */
export function getCaptcha() {
  return request({
    url: '/captchaImage',
    method: 'get'
  })
}

/** 登录 */
export function login(data) {
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

/** 获取当前登录用户信息 */
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

/** 退出登录 */
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

/** 用户注册（公众用户自助注册） */
export function register(data) {
  return request({
    url: '/register',
    method: 'post',
    data
  })
}
