<template>
  <div class="settings-container">
    <el-card class="security-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-main">
            <el-icon class="c-green"><Lock /></el-icon>
            <span>Admin Security Console</span>
          </div>
          <el-tag size="small" effect="dark" class="level-tag">Level: SuperUser</el-tag>
        </div>
      </template>

      <div class="security-brand-line"></div>

      <el-form label-position="top" class="settings-form">
        <div class="form-section-readonly">
          <el-form-item label="Active Administrator ID">
            <el-input :value="userId" disabled class="id-input">
              <template #prefix><el-icon><UserFilled /></el-icon></template>
            </el-input>
            <p class="input-hint">Unique system identifier for audit logs.</p>
          </el-form-item>
        </div>

        <el-divider />

        <el-form-item label="Update Access Credentials">
          <el-input
            v-model="newPass"
            type="password"
            show-password
            placeholder="Enter new secure password (leave blank to keep current)"
            class="brand-input"
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
            active-color="#007934"
            inactive-color="#e2e8f0"
          />
        </div>

        <div class="action-wrapper">
          <el-button type="success" class="save-btn" @click="handleUpdate">
            Update Core Configuration
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
  // Logic: Only log success if pass is updated or state changed
  ElMessage({
    message: 'Administrator security profile synchronized successfully.',
    type: 'success',
    duration: 3000
  })
}
</script>

<style scoped>
.settings-container {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.security-card {
  width: 100%;
  max-width: 550px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  overflow: hidden;
  position: relative;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
  text-transform: uppercase;
  font-size: 14px;
  letter-spacing: 0.5px;
  color: #1e293b;
}

.level-tag {
  background-color: #1e293b !important;
  border: none;
  font-weight: 700;
  font-size: 10px;
}

.security-brand-line {
  height: 4px;
  width: 100%;
  background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%);
  margin-bottom: 25px;
}

.settings-form {
  padding: 0 10px;
}

/* Read-only Block */
.form-section-readonly {
  background-color: #f8fafc;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
}

:deep(.el-form-item__label) {
  font-weight: 800;
  color: #475569;
  font-size: 12px;
  text-transform: uppercase;
  margin-bottom: 8px !important;
}

.id-input :deep(.el-input__wrapper) {
  background-color: #fff;
  box-shadow: none !important;
  border: 1px solid #e2e8f0;
}

.input-hint {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 8px;
  font-style: italic;
}

/* Custom Input Styling */
.brand-input :deep(.el-input__wrapper) {
  padding: 10px 15px;
  border-radius: 8px;
}

/* Permission Switch Box */
.permission-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #fdfdfd;
  border: 1px solid #edf2f7;
  border-radius: 12px;
  margin: 25px 0;
}

.permission-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.p-title {
  font-weight: 700;
  color: #1e293b;
  font-size: 14px;
}

.p-desc {
  font-size: 12px;
  color: #64748b;
}

/* Action Area */
.action-wrapper {
  margin-top: 40px;
  text-align: center;
}

.save-btn {
  width: 100%;
  height: 50px;
  font-weight: 800;
  font-size: 15px;
  border-radius: 10px;
  background-color: #007934 !important;
  border: none;
  box-shadow: 0 8px 20px rgba(0, 121, 52, 0.25);
  transition: all 0.3s;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(0, 121, 52, 0.35);
}

.security-note {
  margin-top: 15px;
  font-size: 11px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.c-green { color: #007934; }

:deep(.el-divider) {
  margin: 30px 0;
}
</style>
