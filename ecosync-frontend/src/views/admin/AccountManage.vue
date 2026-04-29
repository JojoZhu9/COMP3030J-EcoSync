<template>
  <div class="account-container">
    <el-card class="brand-card" shadow="never">
      <template #header>
        <div class="card-header-wrapper">
          <div class="header-left">
            <div class="title-icon"></div>
            <span class="main-title">User Account Control Center</span>
            <el-tag effect="plain" round class="count-badge">Total: {{ users.length }}</el-tag>
          </div>
          <el-button type="success" class="add-btn" @click="showAdd = true">
            <el-icon style="margin-right: 5px;"><Plus /></el-icon> New Staff Account
          </el-button>
        </div>
      </template>

      <el-table
        :data="users"
        v-loading="loading"
        stripe
        class="brand-table"
        header-cell-class-name="brand-table-header"
      >
        <el-table-column prop="user_id" label="UID" width="90">
          <template #default="{ row }">
            <span class="uid-text">#{{ row.user_id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="Username">
          <template #default="{ row }">
            <div class="username-cell">
              <el-avatar :size="24" class="user-avatar">{{ row.username.charAt(0).toUpperCase() }}</el-avatar>
              <span class="user-name">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="role" label="Access Level" width="150">
          <template #default="{ row }">
            <el-tag
              :class="['role-tag', row.role.toLowerCase()]"
              effect="dark"
              round
            >
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Status" width="120">
          <template #default="{ row }">
            <div class="status-wrapper">
              <span :class="['status-dot', row.phone_number === 'BANNED' ? 'is-banned' : 'is-active']"></span>
              <span class="status-text">{{ row.phone_number === 'BANNED' ? 'Banned' : 'Active' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="220" align="right">
          <template #default="{ row }">
            <el-button
              size="small"
              class="action-btn"
              :type="row.phone_number === 'BANNED' ? 'success' : 'warning'"
              plain
              @click="handleBan(row)"
            >
              {{ row.phone_number === 'BANNED' ? 'Unban' : 'Ban Account' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              class="action-btn"
              link
              @click="handleKill(row)"
            >
              Remove
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="showAdd"
      title="Create New Staff Account"
      width="480px"
      custom-class="brand-dialog"
      destroy-on-close
    >
      <div class="dialog-stripe"></div>
      <el-form :model="form" label-position="top" class="brand-form">
        <el-form-item label="Full Username">
          <el-input v-model="form.username" placeholder="Enter unique username" />
        </el-form-item>
        <el-form-item label="Security Password">
          <el-input v-model="form.password" type="password" placeholder="Set a secure password" show-password />
        </el-form-item>
        <el-form-item label="Role Assignment">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="Administrator (Full Access)" value="ADMIN" />
            <el-option label="Staff Member (Standard Access)" value="EMPLOYEE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAdd = false" round>Cancel</el-button>
          <el-button type="success" @click="createUser" class="confirm-btn" round>Create Account</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const users = ref([])
const loading = ref(false)
const showAdd = ref(false)
const form = ref({ username: '', password: '', role: 'EMPLOYEE' })

const fetch = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/users')
    users.value = res.data || res
  } finally {
    loading.value = false
  }
}

const createUser = async () => {
  if(!form.value.username || !form.value.password) return ElMessage.warning('Please fill in all fields')
  try {
    await request.post('/users', form.value)
    ElMessage.success('Account created successfully')
    showAdd.value = false
    fetch()
  } catch (e) {
    ElMessage.error('Failed to create account')
  }
}

const handleBan = async (row: any) => {
  const isBanning = row.phone_number !== 'BANNED'
  const actionText = isBanning ? 'ban' : 'unban'

  ElMessageBox.confirm(`Are you sure you want to ${actionText} this user?`).then(async () => {
    row.phone_number = isBanning ? 'BANNED' : '13800000000'
    await request.put(`/users/${row.user_id}`, row)
    ElMessage.success(`User ${actionText}ned successfully`)
    fetch()
  }).catch(() => {})
}

const handleKill = (row: any) => {
  ElMessageBox.confirm('Permanent Deletion: This action cannot be undone. Proceed?').then(async () => {
    await request.delete(`/users/${row.user_id}`)
    ElMessage.success('Account permanently removed')
    fetch()
  }).catch(() => {})
}

onMounted(fetch)
</script>

<style scoped>
.account-container {
  padding: 30px;
  background-color: #f8f9fa;
  min-height: 100%;
}

.brand-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  background-color: #ffffff;
  position: relative;
  overflow: hidden;
}

.brand-card::before {
  content: "";
  position: absolute;
  top: 0; left: 0; width: 100%; height: 5px;
  background: linear-gradient(to right,
  #ff7900 0%, #ff7900 33.33%,
  #007934 33.33%, #007934 66.66%,
  #e2231a 66.66%, #e2231a 100%);
  z-index: 1;
}

.card-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  width: 4px;
  height: 20px;
  background: #007934;
  border-radius: 2px;
}

.main-title {
  font-size: 18px;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.count-badge {
  background: #f0fdf4;
  color: #007934;
  border-color: #bbf7d0;
  font-weight: 600;
}

.add-btn {
  background-color: #007934 !important;
  border: none;
  font-weight: 700;
  border-radius: 8px;
  padding: 10px 20px;
  box-shadow: 0 4px 12px rgba(0, 121, 52, 0.2);
}

:deep(.brand-table-header) {
  background-color: #fcfcfc !important;
  color: #888 !important;
  font-weight: 800 !important;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 1px;
}

.uid-text {
  font-family: 'Courier New', Courier, monospace;
  font-weight: bold;
  color: #ff7900;
}

.username-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background-color: #007934;
  font-weight: bold;
  font-size: 12px;
}

.user-name {
  font-weight: 600;
  color: #333;
}

.role-tag {
  border: none;
  font-weight: 800;
  min-width: 80px;
  text-align: center;
}
.role-tag.admin { background-color: #e2231a !important; }
.role-tag.employee { background-color: #007934 !important; }

.status-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.status-dot.is-active { background-color: #007934; box-shadow: 0 0 8px #007934; }
.status-dot.is-banned { background-color: #e2231a; }
.status-text {
  font-size: 13px;
  font-weight: 500;
  color: #555;
}

:deep(.brand-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.dialog-stripe {
  height: 4px;
  background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%);
  margin: -30px -20px 20px -20px;
}

.confirm-btn {
  background-color: #007934 !important;
  font-weight: 700;
}
</style>
