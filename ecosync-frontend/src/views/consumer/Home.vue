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
        <h3 class="section-title">Flash Discount Items</h3>
        <p class="section-subtitle">Freshness guaranteed • Limited time eco-savings</p>
      </div>

      <el-skeleton :loading="loading" animated :count="4">
        <template #default>
          <el-row :gutter="20">
            <el-col
              :xs="24" :sm="12" :md="8" :lg="6"
              v-for="prod in productList"
              :key="prod.productId"
            >
              <el-card class="product-card" shadow="never" @click="showDetail(prod)">
                <div class="image-box">
                  <el-icon :size="48" color="#e2e8f0"><Goods /></el-icon>
                  <div class="stock-tag" :class="{ 'urgent': getRemaining(prod) < 5 }">
                    Only {{ getRemaining(prod) }} left
                  </div>
                </div>

                <div class="content">
                  <div class="title-row">
                    <span class="name">{{ prod.productName }}</span>
                  </div>
                  <div class="price-box">
                    <div class="points-price">
                      <span class="num">{{ Number(prod.normalPrice || 0).toFixed(2) }}</span>
                      <span class="unit">元</span>
                    </div>
                  </div>

                  <el-button
                    :type="isAtLimit(prod) ? 'info' : 'success'"
                    class="add-btn"
                    round
                    @click.stop="addToCart(prod)"
                    :disabled="getRemaining(prod) <= 0 || isAtLimit(prod)"
                  >
                    <el-icon v-if="!isAtLimit(prod)"><Plus /></el-icon>
                    {{ isAtLimit(prod) ? 'Limit Reached' : 'Add to Basket' }}
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>

      <el-empty v-if="!loading && productList.length === 0" description="No available items in this store." />
    </div>

    <el-dialog
      v-model="detailVisible"
      class="responsive-dialog"
      :show-close="false"
      center
      append-to-body
    >
      <div v-if="currentProduct" class="detail-container">
        <div class="detail-visual">
          <el-icon :size="80" color="#cbd5e1"><Goods /></el-icon>
          <div class="brand-tag-float">7-Select Eco</div>
        </div>

        <div class="detail-scroll-area">
          <div class="detail-body">
            <div class="detail-header-row">
              <h2 class="d-title">{{ currentProduct.productName }}</h2>
              <div class="d-price-tag">{{ Number(currentProduct.normalPrice || 0).toFixed(2) }} 元</div>
            </div>

            <div class="d-tags">
              <span class="tag-chip green">Certified Fresh</span>
              <span class="tag-chip orange">Eco Discount</span>
            </div>

            <div class="d-info-card">
              <p class="d-desc">
                Currently available at
                <span class="highlight">
                  {{ storeList.find(s => s.storeId === selectedStoreId)?.storeName }}
                </span>.
                Sustainable choice to reduce waste.
              </p>
            </div>

            <div class="d-stats">
              <div class="stat-item">
                <span class="s-label">Condition</span>
                <span class="s-val">Perfect</span>
              </div>
              <div class="stat-item">
                <span class="s-label">Stock</span>
                <span class="s-val">{{ getRemaining(currentProduct) }} Units</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-cancel" @click="detailVisible = false">Close</el-button>
          <el-button
            class="btn-confirm"
            :disabled="isAtLimit(currentProduct)"
            @click="handleDetailBuy"
          >
            {{ isAtLimit(currentProduct) ? 'Limit Reached' : 'Add to Basket' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { LocationFilled, Goods, Plus } from '@element-plus/icons-vue'
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

// 剩餘庫存
const getRemaining = (prod: any) => Number(prod.remainingStock || prod.stock || 0)

// 判斷購貨限額
const isAtLimit = (prod: any) => {
  if (!prod) return false
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  const inCart = cart.find((i: any) => i.productId === prod.productId)
  return inCart && inCart.quantity >= getRemaining(prod)
}

// 獲取店鋪列表
const fetchStores = async () => {
  try {
    const res = await storeApi.getAll() as any
    storeList.value = res.data || res || []
    if (storeList.value.length > 0) {
      const saved = localStorage.getItem('lastStoreId')
      selectedStoreId.value = saved ? Number(saved) : storeList.value[0].storeId
      await fetchProducts()
    }
  } catch (e) { ElMessage.error('Store Sync Failed') }
}

// 獲取商品（聚合）
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
        return {
          ...item,
          productName: stdData.productName || `SKU:${item.barcode}`,
          normalPrice: Number(stdData.normalPrice || 0)
        }
      } catch { return item }
    }))
    productList.value = enriched.filter(i => i.status === 'AVAILABLE')
  } catch (e) { ElMessage.error('Load Items Failed') }
  finally { loading.value = false }
}

// 加入購物車
const addToCart = async (prod: any) => {
  const userId = localStorage.getItem('userId') || '4'
  const maxStock = getRemaining(prod)
  let localCart = JSON.parse(localStorage.getItem('cart') || '[]')
  const existingIndex = localCart.findIndex((i: any) => i.productId === prod.productId)
  const currentQty = existingIndex > -1 ? localCart[existingIndex].quantity : 0

  if (currentQty + 1 > maxStock) {
    return ElMessage.warning({ message: `Limit reached! Only ${maxStock} available.`, plain: true })
  }

  try {
    await request.post('/cart', { userId: Number(userId), productId: Number(prod.productId), quantity: 1 })
    if (existingIndex > -1) { localCart[existingIndex].quantity += 1 }
    else { localCart.push({ productId: prod.productId, productName: prod.productName, pointsPrice: prod.normalPrice, quantity: 1 }) }
    localStorage.setItem('cart', JSON.stringify(localCart))
    ElMessage.success(`${prod.productName} added!`)
  } catch (e) { ElMessage.error('Add failed') }
}

const showDetail = (prod: any) => { currentProduct.value = prod; detailVisible.value = true; }
const handleDetailBuy = () => { if (currentProduct.value) { addToCart(currentProduct.value); detailVisible.value = false; } }
const handleStoreChange = (val: number) => { localStorage.setItem('lastStoreId', String(val)); fetchProducts(); }

onMounted(fetchStores)
</script>

<style scoped>
.home-container { background: #f8fafc; min-height: 100vh; }

/* 頂部吸頂 */
.header-filter {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  display: flex;
}

.header-content {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
}

/* 拉長門店模塊 */
.store-module {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f1f5f9;
  padding: 6px 16px;
  border-radius: 12px;
  /* 增加最小寬度，確保名稱能展開 */
  min-width: 350px;
  max-width: 80%;
}

.store-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  border-right: 1px solid #cbd5e1;
  padding-right: 12px;
  flex-shrink: 0;
}

.icon-pulse { color: #007934; animation: pulse 2s infinite; font-size: 18px; }
.label { font-size: 10px; font-weight: 800; color: #64748b; text-transform: uppercase; }

/* 讓選擇框自動撐開或佔據剩餘空間 */
.store-selector-expanded {
  flex: 1;
}

.store-selector-expanded :deep(.el-input__wrapper) {
  background-color: transparent !important;
  box-shadow: none !important;
  padding-left: 8px;
}

/* 確保文字不被截斷 */
.store-selector-expanded :deep(.el-input__inner) {
  font-weight: 700;
  color: #1e293b;
  font-size: 14px;
  text-overflow: clip; /* 不使用省略號，盡量顯示 */
}

/* 商品卡片與其餘樣式保持一致 */
.product-grid { padding: 30px 20px; max-width: 1200px; margin: 0 auto; }
.section-title { font-size: 22px; font-weight: 900; color: #1e293b; margin: 0; }
.section-subtitle { font-size: 13px; color: #64748b; margin-bottom: 24px; }
.product-card { margin-bottom: 24px; border-radius: 20px; border: 1px solid #f1f5f9; cursor: pointer; transition: all 0.3s ease; }
.product-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.05); }
.image-box { height: 140px; background: #f8fafc; display: flex; justify-content: center; align-items: center; position: relative; border-radius: 20px 20px 0 0; }
.stock-tag { position: absolute; bottom: 8px; right: 8px; font-size: 10px; padding: 3px 10px; background: #e2e8f0; border-radius: 20px; font-weight: 800; }
.stock-tag.urgent { background: #fee2e2; color: #ef4444; }
.content { padding: 16px; }
.name { font-weight: 800; font-size: 15px; color: #1e293b; height: 40px; display: block; overflow: hidden; }
.points-price { color: #f59e0b; font-weight: 900; margin: 12px 0; }
.points-price .num { font-size: 22px; }
.add-btn { width: 100%; height: 44px; font-weight: 800; border: none; }
.add-btn.el-button--success { background: #007934 !important; }

@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.6; } 100% { opacity: 1; } }
</style>
