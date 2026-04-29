<template>
  <div class="staff-app-container">
    <div class="top-brand-bar">
      <div class="brand-content">
        <span class="brand-logo">7-ELEVEn</span>
        <span class="brand-title">Smart Store Management</span>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="card" class="main-tabs">
      <el-tab-pane name="inventory">
        <template #label>
          <div class="tab-label">
            <el-icon><Box /></el-icon><span>Inventory Console</span>
          </div>
        </template>

        <el-row :gutter="25">
          <el-col :span="8">
            <div class="side-panel">
              <ProductEntry :library="library" @refresh="fetchData" />
            </div>
          </el-col>

          <el-col :span="16">
            <el-card class="modern-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <div class="title-group">
                    <span class="dot"></span>
                    <span class="title-text">Live Shelf Stock</span>
                  </div>
                  <el-button @click="fetchData" :loading="loading" icon="Refresh" circle />
                </div>
              </template>

              <el-table :data="enrichedStockList" border stripe height="620" class="custom-table">
                <el-table-column label="Product Information" min-width="220">
                  <template #default="{row}">
                    <div class="product-info-cell">
                      <div class="p-name">{{ row.productName }}</div>
                      <div class="p-bc">BC: {{ row.barcode }}</div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="Stock" width="100" align="center">
                  <template #default="{row}">
                    <span :class="['stock-num', row.remainingStock < 5 ? 'critical' : '']">
                      {{ row.remainingStock }}
                    </span>
                  </template>
                </el-table-column>

                <el-table-column label="Status" width="130" align="center">
                  <template #default="{row}">
                    <el-tag :type="getStatusTag(row.status)" effect="dark" round size="small">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane name="orders">
        <template #label>
          <div class="tab-label">
            <el-icon><List /></el-icon><span>Order Monitor</span>
          </div>
        </template>

        <el-card class="modern-card order-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="title-group">
                <span class="dot orange"></span>
                <span class="title-text">Recent Pickup Orders</span>
              </div>
              <el-button type="warning" @click="syncAllOrders" :loading="loadingOrders" round>
                <el-icon><Refresh /></el-icon>&nbsp;Sync All Orders
              </el-button>
            </div>
          </template>

          <el-table :data="orderList" border highlight-current-row height="620">
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="order-detail-wrapper">
                  <div class="detail-header">
                    <el-icon><ShoppingBag /></el-icon> Itemized Receipt
                  </div>
                  <el-table :data="row.items" size="small" class="inner-table">
                    <el-table-column label="Description" prop="productName" />
                    <el-table-column label="Unit Price" width="120" align="right">
                      <template #default="s">¥{{ s.row.actualPrice.toFixed(2) }}</template>
                    </el-table-column>
                    <el-table-column label="Qty" prop="quantity" width="80" align="center" />
                    <el-table-column label="Subtotal" width="120" align="right">
                      <template #default="s">
                        <span class="subtotal-text">¥{{ (s.row.actualPrice * s.row.quantity).toFixed(2) }}</span>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="ID" width="80" align="center">
              <template #default="{row}"><b>#{{ row.orderId }}</b></template>
            </el-table-column>

            <el-table-column label="Pickup Code" width="200" align="center">
              <template #default="{row}">
                <code class="code-badge">{{ row.pickupCode }}</code>
              </template>
            </el-table-column>

            <el-table-column label="Payment" width="130" align="right">
              <template #default="{row}">
                <span class="amount-text">¥{{ row.totalAmount.toFixed(2) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="Process Status" width="180" align="center">
              <template #default="{row}">
                <el-select v-model="row.status" size="small" class="status-select" @change="updateOrderStatus(row)">
                  <el-option label="Awaiting" value="AWAITING_PICKUP" />
                  <el-option label="Completed" value="COMPLETED" />
                  <el-option label="Cancelled" value="CANCELLED" />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="Timestamp" min-width="160" align="center">
              <template #default="{row}">
                <span class="time-text">{{ formatDate(row.createdAt) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Box, List, Refresh, ShoppingBag } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import ProductEntry from './ProductEntry.vue'

// --- 逻辑部分 (保持之前修复后的稳定逻辑) ---
const activeTab = ref('inventory')
const loading = ref(false)
const loadingOrders = ref(false)
const library = ref([])
const allExpiringProducts = ref([])
const orderList = ref([])
const storeId = localStorage.getItem('lastStoreId') || '1'

const fetchData = async () => {
  loading.value = true
  try {
    const [stockRes, libRes]: any = await Promise.all([
      request.get('/expiring-products'),
      request.get('/products')
    ])
    allExpiringProducts.value = stockRes.data || stockRes
    library.value = libRes.data || libRes
  } catch (err) { console.error(err) } finally { loading.value = false }
}

const syncAllOrders = async () => {
  loadingOrders.value = true
  try {
    const scanRange = Array.from({ length: 50 }, (_, i) => i + 1)
    const results = await Promise.all(scanRange.map(id => request.get(`/orders/user/${id}`).catch(() => null)))
    let rawOrders = []
    results.forEach((res: any) => { if (Array.isArray(res?.data || res)) rawOrders = [...rawOrders, ...(res.data || res)] })

    const ordersWithDetails = await Promise.all(rawOrders.map(async (order) => {
      try {
        const detailRes: any = await request.get(`/orders/${order.orderId}/details`)
        const vo = detailRes.data || detailRes
        const enrichedItems = (vo.items || []).map(item => {
          const stock = allExpiringProducts.value.find(s => Number(s.productId) === Number(item.productId))
          const prod = library.value.find(p => String(p.barcode) === String(stock?.barcode))
          return { ...item, productName: prod?.productName || prod?.product_name || `Item ID: ${item.productId}`, barcode: stock?.barcode }
        })
        return { ...order, items: enrichedItems }
      } catch (e) { return { ...order, items: [] } }
    }))
    orderList.value = Array.from(new Map(ordersWithDetails.map(o => [o.orderId, o])).values()).sort((a, b) => b.orderId - a.orderId)
    ElMessage.success('Live data synchronized')
  } catch (e) { ElMessage.error('Sync failed') } finally { loadingOrders.value = false }
}

const getStatusTag = (s: string) => {
  if (s === 'AVAILABLE') return 'success'
  if (s === 'SOLD_OUT') return 'info'
  return 'danger'
}

const updateOrderStatus = async (row) => {
  try {
    await request.put(`/orders/${row.orderId}`, null, { params: { status: row.status } })
    ElMessage.success('Status updated')
  } catch (e) { ElMessage.error('Update failed') }
}

const formatDate = (val: string) => val ? val.replace('T', ' ').substring(0, 16) : '-'
const enrichedStockList = computed(() => {
  return allExpiringProducts.value.map(stock => {
    const std = library.value.find(p => String(p.barcode) === String(stock.barcode))
    return { ...stock, productName: std?.productName || std?.product_name || 'Unknown' }
  })
})

onMounted(() => { fetchData(); syncAllOrders(); })
</script>

<style scoped>
/* 全局容器背景 */
.staff-app-container {
  min-height: 100vh;
  background-color: #f4f7f6;
  padding: 0 0 30px 0;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 顶部品牌条 */
.top-brand-bar {
  background: #008163; /* 7-Eleven Green */
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.brand-logo {
  color: white;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: -1px;
  margin-right: 15px;
  border-right: 2px solid rgba(255,255,255,0.3);
  padding-right: 15px;
}
.brand-title {
  color: #fff;
  font-weight: 300;
  font-size: 16px;
}

/* 标签页美化 */
.main-tabs {
  padding: 0 25px;
}
.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

/* 卡片样式 */
.modern-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05) !important;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 5px;
}
.title-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.dot {
  width: 10px;
  height: 10px;
  background: #008163;
  border-radius: 50%;
}
.dot.orange { background: #EE7203; }
.title-text {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
}

/* 表格内文字美化 */
.p-name { font-weight: 700; color: #333; margin-bottom: 4px; }
.p-bc { font-size: 11px; color: #999; font-family: monospace; }
.stock-num {
  font-size: 16px;
  font-weight: 800;
  color: #2c3e50;
}
.stock-num.critical { color: #d63031; }

.amount-text {
  color: #EE7203;
  font-weight: 800;
  font-size: 15px;
}
.code-badge {
  background: #f1f2f6;
  padding: 5px 10px;
  border-radius: 6px;
  color: #2f3542;
  font-weight: bold;
  font-family: 'Courier New', Courier, monospace;
}

/* 订单详情展开层 */
.order-detail-wrapper {
  padding: 20px 40px;
  background: #fff;
  border-left: 5px solid #EE7203;
}
.detail-header {
  font-weight: 800;
  color: #EE7203;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.subtotal-text { font-weight: 700; color: #2c3e50; }
.inner-table {
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

/* 侧边面板 */
.side-panel {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.time-text { font-size: 12px; color: #7f8c8d; }
</style>
