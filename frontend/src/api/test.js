import request from '@/utils/request'

/** 获取全部启用量表 */
export function listEnabledScales() {
  return request({ url: '/test/scales', method: 'get' })
}

/** 获取量表题目 */
export function getQuestions(scaleId) {
  return request({ url: '/test/questions', method: 'get', params: { scaleId } })
}

/** 提交答卷 */
export function submitTest(data) {
  return request({ url: '/test/submit', method: 'post', data })
}

/** 我的测评记录 */
export function listMyRecords(params) {
  return request({ url: '/test/record/my', method: 'get', params })
}

/** 全部测评记录（管理端） */
export function listAllRecords(params) {
  return request({ url: '/test/record/list', method: 'get', params })
}

/** 记录详情 */
export function getRecordDetail(recordId) {
  return request({ url: '/test/record/' + recordId, method: 'get' })
}

/** 根据当前用户有权访问的测评记录生成 AI 辅助解读 */
export function generateReportInterpretation(recordId) {
  return request({
    url: '/coze/report/' + recordId,
    method: 'post'
  })
}

/** 删除记录 */
export function delRecord(recordId) {
  return request({ url: '/test/record/' + recordId, method: 'delete' })
}
