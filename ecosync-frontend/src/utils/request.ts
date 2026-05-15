import axios from 'axios'
import { ElMessage } from '@/utils/message'
import router from '../router'

const request = axios.create({
  baseURL: '/api', // 这里必须和后端 RequestMapping 对齐
  timeout: 5000
})

let isRelogging = false

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response && error.response.status === 401 && router.currentRoute.value.name !== 'login') {
      localStorage.removeItem('token')
      if (!isRelogging) {
        isRelogging = true
        ElMessage.error('登录失效')
        router.push('/login').then(() => { isRelogging = false })
      }
    }
    return Promise.reject(error)
  }
)

export default request
