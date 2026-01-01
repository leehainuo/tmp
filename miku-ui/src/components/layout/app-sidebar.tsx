import { useLayout } from '@/providers/layout-provider'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
} from '@/components/ui/sidebar'
import { sidebarData } from './data/sidebar-data'
import { NavGroup } from './nav-group'
import { useAuthStore } from '@/lib/stores/auth-store'
import { convertMenusToSidebar } from './utils/menu-converter'
import { useEffect } from 'react'
import { authApi } from '@/lib/api'
import { AppTitle } from './app-title'

export function AppSidebar() {
  const { collapsible, variant } = useLayout()
  const { auth } = useAuthStore()
  
  // 如果刷新后没有完整用户信息，尝试恢复或重新获取
  useEffect(() => {
    if (auth.user && !auth.fullUserInfo && auth.accessToken) {
      // 先尝试从缓存恢复
      auth.getFullUserInfo().then((fullInfo) => {
        // 如果缓存中没有，从后端获取
        if (!fullInfo) {
          authApi.getUserInfo()
            .then((userInfo) => {
              auth.setUser(userInfo)
            })
            .catch(() => {
              // 获取失败，静默处理
            })
        }
      })
    }
  }, [auth.user, auth.fullUserInfo, auth.accessToken, auth])
  
  // 将所有菜单项合并为单个扁平列表（去掉分组标题）
  const navItems = [] as typeof sidebarData.navGroups[0]['items']

  // 1. 保留 General 分组项（如果存在）
  const generalGroup = sidebarData.navGroups.find((group) => group.title === '通用')
  if (generalGroup) {
    navItems.push(...generalGroup.items)
  }

  // 2. 使用动态菜单替换 Pages 项；若无则使用默认 Pages 项
  if (auth.fullUserInfo?.menus) {
    const converted = convertMenusToSidebar(auth.fullUserInfo.menus)
    if (converted.length > 0 && converted[0].items.length > 0) {
      navItems.push(...converted[0].items)
    } else {
      const pagesGroup = sidebarData.navGroups.find((group) => group.title === '页')
      if (pagesGroup) navItems.push(...pagesGroup.items)
    }
  } else {
    const pagesGroup = sidebarData.navGroups.find((group) => group.title === '页')
    if (pagesGroup) navItems.push(...pagesGroup.items)
  }

  // 3. 保留 Other 分组项（如果存在）
  const otherGroup = sidebarData.navGroups.find((group) => group.title === '其它')
  if (otherGroup) {
    navItems.push(...otherGroup.items)
  }

  // 单个无标题分组（用于去掉分组标签）
  const navGroups = [{ title: '', items: navItems }]

  return (
    <Sidebar collapsible={collapsible} variant={variant}>
      <SidebarHeader>
        {/* <AboutMenu items={sidebarData.teams} /> */}

        {/* Replace <TeamSwitch /> with the following <AppTitle />
         /* if you want to use the normal app title instead of TeamSwitch dropdown */}
        <AppTitle />
      </SidebarHeader>
      <SidebarContent>
        {navGroups.map((props, index) => (
          <NavGroup key={props.title || `group-${index}`} {...props} />
        ))}
      </SidebarContent>
      <SidebarFooter>
        
      </SidebarFooter>
      {/* <SidebarRail /> */}
    </Sidebar>
  )
}
