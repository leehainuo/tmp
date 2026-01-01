'use client'

import { useState } from 'react'
import { type Table } from '@tanstack/react-table'
import { AlertTriangle } from 'lucide-react'
import { toast } from 'sonner'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { deptApi } from '@/lib/api'
import { useSysDept } from '../providers/sys-dept-provider'
import { type FlatDept } from './sys-dept-columns'

type SysDeptMultiDeleteDialogProps<TData> = {
  open: boolean
  onOpenChange: (open: boolean) => void
  table: Table<TData>
}

const CONFIRM_WORD = 'DELETE'

export function SysDeptMultiDeleteDialog<TData>({
  open,
  onOpenChange,
  table,
}: SysDeptMultiDeleteDialogProps<TData>) {
  const [value, setValue] = useState('')
  const [loading, setLoading] = useState(false)
  const { refresh } = useSysDept()

  const selectedRows = table.getFilteredSelectedRowModel().rows

  const handleDelete = async () => {
    if (value.trim() !== CONFIRM_WORD) {
      toast.error(`请输入 "${CONFIRM_WORD}" 以确认删除`)
      return
    }

    setLoading(true)
    try {
      const deletePromises = selectedRows.map((row) => {
        const dept = row.original as FlatDept
        const deptId = dept.id
        if (!deptId) {
          return Promise.reject(new Error('部门ID不存在'))
        }
        return deptApi.remove(deptId)
      })

      await Promise.all(deletePromises)
      toast.success(`成功删除 ${selectedRows.length} 个部门`)
      table.resetRowSelection()
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
      disabled={value.trim() !== CONFIRM_WORD || loading}
      title={
        <span className='text-destructive'>
          <AlertTriangle
            className='stroke-destructive me-1 inline-block'
            size={18}
          />{' '}
          删除 {selectedRows.length} 个部门
        </span>
      }
      desc={
        <div className='space-y-4'>
          <p className='mb-2'>
            确定要删除选中的 {selectedRows.length} 个部门吗？ <br />
            此操作将永久删除这些部门，且无法恢复。
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


