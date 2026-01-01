import React, { useState } from 'react'
import useDialogState from '@/hooks/use-dialog-state'
import { type Permission } from '../data/schema'
import type { Table } from '@tanstack/react-table'
import { type FlatPermission } from '../components/sys-permission-columns'

type SysPermissionDialogType = 'add' | 'edit' | 'delete'

type SysPermissionContextType = {
  open: SysPermissionDialogType | null
  setOpen: (str: SysPermissionDialogType | null) => void
  currentRow: Permission | null
  setCurrentRow: React.Dispatch<React.SetStateAction<Permission | null>>
  refresh?: () => void
  table?: Table<FlatPermission>
  setTable?: (table: Table<FlatPermission> | undefined) => void
}

const SysPermissionContext = React.createContext<SysPermissionContextType | null>(null)

export function SysPermissionProvider({ 
  children, 
  refresh 
}: { 
  children: React.ReactNode
  refresh?: () => void 
}) {
  const [open, setOpen] = useDialogState<SysPermissionDialogType>(null)
  const [currentRow, setCurrentRow] = useState<Permission | null>(null)
  const [table, setTable] = useState<Table<FlatPermission> | undefined>(undefined)

  return (
    <SysPermissionContext.Provider value={{ open, setOpen, currentRow, setCurrentRow, refresh, table, setTable }}>
      {children}
    </SysPermissionContext.Provider>
  )
}

// eslint-disable-next-line react-refresh/only-export-components
export const useSysPermission = () => {
  const sysPermissionContext = React.useContext(SysPermissionContext)

  if (!sysPermissionContext) {
    throw new Error('useSysPermission has to be used within <SysPermissionProvider>')
  }

  return sysPermissionContext
}

