import { Star } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import { Button } from '@/components/ui/button'
import { ProfileDropdown } from '../profile-dropdown'
import { ThemeSwitch } from '../theme-switch'
import { ConfigDrawer } from '../config-drawer'

type HeaderProps = React.HTMLAttributes<HTMLElement> & {
  fixed?: boolean
  ref?: React.Ref<HTMLElement>
}

export function Header({ className, fixed, children, ...props }: HeaderProps) {

  return (
    <header
      className={cn(
        'z-50 bg-background border-b',
        fixed && 'header-fixed peer/header sticky top-0',
        className
      )}
      {...props}
    >
      <div className='relative mx-auto flex w-[calc(100%-2rem)] items-center justify-between py-2 sm:w-[calc(100%-3rem)]'>
      {/* <div className='bg-card relative mx-auto mt-3 flex w-[calc(100%-2rem)] items-center justify-between rounded-xl border px-6 py-2 sm:w-[calc(100%-3rem)]'> */}
        <div className='flex items-center gap-1.5 sm:gap-4'>
          <SidebarTrigger variant='outline' className='size-7' />
          <Separator orientation='vertical' className='hidden h-4 sm:block' />
          {children}
        </div>

        <div className='flex items-center gap-1.5'>
          {/* <Button variant='ghost' size='icon' className='size-9'>
            <Languages className='size-4' aria-hidden='true' />
            <span className='sr-only'>切换语言</span>
          </Button>
          <Button variant='ghost' size='icon' className='size-9'>
            <Activity className='size-4' aria-hidden='true' />
            <span className='sr-only'>活动</span>
          </Button> */}
          <Button variant='ghost' size='icon' className='relative size-9' asChild>
            <a href='https://github.com/leehainuo/tmp' target='_blank' rel='noopener noreferrer' className='relative size-9'>
              <Star className='size-4' aria-hidden='true' />
              <span className='sr-only'>Star</span>
            </a>
          </Button>
          <ThemeSwitch />
          <ConfigDrawer />
          <ProfileDropdown />
        </div>
      </div>
    </header>
  )
}
