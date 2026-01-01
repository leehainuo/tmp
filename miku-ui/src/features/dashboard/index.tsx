import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Search } from '@/components/search'
import { IntroductionCard } from './components/introduction-card'
import { DataCard } from './components/data-card'
import { DollarSign, ShoppingCart, TicketCheck } from "lucide-react";
import SalesMetricsCard from './components/chart1-card'


export function Dashboard() {
  const dashboardData = [
    {
        value: '$13.4k',
        label: '总销售额',
        delta: '+38%',
        color: 'bg-chart-1/10 text-chart-1',
        icon: <TicketCheck />
    },
    {
        value: '$155k',
        label: '订单总数',
        delta: '+22%',
        color: 'bg-chart-2/10 text-chart-2',
        icon: <ShoppingCart />
    },
    {
        value: '$89.34k',
        label: '总利润',
        delta: '-16%',
        color: 'bg-chart-3/10 text-chart-3',
        icon: <DollarSign />
    },
    {
        value: '1200',
        label: '书签',
        delta: '+22%',
        color: 'bg-chart-4/10 text-chart-4',
        icon: <ShoppingCart />
    }
  ]
  return (
    <>
      {/* ===== Top Heading ===== */}
      <Header fixed>
        <Search />
      </Header>

      {/* ===== Main ===== */}
      <Main>
        {/*<div className='mb-2 flex items-center justify-between space-y-2'>*/}
        {/*  <h1 className='text-2xl font-bold tracking-tight'>仪表盘</h1>*/}
        {/*  <div className='flex items-center space-x-2'>*/}
        {/*    <Button>下载</Button>*/}
        {/*  </div>*/}
        {/*</div>*/}
        {/* <DashboardMain /> */}
        <div className='grid grid-cols-2 lg:grid-cols-6 gap-6'>
            <IntroductionCard />
            {dashboardData.map((item, index) => (
              <DataCard
                key={index}
                value={item.value}
                label={item.label}
                delta={item.delta}
                color={item.color}
                icon={item.icon}
              />
            ))}
            <SalesMetricsCard />
        </div>
      </Main>
    </>
  )
}

// topNav removed (not used) — dashboard main view is provided by DashboardMain
        