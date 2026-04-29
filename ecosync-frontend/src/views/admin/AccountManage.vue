<template>
  <div class="account-container">
    <el-card class="brand-card" shadow="never">
      <template #header>
        <div class="card-header-wrapper">
          <div class="header-left">
            <div class="title-icon"></div>
            <span class="main-title">Account Management Center</span>
            <el-tag effect="plain" round class="count-badge">Total: {{ users.length }}</el-tag>
          </div>
          <el-button type="success" class="add-btn" @click="openAddDialog">
            <el-icon style="margin-right: 5px;"><Plus /></el-icon> Add Internal Account
          </el-button>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" stripe class="brand-table">
        <el-table-column label="UID" width="80">
          <template #default="{ row }">
            <span class="uid-text">#{{ row.userId || row.user_id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="Username">
          <template #default="{ row }">
            <div class="username-cell">
              <el-avatar :size="24" class="user-avatar">{{ row.username?.charAt(0).toUpperCase() }}</el-avatar>
              <span class="user-name">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Role" width="120">
          <template #default="{ row }">
            <el-tag :class="['role-tag', row.role?.toLowerCase()]" effect="dark" round>
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Status" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'BANNED' ? 'danger' : 'success'" size="small">
              {{ row.status === 'BANNED' ? 'BANNED' : 'ACTIVE' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="250" align="right">
          <template #default="{ row }">
            <template v-if="row.role !== 'ADMIN'">
              <el-button
                link
                :type="row.status === 'BANNED' ? 'success' : 'warning'"
                @click="toggleBan(row)"
              >
                {{ row.status === 'BANNED' ? 'Unban' : 'Ban' }}
              </el-button>

              <el-button link type="danger" @click="handleDelete(row)">Delete</el-button>
            </template>
            <span v-else class="admin-lock-text">System Protected</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="Register Internal Account" width="420px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item label="Account Name">
          <el-input v-model="form.username" placeholder="Enter username" />
        </el-form-item>

        <el-form-item label="Login Password">
          <el-input v-model="form.passwordHash" type="password" show-password placeholder="Enter password" />
        </el-form-item>

        <el-form-item label="Assign Role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="Administrator (ADMIN)" value="ADMIN" />
            <el-option label="Employee (EMPLOYEE)" value="EMPLOYEE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">Cancel</el-button>
        <el-button type="success" :loading="submitting" @click="createUser">Submit</el-button>
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
const submitting = ref(false)

const form = ref({
  username: '',
  passwordHash: '',
  role: 'EMPLOYEE',
  phoneNumber: '0000000000'
})

const openAddDialog = () => {
  form.value = { username: '', passwordHash: '', role: 'EMPLOYEE', phoneNumber: '0000000000' }
  showAdd.value = true
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/users')
    users.value = res.data || res
  } catch (e) {
    ElMessage.error('Data synchronization failed')
  } finally {
    loading.value = false
  }
}

const createUser = async () => {
  if (!form.value.username || !form.value.passwordHash) {
    return ElMessage.warning('Information incomplete')
  }
  submitting.value = true
  try {
    await request.post('/users', form.value)
    ElMessage.success('Account created successfully')
    showAdd.value = false
    fetchUsers()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || 'Registration failed')
  } finally {
    submitting.value = false
  }
}

const toggleBan = async (row: any) => {
  const id = row.userId || row.user_id
  const isBanning = row.status !== 'BANNED'
  const actionText = isBanning ? 'ban' : 'unban'

  try {
    await ElMessageBox.confirm(`Are you sure you want to ${actionText} user "${row.username}"?`)

    const updateData = {
      ...row,
      status: isBanning ? 'BANNED' : 'NORMAL'
    }

    await request.put(`/users/${id}`, updateData)

    ElMessage.success(`${actionText.charAt(0).toUpperCase() + actionText.slice(1)} successful`)
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(`Failed to ${actionText} user`)
    }
  }
}

const handleDelete = (row: any) => {
  const id = row.userId || row.user_id
  ElMessageBox.confirm(`Are you sure you want to delete user ${row.username}?`).then(async () => {
    try {
      await request.delete(`/users/${id}`)
      ElMessage.success('Deleted successfully')
      fetchUsers()
    } catch (e) {
      ElMessage.error('Deletion failed')
    }
  })
}

onMounted(fetchUsers)
</script>

<style scoped>
.account-container { padding: 30px; background: #f8f9fa; min-height: 80vh; }
.brand-card { border-radius: 12px; }
.card-header-wrapper { display: flex; justify-content: space-between; align-items: center; }
.title-icon { width: 4px; height: 20px; background: #007934; border-radius: 2px; margin-right: 12px; display: inline-block; vertical-align: middle; }
.main-title { font-size: 17px; font-weight: 800; color: #1a1a1a; }
.uid-text { font-family: monospace; font-weight: bold; color: #ff7900; }
.username-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar { background-color: #007934; font-weight: bold; }
.role-tag { border: none; font-weight: 800; min-width: 80px; text-align: center; }
.role-tag.admin { background-color: #e2231a !important; }
.role-tag.employee { background-color: #007934 !important; }
.admin-lock-text { font-size: 12px; color: #94a3b8; font-style: italic; }
</style>
