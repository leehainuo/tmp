'use client'

import { useState } from 'react'
import { type Table } from '@tanstack/react-table'
import { AlertTriangle } from 'lucide-react'
import { toast } from 'sonner'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { logApi } from '@/lib/api'
import { useSysLog } from '../providers/sys-log-provider'

type SysLogMultiDeleteDialogProps<TData> = {
  open: boolean
  onOpenChange: (open: boolean) => void
  table: Table<TData>
}

const CONFIRM_WORD = 'DELETE'

export function SysLogMultiDeleteDialog<TData>({
  open,
  onOpenChange,
  table,
}: SysLogMultiDeleteDialogProps<TData>) {
  const [value, setValue] = useState('')
  const [loading, setLoading] = useState(false)
  const { refresh } = useSysLog()

  const selectedRows = table.getFilteredSelectedRowModel().rows

  const handleDelete = async () => {
    if (value.trim() !== CONFIRM_WORD) {
      toast.error(`请输入 "${CONFIRM_WORD}" 以确认删除`)
      return
    }

    const ids = selectedRows
      .map((row) => {
        const log = row.original as { id?: number }
        return log.id
      })
      .filter((id): id is number => typeof id === 'number')

    if (ids.length === 0) {
      toast.error('未找到可删除的日志ID')
      return
    }

    setLoading(true)
    try {
      await logApi.batchRemove(ids)
      toast.success(`成功删除 ${ids.length} 条日志`)
      table.resetRowSelection()
      onOpenChange(false)
      setValue('')
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
      disabled={value.trim() !== CONFIRM_WORD || loading}
      title={
        <span className='text-destructive'>
          <AlertTriangle className='stroke-destructive me-1 inline-block' size={18} /> 删除{' '}
          {selectedRows.length} 条日志
        </span>
      }
      desc={
        <div className='space-y-4'>
          <p className='mb-2'>
            确定要删除选中的 {selectedRows.length} 条操作日志吗？
            <br />
            此操作将永久删除这些日志记录，且无法恢复。
          </p>
          <Label className='my-4 flex flex-col items-start gap-1.5'>
            <span>请输入 "{CONFIRM_WORD}" 以确认删除：</span>
            <Input
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder={`请输入 "${CONFIRM_WORD}" 以确认`}
              disabled={loading}
            />
          </Label>
          <Alert variant='destructive'>
            <AlertTitle>警告！</AlertTitle>
            <AlertDescription>请谨慎操作，此操作无法撤销。</AlertDescription>
          </Alert>
        </div>
      }
      confirmText={loading ? '删除中...' : '删除'}
      destructive
    />
  )
}


