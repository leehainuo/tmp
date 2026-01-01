import { SysPermissionActionDialog } from './sys-permission-action-dialog'
import { SysPermissionDeleteDialog } from './sys-permission-delete-dialog'
import { useSysPermission } from '../providers/sys-permission-provider'

export function SysPermissionDialogs() {
  const { open, setOpen, currentRow, setCurrentRow } = useSysPermission()
  return (
    <>
      <SysPermissionActionDialog
        key='permission-add'
        open={open === 'add'}
        onOpenChange={() => setOpen('add')}
      />

      {currentRow && (
        <>
          <SysPermissionActionDialog
            key={`permission-edit-${currentRow.id}`}
            open={open === 'edit'}
            onOpenChange={() => {
              setOpen('edit')
              setTimeout(() => {
                setCurrentRow(null)
              }, 500)
            }}
            currentRow={currentRow}
          />

          <SysPermissionDeleteDialog
            key={`permission-delete-${currentRow.id}`}
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

