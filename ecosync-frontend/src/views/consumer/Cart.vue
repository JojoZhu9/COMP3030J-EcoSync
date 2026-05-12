<template>
  <div class="checkout-wrapper">
    <div class="checkout-page">
      <div class="checkout-header">
        <div class="header-left">
          <div class="nav-back" @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
          </div>
          <h2 class="page-title">Shopping Basket</h2>
        </div>
        <el-button type="danger" link @click="handleClearCart" :disabled="cartItems.length === 0">
          <el-icon style="margin-right: 4px"><Delete /></el-icon>Empty
        </el-button>
      </div>

      <div class="scroll-content">
        <div class="checkout-body" v-if="cartItems.length > 0">
          <div class="fulfillment-card">
            <div class="fulfillment-header">
              <span class="label"><el-icon><Shop /></el-icon>PICKUP INFO</span>
              <el-button type="primary" link @click="$router.push('/profile')">Edit</el-button>
            </div>
            <div class="address-content" v-if="userAddress">
              <div class="addr-main">{{ userAddress }}</div>
              <div class="contact-sub">
                <span class="u-tag">Member</span>
                <span>{{ userPhone || 'No phone' }}</span>
              </div>
            </div>
          </div>

          <div class="items-container">
            <div v-for="item in cartItems" :key="item.cartItemId" class="item-tile" :class="{'is-selected': item.selected}">
              <div class="item-check"><el-checkbox v-model="item.selected" size="large" /></div>
              <div class="item-preview">
                <el-icon :size="24"><Box /></el-icon>
              </div>
              <div class="item-details">
                <div class="name-row">
                  <span class="product-name">{{ item.productName || 'Syncing...' }}</span>
                  <el-button type="info" link @click="deleteSingleItem(item.cartItemId)"><el-icon><Close /></el-icon></el-button>
                </div>
                <div class="price-qty-row">
                  <div>
                    <div class="price-tag">￥{{ (item.pointsPrice * item.quantity).toFixed(2) }}</div>
                    <div v-if="item.originalPrice && item.pointsPrice < item.originalPrice" class="cart-original-price">￥{{ (item.originalPrice * item.quantity).toFixed(2) }}</div>
                  </div>
                  <el-input-number
                    v-model="item.quantity"
                    :min="0"
                    :max="item.maxStock || 1"
                    size="small"
                    @change="(val) => updateCartQuantity(item, val)"
                  />
                </div>
                <div v-if="item.maxStock <= 5" style="font-size: 10px; color: #ef4444; margin-top: 4px;">
                  Only {{ item.maxStock }} units left!
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-cart-view">
          <el-empty description="Your basket is empty" :image-size="120" />
        </div>
      </div>

      <div class="sticky-footer" v-if="cartItems.length > 0">
        <div class="total-section">
          <span class="total-label">Subtotal</span>
          <div class="total-amount-box">
            <span class="currency">¥</span>
            <span class="value">{{ totalPrice }}</span>
          </div>
        </div>
        <el-button
          type="success"
          class="pay-now-btn"
          @click="openConfirmDialog"
          :disabled="selectedItems.length === 0"
        >
          Pay Now
        </el-button>
      </div>
    </div>

    <el-dialog v-model="confirmVisible" width="400px" align-center class="receipt-dialog" :show-close="false" append-to-body>
      <div class="receipt-container">
        <div class="receipt-zigzag"></div>
        <div class="receipt-header">
          <div class="brand-logo">7-ELEVEN</div>
          <p class="receipt-title">ORDER PREVIEW</p>
        </div>
        <div class="receipt-info">
          <div class="r-row"><span>Customer:</span><span>{{ rawUser.userName || 'Member' }}</span></div>
          <div class="r-row"><span>Phone:</span><span>{{ userPhone }}</span></div>
          <div class="r-row"><span>Pickup:</span><span class="r-addr">{{ userAddress }}</span></div>
        </div>
        <div class="r-divider"></div>
        <div class="r-items">
          <div v-for="item in selectedItems" :key="item.productId" class="r-item">
            <span class="r-name">{{ item.productName }}</span>
            <span class="r-qty">x{{ item.quantity }}</span>
            <span class="r-price">¥{{ (item.pointsPrice * item.quantity).toFixed(2) }}<span v-if="item.originalPrice && item.pointsPrice < item.originalPrice" style="text-decoration:line-through;color:#94a3b8;font-size:11px;margin-left:4px">{{ (item.originalPrice * item.quantity).toFixed(2) }}</span></span>
          </div>
        </div>
        <div class="r-divider"></div>
        <div class="r-total">
          <span>GRAND TOTAL</span>
          <span class="r-amt">¥{{ totalPrice }}</span>
        </div>
        <div class="barcode-area">|| ||| || ||| |||</div>
      </div>
      <template #footer>
        <div class="dialog-actions">
          <el-button @click="confirmVisible = false" style="flex:1">Cancel</el-button>
          <el-button type="success" @click="executePayment" :loading="checkingOut" style="flex:2; background:#008163 !important; border:none;">Confirm & Pay</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, Delete, Shop, Box, Close } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartItems = ref<any[]>([])
const userAddress = ref('')
const userPhone = ref('')
const userBalance = ref(0)
const rawUser = ref<any>({})
const confirmVisible = ref(false)
const checkingOut = ref(false)

const getUserId = () => localStorage.getItem('userId')
const selectedItems = computed(() => cartItems.value.filter(i => i.selected))
const totalPrice = computed(() => selectedItems.value.reduce((acc, i) => acc + (i.pointsPrice * i.quantity), 0).toFixed(2))

const getDiscountRate = (expirationTime: string, discountRatesStr: string): number => {
  try {
    const rates: number[] = JSON.parse(discountRatesStr)
    if (!Array.isArray(rates) || rates.length === 0) return 1.0
    const hoursLeft = (new Date(expirationTime).getTime() - Date.now()) / (1000 * 60 * 60)
    const index = Math.min(Math.max(0, 12 - Math.ceil(hoursLeft)), rates.length - 1)
    return Number(rates[index]) || 1.0
  } catch { return 1.0 }
}

const initData = async () => {
  const uid = getUserId()
  if (!uid) return router.push('/login')
  try {
    const userRes: any = await request.get(`/users/${uid}`)
    const userData = userRes.data || userRes
    rawUser.value = userData
    userAddress.value = userData.userAddress || ''
    userPhone.value = userData.phone_number || userData.phoneNumber || ''
    userBalance.value = Number(userData.balance || 0)

    const cartRes: any = await request.get(`/cart/user/${uid}`)
    const rawItems = cartRes.data || cartRes || []

    cartItems.value = await Promise.all(rawItems.map(async (item: any) => {
      const pId = item.productId || item.product_id
      try {
        const expRes: any = await request.get(`/expiring-products/${pId}`)
        const exp = expRes.data || expRes
        const stdRes: any = await request.get(`/products/${exp.barcode}`)
        const std = stdRes.data || stdRes
        const normalPrice = Number(std.normalPrice || 0)
        const rate = getDiscountRate(exp.expirationTime, std.discountRates || '[]')

        return {
          ...item,
          productId: pId,
          barcode: exp.barcode,
          productName: std.productName,
          originalPrice: normalPrice,
          pointsPrice: +(normalPrice * rate).toFixed(2),
          maxStock: Number(exp.remainingStock),
          selected: true
        }
      } catch {
        return { ...item, productId: pId, productName: 'Detail Error', pointsPrice: 0, maxStock: 0, selected: false }
      }
    }))
  } catch (e) { ElMessage.error('Data Sync Failed') }
}

onMounted(initData)

const openConfirmDialog = () => {
  if (!userAddress.value) return ElMessage.warning('Set pickup address in profile first')
  confirmVisible.value = true
}

const updateCartQuantity = async (item: any, qty: number) => {
  // 修改点：如果数量被减到 0，执行删除逻辑
  if (qty <= 0) {
    await deleteSingleItem(item.cartItemId)
    return
  }

  try {
    await request.put(`/cart/${item.cartItemId}?quantity=${qty}`)
    const expRes: any = await request.get(`/expiring-products/${item.productId}`)
    const exp = expRes.data || expRes
    item.maxStock = Number(exp.remainingStock)

    if (qty > item.maxStock) {
      item.quantity = item.maxStock
      await request.put(`/cart/${item.cartItemId}?quantity=${item.maxStock}`)
      ElMessage.warning('Quantity adjusted due to stock changes')
    }
  } catch (e) {
    ElMessage.error('Update Failed')
  }
}

const deleteSingleItem = async (id: number) => {
  try {
    await request.delete(`/cart/${id}`)
    cartItems.value = cartItems.value.filter(i => i.cartItemId !== id)
    updateLocalStorageCart()
  } catch (e) { ElMessage.error('Delete Failed') }
}

const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('Clear all items?')
    await request.delete(`/cart/user/${getUserId()}`)
    cartItems.value = []
    updateLocalStorageCart()
  } catch (e) {}
}

const updateLocalStorageCart = () => {
  const simplified = cartItems.value.map(i => ({
    productId: i.productId,
    productName: i.productName,
    pointsPrice: i.pointsPrice,
    quantity: i.quantity
  }))
  localStorage.setItem('cart', JSON.stringify(simplified))
}

const executePayment = async () => {
  checkingOut.value = true
  const uid = getUserId()
  const storeId = localStorage.getItem('lastStoreId') || '1'

  try {
    const res: any = await request.post('/orders/checkout', {
      userId: Number(uid),
      storeId: Number(storeId)
    })

    if (typeof res === 'string' && res.includes('失败')) {
      throw new Error(res)
    }

    localStorage.setItem('cart', '[]')
    ElMessage.success('Order Successful!')
    confirmVisible.value = false
    router.push('/order-status')
  } catch (e: any) {
    ElMessage.error(e.message || 'Payment logic error')
  } finally {
    checkingOut.value = false
  }
}
</script>

<style scoped>
.checkout-wrapper { height: 100vh; width: 100%; overflow: hidden; position: relative; }
.checkout-page { height: 100%; display: flex; flex-direction: column; background: #f6f8fa; position: relative; }

.checkout-header { background: #fff; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; }
.page-title { font-size: 18px; font-weight: 800; color: #1e293b; margin:0; }

.scroll-content { flex: 1; overflow-y: auto; padding-bottom: 100px; }

.fulfillment-card { background: #fff; margin: 15px; padding: 18px; border-radius: 16px; box-shadow: 0 2px 10px rgba(0,0,0,0.03); }
.label { color: #008163; font-weight: 800; font-size: 11px; display: flex; align-items: center; gap: 5px; margin-bottom: 10px; }
.addr-main { font-weight: 800; color: #1e293b; font-size: 15px; }
.u-tag { background: #f1f5f9; padding: 1px 6px; border-radius: 4px; font-size: 10px; color: #64748b; margin-right: 5px; }

.item-tile { background: #fff; margin: 0 15px 12px; padding: 15px; border-radius: 16px; display: flex; gap: 12px; align-items: center; border: 2px solid transparent; transition: 0.3s; }
.item-tile.is-selected { border-color: #008163; background: #f0fdf9; }
.product-name { font-weight: 800; font-size: 14px; color: #1e293b; }
.price-tag { color: #EE7203; font-weight: 900; font-size: 18px; }
.cart-original-price { text-decoration: line-through; color: #94a3b8; font-size: 11px; }

.sticky-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 85px;
  background: #fff;
  padding: 0 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -10px 20px rgba(0,0,0,0.05);
  border-top: 1px solid #eee;
  z-index: 100;
}
.total-amount-box { color: #EE7203; display: flex; align-items: baseline; }
.value { font-size: 28px; font-weight: 900; }
.pay-now-btn { height: 50px; padding: 0 35px; border-radius: 25px; font-weight: 800; background: #008163 !important; border:none; }

.receipt-container { background: #fff; padding: 20px; position: relative; }
.receipt-zigzag { position: absolute; top: -10px; left: 0; width: 100%; height: 10px; background: linear-gradient(-135deg, transparent 5px, #fff 0), linear-gradient(135deg, transparent 5px, #fff 0); background-size: 10px 10px; }
.brand-logo { color: #008163; font-weight: 900; font-size: 24px; text-align: center; }
.r-row { display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 6px; }
.r-divider { border-top: 1px dashed #ccc; margin: 15px 0; }
.r-item { display: flex; justify-content: space-between; font-size: 13px; font-family: monospace; }
.r-amt { font-size: 24px; color: #EE7203; font-weight: 900; }
.barcode-area { text-align: center; font-size: 20px; letter-spacing: 5px; opacity: 0.2; margin-top: 20px; }
</style>
