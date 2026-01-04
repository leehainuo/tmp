import { useEffect, useMemo, useState } from 'react'
import { getRouteApi } from '@tanstack/react-router'
import { useQuery } from '@tanstack/react-query'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Search } from '@/components/search'
import { toast } from 'sonner'
import { logApi, type OperationLogQueryRequest } from '@/lib/api'
import { SysLogProvider } from './providers/sys-log-provider'
import { SysLogActionBar } from './components/sys-log-action-bar'
import { SysLogTable } from './components/sys-log-table'
import { SysLogDialogs } from './components/sys-log-dialogs'

const route = getRouteApi('/_authenticated/system/log/')

export function SysLog() {
  const search = route.useSearch()
  const navigate = route.useNavigate()
  const [refreshKey, setRefreshKey] = useState(0)

  const queryParams: OperationLogQueryRequest = useMemo(
    () => ({
      current: (search.page as number | undefined) || 1,
      size: (search.pageSize as number | undefined) || 10,
      title: (search.title as string | undefined) || undefined,
      operName: (search.operName as string | undefined) || undefined,
    }),
    [search.page, search.pageSize, search.title, search.operName]
  )

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['systemLogs', queryParams, refreshKey],
    queryFn: () => logApi.list(queryParams),
    staleTime: 5000,
  })

  const refresh = () => {
    setRefreshKey((prev) => prev + 1)
    refetch()
  }

  useEffect(() => {
    if (error) {
      toast.error('获取操作日志失败')
    }
  }, [error])

  return (
    <SysLogProvider refresh={refresh}>
      <Header fixed>
        <Search />
      </Header>

      <Main className='flex flex-1 flex-col gap-4 sm:gap-6'>
        <SysLogActionBar search={search} navigate={navigate} loading={isLoading} />

        <div className='bg-background p-4 rounded-lg border'>
          <SysLogTable
            data={data?.records || []}
            total={data?.total || 0}
            loading={isLoading}
            search={search}
            navigate={navigate}
          />
        </div>
      </Main>

      <SysLogDialogs />
    </SysLogProvider>
  )
}


