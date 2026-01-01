import React, { useState } from 'react'
import useDialogState from '@/hooks/use-dialog-state'
import { type User } from '../data/schema'
import type { Table } from '@tanstack/react-table'

type SysUserDialogType = 'add' | 'edit' | 'delete'

type SysUserContextType = {
  open: SysUserDialogType | null
  setOpen: (str: SysUserDialogType | null) => void
  currentRow: User | null
  setCurrentRow: React.Dispatch<React.SetStateAction<User | null>>
  refresh?: () => void
  table?: Table<User>
  setTable?: (table: Table<User> | undefined) => void
}

const SysUserContext = React.createContext<SysUserContextType | null>(null)

export function SysUserProvider({ 
  children, 
  refresh 
}: { 
  children: React.ReactNode
  refresh?: () => void 
}) {
  const [open, setOpen] = useDialogState<SysUserDialogType>(null)
  const [currentRow, setCurrentRow] = useState<User | null>(null)
  const [table, setTable] = useState<Table<User> | undefined>(undefined)

  return (
    <SysUserContext.Provider value={{ open, setOpen, currentRow, setCurrentRow, refresh, table, setTable }}>
      {children}
    </SysUserContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useSysUser = () => {
  const sysUserContext = React.useContext(SysUserContext)

  if (!sysUserContext) {
    throw new Error('useSysUser has to be used within <SysUserProvider>')
  }

  return sysUserContext
}

