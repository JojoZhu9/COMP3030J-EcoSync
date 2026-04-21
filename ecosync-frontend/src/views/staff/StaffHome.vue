<template>
  <div class="staff-container">
    <div class="side-nav">
      <div class="logo-box">
        <img src="https://upload.wikimedia.org/wikipedia/commons/4/40/7-eleven_logo.svg" class="logo-img" alt="7-11">
        <div class="brand-title">7-11 员工管理系统</div>
      </div>

      <el-menu :default-active="activeTab" class="el-menu-vertical" @select="handleMenuSelect">
        <el-menu-item index="stock">
          <el-icon><Box /></el-icon>
          <span>临期商品管理</span>
        </el-menu-item>
        <el-menu-item index="profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>

      <div class="side-footer">
        <el-button class="logout-btn" type="danger" plain icon="SwitchButton" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </div>

    <div class="main-content">
      <div class="top-header">
        <div class="breadcrumb">
          <el-icon v-if="activeTab === 'stock'"><Box /></el-icon>
          <el-icon v-else><User /></el-icon>
          <span style="margin-left: 8px">{{ activeTab === 'stock' ? '临期商品看板' : '个人中心' }}</span>
        </div>
        <div class="header-right">
          <el-tag :type="isAdmin ? 'danger' : 'success'" effect="plain">
            {{ isAdmin ? '系统管理员' : '门店员工' }}
          </el-tag>
          <span class="user-display">{{ userInfo.username }}</span>
        </div>
      </div>

      <div class="view-container">
        <div v-if="activeTab === 'stock'" class="fade-in">
          <el-card shadow="never">
            <template #header>
              <div class="flex-between">
                <div class="search-bar">
                  <span class="label">当前管理:</span>
                  <el-select v-if="isAdmin" v-model="adminFilterStore" placeholder="筛选门店" @change="fetchMyStock" style="width: 150px">
                    <el-option v-for="i in 10" :key="i" :label="`门店 ${i}`" :value="i" />
                  </el-select>
                  <span v-else class="store-tag">门店 ID: {{ storeId }}</span>
                </div>
                <el-button type="primary" icon="Plus" @click="showEntryDialog = true">录入上架</el-button>
              </div>
            </template>

            <el-table :data="myStock" border stripe v-loading="loading" height="calc(100vh - 220px)">
              <el-table-column prop="product_id" label="ID" width="70" />
              <el-table-column prop="barcode" label="条码" width="150" />
              <el-table-column prop="expiration_time" label="过期时间" width="180" sortable />
              <el-table-column prop="remaining_stock" label="库存" width="100" />
              <el-table-column label="状态">
                <template #default="{ row }">
                  <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="created_by" label="操作员" />
            </el-table>
          </el-card>
        </div>

        <div v-if="activeTab === 'profile'" class="fade-in">
          <el-row :gutter="20">
            <el-col :span="10">
              <el-card header="个人账号信息">
                <div class="profile-main">
                  <el-avatar :size="80" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                  <h2>{{ userInfo.username }}</h2>
                </div>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="用户ID">{{ userId }}</el-descriptions-item>
                  <el-descriptions-item label="所属权限">{{ userInfo.role }}</el-descriptions-item>
                  <el-descriptions-item label="关联门店ID">{{ storeId || '未绑定' }}</el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>
            <el-col :span="14">
              <el-card header="所属门店详情">
                <div v-if="storeInfo.store_name">
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="门店全称">{{ storeInfo.store_name }}</el-descriptions-item>
                    <el-descriptions-item label="所在城市">{{ storeInfo.city }}</el-descriptions-item>
                    <el-descriptions-item label="详细地址">{{ storeInfo.address }}</el-descriptions-item>
                  </el-descriptions>
                </div>
                <el-empty v-else description="暂无门店信息" />
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>

    <el-dialog v-model="showEntryDialog" title="新增临期上架" width="500px" @close="stopScanner">
      <el-form :model="form" label-position="top">
        <el-form-item label="商品选择">
          <div style="display: flex; gap: 10px;">
            <el-select
              v-model="selectedBarcode"
              filterable
              placeholder="请搜索名称或输入条码"
              style="flex: 1"
            >
              <el-option
                v-for="p in stdProds"
                :key="p.barcode"
                :label="p.product_name"
                :value="p.barcode"
              >
                <span style="float: left">{{ p.product_name }}</span>
                <span style="float: right; color: #999; font-size: 12px">{{ p.barcode }}</span>
              </el-option>
            </el-select>
            <el-button type="primary" :plain="!isScanning" @click="toggleScanner">
              <el-icon style="margin-right: 4px;"><Camera /></el-icon>
              {{ isScanning ? '取消扫码' : '扫码录入' }}
            </el-button>
          </div>
          <div v-if="selectedProductName" class="selected-hint">
            当前选中：<strong>{{ selectedProductName }}</strong> ({{ selectedBarcode }})
          </div>
        </el-form-item>

        <div v-show="isScanning" id="reader" class="scanner-box"></div>

        <el-form-item label="过期时间">
          <el-date-picker v-model="form.expTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" placeholder="请选择失效日期" />
        </el-form-item>
        <el-form-item label="上架数量">
          <el-input-number v-model="form.stock" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEntryDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEntry">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, User, Plus, SwitchButton, Camera } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'

const router = useRouter()
const userId = localStorage.getItem('userId')
const storeId = localStorage.getItem('storeId')
const isAdmin = ref(localStorage.getItem('userRole') === 'ADMIN')

const activeTab = ref('stock')
const userInfo = ref<any>({})
const storeInfo = ref<any>({})
const myStock = ref([])
const stdProds = ref<any[]>([]) // 存储所有标准商品
const loading = ref(false)
const submitting = ref(false)
const showEntryDialog = ref(false)
const adminFilterStore = ref('')

const selectedBarcode = ref('')
const form = reactive({ expTime: '', stock: 1 })

const isScanning = ref(false)
let scannerInstance: Html5Qrcode | null = null

// 计算属性：根据选中的条码自动获取商品名称
const selectedProductName = computed(() => {
  const item = stdProds.value.find(p => p.barcode === selectedBarcode.value)
  return item ? item.product_name : ''
})

const statusMap: any = {
  'AVAILABLE': { label: '在售', type: 'success' },
  'SOLD_OUT': { label: '已售罄', type: 'info' },
  'DISCARDED': { label: '已报损', type: 'danger' }
}

// 扫码逻辑
const toggleScanner = () => { isScanning.value ? stopScanner() : startScanner() }

const startScanner = () => {
  isScanning.value = true
  setTimeout(() => {
    scannerInstance = new Html5Qrcode("reader")
    scannerInstance.start(
      { facingMode: "environment" },
      { fps: 10, qrbox: { width: 250, height: 150 } },
      (decodedText) => {
        selectedBarcode.value = decodedText
        ElMessage.success(`扫码成功: ${decodedText}`)
        stopScanner()
      },
      () => {}
    ).catch(() => {
      ElMessage.error('无法启动相机')
      isScanning.value = false
    })
  }, 300)
}

const stopScanner = async () => {
  if (scannerInstance?.isScanning) {
    await scannerInstance.stop()
  }
  isScanning.value = false
  scannerInstance = null
}

const handleMenuSelect = (index: string) => { activeTab.value = index }

const handleLogout = () => {
  ElMessageBox.confirm('确定退出系统？', '提示').then(() => {
    localStorage.clear()
    router.push('/login')
  })
}

const fetchMyStock = async () => {
  loading.value = true
  try {
    let url = `/expiring-products`
    if (isAdmin.value && adminFilterStore.value) url += `?store_id=${adminFilterStore.value}`
    else if (!isAdmin.value) url += `?store_id=${storeId}`
    const res: any = await request.get(url)
    myStock.value = res.data || res
  } catch (e) {
    ElMessage.error('获取库存失败')
  } finally {
    loading.value = false
  }
}

const submitEntry = async () => {
  if (!selectedBarcode.value || !form.expTime) return ElMessage.warning('请填写完整')
  submitting.value = true
  try {
    await request.post('/expiring-products', {
      barcode: selectedBarcode.value,
      store_id: Number(storeId),
      expiration_time: form.expTime,
      remaining_stock: form.stock,
      status: 'AVAILABLE',
      created_by: Number(userId)
    })
    ElMessage.success('上架成功')
    showEntryDialog.value = false
    selectedBarcode.value = ''
    form.expTime = ''
    fetchMyStock()
  } catch (e) {
    ElMessage.error('失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  // 获取基础信息
  const uRes: any = await request.get(`/users/${userId}`)
  userInfo.value = uRes.data || uRes
  if (storeId) {
    const sRes: any = await request.get(`/stores/${storeId}`)
    storeInfo.value = sRes.data || sRes
  }
  // 关键：获取所有商品，确保下拉框有数据
  const pRes: any = await request.get('/products')
  stdProds.value = pRes.data || pRes
  fetchMyStock()
})

onUnmounted(() => stopScanner())
</script>

<style scoped>
.staff-container { display: flex; height: 100vh; width: 100vw; background: #f5f7f9; }
.side-nav { width: 220px; background: #004d2c; display: flex; flex-direction: column; }
.logo-box { padding: 30px 0; text-align: center; background: #fff; }
.logo-img { height: 40px; }
.brand-title { margin-top: 10px; font-weight: bold; color: #004d2c; font-size: 14px; }
.el-menu-vertical { border-right: none; background: transparent; flex: 1; }
.el-menu-vertical :deep(.el-menu-item) { color: #fff; }
.el-menu-vertical :deep(.el-menu-item.is-active) { background: #006b3d !important; color: #fff; }
.side-footer { padding: 20px; border-top: 1px solid rgba(255,255,255,0.1); }
.logout-btn { width: 100%; }
.main-content { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.top-header { height: 60px; background: #fff; display: flex; justify-content: space-between; align-items: center; padding: 0 25px; border-bottom: 1px solid #ddd; }
.view-container { padding: 20px; flex: 1; overflow-y: auto; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; align-items: center; gap: 10px; }
.store-tag { color: #004d2c; font-weight: bold; }
.fade-in { animation: fadeIn 0.3s ease-in; }

/* 扫码区域 */
.scanner-box { width: 100%; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; margin-top: 10px; background: #000; }
.selected-hint { margin-top: 8px; font-size: 13px; color: #67c23a; background: #f0f9eb; padding: 5px 10px; border-radius: 4px; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
</style>
