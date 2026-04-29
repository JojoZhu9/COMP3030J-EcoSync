<template>
  <div class="admin-layout">
    <el-aside width="260px" class="brand-aside">
      <div class="aside-header">
        <div class="brand-logo">
          <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
        </div>
        <div class="platform-tag">Admin Console</div>
      </div>

      <div class="menu-wrapper">
        <el-menu
          :default-active="$route.path"
          router
          class="brand-menu"
          background-color="transparent"
          text-color="#a0aec0"
          active-text-color="#ffffff"
        >
          <div class="menu-section-label">Main Operations</div>

          <el-menu-item index="/admin/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>Analytics Dashboard</span>
          </el-menu-item>

          <el-menu-item index="/admin/inventory">
            <el-icon><Box /></el-icon>
            <span>Product Inventory</span>
          </el-menu-item>

          <el-menu-item index="/admin/accounts">
            <el-icon><UserGroup /></el-icon>
            <span>Account Control</span>
          </el-menu-item>

          <div class="menu-section-label">System</div>

          <el-menu-item index="/admin/scan">
            <el-icon><Setting /></el-icon>
            <span>Settings</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="aside-footer">
        <div class="admin-profile">
          <el-avatar :size="32" class="admin-avatar">A</el-avatar>
          <div class="admin-info">
            <div class="admin-name">{{ currentAdmin }}</div>
            <div class="admin-role">Super Administrator</div>
          </div>
        </div>
        <el-button class="logout-btn" link @click="logout">
          <el-icon><SwitchButton /></el-icon> Sign Out
        </el-button>
      </div>
    </el-aside>

    <el-container class="main-container">
      <header class="top-nav">
        <div class="breadcrumb-area">
          <span class="root-node">EcoSync Platform</span>
          <el-icon class="sep"><ArrowRight /></el-icon>
          <span class="current-node">{{ formatPath($route.path) }}</span>
        </div>
        <div class="system-status">
          <span class="status-dot"></span>
          Server Online: 2026-04-28
        </div>
      </header>

      <el-main class="main-content">
        <div class="view-wrapper">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import {
  DataLine, Box, UserFilled as UserGroup, Setting,
  SwitchButton, ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const currentAdmin = ref('Admin User')

onMounted(() => {
  currentAdmin.value = localStorage.getItem('username') || 'Admin User'
})

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

const formatPath = (path: string) => {
  const parts = path.split('/')
  const last = parts[parts.length - 1]
  if (!last) return 'Dashboard'
  return last.charAt(0).toUpperCase() + last.slice(1)
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #f4f7f6;
  overflow: hidden;
}

/* Sidebar Styling */
.brand-aside {
  background-color: #1a202c; /* Deep slate */
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 10px rgba(0,0,0,0.05);
  z-index: 100;
}

.aside-header {
  padding: 40px 24px;
  text-align: center;
}

.brand-logo {
  font-size: 28px;
  font-weight: 900;
  font-style: italic;
  letter-spacing: -1.5px;
  margin-bottom: 5px;
}
.c-orange { color: #ff7900; }
.c-red { color: #e2231a; }
.c-green { color: #007934; }

.platform-tag {
  font-size: 10px;
  color: #718096;
  text-transform: uppercase;
  letter-spacing: 2px;
  font-weight: 700;
}

.menu-wrapper {
  flex: 1;
  padding: 0 16px;
}

.menu-section-label {
  font-size: 11px;
  color: #4a5568;
  font-weight: 800;
  padding: 20px 20px 10px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.brand-menu {
  border: none !important;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  border-radius: 10px;
  margin-bottom: 4px;
  transition: all 0.3s;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255,255,255,0.05) !important;
  color: #fff !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(0, 121, 52, 0.2) 0%, rgba(0, 121, 52, 0) 100%) !important;
  color: #fff !important;
  font-weight: 700;
  position: relative;
}

/* The vertical brand stripe for active items */
:deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: -16px;
  top: 10%;
  height: 80%;
  width: 4px;
  background: linear-gradient(#ff7900, #007934, #e2231a);
  border-radius: 0 4px 4px 0;
}

/* Footer & Profile */
.aside-footer {
  padding: 24px;
  border-top: 1px solid rgba(255,255,255,0.05);
}

.admin-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.admin-avatar {
  background-color: #007934;
  font-weight: bold;
}

.admin-info {
  display: flex;
  flex-direction: column;
}

.admin-name {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.admin-role {
  color: #718096;
  font-size: 11px;
}

.logout-btn {
  width: 100%;
  justify-content: flex-start;
  color: #e53e3e !important;
  font-weight: 600;
  padding: 0;
}

/* Main Content Area */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-nav {
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  border-bottom: 1px solid #edf2f7;
}

.breadcrumb-area {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.root-node { color: #a0aec0; }
.sep { color: #cbd5e0; font-size: 12px; }
.current-node { color: #2d3748; font-weight: 700; }

.system-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #718096;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #48bb78;
  border-radius: 50%;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.2);
}

.main-content {
  padding: 24px;
  overflow-y: auto;
}

.view-wrapper {
  max-width: 1400px;
  margin: 0 auto;
}

/* Custom Scrollbar */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 10px;
}
</style>
