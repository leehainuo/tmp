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
import { PasswordInput } from '@/components/password-input'
import { SelectDropdown } from '@/components/select-dropdown'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { Checkbox } from '@/components/ui/checkbox'
import { ChevronDown } from 'lucide-react'
import { cn } from '@/lib/utils'
import { type User, userFormSchema, type UserForm } from '../data/schema'
import { userApi, roleApi, deptApi, type SysUser } from '@/lib/api'
import { useSysUser } from '../providers/sys-user-provider'
import { toast } from 'sonner'

type SysUserActionDialogProps = {
  currentRow?: User
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function SysUserActionDialog({
  currentRow,
  open,
  onOpenChange,
}: SysUserActionDialogProps) {
  const isEdit = !!currentRow
  const { refresh } = useSysUser()
  const [loading, setLoading] = useState(false)

  // 获取角色列表
  const { data: roles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: () => roleApi.getAll(),
    enabled: open,
  })

  // 获取部门树
  const { data: deptTree = [] } = useQuery({
    queryKey: ['deptTree'],
    queryFn: () => deptApi.getTree(),
    enabled: open,
  })

  // 将部门树转换为扁平列表（用于下拉选择）
  const flattenDepts = (depts: typeof deptTree, level = 0): Array<{ id: number; deptName: string; level: number }> => {
    const result: Array<{ id: number; deptName: string; level: number }> = []
    depts.forEach((dept) => {
      if (dept.id !== undefined) {
        result.push({ id: dept.id, deptName: '  '.repeat(level) + dept.deptName, level })
        if (dept.children && dept.children.length > 0) {
          result.push(...flattenDepts(dept.children, level + 1))
        }
      }
    })
    return result
  }

  const deptOptions = flattenDepts(deptTree)

  const form = useForm<UserForm>({
    // @ts-expect-error - zodResolver type inference issue with react-hook-form
    resolver: zodResolver(userFormSchema),
    defaultValues: isEdit && currentRow
      ? {
          id: currentRow.id || currentRow.userId,
          username: currentRow.username || '',
          nickname: currentRow.nickname || '',
          email: currentRow.email || '',
          phone: currentRow.phone || '',
          deptId: currentRow.deptId || currentRow.dept?.id,
          roleIds: currentRow.roleIds || currentRow.roles?.map(r => r.id) || [],
          status: (currentRow.status === 0 || currentRow.status === 1) ? currentRow.status : 1,
          sex: (currentRow.sex === 0 || currentRow.sex === 1 || currentRow.sex === 2) ? currentRow.sex : undefined,
          password: '',
          confirmPassword: '',
          isEdit: true,
        }
      : {
          username: '',
          nickname: '',
          email: '',
          phone: '',
          deptId: undefined,
          roleIds: [],
          status: 1,
          sex: undefined,
          password: '',
          confirmPassword: '',
          isEdit: false,
        },
  })

  // 编辑时获取完整用户信息
  useEffect(() => {
    if (isEdit && currentRow && open) {
      const userId = currentRow.id || currentRow.userId
      if (userId) {
        userApi.getById(userId).then((user) => {
          const userSex = user.sex
          form.reset({
            id: user.id || user.userId,
            username: user.username || '',
            nickname: user.nickname || '',
            email: user.email || '',
            phone: user.phone || '',
            deptId: user.deptId || user.dept?.id,
            roleIds: user.roleIds || user.roles?.map(r => r.id) || [],
            status: (user.status === 0 || user.status === 1) ? user.status : 1,
            sex: (userSex === 0 || userSex === 1 || userSex === 2) ? userSex : undefined,
            password: '',
            confirmPassword: '',
            isEdit: true,
          })
        }).catch(() => {
          toast.error('获取用户信息失败')
        })
      }
    }
  }, [isEdit, currentRow, open, form])

  const onSubmit = async (values: UserForm) => {
    setLoading(true)
    try {
      const userData: SysUser = {
        id: values.id,
        username: values.username,
        nickname: values.nickname,
        email: values.email || undefined,
        phone: values.phone || undefined,
        deptId: values.deptId,
        roleIds: values.roleIds,
        status: (values.status === 0 || values.status === 1) ? values.status : 1,
        sex: values.sex,
        ...(values.password ? { password: values.password } : {}),
      }

      if (isEdit) {
        await userApi.update(userData)
        toast.success('更新用户成功')
      } else {
        await userApi.add(userData)
        toast.success('新增用户成功')
      }

      form.reset()
      onOpenChange(false)
      refresh?.()
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : (isEdit ? '更新用户失败' : '新增用户失败')
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  const isPasswordTouched = !!form.formState.dirtyFields.password

  return (
    <Dialog
      open={open}
      onOpenChange={(state) => {
        form.reset()
        onOpenChange(state)
      }}
    >
      <DialogContent className='sm:max-w-lg'>
        <DialogHeader className='text-start'>
          <DialogTitle>{isEdit ? '编辑用户' : '新增用户'}</DialogTitle>
          <DialogDescription>
            {isEdit ? '更新用户信息。' : '创建新用户。'}
            完成后点击保存。
          </DialogDescription>
        </DialogHeader>
        <div className='h-[26.25rem] w-[calc(100%+0.75rem)] overflow-y-auto py-1 pe-3'>
          <Form {...form}>
            <form
              id='user-form'
              // @ts-expect-error - react-hook-form type inference issue with zodResolver
              onSubmit={form.handleSubmit(onSubmit)}
              className='space-y-4 px-0.5'
            >
              <FormField
                // @ts-expect-error - react-hook-form type inference issue with zodResolver
                control={form.control}
                name='username'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      用户名
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入用户名'
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
                name='nickname'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      昵称
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入昵称'
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
                      手机号
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder='请输入手机号'
                        className='col-span-4'
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
                name='deptId'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>部门</FormLabel>
                    <FormControl>
                      <SelectDropdown
                        defaultValue={field.value?.toString()}
                        onValueChange={(value) => field.onChange(value ? Number(value) : undefined)}
                        placeholder='请选择部门'
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
                name='roleIds'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>角色</FormLabel>
                    <FormControl>
                      <Popover>
                        <PopoverTrigger asChild>
                          <Button
                            variant='outline'
                            role='combobox'
                            className={cn(
                              'col-span-4 justify-between',
                              (!field.value || field.value.length === 0) && 'text-muted-foreground'
                            )}
                          >
                            {field.value && field.value.length > 0
                              ? `${field.value.length} 个角色已选择`
                              : '请选择角色'}
                            <ChevronDown className='ml-2 h-4 w-4 shrink-0 opacity-50' />
                          </Button>
                        </PopoverTrigger>
                        <PopoverContent className='w-[200px] p-0' align='start'>
                          <div className='max-h-[300px] overflow-y-auto p-2'>
                            {roles.length === 0 ? (
                              <div className='py-6 text-center text-sm text-muted-foreground'>
                                暂无角色
                              </div>
                            ) : (
                              <div className='space-y-2'>
                                {roles.map((role) => (
                                  <label
                                    key={role.id}
                                    className='flex items-center space-x-2 cursor-pointer rounded-sm px-2 py-1.5 hover:bg-accent'
                                  >
                                    <Checkbox
                                      checked={field.value?.includes(role.id) || false}
                                      onCheckedChange={(checked) => {
                                        const currentIds = field.value || []
                                        if (checked) {
                                          field.onChange([...currentIds, role.id])
                                        } else {
                                          field.onChange(currentIds.filter(id => id !== role.id))
                                        }
                                      }}
                                    />
                                    <span className='text-sm'>{role.roleName}</span>
                                  </label>
                                ))}
                              </div>
                            )}
                          </div>
                        </PopoverContent>
                      </Popover>
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
                name='password'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      密码
                    </FormLabel>
                    <FormControl>
                      <PasswordInput
                        placeholder={isEdit ? '留空则不修改密码' : '请输入密码'}
                        className='col-span-4'
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
                name='confirmPassword'
                render={({ field }) => (
                  <FormItem className='grid grid-cols-6 items-center space-y-0 gap-x-4 gap-y-1'>
                    <FormLabel className='col-span-2 text-end'>
                      确认密码
                    </FormLabel>
                    <FormControl>
                      <PasswordInput
                        disabled={!isPasswordTouched}
                        placeholder={isEdit ? '留空则不修改密码' : '请再次输入密码'}
                        className='col-span-4'
                        {...field}
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
          <Button type='submit' form='user-form' disabled={loading}>
            {loading ? '保存中...' : '保存'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

