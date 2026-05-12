<template>
  <div class="login-page">
    <div class="brand-top-stripe"></div>

    <div class="login-wrapper">
      <el-card class="login-card" :body-style="{ padding: '0px' }">
        <div class="card-body">
          <div class="brand-side">
            <div class="brand-logo">
              <span class="c-orange">7</span><span class="c-red">-</span><span class="c-green">ELEVEn</span>
            </div>
            <h3 class="platform-name">SMARTCHAIN RETAIL TECH</h3>
            <div class="slogan">Intelligent Near-Expiry Sales Platform</div>
            <div class="store-decoration">
              <div class="circle-1"></div>
              <div class="circle-2"></div>
            </div>
          </div>

          <div class="form-side">
            <div class="form-header">
              <h2 class="title">{{ isRegister ? 'Join Us' : 'Welcome Back' }}</h2>
              <p class="subtitle">
                {{ isRegister ? 'Register to start your eco-saving journey' : 'Please login to manage your account' }}
              </p>
              <div class="title-underline"></div>
            </div>

            <el-form :model="form" @keyup.enter="handleSubmit" label-position="top" class="custom-form">
              <el-form-item label="Username">
                <el-input
                  v-model="form.username"
                  placeholder="Enter your username"
                  :prefix-icon="User"
                  class="brand-input"
                />
              </el-form-item>

              <el-form-item label="Password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="Enter your password"
                  show-password
                  :prefix-icon="Lock"
                  class="brand-input"
                />
              </el-form-item>

              <el-collapse-transition>
                <div v-if="isRegister">
                  <el-form-item label="Confirm Password">
                    <el-input
                      v-model="form.rePassword"
                      type="password"
                      placeholder="Repeat your password"
                      show-password
                      :prefix-icon="CircleCheck"
                      class="brand-input"
                    />
                  </el-form-item>
                </div>
              </el-collapse-transition>

              <div class="actions">
                <el-button
                  type="success"
                  @click="handleSubmit"
                  :loading="loading"
                  class="submit-btn"
                >
                  {{ isRegister ? 'REGISTER NOW' : 'LOGIN' }}
                </el-button>
              </div>

              <div class="switch-mode">
                <span>{{ isRegister ? 'Already have an account?' : 'Need an account?' }}</span>
                <el-link
                  type="primary"
                  underline="never"
                  @click="toggleMode"
                  class="switch-link"
                >
                  {{ isRegister ? 'Log in' : 'Register now' }}
                </el-link>
              </div>
            </el-form>
          </div>
        </div>
      </el-card>
    </div>

    <footer class="login-footer">
      © 2026 Bibilabo Group 8 | SMARTCHAIN RETAIL TECH for 7-ELEVEn
    </footer>
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

const parseJwt = (token: string) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
    return JSON.parse(jsonPayload)
  } catch (e) {
    return null
  }
}

const handleSubmit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('Please fill in all required fields')
  loading.value = true

  try {
    if (isRegister.value) {
      if (form.password !== form.rePassword) {
        loading.value = false
        return ElMessage.error('Passwords do not match')
      }

      const registerPayload = {
        username: form.username,
        password: form.password,
        passwordHash: form.password,
        rawPassword: form.password,
        role: 'CONSUMER'
      }

      const res = await registerApi(registerPayload)
      const resMsg = String(res)

      // Checking for success in response (Backend usually returns "成功" or "OK")
      if (resMsg.includes('成功') || resMsg.toLowerCase().includes('ok') || resMsg.includes('Success')) {
        ElMessage.success('Registration successful!')
        isRegister.value = false
      } else {
        ElMessage.error(resMsg || 'Registration failed')
      }

    } else {
      const res = await loginApi({
        username: form.username,
        password: form.password
      })

      const resStr = String(res)

      if (resStr.includes('成功') || resStr.toLowerCase().includes('success')) {
        const tokenMatch = resStr.match(/Token[:：]\s*([^,做\s"}]+)/)
        const token = tokenMatch ? tokenMatch[1].trim() : null

        if (token) {
          const payload = parseJwt(token)
          if (!payload) throw new Error('Failed to parse token')

          localStorage.setItem('token', token)
          localStorage.setItem('role', (payload.role || 'CONSUMER').toUpperCase())
          localStorage.setItem('username', payload.username || form.username)

          const userId = payload.userId || payload.id
          if (userId) {
            localStorage.setItem('userId', String(userId))
          }

          ElMessage.success('Login successful')
          const finalRole = (payload.role || 'CONSUMER').toUpperCase()

          if (finalRole === 'ADMIN') router.push('/admin/accounts')
          else if (finalRole === 'EMPLOYEE') router.push('/staff/home')
          else router.push('/home')
        } else {
          ElMessage.error('Access token not found')
        }
      } else {
        // Cleaning up potential Chinese prefix from backend error message
        const cleanMsg = resStr.replace('登录失败: ', '').replace('登录失败', 'Login failed')
        ElMessage.error(cleanMsg || 'Login failed')
      }
    }
  } catch (error: any) {
    console.error('Error:', error)
    const backendError = error.response?.data
    ElMessage.error(typeof backendError === 'string' ? backendError : 'Internal server error')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { height: 100vh; width: 100vw; display: flex; flex-direction: column; background-color: #f4f7f5; position: relative; overflow: hidden; }
.brand-top-stripe { height: 8px; width: 100%; background: linear-gradient(to right, #ff7900 33.33%, #007934 33.33%, #007934 66.66%, #e2231a 66.66%); position: fixed; top: 0; z-index: 100; }
.login-wrapper { flex: 1; display: flex; justify-content: center; align-items: center; padding: 20px; }
.login-card { width: 900px; border-radius: 20px; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1); }
.card-body { display: flex; min-height: 550px; }
.brand-side { width: 40%; background: #002d14; padding: 40px; display: flex; flex-direction: column; justify-content: center; align-items: center; color: white; position: relative; }
.brand-logo { font-size: 48px; font-weight: 900; font-style: italic; margin-bottom: 20px; }
.c-orange { color: #ff7900; } .c-red { color: #e2231a; } .c-green { color: #007934; }
.form-side { flex: 1; background: white; padding: 40px 60px; display: flex; flex-direction: column; justify-content: center; }
.platform-name { font-weight: 800; letter-spacing: 1px; margin-bottom: 5px; text-align: center; }
.slogan { font-size: 12px; opacity: 0.8; text-align: center; }
.title-underline { width: 40px; height: 4px; background: #007934; margin: 15px 0 25px; }
.submit-btn { width: 100%; height: 48px; background-color: #007934 !important; border: none; font-weight: bold; margin-top: 10px; }
.switch-mode { margin-top: 25px; text-align: center; font-size: 14px; }
.login-footer { padding: 20px; text-align: center; color: #999; font-size: 12px; }
</style>
