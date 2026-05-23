<template>
  <div class="staff-profile-page">
    <div class="card-wrapper" v-loading="loading">

      <div class="id-card">
        <div class="id-card-header">
          <div class="brand-logo">7-ELEVEn</div>
          <div class="badge-tag">{{ $t('staff.profile.staffIdentity') }}</div>
        </div>

        <div class="id-card-body">
          <div class="avatar-section">
            <el-avatar :size="90" src="https://api.dicebear.com/7.x/avataaars/svg?seed=Staff&backgroundColor=f1f5f9" class="staff-avatar" />
            <div class="role-badge"><el-icon><UserFilled /></el-icon></div>
          </div>

          <div class="user-main-info">
            <h2 class="user-name">{{ staff.username || $t('staff.profile.loading') }}</h2>
            <div class="emp-id">{{ $t('staff.profile.employeeId') }}: <span>#{{ staff.userId || staff.user_id || '---' }}</span></div>
            <el-tag type="success" effect="dark" round class="role-display">{{ staff.role || 'STAFF' }}</el-tag>
          </div>

          <div class="divider"></div>

          <div class="store-info-section">
            <div class="info-group">
              <span class="info-label">{{ $t('staff.profile.assignedStore') }}</span>
              <span class="info-val store-highlight">
                <el-icon><Shop /></el-icon>
                {{ store.storeName || store.store_name || $t('staff.profile.loading') }}
              </span>
            </div>

            <div class="info-row">
              <div class="info-group half">
                <span class="info-label">{{ $t('staff.profile.city') }}</span>
                <span class="info-val">{{ store.city || '---' }}</span>
              </div>
              <div class="info-group half">
                <span class="info-label">{{ $t('staff.profile.status') }}</span>
                <span class="info-val success-text">{{ $t('staff.profile.active') }}</span>
              </div>
            </div>

            <div class="info-group">
              <span class="info-label">{{ $t('staff.profile.fullAddress') }}</span>
              <span class="info-val address-text">
                <el-icon><Location /></el-icon> {{ store.address || '---' }}
              </span>
            </div>
          </div>
        </div>

        <div class="id-card-footer">
          <div class="barcode-bars">|| █| || █||| █ |||</div>
          <div class="barcode-num">ID: {{ staff.userId || '0000' }} - {{ $t('staff.profile.securePass') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { UserFilled, Location, Shop } from '@element-plus/icons-vue'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(true)
const staff = ref<any>({})
const store = ref<any>({})

const fetchData = async () => {
  loading.value = true
  const uid = localStorage.getItem('userId')

  try {
    const uRes: any = await request.get(`/users/${uid}`)
    const userData = uRes.data || uRes
    staff.value = userData

    const sid = userData.storeId || userData.store_id

    if (sid) {
      const sRes: any = await request.get(`/stores/${sid}`)
      store.value = sRes.data || sRes
    }
  } catch (e) {
    console.error("Fetch profile failed", e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.staff-profile-page {
  background: #f4f6f8;
  min-height: calc(100vh - 60px);
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.card-wrapper {
  width: 100%;
  max-width: 460px;
}

/* 工牌整体样式 */
.id-card {
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(0,0,0,0.08);
  border: 1px solid rgba(0,0,0,0.05);
  position: relative;
}

/* 顶部品牌条 */
.id-card-header {
  height: 80px;
  background: linear-gradient(135deg, #008163 0%, #005a46 100%);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px 24px;
  color: white;
}
.brand-logo { font-size: 20px; font-weight: 900; letter-spacing: 1px; }
.badge-tag { background: rgba(255,255,255,0.2); padding: 4px 10px; border-radius: 12px; font-size: 10px; font-weight: 800; letter-spacing: 1px; }

/* 主体内容区 */
.id-card-body {
  padding: 0 30px 30px;
  position: relative;
  margin-top: -40px;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  justify-content: center;
  position: relative;
  margin-bottom: 16px;
}
.staff-avatar {
  border: 6px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  background: #fff;
}
.role-badge {
  position: absolute;
  bottom: 4px;
  right: calc(50% - 40px);
  width: 24px;
  height: 24px;
  background: #008163;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
  font-size: 12px;
}

/* 员工信息 */
.user-main-info { text-align: center; margin-bottom: 24px; }
.user-name { font-size: 24px; font-weight: 900; color: #1e293b; margin: 0 0 6px 0; letter-spacing: -0.5px; }
.emp-id { font-size: 13px; color: #64748b; font-weight: 600; margin-bottom: 12px; }
.emp-id span { font-family: monospace; font-size: 14px; color: #1e293b; font-weight: 800; }
.role-display { font-weight: 800; letter-spacing: 1px; padding: 0 16px; }

.divider { height: 1px; background: #e2e8f0; margin: 0 0 24px 0; }

/* 店铺信息 */
.store-info-section { display: flex; flex-direction: column; gap: 16px; }
.info-row { display: flex; gap: 16px; }
.info-group { display: flex; flex-direction: column; gap: 6px; }
.info-group.half { flex: 1; }
.info-label { font-size: 11px; font-weight: 800; color: #94a3b8; text-transform: uppercase; letter-spacing: 1px; }
.info-val { font-size: 14px; font-weight: 600; color: #334155; display: flex; align-items: center; gap: 6px; background: #f8fafc; padding: 10px 14px; border-radius: 12px; border: 1px solid #f1f5f9; }
.store-highlight { color: #008163; font-weight: 800; background: #f0fdf4; border-color: #dcfce7; }
.success-text { color: #008163; font-weight: 800; }
.address-text { line-height: 1.4; font-size: 13px; }

/* 底部条码 */
.id-card-footer {
  background: #f8fafc;
  padding: 24px;
  text-align: center;
  border-top: 1px dashed #cbd5e1;
}
.barcode-bars { font-size: 32px; letter-spacing: 2px; line-height: 1; color: #1e293b; opacity: 0.8; margin-bottom: 8px; }
.barcode-num { font-family: 'Courier New', Courier, monospace; font-size: 11px; letter-spacing: 2px; color: #64748b; font-weight: bold; }
</style>
