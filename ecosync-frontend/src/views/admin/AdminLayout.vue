<template>
  <div class="admin-container">
    <el-aside width="220px" class="aside">
      <div class="logo">管理后台</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#fff">
        <el-menu-item index="/admin/dashboard">数据统计</el-menu-item>
        <el-menu-item index="/admin/inventory">商品管理</el-menu-item>
        <el-menu-item index="/admin/accounts">账号管理</el-menu-item>
        <el-menu-item index="/admin/scan">个人设置</el-menu-item>
        <el-menu-item @click="logout">退出登录</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main class="main">
      <router-view />
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.admin-container { display: flex; height: 100vh; }
.aside { background: #304156; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-weight: bold; font-size: 18px; border-bottom: 1px solid #1f2d3d; }
.main { background: #f0f2f5; padding: 20px; overflow-y: auto; flex: 1; }
</style>
