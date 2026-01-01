/**
 * 业务错误类
 * 用于区分业务错误（code !== 200）和 HTTP 错误
 */
export class BusinessError extends Error {
  public readonly code: number
  public readonly message: string
  public readonly timestamp?: number

  constructor(code: number, message: string, timestamp?: number) {
    super(message)
    this.name = 'BusinessError'
    this.code = code
    this.message = message
    this.timestamp = timestamp
  }

  /**
   * 判断是否为权限不足错误
   */
  isForbidden(): boolean {
    return this.code === 403
  }

  /**
   * 判断是否为未授权错误
   */
  isUnauthorized(): boolean {
    return this.code === 401
  }
}

