<template>
  <div class="login-page">
    <div class="brand-top-stripe"></div>

    <div class="back-home-nav">
      <el-button link @click="router.push('/')">
        <el-icon><ArrowLeft /></el-icon> {{ $t('nav.backToHome') }}
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
            <div class="slogan">{{ $t('login.slogan') }}</div>
          </div>

          <div class="form-side">
            <div class="form-header">
              <h2 class="title">{{ isRegister ? $t('login.joinUs') : $t('login.welcomeBack') }}</h2>
              <p class="subtitle">{{ isRegister ? $t('login.registerSubtitle') : $t('login.loginSubtitle') }}</p>
              <div class="title-underline"></div>
            </div>

            <el-form :model="form" @keyup.enter="handleSubmit" label-position="top">
              <el-form-item :label="$t('login.username')">
                <el-input v-model="form.username" :placeholder="$t('login.usernamePlaceholder')" :prefix-icon="User" />
              </el-form-item>
              <el-form-item :label="$t('login.password')">
                <el-input v-model="form.password" type="password" :placeholder="$t('login.passwordPlaceholder')" show-password :prefix-icon="Lock" />
              </el-form-item>

              <div v-if="isRegister">
                <el-form-item :label="$t('login.confirmPassword')">
                  <el-input v-model="form.rePassword" type="password" :placeholder="$t('login.confirmPasswordPlaceholder')" show-password :prefix-icon="CircleCheck" />
                </el-form-item>
                <el-form-item :label="$t('login.phoneNumber')">
                  <el-input v-model="form.phone" :placeholder="$t('login.phonePlaceholder')" maxlength="11" :prefix-icon="Phone" />
                </el-form-item>
                <el-form-item :label="$t('login.address')">
                  <el-input v-model="form.address" :placeholder="$t('login.addressPlaceholder')" :prefix-icon="MapLocation" />
                </el-form-item>
              </div>

              <el-button type="success" @click="handleSubmit" :loading="loading" class="submit-btn">
                {{ isRegister ? $t('login.registerBtn') : $t('login.loginBtn') }}
              </el-button>

              <div class="switch-mode">
                <span>{{ isRegister ? $t('login.hasAccount') : $t('login.noAccount') }}</span>
                <el-link type="primary" underline="never" @click="toggleMode" style="margin-left:5px">
                  {{ isRegister ? $t('login.logInLink') : $t('login.registerLink') }}
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
import { useI18n } from 'vue-i18n'
import { loginApi, registerApi } from '../api/user'
import { ElMessage } from '@/utils/message'
import { useRouter } from 'vue-router'
import { User, Lock, CircleCheck, ArrowLeft, Phone, MapLocation } from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()
const loading = ref(false)
const isRegister = ref(false)
const form = reactive({ username: '', password: '', rePassword: '', phone: '', address: '' })

const toggleMode = () => { isRegister.value = !isRegister.value }

const parseJwt = (token: string) => {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const base64Url = parts[1]!
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
    return JSON.parse(jsonPayload)
  } catch (e) { return null }
}

const handleSubmit = async () => {
  if (!form.username || !form.password) return ElMessage.warning({ message: t('login.validation.fillAll'), duration: 1500 })
  loading.value = true
  try {
    if (isRegister.value) {
      if (form.password !== form.rePassword) {
        loading.value = false
        return ElMessage.error({ message: t('login.validation.passwordMismatch'), duration: 1500 })
      }
      if (form.password.length < 6) {
        loading.value = false
        return ElMessage.error({ message: t('login.validation.passwordLength'), duration: 1500 })
      }
      if (!/^\d{11}$/.test(form.phone)) {
        loading.value = false
        return ElMessage.error({ message: t('login.validation.phoneInvalid'), duration: 1500 })
      }
      if (!form.address.trim()) {
        loading.value = false
        return ElMessage.error({ message: t('login.validation.addressRequired'), duration: 1500 })
      }
      await registerApi({ username: form.username, password: form.password, role: 'CONSUMER', phone: form.phone, address: form.address })
      ElMessage.success({ message: t('login.success.register'), duration: 1500 })
      form.password = ''; form.rePassword = ''
      isRegister.value = false
    } else {
      const res: any = await loginApi({ username: form.username, password: form.password })
      const token = res?.token
      if (token) {
        const payload = parseJwt(token)
        localStorage.setItem('token', token)
        localStorage.setItem('role', (payload.role || 'CONSUMER').toUpperCase())
        localStorage.setItem('userId', String(payload.userId || payload.id))
        window.dispatchEvent(new Event('auth-change'))
        ElMessage.success({ message: t('login.success.login'), duration: 1500 })
        router.push('/')
      } else {
        ElMessage.error({ message: t('login.error.loginFailed'), duration: 1500 })
      }
    }
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || t('message.error')
    ElMessage.error({ message: msg, duration: 2000 })
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
