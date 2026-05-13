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
                      <div class="img-error">
                        <el-icon :size="48" color="#cbd5e1"><Goods /></el-icon>
                        <span class="error-text">Image Unavailable</span>
                      </div>
                    </template>
                  </el-image>

                  <div class="stock-tag" :class="{ 'urgent': getRemaining(prod) < 5 }">
                    Only {{ getRemaining(prod) }} left
                  </div>
                  <div class="expiry-timer-tag" :class="{ 'is-expired': isExpired(prod.expirationTime) }">
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
          <div class="hero-bg-pattern"></div>
          <el-image
            :src="getImageUrl(currentProduct.barcode)"
            fit="contain"
            class="floating-img"
          >
            <template #error>
              <el-icon :size="100" class="floating-icon"><Goods /></el-icon>
            </template>
          </el-image>
          <div class="eco-badge-overlay">
            <el-icon><StarFilled /></el-icon> 7-Eco Quality Assurance
          </div>
          <div v-if="isExpired(currentProduct.expirationTime)" class="expired-stamp">OUT OF DATE</div>
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

          <div class="p-grid-specs">
            <div class="spec-item">
              <span class="spec-label">STATUS</span>
              <span class="spec-value" :class="isExpired(currentProduct.expirationTime) ? 'red' : 'green'">
                {{ isExpired(currentProduct.expirationTime) ? 'Expired' : 'Fresh' }}
              </span>
            </div>
            <div class="spec-item">
              <span class="spec-label">SAVES</span>
              <span class="spec-value green">-{{ Math.round((1 - currentProduct.discountedPrice/currentProduct.normalPrice)*100) }}% OFF</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">ORIGIN</span>
              <span class="spec-value">Local Hub</span>
            </div>
          </div>

          <div class="p-info-card">
            <div class="card-line">
              <el-icon><Location /></el-icon>
              <span>Located at: <b>{{ storeList.find(s => s.storeId === selectedStoreId)?.storeName }}</b></span>
            </div>
            <div class="card-line">
              <el-icon><Timer /></el-icon>
              <span :class="{ 'text-expired': isExpired(currentProduct.expirationTime) }">
                Expiry: {{ new Date(currentProduct.expirationTime).toLocaleString() }}
              </span>
            </div>
          </div>

          <div class="p-stock-control">
            <div class="control-label">
              <span>Availability</span>
              <span class="stock-count">{{ getRemaining(currentProduct) }} left</span>
            </div>
            <el-progress
              :percentage="Math.min((getRemaining(currentProduct)/20)*100, 100)"
              :status="getRemaining(currentProduct) < 5 ? 'exception' : 'success'"
              :show-text="false"
              stroke-width="15"
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
  LocationFilled, Goods, Plus, Location, Timer,
  StarFilled, ShoppingCartFull
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

// 修正后的图片读取逻辑
const getImageUrl = (barcode: string) => {
  if (!barcode) return '';
  // 根据截图：static 是根，后面跟 uploads/products/
  return `https://csi420-02-vm9.ucd.ie/api/uploads/products/${barcode}.jpg`;
}

// 严格过期判定
const isExpired = (expiryDate: string) => {
  if (!expiryDate) return true;
  return new Date().getTime() > new Date(expiryDate).getTime();
}

// 统一按钮状态管理
const getBtnStatus = (prod: any) => {
  if (!prod) return { type: 'info', text: 'Loading...', disabled: true };
  if (isExpired(prod.expirationTime)) return { type: 'info', text: 'Item Expired', disabled: true };
  if (getRemaining(prod) <= 0) return { type: 'info', text: 'Limit Reached', disabled: true };
  return { type: 'success', text: 'Add to My Basket', disabled: false };
}

const getDiscountRate = (expirationTime: string, discountRatesStr: string): number => {
  try {
    const rates: number[] = JSON.parse(discountRatesStr)
    const hoursLeft = (new Date(expirationTime).getTime() - Date.now()) / (1000 * 60 * 60)
    const index = Math.min(Math.max(0, 12 - Math.ceil(hoursLeft)), rates.length - 1)
    return Number(rates[index]) || 1.0
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
    return ElMessage.warning('Cannot add expired items to basket.');
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
.home-container { background: #f8fafc; min-height: 100vh; }
.header-filter { padding: 12px 24px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.store-module { display: flex; align-items: center; gap: 12px; background: #fff; border: 1px solid #e2e8f0; padding: 6px 16px; border-radius: 12px; width: 400px; }
.store-indicator { display: flex; align-items: center; gap: 8px; color: #64748b; font-weight: 800; font-size: 11px; }
.icon-pulse { color: #059669; animation: pulse 2s infinite; }

.product-grid { padding: 30px 20px; max-width: 1240px; margin: 0 auto; }
.product-card { border-radius: 20px; border: 1px solid #f1f5f9; cursor: pointer; transition: all 0.3s ease; }
.product-card:hover { transform: translateY(-5px); box-shadow: 0 12px 30px rgba(0,0,0,0.08); }

.image-box { height: 180px; position: relative; background: #fff; overflow: hidden; border-bottom: 1px solid #f1f5f9; }
.img-error { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; background: #f8fafc; }
.error-text { font-size: 11px; color: #94a3b8; font-weight: 700; margin-top: 8px; }

.expiry-timer-tag { position: absolute; top: 10px; right: 10px; background: #ef4444; color: #fff; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 800; z-index: 2; display: flex; align-items: center; gap: 4px; }
.expiry-timer-tag.is-expired { background: #64748b; }
.stock-tag { position: absolute; bottom: 10px; right: 10px; background: #e2e8f0; font-size: 10px; padding: 2px 8px; border-radius: 8px; font-weight: 800; z-index: 2; }
.stock-tag.urgent { background: #fee2e2; color: #ef4444; }

.content { padding: 16px; }
.name { font-weight: 800; font-size: 16px; color: #1e293b; height: 42px; line-height: 1.3; margin-bottom: 10px; overflow: hidden; }
.points-price { color: #f97316; font-weight: 900; font-size: 26px; }
.points-price .unit { font-size: 18px; margin-right: 2px; }
.card-original-price { text-decoration: line-through; color: #94a3b8; font-size: 13px; margin-bottom: 12px; }

.add-btn { width: 100%; height: 48px; font-weight: 800; font-size: 14px; border: none; }
.add-btn.el-button--success { background: #059669 !important; box-shadow: 0 4px 12px rgba(5,150,105,0.2); }

/* 详情页深度美化样式 */
:deep(.modern-detail-dialog) { border-radius: 35px; overflow: hidden; border: none; }
:deep(.modern-detail-dialog .el-dialog__header) { display: none; }
:deep(.modern-detail-dialog .el-dialog__body) { padding: 0; background: #fff; }

.product-hero { height: 280px; background: linear-gradient(135deg, #059669 0%, #064e3b 100%); position: relative; display: flex; justify-content: center; align-items: center; overflow: hidden; }
.expired-hero { background: linear-gradient(135deg, #475569 0%, #1e293b 100%); }
.hero-bg-pattern { position: absolute; width: 100%; height: 100%; background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 80%); }
.floating-img { width: 220px; height: 220px; z-index: 2; filter: drop-shadow(0 20px 40px rgba(0,0,0,0.3)); animation: float 4s ease-in-out infinite; }
.eco-badge-overlay { position: absolute; top: 25px; left: 25px; background: rgba(255,255,255,0.2); backdrop-filter: blur(10px); color: #fff; padding: 6px 18px; border-radius: 20px; font-size: 12px; font-weight: 800; z-index: 3; display: flex; align-items: center; gap: 6px; }
.expired-stamp { position: absolute; font-size: 50px; font-weight: 900; color: rgba(255,255,255,0.15); transform: rotate(-15deg); border: 5px solid rgba(255,255,255,0.15); padding: 5px 20px; pointer-events: none; }

.product-info-sheet { padding: 35px; }
.p-name { font-size: 28px; font-weight: 900; color: #1e293b; margin: 0 0 10px 0; }
.p-price { font-size: 40px; font-weight: 900; color: #f97316; }
.p-original { text-decoration: line-through; color: #94a3b8; font-size: 20px; margin: 0 15px; }

.p-grid-specs { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 25px 0; }
.spec-item { background: #f8fafc; padding: 15px 5px; border-radius: 20px; display: flex; flex-direction: column; align-items: center; border: 1px solid #f1f5f9; }
.spec-label { font-size: 10px; color: #94a3b8; font-weight: 800; letter-spacing: 0.5px; margin-bottom: 5px; }
.spec-value { font-size: 13px; color: #1e293b; font-weight: 800; }
.spec-value.green { color: #059669; }
.spec-value.red { color: #ef4444; }

.p-info-card { background: #f0fdf4; border-radius: 22px; padding: 20px; border: 1px dashed #059669; margin-bottom: 25px; }
.card-line { display: flex; align-items: center; gap: 12px; font-size: 14px; color: #374151; margin-bottom: 12px; }
.card-line:last-child { margin-bottom: 0; }
.card-line .el-icon { color: #059669; font-size: 18px; }
.text-expired { color: #ef4444; font-weight: 800; }

.p-stock-control { margin-bottom: 5px; }
.control-label { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px; font-weight: 800; color: #64748b; }
.stock-count { color: #1e293b; }

.dialog-action-bar { padding: 0 35px 35px; }
.main-action-btn { width: 100%; height: 65px; border-radius: 22px; font-size: 19px; font-weight: 900; border: none; box-shadow: 0 12px 30px rgba(5,150,105,0.2); }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.6; } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }
</style>
