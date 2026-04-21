import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 将原本的重定向改为引入 Landing 页面
    { path: '/', name: 'landing', component: () => import('../views/Landing.vue') },
    { path: '/login', name: 'login', component: LoginView },

    // --- 用户端 (CONSUMER) ---
    { path: '/home', name: 'home', component: () => import('../views/consumer/Home.vue'), meta: { role: 'CONSUMER' } },
    { path: '/category', name: 'Category', component: () => import('../views/consumer/Category.vue'), meta: { role: 'CONSUMER' } },
    { path: '/cart', name: 'Cart', component: () => import('../views/consumer/Cart.vue'), meta: { role: 'CONSUMER' } },
    { path: '/order-status', name: 'OrderStatus', component: () => import('../views/consumer/OrderStatus.vue'), meta: { role: 'CONSUMER' } },
    { path: '/profile', name: 'Profile', component: () => import('../views/consumer/Profile.vue'), meta: { role: 'CONSUMER' } },

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

  // 核心判断逻辑：排除脏数据
  const isLogged = token && token !== 'null' && token !== 'undefined'

  // 定义不需要登录就能访问的白名单页面
  const publicPages = ['login', 'landing']

  // 1. 未登录强制拦截 (如果去的地方不在白名单里，统一去登录页)
  if (!isLogged && !publicPages.includes(to.name as string)) {
    return { name: 'login' }
  }

  // 2. 已登录禁止再去登录页或落地页，直接根据角色送去各自首页
  if (isLogged && publicPages.includes(to.name as string)) {
    const userRole = (rawRole || '').toUpperCase()
    if (userRole === 'ADMIN') return { name: 'AccountManage' }
    if (userRole === 'EMPLOYEE') return { name: 'staffHome' }
    return { name: 'home' }
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
