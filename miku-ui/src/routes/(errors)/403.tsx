import { createFileRoute } from '@tanstack/react-router'
import { ForbiddenError } from '@/components/error/forbidden-error'

export const Route = createFileRoute('/(errors)/403')({
  component: ForbiddenError,
})
