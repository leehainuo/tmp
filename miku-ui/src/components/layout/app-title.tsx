import { Link } from '@tanstack/react-router'
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from '@/components/ui/sidebar'
import { Logo } from '@/assets/logo'

export function AppTitle() {
  const { setOpenMobile } = useSidebar()
  return (
    <SidebarMenu>
      <SidebarMenuItem className='h-12'>
        <SidebarMenuButton
          size='lg'  // h-8
          className='p-0 h-8 hover:bg-background'
          asChild
        >
          <div>
            <div className='text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-md'>
              <Logo className='size-8' />
            </div>
            <Link
              to='/'
              onClick={() => setOpenMobile(false)}
              className='grid flex-1 text-start text-sm leading-tight'
            >
              <span className='truncate font-bold'>Miku</span>
            </Link>
          </div>
        </SidebarMenuButton>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}
