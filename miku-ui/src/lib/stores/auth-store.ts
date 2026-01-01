import { create } from 'zustand'
import { getCookie, setCookie, removeCookie } from '@/lib/cookies'
import type { UserInfoResponse, UserBasicInfo } from '@/lib/api'

const ACCESS_TOKEN = 'access_token'
const REFRESH_TOKEN = 'refresh_token'
const USER_INFO = 'user_info' // 只存储基本信息
const USER_PERMISSIONS = 'user_permissions' // 权限信息（可选缓存）
const USER_MENUS = 'user_menus' // 菜单信息（可选缓存）

// localStorage 工具函数
const storage = {
  get: (key: string): string | null => {
    if (typeof window === 'undefined') return null
    try {
      return localStorage.getItem(key)
    } catch {
      return null
    }
  },
  set: (key: string, value: string): void => {
    if (typeof window === 'undefined') return
    try {
      localStorage.setItem(key, value)
    } catch {
      // 如果存储失败（比如存储空间不足），静默失败
    }
  },
  remove: (key: string): void => {
    if (typeof window === 'undefined') return
    try {
      localStorage.removeItem(key)
    } catch {
      // 忽略错误
    }
  },
  clear: (): void => {
    if (typeof window === 'undefined') return
    try {
      localStorage.removeItem(USER_INFO)
      localStorage.removeItem(USER_PERMISSIONS)
      localStorage.removeItem(USER_MENUS)
    } catch {
      // 忽略错误
    }
  },
}

interface AuthState {
  auth: {
    // 用户基本信息（始终可用）
    user: UserBasicInfo | null
    // 完整用户信息（包含菜单和权限，按需加载）
    fullUserInfo: UserInfoResponse | null
    // 设置用户信息（自动分离存储）
    setUser: (user: UserInfoResponse | null) => void
    // 获取完整用户信息（带缓存）
    getFullUserInfo: () => Promise<UserInfoResponse | null>
    // Token
    accessToken: string
    refreshToken: string
    setTokens: (accessToken: string, refreshToken: string) => void
    resetAccessToken: () => void
    reset: () => void
  }
}

export const useAuthStore = create<AuthState>()((set, get) => {
  // 初始化：从存储中恢复数据
  const cookieToken = getCookie(ACCESS_TOKEN)
  const cookieRefreshToken = getCookie(REFRESH_TOKEN)
  const storedBasicInfo = storage.get(USER_INFO)
  
  const initToken = cookieToken || ''
  const initRefreshToken = cookieRefreshToken || ''
  let initUser: UserBasicInfo | null = null
  let initFullUserInfo: UserInfoResponse | null = null
  
  if (storedBasicInfo) {
    try {
      initUser = JSON.parse(storedBasicInfo)
      
      // 尝试恢复完整用户信息（包括菜单和权限）
      const cachedPermissions = storage.get(USER_PERMISSIONS)
      const cachedMenus = storage.get(USER_MENUS)
      
      if (cachedPermissions && cachedMenus && initUser) {
        try {
          initFullUserInfo = {
            ...initUser,
            permissions: JSON.parse(cachedPermissions),
            menus: JSON.parse(cachedMenus),
          } as UserInfoResponse
        } catch {
          // 如果解析失败，清除缓存
          storage.remove(USER_PERMISSIONS)
          storage.remove(USER_MENUS)
        }
      }
    } catch {
      storage.remove(USER_INFO)
    }
  }
  
  return {
    auth: {
      user: initUser,
      fullUserInfo: initFullUserInfo, // 从缓存恢复完整信息
      
      // 设置用户信息：分离存储基本信息和大数据
      setUser: (user) => {
        if (user) {
          // 提取基本信息（小数据，快速访问）
          const basicInfo: UserBasicInfo = {
            userId: user.userId,
            username: user.username,
            nickname: user.nickname,
            avatar: user.avatar,
            email: user.email,
            phone: user.phone,
            deptId: user.deptId,
            deptName: user.deptName,
            roles: user.roles,
          }
          
          // 存储基本信息到 localStorage
          storage.set(USER_INFO, JSON.stringify(basicInfo))
          
          // 可选：缓存权限和菜单（如果数据不是特别大）
          if (user.permissions && user.permissions.length > 0) {
            storage.set(USER_PERMISSIONS, JSON.stringify(user.permissions))
          }
          if (user.menus && user.menus.length > 0) {
            // 菜单数据可能很大，可以选择不缓存
            // 或者只缓存菜单结构，不缓存完整数据
            try {
              storage.set(USER_MENUS, JSON.stringify(user.menus))
            } catch {
              // 如果菜单数据太大，不缓存
            }
          }
          
          set((state) => ({
            ...state,
            auth: {
              ...state.auth,
              user: basicInfo,
              fullUserInfo: user, // 同时保存完整信息到内存
            },
          }))
        } else {
          // 清除所有存储
          storage.clear()
          set((state) => ({
            ...state,
            auth: {
              ...state.auth,
              user: null,
              fullUserInfo: null,
            },
          }))
        }
      },
      
      // 获取完整用户信息（带缓存）
      getFullUserInfo: async () => {
        const state = get()
        
        // 如果内存中已有完整信息，直接返回
        if (state.auth.fullUserInfo) {
          return state.auth.fullUserInfo
        }
        
        // 尝试从缓存中恢复
        const cachedPermissions = storage.get(USER_PERMISSIONS)
        const cachedMenus = storage.get(USER_MENUS)
        
        if (cachedPermissions && cachedMenus && state.auth.user) {
          const fullInfo: UserInfoResponse = {
            ...state.auth.user,
            permissions: JSON.parse(cachedPermissions),
            menus: JSON.parse(cachedMenus),
          }
          set((s) => ({
            ...s,
            auth: { ...s.auth, fullUserInfo: fullInfo },
          }))
          return fullInfo
        }
        
        // 如果缓存中没有，需要从后端获取（这里不实现，由调用方决定）
        return null
      },
      
      accessToken: initToken,
      refreshToken: initRefreshToken,
      
      setTokens: (accessToken, refreshToken) =>
        set((state) => {
          // Token 存储在 cookie 中（支持自动发送）
          setCookie(ACCESS_TOKEN, accessToken)
          setCookie(REFRESH_TOKEN, refreshToken)
          return {
            ...state,
            auth: {
              ...state.auth,
              accessToken,
              refreshToken,
            },
          }
        }),
      
      resetAccessToken: () =>
        set((state) => {
          removeCookie(ACCESS_TOKEN)
          removeCookie(REFRESH_TOKEN)
          return {
            ...state,
            auth: {
              ...state.auth,
              accessToken: '',
              refreshToken: '',
            },
          }
        }),
      
      reset: () =>
        set((state) => {
          removeCookie(ACCESS_TOKEN)
          removeCookie(REFRESH_TOKEN)
          storage.clear()
          return {
            ...state,
            auth: {
              ...state.auth,
              user: null,
              fullUserInfo: null,
              accessToken: '',
              refreshToken: '',
            },
          }
        }),
    },
  }
})
