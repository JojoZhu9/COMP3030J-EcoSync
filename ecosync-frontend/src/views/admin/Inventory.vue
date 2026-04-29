<template>
  <div class="inventory-container">
    <el-card class="brand-card" shadow="never">
      <template #header>
        <div class="header-content">
          <div class="header-left">
            <div class="brand-strip-vertical"></div>
            <div class="title-group">
              <span class="main-title">Standard Product Repository</span>
              <span class="sub-title">Inventory & Pricing Control</span>
            </div>
          </div>
          <el-button type="success" class="scan-btn" @click="openAddDialog">
            <el-icon class="mr-5"><Plus /></el-icon> Scan / Register SKU
          </el-button>
        </div>
      </template>

      <el-table :data="products" v-loading="loading" stripe class="sku-table">
        <el-table-column prop="barcode" label="Barcode / UPC" width="180">
          <template #default="{ row }">
            <span class="barcode-display">{{ row.barcode }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Item Description">
          <template #default="{ row }">
            <span class="product-name-text">{{ row.product_name || row.productName || 'Unnamed SKU' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Base Price" width="150">
          <template #default="{ row }">
            <div class="price-cell">
              <span class="currency">¥</span>
              <span class="amount">{{ parseFloat(row.normal_price || row.normalPrice || 0).toFixed(2) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Operation" width="220" align="right">
          <template #default="{ row }">
            <el-button type="primary" link class="action-link" @click="goDetail(row)">
              <el-icon><Setting /></el-icon> Logic Configuration
            </el-button>
            <el-button type="danger" link class="action-link delete" @click="handleDelete(row)">
              Retire SKU
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="Register New Master SKU" width="650px" custom-class="sku-dialog">
      <div class="dialog-brand-line"></div>
      <el-form :model="newP" label-position="top" class="sku-form">
        <el-row :gutter="25">
          <el-col :span="14">
            <el-form-item label="Product Name / Description">
              <el-input v-model="newP.product_name" placeholder="e.g. 7-Select Onigiri Tuna" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="Base Unit Price (¥)">
              <el-input-number v-model="newP.normal_price" :precision="2" :step="0.5" style="width:100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Scan Barcode Data">
          <el-input v-model="newP.barcode" placeholder="Scan or manually enter UPC/EAN code">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-divider content-position="left">
          <span class="divider-label"><el-icon><TrendCharts /></el-icon> Discount Curve (T-12h to T-0h)</span>
        </el-divider>

        <p class="config-hint">Define the pricing multiplier for each hour preceding product expiration.</p>

        <div class="discount-matrix">
          <div v-for="h in 12" :key="h" class="matrix-item" :class="{'urgent-zone': h <= 3}">
            <div class="hour-label">{{ h }}h</div>
            <el-input-number
              v-model="newP.discount_rates[h-1]"
              :min="0.1" :max="1.0" :step="0.1" :precision="1"
              controls-position="right"
              size="small"
              class="matrix-input"
            />
          </div>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showAdd = false" round>Discard</el-button>
          <el-button type="success" @click="submitAdd" :loading="submitLoading" class="submit-btn" round>
            Commit to Database
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Setting, Search, TrendCharts } from '@element-plus/icons-vue'

const router = useRouter()
const products = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const showAdd = ref(false)

const newP = ref({
  barcode: '',
  product_name: '',
  normal_price: 0.00,
  discount_rates: [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/products')
    products.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    ElMessage.error('Infrastructure Link Failure: Cannot sync products')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  newP.value = {
    barcode: '',
    product_name: '',
    normal_price: 0.00,
    discount_rates: [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]
  }
  showAdd.value = true
}

const submitAdd = async () => {
  if (!newP.value.barcode || !newP.value.product_name) {
    return ElMessage.warning('Required fields missing: Barcode and Name')
  }

  submitLoading.value = true
  try {
    await request.post('/products', {
      barcode: newP.value.barcode,
      product_name: newP.value.product_name,
      normal_price: newP.value.normal_price,
      discount_rates: JSON.stringify(newP.value.discount_rates)
    })
    ElMessage.success('Product successfully integrated into master list')
    showAdd.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || 'Integration Error')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row: any) => {
  const id = row.barcode
  ElMessageBox.confirm(`Retire product [${row.product_name || id}] from system? This will stop all active monitoring for this SKU.`, 'Confirm Retirement', {
    confirmButtonClass: 'el-button--danger',
    confirmButtonText: 'Confirm Retirement',
    cancelButtonText: 'Cancel'
  }).then(async () => {
    try {
      await request.delete(`/products/${id}`)
      ElMessage.success('SKU Retired successfully')
      fetchData()
    } catch (e) {
      ElMessage.error('Retirement process failed')
    }
  }).catch(() => {})
}

const goDetail = (row: any) => {
  router.push({ name: 'AdminHome', query: { id: row.barcode } })
}

onMounted(fetchData)
</script>

<style scoped>
.inventory-container { padding: 5px; }

/* 品牌卡片樣式 */
.brand-card { border-radius: 12px; border: 1px solid #e2e8f0; }

.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 15px; }
.brand-strip-vertical { width: 5px; height: 35px; background: #007934; border-radius: 4px; }
.main-title { font-size: 18px; font-weight: 800; color: #1e293b; display: block; }
.sub-title { font-size: 11px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; }

.scan-btn {
  background-color: #007934 !important;
  border: none;
  font-weight: 700;
  padding: 10px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 121, 52, 0.2);
}

/* 表格視覺優化 */
.sku-table { margin-top: 10px; }
.barcode-display {
  font-family: 'Courier New', Courier, monospace;
  font-weight: 800;
  color: #475569;
  letter-spacing: 0.5px;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
}

.product-name-text { font-weight: 700; color: #1e293b; font-size: 15px; }

.price-cell { display: flex; align-items: center; gap: 3px; font-weight: 800; color: #e2231a; }
.currency { font-size: 12px; }
.amount { font-size: 18px; }

.action-link { font-weight: 700; display: inline-flex; align-items: center; gap: 4px; }
.action-link.delete { color: #94a3b8 !important; }
.action-link.delete:hover { color: #ef4444 !important; }

/* 彈窗與折扣矩陣 */
.dialog-brand-line {
  height: 4px;
  width: calc(100% + 40px);
  margin: -30px -20px 20px -20px;
  background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%);
}

.sku-form :deep(.el-form-item__label) {
  font-weight: 800;
  color: #475569;
  text-transform: uppercase;
  font-size: 11px;
}

.divider-label { display: flex; align-items: center; gap: 8px; color: #007934; font-weight: 800; }
.config-hint { font-size: 12px; color: #94a3b8; margin: -10px 0 20px 0; }

.discount-matrix {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  background: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.matrix-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: white;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.matrix-item.urgent-zone {
  border-color: #ff7900;
  background: #fff7ed;
}

.hour-label { font-size: 12px; font-weight: 900; color: #64748b; }
.urgent-zone .hour-label { color: #ff7900; }

.matrix-input :deep(.el-input__inner) { font-weight: 800; }

.submit-btn { background-color: #007934 !important; font-weight: 700; padding: 12px 30px; }
.mr-5 { margin-right: 5px; }
</style>
