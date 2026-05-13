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
          <div class="tab-label"><el-icon><Box /></el-icon><span>Inventory Console</span></div>
        </template>
        <el-row :gutter="25">
          <el-col :span="7">
            <div class="side-panel">
              <ProductEntry :library="library" @refresh="fetchData" />
            </div>
          </el-col>
          <el-col :span="17">
            <el-card class="modern-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <div class="title-group">
                    <span class="dot"></span>
                    <span class="title-text">Live Shelf Stock</span>
                  </div>
                  <el-button type="primary" @click="fetchData" :loading="loading" icon="Refresh" size="default">
                    Refresh Inventory
                  </el-button>
                </div>
              </template>

              <el-table :data="enrichedStockList" border stripe height="620">
                <el-table-column label="Product Information" min-width="180">
                  <template #default="{row}">
                    <div class="product-info-cell">
                      <div class="p-name">{{ row.productName }}</div>
                      <div class="p-bc">BC: {{ row.barcode }}</div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="Expiration Date" width="180" align="center">
                  <template #default="{row}">
                    <div class="expiry-time-cell">
                      <el-icon><Timer /></el-icon>
                      <span>{{ formatDateTime(row.expirationTime || row.expirationDate) }}</span>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="Stock" width="90" align="center">
                  <template #default="{row}">
                    <span :class="['stock-num', row.remainingStock < 5 ? 'critical' : '']">
                      {{ row.remainingStock }}
                    </span>
                  </template>
                </el-table-column>

                <el-table-column label="Action" width="160" align="center">
                  <template #default="{row}">
                    <el-select v-model="row.status" size="small" @change="handleInventoryUpdate(row)">
                      <el-option label="Available" value="AVAILABLE" />
                      <el-option label="Sold Out" value="SOLD_OUT" />
                      <el-option label="Discarded" value="DISCARDED" />
                    </el-select>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane name="orders">
        <template #label>
          <div class="tab-label"><el-icon><List /></el-icon><span>Order Monitor</span></div>
        </template>
        <el-card class="modern-card order-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="title-group">
                <span class="dot orange"></span>
                <span class="title-text">Recent Pickup Orders</span>
              </div>
              <el-button type="warning" @click="syncAllOrders" :loading="loadingOrders" icon="Refresh">
                Sync All Orders
              </el-button>
            </div>
          </template>

          <el-table :data="orderList" border highlight-current-row height="620">
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="order-detail-wrapper">
                  <div class="detail-header"><el-icon><ShoppingBag /></el-icon> Order Items</div>
                  <el-table :data="row.items" size="small">
                    <el-table-column label="Item Name" prop="productName" />
                    <el-table-column label="Price" width="100">
                      <template #default="s">¥{{ s.row.actualPrice?.toFixed(2) }}</template>
                    </el-table-column>
                    <el-table-column label="Qty" prop="quantity" width="80" />
                  </el-table>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Order ID" width="100" align="center">
              <template #default="{row}"><b>#{{ row.orderId }}</b></template>
            </el-table-column>

            <el-table-column label="Pickup Code" width="180" align="center">
              <template #default="{row}"><code class="code-badge">{{ row.pickupCode }}</code></template>
            </el-table-column>

            <el-table-column label="Total Amount" width="130" align="right">
              <template #default="{row}"><span class="amount-text">¥{{ row.totalAmount?.toFixed(2) }}</span></template>
            </el-table-column>

            <el-table-column label="Process Status" width="180" align="center">
              <template #default="{row}">
                <el-tag :type="row.status === 'SOLD_OUT' ? 'success' : 'warning'" effect="dark">
                  {{ row.status === 'AVAILABLE' ? 'Awaiting Pickup' : (row.status === 'SOLD_OUT' ? 'Completed' : 'Cancelled') }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="Created At" min-width="160" align="center">
              <template #default="{row}">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Box, List, Refresh, ShoppingBag, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import ProductEntry from './ProductEntry.vue'

const activeTab = ref('inventory')
const loading = ref(false)
const loadingOrders = ref(false)
const library = ref([])
const allExpiringProducts = ref([])
const orderList = ref([])

// 1. 获取库存和产品库基础数据
const fetchData = async () => {
  loading.value = true
  try {
    const [stockRes, libRes]: any = await Promise.all([
      request.get('/expiring-products'),
      request.get('/products')
    ])
    allExpiringProducts.value = stockRes.data || stockRes
    library.value = libRes.data || libRes
  } catch (err) {
    ElMessage.error('Failed to load inventory data')
  } finally { loading.value = false }
}

// 2. 修改库存项 (ExpiringProductController PUT)
const handleInventoryUpdate = async (row) => {
  try {
    await request.put(`/expiring-products/${row.productId}`, row)
    ElMessage.success('Inventory updated successfully')
  } catch (e) {
    ElMessage.error('Failed to update inventory status')
    fetchData()
  }
}

// 3. 同步订单
const syncAllOrders = async () => {
  loadingOrders.value = true
  try {
    const scanRange = Array.from({ length: 30 }, (_, i) => i + 1)
    const results = await Promise.all(scanRange.map(id => request.get(`/orders/user/${id}`).catch(() => null)))

    let rawOrders = []
    results.forEach((res: any) => {
      const data = res?.data || res
      if (Array.isArray(data)) rawOrders = [...rawOrders, ...data]
    })

    const ordersWithDetails = await Promise.all(rawOrders.map(async (order) => {
      try {
        const detailRes: any = await request.get(`/orders/${order.orderId}/details`)
        const vo = detailRes.data || detailRes
        const enrichedItems = (vo.items || []).map(item => {
          const stock = allExpiringProducts.value.find(s => Number(s.productId) === Number(item.productId))
          const prod = library.value.find(p => String(p.barcode) === String(stock?.barcode))
          return { ...item, productName: prod?.productName || 'Unknown Product' }
        })
        return { ...order, items: enrichedItems }
      } catch (e) { return { ...order, items: [] } }
    }))

    orderList.value = Array.from(new Map(ordersWithDetails.map(o => [o.orderId, o])).values())
      .sort((a, b) => b.orderId - a.orderId)
  } catch (e) {
    ElMessage.error('Order sync failed')
  } finally { loadingOrders.value = false }
}

const formatDateTime = (val: string) => {
  if (!val) return 'No Date'
  return val.replace('T', ' ').substring(0, 16)
}

const enrichedStockList = computed(() => {
  return allExpiringProducts.value.map(stock => {
    const std = library.value.find(p => String(p.barcode) === String(stock.barcode))
    return { ...stock, productName: std?.productName || 'Unknown' }
  })
})

onMounted(() => {
  fetchData()
  syncAllOrders()
})
</script>

<style scoped>
.staff-app-container { min-height: 100vh; background-color: #f4f7f6; padding-bottom: 30px; }
.top-brand-bar { background: #008163; height: 60px; display: flex; align-items: center; padding: 0 30px; margin-bottom: 20px; }
.brand-logo { color: white; font-size: 24px; font-weight: 900; margin-right: 15px; border-right: 2px solid rgba(255,255,255,0.3); padding-right: 15px; }
.brand-title { color: #fff; font-size: 16px; }
.main-tabs { padding: 0 25px; }
.tab-label { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.modern-card { border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.05) !important; border: none; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title-group { display: flex; align-items: center; gap: 10px; }
.dot { width: 10px; height: 10px; background: #008163; border-radius: 50%; }
.dot.orange { background: #EE7203; }
.p-name { font-weight: bold; color: #333; }
.p-bc { font-size: 12px; color: #7f8c8d; }
.stock-num { font-weight: 800; font-size: 16px; }
.stock-num.critical { color: #e74c3c; }
.expiry-time-cell { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #606266; }
.amount-text { color: #EE7203; font-weight: 800; }
.code-badge { background: #f1f2f6; padding: 5px 10px; border-radius: 6px; font-family: monospace; font-weight: bold; }
.order-detail-wrapper { padding: 20px; background: #fafafa; border-left: 5px solid #EE7203; }
.side-panel { background: white; padding: 20px; border-radius: 12px; height: 100%; box-sizing: border-box; }
</style>
