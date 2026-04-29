<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in summary" :key="item.title">
        <el-card shadow="never" class="kpi-card" :class="'kpi-' + index">
          <div class="kpi-content">
            <div class="kpi-info">
              <div class="kpi-label">{{ item.title }}</div>
              <div class="kpi-value">{{ item.value }}</div>
            </div>
            <div class="kpi-icon-box">
              <el-icon v-if="index === 0"><Box /></el-icon>
              <el-icon v-else-if="index === 1"><User /></el-icon>
              <el-icon v-else-if="index === 2"><Timer /></el-icon>
              <el-icon v-else><Warning /></el-icon>
            </div>
          </div>
          <div class="kpi-footer-line"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-24">
      <el-col :span="14">
        <el-card shadow="never" class="stat-card">
          <template #header>
            <div class="card-header">
              <span class="header-text">User Demographics Distribution</span>
              <el-tag size="small" effect="plain" type="info">Global Stats</el-tag>
            </div>
          </template>
          <div class="distribution-box">
            <div v-for="r in roleStats" :key="r.label" class="chart-row">
              <div class="row-info">
                <span class="role-label">{{ r.label }}</span>
                <span class="role-percentage">{{ r.value }}%</span>
              </div>
              <el-progress
                :percentage="r.value"
                :color="r.color"
                :stroke-width="12"
                :show-text="false"
                class="brand-progress"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" class="stat-card system-info-card">
          <template #header>
            <div class="card-header">
              <span class="header-text">System Infrastructure</span>
              <div class="live-pulse">
                <span class="pulse-dot"></span> LIVE
              </div>
            </div>
          </template>
          <el-descriptions :column="1" border class="system-descriptions">
            <el-descriptions-item label="Database Engine">
              <div class="db-info">
                <img src="https://www.mysql.com/common/logos/logo-mysql-170x115.png" alt="MySQL" class="db-logo" />
                <span>v8.0 (SMARTCHAIN_RETAIL_TECH_Main)</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="API Endpoint">
              <el-tag type="success" effect="dark" class="status-badge">
                <el-icon><Check /></el-icon> OPERATIONAL
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Node Location">7-11 HK Digital Hub</el-descriptions-item>
            <el-descriptions-item label="Last Pulse">
              <span class="timestamp">{{ new Date().toLocaleString() }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Box, User, Timer, Warning, Check } from '@element-plus/icons-vue'

const summary = ref([
  { title: 'MASTER PRODUCTS', value: 0 },
  { title: 'REGISTERED USERS', value: 0 },
  { title: 'ACTIVE STAFF TODAY', value: 3 },
  { title: 'SYSTEM ALERTS', value: 12 }
])

const roleStats = ref([
  { label: 'Administrators', value: 0, color: '#e2231a' }, // 7-11 Red
  { label: 'Staff Members', value: 0, color: '#007934' }, // 7-11 Green
  { label: 'Consumers', value: 0, color: '#ff7900' }      // 7-11 Orange
])

onMounted(async () => {
  try {
    const [p, u]: any = await Promise.all([request.get('/products'), request.get('/users')])
    const pList = p.data || p
    const uList = u.data || u

    summary.value[0].value = pList.length
    summary.value[1].value = uList.length

    const total = uList.length || 1
    roleStats.value[0].value = Math.round((uList.filter((x:any) => x.role === 'ADMIN').length / total) * 100)
    roleStats.value[1].value = Math.round((uList.filter((x:any) => x.role === 'EMPLOYEE').length / total) * 100)
    roleStats.value[2].value = Math.round((uList.filter((x:any) => x.role === 'CONSUMER').length / total) * 100)
  } catch (error) {
    console.error('Failed to sync dashboard data', error)
  }
})
</script>

<style scoped>
.mt-24 { margin-top: 24px; }

/* KPI Cards Styles */
.kpi-card {
  border: none;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.kpi-card:hover { transform: translateY(-5px); }

.kpi-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.kpi-label {
  font-size: 11px;
  font-weight: 800;
  color: #94a3b8;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.kpi-value {
  font-size: 32px;
  font-weight: 900;
  color: #1e293b;
  margin-top: 4px;
}

.kpi-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

/* Color coding for cards */
.kpi-0 .kpi-icon-box { background: #f0fdf4; color: #007934; }
.kpi-0 .kpi-footer-line { background: #007934; }

.kpi-1 .kpi-icon-box { background: #eff6ff; color: #3b82f6; }
.kpi-1 .kpi-footer-line { background: #3b82f6; }

.kpi-2 .kpi-icon-box { background: #fff7ed; color: #ff7900; }
.kpi-2 .kpi-footer-line { background: #ff7900; }

.kpi-3 .kpi-icon-box { background: #fef2f2; color: #e2231a; }
.kpi-3 .kpi-footer-line { background: #e2231a; }

.kpi-footer-line {
  height: 4px;
  width: 100%;
  position: absolute;
  bottom: 0;
  left: 0;
  opacity: 0.6;
}

/* Stat Cards */
.stat-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text {
  font-weight: 800;
  color: #1e293b;
  font-size: 15px;
}

.distribution-box {
  padding: 10px 0;
}

.chart-row {
  margin-bottom: 32px;
}

.row-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.role-label { font-weight: 700; color: #475569; }
.role-percentage { font-weight: 900; color: #1e293b; }

.brand-progress :deep(.el-progress-bar__outer) {
  background-color: #f1f5f9;
  border-radius: 6px;
}

/* System Info Card */
.system-info-card {
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 100%);
}

.live-pulse {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 800;
  color: #007934;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: #007934;
  border-radius: 50%;
  box-shadow: 0 0 0 rgba(0, 121, 52, 0.4);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 121, 52, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(0, 121, 52, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 121, 52, 0); }
}

.db-logo { height: 20px; margin-right: 10px; opacity: 0.8; }
.db-info { display: flex; align-items: center; font-weight: 600; color: #334155; }

.status-badge {
  font-weight: 800;
  letter-spacing: 1px;
  padding: 0 12px;
}

.timestamp {
  font-family: 'Courier New', Courier, monospace;
  font-weight: bold;
  color: #64748b;
}

:deep(.el-descriptions__label) {
  background-color: #f8fafc !important;
  color: #64748b;
  font-weight: 700;
  width: 140px;
}

:deep(.el-descriptions__content) {
  color: #1e293b;
  font-weight: 500;
}
</style>
