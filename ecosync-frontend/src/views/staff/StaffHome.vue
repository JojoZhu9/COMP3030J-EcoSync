<template>
  <div class="staff-layout">
    <el-tabs v-model="activeTab" type="border-card" class="main-tabs">

      <el-tab-pane name="inventory">
        <template #label><el-icon><Box /></el-icon><span> Shelf Inventory</span></template>
        <el-row :gutter="20">
          <el-col :span="9"><ProductEntry @refresh="fetchStock" /></el-col>
          <el-col :span="15">
            <el-card shadow="never">
              <el-table :data="stockList" border height="550" size="small">
                <el-table-column label="ID" width="70"><template #default="{row}">#{{ row.productId || row.product_id }}</template></el-table-column>
                <el-table-column prop="barcode" label="Barcode" width="140" />
                <el-table-column label="Stock" width="80"><template #default="{row}">{{ row.remainingStock || row.remaining_stock }}</template></el-table-column>
                <el-table-column label="Status"><template #default="{row}"><el-tag size="small">{{ row.status }}</el-tag></template></el-table-column>
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
              <span class="title">All Orders (Aggregated from User IDs)</span>
              <el-button type="primary" @click="fetchAllOrders" :loading="loadingOrders">
                <el-icon><Refresh /></el-icon> Start Scanning All Users
              </el-button>
            </div>
          </template>

          <el-table :data="orderList" v-loading="loadingOrders" border height="600">
            <el-table-column label="Order ID" width="90">
              <template #default="{row}">#{{ row.orderId || row.order_id }}</template>
            </el-table-column>
            <el-table-column label="User ID" width="80">
              <template #default="{row}">{{ row.userId || row.user_id }}</template>
            </el-table-column>
            <el-table-column label="Amount" width="100">
              <template #default="{row}">¥{{ row.totalAmount || row.total_amount }}</template>
            </el-table-column>
            <el-table-column label="Pickup Code" width="160">
              <template #default="{row}"><code>{{ row.pickupCode || row.pickup_code }}</code></template>
            </el-table-column>
            <el-table-column label="Status (Sync to DB)">
              <template #default="{row}">
                <el-select v-model="row.status" size="small" @change="updateStatus(row)">
                  <el-option label="Pending" value="PENDING" />
                  <el-option label="Paid" value="PAID" />
                  <el-option label="Awaiting Pickup" value="AWAITING_PICKUP" />
                  <el-option label="Completed" value="COMPLETED" />
                  <el-option label="Cancelled" value="CANCELLED" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Box, List, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import ProductEntry from './productentry.vue'

const activeTab = ref('inventory')
const loading = ref(false)
const loadingOrders = ref(false)
const stockList = ref([])
const orderList = ref([])
const storeId = localStorage.getItem('lastStoreId') || '1'

const formatDate = (val: string) => val ? val.replace('T', ' ').substring(0, 16) : '-'

const fetchStock = async () => {
  loading.value = true
  try {
    const res: any = await request.get(`/expiring-products/store/${storeId}`)
    stockList.value = res.data || res
  } finally { loading.value = false }
}

// 🚀 核心修改：暴力扫描 User ID 模式
const fetchAllOrders = async () => {
  loadingOrders.value = true
  orderList.value = [] // 先清空

  // 假设活跃的 User ID 在 1 到 50 之间
  const scanRange = Array.from({ length: 50 }, (_, i) => i + 1)

  try {
    // 并发请求所有 User ID 的订单
    const promises = scanRange.map(id =>
      request.get(`/orders/user/${id}`).catch(() => null) // 忽略报错的 ID
    )

    const results = await Promise.all(promises)

    // 把所有非空的结果合并在一起
    let allFoundOrders = []
    results.forEach((res: any) => {
      if (res && res.data && res.data.length > 0) {
        allFoundOrders = [...allFoundOrders, ...res.data]
      } else if (Array.isArray(res) && res.length > 0) {
        allFoundOrders = [...allFoundOrders, ...res]
      }
    })

    // 去重（防止同一个订单被多次扫描出）
    const uniqueOrders = Array.from(new Map(allFoundOrders.map(item => [item.orderId || item.order_id, item])).values())

    orderList.value = uniqueOrders
    ElMessage.success(`Scan complete! Found ${uniqueOrders.length} orders.`)
  } catch (e) {
    ElMessage.error("Scan failed.")
  } finally {
    loadingOrders.value = false
  }
}

// 修改状态 (注意：后端如果没有 PUT 接口，这里改完刷新还是会变回去)
const updateStatus = async (row: any) => {
  const id = row.orderId || row.order_id
  try {
    // 这是一个猜测的地址，后端不一定有
    await request.put(`/orders/${id}`, { status: row.status })
    ElMessage.success("Status updated locally (Verify if DB changed)")
  } catch (e) {
    ElMessage.error("Update failed: Backend missing PUT /api/orders/{id}")
    fetchAllOrders() // 刷新恢复
  }
}

onMounted(() => {
  fetchStock()
  fetchAllOrders()
})
</script>

<style scoped>
.staff-layout { padding: 20px; background: #f5f7fa; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-weight: bold; }
code { background: #eee; padding: 2px 4px; border-radius: 4px; color: #d00; }
</style>
