<template>
  <div class="home-container">
    <el-affix :offset="0">
      <div class="header-filter">
        <div class="store-selector">
          <el-icon color="#67C23A" :size="20"><LocationFilled /></el-icon>
          <span class="label">当前门店：</span>
          <el-select v-model="selectedStoreId" placeholder="请选择门店" @change="handleStoreChange" style="width: 200px">
            <el-option v-for="item in storeList" :key="item.storeId" :label="item.storeName" :value="item.storeId" />
          </el-select>
        </div>
        <div class="user-info">
          <el-tag type="warning">我的积分: {{ userPoints }}</el-tag>
        </div>
      </div>
    </el-affix>

    <div class="product-grid">
      <el-skeleton :loading="loading" animated :count="4">
        <template #default>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="prod in productList" :key="prod.productId">
              <el-card class="product-card" shadow="hover" @click="showDetail(prod)">
                <div class="image-box">
                  <el-icon :size="50" color="#DCDFE6"><Goods /></el-icon>
                </div>
                <div class="content">
                  <div class="title-row">
                    <span class="name">{{ prod.productName }}</span>
                    <el-tag size="small" type="danger">剩 {{ prod.remainingStock }} 件</el-tag>
                  </div>
                  <div class="price-box">
                    <span class="points">{{ Math.round((prod.normalPrice || 0) * 10) }} 积分</span>
                    <span class="mkt-price">￥{{ prod.normalPrice }}</span>
                  </div>
                  <el-button type="primary" class="buy-btn" round @click.stop="addToCart(prod)">
                    加入购物车
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
      title="商品详情"
      width="90%"
      style="max-width: 500px; border-radius: 16px"
      center
    >
      <div v-if="currentProduct" class="detail-dialog-content">
        <div class="detail-img-placeholder">
          <el-icon :size="80" color="#DCDFE6"><Goods /></el-icon>
        </div>
        <div class="detail-info">
          <h2 class="d-title">{{ currentProduct.productName }}</h2>
          <div class="d-tags">
            <el-tag type="success" effect="plain">正品保证</el-tag>
            <el-tag type="warning" effect="plain">临期优惠</el-tag>
          </div>
          <p class="d-desc">该商品目前在 <b>{{ storeList.find(s => s.storeId === selectedStoreId)?.storeName }}</b> 门店有售。请在有效期内尽快兑换使用。环保先行，悦享生活。</p>

          <div class="d-meta">
            <div class="meta-item">
              <span class="m-label">兑换积分</span>
              <span class="m-value orange">{{ Math.round((currentProduct.normalPrice || 0) * 10) }}</span>
            </div>
            <div class="meta-item">
              <span class="m-label">剩余库存</span>
              <span class="m-value">{{ currentProduct.remainingStock }} 件</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" round @click="detailVisible = false" style="width: 40%">返回</el-button>
          <el-button size="large" type="primary" round @click="handleDetailBuy" style="width: 50%">
            立即加入购物车
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { LocationFilled, Goods } from '@element-plus/icons-vue'
import { storeApi } from '@/api/store'
import { expiringApi, standardApi } from '@/api/product'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const storeList = ref<any[]>([])
const selectedStoreId = ref<number | null>(null)
const productList = ref<any[]>([])
const loading = ref(false)
const userPoints = ref(localStorage.getItem('points') || 0)

// 详情弹窗相关状态
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
  } catch (e) { ElMessage.error('加载门店失败') }
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
        } catch { return { ...item, productName: `商品(${item.barcode})`, normalPrice: 0 } }
      }))
      productList.value = results.map((r: any) => r.value).filter(i => i.status === 'AVAILABLE')
    }
  } finally { loading.value = false }
}

// 展示弹窗逻辑
const showDetail = (prod: any) => {
  currentProduct.value = prod
  detailVisible.value = true
}

// 弹窗内的购买逻辑
const handleDetailBuy = () => {
  if (currentProduct.value) {
    addToCart(currentProduct.value)
    detailVisible.value = false
  }
}

const addToCart = async (prod: any) => {
  try {
    const userId = localStorage.getItem('userId') || 4
    // 1. 同步后端
    try { await request.post('/cart', { userId: Number(userId), productId: prod.productId, quantity: 1 }) } catch (err) {}

    // 2. 写入本地缓存
    let localCart = JSON.parse(localStorage.getItem('cart') || '[]')
    const pPrice = Math.round((prod.normalPrice || 0) * 10)
    const index = localCart.findIndex((i: any) => i.productId === prod.productId)

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
    ElMessage.success('已加入购物车')
  } catch (e) { ElMessage.error('操作失败') }
}

const handleStoreChange = () => {
  localStorage.setItem('lastStoreId', String(selectedStoreId.value))
  fetchProducts()
}
onMounted(fetchStores)
</script>

<style scoped>
.home-container { background: #f5f7fa; min-height: 100vh; }
.header-filter { display: flex; justify-content: space-between; align-items: center; padding: 15px 30px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.06); z-index: 10; }
.product-grid { padding: 20px 30px; }
.product-card { margin-bottom: 20px; border-radius: 12px; cursor: pointer; transition: transform 0.2s; }
.product-card:hover { transform: translateY(-4px); }
.image-box { height: 120px; background: #fafafa; display: flex; justify-content: center; align-items: center; }
.content { padding: 15px; }
.title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: bold; }
.points { font-size: 20px; color: #e6a23c; font-weight: 800; }
.mkt-price { font-size: 12px; color: #999; text-decoration: line-through; margin-left: 8px; }
.buy-btn { width: 100%; font-weight: bold; }

/* 弹窗样式 */
.detail-dialog-content { padding: 10px; }
.detail-img-placeholder { height: 160px; background: #f8f9fb; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-bottom: 20px; }
.d-title { margin: 0 0 10px 0; font-size: 20px; color: #1e293b; }
.d-tags { margin-bottom: 15px; display: flex; gap: 8px; }
.d-desc { font-size: 14px; color: #64748b; line-height: 1.6; margin-bottom: 20px; }
.d-meta { background: #f8fafc; padding: 15px; border-radius: 10px; display: flex; justify-content: space-around; }
.meta-item { display: flex; flex-direction: column; align-items: center; }
.m-label { font-size: 12px; color: #94a3b8; margin-bottom: 4px; }
.m-value { font-size: 18px; font-weight: bold; color: #334155; }
.orange { color: #f59e0b; }
.dialog-footer { display: flex; justify-content: space-between; padding-top: 10px; }
</style>
