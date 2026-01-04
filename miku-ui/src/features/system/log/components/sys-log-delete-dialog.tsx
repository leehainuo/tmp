'use client'

import { useState } from 'react'
import { AlertTriangle } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { logApi } from '@/lib/api'
import { useSysLog } from '../providers/sys-log-provider'
import type { Log } from '../data/schema'
import { toast } from 'sonner'

type SysLogDeleteDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  currentRow: Log
}

export function SysLogDeleteDialog({
  open,
  onOpenChange,
  currentRow,
}: SysLogDeleteDialogProps) {
  const [loading, setLoading] = useState(false)
  const { refresh } = useSysLog()

  const handleDelete = async () => {
    if (!currentRow.id) {
      toast.error('日志ID不存在')
      return
    }

    setLoading(true)
    try {
      await logApi.remove(currentRow.id)
      toast.success('删除日志成功')
      onOpenChange(false)
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : '删除日志失败'
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
      disabled={loading}
      title={
        <span className='text-destructive'>
          <AlertTriangle className='stroke-destructive me-1 inline-block' size={18} /> 删除日志
        </span>
      }
      desc={
        <div className='space-y-4'>
          <p className='mb-2'>
            确定要删除该条操作日志吗？
            <br />
            模块：<span className='font-bold'>{currentRow.title || '-'}</span>，
            操作员：<span className='font-bold'>{currentRow.operName || '-'}</span>
          </p>
          <Alert variant='destructive'>
            <AlertTitle>警告！</AlertTitle>
            <AlertDescription>此操作将永久删除该日志记录，且无法恢复。</AlertDescription>
          </Alert>
        </div>
      }
      confirmText={loading ? '删除中...' : '删除'}
      destructive
    />
  )
}


