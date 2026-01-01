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
import { Textarea } from '@/components/ui/textarea'
import { type Permission, permissionFormSchema, type PermissionForm, type PermissionType } from '../data/schema'
import { permissionApi, type SysPermission } from '@/lib/api'
import { useSysPermission } from '../providers/sys-permission-provider'
import { toast } from 'sonner'

type SysPermissionActionDialogProps = {
  currentRow?: Permission
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function SysPermissionActionDialog({
  currentRow,
  open,
  onOpenChange,
}: SysPermissionActionDialogProps) {
  const isEdit = !!currentRow
  const { refresh } = useSysPermission()
  const [loading, setLoading] = useState(false)

  // 获取权限树（用于选择上级权限）
  const { data: permissionTree = [] } = useQuery({
    queryKey: ['permissionTree'],
    queryFn: () => permissionApi.list(),
    enabled: open,
  })

  const ensurePermissionType = (type?: number | null): PermissionType => {
    return type === 1 || type === 2 || type === 3 || type === 4 ? type : 1
  }

  // 将权限树转换为扁平列表（用于下拉选择）
  const flattenPermissions = (permissions: typeof permissionTree, level = 0, excludeId?: number): Array<{ id: number; permissionName: string; level: number }> => {
    const result: Array<{ id: number; permissionName: string; level: number }> = []
    permissions.forEach((permission) => {
      // 排除当前编辑的权限（避免选择自己作为父级）
      if (permission.id !== excludeId) {
        result.push({ 
          id: permission.id!, 
          permissionName: '  '.repeat(level) + permission.permissionName, 
          level 
        })
        if (permission.children && permission.children.length > 0) {
          result.push(...flattenPermissions(permission.children, level + 1, excludeId))
        }
      }
    })
    return result
  }

  const permissionOptions = flattenPermissions(permissionTree, 0, currentRow?.id)
  // 添加根节点选项
  permissionOptions.unshift({ id: 0, permissionName: '根权限', level: 0 })

  const form = useForm<PermissionForm>({
    // @ts-expect-error - zodResolver type inference issue with react-hook-form
    resolver: zodResolver(permissionFormSchema),
    defaultValues: isEdit && currentRow
      ? {
          id: currentRow.id,
          parentId: currentRow.parentId ?? 0,
          permissionName: currentRow.permissionName || '',
          permissionType: ensurePermissionType(currentRow.permissionType),
          perms: currentRow.perms || '',
          path: currentRow.path || '',
          component: currentRow.component || '',
          icon: currentRow.icon || '',
          orderNum: currentRow.orderNum ?? undefined,
          visible: (currentRow.visible === 0 || currentRow.visible === 1) ? currentRow.visible : 1,
          status: (currentRow.status === 0 || currentRow.status === 1) ? currentRow.status : 1,
          apiMethod: currentRow.apiMethod || '',
          apiPath: currentRow.apiPath || '',
          remark: currentRow.remark || '',
        }
      : {
          parentId: 0,
          permissionName: '',
          permissionType: 1,
          perms: '',
          path: '',
          component: '',
          icon: '',
          orderNum: undefined,
          visible: 1,
          status: 1,
          apiMethod: '',
          apiPath: '',
          remark: '',
        },
  })

  // 编辑时获取完整权限信息
  useEffect(() => {
    if (isEdit && currentRow && open) {
      const permissionId = currentRow.id
      if (permissionId) {
        permissionApi.getById(permissionId).then((permission) => {
          form.reset({
            id: permission.id,
            parentId: permission.parentId ?? 0,
            permissionName: permission.permissionName || '',
            permissionType: ensurePermissionType(permission.permissionType),
            perms: permission.perms || '',
            path: permission.path || '',
            component: permission.component || '',
            icon: permission.icon || '',
            orderNum: permission.orderNum ?? undefined,
            visible: (permission.visible === 0 || permission.visible === 1) ? permission.visible : 1,
            status: (permission.status === 0 || permission.status === 1) ? permission.status : 1,
            apiMethod: permission.apiMethod || '',
            apiPath: permission.apiPath || '',
            remark: permission.remark || '',
          })
        }).catch(() => {
          toast.error('获取权限信息失败')
        })
      }
    }
  }, [isEdit, currentRow, open, form])

  const onSubmit = async (values: PermissionForm) => {
    setLoading(true)
    try {
      const permissionData: SysPermission = {
        id: values.id,
        parentId: values.parentId === 0 ? null : values.parentId,
        permissionName: values.permissionName,
        permissionType: values.permissionType,
        perms: values.perms || undefined,
        path: values.path || undefined,
        component: values.component || undefined,
        icon: values.icon || undefined,
        orderNum: values.orderNum ?? undefined,
        visible: (values.visible === 0 || values.visible === 1) ? values.visible : 1,
        status: (values.status === 0 || values.status === 1) ? values.status : 1,
        apiMethod: values.apiMethod || undefined,
        apiPath: values.apiPath || undefined,
        remark: values.remark || undefined,
      }

      if (isEdit) {
        await permissionApi.update(permissionData)
        toast.success('更新权限成功')
      } else {
        await permissionApi.add(permissionData)
        toast.success('新增权限成功')
      }

      form.reset()
      onOpenChange(false)
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : (isEdit ? '更新权限失败' : '新增权限失败')
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  const permissionType = form.watch('permissionType')

  return (
    <Dialog
      open={open}
      onOpenChange={(state) => {
        form.reset()
        onOpenChange(state)
      }}
    >
      <DialogContent className='sm:max-w-2xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader className='text-start'>
          <DialogTitle>{isEdit ? '编辑权限' : '新增权限'}</DialogTitle>
          <DialogDescription>
            {isEdit ? '更新权限信息。' : '创建新权限。'}
            完成后点击保存。
          </DialogDescription>
        </DialogHeader>
        <div className='w-[calc(100%+0.75rem)] overflow-y-auto py-1 pe-3'>
          <Form {...form}>
            <form
              id='permission-form'
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
                    <FormLabel className='col-span-2 text-end'>上级权限</FormLabel>
                    <FormControl>
                      <SelectDropdown
                        defaultValue={field.value?.toString() || '0'}
                        onValueChange={(value) => field.onChange(value === '0' ? 0 : Number(value))}
                        placeholder='请选择上级权限'
                        className='col-span-4'
                        isControlled
                        items={permissionOptions.map((permission) => ({
                          label: permission.permissionName,
                          value: permission.id.toString(),
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
                name='permissionName'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      权限名称
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入权限名称'
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
                name='permissionType'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>权限类型</FormLabel>
                    <FormControl>
                      <SelectDropdown
                        defaultValue={field.value?.toString()}
                        onValueChange={(value) => field.onChange(Number(value))}
                        placeholder='请选择权限类型'
                        className='col-span-4'
                        isControlled
                        items={[
                          { label: '目录', value: '1' },
                          { label: '菜单', value: '2' },
                          { label: '按钮', value: '3' },
                          { label: '接口', value: '4' },
                        ]}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              {(permissionType === 1 || permissionType === 2) && (
                <>
                  <FormField
                    // @ts-expect-error - react-hook-form type inference issue with zodResolver
                    control={form.control}
                    name='path'
                    render={({ field }) => (
                      <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>路由地址</FormLabel>
                        <FormControl>
                          <Input
                            placeholder='请输入路由地址'
                            className='col-span-4'
                            {...field}
                            value={field.value || ''}
                          />
                        </FormControl>
                        <FormMessage className='col-span-4 col-start-3' />
                      </FormItem>
                    )}
                  />
                  {permissionType === 2 && (
                    <FormField
                      // @ts-expect-error - react-hook-form type inference issue with zodResolver
                      control={form.control}
                      name='component'
                      render={({ field }) => (
                        <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                          <FormLabel className='col-span-2 text-end'>组件路径</FormLabel>
                          <FormControl>
                            <Input
                              placeholder='请输入组件路径'
                              className='col-span-4'
                              {...field}
                              value={field.value || ''}
                            />
                          </FormControl>
                          <FormMessage className='col-span-4 col-start-3' />
                        </FormItem>
                      )}
                    />
                  )}
                </>
              )}
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='perms'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>权限标识</FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入权限标识，如：system:user:list'
                        className='col-span-4'
                        {...field}
                        value={field.value || ''}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              {(permissionType === 1 || permissionType === 2) && (
                <FormField
                  // @ts-expect-error - react-hook-form type inference issue with zodResolver
                  control={form.control}
                  name='icon'
                  render={({ field }) => (
                    <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                      <FormLabel className='col-span-2 text-end'>图标</FormLabel>
                      <FormControl>
                        <Input
                          placeholder='请输入图标名称'
                          className='col-span-4'
                          {...field}
                          value={field.value || ''}
                        />
                      </FormControl>
                      <FormMessage className='col-span-4 col-start-3' />
                    </FormItem>
                  )}
                />
              )}
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='orderNum'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>排序</FormLabel>
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
              {(permissionType === 1 || permissionType === 2) && (
                <FormField
                  // @ts-expect-error - react-hook-form type inference issue with zodResolver
                  control={form.control}
                  name='visible'
                  render={({ field }) => (
                    <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                      <FormLabel className='col-span-2 text-end'>是否可见</FormLabel>
                      <FormControl>
                        <SelectDropdown
                          defaultValue={field.value?.toString() || '1'}
                          onValueChange={(value) => field.onChange(Number(value))}
                          placeholder='请选择'
                          className='col-span-4'
                          isControlled
                          items={[
                            { label: '显示', value: '1' },
                            { label: '隐藏', value: '0' },
                          ]}
                        />
                      </FormControl>
                      <FormMessage className='col-span-4 col-start-3' />
                    </FormItem>
                  )}
                />
              )}
              {permissionType === 4 && (
                <>
                  <FormField
                    // @ts-expect-error - react-hook-form type inference issue with zodResolver
                    control={form.control}
                    name='apiMethod'
                    render={({ field }) => (
                      <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>API方法</FormLabel>
                        <FormControl>
                          <SelectDropdown
                            defaultValue={field.value || ''}
                            onValueChange={(value) => field.onChange(value || undefined)}
                            placeholder='请选择API方法'
                            className='col-span-4'
                            isControlled
                            items={[
                              { label: 'GET', value: 'GET' },
                              { label: 'POST', value: 'POST' },
                              { label: 'PUT', value: 'PUT' },
                              { label: 'DELETE', value: 'DELETE' },
                            ]}
                          />
                        </FormControl>
                        <FormMessage className='col-span-4 col-start-3' />
                      </FormItem>
                    )}
                  />
                  <FormField
                    // @ts-expect-error - react-hook-form type inference issue with zodResolver
                    control={form.control}
                    name='apiPath'
                    render={({ field }) => (
                      <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                        <FormLabel className='col-span-2 text-end'>API路径</FormLabel>
                        <FormControl>
                          <Input
                            placeholder='请输入API路径，如：/system/user/list'
                            className='col-span-4'
                            {...field}
                            value={field.value || ''}
                          />
                        </FormControl>
                        <FormMessage className='col-span-4 col-start-3' />
                      </FormItem>
                    )}
                  />
                </>
              )}
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
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='remark'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-start space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end pt-2'>
                      备注
                    </FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder='请输入备注'
                        className='col-span-4 min-h-[80px]'
                        {...field}
                        value={field.value || ''}
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
          <Button type='submit' form='permission-form' disabled={loading}>
            {loading ? '保存中...' : '保存'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

