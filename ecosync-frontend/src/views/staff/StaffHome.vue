<template>
  <div class="staff-app-container">
    <div class="top-brand-bar">
      <div class="brand-content">
        <span class="brand-logo">7-ELEVEn</span>
        <span class="brand-title">{{ $t('staff.brandTitle') }}</span>
      </div>
      <div class="brand-right">
        <el-tag effect="dark" type="warning" round class="role-tag">{{ $t('staff.staffConsole') }}</el-tag>
      </div>
    </div>

    <div class="main-content">
      <el-tabs v-model="activeTab" class="brand-tabs">
        <el-tab-pane name="inventory">
          <template #label>
            <div class="tab-label"><el-icon><Box /></el-icon><span>{{ $t('staff.inventoryConsole') }}</span></div>
          </template>

          <el-row :gutter="24">
            <el-col :span="7">
              <div class="side-panel">
                <ProductEntry :library="library" @refresh="fetchData" />
              </div>
            </el-col>

            <el-col :span="17">
              <el-card class="modern-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <div class="title-group">
                      <div class="title-icon"><el-icon><Box /></el-icon></div>
                      <div class="title-text-box">
                        <div class="title-text">{{ $t('staff.liveShelfStock') }}</div>
                        <div class="title-sub">{{ $t('staff.liveShelfStockSub') }}</div>
                      </div>
                    </div>
                    <el-button type="primary" class="brand-btn" @click="fetchData" :loading="loading">
                      <el-icon><Refresh /></el-icon> {{ $t('common.sync') }}
                    </el-button>
                  </div>
                </template>

                <div class="table-toolbar">
                  <el-input
                    v-model="searchQuery"
                    :placeholder="$t('staff.searchProductPlaceholder')"
                    :prefix-icon="Search"
                    clearable
                    class="search-input"
                  />
                  <el-radio-group v-model="statusFilter" class="custom-radio">
                    <el-radio-button label="ALL">{{ $t('staff.allItems') }}</el-radio-button>
                    <el-radio-button label="AVAILABLE">{{ $t('staff.inStock') }}</el-radio-button>
                    <el-radio-button label="SOLD_OUT">{{ $t('staff.soldOut') }}</el-radio-button>
                  </el-radio-group>
                </div>

                <el-table :data="filteredStockList" class="custom-table" border stripe height="540">
                  <el-table-column :label="$t('staff.productInfo')" min-width="200">
                    <template #default="{row}">
                      <div class="product-info-cell">
                        <div class="p-name" :title="row.productName">{{ row.productName }}</div>
                        <div class="p-bc">{{ $t('staff.barcodeShort') }}: {{ row.barcode }}</div>
                      </div>
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('staff.expirationDate')" width="180" align="center">
                    <template #default="{row}">
                      <div class="expiry-time-cell">
                        <el-icon><Timer /></el-icon>
                        <span>{{ formatDateTime(row.expirationTime || row.expirationDate) }}</span>
                      </div>
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('staff.stock')" width="100" align="center">
                    <template #default="{row}">
                      <span :class="['stock-num', row.remainingStock < 5 ? 'critical' : '']">
                        {{ row.remainingStock }}
                      </span>
                    </template>
                  </el-table-column>

                  <el-table-column :label="$t('staff.status')" width="140" align="center">
                    <template #default="{row}">
                      <el-tag
                        :type="isExpired(row) ? 'danger' : (row.remainingStock <= 0 ? 'info' : 'success')"
                        round effect="light" class="status-tag"
                      >
                        {{ isExpired(row) ? $t('staff.expired') : (row.remainingStock <= 0 ? $t('staff.soldOut') : $t('staff.inStock')) }}
                      </el-tag>
                    </template>
                  </el-table-column>

                  <template #empty>
                    <el-empty :description="$t('staff.noProducts')" :image-size="100" />
                  </template>
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane name="orders">
          <template #label>
            <div class="tab-label"><el-icon><List /></el-icon><span>{{ $t('staff.orderMonitor') }}</span></div>
          </template>

          <el-card class="modern-card order-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="title-group">
                  <div class="title-icon orange"><el-icon><ShoppingBag /></el-icon></div>
                  <div class="title-text-box">
                    <div class="title-text">{{ $t('staff.recentPickupOrders') }}</div>
                    <div class="title-sub">{{ $t('staff.recentPickupOrdersSub') }}</div>
                  </div>
                </div>
                <el-button type="warning" class="warning-btn" @click="syncAllOrders" :loading="loadingOrders">
                  <el-icon><Refresh /></el-icon> {{ $t('staff.syncOrders') }}
                </el-button>
              </div>
            </template>

            <!-- Pickup Identifier 自动核销 -->
            <div class="verify-bar">
              <el-input
                v-model="pickupCodeInput"
                :placeholder="$t('staff.verifyPlaceholder')"
                clearable
                class="verify-input"
                @keyup.enter="verifyByPickupCode"
              >
                <template #prefix><el-icon><Ticket /></el-icon></template>
              </el-input>
              <el-button type="success" class="verify-btn" :loading="verifying" @click="verifyByPickupCode">
                <el-icon><Check /></el-icon> {{ $t('staff.verify') }}
              </el-button>
            </div>

            <!-- 搜索 & 筛选 -->
            <div class="table-toolbar">
              <el-input
                v-model="orderSearchQuery"
                :placeholder="$t('staff.searchOrderPlaceholder')"
                :prefix-icon="Search"
                clearable
                class="search-input"
              />
              <el-radio-group v-model="orderStatusFilter" class="custom-radio">
                <el-radio-button label="ALL">{{ $t('common.all') }}</el-radio-button>
                <el-radio-button label="PENDING">{{ $t('staff.pending') }}</el-radio-button>
                <el-radio-button label="PAID">{{ $t('staff.paid') }}</el-radio-button>
                <el-radio-button label="AWAITING_PICKUP">{{ $t('staff.awaitingPickup') }}</el-radio-button>
                <el-radio-button label="COMPLETED">{{ $t('staff.completed') }}</el-radio-button>
                <el-radio-button label="CANCELLED">{{ $t('staff.cancelled') }}</el-radio-button>
              </el-radio-group>
            </div>

            <el-table :data="filteredOrderList" class="custom-table" border highlight-current-row height="540">
              <el-table-column type="expand">
                <template #default="{ row }">
                  <div class="order-detail-wrapper">
                    <div class="detail-header"><el-icon><ShoppingBag /></el-icon> {{ $t('staff.orderItems') }} ({{row.items?.length || 0}})</div>
                    <el-table :data="row.items" size="small" border class="inner-table">
                      <el-table-column :label="$t('staff.itemName')" prop="productName" />
                      <el-table-column :label="$t('staff.price')" width="120" align="right">
                        <template #default="s"><span class="inner-price">¥{{ s.row.actualPrice?.toFixed(2) }}</span></template>
                      </el-table-column>
                      <el-table-column :label="$t('staff.qty')" prop="quantity" width="80" align="center">
                        <template #default="s"><b>x{{ s.row.quantity }}</b></template>
                      </el-table-column>
                    </el-table>
                  </div>
                </template>
              </el-table-column>

              <el-table-column :label="$t('staff.orderId')" width="120" align="center">
                <template #default="{row}"><span class="order-id-col">ORD-{{ row.orderId }}</span></template>
              </el-table-column>

              <el-table-column :label="$t('staff.customer')" width="140" align="center">
                <template #default="{row}"><span class="customer-col">{{ row.customerName || $t('common.unknown') }}</span></template>
              </el-table-column>

              <el-table-column :label="$t('staff.pickupIdentifier')" width="200" align="center">
                <template #default="{row}"><code class="code-badge">{{ row.pickupCode }}</code></template>
              </el-table-column>

              <el-table-column :label="$t('staff.finalPayment')" width="140" align="right">
                <template #default="{row}"><span class="amount-text">¥{{ row.totalAmount?.toFixed(2) }}</span></template>
              </el-table-column>

              <el-table-column :label="$t('staff.orderStatus')" width="180" align="center">
                <template #default="{row}">
                  <el-select
                    v-model="row.status"
                    size="default"
                    @change="handleOrderUpdate(row)"
                    :disabled="row.status === 'CANCELLED' || row.status === 'COMPLETED'"
                    class="status-select"
                  >
                    <el-option :label="$t('staff.pending')" value="PENDING" />
                    <el-option :label="$t('staff.paid')" value="PAID" />
                    <el-option :label="$t('staff.awaitingPickup')" value="AWAITING_PICKUP" />
                    <el-option :label="$t('staff.completed')" value="COMPLETED" />
                    <el-option :label="$t('staff.cancelled')" value="CANCELLED" />
                  </el-select>
                </template>
              </el-table-column>

              <el-table-column :label="$t('staff.createdTimestamp')" min-width="160" align="center">
                <template #default="{row}">
                  <span class="timestamp-col">{{ formatDateTime(row.createdAt) }}</span>
                </template>
              </el-table-column>

              <template #empty>
                <el-empty :description="$t('staff.noOrders')" :image-size="100" />
              </template>
            </el-table>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Box, List, Refresh, ShoppingBag, Timer, Search, Ticket, Check } from '@element-plus/icons-vue'
import { ElMessage } from '@/utils/message'
import request from '@/utils/request'
import ProductEntry from './ProductEntry.vue'

const { t } = useI18n()

const activeTab = ref('inventory')
const loading = ref(false)
const loadingOrders = ref(false)
const library = ref<any[]>([])
const allExpiringProducts = ref<any[]>([])
const orderList = ref<any[]>([])
const userMap = ref<Record<number, string>>({})

// === 新增：过滤与搜索相关的状态 ===
const searchQuery = ref('')
const statusFilter = ref('ALL') // 'ALL' | 'AVAILABLE' | 'SOLD_OUT'

// === Order Monitor 搜索、筛选、核销 ===
const orderSearchQuery = ref('')
const orderStatusFilter = ref('ALL')
const pickupCodeInput = ref('')
const verifying = ref(false)

// 1. 数据获取
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
    ElMessage.error(t('staff.dataSyncFailed'))
  } finally { loading.value = false }
}

// 2. 订单状态更新写入数据库
const handleOrderUpdate = async (row: any) => {
  // 防御性检查：如果订单已经是 CANCELLED 或 COMPLETED，则不允许修改
  if (row.status === 'CANCELLED' || row.status === 'COMPLETED') {
    ElMessage.warning(t('staff.orderCannotModify'))
    return
  }
  try {
    await request.put(`/orders/${row.orderId}`, {
      status: row.status
    })
    ElMessage.success(t('staff.orderStatusUpdated'))
  } catch (e) {
    ElMessage.error(t('staff.orderStatusUpdateFailed'))
    syncAllOrders()
  }
}

const fetchUsers = async () => {
  try {
    const res: any = await request.get('/users')
    const users = res.data || res || []
    const map: Record<number, string> = {}
    users.forEach((u: any) => {
      map[u.userId] = u.username || `${t('common.unknown')} #${u.userId}`
    })
    userMap.value = map
  } catch (e) {}
}

// 3. 订单同步
const syncAllOrders = async () => {
  loadingOrders.value = true
  try {
    const scanRange = Array.from({ length: 30 }, (_, i) => i + 1)
    const results = await Promise.all(scanRange.map(id => request.get(`/orders/user/${id}`).catch(() => null)))

    let rawOrders: any[] = []
    results.forEach((res: any) => {
      const data = res?.data || res
      if (Array.isArray(data)) rawOrders = [...rawOrders, ...data]
    })

    const ordersWithDetails = await Promise.all(rawOrders.map(async (order) => {
      try {
        const detailRes: any = await request.get(`/orders/${order.orderId}/details`)
        const vo = detailRes.data || detailRes
        const enrichedItems = (vo.items || []).map((item: any) => {
          const stock = allExpiringProducts.value.find(s => Number(s.productId) === Number(item.productId))
          const prod = library.value.find(p => String(p.barcode) === String(stock?.barcode))
          return { ...item, productName: prod?.productName || `${t('common.unknown')} SKU` }
        })
        return { ...order, items: enrichedItems, customerName: userMap.value[order.userId] || `${t('common.unknown')} #${order.userId}` }
      } catch (e) { return { ...order, items: [], customerName: userMap.value[order.userId] || `${t('common.unknown')} #${order.userId}` } }
    }))

    orderList.value = Array.from(new Map(ordersWithDetails.map(o => [o.orderId, o])).values())
      .sort((a, b) => b.orderId - a.orderId)
  } catch (e) {
    ElMessage.error(t('staff.syncFailed'))
  } finally { loadingOrders.value = false }
}

const formatDateTime = (val: string) => {
  if (!val) return t('common.na')
  return val.replace('T', ' ').substring(0, 16)
}

const isExpired = (row: any) => {
  const expiry = new Date(row.expirationTime || row.expirationDate)
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return expiry.getTime() < now.getTime()
}

// 订单列表：搜索 + 筛选
const filteredOrderList = computed(() => {
  let result = orderList.value

  if (orderSearchQuery.value.trim()) {
    const q = orderSearchQuery.value.toLowerCase()
    result = result.filter(o =>
      String(o.orderId).includes(q) ||
      (o.customerName || '').toLowerCase().includes(q) ||
      (o.pickupCode || '').toLowerCase().includes(q)
    )
  }

  if (orderStatusFilter.value !== 'ALL') {
    result = result.filter(o => o.status === orderStatusFilter.value)
  }

  return result
})

const verifyByPickupCode = async () => {
  const code = pickupCodeInput.value.trim()
  if (!code) return ElMessage.warning(t('staff.enterPickupCode'))

  const target = orderList.value.find(o => o.pickupCode === code)
  if (!target) return ElMessage.error(t('staff.orderNotFound'))
  if (target.status === 'COMPLETED') return ElMessage.info(t('staff.orderAlreadyCompleted'))
  if (target.status === 'CANCELLED') return ElMessage.warning(t('staff.orderCancelled'))

  verifying.value = true
  try {
    await request.put(`/orders/${target.orderId}`, { status: 'COMPLETED' })
    target.status = 'COMPLETED'
    ElMessage.success(t('staff.orderVerified'))
    pickupCodeInput.value = ''
  } catch (e) {
    ElMessage.error(t('staff.verificationFailed'))
  } finally {
    verifying.value = false
  }
}

// 基础关联数据
const enrichedStockList = computed(() => {
  return allExpiringProducts.value.map(stock => {
    const std = library.value.find(p => String(p.barcode) === String(stock.barcode))
    return { ...stock, productName: std?.productName || `${t('common.unknown')} SKU` }
  })
})

// === 新增：过滤后的展示数据 ===
const filteredStockList = computed(() => {
  let result = enrichedStockList.value

  // 模糊搜索
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(item =>
      (item.productName || '').toLowerCase().includes(q) ||
      (item.barcode || '').toLowerCase().includes(q)
    )
  }

  // 状态过滤
  if (statusFilter.value === 'AVAILABLE') {
    result = result.filter(item => item.remainingStock > 0)
  } else if (statusFilter.value === 'SOLD_OUT') {
    result = result.filter(item => item.remainingStock <= 0)
  }

  return result
})

onMounted(() => {
  fetchData()
  fetchUsers().then(() => syncAllOrders())
})
</script>

<style scoped>
.staff-app-container { min-height: 100vh; background-color: #f4f6f8; padding-bottom: 40px; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }

/* 顶部品牌栏 */
.top-brand-bar {
  background: linear-gradient(135deg, #008163 0%, #005a46 100%);
  height: 64px; display: flex; justify-content: space-between; align-items: center;
  padding: 0 30px; margin-bottom: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.brand-content { display: flex; align-items: center; }
.brand-logo { color: white; font-size: 24px; font-weight: 900; margin-right: 16px; border-right: 2px solid rgba(255,255,255,0.2); padding-right: 16px; letter-spacing: 1px; }
.brand-title { color: rgba(255,255,255,0.9); font-size: 15px; font-weight: 500; letter-spacing: 0.5px; }
.role-tag { font-weight: 800; letter-spacing: 1px; }

/* 主体内容 */
.main-content { max-width: 1400px; margin: 0 auto; padding: 0 24px; }

/* 标签页美化 */
:deep(.brand-tabs .el-tabs__item) { font-size: 15px; font-weight: 600; color: #64748b; height: 50px; }
:deep(.brand-tabs .el-tabs__item.is-active) { color: #008163; font-weight: 800; }
:deep(.brand-tabs .el-tabs__active-bar) { background-color: #008163; height: 3px; border-radius: 3px; }
:deep(.brand-tabs .el-tabs__nav-wrap::after) { background-color: #e2e8f0; }
.tab-label { display: flex; align-items: center; gap: 8px; }

/* 卡片美化 */
.modern-card { border-radius: 16px; border: 1px solid rgba(0,0,0,0.04); box-shadow: 0 4px 20px rgba(0,0,0,0.03) !important; background: #fff; }
:deep(.modern-card .el-card__header) { border-bottom: 1px solid #f1f5f9; padding: 20px 24px; }
:deep(.modern-card .el-card__body) { padding: 0; } /* 消除内边距让表格贴边 */

.card-header { display: flex; justify-content: space-between; align-items: center; }
.title-group { display: flex; align-items: center; gap: 14px; }
.title-icon { width: 40px; height: 40px; border-radius: 10px; background: linear-gradient(135deg, #008163, #006b52); color: white; display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: 0 4px 10px rgba(0,129,99,0.2); }
.title-icon.orange { background: linear-gradient(135deg, #ff7900, #ee7203); box-shadow: 0 4px 10px rgba(238,114,3,0.2); }
.title-text-box { display: flex; flex-direction: column; }
.title-text { font-size: 16px; font-weight: 800; color: #1e293b; }
.title-sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* 按钮美化 */
.brand-btn { background: #008163 !important; border: none; font-weight: 700; border-radius: 8px; }
.brand-btn:hover { background: #006b52 !important; box-shadow: 0 4px 12px rgba(0,129,99,0.2); }
.warning-btn { background: #EE7203 !important; border: none; font-weight: 700; border-radius: 8px; }

/* === 工具栏 (搜索 & 过滤) === */
.table-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; background: #fdfdfd; border-bottom: 1px solid #f1f5f9; }
.search-input { width: 320px; }
.search-input :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 8px; box-shadow: 0 0 0 1px #e2e8f0 inset; }
.search-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(0,129,99,0.4) inset; }

/* 表格全局美化 */
.custom-table { border-radius: 0 0 16px 16px; }
:deep(.el-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569; font-weight: 700; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; }

/* 库存单元格样式 */
.product-info-cell { display: flex; flex-direction: column; }
.p-name { font-weight: 700; color: #1e293b; font-size: 14px; margin-bottom: 4px; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.p-bc { font-size: 11px; color: #94a3b8; font-family: monospace; font-weight: 600; }
.stock-num { font-weight: 900; font-size: 16px; color: #1e293b; }
.stock-num.critical { color: #e2231a; }
.expiry-time-cell { display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 13px; color: #475569; font-weight: 500; }
.status-tag { font-weight: 800; letter-spacing: 0.5px; }

/* 订单单元格样式 */
.order-id-col { font-weight: 800; color: #64748b; font-family: monospace; }
.customer-col { font-weight: 700; color: #334155; font-size: 13px; }
.amount-text { color: #EE7203; font-weight: 900; font-size: 16px; }
.code-badge { background: #f1f5f9; color: #008163; padding: 6px 12px; border-radius: 6px; font-family: monospace; font-weight: 900; font-size: 14px; border: 1px dashed #cbd5e1; }
.status-select :deep(.el-input__wrapper) { border-radius: 8px; font-weight: 600; }
.timestamp-col { font-size: 13px; color: #64748b; }

/* 订单展开详情 */
.order-detail-wrapper { padding: 20px 24px; background: #f8fafc; border-left: 4px solid #EE7203; margin: 0; box-shadow: inset 0 2px 8px rgba(0,0,0,0.02); }
.detail-header { font-weight: 800; color: #1e293b; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; font-size: 14px; }
.inner-table { border-radius: 8px; overflow: hidden; }
.inner-price { color: #EE7203; font-weight: 700; }

.side-panel { background: white; padding: 24px; border-radius: 16px; height: 100%; box-sizing: border-box; border: 1px solid rgba(0,0,0,0.04); box-shadow: 0 4px 20px rgba(0,0,0,0.03); }

/* 核销栏 */
.verify-bar { display: flex; gap: 12px; padding: 16px 24px; background: #f8fafc; border-bottom: 1px solid #f1f5f9; }
.verify-input { flex: 1; }
.verify-input :deep(.el-input__wrapper) { background: #fff; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; }
.verify-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(0,129,99,0.4) inset; }
.verify-btn { background: #008163 !important; border: none !important; font-weight: 700; border-radius: 10px; padding: 0 20px; }
</style>
