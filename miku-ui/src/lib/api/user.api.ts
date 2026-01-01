/* eslint-disable @typescript-eslint/no-explicit-any */
import { api } from './client'
import { authApi } from './auth.api'
import type { SysUser, UserQueryRequest, PageResult, UserInfoResponse } from '@/lib/types'

/**
 * 用户管理 API
 */
export const userApi = {
  /**
   * 查询用户列表（分页）
   */
  list: (params: UserQueryRequest): Promise<PageResult<SysUser>> => {
    return api.get('/system/user/list', { params })
  },
  
  /**
   * 获取用户详情
   */
  getById: (id: number): Promise<SysUser> => {
    return api.get(`/system/user/${id}`)
  },
  
  /**
   * 新增用户
   */
  add: (user: SysUser): Promise<void> => {
    return api.post('/system/user', user)
  },
  
  /**
   * 修改用户
   *
   * 前端和后端字段名在项目中存在 `userId`（前端缓存）与 `id`（后端实体）混用的情况。
   * 为提高健壮性：在发送到后端之前，如果缺少 `id` 则尝试使用 `userId` 作为 `id`。
   */
  update: (user: Partial<SysUser>): Promise<void> => {
    type _Payload = Partial<SysUser> & { userId?: number; id?: number }
    const payload = { ...(user as _Payload) } as _Payload
    if (payload.id == null && payload.userId != null) {
      // 兼容后端以 id 为主键的期望
      payload.id = payload.userId
    }

    return api.put('/system/user', payload)
  },
  
  /**
   * 删除用户
   */
  remove: (id: number): Promise<void> => {
    return api.delete(`/system/user/${id}`)
  },
  
  /**
   * 重置密码
   */
  resetPassword: (id: number, newPassword: string): Promise<void> => {
    return api.put(`/system/user/resetPassword/${id}`, null, {
      params: { newPassword },
    })
  },
  /**
   * 获取当前登录用户信息（包含菜单和权限）
   */
  getProfile: (): Promise<UserInfoResponse> => {
    return authApi.getUserInfo()
  },

  /**
   * 更新当前用户资料（nickname/avatar 等）
   */
  updateProfile: (user: Partial<SysUser>): Promise<void> => {
    return userApi.update(user as SysUser)
  },

  /**
   * 修改当前用户密码（使用 resetPassword 接口）
   */
  changePassword: (userId: number, newPassword: string): Promise<void> => {
    return userApi.resetPassword(userId, newPassword)
  },
  /**
   * 上传并设置当前用户头像（单次请求完成上传与更新）
   */
  uploadAvatar: (file: File): Promise<any> => {
    const fd = new FormData()
    fd.append('file', file)
    return api.post('/system/user/avatar', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

