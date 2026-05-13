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
                      <div class="image-error-slot">
                        <el-icon :size="48" color="#e2e8f0"><Goods /></el-icon>
                        <span class="error-tip">No Image</span>
                      </div>
                    </template>
                  </el-image>

                  <div class="stock-tag" :class="{ 'urgent': getRemaining(prod) < 5 }">
                    Only {{ getRemaining(prod) }} left
                  </div>
                  <div class="expiry-timer-tag">
                    <el-icon><Timer /></el-icon>
                    {{ getTimeRemaining(prod.expirationTime || prod.expirationDate) }}
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
                    <div v-if="Number(prod.discountedPrice) < Number(prod.normalPrice)" class="card-original-price">
                      ¥{{ Number(prod.normalPrice).toFixed(2) }}
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

    <el-dialog v-model="detailVisible" width="420px" center append-to-body class="modern-detail-dialog">
      <div v-if="currentProduct" class="detail-wrapper">
        <div class="product-hero">
          <el-image :src="getImageUrl(currentProduct.barcode)" fit="contain" class="floating-img">
            <template #error><el-icon :size="100" color="white"><Goods /></el-icon></template>
          </el-image>
        </div>
        <div class="product-info-sheet">
          <h2 class="p-name">{{ currentProduct.productName }}</h2>
          <div class="p-price-row">
            <span class="p-price">¥{{ currentProduct.discountedPrice }}</span>
            <span class="p-original">¥{{ Number(currentProduct.normalPrice).toFixed(2) }}</span>
          </div>
          <div class="p-tags-container">
            <el-tag type="danger" effect="light">
              Expires: {{ getTimeRemaining(currentProduct.expirationTime || currentProduct.expirationDate) }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { LocationFilled, Goods, Plus, Timer } from '@element-plus/icons-vue'
import { storeApi } from '@/api/store'
import { expiringApi, standardApi } from '@/api/product'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const storeList = ref([])
const selectedStoreId = ref(null)
const productList = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const currentProduct = ref(null)

/**
 * 核心修改：图片读取函数
 * 确保你的图片放在前端项目的：/public/uploads/products/ 目录下
 */
const getImageUrl = (barcode: string) => {
  if (!barcode) return '';
  // 直接通过根路径访问 public 文件夹下的资源
  return `/uploads/products/${barcode}.jpg`;
}

/**
 * 核心修改：计算倒计时
 */
const getTimeRemaining = (expiryDate: string) => {
  if (!expiryDate) return 'N/A';
  const now = new Date().getTime();
  const end = new Date(expiryDate).getTime();
  const diff = end - now;

  if (diff <= 0) return 'Expired';

  const mins = Math.floor(diff / (1000 * 60));
  const hrs = Math.floor(mins / 60);
  const days = Math.floor(hrs / 24);

  if (days > 0) return `${days}d left`;
  if (hrs > 0) return `${hrs}h ${mins % 60}m left`;
  return `${mins}m left`;
}

// 计算折扣逻辑
const getDiscountRate = (expirationTime: string, discountRatesStr: string): number => {
  try {
    const rates = JSON.parse(discountRatesStr || '[1.0]')
    const hoursLeft = (new Date(expirationTime).getTime() - Date.now()) / (1000 * 60 * 60)
    const index = Math.min(Math.max(0, 12 - Math.ceil(hoursLeft)), rates.length - 1)
    return rates[index] || 1.0
  } catch { return 1.0 }
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
        const normalPrice = Number(stdData.normalPrice || 0)

        // 兼容字段名: expirationTime 或 expirationDate
        const time = item.expirationTime || item.expirationDate
        const rate = getDiscountRate(time, stdData.discountRates)

        return {
          ...item,
          productName: stdData.productName,
          normalPrice: normalPrice,
          discountedPrice: (normalPrice * rate).toFixed(2)
        }
      } catch { return item }
    }))
    productList.value = enriched.filter(i => i.status === 'AVAILABLE')
  } catch (e) {
    ElMessage.error('Product sync failed')
  } finally {
    loading.value = false
  }
}

const addToCart = async (prod: any) => {
  const userId = localStorage.getItem('userId') || '4'
  try {
    await request.post('/cart', {
      userId: Number(userId),
      productId: Number(prod.productId),
      quantity: 1
    })
    ElMessage.success('Added to basket')
  } catch (e) { ElMessage.error('Add failed') }
}

const handleStoreChange = (val) => {
  selectedStoreId.value = val;
  fetchProducts();
}

const showDetail = (prod) => {
  currentProduct.value = prod;
  detailVisible.value = true;
}

const getRemaining = (prod: any) => Number(prod.remainingStock || 0)
const isAtLimit = (prod: any) => getRemaining(prod) <= 0

onMounted(async () => {
  try {
    const res: any = await storeApi.getAll()
    storeList.value = res.data || res || []
    if (storeList.value.length > 0) {
      selectedStoreId.value = storeList.value[0].storeId
      fetchProducts()
    }
  } catch (e) { ElMessage.error('Init failed') }
})
</script>

<style scoped>
.home-container { background: #f8fafc; min-height: 100vh; }
.image-box {
  height: 180px;
  background: #fff;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
}
.image-error-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #cbd5e1;
}
.error-tip { font-size: 12px; margin-top: 8px; }

.expiry-timer-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(239, 68, 68, 0.9);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stock-tag {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 700;
}
.stock-tag.urgent { color: #ef4444; background: #fee2e2; }

/* ... 其他样式保持原有美化 ... */
</style>
