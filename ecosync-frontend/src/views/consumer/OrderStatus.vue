<template>
  <div class="order-status-page">
    <div class="brand-top-header">
      <div class="header-main">
        <div class="header-text">
          <h2 class="title">My Transactions</h2>
          <div class="header-desc">Data synchronized with central store network</div>
        </div>
      </div>
      <div class="header-badge">7-ELEVEn Verified</div>
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
        <div v-for="order in orders" :key="order.orderId" class="order-card">

          <div class="card-top">
            <div class="store-info">
              <div class="store-icon-box">
                <el-icon><Shop /></el-icon>
              </div>
              <div class="store-text">
                <span class="store-name">7-Eleven Store #{{ order.storeId }}</span>
                <span class="store-sub">{{ formatDate(order.createdAt) }}</span>
              </div>
            </div>
            <div :class="['status-badge', order.status]">
              <el-icon :size="12" style="margin-right: 4px">
                <Check v-if="order.status === 'COMPLETED'" />
                <Clock v-else-if="order.status === 'AWAITING_PICKUP'" />
                <Close v-else />
              </el-icon>
              {{ formatStatus(order.status) }}
            </div>
          </div>

          <div class="details-box">
            <div class="order-meta">
              <span class="order-id">ORD-{{ order.orderId }}</span>
              <span class="order-items-count">{{ order.details?.length || 0 }} items</span>
            </div>

            <div class="item-list" v-if="order.details && order.details.length > 0">
              <div v-for="(item, idx) in order.details" :key="idx" class="item-row">
                <div class="item-main">
                  <div class="item-img-box">
                    <el-icon :size="16"><Goods /></el-icon>
                  </div>
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
              <div class="pickup-left">
                <div class="pickup-icon">
                  <el-icon :size="20"><Ticket /></el-icon>
                </div>
                <div class="pickup-info">
                  <span class="p-label">PICKUP IDENTIFIER</span>
                  <div class="p-code">{{ order.pickupCode }}</div>
                </div>
              </div>
              <div class="pickup-qr">
                <div class="qr-placeholder">
                  <el-icon :size="24"><Grid /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <div class="card-bottom">
            <div class="total-section">
              <span class="total-label">Final Payment</span>
              <div class="total-val">
                <span class="currency">¥</span>
                <span class="num">{{ order.totalAmount.toFixed(2) }}</span>
              </div>
            </div>
            <!-- 操作按钮区：Awaiting Pickup 显示 Cancel + Shop More -->
            <div class="action-group" v-if="order.status === 'AWAITING_PICKUP'">
              <el-button
                type="danger"
                size="small"
                round
                plain
                class="cancel-order-btn"
                @click="cancelOrder(order.orderId)"
              >
                <el-icon><CircleClose /></el-icon> Cancel
              </el-button>

              <el-button
                type="success"
                size="small"
                round
                class="action-btn"
                @click="router.push('/home')"
              >
                <el-icon><ShoppingCart /></el-icon> Shop More
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">
          <el-icon :size="64" color="#cbd5e1"><Document /></el-icon>
        </div>
        <div class="empty-title">No transactions found</div>
        <div class="empty-desc">Start shopping to see your orders here</div>
        <el-button type="success" round class="empty-action" @click="router.push('/home')">
          <el-icon><ShoppingCart /></el-icon> Go Shopping
        </el-button>
      </div>

      <div class="safe-bottom"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Shop, Ticket, Goods, Check, Clock, Close, Document, ShoppingCart, Grid, CircleClose } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessageBox } from 'element-plus'
import { ElMessage } from '@/utils/message'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeTab = ref('ALL')
const orders = ref<any[]>([])
const loading = ref(false)
const userId = localStorage.getItem('userId') || '12'

const fetchOrders = async () => {
  loading.value = true
  try {
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

    const detailPromises = rawOrders.map(async (order: any) => {
      try {
        const detailRes: any = await request.get(`/orders/${order.orderId}/details`)
        const rawItems = detailRes.data?.items || detailRes.items || []

        const enrichedItems = rawItems.map((item: any) => {
          const stock = stockList.find((s: any) => Number(s.productId) === Number(item.productId))
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

// 记录当前正在"再来一单/继续加购"的订单ID，用于按钮Loading动画
const shopMoreLoading = ref<number | null>(null)

// 处理商品重新加入购物车的逻辑
const handleShopMore = async (order: any) => {
  // 1. 防御性检查：如果没有详情数据则直接返回
  if (!order.details || order.details.length === 0) {
    ElMessage.warning('No items found in this order.')
    return
  }

  // 开启对应按钮的 loading 动画
  shopMoreLoading.value = order.orderId

  try {
    // 2. 遍历该订单中的所有商品，生成加入购物车的 Promise 数组
    const addCartPromises = order.details.map((item: any) => {
      return request.post('/cart', {
        userId: Number(userId),
        productId: Number(item.productId),
        quantity: Number(item.quantity) // 保持当时购买的数量
      })
    })

    // 3. 并发执行所有加购请求
    await Promise.all(addCartPromises)

    ElMessage.success('Items returned to cart successfully!')

    // 4. 全部加购成功后，跳转至首页 /home
    router.push('/home')

  } catch (e: any) {
    // 如果某个商品库存不足或失效，捕获异常提示用户
    ElMessage.error(e.response?.data?.message || e.message || 'Failed to add some items back to cart')
  } finally {
    // 无论成功失败，关闭 loading
    shopMoreLoading.value = null
  }
}

// 取消订单方法 - POST 请求
const cancelOrder = async (orderId: number) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to cancel this order?',
      'Cancel Order',
      {
        confirmButtonText: 'Yes, Cancel',
        cancelButtonText: 'No, Keep It',
        type: 'warning'
      }
    )
    await request.post(`/orders/${orderId}/cancel`)
    ElMessage.success('Order cancelled successfully')
    fetchOrders()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || e.message || 'Failed to cancel order')
    }
  }
}

const formatStatus = (s: string) => s.replace('_', ' ')
const formatDate = (val: string) => val ? val.replace('T', ' ').substring(0, 16) : '-'

onMounted(fetchOrders)
</script>

<style scoped>
.order-status-page { background: #f4f6f8; min-height: 100vh; }

.brand-top-header {
  background: linear-gradient(135deg, #008163 0%, #006b52 60%, #004d3a 100%);
  padding: 32px 24px 40px;
  color: white;
  position: relative;
  overflow: hidden;
}
.brand-top-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 200px;
  height: 200px;
  background: rgba(255,255,255,0.03);
  border-radius: 50%;
}
.header-main { display: flex; align-items: center; gap: 16px; position: relative; z-index: 1; }
.header-text { flex: 1; }
.title { font-size: 22px; font-weight: 900; margin: 0; letter-spacing: -0.3px; }
.header-desc { font-size: 12px; opacity: 0.7; margin-top: 4px; font-weight: 400; }
.header-badge {
  background: #EE7203; color: white; font-size: 10px;
  padding: 4px 10px; border-radius: 20px; font-weight: 800;
  text-transform: uppercase; letter-spacing: 0.5px;
  margin-top: 8px;
  display: inline-block;
}

.sticky-tabs-container {
  position: sticky; top: 0; z-index: 10;
  background: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}
:deep(.brand-tabs .el-tabs__header) { margin: 0; padding: 0 20px; }
:deep(.brand-tabs .el-tabs__item) { font-weight: 600; color: #94a3b8; padding: 0 20px; height: 48px; }
:deep(.brand-tabs .el-tabs__item.is-active) { color: #008163; font-weight: 800; }
:deep(.brand-tabs .el-tabs__active-bar) { background-color: #008163; height: 3px; border-radius: 2px; }
:deep(.brand-tabs .el-tabs__nav-wrap::after) { height: 1px; background: #f1f5f9; }

.list-container { padding: 20px 16px; max-width: 640px; margin: 0 auto; }

.order-card {
  background: #fff; border-radius: 20px; padding: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(0,0,0,0.04);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08);
  border-color: rgba(0, 129, 99, 0.1);
}

.card-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.store-info { display: flex; align-items: center; gap: 12px; }
.store-icon-box {
  background: linear-gradient(135deg, #008163, #006b52);
  color: white; padding: 8px; border-radius: 12px;
  display: flex; box-shadow: 0 2px 8px rgba(0,129,99,0.2);
}
.store-text { display: flex; flex-direction: column; }
.store-name { font-weight: 800; font-size: 15px; color: #1e293b; }
.store-sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }

.status-badge {
  font-size: 11px; font-weight: 800;
  padding: 6px 12px; border-radius: 20px;
  text-transform: uppercase; letter-spacing: 0.3px;
  display: flex; align-items: center;
}
.status-badge.AWAITING_PICKUP { background: #fef3c7; color: #d97706; }
.status-badge.COMPLETED { background: #d1fae5; color: #059669; }
.status-badge.CANCELLED { background: #fee2e2; color: #dc2626; }

.details-box {
  background: #f8fafc; border-radius: 16px;
  padding: 16px; margin-bottom: 16px;
  border: 1px solid #f1f5f9;
}
.order-meta {
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px dashed #e2e8f0;
  padding-bottom: 12px; margin-bottom: 14px;
}
.order-id { font-family: 'Courier New', monospace; font-weight: 700; color: #64748b; font-size: 13px; }
.order-items-count { font-size: 12px; color: #94a3b8; font-weight: 600; background: #e2e8f0; padding: 2px 8px; border-radius: 10px; }

.item-row {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px; padding: 8px 0;
}
.item-row:last-child { margin-bottom: 0; }
.item-main { display: flex; align-items: center; gap: 12px; }
.item-img-box {
  width: 36px; height: 36px;
  background: #fff; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid #e2e8f0; color: #94a3b8;
}
.item-name-group { display: flex; flex-direction: column; }
.item-name { font-weight: 700; font-size: 14px; color: #334155; line-height: 1.3; }
.item-sku { font-size: 10px; color: #94a3b8; margin-top: 2px; font-family: monospace; }

.item-qty-price { text-align: right; display: flex; flex-direction: column; align-items: flex-end; }
.qty { font-size: 12px; color: #94a3b8; margin-bottom: 2px; }
.price { font-weight: 800; color: #1e293b; font-size: 15px; }

.pickup-section {
  background: linear-gradient(135deg, #fff 0%, #f0fdf4 100%);
  border: 2px dashed #008163;
  border-radius: 14px;
  padding: 16px;
  margin-top: 16px;
  display: flex; justify-content: space-between; align-items: center;
}
.pickup-left { display: flex; align-items: center; gap: 14px; }
.pickup-icon {
  width: 44px; height: 44px;
  background: linear-gradient(135deg, #008163, #006b52);
  color: white; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(0,129,99,0.2);
}
.p-label { display: block; font-size: 9px; color: #008163; font-weight: 800; letter-spacing: 1.5px; margin-bottom: 4px; }
.p-code { font-family: 'Courier New', Courier, monospace; font-size: 24px; font-weight: 900; color: #008163; letter-spacing: 2px; }
.qr-placeholder {
  width: 52px; height: 52px;
  background: #fff; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid #e2e8f0; color: #cbd5e1;
}

/* 底部结算 + 操作按钮 */
.card-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 4px; }
.total-section { display: flex; flex-direction: column; }
.total-label { font-size: 12px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; }
.total-val { color: #EE7203; display: flex; align-items: baseline; gap: 2px; margin-top: 2px; }
.currency { font-size: 14px; font-weight: 800; }
.num { font-size: 26px; font-weight: 900; }

/* 操作按钮组：Cancel + Shop More 并排 */
.action-group { display: flex; align-items: center; gap: 8px; }

.action-btn {
  background: #008163 !important; border: none !important;
  font-weight: 700; padding: 0 18px; height: 36px;
  box-shadow: 0 4px 12px rgba(0,129,99,0.2);
}

/* 新增：取消订单按钮 */
.cancel-order-btn {
  font-weight: 700;
  padding: 0 14px;
  height: 36px;
  border-color: #fecaca !important;
  color: #dc2626 !important;
  background: #fef2f2 !important;
}
.cancel-order-btn:hover {
  background: #fee2e2 !important;
  border-color: #ef4444 !important;
  color: #b91c1c !important;
}

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 80px 24px; text-align: center;
}
.empty-icon {
  width: 100px; height: 100px;
  background: #f1f5f9; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20px;
}
.empty-title { font-size: 18px; font-weight: 800; color: #334155; margin-bottom: 8px; }
.empty-desc { font-size: 14px; color: #94a3b8; margin-bottom: 24px; }
.empty-action {
  background: #008163 !important; border: none !important;
  font-weight: 800; padding: 0 28px; height: 44px;
  box-shadow: 0 4px 12px rgba(0,129,99,0.2);
}

.safe-bottom { height: 40px; }
</style>
