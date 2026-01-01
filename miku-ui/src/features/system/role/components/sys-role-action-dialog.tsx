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
import { type Role, roleFormSchema, type RoleForm, DATA_SCOPE } from '../data/schema'
import { roleApi, permissionApi, deptApi, type SysRole } from '@/lib/api'
import { useSysRole } from '../providers/sys-role-provider'
import { PermissionTreeSelect } from './permission-tree-select'
import { DeptTreeSelect } from './dept-tree-select'
import { toast } from 'sonner'

type SysRoleActionDialogProps = {
  currentRow?: Role
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function SysRoleActionDialog({
  currentRow,
  open,
  onOpenChange,
}: SysRoleActionDialogProps) {
  const isEdit = !!currentRow
  const { refresh } = useSysRole()
  const [loading, setLoading] = useState(false)

  // 获取权限树
  const { data: permissionTree = [] } = useQuery({
    queryKey: ['permissionTree'],
    queryFn: () => permissionApi.list(),
    enabled: open,
  })

  // 获取部门树
  const { data: deptTree = [] } = useQuery({
    queryKey: ['deptTree'],
    queryFn: () => deptApi.getTree(),
    enabled: open,
  })

  const form = useForm<RoleForm>({
    // @ts-expect-error - zodResolver type inference issue with react-hook-form
    resolver: zodResolver(roleFormSchema),
    defaultValues: isEdit && currentRow
      ? {
          id: currentRow.id,
          roleName: currentRow.roleName || '',
          roleCode: currentRow.roleCode || '',
          roleSort: currentRow.roleSort,
          status: (currentRow.status === 0 || currentRow.status === 1) ? currentRow.status : 1,
          remark: currentRow.remark || '',
          permissionIds: currentRow.permissionIds || [],
          dataScope: (currentRow.dataScope as 1 | 2 | 3 | 4 | 5) || DATA_SCOPE.ALL,
          deptIds: currentRow.deptIds || [],
        }
      : {
          roleName: '',
          roleCode: '',
          roleSort: undefined,
          status: 1,
          remark: '',
          permissionIds: [],
          dataScope: DATA_SCOPE.ALL as 1 | 2 | 3 | 4 | 5,
          deptIds: [],
        },
  })

  // 监听数据权限范围变化，当不是自定义时清空部门选择
  const dataScope = form.watch('dataScope')

  // 编辑时获取完整角色信息（包含权限和数据权限）
  useEffect(() => {
    if (isEdit && currentRow && open) {
      const roleId = currentRow.id
      if (roleId) {
        roleApi.getById(roleId).then((role) => {
          // 处理权限ID，确保是数字数组
          let permissionIds: number[] = []
          if (role.permissionIds && Array.isArray(role.permissionIds)) {
            permissionIds = role.permissionIds
              .map((id) => typeof id === 'string' ? Number(id) : id)
              .filter((id) => !isNaN(id) && id > 0) as number[]
          }
          
          // 处理部门ID，确保是数字数组
          let deptIds: number[] = []
          if (role.deptIds && Array.isArray(role.deptIds)) {
            deptIds = role.deptIds
              .map((id) => typeof id === 'string' ? Number(id) : id)
              .filter((id) => !isNaN(id) && id > 0) as number[]
          }
          
          form.reset({
            id: role.id,
            roleName: role.roleName || '',
            roleCode: role.roleCode || '',
            roleSort: role.roleSort,
            status: (role.status === 0 || role.status === 1) ? role.status : 1,
            remark: role.remark || '',
            permissionIds,
            dataScope: (role.dataScope as 1 | 2 | 3 | 4 | 5) || DATA_SCOPE.ALL,
            deptIds,
          })
        }).catch(() => {
          toast.error('获取角色信息失败')
        })
      }
    } else if (!isEdit && open) {
      // 新增时重置表单
      form.reset({
        roleName: '',
        roleCode: '',
        roleSort: undefined,
        status: 1,
        remark: '',
        permissionIds: [],
        dataScope: DATA_SCOPE.ALL as 1 | 2 | 3 | 4 | 5,
        deptIds: [],
      })
    }
  }, [isEdit, currentRow, open, form])

  // 当数据权限范围不是自定义时，清空部门选择
  useEffect(() => {
    if (dataScope !== DATA_SCOPE.CUSTOM) {
      form.setValue('deptIds', [])
    }
  }, [dataScope, form])

  const onSubmit = async (values: RoleForm) => {
    setLoading(true)
    try {
      const roleData: SysRole = {
        ...(values.id !== undefined && { id: values.id }),
        roleName: values.roleName,
        roleCode: values.roleCode,
        roleSort: values.roleSort,
        status: (values.status === 0 || values.status === 1) ? values.status : 1,
        remark: values.remark || undefined,
        // 始终传递权限ID列表，空数组表示清空权限
        permissionIds: values.permissionIds || [],
        dataScope: values.dataScope || DATA_SCOPE.ALL,
      }

      if (isEdit) {
        await roleApi.update(roleData)
        // 更新数据权限范围
        if (values.dataScope !== undefined) {
          await roleApi.assignDataScope(
            values.id!,
            values.dataScope,
            values.dataScope === DATA_SCOPE.CUSTOM ? values.deptIds : undefined
          )
        }
        toast.success('更新角色成功')
      } else {
        await roleApi.add(roleData)
        // 新增角色后，刷新列表会自动获取新创建的角色
        // 数据权限范围可以在编辑时设置
        // 如果需要在新增时立即设置，需要后端返回新创建的角色ID
        toast.success('新增角色成功')
      }

      form.reset()
      onOpenChange(false)
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : (isEdit ? '更新角色失败' : '新增角色失败')
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
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader className='text-start'>
          <DialogTitle>{isEdit ? '编辑角色' : '新增角色'}</DialogTitle>
          <DialogDescription>
            {isEdit ? '更新角色信息。' : '创建新角色。'}
            完成后点击保存。
          </DialogDescription>
        </DialogHeader>
        <div className='max-h-[calc(100vh-12rem)] w-[calc(100%+0.75rem)] overflow-y-auto py-1 pe-3'>
          <Form {...form}>
            <form
              id='role-form'
              // @ts-expect-error - react-hook-form type inference issue with zodResolver
              onSubmit={form.handleSubmit(onSubmit)}
              className='space-y-4 px-0.5'
            >
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='roleName'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      角色名称
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入角色名称'
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
                name='roleCode'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      角色编码
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入角色编码'
                        className='col-span-4'
                        autoComplete='off'
                        disabled={isEdit}
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
                name='roleSort'
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
                        autoComplete='off'
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
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='dataScope'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      数据权限范围
                    </FormLabel>
                    <FormControl>
                      <SelectDropdown
                        defaultValue={field.value?.toString() || DATA_SCOPE.ALL.toString()}
                        onValueChange={(value) => field.onChange(Number(value))}
                        placeholder='请选择数据权限范围'
                        className='col-span-4'
                        isControlled
                        items={[
                          { label: '全部数据权限', value: DATA_SCOPE.ALL.toString() },
                          { label: '自定义数据权限', value: DATA_SCOPE.CUSTOM.toString() },
                          { label: '本部门数据权限', value: DATA_SCOPE.DEPT.toString() },
                          { label: '本部门及以下数据权限', value: DATA_SCOPE.DEPT_AND_CHILD.toString() },
                          { label: '仅本人数据权限', value: DATA_SCOPE.SELF.toString() },
                        ]}
                      />
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
              {dataScope === DATA_SCOPE.CUSTOM && (
                <FormField
                  // @ts-expect-error - react-hook-form type inference issue with zodResolver
                  control={form.control}
                  name='deptIds'
                  render={({ field }) => (
                    <FormItem className='grid grid-cols-6 items-start space-y-0 gap-x-4 gap-y-1'>
                      <FormLabel className='col-span-2 text-end pt-2'>
                        部门选择
                      </FormLabel>
                      <FormControl>
                        <div className='col-span-4'>
                          <DeptTreeSelect
                            depts={deptTree}
                            selectedIds={field.value || []}
                            onSelectionChange={(ids) => field.onChange(ids)}
                          />
                        </div>
                      </FormControl>
                      <FormMessage className='col-span-4 col-start-3' />
                    </FormItem>
                  )}
                />
              )}
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='permissionIds'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-start space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end pt-2'>
                      权限分配
                    </FormLabel>
                    <FormControl>
                      <div className='col-span-4'>
                        <PermissionTreeSelect
                          permissions={permissionTree}
                          selectedIds={field.value || []}
                          onSelectionChange={(ids) => field.onChange(ids)}
                        />
                      </div>
                    </FormControl>
                    <FormMessage className='col-span-4 col-start-3' />
                  </FormItem>
                )}
              />
            </form>
          </Form>
        </div>
        <DialogFooter>
          <Button type='submit' form='role-form' disabled={loading}>
            {loading ? '保存中...' : '保存'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

