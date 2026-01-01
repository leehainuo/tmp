### 可以的，你可以做一个“小型主题工作台”

你现在的技术栈（CSS 变量 + Tailwind + 基础 UI 组件）非常适合做你截图那种「组件墙 + 即时调参」的界面。下面给你一套**可直接抄的实现思路 + 示例代码结构**，你只要新建几个组件/路由粘进去即可。

---

### 1. 核心思路

- **展示层**：一块区域用网格展示所有基础组件的当前样式：
  - `Button` 的各种 variant / size
  - `Input`
  - `Card`（包含 header / content / footer）
  - 甚至你的 `DataTable` 简单预览
- **控制层**：侧边或顶部放一排控件，用来修改 **CSS 变量**：
  - `--radius`
  - `--primary` / `--accent` / `--background`
  - 甚至 `--border`、`--input` 等
- **联动**：在 React 中通过 `document.documentElement.style.setProperty('--radius', '0.75rem')` 这类操作，**直接改变量**，所有组件实时更新。

你最终的页面可以像这样：

- 左边：sliders / color pickers（圆角、颜色、间距…）
- 右边：一整个组件预览墙（按钮、输入框、卡片、统计卡片…），类似你发的截图。

---

### 2. 建一个专门的「样式实验室」页面

例如新建一个页面组件 `ThemePlayground`（路由你可以挂在 `/dev/theme` 或 `/settings/theme-lab`）：

```tsx
// 示例：ThemePlayground.tsx（自己选路由挂载）
import { useEffect, useState } from 'react'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

type RadiusTokenKey = '--radius' | '--radius-sm' | '--radius-md' | '--radius-lg' | '--radius-xl'

const RADIUS_TOKENS: { key: RadiusTokenKey; label: string }[] = [
  { key: '--radius-sm', label: 'Sm' },
  { key: '--radius-md', label: 'Md' },
  { key: '--radius-lg', label: 'Lg' },
  { key: '--radius-xl', label: 'Xl' },
]

export function ThemePlayground() {
  const [baseRadius, setBaseRadius] = useState(10) // px，对应 --radius

  // 同步基础圆角变量
  useEffect(() => {
    document.documentElement.style.setProperty('--radius', `${baseRadius / 16}rem`)
  }, [baseRadius])

  return (
    <div className='p-8 space-y-8'>
      {/* 控制区 */}
      <Card>
        <CardHeader>
          <CardTitle>Radius 调整</CardTitle>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='flex items-center gap-4'>
            <span className='w-24 text-sm text-muted-foreground'>基础圆角 (px)</span>
            <input
              type='range'
              min={0}
              max={32}
              value={baseRadius}
              onChange={(e) => setBaseRadius(Number(e.target.value))}
              className='flex-1'
            />
            <Input
              type='number'
              className='w-20'
              value={baseRadius}
              onChange={(e) => setBaseRadius(Number(e.target.value) || 0)}
            />
          </div>

          <div className='flex flex-wrap gap-4'>
            {RADIUS_TOKENS.map((token) => (
              <TokenPreview key={token.key} token={token} />
            ))}
          </div>
        </CardContent>
      </Card>

      {/* 组件预览墙 */}
      <div className='grid gap-6 md:grid-cols-2 xl:grid-cols-3'>
        <ComponentCard title='Button'>
          <div className='flex flex-wrap gap-3'>
            <Button>Default</Button>
            <Button variant='outline'>Outline</Button>
            <Button variant='secondary'>Secondary</Button>
            <Button variant='ghost'>Ghost</Button>
            <Button variant='destructive'>Destructive</Button>
          </div>
        </ComponentCard>

        <ComponentCard title='Input'>
          <div className='space-y-3'>
            <Input placeholder='Search...' />
            <Input placeholder='Error state' aria-invalid='true' />
          </div>
        </ComponentCard>

        <ComponentCard title='Card'>
          <Card>
            <CardHeader>
              <CardTitle>示例卡片</CardTitle>
            </CardHeader>
            <CardContent>
              <p className='text-sm text-muted-foreground'>
                这里展示卡片的边框、阴影、圆角和内边距样式。
              </p>
            </CardContent>
          </Card>
        </ComponentCard>

        {/* 你可以继续加：标签、表单、表格、导航栏等 */}
      </div>
    </div>
  )
}

function ComponentCard(props: { title: string; children: React.ReactNode }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{props.title}</CardTitle>
      </CardHeader>
      <CardContent>{props.children}</CardContent>
    </Card>
  )
}

function TokenPreview({ token }: { token: { key: RadiusTokenKey; label: string } }) {
  return (
    <div className='flex flex-col items-center gap-2'>
      <div
        className='bg-muted border border-border w-16 h-10'
        style={{ borderRadius: `var(${token.key})` }}
      />
      <span className='text-xs text-muted-foreground'>{token.label}</span>
    </div>
  )
}
```

**这个组件的效果：**

- 上面一条 slider 控制 `--radius`，下面小块预览不同 radius token（你现在 `theme.css` 里已经有 `--radius-sm/md/lg/xl`）。
- 右边是一整墙：按钮、输入、卡片，所有样式都实时联动变化。
- 想再玩颜色，只要再加几个控制项，例如 `--primary`、`--background`，用 `<input type="color">` 即可。

---

### 3. 扩展到颜色 / 阴影 / 字体

你已经有这些变量：

- 颜色：`--primary / --accent / --background / --muted / --border / --ring ...`
- 字体：`--font-inter / --font-manrope`（配合 `FontProvider`）
- 圆角：`--radius-*`
- 图表色：`--chart-1` ~ `--chart-5`

可以做成一个通用的小控件：

```tsx
function ColorTokenControl({ label, varName }: { label: string; varName: string }) {
  const [value, setValue] = useState(
    getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
  )

  useEffect(() => {
    document.documentElement.style.setProperty(varName, value)
  }, [value, varName])

  return (
    <div className='flex items-center gap-3'>
      <span className='w-24 text-sm text-muted-foreground'>{label}</span>
      <input
        type='color'
        value`value.startsWith('oklch') ? '#000000' : value` // 简化处理：oklch 用预设；如果你换为 hex 就能完整控制
        onChange={(e) => setValue(e.target.value)}
      />
      <Input
        className='flex-1'
        value={value}
        onChange={(e) => setValue(e.target.value)}
      />
    </div>
  )
}
```

> 你可以先只玩圆角、边框、阴影这些“安全”的变量，颜色这块因为现在是 oklch，后面有精力可以再做更专业的 picker。

---

### 4. 如何让“设计师/用户”真正好用

- **路由收口**：放在 `/settings/appearance` 或 `/dev/style`，加上权限/环境控制（只在 dev 或 admin 登录时可见）。
- **导出配置**：加一个「复制 JSON」或「复制 CSS 变量」按钮：
  - 把当前 `getComputedStyle(document.documentElement)` 里你关心的变量收集成对象，`JSON.stringify` 出来，复制到剪贴板。
  - 之后你可以手动把这些值同步进 `theme.css`，就完成了「从可视化调整 → 固化到设计系统」。

---

### 5. 总结一句话

你完全可以做出你截图那种「组件样式实验室」：

- 用 **CSS 变量** 作为单一数据源；
- 用一个 **ThemePlayground 页面** 来：
  - 左边实时改变量；
  - 右边展示你的 `Button / Input / Card / DataTable` 等基础组件；
- 调整满意后，把变量值写回 `theme.css`，就完成了一轮可视化调参。

如果你告诉我你想把这个页面挂在哪个路由（比如系统设置里，还是单独 `/dev/theme`），我可以帮你把完整路由文件（含 `createFileRoute` 或现有路由结构）也写出来。