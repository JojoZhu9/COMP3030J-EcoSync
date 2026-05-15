<template>
  <div class="settings-container">
    <el-card class="modern-card security-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-main">
            <div class="title-icon"><el-icon><Lock /></el-icon></div>
            <div class="title-text-box">
              <span class="main-title">Admin Security Console</span>
              <span class="sub-title">System access & privileges management</span>
            </div>
          </div>
          <el-tag size="small" effect="dark" class="level-tag" round>Level: SuperUser</el-tag>
        </div>
      </template>

      <div class="security-brand-line"></div>

      <el-form label-position="top" class="settings-form">
        <div class="form-section-readonly">
          <el-form-item label="Active Administrator ID">
            <el-input :value="userId" disabled class="id-input modern-input">
              <template #prefix><el-icon><UserFilled /></el-icon></template>
            </el-input>
            <p class="input-hint">Unique system identifier for audit logs.</p>
          </el-form-item>
        </div>

        <el-divider border-style="dashed" />

        <el-form-item label="Update Access Credentials">
          <el-input
            v-model="newPass"
            type="password"
            show-password
            placeholder="Enter new secure password (leave blank to keep current)"
            class="brand-input modern-input"
          >
            <template #prefix><el-icon><Key /></el-icon></template>
          </el-input>
        </el-form-item>

        <div class="permission-box">
          <div class="permission-info">
            <span class="p-title">System Broadcast Privilege</span>
            <span class="p-desc">Allow this account to push global shelf notifications.</span>
          </div>
          <el-switch
            v-model="hasNotice"
            style="--el-switch-on-color: #008163; --el-switch-off-color: #e2e8f0"
          />
        </div>

        <div class="action-wrapper">
          <el-button type="success" class="save-btn" @click="handleUpdate">
            UPDATE CORE CONFIGURATION
          </el-button>
          <p class="security-note">
            <el-icon><InfoFilled /></el-icon> All changes are logged for security compliance.
          </p>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, UserFilled, Key, InfoFilled } from '@element-plus/icons-vue'

const userId = localStorage.getItem('userId') || 'ADMIN_001'
const newPass = ref('')
const hasNotice = ref(true)

const handleUpdate = () => {
  ElMessage({
    message: 'Administrator security profile synchronized successfully.',
    type: 'success',
    duration: 3000
  })
}
</script>

<style scoped>
.settings-container { display: flex; justify-content: center; padding-top: 40px; }

.modern-card { border-radius: 20px; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 10px 40px rgba(0,0,0,0.04) !important; background: #fff; overflow: hidden; width: 100%; max-width: 600px; }
:deep(.modern-card .el-card__header) { padding: 24px 30px; border-bottom: none; }
:deep(.modern-card .el-card__body) { padding: 0 30px 30px; }

.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-main { display: flex; align-items: center; gap: 14px; }
.title-icon { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #008163, #005a46); color: white; display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: 0 4px 12px rgba(0,129,99,0.2); }
.title-text-box { display: flex; flex-direction: column; }
.main-title { font-size: 18px; font-weight: 800; color: #1e293b; }
.sub-title { font-size: 12px; color: #94a3b8; margin-top: 2px; }

.level-tag { background-color: #1e293b !important; border: none; font-weight: 800; font-size: 11px; padding: 0 12px; }

.security-brand-line { height: 4px; width: 100%; background: linear-gradient(to right, #ff7900 33%, #008163 33%, #008163 66%, #e2231a 66%); margin-bottom: 25px; border-radius: 2px; }

/* Read-only Block */
.form-section-readonly { background-color: #f8fafc; padding: 24px; border-radius: 16px; border: 1px solid #f1f5f9; }

:deep(.el-form-item__label) { font-weight: 800; color: #475569; font-size: 12px; text-transform: uppercase; margin-bottom: 8px !important; letter-spacing: 0.5px; }

.modern-input :deep(.el-input__wrapper) { padding: 12px 16px; border-radius: 10px; background: #fff; box-shadow: 0 0 0 1px #e2e8f0 inset; }
.modern-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px rgba(0,129,99,0.4) inset; }

.input-hint { font-size: 12px; color: #94a3b8; margin-top: 10px; font-weight: 600; display: flex; align-items: center; gap: 4px; }

/* Permission Switch Box */
.permission-box { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; margin: 30px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.permission-info { display: flex; flex-direction: column; gap: 4px; }
.p-title { font-weight: 800; color: #1e293b; font-size: 14px; }
.p-desc { font-size: 12px; color: #64748b; font-weight: 500; }

/* Action Area */
.action-wrapper { margin-top: 40px; text-align: center; }
.save-btn { width: 100%; height: 54px; font-weight: 900; font-size: 14px; letter-spacing: 1px; border-radius: 12px; background: linear-gradient(135deg, #008163, #005a46) !important; border: none; box-shadow: 0 8px 20px rgba(0, 129, 99, 0.25); transition: all 0.3s; }
.save-btn:hover { transform: translateY(-2px); box-shadow: 0 12px 25px rgba(0, 129, 99, 0.35); }

.security-note { margin-top: 16px; font-size: 12px; color: #94a3b8; font-weight: 600; display: flex; align-items: center; justify-content: center; gap: 6px; }

:deep(.el-divider) { margin: 30px 0; border-color: #e2e8f0; }
</style>
