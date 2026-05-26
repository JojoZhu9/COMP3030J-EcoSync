<template>
  <el-card shadow="never">
    <template #header>
      <div style="font-weight: bold; color: #007934;">{{ $t('staff.productEntry.listNewItem') }}</div>
    </template>
    <el-form :model="form" label-position="top">
      <el-form-item :label="$t('staff.productEntry.productLibrary')" required>
        <el-select v-model="form.barcode" filterable style="width: 100%" :placeholder="$t('staff.productEntry.selectSpu')">
          <el-option v-for="p in library" :key="p.barcode" :label="getLocale() === 'en' && p.productNameEn ? p.productNameEn : p.productName" :value="p.barcode" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('staff.productEntry.expirationDate')" required>
        <el-date-picker
          v-model="form.expirationTime"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
          :disabled-date="disabledDate"
        />
      </el-form-item>
      <el-form-item :label="$t('staff.productEntry.stockQuantity')">
        <el-input-number v-model="form.remainingStock" :min="1" :step="1" step-strictly style="width: 100%" />
      </el-form-item>
      <el-button type="success" style="width: 100%; background: #007934; border: none; font-weight: bold;" @click="submit" :loading="busy">
        {{ $t('staff.productEntry.confirmListing') }}
      </el-button>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getLocale } from '@/locales'
import { ElMessage } from '@/utils/message'
import request from '@/utils/request'

const { t } = useI18n()
const emit = defineEmits(['refresh'])
const library = ref<any[]>([])
const busy = ref(false)

const form = reactive({
  barcode: '',
  expirationTime: '',
  remainingStock: 1,
  storeId: Number(localStorage.getItem('storeId') || 1),
  status: 'AVAILABLE',
  createdBy: Number(localStorage.getItem('userId') || 1)
})

const disabledDate = (time: Date) => {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return time.getTime() < now.getTime()
}

const fetchLibrary = async () => {
  const res: any = await request.get('/products')
  library.value = res.data || res
}

const submit = async () => {
  if (!form.barcode || !form.expirationTime) return ElMessage.warning(t('staff.productEntry.incompleteData'))

  const selected = new Date(form.expirationTime)
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  if (selected.getTime() < now.getTime()) {
    return ElMessage.warning(t('staff.productEntry.expirationPast'))
  }

  if (!Number.isInteger(form.remainingStock) || form.remainingStock < 1) {
    return ElMessage.warning(t('staff.productEntry.stockPositiveInteger'))
  }

  busy.value = true
  try {
    await request.post('/expiring-products', form)
    ElMessage.success(t('staff.productEntry.listedSuccessfully'))
    form.barcode = ''; form.expirationTime = ''; form.remainingStock = 1
    emit('refresh')
  } finally {
    busy.value = false
  }
}

onMounted(fetchLibrary)
</script>
