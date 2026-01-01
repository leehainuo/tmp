import { type ColumnDef } from '@tanstack/react-table'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Checkbox } from '@/components/ui/checkbox'
import { DataTableColumnHeader } from '@/components/data-table'
import { LongText } from '@/components/long-text'
import { type Role } from '../data/schema'
import { DataTableRowActions } from './data-table-row-actions'

export const sysRoleColumns: ColumnDef<Role>[] = [
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
    accessorKey: 'roleName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='角色名称' />
    ),
    cell: ({ row }) => (
      <LongText className='max-w-36 ps-2.5'>{row.getValue('roleName')}</LongText>
    ),
    meta: {
      className: cn(
        'ps-0.5'
      ),
    },
    enableHiding: false,
  },
  {
    accessorKey: 'roleCode',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='角色编码' />
    ),
    cell: ({ row }) => (
      <LongText className='max-w-36 ps-2.5'>{row.getValue('roleCode') || '-'}</LongText>
    ),
    meta: { 
      className: 'w-36',
      label: '角色编码'
    },
  },
  {
    accessorKey: 'roleSort',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='排序' />
    ),
    cell: ({ row }) => (
      <div className='w-fit ps-5'>{row.getValue('roleSort') ?? '-'}</div>
    ),
    meta: { label: '排序' },
  },
  {
    accessorKey: 'dataScope',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='数据权限' />
    ),
    cell: ({ row }) => {
      const dataScope = row.getValue('dataScope') as number | undefined
      const scopeMap: Record<number, string> = {
        1: '全部数据权限',
        2: '自定义数据权限',
        3: '本部门数据权限',
        4: '本部门及以下数据权限',
        5: '仅本人数据权限',
      }
      const scopeText = scopeMap[dataScope || 1] || '未知'
      return (
        <Badge variant='secondary' className='border-transparent'>
          {scopeText}
        </Badge>
      )
    },
    meta: { label: '数据权限' },
    enableSorting: false,
  },
  {
    accessorKey: 'remark',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='备注' />
    ),
    cell: ({ row }) => (
      <div className='w-fit text-nowrap max-w-48 truncate'>
        {row.getValue('remark') || '-'}
      </div>
    ),
    meta: { label: '备注' },
    enableSorting: false,
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
  },
  {
    id: 'actions',
    cell: DataTableRowActions,
    meta: {
      className: cn(
        'max-md:drop-shadow-[0_1px_2px_rgb(0_0_0_/_0.1)] max-md:dark:drop-shadow-[0_1px_2px_rgb(255_255_255_/_0.1)]',
        'max-md:sticky end-0 z-10 bg-background rounded-tr-[inherit]'
      ),
    },
  },
]

