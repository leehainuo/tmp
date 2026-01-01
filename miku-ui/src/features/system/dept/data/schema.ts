import { z } from 'zod'
import type { SysDept } from '@/lib/api'

// 部门状态：1正常 0停用
const _deptStatusSchema = z.union([z.literal(1), z.literal(0)])
export type DeptStatus = z.infer<typeof _deptStatusSchema>

// 使用后端 SysDept 类型
export type Dept = SysDept

// 部门表单 Schema
export const deptFormSchema = z.object({
  id: z.number().optional(),
  parentId: z.number().nullable().optional(),
  deptName: z.string().min(1, '部门名称不能为空'),
  orderNum: z.number().optional().nullable(),
  leader: z.string().optional().nullable(),
  phone: z
    .string()
    .regex(/^$|^1[3-9]\d{9}$/, '手机号格式不正确')
    .optional()
    .nullable(),
  email: z.string().email('邮箱格式不正确').optional().nullable(),
  status: z.union([z.literal(1), z.literal(0)]).default(1),
})

export type DeptForm = z.infer<typeof deptFormSchema>


