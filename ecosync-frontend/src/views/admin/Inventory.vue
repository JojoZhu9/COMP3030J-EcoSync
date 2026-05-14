<template>
  <div class="inventory-container">
    <el-card class="brand-card" shadow="never">
      <template #header>
        <div class="header-content">
          <div class="header-left">
            <div class="brand-strip"></div>
            <span class="main-title">Standard Product Maintenance</span>
          </div>
          <el-button type="success" @click="openAddDialog">
            <el-icon class="mr-5"><Plus /></el-icon> Scan / Register New Product
          </el-button>
        </div>
      </template>

      <el-table :data="products" v-loading="loading" stripe border>
        <el-table-column prop="barcode" label="Barcode" width="150" />
        <el-table-column label="Image" width="100">
          <template #default="{ row }">
            <el-image
              style="width: 50px; height: 50px; border-radius: 4px; background: #f5f7fa"
              :src="row.image_url || row.imageUrl ? `/uploads/products/${row.image_url || row.imageUrl}` : `/uploads/products/${row.barcode}.jpg`"
              fit="cover"
            >
              <template #error><div class="image-slot-error">No Pic</div></template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="Product Name">
          <template #default="{ row }">
            {{ row.product_name || row.productName || 'Unnamed Product' }}
          </template>
        </el-table-column>
        <el-table-column label="Base Price">
          <template #default="{ row }">
            <span class="price-tag">¥{{ parseFloat(row.normal_price || row.normalPrice || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="Manage" width="220" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="goDetail(row)">
              <el-icon class="mr-5"><TrendCharts /></el-icon> Strategy
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="Register New Product" width="850px" destroy-on-close @closed="stopScanner">
      <div class="dialog-flex-layout">
        <div class="preview-panel">
          <div class="panel-title">NEW ENTRY PREVIEW</div>
          <div class="chart-container">
            <div class="y-axis"><span>100%</span><span>50%</span><span>0%</span></div>
            <div class="visual-graph">
              <div v-for="(rate, index) in newP.discount_rates" :key="index"
                   class="graph-bar"
                   :style="{ height: (rate * 100) + '%', backgroundColor: getBarColor(rate) }">
              </div>
            </div>
            <div class="x-axis"><span>12h</span><span>6h</span><span>Exp</span></div>
          </div>
          <div class="price-stats">
            <div class="stat-item"><span class="label">Base:</span><span class="value">¥{{ newP.normal_price }}</span></div>
            <div class="stat-item highlight"><span class="label">Final:</span><span class="value">¥{{ (newP.normal_price * (newP.discount_rates[0] || 1)).toFixed(2) }}</span></div>
          </div>
        </div>

        <div class="form-panel">
          <el-form ref="formRef" :model="newP" :rules="rules" label-position="top">
            <el-form-item label="Product Image">
              <el-upload class="product-uploader" action="#" :auto-upload="false" :show-file-list="false" :on-change="handleFileChange">
                <img v-if="imgPreview" :src="imgPreview" class="preview-img" />
                <el-icon v-else class="uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="14">
                <el-form-item label="Barcode" prop="barcode">
                  <el-input v-model="newP.barcode" placeholder="Scan barcode">
                    <template #append><el-button @click="startScanner"><el-icon><Camera /></el-icon></el-button></template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="Base Price"><el-input-number v-model="newP.normal_price" :precision="2" style="width:100%" /></el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="Product Name" prop="product_name">
              <el-input v-model="newP.product_name" />
            </el-form-item>
            <el-divider content-position="left">Default Strategy (12h)</el-divider>
            <div class="matrix-grid">
              <div v-for="h in 12" :key="h" class="discount-input-group">
                <span class="hour-label">{{ h }}h</span>
                <el-input-number v-model="newP.discount_rates[h-1]" :min="0.1" :max="1.0" :step="0.1" size="small" controls-position="right" style="width: 70px" />
              </div>
            </div>
          </el-form>
        </div>
      </div>

      <div v-if="scanning" class="scanner-overlay">
        <div id="scanner-reader"></div>
        <el-button type="danger" @click="stopScanner" class="close-scanner">Cancel Scan</el-button>
      </div>

      <template #footer>
        <el-button @click="showAdd = false">Cancel</el-button>
        <el-button type="primary" @click="submitAdd" :loading="submitLoading">Confirm & Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Camera, TrendCharts } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'

const router = useRouter()
const formRef = ref<FormInstance>()
const products = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const showAdd = ref(false)
const imgFile = ref<File | null>(null)
const imgPreview = ref('')

const newP = ref({
  barcode: '',
  product_name: '',
  normal_price: 0.00,
  discount_rates: [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]
})

const rules = reactive({
  barcode: [{ required: true, message: 'Barcode required', trigger: 'blur' }],
  product_name: [{ required: true, message: 'Name required', trigger: 'blur' }]
})

// 跳转至策略详情页
const goDetail = (row: any) => {
  router.push({ name: 'AdminHome', query: { id: row.barcode } })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/products')
    products.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) { ElMessage.error('Server connection failed') }
  finally { loading.value = false }
}

const openAddDialog = () => {
  newP.value = { barcode: '', product_name: '', normal_price: 0.00, discount_rates: Array(12).fill(1.0) }
  imgPreview.value = ''; imgFile.value = null; showAdd.value = true
}

const handleFileChange = (file: any) => {
  imgFile.value = file.raw
  imgPreview.value = URL.createObjectURL(file.raw)
}

const submitAdd = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    const fd = new FormData()
    if (imgFile.value) fd.append('image', imgFile.value)
    fd.append('barcode', newP.value.barcode)
    fd.append('product_name', newP.value.product_name)
    fd.append('normal_price', newP.value.normal_price.toString())
    fd.append('discount_rates', JSON.stringify(newP.value.discount_rates))
    try {
      await request.post('/products', fd)
      ElMessage.success('Product registered!')
      showAdd.value = false
      fetchData()
    } catch (e) { ElMessage.error('Registration failed') }
    finally { submitLoading.value = false }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`Delete [${row.product_name || row.barcode}]?`, 'Warning', { type: 'warning' }).then(async () => {
    try {
      await request.delete(`/products/${row.barcode}`)
      ElMessage.success('Deleted')
      fetchData()
    } catch (e) {
      ElMessageBox.alert('Cannot delete: This product is linked to active orders.', 'Error', { type: 'error' })
    }
  }).catch(() => {})
}

// 扫描器逻辑
const scanning = ref(false)
let html5QrCode: Html5Qrcode | null = null
const startScanner = async () => {
  scanning.value = true
  setTimeout(async () => {
    try {
      html5QrCode = new Html5Qrcode("scanner-reader")
      await html5QrCode.start({ facingMode: "environment" }, { fps: 10, qrbox: 250 }, (text) => {
        newP.value.barcode = text; stopScanner(); ElMessage.success('Scanned!')
      }, () => {})
    } catch (e) { scanning.value = false }
  }, 100)
}
const stopScanner = async () => {
  if (html5QrCode?.isScanning) { await html5QrCode.stop(); await html5QrCode.clear() }
  scanning.value = false; html5QrCode = null
}

const getBarColor = (rate: number) => rate > 0.8 ? '#409EFF' : (rate > 0.5 ? '#E6A23C' : '#F56C6C')
onMounted(fetchData)
onUnmounted(stopScanner)
</script>

<style scoped>
/* 保持你现有的库存页样式，主要包含 .inventory-container, .preview-panel, .visual-graph 等 */
.inventory-container { padding: 25px; background: #f0f2f5; min-height: 100vh; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.brand-strip { width: 4px; height: 18px; background: #007934; margin-right: 10px; display: inline-block; vertical-align: middle; }
.main-title { font-weight: bold; font-size: 16px; color: #1a202c; }
.dialog-flex-layout { display: flex; gap: 25px; align-items: flex-start; }
.preview-panel { flex: 1; background: #1a1c1e; border-radius: 8px; padding: 20px; color: #fff; }
.chart-container { height: 180px; position: relative; padding-left: 35px; margin-top: 15px; }
.visual-graph { height: 100%; display: flex; align-items: flex-end; gap: 4px; border-left: 1px solid #333; border-bottom: 1px solid #333; }
.graph-bar { flex: 1; border-radius: 2px 2px 0 0; transition: height 0.3s; }
.y-axis { position: absolute; left: 0; height: 100%; display: flex; flex-direction: column; justify-content: space-between; font-size: 10px; color: #666; }
.x-axis { display: flex; justify-content: space-between; margin-top: 10px; font-size: 10px; color: #666; }
.price-tag { color: #e2231a; font-weight: bold; }
.product-uploader { border: 1px dashed #d9d9d9; border-radius: 6px; width: 100px; height: 100px; display: flex; justify-content: center; align-items: center; cursor: pointer; }
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.matrix-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.discount-input-group { display: flex; align-items: center; justify-content: space-between; background: #f8fafc; padding: 5px; border-radius: 4px; border: 1px solid #e2e8f0; }
.hour-label { font-size: 12px; font-weight: bold; color: #64748b; }
.scanner-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.8); z-index: 100; display: flex; flex-direction: column; align-items: center; justify-content: center; }
#scanner-reader { width: 300px; border-radius: 8px; overflow: hidden; }
.image-slot-error { font-size: 10px; color: #999; }
.mr-5 { margin-right: 5px; }
</style>
