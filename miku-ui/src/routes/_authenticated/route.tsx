import { createFileRoute, redirect } from '@tanstack/react-router'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { useAuthStore } from '@/lib/stores/auth-store'

export const Route = createFileRoute('/_authenticated')({
  beforeLoad: () => {
    const { auth } = useAuthStore.getState()
    if (!auth.accessToken) {
      throw redirect({
        to: '/sign-in',
        search: {
          redirect: window.location.pathname + window.location.search,
        },
      })
    }
  },
  component: AuthenticatedLayout,
})
