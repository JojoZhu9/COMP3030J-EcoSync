<template>
  <div class="pc-layout">
    <template v-if="['login', 'landing'].includes(route.name as string)">
      <router-view />
    </template>

    <template v-else-if="currentRole === 'EMPLOYEE'">
      <router-view />
    </template>

    <template v-else>
      <div class="main-wrapper">
        <aside class="side-bar" :class="roleClass">
          <div class="logo-area">EcoSync {{ roleTitle }}</div>

          <nav class="nav-menu">
            <template v-if="currentRole === 'ADMIN'">
              <div class="menu-item" :class="{ active: route.path.includes('accounts') }" @click="go('/admin/accounts')">
                <el-icon><UserFilled /></el-icon> <span>Account Management</span>
              </div>
              <div class="menu-item" :class="{ active: route.path.includes('inventory') }" @click="go('/admin/inventory')">
                <el-icon><Box /></el-icon> <span>Inventory Management</span>
              </div>
              <div class="menu-item" :class="{ active: route.path.includes('dashboard') }" @click="go('/admin/dashboard')">
                <el-icon><TrendCharts /></el-icon> <span>Dashboard</span>
              </div>
            </template>

            <template v-else>
              <div class="menu-item" :class="{ active: route.path === '/home' }" @click="go('/home')">
                <el-icon><HomeFilled /></el-icon> <span>Store Home</span>
              </div>
              <div class="menu-item" :class="{ active: route.path === '/cart' }" @click="go('/cart')">
                <el-icon><ShoppingCart /></el-icon> <span>Shopping Cart</span>
              </div>
              <div class="menu-item" :class="{ active: route.path === '/order-status' }" @click="go('/order-status')">
                <el-icon><List /></el-icon> <span>My Orders</span>
              </div>
              <div class="menu-item" :class="{ active: route.path === '/profile' }" @click="go('/profile')">
                <el-icon><User /></el-icon> <span>Profile</span>
              </div>
            </template>
          </nav>

          <div class="logout-footer" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> Logout
          </div>
        </aside>

        <main class="content-area">
          <router-view />
        </main>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled, UserFilled, Box, List,
  SwitchButton, TrendCharts, ShoppingCart, User
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const getCurrentUserRole = () => {
  const token = localStorage.getItem('token')
  if (!token || token === 'null' || token === 'undefined') return null
  return (localStorage.getItem('role') || '').toUpperCase()
}

const currentRole = ref(getCurrentUserRole())

const roleClass = computed(() => {
  if (currentRole.value === 'ADMIN') return 'bg-admin'
  return 'bg-consumer'
})

const roleTitle = computed(() => {
  if (currentRole.value === 'ADMIN') return 'Admin Panel'
  if (currentRole.value === 'EMPLOYEE') return 'Staff Panel'
  return 'Store'
})

const sync = () => {
  currentRole.value = getCurrentUserRole()
}

onMounted(sync)
// Listen for route changes to ensure role state is synced
watch(() => route.path, sync)

const go = (p: string) => router.push(p)

const handleLogout = () => {
  localStorage.clear()
  sync()
  router.push('/login')
}
</script>

<style>
/* Styles remain unchanged */
.main-wrapper { display: flex; height: 100vh; width: 100vw; overflow: hidden; }
.side-bar { width: 220px; color: white; display: flex; flex-direction: column; transition: 0.3s; z-index: 100; }
.bg-admin { background: #001529; }
.bg-consumer { background: #2c3e50; }
.logo-area { padding: 25px 20px; font-size: 18px; font-weight: bold; border-bottom: 1px solid rgba(255,255,255,0.1); text-align: center; }
.nav-menu { flex: 1; padding: 15px 0; }
.menu-item { padding: 15px 25px; cursor: pointer; display: flex; align-items: center; gap: 12px; color: rgba(255,255,255,0.7); }
.menu-item:hover { color: white; background: rgba(255,255,255,0.1); }
.menu-item.active { background: #409eff; color: white; font-weight: bold; }
.logout-footer { padding: 20px; border-top: 1px solid rgba(255,255,255,0.1); cursor: pointer; color: #ff7875; display: flex; align-items: center; gap: 10px; }
.content-area { flex: 1; background: #f0f2f5; padding: 0; overflow-y: auto; }
</style>
