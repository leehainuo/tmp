import { useState } from 'react'
import { Columns3 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import type { Table } from '@tanstack/react-table'

type DataTableViewOptionsProps<TData> = {
  table: Table<TData>
  /** 是否显示文字标签 */
  showLabel?: boolean
  /** 自定义类名 */
  className?: string
}

/**
 * 获取列的显示标签
 * 优先使用 meta.label，如果没有则使用 column.id
 */
function getColumnLabel(column: { id: string; columnDef: { meta?: { label?: string } } }): string {
  return column.columnDef.meta?.label || column.id
}

export function DataTableViewOptions<TData>({
  table,
  showLabel = false,
  className,
}: DataTableViewOptionsProps<TData>) {
  // 使用本地状态来跟踪下拉菜单的打开状态，确保 UI 及时更新
  const [open, setOpen] = useState(false)

  return (
    <DropdownMenu open={open} onOpenChange={setOpen} modal={false}>
      <DropdownMenuTrigger asChild>
        <Button
          variant='outline'
          size={showLabel ? 'sm' : 'icon'}
          className={showLabel ? 'ms-auto hidden h-8 lg:flex' : `shrink-0 ${className || ''}`}
          title='视图选项'
        >
          <Columns3 className='size-4' />
          {showLabel && <span className='ml-2'>视图</span>}
          <span className='sr-only'>视图选项</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align='end' className='w-[150px]'>
        <DropdownMenuLabel>切换列</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {table
          .getAllColumns()
          .filter(
            (column) =>
              typeof column.accessorFn !== 'undefined' && column.getCanHide()
          )
          .map((column) => {
            // 直接从 table 状态获取可见性，确保实时同步
            const isVisible = column.getIsVisible()
            return (
              <DropdownMenuCheckboxItem
                key={column.id}
                checked={isVisible}
                onCheckedChange={(value) => {
                  column.toggleVisibility(!!value)
                  // 强制重新渲染下拉菜单以更新 checked 状态
                  setOpen(true)
                }}
              >
                {getColumnLabel(column)}
              </DropdownMenuCheckboxItem>
            )
          })}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
