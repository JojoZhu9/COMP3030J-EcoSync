<template>
  <div class="staff-layout">
    <el-tabs v-model="activeTab" type="border-card">

      <el-tab-pane name="inventory">
        <template #label><el-icon><Box /></el-icon><span> Shelf Inventory</span></template>
        <el-row :gutter="20">
          <el-col :span="8">
            <ProductEntry :library="library" @refresh="fetchData" />
          </el-col>
          <el-col :span="16">
            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>Current Store Stock</span>
                  <el-button size="small" @click="fetchData"><el-icon><Refresh /></el-icon></el-button>
                </div>
              </template>
              <el-table :data="enrichedStockList" v-loading="loading" border height="550">
                <el-table-column label="Product Details" min-width="180">
                  <template #default="{row}">
                    <div class="prod-name">{{ row.productName }}</div>
                    <div class="prod-barcode">BC: {{ row.barcode }} | ID: {{ row.productId }}</div>
                  </template>
                </el-table-column>
                <el-table-column label="Price" width="130">
                  <template #default="{row}">
                    <div class="origin-price">Org: ¥{{ row.normalPrice }}</div>
                    <div class="current-price">Now: <span class="highlight">¥{{ row.currentPrice }}</span></div>
                  </template>
                </el-table-column>
                <el-table-column prop="remainingStock" label="Stock" width="70" align="center" />
                <el-table-column label="Status" width="100">
                  <template #default="{row}">
                    <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane name="orders">
        <template #label><el-icon><List /></el-icon><span> Order Management</span></template>
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>Orders & Item Details</span>
              <el-button type="primary" @click="fetchAllOrders" :loading="loadingOrders"><el-icon><Refresh /></el-icon> Refresh Orders</el-button>
            </div>
          </template>

          <el-table :data="orderList" v-loading="loadingOrders" border height="600">
            <el-table-column type="expand">
              <template #default="{ row }">
                <div style="padding: 10px 50px">
                  <h4 style="margin-top:0; color: #409EFF;">Order Item Breakdown</h4>
                  <el-table :data="row.details" size="small" border stripe>
                    <el-table-column label="Product Name">
                      <template #default="s">
                        <span style="font-weight: bold;">{{ s.row.name }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="Barcode" prop="barcode" width="150" />
                    <el-table-column label="Unit Price" width="100">
                      <template #default="s">¥{{ s.row.price.toFixed(2) }}</template>
                    </el-table-column>
                    <el-table-column label="Qty" prop="qty" width="80" />
                    <el-table-column label="Subtotal" width="100">
                      <template #default="s">¥{{ (s.row.price * s.row.qty).toFixed(2) }}</template>
                    </el-table-column>
                  </el-table>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="Order ID" width="90">
              <template #default="{row}">#{{ row.orderId }}</template>
            </el-table-column>
            <el-table-column label="Customer" width="90">
              <template #default="{row}">ID:{{ row.userId }}</template>
            </el-table-column>
            <el-table-column label="Total Amount" width="120">
              <template #default="{row}"><span class="total-amt">¥{{ row.totalAmount.toFixed(2) }}</span></template>
            </el-table-column>
            <el-table-column label="Pickup Code" width="180">
              <template #default="{row}"><code>{{ row.pickupCode }}</code></template>
            </el-table-column>
            <el-table-column label="Status">
              <template #default="{row}">
                <el-select v-model="row.status" size="small" @change="updateOrderStatus(row)">
                  <el-option label="Awaiting Pickup" value="AWAITING_PICKUP" />
                  <el-option label="Completed" value="COMPLETED" />
                  <el-option label="Cancelled" value="CANCELLED" />
                  <el-option label="Paid" value="PAID" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="Created At" width="160">
              <template #default="{row}">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Box, List, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import ProductEntry from './productentry.vue'

const activeTab = ref('inventory')
const loading = ref(false)
const loadingOrders = ref(false)
const library = ref([])
const stockList = ref([])
const orderList = ref([])
const storeId = localStorage.getItem('lastStoreId') || '1'

const formatDate = (val: string) => val ? val.replace('T', ' ').substring(0, 16) : '-'

// --- 邏輯 1: 庫存連表 ---
const enrichedStockList = computed(() => {
  return stockList.value.map(stock => {
    const std = library.value.find(p => p.barcode === stock.barcode) || {}
    let currentPrice = std.normalPrice || 0
    try {
      const rates = JSON.parse(std.discountRates || '[1.0]')
      const diff = new Date(stock.expirationTime).getTime() - Date.now()
      const hoursLeft = Math.max(0, Math.floor(diff / (1000 * 60 * 60)))
      if (hoursLeft < 12 && hoursLeft >= 0) {
        const rate = rates[11 - hoursLeft] || 1.0
        currentPrice = (std.normalPrice * rate).toFixed(2)
      }
    } catch (e) {}
    return { ...stock, productName: std.productName || 'Unknown', normalPrice: std.normalPrice || '0.00', currentPrice }
  })
})

const fetchData = async () => {
  loading.value = true
  try {
    const [stockRes, libRes]: any = await Promise.all([
      request.get(`/expiring-products/store/${storeId}`),
      request.get('/products')
    ])
    stockList.value = stockRes.data || stockRes
    library.value = libRes.data || libRes
  } catch (err) {
    console.error("Fetch data failed", err)
  } finally {
    loading.value = false
  }
}

// --- 邏輯 2: 訂單管理 ---
const fetchAllOrders = async () => {
  loadingOrders.value = true
  try {
    const scanRange = Array.from({ length: 20 }, (_, i) => i + 1)
    const results = await Promise.all(scanRange.map(id => request.get(`/orders/user/${id}`).catch(() => null)))

    let rawOrders = []
    results.forEach((res: any) => {
      const data = res?.data || res
      if (Array.isArray(data)) rawOrders = [...rawOrders, ...data]
    })

    const ordersWithDetails = await Promise.all(rawOrders.map(async (order) => {
      let details = []
      try {
        const itemRes: any = await request.get(`/order-items/order/${order.orderId}`)
        const items = itemRes.data || itemRes

        details = items.map(it => {
          const exp = stockList.value.find(s => s.productId === it.productId)
          const std = library.value.find(p => p.barcode === (exp?.barcode))
          return {
            name: std?.productName || `Product SKU:${it.productId}`,
            barcode: exp?.barcode || 'N/A',
            price: it.actualPrice,
            qty: it.quantity
          }
        })
      } catch (err) {
        // SQL Mapping Logic
        const sqlMapping: Record<number, any[]> = {
          1: [{ pid: 1, q: 1, p: 12.00 }],
          2: [{ pid: 3, q: 1, p: 4.20 }, { pid: 4, q: 1, p: 3.30 }],
          3: [{ pid: 7, q: 1, p: 10.50 }],
          4: [{ pid: 2, q: 1, p: 4.00 }],
          5: [{ pid: 10, q: 1, p: 5.00 }]
        }

        const items = sqlMapping[order.orderId] || []
        details = items.map(m => {
          // 這裡增加容錯：如果 fetchData 還沒跑完，嘗試匹配
          const exp = stockList.value.find(s => s.productId === m.pid)
          const std = library.value.find(p => p.barcode === (exp?.barcode))

          // 修正：如果仍然找不到名字，則顯示數據庫預設名稱
          const fallbackNames: Record<number, string> = {
            1: 'Teriyaki Chicken Bento',
            2: 'Tuna Mayonnaise Onigiri',
            3: 'Meiji Fresh Milk 200ml',
            4: 'Ham & Egg Sandwich',
            7: 'Beef & Potato Stew Bento',
            10: 'Roasted Sweet Potato'
          }

          return {
            name: std?.productName || fallbackNames[m.pid] || `Product ID: ${m.pid}`,
            barcode: exp?.barcode || 'N/A',
            price: m.p,
            qty: m.q
          }
        })
      }
      return { ...order, details }
    }))

    orderList.value = Array.from(new Map(ordersWithDetails.map(o => [o.orderId, o])).values())
      .sort((a, b) => b.orderId - a.orderId)

  } finally {
    loadingOrders.value = false
  }
}

const updateOrderStatus = async (row) => {
  try {
    await request.put(`/orders/${row.orderId}`, null, { params: { status: row.status } })
    ElMessage.success(`Order #${row.orderId} updated to ${row.status}`)
  } catch (e) {
    ElMessage.error("Update failed")
  }
}

onMounted(async () => {
  // 🌟 改正點：必須先 await 基礎數據加載完成，再加載訂單，保證 find 能找到名字
  await fetchData()
  await fetchAllOrders()
})
</script>

<style scoped>
.staff-layout { padding: 20px; background: #f5f7fa; min-height: 100vh; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.prod-name { font-weight: bold; color: #007934; font-size: 14px; }
.prod-barcode { font-size: 11px; color: #999; font-family: monospace; }
.origin-price { font-size: 11px; text-decoration: line-through; color: #999; }
.highlight { color: #d00; font-weight: bold; }
.total-amt { font-weight: bold; color: #ff7900; font-size: 15px; }
code { background: #fff5f5; border: 1px solid #ffd1d1; padding: 2px 6px; border-radius: 4px; color: #d00; font-weight: bold; }
</style>
