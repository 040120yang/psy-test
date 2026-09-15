import request from '@/utils/request'

/** 与扣子 AI 智能体对话 */
export function chatWithCoze(data) {
  return request({ url: '/coze/chat', method: 'post', data })
}
