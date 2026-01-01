import z from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { SysUser } from '@/features/system/user'

const usersSearchSchema = z.object({
  page: z.number().optional().catch(1),
  pageSize: z.number().optional().catch(10),
  // 状态筛选：1正常 0停用
  status: z
    .array(z.union([z.literal(1), z.literal(0)]))
    .optional()
    .catch([]),
  // 文本筛选
  username: z.string().optional().catch(''),
  phone: z.string().optional().catch(''),
  // 列可见性：对象，键为列 ID，值为布尔值（false 表示隐藏）
  columnVisibility: z.record(z.string(), z.boolean()).optional().catch({}),
})

export const Route = createFileRoute('/_authenticated/system/user/')({
  validateSearch: usersSearchSchema,
  component: SysUser,
})
