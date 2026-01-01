/**
 * Cookie utility functions using manual document.cookie approach
 * Replaces js-cookie dependency for better consistency
 */

const DEFAULT_MAX_AGE = 60 * 60 * 24 * 7 // 7 days

/**
 * Get a cookie value by name
 */
export function getCookie(name: string): string | undefined {
  if (typeof document === 'undefined') return undefined

  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) {
    const cookieValue = parts.pop()?.split(';').shift()
    return cookieValue
  }
  return undefined
}

/**
 * Set a cookie with name, value, and optional max age
 * Note: Cookie values should be kept small (under 4KB)
 */
export function setCookie(
  name: string,
  value: string,
  maxAge: number = DEFAULT_MAX_AGE
): void {
  if (typeof document === 'undefined') return

  // Cookie 值不能超过 4KB，如果超过则使用 localStorage
  if (value.length > 4000) {
    try {
      localStorage.setItem(name, value)
      return
    } catch (_e) {
      return
    }
  }

  try {
    document.cookie = `${name}=${value}; path=/; max-age=${maxAge}`
  } catch (_e) {
    // 如果设置 cookie 失败，尝试使用 localStorage
    try {
      localStorage.setItem(name, value)
    } catch {
      // 忽略错误
    }
  }
}

/**
 * Remove a cookie by setting its max age to 0
 */
export function removeCookie(name: string): void {
  if (typeof document === 'undefined') return

  document.cookie = `${name}=; path=/; max-age=0`
  // 同时尝试从 localStorage 中删除
  try {
    localStorage.removeItem(name)
  } catch (e) {
    // 忽略 localStorage 错误
  }
}
