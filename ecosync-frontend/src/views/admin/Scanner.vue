<template>
  <div class="page-container">
    <el-card header="管理员个人安全设置" style="max-width: 500px">
      <el-form label-position="top">
        <el-form-item label="当前管理员 ID">
          <el-input :value="userId" disabled />
        </el-form-item>
        <el-form-item label="修改登录密码">
          <el-input v-model="newPass" type="password" show-password placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="系统公告权限">
          <el-switch v-model="hasNotice" />
        </el-form-item>
        <el-button type="danger" plain @click="handleUpdate">保存配置修改</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const userId = localStorage.getItem('userId')
const newPass = ref('')
const hasNotice = ref(true)

const handleUpdate = () => {
  ElMessage.success('管理员安全配置已更新')
}
</script>
