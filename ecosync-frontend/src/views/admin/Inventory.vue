<template>
  <div class="inventory-container">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: bold">标准库维护 (standard_products)</span>
          <el-button type="success" @click="openAddDialog">扫码/录入新商品</el-button>
        </div>
      </template>

      <el-table :data="products" v-loading="loading" stripe border>
        <el-table-column prop="barcode" label="条形码" width="150" />
        <el-table-column label="商品名称">
          <template #default="{ row }">
            {{ row.productName || row.product_name || '未命名' }}
          </template>
        </el-table-column>
        <el-table-column label="基础价格">
          <template #default="{ row }">
            <span>￥{{ parseFloat(row.normalPrice || row.normal_price || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="管理" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row)">配置积分规则</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="录入基础商品" width="450px" destroy-on-close>
      <el-form :model="newP" label-position="top">
        <el-form-item label="条形码">
          <el-input v-model="newP.barcode" placeholder="请扫描或输入商品条码" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="newP.productName" placeholder="输入商品名称" />
        </el-form-item>
        <el-form-item label="基准单价">
          <el-input-number v-model="newP.normalPrice" :precision="2" :step="0.1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="submitLoading">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const products = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const showAdd = ref(false)
const newP = ref({ barcode: '', productName: '', normalPrice: 0.00 })

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/products')
    products.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    ElMessage.error('无法连接服务器获取数据')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  newP.value = { barcode: '', productName: '', normalPrice: 0.00 }
  showAdd.value = true
}

const submitAdd = async () => {
  if (!newP.value.barcode || !newP.value.productName) return ElMessage.warning('请填写完整信息')
  submitLoading.value = true
  try {
    await request.post('/products', {
      barcode: newP.value.barcode,
      productName: newP.value.productName,
      normalPrice: newP.value.normalPrice
    })
    ElMessage.success('录入成功')
    showAdd.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('录入失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定删除吗？`, '警告', { type: 'warning' }).then(async () => {
    try {
      await request.delete(`/products/${row.barcode}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

const goDetail = (row: any) => {
  router.push({ name: 'AdminHome', query: { id: row.barcode } })
}

onMounted(fetchData)
</script>
