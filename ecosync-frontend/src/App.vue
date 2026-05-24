<template>
  <el-config-provider :locale="elementLocale">
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
          <div v-if="currentRole !== 'ADMIN'" class="menu-item" :class="{ active: route.path === '/' }" @click="go('/')">{{ $t('nav.homePage') }}</div>

          <template v-if="currentRole === 'ADMIN'">
            <div class="menu-item" :class="{ active: route.path === '/' }" @click="go('/')">{{ $t('nav.homePage') }}</div>
            <div class="menu-item" :class="{ active: route.path.includes('accounts') }" @click="go('/admin/accounts')">{{ $t('nav.accounts') }}</div>
            <div class="menu-item" :class="{ active: route.path.includes('stores') }" @click="go('/admin/stores')">{{ $t('nav.stores') }}</div>
            <div class="menu-item" :class="{ active: route.path.includes('inventory') }" @click="go('/admin/inventory')">{{ $t('nav.inventory') }}</div>
            <div class="menu-item" :class="{ active: route.path.includes('dashboard') }" @click="go('/admin/dashboard')">{{ $t('nav.analytics') }}</div>
          </template>

          <template v-else-if="currentRole === 'EMPLOYEE'">
            <div class="menu-item" :class="{ active: route.path.includes('staff/home') }" @click="go('/staff/home')">{{ $t('nav.workspace') }}</div>
          </template>

          <template v-else>
            <template v-if="isLogged">
              <div class="menu-item" :class="{ active: route.path === '/home' }" @click="go('/home')">{{ $t('nav.storeHome') }}</div>
              <div class="menu-item" :class="{ active: route.path === '/cart' }" @click="go('/cart')">{{ $t('nav.cart') }}</div>
              <div class="menu-item" :class="{ active: route.path === '/order-status' }" @click="go('/order-status')">{{ $t('nav.orders') }}</div>
            </template>
          </template>
        </nav>

        <div class="nav-right">
          <el-dropdown trigger="click" @command="handleLocaleChange">
            <span class="locale-switch">
              <span class="locale-label">{{ currentLocaleLabel }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="en" :class="{ active: locale === 'en' }">English</el-dropdown-item>
                <el-dropdown-item command="zh" :class="{ active: locale === 'zh' }">中文</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button v-if="!isLogged" class="login-nav-btn" type="success" round @click="go('/login')">{{ $t('common.login') }}</el-button>

          <template v-else>
            <div v-if="currentRole !== 'ADMIN'" class="menu-item profile-icon" :class="{ active: route.path.includes('profile') }" @click="goProfile">
              <el-icon><User /></el-icon>
            </div>
            <div class="logout-btn" @click="handleLogout" :title="$t('common.logout')">
              <el-icon><SwitchButton /></el-icon>
            </div>
          </template>
        </div>
      </header>

        <main class="content-area">
          <div :class="{ 'content-inner': route.path !== '/' }">
            <router-view :key="localeKey" />
          </div>
        </main>
      </template>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { SwitchButton, User } from '@element-plus/icons-vue'
import { setLocale, getLocale, type Locale } from './locales'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import enLocale from 'element-plus/es/locale/lang/en'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const isLogged = ref(false)
const currentRole = ref<string | null>(null)
const locale = ref<Locale>(getLocale())
const localeKey = ref(0)

const currentLocaleLabel = computed(() => locale.value === 'zh' ? '中文' : 'EN')
const elementLocale = computed(() => locale.value === 'zh' ? zhCn : enLocale)

const syncStatus = () => {
  const token = localStorage.getItem('token')
  isLogged.value = !!(token && token !== 'null' && token !== 'undefined')
  const role = localStorage.getItem('role')
  currentRole.value = role ? role.toUpperCase() : null
}

const roleClass = computed(() => currentRole.value === 'ADMIN' ? 'bg-admin' : 'bg-consumer')
const roleTitle = computed(() => {
  if (currentRole.value === 'ADMIN') return t('nav.adminPanel')
  if (currentRole.value === 'EMPLOYEE') return t('nav.staffPanel')
  return t('nav.customer')
})

onMounted(() => {
  syncStatus()
  window.addEventListener('auth-change', syncStatus)
})
onUnmounted(() => window.removeEventListener('auth-change', syncStatus))

const go = (p: string) => router.push(p)
const goProfile = () => go(currentRole.value === 'EMPLOYEE' ? '/staff/profile' : '/profile')

const handleLogout = () => {
  const savedLocale = localStorage.getItem('locale')
  localStorage.clear()
  if (savedLocale) localStorage.setItem('locale', savedLocale)
  window.dispatchEvent(new Event('auth-change'))
  router.push('/login')
}

const handleLocaleChange = (command: Locale) => {
  if (command === locale.value) return
  setLocale(command)
  locale.value = command
  localeKey.value++
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

.nav-menu { display: flex; align-items: center; gap: 8px; flex: 1; justify-content: flex-start; margin-left: 40px; }

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

.locale-switch { display: flex; align-items: center; gap: 4px; cursor: pointer; font-size: 14px; font-weight: 600; color: #475569; padding: 6px 10px; border-radius: 8px; transition: 0.2s; }
.locale-switch:hover { background: rgba(0,0,0,0.05); }
.bg-admin .locale-switch { color: #cbd5e1; }
.bg-admin .locale-switch:hover { background: rgba(255,255,255,0.1); }
.locale-label { min-width: 28px; text-align: center; }

.content-area { min-height: calc(100vh - var(--navbar-height) - 6px); }
.content-inner { max-width: 1400px; margin: 0 auto; padding: 20px; }
</style>
