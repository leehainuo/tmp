import { Card, CardContent, CardFooter } from '@/components/ui/card'
import {
    type ChartConfig,
    ChartContainer,
    ChartLegend,
    ChartLegendContent,
    ChartTooltip,
    ChartTooltipContent,
  } from '@/components/ui/chart'
import { Bar, BarChart, CartesianGrid, XAxis } from "recharts"

const chartData = [
    { month: "一月", desktop: 186, mobile: 80 },
    { month: "二月", desktop: 305, mobile: 200 },
    { month: "三月", desktop: 237, mobile: 120 },
    { month: "四月", desktop: 73, mobile: 190 },
    { month: "五月", desktop: 209, mobile: 130 },
    { month: "六月", desktop: 214, mobile: 140 },
]

const chartConfig = {
  desktop: {
    label: "桌面端",
    color: "#161616",
  },
  mobile: {
    label: "移动端",
    color: "#737373",
  },
} satisfies ChartConfig

export function DeviceUsageCard() {
    return (
      <Card className='shadow-none lg:col-span-4'>
        <CardContent>
          <ChartContainer config={chartConfig} className="min-h-[200px] max-h-59.5 w-full">
            <BarChart accessibilityLayer data={chartData}>
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="month"
                tickLine={false}
                tickMargin={10}
                axisLine={false}
                tickFormatter={(value) => value.slice(0, 3)}
              />
              <ChartTooltip content={<ChartTooltipContent />} />
              
              <Bar dataKey="desktop" fill="var(--color-desktop)" radius={4} />
              <Bar dataKey="mobile" fill="var(--color-mobile)" radius={4} />
            </BarChart>
          </ChartContainer>
        </CardContent>
      </Card>

      )
}