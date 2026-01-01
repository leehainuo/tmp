import { useState, useEffect, useCallback } from 'react'
import { Plus, Filter } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { DataTableToolbar } from '@/components/data-table'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { Checkbox } from '@/components/ui/checkbox'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { useDebounceValue } from '@/hooks/use-debounce-value'
import { cleanSearchParams } from '@/lib/clean-search-params'
import { useSysPermission } from '../providers/sys-permission-provider'
import type { NavigateFn } from '@/hooks/use-table-url-state'
import { ExportButton } from '../../../../components/export-button'

type SysPermissionActionBarProps = {
  search: Record<string, unknown>
  navigate: NavigateFn
  loading?: boolean
}

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '停用', value: 0 },
] as const

const visibleOptions = [
  { label: '显示', value: 1 },
  { label: '隐藏', value: 0 },
] as const

// 默认的搜索参数清理配置
const DEFAULT_SEARCH_PARAMS = { page: 1, pageSize: 10 }

export function SysPermissionActionBar({
  search,
  navigate,
  loading = false,
}: SysPermissionActionBarProps) {
  const { setOpen, refresh, table } = useSysPermission()
  const [filterOpen, setFilterOpen] = useState(false)

  // 获取当前搜索和筛选状态
  const currentPermissionName = (search.permissionName as string) || ''
  const currentStatus = (search.status as number[]) || []
  const currentVisible = (search.visible as number[]) || []

  // 初始化搜索输入值
  const initialSearchValue = currentPermissionName || ''
  const [inputValue, setInputValue] = useState(initialSearchValue)
  
  // 防抖处理搜索值（500ms 延迟）
  const [debouncedValue] = useDebounceValue(inputValue, 500)

  // 通用的更新搜索参数函数
  const updateSearchParams = useCallback((
    updates: {
      permissionName?: string | undefined
      status?: number[] | undefined
      visible?: number[] | undefined
      page?: number
    }
  ) => {
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
  }, [navigate])

  // 当防抖值变化时，同步到 URL
  useEffect(() => {
    if (currentPermissionName !== debouncedValue) {
      updateSearchParams({
        permissionName: debouncedValue || undefined,
      })
    }
  }, [debouncedValue, currentPermissionName, updateSearchParams])

  // 处理权限名称搜索
  const handlePermissionNameSearch = (value: string) => {
    updateSearchParams({
      permissionName: value || undefined,
    })
  }

  // 处理状态筛选
  const handleStatusChange = (value: number, checked: boolean) => {
    const newStatus = checked
      ? [...currentStatus, value]
      : currentStatus.filter((s) => s !== value)

    updateSearchParams({
      status: newStatus.length > 0 ? newStatus : undefined,
    })
  }

  // 处理可见性筛选
  const handleVisibleChange = (value: number, checked: boolean) => {
    const newVisible = checked
      ? [...currentVisible, value]
      : currentVisible.filter((v) => v !== value)

    updateSearchParams({
      visible: newVisible.length > 0 ? newVisible : undefined,
    })
  }

  // 清空所有筛选
  const handleClearFilters = () => {
    updateSearchParams({
      permissionName: undefined,
      status: undefined,
      visible: undefined,
    })
    setFilterOpen(false)
  }

  // 处理重置视图（列显示/隐藏）
  const handleResetView = () => {
    if (table) {
      table.setColumnVisibility({})
    }
  }

  // 处理刷新（清除所有筛选条件、重置视图并刷新数据）
  const handleRefresh = () => {
    // 清空搜索输入框
    setInputValue('')
    // 清除所有筛选条件
    handleClearFilters()
    // 重置视图（列显示/隐藏）
    handleResetView()
    // 刷新数据
    refresh?.()
  }

  // 检查是否有激活的筛选
  const hasActiveFilters = Boolean(
    currentPermissionName || currentStatus.length > 0 || currentVisible.length > 0
  )

  // 处理清空搜索（立即执行，不等待防抖）
  const handleClearSearch = () => {
    setInputValue('')
    updateSearchParams({
      permissionName: undefined,
    })
  }

  return (
    <DataTableToolbar
      table={table!}
      search={{
        placeholder: '搜索权限名称',
        value: inputValue,
        onChange: (value: string) => setInputValue(value),
        onSearch: (value: string) => {
          // 按 Enter 键时立即搜索（不使用防抖）
          setInputValue(value)
          updateSearchParams({
            permissionName: value?.trim() || undefined,
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
            <PopoverContent className='w-64' align='end'>
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

                {/* 权限名称搜索 */}
                <div className='space-y-2'>
                  <Label htmlFor='filter-permissionName' className='text-sm'>
                    权限名称
                  </Label>
                  <input
                    id='filter-permissionName'
                    type='text'
                    placeholder='输入权限名称...'
                    value={currentPermissionName}
                    onChange={(e) => handlePermissionNameSearch(e.target.value)}
                    className='flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50'
                  />
                </div>

                {/* 状态筛选 */}
                <div className='space-y-2'>
                  <Label className='text-sm'>状态</Label>
                  <div className='space-y-2'>
                    {statusOptions.map((option) => (
                      <div
                        key={option.value}
                        className='flex items-center space-x-2'
                      >
                        <Checkbox
                          id={`status-${option.value}`}
                          checked={currentStatus.includes(option.value)}
                          onCheckedChange={(checked) =>
                            handleStatusChange(option.value, checked === true)
                          }
                        />
                        <Label
                          htmlFor={`status-${option.value}`}
                          className='text-sm font-normal cursor-pointer'
                        >
                          {option.label}
                        </Label>
                      </div>
                    ))}
                  </div>
                </div>

                {/* 可见性筛选 */}
                <div className='space-y-2'>
                  <Label className='text-sm'>可见性</Label>
                  <div className='space-y-2'>
                    {visibleOptions.map((option) => (
                      <div
                        key={option.value}
                        className='flex items-center space-x-2'
                      >
                        <Checkbox
                          id={`visible-${option.value}`}
                          checked={currentVisible.includes(option.value)}
                          onCheckedChange={(checked) =>
                            handleVisibleChange(option.value, checked === true)
                          }
                        />
                        <Label
                          htmlFor={`visible-${option.value}`}
                          className='text-sm font-normal cursor-pointer'
                        >
                          {option.label}
                        </Label>
                      </div>
                    ))}
                  </div>
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
        <div className='flex gap-2'>
          <ExportButton moduleName='权限' />
          <Button
            className='space-x-1'
            onClick={() => setOpen('add')}
          >
            <span>新增</span>
            <Plus size={18} />
          </Button>
        </div>
      }
      extra={null}
    />
  )
}

