<template>
  <div class="checkout-page">
    <div class="checkout-header">
      <div class="header-left">
        <div class="nav-back" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <h2 class="page-title">Shopping Cart</h2>
      </div>
      <el-button
        type="danger"
        link
        class="clear-action"
        @click="handleClearCart"
        :disabled="cartItems.length === 0"
      >
        <el-icon style="margin-right: 4px"><Delete /></el-icon>Clear All
      </el-button>
    </div>

    <div class="checkout-body" v-if="cartItems.length > 0">
      <div class="fulfillment-card">
        <div class="fulfillment-header">
          <span class="label">
            <el-icon class="label-icon"><LocationInformation /></el-icon>
            Delivery Logistics
          </span>
          <el-button type="primary" link @click="$router.push('/profile')">Edit Details</el-button>
        </div>

        <div class="address-content" v-if="userAddress && userAddress !== 'null'">
          <div class="addr-main">{{ userAddress }}</div>
          <div class="contact-sub">
            <span class="user-tag">Recipient</span>
            <span class="phone-num">{{ userPhone || 'No phone provided' }}</span>
          </div>
        </div>
        <div class="address-placeholder" v-else @click="$router.push('/profile')">
          <div class="empty-prompt">
            <el-icon class="pulse-icon"><WarningFilled /></el-icon>
            <span>Set delivery address to proceed</span>
          </div>
        </div>
      </div>

      <div class="items-container">
        <div class="section-header">
          <h3 class="section-title">Purchase Summary</h3>
          <span class="item-count">{{ cartItems.length }} items</span>
        </div>

        <div v-for="item in cartItems" :key="item.cartItemId"
             class="item-tile" :class="{'is-selected': item.selected}">
          <div class="item-check">
            <el-checkbox v-model="item.selected" size="large" />
          </div>

          <div class="item-preview">
            <div class="placeholder-img">
              <el-icon :size="28"><Box /></el-icon>
            </div>
          </div>

          <div class="item-details">
            <div class="name-row">
              <span class="product-name">{{ item.productName }}</span>
              <el-button type="info" link @click="deleteSingleItem(item.cartItemId)" class="mini-remove">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>

            <div class="stock-info">
              SKU: {{ item.barcode }} |
              <span :class="{'low-stock': item.maxStock <= 5}">Stock: {{ item.maxStock }}</span>
            </div>

            <div class="price-qty-row">
              <div class="price-tag">
                <span class="currency">元</span>
                <span class="val">{{ Number(item.pointsPrice).toFixed(2) }}</span>
              </div>

              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.maxStock"
                size="small"
                controls-position="right"
                @change="(val) => updateCartQuantity(item, val)"
                class="brand-stepper"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="summary-tile">
        <div class="summary-row">
          <span class="s-label">Selected Subtotal</span>
          <span class="s-value">元 {{ totalPrice }}</span>
        </div>
        <div class="summary-row">
          <span class="s-label">Service Fee</span>
          <span class="s-value free">WAIVED</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-cart-view">
      <el-empty description="Your basket is currently empty">
        <el-button type="success" plain round @click="$router.push('/home')">Browse Products</el-button>
      </el-empty>
    </div>

    <div class="sticky-checkout" v-if="cartItems.length > 0">
      <div class="total-info">
        <span class="total-label">Grand Total ({{ selectedItems.length }} items)</span>
        <div class="total-price-box">
          <span class="total-val">{{ totalPrice }}</span>
          <span class="total-currency">元</span>
        </div>
      </div>
      <el-button
        type="success"
        class="checkout-main-btn"
        @click="openConfirmDialog"
        :disabled="selectedItems.length === 0"
      >
        Checkout
      </el-button>
    </div>

    <el-dialog v-model="confirmVisible" title="Confirm Payment" width="90%" align-center class="confirm-dialog">
      <div class="confirm-box">
        <div class="confirm-scroll-area">
          <div v-for="item in selectedItems" :key="item.cartItemId" class="confirm-item">
            <span class="c-name">{{ item.productName }} <small>x{{ item.quantity }}</small></span>
            <span class="c-price">{{ (item.pointsPrice * item.quantity).toFixed(2) }} 元</span>
          </div>
        </div>
        <el-divider border-style="dashed" />
        <div class="confirm-total">
          <span>Total Deduction:</span>
          <span class="total-highlight">{{ totalPrice }} 元</span>
        </div>
        <div class="confirm-address-box">
          <div class="c-title">Delivery To:</div>
          <div class="c-desc">{{ userAddress }}</div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="confirmVisible = false" round>Cancel</el-button>
          <el-button type="success" @click="executePayment" round :loading="checkingOut">Confirm & Pay</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, Delete, LocationInformation, WarningFilled, Box, Close } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartItems = ref<any[]>([])
const userAddress = ref('')
const userPhone = ref('')
const confirmVisible = ref(false)
const checkingOut = ref(false)

const getUserId = () => localStorage.getItem('userId')

onMounted(async () => {
  const uid = getUserId()
  if (!uid) return router.push('/login')
  fetchUserProfile(uid)
  fetchDatabaseCart(uid)
})

const fetchUserProfile = async (uid: string) => {
  try {
    const res: any = await request.get(`/users/${uid}`)
    userAddress.value = res.data?.defaultAddress || ''
    userPhone.value = res.data?.phoneNumber || ''
  } catch (e) { console.error(e) }
}

const fetchDatabaseCart = async (uid: string) => {
  try {
    const cartRes: any = await request.get(`/cart/user/${uid}`)
    const rawCartItems = cartRes.data || cartRes || []

    const enrichedItems = await Promise.all(rawCartItems.map(async (item: any) => {
      try {
        const expRes: any = await request.get(`/expiring-products/${item.productId}`)
        const expData = expRes.data || expRes
        const stdRes: any = await request.get(`/products/${expData.barcode}`)
        const stdData = stdRes.data || stdRes

        return {
          ...item,
          barcode: expData.barcode,
          productName: stdData.productName || 'Unknown Product',
          pointsPrice: Number(stdData.normalPrice || 0),
          maxStock: Number(expData.remainingStock || 0),
          selected: true
        }
      } catch (err) {
        return { ...item, productName: 'Item Info Missing', pointsPrice: 0, maxStock: 0, selected: false }
      }
    }))
    cartItems.value = enrichedItems
  } catch (e) {
    ElMessage.error('Cart sync failed')
  }
}

const updateCartQuantity = async (item: any, qty: number) => {
  await request.put(`/cart/${item.cartItemId}?quantity=${qty}`)
}

const deleteSingleItem = async (id: number) => {
  await request.delete(`/cart/${id}`)
  cartItems.value = cartItems.value.filter(i => i.cartItemId !== id)
  ElMessage.success('Removed from basket')
}

const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('Clear all items?', 'Warning', { type: 'warning', roundButton: true })
    await request.delete(`/cart/user/${getUserId()}`)
    cartItems.value = []
  } catch (e) {}
}

const selectedItems = computed(() => cartItems.value.filter(i => i.selected))
const totalPrice = computed(() => {
  return selectedItems.value.reduce((acc, i) => acc + (i.pointsPrice * i.quantity), 0).toFixed(2)
})

const openConfirmDialog = () => {
  if (!userAddress.value || userAddress.value === 'null') {
    return ElMessage.warning('Please set a delivery address first')
  }
  confirmVisible.value = true
}

const executePayment = async () => {
  checkingOut.value = true
  try {
    // 這裡模擬支付流程，支付後清空購物車
    await request.delete(`/cart/user/${getUserId()}`)
    ElMessage.success('Order Success!')
    router.push('/order-status')
  } catch (e) {
    ElMessage.error('Payment failed')
  } finally {
    checkingOut.value = false
    confirmVisible.value = false
  }
}
</script>

<style scoped>
.checkout-page { background: #f8fafc; min-height: 100vh; padding-bottom: 110px; }

/* Header 樣式 */
.checkout-header { background: #fff; padding: 16px 20px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f1f5f9; position: sticky; top: 0; z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.nav-back { cursor: pointer; display: flex; align-items: center; color: #64748b; font-size: 20px; }
.page-title { font-size: 18px; font-weight: 800; color: #1e293b; margin: 0; }

/* 物流卡片 */
.fulfillment-card { background: #fff; border-radius: 16px; margin: 16px; padding: 18px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.fulfillment-header { display: flex; justify-content: space-between; margin-bottom: 12px; }
.label { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 700; color: #64748b; }
.addr-main { font-weight: 800; font-size: 16px; color: #1e293b; margin-bottom: 6px; }
.user-tag { background: #f1f5f9; padding: 2px 8px; border-radius: 6px; font-size: 11px; color: #475569; margin-right: 8px; font-weight: 700; }
.phone-num { font-size: 13px; color: #64748b; }

.empty-prompt { display: flex; align-items: center; gap: 8px; color: #ef4444; font-weight: 700; font-size: 14px; }
.pulse-icon { animation: pulse 2s infinite; }

/* 商品清單 */
.section-header { margin: 20px 16px 12px; display: flex; justify-content: space-between; align-items: baseline; }
.section-title { font-size: 16px; font-weight: 800; color: #1e293b; margin: 0; }
.item-count { font-size: 12px; color: #94a3b8; font-weight: 600; }

.item-tile { background: #fff; border-radius: 16px; margin: 0 16px 12px; padding: 14px; display: flex; align-items: center; gap: 12px; border: 1px solid transparent; transition: all 0.3s ease; }
.item-tile.is-selected { border-color: #10b981; background: #f0fdf4; box-shadow: 0 4px 15px rgba(16, 185, 129, 0.05); }

.placeholder-img { width: 68px; height: 68px; background: #f1f5f9; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #cbd5e1; }
.item-details { flex: 1; min-width: 0; }
.product-name { font-weight: 800; font-size: 15px; color: #1e293b; display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.stock-info { font-size: 11px; color: #94a3b8; margin: 4px 0 8px; font-weight: 600; }
.low-stock { color: #ef4444; }

.price-qty-row { display: flex; justify-content: space-between; align-items: center; }
.price-tag { color: #f59e0b; font-weight: 900; font-size: 18px; }
.currency { font-size: 12px; margin-right: 2px; }

/* 費用彙總 */
.summary-tile { background: #fff; border-radius: 16px; margin: 16px; padding: 16px; }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; }
.s-label { color: #64748b; font-weight: 600; }
.s-value { color: #1e293b; font-weight: 800; }
.free { color: #10b981; }

/* 底部按鈕欄 */
.sticky-checkout { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; padding: 15px 20px 35px; display: flex; align-items: center; justify-content: space-between; border-top: 1px solid #f1f5f9; box-shadow: 0 -4px 20px rgba(0,0,0,0.05); z-index: 100; }
.total-label { font-size: 12px; color: #94a3b8; font-weight: 700; display: block; }
.total-price-box { display: flex; align-items: baseline; gap: 4px; color: #f59e0b; }
.total-val { font-size: 28px; font-weight: 900; }
.total-currency { font-size: 14px; font-weight: 800; }

.checkout-main-btn { height: 50px; padding: 0 40px; border-radius: 14px; font-weight: 800; font-size: 16px; background: #007934 !important; border: none; box-shadow: 0 4px 12px rgba(0, 121, 52, 0.2); transition: transform 0.2s; }
.checkout-main-btn:active { transform: scale(0.96); }

/* 確認彈窗 */
.confirm-item { display: flex; justify-content: space-between; padding: 10px 0; font-weight: 700; }
.c-name small { color: #94a3b8; margin-left: 8px; }
.total-highlight { font-size: 22px; font-weight: 900; color: #f59e0b; }
.confirm-address-box { background: #f8fafc; padding: 12px; border-radius: 12px; margin-top: 15px; }
.c-title { font-size: 11px; color: #94a3b8; text-transform: uppercase; font-weight: 800; }
.c-desc { font-size: 13px; color: #1e293b; font-weight: 700; margin-top: 4px; }

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

:deep(.el-input-number.brand-stepper) {
  width: 100px;
}
:deep(.brand-stepper .el-input__wrapper) {
  border-radius: 8px;
  background: #f8fafc;
}
</style>
