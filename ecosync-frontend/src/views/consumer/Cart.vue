<<template>
  <div class="checkout-wrapper">
    <div class="checkout-page">
      <div class="checkout-header">
        <div class="header-left">
          <div class="nav-back" @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
          </div>
          <h2 class="page-title">Shopping Cart</h2>
        </div>
        <el-button class="empty-btn" type="danger" link @click="handleClearCart" :disabled="cartItems.length === 0">
          <el-icon style="margin-right: 4px"><Delete /></el-icon>Empty Cart
        </el-button>
      </div>

      <div class="scroll-content">
        <div class="checkout-body" v-if="cartItems.length > 0">
          <div class="fulfillment-card">
            <div class="fulfillment-header">
              <span class="label"><el-icon><Shop /></el-icon> PICKUP INFO</span>
            </div>
            <div class="address-content" v-if="userAddress">
              <div class="addr-main">{{ userAddress }}</div>
              <div class="contact-sub">
                <span class="u-tag">Member</span>
                <span class="u-phone">{{ userPhone || 'No phone' }}</span>
              </div>
            </div>
            <div class="address-content empty-addr" v-else>
              Please set your pickup address in profile.
            </div>
          </div>

          <div class="items-container">
            <div v-for="item in cartItems" :key="item.cartItemId" class="item-tile" :class="{'is-selected': item.selected}">
              <div class="item-check">
                <el-checkbox v-model="item.selected" size="large" class="custom-checkbox" />
              </div>

              <div class="item-preview">
                <el-image :src="getImageUrl(item)" fit="cover" class="cart-img">
                  <template #error>
                    <div class="cart-img-fallback">
                      <el-icon :size="24" color="#cbd5e1"><Goods /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>

              <div class="item-details">
                <div class="name-row">
                  <span class="product-name" :title="item.productName">{{ item.productName || 'Syncing...' }}</span>
                  <!-- 删除按钮：改为更醒目的圆形按钮 -->
                  <div class="delete-btn-wrap" @click="deleteSingleItem(item.cartItemId)">
                    <el-icon :size="18"><Close /></el-icon>
                  </div>
                </div>

                <div class="price-qty-row">
                  <div class="price-box">
                    <div class="points-price">
                      <span class="unit">¥</span>
                      <span class="num">{{ (item.pointsPrice * item.quantity).toFixed(2) }}</span>
                    </div>
                    <div v-if="item.originalPrice && item.pointsPrice < item.originalPrice" class="cart-original-price">
                      ¥{{ (item.originalPrice * item.quantity).toFixed(2) }}
                    </div>
                  </div>

                  <div class="qty-control">
                    <!-- 数量输入器：改为 default 尺寸，更大更醒目 -->
                    <el-input-number
                      v-model="item.quantity"
                      :min="0"
                      :max="item.maxStock || 1"
                      size="default"
                      class="modern-input-number"
                      @change="(val) => updateCartQuantity(item, val)"
                    />
                  </div>
                </div>
                <div v-if="item.maxStock <= 5" class="stock-warning">
                  Only {{ item.maxStock }} units left!
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-cart-view">
          <el-empty description="Your cart is empty right now." :image-size="160">
            <template #image>
              <el-icon :size="80" color="#cbd5e1"><Shop /></el-icon>
            </template>
            <el-button type="primary" round class="go-shop-btn" @click="$router.push('/home')">
              Go Shopping
            </el-button>
          </el-empty>
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
          Pay Now ({{ selectedItems.length }})
        </el-button>
      </div>
    </div>

    <el-dialog v-model="confirmVisible" width="360px" align-center class="receipt-dialog" :show-close="false" append-to-body>
      <div class="receipt-container">
        <div class="receipt-zigzag-top"></div>
        <div class="receipt-header">
          <div class="brand-logo">7-ELEVEN</div>
          <p class="receipt-title">ORDER PREVIEW</p>
        </div>

        <div class="receipt-info">
          <div class="r-row"><span>Customer:</span><span class="r-val">{{ rawUser.userName || 'Member' }}</span></div>
          <div class="r-row"><span>Phone:</span><span class="r-val">{{ userPhone }}</span></div>
          <div class="r-row"><span>Pickup:</span><span class="r-val r-addr">{{ userAddress }}</span></div>
        </div>

        <div class="r-divider"></div>

        <div class="r-items">
          <div v-for="item in selectedItems" :key="item.productId" class="r-item">
            <div class="r-item-main">
              <span class="r-name">{{ item.productName }}</span>
              <span class="r-qty">x{{ item.quantity }}</span>
            </div>
            <div class="r-item-price">
              <span v-if="item.originalPrice && item.pointsPrice < item.originalPrice" class="r-old-price">
                {{ (item.originalPrice * item.quantity).toFixed(2) }}
              </span>
              <span>¥{{ (item.pointsPrice * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="r-divider"></div>

        <div class="r-total">
          <span class="r-total-label">GRAND TOTAL</span>
          <span class="r-amt">¥{{ totalPrice }}</span>
        </div>

        <div class="barcode-area">
          <svg ref="receiptBarcodeRef"></svg>
          <div class="barcode-num">{{ receiptOrderNum }}</div>
        </div>
        <div class="receipt-zigzag-bottom"></div>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button plain round @click="confirmVisible = false" class="cancel-btn">Cancel</el-button>
          <el-button type="success" round @click="executePayment" :loading="checkingOut" class="confirm-btn">
            Confirm & Pay
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, onMounted, nextTick} from 'vue'
import { ArrowLeft, Delete, Shop, Box, Close, Goods } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import JsBarcode from 'jsbarcode' // 引入条码库

const router = useRouter()
const cartItems = ref<any[]>([])
const userAddress = ref('')
const userPhone = ref('')
const userBalance = ref(0)
const rawUser = ref<any>({})
const confirmVisible = ref(false)
const checkingOut = ref(false)
const receiptBarcodeRef = ref<HTMLElement | null>(null)
const receiptOrderNum = ref('')

const getUserId = () => localStorage.getItem('userId')
const selectedItems = computed(() => cartItems.value.filter(i => i.selected))
const totalPrice = computed(() => selectedItems.value.reduce((acc, i) => acc + (i.pointsPrice * i.quantity), 0).toFixed(2))

const getImageUrl = (item: any) => {
  const name = item.imageUrl || `${item.barcode}.jpg`
  return `/uploads/products/${name}`
}

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
          selected: true,
          imageUrl: std.imageUrl || std.image_url || null
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

  // 👇 新增：生成流水号并渲染真实条码
  receiptOrderNum.value = '00' + Date.now().toString().slice(-8)
  nextTick(() => {
    if (receiptBarcodeRef.value) {
      JsBarcode(receiptBarcodeRef.value, receiptOrderNum.value, {
        format: "CODE128",
        displayValue: false,
        height: 40,
        width: 2,
        background: "transparent",
        lineColor: "#1e293b",
        margin: 0
      })
    }
  })
}

const updateCartQuantity = async (item: any, qty: number) => {
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
    await ElMessageBox.confirm('Clear all items from your cart?', 'Warning', { confirmButtonText: 'Clear', cancelButtonText: 'Cancel', type: 'warning' })
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
.checkout-wrapper { height: 100vh; width: 100%; overflow: hidden; position: relative; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }
.checkout-page { height: 100%; display: flex; flex-direction: column; background: #f4f6f8; position: relative; }

.checkout-header {
  background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(8px);
  padding: 16px 24px; display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid #e2e8f0; z-index: 10;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.nav-back { cursor: pointer; padding: 4px; border-radius: 50%; display: flex; align-items: center; justify-content: center; transition: background 0.2s; }
.nav-back:hover { background: #f1f5f9; }
.page-title { font-size: 20px; font-weight: 900; color: #1e293b; margin: 0; }
.empty-btn { font-weight: bold; }

.scroll-content { flex: 1; overflow-y: auto; padding-bottom: 140px; }

.fulfillment-card {
  background: #fff; margin: 20px 24px 16px; padding: 20px;
  border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.02); transition: transform 0.2s;
}
.fulfillment-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.label { color: #008163; font-weight: 800; font-size: 13px; display: flex; align-items: center; gap: 6px; letter-spacing: 0.5px; }
.addr-main { font-weight: 800; color: #1e293b; font-size: 16px; margin-bottom: 8px; line-height: 1.4; }
.contact-sub { display: flex; align-items: center; }
.u-tag { background: #e2e8f0; padding: 2px 8px; border-radius: 6px; font-size: 11px; font-weight: bold; color: #475569; margin-right: 8px; }
.u-phone { color: #64748b; font-size: 14px; font-weight: 600; }
.empty-addr { color: #ef4444; font-weight: 600; font-size: 14px; }

.items-container { padding: 0 24px; }
.item-tile {
  background: #fff; margin-bottom: 16px; padding: 16px;
  border-radius: 16px; display: flex; gap: 16px; align-items: center;
  border: 2px solid transparent; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.item-tile:hover { box-shadow: 0 8px 16px rgba(0,0,0,0.06); transform: translateY(-2px); }
.item-tile.is-selected { border-color: #008163; background: #f8fafc; box-shadow: 0 8px 16px rgba(0, 129, 99, 0.08); }

.item-check { display: flex; align-items: center; justify-content: center; }
/* 复选框：删除内部对勾，选中时纯色填充 */
.custom-checkbox :deep(.el-checkbox__inner) {
  border-radius: 6px;
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  background: #fff;
}
/* 彻底删除对勾 */
.custom-checkbox :deep(.el-checkbox__inner::after) {
  display: none !important;
  border: none !important;
  content: none !important;
}
/* 选中状态：品牌绿纯色填充 */
.custom-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #008163;
  border-color: #008163;
}

.item-preview {
  width: 72px; height: 72px; background: #f1f5f9; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  overflow: hidden;
}
.cart-img { width: 100%; height: 100%; }
.cart-img-fallback { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f1f5f9; }

.item-details { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: space-between; }
.name-row { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.product-name {
  font-weight: 800; font-size: 15px; color: #334155; line-height: 1.3;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

/* ===== 删除按钮：改为醒目的圆形按钮 ===== */
.delete-btn-wrap {
  width: 36px; height: 36px;
  background: #f1f5f9;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.2s ease;
  flex-shrink: 0;
}
.delete-btn-wrap:hover {
  background: #fee2e2;
  color: #ef4444;
  transform: scale(1.05);
}

.price-qty-row { display: flex; justify-content: space-between; align-items: flex-end; }
.price-box { display: flex; flex-direction: column; }
.points-price { color: #ee7203; font-weight: 900; font-size: 20px; line-height: 1; }
.points-price .unit { font-size: 14px; margin-right: 2px; }
.cart-original-price { text-decoration: line-through; color: #94a3b8; font-size: 12px; margin-top: 4px; }

/* ===== 步进器：增大尺寸和按钮 ===== */
.qty-control { display: flex; align-items: center; }
.modern-input-number { width: 120px; }
.modern-input-number :deep(.el-input__wrapper) { background: #f1f5f9; box-shadow: none !important; border-radius: 10px; }
/* 增大加减按钮 */
.modern-input-number :deep(.el-input-number__decrease),
.modern-input-number :deep(.el-input-number__increase) {
  width: 32px;
  height: 32px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #64748b;
  font-size: 16px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.modern-input-number :deep(.el-input-number__decrease:hover),
.modern-input-number :deep(.el-input-number__increase:hover) {
  background: #008163;
  color: #fff;
  border-color: #008163;
}
/* 增大中间数字输入框 */
.modern-input-number :deep(.el-input__inner) {
  font-size: 16px;
  font-weight: 800;
  color: #1e293b;
  text-align: center;
}

.stock-warning { font-size: 11px; color: #ef4444; margin-top: 6px; font-weight: bold; background: #fef2f2; padding: 2px 6px; border-radius: 4px; width: fit-content; }

.empty-cart-view { margin-top: 60px; }
.go-shop-btn { margin-top: 16px; background: #008163; border: none; font-weight: bold; padding: 0 30px; }

.sticky-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: 90px;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -4px 24px rgba(0,0,0,0.06);
  border-top: 1px solid rgba(226, 232, 240, 0.6);
  z-index: 100;
}
.total-section { display: flex; flex-direction: column; }
.total-label { font-size: 13px; color: #64748b; font-weight: bold; margin-bottom: 2px; }
.total-amount-box { color: #EE7203; display: flex; align-items: baseline; }
.total-amount-box .currency { font-size: 16px; font-weight: bold; margin-right: 2px; }
.total-amount-box .value { font-size: 28px; font-weight: 900; }
.pay-now-btn {
  height: 48px;
  padding: 0 24px;
  border-radius: 24px;
  font-weight: 800;
  font-size: 16px;
  background: #008163 !important;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 129, 99, 0.3);
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}
.pay-now-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(0, 129, 99, 0.4); }

.receipt-dialog :deep(.el-dialog) { background: transparent; box-shadow: none; }
.receipt-dialog :deep(.el-dialog__header) { display: none; }
.receipt-dialog :deep(.el-dialog__body) { padding: 0; }

.receipt-container {
  background: #fdfbf7;
  padding: 30px 24px; position: relative; margin: 10px 0;
  box-shadow: 0 10px 25px rgba(0,0,0,0.15); border-radius: 4px;
}
.receipt-zigzag-top {
  position: absolute; top: -8px; left: 0; width: 100%; height: 8px;
  background: linear-gradient(-135deg, transparent 4px, #fdfbf7 0), linear-gradient(135deg, transparent 4px, #fdfbf7 0);
  background-size: 8px 8px;
}
.receipt-zigzag-bottom {
  position: absolute; bottom: -8px; left: 0; width: 100%; height: 8px;
  background: linear-gradient(-45deg, transparent 4px, #fdfbf7 0), linear-gradient(45deg, transparent 4px, #fdfbf7 0);
  background-size: 8px 8px;
}

.receipt-header { text-align: center; margin-bottom: 24px; }
.brand-logo { color: #008163; font-weight: 900; font-size: 26px; letter-spacing: 1px; }
.receipt-title { font-size: 14px; color: #1e293b; font-weight: bold; letter-spacing: 2px; margin: 4px 0 0; }

.receipt-info { font-family: 'Courier New', Courier, monospace; font-size: 13px; color: #334155; }
.r-row { display: flex; justify-content: space-between; margin-bottom: 8px; }
.r-val { font-weight: bold; text-align: right; }
.r-addr { display: block; max-width: 180px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.r-divider { border-top: 2px dashed #cbd5e1; margin: 16px 0; }

.r-items { font-family: 'Courier New', Courier, monospace; }
.r-item { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; font-size: 14px; color: #1e293b; }
.r-item-main { flex: 1; display: flex; flex-direction: column; padding-right: 12px; }
.r-name { font-weight: bold; line-height: 1.2; margin-bottom: 4px; }
.r-qty { color: #64748b; font-size: 13px; }
.r-item-price { text-align: right; font-weight: bold; display: flex; flex-direction: column; }
.r-old-price { text-decoration: line-through; color: #94a3b8; font-size: 12px; }

.r-total { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.r-total-label { font-weight: 900; font-size: 16px; color: #1e293b; }
.r-amt { font-size: 26px; color: #EE7203; font-weight: 900; }

.barcode-area { text-align: center; margin-top: 30px; opacity: 0.7; }
.barcode-bars { font-size: 24px; letter-spacing: 2px; line-height: 1; color: #1e293b; }
.barcode-num { font-family: 'Courier New', Courier, monospace; font-size: 12px; letter-spacing: 4px; margin-top: 4px; }

.dialog-actions { display: flex; gap: 16px; margin-top: 24px; }
.cancel-btn { flex: 1; border: 2px solid #e2e8f0; font-weight: bold; }
.confirm-btn { flex: 2; background: #008163 !important; border: none; font-weight: 800; font-size: 16px; box-shadow: 0 4px 12px rgba(0, 129, 99, 0.2); }
</style>
