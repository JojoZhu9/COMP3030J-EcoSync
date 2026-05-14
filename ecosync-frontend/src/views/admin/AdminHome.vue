<template>
  <div class="page-container pc-staff-layout" v-loading="loading">
    <div v-if="prod" class="content-wrapper">
      <div class="brand-top-stripe"></div>

      <el-page-header @back="$router.push('/admin/inventory')" class="pc-header">
        <template #content>
          <div class="header-logo-content">
            <div class="brand-logo-small">
              <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
            </div>
            <span class="divider">|</span>
            <span class="header-title">Strategy: <span class="highlight-name">{{ prod.productName }}</span></span>
          </div>
        </template>
      </el-page-header>

      <el-row :gutter="24" class="mt-20">
        <el-col :span="8">
          <el-card class="pc-card info-card" shadow="never">
            <div class="digital-label">
              <div class="label-header">
                <span class="sku-tag">SKU: {{ prod.barcode }}</span>
                <el-tag size="small" type="success" effect="dark" round>ACTIVE</el-tag>
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
            </div>
          </el-card>
        </el-col>

        <el-col :span="16">
          <el-card class="pc-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon class="c-orange"><TrendCharts /></el-icon>
                <span>Price Fitting Curve (12h Decay)</span>
              </div>
            </template>

            <div class="chart-section">
              <div ref="discountChartRef" style="width: 100%; height: 100%"></div>
            </div>

            <el-divider border-style="dashed"><span class="divider-text">ADJUST COEFFICIENTS</span></el-divider>

            <div class="discount-grid">
              <div v-for="h in 12" :key="h" class="discount-item" :class="getRiskClass(h)">
                <div class="hour-badge">{{ h }}h</div>
                <el-input-number
                  v-model="editForm.rates[h-1]"
                  :min="0.1" :max="1" :step="0.05" :precision="2"
                  size="small" controls-position="right"
                  @change="updateChartData"
                />
              </div>
            </div>

            <div class="promo-preview-box">
              <div class="promo-header"><el-icon><View /></el-icon> Store-Front Preview</div>
              <div class="promo-body">
                <div class="promo-tag">
                  <div class="tag-top">SPECIAL OFFER</div>
                  <div class="tag-middle">
                    <div class="old-price">Was ¥{{ parseFloat(prod.normalPrice).toFixed(2) }}</div>
                    <div class="new-price">
                      <span class="yen">¥</span>
                      <span class="val">{{ (parseFloat(prod.normalPrice) * editForm.rates[previewHour-1]).toFixed(2) }}</span>
                    </div>
                  </div>
                  <div class="tag-bottom">{{ previewHour }}H BEFORE EXPIRY</div>
                </div>
                <div class="promo-controls">
                  <p class="control-hint">Simulate time to expiry:</p>
                  <el-slider v-model="previewHour" :min="1" :max="12" show-stops />
                  <div class="rate-indicator">Multiplier: <span class="c-red">{{ editForm.rates[previewHour-1].toFixed(2) }}x</span></div>
                </div>
              </div>
            </div>

            <el-button type="success" @click="saveRule" class="save-strategy-btn" :loading="saving">
              DEPLOY STRATEGY TO STORE
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { ArrowLeft, TrendCharts, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()
const prod = ref<any>(null)
const loading = ref(true)
const saving = ref(false)
const previewHour = ref(1)
const discountChartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

const editForm = reactive({ rates: Array(12).fill(1.0) })

const getRiskClass = (h: number) => h <= 3 ? 'risk-critical' : (h <= 6 ? 'risk-warning' : 'risk-stable')

const updateChartData = () => {
  if (!chartInstance) return
  chartInstance.setOption({
    grid: { top: '10%', left: '3%', right: '3%', bottom: '10%', containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Array.from({length:12}, (_, i) => `${i+1}h`) },
    yAxis: { type: 'value', max: 1.0, min: 0 },
    series: [{
      data: [...editForm.rates],
      type: 'line',
      smooth: true, // 关键：平滑拟合曲线
      symbolSize: 8,
      itemStyle: { color: '#007934' },
      lineStyle: { width: 4 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: 'rgba(0,121,52,0.3)'}, {offset: 1, color: 'transparent'}]) }
    }]
  })
}

const fetchDetail = async () => {
  const barcode = route.query.id
  if (!barcode) return router.push('/admin/inventory')
  loading.value = true
  try {
    const res: any = await request.get(`/products/${barcode}`)
    const data = res.data?.data || res.data || res
    prod.value = {
      barcode: data.barcode,
      productName: data.product_name || data.productName,
      normalPrice: data.normal_price || data.normalPrice
    }
    const rawRates = data.discount_rates || data.discountRates
    if (rawRates) {
      const parsed = typeof rawRates === 'string' ? JSON.parse(rawRates) : rawRates
      if (Array.isArray(parsed)) editForm.rates = parsed
    }
    nextTick(() => {
      if (discountChartRef.value) {
        chartInstance = echarts.init(discountChartRef.value)
        updateChartData()
      }
    })
  } catch (e) { ElMessage.error('Load failed') }
  finally { loading.value = false }
}

const saveRule = async () => {
  saving.value = true
  try {
    await request.put(`/products/${prod.value.barcode}`, {
      ...prod.value,
      discount_rates: JSON.stringify(editForm.rates)
    })
    ElMessage.success('Strategy Deployed!')
  } catch (e) { ElMessage.error('Save failed') }
  finally { saving.value = false }
}

onMounted(fetchDetail)
window.addEventListener('resize', () => chartInstance?.resize())
onUnmounted(() => chartInstance?.dispose())
</script>

<style scoped>
/* 保持 AdminHome 最初版的 CSS 样式，包含 .digital-label, .promo-tag, .discount-grid 等 */
.pc-staff-layout { background: #f4f7f6; min-height: 100vh; padding: 20px; }
.brand-top-stripe { height: 6px; width: 100%; position: fixed; top: 0; left: 0; z-index: 10; background: linear-gradient(90deg, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%); }
.pc-header { background: #fff; padding: 15px 25px; border-radius: 8px; margin-bottom: 20px; }
.digital-label { background: #fff; padding: 20px; border: 2px solid #1a202c; border-radius: 4px; box-shadow: 4px 4px 0 #1a202c; }
.label-name { font-size: 22px; font-weight: 900; color: #1a202c; margin: 15px 0; }
.amount { font-size: 36px; font-weight: 900; color: #e2231a; }
.chart-section { height: 260px; margin-bottom: 20px; }
.discount-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; background: #f8fafc; padding: 15px; border-radius: 8px; }
.discount-item { background: #fff; padding: 10px; border-radius: 6px; border: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between; }
.risk-critical { border-left: 4px solid #e2231a; }
.risk-warning { border-left: 4px solid #ff7900; }
.risk-stable { border-left: 4px solid #007934; }
.promo-tag { width: 180px; background: #FFD700; border: 2px solid #e2231a; padding: 10px; text-align: center; transform: rotate(-2deg); }
.tag-top { background: #e2231a; color: #fff; font-size: 10px; font-weight: 900; }
.new-price { color: #e2231a; font-weight: 900; font-size: 28px; }
.save-strategy-btn { width: 100%; height: 50px; font-weight: 900; background: #007934 !important; color: #fff; margin-top: 20px; border: none; }
.c-orange { color: #ff7900; } .c-red { color: #e2231a; } .c-green { color: #007934; }
</style>
