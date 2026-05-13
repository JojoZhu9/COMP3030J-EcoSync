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
          <el-col :span="8">
            <div class="side-panel">
              <ProductEntry :library="library" @refresh="fetchData" />
            </div>
          </el-col>
          <el-col :span="16">
            <el-card class="modern-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <div class="title-group"><span class="dot"></span><span class="title-text">Live Shelf Stock</span></div>
                  <el-button @click="fetchData" :loading="loading" icon="Refresh" circle />
                </div>
              </template>
              <el-table :data="enrichedStockList" border stripe height="620">
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
                    <span :class="['stock-num', row.remainingStock < 5 ? 'critical' : '']">{{ row.remainingStock }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="Status" width="130" align="center">
                  <template #default="{row}">
                    <el-tag :type="getInventoryStatusTag(row.status)" effect="dark" round size="small">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="Action" width="150" align="center">
                  <template #default="{row}">
                    <el-select v-model="row.status" size="small" @change="updateInventoryStatus(row)">
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
              <div class="title-group"><span class="dot orange"></span><span class="title-text">Recent Pickup Orders</span></div>
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

            <el-table-column label="Order ID" width="100" align="center">
              <template #default="{row}"><b>#{{ row.orderId }}</b></template>
            </el-table-column>

            <el-table-column label="Pickup Code" width="150" align="center">
              <template #default="{row}"><code class="code-badge">{{ row.pickupCode }}</code></template>
            </el-table-column>

            <el-table-column label="Amount" width="120" align="right">
              <template #default="{row}"><span class="amount-text">¥{{ row.totalAmount.toFixed(2) }}</span></template>
            </el-table-column>

            <el-table-column label="Status" width="140" align="center">
              <template #default="{row}">
                <el-tag :type="getOrderStatusTag(row.status)">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="Management" width="220" align="center">
              <template #default="{row}">
                <div v-if="row.status === 'AVAILABLE' || row.status === 'PAID' || row.status === 'AWAITING_PICKUP'">
                  <el-button
                    type="success"
                    size="small"
                    @click="handlePickup(row)"
                    icon="Check"
                  >完成核销</el-button>
                </div>
                <span v-else class="time-text">No Action Needed</span>
              </template>
            </el-table-column>

            <el-table-column label="Order Time" min-width="160" align="center">
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
import { Box, List, Refresh, ShoppingBag, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import ProductEntry from './ProductEntry.vue'

const activeTab = ref('inventory')
const loading = ref(false)
const loadingOrders = ref(false)
const library = ref([])
const allExpiringProducts = ref([])
const orderList = ref([])

// 1. 初始化获取基础数据
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

// 2. 更新库存状态 (对应后端 ExpiringProductController.java: PUT /api/expiring-products/{id})
const updateInventoryStatus = async (row) => {
  try {
    await request.put(`/expiring-products/${row.productId}`, {
      productId: row.productId,
      status: row.status // 发送 AVAILABLE, SOLD_OUT 或 DISCARDED
    })
    ElMessage.success('Stock updated successfully')
  } catch (e) { ElMessage.error('Update failed') }
}

// 3. 核心：核销订单 (对应后端 OrderController.java: POST /api/orders/pickup)
// 解决了之前 404 的问题，因为后端没有 PUT /orders 接口，只有这个 pickup 接口
const handlePickup = async (row) => {
  try {
    await ElMessageBox.confirm(`确认核销提货码 [${row.pickupCode}] 吗?`, '订单提货确认')

    // 后端要求是 POST 且 pickupCode 作为 Query 参数 (@RequestParam)
    const res: any = await request.post('/orders/pickup', null, {
      params: { pickupCode: row.pickupCode }
    })

    // 处理后端返回的 String 结果
    if (typeof res === 'string' && res.includes('失败')) {
      ElMessage.error(res)
    } else {
      ElMessage.success('核销成功！订单已转为完成状态')
      syncAllOrders() // 刷新列表查看最新状态
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// 4. 获取订单列表
const syncAllOrders = async () => {
  loadingOrders.value = true
  try {
    // 模拟扫描多用户订单（实际环境应由后端提供分页查询）
    const scanRange = Array.from({ length: 20 }, (_, i) => i + 1)
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
          return { ...item, productName: prod?.productName || `SKU:${item.productId}` }
        })
        return { ...order, items: enrichedItems }
      } catch (e) { return { ...order, items: [] } }
    }))

    orderList.value = Array.from(new Map(ordersWithDetails.map(o => [o.orderId, o])).values())
      .sort((a, b) => b.orderId - a.orderId)
  } catch (e) { ElMessage.error('Sync failed') } finally { loadingOrders.value = false }
}

const getInventoryStatusTag = (s: string) => {
  const map = { 'AVAILABLE': 'success', 'SOLD_OUT': 'info', 'DISCARDED': 'danger' }
  return map[s] || 'warning'
}

const getOrderStatusTag = (s: string) => {
  if (s === 'COMPLETED' || s === 'SOLD_OUT') return 'success'
  if (s === 'CANCELLED' || s === 'DISCARDED') return 'danger'
  return 'warning' // PENDING, AWAITING_PICKUP 等
}

const formatDate = (val: string) => val ? val.replace('T', ' ').substring(0, 16) : '-'

const enrichedStockList = computed(() => {
  return allExpiringProducts.value.map(stock => {
    const std = library.value.find(p => String(p.barcode) === String(stock.barcode))
    return { ...stock, productName: std?.productName || 'Unknown' }
  })
})

onMounted(() => { fetchData(); syncAllOrders(); })
</script>

<style scoped>
/* 保持你的美化样式不变 */
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
.amount-text { color: #EE7203; font-weight: 800; }
.code-badge { background: #f1f2f6; padding: 5px 10px; border-radius: 6px; font-family: monospace; font-weight: bold; }
.order-detail-wrapper { padding: 20px; background: #fafafa; border-left: 5px solid #EE7203; }
.time-text { font-size: 12px; color: #7f8c8d; }
</style>
