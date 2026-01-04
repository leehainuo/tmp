import { z } from 'zod'
import type { SysOperationLog } from '@/lib/api/log.api'

// 使用后端 SysOperationLog 类型
export type Log = SysOperationLog

// 日志查询表单（如后续需要可以扩展）
export const logFilterSchema = z.object({
  title: z.string().optional(),
  operName: z.string().optional(),
})

export type LogFilter = z.infer<typeof logFilterSchema>

