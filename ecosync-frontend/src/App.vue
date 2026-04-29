<template>
  <div class="pc-layout">
    <div class="brand-header-stripe"></div>

    <template v-if="['login', 'landing'].includes(route.name as string)">
      <router-view />
    </template>

    <template v-else>
      <div class="main-wrapper">
        <aside class="side-bar" :class="roleClass">
          <div class="logo-area">
            <div class="brand-logo">
              <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
            </div>
            <div class="platform-name">SMARTCHAIN RETAIL TECH</div>
            <el-tag effect="dark" :type="currentRole === 'ADMIN' ? 'danger' : 'success'" round size="small" class="role-badge">
              {{ roleTitle }}
            </el-tag>
          </div>

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

            <template v-else-if="currentRole === 'EMPLOYEE'">
              <div class="menu-item" :class="{ active: route.path.includes('staff/home') }" @click="go('/staff/home')">
                <el-icon><HomeFilled /></el-icon> <span>Staff Workspace</span>
              </div>
              <div class="menu-item" :class="{ active: route.path === '/profile' }" @click="go('/profile')">
                <el-icon><User /></el-icon> <span>Profile</span>
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
            <div class="logout-inner">
              <el-icon><SwitchButton /></el-icon> <span>Logout</span>
            </div>
          </div>
        </aside>

        <main class="content-area">
          <div class="content-inner">
            <router-view />
          </div>
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
watch(() => route.path, sync)

const go = (p: string) => router.push(p)

const handleLogout = () => {
  localStorage.clear()
  sync()
  router.push('/login')
}
</script>

<style>
:root {
  --711-green: #007934;
  --711-red: #e2231a;
  --711-orange: #ff7900;
  --sidebar-width: 260px;
}
body { margin: 0; padding: 0; font-family: 'Inter', 'PingFang SC', sans-serif; }
.brand-header-stripe { height: 6px; width: 100%; background: linear-gradient(to right, var(--711-orange) 0%, var(--711-orange) 33.33%, var(--711-green) 33.33%, var(--711-green) 66.66%, var(--711-red) 66.66%, var(--711-red) 100%); position: fixed; top: 0; left: 0; z-index: 2000; }
.main-wrapper { display: flex; height: 100vh; width: 100vw; overflow: hidden; padding-top: 6px; box-sizing: border-box; }
.side-bar { width: var(--sidebar-width); display: flex; flex-direction: column; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); z-index: 100; box-shadow: 4px 0 15px rgba(0,0,0,0.05); }
.bg-admin { background: #002d14; color: white; }
.bg-consumer { background: #ffffff; color: #333; border-right: 1px solid #f0f0f0; }
.logo-area { padding: 40px 20px; text-align: center; border-bottom: 1px solid rgba(128, 128, 128, 0.1); }
.brand-logo { font-size: 32px; font-weight: 900; font-style: italic; letter-spacing: -1.5px; margin-bottom: 5px; }
.c-orange { color: var(--711-orange); }
.c-red { color: var(--711-red); }
.c-green { color: var(--711-green); }
.platform-name { font-size: 14px; font-weight: 600; letter-spacing: 2px; text-transform: uppercase; margin-bottom: 15px; opacity: 0.8; }
.role-badge { font-weight: bold; border: none; }
.nav-menu { flex: 1; padding: 20px 12px; }
.menu-item { padding: 14px 20px; margin-bottom: 8px; border-radius: 12px; cursor: pointer; display: flex; align-items: center; gap: 14px; font-size: 15px; font-weight: 500; transition: all 0.2s ease; }
.bg-consumer .menu-item { color: #5c6b77; }
.bg-consumer .menu-item:hover { background: #f5f7f9; color: var(--711-green); }
.bg-consumer .menu-item.active { background: var(--711-green); color: white; box-shadow: 0 4px 12px rgba(0, 121, 52, 0.3); }
.bg-admin .menu-item { color: rgba(255,255,255,0.6); }
.bg-admin .menu-item:hover { background: rgba(255,255,255,0.05); color: white; }
.bg-admin .menu-item.active { background: var(--711-orange); color: white; box-shadow: 0 4px 12px rgba(255, 121, 0, 0.3); }
.logout-footer { padding: 20px 12px; border-top: 1px solid rgba(128, 128, 128, 0.1); }
.logout-inner { display: flex; align-items: center; gap: 10px; padding: 12px 20px; border-radius: 10px; cursor: pointer; color: var(--711-red); font-weight: 600; transition: background 0.2s; }
.logout-inner:hover { background: rgba(226, 35, 26, 0.08); }
.content-area { flex: 1; background: #f8f9fa; overflow-y: auto; position: relative; }
.content-area::before { content: ""; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-image: radial-gradient(#d1d1d1 0.5px, transparent 0.5px); background-size: 24px 24px; opacity: 0.3; pointer-events: none; }
.content-inner { position: relative; z-index: 1; height: 100%; }
</style>```
