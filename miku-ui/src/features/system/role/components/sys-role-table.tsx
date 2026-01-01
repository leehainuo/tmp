import { useEffect, useState } from 'react'
import {
  type SortingState,
  flexRender,
  getCoreRowModel,
  getSortedRowModel,
  useReactTable,
} from '@tanstack/react-table'
import { cn } from '@/lib/utils'
import { type NavigateFn, useTableUrlState, singleSelectStatusFilter } from '@/hooks/use-table-url-state'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { DataTablePagination } from '@/components/data-table'
import { type Role } from '../data/schema'
import { DataTableBulkActions } from './data-table-bulk-actions'
import { sysRoleColumns as columns } from './sys-role-columns'
import { useSysRole } from '../providers/sys-role-provider'

type DataTableProps = {
  data: Role[]
  total?: number
  loading?: boolean
  search: Record<string, unknown>
  navigate: NavigateFn
}

export function SysRoleTable({ data, total = 0, loading = false, search, navigate }: DataTableProps) {
  const { setTable } = useSysRole()
  
  // Local UI-only states
  const [rowSelection, setRowSelection] = useState({})
  const [sorting, setSorting] = useState<SortingState>([])

  // Synced with URL states (keys/defaults mirror roles route search schema)
  const {
    columnFilters,
    onColumnFiltersChange,
    pagination,
    onPaginationChange,
    columnVisibility,
    onColumnVisibilityChange,
    ensurePageInRange,
  } = useTableUrlState({
    search,
    navigate,
    pagination: { defaultPage: 1, defaultPageSize: 10 },
    globalFilter: { enabled: false },
    columnFilters: [
      // roleName per-column text filter
      { columnId: 'roleName', searchKey: 'roleName', type: 'string' },
      {
        columnId: 'status',
        searchKey: 'status',
        type: 'array',
        ...singleSelectStatusFilter,
      },
    ],
    columnVisibility: { enabled: true, key: 'columnVisibility' },
  })

  // 计算总页数（服务器端分页）
  const pageCount = Math.ceil(total / (pagination.pageSize || 10))

  // eslint-disable-next-line react-hooks/incompatible-library
  const table = useReactTable({
    data,
    columns,
    state: {
      sorting,
      pagination: {
        ...pagination,
        pageIndex: (pagination.pageIndex || 0),
      },
      rowSelection,
      columnFilters,
      columnVisibility: columnVisibility || {},
    },
    enableRowSelection: true,
    onPaginationChange,
    onColumnFiltersChange,
    onRowSelectionChange: setRowSelection,
    onSortingChange: setSorting,
    onColumnVisibilityChange,
    // 服务器端分页，不使用客户端分页模型
    manualPagination: true,
    pageCount,
    getCoreRowModel: getCoreRowModel(),
    // 服务器端筛选，不使用客户端筛选模型
    manualFiltering: true,
    getSortedRowModel: getSortedRowModel(),
  })

  useEffect(() => {
    if (pageCount > 0) {
      ensurePageInRange(pageCount)
    }
  }, [pageCount, ensurePageInRange])

  // 将 table 实例保存到 Context 中
  useEffect(() => {
    if (setTable) {
      setTable(table)
    }
    // 清理函数：组件卸载时清除 table 实例
    return () => {
      if (setTable) {
        setTable(undefined)
      }
    }
  }, [table, setTable])

  return (
    <div
      className={cn(
        'max-sm:has-[div[role="toolbar"]]:mb-16', // Add margin bottom to the table on mobile when the toolbar is visible
        'flex flex-1 flex-col gap-4'
      )}
    >
      <div className='overflow-hidden'>
        <Table>
          <TableHeader>
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id} className='group/row'>
                {headerGroup.headers.map((header) => {
                  return (
                    <TableHead
                      key={header.id}
                      colSpan={header.colSpan}
                      className={cn(
                        'bg-background group-hover/row:bg-muted group-data-[state=selected]/row:bg-muted',
                        header.column.columnDef.meta?.className,
                        header.column.columnDef.meta?.thClassName
                      )}
                    >
                      {header.isPlaceholder
                        ? null
                        : flexRender(
                            header.column.columnDef.header,
                            header.getContext()
                          )}
                    </TableHead>
                  )
                })}
              </TableRow>
            ))}
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className='h-24 text-center'
                >
                  加载中...
                </TableCell>
              </TableRow>
            ) : table.getRowModel().rows?.length ? (
              table.getRowModel().rows.map((row) => (
                <TableRow
                  key={row.id}
                  data-state={row.getIsSelected() && 'selected'}
                  className='group/row'
                >
                  {row.getVisibleCells().map((cell) => (
                    <TableCell
                      key={cell.id}
                      className={cn(
                        'bg-background group-hover/row:bg-muted group-data-[state=selected]/row:bg-muted',
                        cell.column.columnDef.meta?.className,
                        cell.column.columnDef.meta?.tdClassName
                      )}
                    >
                      {flexRender(
                        cell.column.columnDef.cell,
                        cell.getContext()
                      )}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className='h-24 text-center'
                >
                  暂无数据
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
      <DataTablePagination table={table} className='mt-auto' />
      <DataTableBulkActions table={table} />
    </div>
  )
}

