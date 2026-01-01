import { api } from './client'
import type { SysDept, DeptQueryRequest } from '@/lib/types'

/**
 * 部门管理 API
 */
export const deptApi = {
  /**
   * 查询部门列表（分页）
   */
  list: (params?: DeptQueryRequest) => {
    return api.get('/system/dept/list', { params })
  },
  
  /**
   * 查询部门树
   */
  getTree: (): Promise<SysDept[]> => {
    return api.get('/system/dept/tree')
  },
  
  /**
   * 查询部门列表（树形）
   */
  listTree: (params?: DeptQueryRequest): Promise<SysDept[]> => {
    return api.get('/system/dept/list-tree', { params })
  },
  
  /**
   * 获取部门详情
   */
  getById: (id: number): Promise<SysDept> => {
    return api.get(`/system/dept/${id}`)
  },
  
  /**
   * 新增部门
   */
  add: (dept: SysDept): Promise<void> => {
    return api.post('/system/dept', dept)
  },
  
  /**
   * 修改部门
   */
  update: (dept: SysDept): Promise<void> => {
    return api.put('/system/dept', dept)
  },
  
  /**
   * 删除部门
   */
  remove: (id: number): Promise<void> => {
    return api.delete(`/system/dept/${id}`)
  },
}

