import { useState } from 'react'
import { type Table } from '@tanstack/react-table'
import { Trash2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { DataTableBulkActions as BulkActionsToolbar } from '@/components/data-table'
import { SysUserMultiDeleteDialog } from './sys-user-multi-delete-dialog'

type DataTableBulkActionsProps<TData> = {
  table: Table<TData>
}

export function DataTableBulkActions<TData>({
  table,
}: DataTableBulkActionsProps<TData>) {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  return (
    <>
      <BulkActionsToolbar table={table} entityName='用户'>
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              variant='destructive'
              size='icon'
              onClick={() => setShowDeleteConfirm(true)}
              className='size-8'
              aria-label='删除选中的用户'
              title='删除选中的用户'
            >
              <Trash2 />
              <span className='sr-only'>删除选中的用户</span>
            </Button>
          </TooltipTrigger>
          <TooltipContent>
            <p>删除选中的用户</p>
          </TooltipContent>
        </Tooltip>
      </BulkActionsToolbar>

      <SysUserMultiDeleteDialog
        table={table}
        open={showDeleteConfirm}
        onOpenChange={setShowDeleteConfirm}
      />
    </>
  )
}

