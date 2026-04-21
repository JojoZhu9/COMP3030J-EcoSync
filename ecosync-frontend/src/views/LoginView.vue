<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="header">
        <h2 class="login-title">EcoSync {{ isRegister ? '账号注册' : '系统登录' }}</h2>
        <p class="subtitle">{{ isRegister ? '注册成为消费者，开启环保省钱之旅' : '欢迎回来，请登录您的账号' }}</p>
      </div>

      <el-form :model="form" @keyup.enter="handleSubmit" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock" />
        </el-form-item>

        <el-collapse-transition>
          <div v-if="isRegister">
            <el-form-item label="确认密码">
              <el-input v-model="form.rePassword" type="password" placeholder="请再次输入密码" show-password prefix-icon="CircleCheck" />
            </el-form-item>
          </div>
        </el-collapse-transition>

        <div class="actions">
          <el-button type="primary" @click="handleSubmit" :loading="loading" class="submit-btn">
            {{ isRegister ? '立即注册' : '登 录' }}
          </el-button>
        </div>

        <div class="switch-mode">
          <span>{{ isRegister ? '已有账号?' : '还没有账号?' }}</span>
          <el-link type="primary" underline="hover" @click="toggleMode" style="margin-left: 8px">
            {{ isRegister ? '去登录' : '立即注册' }}
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { loginApi, registerApi } from '../api/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { User, Lock, CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const isRegister = ref(false)
const form = reactive({ username: '', password: '', rePassword: '' })

const toggleMode = () => {
  isRegister.value = !isRegister.value
  form.username = ''; form.password = ''; form.rePassword = ''
}

const handleSubmit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请填写完整信息')
  loading.value = true

  try {
    if (isRegister.value) {
      if (form.password !== form.rePassword) {
        loading.value = false
        return ElMessage.error('两次输入的密码不一致')
      }
      await registerApi({
        username: form.username,
        passwordHash: form.password,
        role: 'CONSUMER'
      })
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
    } else {
      // 执行登录
      const res = await loginApi(form) as any
      // 强转字符串处理后端返回的 "登录成功，Token: xxx"
      const resStr = String(res)

      if (resStr.includes('成功')) {
        // 优化正则表达式：兼容中英文冒号、空格及逗号
        const tokenMatch = resStr.match(/Token[:：]\s*([^,，\s]+)/)
        const token = tokenMatch ? tokenMatch[1].trim() : null

        if (token) {
          try {
            // 解析 Token Payload
            const base64Url = token.split('.')[1]
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
            const payload = JSON.parse(window.atob(base64))

            if (payload.status === 'BANNED') {
              loading.value = false
              return ElMessage.error('您的账号已被封禁，请联系管理员处理')
            }

            // 清理并存储
            localStorage.clear()
            localStorage.setItem('token', token)

            const finalRole = (payload.role || 'CONSUMER').toUpperCase()
            localStorage.setItem('role', finalRole)
            localStorage.setItem('username', payload.username)
            localStorage.setItem('userId', payload.userId)
            localStorage.setItem('balance', payload.balance || '0.00')

            ElMessage.success('欢迎回来！')

            // 路由跳转
            if (finalRole === 'ADMIN') await router.push('/admin/accounts')
            else if (finalRole === 'EMPLOYEE') await router.push('/staff/home')
            else await router.push('/home')

          } catch (e) {
            console.error('Token解析错误:', e)
            ElMessage.error('身份验证解析失败，请联系开发人员')
          }
        } else {
          ElMessage.error('无法解析身份令牌(Token)')
        }
      } else {
        // 如果后端返回的是 "登录失败：xxx"，直接抛出该信息
        ElMessage.error(resStr.replace('登录失败: ', '') || '用户名或密码错误')
      }
    }
  } catch (error: any) {
    // 捕获 HTTP 错误（如 401, 404, 500）
    const errorMsg = error.response?.data || '服务器连接失败'
    ElMessage.error(typeof errorMsg === 'string' ? errorMsg : '系统繁忙')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); }
.login-card { width: 420px; padding: 30px; border-radius: 15px; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1); }
.header { text-align: center; margin-bottom: 30px; }
.login-title { margin: 0; color: #303133; font-size: 24px; }
.subtitle { margin-top: 8px; color: #909399; font-size: 14px; }
.submit-btn { width: 100%; height: 45px; font-size: 16px; margin-top: 10px; }
.switch-mode { margin-top: 20px; text-align: center; font-size: 14px; color: #606266; }
</style>
