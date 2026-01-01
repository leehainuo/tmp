import { useMemo } from 'react'
import { ChevronsUpDown } from 'lucide-react'
import { useNavigate, useLocation } from '@tanstack/react-router'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuShortcut,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from '@/components/ui/sidebar'

type AboutMenuProps = {
  items: {
    name: string
    logo: React.ElementType
    plan: string
  }[]
} 

export function AboutMenu({ items }: AboutMenuProps) {
  const { isMobile } = useSidebar()
  const navigate = useNavigate()
  const location = useLocation()

  // 根据当前路由派生选中项，避免在 effect 中调用 setState
  const activeItem = useMemo(() => {
    const pathname = location.pathname
    
    // 判断当前路由对应的菜单项
    if (pathname.startsWith('/embed/')) {
      // 内嵌网页 -> 开发者
      return items.find(item => item.name === '开发者') || items[0]
    } else if (pathname === '/help-center') {
      // 帮助中心 -> 文档
      return items.find(item => item.name === '文档') || items[0]
    } else {
      // 其他路由（包括首页） -> Miku
      return items.find(item => item.name === 'Miku') || items[0]
    }
  }, [location.pathname, items])

  const handleItemClick = (item: typeof items[0]) => {
    // 根据不同的选项进行导航
    if (item.name === 'Miku') {
      navigate({ to: '/' })
    } else if (item.name === '开发者') {
      // 导航到内嵌网页，开发者官网地址
      navigate({ to: '/404' })
    } else if (item.name === '文档') {
      navigate({ to: '/help-center' })
    }
  }

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <SidebarMenuButton
              size='lg'
              className='data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground'
            >
              <div className='bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-md'>
                <activeItem.logo className='size-6' />
              </div>
              <div className='grid flex-1 text-start text-sm leading-tight'>
                <span className='truncate font-semibold'>
                  {activeItem.name}
                </span>
                <span className='truncate text-xs'>{activeItem.plan}</span>
              </div>
              <ChevronsUpDown className='ms-auto' />
            </SidebarMenuButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent
            className='w-(--radix-dropdown-menu-trigger-width) min-w-56 rounded-lg'
            align='start'
            side={isMobile ? 'bottom' : 'right'}
            sideOffset={4}
          >
            <DropdownMenuLabel className='text-muted-foreground text-xs'>
              关于
            </DropdownMenuLabel>
            {items.map((item, index) => (
              <DropdownMenuItem
                key={item.name}
                onClick={() => handleItemClick(item)}
                className='gap-2 p-2'
              >
                <div className='flex size-6 items-center justify-center rounded-sm border'>
                  <item.logo className='size-4 shrink-0' />
                </div>
                {item.name}
                <DropdownMenuShortcut>⌘{index + 1}</DropdownMenuShortcut>
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}
