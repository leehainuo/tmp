import { useMemo, useState } from 'react'
import type {
  ColumnFiltersState,
  OnChangeFn,
  PaginationState,
  VisibilityState,
} from '@tanstack/react-table'

type SearchRecord = Record<string, unknown>

/**
 * 单选状态筛选的序列化/反序列化配置（0/1 值）
 * 用于状态、可见性等二选一的筛选条件
 */
export const singleSelectStatusFilter = {
  serialize: (value: unknown) => {
    if (Array.isArray(value) && value.length > 0) {
      const numValue = typeof value[0] === 'string' ? Number(value[0]) : value[0]
      if (numValue === 0 || numValue === 1) {
        return [numValue]
      }
    }
    return undefined
  },
  deserialize: (value: unknown) => {
    if (Array.isArray(value) && value.length > 0) {
      return [String(value[0])]
    }
    return []
  },
}

export type NavigateFn = (opts: {
  search?:
    | true
    | SearchRecord
    | ((prev: SearchRecord) => Partial<SearchRecord> | SearchRecord)
  replace?: boolean
}) => void

type UseTableUrlStateParams = {
  search: SearchRecord
  navigate: NavigateFn
  pagination?: {
    pageKey?: string
    pageSizeKey?: string
    defaultPage?: number
    defaultPageSize?: number
  }
  globalFilter?: {
    enabled?: boolean
    key?: string
    trim?: boolean
  }
  columnFilters?: Array<
    | {
        columnId: string
        searchKey: string
        type?: 'string'
        // Optional transformers for custom types
        serialize?: (value: unknown) => unknown
        deserialize?: (value: unknown) => unknown
      }
    | {
        columnId: string
        searchKey: string
        type: 'array'
        serialize?: (value: unknown) => unknown
        deserialize?: (value: unknown) => unknown
      }
  >
  columnVisibility?: {
    enabled?: boolean
    key?: string
  }
}

type UseTableUrlStateReturn = {
  // Global filter
  globalFilter?: string
  onGlobalFilterChange?: OnChangeFn<string>
  // Column filters
  columnFilters: ColumnFiltersState
  onColumnFiltersChange: OnChangeFn<ColumnFiltersState>
  // Pagination
  pagination: PaginationState
  onPaginationChange: OnChangeFn<PaginationState>
  // Column visibility
  columnVisibility?: VisibilityState
  onColumnVisibilityChange?: OnChangeFn<VisibilityState>
  // Helpers
  ensurePageInRange: (
    pageCount: number,
    opts?: { resetTo?: 'first' | 'last' }
  ) => void
}

export function useTableUrlState(
  params: UseTableUrlStateParams
): UseTableUrlStateReturn {
  const {
    search,
    navigate,
    pagination: paginationCfg,
    globalFilter: globalFilterCfg,
    columnFilters: columnFiltersCfg = [],
    columnVisibility: columnVisibilityCfg,
  } = params

  const pageKey = paginationCfg?.pageKey ?? ('page' as string)
  const pageSizeKey = paginationCfg?.pageSizeKey ?? ('pageSize' as string)
  const defaultPage = paginationCfg?.defaultPage ?? 1
  const defaultPageSize = paginationCfg?.defaultPageSize ?? 10

  const globalFilterKey = globalFilterCfg?.key ?? ('filter' as string)
  const globalFilterEnabled = globalFilterCfg?.enabled ?? true
  const trimGlobal = globalFilterCfg?.trim ?? true

  const columnVisibilityKey = columnVisibilityCfg?.key ?? ('columnVisibility' as string)
  const columnVisibilityEnabled = columnVisibilityCfg?.enabled ?? false

  // Build initial column filters from the current search params
  const initialColumnFilters: ColumnFiltersState = useMemo(() => {
    const collected: ColumnFiltersState = []
    for (const cfg of columnFiltersCfg) {
      const raw = (search as SearchRecord)[cfg.searchKey]
      const deserialize = cfg.deserialize ?? ((v: unknown) => v)
      if (cfg.type === 'string') {
        const value = (deserialize(raw) as string) ?? ''
        if (typeof value === 'string' && value.trim() !== '') {
          collected.push({ id: cfg.columnId, value })
        }
      } else {
        // default to array type
        const value = (deserialize(raw) as unknown[]) ?? []
        if (Array.isArray(value) && value.length > 0) {
          collected.push({ id: cfg.columnId, value })
        }
      }
    }
    return collected
  }, [columnFiltersCfg, search])

  const [columnFilters, setColumnFilters] =
    useState<ColumnFiltersState>(initialColumnFilters)

  const pagination: PaginationState = useMemo(() => {
    const rawPage = (search as SearchRecord)[pageKey]
    const rawPageSize = (search as SearchRecord)[pageSizeKey]
    const pageNum = typeof rawPage === 'number' ? rawPage : defaultPage
    const pageSizeNum =
      typeof rawPageSize === 'number' ? rawPageSize : defaultPageSize
    return { pageIndex: Math.max(0, pageNum - 1), pageSize: pageSizeNum }
  }, [search, pageKey, pageSizeKey, defaultPage, defaultPageSize])

  const onPaginationChange: OnChangeFn<PaginationState> = (updater) => {
    const next = typeof updater === 'function' ? updater(pagination) : updater
    const nextPage = next.pageIndex + 1
    const nextPageSize = next.pageSize
    navigate({
      search: (prev) => ({
        ...(prev as SearchRecord),
        [pageKey]: nextPage <= defaultPage ? undefined : nextPage,
        [pageSizeKey]:
          nextPageSize === defaultPageSize ? undefined : nextPageSize,
      }),
    })
  }

  const [globalFilter, setGlobalFilter] = useState<string | undefined>(() => {
    if (!globalFilterEnabled) return undefined
    const raw = (search as SearchRecord)[globalFilterKey]
    return typeof raw === 'string' ? raw : ''
  })

  const onGlobalFilterChange: OnChangeFn<string> | undefined =
    globalFilterEnabled
      ? (updater) => {
          const next =
            typeof updater === 'function'
              ? updater(globalFilter ?? '')
              : updater
          const value = trimGlobal ? next.trim() : next
          setGlobalFilter(value)
          navigate({
            search: (prev) => ({
              ...(prev as SearchRecord),
              [pageKey]: undefined,
              [globalFilterKey]: value ? value : undefined,
            }),
          })
        }
      : undefined

  const onColumnFiltersChange: OnChangeFn<ColumnFiltersState> = (updater) => {
    const next =
      typeof updater === 'function' ? updater(columnFilters) : updater
    setColumnFilters(next)

    const patch: Record<string, unknown> = {}

    for (const cfg of columnFiltersCfg) {
      const found = next.find((f) => f.id === cfg.columnId)
      const serialize = cfg.serialize ?? ((v: unknown) => v)
      if (cfg.type === 'string') {
        const value =
          typeof found?.value === 'string' ? (found.value as string) : ''
        patch[cfg.searchKey] =
          value.trim() !== '' ? serialize(value) : undefined
      } else {
        const value = Array.isArray(found?.value)
          ? (found!.value as unknown[])
          : []
        patch[cfg.searchKey] = value.length > 0 ? serialize(value) : undefined
      }
    }

    navigate({
      search: (prev) => ({
        ...(prev as SearchRecord),
        [pageKey]: undefined,
        ...patch,
      }),
    })
  }

  // Column visibility state (synced with URL)
  const initialColumnVisibility: VisibilityState = useMemo(() => {
    if (!columnVisibilityEnabled) return {}
    const raw = (search as SearchRecord)[columnVisibilityKey]
    if (typeof raw === 'object' && raw !== null && !Array.isArray(raw)) {
      // 只保留布尔值，过滤掉其他类型
      const visibility: VisibilityState = {}
      const rawObj = raw as Record<string, unknown>
      for (const key in rawObj) {
        if (Object.prototype.hasOwnProperty.call(rawObj, key)) {
          const value = rawObj[key]
          if (typeof value === 'boolean') {
            visibility[key] = value
          }
        }
      }
      return visibility
    }
    return {}
  }, [columnVisibilityEnabled, columnVisibilityKey, search])

  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>(initialColumnVisibility)

  const onColumnVisibilityChange: OnChangeFn<VisibilityState> | undefined =
    columnVisibilityEnabled
      ? (updater) => {
          const next =
            typeof updater === 'function'
              ? updater(columnVisibility)
              : updater
          setColumnVisibility(next)
          
          // 只保存隐藏的列（false 值），不保存显示的列（true 值或 undefined）
          const hiddenColumns: VisibilityState = {}
          for (const key in next) {
            if (Object.prototype.hasOwnProperty.call(next, key)) {
              if (next[key] === false) {
                hiddenColumns[key] = false
              }
            }
          }
          
          navigate({
            search: (prev) => ({
              ...(prev as SearchRecord),
              [columnVisibilityKey]: Object.keys(hiddenColumns).length > 0 ? hiddenColumns : undefined,
            }),
          })
        }
      : undefined

  const ensurePageInRange = (
    pageCount: number,
    opts: { resetTo?: 'first' | 'last' } = { resetTo: 'first' }
  ) => {
    const currentPage = (search as SearchRecord)[pageKey]
    const pageNum = typeof currentPage === 'number' ? currentPage : defaultPage
    if (pageCount > 0 && pageNum > pageCount) {
      navigate({
        replace: true,
        search: (prev) => ({
          ...(prev as SearchRecord),
          [pageKey]: opts.resetTo === 'last' ? pageCount : undefined,
        }),
      })
    }
  }

  return {
    globalFilter: globalFilterEnabled ? (globalFilter ?? '') : undefined,
    onGlobalFilterChange,
    columnFilters,
    onColumnFiltersChange,
    pagination,
    onPaginationChange,
    columnVisibility: columnVisibilityEnabled ? columnVisibility : undefined,
    onColumnVisibilityChange,
    ensurePageInRange,
  }
}
