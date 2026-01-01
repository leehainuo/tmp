import { type ColumnDef } from '@tanstack/react-table'
import { ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Checkbox } from '@/components/ui/checkbox'
import { DataTableColumnHeader } from '@/components/data-table'
import { LongText } from '@/components/long-text'
import { type FlatTreeNode } from '@/hooks/use-tree-table'
import { type Dept } from '../data/schema'
import { DataTableRowActions } from './data-table-row-actions'

export type FlatDept = FlatTreeNode<Dept>

export function createSysDeptColumns(
  toggleExpand: (id?: number) => void
): ColumnDef<FlatDept>[] {
  return [
  {
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        checked={
          table.getIsAllPageRowsSelected() ||
          (table.getIsSomePageRowsSelected() && 'indeterminate')
        }
        onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
        aria-label='Select all'
        className='translate-y-[2px]'
      />
    ),
    meta: {
      className: cn(
        'max-md:drop-shadow-[0_1px_2px_rgb(0_0_0_/_0.1)] max-md:dark:drop-shadow-[0_1px_2px_rgb(255_255_255_/_0.1)]',
        'max-md:sticky start-0 z-10 rounded-tl-[inherit]'
      ),
      thClassName: 'w-12 min-w-12',
      tdClassName: 'w-12 min-w-12',
    },
    cell: ({ row }) => (
      <Checkbox
        checked={row.getIsSelected()}
        onCheckedChange={(value) => row.toggleSelected(!!value)}
        aria-label='Select row'
        className='translate-y-[2px]'
      />
    ),
    enableSorting: false,
    enableHiding: false,
  },
  {
    id: 'index',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='序号' />
    ),
    cell: ({ row, table }) => {
      const state = table.getState()
      const pageIndex = state.pagination?.pageIndex ?? 0
      const pageSize = state.pagination?.pageSize ?? (table.getRowModel().rows.length || 0)
      const serial = pageSize ? pageIndex * pageSize + row.index + 1 : row.index + 1
      return <div className='ps-2.5'>{serial}</div>
    },
    meta: {
      thClassName: 'w-12 min-w-12',
      tdClassName: 'w-12 min-w-12',
    },
    enableSorting: false,
    enableHiding: false,
  },
  {
    accessorKey: 'deptName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='部门名称' />
    ),
    cell: ({ row }) => {
      const level = row.original.level || 0
      const hasChildren = row.original.hasChildren
      const isExpanded = row.original.isExpanded
      const id = row.original.id

      return (
        <div
          className='flex items-center gap-2 ps-2'
          style={{ paddingLeft: `${level * 20 + 8}px` }}
        >
          {hasChildren ? (
            <button
              type='button'
              onClick={(event) => {
                event.stopPropagation()
                toggleExpand(id)
              }}
              className='flex h-6 w-6 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted'
              aria-label={isExpanded ? '折叠子部门' : '展开子部门'}
            >
              <ChevronRight
                className={cn(
                  'h-3.5 w-3.5 transition-transform duration-200',
                  isExpanded && 'rotate-90'
                )}
              />
            </button>
          ) : (
            <span className='inline-block h-6 w-6' />
          )}
          <LongText className='max-w-48'>{row.getValue('deptName')}</LongText>
        </div>
      )
    },
    meta: {
      className: cn(
        'ps-0.5'
      ),
      thClassName: 'min-w-[220px]',
      tdClassName: 'min-w-[220px]',
    },
    enableHiding: false,
  },
  {
    accessorKey: 'leader',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='负责人' />
    ),
    cell: ({ row }) => (
      <div>{row.getValue('leader') || '-'}</div>
    ),
    meta: {
      label: '负责人',
      thClassName: 'w-28 min-w-28',
      tdClassName: 'w-28 min-w-28',
    },
    enableSorting: false,
  },
  {
    accessorKey: 'phone',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='联系电话' />
    ),
    cell: ({ row }) => (
      <div className='text-nowrap'>{row.getValue('phone') || '-'}</div>
    ),
    meta: {
      label: '联系电话',
      thClassName: 'w-40 min-w-40',
      tdClassName: 'w-40 min-w-40',
    },
    enableSorting: false,
  },
  {
    accessorKey: 'email',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='邮箱' />
    ),
    cell: ({ row }) => (
      <div className='text-nowrap max-w-48 truncate'>{row.getValue('email') || '-'}</div>
    ),
    meta: {
      label: '邮箱',
      thClassName: 'min-w-[220px]',
      tdClassName: 'min-w-[220px]',
    },
    enableSorting: false,
  },
  {
    accessorKey: 'orderNum',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='排序' />
    ),
    cell: ({ row }) => (
      <div className='ps-5'>{row.getValue('orderNum') ?? '-'}</div>
    ),
    meta: {
      label: '排序',
      thClassName: 'w-20 min-w-20',
      tdClassName: 'w-20 min-w-20',
    },
  },
  {
    accessorKey: 'status',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='状态' />
    ),
    cell: ({ row }) => {
      const status = row.getValue('status') as number | undefined
      const isActive = status === 1
      return (
        <Badge
          variant='outline'
          className={cn(
            'border-transparent',
            isActive
              ? 'bg-green-50 text-green-600 dark:bg-green-950 dark:text-green-400' 
              : 'bg-red-50 text-red-600 dark:bg-red-950 dark:text-red-400'
          )}
        >
          {isActive ? '正常' : '停用'}
        </Badge>
      )
    },
    meta: {
      thClassName: 'w-24 min-w-24',
      tdClassName: 'w-24 min-w-24',
    },
    filterFn: (row, id, value) => {
      if (!value || value.length === 0) return true
      const status = row.getValue(id) as number | undefined
      return value.includes(status)
    },
    enableSorting: false,
    enableHiding: false,
  },
  {
    id: 'actions',
    cell: DataTableRowActions,
    meta: {
      className: cn(
        'max-md:drop-shadow-[0_1px_2px_rgb(0_0_0_/_0.1)] max-md:dark:drop-shadow-[0_1px_2px_rgb(255_255_255_/_0.1)]',
        'max-md:sticky end-0 z-10 bg-background rounded-tr-[inherit]'
      ),
      thClassName: 'w-10 min-w-10',
      tdClassName: 'w-10 min-w-10',
    },
  },
]
}


