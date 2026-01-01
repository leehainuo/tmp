'use client'

import { useState, useMemo } from 'react'
import { Checkbox } from '@/components/ui/checkbox'
import { ScrollArea } from '@/components/ui/scroll-area'
import { type SysPermission } from '@/lib/types'
import { ChevronRight, ChevronDown } from 'lucide-react'
import { cn } from '@/lib/utils'

type PermissionTreeSelectProps = {
  permissions: SysPermission[]
  selectedIds: number[]
  onSelectionChange: (selectedIds: number[]) => void
  className?: string
}

type PermissionNode = SysPermission & {
  children?: PermissionNode[]
}

export function PermissionTreeSelect({
  permissions,
  selectedIds,
  onSelectionChange,
  className,
}: PermissionTreeSelectProps) {
  // 默认展开第一层节点
  const [expandedIds, setExpandedIds] = useState<Set<number>>(() => {
    const initial = new Set<number>()
    permissions.forEach((p) => {
      if (p.id && p.parentId === 0) {
        initial.add(p.id)
      }
    })
    return initial
  })

  // 构建权限映射，方便查找
  const permissionMap = useMemo(() => {
    const map = new Map<number, PermissionNode>()
    const buildMap = (nodes: PermissionNode[]) => {
      nodes.forEach((node) => {
        if (node.id) {
          map.set(node.id, node)
        }
        if (node.children) {
          buildMap(node.children)
        }
      })
    }
    buildMap(permissions as PermissionNode[])
    return map
  }, [permissions])

  // 获取所有子节点ID
  const getChildrenIds = (node: PermissionNode): number[] => {
    const ids: number[] = []
    if (node.id) {
      ids.push(node.id)
    }
    if (node.children) {
      node.children.forEach((child) => {
        ids.push(...getChildrenIds(child))
      })
    }
    return ids
  }

  // 获取所有父节点ID
  const getParentIds = (nodeId: number): number[] => {
    const ids: number[] = []
    const node = permissionMap.get(nodeId)
    if (node?.parentId && node.parentId !== 0) {
      ids.push(node.parentId)
      ids.push(...getParentIds(node.parentId))
    }
    return ids
  }

  // 切换节点选中状态
  const toggleNode = (node: PermissionNode, checked: boolean) => {
    if (!node.id) return

    const nodeIds = getChildrenIds(node)
    let newSelectedIds: number[]

    if (checked) {
      // 选中节点及其所有子节点
      newSelectedIds = [...new Set([...selectedIds, ...nodeIds])]
      
      // 检查是否所有兄弟节点都被选中，如果是则选中父节点
      const parent = node.parentId ? permissionMap.get(node.parentId) : null
      if (parent?.children) {
        const siblingIds = parent.children
          .filter((sibling) => sibling.id !== node.id)
          .map((sibling) => getChildrenIds(sibling))
          .flat()
        
        const allSiblingsSelected = siblingIds.every((id) => newSelectedIds.includes(id))
        if (allSiblingsSelected && parent.id) {
          newSelectedIds.push(...getChildrenIds(parent))
        }
      }
    } else {
      // 取消选中节点及其所有子节点
      newSelectedIds = selectedIds.filter((id) => !nodeIds.includes(id))
      
      // 取消选中所有父节点
      const parentIds = getParentIds(node.id)
      newSelectedIds = newSelectedIds.filter((id) => !parentIds.includes(id))
    }

    newSelectedIds = [...new Set(newSelectedIds)]
    onSelectionChange(newSelectedIds)
  }

  // 检查节点是否被选中
  const isNodeChecked = (node: PermissionNode): boolean => {
    if (!node.id) return false
    const nodeIds = getChildrenIds(node)
    return nodeIds.every((id) => selectedIds.includes(id))
  }

  // 检查节点是否部分选中
  const isNodeIndeterminate = (node: PermissionNode): boolean => {
    if (!node.id) return false
    const nodeIds = getChildrenIds(node)
    const hasSelected = nodeIds.some((id) => selectedIds.includes(id))
    const allSelected = nodeIds.every((id) => selectedIds.includes(id))
    return hasSelected && !allSelected
  }

  // 切换节点展开/折叠
  const toggleExpand = (nodeId: number) => {
    setExpandedIds((prev) => {
      const next = new Set(prev)
      if (next.has(nodeId)) {
        next.delete(nodeId)
      } else {
        next.add(nodeId)
      }
      return next
    })
  }

  // 渲染权限树节点
  const renderNode = (node: PermissionNode, level = 0): React.ReactNode => {
    if (!node.id) return null

    const hasChildren = node.children && node.children.length > 0
    const isExpanded = expandedIds.has(node.id)
    const checked = isNodeChecked(node)
    const indeterminate = isNodeIndeterminate(node)

    // 权限类型标签
    const typeLabels: Record<number, string> = {
      1: '目录',
      2: '菜单',
      3: '按钮',
      4: '接口',
    }
    const typeLabel = typeLabels[node.permissionType] || '未知'

    return (
      <div key={node.id} className='select-none'>
        <div
          className={cn(
            'flex items-center gap-2 py-1.5 px-2 rounded-md hover:bg-accent transition-colors cursor-pointer'
          )}
          style={{ paddingLeft: `${level * 1.5 + 0.5}rem` }}
        >
          {/* 展开/折叠按钮 */}
          {hasChildren ? (
            <button
              type='button'
              onClick={() => toggleExpand(node.id!)}
              className='p-0.5 hover:bg-accent rounded'
            >
              {isExpanded ? (
                <ChevronDown className='h-4 w-4 text-muted-foreground' />
              ) : (
                <ChevronRight className='h-4 w-4 text-muted-foreground' />
              )}
            </button>
          ) : (
            <div className='w-5' />
          )}

          {/* 复选框 */}
          <div className='relative'>
            <Checkbox
              checked={checked}
              onCheckedChange={(value) => toggleNode(node, value === true)}
            />
            {indeterminate && (
              <div className='absolute inset-0 flex items-center justify-center pointer-events-none'>
                <div className='h-2 w-2 bg-primary rounded-sm' />
              </div>
            )}
          </div>

          {/* 权限名称和类型 */}
          <div className='flex-1 flex items-center gap-2'>
            <span className='text-sm'>{node.permissionName}</span>
            <span className='text-xs text-muted-foreground'>({typeLabel})</span>
          </div>
        </div>

        {/* 子节点 */}
        {hasChildren && isExpanded && (
          <div>
            {node.children!.map((child) => renderNode(child, level + 1))}
          </div>
        )}
      </div>
    )
  }

  return (
    <div className={cn('border rounded-md bg-background', className)}>
      <ScrollArea className='h-[350px] p-2'>
        <div className='space-y-0.5'>
          {permissions.length > 0 ? (
            permissions.map((permission) => renderNode(permission as PermissionNode))
          ) : (
            <div className='text-center text-muted-foreground py-8 text-sm'>
              暂无权限数据
            </div>
          )}
        </div>
      </ScrollArea>
    </div>
  )
}

