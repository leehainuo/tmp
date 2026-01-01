import { type ColumnDef } from '@tanstack/react-table'
import { ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Checkbox } from '@/components/ui/checkbox'
import { DataTableColumnHeader } from '@/components/data-table'
import { LongText } from '@/components/long-text'
import { type FlatTreeNode } from '@/hooks/use-tree-table'
import { type Permission } from '../data/schema'
import { DataTableRowActions } from './data-table-row-actions'

export type FlatPermission = FlatTreeNode<Permission>

// 权限类型映射
const permissionTypeMap: Record<
  number,
  { label: string; variant: 'default' | 'secondary' | 'outline' | 'destructive' }
> = {
  1: { label: '目录', variant: 'default' },
  2: { label: '菜单', variant: 'secondary' },
  3: { label: '按钮', variant: 'outline' },
  4: { label: '接口', variant: 'outline' },
}

export function createSysPermissionColumns(
  toggleExpand: (id?: number) => void
): ColumnDef<FlatPermission>[] {
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
    accessorKey: 'permissionName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='权限名称' />
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
              aria-label={isExpanded ? '折叠子权限' : '展开子权限'}
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
          <LongText className='max-w-48'>
            {row.getValue('permissionName')}
          </LongText>
        </div>
      )
    },
    meta: {
      className: cn(
        'ps-0.5'
      ),
      thClassName: 'min-w-[200px]',
      tdClassName: 'min-w-[200px]',
    },
    enableHiding: false,
    },
    {
    accessorKey: 'permissionType',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='类型' />
    ),
    cell: ({ row }) => {
      const type = row.getValue('permissionType') as number
      const typeInfo = permissionTypeMap[type] || { label: '未知', variant: 'outline' as const }
      return (
        <Badge variant={typeInfo.variant} className='text-xs'>
          {typeInfo.label}
        </Badge>
      )
    },
    meta: {
      label: '类型',
      thClassName: 'w-28 min-w-28',
      tdClassName: 'w-28 min-w-28',
    },
    enableSorting: false,
  },
  {
    accessorKey: 'path',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='路由地址' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-nowrap max-w-48 truncate'>
        {row.original.path || '-'}
      </div>
    ),
    meta: {
      label: '路由地址',
      thClassName: 'min-w-[160px]',
      tdClassName: 'min-w-[160px]',
    },
    enableSorting: false,
  },
  {
    accessorKey: 'component',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='组件路径' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-nowrap max-w-48 truncate'>
        {row.original.component || '-'}
      </div>
    ),
    meta: {
      label: '组件路径',
      thClassName: 'min-w-[200px]',
      tdClassName: 'min-w-[200px]',
    },
    enableSorting: false,
  },
  {
    accessorKey: 'perms',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='权限标识' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-nowrap max-w-48 truncate'>
        {row.original.perms || '-'}
      </div>
    ),
    meta: {
      label: '权限标识',
      thClassName: 'min-w-[180px]',
      tdClassName: 'min-w-[180px]',
    },
    enableSorting: false,
  },
  {
    accessorKey: 'orderNum',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='排序' />
    ),
    cell: ({ row }) => (
      <div className='w-fit ps-5'>{row.original.orderNum ?? '-'}</div>
    ),
    meta: {
      label: '排序',
      thClassName: 'w-20 min-w-20',
      tdClassName: 'w-20 min-w-20',
    },
  },
  {
    accessorKey: 'visible',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='可见' />
    ),
    cell: ({ row }) => {
      const visible = row.getValue('visible') as number | undefined
      const isVisible = visible === 1
      return (
        <Badge 
          variant='outline' 
          className={cn(
            'border-transparent',
            isVisible 
              ? 'bg-blue-50 text-blue-600 dark:bg-blue-950 dark:text-blue-400' 
              : ''
          )}
        >
          {isVisible ? '显示' : '隐藏'}
        </Badge>
      )
    },
    enableSorting: false,
    meta: {
      label: '可见',
      thClassName: 'w-24 min-w-24',
      tdClassName: 'w-24 min-w-24',
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
        <div className='flex space-x-2'>
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
        </div>
      )
    },
    filterFn: (row, id, value) => {
      if (!value || value.length === 0) return true
      const status = row.getValue(id) as number | undefined
      return value.includes(status)
    },
    enableHiding: false,
    enableSorting: false,
    meta: {
      thClassName: 'w-24 min-w-24',
      tdClassName: 'w-24 min-w-24',
    },
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

