import { Link } from '@tanstack/react-router'
import useDialogState from '@/hooks/use-dialog-state'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { SignOutDialog } from '@/components/sign-out-dialog'
import { useQuery } from '@tanstack/react-query'
import { userApi } from '@/lib/api'
import { useAuthStore } from '@/lib/stores/auth-store'
import type { UserInfoResponse } from '@/lib/types'

export function ProfileDropdown() {
  const [open, setOpen] = useDialogState()
  const { auth } = useAuthStore()
  const cachedUser = auth.user

  const cachedInitial = cachedUser
    ? ({ ...cachedUser, permissions: [], menus: [] } as UserInfoResponse)
    : undefined

  const { data: profile } = useQuery<UserInfoResponse>({
    queryKey: ['profile'],
    queryFn: () => userApi.getProfile(),
    staleTime: 5_000,
    initialData: cachedInitial,
  })

  return (
    <>
      <DropdownMenu modal={false}>
        <DropdownMenuTrigger asChild>
          <Button variant='ghost' className='relative h-8 w-8 rounded-md'>
            <Avatar className='h-8 w-8 rounded-md'>
              <AvatarImage
               src={profile?.avatar ?? cachedUser?.avatar ?? '/avatars/01.png'}
               alt={profile?.nickname ?? cachedUser?.nickname ?? profile?.username ?? 'User'} 
              />
              <AvatarFallback className='rounded-md'>
                {(profile?.nickname ?? cachedUser?.nickname ?? profile?.username ?? 'M').slice(0, 1)}
              </AvatarFallback>
            </Avatar>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className='w-56' align='end' forceMount>
          <DropdownMenuLabel className='font-normal'>
            <div className='flex flex-col gap-1.5'>
              <p className='text-sm leading-none font-medium'>
                {profile?.nickname ?? cachedUser?.nickname ?? profile?.username ?? 'Miku'}
              </p>
              <p className='text-muted-foreground text-xs leading-none'>
                {profile?.email ?? cachedUser?.email ?? ''}
              </p>
            </div>
          </DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            <DropdownMenuItem asChild>
              <Link to='/settings/account'>
                账户设置
                <DropdownMenuShortcut>⌘B</DropdownMenuShortcut>
              </Link>
            </DropdownMenuItem>
            <DropdownMenuItem asChild>
              <Link to='/settings/appearance'>
                系统设置
                <DropdownMenuShortcut>⌘S</DropdownMenuShortcut>
              </Link>
            </DropdownMenuItem>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuItem variant='destructive' onClick={() => setOpen(true)}>
            退出登录
            <DropdownMenuShortcut className='text-current'>
              ⇧⌘Q
            </DropdownMenuShortcut>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <SignOutDialog open={!!open} onOpenChange={setOpen} />
    </>
  )
}
