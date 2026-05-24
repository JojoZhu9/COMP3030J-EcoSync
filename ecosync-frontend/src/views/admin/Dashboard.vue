<template>
  <div class="dashboard-container">
    <!-- KPI Cards -->
    <el-row :gutter="20" class="kpi-row">
      <el-col :xs="12" :sm="6" :lg="6" v-for="(item, index) in kpiList" :key="index">
        <el-card shadow="never" class="kpi-card" :class="'kpi-' + index">
          <div class="kpi-main">
            <div class="kpi-icon-box">
              <el-icon :size="22"><component :is="item.icon" /></el-icon>
            </div>
            <div class="kpi-info">
              <div class="kpi-label">{{ $t(item.labelKey) }}</div>
              <div class="kpi-value">{{ item.value }}</div>
              <div class="kpi-sub">{{ item.sub || ' ' }}</div>
            </div>
          </div>
          <div class="kpi-footer-line"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 2: Sales Trend + Order Status -->
    <el-row :gutter="20" class="chart-row" align="stretch">
      <el-col :xs="24" :lg="16" style="display: flex; margin-bottom: 12px;">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">{{ $t('admin.dashboard.salesTrend') }}</span>
              <el-radio-group v-model="salesPeriod" size="small" @change="fetchSalesTrend">
                <el-radio-button label="day">{{ $t('admin.dashboard.daily') }}</el-radio-button>
                <el-radio-button label="month">{{ $t('admin.dashboard.monthly') }}</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="salesChartRef" class="chart-box" v-show="hasSalesData"></div>
          <div class="chart-empty" v-show="!hasSalesData"><el-empty :description="$t('admin.dashboard.noSalesData')" /></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8" style="display: flex; margin-bottom: 12px;">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">{{ $t('admin.dashboard.orderStatusDistribution') }}</span>
            </div>
          </template>
          <div ref="statusChartRef" class="chart-box" v-show="hasStatusData"></div>
          <div class="chart-empty" v-show="!hasStatusData"><el-empty :description="$t('admin.dashboard.noOrderData')" /></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 3: Top Products + Store Sales -->
    <el-row :gutter="20" class="chart-row" align="stretch">
      <el-col :xs="24" :lg="12" style="display: flex; margin-bottom: 12px;">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">{{ $t('admin.dashboard.top10BestSelling') }}</span>
            </div>
          </template>
          <div ref="topProductChartRef" class="chart-box" v-show="hasTopProductData"></div>
          <div class="chart-empty" v-show="!hasTopProductData"><el-empty :description="$t('admin.dashboard.noProductSalesData')" /></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" style="display: flex; margin-bottom: 12px;">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">{{ $t('admin.dashboard.storeSalesComparison') }}</span>
            </div>
          </template>
          <div ref="storeChartRef" class="chart-box" v-show="hasStoreData"></div>
          <div class="chart-empty" v-show="!hasStoreData"><el-empty :description="$t('admin.dashboard.noStoreSalesData')" /></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Row 4: User Growth + Discount Distribution -->
    <el-row :gutter="20" class="chart-row" align="stretch">
      <el-col :xs="24" :lg="12" style="display: flex; margin-bottom: 12px;">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">{{ $t('admin.dashboard.userGrowthTrend') }}</span>
              <el-radio-group v-model="userPeriod" size="small" @change="fetchUserGrowth">
                <el-radio-button label="day">{{ $t('admin.dashboard.daily') }}</el-radio-button>
                <el-radio-button label="month">{{ $t('admin.dashboard.monthly') }}</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="userChartRef" class="chart-box" v-show="hasUserData"></div>
          <div class="chart-empty" v-show="!hasUserData"><el-empty :description="$t('admin.dashboard.noUserGrowthData')" /></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" style="display: flex; margin-bottom: 12px;">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">{{ $t('admin.dashboard.discountRateDistribution') }}</span>
            </div>
          </template>
          <div ref="discountChartRef" class="chart-box" v-show="hasDiscountData"></div>
          <div class="chart-empty" v-show="!hasDiscountData"><el-empty :description="$t('admin.dashboard.noDiscountData')" /></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { getLocale } from '@/locales'
import request from '@/utils/request'
import * as echarts from 'echarts'
import {
  Money, Present, Food, ShoppingBag, User, Box, OfficeBuilding, Timer
} from '@element-plus/icons-vue'

const { t } = useI18n()

const kpiList = ref([
  { labelKey: 'admin.dashboard.totalSales', value: '¥0', sub: '', icon: Money },
  { labelKey: 'admin.dashboard.discountSavings', value: '¥0', sub: '', icon: Present },
  { labelKey: 'admin.dashboard.wasteSaved', value: '0 kg', sub: '¥0', icon: Food },
  { labelKey: 'admin.dashboard.totalOrders', value: '0', sub: '', icon: ShoppingBag },
  { labelKey: 'admin.dashboard.totalUsers', value: '0', sub: '', icon: User },
  { labelKey: 'admin.dashboard.products', value: '0', sub: '', icon: Box },
  { labelKey: 'admin.dashboard.stores', value: '0', sub: '', icon: OfficeBuilding },
  { labelKey: 'admin.dashboard.activeItems', value: '0', sub: '', icon: Timer }
])

const salesPeriod = ref('day')
const userPeriod = ref('day')

const salesChartRef = ref<HTMLElement | null>(null)
const statusChartRef = ref<HTMLElement | null>(null)
const topProductChartRef = ref<HTMLElement | null>(null)
const storeChartRef = ref<HTMLElement | null>(null)
const userChartRef = ref<HTMLElement | null>(null)
const discountChartRef = ref<HTMLElement | null>(null)

let salesChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null
let topProductChart: echarts.ECharts | null = null
let storeChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null
let discountChart: echarts.ECharts | null = null

const brandColors = ['#008163', '#EE7203', '#e2231a', '#3b82f6', '#8b5cf6', '#14b8a6', '#f59e0b', '#ef4444']

const hasSalesData = ref(false)
const hasStatusData = ref(false)
const hasTopProductData = ref(false)
const hasStoreData = ref(false)
const hasUserData = ref(false)
const hasDiscountData = ref(false)

const fetchSummary = async () => {
  try {
    const res: any = await request.get('/analytics/summary')
    const d = res.data || res
    const kpi = kpiList.value
    kpi[0]!.value = '¥' + Number(d.totalSales || 0).toFixed(2)
    kpi[1]!.value = '¥' + Number(d.totalSavings || 0).toFixed(2)
    kpi[2]!.value = (d.wasteReductionKg || 0) + ' kg'
    kpi[2]!.sub = '¥' + Number(d.wasteReductionValue || 0).toFixed(2)
    kpi[3]!.value = String(d.totalOrders || 0)
    kpi[4]!.value = String(d.totalUsers || 0)
    kpi[5]!.value = String(d.totalProducts || 0)
    kpi[6]!.value = String(d.totalStores || 0)
  } catch (e) {}
}

const fetchActiveItems = async () => {
  try {
    const res: any = await request.get('/expiring-products')
    const list = res.data || res || []
    const active = list.filter((x: any) => x.status === 'AVAILABLE').length
    kpiList.value[7]!.value = String(active)
  } catch (e) {}
}

const fetchSalesTrend = async () => {
  try {
    const res: any = await request.get(`/analytics/sales-trend?period=${salesPeriod.value}`)
    const list: any[] = res.data || res || []
    const xData = list.map(i => i.period)
    const yData = list.map(i => Number(i.amount || 0))
    const countData = list.map(i => Number(i.orderCount || 0))

    hasSalesData.value = list.length > 0
    await nextTick()
    if (!salesChart && salesChartRef.value) salesChart = echarts.init(salesChartRef.value)
    if (!salesChart) return
    salesChart.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(15,23,42,0.95)',
        borderWidth: 0,
        textStyle: { color: '#fff' },
        axisPointer: { type: 'cross', crossStyle: { color: '#999' } }
      },
      legend: { data: [t('admin.dashboard.salesAmount'), t('admin.dashboard.orderCount')], bottom: 0, textStyle: { color: '#64748b' } },
      grid: { top: '10%', left: '3%', right: '4%', bottom: '15%', containLabel: true },
      xAxis: {
        type: 'category',
        data: xData,
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#64748b', rotate: salesPeriod.value === 'day' ? 0 : 0 }
      },
      yAxis: [
        { type: 'value', name: t('admin.dashboard.amountYuan'), axisLabel: { color: '#64748b' }, splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } } },
        { type: 'value', name: t('admin.dashboard.orderCount'), axisLabel: { color: '#64748b' }, splitLine: { show: false } }
      ],
      series: [
        {
          name: t('admin.dashboard.salesAmount'),
          type: 'line',
          smooth: true,
          data: yData,
          itemStyle: { color: '#008163' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(0,129,99,0.35)' },
              { offset: 1, color: 'rgba(0,129,99,0.02)' }
            ])
          },
          lineStyle: { width: 3, shadowColor: 'rgba(0,129,99,0.3)', shadowBlur: 10 }
        },
        {
          name: t('admin.dashboard.orderCount'),
          type: 'bar',
          yAxisIndex: 1,
          data: countData,
          itemStyle: { color: '#EE7203', borderRadius: [4, 4, 0, 0] },
          barWidth: '30%'
        }
      ]
    }, true)
    salesChart.resize()
  } catch (e) {}
}

const fetchOrderStatus = async () => {
  try {
    const res: any = await request.get('/analytics/order-status')
    const list: any[] = res.data || res || []
    const data = list.map(i => ({ value: i.count, name: i.status }))
    hasStatusData.value = data.length > 0

    const statusColors: Record<string, string> = {
      PENDING: '#f59e0b',
      PAID: '#3b82f6',
      AWAITING_PICKUP: '#8b5cf6',
      COMPLETED: '#008163',
      CANCELLED: '#e2231a'
    }

    await nextTick()
    if (!statusChart && statusChartRef.value) statusChart = echarts.init(statusChartRef.value)
    if (!statusChart) return
    statusChart.setOption({
      tooltip: { trigger: 'item', backgroundColor: 'rgba(15,23,42,0.95)', borderWidth: 0, textStyle: { color: '#fff' } },
      legend: { top: 'top', left: 'center', itemWidth: 10, itemHeight: 10, textStyle: { color: '#64748b', fontSize: 11 } },
      series: [{
        type: 'pie',
        radius: ['35%', '62%'],
        center: ['50%', '58%'],
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{d}%', color: '#334155', fontWeight: 'bold' },
        data: data.map(d => ({ ...d, itemStyle: { color: statusColors[d.name] || '#94a3b8' } }))
      }]
    }, true)
    statusChart.resize()
  } catch (e) {}
}

const fetchTopProducts = async () => {
  try {
    const res: any = await request.get('/analytics/top-products')
    const list: any[] = res.data || res || []
    const yData = list.map(i => getLocale() === 'en' && i.productNameEn ? i.productNameEn : i.productName).reverse()
    const xData = list.map(i => Number(i.totalSold || 0)).reverse()
    hasTopProductData.value = list.length > 0

    await nextTick()
    if (!topProductChart && topProductChartRef.value) topProductChart = echarts.init(topProductChartRef.value)
    if (!topProductChart) return
    topProductChart.setOption({
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.95)', borderWidth: 0, textStyle: { color: '#fff' }, axisPointer: { type: 'shadow' } },
      grid: { top: '5%', left: '3%', right: '8%', bottom: '5%', containLabel: true },
      xAxis: { type: 'value', axisLabel: { color: '#64748b' }, splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } } },
      yAxis: { type: 'category', data: yData, axisLabel: { color: '#334155', fontWeight: 'bold', fontSize: 11 }, axisLine: { show: false }, axisTick: { show: false } },
      series: [{
        type: 'bar',
        data: xData,
        itemStyle: { color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [{ offset: 0, color: '#008163' }, { offset: 1, color: '#14b8a6' }]), borderRadius: [0, 6, 6, 0] },
        barWidth: 16,
        label: { show: true, position: 'right', color: '#334155', fontWeight: 'bold' }
      }]
    }, true)
    topProductChart.resize()
  } catch (e) {}
}

const fetchStoreSales = async () => {
  try {
    const res: any = await request.get('/analytics/store-sales')
    const list: any[] = res.data || res || []
    const xData = list.map(i => getLocale() === 'en' && i.storeNameEn ? i.storeNameEn : i.storeName)
    const yData = list.map(i => Number(i.amount || 0))
    hasStoreData.value = list.length > 0

    await nextTick()
    if (!storeChart && storeChartRef.value) storeChart = echarts.init(storeChartRef.value)
    if (!storeChart) return
    storeChart.setOption({
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.95)', borderWidth: 0, textStyle: { color: '#fff' } },
      grid: { top: '10%', left: '3%', right: '4%', bottom: '5%', containLabel: true },
      xAxis: { type: 'category', data: xData, axisLabel: { color: '#64748b', rotate: 20 }, axisLine: { lineStyle: { color: '#e2e8f0' } } },
      yAxis: { type: 'value', axisLabel: { color: '#64748b', formatter: '¥{value}' }, splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } } },
      series: [{
        type: 'bar',
        data: yData,
        itemStyle: { color: (params: any) => brandColors[params.dataIndex % brandColors.length], borderRadius: [6, 6, 0, 0] },
        barWidth: '40%',
        label: { show: true, position: 'top', color: '#334155', fontWeight: 'bold', formatter: '¥{c}' }
      }]
    }, true)
    storeChart.resize()
  } catch (e) {}
}

const fetchUserGrowth = async () => {
  try {
    const res: any = await request.get(`/analytics/user-growth?period=${userPeriod.value}`)
    const list: any[] = res.data || res || []
    const xData = list.map(i => i.period)
    const yData = list.map(i => Number(i.count || 0))
    hasUserData.value = list.length > 0

    await nextTick()
    if (!userChart && userChartRef.value) userChart = echarts.init(userChartRef.value)
    if (!userChart) return
    userChart.setOption({
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.95)', borderWidth: 0, textStyle: { color: '#fff' } },
      grid: { top: '10%', left: '3%', right: '4%', bottom: '5%', containLabel: true },
      xAxis: { type: 'category', data: xData, axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#64748b' } },
      yAxis: { type: 'value', axisLabel: { color: '#64748b' }, splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } } },
      series: [{
        type: 'line',
        data: yData,
        smooth: true,
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59,130,246,0.35)' },
            { offset: 1, color: 'rgba(59,130,246,0.02)' }
          ])
        },
        lineStyle: { width: 3 },
        symbolSize: 6
      }]
    }, true)
    userChart.resize()
  } catch (e) {}
}

const fetchDiscountDistribution = async () => {
  try {
    const res: any = await request.get('/analytics/discount-distribution')
    const list: any[] = res.data || res || []
    const xData = list.map(i => i.range)
    const yData = list.map(i => Number(i.count || 0))
    hasDiscountData.value = list.length > 0

    await nextTick()
    if (!discountChart && discountChartRef.value) discountChart = echarts.init(discountChartRef.value)
    if (!discountChart) return
    discountChart.setOption({
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.95)', borderWidth: 0, textStyle: { color: '#fff' } },
      grid: { top: '10%', left: '3%', right: '4%', bottom: '5%', containLabel: true },
      xAxis: { type: 'category', data: xData, axisLabel: { color: '#64748b', rotate: 25 }, axisLine: { lineStyle: { color: '#e2e8f0' } } },
      yAxis: { type: 'value', axisLabel: { color: '#64748b' }, splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } } },
      series: [{
        type: 'bar',
        data: yData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e2231a' },
            { offset: 0.5, color: '#EE7203' },
            { offset: 1, color: '#008163' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        barWidth: '50%',
        label: { show: true, position: 'top', color: '#334155', fontWeight: 'bold' }
      }]
    }, true)
    discountChart.resize()
  } catch (e) {}
}

onMounted(async () => {
  await fetchSummary()
  await fetchActiveItems()

  fetchSalesTrend()
  fetchOrderStatus()
  fetchTopProducts()
  fetchStoreSales()
  fetchUserGrowth()
  fetchDiscountDistribution()

  window.addEventListener('resize', () => {
    salesChart?.resize()
    statusChart?.resize()
    topProductChart?.resize()
    storeChart?.resize()
    userChart?.resize()
    discountChart?.resize()
  })
})

onUnmounted(() => {
  salesChart?.dispose()
  statusChart?.dispose()
  topProductChart?.dispose()
  storeChart?.dispose()
  userChart?.dispose()
  discountChart?.dispose()
})
</script>

<style scoped>
.dashboard-container { padding: 8px; }
.chart-row { margin-top: 20px; }

/* KPI Cards */
.kpi-row { margin-bottom: 8px; }
.kpi-card { border-radius: 16px; border: 1px solid rgba(0,0,0,0.04); position: relative; overflow: hidden; transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); box-shadow: 0 4px 15px rgba(0,0,0,0.02) !important; margin-bottom: 12px; }
.kpi-card:hover { transform: translateY(-4px); box-shadow: 0 10px 25px rgba(0,0,0,0.06) !important; }
.kpi-main { display: flex; align-items: center; gap: 14px; padding: 18px 16px; }
.kpi-info { flex: 1; min-width: 0; }
.kpi-label { font-size: 10px; font-weight: 800; color: #64748b; letter-spacing: 0.8px; text-transform: uppercase; margin-bottom: 4px; }
.kpi-value { font-size: 22px; font-weight: 900; color: #1e293b; line-height: 1.2; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.kpi-sub { font-size: 11px; color: #94a3b8; font-weight: 700; margin-top: 2px; height: 16px; }
.kpi-icon-box { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0; }
.kpi-footer-line { height: 4px; width: 100%; position: absolute; bottom: 0; left: 0; }

.kpi-0 .kpi-icon-box { background: #f0fdf4; color: #008163; }
.kpi-0 .kpi-footer-line { background: linear-gradient(90deg, #008163, #005a46); }
.kpi-1 .kpi-icon-box { background: #fff7ed; color: #EE7203; }
.kpi-1 .kpi-footer-line { background: linear-gradient(90deg, #EE7203, #c2410c); }
.kpi-2 .kpi-icon-box { background: #fef2f2; color: #e2231a; }
.kpi-2 .kpi-footer-line { background: linear-gradient(90deg, #e2231a, #b91c1c); }
.kpi-3 .kpi-icon-box { background: #eff6ff; color: #3b82f6; }
.kpi-3 .kpi-footer-line { background: linear-gradient(90deg, #3b82f6, #1d4ed8); }
.kpi-4 .kpi-icon-box { background: #f5f3ff; color: #8b5cf6; }
.kpi-4 .kpi-footer-line { background: linear-gradient(90deg, #8b5cf6, #7c3aed); }
.kpi-5 .kpi-icon-box { background: #ecfdf5; color: #14b8a6; }
.kpi-5 .kpi-footer-line { background: linear-gradient(90deg, #14b8a6, #0d9488); }
.kpi-6 .kpi-icon-box { background: #fffbeb; color: #f59e0b; }
.kpi-6 .kpi-footer-line { background: linear-gradient(90deg, #f59e0b, #d97706); }
.kpi-7 .kpi-icon-box { background: #fdf2f8; color: #ec4899; }
.kpi-7 .kpi-footer-line { background: linear-gradient(90deg, #ec4899, #db2777); }

/* Chart Cards */
.chart-card { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 4px 20px rgba(0,0,0,0.03) !important; width: 100%; display: flex; flex-direction: column; min-height: 450px; }
:deep(.chart-card .el-card__header) { padding: 16px 20px; border-bottom: 1px solid #f1f5f9; background: #fff; flex-shrink: 0; }
:deep(.chart-card .el-card__body) { flex: 1; min-height: 0; padding: 12px; position: relative; display: flex; flex-direction: column; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.header-title { font-weight: 800; color: #1e293b; font-size: 15px; }
.chart-box { width: 100%; flex: 1; min-height: 280px; }
.chart-empty { width: 100%; flex: 1; min-height: 280px; display: flex; align-items: center; justify-content: center; }
</style>
