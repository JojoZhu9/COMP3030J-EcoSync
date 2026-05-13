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
                  <el-button type="primary" size="small" @click="fetchData" :loading="loading" plain>
                    <el-icon><Refresh /></el-icon>&nbsp;Refresh Data
                  </el-button>
                </div>
              </template>

              <el-table :data="enrichedStockList" border stripe height="620">
                <el-table-column label="Product Information" min-width="200">
                  <template #default="{row}">
                    <div class="product-info-cell">
                      <div class="p-name">{{ row.productName }}</div>
                      <div class="p-bc">BC: {{ row.barcode }}</div>
                    </div>
                  </template>
                </el-table-column>

                <el-table-column label="Expiration" width="160" align="center">
                  <template #default="{row}">
                    <div class="time-column">
                      <el-icon><Timer /></el-icon>
                      <span class="time-text">{{ formatDate(row.expirationTime || row.expirationDate) }}</span>
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

                <el-table-column label="Status Action" width="160" align="center">
                  <template #default="{row}">
                    <el-select
                      v-model="row.status"
                      size="small"
                      @change="handleInventoryUpdate(row)"
                      placeholder="Change Status"
                    >
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
              <el-button type="warning" @click="syncAllOrders" :loading="loadingOrders" round>
                <el-icon><Refresh /></el-icon>&nbsp;Sync All Orders
              </el-button>
            </div>
          </template>

          <el-table :data="orderList" border highlight-current-row height="620">
            <el-table-column type="expand">
              <template #default="{ row }">
                <div class="order-detail-wrapper">
                  <div class="detail-header"><el-icon><ShoppingBag /></el-icon> Itemized Receipt</div>
                  <el-table :data="row.items" size="small" class="inner-table">
                    <el-table-column label="Description" prop="productName" />
                    <el-table-column label="Price" width="120" align="right">
                      <template #default="s">¥{{ s.row.actualPrice.toFixed(2) }}</template>
                    </el-table-column>
                    <el-table-column label="Qty" prop="quantity" width="80" align="center" />
                  </el-table>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="ID" width="80" align="center">
              <template #default="{row}"><b>#{{ row.orderId }}</b></template>
            </el-table-column>

            <el-table-column label="Pickup Code" width="180" align="center">
              <template #default="{row}"><code class="code-badge">{{ row.pickupCode }}</code></template>
            </el-table-column>

            <el-table-column label="Payment" width="120" align="right">
              <template #default="{row}"><span class="amount-text">¥{{ row.totalAmount.toFixed(2) }}</span></template>
            </el-table-column>

            <el-table-column label="Update Status" width="180" align="center">
              <template #default="{row}">
                <el-select v-model="row.status" size="small" @change="handleOrderUpdate(row)">
                  <el-option label="Awaiting Pickup" value="AVAILABLE" />
                  <el-option label="Completed" value="SOLD_OUT" />
                  <el-option label="Cancelled" value="DISCARDED" />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="Time" min-width="160" align="center">
              <template #default="{row}"><span class="time-text">{{ formatDate(row.createdAt) }}</span></template>
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
    ElMessage.success('Inventory synced')
  } catch (err) {
    ElMessage.error('Failed to load inventory')
  } finally { loading.value = false }
}

// 2. 修改库存状态 (对应后端的 PUT /api/expiring-products/{id})
const handleInventoryUpdate = async (row) => {
  try {
    // 匹配后端 @RequestBody ExpiringProduct 结构
    const updatePayload = {
      productId: row.productId,
      barcode: row.barcode,
      storeId: row.storeId,
      remainingStock: row.remainingStock,
      status: row.status,
      expirationTime: row.expirationTime
    }

    const res: any = await request.put(`/expiring-products/${row.productId}`, updatePayload)

    if (res.toString().includes('失败')) {
      throw new Error(res)
    }
    ElMessage.success(`Stock #${row.productId} updated to ${row.status}`)
  } catch (e: any) {
    ElMessage.error('Stock update failed: ' + (e.message || 'Server Error'))
    fetchData() // 失败回滚
  }
}

// 3. 修改订单状态 (对应 OrderController 的 PutMapping)
const handleOrderUpdate = async (row) => {
  try {
    await request.put(`/orders/${row.orderId}`, null, {
      params: { status: row.status }
    })
    ElMessage.success(`Order #${row.orderId} updated`)
  } catch (e) {
    ElMessage.error('Order update failed')
    syncAllOrders()
  }
}

// 4. 同步订单
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
          return { ...item, productName: prod?.productName || `ID: ${item.productId}` }
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

const formatDate = (val: string) => {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 16)
}

const enrichedStockList = computed(() => {
  return allExpiringProducts.value.map(stock => {
    const std = library.value.find(p => String(p.barcode) === String(stock.barcode))
    return { ...stock, productName: std?.productName || 'Unknown SKU' }
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
.product-info-cell .p-name { font-weight: bold; color: #333; }
.product-info-cell .p-bc { font-size: 12px; color: #7f8c8d; }
.stock-num { font-weight: 800; font-size: 16px; }
.stock-num.critical { color: #e74c3c; }
.amount-text { color: #EE7203; font-weight: 800; }
.code-badge { background: #f1f2f6; padding: 5px 10px; border-radius: 6px; font-family: monospace; font-weight: bold; }
.order-detail-wrapper { padding: 20px; background: #fafafa; border-left: 5px solid #EE7203; }
.time-column { display: flex; align-items: center; gap: 5px; justify-content: center; }
.time-text { font-size: 12px; color: #606266; }
.side-panel { background: white; padding: 20px; border-radius: 12px; height: 100%; box-sizing: border-box; }
</style>
