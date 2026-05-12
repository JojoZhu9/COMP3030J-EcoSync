<template>
  <div class="profile-page">
    <div class="brand-stripe"></div>

    <div class="member-hero">
      <div class="hero-content">
        <div class="avatar-container">
          <el-avatar :size="82" src="https://api.dicebear.com/7.x/avataaars/svg?seed=Lucky" class="member-avatar" />
          <div class="status-ring"></div>
        </div>
        <div class="member-info">
          <div class="id-row">
            <span class="member-id">UID: {{ userId }}</span>
            <el-tag size="small" class="active-tag">ACTIVE MEMBER</el-tag>
          </div>
          <h2 class="welcome-text">Welcome back, {{ userForm.username || 'Eco-Partner' }}!</h2>
        </div>
      </div>
    </div>

    <div class="wallet-card">
      <div class="wallet-content">
        <div class="wallet-left">
          <span class="wallet-label">Available Balance</span>
          <div class="points-display">
            <span class="points-unit">¥</span>
            <span class="points-num">{{ userForm.points }}</span>
          </div>
        </div>
        <div class="wallet-right">
          <el-button type="warning" plain round size="small" @click="handleRecharge">
            Top Up
          </el-button>
        </div>
      </div>
    </div>

    <div class="settings-body">
      <el-card class="settings-card" shadow="never">
        <template #header>
          <div class="card-title">
            <el-icon><MapLocation /></el-icon>
            <span>Logistics & Fulfillment</span>
          </div>
        </template>

        <el-form :model="userForm" label-position="top" class="custom-form">
          <el-form-item label="DEFAULT DELIVERY ADDRESS">
            <el-input
              v-model="userForm.address"
              placeholder="e.g. 123 Fresh St, Tokyo"
              :prefix-icon="Location"
              clearable
            />
          </el-form-item>
          <el-form-item label="CONTACT TELEPHONE">
            <el-input
              v-model="userForm.phone"
              placeholder="Primary phone number"
              :prefix-icon="Phone"
              clearable
            />
          </el-form-item>

          <el-button
            type="primary"
            class="commit-btn"
            @click="updateUserInfo"
            :loading="saving"
          >
            Update Profile Data
          </el-button>
        </el-form>
      </el-card>

      <p class="app-version">Version 2.4.0-Stable | 7-Eco Framework</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { MapLocation, Location, Phone } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const userId = localStorage.getItem('userId') || '12'
const saving = ref(false)
const rawUserData = ref<any>({})

const userForm = reactive({
  address: '',
  phone: '',
  points: 0,
  username: ''
})

const fetchUser = async () => {
  try {
    const res: any = await request.get(`/users/${userId}`)
    const data = res.data?.data || res.data || res

    rawUserData.value = data
    userForm.username = data.username || ''
    userForm.points = data.balance || 0
    userForm.phone = data.phoneNumber || data.phone_number || ''
    userForm.address = data.userAddress || data.user_address || ''
  } catch (e) {
    ElMessage.error('Infrastructure Sync Error')
  }
}

/**
 * 自定义充值逻辑：同步至数据库
 */
const handleRecharge = () => {
  ElMessageBox.prompt('Please enter recharge amount (¥)', 'Balance Top-up', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    inputPattern: /^\d+(\.\d{1,2})?$/,
    inputErrorMessage: 'Please enter a valid amount',
    buttonSize: 'small'
  }).then(async ({ value }) => {
    try {
      const amount = parseFloat(value)
      if (isNaN(amount) || amount <= 0) return

      // 计算新余额
      const newBalance = Number(userForm.points) + amount

      // 构建同步 Payload
      const payload = {
        ...rawUserData.value,
        balance: newBalance
      }

      // 同步到数据库
      await request.put(`/users/${userId}`, payload)

      ElMessage.success(`Successfully recharged ¥${amount.toFixed(2)}`)
      await fetchUser() // 刷新本地数据
    } catch (e) {
      ElMessage.error('Recharge failed. System sync error.')
    }
  }).catch(() => {})
}

const updateUserInfo = async () => {
  if (!userForm.address || !userForm.phone) {
    return ElMessage.warning('Required fields missing')
  }

  saving.value = true
  try {
    const payload = {
      ...rawUserData.value,
      phoneNumber: userForm.phone,
      userAddress: userForm.address,
    }

    await request.put(`/users/${userId}`, payload)
    ElMessage.success('Fulfillment data synchronized!')
    await fetchUser()
  } catch (e) {
    ElMessage.error('Synchronization failed')
  } finally {
    saving.value = false
  }
}

onMounted(fetchUser)
</script>

<style scoped>
.profile-page { background: #f8fafc; min-height: 100vh; padding-bottom: 50px; }
.brand-stripe { height: 4px; background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%); }

.member-hero { background: #1e293b; padding: 40px 24px 60px; color: white; position: relative; overflow: hidden; }
.member-hero::after { content: "7"; position: absolute; right: -20px; bottom: -40px; font-size: 200px; font-weight: 900; color: rgba(255,255,255,0.03); }

.hero-content { display: flex; align-items: center; gap: 20px; position: relative; z-index: 1; }
.member-avatar { border: 4px solid #334155; }
.status-ring { position: absolute; bottom: 0; right: 0; width: 18px; height: 18px; background: #007934; border: 3px solid #1e293b; border-radius: 50%; }

.id-row { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.member-id { font-family: monospace; font-weight: 700; color: #94a3b8; font-size: 13px; }
.active-tag { background: #007934 !important; color: white !important; border: none; font-weight: 800; font-size: 9px; }
.welcome-text { margin: 0; font-size: 18px; font-weight: 800; }

.wallet-card {
  margin: -30px 20px 24px;
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
  position: relative;
  z-index: 2;
}
/* 钱包布局调整 */
.wallet-content { display: flex; justify-content: space-between; align-items: center; }
.wallet-label { font-size: 11px; font-weight: 800; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; }
.points-display { display: flex; align-items: baseline; gap: 4px; margin-top: 4px; }
.points-num { font-size: 36px; font-weight: 900; color: #ff7900; line-height: 1; }
.points-unit { font-size: 20px; font-weight: 800; color: #ff7900; margin-right: 2px; }

.settings-body { padding: 0 20px; }
.settings-card { border-radius: 16px; border: 1px solid #f1f5f9; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 800; color: #475569; font-size: 14px; text-transform: uppercase; }

.custom-form :deep(.el-form-item__label) { font-size: 10px; font-weight: 800; color: #94a3b8; margin-bottom: 8px !important; }
.commit-btn { width: 100%; height: 48px; border-radius: 12px; font-weight: 800; background-color: #007934 !important; border: none; box-shadow: 0 8px 16px rgba(0, 121, 52, 0.2); margin-top: 10px; }

.app-version { text-align: center; font-size: 10px; color: #cbd5e1; font-weight: 700; margin-top: 30px; text-transform: uppercase; letter-spacing: 1px; }
</style>
