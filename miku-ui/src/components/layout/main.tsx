import { cn } from '@/lib/utils'
import { useLayout } from '@/providers/layout-provider'

type MainProps = React.HTMLAttributes<HTMLElement> & {
  fixed?: boolean
  fluid?: boolean
  ref?: React.Ref<HTMLElement>
}

export function Main({ fixed, className, fluid, ...props }: MainProps) {
  const { variant } = useLayout()

  return (
    <main
      data-layout={fixed ? 'fixed' : 'auto'}
      className={cn(
        'mx-auto w-[calc(100%-2rem)] py-4 sm:w-[calc(100%-3rem)]',

        variant === 'floating' || variant === 'inset'
        ? ''
        : 'bg-inset dark:bg-background',

        // If layout is fixed, make the main container flex and grow
        fixed && 'flex grow flex-col overflow-hidden',

        // If layout is not fluid, set the max-width
        !fluid &&
          '', //'@7xl/content:mx-auto @7xl/content:w-full @7xl/content:max-w-7xl',
        className
      )}
      {...props}
    />
  )
}
// import { cn } from '@/lib/utils'
// import { useLayout } from '@/providers/layout-provider'
//
// type MainProps = React.HTMLAttributes<HTMLElement> & {
//   fixed?: boolean
//   fluid?: boolean
//   ref?: React.Ref<HTMLElement>
// }
//
// export function Main({ fixed, className, fluid, ...props }: MainProps) {
//   const { variant } = useLayout()
//   const isInsetLike = variant === 'floating' || variant === 'inset'
//
//   return (
//     <main
//       data-layout={fixed ? 'fixed' : 'auto'}
//       className={cn(
//         'px-4 py-6',
//         // 浮动 / inset 时透明背景；其它布局用 bg-muted
//         isInsetLike ? 'bg-transparent' : 'bg-muted',
//         // 固定布局填满
//         fixed && 'flex grow flex-col overflow-hidden',
//         // 只在非 fluid 且非 inset/floating 时限制最大宽度
//         !fluid && !isInsetLike &&
//           '@7xl/content:mx-auto @7xl/content:w-full @7xl/content:max-w-7xl',
//         className
//       )}
//       {...props}
//     />
//   )
// }