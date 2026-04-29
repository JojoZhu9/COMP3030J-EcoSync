<template>
  <div class="staff-profile-container">
    <el-card class="profile-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-icon><UserFilled /></el-icon>
          <span>Staff Identity Card</span>
        </div>
      </template>

      <el-descriptions :column="1" border v-if="staff.username">
        <el-descriptions-item label="Username">
          <span class="user-name">{{ staff.username }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="Employee ID">
          #{{ staff.userId || staff.user_id }}
        </el-descriptions-item>

        <el-descriptions-item label="System Role">
          <el-tag type="success" effect="dark">{{ staff.role }}</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="Assigned Store">
          <span class="store-highlight">{{ store.storeName || store.store_name }}</span>
        </el-descriptions-item>

        <el-descriptions-item label="Store City">
          {{ store.city }}
        </el-descriptions-item>

        <el-descriptions-item label="Full Address">
          <el-icon><Location /></el-icon> {{ store.address }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { UserFilled, Location } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(true)
const staff = ref<any>({})
const store = ref<any>({})

const fetchData = async () => {
  loading.value = true
  const uid = localStorage.getItem('userId')

  try {
    const uRes: any = await request.get(`/users/${uid}`)
    const userData = uRes.data || uRes
    staff.value = userData

    const sid = userData.storeId || userData.store_id

    if (sid) {
      const sRes: any = await request.get(`/stores/${sid}`)
      store.value = sRes.data || sRes
    }
  } catch (e) {
    console.error("Fetch profile failed", e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.staff-profile-container {
  padding: 40px;
  display: flex;
  justify-content: center;
}
.profile-card {
  width: 100%;
  max-width: 650px;
  border-radius: 12px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
  color: #007934;
}
.user-name {
  font-size: 18px;
  font-weight: bold;
}
.store-highlight {
  color: #007934;
  font-weight: bold;
}
:deep(.el-descriptions__label) {
  background-color: #f6ffed !important;
  width: 160px;
  font-weight: bold;
}
</style>
