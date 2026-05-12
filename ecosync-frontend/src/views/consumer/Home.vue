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
                  <el-image
                    :src="getImageUrl(prod.barcode)"
                    fit="cover"
                    style="width: 100%; height: 100%"
                  >
                    <template #error>
                      <el-icon :size="48" color="#e2e8f0"><Goods /></el-icon>
                    </template>
                  </el-image>

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
                      <span class="unit">¥</span>
                      <span class="num">{{ Number(prod.normalPrice || 0).toFixed(2) }}</span>
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
      class="modern-detail-dialog"
      :show-close="true"
      width="420px"
      center
      append-to-body
    >
      <div v-if="currentProduct" class="detail-wrapper">
        <div class="product-hero">
          <div class="hero-bg"></div>

          <el-image
            :src="getImageUrl(currentProduct.barcode)"
            fit="contain"
            class="floating-img"
          >
            <template #error>
              <el-icon :size="100" class="floating-icon"><Goods /></el-icon>
            </template>
          </el-image>

          <div class="eco-badge">
            <el-icon style="margin-right:4px"><Star /></el-icon> 7-Eco Save
          </div>
        </div>

        <div class="product-info-sheet">
          <div class="info-header">
            <h2 class="p-name">{{ currentProduct.productName }}</h2>
            <div class="p-price-row">
              <span class="p-price">¥{{ Number(currentProduct.normalPrice || 0).toFixed(2) }}</span>
              <span class="p-original">¥{{ (Number(currentProduct.normalPrice || 0) * 1.5).toFixed(2) }}</span>
            </div>
          </div>

          <div class="p-tags-container">
            <el-tag effect="dark" type="success" round size="small">Fresh Pick</el-tag>
            <el-tag effect="plain" type="warning" round size="small">Limited Quantity</el-tag>
          </div>

          <div class="p-description-card">
            <div class="desc-item">
              <el-icon><Shop /></el-icon>
              <span>Available at <b>{{ storeList.find(s => s.storeId === selectedStoreId)?.storeName }}</b></span>
            </div>
            <div class="desc-item">
              <el-icon><Timer /></el-icon>
              <span>Best consumed within 24-48 hours</span>
            </div>
          </div>

          <div class="p-inventory-bar">
            <div class="bar-label">
              <span>Stock Status</span>
              <span class="stock-num">{{ getRemaining(currentProduct) }} units left</span>
            </div>
            <el-progress
              :percentage="Math.min((getRemaining(currentProduct) / 20) * 100, 100)"
              :status="getRemaining(currentProduct) < 5 ? 'exception' : 'success'"
              :show-text="false"
              stroke-width="10"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-action-bar">
          <el-button
            type="success"
            class="main-action-btn"
            :disabled="isAtLimit(currentProduct) || getRemaining(currentProduct) <= 0"
            @click="handleDetailBuy"
          >
            <el-icon style="margin-right: 8px"><ShoppingCartFull /></el-icon>
            {{ isAtLimit(currentProduct) ? 'Purchase Limit Reached' : 'Add to My Basket' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  LocationFilled, Goods, Plus, Shop, Timer,
  Star, ShoppingCartFull
} from '@element-plus/icons-vue'
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

// --- 图片路径拼接函数 ---
// 这里硬编码后端地址，因为浏览器能直接打开这个地址，说明它是通的。
const getImageUrl = (barcode: string) => {
  if (!barcode) return '';
  return `http://localhost:8080/uploads/products/${barcode}.jpg`;
}

const getRemaining = (prod: any) => Number(prod.remainingStock || prod.stock || 0)

const isAtLimit = (prod: any) => {
  if (!prod) return false
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  const inCart = cart.find((i: any) => i.productId === prod.productId)
  return inCart && inCart.quantity >= getRemaining(prod)
}

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
          barcode: item.barcode,
          productName: stdData.productName || `SKU:${item.barcode}`,
          normalPrice: Number(stdData.normalPrice || 0)
        }
      } catch { return item }
    }))
    productList.value = enriched.filter(i => i.status === 'AVAILABLE')
  } catch (e) { ElMessage.error('Load Items Failed') }
  finally { loading.value = false }
}

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
    await request.post('/cart', {
      userId: Number(userId),
      productId: Number(prod.productId),
      quantity: 1
    })

    if (existingIndex > -1) { localCart[existingIndex].quantity += 1 }
    else { localCart.push({ productId: prod.productId, productName: prod.productName, pointsPrice: prod.normalPrice, quantity: 1 }) }

    localStorage.setItem('cart', JSON.stringify(localCart))
    ElMessage.success(`${prod.productName} added!`)
  } catch (e) { ElMessage.error('Add failed') }
}

const showDetail = (prod: any) => {
  currentProduct.value = prod;
  detailVisible.value = true;
}

const handleDetailBuy = () => {
  if (currentProduct.value) {
    addToCart(currentProduct.value);
    detailVisible.value = false;
  }
}

const handleStoreChange = (val: number) => {
  localStorage.setItem('lastStoreId', String(val));
  fetchProducts();
}

onMounted(fetchStores)
</script>

<style scoped>
/* 保持原有样式 */
.floating-img {
  width: 160px;
  height: 160px;
  z-index: 2;
  object-fit: contain;
  filter: drop-shadow(0 10px 20px rgba(0,0,0,0.2));
  animation: float 4s ease-in-out infinite;
}

.home-container { background: #f8fafc; min-height: 100vh; }

.header-filter {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  display: flex;
}
.header-content { width: 100%; max-width: 1200px; margin: 0 auto; display: flex; }

.store-module {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f1f5f9;
  padding: 6px 16px;
  border-radius: 12px;
  min-width: 350px;
}
.store-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  border-right: 1px solid #cbd5e1;
  padding-right: 12px;
}
.icon-pulse { color: #007934; animation: pulse 2s infinite; font-size: 18px; }
.label { font-size: 10px; font-weight: 800; color: #64748b; text-transform: uppercase; }
.store-selector-expanded { flex: 1; }

.product-grid { padding: 30px 20px; max-width: 1200px; margin: 0 auto; }
.section-title { font-size: 22px; font-weight: 900; color: #1e293b; margin: 0; }
.section-subtitle { font-size: 13px; color: #64748b; margin-bottom: 24px; }

.product-card { margin-bottom: 24px; border-radius: 20px; border: 1px solid #f1f5f9; cursor: pointer; transition: all 0.3s ease; overflow: hidden; }
.product-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.05); }

.image-box { height: 160px; background: white; display: flex; justify-content: center; align-items: center; position: relative; border-bottom: 1px solid #f1f5f9; }
.stock-tag { position: absolute; bottom: 8px; right: 8px; font-size: 10px; padding: 3px 10px; background: #e2e8f0; border-radius: 20px; font-weight: 800; z-index: 3; }
.stock-tag.urgent { background: #fee2e2; color: #ef4444; }

.content { padding: 16px; }
.name { font-weight: 800; font-size: 15px; color: #1e293b; height: 40px; display: block; overflow: hidden; }
.points-price { color: #ee7203; font-weight: 900; margin: 12px 0; }
.points-price .num { font-size: 24px; }
.points-price .unit { font-size: 18px; margin-right: 2px; }

.add-btn { width: 100%; height: 44px; font-weight: 800; border: none; }
.add-btn.el-button--success { background: #008163 !important; }

:deep(.modern-detail-dialog) { border-radius: 28px; overflow: hidden; }
:deep(.modern-detail-dialog .el-dialog__header) { display: none; }
:deep(.modern-detail-dialog .el-dialog__body) { padding: 0; }

.product-hero {
  height: 220px;
  background: linear-gradient(135deg, #008163 0%, #004b3a 100%);
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}
.hero-bg { position: absolute; width: 100%; height: 100%; background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 80%); }
.floating-icon { color: white !important; filter: drop-shadow(0 10px 20px rgba(0,0,0,0.2)); animation: float 4s ease-in-out infinite; }
.eco-badge { position: absolute; top: 20px; left: 20px; background: rgba(255,255,255,0.2); backdrop-filter: blur(8px); color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 800; display: flex; align-items: center; z-index: 3; }

.product-info-sheet { padding: 24px; }
.p-name { font-size: 24px; font-weight: 900; color: #1e293b; margin-bottom: 8px; }
.p-price-row { display: flex; align-items: baseline; gap: 10px; margin-bottom: 20px; }
.p-price { font-size: 32px; font-weight: 900; color: #ee7203; }
.p-original { text-decoration: line-through; color: #94a3b8; font-size: 14px; }
.p-tags-container { display: flex; gap: 8px; margin-bottom: 24px; }
.p-description-card { background: #f8fafc; padding: 16px; border-radius: 16px; margin-bottom: 24px; }
.desc-item { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #475569; margin-bottom: 8px; }
.desc-item .el-icon { color: #008163; }

.p-inventory-bar { margin-bottom: 10px; }
.bar-label { display: flex; justify-content: space-between; font-size: 12px; font-weight: 800; margin-bottom: 8px; color: #64748b; }
.stock-num { color: #1e293b; }

.dialog-action-bar { padding: 0 24px 24px; }
.main-action-btn { width: 100%; height: 54px; border-radius: 18px; font-size: 16px; font-weight: 800; background: #008163 !important; border: none; box-shadow: 0 8px 20px rgba(0,129,99,0.2); }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.6; } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
</style>
