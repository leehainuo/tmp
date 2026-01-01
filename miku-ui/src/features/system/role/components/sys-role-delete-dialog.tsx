'use client'

import { useState } from 'react'
import { AlertTriangle } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { type Role } from '../data/schema'
import { roleApi } from '@/lib/api'
import { useSysRole } from '../providers/sys-role-provider'
import { toast } from 'sonner'

type SysRoleDeleteDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentRow: Role
}

export function SysRoleDeleteDialog({
  open,
  onOpenChange,
  currentRow,
}: SysRoleDeleteDialogProps) {
  const [value, setValue] = useState('')
  const [loading, setLoading] = useState(false)
  const { refresh } = useSysRole()

  const handleDelete = async () => {
    if (value.trim() !== currentRow.roleName) return

    const roleId = currentRow.id
    if (!roleId) {
      toast.error('角色ID不存在')
      return
    }

    setLoading(true)
    try {
      await roleApi.remove(roleId)
      toast.success('删除角色成功')
      onOpenChange(false)
      setValue('')
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '删除角色失败'
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
      disabled={value.trim() !== currentRow.roleName || loading}
      title={
        <span className='text-destructive'>
          <AlertTriangle
            className='stroke-destructive me-1 inline-block'
            size={18}
          />{' '}
          删除角色
        </span>
      }
      desc={
        <div className='space-y-4'>
          <p className='mb-2'>
            确定要删除{' '}
            <span className='font-bold'>{currentRow.roleName}</span> 吗？
            <br />
            此操作将永久删除该角色，且无法恢复。
          </p>

          <Label className='my-2'>
            请输入角色名称确认删除：
          </Label>
          <Input
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder='请输入角色名称'
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

