<template>
  <div class="inventory-container">
    <el-card class="modern-card" shadow="never">
      <template #header>
        <div class="header-content">
          <div class="header-left">
            <div class="title-icon"><el-icon><Box /></el-icon></div>
            <div class="title-text-box">
              <span class="main-title">{{ $t('admin.inventory.standardProductLibrary') }}</span>
              <span class="sub-title">{{ $t('admin.inventory.maintainMasterData') }}</span>
            </div>
          </div>
          <el-button type="success" class="brand-btn" @click="openAddDialog">
            <el-icon class="mr-5"><Plus /></el-icon> {{ $t('admin.inventory.registerProduct') }}
          </el-button>
        </div>
      </template>

      <div class="table-toolbar">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('admin.inventory.searchPlaceholder')"
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
        <div class="toolbar-right">
          <el-button v-if="selectedProducts.length" type="danger" plain round size="small" @click="handleBatchDelete"
          >
            {{ $t('admin.inventory.deleteSelected', { count: selectedProducts.length }) }}
          </el-button>
          <el-radio-group v-model="priceFilter" class="custom-radio">
            <el-radio-button label="ALL">{{ $t('admin.inventory.allItems') }}</el-radio-button>
            <el-radio-button label="UNDER_20">{{ $t('admin.inventory.under20') }}</el-radio-button>
            <el-radio-button label="OVER_20">{{ $t('admin.inventory.above20') }}</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <el-table :data="filteredProducts" v-loading="loading" class="custom-table" stripe border height="600" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />

        <el-table-column :label="$t('admin.inventory.barcode')" width="160" align="center">
          <template #default="{ row }">
            <span class="barcode-text">{{ row.barcode }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('admin.inventory.productPreview')" width="140" align="center">
          <template #default="{ row }">
            <el-image
              class="product-avatar cursor-pointer"
              :src="row.image_url || row.imageUrl ? `/uploads/products/${row.image_url || row.imageUrl}` : `/uploads/products/${row.barcode}.jpg`"
              fit="cover"
              :preview-src-list="[row.image_url || row.imageUrl ? `/uploads/products/${row.image_url || row.imageUrl}` : `/uploads/products/${row.barcode}.jpg`]"
              :initial-index="0"
              preview-teleported
            >
              <template #error><div class="image-slot-error"><el-icon><Box /></el-icon></div></template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column :label="$t('admin.inventory.productName')" min-width="200">
          <template #default="{ row }">
            <span class="product-name-txt">{{ row.product_name || row.productName || $t('admin.inventory.unnamedProduct') }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('admin.inventory.basePrice')" width="150" align="right">
          <template #default="{ row }">
            <span class="price-tag">¥{{ parseFloat(row.normal_price || row.normalPrice || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="$t('admin.inventory.management')" width="240" align="center">
          <template #default="{ row }">
            <el-button class="action-btn-primary" size="small" round @click="goDetail(row)">
              <el-icon class="mr-5"><TrendCharts /></el-icon> {{ $t('admin.inventory.strategy') }}
            </el-button>
            <el-button type="danger" plain size="small" round @click="handleDelete(row)">{{ $t('admin.inventory.delete') }}</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty :description="$t('admin.inventory.noProductsFound')" />
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" :title="$t('admin.inventory.registerNewMasterProduct')" width="850px" class="modern-dialog" destroy-on-close @closed="stopScanner">
      <div class="dialog-flex-layout">
        <div class="preview-panel">
          <div class="panel-title">{{ $t('admin.inventory.discountCurvePreview') }}</div>
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
            <div class="stat-item"><span class="label">{{ $t('admin.inventory.base') }}</span><span class="value">¥{{ newP.normal_price }}</span></div>
            <div class="stat-item highlight"><span class="label">{{ $t('admin.inventory.final') }}</span><span class="value">¥{{ (newP.normal_price * (newP.discount_rates[0] || 1)).toFixed(2) }}</span></div>
          </div>
        </div>

        <div class="form-panel">
          <el-form ref="formRef" :model="newP" :rules="rules" label-position="top">
            <el-form-item :label="$t('admin.inventory.productImage')">
              <div class="image-preview-wrap">
                <el-upload v-if="!imgPreview" class="product-uploader" action="#" :auto-upload="false" :show-file-list="false" :on-change="handleFileChange"
                >
                  <el-icon class="uploader-icon"><Plus /></el-icon>
                </el-upload>
                <div v-else class="preview-with-delete"
                >
                  <img :src="imgPreview" class="preview-img" />
                  <el-button class="delete-img-btn" circle size="small" type="danger" @click="removeImage"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="14">
                <el-form-item :label="$t('admin.inventory.barcodeSku')" prop="barcode">
                  <el-input v-model="newP.barcode" :placeholder="$t('admin.inventory.scanOrEnter')" class="modern-input">
                    <template #append><el-button @click="startScanner"><el-icon><Camera /></el-icon></el-button></template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="10">
                <el-form-item :label="$t('admin.inventory.standardPrice')" prop="normal_price">
                  <el-input-number v-model="newP.normal_price" :precision="2" :min="0.01" class="modern-input-num" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item :label="$t('admin.inventory.productTitle') + ' (中文)'" prop="product_name">
              <el-input v-model="newP.product_name" placeholder="输入中文商品名称" class="modern-input" />
            </el-form-item>
            <el-form-item label="Product Title (English)">
              <el-input v-model="newP.product_name_en" placeholder="Enter English product name" class="modern-input" />
            </el-form-item>
            <el-divider content-position="left"><span style="color:#64748b; font-weight:700">{{ $t('admin.inventory.decayStrategy') }}</span></el-divider>
            <div class="matrix-grid">
              <div v-for="h in 12" :key="h" class="discount-input-group">
                <span class="hour-label">{{ h }}h</span>
                <el-input-number v-model="newP.discount_rates[h-1]" :min="0.1" :max="1.0" :step="0.05" size="small" controls-position="right" style="width: 75px" @blur="validateSingleDecay(h-1)" />
              </div>
            </div>
          </el-form>
        </div>
      </div>

      <div v-if="scanning" class="scanner-overlay">
        <div id="scanner-reader"></div>
        <el-button type="danger" round @click="stopScanner" class="close-scanner">{{ $t('admin.inventory.cancelScan') }}</el-button>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button round @click="showAdd = false">{{ $t('admin.inventory.cancel') }}</el-button>
          <el-button round type="success" class="brand-btn" @click="submitAdd" :loading="submitLoading">{{ $t('admin.inventory.confirmSave') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import { ElMessageBox, type FormInstance } from 'element-plus'
import { ElMessage } from '@/utils/message'
import { Plus, Camera, TrendCharts, Search, Box, Delete } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'

const { t } = useI18n()
const router = useRouter()
const formRef = ref<FormInstance>()
const products = ref<any[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const showAdd = ref(false)
const imgFile = ref<File | null>(null)
const imgPreview = ref('')
const selectedProducts = ref<any[]>([])

const searchQuery = ref('')
const priceFilter = ref('ALL')

const newP = ref({
  barcode: '',
  product_name: '',
  product_name_en: '',
  normal_price: 0.00,
  discount_rates: Array(12).fill(1.0)
})

const validateBarcode = (_rule: any, value: string, callback: any) => {
  if (!value) return callback(new Error(t('admin.inventory.barcodeRequired')))
  if (!/^\d{13}$/.test(value)) return callback(new Error(t('admin.inventory.barcode13Digits')))
  callback()
}

const validatePrice = (_rule: any, value: number, callback: any) => {
  if (value == null || value === undefined) return callback(new Error(t('admin.inventory.priceRequired')))
  if (value <= 0) return callback(new Error(t('admin.inventory.priceGreaterThan0')))
  callback()
}

const validateDecay = (_rule: any, _value: any, callback: any) => {
  for (const rate of newP.value.discount_rates) {
    if (rate < 0.1 || rate > 1.0) {
      return callback(new Error(t('admin.inventory.decayRateRange')))
    }
  }
  callback()
}

const rules = reactive({
  barcode: [{ required: true, validator: validateBarcode, trigger: 'blur' }],
  product_name: [{ required: true, message: t('admin.inventory.nameRequired'), trigger: 'blur' }],
  normal_price: [{ required: true, validator: validatePrice, trigger: 'change' }],
  discount_rates: [{ validator: validateDecay, trigger: 'change' }]
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

const goDetail = (row: any) => {
  router.push({ name: 'AdminHome', query: { id: row.barcode } })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/products')
    products.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) { ElMessage.error(t('admin.inventory.serverConnectionFailed')) }
  finally { loading.value = false }
}

const openAddDialog = () => {
  newP.value = { barcode: '', product_name: '', product_name_en: '', normal_price: 0.00, discount_rates: Array(12).fill(1.0) }
  imgPreview.value = ''; imgFile.value = null; showAdd.value = true
}

const handleFileChange = (file: any) => {
  imgFile.value = file.raw
  imgPreview.value = URL.createObjectURL(file.raw)
}

const removeImage = () => {
  imgFile.value = null
  imgPreview.value = ''
}

const validateSingleDecay = (index: number) => {
  const v = newP.value.discount_rates[index]
  if (v < 0.1) newP.value.discount_rates[index] = 0.1
  if (v > 1.0) newP.value.discount_rates[index] = 1.0
}

const processImage = async (file: File, barcode: string): Promise<File> => {
  return new Promise((resolve) => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      URL.revokeObjectURL(url)
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')!
      let { width, height } = img
      const maxDim = 1200
      if (width > maxDim || height > maxDim) {
        if (width > height) {
          height = Math.round(height * maxDim / width)
          width = maxDim
        } else {
          width = Math.round(width * maxDim / height)
          height = maxDim
        }
      }
      canvas.width = width
      canvas.height = height
      ctx.fillStyle = '#fff'
      ctx.fillRect(0, 0, width, height)
      ctx.drawImage(img, 0, 0, width, height)

      const toBlob = (quality: number): Promise<Blob | null> => new Promise((res) => canvas.toBlob((b) => res(b), 'image/jpeg', quality))

      const tryCompress = async () => {
        let quality = 0.92
        let blob = await toBlob(quality)
        while (blob && blob.size > 1024 * 1024 && quality > 0.3) {
          quality -= 0.1
          blob = await toBlob(quality)
        }
        if (!blob) blob = await toBlob(0.8)
        const newFile = new File([blob!], `${barcode}.jpg`, { type: 'image/jpeg' })
        resolve(newFile)
      }
      tryCompress()
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      resolve(file)
    }
    img.src = url
  })
}

const submitAdd = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const fd = new FormData()
      if (imgFile.value) {
        const processed = await processImage(imgFile.value, newP.value.barcode)
        fd.append('image', processed)
      }
      fd.append('barcode', newP.value.barcode)
      fd.append('product_name', newP.value.product_name)
      if (newP.value.product_name_en) {
        fd.append('product_name_en', newP.value.product_name_en)
      }
      fd.append('normal_price', newP.value.normal_price.toString())
      fd.append('discount_rates', JSON.stringify(newP.value.discount_rates))
      await request.post('/products', fd)
      ElMessage.success(t('admin.inventory.productRegistered'))
      showAdd.value = false
      fetchData()
    } catch (e) { ElMessage.error(t('admin.inventory.registrationFailed')) }
    finally { submitLoading.value = false }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(t('admin.inventory.deleteConfirm', { name: row.product_name || row.productName || row.barcode }), t('admin.inventory.warning'), { type: 'error' }).then(async () => {
    try {
      await request.delete(`/products/${row.barcode}`)
      ElMessage.success(t('admin.inventory.deleted'))
      fetchData()
    } catch (e: any) {
      const msg = e?.response?.data?.message || t('admin.inventory.deleteFailed')
      ElMessage.error(msg)
    }
  }).catch(() => {})
}

const scanning = ref(false)
let html5QrCode: Html5Qrcode | null = null
const startScanner = async () => {
  scanning.value = true
  setTimeout(async () => {
    try {
      html5QrCode = new Html5Qrcode("scanner-reader")
      await html5QrCode.start({ facingMode: "environment" }, { fps: 10, qrbox: 250 }, (text) => {
        newP.value.barcode = text; stopScanner(); ElMessage.success(t('admin.inventory.scanned'))
      }, () => {})
    } catch (e) { scanning.value = false }
  }, 100)
}
const stopScanner = async () => {
  if (html5QrCode?.isScanning) { await html5QrCode.stop(); await html5QrCode.clear() }
  scanning.value = false; html5QrCode = null
}

const getBarColor = (rate: number) => rate > 0.8 ? '#008163' : (rate > 0.5 ? '#EE7203' : '#e2231a')

const handleSelectionChange = (rows: any[]) => {
  selectedProducts.value = rows
}

const handleBatchDelete = async () => {
  if (!selectedProducts.value.length) return
  const count = selectedProducts.value.length
  try {
    await ElMessageBox.confirm(
      t('admin.inventory.batchDeleteConfirm', { count, plural: count > 1 ? 's' : '' }),
      t('admin.inventory.batchDelete'),
      { type: 'error' }
    )
    let success = 0
    let failed = 0
    for (const row of selectedProducts.value) {
      try {
        await request.delete(`/products/${row.barcode}`)
        success++
      } catch (e) {
        failed++
      }
    }
    if (success) ElMessage.success(t('admin.inventory.batchDeleteSuccess', { count: success, plural: success > 1 ? 's' : '' }))
    if (failed) ElMessage.warning(t('admin.inventory.batchDeleteFailed', { count: failed }))
    fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(t('admin.inventory.batchDeleteError'))
  }
}

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
.toolbar-right { display: flex; align-items: center; gap: 12px; }
.search-input { width: 340px; }
.search-input :deep(.el-input__wrapper) { background: #f8fafc; border-radius: 10px; box-shadow: 0 0 0 1px #e2e8f0 inset; padding: 6px 12px; }
.search-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(0,129,99,0.4) inset; }

/* 表格全局美化 */
.custom-table { width: 100%; border-radius: 0 0 16px 16px; }
:deep(.el-table th.el-table__cell) { background-color: #f8fafc !important; color: #475569; font-weight: 700; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; }
.barcode-text { font-family: monospace; font-weight: 800; color: #64748b; font-size: 14px; letter-spacing: 1px; }
.product-avatar { width: 44px; height: 44px; border-radius: 8px; border: 1px solid #e2e8f0; background: #f8fafc; display: flex; align-items: center; justify-content: center; }
.cursor-pointer { cursor: pointer; }
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
.image-preview-wrap { display: flex; align-items: center; }
.product-uploader { border: 2px dashed #cbd5e1; border-radius: 12px; width: 100px; height: 100px; display: flex; justify-content: center; align-items: center; cursor: pointer; transition: all 0.2s; background: #f8fafc; }
.product-uploader:hover { border-color: #008163; background: #f0fdf4; }
.preview-with-delete { position: relative; width: 100px; height: 100px; }
.preview-img { width: 100%; height: 100%; object-fit: cover; border-radius: 10px; }
.delete-img-btn { position: absolute; top: -8px; right: -8px; z-index: 2; }
.uploader-icon { font-size: 24px; color: #94a3b8; }

.matrix-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.discount-input-group { display: flex; align-items: center; justify-content: space-between; background: #f8fafc; padding: 6px 10px; border-radius: 8px; border: 1px solid #e2e8f0; }
.hour-label { font-size: 12px; font-weight: 800; color: #475569; }

.scanner-overlay { position: absolute; inset: 0; background: rgba(15,23,42,0.9); z-index: 100; display: flex; flex-direction: column; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
#scanner-reader { width: 320px; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5); border: 4px solid #fff; }
.close-scanner { margin-top: 24px; font-weight: 800; padding: 0 30px; height: 44px; }
.mr-5 { margin-right: 5px; }
</style>
