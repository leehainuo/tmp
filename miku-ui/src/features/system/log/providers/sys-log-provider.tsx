import React, { useState } from 'react'
import type { Table } from '@tanstack/react-table'
import type { Log } from '../data/schema'

type SysLogDialogType = 'clean' | 'delete'

type SysLogContextType = {
  open: SysLogDialogType | null
  setOpen: (type: SysLogDialogType | null) => void
  currentRow: Log | null
  setCurrentRow: React.Dispatch<React.SetStateAction<Log | null>>
  refresh?: () => void
  table?: Table<Log>
  setTable?: (table: Table<Log> | undefined) => void
}

const SysLogContext = React.createContext<SysLogContextType | null>(null)

export function SysLogProvider({
  children,
  refresh,
}: {
  children: React.ReactNode
  refresh?: () => void
}) {
  const [open, setOpen] = useState<SysLogDialogType | null>(null)
  const [currentRow, setCurrentRow] = useState<Log | null>(null)
  const [table, setTable] = useState<Table<Log> | undefined>(undefined)

  return (
    <SysLogContext.Provider
      value={{ open, setOpen, currentRow, setCurrentRow, refresh, table, setTable }}
    >
      {children}
    </SysLogContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useSysLog = () => {
  const ctx = React.useContext(SysLogContext)
  if (!ctx) {
    throw new Error('useSysLog must be used within <SysLogProvider>')
  }
  return ctx
}

