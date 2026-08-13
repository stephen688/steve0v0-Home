import axios, { AxiosError, type AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/auth'

/**
 * 统一 API 响应结构
 * 与后端 Result<T> 保持一致
 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

/**
 * 业务异常
 */
export class BusinessError extends Error {
  public readonly code: number

  constructor(code: number, message: string) {
    super(message)
    this.code = code
    this.name = 'BusinessError'
  }
}

const configuredApiBase = import.meta.env.VITE_API_BASE as string | undefined
const apiBase = configuredApiBase
  ? `${configuredApiBase.replace(/\/$/, '')}${configuredApiBase.replace(/\/$/, '').endsWith('/api') ? '' : '/api'}`
  : '/api'

// 创建 axios 实例，所有管理端请求以 /api 开头
const client = axios.create({
  baseURL: apiBase,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动携带 JWT
client.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

// 响应拦截器：统一处理业务码与 HTTP 错误
client.interceptors.response.use(
  (response: AxiosResponse<ApiResult<unknown>>) => {
    const result = response.data
    if (result.code !== 200) {
      throw new BusinessError(result.code, result.message || '请求失败')
    }
    return response
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message

      if (status === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        window.location.href = '/login'
        return Promise.reject(new BusinessError(401, '登录已过期，请重新登录'))
      }

      if (status === 429) {
        return Promise.reject(new BusinessError(429, message || '尝试次数过多，请稍后再试'))
      }

      return Promise.reject(new BusinessError(status, message || `请求失败 (${status})`))
    }

    if (error.request) {
      return Promise.reject(new BusinessError(-1, '网络异常，请检查后端服务是否启动'))
    }

    return Promise.reject(new BusinessError(-1, error.message || '未知错误'))
  }
)

export default client
