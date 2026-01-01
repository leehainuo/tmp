/* eslint-disable @typescript-eslint/no-explicit-any */
import { AxiosError } from 'axios'
import { toast } from 'sonner'
import { BusinessError } from '@/lib/errors'

export function handleServerError(error: unknown) {
  // eslint-disable-next-line no-console
  console.log(error)

  let errMsg = 'Something went wrong!'

  // 优先处理业务错误（BusinessError）
  if (error instanceof BusinessError) {
    errMsg = error.message || '请求失败'
    toast.error(errMsg)
    return
  }

  // 处理 HTTP 204 状态码
  if (
    error &&
    typeof error === 'object' &&
    'status' in error &&
    Number(error.status) === 204
  ) {
    errMsg = 'Content not found.'
    toast.error(errMsg)
    return
  }

  // 处理 Axios HTTP 错误
  if (error instanceof AxiosError) {
    // 尝试从 response.data 中获取错误消息
    const responseData = error.response?.data
    if (responseData && typeof responseData === 'object') {
      // 优先使用 message，其次使用 title
      errMsg = (responseData as any).message || (responseData as any).title || error.message
    } else {
      errMsg = error.message || '请求失败'
    }
    toast.error(errMsg)
    return
  }

  // 处理普通 Error
  if (error instanceof Error) {
    errMsg = error.message
    toast.error(errMsg)
    return
  }

  // 其他未知错误
  toast.error(errMsg)
}
