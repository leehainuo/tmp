import { useSysLog } from '../providers/sys-log-provider'
import { SysLogCleanDialog } from './sys-log-clean-dialog'
import { SysLogDeleteDialog } from './sys-log-delete-dialog'

export function SysLogDialogs() {
  const { open, setOpen, currentRow, setCurrentRow } = useSysLog()

  return (
    <>
      <SysLogCleanDialog
        open={open === 'clean'}
        onOpenChange={(next) => setOpen(next ? 'clean' : null)}
      />

      {currentRow && (
        <SysLogDeleteDialog
          open={open === 'delete'}
          onOpenChange={(next) => {
            setOpen(next ? 'delete' : null)
            if (!next) {
              setTimeout(() => setCurrentRow(null), 300)
            }
          }}
          currentRow={currentRow}
        />
      )}
    </>
  )
}


