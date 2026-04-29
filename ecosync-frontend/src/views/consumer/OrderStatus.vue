<template>
  <div class="order-status-page">
    <div class="brand-top-header">
      <div class="header-main">
        <h2 class="title">My Transactions</h2>
        <div class="header-badge">7-ELEVEn Verified</div>
      </div>
      <div class="header-desc">Data synchronized with central store network</div>
    </div>

    <div class="sticky-tabs-container">
      <el-tabs v-model="activeTab" class="brand-tabs" @tab-change="fetchOrders">
        <el-tab-pane label="All History" name="ALL" />
        <el-tab-pane label="Awaiting Pickup" name="AWAITING_PICKUP" />
        <el-tab-pane label="Completed" name="COMPLETED" />
      </el-tabs>
    </div>

    <div class="list-container" v-loading="loading">
      <div v-if="orders.length > 0" class="order-feed">
        <div v-for="order in orders" :key="order.orderId" class="order-card shadow-sm">

          <div class="card-top">
            <div class="store-info">
              <div class="store-icon-box">
                <el-icon><Shop /></el-icon>
              </div>
              <span class="store-name">7-Eleven Store #{{ order.storeId }}</span>
            </div>
            <div :class="['status-badge', order.status]">
              {{ formatStatus(order.status) }}
            </div>
          </div>

          <div class="details-box">
            <div class="order-meta">
              <span class="order-id">ORD-{{ order.orderId }}</span>
              <span class="order-time">{{ formatDate(order.createdAt) }}</span>
            </div>

            <div class="item-list" v-if="order.details && order.details.length > 0">
              <div v-for="(item, idx) in order.details" :key="idx" class="item-row">
                <div class="item-main">
                  <div class="item-dot"></div>
                  <div class="item-name-group">
                    <span class="item-name">{{ item.productName }}</span>
                    <span class="item-sku">SKU: {{ item.productId }}</span>
                  </div>
                </div>
                <div class="item-qty-price">
                  <span class="qty">x{{ item.quantity }}</span>
                  <span class="price">¥{{ item.actualPrice.toFixed(2) }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-item-hint">
              <el-skeleton :rows="2" animated />
            </div>

            <div class="pickup-section" v-if="order.status !== 'CANCELLED'">
              <div class="pickup-info">
                <span class="p-label">PICKUP IDENTIFIER</span>
                <div class="p-code">{{ order.pickupCode }}</div>
              </div>
              <div class="pickup-qr-sim">
                <el-icon color="#008163" :size="32"><Ticket /></el-icon>
              </div>
            </div>
          </div>

          <div class="card-bottom">
            <div class="total-label">Final Payment</div>
            <div class="total-val">
              <span class="currency">¥</span>
              <span class="num">{{ order.totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else :image-size="120" description="No transactions found">
        <el-button type="success" plain @click="fetchOrders">Refresh List</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Shop, Ticket } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeTab = ref('ALL')
const orders = ref<any[]>([])
const loading = ref(false)
const userId = localStorage.getItem('userId') || '12'

const fetchOrders = async () => {
  loading.value = true
  try {
    // 1. 并发获取：订单列表 + 全量临期库 (解决名字丢失) + 标准库
    const [ordersRes, allStockRes, libRes]: any = await Promise.all([
      request.get(`/orders/user/${userId}`),
      request.get('/expiring-products'),
      request.get('/products')
    ])

    let rawOrders = Array.isArray(ordersRes) ? ordersRes : (ordersRes.data || [])
    const stockList = allStockRes.data || allStockRes
    const library = libRes.data || libRes

    if (activeTab.value !== 'ALL') {
      rawOrders = rawOrders.filter((o: any) => o.status === activeTab.value)
    }

    // 2. 深度补全订单详情中的商品名
    const detailPromises = rawOrders.map(async (order: any) => {
      try {
        const detailRes: any = await request.get(`/orders/${order.orderId}/details`)
        const rawItems = detailRes.data?.items || detailRes.items || []

        const enrichedItems = rawItems.map((item: any) => {
          // 查找条码
          const stock = stockList.find((s: any) => Number(s.productId) === Number(item.productId))
          // 查找名字
          const product = library.find((p: any) => String(p.barcode) === String(stock?.barcode))

          return {
            ...item,
            productName: product ? (product.productName || product.product_name) : `ID: ${item.productId}`
          }
        })
        return { ...order, details: enrichedItems }
      } catch (e) {
        return { ...order, details: [] }
      }
    })

    orders.value = await Promise.all(detailPromises)
    orders.value.sort((a, b) => b.orderId - a.orderId)

  } catch (err) {
    console.error("Fetch orders failed", err)
  } finally {
    loading.value = false
  }
}

const formatStatus = (s: string) => s.replace('_', ' ')
const formatDate = (val: string) => val ? val.replace('T', ' ').substring(0, 16) : '-'

onMounted(fetchOrders)
</script>

<style scoped>
.order-status-page { background: #f3f4f6; min-height: 100vh; padding-bottom: 40px; }

/* 顶部美化 */
.brand-top-header { background: linear-gradient(135deg, #008163 0%, #006b52 100%); padding: 30px 20px; color: white; }
.header-main { display: flex; align-items: center; gap: 12px; }
.title { font-size: 24px; font-weight: 800; margin: 0; letter-spacing: -0.5px; }
.header-badge { background: #EE7203; color: white; font-size: 10px; padding: 2px 8px; border-radius: 4px; font-weight: 800; text-transform: uppercase; }
.header-desc { font-size: 12px; opacity: 0.8; margin-top: 5px; font-weight: 300; }

/* 粘性 Tabs */
.sticky-tabs-container { position: sticky; top: 0; z-index: 10; background: #fff; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
:deep(.brand-tabs .el-tabs__header) { margin: 0; padding: 0 15px; }
:deep(.brand-tabs .el-tabs__item.is-active) { color: #008163; font-weight: 800; }
:deep(.brand-tabs .el-tabs__active-bar) { background-color: #008163; height: 3px; }

/* 订单卡片 */
.list-container { padding: 16px; max-width: 600px; margin: 0 auto; }
.order-card { background: #fff; border-radius: 16px; padding: 18px; margin-bottom: 18px; border: 1px solid rgba(0,0,0,0.05); transition: transform 0.2s; }
.order-card:active { transform: scale(0.98); }

.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.store-info { display: flex; align-items: center; gap: 10px; }
.store-icon-box { background: #008163; color: white; padding: 6px; border-radius: 8px; display: flex; }
.store-name { font-weight: 700; font-size: 15px; color: #1f2937; }

.status-badge { font-size: 11px; font-weight: 800; padding: 4px 10px; border-radius: 6px; text-transform: uppercase; }
.status-badge.AWAITING_PICKUP { background: #fef3c7; color: #d97706; }
.status-badge.COMPLETED { background: #d1fae5; color: #059669; }
.status-badge.CANCELLED { background: #fee2e2; color: #dc2626; }

/* 详情盒 */
.details-box { background: #f9fafb; border-radius: 12px; padding: 15px; margin-bottom: 15px; }
.order-meta { display: flex; justify-content: space-between; border-bottom: 1px solid #edf2f7; padding-bottom: 10px; margin-bottom: 12px; }
.order-id { font-family: monospace; font-weight: 600; color: #64748b; }
.order-time { font-size: 12px; color: #94a3b8; }

.item-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.item-main { display: flex; align-items: center; gap: 10px; }
.item-dot { width: 6px; height: 6px; background: #cbd5e1; border-radius: 50%; }
.item-name { display: block; font-weight: 700; font-size: 14px; color: #374151; }
.item-sku { display: block; font-size: 10px; color: #94a3b8; }

.item-qty-price { text-align: right; }
.qty { font-size: 12px; color: #94a3b8; margin-right: 8px; }
.price { font-weight: 700; color: #1f2937; }

/* 取货码部分 */
.pickup-section { background: #fff; border: 2px dashed #e2e8f0; border-radius: 10px; padding: 12px 15px; margin-top: 15px; display: flex; justify-content: space-between; align-items: center; }
.p-label { display: block; font-size: 9px; color: #94a3b8; font-weight: 800; letter-spacing: 1px; }
.p-code { font-family: 'Courier New', Courier, monospace; font-size: 22px; font-weight: 900; color: #008163; }

/* 底部结算 */
.card-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 5px; }
.total-label { font-size: 13px; font-weight: 600; color: #64748b; }
.total-val { color: #EE7203; display: flex; align-items: baseline; gap: 2px; }
.currency { font-size: 14px; font-weight: 800; }
.num { font-size: 24px; font-weight: 900; }
</style>
