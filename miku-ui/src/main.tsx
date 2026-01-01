import { StrictMode } from 'react'
import ReactDOM from 'react-dom/client'
import { AxiosError } from 'axios'
import {
  QueryCache,
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query'
import { RouterProvider, createRouter } from '@tanstack/react-router'
import { toast } from 'sonner'
import { useAuthStore } from '@/lib/stores/auth-store'
import { handleServerError } from '@/lib/handle-server-error'
import { BusinessError } from '@/lib/errors'
import { DirectionProvider } from './providers/direction-provider'
import { FontProvider } from './providers/font-provider'
import { ThemeProvider } from './providers/theme-provider'
// Generated Routes
import { routeTree } from './routeTree.gen'
// Styles
import './styles/index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        // eslint-disable-next-line no-console
        if (import.meta.env.DEV) console.log({ failureCount, error })

        if (failureCount >= 0 && import.meta.env.DEV) return false
        if (failureCount > 3 && import.meta.env.PROD) return false

        // 业务错误（BusinessError）和 HTTP 401/403 不重试
        if (error instanceof BusinessError) {
          return false
        }

        return !(
          error instanceof AxiosError &&
          [401, 403].includes(error.response?.status ?? 0)
        )
      },
      refetchOnWindowFocus: import.meta.env.PROD,
      staleTime: 10 * 1000, // 10s
    },
    mutations: {
      onError: (error) => {
        handleServerError(error)

        if (error instanceof AxiosError) {
          if (error.response?.status === 304) {
            toast.error('Content not modified!')
          }
        }
      },
    },
  },
  queryCache: new QueryCache({
    onError: (error) => {
      // 处理业务错误（BusinessError）
      if (error instanceof BusinessError) {
        // 业务 code 403：权限不足
        if (error.isForbidden()) {
          // 显示错误提示
          toast.error(error.message || '权限不足')
          // 跳转到 403 页面
          router.navigate({ to: '/403', replace: true })
          return
        }
        // 业务 code 401：未授权
        // 注意：如果 token 刷新逻辑在拦截器中已经处理过，这里应该不会再收到 401
        // 但如果收到了，说明刷新失败或没有 refreshToken，需要跳转到登录页
        if (error.isUnauthorized()) {
          const { auth } = useAuthStore.getState()
          // 只有在没有 refreshToken 或者刷新失败的情况下才跳转
          // 如果还有 refreshToken，说明刷新逻辑可能还没执行完，不跳转
          if (!auth.refreshToken) {
            toast.error(error.message || '未授权，请重新登录')
            auth.reset()
            const redirect = `${router.history.location.href}`
            router.navigate({ to: '/sign-in', search: { redirect } })
          }
          // 如果有 refreshToken，说明可能是刷新失败，也跳转
          else {
            toast.error(error.message || '令牌已过期，请重新登录')
            auth.reset()
          const redirect = `${router.history.location.href}`
          router.navigate({ to: '/sign-in', search: { redirect } })
          }
          return
        }
        // 其他业务错误已在 handleServerError 中处理
        return
      }

      // 处理 HTTP 错误（AxiosError）
      if (error instanceof AxiosError) {
        if (error.response?.status === 401) {
          const { auth } = useAuthStore.getState()
          // 只有在没有 refreshToken 的情况下才跳转
          // 如果有 refreshToken，说明刷新逻辑可能还在执行，不跳转（让拦截器处理）
          if (!auth.refreshToken) {
          toast.error('Session expired!')
            auth.reset()
            const redirect = `${router.history.location.href}`
            router.navigate({ to: '/sign-in', search: { redirect } })
          }
          // 如果有 refreshToken 但还收到 401，说明刷新失败，也跳转
          else {
            toast.error('令牌已过期，请重新登录')
            auth.reset()
          const redirect = `${router.history.location.href}`
          router.navigate({ to: '/sign-in', search: { redirect } })
          }
          return
        }
        if (error.response?.status === 403) {
          // HTTP 403：跳转到 403 页面
          router.navigate({ to: '/403', replace: true })
          return
        }
        if (error.response?.status === 500) {
          toast.error('Internal Server Error!')
          // Only navigate to error page in production to avoid disrupting HMR in development
          if (import.meta.env.PROD) {
            router.navigate({ to: '/500' })
          }
          return
        }
      }
    },
  }),
})

// Create a new router instance
const router = createRouter({
  routeTree,
  context: { queryClient },
  defaultPreload: 'intent',
  defaultPreloadStaleTime: 0,
})

// Register the router instance for type safety
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

// Render the app
const rootElement = document.getElementById('root')!
if (!rootElement.innerHTML) {
  const root = ReactDOM.createRoot(rootElement)
  root.render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider>
          <FontProvider>
            <DirectionProvider>
              <RouterProvider router={router} />
            </DirectionProvider>
          </FontProvider>
        </ThemeProvider>
      </QueryClientProvider>
    </StrictMode>
  )
}
