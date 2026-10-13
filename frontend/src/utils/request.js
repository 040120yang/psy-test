import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken, removeUser } from '@/utils/auth'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/dev-api',
  timeout: 120000
})

service.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}, error => Promise.reject(error))

service.interceptors.response.use(async response => {
  // 文件下载等二进制响应：直接返回 Blob，跳过业务状态码校验
  if (response.config && response.config.responseType === 'blob') {
    const data = response.data
    const isJsonBlob = typeof Blob !== 'undefined' && data instanceof Blob
      && data.type && data.type.indexOf('application/json') !== -1
    // 后端以 JSON 返回了错误（blob 内容为 JSON）时，解析并提示
    if (isJsonBlob) {
      try {
        const payload = JSON.parse(await data.text())
        ElMessage.error(payload.msg || '请求失败')
        return Promise.reject(new Error(payload.msg || '请求失败'))
      } catch (e) {
        // 非 JSON 或解析失败：按二进制原样返回
      }
    }
    return data
  }

  const res = response.data
  if (res.code === 200) return res
  if (res.code === 401) {
    ElMessage.error('登录状态已失效，请重新登录')
    removeToken(); removeUser()
    router.push('/login')
    return Promise.reject(new Error(res.msg || '未登录'))
  }
  ElMessage.error(res.msg || '请求失败')
  return Promise.reject(new Error(res.msg || '请求失败'))
}, error => {
  const status = error.response?.status
  if (status === 401) {
    ElMessage.error('登录状态已失效，请重新登录')
    removeToken(); removeUser()
    router.push('/login')
  } else {
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常，请稍后重试')
  }
  return Promise.reject(error)
})

export default service
