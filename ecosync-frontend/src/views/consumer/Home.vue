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
                  <div class="expiry-timer-tag" :class="{ 'expired-bg': isExpired(prod.expirationTime) }">
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
                      <span class="num">{{ prod.discountedPrice }}</span>
                    </div>
                    <div v-if="prod.discountedPrice < prod.normalPrice" class="card-original-price">
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
                    <el-icon v-if="!getBtnStatus(prod).disabled"><Plus /></el-icon>
                    {{ getBtnStatus(prod).text }}
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
      width="460px"
      center
      append-to-body
    >
      <div v-if="currentProduct" class="detail-wrapper">
        <div class="product-hero" :class="{ 'expired-hero': isExpired(currentProduct.expirationTime) }">
          <div class="hero-bg"></div>
          <el-image :src="getImageUrl(currentProduct.barcode)" fit="contain" class="floating-img">
            <template #error><el-icon :size="100" class="floating-icon"><Goods /></el-icon></template>
          </el-image>
          <div class="eco-badge">
            <el-icon style="margin-right:4px"><Star /></el-icon> 7-Eco Premium
          </div>
          <div v-if="isExpired(currentProduct.expirationTime)" class="expired-overlay">OFF SHELF</div>
        </div>

        <div class="product-info-sheet">
          <div class="info-header">
            <h2 class="p-name">{{ currentProduct.productName }}</h2>
            <div class="p-price-row">
              <span class="p-price">¥{{ currentProduct.discountedPrice }}</span>
              <span v-if="currentProduct.discountedPrice < currentProduct.normalPrice" class="p-original">
                ¥{{ Number(currentProduct.normalPrice).toFixed(2) }}
              </span>
            </div>
          </div>

          <div class="p-tags-container">
            <el-tag effect="dark" type="success" round>Eco Choice</el-tag>
            <el-tag :type="isExpired(currentProduct.expirationTime) ? 'danger' : 'warning'" effect="plain" round>
              {{ getTimeRemaining(currentProduct.expirationTime) }}
            </el-tag>
          </div>

          <div class="p-specs-grid">
            <div class="spec-card">
              <span class="spec-label">Nutrition Grade</span>
              <span class="spec-val">Grade A</span>
            </div>
            <div class="spec-card">
              <span class="spec-label">Storage</span>
              <span class="spec-val">Cold Store</span>
            </div>
            <div class="spec-card">
              <span class="spec-label">Carbon Save</span>
              <span class="spec-val">-0.45kg</span>
            </div>
          </div>

          <div class="p-description-card">
            <h4 class="card-inner-title">Logistics & Origin</h4>
            <div class="desc-item">
              <el-icon><Shop /></el-icon>
              <span>Located at: <b>{{ storeList.find(s => s.storeId === selectedStoreId)?.storeName }}</b></span>
            </div>
            <div class="desc-item">
              <el-icon><Timer /></el-icon>
              <span>Final Expiration: {{ new Date(currentProduct.expirationTime).toLocaleString() }}</span>
            </div>
          </div>

          <div class="p-inventory-bar">
            <div class="bar-label">
              <span>Stock availability</span>
              <span class="stock-num">{{ getRemaining(currentProduct) }} units left</span>
            </div>
            <el-progress
              :percentage="Math.min((getRemaining(currentProduct) / 20) * 100, 100)"
              :status="getRemaining(currentProduct) < 5 ? 'exception' : 'success'"
              :show-text="false"
              stroke-width="12"
            />
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
            <el-icon style="margin-right: 8px"><ShoppingCartFull /></el-icon>
            {{ getBtnStatus(currentProduct).text }}
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

// 1. 修复：静态资源路径。SpringBoot 映射 static 到根。
const getImageUrl = (barcode: string) => {
  if (!barcode) return '';
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  return `${baseUrl}/uploads/products/${barcode}.jpg`;
}

// 判断是否过期
const isExpired = (expiryDate: string) => {
  if (!expiryDate) return true;
  return new Date().getTime() > new Date(expiryDate).getTime();
}

// 统一获取按钮状态
const getBtnStatus = (prod: any) => {
  if (!prod) return { type: 'info', text: 'Loading...', disabled: true };
  if (isExpired(prod.expirationTime)) return { type: 'info', text: 'Item Expired', disabled: true };
  if (getRemaining(prod) <= 0) return { type: 'info', text: 'Limit Reached', disabled: true };
  return { type: 'success', text: 'Add to My Basket', disabled: false };
}

const getDiscountRate = (expirationTime: string, discountRatesStr: string): number => {
  try {
    const rates: number[] = JSON.parse(discountRatesStr)
    if (!Array.isArray(rates) || rates.length === 0) return 1.0
    const hoursLeft = (new Date(expirationTime).getTime() - Date.now()) / (1000 * 60 * 60)
    const step = Math.max(0, 12 - Math.ceil(hoursLeft))
    return Number(rates[Math.min(step, rates.length - 1)]) || 1.0
  } catch { return 1.0 }
}

const getTimeRemaining = (expiryDate: string) => {
  if (!expiryDate) return 'N/A';
  const diff = new Date(expiryDate).getTime() - new Date().getTime();
  if (diff <= 0) return 'Expired';
  const hours = Math.floor(diff / (1000 * 60 * 60));
  return hours < 24 ? `${hours}h left` : `${Math.floor(hours / 24)}d left`;
}

const getRemaining = (prod: any) => Number(prod.remainingStock || 0)

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
        const rate = getDiscountRate(item.expirationTime, stdData.discountRates || '[]')
        return {
          ...item,
          productName: stdData.productName,
          normalPrice: stdData.normalPrice,
          discountedPrice: (stdData.normalPrice * rate).toFixed(2)
        }
      } catch { return item }
    }))
    productList.value = enriched.filter(i => i.status === 'AVAILABLE')
  } catch (e) { ElMessage.error('Load Items Failed') }
  finally { loading.value = false }
}

const addToCart = async (prod: any) => {
  if (isExpired(prod.expirationTime)) {
    return ElMessage.error('Cannot purchase expired items.');
  }
  const userId = localStorage.getItem('userId') || '12'
  try {
    await request.post('/cart', {
      userId: Number(userId),
      productId: Number(prod.productId),
      quantity: 1
    })
    const target = productList.value.find(p => p.productId === prod.productId)
    if (target) target.remainingStock--
    ElMessage.success(`${prod.productName} added!`)
  } catch (e) { ElMessage.error('Add failed') }
}

const showDetail = (prod: any) => { currentProduct.value = prod; detailVisible.value = true; }
const handleDetailBuy = () => { if (currentProduct.value) addToCart(currentProduct.value); }
const handleStoreChange = (val: number) => { localStorage.setItem('lastStoreId', String(val)); fetchProducts(); }
onMounted(fetchStores)
</script>

<style scoped>
/* 原有基础样式保持 */
.home-container { background: #f8fafc; min-height: 100vh; }
.header-filter { padding: 12px 24px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); box-shadow: 0 4px 20px rgba(0,0,0,0.03); }
.store-module { display: flex; align-items: center; gap: 12px; background: #f1f5f9; padding: 6px 16px; border-radius: 12px; min-width: 350px; }
.product-grid { padding: 30px 20px; max-width: 1200px; margin: 0 auto; }
.product-card { margin-bottom: 24px; border-radius: 20px; cursor: pointer; transition: all 0.3s ease; }
.product-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.08); }
.image-box { height: 160px; position: relative; border-bottom: 1px solid #f1f5f9; background: white; }

.expiry-timer-tag { position: absolute; top: 8px; right: 8px; background: #ef4444; color: white; padding: 2px 8px; border-radius: 12px; font-size: 11px; font-weight: 800; display: flex; align-items: center; gap: 4px; z-index: 3; }
.expired-bg { background: #64748b !important; }

/* 详情页深度美化 */
:deep(.modern-detail-dialog) { border-radius: 28px; overflow: hidden; border: none; }
:deep(.modern-detail-dialog .el-dialog__header) { display: none; }
:deep(.modern-detail-dialog .el-dialog__body) { padding: 0; background: white; }

.product-hero { height: 260px; background: linear-gradient(135deg, #008163 0%, #004b3a 100%); position: relative; display: flex; justify-content: center; align-items: center; overflow: hidden; }
.expired-hero { background: linear-gradient(135deg, #475569 0%, #1e293b 100%); }
.expired-overlay { position: absolute; font-size: 48px; font-weight: 900; color: rgba(255,255,255,0.2); transform: rotate(-15deg); border: 4px solid rgba(255,255,255,0.2); padding: 0 20px; }

.floating-img { width: 180px; height: 180px; z-index: 2; filter: drop-shadow(0 15px 30px rgba(0,0,0,0.3)); animation: float 4s ease-in-out infinite; }

.product-info-sheet { padding: 28px; }
.p-name { font-size: 26px; font-weight: 900; color: #1e293b; margin-bottom: 10px; }
.p-price { font-size: 36px; font-weight: 900; color: #ee7203; }
.p-original { text-decoration: line-through; color: #94a3b8; font-size: 16px; margin-left: 10px; }

.p-specs-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 24px 0; }
.spec-card { background: #f8fafc; padding: 12px 8px; border-radius: 14px; display: flex; flex-direction: column; align-items: center; border: 1px solid #f1f5f9; }
.spec-label { font-size: 10px; color: #94a3b8; text-transform: uppercase; margin-bottom: 4px; font-weight: 700; }
.spec-val { font-size: 13px; color: #1e293b; font-weight: 800; }

.p-description-card { background: #f0fdf4; padding: 18px; border-radius: 20px; margin-bottom: 24px; border: 1px dashed #bbf7d0; }
.card-inner-title { margin: 0 0 12px 0; font-size: 14px; color: #166534; font-weight: 800; }
.desc-item { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #334155; margin-bottom: 10px; }

.dialog-action-bar { padding: 0 28px 30px; }
.main-action-btn { width: 100%; height: 58px; border-radius: 20px; font-size: 17px; font-weight: 800; border: none; box-shadow: 0 8px 25px rgba(0,0,0,0.1); }

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }
</style>
