import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { cn } from '@/lib/utils'
import React, { useState, useEffect } from 'react'

type DataCardProps = {
    value?: React.ReactNode
    label?: string
    delta?: string
    color?: string
    icon?: React.ReactNode
    badgeText?: string
    className?: string
}

export function DataCard({
    value = '$13.4k',
    label = '总销售额',
    delta = '+38%',
    color = 'bg-chart-1/10 text-chart-1',
    icon,
    badgeText = '过去6个月',
    className,
}: DataCardProps) {
    const [animatedValue, setAnimatedValue] = useState(0)
    const [animatedDelta, setAnimatedDelta] = useState(0)

    // 解析字符串中的数字值
    const parseValue = (str: string) => {
        const match = str.match(/[\d.]+/)
        if (!match) return 0
        const num = parseFloat(match[0])
        if (str.includes('k')) return num * 1000
        if (str.includes('M')) return num * 1000000
        return num
    }

    // 格式化数字回字符串
    const formatValue = (num: number, original: string) => {
        if (original.includes('$')) {
            if (num >= 1000000) return `$${(num / 1000000).toFixed(1)}M`
            if (num >= 1000) return `$${(num / 1000).toFixed(1)}k`
            return `$${num.toFixed(0)}`
        }
        return num.toString()
    }

    // 格式化百分比
    const formatDelta = (num: number, original: string) => {
        const prefix = original.startsWith('+') ? '+' : original.startsWith('-') ? '-' : ''
        return `${prefix}${num.toFixed(0)}%`
    }

    useEffect(() => {
        const targetValue = parseValue(typeof value === 'string' ? value : '0')
        const targetDelta = parseValue(typeof delta === 'string' ? delta : '0')

        const duration = 1500 // 1.5秒动画
        const steps = 60 // 60帧
        const interval = duration / steps

        let currentStep = 0
        const timer = setInterval(() => {
            currentStep++
            const progress = currentStep / steps
            const easeProgress = 1 - Math.pow(1 - progress, 2) // ease-out quad: 一开始快，慢慢变慢

            setAnimatedValue(Math.round(targetValue * easeProgress))
            setAnimatedDelta(Math.round(targetDelta * easeProgress))

            if (currentStep >= steps) {
                clearInterval(timer)
                setAnimatedValue(targetValue)
                setAnimatedDelta(targetDelta)
            }
        }, interval)

        return () => clearInterval(timer)
    }, [value, delta])

    const defaultIcon = (
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-ticket-check" aria-hidden="true">
            <path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z"></path>
            <path d="m9 12 2 2 4-4"></path>
        </svg>
    )

    return (
        <Card className={cn('shadow-none col-span-1 lg:col-span-2 gap-4', className)}>
            <CardHeader className="px-6 flex items-center justify-between gap-4">
                <span className="relative flex shrink-0 overflow-hidden rounded-md">
                    <span className={cn('flex items-center justify-center rounded-md size-9.5 [&>svg]:size-4.75', color)}>
                        {icon ?? defaultIcon}
                    </span>
                </span>
                <p className="flex items-center gap-1">
                    {formatDelta(animatedDelta, typeof delta === 'string' ? delta : '+0%')}
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-chevron-up size-4" aria-hidden="true">
                        <path d="m18 15-6-6-6 6"></path>
                    </svg>
                </p>
            </CardHeader>
            <CardContent className="px-6 flex flex-1 flex-col justify-between gap-4">
                <p className="flex flex-col gap-1">
                    <span className="text-lg font-semibold">{formatValue(animatedValue, typeof value === 'string' ? value : '$0')}</span>
                    <span className="text-muted-foreground text-sm">{label}</span>
                </p>
            </CardContent>
            <CardFooter>
            <span className="inline-flex w-fit shrink-0 items-center justify-center gap-1 overflow-hidden rounded-full border px-2 py-0.5 text-xs font-medium whitespace-nowrap border-transparent bg-primary/10 text-primary">
                    {badgeText}
                </span>
            </CardFooter>
        </Card>
    )
}