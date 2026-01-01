import { SysUserActionDialog } from './sys-user-action-dialog'
import { SysUserDeleteDialog } from './sys-user-delete-dialog'
import { useSysUser } from '../providers/sys-user-provider'

export function SysUserDialogs() {
  const { open, setOpen, currentRow, setCurrentRow } = useSysUser()
  return (
    <>
      <SysUserActionDialog
        key='user-add'
        open={open === 'add'}
        onOpenChange={() => setOpen('add')}
      />

      {currentRow && (
        <>
          <SysUserActionDialog
            key={`user-edit-${currentRow.id}`}
            open={open === 'edit'}
            onOpenChange={() => {
              setOpen('edit')
              setTimeout(() => {
                setCurrentRow(null)
              }, 500)
            }}
            currentRow={currentRow}
          />

          <SysUserDeleteDialog
            key={`user-delete-${currentRow.id}`}
            open={open === 'delete'}
            onOpenChange={() => {
              setOpen('delete')
              setTimeout(() => {
                setCurrentRow(null)
              }, 500)
            }}
            currentRow={currentRow}
          />
        </>
      )}
    </>
  )
}

