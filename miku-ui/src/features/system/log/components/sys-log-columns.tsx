import type { ColumnDef } from '@tanstack/react-table'
import { cn } from '@/lib/utils'
import { Checkbox } from '@/components/ui/checkbox'
import { Badge } from '@/components/ui/badge'
import { DataTableColumnHeader } from '@/components/data-table'
import type { Log } from '../data/schema'
import { DataTableRowActions } from './data-table-row-actions'

export const sysLogColumns: ColumnDef<Log>[] = [
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
    accessorKey: 'title',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='模块' />
    ),
    cell: ({ row }) => <div className='ps-2.5'>{row.getValue('title') || '-'}</div>,
    meta: { label: '模块' },
  },
  {
    accessorKey: 'businessType',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='业务类型' />
    ),
    cell: ({ row }) => {
      const type = row.getValue('businessType') as number | null
      const map: Record<number, string> = {
        0: '其它',
        1: '新增',
        2: '修改',
        3: '删除',
      }
      return <span>{type != null ? map[type] || type : '-'}</span>
    },
    enableSorting: false,
  },
  {
    accessorKey: 'operName',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='操作人员' />
    ),
    cell: ({ row }) => <div className='ps-2.5'>{row.getValue('operName') || '-'}</div>,
    meta: { label: '操作人员' },
  },
  {
    accessorKey: 'operIp',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='IP' />
    ),
    cell: ({ row }) => <div className='ps-2.5'>{row.getValue('operIp') || '-'}</div>,
    meta: { label: 'IP' },
  },
  {
    accessorKey: 'operUrl',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='请求地址' />
    ),
    cell: ({ row }) => (
      <div className='max-w-80 truncate ps-2.5' title={row.original.operUrl}>
        {row.original.operUrl || '-'}
      </div>
    ),
    meta: { label: '请求地址' },
  },
  {
    accessorKey: 'status',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='状态' />
    ),
    cell: ({ row }) => {
      const status = row.getValue('status') as number
      const isOk = status === 1
      return (
        <Badge
          variant='outline'
          className={cn(
            'border-transparent',
            isOk
              ? 'bg-green-50 text-green-600 dark:bg-green-950 dark:text-green-400'
              : 'bg-red-50 text-red-600 dark:bg-red-950 dark:text-red-400'
          )}
        >
          {isOk ? '成功' : '失败'}
        </Badge>
      )
    },
    enableSorting: false,
  },
  {
    accessorKey: 'operTime',
    header: ({ column }) => (
      <DataTableColumnHeader column={column} title='操作时间' />
    ),
    cell: ({ row }) => <div className='ps-2.5'>{row.getValue('operTime') || '-'}</div>,
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


