import { useEffect, useMemo, useState } from 'react'
import { getRouteApi } from '@tanstack/react-router'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Search } from '@/components/search'
import { SysPermissionDialogs } from './components/sys-permission-dialogs'
import { SysPermissionProvider } from './providers/sys-permission-provider'
import { SysPermissionTable } from './components/sys-permission-table'
import { SysPermissionActionBar } from './components/sys-permission-action-bar'
import { permissionApi } from '@/lib/api'
import { useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'

const route = getRouteApi('/_authenticated/system/permission/')

export function SysPermission() {
  const search = route.useSearch()
  const navigate = route.useNavigate()
  const [refreshKey, setRefreshKey] = useState(0)

  // 构建查询参数
  const queryParams = useMemo(() => ({
    visible: search.visible && search.visible.length > 0 
      ? search.visible[0] 
      : undefined,
    status: search.status && search.status.length > 0 
      ? search.status[0] 
      : undefined,
    permissionName: search.permissionName || undefined,
  }), [search.visible, search.status, search.permissionName])

  // 获取权限列表（树形）
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['systemPermissions', queryParams, refreshKey],
    queryFn: () => permissionApi.list(queryParams),
    staleTime: 5000,
  })

  // 刷新数据
  const refresh = () => {
    setRefreshKey((prev) => prev + 1)
    refetch()
  }

  useEffect(() => {
    if (error) {
      toast.error('获取权限列表失败')
    }
  }, [error])

  return (
    <SysPermissionProvider refresh={refresh}>
      <Header fixed>
        <Search />
      </Header>

      <Main className='flex flex-1 flex-col gap-4 sm:gap-6'>
        {/* 操作区 */}
        <SysPermissionActionBar
          search={search}
          navigate={navigate}
          loading={isLoading}
        />

        {/* 表格 */}
        <div className='bg-background p-4 rounded-lg border'>
          <SysPermissionTable 
            data={data || []} 
            loading={isLoading}
            search={search}
            navigate={navigate}
          />
        </div>
      </Main>

      <SysPermissionDialogs />
    </SysPermissionProvider>
  )
}

