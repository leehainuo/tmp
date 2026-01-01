import { format } from 'date-fns'
import { Calendar as CalendarIcon } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import type { DateRange } from 'react-day-picker'

type DateRangePickerProps = {
  value: DateRange | undefined
  onChange: (range: DateRange | undefined) => void
  placeholder?: string
}

export function DateRangePicker({
  value,
  onChange,
  placeholder = '选择日期范围',
}: DateRangePickerProps) {
  const label =
    value?.from && value?.to
      ? `${format(value.from, 'yyyy-MM-dd')} ~ ${format(value.to, 'yyyy-MM-dd')}`
      : value?.from
        ? `${format(value.from, 'yyyy-MM-dd')} ~ ?`
        : ''

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant='outline'
          data-empty={!value?.from}
          className='data-[empty=true]:text-muted-foreground w-[260px] justify-start text-start font-normal'
        >
          {value?.from ? (
            <span>{label}</span>
          ) : (
            <span>{placeholder}</span>
          )}
          <CalendarIcon className='ms-auto h-4 w-4 opacity-50' />
        </Button>
      </PopoverTrigger>
      <PopoverContent className='w-auto p-0' align='end'>
        <Calendar
          mode='range'
          captionLayout='dropdown'
          selected={value}
          onSelect={onChange}
          disabled={(date: Date) =>
            date > new Date() || date < new Date('1900-01-01')
          }
          numberOfMonths={2}
        />
      </PopoverContent>
    </Popover>
  )
}


