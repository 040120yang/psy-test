/**
 * 认证信息存储：使用 sessionStorage
 * 关闭浏览器（会话结束）后登录态自动失效，每次运行需重新登录
 */
const TokenKey = 'PsyTest-Token'
const UserKey = 'PsyTest-User'

export function getToken() {
  return sessionStorage.getItem(TokenKey)
}

export function setToken(token) {
  sessionStorage.setItem(TokenKey, token)
}

export function removeToken() {
  sessionStorage.removeItem(TokenKey)
}

export function getUser() {
  const str = sessionStorage.getItem(UserKey)
  if (!str) return null
  try {
    return JSON.parse(str)
  } catch (e) {
    return null
  }
}

export function setUser(user) {
  sessionStorage.setItem(UserKey, JSON.stringify(user))
}

export function removeUser() {
  sessionStorage.removeItem(UserKey)
}
