import { z } from 'zod'
import type { SysRole } from '@/lib/api'

// 后端角色状态：1正常 0停用
const _roleStatusSchema = z.union([
  z.literal(1),
  z.literal(0),
])

export type RoleStatus = z.infer<typeof _roleStatusSchema>

// 使用后端 SysRole 类型
export type Role = SysRole

// 数据权限范围枚举
export const DATA_SCOPE = {
  ALL: 1,           // 全部数据权限
  CUSTOM: 2,        // 自定义数据权限
  DEPT: 3,          // 本部门数据权限
  DEPT_AND_CHILD: 4, // 本部门及以下数据权限
  SELF: 5,          // 仅本人数据权限
} as const

// 角色表单 Schema
export const roleFormSchema = z.object({
  id: z.number().optional(),
  roleName: z.string().min(1, '角色名称不能为空'),
  roleCode: z.string().min(1, '角色编码不能为空'),
  roleSort: z.number().optional(),
  status: z.union([z.literal(1), z.literal(0)]).default(1),
  remark: z.string().optional(),
  permissionIds: z.array(z.number()).optional(),
  dataScope: z.union([
    z.literal(1), // 全部数据权限
    z.literal(2), // 自定义数据权限
    z.literal(3), // 本部门数据权限
    z.literal(4), // 本部门及以下数据权限
    z.literal(5), // 仅本人数据权限
  ]).optional(),
  deptIds: z.array(z.number()).optional(),
})

export type RoleForm = z.infer<typeof roleFormSchema>

