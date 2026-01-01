import { api } from './client'
import type { SysRole, RoleQueryRequest, PageResult } from '@/lib/types'

/**
 * 角色管理 API
 */
export const roleApi = {
  /**
   * 查询角色列表（分页）
   */
  list: (params?: RoleQueryRequest): Promise<PageResult<SysRole>> => {
    return api.get('/system/role/list', { params })
  },
  
  /**
   * 获取所有角色（用于下拉选择）
   */
  getAll: async (): Promise<SysRole[]> => {
    const result = await api.get('/system/role/list', {
      params: { current: 1, size: 1000 },
    }) as PageResult<SysRole>
    return result.records || []
  },
  
  /**
   * 获取角色详情
   */
  getById: (id: number): Promise<SysRole> => {
    return api.get(`/system/role/${id}`)
  },
  
  /**
   * 新增角色
   */
  add: (role: SysRole): Promise<void> => {
    return api.post('/system/role', role)
  },
  
  /**
   * 修改角色
   */
  update: (role: SysRole): Promise<void> => {
    return api.put('/system/role', role)
  },
  
  /**
   * 删除角色
   */
  remove: (id: number): Promise<void> => {
    return api.delete(`/system/role/${id}`)
  },
  
  /**
   * 分配权限
   */
  assignPermissions: (roleId: number, permissionIds: number[]): Promise<void> => {
    return api.put(`/system/role/assignPermissions/${roleId}`, permissionIds)
  },
  
  /**
   * 分配数据权限
   */
  assignDataScope: (roleId: number, dataScope: number, deptIds?: number[]): Promise<void> => {
    return api.put(`/system/role/assignDataScope/${roleId}`, deptIds || [], {
      params: { dataScope }
    })
  },
}

