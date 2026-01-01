import { api } from './client'
import type { SysPermission } from '@/lib/types'

/**
 * 权限（菜单）管理 API
 */
export const permissionApi = {
  /**
   * 查询权限列表（树形）
   */
  list: (params?: { visible?: number; status?: number; permissionName?: string }): Promise<SysPermission[]> => {
    return api.get('/system/permission/list', { params })
  },
  
  /**
   * 获取权限详情
   */
  getById: (id: number): Promise<SysPermission> => {
    return api.get(`/system/permission/${id}`)
  },
  
  /**
   * 新增权限
   */
  add: (permission: SysPermission): Promise<void> => {
    return api.post('/system/permission', permission)
  },
  
  /**
   * 修改权限
   */
  update: (permission: SysPermission): Promise<void> => {
    return api.put('/system/permission', permission)
  },
  
  /**
   * 删除权限
   */
  remove: (id: number): Promise<void> => {
    return api.delete(`/system/permission/${id}`)
  },
  
  /**
   * 获取菜单树（用于前端路由）
   */
  getMenuTree: (): Promise<SysPermission[]> => {
    return api.get('/system/permission/menuTree')
  },
}

