import { Link } from '@tanstack/react-router'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { AuthLayout } from '../auth-layout'
import { SignUpForm } from './components/sign-up-form'

export function SignUp() {
  return (
    <AuthLayout>
      <Card className='gap-4'>
        <CardHeader>
          <CardTitle className='text-lg tracking-tight'>
            创建账号
          </CardTitle>
          <CardDescription>
            请输入邮箱和密码创建账号。 <br />
            已有账号？{' '}
            <Link
              to='/sign-in'
              className='hover:text-primary underline underline-offset-4'
            >
              登录
            </Link>
          </CardDescription>
        </CardHeader>
        <CardContent>
          <SignUpForm />
        </CardContent>
        <CardFooter>
          <p className='text-muted-foreground px-8 text-center text-sm'>
            创建账号，即表示您同意我们的{' '}
            <a
              href='/terms'
              className='hover:text-primary underline underline-offset-4'
            >
              服务条款
            </a>{' '}
            and{' '}
            <a
              href='/privacy'
              className='hover:text-primary underline underline-offset-4'
            >
              隐私政策
            </a>
            。
          </p>
        </CardFooter>
      </Card>
    </AuthLayout>
  )
}
