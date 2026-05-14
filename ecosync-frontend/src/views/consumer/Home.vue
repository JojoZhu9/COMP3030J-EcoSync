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
        <p class="section-subtitle">Real-time dynamic pricing • Only for items within 12h of expiry</p>
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
                :class="{ 'card-expired': isExpired(prod.expirationTime) }"
                shadow="never"
                @click="showDetail(prod)"
              >
                <div v-if="isExpired(prod.expirationTime)" class="expired-overlay">EXPIRED</div>

                <div class="image-box">
                  <el-image :src="getImageUrl(prod)" fit="cover" style="width: 100%; height: 100%">
                    <template #error><el-icon :size="48" color="#e2e8f0"><Goods /></el-icon></template>
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
                    :type="getBtnConfig(prod).type"
                    class="add-btn"
                    :class="{ 'is-disabled-style': getBtnConfig(prod).disabled }"
                    round
                    @click.stop="addToCart(prod)"
                    :disabled="getBtnConfig(prod).disabled"
                  >
                    <el-icon v-if="!getBtnConfig(prod).disabled"><Plus /></el-icon>
                    {{ getBtnConfig(prod).text }}
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>

      <el-empty v-if="!loading && productList.length === 0" description="No available items." />
    </div>

    <el-dialog v-model="detailVisible" width="420px" center append-to-body class="modern-detail-dialog">
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
          <el-divider />
          <div class="p-tags-container">
            <el-tag :type="isExpired(currentProduct.expirationTime) ? 'info' : 'danger'" effect="dark">
              {{ isExpired(currentProduct.expirationTime) ? 'ALREADY EXPIRED' : getTimeRemaining(currentProduct.expirationTime) }}
            </el-tag>
          </div>
        </div>
        <div class="dialog-action-bar">
          <el-button
            :type="getBtnConfig(currentProduct).type"
            class="main-action-btn"
            style="width: 100%"
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

const storeList = ref<any[]>([])
const selectedStoreId = ref<number | null>(null)
const productList = ref<any[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentProduct = ref<any>(null)

// --- 核心逻辑修复区 ---

/**
 * 1. 严格价格计算：判定12小时临期界限
 */
const calculateDisplayPrice = (normalPrice: number, expirationTime: string, ratesStr: string) => {
  const now = Date.now()
  const exp = new Date(expirationTime).getTime()
  const diffMs = exp - now
  const hoursLeft = diffMs / (1000 * 60 * 60)

  // 情况 A: 已经过期或距离过期超过12小时 -> 必须原价
  if (hoursLeft <= 0 || hoursLeft > 12) {
    return normalPrice.toFixed(2)
  }

  // 情况 B: 0 < hoursLeft <= 12 -> 执行阶梯打折
  try {
    const rates = JSON.parse(ratesStr)
    if (!Array.isArray(rates) || rates.length === 0) return normalPrice.toFixed(2)

    // 计算索引：12小时窗口内的倒计时索引
    // 12h -> index 0, 1h -> index 11
    const index = Math.floor(12 - hoursLeft)
    const safeIndex = Math.max(0, Math.min(index, rates.length - 1))

    return (normalPrice * rates[safeIndex]).toFixed(2)
  } catch (e) {
    return normalPrice.toFixed(2)
  }
}

/**
 * 2. 状态判定
 */
const isExpired = (expiryDate: string) => {
  if (!expiryDate) return true
  return new Date(expiryDate).getTime() <= Date.now()
}

const getBtnConfig = (prod: any) => {
  if (!prod) return { type: 'info', text: 'Loading', disabled: true }

  // 优先级1：过期
  if (isExpired(prod.expirationTime)) {
    return { type: 'info', text: 'Expired', disabled: true }
  }

  // 优先级2：售罄
  if (Number(prod.remainingStock || 0) <= 0) {
    return { type: 'info', text: 'Out of Stock', disabled: true }
  }

  // 优先级3：正常
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

// --- 数据加载 ---

const fetchProducts = async () => {
  if (!selectedStoreId.value) return
  loading.value = true
  try {
    const res: any = await expiringApi.getByStore(selectedStoreId.value)
    const rawItems = res.data || res || []

    const enriched = await Promise.all(rawItems.map(async (item: any) => {
      try {
        // 获取标准表信息以拿到原价和折扣数组
        const std: any = await standardApi.getByBarcode(item.barcode)
        const stdData = std.data || std

        // 兼容处理：后端字段名可能不统一 (normalPrice vs normal_price)
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

    // 只显示还未被物理删除且状态正常的商品
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
const handleDetailBuy = () => {
  if (currentProduct.value) addToCart(currentProduct.value)
}

onMounted(fetchStores)
</script>

<style scoped>
.home-container { background: #f8fafc; min-height: 100vh; padding-bottom: 40px; }
.header-filter { padding: 12px 24px; background: #fff; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.store-module { display: flex; align-items: center; gap: 10px; background: #f1f5f9; padding: 5px 15px; border-radius: 10px; width: fit-content; }
.product-grid { padding: 20px; max-width: 1200px; margin: 0 auto; }

/* 卡片过期样式 */
.product-card { border-radius: 15px; overflow: hidden; position: relative; transition: all 0.3s; }
.card-expired { opacity: 0.6; filter: grayscale(1); cursor: not-allowed !important; }
.expired-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.4); color: white; display: flex;
  align-items: center; justify-content: center; z-index: 10;
  font-weight: 900; font-size: 24px; letter-spacing: 2px;
}

.image-box { height: 160px; background: #fff; position: relative; }
.expiry-timer-tag {
  position: absolute; top: 8px; right: 8px; background: #e2231a;
  color: #fff; padding: 2px 8px; border-radius: 10px; font-size: 11px; font-weight: bold;
}
.bg-gray { background: #64748b !important; }

.content { padding: 15px; }
.name { font-weight: 800; font-size: 14px; height: 36px; display: block; }
.points-price { color: #ee7203; font-weight: 900; font-size: 20px; }
.card-original-price { text-decoration: line-through; color: #94a3b8; font-size: 12px; }

.add-btn { width: 100%; margin-top: 10px; font-weight: bold; }
/* 强制灰色按钮样式 */
.is-disabled-style {
  background-color: #e2e8f0 !important;
  border-color: #e2e8f0 !important;
  color: #94a3b8 !important;
  cursor: not-allowed !important;
}

.product-hero { height: 180px; background: #008163; display: flex; align-items: center; justify-content: center; }
.hero-expired { background: #64748b; }
.floating-img { width: 120px; height: 120px; }
.p-price { font-size: 28px; color: #ee7203; font-weight: 900; }
.p-original { text-decoration: line-through; color: #94a3b8; margin-left: 10px; }
</style>
