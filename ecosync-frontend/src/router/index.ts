import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'landing', component: () => import('../views/Landing.vue') },
    { path: '/login', name: 'login', component: LoginView },

    // --- 用户端 (CONSUMER) ---
    { path: '/home', name: 'home', component: () => import('../views/consumer/Home.vue'), meta: { role: 'CONSUMER' } },
    { path: '/category', name: 'Category', component: () => import('../views/consumer/Category.vue'), meta: { role: 'CONSUMER' } },
    { path: '/cart', name: 'Cart', component: () => import('../views/consumer/Cart.vue'), meta: { role: 'CONSUMER' } },
    { path: '/order-status', name: 'OrderStatus', component: () => import('../views/consumer/OrderStatus.vue'), meta: { role: 'CONSUMER' } },
    // 💡 修复：删除了 profile 的 meta 角色限制，让员工也能访问
    { path: '/profile', name: 'Profile', component: () => import('../views/consumer/Profile.vue') },

    // --- 管理员 (ADMIN) ---
    { path: '/admin', redirect: '/admin/accounts', meta: { role: 'ADMIN' } },
    { path: '/admin/accounts', name: 'AccountManage', component: () => import('../views/admin/AccountManage.vue'), meta: { role: 'ADMIN' } },
    { path: '/admin/inventory', name: 'Inventory', component: () => import('../views/admin/Inventory.vue'), meta: { role: 'ADMIN' } },
    { path: '/admin/dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { role: 'ADMIN' } },
    { path: '/admin/home', name: 'AdminHome', component: () => import('../views/admin/AdminHome.vue'), meta: { role: 'ADMIN' } },

    // --- 员工 (STAFF) ---
    { path: '/staff/home', name: 'staffHome', component: () => import('../views/staff/StaffHome.vue'), meta: { role: 'EMPLOYEE' } }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const rawRole = localStorage.getItem('role')

  const isLogged = token && token !== 'null' && token !== 'undefined'
  const publicPages = ['login', 'landing']

  // 1. 未登录强制拦截
  if (!isLogged && !publicPages.includes(to.name as string)) {
    return { name: 'login' }
  }

  // 2. 已登录禁止再去登录页或落地页
  if (isLogged && publicPages.includes(to.name as string)) {
    const userRole = (rawRole || '').toUpperCase()
    if (userRole === 'ADMIN') return { name: 'AccountManage' }
    if (userRole === 'EMPLOYEE') return { name: 'staffHome' }
    return { name: 'home' }
  }

  // 💡 修复：让个人主页对所有已登录用户无条件放行
  if (to.name === 'Profile') {
    return true
  }

  // 3. 权限越权拦截
  if (to.meta.role) {
    const userRole = (rawRole || '').toUpperCase()
    if (to.meta.role !== userRole) {
      if (userRole === 'ADMIN') return { name: 'AccountManage' }
      if (userRole === 'EMPLOYEE') return { name: 'staffHome' }
      return { name: 'home' }
    }
  }
})

export default router
