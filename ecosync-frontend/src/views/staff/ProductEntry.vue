<el-col :span="15">
<el-card>
  <template #header>
    <div class="flex-between">
        <span>
          {{ isAdmin ? '全系统临期商品监管 (Admin)' : `本分店上架记录 (store_id: ${storeId})` }}
        </span>
      <div class="flex-gap">
        <el-select v-if="isAdmin" v-model="adminFilterStore" placeholder="筛选门店" size="small" clearable @change="fetchMyStock" style="width: 120px">
          <el-option v-for="i in 10" :key="i" :label="`门店 ${i}`" :value="i" />
        </el-select>
        <el-button size="small" type="primary" @click="fetchMyStock">刷新数据</el-button>
      </div>
    </div>
  </template>

  <el-table :data="myStock" border size="small" stripe v-loading="loading">
    <el-table-column prop="product_id" label="ID" width="60" />
    <el-table-column prop="store_id" label="门店" width="60" v-if="isAdmin" />
    <el-table-column prop="barcode" label="条码" width="130" />
    <el-table-column prop="expiration_time" label="过期时间" width="160" sortable />
    <el-table-column prop="remaining_stock" label="库存" width="70" />
    <el-table-column label="状态">
      <template #default="{ row }">
        <el-tag :type="statusMap[row.status].type" size="small">
          {{ statusMap[row.status].label }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="created_by" label="操作员" width="70" />
  </el-table>
</el-card>
</el-col>

<script setup lang="ts">
// ... 原有 import 保持不变 ...

const isAdmin = ref(localStorage.getItem('userRole') === 'ADMIN')
const adminFilterStore = ref('')
const loading = ref(false)

const statusMap: any = {
  'AVAILABLE': { label: '在售', type: 'success' },
  'SOLD_OUT': { label: '已售罄', type: 'info' },
  'DISCARDED': { label: '已报损', type: 'danger' }
}

const fetchMyStock = async () => {
  loading.value = true
  try {
    // 逻辑：如果是管理员且没选特定门店，查询全部；否则按门店查询
    let url = '/expiring-products'
    if (isAdmin.value && adminFilterStore.value) {
      url += `?store_id=${adminFilterStore.value}`
    } else if (!isAdmin.value) {
      url += `?store_id=${storeId}`
    }

    const res: any = await request.get(url)
    myStock.value = res.data || res
  } catch (e) {
    ElMessage.error('获取库存数据失败')
  } finally {
    loading.value = false
  }
}

// 修改上架逻辑，确保数据与数据库表结构 image_13b3b1.png 一致
const submitEntry = async () => {
  if (form.price <= 0) return ElMessage.error('时间设置无效')
  submitting.value = true

  const payload = {
    barcode: selectedBarcode.value,
    store_id: Number(storeId),
    expiration_time: form.expTime, // 确保后端接受 datetime 格式
    remaining_stock: form.stock,
    status: 'AVAILABLE',
    created_by: Number(userId)
  }

  try {
    await request.post('/expiring-products', payload)
    ElMessage.success({
      message: `商品 [${selData.value.product_name}] 已成功上架！`,
      type: 'success'
    })
    // 重置表单
    selectedBarcode.value = ''
    selData.value = null
    form.expTime = ''
    fetchMyStock()
  } catch (e) {
    ElMessage.error('上架失败，请检查网络或条码是否重复')
  } finally {
    submitting.value = false
  }
}
</script>
