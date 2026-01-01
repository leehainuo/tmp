import { useMemo, useState, useEffect } from 'react'
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
import { useTreeTable } from '@/hooks/use-tree-table'
import { type Dept } from '../data/schema'
import { createSysDeptColumns } from './sys-dept-columns'
import { DataTableBulkActions } from './data-table-bulk-actions'
import { DataTablePagination } from '@/components/data-table'
import { useSysDept } from '../providers/sys-dept-provider'

type DataTableProps = {
  data: Dept[]
  loading?: boolean
  search: Record<string, unknown>
  navigate: NavigateFn
}

export function SysDeptTable({ data, loading = false, search, navigate }: DataTableProps) {
  const { setTable } = useSysDept()
  
  // Local UI-only states
  const [rowSelection, setRowSelection] = useState({})
  const [sorting, setSorting] = useState<SortingState>([])
  const { flatData, toggleExpand } = useTreeTable<Dept>({ data })

  // Synced with URL states
  const {
    columnFilters,
    onColumnFiltersChange,
    columnVisibility,
    onColumnVisibilityChange,
  } = useTableUrlState({
    search,
    navigate,
    globalFilter: { enabled: false },
    columnFilters: [
      { columnId: 'deptName', searchKey: 'deptName', type: 'string' },
      {
        columnId: 'status',
        searchKey: 'status',
        type: 'array',
        ...singleSelectStatusFilter,
      },
    ],
    columnVisibility: { enabled: true, key: 'columnVisibility' },
  })

  const columns = useMemo(
    () => createSysDeptColumns(toggleExpand),
    [toggleExpand]
  )

  // eslint-disable-next-line react-hooks/incompatible-library
  const table = useReactTable({
    data: flatData,
    columns,
    state: {
      sorting,
      rowSelection,
      columnVisibility: columnVisibility || {},
      columnFilters,
    },
    enableRowSelection: true,
    onRowSelectionChange: setRowSelection,
    onSortingChange: setSorting,
    onColumnVisibilityChange,
    onColumnFiltersChange,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
  })

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
        'max-sm:has-[div[role="toolbar"]]:mb-16',
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


