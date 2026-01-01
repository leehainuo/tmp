/**
 * API 模块统一导出
 * 
 * 使用方式：
 * import { api, authApi, userApi } from '@/lib/api'
 * import type { SysUser, PageResult } from '@/lib/api'
 */

// 导出 Axios 实例
export { api, default } from './client'

// 导出所有类型（从 types 目录重新导出）
export type * from '@/lib/types'

// 导出所有 API
export { authApi } from './auth.api'
export { userApi } from './user.api'
export { roleApi } from './role.api'
export { deptApi } from './dept.api'
export { permissionApi } from './permission.api'
export { logApi } from './log.api'


