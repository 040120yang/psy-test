import request from '@/utils/request'

/** 系统概览统计 */
export function getStats() {
  return request({ url: '/dashboard/stats', method: 'get' })
}

/** 待办工作台：待处理沟通、逾期随访、待填写评估结论、本周新增中/重度测评 */
export function getWorkbench() {
  return request({ url: '/dashboard/workbench', method: 'get' })
}
