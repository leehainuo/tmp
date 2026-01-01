'use client'

import { useState } from 'react'
import { AlertTriangle } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { type Permission } from '../data/schema'
import { permissionApi } from '@/lib/api'
import { useSysPermission } from '../providers/sys-permission-provider'
import { toast } from 'sonner'

type SysPermissionDeleteDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentRow: Permission
}

export function SysPermissionDeleteDialog({
  open,
  onOpenChange,
  currentRow,
}: SysPermissionDeleteDialogProps) {
  const [value, setValue] = useState('')
  const [loading, setLoading] = useState(false)
  const { refresh } = useSysPermission()

  const handleDelete = async () => {
    if (value.trim() !== currentRow.permissionName) return

    const permissionId = currentRow.id
    if (!permissionId) {
      toast.error('权限ID不存在')
      return
    }

    setLoading(true)
    try {
      await permissionApi.remove(permissionId)
      toast.success('删除权限成功')
      onOpenChange(false)
      setValue('')
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '删除权限失败'
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
      disabled={value.trim() !== currentRow.permissionName || loading}
      title={
        <span className='text-destructive'>
          <AlertTriangle
            className='stroke-destructive me-1 inline-block'
            size={18}
          />{' '}
          删除权限
        </span>
      }
      desc={
        <div className='space-y-4'>
          <p className='mb-2'>
            确定要删除权限{' '}
            <span className='font-bold'>{currentRow.permissionName}</span> 吗？
            <br />
            此操作将永久删除该权限，且无法恢复。
          </p>

          <Label className='my-2'>
            请输入权限名称确认删除：
          </Label>
          <Input
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder='请输入权限名称'
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

