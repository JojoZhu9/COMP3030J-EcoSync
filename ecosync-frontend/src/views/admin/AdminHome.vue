<template>
  <div class="page-container pc-staff-layout" v-loading="loading">
    <div v-if="prod" class="content-wrapper">
      <div class="brand-top-stripe"></div>

      <el-page-header @back="$router.back()" class="pc-header">
        <template #icon>
          <el-icon class="c-green"><ArrowLeft /></el-icon>
        </template>
        <template #content>
          <div class="header-logo-content">
            <div class="brand-logo-small">
              <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
            </div>
            <span class="divider">|</span>
            <span class="header-title">Strategy Configuration: <span class="highlight-name">{{ prod.productName }}</span></span>
          </div>
        </template>
        <template #extra>
          <el-button color="#007934" plain round icon="Refresh" @click="fetchDetail" class="refresh-btn">Sync Data</el-button>
        </template>
      </el-page-header>

      <el-row :gutter="24" class="mt-20">
        <el-col :span="8">
          <el-card class="pc-card info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon class="c-green"><Document /></el-icon>
                <span>Master Data</span>
              </div>
            </template>

            <div class="digital-label">
              <div class="label-header">
                <span class="sku-tag">SKU: {{ prod.product_id || 'N/A' }}</span>
                <el-tag size="small" type="success" effect="dark" round>IN STOCK</el-tag>
              </div>

              <h2 class="label-name">{{ prod.productName }}</h2>

              <div class="barcode-box">
                <div class="barcode-visual">|| ||| || |||| | ||</div>
                <div class="barcode-num">{{ prod.barcode }}</div>
              </div>

              <div class="label-price-row">
                <div class="price-label">LIST PRICE</div>
                <div class="price-value">
                  <span class="currency">¥</span>
                  <span class="amount">{{ parseFloat(prod.normalPrice || 0).toFixed(2) }}</span>
                </div>
              </div>

              <div class="label-footer">
                <p>Data Source: 7-ELEVEn ERP Central</p>
                <p>Last Sync: {{ new Date().toLocaleDateString() }}</p>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="16">
          <el-card class="pc-card discount-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon class="c-orange"><TrendCharts /></el-icon>
                <span>Dynamic Discount Strategy (1-12h Prior)</span>
              </div>
            </template>

            <div class="chart-section">
              <div ref="discountChartRef" class="discount-chart-container"></div>
            </div>

            <el-divider border-style="dashed">
              <span class="divider-text">ADJUST COEFFICIENTS</span>
            </el-divider>

            <div class="discount-grid">
              <div v-for="h in 12" :key="h"
                   class="discount-item"
                   :class="getRiskClass(h)">
                <div class="hour-badge">{{ h }}h</div>
                <el-input-number
                  v-model="editForm.rates[h-1]"
                  :min="0.1" :max="1" :step="0.05" :precision="2"
                  size="small" controls-position="right"
                  class="brand-input-number"
                  @change="updateChartData"
                />
              </div>
            </div>

            <div class="promo-preview-box">
              <div class="promo-header">
                <el-icon><View /></el-icon> Store-Front Price Preview
              </div>

              <div class="promo-body">
                <div class="promo-tag">
                  <div class="tag-top">OFFICIAL DISCOUNT</div>
                  <div class="tag-middle">
                    <div class="old-price">Was ¥{{ parseFloat(prod.normalPrice || 0).toFixed(2) }}</div>
                    <div class="new-price">
                      <span class="yen">¥</span>
                      <span class="val">{{ (parseFloat(prod.normalPrice || 0) * editForm.rates[previewHour-1]).toFixed(2) }}</span>
                    </div>
                  </div>
                  <div class="tag-bottom">Valid for {{ previewHour }}h before expiry</div>
                </div>

                <div class="promo-controls">
                  <p class="control-hint">Simulate time remaining:</p>
                  <el-slider v-model="previewHour" :min="1" :max="12" :step="1" show-stops />
                  <div class="rate-indicator">
                    Applied Multiplier: <span class="c-red">{{ editForm.rates[previewHour-1].toFixed(2) }}x</span>
                  </div>
                </div>
              </div>
            </div>

            <el-button type="success" @click="saveRule" class="save-strategy-btn" :loading="saving">
              <el-icon style="margin-right: 8px;"><Checked /></el-icon> Deploy Strategy to Store
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh, Document, TrendCharts, Checked, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const route = useRoute()
const prod = ref<any>(null)
const loading = ref(true)
const saving = ref(false)
const previewHour = ref(1)

const discountChartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const editForm = reactive({
  rates: Array(12).fill(1.0)
})

const getRiskClass = (h: number) => {
  if (h <= 3) return 'risk-critical'
  if (h <= 6) return 'risk-warning'
  return 'risk-stable'
}

const updateChartData = () => {
  if (!chartInstance) return
  const option = {
    grid: { top: '15%', left: '5%', right: '5%', bottom: '10%', containLabel: true },
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#007934', borderWidth: 1 },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 12 }, (_, i) => `${i + 1}h`),
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { color: '#6B7280', fontWeight: 'bold' }
    },
    yAxis: {
      type: 'value',
      max: 1.0,
      min: 0,
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      data: [...editForm.rates],
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: { color: '#007934' },
      lineStyle: { width: 4, color: '#007934' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 121, 52, 0.2)' },
          { offset: 1, color: 'transparent' }
        ])
      }
    }]
  }
  chartInstance.setOption(option)
}

const initAndRenderChart = () => {
  if (!discountChartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(discountChartRef.value)
  updateChartData()
}

const fetchDetail = async () => {
  loading.value = true
  const id = route.query.id
  if (!id) return (loading.value = false)
  try {
    const res: any = await request.get(`/products/${id}`)
    prod.value = res.data?.data || res.data || res
    if (prod.value.discountRates) {
      const parsed = typeof prod.value.discountRates === 'string' ? JSON.parse(prod.value.discountRates) : prod.value.discountRates
      if (Array.isArray(parsed)) editForm.rates = parsed
    }
    nextTick(() => initAndRenderChart())
  } catch (e) {
    ElMessage.error('Failed to load product details')
  } finally {
    loading.value = false
  }
}

const saveRule = async () => {
  const id = route.query.id
  if (!id || !prod.value) return
  saving.value = true
  try {
    await request.put(`/products/${id}`, {
      ...prod.value,
      discountRates: JSON.stringify(editForm.rates)
    })
    ElMessage.success('Strategy deployed successfully')
  } catch (e) {
    ElMessage.error('Failed to save strategy')
  } finally {
    saving.value = false
  }
}

const handleResize = () => chartInstance?.resize()
onMounted(() => {
  fetchDetail()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
:root {
  --711-green: #007934;
  --711-red: #e2231a;
  --711-orange: #ff7900;
}

.pc-staff-layout { background: #f9fafb; min-height: 100vh; padding: 24px; }
.content-wrapper { max-width: 1400px; margin: 0 auto; }

/* Brand Stripe */
.brand-top-stripe {
  height: 6px; width: 100%; position: fixed; top: 0; left: 0; z-index: 2000;
  background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%);
}

/* Header Styling */
.pc-header {
  background: white; padding: 20px 30px; border-radius: 12px; margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.header-logo-content { display: flex; align-items: center; gap: 15px; }
.brand-logo-small { font-size: 24px; font-weight: 900; font-style: italic; letter-spacing: -1px; }
.divider { color: #E5E7EB; font-weight: 300; }
.header-title { font-weight: 700; color: #374151; font-size: 16px; }
.highlight-name { color: var(--711-green); }
.c-green { color: #007934; }
.c-red { color: #e2231a; }
.c-orange { color: #ff7900; }

/* Card Universal */
.pc-card { border-radius: 16px; border: 1px solid #E5E7EB; margin-bottom: 24px; }
.card-header { display: flex; align-items: center; gap: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; font-size: 14px; }

/* Digital Label (Left Column) */
.digital-label {
  background: #fff; padding: 24px; border: 2px solid #111827; border-radius: 4px;
  box-shadow: 4px 4px 0px #111827;
}
.label-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.sku-tag { font-family: monospace; font-weight: bold; color: #6B7280; }
.label-name { font-size: 24px; font-weight: 900; margin: 0 0 20px 0; line-height: 1.2; color: #111827; }
.barcode-box { text-align: center; margin-bottom: 24px; padding: 10px; border: 1px dashed #D1D5DB; }
.barcode-visual { font-family: 'Courier New'; font-size: 20px; letter-spacing: 2px; }
.barcode-num { font-size: 12px; letter-spacing: 4px; margin-top: 5px; }
.label-price-row { border-top: 2px solid #111827; padding-top: 15px; display: flex; justify-content: space-between; align-items: flex-end; }
.price-label { font-size: 12px; font-weight: 900; color: #6B7280; }
.price-value { color: #e2231a; }
.currency { font-size: 18px; font-weight: 800; margin-right: 4px; }
.amount { font-size: 42px; font-weight: 900; line-height: 1; }
.label-footer { margin-top: 25px; font-size: 10px; color: #9CA3AF; text-transform: uppercase; }

/* Discount Section (Right Column) */
.chart-section { height: 300px; margin-bottom: 20px; }
.discount-chart-container { width: 100%; height: 100%; }
.divider-text { font-size: 12px; font-weight: 800; color: #9CA3AF; letter-spacing: 2px; }

.discount-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
  padding: 20px; background: #F9FAFB; border-radius: 12px;
}
.discount-item {
  background: white; padding: 12px; border-radius: 10px; border: 1px solid #E5E7EB;
  display: flex; align-items: center; justify-content: space-between;
}
.hour-badge {
  width: 32px; height: 32px; border-radius: 8px; background: #F3F4F6;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 12px; color: #374151;
}

/* Risk Levels */
.risk-critical { border-left: 4px solid #e2231a; }
.risk-critical .hour-badge { background: #fee2e2; color: #e2231a; }
.risk-warning { border-left: 4px solid #ff7900; }
.risk-warning .hour-badge { background: #ffedd5; color: #ff7900; }
.risk-stable { border-left: 4px solid #007934; }
.risk-stable .hour-badge { background: #dcfce7; color: #007934; }

/* Promo Preview (Yellow Tag) */
.promo-preview-box {
  margin-top: 30px; border: 1px solid #FDE68A; border-radius: 12px; overflow: hidden;
}
.promo-header { background: #FEF3C7; padding: 12px 20px; font-weight: 800; color: #92400E; font-size: 13px; display: flex; align-items: center; gap: 8px; }
.promo-body { padding: 25px; display: flex; gap: 30px; align-items: center; background: #fffbeb; }

.promo-tag {
  width: 200px; background: #FFD700; border: 2px solid #e2231a; padding: 10px;
  text-align: center; transform: rotate(-2deg); box-shadow: 4px 4px 0px rgba(226,35,26,0.2);
}
.tag-top { background: #e2231a; color: white; font-weight: 900; font-size: 11px; padding: 2px 0; margin-bottom: 10px; }
.old-price { text-decoration: line-through; font-size: 12px; color: #666; }
.new-price { color: #e2231a; font-weight: 900; margin: 5px 0; }
.new-price .yen { font-size: 16px; }
.new-price .val { font-size: 32px; }
.tag-bottom { font-size: 10px; font-weight: 700; border-top: 1px solid rgba(0,0,0,0.1); padding-top: 5px; }

.promo-controls { flex: 1; }
.control-hint { font-size: 13px; font-weight: 700; color: #92400E; margin-bottom: 10px; }
.rate-indicator { margin-top: 15px; font-size: 14px; font-weight: 800; color: #4B5563; }

/* Action Button */
.save-strategy-btn {
  width: 100%; height: 56px; border-radius: 12px; font-size: 18px; font-weight: 900;
  background: #007934 !important; border: none; margin-top: 30px;
  box-shadow: 0 10px 20px rgba(0, 121, 52, 0.2);
  transition: all 0.3s;
}
.save-strategy-btn:hover { transform: translateY(-2px); box-shadow: 0 15px 30px rgba(0, 121, 52, 0.3); }

:deep(.brand-input-number) .el-input__inner { font-weight: 800; color: #111827; }
</style>
