import { useState, type ReactNode } from 'react'
import { ChevronDown, ChevronUp, Filter, RefreshCw, Search } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible'
import type { Table } from '@tanstack/react-table'
import { DataTableViewOptions } from './view-options'

type DataTableToolbarProps<TData> = {
  table: Table<TData>
  /** 搜索相关配置 */
  search?: {
    placeholder?: string
    value?: string
    onChange?: (value: string) => void
    onSearch?: (value: string) => void
    onClear?: () => void
  }
  /** 筛选按钮配置 */
  filter?: {
    label?: string
    onClick?: () => void
    active?: boolean
    trigger?: ReactNode // 自定义触发器（用于 Popover）
  }
  /** 刷新按钮配置 */
  refresh?: {
    onClick?: () => void
    loading?: boolean
  }
  /** 是否显示视图选项 */
  showViewOptions?: boolean
  /** 主要操作按钮区域 */
  actions?: ReactNode
  /** 额外的操作按钮（右侧） */
  extra?: ReactNode
  /** 是否默认展开 */
  defaultOpen?: boolean
  /** 是否显示展开/折叠按钮 */
  showCollapse?: boolean
  /** 自定义类名 */
  className?: string
}

export function DataTableToolbar<TData>({
  table,
  search,
  filter,
  refresh,
  showViewOptions = true,
  actions,
  extra,
  defaultOpen = true,
  showCollapse = true,
  className,
}: DataTableToolbarProps<TData>) {
  const [isOpen, setIsOpen] = useState(defaultOpen)
  
  // 使用受控组件，直接使用外部传入的值
  const searchValue = search?.value || ''

  const handleSearchChange = (value: string) => {
    search?.onChange?.(value)
  }

  const handleSearchKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      search?.onSearch?.(searchValue)
    }
  }

  return (
    <div
      className={cn(
        'bg-background rounded-lg border p-4 transition-all',
        className
      )}
    >
      <Collapsible open={isOpen} onOpenChange={setIsOpen}>
        <div className='flex items-center justify-between gap-2'>
          {/* 左侧：搜索和筛选 */}
          <div className='flex flex-1 items-center gap-2'>
            {/* 搜索框 */}
            {search && (
              <div className='relative flex-1 max-w-md'>
                <Search className='absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  placeholder={search.placeholder || '搜索...'}
                  value={searchValue}
                  onChange={(e) => handleSearchChange(e.target.value)}
                  onKeyDown={handleSearchKeyDown}
                  className='pl-9 pr-9'
                />
                {searchValue && (
                  <button
                    type='button'
                    onClick={() => {
                      // 如果有 onClear 回调，优先使用它（可以立即清空，不等待防抖）
                      if (search?.onClear) {
                        search.onClear()
                      } else {
                        // 否则使用 onChange 和 onSearch
                        search?.onChange?.('')
                        search?.onSearch?.('')
                      }
                    }}
                    className='absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground'
                  >
                    <span className='sr-only'>清空搜索</span>
                    ×
                  </button>
                )}
              </div>
            )}

            {/* 筛选按钮 */}
            {filter && (
              filter.trigger || (
                <Button
                  variant={filter.active ? 'default' : 'outline'}
                  size='icon'
                  onClick={filter.onClick}
                  className='shrink-0'
                  title={filter.label || '筛选'}
                >
                  <Filter className='size-4' />
                  <span className='sr-only'>{filter.label || '筛选'}</span>
                </Button>
              )
            )}

            {/* 展开/折叠按钮 */}
            {showCollapse && (
              <CollapsibleTrigger asChild>
                <Button
                  variant='outline'
                  size='icon'
                  className='shrink-0'
                  title={isOpen ? '折叠' : '展开'}
                >
                  {isOpen ? (
                    <ChevronUp className='size-4' />
                  ) : (
                    <ChevronDown className='size-4' />
                  )}
                  <span className='sr-only'>{isOpen ? '折叠' : '展开'}</span>
                </Button>
              </CollapsibleTrigger>
            )}
          </div>

          {/* 右侧：刷新和主要操作 */}
          <div className='flex items-center gap-2'>
            {/* 刷新按钮 */}
            {refresh && (
              <Button
                variant='outline'
                size='icon'
                onClick={refresh.onClick}
                disabled={refresh.loading}
                className='shrink-0'
                title='刷新'
              >
                <RefreshCw className='size-4' />
                <span className='sr-only'>刷新</span>
              </Button>
            )}

            {/* 视图选项按钮（列显示/隐藏） */}
            {showViewOptions && (
              <DataTableViewOptions table={table} />
            )}

            {/* 额外操作 */}
            {extra}
          </div>
        </div>

        {/* 可折叠内容区域 */}
        <CollapsibleContent className='mt-4 space-y-2'>
          {actions && (
            <div className='flex flex-wrap items-center gap-2 border-t pt-4'>
              {actions}
            </div>
          )}
        </CollapsibleContent>
      </Collapsible>
    </div>
  )
}
