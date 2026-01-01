import { useEffect, useMemo, useState } from 'react'
import { getRouteApi } from '@tanstack/react-router'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Search } from '@/components/search'
import { SysDeptDialogs } from './components/sys-dept-dialogs'
import { SysDeptProvider } from './providers/sys-dept-provider'
import { SysDeptTable } from './components/sys-dept-table'
import { SysDeptActionBar } from './components/sys-dept-action-bar'
import { deptApi, type DeptQueryRequest } from '@/lib/api'
import { useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'

const route = getRouteApi('/_authenticated/system/dept/')

export function SysDept() {
  const search = route.useSearch()
  const navigate = route.useNavigate()
  const [refreshKey, setRefreshKey] = useState(0)

  // 构建查询参数
  const queryParams: DeptQueryRequest = useMemo(() => ({
    deptName: search.deptName || undefined,
    status: search.status && search.status.length > 0 ? search.status[0] : undefined,
  }), [search.deptName, search.status])

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['systemDepts', queryParams, refreshKey],
    queryFn: () => deptApi.listTree(queryParams),
    staleTime: 5000,
  })

  const refresh = () => {
    setRefreshKey((prev) => prev + 1)
    refetch()
  }

  useEffect(() => {
    if (error) {
      toast.error('获取部门列表失败')
    }
  }, [error])

  return (
    <SysDeptProvider refresh={refresh}>
      <Header fixed>
        <Search />
      </Header>

      <Main className='flex flex-1 flex-col gap-4 sm:gap-6'>
        {/* 操作区 */}
        <SysDeptActionBar
          search={search}
          navigate={navigate}
          loading={isLoading}
        />

        {/* 表格 */}
        <div className='bg-background p-4 rounded-lg border'>
          <SysDeptTable
            data={data || []}
            loading={isLoading}
            search={search}
            navigate={navigate}
          />
        </div>
      </Main>

      <SysDeptDialogs />
    </SysDeptProvider>
  )
}


