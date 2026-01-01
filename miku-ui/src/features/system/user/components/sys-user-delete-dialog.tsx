'use client'

import { useState } from 'react'
import { AlertTriangle } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { type User } from '../data/schema'
import { userApi } from '@/lib/api'
import { useSysUser } from '../providers/sys-user-provider'
import { toast } from 'sonner'

type SysUserDeleteDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentRow: User
}

export function SysUserDeleteDialog({
  open,
  onOpenChange,
  currentRow,
}: SysUserDeleteDialogProps) {
  const [value, setValue] = useState('')
  const [loading, setLoading] = useState(false)
  const { refresh } = useSysUser()

  const handleDelete = async () => {
    if (value.trim() !== currentRow.username) return

    const userId = currentRow.id || currentRow.userId
    if (!userId) {
      toast.error('用户ID不存在')
      return
    }

    setLoading(true)
    try {
      await userApi.remove(userId)
      toast.success('删除用户成功')
      onOpenChange(false)
      setValue('')
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '删除用户失败'
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ConfirmDialog
      open={open}
      onOpenChange={onOpenChange}
      handleConfirm={handleDelete}
      disabled={value.trim() !== currentRow.username || loading}
      title={
        <span className='text-destructive'>
          <AlertTriangle
            className='stroke-destructive me-1 inline-block'
            size={18}
          />{' '}
          删除用户
        </span>
      }
      desc={
        <div className='space-y-4'>
          <p className='mb-2'>
            确定要删除用户{' '}
            <span className='font-bold'>{currentRow.username}</span> 吗？
            <br />
            此操作将永久删除该用户，且无法恢复。
          </p>

          <Label className='my-2'>
            请输入用户名确认删除：
          </Label>
          <Input
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder='请输入用户名'
              disabled={loading}
            />

          <Alert variant='destructive'>
            <AlertTitle>警告！</AlertTitle>
            <AlertDescription>
              请谨慎操作，此操作无法撤销。
            </AlertDescription>
          </Alert>
        </div>
      }
      confirmText={loading ? '删除中...' : '删除'}
      destructive
    />
  )
}

