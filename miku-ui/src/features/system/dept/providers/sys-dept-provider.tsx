import React, { useState } from 'react'
import useDialogState from '@/hooks/use-dialog-state'
import { type Dept } from '../data/schema'
import type { Table } from '@tanstack/react-table'
import { type FlatDept } from '../components/sys-dept-columns'

type SysDeptDialogType = 'add' | 'edit' | 'delete'

type SysDeptContextType = {
  open: SysDeptDialogType | null
  setOpen: (str: SysDeptDialogType | null) => void
  currentRow: Dept | null
  setCurrentRow: React.Dispatch<React.SetStateAction<Dept | null>>
  refresh?: () => void
  table?: Table<FlatDept>
  setTable?: (table: Table<FlatDept> | undefined) => void
}

const SysDeptContext = React.createContext<SysDeptContextType | null>(null)

export function SysDeptProvider({
  children,
  refresh,
}: {
  children: React.ReactNode
  refresh?: () => void
}) {
  const [open, setOpen] = useDialogState<SysDeptDialogType>(null)
  const [currentRow, setCurrentRow] = useState<Dept | null>(null)
  const [table, setTable] = useState<Table<FlatDept> | undefined>(undefined)

  return (
    <SysDeptContext.Provider value={{ open, setOpen, currentRow, setCurrentRow, refresh, table, setTable }}>
      {children}
    </SysDeptContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useSysDept = () => {
  const sysDeptContext = React.useContext(SysDeptContext)

  if (!sysDeptContext) {
    throw new Error('useSysDept has to be used within <SysDeptProvider>')
  }

  return sysDeptContext
}

