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
            {{ row.product_name || row.productName || '未命名' }}
          </template>
        </el-table-column>
        <el-table-column label="基础价格">
          <template #default="{ row }">
            <span>￥{{ parseFloat(row.normal_price || row.normalPrice || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="管理" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row)">配置管理</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="录入基础商品" width="600px" destroy-on-close>
      <el-form :model="newP" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="条形码">
              <el-input v-model="newP.barcode" placeholder="请扫描或输入商品条码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基准单价">
              <el-input-number v-model="newP.normal_price" :precision="2" :step="0.1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品名称">
          <el-input v-model="newP.product_name" placeholder="输入商品名称" />
        </el-form-item>

        <el-divider content-position="left">临期折扣配置 (距离过期 1-12 小时)</el-divider>
        <p style="font-size: 12px; color: #909399; margin-bottom: 10px;">
          设置对应小时的折扣系数（0.1 表示 1 折，1.0 表示原价）
        </p>

        <el-row :gutter="10">
          <el-col :span="6" v-for="h in 12" :key="h" style="margin-bottom: 10px;">
            <div class="discount-input-group">
              <span class="hour-label">{{ h }}h:</span>
              <el-input-number
                v-model="newP.discount_rates[h-1]"
                :min="0.1"
                :max="1.0"
                :step="0.1"
                :precision="1"
                controls-position="right"
                size="small"
                style="width: 70px"
              />
            </div>
          </el-col>
        </el-row>
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

// 初始化数据结构，包含 12 小时的折扣率数组
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
    // 兼容不同的后端返回结构
    products.value = Array.isArray(res) ? res : (res.data || [])
  } catch (e) {
    ElMessage.error('无法连接服务器获取数据')
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
    return ElMessage.warning('请填写完整信息')
  }

  submitLoading.value = true
  try {
    // 按照新的表结构发送数据，discount_rates 转化为 JSON 字符串提交
    await request.post('/products', {
      barcode: newP.value.barcode,
      product_name: newP.value.product_name,
      normal_price: newP.value.normal_price,
      discount_rates: JSON.stringify(newP.value.discount_rates)
    })

    ElMessage.success('录入成功')
    showAdd.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '录入失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row: any) => {
  const id = row.barcode
  ElMessageBox.confirm(`确定删除商品 [${row.product_name || id}] 吗？`, '警告', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await request.delete(`/products/${id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const goDetail = (row: any) => {
  // 修改路由跳转逻辑，对应原来的详情/规则配置页
  router.push({ name: 'AdminHome', query: { id: row.barcode } })
}

onMounted(fetchData)
</script>

<style scoped>
.discount-input-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f5f7fa;
  padding: 5px 8px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.hour-label {
  font-size: 12px;
  color: #606266;
  font-weight: bold;
}

:deep(.el-divider__text) {
  font-weight: bold;
  color: #409eff;
}
</style>
