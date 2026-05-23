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
            <el-tag size="small" class="active-tag">{{ rawUserData.role || 'MEMBER' }}</el-tag>
          </div>
          <h2 class="welcome-text">{{ $t('consumer.profile.welcome', { name: rawUserData.username || $t('common.unknown') }) }}</h2>
        </div>
      </div>
      <div class="hero-decoration">
        <el-icon :size="120" color="rgba(255,255,255,0.03)"><User /></el-icon>
      </div>
    </div>

    <div class="wallet-card">
      <div class="wallet-content">
        <div class="wallet-left">
          <div class="wallet-header">
            <el-icon :size="16" color="#ff7900"><Wallet /></el-icon>
            <span class="wallet-label">{{ $t('consumer.profile.availableBalance') }}</span>
          </div>
          <div class="points-display">
            <span class="points-unit">¥</span>
            <span class="points-num">{{ formatBalance(rawUserData.balance) }}</span>
          </div>
        </div>
        <div class="wallet-right">
          <el-button type="warning" round size="default" class="recharge-btn" @click="handleRecharge">
            {{ $t('consumer.profile.topUp') }}
          </el-button>
        </div>
      </div>
      <div class="wallet-progress">
        <div class="progress-bar" :style="{ width: progressWidth + '%' }"></div>
      </div>
      <div class="wallet-hint">{{ $t('consumer.profile.nextTier', { amount: nextTierTarget.toFixed(2) }) }}</div>
    </div>

    <div class="settings-body">
      <div class="settings-card">
        <div class="card-header">
          <div class="card-title">
            <div class="title-icon green">
              <el-icon><User /></el-icon>
            </div>
            <div class="title-text">
              <span class="title-main">{{ $t('consumer.profile.profileContact') }}</span>
              <span class="title-sub">{{ $t('consumer.profile.manageInfo') }}</span>
            </div>
          </div>
        </div>
        <div class="card-body">
          <div class="form-group">
            <label class="form-label">
              <el-icon :size="14"><User /></el-icon>
              {{ $t('consumer.profile.usernameLabel') }}
            </label>
            <el-input
              v-model="rawUserData.username"
              :placeholder="$t('consumer.profile.usernamePlaceholder')"
              :prefix-icon="User"
              clearable
              class="custom-input"
            />
          </div>

          <div class="form-group">
            <label class="form-label">
              <el-icon :size="14"><Location /></el-icon>
              {{ $t('consumer.profile.addressLabel') }}
            </label>
            <el-input
              v-model="rawUserData.userAddress"
              :placeholder="$t('consumer.profile.addressPlaceholder')"
              :prefix-icon="Location"
              clearable
              class="custom-input"
            />
          </div>
          <div class="form-group">
            <label class="form-label">
              <el-icon :size="14"><Phone /></el-icon>
              {{ $t('consumer.profile.phoneLabel') }}
            </label>
            <el-input
              v-model="rawUserData.phoneNumber"
              :placeholder="$t('consumer.profile.phonePlaceholder')"
              :prefix-icon="Phone"
              clearable
              class="custom-input"
            />
          </div>
          <el-button
            type="primary"
            class="commit-btn"
            @click="updateUserInfo"
            :loading="saving"
          >
            {{ $t('consumer.profile.updateProfile') }}
          </el-button>
        </div>
      </div>

      <div class="settings-card">
        <div class="card-header">
          <div class="card-title">
            <div class="title-icon orange">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="title-text">
              <span class="title-main">{{ $t('consumer.profile.securitySettings') }}</span>
              <span class="title-sub">{{ $t('consumer.profile.protectAccount') }}</span>
            </div>
          </div>
        </div>
        <div class="card-body">
          <div class="security-item">
            <div class="sec-left">
              <div class="sec-icon-box">
                <el-icon :size="18"><Key /></el-icon>
              </div>
              <div class="sec-info">
                <div class="sec-label">{{ $t('consumer.profile.accountPassword') }}</div>
                <div class="sec-desc">{{ $t('consumer.profile.passwordDesc') }}</div>
              </div>
            </div>
            <el-button type="info" plain round size="small" class="sec-btn" @click="pwdDialogVisible = true">
              {{ $t('consumer.profile.change') }}
            </el-button>
          </div>
          <div class="divider"></div>
          <div class="security-item">
            <div class="sec-left">
              <div class="sec-icon-box red">
                <el-icon :size="18"><SwitchButton /></el-icon>
              </div>
              <div class="sec-info">
                <div class="sec-label">{{ $t('consumer.profile.session') }}</div>
                <div class="sec-desc">{{ $t('consumer.profile.signOutDesc') }}</div>
              </div>
            </div>
            <el-button type="danger" plain round size="small" class="sec-btn" @click="handleLogout">
              {{ $t('consumer.profile.logOut') }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="footer-info">
        <div class="footer-brand">
          <span class="footer-logo">7-ELEVEn</span>
          <span class="footer-divider">|</span>
          <span class="footer-version">v2.4.0-Stable</span>
        </div>
        <div class="footer-copy">7-Eco Framework · All rights reserved</div>
      </div>
    </div>

    <el-dialog
      v-model="pwdDialogVisible"
      :title="$t('consumer.profile.changePassword')"
      width="400px"
      align-center
      class="modern-dialog"
      @closed="resetPwdForm"
    >
      <div class="dialog-icon">
        <el-icon :size="48" color="#008163"><Lock /></el-icon>
      </div>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top">
        <el-form-item :label="$t('consumer.profile.newPasswordLabel')" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password :placeholder="$t('consumer.profile.newPasswordPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('consumer.profile.confirmPasswordLabel')" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password :placeholder="$t('consumer.profile.confirmPasswordPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pwdDialogVisible = false" round>{{ $t('common.cancel') }}</el-button>
          <el-button type="success" :loading="pwdSaving" @click="handleUpdatePassword" round>
            {{ $t('consumer.profile.confirmUpdate') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { Location, Phone, Lock, Wallet, User, Key, SwitchButton } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessageBox, type FormInstance } from 'element-plus'
import { ElMessage } from '@/utils/message'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
const userId = localStorage.getItem('userId') || '12'
const saving = ref(false)
const pwdSaving = ref(false)
const pwdDialogVisible = ref(false)
const pwdFormRef = ref<FormInstance>()

const rawUserData = ref<any>({
  userId: Number(userId),
  username: '',
  passwordHash: '',
  role: '',
  status: '',
  storeId: null,
  balance: 0,
  phoneNumber: '',
  userAddress: '',
  createdAt: null
})

const pwdForm = reactive({ newPassword: '', confirmPassword: '' })

const pwdRules = reactive({
  newPassword: [{ required: true, min: 6, message: () => t('consumer.profile.pwdMinLength'), trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: () => t('consumer.profile.pwdConfirm'), trigger: 'blur' },
    { validator: (rule: any, value: any, callback: any) => {
        if (value !== pwdForm.newPassword) callback(new Error(t('consumer.profile.pwdMismatch')))
        else callback()
      }, trigger: 'blur'
    }
  ]
})

// 动态计算下一个等级的目标金额
const nextTierTarget = computed(() => {
  const bal = Number(rawUserData.value.balance) || 0
  if (bal < 500) return 500
  if (bal < 1000) return 1000
  if (bal < 5000) return 5000
  return Math.ceil((bal + 1) / 5000) * 5000 // 超过5000后每5000一升级
})

// 计算进度条真实百分比
const progressWidth = computed(() => {
  const bal = Number(rawUserData.value.balance) || 0
  return Math.min((bal / nextTierTarget.value) * 100, 100)
})

const fetchUser = async () => {
  try {
    const res: any = await request.get(`/users/${userId}`)
    const data = res.data?.data || res.data || res
    if (data && typeof data === 'object') rawUserData.value = data
  } catch (e) {
    ElMessage.error(t('consumer.profile.profileSyncFailed'))
  }
}

const handleUpdatePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdSaving.value = true
    try {
      const payload = { ...rawUserData.value, passwordHash: pwdForm.newPassword }
      await request.put(`/users/${userId}`, payload)
      ElMessage.success(t('consumer.profile.passwordUpdated'))
      pwdDialogVisible.value = false
      await fetchUser()
    } catch (e: any) {
      ElMessage.error(t('consumer.profile.passwordUpdateFailed'))
    } finally {
      pwdSaving.value = false
    }
  })
}

// 更新用户信息（包括用户名、地址、电话）
const updateUserInfo = async () => {
  if (!rawUserData.value.username || !rawUserData.value.userAddress || !rawUserData.value.phoneNumber) {
    return ElMessage.warning(t('consumer.profile.fieldsEmpty'))
  }
  if (!/^\d{11}$/.test(rawUserData.value.phoneNumber)) {
    return ElMessage.warning(t('consumer.profile.phoneInvalid'))
  }
  saving.value = true
  try {
    await request.put(`/users/${userId}`, rawUserData.value)
    ElMessage.success(t('consumer.profile.profileSaved'))
    await fetchUser() // 刷新本地数据以更新 UI 上的欢迎词
  } catch (e) {
    ElMessage.error(t('consumer.profile.updateFailed'))
  } finally {
    saving.value = false
  }
}

const handleRecharge = () => {
  ElMessageBox.prompt(t('consumer.profile.amountLabel'), t('consumer.profile.topUpTitle'), {
    confirmButtonText: t('common.confirm'),
    inputPattern: /^\d+(\.\d{1,2})?$/,
    inputErrorMessage: t('consumer.profile.invalidAmount'),
  }).then(async ({ value }) => {
    try {
      const currentBalance = Number(rawUserData.value.balance) || 0
      const newBalance = (currentBalance + parseFloat(value)).toFixed(2)
      const payload = { ...rawUserData.value, balance: newBalance }
      await request.put(`/users/${userId}`, payload)
      ElMessage.success(t('consumer.profile.balanceUpdated'))
      await fetchUser()
    } catch (e) {
      ElMessage.error(t('consumer.profile.transactionFailed'))
    }
  })
}

const handleLogout = () => {
  ElMessageBox.confirm(t('consumer.profile.signOutConfirm'), t('consumer.profile.logOutTitle'), {
    confirmButtonText: t('consumer.profile.yesSignOut'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
    localStorage.removeItem('cart')
    window.dispatchEvent(new Event('auth-change'))
    ElMessage.success(t('consumer.profile.signedOut'))
    router.push('/login')
  })
}

const formatBalance = (val: any) => (val ? Number(val).toFixed(2) : '0.00')
const resetPwdForm = () => {
  pwdForm.newPassword = ''; pwdForm.confirmPassword = ''
  pwdFormRef.value?.resetFields()
}

onMounted(fetchUser)
</script>

<style scoped>
/* 样式部分保持不变，确保页面美观度 */
.profile-page { background: #f4f6f8; min-height: 100vh; padding-bottom: 40px; }
.brand-stripe { height: 4px; background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%); }
.member-hero {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  padding: 40px 24px 70px; color: white; position: relative; overflow: hidden;
}
.hero-content { display: flex; align-items: center; gap: 20px; position: relative; z-index: 1; }
.hero-decoration { position: absolute; right: -20px; top: 50%; transform: translateY(-50%); z-index: 0; }
.avatar-container { position: relative; }
.member-avatar { border: 4px solid rgba(255,255,255,0.1); box-shadow: 0 8px 24px rgba(0,0,0,0.3); }
.status-ring {
  position: absolute; bottom: 2px; right: 2px; width: 20px; height: 20px;
  background: #007934; border: 3px solid #1e293b; border-radius: 50%;
}
.id-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.member-id { font-family: monospace; font-weight: 700; color: #94a3b8; font-size: 13px; }
.active-tag {
  background: linear-gradient(135deg, #007934, #006b52) !important;
  color: white !important; border: none; font-weight: 800; font-size: 10px; letter-spacing: 0.5px;
}
.welcome-text { margin: 0; font-size: 20px; font-weight: 900; letter-spacing: -0.3px; }
.wallet-card {
  margin: -40px 20px 24px; background: white; border-radius: 20px; padding: 24px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08); position: relative; z-index: 2; border: 1px solid rgba(0,0,0,0.04);
}
.wallet-content { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.wallet-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.wallet-label { font-size: 11px; font-weight: 800; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; }
.points-display { display: flex; align-items: baseline; gap: 4px; }
.points-num { font-size: 36px; font-weight: 900; color: #ff7900; line-height: 1; }
.points-unit { font-size: 20px; font-weight: 800; color: #ff7900; }
.recharge-btn {
  background: linear-gradient(135deg, #ff7900, #ee7203) !important;
  border: none !important; color: white !important; font-weight: 800; padding: 0 20px; height: 40px;
  box-shadow: 0 4px 12px rgba(238,114,3,0.3);
}
.wallet-progress { height: 4px; background: #f1f5f9; border-radius: 2px; overflow: hidden; }
.progress-bar { height: 100%; background: linear-gradient(90deg, #ff7900, #ee7203); border-radius: 2px; transition: width 0.5s ease; }
.wallet-hint { font-size: 11px; color: #94a3b8; margin-top: 8px; text-align: right; font-weight: 600; }
.settings-body { padding: 0 20px; }
.settings-card {
  background: white; border-radius: 20px; margin-bottom: 20px; border: 1px solid rgba(0,0,0,0.04);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03); overflow: hidden; transition: all 0.3s ease;
}
.settings-card:hover { box-shadow: 0 8px 20px rgba(0,0,0,0.06); }
.card-header { padding: 20px 20px 0; }
.card-title { display: flex; align-items: center; gap: 14px; }
.title-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: white; font-size: 20px; }
.title-icon.green { background: linear-gradient(135deg, #008163, #006b52); box-shadow: 0 4px 12px rgba(0,129,99,0.2); }
.title-icon.orange { background: linear-gradient(135deg, #ff7900, #ee7203); box-shadow: 0 4px 12px rgba(238,114,3,0.2); }
.title-text { display: flex; flex-direction: column; }
.title-main { font-weight: 800; color: #1e293b; font-size: 15px; }
.title-sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.card-body { padding: 20px; }
.form-group { margin-bottom: 20px; }
.form-label { display: flex; align-items: center; gap: 6px; font-size: 10px; font-weight: 800; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 10px; }
.custom-input :deep(.el-input__wrapper) { background: #f8fafc; box-shadow: none !important; border: 1px solid #e2e8f0; border-radius: 12px; padding: 4px 12px; }
.custom-input :deep(.el-input__wrapper.is-focus) { border-color: #008163; box-shadow: 0 0 0 3px rgba(0,129,99,0.1) !important; }
.commit-btn {
  width: 100%; height: 48px; border-radius: 14px; font-weight: 800; font-size: 15px;
  background: linear-gradient(135deg, #008163, #006b52) !important; border: none !important;
  box-shadow: 0 4px 12px rgba(0,129,99,0.2); transition: all 0.2s;
}
.commit-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(0,129,99,0.3); }
.security-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.sec-left { display: flex; align-items: center; gap: 14px; }
.sec-icon-box { width: 44px; height: 44px; border-radius: 12px; background: #f1f5f9; color: #64748b; display: flex; align-items: center; justify-content: center; }
.sec-icon-box.red { background: #fef2f2; color: #ef4444; }
.sec-info { display: flex; flex-direction: column; }
.sec-label { font-size: 14px; font-weight: 800; color: #1e293b; }
.sec-desc { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.sec-btn { font-weight: 700; }
.divider { height: 1px; background: #f1f5f9; margin: 8px 0; }
.footer-info { text-align: center; padding: 30px 0 20px; }
.footer-brand { display: flex; align-items: center; justify-content: center; gap: 10px; margin-bottom: 8px; }
.footer-logo { font-weight: 900; font-size: 14px; color: #008163; letter-spacing: 1px; }
.footer-divider { color: #cbd5e1; }
.footer-version { font-size: 11px; color: #94a3b8; font-weight: 700; }
.footer-copy { font-size: 10px; color: #cbd5e1; }
:deep(.modern-dialog .el-dialog) { border-radius: 20px; overflow: hidden; }
:deep(.modern-dialog .el-dialog__header) { padding: 24px 24px 0; margin-bottom: 8px; }
:deep(.modern-dialog .el-dialog__title) { font-weight: 800; font-size: 18px; color: #1e293b; }
:deep(.modern-dialog .el-dialog__body) { padding: 0 24px 16px; }
.dialog-icon { width: 72px; height: 72px; background: #f0fdf4; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }
:deep(.modern-dialog .el-form-item__label) { font-size: 10px; font-weight: 800; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; }
:deep(.modern-dialog .el-input__wrapper) { background: #f8fafc; box-shadow: none !important; border: 1px solid #e2e8f0; border-radius: 12px; }
.dialog-footer { display: flex; gap: 12px; }
.dialog-footer .el-button { flex: 1; height: 44px; font-weight: 700; border-radius: 12px; }
</style>
