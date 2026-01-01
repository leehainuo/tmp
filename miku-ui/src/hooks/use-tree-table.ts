import { useCallback, useEffect, useMemo, useState } from 'react'

type TreeNode = {
  id?: number
  children?: TreeNode[]
}

export type FlatTreeNode<T extends TreeNode> = T & {
  level: number
  hasChildren: boolean
  isExpanded: boolean
  pathIds: number[]
  animateIn?: boolean
}

export type UseTreeTableOptions<T extends TreeNode> = {
  data: T[]
  animationDuration?: number
}

export function useTreeTable<T extends TreeNode>({
  data,
  animationDuration = 320,
}: UseTreeTableOptions<T>) {
  const [expandedIds, setExpandedIds] = useState<Record<number, boolean>>({})
  const [recentlyExpandedId, setRecentlyExpandedId] = useState<number | null>(
    null
  )

  const toggleExpand = useCallback((id?: number) => {
    if (typeof id !== 'number') return
    setExpandedIds((prev) => {
      const nextState = !(prev[id] ?? false)
      const next = { ...prev, [id]: nextState }
      setRecentlyExpandedId(nextState ? id : null)
      return next
    })
  }, [])

  useEffect(() => {
    if (!recentlyExpandedId) return
    const timer = setTimeout(
      () => setRecentlyExpandedId(null),
      animationDuration
    )
    return () => clearTimeout(timer)
  }, [animationDuration, recentlyExpandedId])

  const flattenTree = useCallback(
    (
      tree: T[],
      expandedMap: Record<number, boolean>,
      level = 0,
      parentPath: number[] = []
    ): FlatTreeNode<T>[] => {
      function walk(
        nodes: T[],
        currentLevel: number,
        currentPath: number[]
      ): FlatTreeNode<T>[] {
        const result: FlatTreeNode<T>[] = []

        nodes.forEach((item) => {
          const nodeId = item.id
          const hasChildren = !!(item.children && item.children.length > 0)
          const isExpanded =
            nodeId !== undefined ? expandedMap[nodeId] ?? false : true
          const pathIds =
            nodeId !== undefined ? [...currentPath, nodeId] : [...currentPath]

          result.push({
            ...item,
            level: currentLevel,
            hasChildren,
            isExpanded,
            pathIds,
          })

          if (hasChildren && isExpanded) {
            result.push(
              ...walk(
                (item.children || []) as T[],
                currentLevel + 1,
                pathIds
              )
            )
          }
        })

        return result
      }

      return walk(tree, level, parentPath)
    },
    []
  )

  const flatData = useMemo(() => {
    if (data.length === 0) return []
    const flattened = flattenTree(data, expandedIds)

    if (!recentlyExpandedId) {
      return flattened
    }

    return flattened.map((item) => ({
      ...item,
      animateIn:
        recentlyExpandedId !== null &&
        item.pathIds.includes(recentlyExpandedId) &&
        item.id !== recentlyExpandedId,
    }))
  }, [data, expandedIds, flattenTree, recentlyExpandedId])

  return {
    flatData,
    toggleExpand,
    expandedIds,
    animationDuration,
  }
}

