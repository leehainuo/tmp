import { z } from 'zod'
import type { SysPermission } from '@/lib/api'

// 后端权限状态：1正常 0停用
const _permissionStatusSchema = z.union([
  z.literal(1),
  z.literal(0),
])

export type PermissionStatus = z.infer<typeof _permissionStatusSchema>

// 权限类型：1=目录 2=菜单 3=按钮 4=接口
const _permissionTypeSchema = z.union([
  z.literal(1),
  z.literal(2),
  z.literal(3),
  z.literal(4),
])

export type PermissionType = z.infer<typeof _permissionTypeSchema>

// 使用后端 SysPermission 类型
export type Permission = SysPermission

// 权限表单 Schema
export const permissionFormSchema = z.object({
  id: z.number().optional(),
  parentId: z.number().nullable().optional(),
  permissionName: z.string().min(1, '权限名称不能为空'),
  permissionType: z.union([z.literal(1), z.literal(2), z.literal(3), z.literal(4)]),
  perms: z.string().optional().nullable(),
  path: z.string().optional().nullable(),
  component: z.string().optional().nullable(),
  icon: z.string().optional().nullable(),
  orderNum: z.number().optional().nullable(),
  visible: z.union([z.literal(1), z.literal(0)]).optional().nullable(),
  status: z.union([z.literal(1), z.literal(0)]).default(1),
  apiMethod: z.string().optional().nullable(),
  apiPath: z.string().optional().nullable(),
  remark: z.string().optional().nullable(),
})

export type PermissionForm = z.infer<typeof permissionFormSchema>

