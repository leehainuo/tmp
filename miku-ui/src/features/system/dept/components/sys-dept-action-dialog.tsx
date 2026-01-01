'use client'

import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { SelectDropdown } from '@/components/select-dropdown'
import { type Dept, deptFormSchema, type DeptForm } from '../data/schema'
import { deptApi, type SysDept } from '@/lib/api'
import { useSysDept } from '../providers/sys-dept-provider'
import { toast } from 'sonner'

type SysDeptActionDialogProps = {
  currentRow?: Dept
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function SysDeptActionDialog({
  currentRow,
  open,
  onOpenChange,
}: SysDeptActionDialogProps) {
  const isEdit = !!currentRow
  const { refresh } = useSysDept()
  const [loading, setLoading] = useState(false)

  const { data: deptTree = [] } = useQuery({
    queryKey: ['deptTreeSelector'],
    queryFn: () => deptApi.listTree(),
    enabled: open,
  })

  const flattenDepts = (
    depts: typeof deptTree,
    level = 0,
    excludeId?: number
  ): Array<{ id: number; deptName: string; level: number }> => {
    const result: Array<{ id: number; deptName: string; level: number }> = []
    depts.forEach((dept) => {
      if (dept.id !== excludeId) {
        result.push({
          id: dept.id!,
          deptName: '  '.repeat(level) + dept.deptName,
          level,
        })
        if (dept.children && dept.children.length > 0) {
          result.push(...flattenDepts(dept.children, level + 1, excludeId))
        }
      }
    })
    return result
  }

  const deptOptions = flattenDepts(deptTree, 0, currentRow?.id)
  deptOptions.unshift({ id: 0, deptName: '顶级部门', level: 0 })

  const form = useForm<DeptForm>({
    // @ts-expect-error - zodResolver type inference issue with react-hook-form
    resolver: zodResolver(deptFormSchema),
    defaultValues: isEdit && currentRow
      ? {
          id: currentRow.id,
          parentId: currentRow.parentId ?? 0,
          deptName: currentRow.deptName || '',
          orderNum: currentRow.orderNum ?? undefined,
          leader: currentRow.leader || '',
          phone: currentRow.phone || '',
          email: currentRow.email || '',
          status: (currentRow.status === 0 || currentRow.status === 1) ? currentRow.status : 1,
        }
      : {
          parentId: 0,
          deptName: '',
          orderNum: undefined,
          leader: '',
          phone: '',
          email: '',
          status: 1,
        },
  })

  useEffect(() => {
    if (isEdit && currentRow && open) {
      const deptId = currentRow.id
      if (deptId) {
        deptApi.getById(deptId).then((dept) => {
          form.reset({
            id: dept.id,
            parentId: dept.parentId ?? 0,
            deptName: dept.deptName || '',
            orderNum: dept.orderNum ?? undefined,
            leader: dept.leader || '',
            phone: dept.phone || '',
            email: dept.email || '',
            status: (dept.status === 0 || dept.status === 1) ? dept.status : 1,
          })
        }).catch(() => {
          toast.error('获取部门信息失败')
        })
      }
    }
  }, [isEdit, currentRow, open, form])

  const onSubmit = async (values: DeptForm) => {
    setLoading(true)
    try {
      const deptData: SysDept = {
        id: values.id,
        parentId: values.parentId === 0 ? null : values.parentId,
        deptName: values.deptName,
        orderNum: values.orderNum ?? undefined,
        leader: values.leader || undefined,
        phone: values.phone || undefined,
        email: values.email || undefined,
        status: (values.status === 0 || values.status === 1) ? values.status : 1,
      }

      if (isEdit) {
        await deptApi.update(deptData)
        toast.success('更新部门成功')
      } else {
        await deptApi.add(deptData)
        toast.success('新增部门成功')
      }

      form.reset()
      onOpenChange(false)
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : (isEdit ? '更新部门失败' : '新增部门失败')
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(state) => {
        form.reset()
        onOpenChange(state)
      }}
    >
      <DialogContent className='sm:max-w-xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader className='text-start'>
          <DialogTitle>{isEdit ? '编辑部门' : '新增部门'}</DialogTitle>
          <DialogDescription>
            {isEdit ? '更新部门信息。' : '创建新部门。'}
            完成后点击保存。
          </DialogDescription>
        </DialogHeader>
        <div className='w-[calc(100%+0.75rem)] overflow-y-auto py-1 pe-3'>
          <Form {...form}>
            <form
              id='dept-form'
              // @ts-expect-error - react-hook-form type inference issue with zodResolver
              onSubmit={form.handleSubmit(onSubmit)}
              className='space-y-4 px-0.5'
            >
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='parentId'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>上级部门</FormLabel>
                    <FormControl>
                      <SelectDropdown
                        defaultValue={field.value?.toString() || '0'}
                        onValueChange={(value) => field.onChange(value === '0' ? 0 : Number(value))}
                        placeholder='请选择上级部门'
                        className='col-span-4'
                        isControlled
                        items={deptOptions.map((dept) => ({
                          label: dept.deptName,
                          value: dept.id.toString(),
                        }))}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='deptName'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      部门名称
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入部门名称'
                        className='col-span-4'
                        autoComplete='off'
                        {...field}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              <FormField
              // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='leader'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      负责人
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入负责人'
                        className='col-span-4'
                        autoComplete='off'
                        {...field}
                        value={field.value || ''}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='phone'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      联系电话
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入联系电话'
                        className='col-span-4'
                        {...field}
                        value={field.value || ''}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='email'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>邮箱</FormLabel>
                    <FormControl>
                      <Input
                        type='email'
                        placeholder='example@email.com'
                        className='col-span-4'
                        {...field}
                        value={field.value || ''}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='orderNum'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      排序
                    </FormLabel>
                    <FormControl>
                      <Input
                        type='number'
                        placeholder='请输入排序号'
                        className='col-span-4'
                        {...field}
                        value={field.value ?? ''}
                        onChange={(e) => field.onChange(e.target.value ? Number(e.target.value) : undefined)}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='status'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>状态</FormLabel>
                    <FormControl>
                      <SelectDropdown
                        defaultValue={field.value?.toString()}
                        onValueChange={(value) => field.onChange(Number(value))}
                        placeholder='请选择状态'
                        className='col-span-4'
                        isControlled
                        items={[
                          { label: '正常', value: '1' },
                          { label: '停用', value: '0' },
                        ]}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
            </form>
          </Form>
        </div>
        <DialogFooter>
          <Button type='submit' form='dept-form' disabled={loading}>
            {loading ? '保存中...' : '保存'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}


