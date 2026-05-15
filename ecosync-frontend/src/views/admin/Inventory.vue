<template>
  <div class="inventory-container">
    <el-card class="modern-card" shadow="never">
      <template #header>
        <div class="header-content">
          <div class="header-left">
            <div class="title-icon"><el-icon><Box /></el-icon></div>
            <div class="title-text-box">
              <span class="main-title">Standard Product Library</span>
              <span class="sub-title">Maintain master product data and default discount curves</span>
            </div>
          </div>
          <el-button type="success" class="brand-btn" @click="openAddDialog">
            <el-icon class="mr-5"><Plus /></el-icon> Register Product
          </el-button>
        </div>
      </template>

      <div class="table-toolbar">
        <el-input
          v-model="searchQuery"
          placeholder="Search product name or barcode..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
        <el-radio-group v-model="priceFilter" class="custom-radio">
          <el-radio-button label="ALL">All Items</el-radio-button>
          <el-radio-button label="UNDER_20">Under ¥20</el-radio-button>
          <el-radio-button label="OVER_20">Above ¥20</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredProducts" v-loading="loading" class="custom-table" stripe border height="600">
        <el-table-column label="Barcode" width="160" align="center">
          <template #default="{ row }">
            <span class="barcode-text">{{ row.barcode }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Product Preview" width="140" align="center">
          <template #default="{ row }">
            <el-image
              class="product-avatar"
              :src="row.image_url || row.imageUrl ? `/uploads/products/${row.image_url || row.imageUrl}` : `/uploads/products/${row.barcode}.jpg`"
              fit="cover"
            >
              <template #error><div class="image-slot-error"><el-icon><Box/></el-icon></div></template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column label="Product Name" min-width="200">
          <template #default="{ row }">
            <span class="product-name-txt">{{ row.product_name || row.productName || 'Unnamed Product' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Base Price" width="150" align="right">
          <template #default="{ row }">
            <span class="price-tag">¥{{ parseFloat(row.normal_price || row.normalPrice || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Management" width="240" align="center">
          <template #default="{ row }">
            <el-button class="action-btn-primary" size="small" round @click="goDetail(row)">
              <el-icon class="mr-5"><TrendCharts /></el-icon> Strategy
            </el-button>
            <el-button type="danger" plain size="small" round @click="handleDelete(row)">Delete</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="No products found matching criteria" />
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="Register New Master Product" width="850px" class="modern-dialog" destroy-on-close @closed="stopScanner">
      <div class="dialog-flex-layout">
        <div class="preview-panel">
          <div class="panel-title">DISCOUNT CURVE PREVIEW</div>
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
                <el-form-item label="Barcode (SKU)" prop="barcode">
                  <el-input v-model="newP.barcode" placeholder="Scan or enter barcode" class="modern-input">
                    <template #append><el-button @click="startScanner"><el-icon><Camera /></el-icon></el-button></template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item label="Standard Price">
                  <el-input-number v-model="newP.normal_price" :precision="2" class="modern-input-num" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="Product Title" prop="product_name">
              <el-input v-model="newP.product_name" class="modern-input" />
            </el-form-item>
            <el-divider content-position="left"><span style="color:#64748b; font-weight:700">Decay Strategy (12h)</span></el-divider>
            <div class="matrix-grid">
              <div v-for="h in 12" :key="h" class="discount-input-group">
                <span class="hour-label">{{ h }}h</span>
                <el-input-number v-model="newP.discount_rates[h-1]" :min="0.1" :max="1.0" :step="0.05" size="small" controls-position="right" style="width: 75px" />
              </div>
            </div>
          </el-form>
        </div>
      </div>

      <div v-if="scanning" class="scanner-overlay">
        <div id="scanner-reader"></div>
        <el-button type="danger" round @click="stopScanner" class="close-scanner">Cancel Scan</el-button>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="showAdd = false">Cancel</el-button>
          <el-button round type="success" class="brand-btn" @click="submitAdd" :loading="submitLoading">Confirm & Save</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessageBox, type FormInstance } from 'element-plus'
import { ElMessage } from '@/utils/message'
import { Plus, Camera, TrendCharts, Search, Box } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'

const router = useRouter()
const formRef = ref<FormInstance>()
const products = ref<any[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const showAdd = ref(false)
const imgFile = ref<File | null>(null)
const imgPreview = ref('')

const searchQuery = ref('')
const priceFilter = ref('ALL')

const newP = ref({
  barcode: '',
  product_name: '',
  normal_price: 0.00,
  discount_rates: Array(12).fill(1.0)
})

const rules = reactive({
  barcode: [{ required: true, message: 'Barcode required', trigger: 'blur' }],
  product_name: [{ required: true, message: 'Name required', trigger: 'blur' }]
})

const filteredProducts = computed(() => {
  let result = products.value

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(p =>
      (p.product_name || p.productName || '').toLowerCase().includes(q) ||
      (p.barcode || '').toLowerCase().includes(q)
    )
  }

  if (priceFilter.value === 'UNDER_20') {
    result = result.filter(p => parseFloat(p.normal_price || p.normalPrice || 0) < 20)
  } else if (priceFilter.value === 'OVER_20') {
    result = result.filter(p => parseFloat(p.normal_price || p.normalPrice || 0) >= 20)
  }

  return result
})

// 【核心修复】：恢复使用 name 匹配路由，解决空白页问题
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

// 【删除提示优化】：明确告知用户外键约束导致无法删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`Delete [${row.product_name || row.productName || row.barcode}]?`, 'Warning', { type: 'error' }).then(async () => {
    try {
      await request.delete(`/products/${row.barcode}`)
      ElMessage.success('Deleted')
      fetchData()
    } catch (e) {
      ElMessageBox.alert(
        `<strong>Cannot delete product!</strong><br><br>This product is currently linked to active inventory or historical orders.<br><br><span style="color:#64748b;font-size:12px;">* Database Foreign Key constraints prevent hard deletion to protect transaction history. Please archive or disable it instead.</span>`,
        'Database Protection Triggered',
        { type: 'error', dangerouslyUseHTMLString: true }
      )
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

const getBarColor = (rate: number) => rate > 0.8 ? '#008163' : (rate > 0.5 ? '#EE7203' : '#e2231a')
onMounted(fetchData)
onUnmounted(stopScanner)
</script>

<style scoped>
.inventory-container { min-height: 80vh; }
.modern-card { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 4px 20px rgba(0,0,0,0.03) !important; overflow: hidden; background: #fff; }
:deep(.modern-card .el-card__header) { padding: 20px 24px; border-bottom: 1px solid #f1f5f9; background: #fff; }
:deep(.modern-card .el-card__body) { padding: 0; }

.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 14px; }
.title-icon { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #008163, #005a46); color: white; display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: 0 4px 12px rgba(0,129,99,0.2); }
.title-text-box { display: flex; flex-direction: column; }
.main-title { font-size: 18px; font-weight: 800; color: #1e293b; }
.sub-title { font-size: 12px; color: #94a3b8; margin-top: 2px; }

.brand-btn { background: #008163 !important; border: none; font-weight: 700; border-radius: 10px; height: 40px; padding: 0 20px; box-shadow: 0 4px 12px rgba(0,129,99,0.2); }

/* 工具栏 */
.table-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; background: #fdfdfd; border-bottom: 1px solid #f1f5f9; }
.search-input { width: 340px; }
.search-input :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; padding: 6px 12px; }
.search-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(0,129,99,0.4) inset; }

/* 表格全局美化 */
.custom-table { width: 100%; border-radius: 0 0 16px 16px; }
:deep(.el-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569; font-weight: 700; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; }
.barcode-text { font-family: monospace; font-weight: 800; color: #64748b; font-size: 14px; letter-spacing: 1px; }
.product-avatar { width: 44px; height: 44px; border-radius: 8px; border: 1px solid #e2e8f0; background: #f8fafc; display: flex; align-items: center; justify-content: center; }
.image-slot-error { font-size: 18px; color: #cbd5e1; }
.product-name-txt { font-weight: 700; color: #1e293b; font-size: 14px; }
.price-tag { color: #EE7203; font-weight: 900; font-size: 16px; }

.action-btn-primary { background: #f0fdf4 !important; color: #008163 !important; border-color: #bbf7d0 !important; font-weight: 700; }
.action-btn-primary:hover { background: #dcfce7 !important; border-color: #86efac !important; }

/* 弹窗美化 */
:deep(.modern-dialog) { border-radius: 20px; overflow: hidden; }
:deep(.modern-dialog .el-dialog__header) { padding: 24px; border-bottom: 1px solid #f1f5f9; margin-right: 0; }
:deep(.modern-dialog .el-dialog__title) { font-weight: 800; color: #1e293b; font-size: 18px; }

.dialog-flex-layout { display: flex; gap: 30px; align-items: flex-start; margin-top: 10px; }
.preview-panel { flex: 1; background: #0f172a; border-radius: 16px; padding: 24px; color: #fff; box-shadow: inset 0 2px 10px rgba(0,0,0,0.5); }
.panel-title { font-size: 11px; font-weight: 800; color: #94a3b8; letter-spacing: 1px; text-transform: uppercase; margin-bottom: 15px; }
.chart-container { height: 180px; position: relative; padding-left: 40px; margin-top: 15px; }
.visual-graph { height: 100%; display: flex; align-items: flex-end; gap: 4px; border-left: 2px solid #334155; border-bottom: 2px solid #334155; }
.graph-bar { flex: 1; border-radius: 4px 4px 0 0; transition: height 0.3s, background-color 0.3s; opacity: 0.9; }
.y-axis { position: absolute; left: 0; height: 100%; display: flex; flex-direction: column; justify-content: space-between; font-size: 10px; color: #64748b; font-weight: bold; }
.x-axis { display: flex; justify-content: space-between; margin-top: 12px; font-size: 10px; color: #64748b; font-weight: bold; }
.price-stats { display: flex; gap: 20px; margin-top: 24px; padding-top: 16px; border-top: 1px dashed #334155; }
.stat-item { display: flex; flex-direction: column; }
.stat-item .label { font-size: 11px; color: #94a3b8; font-weight: bold; }
.stat-item .value { font-size: 18px; font-weight: 900; color: #f8fafc; }
.stat-item.highlight .value { color: #EE7203; font-size: 24px; }

.form-panel { flex: 1.2; }
:deep(.el-form-item__label) { font-weight: 800; color: #475569; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; padding-bottom: 4px !important; }
.modern-input :deep(.el-input__wrapper), .modern-input-num :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; }
.product-uploader { border: 2px dashed #cbd5e1; border-radius: 12px; width: 100px; height: 100px; display: flex; justify-content: center; align-items: center; cursor: pointer; transition: all 0.2s; background: #f8fafc; }
.product-uploader:hover { border-color: #008163; background: #f0fdf4; }
.preview-img { width: 100%; height: 100%; object-fit: cover; border-radius: 10px; }
.uploader-icon { font-size: 24px; color: #94a3b8; }

.matrix-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.discount-input-group { display: flex; align-items: center; justify-content: space-between; background: #f8fafc; padding: 6px 10px; border-radius: 8px; border: 1px solid #e2e8f0; }
.hour-label { font-size: 12px; font-weight: 800; color: #475569; }

.scanner-overlay { position: absolute; inset: 0; background: rgba(15,23,42,0.9); z-index: 100; display: flex; flex-direction: column; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
#scanner-reader { width: 320px; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5); border: 4px solid #fff; }
.close-scanner { margin-top: 24px; font-weight: 800; padding: 0 30px; height: 44px; }
.mr-5 { margin-right: 5px; }
</style>
