'use client'

import { useState, useMemo } from 'react'
import { Checkbox } from '@/components/ui/checkbox'
import { ScrollArea } from '@/components/ui/scroll-area'
import { type SysDept } from '@/lib/types'
import { ChevronRight, ChevronDown } from 'lucide-react'
import { cn } from '@/lib/utils'

type DeptTreeSelectProps = {
  depts: SysDept[]
  selectedIds: number[]
  onSelectionChange: (selectedIds: number[]) => void
  className?: string
}

type DeptNode = SysDept & {
  children?: DeptNode[]
}

export function DeptTreeSelect({
  depts,
  selectedIds,
  onSelectionChange,
  className,
}: DeptTreeSelectProps) {
  // 默认展开第一层节点
  const [expandedIds, setExpandedIds] = useState<Set<number>>(() => {
    const initial = new Set<number>()
    depts.forEach((d) => {
      if (d.id && d.parentId === 0) {
        initial.add(d.id)
      }
    })
    return initial
  })

  // 构建部门映射，方便查找
  const deptMap = useMemo(() => {
    const map = new Map<number, DeptNode>()
    const buildMap = (nodes: DeptNode[]) => {
      nodes.forEach((node) => {
        if (node.id) {
          map.set(node.id, node)
        }
        if (node.children) {
          buildMap(node.children)
        }
      })
    }
    buildMap(depts as DeptNode[])
    return map
  }, [depts])

  // 获取所有子节点ID
  const getChildrenIds = (node: DeptNode): number[] => {
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
    const node = deptMap.get(nodeId)
    if (node?.parentId && node.parentId !== 0) {
      ids.push(node.parentId)
      ids.push(...getParentIds(node.parentId))
    }
    return ids
  }

  // 切换节点选中状态
  const toggleNode = (node: DeptNode, checked: boolean) => {
    const nodeId = node.id!
    const childrenIds = getChildrenIds(node)
    const parentIds = getParentIds(nodeId)

    let newSelectedIds = [...selectedIds]

    if (checked) {
      // 选中：添加当前节点和所有子节点
      newSelectedIds.push(nodeId, ...childrenIds)
      // 如果所有子节点都被选中，也选中父节点
      parentIds.forEach((parentId) => {
        const parent = deptMap.get(parentId)
        if (parent?.children) {
          const allChildrenSelected = parent.children.every(
            (child) => child.id && newSelectedIds.includes(child.id)
          )
          if (allChildrenSelected && !newSelectedIds.includes(parentId)) {
            newSelectedIds.push(parentId)
          }
        }
      })
    } else {
      // 取消选中：移除当前节点和所有子节点
      newSelectedIds = newSelectedIds.filter(
        (id) => id !== nodeId && !childrenIds.includes(id)
      )
      // 取消选中所有父节点
      parentIds.forEach((parentId) => {
        newSelectedIds = newSelectedIds.filter((id) => id !== parentId)
      })
    }

    // 去重
    newSelectedIds = Array.from(new Set(newSelectedIds))
    onSelectionChange(newSelectedIds)
  }

  // 切换展开/折叠
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

  // 检查节点是否被选中
  const isNodeSelected = (node: DeptNode): boolean => {
    return node.id !== undefined && selectedIds.includes(node.id)
  }

  // 检查节点是否部分选中（子节点部分选中）
  const isNodeIndeterminate = (node: DeptNode): boolean => {
    if (!node.children || node.children.length === 0) {
      return false
    }
    const selectedChildren = node.children.filter((child) =>
      isNodeSelected(child)
    )
    return selectedChildren.length > 0 && selectedChildren.length < node.children.length
  }

  // 渲染树节点
  const renderNode = (node: DeptNode, level = 0): React.ReactNode => {
    const nodeId = node.id!
    const hasChildren = node.children && node.children.length > 0
    const isExpanded = expandedIds.has(nodeId)
    const isSelected = isNodeSelected(node)
    const isIndeterminate = isNodeIndeterminate(node)

    return (
      <div key={nodeId} className='select-none'>
        <div
          className={cn(
            'flex items-center gap-2 py-1.5 px-2 rounded-md hover:bg-accent cursor-pointer',
            level > 0 && 'ml-4'
          )}
          style={{ paddingLeft: `${level * 16 + 8}px` }}
        >
          {/* 展开/折叠图标 */}
          {hasChildren ? (
            <button
              type='button'
              onClick={(e) => {
                e.stopPropagation()
                toggleExpand(nodeId)
              }}
              className='p-0.5 hover:bg-accent rounded'
            >
              {isExpanded ? (
                <ChevronDown className='h-4 w-4' />
              ) : (
                <ChevronRight className='h-4 w-4' />
              )}
            </button>
          ) : (
            <div className='w-5' />
          )}

          {/* 复选框 */}
          <Checkbox
            checked={isSelected}
            onCheckedChange={(checked) => toggleNode(node, checked as boolean)}
            onClick={(e) => e.stopPropagation()}
            ref={(el) => {
              if (el) {
                el.indeterminate = isIndeterminate
              }
            }}
          />

          {/* 部门名称 */}
          <span className='flex-1 text-sm'>{node.deptName}</span>
        </div>

        {/* 子节点 */}
        {hasChildren && isExpanded && (
          <div>{node.children!.map((child) => renderNode(child, level + 1))}</div>
        )}
      </div>
    )
  }

  return (
    <div className={cn('border rounded-md', className)}>
      <ScrollArea className='h-[300px]'>
        <div className='p-2'>
          {depts
            .filter((d) => d.parentId === 0)
            .map((dept) => renderNode(dept as DeptNode))}
        </div>
      </ScrollArea>
    </div>
  )
}

