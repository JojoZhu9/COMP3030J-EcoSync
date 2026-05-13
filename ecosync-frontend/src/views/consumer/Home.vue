<template>
  <div class="home-container">
    <el-affix :offset="0">
      <div class="header-filter">
        <div class="header-content">
          <div class="store-module">
            <div class="store-indicator">
              <el-icon class="icon-pulse"><LocationFilled /></el-icon>
              <span class="label">Current Store</span>
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
                    :src="`/uploads/products/${prod.barcode}.jpg`"
                    fit="cover"
                    style="width: 100%; height: 100%"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon :size="48" color="#e2e8f0"><Goods /></el-icon>
                        <span class="error-text">No Image</span>
                      </div>
                    </template>
                  </el-image>

                  <div class="stock-tag" :class="{ 'urgent': prod.remainingStock < 5 }">
                    Only {{ prod.remainingStock }} left
                  </div>

                  <div class="expiry-timer-tag">
                    <el-icon><Timer /></el-icon>
                    {{ formatTimeRemaining(prod.expirationTime || prod.expirationDate) }}
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
                    <div class="card-original-price">
                      ¥{{ Number(prod.normalPrice).toFixed(2) }}
                    </div>
                  </div>

                  <el-button
                    type="success"
                    class="add-btn"
                    round
                    @click.stop="addToCart(prod)"
                    :disabled="prod.remainingStock <= 0"
                  >
                    <el-icon><Plus /></el-icon> Add to Basket
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-skeleton>

      <el-empty v-if="!loading && productList.length === 0" description="No available items." />
    </div>

    <el-dialog v-model="detailVisible" width="400px" center append-to-body custom-class="modern-dialog">
      <div v-if="currentProduct" class="detail-wrapper">
        <div class="product-hero">
          <el-image :src="`/uploads/products/${currentProduct.barcode}.jpg`" fit="contain" />
        </div>
        <div class="product-info-sheet">
          <h2 class="p-name">{{ currentProduct.productName }}</h2>
          <div class="p-price-row">
            <span class="p-price">¥{{ currentProduct.discountedPrice }}</span>
            <span class="p-original">¥{{ currentProduct.normalPrice }}</span>
          </div>
          <el-tag type="danger" effect="dark">
            Expires: {{ formatTimeRemaining(currentProduct.expirationTime || currentProduct.expirationDate) }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { LocationFilled, Goods, Plus, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const storeList = ref([])
const selectedStoreId = ref(null)
const productList = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentProduct = ref(null)

// 1. 获取倒计时文字 (修复逻辑)
const formatTimeRemaining = (expiryDate: any) => {
  if (!expiryDate) return 'Calculating...'

  const end = new Date(expiryDate).getTime()
  const now = new Date().getTime()
  const diff = end - now

  if (diff <= 0) return 'Expired'

  const mins = Math.floor(diff / (1000 * 60))
  const hrs = Math.floor(mins / 60)

  if (hrs > 24) return `${Math.floor(hrs / 24)}d left`
  if (hrs > 0) return `${hrs}h ${mins % 60}m left`
  return `${mins}m left`
}

// 2. 加载数据
const fetchProducts = async () => {
  if (!selectedStoreId.value) return
  loading.value = true
  try {
    // 获取特价库存记录
    const expRes: any = await request.get(`/expiring-products/store/${selectedStoreId.value}`)
    const expiringItems = expRes.data || expRes || []

    // 获取标准产品库信息（为了拿到原价和名字）
    const libRes: any = await request.get('/products')
    const library = libRes.data || libRes || []

    const enriched = expiringItems.map(item => {
      const std = library.find(p => String(p.barcode) === String(item.barcode))
      return {
        ...item,
        productName: std?.productName || 'Unknown Product',
        normalPrice: std?.normalPrice || 0,
        // 假设折扣价是原价的 0.7 倍，具体可根据业务逻辑调整
        discountedPrice: (Number(std?.normalPrice || 0) * 0.7).toFixed(2)
      }
    })

    productList.value = enriched.filter(i => i.status === 'AVAILABLE')
  } catch (e) {
    ElMessage.error('Failed to sync products')
  } finally {
    loading.value = false
  }
}

const addToCart = async (prod: any) => {
  try {
    await request.post('/cart', {
      userId: Number(localStorage.getItem('userId') || 1),
      productId: prod.productId,
      quantity: 1
    })
    ElMessage.success('Added to Basket')
  } catch (e) {
    ElMessage.error('Add failed')
  }
}

const handleStoreChange = (val) => {
  selectedStoreId.value = val
  fetchProducts()
}

const showDetail = (prod) => {
  currentProduct.value = prod
  detailVisible.value = true
}

onMounted(async () => {
  try {
    const res: any = await request.get('/stores')
    storeList.value = res.data || res || []
    if (storeList.value.length > 0) {
      selectedStoreId.value = storeList.value[0].storeId
      fetchProducts()
    }
  } catch (e) {
    ElMessage.error('Init failed')
  }
})
</script>

<style scoped>
.home-container { background: #f4f7f6; min-height: 100vh; padding-bottom: 50px; }
.header-filter { background: white; padding: 15px 25px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.store-module { display: flex; align-items: center; gap: 12px; }
.store-indicator { display: flex; align-items: center; gap: 5px; color: #008163; font-weight: 800; }
.icon-pulse { animation: pulse 2s infinite; }

.product-grid { padding: 25px; max-width: 1300px; margin: 0 auto; }
.section-title { font-size: 24px; color: #2c3e50; margin: 0; }
.section-subtitle { color: #7f8c8d; margin: 5px 0 20px 0; }

.product-card {
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 25px;
  transition: transform 0.3s;
  cursor: pointer;
  border: none !important;
}
.product-card:hover { transform: translateY(-5px); }

.image-box { height: 200px; background: white; position: relative; }
.image-placeholder { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; background: #f8fafc; color: #cbd5e1; }

.expiry-timer-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(231, 76, 60, 0.9);
  color: white;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 2;
}

.stock-tag {
  position: absolute;
  bottom: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 800;
  color: #2c3e50;
}
.stock-tag.urgent { color: #e74c3c; background: #ffdada; }

.content { padding: 15px; }
.name { font-size: 16px; font-weight: 700; color: #2c3e50; display: block; height: 44px; overflow: hidden; }
.price-box { display: flex; align-items: baseline; gap: 8px; margin: 10px 0; }
.points-price { color: #e67e22; font-weight: 800; font-size: 20px; }
.card-original-price { text-decoration: line-through; color: #bdc3c7; font-size: 13px; }

.add-btn { width: 100%; font-weight: bold; background: #008163 !important; border: none !important; }

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}
</style>
