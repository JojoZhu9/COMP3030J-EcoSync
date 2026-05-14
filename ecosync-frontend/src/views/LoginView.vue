<template>
  <div class="login-page">
    <div class="brand-top-stripe"></div>

    <div class="back-home-nav">
      <el-button link @click="router.push('/')">
        <el-icon><ArrowLeft /></el-icon> Back to Home page
      </el-button>
    </div>

    <div class="login-wrapper">
      <el-card class="login-card" :body-style="{ padding: '0px' }">
        <div class="card-body">
          <div class="brand-side">
            <div class="brand-logo">
              <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
            </div>
            <h3 class="platform-name">SMARTCHAIN RETAIL TECH</h3>
            <div class="slogan">Intelligent Near-Expiry Sales Platform</div>
          </div>

          <div class="form-side">
            <div class="form-header">
              <h2 class="title">{{ isRegister ? 'Join Us' : 'Welcome Back' }}</h2>
              <p class="subtitle">{{ isRegister ? 'Register to start your eco-saving journey' : 'Please login to manage your account' }}</p>
              <div class="title-underline"></div>
            </div>

            <el-form :model="form" @keyup.enter="handleSubmit" label-position="top">
              <el-form-item label="Username">
                <el-input v-model="form.username" placeholder="Enter username" :prefix-icon="User" />
              </el-form-item>
              <el-form-item label="Password">
                <el-input v-model="form.password" type="password" placeholder="Enter password" show-password :prefix-icon="Lock" />
              </el-form-item>

              <div v-if="isRegister">
                <el-form-item label="Confirm Password">
                  <el-input v-model="form.rePassword" type="password" placeholder="Repeat password" show-password :prefix-icon="CircleCheck" />
                </el-form-item>
              </div>

              <el-button type="success" @click="handleSubmit" :loading="loading" class="submit-btn">
                {{ isRegister ? 'REGISTER NOW' : 'LOGIN' }}
              </el-button>

              <div class="switch-mode">
                <span>{{ isRegister ? 'Already have an account?' : 'Need an account?' }}</span>
                <el-link type="primary" underline="never" @click="toggleMode" style="margin-left:5px">
                  {{ isRegister ? 'Log in' : 'Register now' }}
                </el-link>
              </div>
            </el-form>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { loginApi, registerApi } from '../api/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { User, Lock, CircleCheck, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const isRegister = ref(false)
const form = reactive({ username: '', password: '', rePassword: '' })

const toggleMode = () => { isRegister.value = !isRegister.value }

const parseJwt = (token: string) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
    return JSON.parse(jsonPayload)
  } catch (e) { return null }
}

const handleSubmit = async () => {
  if (!form.username || !form.password) return ElMessage.warning({ message: 'Please fill in all fields', duration: 1500 })
  loading.value = true
  try {
    if (isRegister.value) {
      if (form.password !== form.rePassword) return ElMessage.error({ message: 'Passwords do not match', duration: 1500 })
      await registerApi({ username: form.username, password: form.password, role: 'CONSUMER' })
      ElMessage.success({ message: 'Registration Successful!', duration: 1500 })
      isRegister.value = false
    } else {
      const res: any = await loginApi({ username: form.username, password: form.password })
      const resStr = String(res)
      if (resStr.includes('成功')) {
        const token = resStr.match(/Token[:：]\s*([^,，\s"}]+)/)?.[1]
        if (token) {
          const payload = parseJwt(token)
          localStorage.setItem('token', token)
          localStorage.setItem('role', (payload.role || 'CONSUMER').toUpperCase())
          localStorage.setItem('userId', String(payload.userId || payload.id))
          window.dispatchEvent(new Event('auth-change'))
          ElMessage.success({ message: 'Login Successful', duration: 1500 })

          // 修改点：登录成功后跳转到 Introduction (根路径)
          router.push('/')
        }
      } else {
        ElMessage.error({ message: 'Login Failed', duration: 1500 })
      }
    }
  } catch (error) {
    ElMessage.error({ message: 'Service Error', duration: 1500 })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { height: 100vh; width: 100vw; display: flex; flex-direction: column; background-color: #f4f7f5; position: relative; justify-content: center; align-items: center; }
.back-home-nav { position: absolute; top: 30px; left: 30px; z-index: 10; }
.brand-top-stripe { height: 8px; width: 100%; background: linear-gradient(to right, #ff7900 33%, #007934 33%, #007934 66%, #e2231a 66%); position: fixed; top: 0; }
.login-card { width: 850px; border-radius: 20px; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1); }
.card-body { display: flex; min-height: 500px; }
.brand-side { width: 35%; background: #002d14; padding: 40px; display: flex; flex-direction: column; justify-content: center; align-items: center; color: white; text-align: center; }
.brand-logo { font-size: 36px; font-weight: 900; font-style: italic; margin-bottom: 20px; }
.c-orange { color: #ff7900; } .c-red { color: #e2231a; } .c-green { color: #007934; }
.form-side { flex: 1; background: white; padding: 40px 60px; display: flex; flex-direction: column; justify-content: center; }
.title-underline { width: 40px; height: 4px; background: #007934; margin: 15px 0 25px; }
.submit-btn { width: 100%; height: 45px; background-color: #007934 !important; border: none; font-weight: bold; margin-top: 10px; }
.switch-mode { margin-top: 25px; text-align: center; font-size: 14px; }
</style>
