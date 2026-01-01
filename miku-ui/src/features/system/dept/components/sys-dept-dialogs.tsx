import { SysDeptActionDialog } from './sys-dept-action-dialog'
import { SysDeptDeleteDialog } from './sys-dept-delete-dialog'
import { useSysDept } from '../providers/sys-dept-provider'

export function SysDeptDialogs() {
  const { open, setOpen, currentRow, setCurrentRow } = useSysDept()
  return (
    <>
      <SysDeptActionDialog
        key='dept-add'
        open={open === 'add'}
        onOpenChange={() => setOpen('add')}
      />

      {currentRow && (
        <>
          <SysDeptActionDialog
            key={`dept-edit-${currentRow.id}`}
            open={open === 'edit'}
            onOpenChange={() => {
              setOpen('edit')
              setTimeout(() => {
                setCurrentRow(null)
              }, 500)
            }}
            currentRow={currentRow}
          />

          <SysDeptDeleteDialog
            key={`dept-delete-${currentRow.id}`}
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


