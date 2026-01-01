import { Download } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { toast } from 'sonner'

type ExportButtonProps = {
  label?: string
  moduleName?: string
}

export function ExportButton({ 
  label = '导出', 
  moduleName = '数据' 
}: ExportButtonProps) {
  const handleExport = () => {
    // 不调用接口，仅显示提示
    toast.info(`正在导出${moduleName}...`)
  }

  return (
    <Button
      variant='outline'
      className='space-x-1'
      onClick={handleExport}
    >
      <span>{label}</span>
      <Download size={18} />
    </Button>
  )
}

