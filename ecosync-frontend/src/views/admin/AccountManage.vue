<template>
  <div class="account-container">
    <el-card>
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>系统用户账号管理 (Total: {{ users.length }})</span>
          <el-button type="primary" @click="showAdd = true">新增职员账号</el-button>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" stripe border>
        <el-table-column prop="user_id" label="UID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : (row.role === 'EMPLOYEE' ? 'primary' : 'info')">
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.phone_number === 'BANNED' ? 'danger' : 'success'">
              {{ row.phone_number === 'BANNED' ? '已封禁' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.phone_number === 'BANNED' ? 'success' : 'warning'"
              @click="handleBan(row)"
            >
              {{ row.phone_number === 'BANNED' ? '解封' : '封禁' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="handleKill(row)">注销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="创建新账号" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员 (ADMIN)" value="ADMIN" />
            <el-option label="员工 (EMPLOYEE)" value="EMPLOYEE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="createUser">立即创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)
const showAdd = ref(false)
const form = ref({ username: '', password: '', role: 'EMPLOYEE' })

const fetch = async () => {
  loading.value = true
  const res: any = await request.get('/users')
  users.value = res.data || res
  loading.value = false
}

const createUser = async () => {
  await request.post('/users', form.value)
  ElMessage.success('账号创建成功')
  showAdd.value = false
  fetch()
}

const handleBan = async (row: any) => {
  const isBanning = row.phone_number !== 'BANNED'
  row.phone_number = isBanning ? 'BANNED' : '13800000000'
  await request.put(`/users/${row.user_id}`, row)
  ElMessage.success(isBanning ? '账号已封禁' : '账号已解封')
  fetch()
}

const handleKill = (row: any) => {
  ElMessageBox.confirm('确定要彻底删除该账号吗？不可恢复！').then(async () => {
    await request.delete(`/users/${row.user_id}`)
    ElMessage.success('用户已注销')
    fetch()
  })
}

onMounted(fetch)
</script>
