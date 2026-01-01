import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { cn } from '@/lib/utils'

export function IntroductionCard() {
    return (
        <Card
         className={cn(
            'shadow-none col-span-2 lg:col-span-4'
         )}   
        >
            <CardHeader>
                <CardTitle className='text-xl'>
                    欢迎回来 Miku
                </CardTitle>
            </CardHeader>
            <CardContent>

            </CardContent>
            <CardFooter>
                <div className='flex flex-col gap-2 text-xs text-muted-foreground'>
                    <p>版本号: 1.0.0</p>
                    <p>技术栈: React + Vite</p>
                </div>

                
            </CardFooter>
        </Card>
    )
}