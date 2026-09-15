import request from '@/utils/request'

/** 患者列表 */
export function listPatient(params) {
  return request({ url: '/patient/list', method: 'get', params })
}

/** 新增患者 */
export function addPatient(data) {
  return request({ url: '/patient', method: 'post', data })
}

/** 修改患者 */
export function updatePatient(data) {
  return request({ url: '/patient', method: 'put', data })
}

/** 删除患者 */
export function delPatient(patientId) {
  return request({ url: '/patient/' + patientId, method: 'delete' })
}
