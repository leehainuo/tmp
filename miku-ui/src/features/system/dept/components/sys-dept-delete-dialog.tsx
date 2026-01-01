'use client'

import { useState } from 'react'
import { AlertTriangle } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { type Dept } from '../data/schema'
import { deptApi } from '@/lib/api'
import { useSysDept } from '../providers/sys-dept-provider'
import { toast } from 'sonner'

type SysDeptDeleteDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentRow: Dept
}

export function SysDeptDeleteDialog({
  open,
  onOpenChange,
  currentRow,
}: SysDeptDeleteDialogProps) {
  const [value, setValue] = useState('')
  const [loading, setLoading] = useState(false)
  const { refresh } = useSysDept()

  const handleDelete = async () => {
    if (value.trim() !== currentRow.deptName) return

    const deptId = currentRow.id
    if (!deptId) {
      toast.error('部门ID不存在')
      return
    }

    setLoading(true)
    try {
      await deptApi.remove(deptId)
      toast.success('删除部门成功')
      onOpenChange(false)
      setValue('')
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '删除部门失败'
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
      disabled={value.trim() !== currentRow.deptName || loading}
      title={
        <span className='text-destructive'>
          <AlertTriangle
            className='stroke-destructive me-1 inline-block'
            size={18}
          />{' '}
          删除部门
        </span>
      }
      desc={
        <div className='space-y-4'>
          <p className='mb-2'>
            确定要删除部门{' '}
            <span className='font-bold'>{currentRow.deptName}</span> 吗？
            <br />
            此操作将永久删除该部门，且无法恢复。
          </p>

          <Label className='my-2'>
            请输入部门名称确认删除：
          </Label>
          <Input
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder='请输入部门名称'
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


