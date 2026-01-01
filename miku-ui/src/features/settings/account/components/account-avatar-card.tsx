import { useState, useRef, useEffect } from 'react'
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar'
import { cn } from '@/lib/utils'
import SettingsCard from '@/features/settings/components/settings-card'
import { userApi } from '@/lib/api'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/lib/stores/auth-store'
import type { UserInfoResponse } from '@/lib/types'
import { toast } from 'sonner'
import { Loader2 } from 'lucide-react'

type UploadedFileInfo = {
  url?: string
  path?: string
  [k: string]: unknown
}

type AccountAvatarCardProps = {
  src?: string
  alt?: string
}

export function AccountAvatarCard({ src, alt = 'Avatar' }: AccountAvatarCardProps) {
  const [preview, setPreview] = useState<string | undefined>(src)
  const [isUploading, setIsUploading] = useState(false)
  const inputRef = useRef<HTMLInputElement | null>(null)
  const queryClient = useQueryClient()
  const { auth } = useAuthStore()

  function onButtonClick() {
    inputRef.current?.click()
  }

  useEffect(() => {
    setPreview(src)
  }, [src])

  // atomic avatar upload: upload file and update user in one request
  const uploadAvatarMutation = useMutation<UploadedFileInfo, unknown, File>({
    mutationFn: (file: File) => {
      return userApi.uploadAvatar(file)
    },
    onMutate: () => {
      setIsUploading(true)
    },
    onSuccess: (fileInfo: UploadedFileInfo) => {
      // update auth store immediately and refresh profile cache
      const url = (fileInfo && (fileInfo.url as string | undefined)) || (fileInfo?.path as string | undefined)
      const currentFull: UserInfoResponse = auth.fullUserInfo ?? ({
        userId: auth.user?.userId,
        username: auth.user?.username ?? '',
        nickname: auth.user?.nickname ?? '',
        avatar: auth.user?.avatar,
        email: auth.user?.email,
        phone: auth.user?.phone,
        deptId: auth.user?.deptId,
        deptName: auth.user?.deptName,
        roles: auth.user?.roles ?? [],
        permissions: [],
        menus: [],
      } as UserInfoResponse)
      const updatedFull = { ...currentFull, avatar: url }
      auth.setUser?.(updatedFull)
      toast.success('头像已更新')
      queryClient.invalidateQueries({ queryKey: ['accountProfile'] })
    },
    onError: (err: unknown) => {
      const message = err instanceof Error ? err.message : '上传失败'
      toast.error(message)
    },
    onSettled: () => {
      setIsUploading(false)
    },
  })

 

  async function handleFile(file?: File | null) {
    if (!file) return
    // show local preview immediately
    const objectUrl = URL.createObjectURL(file)
    setPreview(objectUrl)

    try {
      await uploadAvatarMutation.mutateAsync(file)
    } catch {
      // errors handled by mutation onError
    } finally {
      // revoke object URL after some time to free memory (keep preview for UX)
      setTimeout(() => {
        URL.revokeObjectURL(objectUrl)
      }, 10000)
    }
  }

  function onInputChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null
    handleFile(file)
    // clear the input value so selecting same file again triggers change
    e.currentTarget.value = ''
  }

  return (
    <SettingsCard
      title={<span>头像</span>}
      description={
        <span>
          这是你的头像。
          <br />
          点击头像即可从您的文件中上传自定义头像。
        </span>
      }
      footer={<span>头像为可选项，但强烈建议设置。</span>}
    >
      <div className="flex-none">
        <div className="avatar">
          <button
            type="button"
            onClick={onButtonClick}
            aria-label="Upload avatar"
            className={cn("geist-reset inline-flex items-center justify-center rounded-full overflow-hidden", "focus:outline-none focus:ring-2 focus:ring-ring")}
            style={{ width: 78, height: 78 }}
          >
            <input
              ref={inputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={onInputChange}
            />
            <Avatar className="size-19.5">
              {preview ? <AvatarImage src={preview} alt={alt} /> : <AvatarFallback>{(alt && alt[0]) || "?"}</AvatarFallback>}
            </Avatar>
            {isUploading ? (
              <div className="absolute inset-0 flex items-center justify-center bg-white/60">
                <Loader2 className="animate-spin" />
              </div>
            ) : null}
          </button>
        </div>
      </div>
    </SettingsCard>
  )
}


