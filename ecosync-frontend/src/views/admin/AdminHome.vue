<template>
  <div class="page-container pc-staff-layout" v-loading="loading">
    <div v-if="prod" class="content-wrapper">
      <div class="brand-stripe"></div>

      <el-page-header @back="$router.back()" class="pc-header">
        <template #icon>
          <el-icon class="pc-green"><ArrowLeft /></el-icon>
        </template>
        <template #content>
          <div class="header-logo-content">
            <img src="https://upload.wikimedia.org/wikipedia/commons/4/40/7-eleven_logo.svg" alt="7-11 Logo" class="logo-img" />
            <span class="font-bold pc-text-dark divider">|</span>
            <span class="font-bold pc-text-dark"> Product Configuration: <span class="highlight-name">{{ prod.productName }}</span> </span>
          </div>
        </template>
        <template #extra>
          <el-button color="#007934" plain size="small" icon="Refresh" @click="fetchDetail" class="refresh-btn">Refresh Data</el-button>
        </template>
      </el-page-header>

      <el-row :gutter="20" class="mt-20">
        <el-col :span="9">
          <el-card class="pc-card info-card" shadow="hover">
            <template #header>
              <div class="card-header pc-green-title">
                <el-icon><Document /></el-icon> Basic Product Info
              </div>
            </template>

            <div class="receipt-style-box">
              <el-descriptions :column="1" border size="large" class="detailed-descriptions">
                <el-descriptions-item label="Product Name" label-class-name="desc-label">
                  <span class="desc-val">{{ prod.productName }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="Barcode" label-class-name="desc-label">
                  <div class="barcode-wrapper">
                    <el-icon><CopyDocument /></el-icon>
                    <span class="barcode-text">{{ prod.barcode }}</span>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="Standard Price" label-class-name="desc-label">
                  <span class="price-text"><span class="currency">￥</span>{{ parseFloat(prod.normalPrice || 0).toFixed(2) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="Status" label-class-name="desc-label">
                  <el-tag color="#007934" effect="dark" size="small" class="status-tag">Listed</el-tag>
                </el-descriptions-item>
              </el-descriptions>
              <div class="receipt-footer">
                * Basic data is synced from 7-ELEVEn HQ. Stores can only configure near-expiry discounts.
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="15">
          <el-card class="pc-card discount-card" shadow="hover">
            <template #header>
              <div class="card-header pc-orange-title">
                <el-icon><TrendCharts /></el-icon> Near-Expiry Discount Curve Configuration (1-12 Hours Prior)
              </div>
            </template>

            <el-form label-position="top">
              <div ref="discountChartRef" class="discount-chart-container"></div>

              <el-divider content-position="center" class="brand-divider">
                <el-icon><EditPen /></el-icon> Fine-Tune Discount Coefficient (1.0 = No Discount)
              </el-divider>

              <div class="discount-grid-optimized">
                <div v-for="h in 12" :key="h" class="discount-item-opt" :class="{'high-risk': h <= 3}">
                  <span class="hour-label-opt">{{ h }}h Prior</span>
                  <el-input-number
                    v-model="editForm.rates[h-1]"
                    :min="0.1" :max="1" :step="0.1" :precision="1"
                    size="small" controls-position="right"
                    class="pc-input-number"
                    @change="updateChartData"
                  />
                </div>
              </div>

              <div class="pc-price-preview-box mt-20">
                <div class="preview-title">
                  <el-icon><View /></el-icon> Real-Time Pricing Strategy Preview
                </div>
                <div class="preview-content">
                  <div class="preview-text">
                    If the product expires in <el-tag color="#ff7900" effect="dark" size="small" round>{{ previewHour }} hours</el-tag>
                    (Current coefficient: <b class="orange">{{ editForm.rates[previewHour-1].toFixed(1) }}</b>)
                  </div>
                  <div class="preview-calc">
                    Suggested Price = ￥{{ parseFloat(prod.normalPrice || 0).toFixed(2) }} × {{ editForm.rates[previewHour-1].toFixed(1) }} =
                    <span class="final-price-text"><span class="currency">￥</span>{{ (parseFloat(prod.normalPrice || 0) * editForm.rates[previewHour-1]).toFixed(2) }}</span>
                  </div>
                </div>
                <el-slider v-model="previewHour" :min="1" :max="12" :step="1" show-stops class="preview-slider" />
              </div>

              <el-button type="success" @click="saveRule" class="pc-save-btn mt-20" :loading="saving" icon="Checked">
                Save & Apply 7-11 Discount Strategy
              </el-button>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else-if="!loading" description="No relevant product information found" class="pc-empty">
      <template #image>
        <img src="https://upload.wikimedia.org/wikipedia/commons/4/40/7-eleven_logo.svg" style="filter: grayscale(100%); opacity: 0.3; width: 120px;" />
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, nextTick, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Refresh, Document, TrendCharts, Checked, View, CopyDocument, EditPen } from '@element-plus/icons-vue'
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

const initAndRenderChart = () => {
  if (!discountChartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(discountChartRef.value)
  }
  updateChartData()
}

const updateChartData = () => {
  if (!chartInstance) return
  const xAxisData = Array.from({ length: 12 }, (_, i) => `${i + 1}h`)
  const yData = [...editForm.rates]

  const option = {
    // 7-11 Brand Green
    color: ['#007934'],
    tooltip: {
      trigger: 'axis',
      formatter: '{b} before expiry <br/> Discount Rate: {c}',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#007934',
      textStyle: { color: '#333' }
    },
    grid: { left: '2%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#606266', fontWeight: 'bold' }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1.1,
      interval: 0.2,
      splitLine: { lineStyle: { type: 'dashed', color: '#ebeef5' } },
      axisLabel: { color: '#909399' }
    },
    series: [{
      data: yData,
      type: 'line',
      smooth: true,
      symbolSize: 10,
      itemStyle: {
        color: '#ff7900', // 7-11 Orange Node
        borderWidth: 2,
        borderColor: '#fff'
      },
      lineStyle: {
        width: 4,
        shadowColor: 'rgba(0, 121, 52, 0.3)',
        shadowBlur: 10,
        shadowOffsetY: 5
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0, 121, 52, 0.25)' },
          { offset: 1, color: 'rgba(0, 121, 52, 0.0)' }
        ])
      }
    }]
  }
  chartInstance.setOption(option)
}

const fetchDetail = async () => {
  loading.value = true
  const id = route.query.id
  if (!id) {
    loading.value = false
    return
  }
  try {
    const res: any = await request.get(`/products/${id}`)
    const data = res.data?.data || res.data || res
    prod.value = data

    if (data.discountRates) {
      try {
        const parsed = typeof data.discountRates === 'string' ? JSON.parse(data.discountRates) : data.discountRates
        if (Array.isArray(parsed) && parsed.length === 12) {
          editForm.rates = parsed
        }
      } catch (err) {
        console.warn('Parsing failed', err)
      }
    }
    nextTick(() => initAndRenderChart())
  } catch (e) {
    ElMessage.error('Failed to fetch details')
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
    ElMessage.success('7-ELEVEn discount strategy applied successfully')
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
/* --- Global Layout & Brand Colors --- */
.pc-staff-layout { background-color: #f0f2f5; min-height: 100vh; padding: 20px 30px; font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif; }
.content-wrapper { max-width: 1400px; margin: 0 auto; position: relative; }

/* Brand Top Stripe (Orange-Green-Red) */
.brand-stripe {
  height: 5px;
  width: 100%;
  background: linear-gradient(to right, #ff7900 33.3%, #007934 33.3%, #007934 66.6%, #e2231a 66.6%);
  border-radius: 8px 8px 0 0;
  margin-bottom: -5px;
  position: relative;
  z-index: 2;
}

/* Header Navigation */
.pc-header { background: #fff; padding: 18px 24px; border-radius: 0 0 8px 8px; margin-bottom: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); position: relative; z-index: 1; }
.header-logo-content { display: flex; align-items: center; gap: 12px; }
.logo-img { height: 26px; }
.divider { color: #dcdfe6; font-weight: 300; }
.highlight-name { color: #007934; }
.pc-green { color: #007934 !important; }
.refresh-btn { font-weight: bold; border-radius: 6px; }

/* Card Universal Styles */
.pc-card { border-radius: 12px; border: none; box-shadow: 0 6px 16px rgba(0,0,0,0.04) !important; transition: all 0.3s ease; }
.pc-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,0.08) !important; }
:deep(.el-card__header) { padding: 16px 24px; border-bottom: 1px solid #f0f0f0; background: #fafafa; border-radius: 12px 12px 0 0; }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 15px; }

/* Color Definitions */
.pc-green-title { color: #007934; }
.pc-orange-title { color: #ff7900; }
.orange { color: #ff7900 !important; }

/* --- Left Side: Basic Information (Receipt Style) --- */
.receipt-style-box { padding: 10px; }
.detailed-descriptions { border-radius: 8px; overflow: hidden; }
:deep(.desc-label) { background-color: #f7f9fc !important; color: #606266; width: 110px; text-align: right; font-weight: bold; }
.desc-val { font-weight: 600; color: #303133; font-size: 15px; }
.barcode-wrapper { display: flex; align-items: center; gap: 8px; background: #f4f4f5; padding: 4px 10px; border-radius: 6px; width: fit-content; }
.barcode-text { font-family: 'Courier New', Courier, monospace; letter-spacing: 1px; color: #333; font-weight: bold; }
.price-text { color: #e2231a; font-size: 22px; font-weight: 900; }
.currency { font-size: 14px; margin-right: 2px; }
.status-tag { border: none; font-weight: bold; }
.receipt-footer { margin-top: 20px; font-size: 12px; color: #909399; text-align: center; border-top: 1px dashed #dcdfe6; padding-top: 15px; }

/* --- Right Side: Discount Chart & Configuration --- */
.discount-chart-container { width: 100%; height: 280px; margin-bottom: 10px; }
.brand-divider :deep(.el-divider__text) { color: #007934; font-weight: bold; font-size: 13px; background-color: #fff; }

/* Grid Style Optimization */
.discount-grid-optimized { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; background: #f9fafc; padding: 18px; border-radius: 10px; border: 1px solid #ebeef5; }
.discount-item-opt { display: flex; align-items: center; justify-content: space-between; background: #fff; padding: 6px 10px; border: 1px solid #e4e7ed; border-radius: 6px; transition: border 0.3s; }
.discount-item-opt:hover { border-color: #007934; }
.discount-item-opt.high-risk { border-left: 3px solid #e2231a; }
.hour-label-opt { font-size: 13px; color: #606266; font-weight: bold; width: 55px; }
.pc-input-number :deep(.el-input__wrapper) { box-shadow: none !important; border: 1px solid transparent; background: transparent; }
.pc-input-number :deep(.el-input__inner) { font-weight: 900; color: #007934; font-size: 14px; }

/* Price Preview Box - Promotional Sign Style */
.pc-price-preview-box { background: linear-gradient(to right, #fff5eb, #ffffff); padding: 20px; border-radius: 10px; border: 1px solid #ffce99; position: relative; overflow: hidden; }
.pc-price-preview-box::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: #ff7900; }
.preview-title { display: flex; align-items: center; gap: 8px; color: #d35400; font-weight: bold; font-size: 15px; margin-bottom: 15px; }
.preview-content { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 2px 8px rgba(255,121,0,0.08); }
.preview-text { font-size: 14px; color: #555; }
.preview-calc { font-size: 14px; color: #666; font-weight: 500; display: flex; align-items: center; }
.final-price-text { color: #e2231a; font-size: 28px; font-weight: 900; margin-left: 10px; }
.preview-slider { margin: 0 15px; }
:deep(.el-slider__bar) { background-color: #ff7900; }
:deep(.el-slider__button) { border-color: #ff7900; }

/* Bottom Save Button */
.pc-save-btn { width: 100%; height: 48px; font-size: 16px; font-weight: bold; letter-spacing: 1px; border-radius: 8px; border: none; background: linear-gradient(to right, #008050, #007934) !important; color: #fff !important; box-shadow: 0 4px 12px rgba(0, 121, 52, 0.3); transition: all 0.3s ease; }
.pc-save-btn:hover { background: linear-gradient(to right, #009660, #008a3d) !important; transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0, 121, 52, 0.4); }

.mt-20 { margin-top: 20px !important; }
.pc-empty { padding-top: 100px; }
</style>
