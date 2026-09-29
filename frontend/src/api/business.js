import request from '@/utils/request'
export function listMessages(params) { return request({ url:'/business/message/my', method:'get', params }) }
export function markMessageRead(id) { return request({ url:`/business/message/read/${id}`, method:'put' }) }
export function getUnreadCount() { return request({ url:'/business/message/unread', method:'get' }) }
export function sendMessage(data) { return request({ url:'/business/message/send', method:'post', data }) }
export function listKnowledge(params) { return request({ url:'/business/knowledge/list', method:'get', params }) }
export function getKnowledge(id) { return request({ url:`/business/knowledge/${id}`, method:'get' }) }
export function addKnowledge(data) { return request({ url:'/business/knowledge', method:'post', data }) }
export function updateKnowledge(data) { return request({ url:'/business/knowledge', method:'put', data }) }
export function delKnowledge(id) { return request({ url:`/business/knowledge/${id}`, method:'delete' }) }
export function saveDiagnosis(data) { return request({ url:'/business/record/diagnosis', method:'put', data }) }
export function compareHistory() { return request({ url:'/business/record/compare', method:'get' }) }
export function listOperLogs(params) { return request({ url:'/business/log/oper', method:'get', params }) }
export function listLoginLogs(params) { return request({ url:'/business/log/login', method:'get', params }) }
