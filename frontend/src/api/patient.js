import request from '@/utils/request'

/** 学生列表 */
export function listPatient(params) {
  return request({ url: '/patient/list', method: 'get', params })
}

/** 新增学生 */
export function addPatient(data) {
  return request({ url: '/patient', method: 'post', data })
}

/** 修改学生 */
export function updatePatient(data) {
  return request({ url: '/patient', method: 'put', data })
}

/** 删除学生 */
export function delPatient(patientId) {
  return request({ url: '/patient/' + patientId, method: 'delete' })
}
