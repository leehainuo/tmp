/* eslint-disable @typescript-eslint/no-explicit-any */
/**
 * API 通用类型定义
 */

/**
 * 后端统一返回结果类型
 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 分页结果类型
 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  accessTokenExpiresIn: number
  refreshTokenExpiresIn: number
  tokenType?: string
}

/**
 * 用户基本信息（精简版，用于存储）
 */
export interface UserBasicInfo {
  userId: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  deptId?: number
  deptName?: string
  roles: string[]
}

/**
 * 后端菜单项类型
 */
export interface BackendMenu {
  id: number
  parentId: number | null
  permissionName: string
  permissionType: number // 1=目录 2=菜单 3=按钮 4=接口
  path?: string | null
  component?: string | null
  icon?: string | null
  orderNum?: number | null
  visible?: number | null
  status?: number | null
  children?: BackendMenu[]
}

/**
 * 系统权限类型（菜单+按钮+接口）
 */
export interface SysPermission {
  id?: number
  parentId?: number | null
  permissionName: string
  permissionType: number // 1=目录 2=菜单 3=按钮 4=接口
  perms?: string | null
  path?: string | null
  component?: string | null
  icon?: string | null
  orderNum?: number | null
  visible?: number | null
  status?: number | null
  apiMethod?: string | null
  apiPath?: string | null
  remark?: string | null
  children?: SysPermission[]
}

/**
 * 用户完整信息响应（包含菜单和权限）
 */
export interface UserInfoResponse extends UserBasicInfo {
  permissions: string[]
  menus: BackendMenu[]
}

/**
 * 系统用户类型
 */
export interface SysUser {
  id?: number
  userId?: number
  username: string
  nickname: string
  password?: string
  deptId?: number
  deptName?: string
  email?: string
  phone?: string
  sex?: number // 0未知 1男 2女
  avatar?: string
  status: number // 1正常 0停用
  loginIp?: string
  loginTime?: string
  roles?: Array<{ id: number; roleName: string; roleCode: string }>
  roleIds?: number[]
  dept?: { id: number; deptName: string }
}

/**
 * 用户查询请求
 */
export interface UserQueryRequest {
  current?: number
  size?: number
  username?: string
  phone?: string
  status?: number
}

/**
 * 系统角色类型
 */
export interface SysRole {
  id?: number
  roleName: string
  roleCode: string
  roleSort?: number
  dataScope?: number
  status?: number
  remark?: string
  permissionIds?: number[]
  deptIds?: number[]
}

/**
 * 角色查询请求
 */
export interface RoleQueryRequest {
  current?: number
  size?: number
  roleName?: string
  status?: number
}

/**
 * 系统部门类型
 */
export interface SysDept {
  id?: number
  parentId?: number | null
  ancestors?: string
  deptName: string
  orderNum?: number
  leader?: string
  phone?: string
  email?: string
  status?: number
  delFlag?: number
  children?: SysDept[]
}

/**
 * 部门查询请求
 */
export interface DeptQueryRequest {
  deptName?: string
  status?: number
}

