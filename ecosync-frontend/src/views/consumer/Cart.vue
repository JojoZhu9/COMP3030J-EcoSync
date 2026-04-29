<template>
  <div class="checkout-page">
    <div class="brand-bar"></div>
    <div class="checkout-header">
      <div class="nav-back" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <h2 class="page-title">Shopping Cart</h2>
      <el-button
        type="danger"
        link
        class="clear-action"
        @click="clearSelectedCart"
        :disabled="selectedItems.length === 0"
      >
        Remove Selected
      </el-button>
    </div>

    <div class="checkout-body" v-if="cartItems.length > 0">
      <div class="fulfillment-card">
        <div class="fulfillment-header">
          <span class="label"><el-icon><LocationInformation /></el-icon> Delivery Logistics</span>
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
        <div class="fulfillment-footer-line"></div>
      </div>

      <div class="items-container">
        <h3 class="section-title">Purchase Summary</h3>
        <div v-for="item in cartItems" :key="item.id || item.cartId" class="item-tile" :class="{'is-selected': item.selected}">
          <div class="item-check">
            <el-checkbox v-model="item.selected" size="large" />
          </div>
          <div class="item-preview">
            <el-icon :size="24"><Box /></el-icon>
          </div>
          <div class="item-details">
            <div class="name-row">
              <span class="name">{{ item.productName }}</span>
              <div class="points-badge">
                <span class="val">{{ item.pointsPrice || item.price }}</span>
                <span class="unit">pts</span>
              </div>
            </div>
            <div class="stock-info">SKU: {{ item.productId }} | Stock: {{ item.stock || item.remaining_stock || 99 }}</div>
            <div class="quantity-control">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.stock || item.remaining_stock || 99"
                size="small"
                @change="(val) => updateCartQuantity(item, val)"
                class="brand-stepper"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="summary-tile">
        <div class="summary-row">
          <span>Selected Subtotal</span>
          <span>{{ totalPrice }} pts</span>
        </div>
        <div class="summary-row">
          <span>Service Fee</span>
          <span class="free-text">WAIVED</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-cart-view">
      <el-empty description="Your basket is currently empty">
        <el-button type="success" plain round @click="$router.push('/home')">Browse Products</el-button>
      </el-empty>
    </div>

    <div class="sticky-checkout" v-if="cartItems.length > 0">
      <div class="total-section">
        <span class="total-label">Grand Total ({{ selectedItems.length }} items)</span>
        <div class="total-amount">
          <span class="points-val">{{ totalPrice }}</span>
          <span class="points-label">PTS</span>
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

    <el-dialog v-model="confirmVisible" title="Confirm Payment" width="90%" align-center>
      <div class="confirm-box">
        <div v-for="item in selectedItems" :key="item.productId" class="confirm-item">
          <span class="confirm-name">{{ item.productName }} <small>x{{ item.quantity }}</small></span>
          <span class="confirm-price">{{ (item.pointsPrice || item.price) * item.quantity }} pts</span>
        </div>
        <el-divider border-style="dashed" />
        <div class="confirm-total">
          <span>Total Deduction:</span>
          <span class="total-highlight">{{ totalPrice }} pts</span>
        </div>
        <div class="confirm-address-box">
          <div class="c-title">Delivery To:</div>
          <div class="c-desc">{{ userAddress }}</div>
          <div class="c-desc">{{ userPhone }}</div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="confirmVisible = false">Cancel</el-button>
          <el-button type="success" :loading="checkingOut" @click="executePayment">Confirm & Pay</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { LocationInformation, Box, WarningFilled, ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartItems = ref<any[]>([])
const checkingOut = ref(false)
const confirmVisible = ref(false)

const userAddress = ref('')
const userPhone = ref('')
const userId = localStorage.getItem('userId')

onMounted(async () => {
  await fetchUserProfile()
  await fetchDatabaseCart()
})

const fetchUserProfile = async () => {
  try {
    const res: any = await request.get(`/users/${userId}`)
    const data = res.data || res
    userAddress.value = data.defaultAddress || ''
    userPhone.value = data.phoneNumber || ''
  } catch (e) {
    userAddress.value = localStorage.getItem('userAddress') || ''
    userPhone.value = localStorage.getItem('userPhone') || ''
  }
}

const fetchDatabaseCart = async () => {
  try {
    const res: any = await request.get(`/cart/user/${userId}`)
    const dbItems = res.data || res || []
    const localCart = JSON.parse(localStorage.getItem('cart') || '[]')

    cartItems.value = dbItems.map((item: any) => {
      const localMatch = localCart.find((l: any) => l.productId === item.productId) || {}
      return {
        ...item,
        productName: item.productName || localMatch.productName || `SKU ID: ${item.productId}`,
        pointsPrice: item.pointsPrice || item.price || localMatch.pointsPrice || 0,
        selected: true
      }
    })
  } catch (e) {
    ElMessage.error('Failed to load cart from database.')
  }
}

const updateCartQuantity = async (item: any, newQuantity: number) => {
  try {
    const cartItemId = item.id || item.cartId || item.cartItemId
    await request.put(`/cart/${cartItemId}?quantity=${newQuantity}`)
  } catch (e) {
    ElMessage.error('Failed to update quantity.')
  }
}

const selectedItems = computed(() => cartItems.value.filter(item => item.selected))

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    const price = Number(item.pointsPrice || item.price || 0)
    const qty = Number(item.quantity || 1)
    return sum + (price * qty)
  }, 0)
})

const clearSelectedCart = async () => {
  try {
    const idsToDelete = selectedItems.value.map(i => i.id || i.cartId || i.cartItemId)
    await Promise.all(idsToDelete.map(id => request.delete(`/cart/${id}`)))
    await fetchDatabaseCart()
    ElMessage.success('Selected items removed.')
  } catch(e) {
    ElMessage.error('Clear failed.')
  }
}

const openConfirmDialog = () => {
  if (!userAddress.value || userAddress.value === 'null') {
    ElMessageBox.alert('Please update your delivery profile before checkout.', 'Address Missing', {
      confirmButtonText: 'Go to Profile',
      callback: () => router.push('/profile')
    })
    return
  }
  confirmVisible.value = true
}

const executePayment = async () => {
  checkingOut.value = true
  try {
    const payload = {
      userId: Number(userId),
      storeId: Number(localStorage.getItem('lastStoreId') || 1),
      deliveryAddress: userAddress.value,
      contactPhone: userPhone.value,
      items: selectedItems.value,
      totalCost: totalPrice.value
    }

    await request.post('/orders/checkout', payload)

    const idsToRemove = selectedItems.value.map(i => i.id || i.cartId || i.cartItemId)
    if (idsToRemove.length > 0) {
      await Promise.all(idsToRemove.map(id => request.delete(`/cart/${id}`)))
    }

    ElMessage.success('Payment successful!')
    confirmVisible.value = false
    router.push('/order-status')
  } catch (e: any) {
    ElMessage.error('Transaction failed. Please check your points balance.')
  } finally {
    checkingOut.value = false
  }
}
</script>

<style scoped>
/* 樣式保持原有的品牌設計感，未做修改 */
.checkout-page { background: #f8fafc; min-height: 100vh; padding-bottom: 120px; }
.brand-bar { height: 4px; background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%); }
.checkout-header { background: #fff; padding: 16px 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 10px rgba(0,0,0,0.02); }
.page-title { font-size: 16px; font-weight: 800; color: #1e293b; margin: 0; text-transform: uppercase; letter-spacing: 0.5px; }
.clear-action { font-size: 12px; font-weight: 700; }
.checkout-body { padding: 16px; }
.fulfillment-card { background: #fff; border-radius: 16px; padding: 20px; margin-bottom: 24px; position: relative; box-shadow: 0 4px 15px rgba(0,0,0,0.03); }
.fulfillment-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.fulfillment-header .label { font-size: 11px; font-weight: 800; color: #94a3b8; display: flex; align-items: center; gap: 6px; text-transform: uppercase; }
.addr-main { font-size: 16px; font-weight: 700; color: #1e293b; line-height: 1.4; margin-bottom: 8px; }
.contact-sub { display: flex; align-items: center; gap: 10px; }
.user-tag { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: 700; color: #64748b; }
.phone-num { font-size: 14px; color: #64748b; font-weight: 500; }
.fulfillment-footer-line { position: absolute; bottom: 0; left: 0; width: 100%; height: 4px; background-image: repeating-linear-gradient(45deg, #e2231a, #e2231a 10px, #fff 10px, #fff 20px, #007934 20px, #007934 30px, #fff 30px, #fff 40px); border-radius: 0 0 16px 16px; opacity: 0.3; }
.section-title { font-size: 13px; font-weight: 800; color: #64748b; margin: 0 0 12px 4px; text-transform: uppercase; }
.item-tile { background: #fff; border-radius: 14px; padding: 16px; margin-bottom: 12px; display: flex; gap: 12px; align-items: center; transition: border 0.3s; }
.item-tile.is-selected { border: 1px solid #007934; background: #f0fdf4; }
.item-check { display: flex; align-items: center; }
.item-preview { width: 64px; height: 64px; background: #f8fafc; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #cbd5e1; }
.item-details { flex: 1; }
.name-row { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 4px; }
.name { font-weight: 700; color: #1e293b; font-size: 15px; }
.points-badge { display: flex; flex-direction: column; align-items: flex-end; }
.points-badge .val { color: #ff7900; font-weight: 900; font-size: 16px; }
.points-badge .unit { font-size: 9px; font-weight: 700; color: #94a3b8; margin-top: -4px; }
.stock-info { font-size: 11px; color: #cbd5e1; margin-bottom: 10px; font-family: monospace; }
.brand-stepper :deep(.el-input-number__increase), .brand-stepper :deep(.el-input-number__decrease) { background-color: transparent; border: none; }
.summary-tile { background: #fff; border-radius: 14px; padding: 16px; margin-top: 24px; }
.summary-row { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; color: #64748b; }
.free-text { color: #007934; font-weight: 800; }
.sticky-checkout { position: fixed; bottom: 0; left: 0; right: 0; background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(10px); padding: 20px 24px 34px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 -10px 30px rgba(0,0,0,0.05); z-index: 1000; border-top: 1px solid #f1f5f9; }
.total-section .total-label { display: block; font-size: 11px; font-weight: 800; color: #94a3b8; text-transform: uppercase; }
.total-amount { display: flex; align-items: baseline; gap: 4px; }
.points-val { font-size: 28px; font-weight: 900; color: #ff7900; }
.points-label { font-size: 12px; font-weight: 800; color: #ff7900; }
.checkout-main-btn { height: 52px; width: 180px; border-radius: 16px; font-weight: 900; font-size: 16px; background: #007934 !important; border: none; box-shadow: 0 8px 20px rgba(0, 121, 52, 0.25); }
.checkout-main-btn:active { transform: scale(0.98); }
.pulse-icon { animation: pulse 2s infinite; color: #ff7900; font-size: 20px; }
@keyframes pulse { 0% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.1); opacity: 0.7; } 100% { transform: scale(1); opacity: 1; } }
.confirm-box { padding: 10px; }
.confirm-item { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px; }
.confirm-name small { color: #94a3b8; margin-left: 5px; font-weight: bold; }
.confirm-price { font-weight: 600; }
.confirm-total { display: flex; justify-content: space-between; align-items: center; margin: 15px 0; font-weight: 800; font-size: 16px; }
.total-highlight { color: #ff7900; font-size: 20px; }
.confirm-address-box { background: #f1f5f9; padding: 12px; border-radius: 8px; margin-top: 15px; }
.c-title { font-size: 11px; font-weight: 800; color: #64748b; margin-bottom: 4px; }
.c-desc { font-size: 13px; color: #1e293b; margin-bottom: 2px; }
</style>
