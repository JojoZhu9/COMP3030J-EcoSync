<template>
  <el-card shadow="never">
    <template #header>
      <div style="font-weight: bold; color: #007934;">List New Item</div>
    </template>
    <el-form :model="form" label-position="top">
      <el-form-item label="Product Library" required>
        <el-select v-model="form.barcode" filterable style="width: 100%" placeholder="Select SPU">
          <el-option v-for="p in library" :key="p.barcode" :label="p.productName" :value="p.barcode" />
        </el-select>
      </el-form-item>
      <el-form-item label="Expiration Date" required>
        <el-date-picker v-model="form.expirationTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
      </el-form-item>
      <el-form-item label="Stock Quantity">
        <el-input-number v-model="form.remainingStock" :min="1" style="width: 100%" />
      </el-form-item>
      <el-button type="success" style="width: 100%; background: #007934; border: none; font-weight: bold;" @click="submit" :loading="busy">
        Confirm Listing
      </el-button>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from '@/utils/message'
import request from '@/utils/request'

const emit = defineEmits(['refresh'])
const library = ref([])
const busy = ref(false)

const form = reactive({
  barcode: '',
  expirationTime: '',
  remainingStock: 1,
  storeId: Number(localStorage.getItem('lastStoreId') || 1),
  status: 'AVAILABLE',
  createdBy: Number(localStorage.getItem('userId') || 1)
})

const fetchLibrary = async () => {
  const res: any = await request.get('/products')
  library.value = res.data || res
}

const submit = async () => {
  if(!form.barcode || !form.expirationTime) return ElMessage.warning("Incomplete data")
  busy.value = true
  try {
    await request.post('/expiring-products', form)
    ElMessage.success("Listed Successfully")
    form.barcode = ''; form.expirationTime = '';
    emit('refresh')
  } finally {
    busy.value = false
  }
}

onMounted(fetchLibrary)
</script>
