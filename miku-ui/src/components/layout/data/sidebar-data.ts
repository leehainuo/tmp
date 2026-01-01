import {
  Construction,
  LayoutDashboard,
  Bug,
  FileX,
  HelpCircle,
  Lock,
  Package,
  Palette,
  ServerOff,
  Wrench,
  UserX,
  ShieldCheck,
  GalleryVerticalEnd,
  Settings2,
  CodeXml,
} from 'lucide-react'
import { type SidebarData } from '../types'
import { Logo } from '@/assets/logo'

export const sidebarData: SidebarData = {
  user: {
    name: 'miku',
    email: 'miku@example.com',
    avatar: '/avatars/shadcn.jpg',
  },
  teams: [
    {
      name: 'Miku',
      logo: Logo,
      plan: 'Vite + ShadcnUI',
    },
    {
      name: '文档',
      logo: GalleryVerticalEnd,
      plan: '详细文档',
    },
    {
      name: '开发者',
      logo: CodeXml,
      plan: '开发者官网',
    },
  ],
  navGroups: [
    {
      title: '通用',
      items: [
        {
          title: '仪表盘',
          url: '/',
          icon: LayoutDashboard,
        },
        // {
        //   title: '任务',
        //   url: '/tasks',
        //   icon: ListTodo,
        // },
        {
          title: '应用',
          url: '/apps',
          icon: Package,
        },
      ],
    },
    {
      title: '页',
      items: [
        {
          title: '认证',
          icon: ShieldCheck,
          items: [
            {
              title: 'Sign In',
              url: '/sign-in',
            },
            {
              title: 'Sign In (2 Col)',
              url: '/sign-in-2',
            },
            {
              title: 'Sign Up',
              url: '/sign-up',
            },
            {
              title: 'Forgot Password',
              url: '/forgot-password',
            },
            {
              title: 'OTP',
              url: '/otp',
            },
          ],
        },
        {
          title: '错误',
          icon: Bug,
          items: [
            {
              title: 'Unauthorized',
              url: '/errors/unauthorized',
              icon: Lock,
            },
            {
              title: 'Forbidden',
              url: '/errors/forbidden',
              icon: UserX,
            },
            {
              title: 'Not Found',
              url: '/errors/not-found',
              icon: FileX,
            },
            {
              title: 'Internal Server Error',
              url: '/errors/internal-server-error',
              icon: ServerOff,
            },
            {
              title: 'Maintenance Error',
              url: '/errors/maintenance-error',
              icon: Construction,
            },
          ],
        },
      ],
    },
    {
      title: '其它',
      items: [
        {
          title: '设置',
          icon: Settings2,
          items: [
            {
              title: '账户设置',
              url: '/settings/account',
              icon: Wrench,
            },
            {
              title: '外观设置',
              url: '/settings/appearance',
              icon: Palette,
            },
          ],
        },
        {
          title: '帮助中心',
          url: '/help-center',
          icon: HelpCircle,
        },
      ],
    },
  ],
}
