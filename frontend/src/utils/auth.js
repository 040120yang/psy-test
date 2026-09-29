const TOKEN_KEY = 'psy-test-token'
const USER_KEY = 'psy-test-user'

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}
export function setToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token)
}
export function removeToken() {
  sessionStorage.removeItem(TOKEN_KEY)
}
export function getUser() {
  const raw = sessionStorage.getItem(USER_KEY)
  if (!raw) return null
  try { return JSON.parse(raw) } catch { return null }
}
export function setUser(user) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}
export function removeUser() {
  sessionStorage.removeItem(USER_KEY)
}
export function getUserRole() {
  return getUser()?.roles?.[0] || 'user'
}
export function isAdmin() {
  return getUserRole() === 'admin'
}
