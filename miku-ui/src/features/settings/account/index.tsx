import { ContentSection } from '../components/content-section'
import { AccountAvatarCard } from './components/account-avatar-card'
import { AccountNicknameCard } from './components/account-nickname-card'
import { AccountPasswordCard } from './components/account-password-card'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { userApi } from '@/lib/api'
import { useAuthStore } from '@/lib/stores/auth-store'
import type { SysUser, UserInfoResponse } from '@/lib/types'
import { toast } from 'sonner'

export function SettingsAccount() {
  const { auth } = useAuthStore()
  const cachedUser = auth.user

  const queryClient = useQueryClient()

  // Use react-query to fetch profile; use cached basic user info as initialData so avatar appears immediately on refresh
  const cachedInitial = cachedUser
    ? ({ ...cachedUser, permissions: [], menus: [] } as UserInfoResponse)
    : undefined

  const [nickResetKey, setNickResetKey] = useState<number>(0)
  const [pwResetKey, setPwResetKey] = useState<number>(0)

  const { data: profile } = useQuery<UserInfoResponse>({
    queryKey: ['accountProfile'],
    queryFn: () => userApi.getProfile(),
    staleTime: 5000,
    initialData: cachedInitial,
  })

  // keep auth store in sync when fresh profile is loaded
  useEffect(() => {
    if (!profile) return

    // avoid updating auth store if the profile is identical to current basic info
    const currentId = auth.user?.userId ?? auth.fullUserInfo?.userId
    if (currentId && profile.userId && currentId === profile.userId) return

    auth.setUser?.(profile)
  }, [profile, auth])

  const updateProfileMutation = useMutation({
    mutationFn: (patch: Partial<SysUser>) => userApi.updateProfile(patch),
    onSuccess: () => {
      toast.success('保存成功')
      // refresh profile after successful update
      queryClient.invalidateQueries({ queryKey: ['accountProfile'] })
    },
    onError: (err: unknown) => {
      const message = err instanceof Error ? err.message : '保存失败'
      toast.error(message)
    },
  })

  const changePasswordMutation = useMutation({
    mutationFn: ({ userId, newPassword }: { userId: number; newPassword: string }) =>
      userApi.changePassword(userId, newPassword),
    onSuccess: () => toast.success('密码已修改'),
    onError: (err: unknown) => {
      const message = err instanceof Error ? err.message : '修改密码失败'
      toast.error(message)
    },
  })

  return (
    <ContentSection>
      {/* 显示头像但不预填表单数据 */}
      <AccountAvatarCard src={profile?.avatar ?? cachedUser?.avatar} alt={profile?.nickname || cachedUser?.nickname || profile?.username} />
      <AccountNicknameCard

        onSave={(value: string) => {
          const userId = profile?.userId ?? cachedUser?.userId
          if (!userId) {
            toast.error('未获取到用户 ID，无法保存')
            return
          }
          updateProfileMutation
            .mutateAsync({ id: userId, nickname: value })
            .then(() => setNickResetKey((k) => k + 1))
        }}
        resetKey={nickResetKey}
      />
      <AccountPasswordCard
        onChangePassword={(newPassword: string) =>
          {
            const userId = profile?.userId ?? cachedUser?.userId
            if (!userId) {
              toast.error('未获取到用户 ID，无法修改密码')
              return
            }
            changePasswordMutation
              .mutateAsync({ userId: userId as number, newPassword })
              .then(() => setPwResetKey((k) => k + 1))
          }
        }
        resetKey={pwResetKey}
      />
    </ContentSection>
  )
}
