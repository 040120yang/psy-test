import request from '@/utils/request'

/** 系统概览统计 */
export function getStats() {
  return request({ url: '/dashboard/stats', method: 'get' })
}
