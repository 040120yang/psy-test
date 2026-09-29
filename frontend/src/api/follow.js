import request from '@/utils/request'
export function listMyFollows(params) { return request({ url:'/follow/my', method:'get', params }) }
export function listAdminFollows(params) { return request({ url:'/follow/admin/list', method:'get', params }) }
export function listOverdueFollows(params) { return request({ url:'/follow/overdue', method:'get', params }) }
export function getFollow(id) { return request({ url:`/follow/${id}`, method:'get' }) }
export function addFollow(data) { return request({ url:'/follow', method:'post', data }) }
export function updateFollow(data) { return request({ url:'/follow', method:'put', data }) }
export function finishFollow(data) { return request({ url:'/follow/finish', method:'put', data }) }
export function delFollow(id) { return request({ url:`/follow/${id}`, method:'delete' }) }
