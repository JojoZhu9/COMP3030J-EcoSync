<template>
  <div class="cart-container">
    <div class="cart-header">
      <div class="header-left">
        <h2 class="title">确认订单</h2>
      </div>
      <el-button type="danger" link @click="clearCart" :disabled="cartItems.length === 0">清空</el-button>
    </div>

    <div class="cart-list" v-if="cartItems.length > 0">
      <el-card v-for="item in cartItems" :key="item.productId" class="cart-item" shadow="never">
        <div class="item-layout">
          <div class="item-img-box">
            <el-icon :size="30"><Goods /></el-icon>
          </div>
          <div class="item-main">
            <div class="item-row">
              <span class="product-name">{{ item.productName }}</span>
              <span class="product-price">{{ item.pointsPrice }} 积分</span>
            </div>
            <div class="item-row mt-10">
              <span class="product-id">ID: {{ item.productId }}</span>
              <el-input-number v-model="item.quantity" :min="1" size="small" @change="saveToLocal" />
            </div>
          </div>
        </div>
      </el-card>

      <div class="delivery-section">
        <div class="section-header">
          <span><el-icon><Location /></el-icon> 配送至</span>
          <el-link type="primary" :underline="'none'" @click="$router.push('/profile')">修改</el-link>
        </div>
        <div class="address-box" v-if="userAddress && userAddress !== 'null'">
          <p class="addr-text">{{ userAddress }}</p>
          <p class="phone-text">{{ userPhone }}</p>
        </div>
        <div class="address-empty" v-else @click="$router.push('/profile')">
          <el-icon><Warning /></el-icon> 尚未设置地址，点击去填写
        </div>
      </div>

      <div class="checkout-footer">
        <div class="total-bar">
          <span class="label">实付积分:</span>
          <span class="value">{{ totalPrice }}</span>
        </div>
        <el-button type="primary" size="large" class="checkout-btn" @click="handleCheckout" :loading="checkingOut">
          立即结算
        </el-button>
      </div>
    </div>

    <el-empty v-else description="购物车里什么都没有" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Location, Goods, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartItems = ref<any[]>([])
const checkingOut = ref(false)
const userAddress = ref(localStorage.getItem('userAddress') || '')
const userPhone = ref(localStorage.getItem('userPhone') || '')
const userId = localStorage.getItem('userId')

onMounted(() => {
  const saved = localStorage.getItem('cart')
  if (saved) cartItems.value = JSON.parse(saved)
})

const totalPrice = computed(() => cartItems.value.reduce((sum, item) => sum + (item.pointsPrice * item.quantity), 0))

const saveToLocal = () => localStorage.setItem('cart', JSON.stringify(cartItems.value))

const clearCart = () => {
  cartItems.value = []
  localStorage.removeItem('cart')
}

const handleCheckout = async () => {
  if (cartItems.value.length === 0) return
  if (!userAddress.value || userAddress.value === 'null') {
    ElMessageBox.alert('请先去个人中心设置配送地址', '提示')
    return
  }

  checkingOut.value = true
  try {
    const payload = {
      userId: Number(userId),
      storeId: Number(localStorage.getItem('lastStoreId') || 1),
      deliveryAddress: userAddress.value,
      contactPhone: userPhone.value
    }
    await request.post('/orders/checkout', payload)
    ElMessage.success('订单已提交！')
    clearCart()
    router.push('/order-status')
  } catch (e: any) {
    ElMessage.error('结算失败，积分可能不足')
  } finally {
    checkingOut.value = false
  }
}
</script>

<style scoped>
.cart-container { background: #f5f7f9; min-height: 100vh; padding: 15px 15px 100px; }
.cart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding: 5px 0; }
.header-left { display: flex; align-items: center; }
.title { font-size: 18px; margin: 0; color: #334155; }
.cart-item { border-radius: 12px; margin-bottom: 12px; border: none; }
.item-layout { display: flex; gap: 15px; align-items: center; }
.item-img-box { width: 60px; height: 60px; background: #f0f2f5; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #909399; }
.item-main { flex: 1; }
.item-row { display: flex; justify-content: space-between; align-items: center; }
.product-name { font-weight: bold; font-size: 15px; }
.product-price { color: #f59e0b; font-weight: bold; }
.product-id { font-size: 11px; color: #bbb; }
.mt-10 { margin-top: 10px; }
.delivery-section { background: white; padding: 15px; border-radius: 12px; margin-top: 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; font-size: 14px; color: #666; }
.addr-text { font-weight: bold; font-size: 15px; margin: 0 0 5px 0; }
.checkout-footer { position: fixed; bottom: 0; left: 0; right: 0; background: white; padding: 15px 20px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 -4px 10px rgba(0,0,0,0.05); z-index: 100; }
.total-bar .value { color: #f59e0b; font-size: 24px; font-weight: 800; margin-left: 8px; }
.checkout-btn { width: 140px; height: 45px; border-radius: 22.5px; font-weight: bold; }
</style>
