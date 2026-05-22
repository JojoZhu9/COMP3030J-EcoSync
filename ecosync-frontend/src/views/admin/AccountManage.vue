<template>
  <div class="account-container">
    <el-card class="modern-card" shadow="never">
      <template #header>
        <div class="card-header-wrapper">
          <div class="header-left">
            <div class="title-icon"><el-icon><UserGroup /></el-icon></div>
            <div class="title-text-box">
              <span class="main-title">Account Management Center</span>
              <span class="sub-title">Manage internal staff and admin credentials</span>
            </div>
            <el-tag effect="light" type="success" round class="count-badge">Total: {{ filteredUsers.length }}</el-tag>
          </div>
          <el-button type="success" class="brand-btn" @click="openAddDialog">
            <el-icon style="margin-right: 6px;"><Plus /></el-icon> Add Account
          </el-button>
        </div>
      </template>

      <div class="table-toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="Search by UID or Username..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
        <el-radio-group v-model="roleFilter" class="custom-radio">
          <el-radio-button label="ALL">All Roles</el-radio-button>
          <el-radio-button label="ADMIN">Admins</el-radio-button>
          <el-radio-button label="EMPLOYEE">Employees</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredUsers" v-loading="loading" class="custom-table" stripe border height="600">
        <el-table-column label="UID" width="100" align="center">
          <template #default="{ row }">
            <span class="uid-text">#{{ row.userId || row.user_id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="Username" min-width="180">
          <template #default="{ row }">
            <div class="username-cell">
              <el-avatar :size="32" class="user-avatar">{{ row.username?.charAt(0).toUpperCase() }}</el-avatar>
              <span class="user-name">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="System Role" width="160" align="center">
          <template #default="{ row }">
            <el-tag :class="['role-tag', row.role?.toLowerCase()]" effect="dark" round>
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Store" min-width="180" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'EMPLOYEE' && row.storeId" effect="light" round class="store-tag" :title="storeNameMap[row.storeId] || 'Store #' + row.storeId">
              {{ storeNameMap[row.storeId] || 'Store #' + row.storeId }}
            </el-tag>
            <span v-else-if="row.role === 'EMPLOYEE'" class="no-store">Unassigned</span>
            <span v-else class="no-store">—</span>
          </template>
        </el-table-column>

        <el-table-column label="Account Status" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'BANNED' ? 'danger' : 'success'" effect="light" round class="status-tag">
              {{ row.status === 'BANNED' ? 'BANNED' : 'ACTIVE' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="280" align="center">
          <template #default="{ row }">
            <template v-if="row.role !== 'ADMIN'">
              <el-button
                plain
                size="small"
                round
                :type="row.status === 'BANNED' ? 'success' : 'warning'"
                @click="toggleBan(row)"
              >
                {{ row.status === 'BANNED' ? 'Unban' : 'Suspend' }}
              </el-button>
              <el-button v-if="row.role === 'EMPLOYEE'" plain size="small" round type="primary" @click="openMigrateDialog(row)">Migrate</el-button>
              <el-button plain size="small" round type="danger" @click="handleDelete(row)">Delete</el-button>
            </template>
            <span v-else class="admin-lock-text"><el-icon><Lock /></el-icon> System Protected</span>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="No accounts found matching criteria" />
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="Register Internal Account" width="450px" class="modern-dialog" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item label="Account Username">
          <el-input v-model="form.username" placeholder="Enter unique username" class="dialog-input" />
        </el-form-item>

        <el-form-item label="Secure Password">
          <el-input v-model="form.passwordHash" type="password" show-password placeholder="Enter secure password" class="dialog-input" />
        </el-form-item>

        <el-form-item label="Assign System Role">
          <el-select v-model="form.role" style="width: 100%" class="dialog-select">
            <el-option label="Administrator (ADMIN)" value="ADMIN" />
            <el-option label="Store Employee (EMPLOYEE)" value="EMPLOYEE" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.role === 'EMPLOYEE'" label="Assign Store">
          <el-select v-model="form.storeId" style="width: 100%" class="dialog-select" clearable placeholder="Select a store">
            <el-option v-for="s in stores" :key="s.storeId || s.store_id" :label="(s.storeName || s.store_name) + ' — ' + s.city" :value="s.storeId || s.store_id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="showAdd = false">Cancel</el-button>
          <el-button round type="success" class="brand-btn" :loading="submitting" @click="createUser">Register Account</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="showMigrate" title="Migrate Employee Store" width="400px" class="modern-dialog" destroy-on-close>
      <p style="margin-bottom: 16px; color: #475569;">
        Migrating employee <strong>{{ migrateTarget?.username }}</strong> to a new store:
      </p>
      <el-select v-model="migrateStoreId" style="width: 100%" placeholder="Select target store">
        <el-option v-for="s in stores" :key="s.storeId || s.store_id" :label="(s.storeName || s.store_name) + ' — ' + s.city" :value="s.storeId || s.store_id" />
      </el-select>
      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="showMigrate = false">Cancel</el-button>
          <el-button round type="success" class="brand-btn" :loading="migrateLoading" @click="submitMigrate">Confirm Migrate</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { storeApi } from '@/api/store'
import { ElMessageBox } from 'element-plus'
import { ElMessage } from '@/utils/message'
import { Plus, Search, UserFilled as UserGroup, Lock } from '@element-plus/icons-vue'

const users = ref<any[]>([])
const stores = ref<any[]>([])
const loading = ref(false)
const showAdd = ref(false)
const showMigrate = ref(false)
const submitting = ref(false)
const migrateLoading = ref(false)
const migrateTarget = ref<any>(null)

// 新增搜索与过滤状态
const searchQuery = ref('')
const roleFilter = ref('ALL') // 'ALL' | 'ADMIN' | 'EMPLOYEE'

const form = ref({ username: '', passwordHash: '', role: 'EMPLOYEE', phoneNumber: '0000000000', storeId: null as number | null })
const migrateStoreId = ref<number | null>(null)

const storeNameMap = computed(() => {
  const map: Record<number, string> = {}
  for (const s of stores.value) {
    map[s.storeId || s.store_id] = s.storeName || s.store_name
  }
  return map
})

const filteredUsers = computed(() => {
  let result = users.value

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter((u) =>
      (u.username || '').toLowerCase().includes(q) ||
      String(u.userId || u.user_id || '').includes(q)
    )
  }

  if (roleFilter.value !== 'ALL') {
    result = result.filter((u) => u.role === roleFilter.value)
  }

  return result
})

const openAddDialog = () => {
  form.value = { username: '', passwordHash: '', role: 'EMPLOYEE', phoneNumber: '0000000000', storeId: null }
  showAdd.value = true
}

const openMigrateDialog = (row: any) => {
  migrateTarget.value = row
  migrateStoreId.value = row.storeId || null
  showMigrate.value = true
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
  const actionText = isBanning ? 'suspend' : 'unban'
  try {
    await ElMessageBox.confirm(`Are you sure you want to ${actionText} user "${row.username}"?`, 'Confirm Action', { type: 'warning' })
    const updateData = { ...row, status: isBanning ? 'BANNED' : 'NORMAL' }
    await request.put(`/users/${id}`, updateData)
    ElMessage.success(`${actionText.charAt(0).toUpperCase() + actionText.slice(1)} successful`)
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(`Failed to ${actionText} user`)
  }
}

const handleDelete = (row: any) => {
  const id = row.userId || row.user_id
  ElMessageBox.confirm(`Are you sure you want to completely delete user ${row.username}?`, 'Warning', { type: 'error' }).then(async () => {
    try {
      await request.delete(`/users/${id}`)
      ElMessage.success('Deleted successfully')
      fetchUsers()
    } catch (e) { ElMessage.error('Deletion failed') }
  })
}

const fetchStores = async () => {
  try {
    const res: any = await storeApi.getAll()
    stores.value = res.data || res
  } catch (e) {
    ElMessage.error('Failed to load stores')
  }
}

const submitMigrate = async () => {
  if (!migrateTarget.value || !migrateStoreId.value) {
    return ElMessage.warning('Please select a target store')
  }
  migrateLoading.value = true
  try {
    const id = migrateTarget.value.userId || migrateTarget.value.user_id
    await request.put(`/users/${id}`, { ...migrateTarget.value, storeId: migrateStoreId.value })
    ElMessage.success('Employee migrated successfully')
    showMigrate.value = false
    fetchUsers()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || 'Migration failed')
  } finally {
    migrateLoading.value = false
  }
}

onMounted(() => {
  fetchUsers()
  fetchStores()
})
</script>

<style scoped>
.account-container { min-height: 80vh; }
.modern-card { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 4px 20px rgba(0,0,0,0.03) !important; overflow: hidden; }
:deep(.modern-card .el-card__header) { background: #fff; padding: 20px 24px; border-bottom: 1px solid #f1f5f9; }
:deep(.modern-card .el-card__body) { padding: 0; }

.card-header-wrapper { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 14px; }
.title-icon { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #008163, #005a46); color: white; display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: 0 4px 12px rgba(0,129,99,0.2); }
.title-text-box { display: flex; flex-direction: column; }
.main-title { font-size: 18px; font-weight: 800; color: #1e293b; }
.sub-title { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.count-badge { margin-left: 10px; font-weight: 800; border-radius: 12px; }

.brand-btn { background: #008163 !important; border: none; font-weight: 700; border-radius: 10px; height: 40px; padding: 0 20px; box-shadow: 0 4px 12px rgba(0,129,99,0.2); }

/* 工具栏 */
.table-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; background: #fdfdfd; border-bottom: 1px solid #f1f5f9; }
.search-input { width: 340px; }
.search-input :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; padding: 6px 12px; }
.search-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(0,129,99,0.4) inset; }

/* 表格样式 */
.custom-table { width: 100%; border-radius: 0 0 16px 16px; }
:deep(.el-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569; font-weight: 700; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; }
.uid-text { font-family: monospace; font-weight: 900; color: #64748b; font-size: 14px; }
.username-cell { display: flex; align-items: center; gap: 12px; }
.user-avatar { background: linear-gradient(135deg, #008163, #006b52); color: white; font-weight: 800; font-size: 14px; border: 2px solid #e2e8f0; }
.user-name { font-weight: 700; color: #1e293b; font-size: 14px; }

.role-tag { font-weight: 800; min-width: 90px; text-align: center; letter-spacing: 0.5px; border: none; }
.role-tag.admin { background: linear-gradient(135deg, #e2231a, #b91c1c) !important; box-shadow: 0 2px 8px rgba(226,35,26,0.2); }
.role-tag.employee { background: linear-gradient(135deg, #008163, #005a46) !important; box-shadow: 0 2px 8px rgba(0,129,99,0.2); }
.status-tag { font-weight: 800; letter-spacing: 1px; }

.admin-lock-text { font-size: 12px; color: #94a3b8; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 4px; }
.store-tag { font-weight: 700; background: #f0f9ff !important; color: #0369a1 !important; border: 1px solid #bae6fd !important; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-flex; }
.no-store { font-size: 12px; color: #94a3b8; font-weight: 600; }

/* 弹窗样式 */
:deep(.modern-dialog) { border-radius: 20px; overflow: hidden; }
:deep(.modern-dialog .el-dialog__header) { padding: 24px; border-bottom: 1px solid #f1f5f9; margin-right: 0; }
:deep(.modern-dialog .el-dialog__title) { font-weight: 800; color: #1e293b; font-size: 18px; }
:deep(.modern-dialog .el-form-item__label) { font-weight: 700; color: #475569; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }
.dialog-input :deep(.el-input__wrapper), .dialog-select :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; }
.dialog-footer { display: flex; gap: 12px; justify-content: flex-end; padding-top: 10px; }
</style>
