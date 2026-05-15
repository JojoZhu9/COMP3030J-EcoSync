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
            <span class="header-title">Product Strategy: <span class="highlight-name">{{ prod.productName }}</span></span>
          </div>
        </template>
      </el-page-header>

      <el-row :gutter="24" class="mt-20">
        <el-col :span="8">
          <el-card class="modern-card info-card" shadow="never">
            <div class="digital-label">
              <div class="label-header">
                <span class="sku-tag">SKU: {{ prod.barcode }}</span>
                <el-tag size="small" type="success" effect="dark" round class="pulse-tag">LIVE</el-tag>
              </div>
              <h2 class="label-name">{{ prod.productName }}</h2>
              <div class="barcode-box">
                <svg ref="barcodeSvgRef"></svg>
                <div class="barcode-num">{{ prod.barcode }}</div>
              </div>
              <div class="label-price-row">
                <div class="price-label">STANDARD RETAIL PRICE</div>
                <div class="price-value">
                  <span class="currency">¥</span>
                  <span class="amount">{{ parseFloat(prod.normalPrice || 0).toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="16">
          <el-card class="modern-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title-box">
                  <el-icon class="c-orange title-icon"><TrendCharts /></el-icon>
                  <span class="main-title">Dynamic Price Curve (12h Decay)</span>
                </div>
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
                  class="modern-input-num"
                />
              </div>
            </div>

            <div class="promo-preview-box">
              <div class="promo-header"><el-icon><View /></el-icon> E-Label Preview Simulation</div>
              <div class="promo-body">
                <div class="promo-tag">
                  <div class="tag-top">LIMITED TIME OFFER</div>
                  <div class="tag-middle">
                    <div class="old-price">Orig: ¥{{ parseFloat(prod.normalPrice).toFixed(2) }}</div>
                    <div class="new-price">
                      <span class="yen">¥</span>
                      <span class="val">{{ (parseFloat(prod.normalPrice) * editForm.rates[previewHour-1]).toFixed(2) }}</span>
                    </div>
                  </div>
                  <div class="tag-bottom">ONLY {{ previewHour }}H LEFT BEFORE EXPIRY</div>
                </div>

                <div class="promo-controls">
                  <p class="control-hint">Simulate time remaining:</p>
                  <el-slider v-model="previewHour" :min="1" :max="12" show-stops class="custom-slider" />
                  <div class="rate-indicator">Multiplier Applied: <span class="c-red">{{ editForm.rates[previewHour-1].toFixed(2) }}x</span></div>
                </div>
              </div>
            </div>

            <el-button type="success" @click="saveRule" class="save-strategy-btn" :loading="saving">
              DEPLOY STRATEGY TO STORE NETWORK
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
import { TrendCharts, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import JsBarcode from 'jsbarcode'

const route = useRoute()
const router = useRouter()
const prod = ref<any>(null)
const loading = ref(true)
const saving = ref(false)
const previewHour = ref(1)

const discountChartRef = ref<HTMLElement | null>(null)
const barcodeSvgRef = ref<HTMLElement | null>(null) // 条码渲染的引用节点

let chartInstance: echarts.ECharts | null = null
const editForm = reactive({ rates: Array(12).fill(1.0) })

const getRiskClass = (h: number) => h <= 3 ? 'risk-critical' : (h <= 6 ? 'risk-warning' : 'risk-stable')

const updateChartData = () => {
  if (!chartInstance) return
  chartInstance.setOption({
    grid: { top: '10%', left: '3%', right: '3%', bottom: '10%', containLabel: true },
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(15,23,42,0.9)', textStyle: { color: '#fff' }, borderWidth: 0 },
    xAxis: { type: 'category', data: Array.from({length:12}, (_, i) => `${i+1}h`), axisLine: { lineStyle: { color: '#cbd5e1' } }, axisLabel: { color: '#64748b', fontWeight: 'bold' } },
    yAxis: { type: 'value', max: 1.0, min: 0, splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } }, axisLabel: { color: '#64748b' } },
    series: [{
      data: [...editForm.rates],
      type: 'line',
      smooth: true,
      symbolSize: 8,
      itemStyle: { color: '#008163' },
      lineStyle: { width: 4, shadowColor: 'rgba(0,129,99,0.3)', shadowBlur: 10, shadowOffsetY: 5 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: 'rgba(0,129,99,0.3)'}, {offset: 1, color: 'transparent'}]) }
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

    // 携带所有数据，防止保存时覆盖
    prod.value = {
      barcode: data.barcode,
      productName: data.product_name || data.productName,
      normalPrice: data.normal_price || data.normalPrice,
      imageUrl: data.image_url || data.imageUrl,
      status: data.status || 'ACTIVE'
    }

    const rawRates = data.discount_rates || data.discountRates
    if (rawRates) {
      const parsed = typeof rawRates === 'string' ? JSON.parse(rawRates) : rawRates
      if (Array.isArray(parsed)) editForm.rates = parsed
    }

    nextTick(() => {
      // 1. 渲染图表
      if (discountChartRef.value) {
        chartInstance = echarts.init(discountChartRef.value)
        updateChartData()
      }

      // 2. 渲染动态条形码
      if (barcodeSvgRef.value && prod.value.barcode) {
        JsBarcode(barcodeSvgRef.value, prod.value.barcode, {
          format: "CODE128",
          displayValue: false, // 隐藏默认文字，使用我们自己设计的更美观的文字
          height: 44,
          width: 2,
          background: "transparent",
          lineColor: "#1e293b",
          margin: 0
        })
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
      discountRates: JSON.stringify(editForm.rates) // 使用驼峰匹配后端
    })
    ElMessage.success('Strategy Deployed Successfully!')
  } catch (e) { ElMessage.error('Save failed') }
  finally { saving.value = false }
}

onMounted(fetchDetail)
window.addEventListener('resize', () => chartInstance?.resize())
onUnmounted(() => chartInstance?.dispose())
</script>

<style scoped>
.pc-staff-layout { background: transparent; min-height: 80vh; }
.brand-top-stripe { height: 6px; width: 100%; position: fixed; top: 0; left: 0; z-index: 10; background: linear-gradient(90deg, #ff7900 33%, #008163 33%, #008163 66%, #e2231a 66%); }
.pc-header { background: #fff; padding: 20px 25px; border-radius: 16px; margin-bottom: 24px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid rgba(0,0,0,0.04); }
.header-logo-content { display: flex; align-items: center; gap: 12px; }
.brand-logo-small { font-weight: 900; font-style: italic; font-size: 18px; letter-spacing: -0.5px; }
.divider { color: #cbd5e1; font-weight: 300; }
.header-title { font-size: 16px; color: #64748b; font-weight: 600; }
.highlight-name { color: #1e293b; font-weight: 800; }
.mt-20 { margin-top: 20px; }

.modern-card { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 4px 20px rgba(0,0,0,0.03) !important; background: #fff; }
:deep(.modern-card .el-card__header) { padding: 20px 24px; border-bottom: 1px solid #f1f5f9; }

.digital-label { background: #fdfdfd; padding: 24px; border: 2px solid #1e293b; border-radius: 12px; box-shadow: 6px 6px 0 rgba(30,41,59,0.1); position: relative; }
.label-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px dashed #e2e8f0; padding-bottom: 12px; }
.sku-tag { font-family: monospace; font-weight: 900; color: #64748b; font-size: 14px; letter-spacing: 1px; }
.pulse-tag { animation: pulse 2s infinite; font-weight: 800; }
.label-name { font-size: 24px; font-weight: 900; color: #1e293b; margin: 20px 0; line-height: 1.3; }
.barcode-box { text-align: center; margin: 20px 0; opacity: 0.9; }
.barcode-num { font-family: 'Courier New', Courier, monospace; font-size: 13px; letter-spacing: 4px; margin-top: 4px; font-weight: bold; color: #64748b;}
.label-price-row { background: #f8fafc; padding: 16px; border-radius: 8px; border: 1px solid #e2e8f0; }
.price-label { font-size: 10px; font-weight: 800; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px; }
.price-value { color: #1e293b; }
.price-value .currency { font-size: 16px; font-weight: bold; margin-right: 4px; }
.price-value .amount { font-size: 42px; font-weight: 900; line-height: 1; }

.header-title-box { display: flex; align-items: center; gap: 10px; }
.title-icon { font-size: 20px; }
.main-title { font-size: 16px; font-weight: 800; color: #1e293b; }

.chart-section { height: 300px; margin-bottom: 20px; }
.divider-text { font-size: 11px; font-weight: 800; color: #94a3b8; letter-spacing: 1px; }

.discount-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; background: #f8fafc; padding: 20px; border-radius: 12px; border: 1px solid #f1f5f9; margin-bottom: 24px; }
.discount-item { background: #fff; padding: 12px; border-radius: 8px; border: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.hour-badge { font-weight: 800; color: #475569; font-size: 14px; }
.modern-input-num :deep(.el-input__wrapper) { box-shadow: none !important; border: 1px solid #e2e8f0; border-radius: 6px; }

.risk-critical { border-left: 4px solid #e2231a; }
.risk-warning { border-left: 4px solid #EE7203; }
.risk-stable { border-left: 4px solid #008163; }

.promo-preview-box { border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; margin-bottom: 24px; }
.promo-header { background: #f8fafc; padding: 12px 20px; font-weight: 800; color: #475569; font-size: 13px; display: flex; align-items: center; gap: 8px; border-bottom: 1px solid #e2e8f0; }
.promo-body { display: flex; align-items: center; gap: 40px; padding: 30px; background: #fff; }

.promo-tag { width: 220px; background: #FFD700; border: 3px solid #e2231a; padding: 12px; text-align: center; transform: rotate(-3deg); box-shadow: 4px 6px 12px rgba(226,35,26,0.15); }
.tag-top { background: #e2231a; color: #fff; font-size: 12px; font-weight: 900; padding: 4px; letter-spacing: 1px; }
.tag-middle { padding: 12px 0; background: #fff; border-left: 2px dashed #e2231a; border-right: 2px dashed #e2231a; }
.old-price { text-decoration: line-through; color: #94a3b8; font-size: 13px; font-weight: bold; margin-bottom: 4px; }
.new-price { color: #e2231a; font-weight: 900; line-height: 1; }
.new-price .yen { font-size: 18px; margin-right: 2px; }
.new-price .val { font-size: 36px; }
.tag-bottom { background: #1e293b; color: #FFD700; font-size: 10px; font-weight: 900; padding: 6px; letter-spacing: 0.5px; }

.promo-controls { flex: 1; }
.control-hint { font-size: 12px; font-weight: 800; color: #64748b; margin-bottom: 10px; }
.custom-slider { margin-bottom: 20px; }
.rate-indicator { font-size: 14px; font-weight: 700; color: #475569; background: #f8fafc; padding: 10px 16px; border-radius: 8px; border: 1px solid #e2e8f0; display: inline-block; }

.save-strategy-btn { width: 100%; height: 54px; font-weight: 900; font-size: 15px; background: linear-gradient(135deg, #008163, #005a46) !important; color: #fff; border: none; border-radius: 12px; box-shadow: 0 6px 16px rgba(0,129,99,0.3); transition: transform 0.2s; }
.save-strategy-btn:hover { transform: translateY(-2px); }

.c-orange { color: #EE7203; } .c-red { color: #e2231a; } .c-green { color: #008163; }
</style>
