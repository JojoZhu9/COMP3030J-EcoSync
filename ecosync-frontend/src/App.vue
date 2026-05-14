<template>
  <div class="pc-layout">
    <div class="brand-top-stripe"></div>

    <template v-if="route.name === 'login'">
      <router-view />
    </template>

    <template v-else>
      <header class="top-navbar" :class="roleClass">
        <div class="nav-left">
          <div class="brand-logo" @click="go('/')">
            <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
          </div>
          <span class="logo-text divider">|</span>
          <span class="logo-text">Intelligent Near-Expiry</span>
          <el-tag v-if="isLogged" effect="dark" :type="currentRole === 'ADMIN' ? 'danger' : 'success'" round size="small" class="role-badge">
            {{ roleTitle }}
          </el-tag>
        </div>

        <nav class="nav-menu">
          <div v-if="currentRole !== 'ADMIN'" class="menu-item" :class="{ active: route.path === '/' }" @click="go('/')">Home Page</div>

          <template v-if="currentRole === 'ADMIN'">
            <div class="menu-item" :class="{ active: route.path.includes('accounts') }" @click="go('/admin/accounts')">Accounts</div>
            <div class="menu-item" :class="{ active: route.path.includes('inventory') }" @click="go('/admin/inventory')">Inventory</div>
            <div class="menu-item" :class="{ active: route.path.includes('dashboard') }" @click="go('/admin/dashboard')">Analytics</div>
          </template>

          <template v-else-if="currentRole === 'EMPLOYEE'">
            <div class="menu-item" :class="{ active: route.path.includes('staff/home') }" @click="go('/staff/home')">Workspace</div>
          </template>

          <template v-else>
            <template v-if="isLogged">
              <div class="menu-item" :class="{ active: route.path === '/home' }" @click="go('/home')">Store Home</div>
              <div class="menu-item" :class="{ active: route.path === '/cart' }" @click="go('/cart')">Cart</div>
              <div class="menu-item" :class="{ active: route.path === '/order-status' }" @click="go('/order-status')">Orders</div>
            </template>
          </template>
        </nav>

        <div class="nav-right">
          <el-button v-if="!isLogged" class="login-nav-btn" type="success" round @click="go('/login')">Log In</el-button>

          <template v-else>
            <div v-if="currentRole !== 'ADMIN'" class="menu-item profile-icon" :class="{ active: route.path.includes('profile') }" @click="goProfile">
              <el-icon><User /></el-icon>
            </div>
            <div class="logout-btn" @click="handleLogout" title="Logout">
              <el-icon><SwitchButton /></el-icon>
            </div>
          </template>
        </div>
      </header>

      <main class="content-area">
        <div :class="{ 'content-inner': route.path !== '/' }">
          <router-view />
        </div>
      </main>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { SwitchButton, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isLogged = ref(false)
const currentRole = ref<string | null>(null)

const syncStatus = () => {
  const token = localStorage.getItem('token')
  isLogged.value = !!(token && token !== 'null' && token !== 'undefined')
  const role = localStorage.getItem('role')
  currentRole.value = role ? role.toUpperCase() : null
}

const roleClass = computed(() => currentRole.value === 'ADMIN' ? 'bg-admin' : 'bg-consumer')
const roleTitle = computed(() => {
  if (currentRole.value === 'ADMIN') return 'Admin Panel'
  if (currentRole.value === 'EMPLOYEE') return 'Staff Panel'
  return 'Customer'
})

onMounted(() => {
  syncStatus()
  window.addEventListener('auth-change', syncStatus)
})
onUnmounted(() => window.removeEventListener('auth-change', syncStatus))

const go = (p: string) => router.push(p)
const goProfile = () => go(currentRole.value === 'EMPLOYEE' ? '/staff/profile' : '/profile')

const handleLogout = () => {
  localStorage.clear()
  window.dispatchEvent(new Event('auth-change'))
  router.push('/login')
}
</script>

<style>
:root { --711-green: #007934; --711-red: #e2231a; --711-orange: #ff7900; --navbar-height: 70px; }
body { margin: 0; padding: 0; font-family: 'Inter', sans-serif; background: #f8f9fa; }
.c-orange { color: var(--711-orange); } .c-red { color: var(--711-red); } .c-green { color: var(--711-green); }

.brand-top-stripe { height: 6px; width: 100%; background: linear-gradient(to right, #ff7900 33.33%, #007934 33.33%, #007934 66.66%, #e2231a 66.66%); position: fixed; top: 0; z-index: 2000; }

.top-navbar { display: flex; align-items: center; justify-content: space-between; height: var(--navbar-height); background: rgba(255,255,255,0.95); backdrop-filter: blur(15px); padding: 0 40px; position: sticky; top: 6px; z-index: 1000; border-bottom: 1px solid #eaeaea; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.bg-admin { background: #002d14; color: white; border-bottom: none; }
.bg-admin .c-green, .bg-admin .c-red, .bg-admin .c-orange { color: white; }

.nav-left { display: flex; align-items: center; gap: 15px; cursor: pointer; }
.brand-logo { font-size: 26px; font-weight: 900; font-style: italic; }
.logo-text { font-size: 14px; font-weight: 600; color: #888; text-transform: uppercase; letter-spacing: 1px; }
.divider { margin: 0 5px; color: #eee; }
.bg-admin .logo-text { color: #a0aec0; }
.role-badge { margin-left: 10px; }

/* ======== 修改这里：向左对齐并加一点左边距 ======== */
.nav-menu { display: flex; align-items: center; gap: 8px; flex: 1; justify-content: flex-start; margin-left: 40px; }
/* ================================================ */

.menu-item { padding: 8px 16px; border-radius: 20px; cursor: pointer; font-weight: 600; transition: 0.3s; color: #475569; font-size: 15px; }
.bg-admin .menu-item { color: #cbd5e1; }

.menu-item:hover { background: rgba(0, 121, 52, 0.1); color: var(--711-green); }
.bg-admin .menu-item:hover { background: rgba(255, 255, 255, 0.1); color: white; }

.menu-item.active { background: var(--711-green); color: #fff; }
.bg-admin .menu-item.active { background: var(--711-orange); color: white; }

.nav-right { display: flex; align-items: center; gap: 10px; }
.login-nav-btn { background-color: #007934 !important; border: none; font-weight: bold; padding: 18px 25px; box-shadow: 0 4px 10px rgba(0, 121, 52, 0.2); }
.profile-icon { font-size: 18px; padding: 8px; display: flex; align-items: center; justify-content: center; }
.logout-btn { color: var(--711-red); font-size: 22px; cursor: pointer; padding: 8px; transition: 0.2s; display: flex; align-items: center; }
.logout-btn:hover { transform: scale(1.1); }

.content-area { min-height: calc(100vh - var(--navbar-height) - 6px); }
/* 有内容内边距的页面 */
.content-inner { max-width: 1400px; margin: 0 auto; padding: 20px; }
</style>
