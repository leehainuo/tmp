import z from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { SysLog } from '@/features/system/log'

const logSearchSchema = z.object({
  page: z.number().optional().catch(1),
  pageSize: z.number().optional().catch(10),
  title: z.string().optional().catch(''),
  operName: z.string().optional().catch(''),
  columnVisibility: z.record(z.string(), z.boolean()).optional().catch({}),
})

export const Route = createFileRoute('/_authenticated/system/log/')({
  validateSearch: logSearchSchema,
  component: SysLog,
})


