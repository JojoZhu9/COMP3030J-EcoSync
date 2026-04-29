<template>
  <div class="inventory-container">
    <el-card class="brand-card" shadow="never">
      <template #header>
        <div class="header-content">
          <div class="header-left">
            <div class="brand-strip"></div>
            <span class="main-title">Standard Product Maintenance (standard_products)</span>
          </div>
          <el-button type="success" @click="openAddDialog">
            <el-icon class="mr-5"><Plus /></el-icon> Scan / Register Product
          </el-button>
        </div>
      </template>

      <el-table :data="products" v-loading="loading" stripe border>
        <el-table-column prop="barcode" label="Barcode" width="150" />
        <el-table-column label="Product Name">
          <template #default="{ row }">
            {{ row.product_name || row.productName || 'Unnamed' }}
          </template>
        </el-table-column>
        <el-table-column label="Base Price">
          <template #default="{ row }">
            <span class="price-tag">¥{{ parseFloat(row.normal_price || row.normalPrice || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="Manage" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row)">Strategy</el-button>
            <el-button type="danger" link @click="handleDelete(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="Product Registration & Strategy Preview" width="850px" destroy-on-close>
      <div class="dialog-flex-layout">

        <div class="preview-panel">
          <div class="panel-title">PRICE DECAY PREVIEW</div>
          <div class="chart-container">
            <div class="y-axis">
              <span>100%</span><span>50%</span><span>0%</span>
            </div>
            <div class="visual-graph">
              <div v-for="(rate, index) in newP.discount_rates" :key="index"
                   class="graph-bar"
                   :style="{
                     height: (rate * 100) + '%',
                     backgroundColor: getBarColor(rate)
                   }">
                <div class="bar-tooltip">{{ (rate * 100).toFixed(0) }}% Price</div>
              </div>
            </div>
            <div class="x-axis">
              <span>12h</span><span>9h</span><span>6h</span><span>3h</span><span>Exp</span>
            </div>
          </div>
          <div class="price-stats">
            <div class="stat-item">
              <span class="label">Base Price:</span>
              <span class="value">¥{{ newP.normal_price }}</span>
            </div>
            <div class="stat-item highlight">
              <span class="label">Final Price:</span>
              <span class="value">¥{{ (newP.normal_price * newP.discount_rates[0]).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="form-panel">
          <el-form :model="newP" label-position="top">
            <el-row :gutter="20">
              <el-col :span="14">
                <el-form-item label="Barcode">
                  <el-input v-model="newP.barcode" placeholder="Scan or enter barcode" />
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="Base Unit Price">
                  <el-input-number v-model="newP.normal_price" :precision="2" :step="0.5" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="Product Name">
              <el-input v-model="newP.product_name" placeholder="Enter product name" />
            </el-form-item>

            <el-divider content-position="left">Expiry Discount Settings (1-12 Hours)</el-divider>

            <div class="matrix-grid">
              <div v-for="h in 12" :key="h" class="discount-input-group">
                <span class="hour-label">{{ h }}h</span>
                <el-input-number
                  v-model="newP.discount_rates[h-1]"
                  :min="0.1" :max="1.0" :step="0.1" :precision="1"
                  controls-position="right"
                  size="small"
                  style="width: 70px"
                />
              </div>
            </div>
          </el-form>
        </div>
      </div>

      <template #footer>
        <el-button @click="showAdd = false">Cancel</el-button>
        <el-button type="primary" @click="submitAdd" :loading="submitLoading">Confirm Entry</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

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

const getBarColor = (rate: number) => {
  if (rate > 0.8) return '#409EFF'
  if (rate > 0.5) return '#E6A23C'
  return '#F56C6C'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/products')
    products.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    ElMessage.error('Unable to connect to server')
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
    return ElMessage.warning('Please fill in all information')
  }
  submitLoading.value = true
  try {
    await request.post('/products', {
      barcode: newP.value.barcode,
      product_name: newP.value.product_name,
      normal_price: newP.value.normal_price,
      discount_rates: JSON.stringify(newP.value.discount_rates)
    })
    ElMessage.success('Product registered successfully')
    showAdd.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error('Registration failed')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row: any) => {
  const id = row.barcode
  ElMessageBox.confirm(`Are you sure you want to delete [${row.product_name}]?`, 'Warning', { type: 'warning' }).then(async () => {
    await request.delete(`/products/${id}`)
    fetchData()
  })
}

const goDetail = (row: any) => {
  router.push({ name: 'AdminHome', query: { id: row.barcode } })
}

onMounted(fetchData)
</script>

<style scoped>
.inventory-container { padding: 25px; background: #f0f2f5; min-height: 100vh; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.brand-strip { width: 4px; height: 18px; background: #409eff; margin-right: 10px; display: inline-block; vertical-align: middle; }
.main-title { font-weight: bold; font-size: 16px; }

/* Layout: Chart Left, Form Right */
.dialog-flex-layout { display: flex; gap: 25px; align-items: flex-start; }

/* Preview Panel Styles */
.preview-panel {
  flex: 1; background: #1a1c1e; border-radius: 8px; padding: 20px; color: #fff;
  box-shadow: inset 0 0 20px rgba(0,0,0,0.5);
}
.panel-title { font-size: 12px; color: #409eff; letter-spacing: 1px; margin-bottom: 20px; font-weight: bold; }

.chart-container { height: 200px; display: flex; flex-direction: column; position: relative; padding-left: 30px; }
.visual-graph { flex: 1; display: flex; align-items: flex-end; gap: 4px; border-left: 1px solid #333; border-bottom: 1px solid #333; padding: 0 5px; }
.graph-bar { flex: 1; transition: all 0.4s ease; position: relative; border-radius: 2px 2px 0 0; }
.graph-bar:hover .bar-tooltip { opacity: 1; }
.bar-tooltip {
  position: absolute; top: -25px; left: 50%; transform: translateX(-50%);
  background: #fff; color: #000; font-size: 10px; padding: 2px 4px; border-radius: 3px;
  opacity: 0; pointer-events: none; white-space: nowrap;
}

.y-axis { position: absolute; left: 0; height: 100%; display: flex; flex-direction: column; justify-content: space-between; font-size: 10px; color: #666; }
.x-axis { display: flex; justify-content: space-between; margin-top: 10px; font-size: 10px; color: #666; padding: 0 10px; }

.price-stats { margin-top: 20px; border-top: 1px solid #333; padding-top: 15px; }
.stat-item { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; }
.highlight { color: #409eff; font-weight: bold; font-size: 15px; }

/* Matrix Grid */
.form-panel { flex: 1.3; }
.matrix-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.discount-input-group {
  display: flex; align-items: center; justify-content: space-between;
  background: #f5f7fa; padding: 5px 8px; border-radius: 4px; border: 1px solid #e4e7ed;
}
.hour-label { font-size: 12px; color: #606266; font-weight: bold; }

:deep(.el-divider__text) { font-weight: bold; color: #409eff; }
.price-tag { color: #f56c6c; font-weight: bold; }
</style>
