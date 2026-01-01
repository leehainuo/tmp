import type { NavGroup, NavItem, NavLink, NavCollapsible } from '../types'
import { getIcon } from './icon-mapper'

// 后端菜单项类型
export interface BackendMenu {
  id: number
  parentId: number | null
  permissionName: string
  permissionType: number // 1=目录 2=菜单 3=按钮 4=接口
  path?: string | null
  component?: string | null
  icon?: string | null
  orderNum?: number | null
  visible?: number | null
  status?: number | null
  children?: BackendMenu[]
}

/**
 * 将后端菜单转换为前端侧边栏格式
 * @param menus 后端菜单树
 * @returns 前端侧边栏菜单组
 */
export function convertMenusToSidebar(menus: BackendMenu[] | null | undefined): NavGroup[] {
  if (!menus || menus.length === 0) {
    return []
  }

  // 过滤掉不可见的菜单和按钮/接口类型
  const visibleMenus = menus.filter(menu => 
    menu.visible === 1 && 
    menu.status === 1 && 
    (menu.permissionType === 1 || menu.permissionType === 2) // 只保留目录和菜单
  )

  if (visibleMenus.length === 0) {
    return []
  }

  // 按 orderNum 排序（如果存在）
  const sortedMenus = [...visibleMenus].sort((a, b) => {
    const orderA = a.orderNum ?? 0
    const orderB = b.orderNum ?? 0
    return orderA - orderB
  })

  // 将菜单树转换为导航项
  const navItems = sortedMenus.map(menu => convertMenuToNavItem(menu)).filter(Boolean) as NavItem[]

  if (navItems.length === 0) {
    return []
  }

  // 将所有顶级菜单作为一个组返回
  // 如果需要按某种规则分组，可以在这里实现
  return [
    {
      title: '', // 可以设置为空或根据需求设置
      items: navItems,
    },
  ]
}

/**
 * 将单个后端菜单项转换为前端导航项
 */
function convertMenuToNavItem(menu: BackendMenu): NavItem | null {
  const icon = getIcon(menu.icon)
  
  // 如果有子菜单且子菜单不为空
  if (menu.children && menu.children.length > 0) {
    // 过滤子菜单
    const visibleChildren = menu.children
      .filter(child => 
        child.visible === 1 && 
        child.status === 1 && 
        (child.permissionType === 1 || child.permissionType === 2)
      )
      // 按 orderNum 排序
      .sort((a, b) => {
        const orderA = a.orderNum ?? 0
        const orderB = b.orderNum ?? 0
        return orderA - orderB
      })

    if (visibleChildren.length === 0) {
      // 如果没有可见的子菜单，且当前是目录类型，不显示
      if (menu.permissionType === 1) {
        return null
      }
      // 如果是菜单类型但没有子菜单，转换为链接
      if (menu.path) {
        const navLink: NavLink = {
          title: menu.permissionName,
          url: menu.path,
        }
        if (icon) {
          navLink.icon = icon
        }
        return navLink
      }
      return null
    }

    // 转换为可折叠菜单
    const subItems = visibleChildren
      .map(child => convertMenuToNavItem(child))
      .filter(Boolean) as Array<NavLink | NavCollapsible>

    // 将子项转换为导航项格式
    const navCollapsible: NavCollapsible = {
      title: menu.permissionName,
      items: subItems.map(item => {
        // 如果是链接类型，直接使用
        if ('url' in item && item.url && !('items' in item)) {
          return {
            title: item.title,
            url: item.url,
            icon: item.icon,
            badge: item.badge,
          }
        }
        // 如果是可折叠类型，需要将其子项展开或使用第一个子项的URL
        if ('items' in item && item.items && item.items.length > 0) {
          // 使用第一个子项的URL作为占位符，或者可以展开显示
          const firstChild = item.items[0]
          return {
            title: item.title,
            url: firstChild.url || '#',
            icon: item.icon,
          }
        }
        // 默认情况
        return {
          title: item.title,
          url: '#',
        }
      }),
    }

    if (icon) {
      navCollapsible.icon = icon
    }

    return navCollapsible
  }

  // 没有子菜单，转换为链接
  if (!menu.path) {
    // 如果是目录类型但没有路径，不显示
    if (menu.permissionType === 1) {
      return null
    }
    return null
  }

  const navLink: NavLink = {
    title: menu.permissionName,
    url: menu.path,
  }

  if (icon) {
    navLink.icon = icon
  }

  return navLink
}

