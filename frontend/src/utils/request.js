import axios from 'axios'
import { Message } from 'element-ui'
import { getToken, removeToken, removeUser } from '@/utils/auth'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 30000
})

// 请求拦截器：携带令牌
service.interceptors.request.use(config => {
  if (getToken()) {
    config.headers['Authorization'] = 'Bearer ' + getToken()
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器：统一处理业务码
service.interceptors.response.use(response => {
  const res = response.data
  // 若依风格：code 200 成功，其余失败
  if (res.code === 200) {
    return res
  }
  if (res.code === 401) {
    Message.error('登录状态已失效，请重新登录')
    removeToken()
    removeUser()
    router.push('/login')
    return Promise.reject(new Error(res.msg || '未登录'))
  }
  Message.error(res.msg || '请求失败')
  return Promise.reject(new Error(res.msg || 'Error'))
}, error => {
  const status = error.response && error.response.status
  if (status === 401) {
    Message.error('登录状态已失效，请重新登录')
    removeToken()
    removeUser()
    router.push('/login')
  } else {
    Message.error(error.message || '网络异常，请稍后重试')
  }
  return Promise.reject(error)
})

export default service
