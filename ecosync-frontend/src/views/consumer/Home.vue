<template>
  <div class="home-container">
    <el-affix :offset="0">
      <div class="header-filter">
        <div class="store-module">
          <div class="store-indicator">
            <el-icon class="icon-pulse"><LocationFilled /></el-icon>
            <span class="label">Nearby Store</span>
          </div>
          <el-select
            v-model="selectedStoreId"
            placeholder="Select Store"
            @change="handleStoreChange"
            class="brand-select"
          >
            <el-option v-for="item in storeList" :key="item.storeId" :label="item.storeName" :value="item.storeId" />
          </el-select>
        </div>
        <div class="points-badge">
          <span class="p-label">My Wallet</span>
          <span class="p-value">{{ userPoints }} <small>pts</small></span>
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
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="prod in productList" :key="prod.productId">
              <el-card class="product-card" shadow="never" @click="showDetail(prod)">
                <div class="image-box">
                  <el-icon :size="48" color="#e2e8f0"><Goods /></el-icon>
                  <div class="stock-tag" :class="{ 'urgent': prod.remainingStock < 5 }">
                    Only {{ prod.remainingStock }} left
                  </div>
                </div>

                <div class="content">
                  <div class="title-row">
                    <span class="name">{{ prod.productName }}</span>
                  </div>
                  <div class="price-box">
                    <div class="points-price">
                      <span class="num">{{ Math.round((prod.normalPrice || 0) * 10) }}</span>
                      <span class="unit">PTS</span>
                    </div>
                    <div class="mkt-price">orig. ¥{{ prod.normalPrice }}</div>
                  </div>
                  <el-button type="success" class="add-btn" round @click.stop="addToCart(prod)">
                    <el-icon><Plus /></el-icon> Add to Basket
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>
    </div>

    <el-dialog
      v-model="detailVisible"
      width="90%"
      class="detail-dialog"
      :show-close="false"
      center
    >
      <div v-if="currentProduct" class="detail-container">
        <div class="detail-visual">
          <el-icon :size="100" color="#cbd5e1"><Goods /></el-icon>
          <div class="brand-tag-float">7-Select Eco</div>
        </div>

        <div class="detail-body">
          <div class="detail-header-row">
            <h2 class="d-title">{{ currentProduct.productName }}</h2>
            <div class="d-price-tag">{{ Math.round((currentProduct.normalPrice || 0) * 10) }} pts</div>
          </div>

          <div class="d-tags">
            <span class="tag-chip green">Certified Fresh</span>
            <span class="tag-chip orange">Last Call Discount</span>
          </div>

          <div class="d-info-card">
            <p class="d-desc">
              Currently available at <span class="highlight">{{ storeList.find(s => s.storeId === selectedStoreId)?.storeName }}</span>.
              By choosing this item, you are helping reduce food waste.
            </p>
          </div>

          <div class="d-stats">
            <div class="stat-item">
              <span class="s-label">Condition</span>
              <span class="s-val">Perfect</span>
            </div>
            <div class="stat-item">
              <span class="s-label">Availability</span>
              <span class="s-val">{{ currentProduct.remainingStock }} Units</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-cancel" @click="detailVisible = false">Close</el-button>
          <el-button class="btn-confirm" @click="handleDetailBuy">
            Add to Basket
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
const userPoints = ref(localStorage.getItem('points') || 0)

const detailVisible = ref(false)
const currentProduct = ref<any>(null)

const fetchStores = async () => {
  try {
    const res = await storeApi.getAll() as any
    storeList.value = res.data || res || []
    if (storeList.value.length > 0) {
      selectedStoreId.value = storeList.value[0].storeId
      localStorage.setItem('lastStoreId', String(selectedStoreId.value))
      await fetchProducts()
    }
  } catch (e) { ElMessage.error('Store Sync Failed') }
}

const fetchProducts = async () => {
  if (!selectedStoreId.value) return
  loading.value = true
  try {
    const res = await expiringApi.getByStore(selectedStoreId.value) as any
    const rawItems = res.data || res
    if (Array.isArray(rawItems)) {
      const results = await Promise.allSettled(rawItems.map(async (item: any) => {
        try {
          const std = await standardApi.getByBarcode(item.barcode) as any
          const stdData = std.data || std
          return { ...item, productName: stdData.productName, normalPrice: stdData.normalPrice }
        } catch { return { ...item, productName: `Unknown SKU(${item.barcode})`, normalPrice: 0 } }
      }))
      productList.value = results
        .filter((r): r is PromiseFulfilledResult<any> => r.status === 'fulfilled')
        .map(r => r.value)
        .filter(i => i.status === 'AVAILABLE')
    }
  } finally { loading.value = false }
}

const showDetail = (prod: any) => {
  currentProduct.value = prod
  detailVisible.value = true
}

const handleDetailBuy = () => {
  if (currentProduct.value) {
    addToCart(currentProduct.value)
    detailVisible.value = false
  }
}

// 修改点：加入了严格的库存判定逻辑
const addToCart = async (prod: any) => {
  // 1. 判断商品本身是否已无库存
  if (prod.remainingStock <= 0) {
    return ElMessage.warning('Sorry, this item is out of stock!')
  }

  let localCart = JSON.parse(localStorage.getItem('cart') || '[]')
  const index = localCart.findIndex((i: any) => i.productId === prod.productId)

  // 2. 判断加入后是否会超出库存上限
  if (index > -1 && localCart[index].quantity >= prod.remainingStock) {
    return ElMessage.warning(`Limit reached! Only ${prod.remainingStock} items available.`)
  }

  try {
    const userId = localStorage.getItem('userId') || 4
    try {
      await request.post('/cart', { userId: Number(userId), productId: prod.productId, quantity: 1 })
    } catch (err) { /* Silent fail for offline-first support */ }

    const pPrice = Math.round((prod.normalPrice || 0) * 10)

    if (index > -1) {
      localCart[index].quantity += 1
    } else {
      localCart.push({
        productId: prod.productId,
        productName: prod.productName,
        pointsPrice: pPrice,
        quantity: 1
      })
    }
    localStorage.setItem('cart', JSON.stringify(localCart))
    ElMessage.success('Added to basket')
  } catch (e) {
    ElMessage.error('Action Failed')
  }
}

const handleStoreChange = () => {
  localStorage.setItem('lastStoreId', String(selectedStoreId.value))
  fetchProducts()
}
onMounted(fetchStores)
</script>

<style scoped>
/* 保持所有样式不变 */
.home-container { background: #f8fafc; min-height: 100vh; }
.header-filter { display: flex; justify-content: space-between; align-items: center; padding: 12px 24px; background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(10px); box-shadow: 0 4px 20px rgba(0,0,0,0.03); z-index: 100; }
.store-module { display: flex; align-items: center; gap: 12px; }
.store-indicator { display: flex; flex-direction: column; }
.store-indicator .label { font-size: 10px; font-weight: 800; color: #94a3b8; text-transform: uppercase; }
.icon-pulse { color: #007934; animation: pulse 2s infinite; }
.brand-select :deep(.el-input__wrapper) { box-shadow: none !important; background: #f1f5f9; border-radius: 10px; }
.points-badge { background: #1e293b; padding: 6px 16px; border-radius: 12px; display: flex; flex-direction: column; align-items: flex-end; }
.p-label { color: #64748b; font-size: 9px; font-weight: 800; text-transform: uppercase; }
.p-value { color: #fff; font-weight: 900; font-size: 16px; }
.p-value small { font-size: 10px; color: #ff7900; }
.product-grid { padding: 30px 24px; max-width: 1200px; margin: 0 auto; }
.grid-header { margin-bottom: 24px; }
.section-title { font-size: 22px; font-weight: 900; color: #1e293b; margin: 0; }
.section-subtitle { font-size: 13px; color: #94a3b8; margin-top: 4px; }
.product-card { margin-bottom: 24px; border-radius: 20px; border: 1px solid #f1f5f9; overflow: hidden; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.product-card:hover { transform: translateY(-6px); box-shadow: 0 12px 30px rgba(0,0,0,0.05); }
.image-box { height: 160px; background: #fcfcfc; display: flex; justify-content: center; align-items: center; position: relative; }
.stock-tag { position: absolute; top: 12px; right: 12px; background: #f1f5f9; padding: 4px 10px; border-radius: 8px; font-size: 10px; font-weight: 800; color: #64748b; }
.stock-tag.urgent { background: #fef2f2; color: #ef4444; }
.content { padding: 20px; }
.name { font-weight: 800; font-size: 16px; color: #1e293b; display: block; height: 44px; overflow: hidden; }
.price-box { display: flex; align-items: baseline; gap: 10px; margin: 15px 0; }
.points-price { color: #ff7900; font-weight: 900; display: flex; align-items: baseline; gap: 2px; }
.points-price .num { font-size: 24px; }
.points-price .unit { font-size: 10px; }
.mkt-price { font-size: 12px; color: #cbd5e1; text-decoration: line-through; }
.add-btn { width: 100%; height: 42px; background-color: #007934 !important; border: none; font-weight: 800; }
:deep(.detail-dialog) { border-radius: 24px !important; overflow: hidden; }
.detail-visual { height: 220px; background: #f8fafc; display: flex; justify-content: center; align-items: center; position: relative; margin: -25px -25px 25px; }
.brand-tag-float { position: absolute; bottom: 20px; left: 20px; background: #007934; color: #fff; padding: 5px 12px; border-radius: 20px; font-size: 10px; font-weight: 900; letter-spacing: 1px; }
.detail-header-row { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.d-title { margin: 0; font-size: 24px; font-weight: 900; color: #1e293b; flex: 1; }
.d-price-tag { font-size: 20px; font-weight: 900; color: #ff7900; }
.d-tags { display: flex; gap: 8px; margin-bottom: 20px; }
.tag-chip { padding: 4px 12px; border-radius: 6px; font-size: 11px; font-weight: 800; text-transform: uppercase; }
.tag-chip.green { background: #f0fdf4; color: #007934; }
.tag-chip.orange { background: #fff7ed; color: #ff7900; }
.d-info-card { background: #f1f5f9; padding: 16px; border-radius: 16px; margin-bottom: 20px; }
.d-desc { margin: 0; font-size: 14px; color: #475569; line-height: 1.6; }
.highlight { font-weight: 800; color: #007934; }
.d-stats { display: flex; border-top: 1px solid #f1f5f9; padding-top: 20px; }
.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.s-label { font-size: 10px; color: #94a3b8; font-weight: 800; text-transform: uppercase; margin-bottom: 4px; }
.s-val { font-size: 15px; font-weight: 800; color: #1e293b; }
.btn-cancel { border-radius: 14px; height: 48px; width: 35%; font-weight: 800; }
.btn-confirm { border-radius: 14px; height: 48px; width: 60%; background: #1e293b !important; color: #fff; font-weight: 800; border: none; }
@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
</style>
