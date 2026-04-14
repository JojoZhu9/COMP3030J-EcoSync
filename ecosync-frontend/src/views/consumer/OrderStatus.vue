<template>
  <div class="order-container">
    <div class="nav-bar">
      <span class="nav-title">订单中心</span>
    </div>

    <div class="sticky-tabs">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="全部分类" name="ALL" />
        <el-tab-pane label="等待发货" name="PENDING" />
        <el-tab-pane label="正在配送" name="DELIVERING" />
        <el-tab-pane label="已完结" name="COMPLETED" />
      </el-tabs>
    </div>

    <div class="list-wrapper" v-loading="loading">
      <div v-if="filteredOrders.length > 0">
        <div v-for="order in filteredOrders" :key="order.orderId" class="order-tile">
          <div class="tile-header">
            <span class="store-tag">门店 ID:{{ order.storeId }}</span>
            <span :class="['status-dot', order.status]">{{ getStatusLabel(order.status) }}</span>
          </div>

          <div class="tile-content">
            <div class="product-preview">
              <div class="id-badge">单号:{{ order.orderId }}</div>
              <div class="content-text">
                <p class="addr-line"><el-icon><Location /></el-icon>{{ order.deliveryAddress }}</p>
                <p class="phone-line"><el-icon><Phone /></el-icon>{{ order.contactPhone }}</p>
              </div>
            </div>

            <div class="items-mini-list" v-if="order.items && order.items.length > 0">
              <div v-for="item in order.items" :key="item.itemId" class="mini-item">
                <span>商品 ID #{{ item.productId }}</span>
                <span>x{{ item.quantity }}</span>
              </div>
            </div>
          </div>

          <div class="tile-footer">
            <span class="create-time">{{ formatDate(order.createdAt) }}</span>
            <div class="price-info">
              实付 <span class="total-num">{{ order.totalPointsSpent }}</span> 积分
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="还没有相关订单记录" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Location, Phone } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeTab = ref('ALL')
const allOrders = ref<any[]>([])
const loading = ref(false)
const userId = localStorage.getItem('userId') || 4

const fetchOrders = async () => {
  loading.value = true
  try {
    const res: any = await request.get(`/orders/user/${userId}`)
    allOrders.value = res.data || res || []
  } catch (e) {
    console.error('API Error')
  } finally {
    loading.value = false
  }
}

const filteredOrders = computed(() => {
  if (activeTab.value === 'ALL') return allOrders.value
  return allOrders.value.filter(o => o.status === activeTab.value)
})

const getStatusLabel = (s: string) => ({
  PENDING: '待处理', DELIVERING: '配送中', COMPLETED: '已收货', CANCELLED: '已关闭'
}[s] || s)

const formatDate = (d: any) => d ? new Date(d).toLocaleString().split(' ')[0] : '-'

onMounted(fetchOrders)
</script>

<style scoped>
.order-container { background: #f4f6f8; min-height: 100vh; }
.nav-bar { padding: 15px 20px; background: white; border-bottom: 1px solid #eee; }
.nav-title { font-weight: bold; font-size: 18px; color: #1e293b; }
.sticky-tabs { background: white; padding: 0 10px; margin-bottom: 12px; }
.custom-tabs :deep(.el-tabs__header) { margin: 0; }
.list-wrapper { padding: 0 12px 30px; }
.order-tile { background: white; border-radius: 12px; padding: 16px; margin-bottom: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }
.tile-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.store-tag { font-size: 14px; font-weight: bold; color: #64748b; }
.status-dot { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.status-dot.PENDING { background: #fff7ed; color: #ea580c; }
.status-dot.DELIVERING { background: #eff6ff; color: #3b82f6; }
.status-dot.COMPLETED { background: #f0fdf4; color: #16a34a; }
.tile-content { background: #f8fafc; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
.product-preview { display: flex; gap: 12px; align-items: flex-start; margin-bottom: 10px; }
.id-badge { background: #e2e8f0; padding: 2px 6px; border-radius: 4px; font-size: 11px; color: #475569; }
.addr-line, .phone-line { font-size: 13px; color: #666; margin: 0 0 4px 0; display: flex; align-items: center; gap: 4px; }
.items-mini-list { border-top: 1px solid #e2e8f0; padding-top: 8px; }
.mini-item { display: flex; justify-content: space-between; font-size: 12px; color: #94a3b8; margin-bottom: 4px; }
.tile-footer { display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
.create-time { color: #94a3b8; }
.total-num { color: #f59e0b; font-size: 18px; font-weight: 800; }
</style>
