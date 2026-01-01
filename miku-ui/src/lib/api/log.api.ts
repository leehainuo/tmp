import { api } from './client'
import type { PageResult } from '@/lib/types'

export interface SysOperationLog {
  id?: number
  title?: string
  businessType?: number
  method?: string
  requestMethod?: string
  operatorType?: number
  operName?: string
  deptName?: string
  operUrl?: string
  operIp?: string
  operLocation?: string
  operParam?: string
  jsonResult?: string
  status?: number
  errorMsg?: string
  operTime?: string
}

export interface OperationLogQueryRequest {
  current?: number
  size?: number
  operName?: string
  businessType?: number
  status?: number
  startTime?: string
  endTime?: string
  title?: string
}

export interface LogCleanRequest {
  /** 开始日期：yyyy-MM-dd */
  startDate?: string
  /** 结束日期：yyyy-MM-dd */
  endDate?: string
  /** 保留最近几个月（当未指定日期范围时生效） */
  months?: number
}

export const logApi = {
  /**
   * 查询操作日志列表（分页）
   */
  list: (params: OperationLogQueryRequest): Promise<PageResult<SysOperationLog>> => {
    return api.get('/system/log/list', { params })
  },

  /**
   * 按时间范围或月份策略清理操作日志
   */
  clean: (params: LogCleanRequest): Promise<number> => {
    return api.delete('/system/log/clean', { params })
  },

  /**
   * 删除单条操作日志
   */
  remove: (id: number): Promise<void> => {
    return api.delete(`/system/log/${id}`)
  },

  /**
   * 批量删除操作日志
   */
  batchRemove: (ids: number[]): Promise<void> => {
    return api.delete('/system/log/batch', { data: ids })
  },
}


