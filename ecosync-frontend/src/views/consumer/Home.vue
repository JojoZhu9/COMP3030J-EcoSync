<template>
  <div class="home-container">
    <el-affix :offset="0">
      <div class="header-filter">
        <div class="header-content">
          <!-- 店铺选择 -->
          <div class="store-module">
            <div class="store-indicator">
              <el-icon class="icon-pulse"><LocationFilled /></el-icon>
              <span class="label">{{ $t('consumer.home.store') }}</span>
            </div>
            <el-select
              v-model="selectedStoreId"
              :placeholder="$t('consumer.home.selectStore')"
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
            <div class="store-name-display" v-if="currentStoreName">
              <el-icon><Shop /></el-icon>
              <span>{{ currentStoreName }}</span>
            </div>
          </div>

          <!-- 最近店铺 -->
          <div v-if="nearestStore" class="nearest-store-bar">
            <el-tag effect="dark" type="success" round class="nearest-tag">
              <el-icon><MapLocation /></el-icon>
              {{ $t('consumer.home.nearest') }} <strong>{{ nearestStore.storeName }}</strong>
              <span class="dist">{{ nearestStore.distance.toFixed(1) }} {{ $t('consumer.home.km') }}</span>
            </el-tag>
            <el-button link type="primary" size="small" @click="openMapDialog">{{ $t('consumer.home.viewMap') }}</el-button>
          </div>
          <div v-else class="nearest-store-bar">
            <el-button size="small" round type="success" plain @click="findNearestStore">
              <el-icon><MapLocation /></el-icon> {{ $t('consumer.home.findNearestStore') }}
            </el-button>
          </div>

          <!-- 搜索栏 -->
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              :placeholder="$t('consumer.home.searchPlaceholder')"
              clearable
              :prefix-icon="Search"
              class="search-input"
            />
          </div>

          <!-- 过滤器区域：时间区间 + 库存筛选 -->
          <div class="filter-area">
            <!-- 时间区间过滤器 -->
            <div class="time-filters">
              <div
                v-for="filter in timeFilters"
                :key="filter.value"
                :class="['time-chip', { active: selectedTimeFilter === filter.value }]"
                @click="selectedTimeFilter = selectedTimeFilter === filter.value ? '' : filter.value"
              >
                <el-icon :size="14"><component :is="filter.icon" /></el-icon>
                <span>{{ filter.label }}</span>
              </div>
            </div>
            <!-- 库存筛选开关 -->
            <div
              :class="['stock-chip', { active: inStockOnly }]"
              @click="inStockOnly = !inStockOnly"
            >
              <el-icon :size="14"><Box /></el-icon>
              <span>{{ $t('consumer.home.inStockOnly') }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-affix>

    <div class="product-grid">
      <div class="grid-header">
        <h3 class="section-title">
          <span class="brand-text">7-Eleven</span> {{ $t('consumer.home.ecoFlashTitle').replace('7-Eleven ', '') }}
        </h3>
        <p class="section-subtitle">{{ $t('consumer.home.ecoFlashSubtitle') }}</p>
        <p class="result-count" v-if="filteredProducts.length !== productList.length">
          {{ $t('consumer.home.showingResults', { count: filteredProducts.length, total: productList.length }) }}
          <span v-if="selectedTimeFilter">{{ ' ' + $t('consumer.home.filterSuffix', { label: timeFilters.find(f => f.value === selectedTimeFilter)?.label }) }}</span>
          <span v-if="searchKeyword.trim()">{{ ' ' + $t('consumer.home.searchSuffix', { keyword: searchKeyword }) }}</span>
        </p>
      </div>

      <el-skeleton :loading="loading" animated :count="8">
        <template #default>
          <el-row :gutter="20">
            <el-col
              :xs="24" :sm="12" :md="8" :lg="6"
              v-for="prod in filteredProducts"
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
                  <span class="expired-text">{{ $t('consumer.home.expired') }}</span>
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
                    {{ getRemaining(prod) > 0 ? $t('consumer.home.onlyXLeft', { count: getRemaining(prod) }) : $t('consumer.home.soldOut') }}
                  </div>

                  <div class="expiry-timer-tag" :class="{ 'bg-gray': isExpired(prod.expirationTime), 'bg-urgent': getExpiryHours(prod.expirationTime) <= 1 && !isExpired(prod.expirationTime) }">
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
                      <span v-else class="disabled-text">{{ getBtnConfig(prod).text === $t('consumer.home.outOfStock') ? '0' : '-' }}</span>
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>

      <el-empty v-if="!loading && filteredProducts.length === 0" :description="$t('consumer.home.noAvailableItems')" />
    </div>

    <!-- 地图弹窗 -->
    <el-dialog v-model="mapDialogVisible" :title="$t('consumer.home.storeLocation')" width="600px" class="modern-dialog"
      destroy-on-close
    >
      <div v-if="mapStore" class="map-body"
      >
        <p class="map-store-name"
        >{{ mapStore.storeName }}
        </p>
        <p class="map-store-addr"
        >{{ mapStore.address }}, {{ mapStore.city }}
        </p>
        <iframe
          v-if="mapStore.latitude && mapStore.longitude"
          width="100%" height="350" frameborder="0" scrolling="no"
          :src="`https://www.openstreetmap.org/export/embed.html?bbox=${mapStore.longitude-0.01}%2C${mapStore.latitude-0.01}%2C${mapStore.longitude+0.01}%2C${mapStore.latitude+0.01}&layer=mapnik&marker=${mapStore.latitude}%2C${mapStore.longitude}`"
          style="border-radius: 12px; border: 1px solid #e2e8f0;"
        >
        </iframe>
        <el-empty v-else :description="$t('consumer.home.noCoordinates')" />
      </div>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" width="420px" center append-to-body class="modern-detail-dialog" :show-close="false">
      <div v-if="currentProduct" class="detail-wrapper">
        <div class="product-hero-full" :class="{ 'hero-expired': isExpired(currentProduct.expirationTime) }">
          <el-image :src="getImageUrl(currentProduct)" fit="cover" class="hero-image-full" />
          <div v-if="isExpired(currentProduct.expirationTime)" class="hero-expired-overlay">
            <span class="hero-expired-text">{{ $t('consumer.home.expired') }}</span>
          </div>
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
              {{ isExpired(currentProduct.expirationTime) ? $t('consumer.home.alreadyExpired') : getTimeRemaining(currentProduct.expirationTime) }}
            </el-tag>
            <el-tag type="warning" effect="plain" round v-if="getRemaining(currentProduct) > 0">
              {{ $t('consumer.home.stockLabel', { count: getRemaining(currentProduct) }) }}
            </el-tag>
          </div>
          <div class="product-info-sheet">
            <h2 class="p-name">{{ currentProduct.productName }}</h2>
            <div class="p-tags-container">
            </div>

            <div class="p-barcode-section">
              <svg ref="detailBarcodeRef"></svg>
              <div class="p-barcode-num">{{ currentProduct.barcode }}</div>
            </div>
          </div>
        </div>
        <div class="dialog-action-bar">
          <el-button plain @click="detailVisible = false" class="cancel-btn">{{ $t('common.cancel') }}</el-button>
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
import {ref, onMounted, computed, nextTick} from 'vue'
import { LocationFilled, Goods, Plus, Shop, Timer, Search, Clock, Calendar, MapLocation, Box } from '@element-plus/icons-vue'
import { storeApi } from '@/api/store'
import { expiringApi, standardApi } from '@/api/product'
import { cartApi } from '@/api/cart'
import request from '@/utils/request'
import { ElMessage } from '@/utils/message'
import { useI18n } from 'vue-i18n'
import JsBarcode from 'jsbarcode'

const { t } = useI18n()

const storeList = ref<any[]>([])
const selectedStoreId = ref<number | null>(null)
const productList = ref<any[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentProduct = ref<any>(null)
const detailBarcodeRef = ref<HTMLElement | null>(null)
const nearestStore = ref<any>(null)
const mapDialogVisible = ref(false)
const mapStore = ref<any>(null)

// 过滤器
const searchKeyword = ref('')
const selectedTimeFilter = ref('')   // 时间区间
const inStockOnly = ref(true)        // 默认只显示有库存的商品

// 时间区间过滤选项
const timeFilters = computed(() => [
  { value: '1h', label: t('consumer.home.timeFilter1h'), icon: Timer },
  { value: '5h', label: t('consumer.home.timeFilter5h'), icon: Clock },
  { value: '10h', label: t('consumer.home.timeFilter10h'), icon: Clock },
  { value: '1d', label: t('consumer.home.timeFilter1d'), icon: Calendar }
])

const currentStoreName = computed(() => {
  const store = storeList.value.find(s => s.storeId === selectedStoreId.value)
  return store?.storeName || ''
})

const getExpiryHours = (expiryDate: string): number => {
  const diff = new Date(expiryDate).getTime() - Date.now()
  return diff / (1000 * 60 * 60)
}

// 过滤器叠加生效
const filteredProducts = computed(() => {
  let result = productList.value

  // 1. 时间区间过滤
  if (selectedTimeFilter.value) {
    result = result.filter(p => {
      const hours = getExpiryHours(p.expirationTime)
      switch (selectedTimeFilter.value) {
        case '1h': return hours > 0 && hours <= 1
        case '5h': return hours > 0 && hours <= 5
        case '10h': return hours > 0 && hours <= 10
        case '1d': return hours > 24
        default: return true
      }
    })
  }

  // 2. 名称搜索过滤
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(p =>
      (p.productName || '').toLowerCase().includes(kw) ||
      (p.barcode || '').toLowerCase().includes(kw)
    )
  }

  // 3. 库存筛选
  if (inStockOnly.value) {
    result = result.filter(p => getRemaining(p) > 0)
  }

  return result
})

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
  if (!prod) return { type: 'info', text: t('consumer.home.loading'), disabled: true }
  if (isExpired(prod.expirationTime)) return { type: 'info', text: t('consumer.home.expired'), disabled: true }
  if (Number(prod.remainingStock || 0) <= 0) return { type: 'info', text: t('consumer.home.outOfStock'), disabled: true }
  return { type: 'success', text: t('consumer.home.addToBasket'), disabled: false }
}

const getTimeRemaining = (expiryDate: string) => {
  const diff = new Date(expiryDate).getTime() - Date.now()
  if (diff <= 0) return t('consumer.home.expired')
  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  if (h >= 24) return t('consumer.home.daysLeft', { days: Math.floor(h/24) })
  if (h > 0) return t('consumer.home.hoursMinsLeft', { h, m })
  return t('consumer.home.minsLeft', { m })
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
    const now = new Date()
    productList.value = enriched.filter(i => i.status === 'AVAILABLE' && new Date(i.expirationTime) > now)
  } catch (e) {
    ElMessage.error(t('consumer.home.productSyncFailed'))
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
  } catch (e) { ElMessage.error(t('consumer.home.storeSyncFailed')) }
}

const addToCart = async (prod: any) => {
  if (isExpired(prod.expirationTime)) return ElMessage.warning(t('consumer.home.itemExpiredWarning'))
  const userId = localStorage.getItem('userId') || '4'
  try {
    const cartRes: any = await cartApi.getByUserId(Number(userId))
    const cartItems = cartRes.data || cartRes || []
    const existingItem = cartItems.find((item: any) => item.productId === prod.productId)
    const existingQty = existingItem ? Number(existingItem.quantity || 0) : 0
    const stock = Number(prod.remainingStock || 0)
    if (existingQty >= stock) {
      return ElMessage.warning(t('consumer.home.stockLimitWarning', { stock, existing: existingQty }))
    }
    await request.post('/cart', {
      userId: Number(userId),
      productId: Number(prod.productId),
      quantity: 1
    })
    ElMessage.success(t('consumer.home.addedToBasket'))
  } catch (e: any) {
    if (e?.message?.includes('Only')) return
    ElMessage.error(t('consumer.home.addFailed'))
  }
}

const handleStoreChange = (val: number) => {
  localStorage.setItem('lastStoreId', String(val))
  searchKeyword.value = ''
  selectedTimeFilter.value = ''
  fetchProducts()
}

const showDetail = (prod: any) => {
  currentProduct.value = prod
  detailVisible.value = true

  // 👇 弹窗打开后，等 DOM 渲染完毕立刻画条码
  nextTick(() => {
    if (detailBarcodeRef.value && prod.barcode) {
      JsBarcode(detailBarcodeRef.value, prod.barcode, {
        format: "CODE128",
        displayValue: false,
        height: 35,
        width: 1.5,
        lineColor: "#1e293b",
        background: "transparent",
        margin: 0
      })
    }
  })
}

const handleDetailBuy = () => { if (currentProduct.value) addToCart(currentProduct.value) }

const toRad = (deg: number) => deg * Math.PI / 180

const haversine = (lat1: number, lon1: number, lat2: number, lon2: number) => {
  const R = 6371
  const dLat = toRad(lat2 - lat1)
  const dLon = toRad(lon2 - lon1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2
  return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

const findNearestStore = () => {
  if (!navigator.geolocation) {
    ElMessage.warning(t('consumer.home.geoNotSupported'))
    return
  }
  ElMessage.info(t('consumer.home.locating'))
  navigator.geolocation.getCurrentPosition((pos) => {
    const userLat = pos.coords.latitude
    const userLng = pos.coords.longitude
    let best: Record<string, unknown> | null = null
    let bestDist = Infinity
    for (const s of storeList.value) {
      const lat = s.latitude
      const lng = s.longitude
      if (lat == null || lng == null) continue
      const d = haversine(userLat, userLng, lat, lng)
      if (d < bestDist) {
        bestDist = d
        best = { ...s, distance: d }
      }
    }
    if (best) {
      nearestStore.value = best
      ElMessage.success(t('consumer.home.nearestStoreFound', { name: best.storeName, dist: bestDist.toFixed(1) }))
    } else {
      ElMessage.warning(t('consumer.home.noStoreWithCoords'))
    }
  }, () => {
    ElMessage.error(t('consumer.home.locationError'))
  })
}

const openMapDialog = () => {
  mapStore.value = nearestStore.value
  mapDialogVisible.value = true
}

onMounted(fetchStores)
</script>

<style scoped>
.home-container { background: #f4f6f8; min-height: 100vh; padding-bottom: 40px; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }

.header-filter { padding: 12px 24px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(8px); border-bottom: 1px solid #e2e8f0; }
.header-content { max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; gap: 12px; }

.store-module { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.store-indicator { display: flex; align-items: center; gap: 6px; color: #008163; font-weight: bold; }
.store-name-display {
  display: flex; align-items: center; gap: 6px;
  background: linear-gradient(135deg, #008163, #006b52);
  color: white; padding: 6px 14px; border-radius: 20px;
  font-weight: 800; font-size: 13px;
  box-shadow: 0 2px 8px rgba(0, 129, 99, 0.2);
}

.search-bar { width: 100%; max-width: 400px; }
.search-input :deep(.el-input__wrapper) {
  background: #f8fafc; box-shadow: none !important;
  border: 1px solid #e2e8f0; border-radius: 12px;
  padding: 4px 12px;
}
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #008163; box-shadow: 0 0 0 3px rgba(0, 129, 99, 0.1) !important;
}

/* 过滤器区域：时间区间 */
.filter-area {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* 时间区间过滤器 */
.time-filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.time-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 20px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.time-chip:hover {
  background: #e2e8f0;
  color: #475569;
}
.time-chip.active {
  background: #e2231a;
  color: white;
  border-color: #e2231a;
  box-shadow: 0 2px 8px rgba(226, 35, 26, 0.2);
}
/* > 1d 特殊颜色 */
.time-chip.active[data-value="1d"] {
  background: #3b82f6;
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

/* 库存筛选开关 */
.stock-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 20px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.stock-chip:hover {
  background: #e2e8f0;
  color: #475569;
}
.stock-chip.active {
  background: #008163;
  color: white;
  border-color: #008163;
  box-shadow: 0 2px 8px rgba(0, 129, 99, 0.2);
}

.product-grid { padding: 24px; max-width: 1200px; margin: 0 auto; }
.grid-header { margin-bottom: 24px; }
.section-title { font-size: 24px; font-weight: 900; color: #1e293b; margin: 0 0 4px 0; }
.brand-text { color: #008163; }
.section-subtitle { color: #64748b; font-size: 14px; margin: 0; }
.result-count { color: #94a3b8; font-size: 12px; font-weight: 600; margin-top: 8px; }

.card-col { margin-bottom: 20px; }
.product-card { border-radius: 16px; border: none; overflow: hidden; position: relative; transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.2s; background: #fff; cursor: pointer; }
.product-card:hover:not(.card-expired) { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.08) !important; }
.card-expired { opacity: 0.65; filter: grayscale(0.8); cursor: not-allowed !important; }

.expired-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255,255,255,0.6); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center; z-index: 10;
}
.expired-text {
  background: #1e293b; color: white; padding: 6px 16px; border-radius: 20px;
  font-weight: 900; font-size: 14px; letter-spacing: 2px; transform: rotate(-10deg);
}

.image-box { height: 180px; background: #f8fafc; position: relative; display: flex; align-items: center; justify-content: center; }
.product-img { width: 100%; height: 100%; transition: transform 0.3s; }
.product-card:hover .product-img { transform: scale(1.05); }
.image-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f1f5f9; }

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
.bg-urgent { background: #dc2626 !important; box-shadow: 0 2px 8px rgba(220, 38, 38, 0.4) !important; }

.content { padding: 16px; }
.title-row { margin-bottom: 12px; }
.name {
  font-weight: 700; font-size: 15px; color: #334155; line-height: 1.4;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; height: 42px;
}

.price-action-row { display: flex; justify-content: space-between; align-items: flex-end; }
.price-box { display: flex; flex-direction: column; }
.points-price { color: #ee7203; font-weight: 900; font-size: 22px; line-height: 1; }
.points-price .unit { font-size: 14px; margin-right: 2px; }
.card-original-price { text-decoration: line-through; color: #94a3b8; font-size: 12px; margin-top: 4px; }

.add-btn { box-shadow: 0 4px 10px rgba(0, 129, 99, 0.2); transition: all 0.2s; }
.add-btn:hover:not(.is-disabled-style) { transform: scale(1.05); }
.is-disabled-style {
  background-color: #f1f5f9 !important; border-color: #f1f5f9 !important;
  color: #94a3b8 !important; cursor: not-allowed !important; box-shadow: none !important;
}
.disabled-text { font-size: 16px; font-weight: bold; }

/* 最近店铺 */
.nearest-store-bar { display: flex; align-items: center; gap: 10px; }
.nearest-tag { font-weight: 800; display: flex; align-items: center; gap: 6px; }
.nearest-tag .dist { opacity: 0.8; font-weight: 600; }

/* 地图弹窗 */
.map-body { padding: 10px 0; }
.map-store-name { font-size: 16px; font-weight: 800; color: #1e293b; margin: 0 0 6px 0; }
.map-store-addr { font-size: 13px; color: #64748b; margin: 0 0 16px 0; }
.modern-dialog :deep(.el-dialog) { border-radius: 16px; }

/* 详情弹窗 */
.modern-detail-dialog :deep(.el-dialog) { border-radius: 20px; overflow: hidden; padding: 0; }
.modern-detail-dialog :deep(.el-dialog__header) { display: none; }
.modern-detail-dialog :deep(.el-dialog__body) { padding: 0; }

.detail-wrapper { margin: 0; }

.product-hero-full {
  width: 100%; height: 280px;
  background: linear-gradient(135deg, #008163 0%, #00a884 100%);
  position: relative; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.hero-expired {
  background: linear-gradient(135deg, #64748b 0%, #94a3b8 100%);
  filter: grayscale(1);
}

.hero-image-full {
  width: 100%; height: 100%;
  object-fit: cover;
}

.hero-expired-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255,255,255,0.5); backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center;
}
.hero-expired-text {
  background: #1e293b; color: white;
  padding: 10px 24px; border-radius: 24px;
  font-weight: 900; font-size: 18px; letter-spacing: 3px;
  transform: rotate(-8deg);
}

.product-info-sheet { padding: 24px; background: white; position: relative; }
.p-name { font-size: 20px; font-weight: 800; color: #1e293b; margin: 0 0 12px 0; line-height: 1.4; }
.p-price-row { display: flex; align-items: baseline; gap: 8px; }
.p-price { font-size: 32px; color: #ee7203; font-weight: 900; }
.p-original { text-decoration: line-through; color: #94a3b8; font-size: 16px; }

.p-tags-container { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 8px; }

.dialog-action-bar { padding: 16px 24px 24px; background: white; display: flex; gap: 12px; }
.cancel-btn { flex: 1; border-radius: 12px; font-weight: bold; }
.main-action-btn { flex: 2; border-radius: 12px; font-weight: bold; box-shadow: 0 4px 12px rgba(0, 129, 99, 0.2); }

.p-barcode-section {
  margin-top: 24px;
  text-align: center;
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
}
.p-barcode-num {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  letter-spacing: 4px;
  color: #64748b;
  margin-top: 4px;
  font-weight: bold;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(226, 35, 26, 0.4); }
  70% { box-shadow: 0 0 0 6px rgba(226, 35, 26, 0); }
  100% { box-shadow: 0 0 0 0 rgba(226, 35, 26, 0); }
}
</style>
