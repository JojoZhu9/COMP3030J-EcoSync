<template>
  <div class="dashboard-container">
    <el-row :gutter="24">
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

    <el-row :gutter="24" class="mt-24">
      <el-col :span="14">
        <el-card shadow="never" class="modern-card">
          <template #header>
            <div class="card-header">
              <span class="header-text">User Demographics Distribution</span>
              <el-tag size="small" effect="plain" type="info" class="brand-tag">Global Stats</el-tag>
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
                :stroke-width="14"
                :show-text="false"
                class="brand-progress"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" class="modern-card system-info-card">
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
                <span>v8.0 (SMART_RETAIL_MAIN)</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="API Endpoint">
              <el-tag type="success" effect="dark" class="status-badge" round>
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
  { label: 'Administrators', value: 0, color: '#e2231a' },
  { label: 'Staff Members', value: 0, color: '#008163' },
  { label: 'Consumers', value: 0, color: '#EE7203' }
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
  } catch (error) {}
})
</script>

<style scoped>
.mt-24 { margin-top: 24px; }

.modern-card { border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 4px 20px rgba(0,0,0,0.03) !important; min-height: 400px; }
:deep(.modern-card .el-card__header) { padding: 20px 24px; border-bottom: 1px solid #f1f5f9; background: #fff; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-text { font-weight: 800; color: #1e293b; font-size: 16px; }
.brand-tag { font-weight: 800; border-radius: 8px; }

/* KPI Cards Styles */
.kpi-card { border: 1px solid rgba(0,0,0,0.04); border-radius: 16px; position: relative; overflow: hidden; transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); box-shadow: 0 4px 15px rgba(0,0,0,0.02) !important; }
.kpi-card:hover { transform: translateY(-5px); box-shadow: 0 10px 25px rgba(0,0,0,0.06) !important; }
.kpi-content { display: flex; justify-content: space-between; align-items: center; padding: 24px; }
.kpi-label { font-size: 11px; font-weight: 800; color: #64748b; letter-spacing: 1px; text-transform: uppercase; margin-bottom: 6px; }
.kpi-value { font-size: 36px; font-weight: 900; color: #1e293b; line-height: 1; }
.kpi-icon-box { width: 54px; height: 54px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 26px; }

/* Color coding for cards */
.kpi-0 .kpi-icon-box { background: #f0fdf4; color: #008163; }
.kpi-0 .kpi-footer-line { background: linear-gradient(90deg, #008163, #005a46); }

.kpi-1 .kpi-icon-box { background: #eff6ff; color: #3b82f6; }
.kpi-1 .kpi-footer-line { background: linear-gradient(90deg, #3b82f6, #1d4ed8); }

.kpi-2 .kpi-icon-box { background: #fff7ed; color: #EE7203; }
.kpi-2 .kpi-footer-line { background: linear-gradient(90deg, #EE7203, #c2410c); }

.kpi-3 .kpi-icon-box { background: #fef2f2; color: #e2231a; }
.kpi-3 .kpi-footer-line { background: linear-gradient(90deg, #e2231a, #b91c1c); }

.kpi-footer-line { height: 5px; width: 100%; position: absolute; bottom: 0; left: 0; }

.distribution-box { padding: 20px; }
.chart-row { margin-bottom: 36px; }
.row-info { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 15px; }
.role-label { font-weight: 800; color: #334155; }
.role-percentage { font-weight: 900; color: #1e293b; }
.brand-progress :deep(.el-progress-bar__outer) { background-color: #f1f5f9; border-radius: 8px; }
.brand-progress :deep(.el-progress-bar__inner) { border-radius: 8px; }

/* System Info Card */
.system-info-card { background: linear-gradient(145deg, #ffffff 0%, #f8fafc 100%); }
.live-pulse { display: flex; align-items: center; gap: 6px; font-size: 11px; font-weight: 900; color: #008163; letter-spacing: 1px; }
.pulse-dot { width: 8px; height: 8px; background: #008163; border-radius: 50%; box-shadow: 0 0 0 rgba(0, 129, 99, 0.4); animation: pulse 1.5s infinite; }
@keyframes pulse { 0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 129, 99, 0.7); } 70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(0, 129, 99, 0); } 100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 129, 99, 0); } }

.system-descriptions { padding: 10px; }
.db-logo { height: 22px; margin-right: 12px; opacity: 0.9; }
.db-info { display: flex; align-items: center; font-weight: 700; color: #334155; font-size: 13px; }
.status-badge { font-weight: 800; letter-spacing: 1px; padding: 0 14px; }
.timestamp { font-family: 'Courier New', Courier, monospace; font-weight: 800; color: #64748b; font-size: 13px; }

:deep(.el-descriptions__label) { background-color: #f8fafc !important; color: #64748b; font-weight: 800; width: 140px; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }
:deep(.el-descriptions__content) { color: #1e293b; font-weight: 600; font-size: 14px; }
</style>
