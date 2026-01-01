import axios, { type AxiosInstance, type AxiosError } from 'axios'
import { useAuthStore } from '@/lib/stores/auth-store'
import type { ApiResult, LoginResponse } from '@/lib/types'
import { BusinessError } from '@/lib/errors'

/**
 * 创建 Axios 实例
 */
export const api: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 刷新 Token 的 Promise（用于防止并发刷新）
let refreshTokenPromise: Promise<LoginResponse> | null = null

/**
 * 刷新 Access Token
 */
async function refreshAccessToken(): Promise<LoginResponse> {
  const { auth } = useAuthStore.getState()
  
  if (!auth.refreshToken) {
    throw new Error('没有刷新令牌')
  }
  
  try {
    // 创建一个新的 axios 实例用于刷新（不使用拦截器，避免循环）
    const refreshApi = axios.create({
      baseURL: '/api',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    })
    
    // 调用刷新接口
    const response = await refreshApi.post<ApiResult<LoginResponse>>(
      '/auth/refresh',
      { refreshToken: auth.refreshToken }
    )
    
    const result = response.data
    if (result.code !== 200) {
      throw new Error(result.message || '刷新令牌失败')
    }
    
    const tokenData = result.data
    // 更新 token
    auth.setTokens(tokenData.accessToken, tokenData.refreshToken)
    
    return tokenData
  } catch (error) {
    // 刷新失败，清除所有认证信息
    auth.reset()
    throw error
  }
}

/**
 * 请求拦截器 - 添加 token
 */
api.interceptors.request.use(
  (config) => {
    const { auth } = useAuthStore.getState()
    if (auth.accessToken) {
      config.headers.Authorization = `Bearer ${auth.accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器 - 处理统一返回格式和错误
 */
api.interceptors.response.use(
  (response) => {
    const result: ApiResult = response.data
    // 如果 code 不是 200，抛出 BusinessError（会在错误回调中统一处理）
    if (result.code !== 200) {
      // 创建一个包含原始请求配置的错误，以便在错误回调中重试
      const error = new BusinessError(result.code, result.message || '请求失败', result.timestamp) as BusinessError & { config?: typeof response.config; response?: typeof response }
      error.config = response.config
      error.response = response
      return Promise.reject(error)
    }
    return result.data
  },
  async (error: AxiosError<ApiResult> | (BusinessError & { config?: AxiosError<ApiResult>['config']; response?: AxiosError<ApiResult>['response'] })) => {
    const originalRequest = (error as AxiosError<ApiResult> | (BusinessError & { config?: AxiosError<ApiResult>['config'] })).config
    
    // 检查是否是 401 错误（业务 code 401 或 HTTP 401）
    const is401Error = 
      (error instanceof BusinessError && error.isUnauthorized()) ||
      ((error as AxiosError).response?.status === 401)
    
    // 如果是 401 错误，尝试刷新 token
    if (
      is401Error &&
        originalRequest &&
        !originalRequest.url?.includes('/auth/refresh')
      ) {
      const { auth } = useAuthStore.getState()
      
      // 如果有 refreshToken，尝试刷新
      if (auth.refreshToken) {
        // 避免重复刷新（如果已经在刷新中，等待刷新完成）
        if (!refreshTokenPromise) {
          refreshTokenPromise = refreshAccessToken()
        }
        
        try {
          // 等待刷新完成
          await refreshTokenPromise
          
          // 刷新成功，使用新的 token 重试原始请求
          const { auth: newAuth } = useAuthStore.getState()
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${newAuth.accessToken}`
          }
          
          // 清除刷新 Promise，允许下次刷新
          refreshTokenPromise = null
          
          // 重试原始请求
          return api(originalRequest)
        } catch (_refreshError) {
          // 刷新失败，清除 Promise
          refreshTokenPromise = null
          const { auth: failedAuth } = useAuthStore.getState()
          failedAuth.reset()
          
          // 如果是 BusinessError，保持 BusinessError 类型
          if (error instanceof BusinessError) {
            return Promise.reject(error)
          }
          
          // 否则抛出 BusinessError
          const axiosError = error as AxiosError<ApiResult>
          const result = axiosError.response?.data
          const apiResult = result && typeof result === 'object' && 'code' in result 
            ? (result as ApiResult)
            : null
          
          return Promise.reject(
            new BusinessError(
              apiResult?.code || 401,
              apiResult?.message || '刷新令牌失败',
              apiResult?.timestamp
            )
          )
        }
      }
    }
    
    // 如果不是 401 错误，或者是 401 但没有 refreshToken，按原逻辑处理
    if (error instanceof BusinessError) {
      return Promise.reject(error)
    }
    
    const axiosError = error as AxiosError<ApiResult>
    
    // 处理 HTTP 错误
    if (axiosError.response) {
      const result = axiosError.response.data
      
      // 如果响应体包含业务 code（统一返回格式）
      if (result && typeof result === 'object' && 'code' in result) {
        const apiResult = result as ApiResult
        if (apiResult.code !== 200) {
          return Promise.reject(
            new BusinessError(apiResult.code, apiResult.message || '请求失败', apiResult.timestamp)
          )
        }
      }
      
      const message = result?.message || axiosError.message || '请求失败'
      return Promise.reject(new Error(message))
    }
    
    // 网络错误或其他错误
    return Promise.reject(axiosError)
  }
)

export default api

