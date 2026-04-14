<template>
  <div class="staff-home">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="status-card">
          <template #header>今日录入</template>
          <div class="num">12 <span class="unit">件</span></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="status-card danger">
          <template #header>急需处理(即将过期)</template>
          <div class="num">5 <span class="unit">件</span></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="status-card">
          <template #header>待核销订单</template>
          <div class="num">3 <span class="unit">单</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="todo-list" style="margin-top: 20px;">
      <template #header>快捷操作</template>
      <el-button type="primary" size="large" @click="$router.push('/staff/entry')">扫码录入新临期</el-button>
      <el-button type="success" size="large">查看本店订单</el-button>
      <el-button type="warning" size="large">库存报损</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.status-card { text-align: center; }
.status-card .num { font-size: 36px; font-weight: bold; color: #409EFF; padding: 10px 0; }
.danger .num { color: #F56C6C; }
.unit { font-size: 14px; font-weight: normal; color: #999; }
</style>
