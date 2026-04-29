import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'landing', component: () => import('../views/Landing.vue') },
    { path: '/login', name: 'login', component: LoginView },

    // --- CONSUMER ---
    { path: '/home', name: 'home', component: () => import('../views/consumer/Home.vue'), meta: { role: 'CONSUMER' } },
    { path: '/category', name: 'Category', component: () => import('../views/consumer/Category.vue'), meta: { role: 'CONSUMER' } },
    { path: '/cart', name: 'Cart', component: () => import('../views/consumer/Cart.vue'), meta: { role: 'CONSUMER' } },
    { path: '/order-status', name: 'OrderStatus', component: () => import('../views/consumer/OrderStatus.vue'), meta: { role: 'CONSUMER' } },
    { path: '/profile', name: 'Profile', component: () => import('../views/consumer/Profile.vue') },

    // --- ADMIN ---
    { path: '/admin', redirect: '/admin/accounts', meta: { role: 'ADMIN' } },
    { path: '/admin/accounts', name: 'AccountManage', component: () => import('../views/admin/AccountManage.vue'), meta: { role: 'ADMIN' } },
    { path: '/admin/inventory', name: 'Inventory', component: () => import('../views/admin/Inventory.vue'), meta: { role: 'ADMIN' } },
    { path: '/admin/dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { role: 'ADMIN' } },

    // 🌟 補上缺失的 AdminHome 路由，解決 Inventory.vue 跳轉報錯問題
    {
      path: '/admin/home',
      name: 'AdminHome',
      component: () => import('../views/admin/AdminHome.vue'),
      meta: { role: 'ADMIN' }
    },

    // --- STAFF ---
    { path: '/staff/home', name: 'staffHome', component: () => import('../views/staff/StaffHome.vue'), meta: { role: 'EMPLOYEE' } },
    // 獨立的店員 Profile 頁面
    { path: '/staff/profile', name: 'staffProfile', component: () => import('../views/staff/StaffProfile.vue'), meta: { role: 'EMPLOYEE' } }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const rawRole = localStorage.getItem('role')
  const isLogged = token && token !== 'null' && token !== 'undefined'

  // 1. 未登錄攔截
  if (!isLogged && !['login', 'landing'].includes(to.name as string)) {
    return { name: 'login' }
  }

  // 2. 已登錄重定向（防止重複進入登錄頁）
  if (isLogged && ['login', 'landing'].includes(to.name as string)) {
    const userRole = (rawRole || '').toUpperCase()
    if (userRole === 'ADMIN') return { name: 'AccountManage' }
    if (userRole === 'EMPLOYEE') return { name: 'staffHome' }
    return { name: 'home' }
  }

  // 3. 角色權限簡單校驗 (選填，增加安全性)
  const userRole = (rawRole || '').toUpperCase()
  if (to.meta.role && to.meta.role !== userRole) {
    console.error('Security Alert: Unauthorized access attempt.')
    if (userRole === 'ADMIN') return { name: 'AccountManage' }
    if (userRole === 'EMPLOYEE') return { name: 'staffHome' }
    return { name: 'home' }
  }
})

export default router
