<template>
  <div class="order-page">
    <div class="brand-stripe"></div>
    <div class="order-header">
      <h2 class="title">My Transactions</h2>
      <div class="header-desc">Eco-Saving & Freshness Tracking</div>
    </div>

    <div class="sticky-tabs-container">
      <el-tabs v-model="activeTab" class="brand-tabs" @tab-change="fetchOrders">
        <el-tab-pane label="All" name="ALL" />
        <el-tab-pane label="Pending" name="PENDING" />
        <el-tab-pane label="Shipping" name="DELIVERING" />
        <el-tab-pane label="Finished" name="COMPLETED" />
      </el-tabs>
    </div>

    <div class="list-container" v-loading="loading">
      <div v-if="filteredOrders.length > 0" class="order-feed">
        <div v-for="order in filteredOrders" :key="order.orderId" class="order-card">
          <div class="card-top">
            <div class="store-info">
              <el-icon class="store-icon"><Shop /></el-icon>
              <span class="store-id">NODE-STORE #{{ order.storeId }}</span>
            </div>
            <div :class="['status-pill', order.status]">
              {{ getStatusLabel(order.status) }}
            </div>
          </div>

          <div class="logistics-box">
            <div class="order-meta">
              <span class="ticket-id">TRX-{{ order.orderId }}</span>
              <span class="date-stamp">{{ formatDate(order.createdAt) }}</span>
            </div>

            <div class="address-row">
              <div class="dot-indicator"></div>
              <div class="addr-text">{{ order.deliveryAddress }}</div>
            </div>

            <div class="contact-info">
              <span class="phone-label">
                <el-icon><Iphone /></el-icon> {{ order.contactPhone }}
              </span>
            </div>

            <div class="sku-summary" v-if="order.items?.length">
              <div v-for="item in order.items" :key="item.itemId" class="sku-line">
                <span class="sku-name"><el-icon><Box /></el-icon> SKU:{{ item.productId }}</span>
                <span class="sku-qty">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>

          <div class="card-bottom">
            <div class="eco-benefit">
              <el-icon><PriceTag /></el-icon>
              <span>Points Transaction</span>
            </div>
            <div class="total-spent">
              <span class="curr">PTS</span>
              <span class="amount">{{ order.totalPointsSpent }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else :image-size="120" description="No orders found in this category">
        <el-button type="success" plain round @click="activeTab = 'ALL'">View All History</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Shop, Box, Iphone, PriceTag } from '@element-plus/icons-vue'
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
    console.error('Data Sync Failure')
  } finally {
    loading.value = false
  }
}

const filteredOrders = computed(() => {
  if (activeTab.value === 'ALL') return allOrders.value
  return allOrders.value.filter(o => o.status === activeTab.value)
})

const getStatusLabel = (s: string) => ({
  PENDING: 'In Queue',
  DELIVERING: 'Out for Delivery',
  COMPLETED: 'Fulfilled',
  CANCELLED: 'Closed'
}[s] || s)

const formatDate = (d: any) => d ? new Date(d).toLocaleDateString() : 'N/A'

onMounted(fetchOrders)
</script>

<style scoped>
.order-page { background: #f8fafc; min-height: 100vh; padding-bottom: 50px; }

.brand-stripe {
  height: 4px;
  background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%);
}

.order-header { padding: 24px 20px 10px; background: white; }
.title { margin: 0; font-size: 20px; font-weight: 900; color: #1e293b; text-transform: uppercase; letter-spacing: -0.5px; }
.header-desc { font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; margin-top: 4px; }

.sticky-tabs-container {
  background: white;
  padding: 0 10px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.brand-tabs :deep(.el-tabs__item) {
  font-weight: 800;
  font-size: 13px;
  color: #94a3b8;
}

.brand-tabs :deep(.el-tabs__item.is-active) {
  color: #007934;
}

.brand-tabs :deep(.el-tabs__active-bar) {
  background-color: #007934;
  height: 3px;
}

.list-container { padding: 16px; }

/* Order Card Design */
.order-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.store-info { display: flex; align-items: center; gap: 8px; }
.store-icon { color: #007934; font-size: 18px; }
.store-id { font-weight: 800; font-size: 13px; color: #475569; }

.status-pill {
  font-size: 10px;
  font-weight: 900;
  text-transform: uppercase;
  padding: 4px 10px;
  border-radius: 20px;
  letter-spacing: 0.5px;
}
.status-pill.PENDING { background: #fff7ed; color: #ff7900; }
.status-pill.DELIVERING { background: #eff6ff; color: #3b82f6; }
.status-pill.COMPLETED { background: #f0fdf4; color: #007934; }

.logistics-box {
  background: #f8fafc;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 16px;
}

.order-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.ticket-id { font-family: monospace; font-size: 12px; font-weight: 700; color: #94a3b8; }
.date-stamp { font-size: 11px; color: #94a3b8; font-weight: 700; }

.address-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 8px;
}
.dot-indicator { width: 8px; height: 8px; background: #cbd5e1; border-radius: 50%; margin-top: 5px; flex-shrink: 0; }
.addr-text { font-size: 14px; font-weight: 700; color: #1e293b; line-height: 1.4; }

.contact-info { margin-left: 18px; margin-bottom: 15px; }
.phone-label { font-size: 12px; color: #64748b; font-weight: 600; display: flex; align-items: center; gap: 4px; }

.sku-summary {
  border-top: 1px dashed #e2e8f0;
  padding-top: 12px;
}
.sku-line {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 4px;
  font-weight: 700;
}
.sku-name { display: flex; align-items: center; gap: 4px; }

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.eco-benefit {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 800;
  color: #94a3b8;
  text-transform: uppercase;
}

.total-spent { display: flex; align-items: baseline; gap: 4px; }
.curr { font-size: 10px; font-weight: 900; color: #ff7900; }
.amount { font-size: 24px; font-weight: 900; color: #ff7900; }

:deep(.el-empty__description) {
  font-weight: 800;
  font-size: 13px;
  color: #cbd5e1;
  text-transform: uppercase;
}
</style>
