import { type ColumnDef } from '@tanstack/react-table'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Checkbox } from '@/components/ui/checkbox'
import { DataTableColumnHeader } from '@/components/data-table'
import { LongText } from '@/components/long-text'
import { type User } from '../data/schema'
import { DataTableRowActions } from './data-table-row-actions'

export const sysUserColumns: ColumnDef<User>[] = [
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
    accessorKey: 'username',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='用户名' />
    ),
    cell: ({ row }) => (
      <LongText className='max-w-36 ps-2.5'>{row.getValue('username')}</LongText>
    ),
    meta: {
      className: cn(
        'ps-0.5'
      ),
    },
    enableHiding: false,
  },
  {
    accessorKey: 'nickname',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='昵称' />
    ),
    cell: ({ row }) => (
      <LongText className='max-w-36 ps-2.5'>{row.getValue('nickname') || '-'}</LongText>
    ),
    meta: { 
      className: 'w-36',
      label: '昵称'
    },
  },
  {
    accessorKey: 'deptName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='部门' />
    ),
    cell: ({ row }) => (
      <div className='w-fit'>{row.original.deptName || row.original.dept?.deptName || '-'}</div>
    ),
    meta: { label: '部门' },
    enableSorting: false,
  },
  {
    accessorKey: 'email',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='邮箱' />
    ),
    cell: ({ row }) => (
      <div className='w-fit ps-3 text-nowrap'>{row.getValue('email') || '-'}</div>
    ),
    meta: { label: '邮箱' },
  },
  {
    accessorKey: 'phone',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='手机号' />
    ),
    cell: ({ row }) => <div>{row.getValue('phone') || '-'}</div>,
    meta: { label: '手机号' },
    enableSorting: false,
  },
  {
    id: 'roles',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='角色' />
    ),
    cell: ({ row }) => {
      const roles = row.original.roles || []
      if (roles.length === 0) {
        return <span className='text-muted-foreground text-sm'>-</span>
      }
      return (
        <div className='flex flex-wrap gap-1'>
          {roles.slice(0, 2).map((role) => (
            <Badge key={role.id} variant='secondary' className='text-xs'>
              {role.roleName}
            </Badge>
          ))}
          {roles.length > 2 && (
            <Badge variant='secondary' className='text-xs'>
              +{roles.length - 2}
            </Badge>
          )}
        </div>
      )
    },
    enableSorting: false,
    enableHiding: false,
  },
  {
    accessorKey: 'status',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='状态' />
    ),
    cell: ({ row }) => {
      const status = row.getValue('status') as number
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
      const status = row.getValue(id) as number
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

