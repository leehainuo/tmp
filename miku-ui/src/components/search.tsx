import { SearchIcon } from 'lucide-react'
import { cn } from '@/lib/utils'
import { useSearch } from '@/providers/search-provider'
import { Button } from './ui/button'

type SearchProps = {
  className?: string
  type?: React.HTMLInputTypeAttribute
  placeholder?: string
}

export function Search({
  className = '',
  placeholder = '输入搜索内容...',
}: SearchProps) {
  const { setOpen } = useSearch()
  return (
    <Button
      variant='ghost'
      className={cn(
        'bg-transparent! shrink-0 items-center justify-center gap-2 h-9 has-[>svg]:px-3 hidden px-1 py-0 font-normal sm:block',
        className
      )}
      onClick={() => setOpen(true)}
    >
      <div className='text-muted-foreground hidden items-center gap-1.5 text-sm sm:flex'>
        <SearchIcon className='size-4' aria-hidden='true' />
        <span>{placeholder}</span>
      </div>
    </Button>
  )
}
