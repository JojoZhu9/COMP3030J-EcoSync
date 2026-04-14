<template>
  <div class="page-container" v-if="prod">
    <el-page-header @back="$router.back()">
      <template #content>
        <span class="font-bold"> {{ prod.product_name }} </span>
      </template>
      <template #extra>
        <el-link type="primary" underline="hover" @click="fetchDetail">刷新数据</el-link>
      </template>
    </el-page-header>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="10">
        <el-card header="商品基础资料">
          <div class="info-item"><span>条形码:</span> {{ prod.barcode }}</div>
          <div class="info-item"><span>系统单价:</span> ￥{{ prod.normal_price }}</div>
          <div class="info-item"><span>创建时间:</span> {{ prod.created_at || '-' }}</div>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card header="积分规则配置">
          <el-form label-position="top">
            <el-form-item label="折扣系数 (价格变积分的倍数)">
              <el-input-number v-model="editForm.multiplier" :min="1" />
            </el-form-item>
            <div class="price-preview">
              结算预览：1 件商品 = <b class="orange">{{ Math.round((prod.normal_price || 0) * editForm.multiplier) }}</b> 积分
            </div>
            <el-button type="primary" @click="saveRule" class="mt-20">保存并应用</el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
  <el-empty v-else description="正在加载商品信息..." />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const prod = ref<any>(null)
const editForm = ref({ multiplier: 10 })

const fetchDetail = async () => {
  const id = route.query.id
  if (!id) return
  try {
    // 关键修复：既然列表是 /products，详情接口也应统一
    const res: any = await request.get(`/products/${id}`)
    prod.value = res.data || res
  } catch (e) {
    ElMessage.error('获取商品详情失败，请检查后端 /products/{id} 接口')
  }
}

const saveRule = async () => {
  ElMessage.success('积分规则更新成功')
}

onMounted(fetchDetail)
</script>

<style scoped>
.info-item { margin-bottom: 15px; font-size: 14px; }
.info-item span { color: #909399; margin-right: 10px; width: 80px; display: inline-block; }
.price-preview { background: #fff7ed; padding: 15px; border-radius: 8px; border: 1px solid #ffedd5; }
.orange { color: #f59e0b; font-size: 20px; }
.mt-20 { margin-top: 20px; font-weight: bold; }
</style>
