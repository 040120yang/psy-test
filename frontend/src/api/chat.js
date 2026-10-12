import request from '@/utils/request'

/** 学生端：我的沟通会话（负责教师 + 消息列表） */
export function getMyChat(params) {
  return request({ url: '/chat/my', method: 'get', params })
}

/** 教师端：会话列表 */
export function listConversations() {
  return request({ url: '/chat/conversations', method: 'get' })
}

/** 教师端：指定学生的会话消息 */
export function getChatDetail(studentId) {
  return request({ url: '/chat/detail', method: 'get', params: { studentId } })
}

/** 发送消息（学生端与教师端共用） */
export function sendChat(data) {
  return request({ url: '/chat/send', method: 'post', data })
}

/** 当前用户未读沟通消息数 */
export function getChatUnread() {
  return request({ url: '/chat/unread', method: 'get' })
}

/** 标记已读（教师端需传 studentId） */
export function markChatRead(data) {
  return request({ url: '/chat/read', method: 'put', data })
}
