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
              <div class="menu-item" :class="{ active: route.path === '/staff/profile' }" @click="go('/staff/profile')">
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
import { HomeFilled, UserFilled, Box, List, SwitchButton, TrendCharts, ShoppingCart, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const getCurrentUserRole = () => {
  const role = localStorage.getItem('role')
  return role ? role.toUpperCase() : null
}

const currentRole = ref(getCurrentUserRole())
const roleClass = computed(() => currentRole.value === 'ADMIN' ? 'bg-admin' : 'bg-consumer')
const roleTitle = computed(() => {
  if (currentRole.value === 'ADMIN') return 'Admin Panel'
  if (currentRole.value === 'EMPLOYEE') return 'Staff Panel'
  return 'Store'
})

const sync = () => { currentRole.value = getCurrentUserRole() }
onMounted(sync)
watch(() => route.path, sync)

const go = (p: string) => router.push(p)
const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style>
/* 保持原樣式不變 */
:root { --711-green: #007934; --711-red: #e2231a; --711-orange: #ff7900; --sidebar-width: 260px; }
body { margin: 0; padding: 0; font-family: 'Inter', sans-serif; }
.brand-header-stripe { height: 6px; width: 100%; background: linear-gradient(to right, var(--711-orange) 33%, var(--711-green) 33%, var(--711-green) 66%, var(--711-red) 66%); position: fixed; top: 0; z-index: 2000; }
.main-wrapper { display: flex; height: 100vh; padding-top: 6px; }
.side-bar { width: var(--sidebar-width); display: flex; flex-direction: column; background: #fff; border-right: 1px solid #f0f0f0; }
.bg-admin { background: #002d14; color: white; }
.logo-area { padding: 30px 20px; text-align: center; }
.brand-logo { font-size: 28px; font-weight: 900; font-style: italic; }
.c-orange { color: var(--711-orange); } .c-red { color: var(--711-red); } .c-green { color: var(--711-green); }
.nav-menu { flex: 1; padding: 10px; }
.menu-item { padding: 12px 20px; margin-bottom: 5px; border-radius: 8px; cursor: pointer; display: flex; align-items: center; gap: 12px; transition: 0.3s; }
.menu-item.active { background: var(--711-green); color: #fff; }
.bg-admin .menu-item.active { background: var(--711-orange); }
.logout-footer { padding: 20px; border-top: 1px solid #eee; cursor: pointer; color: var(--711-red); font-weight: bold; }
.content-area { flex: 1; background: #f8f9fa; overflow-y: auto; }
</style>
