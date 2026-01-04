import { useState, useEffect, useCallback } from 'react'
import { Filter, Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DataTableToolbar } from '@/components/data-table'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { useDebounceValue } from '@/hooks/use-debounce-value'
import { cleanSearchParams } from '@/lib/clean-search-params'
import type { NavigateFn } from '@/hooks/use-table-url-state'
import { useSysLog } from '../providers/sys-log-provider'

type SysLogActionBarProps = {
  search: Record<string, unknown>
  navigate: NavigateFn
  loading?: boolean
}

const DEFAULT_SEARCH_PARAMS = { page: 1, pageSize: 10 }

export function SysLogActionBar({
  search,
  navigate,
  loading = false,
}: SysLogActionBarProps) {
  const { setOpen, refresh, table } = useSysLog()
  const [filterOpen, setFilterOpen] = useState(false)

  const currentTitle = (search.title as string) || ''
  const currentOperName = (search.operName as string) || ''

  const initialSearchValue = currentTitle || currentOperName || ''
  const [inputValue, setInputValue] = useState(initialSearchValue)
  const [debouncedValue] = useDebounceValue(inputValue, 500)

  const updateSearchParams = useCallback(
    (updates: { title?: string; operName?: string; page?: number }) => {
      navigate({
        search: (prev) => {
          const updated = {
            ...prev,
            ...updates,
            page: updates.page ?? 1,
          }
          return cleanSearchParams(updated, DEFAULT_SEARCH_PARAMS)
        },
      })
    },
    [navigate]
  )

  useEffect(() => {
    const currentSearchValue = currentTitle || currentOperName || ''
    if (currentSearchValue !== debouncedValue) {
      updateSearchParams({
        title: debouncedValue || undefined,
        operName: undefined,
      })
    }
  }, [debouncedValue, currentTitle, currentOperName, updateSearchParams])

  const handleTitleSearch = (value: string) => {
    updateSearchParams({
      title: value || undefined,
    })
  }

  const handleOperNameSearch = (value: string) => {
    updateSearchParams({
      operName: value || undefined,
    })
  }

  const handleClearFilters = () => {
    updateSearchParams({
      title: undefined,
      operName: undefined,
    })
    setFilterOpen(false)
  }

  const handleRefresh = () => {
    setInputValue('')
    handleClearFilters()
    refresh?.()
  }

  const handleClearSearch = () => {
    setInputValue('')
    updateSearchParams({
      title: undefined,
      operName: undefined,
    })
  }

  const hasActiveFilters = Boolean(currentTitle || currentOperName)

  return (
    <DataTableToolbar
      table={table!}
      search={{
        placeholder: '搜索模块标题',
        value: inputValue,
        onChange: (value: string) => setInputValue(value),
        onSearch: (value: string) => {
          setInputValue(value)
          updateSearchParams({
            title: value?.trim() || undefined,
            operName: undefined,
          })
        },
        onClear: handleClearSearch,
      }}
      filter={{
        label: '筛选',
        active: hasActiveFilters,
        trigger: (
          <Popover open={filterOpen} onOpenChange={setFilterOpen}>
            <PopoverTrigger asChild>
              <Button
                variant={hasActiveFilters ? 'default' : 'outline'}
                size='icon'
                className='shrink-0'
                title='筛选'
              >
                <Filter className='size-4' />
                <span className='sr-only'>筛选</span>
              </Button>
            </PopoverTrigger>
            <PopoverContent className='w-72' align='end'>
              <div className='space-y-4'>
                <div className='flex items-center justify-between'>
                  <h4 className='font-medium'>筛选条件</h4>
                  {hasActiveFilters && (
                    <Button
                      variant='ghost'
                      size='sm'
                      onClick={handleClearFilters}
                      className='h-auto p-0 text-xs'
                    >
                      清空
                    </Button>
                  )}
                </div>
                <Separator />
                <div className='space-y-2'>
                  <Label htmlFor='filter-title' className='text-sm'>
                    模块标题
                  </Label>
                  <input
                    id='filter-title'
                    type='text'
                    placeholder='输入模块标题...'
                    value={currentTitle}
                    onChange={(e) => handleTitleSearch(e.target.value)}
                    className='flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50'
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='filter-oper-name' className='text-sm'>
                    操作人员
                  </Label>
                  <input
                    id='filter-oper-name'
                    type='text'
                    placeholder='输入操作人员...'
                    value={currentOperName}
                    onChange={(e) => handleOperNameSearch(e.target.value)}
                    className='flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50'
                  />
                </div>
              </div>
            </PopoverContent>
          </Popover>
        ),
      }}
      refresh={{
        onClick: handleRefresh,
        loading,
      }}
      showViewOptions={!!table}
      actions={
        <Button
          variant='destructive'
          className='space-x-1'
          onClick={() => setOpen('clean')}
        >
          <Trash2 size={18} />
          <span>清理日志</span>
        </Button>
      }
      extra={null}
    />
  )
}


