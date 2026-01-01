/**
 * Bug: tooltip 一直是左上角
 * 
 * Close: [
 *    前往 chart.tsx 查看     
 * ]
 * 
 * Link: https://github.com/shadcn-ui/ui/issues/8005
 */
import { Label, Pie, PieChart } from 'recharts'
import {
  type ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart'
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

const revenueChartData = [
  { month: '一月', sales: 340, fill: 'var(--color-january)' },
  { month: '二月', sales: 200, fill: 'var(--color-february)' },
  { month: '三月', sales: 200, fill: 'var(--color-march)' },
]

const revenueChartConfig = {
  sales: {
    label: 'Sales',
  },

  january: {
    label: 'January',
    color: 'var(--primary)',
  },

  february: {
    label: 'February',
    color: 'color-mix(in oklab, var(--primary) 60%, transparent)',
  },

  march: {
    label: 'March',
    color: 'color-mix(in oklab, var(--primary) 20%, transparent)',
  },
} satisfies ChartConfig

const SalesMetricsCard = () => {
  return (
          <Card className='gap-4 py-4 shadow-none lg:col-span-2'>
            <CardHeader className='gap-1'>
              <CardTitle className='text-lg font-semibold'>
                营收目标
              </CardTitle>
            </CardHeader>

            <CardContent className='px-0'>
  
              <ChartContainer
                config={revenueChartConfig}
                className='h-38.5 w-full'
              >
                <PieChart margin={{ top: 0, bottom: 0, left: 0, right: 0 }}>
                  <ChartTooltip
                    cursor={false}
                    content={<ChartTooltipContent hideLabel />}
                  />
                  <Pie
                    data={revenueChartData}
                    dataKey='sales'
                    nameKey='month'
                    startAngle={300}
                    endAngle={660}
                    innerRadius={58}
                    outerRadius={75}
                    paddingAngle={2}
                  >
                    <Label
                      content={({ viewBox }) => {
                        if (viewBox && 'cx' in viewBox && 'cy' in viewBox) {
                          return (
                            <text
                              x={viewBox.cx}
                              y={viewBox.cy}
                              textAnchor='middle'
                              dominantBaseline='middle'
                            >
                              <tspan
                                x={viewBox.cx}
                                y={(viewBox.cy || 0) - 12}
                                className='fill-card-foreground text-lg font-medium'
                              >
                                256.24
                              </tspan>
                              <tspan
                                x={viewBox.cx}
                                y={(viewBox.cy || 0) + 19}
                                className='fill-muted-foreground text-sm'
                              >
                                总利润
                              </tspan>
                            </text>
                          )
                        }
                      }}
                    />
                  </Pie>
                </PieChart>
              </ChartContainer>
            </CardContent>
            <CardFooter className='justify-end'>
              <span className='text-2xl font-medium'>56%</span>
            </CardFooter>
          </Card>

  )
}

export default SalesMetricsCard
