import { SysRoleActionDialog } from './sys-role-action-dialog'
import { SysRoleDeleteDialog } from './sys-role-delete-dialog'
import { useSysRole } from '../providers/sys-role-provider'

export function SysRoleDialogs() {
  const { open, setOpen, currentRow, setCurrentRow } = useSysRole()
  return (
    <>
      <SysRoleActionDialog
        key='role-add'
        open={open === 'add'}
        onOpenChange={() => setOpen('add')}
      />

      {currentRow && (
        <>
          <SysRoleActionDialog
            key={`role-edit-${currentRow.id}`}
            open={open === 'edit'}
            onOpenChange={() => {
              setOpen('edit')
              setTimeout(() => {
                setCurrentRow(null)
              }, 500)
            }}
            currentRow={currentRow}
          />

          <SysRoleDeleteDialog
            key={`role-delete-${currentRow.id}`}
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

