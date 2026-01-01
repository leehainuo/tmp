import { useEffect, useMemo, useState } from 'react'
import { getRouteApi } from '@tanstack/react-router'
import { useQuery } from '@tanstack/react-query'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Search } from '@/components/search'
import { SysUserDialogs } from './components/sys-user-dialogs'
import { SysUserProvider } from './providers/sys-user-provider'
import { SysUserTable } from './components/sys-user-table'
import { SysUserActionBar } from './components/sys-user-action-bar'
import { userApi, type UserQueryRequest } from '@/lib/api'
import { toast } from 'sonner'

const route = getRouteApi('/_authenticated/system/user/')

export function SysUser() {
  const search = route.useSearch()
  const navigate = route.useNavigate()
  const [refreshKey, setRefreshKey] = useState(0)

  
  const queryParams: UserQueryRequest = useMemo(() => ({
    current: search.page || 1,
    size: search.pageSize || 10,
    username: search.username || undefined,
    phone: search.phone || undefined,
    status: search.status && search.status.length > 0 ? search.status[0] : undefined,
  }), [search.page, search.pageSize, search.username, search.phone, search.status])

  // 获取用户列表
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['systemUsers', queryParams, refreshKey],
    queryFn: () => userApi.list(queryParams),
    staleTime: 5000,
  })

  // 刷新数据
  const refresh = () => {
    setRefreshKey((prev) => prev + 1)
    refetch()
  }

  useEffect(() => {
    if (error) {
      toast.error('获取用户列表失败')
    }
  }, [error])

  return (
    <SysUserProvider refresh={refresh}>
      <Header fixed>
        <Search />
      </Header>

      <Main className='flex flex-1 flex-col gap-4 sm:gap-6'>
        {/* 操作区 */}
        <SysUserActionBar
          search={search}
          navigate={navigate}
          loading={isLoading}
        />

        {/* 表格 */}
        <div className='bg-background p-4 rounded-lg border'>
          <SysUserTable 
            data={data?.records || []} 
            total={data?.total || 0}
            loading={isLoading}
            search={search} 
            navigate={navigate}
          />
        </div>
      </Main>

      <SysUserDialogs />
    </SysUserProvider>
  )
}