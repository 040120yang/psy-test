import request from '@/utils/request'

// ===== 用户管理 =====
export function listUser(params) {
  return request({ url: '/system/user/list', method: 'get', params })
}

export function listRoles() {
  return request({ url: '/system/user/roles', method: 'get' })
}

export function addUser(data) {
  return request({ url: '/system/user', method: 'post', data })
}

export function updateUser(data) {
  return request({ url: '/system/user', method: 'put', data })
}

export function delUser(userId) {
  return request({ url: '/system/user/' + userId, method: 'delete' })
}

export function resetUserPwd(data) {
  return request({ url: '/system/user/resetPwd', method: 'put', data })
}

// ===== 量表管理 =====
export function listScale(params) {
  return request({ url: '/system/scale/list', method: 'get', params })
}

export function addScale(data) {
  return request({ url: '/system/scale', method: 'post', data })
}

export function updateScale(data) {
  return request({ url: '/system/scale', method: 'put', data })
}

export function delScale(scaleId) {
  return request({ url: '/system/scale/' + scaleId, method: 'delete' })
}

// ===== 题目管理 =====
export function listQuestion(params) {
  return request({ url: '/system/question/list', method: 'get', params })
}

export function listQuestionByScale(scaleId) {
  return request({ url: '/system/question/listByScale', method: 'get', params: { scaleId } })
}

export function addQuestion(data) {
  return request({ url: '/system/question', method: 'post', data })
}

export function updateQuestion(data) {
  return request({ url: '/system/question', method: 'put', data })
}

export function delQuestion(questionId) {
  return request({ url: '/system/question/' + questionId, method: 'delete' })
}
