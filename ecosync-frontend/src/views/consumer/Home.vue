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
        <h3 class="section-title">
          <span class="brand-text">7-Eleven</span> Eco-Flash
        </h3>
        <p class="section-subtitle">Real-time dynamic pricing • Only for items within 12h of expiry</p>
      </div>

      <el-skeleton :loading="loading" animated :count="8">
        <template #default>
          <el-row :gutter="20">
            <el-col
              :xs="24" :sm="12" :md="8" :lg="6"
              v-for="prod in productList"
              :key="prod.productId"
              class="card-col"
            >
              <el-card
                class="product-card"
                :class="{ 'card-expired': isExpired(prod.expirationTime) }"
                shadow="hover"
                @click="showDetail(prod)"
              >
                <div v-if="isExpired(prod.expirationTime)" class="expired-overlay">
                  <span class="expired-text">EXPIRED</span>
                </div>

                <div class="image-box">
                  <el-image :src="getImageUrl(prod)" fit="cover" class="product-img">
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon :size="48" color="#cbd5e1"><Goods /></el-icon>
                      </div>
                    </template>
                  </el-image>

                  <div class="stock-tag" :class="{ 'urgent': getRemaining(prod) < 5 && getRemaining(prod) > 0 }">
                    {{ getRemaining(prod) > 0 ? `Only ${getRemaining(prod)} left` : 'Sold Out' }}
                  </div>

                  <div class="expiry-timer-tag" :class="{ 'bg-gray': isExpired(prod.expirationTime) }">
                    <el-icon><Timer /></el-icon> {{ getTimeRemaining(prod.expirationTime) }}
                  </div>
                </div>

                <div class="content">
                  <div class="title-row">
                    <span class="name" :title="prod.productName">{{ prod.productName }}</span>
                  </div>

                  <div class="price-action-row">
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
                      :type="getBtnConfig(prod).type"
                      class="add-btn"
                      :class="{ 'is-disabled-style': getBtnConfig(prod).disabled }"
                      circle
                      @click.stop="addToCart(prod)"
                      :disabled="getBtnConfig(prod).disabled"
                    >
                      <el-icon v-if="!getBtnConfig(prod).disabled"><Plus /></el-icon>
                      <span v-else class="disabled-text">{{ getBtnConfig(prod).text === 'Out of Stock' ? '0' : '-' }}</span>
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>

      <el-empty v-if="!loading && productList.length === 0" description="No available items right now." />
    </div>

    <el-dialog v-model="detailVisible" width="400px" center append-to-body class="modern-detail-dialog" :show-close="false">
      <div v-if="currentProduct" class="detail-wrapper">
        <div class="product-hero" :class="{ 'hero-expired': isExpired(currentProduct.expirationTime) }">
          <el-image :src="getImageUrl(currentProduct)" fit="contain" class="floating-img" />
        </div>
        <div class="product-info-sheet">
          <h2 class="p-name">{{ currentProduct.productName }}</h2>
          <div class="p-price-row">
            <span class="p-price">¥{{ currentProduct.displayPrice }}</span>
            <span v-if="Number(currentProduct.displayPrice) < Number(currentProduct.normalPrice)" class="p-original">
              ¥{{ Number(currentProduct.normalPrice).toFixed(2) }}
            </span>
          </div>
          <el-divider border-style="dashed" />
          <div class="p-tags-container">
            <el-tag :type="isExpired(currentProduct.expirationTime) ? 'info' : 'danger'" effect="light" round>
              {{ isExpired(currentProduct.expirationTime) ? 'ALREADY EXPIRED' : getTimeRemaining(currentProduct.expirationTime) }}
            </el-tag>
            <el-tag type="warning" effect="plain" round v-if="getRemaining(currentProduct) > 0">
              Stock: {{ getRemaining(currentProduct) }}
            </el-tag>
          </div>
        </div>
        <div class="dialog-action-bar">
          <el-button plain @click="detailVisible = false" class="cancel-btn">Cancel</el-button>
          <el-button
            :type="getBtnConfig(currentProduct).type"
            class="main-action-btn"
            :disabled="getBtnConfig(currentProduct).disabled"
            @click="handleDetailBuy"
          >
            {{ getBtnConfig(currentProduct).text }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { LocationFilled, Goods, Plus, Shop, Timer } from '@element-plus/icons-vue'
import { storeApi } from '@/api/store'
import { expiringApi, standardApi } from '@/api/product'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// ... (保持你原有的所有 TypeScript 逻辑完全不变，以确保功能稳定) ...
const storeList = ref<any[]>([])
const selectedStoreId = ref<number | null>(null)
const productList = ref<any[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentProduct = ref<any>(null)

const calculateDisplayPrice = (normalPrice: number, expirationTime: string, ratesStr: string) => {
  const now = Date.now()
  const exp = new Date(expirationTime).getTime()
  const diffMs = exp - now
  const hoursLeft = diffMs / (1000 * 60 * 60)
  if (hoursLeft <= 0 || hoursLeft > 12) return normalPrice.toFixed(2)
  try {
    const rates = JSON.parse(ratesStr)
    if (!Array.isArray(rates) || rates.length === 0) return normalPrice.toFixed(2)
    const index = Math.floor(12 - hoursLeft)
    const safeIndex = Math.max(0, Math.min(index, rates.length - 1))
    return (normalPrice * rates[safeIndex]).toFixed(2)
  } catch (e) {
    return normalPrice.toFixed(2)
  }
}

const isExpired = (expiryDate: string) => {
  if (!expiryDate) return true
  return new Date(expiryDate).getTime() <= Date.now()
}

const getBtnConfig = (prod: any) => {
  if (!prod) return { type: 'info', text: 'Loading', disabled: true }
  if (isExpired(prod.expirationTime)) return { type: 'info', text: 'Expired', disabled: true }
  if (Number(prod.remainingStock || 0) <= 0) return { type: 'info', text: 'Out of Stock', disabled: true }
  return { type: 'success', text: 'Add to Basket', disabled: false }
}

const getTimeRemaining = (expiryDate: string) => {
  const diff = new Date(expiryDate).getTime() - Date.now()
  if (diff <= 0) return 'Expired'
  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  if (h >= 24) return `${Math.floor(h/24)}d left`
  if (h > 0) return `${h}h ${m}m left`
  return `${m}m left`
}

const fetchProducts = async () => {
  if (!selectedStoreId.value) return
  loading.value = true
  try {
    const res: any = await expiringApi.getByStore(selectedStoreId.value)
    const rawItems = res.data || res || []
    const enriched = await Promise.all(rawItems.map(async (item: any) => {
      try {
        const std: any = await standardApi.getByBarcode(item.barcode)
        const stdData = std.data || std
        const basePrice = Number(stdData.normalPrice || stdData.normal_price || 0)
        const rates = stdData.discountRates || stdData.discount_rates || '[]'
        return {
          ...item,
          productName: stdData.productName || stdData.product_name || `SKU:${item.barcode}`,
          normalPrice: basePrice,
          displayPrice: calculateDisplayPrice(basePrice, item.expirationTime, rates),
          imageUrl: stdData.imageUrl || stdData.image_url || null
        }
      } catch (e) {
        return { ...item, displayPrice: '0.00', normalPrice: 0 }
      }
    }))
    productList.value = enriched.filter(i => i.status === 'AVAILABLE')
  } catch (e) {
    ElMessage.error('Failed to sync product data')
  } finally {
    loading.value = false
  }
}

const getImageUrl = (prod: any) => {
  const name = prod.imageUrl || `${prod.barcode}.jpg`
  return `/uploads/products/${name}`
}
const getRemaining = (prod: any) => Number(prod.remainingStock || 0)

const fetchStores = async () => {
  try {
    const res: any = await storeApi.getAll()
    storeList.value = res.data || res || []
    if (storeList.value.length > 0) {
      const saved = localStorage.getItem('lastStoreId')
      selectedStoreId.value = saved ? Number(saved) : storeList.value[0].storeId
      fetchProducts()
    }
  } catch (e) { ElMessage.error('Store Sync Failed') }
}

const addToCart = async (prod: any) => {
  if (isExpired(prod.expirationTime)) return ElMessage.warning('This item has expired!')
  const userId = localStorage.getItem('userId') || '4'
  try {
    await request.post('/cart', {
      userId: Number(userId),
      productId: Number(prod.productId),
      quantity: 1
    })
    if (prod.remainingStock > 0) prod.remainingStock--
    ElMessage.success('Added to basket')
  } catch (e) { ElMessage.error('Failed to add') }
}

const handleStoreChange = (val: number) => {
  localStorage.setItem('lastStoreId', String(val))
  fetchProducts()
}

const showDetail = (prod: any) => { currentProduct.value = prod; detailVisible.value = true; }
const handleDetailBuy = () => { if (currentProduct.value) addToCart(currentProduct.value) }

onMounted(fetchStores)
</script>

<style scoped>
/* 基础容器 */
.home-container { background: #f4f6f8; min-height: 100vh; padding-bottom: 40px; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }

/* 头部导航区 */
.header-filter { padding: 12px 24px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(8px); border-bottom: 1px solid #e2e8f0; }
.store-module { display: flex; align-items: center; gap: 8px; background: #f8fafc; padding: 6px 16px; border-radius: 20px; width: fit-content; border: 1px solid #e2e8f0; transition: all 0.3s; }
.store-module:hover { border-color: #008163; box-shadow: 0 0 0 2px rgba(0, 129, 99, 0.1); }
.store-indicator { display: flex; align-items: center; gap: 6px; color: #008163; font-weight: bold; }

/* 标题区 */
.product-grid { padding: 24px; max-width: 1200px; margin: 0 auto; }
.grid-header { margin-bottom: 24px; }
.section-title { font-size: 24px; font-weight: 900; color: #1e293b; margin: 0 0 4px 0; }
.brand-text { color: #008163; }
.section-subtitle { color: #64748b; font-size: 14px; margin: 0; }

/* 卡片整体与动画 */
.card-col { margin-bottom: 20px; }
.product-card { border-radius: 16px; border: none; overflow: hidden; position: relative; transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.2s; background: #fff; cursor: pointer; }
.product-card:hover:not(.card-expired) { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.08) !important; }
.card-expired { opacity: 0.65; filter: grayscale(0.8); cursor: not-allowed !important; }

/* 过期毛玻璃遮罩 */
.expired-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255,255,255,0.6); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center; z-index: 10;
}
.expired-text {
  background: #1e293b; color: white; padding: 6px 16px; border-radius: 20px;
  font-weight: 900; font-size: 14px; letter-spacing: 2px; transform: rotate(-10deg);
}

/* 图片区域与角标 */
.image-box { height: 180px; background: #f8fafc; position: relative; display: flex; align-items: center; justify-content: center; }
.product-img { width: 100%; height: 100%; transition: transform 0.3s; }
.product-card:hover .product-img { transform: scale(1.05); }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f1f5f9; }

/* 补充了你原本缺失的库存标签样式 */
.stock-tag {
  position: absolute; bottom: 10px; left: 10px; background: rgba(30, 41, 59, 0.7);
  color: #fff; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; backdrop-filter: blur(4px);
}
.stock-tag.urgent { background: rgba(226, 35, 26, 0.85); animation: pulse 2s infinite; }

.expiry-timer-tag {
  position: absolute; top: 10px; right: 10px; background: #e2231a; box-shadow: 0 2px 8px rgba(226, 35, 26, 0.3);
  color: #fff; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; display: flex; align-items: center; gap: 4px;
}
.bg-gray { background: #64748b !important; box-shadow: none !important; }

/* 内容区域与文字截断 */
.content { padding: 16px; }
.title-row { margin-bottom: 12px; }
.name {
  font-weight: 700; font-size: 15px; color: #334155; line-height: 1.4;
  /* 多行文本省略号 */
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 42px;
}

/* 价格与按钮同行布局 */
.price-action-row { display: flex; justify-content: space-between; align-items: flex-end; }
.price-box { display: flex; flex-direction: column; }
.points-price { color: #ee7203; font-weight: 900; font-size: 22px; line-height: 1; }
.points-price .unit { font-size: 14px; margin-right: 2px; }
.card-original-price { text-decoration: line-through; color: #94a3b8; font-size: 12px; margin-top: 4px; }

/* 按钮样式优化 */
.add-btn { box-shadow: 0 4px 10px rgba(0, 129, 99, 0.2); transition: all 0.2s; }
.add-btn:hover:not(.is-disabled-style) { transform: scale(1.05); }
.is-disabled-style {
  background-color: #f1f5f9 !important; border-color: #f1f5f9 !important;
  color: #94a3b8 !important; cursor: not-allowed !important; box-shadow: none !important;
}
.disabled-text { font-size: 16px; font-weight: bold; }

/* --- 弹窗美化 --- */
.detail-wrapper { margin: -20px -25px; /* 抵消 el-dialog 默认 padding */ }
.product-hero { height: 220px; background: linear-gradient(135deg, #008163 0%, #00a884 100%); display: flex; align-items: center; justify-content: center; position: relative; }
.hero-expired { background: linear-gradient(135deg, #64748b 0%, #94a3b8 100%); filter: grayscale(1); }
.floating-img { width: 150px; height: 150px; filter: drop-shadow(0 10px 15px rgba(0,0,0,0.2)); transition: transform 0.3s; }
.floating-img:hover { transform: translateY(-5px); }

.product-info-sheet { padding: 24px; background: white; border-radius: 20px 20px 0 0; margin-top: -20px; position: relative; }
.p-name { font-size: 18px; font-weight: 800; color: #1e293b; margin: 0 0 12px 0; line-height: 1.4; }
.p-price-row { display: flex; align-items: baseline; gap: 8px; }
.p-price { font-size: 32px; color: #ee7203; font-weight: 900; }
.p-original { text-decoration: line-through; color: #94a3b8; font-size: 16px; }

.p-tags-container { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 8px; }
.dialog-action-bar { padding: 16px 24px 24px; background: white; display: flex; gap: 12px; }
.cancel-btn { flex: 1; border-radius: 12px; font-weight: bold; }
.main-action-btn { flex: 2; border-radius: 12px; font-weight: bold; box-shadow: 0 4px 12px rgba(0, 129, 99, 0.2); }

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(226, 35, 26, 0.4); }
  70% { box-shadow: 0 0 0 6px rgba(226, 35, 26, 0); }
  100% { box-shadow: 0 0 0 0 rgba(226, 35, 26, 0); }
}
</style>
