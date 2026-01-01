import { createFileRoute } from '@tanstack/react-router'
import { Header } from '@/components/layout/header'
import { Search } from '@/components/search'
import { ForbiddenError } from '@/components/error/forbidden-error'
import { GeneralError } from '@/components/error/general-error'
import { MaintenanceError } from '@/components/error/maintenance-error'
import { NotFoundError } from '@/components/error/not-found-error'
import { UnauthorisedError } from '@/components/error/unauthorized-error'

export const Route = createFileRoute('/_authenticated/errors/$error')({
  component: RouteComponent,
})

function RouteComponent() {
  const { error } = Route.useParams()

  const errorMap: Record<string, React.ComponentType> = {
    unauthorized: UnauthorisedError,
    forbidden: ForbiddenError,
    'not-found': NotFoundError,
    'internal-server-error': GeneralError,
    'maintenance-error': MaintenanceError,
  }
  const ErrorComponent = errorMap[error] || NotFoundError

  return (
    <>
      <Header fixed>
        <Search />
      </Header>
      <div className='flex-1 [&>div]:h-full'>
        <ErrorComponent />
      </div>
    </>
  )
}
