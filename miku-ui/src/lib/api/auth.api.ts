import { api } from './client'
import type { LoginRequest, LoginResponse, UserInfoResponse } from '@/lib/types'

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login: (data: LoginRequest): Promise<LoginResponse> => {
    return api.post('/auth/login', data)
  },
  
  /**
   * 获取用户信息（包含菜单和权限）
   */
  getUserInfo: (): Promise<UserInfoResponse> => {
    return api.get('/auth/info')
  },
  
  /**
   * 刷新 token
   */
  refreshToken: (refreshToken: string): Promise<LoginResponse> => {
    return api.post('/auth/refresh', { refreshToken })
  },
  
  /**
   * 用户登出
   */
  logout: (): Promise<void> => {
    return api.post('/auth/logout')
  },
}

