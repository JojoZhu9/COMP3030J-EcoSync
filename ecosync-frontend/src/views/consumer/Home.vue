<template>
  <div class="home-container">
    <el-affix :offset="0">
      <div class="header-filter">
        <div class="header-content">
          <div class="store-module">
            <div class="store-indicator">
              <el-icon class="icon-pulse"><LocationFilled /></el-icon>
              <span class="label">Store</span>
            </div>
            <el-select
              v-model="selectedStoreId"
              placeholder="Select Store"
              @change="handleStoreChange"
              class="brand-select store-selector-expanded"
            >
              <el-option
                v-for="item in storeList"
                :key="item.storeId"
                :label="item.storeName"
                :value="item.storeId"
              />
            </el-select>
          </div>
        </div>
      </div>
    </el-affix>

    <div class="product-grid">
      <div class="grid-header">
        <h3 class="section-title">7-Eleven Eco-Flash</h3>
        <p class="section-subtitle">Real-time dynamic pricing based on freshness</p>
      </div>

      <el-skeleton :loading="loading" animated :count="4">
        <template #default>
          <el-row :gutter="20">
            <el-col
              :xs="24" :sm="12" :md="8" :lg="6"
              v-for="prod in productList"
              :key="prod.productId"
            >
              <el-card
                class="product-card"
                :class="{ 'is-expired': checkExpired(prod.expirationTime) }"
                shadow="never"
                @click="showDetail(prod)"
              >
                <div class="image-box">
                  <el-image :src="getImageUrl(prod)" fit="cover" style="width: 100%; height: 100%">
                    <template #error><el-icon :size="48" color="#e2e8f0"><Goods /></el-icon></template>
                  </el-image>

                  <div class="stock-tag" :class="{ 'urgent': getRemaining(prod) < 5 }">
                    {{ getRemaining(prod) > 0 ? `Only ${getRemaining(prod)} left` : 'Sold Out' }}
                  </div>

                  <div class="expiry-timer-tag" :class="{ 'expired-bg': checkExpired(prod.expirationTime) }">
                    <el-icon><Timer /></el-icon> {{ getTimeRemaining(prod.expirationTime) }}
                  </div>
                </div>

                <div class="content">
                  <div class="title-row">
                    <span class="name">{{ prod.productName }}</span>
                  </div>

                  <div class="price-box">
                    <div class="points-price">
                      <span class="unit">¥</span>
                      <span class="num">{{ prod.displayPrice }}</span>
                    </div>
                    <div v-if="Number(prod.displayPrice) < Number(prod.normalPrice)" class="card-original-price">
                      ¥{{ Number(prod.normalPrice).toFixed(2) }}
                    </div>
                  </div>

                  <el-button
                    :type="getBtnStatus(prod).type"
                    class="add-btn"
                    round
                    @click.stop="addToCart(prod)"
                    :disabled="getBtnStatus(prod).disabled"
                  >
                    <el-icon v-if="getBtnStatus(prod).icon"><Plus /></el-icon>
                    {{ getBtnStatus(prod).text }}
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>

      <el-empty v-if="!loading && productList.length === 0" description="No available items." />
    </div>

    <el-dialog
      v-model="detailVisible"
      class="modern-detail-dialog"
      :show-close="true"
      width="420px"
      center
      append-to-body
    >
      <div v-if="currentProduct" class="detail-wrapper">
        <div class="product-hero">
          <div class="hero-bg"></div>
          <el-image :src="getImageUrl(currentProduct)" fit="contain" class="floating-img">
            <template #error><el-icon :size="100" class="floating-icon"><Goods /></el-icon></template>
          </el-image>
          <div class="eco-badge"><el-icon><Star /></el-icon> Freshness Strategy</div>
        </div>

        <div class="product-info-sheet">
          <div class="info-header">
            <h2 class="p-name">{{ currentProduct.productName }}</h2>
            <div class="p-price-row">
              <span class="p-price">¥{{ currentProduct.displayPrice }}</span>
              <span v-if="Number(currentProduct.displayPrice) < Number(currentProduct.normalPrice)" class="p-original">
                ¥{{ Number(currentProduct.normalPrice).toFixed(2) }}
              </span>
            </div>
          </div>

          <div class="p-tags-container">
            <el-tag :type="checkExpired(currentProduct.expirationTime) ? 'info' : 'danger'" effect="dark" round>
              {{ checkExpired(currentProduct.expirationTime) ? 'EXPIRED' : getTimeRemaining(currentProduct.expirationTime) }}
            </el-tag>
            <el-tag type="success" effect="plain" round>Eco-Save</el-tag>
          </div>

          <div class="p-description-card">
            <div class="desc-item">
              <el-icon><Shop /></el-icon>
              <span>{{ storeList.find(s => s.storeId === selectedStoreId)?.storeName }}</span>
            </div>
            <div class="desc-item">
              <el-icon><Timer /></el-icon>
              <span>Ends at: {{ new Date(currentProduct.expirationTime).toLocaleString() }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-action-bar">
          <el-button
            :type="getBtnStatus(currentProduct).type"
            class="main-action-btn"
            :disabled="getBtnStatus(currentProduct).disabled"
            @click="handleDetailBuy"
          >
            {{ getBtnStatus(currentProduct).text }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { LocationFilled, Goods, Plus, Shop, Timer, Star } from '@element-plus/icons-vue'
import { storeApi } from '@/api/store'
import { expiringApi, standardApi } from '@/api/product'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const storeList = ref<any[]>([])
const selectedStoreId = ref<number | null>(null)
const productList = ref<any[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentProduct = ref<any>(null)

/** * 1. 核心价格计算逻辑
 * > 12h: 原价
 * <= 12h: 原价 * 折扣率数组对应索引
 * <= 0h: 标记过期
 */
const calculateCurrentPrice = (normalPrice: number, expirationTime: string, discountRatesStr: string) => {
  const now = Date.now()
  const exp = new Date(expirationTime).getTime()
  const hoursLeft = (exp - now) / (1000 * 60 * 60)

  // 场景 A: 已过期
  if (hoursLeft <= 0) return normalPrice.toFixed(2)

  // 场景 B: 距离过期超过 12 小时 -> 维持原价
  if (hoursLeft > 12) return normalPrice.toFixed(2)

  // 场景 C: 进入 12 小时临期窗口 -> 计算折扣
  try {
    const rates: number[] = JSON.parse(discountRatesStr)
    if (!Array.isArray(rates) || rates.length === 0) return normalPrice.toFixed(2)

    // 计算索引：11.5h -> index 0; 0.5h -> index 11
    const index = 12 - Math.ceil(hoursLeft)
    const safeIndex = Math.max(0, Math.min(index, rates.length - 1))
    const rate = Number(rates[safeIndex]) || 1.0

    return (normalPrice * rate).toFixed(2)
  } catch {
    return normalPrice.toFixed(2)
  }
}

const checkExpired = (expiryDate: string) => {
  return new Date(expiryDate).getTime() <= Date.now()
}

const getBtnStatus = (prod: any) => {
  if (!prod) return { type: 'info', text: 'Loading', disabled: true, icon: false }

  const isExpired = checkExpired(prod.expirationTime)
  const isOutStock = Number(prod.remainingStock || 0) <= 0

  if (isExpired) return { type: 'info', text: 'Expired', disabled: true, icon: false }
  if (isOutStock) return { type: 'info', text: 'Out of Stock', disabled: true, icon: false }

  return { type: 'success', text: 'Add to Basket', disabled: false, icon: true }
}

const getTimeRemaining = (expiryDate: string) => {
  if (!expiryDate) return 'N/A'
  const diff = new Date(expiryDate).getTime() - Date.now()
  if (diff <= 0) return 'Expired'
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return 'Due soon'
  if (hours < 24) return `${hours}h left`
  return `${Math.floor(hours / 24)}d left`
}

const getImageUrl = (prod: any) => {
  if (prod.imageUrl) return `/uploads/products/${prod.imageUrl}`
  return `/uploads/products/${prod.barcode}.jpg`
}

const getRemaining = (prod: any) => Number(prod.remainingStock || 0)

const fetchStores = async () => {
  try {
    const res = await storeApi.getAll() as any
    storeList.value = res.data || res || []
    if (storeList.value.length > 0) {
      const saved = localStorage.getItem('lastStoreId')
      selectedStoreId.value = saved ? Number(saved) : storeList.value[0].storeId
      fetchProducts()
    }
  } catch (e) { ElMessage.error('Store Sync Failed') }
}

const fetchProducts = async () => {
  if (!selectedStoreId.value) return
  loading.value = true
  try {
    const res = await expiringApi.getByStore(selectedStoreId.value) as any
    const rawItems = res.data || res || []

    const enriched = await Promise.all(rawItems.map(async (item: any) => {
      try {
        const std = await standardApi.getByBarcode(item.barcode) as any
        const stdData = std.data || std
        const nPrice = Number(stdData.normalPrice || 0)

        return {
          ...item,
          productName: stdData.productName || `SKU:${item.barcode}`,
          normalPrice: nPrice,
          displayPrice: calculateCurrentPrice(nPrice, item.expirationTime, stdData.discountRates || '[]'),
          imageUrl: stdData.imageUrl || null
        }
      } catch { return item }
    }))

    // 只显示状态为 AVAILABLE 的商品（过期的商品在渲染时通过 getBtnStatus 变灰）
    productList.value = enriched.filter(i => i.status === 'AVAILABLE')
  } catch (e) { ElMessage.error('Load Items Failed') }
  finally { loading.value = false }
}

const addToCart = async (prod: any) => {
  if (checkExpired(prod.expirationTime)) {
    ElMessage.error('Item has expired!')
    return
  }

  const userId = localStorage.getItem('userId') || '4'
  try {
    await request.post('/cart', {
      userId: Number(userId),
      productId: Number(prod.productId),
      quantity: 1
    })
    // 本地更新库存显示
    if (prod.remainingStock !== undefined) prod.remainingStock--
    ElMessage.success(`${prod.productName} added!`)
  } catch (e) { ElMessage.error('Add failed') }
}

const handleStoreChange = (val: number) => {
  localStorage.setItem('lastStoreId', String(val))
  fetchProducts()
}

const showDetail = (prod: any) => { currentProduct.value = prod; detailVisible.value = true; }
const handleDetailBuy = () => {
  if (currentProduct.value) {
    addToCart(currentProduct.value)
    if (getRemaining(currentProduct.value) <= 0) detailVisible.value = false
  }
}

onMounted(fetchStores)
</script>

<style scoped>
/* 核心样式保持 7-Eleven 风格 */
.home-container { background: #f8fafc; min-height: 100vh; padding-bottom: 50px; }
.header-filter {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  display: flex;
}
.header-content { width: 100%; max-width: 1200px; margin: 0 auto; }
.store-module {
  display: flex; align-items: center; gap: 12px;
  background: #f1f5f9; padding: 6px 16px; border-radius: 12px; max-width: 400px;
}
.product-grid { padding: 30px 20px; max-width: 1200px; margin: 0 auto; }
.section-title { font-size: 22px; font-weight: 900; color: #1e293b; margin-bottom: 4px; }
.section-subtitle { font-size: 13px; color: #64748b; margin-bottom: 24px; }

/* 卡片样式 */
.product-card {
  margin-bottom: 24px; border-radius: 20px; border: 1px solid #f1f5f9;
  cursor: pointer; transition: all 0.3s ease; position: relative;
}
.product-card.is-expired { opacity: 0.7; filter: grayscale(0.5); }
.product-card:hover:not(.is-expired) { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.05); }

.image-box { height: 160px; background: white; display: flex; justify-content: center; align-items: center; position: relative; }
.expiry-timer-tag {
  position: absolute; top: 8px; right: 8px;
  background: #ef4444; color: white; padding: 2px 8px;
  border-radius: 12px; font-size: 11px; font-weight: 800;
  display: flex; align-items: center; gap: 4px; z-index: 3;
}
.expired-bg { background: #64748b !important; }

.stock-tag {
  position: absolute; bottom: 8px; right: 8px; font-size: 10px;
  padding: 3px 10px; background: #e2e8f0; border-radius: 20px; font-weight: 800;
}
.stock-tag.urgent { background: #fee2e2; color: #ef4444; }

.content { padding: 16px; }
.name { font-weight: 800; font-size: 15px; color: #1e293b; height: 40px; display: block; overflow: hidden; }
.points-price { color: #ee7203; font-weight: 900; margin-top: 8px; }
.points-price .num { font-size: 24px; }
.card-original-price { text-decoration: line-through; color: #94a3b8; font-size: 12px; }

.add-btn { width: 100%; height: 44px; font-weight: 800; border: none; margin-top: 12px; }
.add-btn.el-button--success { background: #008163 !important; }

/* 详情对话框样式 */
.product-hero { height: 200px; background: linear-gradient(135deg, #008163 0%, #004b3a 100%); position: relative; display: flex; justify-content: center; align-items: center; }
.floating-img { width: 140px; height: 140px; filter: drop-shadow(0 10px 20px rgba(0,0,0,0.2)); }
.product-info-sheet { padding: 24px; }
.p-name { font-size: 22px; font-weight: 900; margin-bottom: 8px; }
.p-price { font-size: 32px; font-weight: 900; color: #ee7203; }
.p-description-card { background: #f8fafc; padding: 16px; border-radius: 16px; margin-top: 20px; }
.desc-item { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #475569; margin-bottom: 8px; }
.main-action-btn { width: 100%; height: 50px; border-radius: 15px; font-weight: 800; font-size: 16px; border: none; }
</style>
