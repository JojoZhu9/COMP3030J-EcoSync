<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in summary" :key="item.title">
        <el-card shadow="hover" style="text-align: center;">
          <div style="color: #909399; font-size: 14px;">{{ item.title }}</div>
          <div style="font-size: 28px; font-weight: bold; margin-top: 10px;">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card header="用户分布构成">
          <div class="stat-chart-mock">
            <div v-for="r in roleStats" :key="r.label" class="chart-row">
              <span style="width: 80px">{{ r.label }}</span>
              <el-progress :percentage="r.value" :color="r.color" style="flex:1" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="系统基础信息">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="当前数据库">MySQL 8.0 (EcoSync_DB)</el-descriptions-item>
            <el-descriptions-item label="后端 API 状态"><el-tag type="success">RUNNING</el-tag></el-descriptions-item>
            <el-descriptions-item label="最后同步时间">{{ new Date().toLocaleString() }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const summary = ref([
  { title: '基础商品总数', value: 0 },
  { title: '注册用户总数', value: 0 },
  { title: '今日活跃职员', value: 3 },
  { title: '系统预警条数', value: 12 }
])

const roleStats = ref([
  { label: '管理员', value: 0, color: '#f56c6c' },
  { label: '员工', value: 0, color: '#409eff' },
  { label: '消费者', value: 0, color: '#67c23a' }
])

onMounted(async () => {
  const [p, u]: any = await Promise.all([request.get('/products'), request.get('/users')])
  const pList = p.data || p; const uList = u.data || u

  summary.value[0].value = pList.length
  summary.value[1].value = uList.length

  const total = uList.length || 1
  roleStats.value[0].value = Math.round((uList.filter((x:any)=>x.role==='ADMIN').length / total) * 100)
  roleStats.value[1].value = Math.round((uList.filter((x:any)=>x.role==='EMPLOYEE').length / total) * 100)
  roleStats.value[2].value = Math.round((uList.filter((x:any)=>x.role==='CONSUMER').length / total) * 100)
})
</script>

<style scoped>
.mt-20 { margin-top: 20px; }
.chart-row { display: flex; align-items: center; margin-bottom: 25px; }
</style>
