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
          text-color="#94a3b8"
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
          <el-avatar :size="36" class="admin-avatar">{{ currentAdmin.charAt(0).toUpperCase() }}</el-avatar>
          <div class="admin-info">
            <div class="admin-name">{{ currentAdmin }}</div>
            <div class="admin-role">Super Administrator</div>
          </div>
        </div>
        <el-button class="logout-btn" type="danger" plain @click="logout">
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
import { DataLine, Box, UserFilled as UserGroup, Setting, SwitchButton, ArrowRight } from '@element-plus/icons-vue'

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
.admin-layout { display: flex; height: 100vh; background-color: #f4f6f8; overflow: hidden; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }
.brand-aside { background-color: #0f172a; display: flex; flex-direction: column; box-shadow: 4px 0 24px rgba(0,0,0,0.1); z-index: 100; }
.aside-header { padding: 40px 24px 30px; text-align: center; }
.brand-logo { font-size: 28px; font-weight: 900; font-style: italic; letter-spacing: -1px; margin-bottom: 6px; }
.c-orange { color: #EE7203; } .c-red { color: #e2231a; } .c-green { color: #008163; }
.platform-tag { font-size: 11px; color: #64748b; text-transform: uppercase; letter-spacing: 2px; font-weight: 800; background: rgba(255,255,255,0.05); display: inline-block; padding: 4px 12px; border-radius: 12px; }
.menu-wrapper { flex: 1; padding: 0 16px; overflow-y: auto; }
.menu-section-label { font-size: 11px; color: #475569; font-weight: 800; padding: 24px 20px 10px; text-transform: uppercase; letter-spacing: 1px; }
.brand-menu { border: none !important; }
:deep(.el-menu-item) { height: 50px; line-height: 50px; border-radius: 12px; margin-bottom: 6px; transition: all 0.3s; font-weight: 600; }
:deep(.el-menu-item:hover) { background-color: rgba(255,255,255,0.08) !important; color: #fff !important; }
:deep(.el-menu-item.is-active) { background: linear-gradient(90deg, rgba(0, 129, 99, 0.25) 0%, rgba(0, 129, 99, 0) 100%) !important; color: #fff !important; font-weight: 800; position: relative; }
:deep(.el-menu-item.is-active)::before { content: ''; position: absolute; left: -16px; top: 15%; height: 70%; width: 5px; background: linear-gradient(180deg, #008163, #005a46); border-radius: 0 4px 4px 0; }
.aside-footer { padding: 24px; background: rgba(0,0,0,0.2); border-top: 1px solid rgba(255,255,255,0.05); }
.admin-profile { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; }
.admin-avatar { background: linear-gradient(135deg, #008163, #006b52); font-weight: 900; font-size: 16px; color: white; border: 2px solid rgba(255,255,255,0.1); }
.admin-info { display: flex; flex-direction: column; }
.admin-name { color: #f8fafc; font-size: 15px; font-weight: 700; }
.admin-role { color: #64748b; font-size: 12px; font-weight: 600; }
.logout-btn { width: 100%; border-radius: 10px; font-weight: 700; height: 40px; }
.main-container { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.top-nav { height: 70px; background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); display: flex; align-items: center; justify-content: space-between; padding: 0 32px; border-bottom: 1px solid #e2e8f0; z-index: 10; }
.breadcrumb-area { display: flex; align-items: center; gap: 10px; font-size: 15px; }
.root-node { color: #64748b; font-weight: 600; }
.sep { color: #cbd5e1; font-size: 14px; }
.current-node { color: #1e293b; font-weight: 800; }
.system-status { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 700; color: #475569; background: #f1f5f9; padding: 6px 14px; border-radius: 20px; }
.status-dot { width: 8px; height: 8px; background: #10b981; border-radius: 50%; box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2); animation: pulse 2s infinite; }
.main-content { padding: 32px; overflow-y: auto; }
.view-wrapper { max-width: 1400px; margin: 0 auto; }
@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); } 70% { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0); } 100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); } }
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
</style>
