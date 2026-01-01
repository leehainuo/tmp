import { useEffect, useMemo, useState } from 'react'
import { getRouteApi } from '@tanstack/react-router'
import { useQuery } from '@tanstack/react-query'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Search } from '@/components/search'
import { SysRoleDialogs } from './components/sys-role-dialogs'
import { SysRoleProvider } from './providers/sys-role-provider'
import { SysRoleTable } from './components/sys-role-table'
import { SysRoleActionBar } from './components/sys-role-action-bar'
import { roleApi, type RoleQueryRequest } from '@/lib/api'
import { toast } from 'sonner'

const route = getRouteApi('/_authenticated/system/role/')

export function SysRole() {
  const search = route.useSearch()
  const navigate = route.useNavigate()
  const [refreshKey, setRefreshKey] = useState(0)

  // 构建查询参数
  const queryParams: RoleQueryRequest = useMemo(() => ({
    current: search.page || 1,
    size: search.pageSize || 10,
    roleName: search.roleName || undefined,
    status: search.status && search.status.length > 0 ? search.status[0] : undefined,
  }), [search.page, search.pageSize, search.roleName, search.status])

  // 获取角色列表
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['systemRoles', queryParams, refreshKey],
    queryFn: () => roleApi.list(queryParams),
    staleTime: 5000,
  })

  // 刷新数据
  const refresh = () => {
    setRefreshKey((prev) => prev + 1)
    refetch()
  }

  useEffect(() => {
    if (error) {
      toast.error('获取角色列表失败')
    }
  }, [error])

  return (
    <SysRoleProvider refresh={refresh}>
      <Header fixed>
        <Search />
      </Header>

      <Main className='flex flex-1 flex-col gap-4 sm:gap-6'>
        {/* 操作区 */}
        <SysRoleActionBar
          search={search}
          navigate={navigate}
          loading={isLoading}
        />

        {/* 表格 */}
        <div className='bg-background p-4 rounded-lg border'>
          <SysRoleTable 
            data={data?.records || []} 
            total={data?.total || 0}
            loading={isLoading}
            search={search} 
            navigate={navigate} 
          />
        </div>
      </Main>

      <SysRoleDialogs />
    </SysRoleProvider>
  )
}

