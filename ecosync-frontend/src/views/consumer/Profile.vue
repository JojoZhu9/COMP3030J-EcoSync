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
            <el-tag size="small" class="active-tag">{{ rawUserData.role || 'USER' }}</el-tag>
          </div>
          <h2 class="welcome-text">Welcome back, {{ rawUserData.username }}!</h2>
        </div>
      </div>
    </div>

    <div class="wallet-card">
      <div class="wallet-content">
        <div class="wallet-left">
          <span class="wallet-label">Available Balance</span>
          <div class="points-display">
            <span class="points-unit">¥</span>
            <span class="points-num">{{ formatBalance(rawUserData.balance) }}</span>
          </div>
        </div>
        <div class="wallet-right">
          <el-button type="warning" plain round size="small" @click="handleRecharge">Top Up</el-button>
        </div>
      </div>
    </div>

    <div class="settings-body">
      <el-card class="settings-card" shadow="never">
        <template #header>
          <div class="card-title"><el-icon><MapLocation /></el-icon><span>Logistics & Fulfillment</span></div>
        </template>
        <el-form label-position="top" class="custom-form">
          <el-form-item label="DEFAULT DELIVERY ADDRESS">
            <el-input v-model="rawUserData.userAddress" :prefix-icon="Location" clearable />
          </el-form-item>
          <el-form-item label="CONTACT TELEPHONE">
            <el-input v-model="rawUserData.phoneNumber" :prefix-icon="Phone" clearable />
          </el-form-item>
          <el-button type="primary" class="commit-btn" @click="updateUserInfo" :loading="saving">
            Update Profile Data
          </el-button>
        </el-form>
      </el-card>

      <el-card class="settings-card security-section" shadow="never">
        <template #header>
          <div class="card-title"><el-icon><Lock /></el-icon><span>Security Settings</span></div>
        </template>
        <div class="security-item">
          <div class="sec-info">
            <div class="sec-label">Account Password</div>
            <div class="sec-desc">Update plain text password; server will MD5 encrypt it.</div>
          </div>
          <el-button type="info" plain size="small" @click="pwdDialogVisible = true">Change Password</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="pwdDialogVisible" title="Change Password" width="400px" @closed="resetPwdForm">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top">
        <el-form-item label="NEW PASSWORD" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="CONFIRM NEW" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">Cancel</el-button>
        <el-button type="success" :loading="pwdSaving" @click="handleUpdatePassword">Confirm</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { MapLocation, Location, Phone, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'

// 1. 确保 ID 是数字类型（后端要求 Integer）
const userId = Number(localStorage.getItem('userId') || 12)
const saving = ref(false)
const pwdSaving = ref(false)
const pwdDialogVisible = ref(false)
const pwdFormRef = ref<FormInstance>()

// 2. 严格对齐 User.java 实体类
const rawUserData = ref<any>({
  userId: userId,
  username: '',
  passwordHash: '',
  role: '',
  status: '',
  balance: 0,
  phoneNumber: '',
  userAddress: ''
})

const pwdForm = reactive({ newPassword: '', confirmPassword: '' })
const pwdRules = reactive({
  newPassword: [{ required: true, min: 6, message: 'Min 6 chars', trigger: 'blur' }],
  confirmPassword: [{ validator: (r, v, cb) => (v !== pwdForm.newPassword ? cb(new Error('Mismatch!')) : cb()), trigger: 'blur' }]
})

const resetPwdForm = () => {
  pwdForm.newPassword = ''; pwdForm.confirmPassword = ''; pwdFormRef.value?.resetFields()
}

// 获取用户信息
const fetchUser = async () => {
  try {
    // 路径：/api/users/${id}
    const res: any = await request.get(`/api/users/${userId}`)
    rawUserData.value = res.data?.data || res.data || res
    console.log('User Data Synced:', rawUserData.value)
  } catch (e) {
    ElMessage.error('Fetch Error')
  }
}

// 核心：修改密码
const handleUpdatePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSaving.value = true
    try {
      // 构造完整 User 对象发送
      const payload = {
        ...rawUserData.value,
        passwordHash: pwdForm.newPassword  // 发送明文，后端 Service 自动 MD5
      }
      console.log('Sending Password Update:', payload)

      await request.put(`/api/users/${userId}`, payload)
      ElMessage.success('Password updated!')
      pwdDialogVisible.value = false
      await fetchUser()
    } catch (e) {
      ElMessage.error('Password Update Failed')
    } finally { pwdSaving.value = false }
  })
}

// 核心：更新地址和电话
const updateUserInfo = async () => {
  saving.value = true
  try {
    // 此时 rawUserData.passwordHash 里是旧的 MD5 密文
    // 后端 Service 判定 equals 成立，则不会重复加密
    console.log('Sending Profile Update:', rawUserData.value)

    await request.put(`/api/users/${userId}`, rawUserData.value)
    ElMessage.success('Profile saved!')
    await fetchUser()
  } catch (e) {
    ElMessage.error('Update Failed')
  } finally { saving.value = false }
}

// 核心：充值
const handleRecharge = () => {
  ElMessageBox.prompt('Amount (¥)', 'Top-up', {
    confirmButtonText: 'Confirm',
    inputPattern: /^\d+(\.\d{1,2})?$/,
    inputErrorMessage: 'Invalid number'
  }).then(async ({ value }) => {
    try {
      const payload = {
        ...rawUserData.value,
        balance: (Number(rawUserData.value.balance) + parseFloat(value)).toFixed(2)
      }
      await request.put(`/api/users/${userId}`, payload)
      ElMessage.success('Balance updated')
      await fetchUser()
    } catch (e) { ElMessage.error('Recharge failed') }
  })
}

const formatBalance = (val: any) => (val ? Number(val).toFixed(2) : '0.00')
const resetPwdFormFields = () => { resetPwdForm() }

onMounted(fetchUser)
</script>

<style scoped>
/* 样式部分保持不变，确保美观 */
.profile-page { background: #f8fafc; min-height: 100vh; padding-bottom: 50px; }
.brand-stripe { height: 4px; background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%); }
.member-hero { background: #1e293b; padding: 40px 24px 60px; color: white; position: relative; }
.hero-content { display: flex; align-items: center; gap: 20px; z-index: 1; position: relative; }
.member-avatar { border: 4px solid #334155; }
.status-ring { position: absolute; bottom: 0; right: 0; width: 18px; height: 18px; background: #007934; border: 3px solid #1e293b; border-radius: 50%; }
.id-row { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.member-id { font-family: monospace; font-weight: 700; color: #94a3b8; font-size: 13px; }
.active-tag { background: #007934 !important; color: white !important; border: none; font-weight: 800; font-size: 9px; }
.welcome-text { margin: 0; font-size: 18px; font-weight: 800; }
.wallet-card { margin: -30px 20px 24px; background: white; border-radius: 20px; padding: 24px; box-shadow: 0 10px 25px rgba(0,0,0,0.05); position: relative; z-index: 2; }
.wallet-content { display: flex; justify-content: space-between; align-items: center; }
.wallet-label { font-size: 11px; font-weight: 800; color: #94a3b8; text-transform: uppercase; }
.points-display { display: flex; align-items: baseline; gap: 4px; margin-top: 4px; }
.points-num { font-size: 36px; font-weight: 900; color: #ff7900; }
.points-unit { font-size: 20px; font-weight: 800; color: #ff7900; }
.settings-body { padding: 0 20px; }
.settings-card { border-radius: 16px; border: 1px solid #f1f5f9; margin-bottom: 20px; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 800; color: #475569; font-size: 14px; text-transform: uppercase; }
.security-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; }
.sec-label { font-size: 14px; font-weight: 800; color: #1e293b; }
.sec-desc { font-size: 12px; color: #94a3b8; }
.custom-form :deep(.el-form-item__label) { font-size: 10px; font-weight: 800; color: #94a3b8; margin-bottom: 8px !important; }
.commit-btn { width: 100%; height: 48px; border-radius: 12px; font-weight: 800; background-color: #007934 !important; border: none; margin-top: 10px; }
</style>
