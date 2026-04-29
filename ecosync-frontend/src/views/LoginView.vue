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
            <div class="slogan">SMARTCHAIN RETAIL TECH</div>
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
                  :underline="false"
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

const handleSubmit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('請填寫完整信息')
  loading.value = true

  try {
    if (isRegister.value) {
      if (form.password !== form.rePassword) {
        loading.value = false
        return ElMessage.error('兩次輸入的密碼不一致')
      }
      await registerApi({
        username: form.username,
        passwordHash: form.password,
        role: 'CONSUMER'
      })
      ElMessage.success('註冊成功，請登錄')
      isRegister.value = false
    } else {
      const res = await loginApi(form) as any
      const resStr = String(res)

      if (resStr.includes('成功')) {
        const tokenMatch = resStr.match(/Token[:：]\s*([^,，\s]+)/)
        const token = tokenMatch ? tokenMatch[1].trim() : null

        if (token) {
          try {
            const base64Url = token.split('.')[1]
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
            const payload = JSON.parse(window.atob(base64))

            if (payload.status === 'BANNED') {
              loading.value = false
              return ElMessage.error('您的賬號已被封禁，請聯繫管理員處理')
            }

            localStorage.clear()
            localStorage.setItem('token', token)

            const finalRole = (payload.role || 'CONSUMER').toUpperCase()
            localStorage.setItem('role', finalRole)
            localStorage.setItem('username', payload.username)
            localStorage.setItem('userId', payload.userId)
            localStorage.setItem('balance', payload.balance || '0.00')

            ElMessage.success('歡迎回來！')

            if (finalRole === 'ADMIN') await router.push('/admin/accounts')
            else if (finalRole === 'EMPLOYEE') await router.push('/staff/home')
            else await router.push('/home')

          } catch (e) {
            console.error('Token解析錯誤:', e)
            ElMessage.error('身份驗證解析失敗，請聯繫開發人員')
          }
        } else {
          ElMessage.error('無法解析身份令牌(Token)')
        }
      } else {
        ElMessage.error(resStr.replace('登錄失敗: ', '') || '用戶名或密碼錯誤')
      }
    }
  } catch (error: any) {
    const errorMsg = error.response?.data || '服務器連接失敗'
    ElMessage.error(typeof errorMsg === 'string' ? errorMsg : '系統繁忙')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  background-color: #f4f7f5;
  background-image: radial-gradient(#d1d1d1 0.5px, transparent 0.5px);
  background-size: 30px 30px;
  position: relative;
  overflow: hidden;
}

.brand-top-stripe {
  height: 8px;
  width: 100%;
  background: linear-gradient(to right,
  #ff7900 0%, #ff7900 33.33%,
  #007934 33.33%, #007934 66.66%,
  #e2231a 66.66%, #e2231a 100%);
  position: fixed;
  top: 0;
  z-index: 10;
}

.login-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.login-card {
  width: 900px;
  max-width: 100%;
  border-radius: 20px;
  border: none;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.card-body {
  display: flex;
  min-height: 550px;
}

/* 修復後的左側裝飾區樣式 */
.brand-side {
  width: 40%;
  background: #002d14;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center; /* 水平居中核心 */
  text-align: center;   /* 文字居中核心 */
  color: white;
  position: relative;
  overflow: hidden;
}

.brand-logo {
  font-size: 48px;
  font-weight: 900;
  font-style: italic;
  letter-spacing: -2px;
  margin-bottom: 20px;
  z-index: 2;
  line-height: 1;
}

.c-orange { color: #ff7900; }
.c-red { color: #e2231a; }
.c-green { color: #007934; }

.platform-name {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin: 0;
  z-index: 2;
  width: 100%; /* 確保寬度撐滿以實現對齊 */
}

.slogan {
  margin-top: 15px;
  font-size: 14px;
  opacity: 0.6;
  letter-spacing: 1px;
  z-index: 2;
}

.store-decoration .circle-1 {
  position: absolute; bottom: -50px; right: -50px;
  width: 200px; height: 200px;
  background: rgba(0, 121, 52, 0.2);
  border-radius: 50%;
}

.store-decoration .circle-2 {
  position: absolute; top: -30px; left: -30px;
  width: 120px; height: 120px;
  background: rgba(255, 121, 0, 0.1);
  border-radius: 50%;
}

/* 右側表單區樣式保持不變 */
.form-side {
  flex: 1;
  background: white;
  padding: 60px 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header .title {
  font-size: 32px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 10px 0;
}

.form-header .subtitle {
  color: #888;
  font-size: 15px;
}

.title-underline {
  width: 50px;
  height: 5px;
  background: #ff7900;
  margin-top: 15px;
  border-radius: 10px;
}

.custom-form :deep(.el-form-item__label) {
  font-weight: 700;
  color: #333;
}

.brand-input :deep(.el-input__wrapper) {
  padding: 12px 15px;
  border-radius: 10px;
  background-color: #f9f9f9;
  box-shadow: none;
  border: 1px solid #eee;
}

.submit-btn {
  width: 100%;
  height: 50px;
  background-color: #007934 !important;
  border: none;
  font-weight: 800;
  border-radius: 10px;
}

.login-footer {
  padding: 30px;
  text-align: center;
  color: #aaa;
  font-size: 12px;
}

@media (max-width: 768px) {
  .brand-side { display: none; }
  .login-card { width: 100%; }
  .form-side { padding: 40px 30px; }
}
</style>
