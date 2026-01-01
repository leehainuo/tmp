import { type ChangeEvent, useState } from 'react'
import { getRouteApi } from '@tanstack/react-router'
import { SlidersHorizontal, ArrowUpAZ, ArrowDownAZ } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Search } from '@/components/search'
import { apps } from './data/apps'

const route = getRouteApi('/_authenticated/apps/')

type AppType = 'all' | 'connected' | 'notConnected'

const appText = new Map<AppType, string>([
  ['all', '所有应用'],
  ['connected', '已连接'],
  ['notConnected', '未连接'],
])

export function Apps() {
  const {
    filter = '',
    type = 'all',
    sort: initSort = 'asc',
  } = route.useSearch()
  const navigate = route.useNavigate()

  const [sort, setSort] = useState(initSort)
  const [appType, setAppType] = useState(type)
  const [searchTerm, setSearchTerm] = useState(filter)

  const filteredApps = apps
    .sort((a, b) =>
      sort === 'asc'
        ? a.name.localeCompare(b.name)
        : b.name.localeCompare(a.name)
    )
    .filter((app) =>
      appType === 'connected'
        ? app.connected
        : appType === 'notConnected'
          ? !app.connected
          : true
    )
    .filter((app) => app.name.toLowerCase().includes(searchTerm.toLowerCase()))

  const handleSearch = (e: ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value)
    navigate({
      search: (prev) => ({
        ...prev,
        filter: e.target.value || undefined,
      }),
    })
  }

  const handleTypeChange = (value: AppType) => {
    setAppType(value)
    navigate({
      search: (prev) => ({
        ...prev,
        type: value === 'all' ? undefined : value,
      }),
    })
  }

  const handleSortChange = (sort: 'asc' | 'desc') => {
    setSort(sort)
    navigate({ search: (prev) => ({ ...prev, sort }) })
  }

  return (
    <>
      {/* ===== Top Heading ===== */}
      <Header fixed>
        <Search />
      </Header>

      {/* ===== Content ===== */}
      <Main fixed>

        <div className='bg-background rounded-lg border p-4 my-4 flex items-end justify-between sm:items-center'>
          <div className='flex flex-col gap-4 sm:my-4 sm:flex-row'>
            <Input
              placeholder='搜索应用...'
              className='h-9 w-40 lg:w-[250px]'
              value={searchTerm}
              onChange={handleSearch}
            />
            <Select value={appType} onValueChange={handleTypeChange}>
              <SelectTrigger className='w-36'>
                <SelectValue>{appText.get(appType)}</SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>所有应用</SelectItem>
                <SelectItem value='connected'>已连接</SelectItem>
                <SelectItem value='notConnected'>未连接</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <Select value={sort} onValueChange={handleSortChange}>
            <SelectTrigger className='w-16'>
              <SelectValue>
                <SlidersHorizontal size={18} />
              </SelectValue>
            </SelectTrigger>
            <SelectContent align='end'>
              <SelectItem value='asc'>
                <div className='flex items-center gap-4'>
                  <ArrowUpAZ size={16} />
                  <span>升序</span>
                </div>
              </SelectItem>
              <SelectItem value='desc'>
                <div className='flex items-center gap-4'>
                  <ArrowDownAZ size={16} />
                  <span>降序</span>
                </div>
              </SelectItem>
            </SelectContent>
          </Select>
        </div>
        <Separator className='shadow-sm' />
        <ul className='faded-bottom no-scrollbar grid gap-4 overflow-auto pt-4 pb-16 md:grid-cols-2 lg:grid-cols-3'>
          {filteredApps.map((app) => (
            <li
              key={app.name}
              className='bg-background rounded-lg border p-4 hover:shadow-md'
            >
              <div className='mb-8 flex items-center justify-between'>
                <div
                  className={`bg-muted flex size-10 items-center justify-center rounded-lg p-2`}
                >
                  {app.logo}
                </div>
                <Button
                  variant='outline'
                  size='sm'
                  className={`w-20 ${app.connected ? 'border border-blue-300 bg-blue-50 hover:bg-blue-100 dark:border-blue-700 dark:bg-blue-950 dark:hover:bg-blue-900' : ''}`}
                >
                  {app.connected ? '已连接' : '连接'}
                </Button>
              </div>
              <div>
                <h2 className='mb-1 font-semibold'>{app.name}</h2>
                <p className='line-clamp-2 text-gray-500'>{app.desc}</p>
              </div>
            </li>
          ))}
        </ul>
      </Main>
    </>
  )
}
