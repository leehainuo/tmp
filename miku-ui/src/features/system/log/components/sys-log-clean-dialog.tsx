import { useState } from 'react'
import { format } from 'date-fns'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { DateRangePicker } from '@/components/date-range-picker'
import type { DateRange } from 'react-day-picker'
import { logApi } from '@/lib/api'
import { toast } from 'sonner'
import { useSysLog } from '../providers/sys-log-provider'

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function SysLogCleanDialog({ open, onOpenChange }: Props) {
  const { refresh } = useSysLog()
  const [range, setRange] = useState<DateRange | undefined>()
  const [months, setMonths] = useState<number>(6)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async () => {
    try {
      setSubmitting(true)
      let startDate: string | undefined
      let endDate: string | undefined

      if (range?.from) {
        startDate = format(range.from, 'yyyy-MM-dd')
      }
      if (range?.to) {
        endDate = format(range.to, 'yyyy-MM-dd')
      }

      const cleaned = await logApi.clean({
        startDate,
        endDate,
        months,
      })

      toast.success(`清理完成，共删除 ${cleaned ?? 0} 条日志`)
      refresh?.()
      onOpenChange(false)
    } catch (error) {
      console.error(error)
      toast.error('清理日志失败，请稍后重试')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-w-md'>
        <DialogHeader>
          <DialogTitle>清理操作日志</DialogTitle>
        </DialogHeader>
        <div className='space-y-4 py-2'>
          <div className='space-y-2'>
            <Label>时间范围（可选）</Label>
            <p className='text-xs text-muted-foreground'>
              选择起止日期后，将清理该日期范围内的日志；如果不选择，则按“保留最近 N 个月”策略清理更早的日志。
            </p>
            <DateRangePicker value={range} onChange={setRange} placeholder='选择开始和结束日期' />
          </div>
          <div className='space-y-2'>
            <Label htmlFor='months'>保留最近（月）</Label>
            <input
              id='months'
              type='number'
              min={1}
              max={36}
              value={months}
              onChange={(e) => setMonths(Number(e.target.value) || 1)}
              className='flex h-9 w-32 rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50'
            />
            <p className='text-xs text-muted-foreground'>
              当未选择时间范围时生效，例如填 6 表示只保留最近 6 个月的日志。
            </p>
          </div>
        </div>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)} disabled={submitting}>
            取消
          </Button>
          <Button variant='destructive' onClick={handleSubmit} disabled={submitting}>
            {submitting ? '正在清理...' : '确认清理'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}


