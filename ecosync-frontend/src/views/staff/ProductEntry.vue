<template>
  <div class="product-entry">
    <el-card>
      <template #header><b>临期商品上架 (Employee 专用)</b></template>
      <el-form :model="form" label-width="120px" style="max-width: 500px">
        <el-form-item label="扫描条码">
          <el-input v-model="form.barcode" placeholder="输入条码并回车" @keyup.enter="checkBarcode">
            <template #append>
              <el-button @click="checkBarcode">查询标库</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="识别结果">
          <el-tag v-if="productInfo" type="success">{{ productInfo.productName }} (￥{{ productInfo.normalPrice }})</el-tag>
          <el-tag v-else type="info">请先输入有效条码</el-tag>
        </el-form-item>

        <el-form-item label="到期时间">
          <el-date-picker v-model="form.expirationTime" type="datetime" placeholder="请选择精确到期时间" />
        </el-form-item>

        <el-form-item label="实物库存">
          <el-input-number v-model="form.remainingStock" :min="1" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit" :disabled="!productInfo">确认上架</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { standardApi, expiringApi } from '../../api/product'
import { ElMessage } from 'element-plus'

const form = reactive({ barcode: '', expirationTime: '', remainingStock: 1, storeId: 1, status: 'AVAILABLE' })
const productInfo = ref<any>(null)

const checkBarcode = async () => {
  if (!form.barcode) return
  const res = await standardApi.getByBarcode(form.barcode)
  if (res.data) {
    productInfo.value = res.data
    ElMessage.success('匹配成功')
  } else {
    productInfo.value = null
    ElMessage.error('标库中不存在此条码')
  }
}

const submit = async () => {
  try {
    await expiringApi.add(form)
    ElMessage.success('上架成功')
    form.barcode = ''
    productInfo.value = null
  } catch (e) { ElMessage.error('提交失败') }
}
</script>
