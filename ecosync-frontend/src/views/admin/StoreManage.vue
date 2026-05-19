<template>
  <div class="store-container">
    <el-card class="modern-card" shadow="never">
      <template #header>
        <div class="card-header-wrapper">
          <div class="header-left">
            <div class="title-icon"><el-icon><Shop /></el-icon></div>
            <div class="title-text-box">
              <span class="main-title">Store Management</span>
              <span class="sub-title">Manage 7-ELEVEn store locations and assignments</span>
            </div>
            <el-tag effect="light" type="success" round class="count-badge">Total: {{ stores.length }}</el-tag>
          </div>
          <el-button type="success" class="brand-btn" @click="openAddDialog">
            <el-icon style="margin-right: 6px;"><Plus /></el-icon> Add Store
          </el-button>
        </div>
      </template>

      <div class="table-toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="Search store name or city..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>

      <el-table :data="filteredStores" v-loading="loading" class="custom-table" stripe border height="600">
        <el-table-column label="ID" width="80" align="center">
          <template #default="{ row }">
            <span class="uid-text">#{{ row.storeId || row.store_id }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="storeName" label="Store Name" min-width="180">
          <template #default="{ row }">
            <span class="store-name">{{ row.storeName || row.store_name }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="city" label="City" width="120" align="center" />

        <el-table-column prop="address" label="Address" min-width="200" />

        <el-table-column label="Coordinates" width="200" align="center">
          <template #default="{ row }">
            <span v-if="row.latitude && row.longitude" class="coord-text">
              {{ row.latitude.toFixed(4) }}, {{ row.longitude.toFixed(4) }}
            </span>
            <span v-else class="no-data">—</span>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="200" align="center">
          <template #default="{ row }">
            <el-button plain size="small" round type="primary" @click="openEditDialog(row)">Edit</el-button>
            <el-button plain size="small" round type="danger" @click="handleDelete(row)">Delete</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="No stores found" />
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? 'Edit Store' : 'Add New Store'" width="500px" class="modern-dialog" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item label="Store Name">
          <el-input v-model="form.storeName" placeholder="Enter store name" class="dialog-input" />
        </el-form-item>

        <el-form-item label="City">
          <el-input v-model="form.city" placeholder="Enter city" class="dialog-input" />
        </el-form-item>

        <el-form-item label="Address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="Enter full address" class="dialog-input" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Latitude">
              <el-input-number v-model="form.latitude" :precision="6" style="width: 100%" class="dialog-input-num" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Longitude">
              <el-input-number v-model="form.longitude" :precision="6" style="width: 100%" class="dialog-input-num" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="showDialog = false">Cancel</el-button>
          <el-button round type="success" class="brand-btn" :loading="submitting" @click="submit">{{ isEdit ? 'Update' : 'Add' }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { storeApi } from '@/api/store'
import { ElMessageBox } from 'element-plus'
import { ElMessage } from '@/utils/message'
import { Plus, Search, Shop } from '@element-plus/icons-vue'

const stores = ref<any[]>([])
const loading = ref(false)
const showDialog = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

const searchQuery = ref('')

const form = ref<any>({
  storeName: '',
  city: '',
  address: '',
  latitude: undefined,
  longitude: undefined
})

const filteredStores = computed(() => {
  let result = stores.value
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter((s) =>
      (s.storeName || s.store_name || '').toLowerCase().includes(q) ||
      (s.city || '').toLowerCase().includes(q) ||
      (s.address || '').toLowerCase().includes(q)
    )
  }
  return result
})

const openAddDialog = () => {
  isEdit.value = false
  form.value = { storeName: '', city: '', address: '', latitude: undefined, longitude: undefined }
  showDialog.value = true
}

const openEditDialog = (row: any) => {
  isEdit.value = true
  form.value = {
    storeId: row.storeId || row.store_id,
    storeName: row.storeName || row.store_name,
    city: row.city,
    address: row.address,
    latitude: row.latitude,
    longitude: row.longitude
  }
  showDialog.value = true
}

const fetchStores = async () => {
  loading.value = true
  try {
    const res: any = await storeApi.getAll()
    stores.value = res.data || res
  } catch (e) {
    ElMessage.error('Failed to load stores')
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!form.value.storeName || !form.value.city || !form.value.address) {
    return ElMessage.warning('Please fill in all required fields')
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await storeApi.update(form.value.storeId, form.value)
      ElMessage.success('Store updated successfully')
    } else {
      await storeApi.create(form.value)
      ElMessage.success('Store added successfully')
    }
    showDialog.value = false
    fetchStores()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || 'Operation failed')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row: any) => {
  const id = row.storeId || row.store_id
  const name = row.storeName || row.store_name
  ElMessageBox.confirm(`Delete store "${name}"?`, 'Warning', { type: 'error' }).then(async () => {
    try {
      await storeApi.delete(id)
      ElMessage.success('Deleted successfully')
      fetchStores()
    } catch (e) {
      ElMessage.error('Deletion failed. Store may have employees or inventory linked.')
    }
  }).catch(() => {})
}

onMounted(fetchStores)
</script>

<style scoped>
.store-container { min-height: 80vh; }
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

.table-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; background: #fdfdfd; border-bottom: 1px solid #f1f5f9; }
.search-input { width: 340px; }
.search-input :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; padding: 6px 12px; }
.search-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(0,129,99,0.4) inset; }

.custom-table { width: 100%; border-radius: 0 0 16px 16px; }
:deep(.el-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569; font-weight: 700; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; }
.uid-text { font-family: monospace; font-weight: 900; color: #64748b; font-size: 14px; }
.store-name { font-weight: 700; color: #1e293b; font-size: 14px; }
.coord-text { font-family: monospace; font-size: 12px; color: #008163; font-weight: 600; }
.no-data { color: #94a3b8; font-size: 12px; }

:deep(.modern-dialog) { border-radius: 20px; overflow: hidden; }
:deep(.modern-dialog .el-dialog__header) { padding: 24px; border-bottom: 1px solid #f1f5f9; margin-right: 0; }
:deep(.modern-dialog .el-dialog__title) { font-weight: 800; color: #1e293b; font-size: 18px; }
:deep(.modern-dialog .el-form-item__label) { font-weight: 700; color: #475569; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }
.dialog-input :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; }
.dialog-input-num :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; }
.dialog-footer { display: flex; gap: 12px; justify-content: flex-end; padding-top: 10px; }
</style>
