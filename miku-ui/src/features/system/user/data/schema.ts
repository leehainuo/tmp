import { z } from 'zod'
import type { SysUser } from '@/lib/api'

// 后端用户状态：1正常 0停用
const _userStatusSchema = z.union([
  z.literal(1),
  z.literal(0),
])

export type UserStatus = z.infer<typeof _userStatusSchema>

// 使用后端 SysUser 类型
export type User = SysUser

// 用户表单 Schema
export const userFormSchema = z
  .object({
    id: z.number().optional(),
    username: z.string().min(1, '用户名不能为空'),
    nickname: z.string().min(1, '昵称不能为空'),
    email: z.string().email('邮箱格式不正确').optional().or(z.literal('')),
    phone: z.string().optional().or(z.literal('')),
    password: z.string().optional(),
    confirmPassword: z.string().optional(),
    deptId: z.number().optional(),
    roleIds: z.array(z.number()).optional(),
    status: z.union([z.literal(1), z.literal(0)]).default(1),
    sex: z.union([z.literal(0), z.literal(1), z.literal(2)]).optional(),
    isEdit: z.boolean(),
  })
  .refine(
    (data) => {
      if (data.isEdit && !data.password) return true
      return data.password && data.password.length > 0
    },
    {
      message: '密码不能为空',
      path: ['password'],
    }
  )
  .refine(
    ({ isEdit, password }) => {
      if (isEdit && !password) return true
      return password ? password.length >= 6 : true
    },
    {
      message: '密码长度至少6位',
      path: ['password'],
    }
  )
  .refine(
    ({ isEdit, password, confirmPassword }) => {
      if (isEdit && !password) return true
      return password === confirmPassword
    },
    {
      message: '两次输入的密码不一致',
      path: ['confirmPassword'],
    }
  )

export type UserForm = z.infer<typeof userFormSchema>

