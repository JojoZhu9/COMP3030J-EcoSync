<template>
  <div class="profile-wrapper">
    <div class="user-hero">
      <div class="user-main">
        <el-avatar :size="80" src="https://api.dicebear.com/7.x/avataaars/svg?seed=Lucky" class="avatar-border" />
        <div class="user-text">
          <h2 class="nickname">用户 ID: {{ userId }}</h2>
          <el-tag size="small" effect="plain" round type="info">账号已激活</el-tag>
        </div>
      </div>
    </div>

    <div class="asset-card">
      <div class="asset-header"><span>账户积分</span></div>
      <div class="asset-body">
        <div class="points-box">
          <span class="num">{{ userForm.points }}</span>
          <span class="unit">Points</span>
        </div>
      </div>
    </div>

    <div class="info-form-container">
      <el-card class="form-card">
        <template #header>
          <div class="card-header"><span><el-icon><Edit /></el-icon> 配送信息设置</span></div>
        </template>
        <el-form :model="userForm" label-position="top">
          <el-form-item label="默认收货地址">
            <el-input v-model="userForm.address" placeholder="请输入您的住址" clearable />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="userForm.phone" placeholder="请输入电话" clearable />
          </el-form-item>
          <el-button type="primary" class="save-btn" @click="updateUserInfo" :loading="saving">保存并同步</el-button>
        </el-form>
      </el-card>

      <div class="menu-group mt-20">
        <div class="menu-item" @click="handleLogout">
          <el-icon class="logout"><SwitchButton /></el-icon>
          <span style="color:red; margin-left:10px">退出登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { Edit, SwitchButton } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const userId = localStorage.getItem('userId') || '4'
const saving = ref(false)
const rawUserData = ref<any>({})
const userForm = reactive({ address: '', phone: '', points: 0 })

const fetchUser = async () => {
  try {
    const res: any = await request.get(`/users/${userId}`)
    rawUserData.value = res.data || res
    userForm.address = rawUserData.value.defaultAddress || ''
    userForm.phone = rawUserData.value.phoneNumber || ''
    userForm.points = rawUserData.value.points || 0
    localStorage.setItem('userAddress', userForm.address)
    localStorage.setItem('userPhone', userForm.phone)
  } catch (e) { ElMessage.error('获取用户信息失败') }
}

const updateUserInfo = async () => {
  if (!userForm.address || !userForm.phone) return ElMessage.warning('请填写完整')
  saving.value = true
  try {
    const payload = { ...rawUserData.value, defaultAddress: userForm.address, phoneNumber: userForm.phone }
    await request.put(`/users/${userId}`, payload)
    localStorage.setItem('userAddress', userForm.address)
    localStorage.setItem('userPhone', userForm.phone)
    ElMessage.success('保存成功')
    fetchUser()
  } catch (e) { ElMessage.error('保存失败') } finally { saving.value = false }
}

const handleLogout = () => { localStorage.clear(); window.location.href = '/login'; }
onMounted(fetchUser)
</script>

<style scoped>
.profile-wrapper { background: #f7f8fa; min-height: 100vh; padding-bottom: 40px; }
.user-hero { background: linear-gradient(135deg, #1e293b 0%, #334155 100%); padding: 50px 24px 80px; color: white; }
.asset-card { margin: -40px 16px 20px; background: white; border-radius: 16px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }
.num { font-size: 32px; font-weight: bold; color: #f59e0b; }
.info-form-container { padding: 0 16px; }
.save-btn { width: 100%; margin-top: 10px; height: 45px; border-radius: 8px; font-weight: bold; }
.mt-20 { margin-top: 20px; }
.menu-group { background: white; border-radius: 12px; padding: 15px; cursor: pointer; }
</style>
