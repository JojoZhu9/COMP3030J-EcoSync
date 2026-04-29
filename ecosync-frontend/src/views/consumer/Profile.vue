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
          <h2 class="welcome-text">Welcome back, Eco-Partner!</h2>
        </div>
      </div>
    </div>

    <div class="wallet-card">
      <div class="wallet-left">
        <span class="wallet-label">Eco-Points Balance</span>
        <div class="points-display">
          <span class="points-num">{{ userForm.points }}</span>
          <span class="points-unit">pts</span>
        </div>
      </div>
      <div class="wallet-right">
        <el-button type="success" size="small" round @click="$router.push('/home')">
          Redeem Items
        </el-button>
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

      <div class="system-section">
        <div class="action-item logout" @click="handleLogout">
          <div class="action-left">
            <el-icon><SwitchButton /></el-icon>
            <span>Terminate Session</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <p class="app-version">Version 2.4.0-Stable | 7-Eco Framework</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { MapLocation, Location, Phone, SwitchButton, ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const userId = localStorage.getItem('userId') || '4'
const saving = ref(false)
const rawUserData = ref<any>({})
const userForm = reactive({ address: '', phone: '', points: 0 })

// 强制获取后端最新数据
const fetchUser = async () => {
  try {
    const res: any = await request.get(`/users/${userId}`)
    rawUserData.value = res.data || res

    // 确保与后端的 defaultAddress 和 phoneNumber 字段正确映射
    userForm.address = rawUserData.value.defaultAddress || ''
    userForm.phone = rawUserData.value.phoneNumber || ''
    userForm.points = rawUserData.value.points || 0

    localStorage.setItem('userAddress', userForm.address)
    localStorage.setItem('userPhone', userForm.phone)
  } catch (e) {
    ElMessage.error('Infrastructure Link Failure: Sync Error')
  }
}

// 确保正确组装 payload 保存到数据库
const updateUserInfo = async () => {
  if (!userForm.address || !userForm.phone) return ElMessage.warning('Field validation failed')
  saving.value = true
  try {
    // 确保传输字段和数据库实体一致
    const payload = {
      ...rawUserData.value,
      defaultAddress: userForm.address,
      phoneNumber: userForm.phone
    }

    await request.put(`/users/${userId}`, payload)

    // 更新本地缓存以防万一
    localStorage.setItem('userAddress', userForm.address)
    localStorage.setItem('userPhone', userForm.phone)

    ElMessage.success('Fulfillment data synchronized successfully!')
    fetchUser()
  } catch (e) {
    ElMessage.error('Synchronization failed')
  } finally {
    saving.value = false
  }
}

const handleLogout = () => { localStorage.clear(); window.location.href = '/login'; }
onMounted(fetchUser)
</script>

<style scoped>
.profile-page { background: #f8fafc; min-height: 100vh; padding-bottom: 50px; }
.brand-stripe { height: 4px; background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%); }
.member-hero { background: #1e293b; padding: 40px 24px 60px; color: white; position: relative; overflow: hidden; }
.member-hero::after { content: "7"; position: absolute; right: -20px; bottom: -40px; font-size: 200px; font-weight: 900; color: rgba(255,255,255,0.03); }
.hero-content { display: flex; align-items: center; gap: 20px; position: relative; z-index: 1; }
.avatar-container { position: relative; }
.member-avatar { border: 4px solid #334155; }
.status-ring { position: absolute; bottom: 0; right: 0; width: 18px; height: 18px; background: #007934; border: 3px solid #1e293b; border-radius: 50%; }
.id-row { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.member-id { font-family: monospace; font-weight: 700; color: #94a3b8; font-size: 13px; }
.active-tag { background: #007934 !important; color: white !important; border: none; font-weight: 800; font-size: 9px; }
.welcome-text { margin: 0; font-size: 18px; font-weight: 800; }
.wallet-card { margin: -30px 20px 24px; background: white; border-radius: 20px; padding: 24px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 10px 25px rgba(0,0,0,0.05); position: relative; z-index: 2; }
.wallet-label { font-size: 11px; font-weight: 800; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; }
.points-display { display: flex; align-items: baseline; gap: 4px; margin-top: 4px; }
.points-num { font-size: 36px; font-weight: 900; color: #ff7900; line-height: 1; }
.points-unit { font-size: 12px; font-weight: 800; color: #ff7900; }
.settings-body { padding: 0 20px; }
.settings-card { border-radius: 16px; border: 1px solid #f1f5f9; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 800; color: #475569; font-size: 14px; text-transform: uppercase; }
.custom-form :deep(.el-form-item__label) { font-size: 10px; font-weight: 800; color: #94a3b8; margin-bottom: 8px !important; }
.commit-btn { width: 100%; height: 48px; border-radius: 12px; font-weight: 800; background-color: #007934 !important; border: none; box-shadow: 0 8px 16px rgba(0, 121, 52, 0.2); margin-top: 10px; }
.system-section { margin-top: 24px; }
.action-item { background: white; padding: 16px 20px; border-radius: 12px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; border: 1px solid #f1f5f9; transition: all 0.2s; }
.action-item:active { transform: scale(0.98); background: #f8fafc; }
.action-left { display: flex; align-items: center; gap: 12px; font-weight: 700; font-size: 14px; }
.logout { color: #ef4444; }
.app-version { text-align: center; font-size: 10px; color: #cbd5e1; font-weight: 700; margin-top: 30px; text-transform: uppercase; letter-spacing: 1px; }
</style>
