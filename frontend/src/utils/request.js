import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken, removeUser } from '@/utils/auth'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/dev-api',
  timeout: 30000
})

service.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}, error => Promise.reject(error))

service.interceptors.response.use(response => {
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
