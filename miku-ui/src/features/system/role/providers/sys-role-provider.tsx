import React, { useState } from 'react'
import useDialogState from '@/hooks/use-dialog-state'
import { type Role } from '../data/schema'
import type { Table } from '@tanstack/react-table'

type SysRoleDialogType = 'add' | 'edit' | 'delete'

type SysRoleContextType = {
  open: SysRoleDialogType | null
  setOpen: (str: SysRoleDialogType | null) => void
  currentRow: Role | null
  setCurrentRow: React.Dispatch<React.SetStateAction<Role | null>>
  refresh?: () => void
  table?: Table<Role>
  setTable?: (table: Table<Role> | undefined) => void
}

const SysRoleContext = React.createContext<SysRoleContextType | null>(null)

export function SysRoleProvider({ 
  children, 
  refresh 
}: { 
  children: React.ReactNode
  refresh?: () => void 
}) {
  const [open, setOpen] = useDialogState<SysRoleDialogType>(null)
  const [currentRow, setCurrentRow] = useState<Role | null>(null)
  const [table, setTable] = useState<Table<Role> | undefined>(undefined)

  return (
    <SysRoleContext.Provider value={{ open, setOpen, currentRow, setCurrentRow, refresh, table, setTable }}>
      {children}
    </SysRoleContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useSysRole = () => {
  const sysRoleContext = React.useContext(SysRoleContext)

  if (!sysRoleContext) {
    throw new Error('useSysRole has to be used within <SysRoleProvider>')
  }

  return sysRoleContext
}

