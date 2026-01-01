/**
 * 清理搜索参数中的默认值，避免在 URL 中显示默认参数
 * 参考 use-table-url-state.ts 中的逻辑
 */
export function cleanSearchParams<T extends Record<string, unknown>>(
  params: T,
  defaults: {
    page?: number
    pageSize?: number
    [key: string]: unknown
  } = {}
): Partial<T> {
  const cleaned: Partial<T> = { ...params }
  const defaultPage = defaults.page ?? 1
  const defaultPageSize = defaults.pageSize ?? 10

  // 清理 page 参数：如果等于默认值，则设为 undefined
  if ('page' in cleaned) {
    const page = cleaned.page as number | undefined
    if (page !== undefined && page <= defaultPage) {
      delete cleaned.page
    }
  }

  // 清理 pageSize 参数：如果等于默认值，则设为 undefined
  if ('pageSize' in cleaned) {
    const pageSize = cleaned.pageSize as number | undefined
    if (pageSize !== undefined && pageSize === defaultPageSize) {
      delete cleaned.pageSize
    }
  }

  // 清理空字符串和空数组
  Object.keys(cleaned).forEach((key) => {
    const value = cleaned[key]
    if (value === undefined || value === null || value === '') {
      delete cleaned[key]
    } else if (Array.isArray(value) && value.length === 0) {
      delete cleaned[key]
    }
  })

  return cleaned
}

