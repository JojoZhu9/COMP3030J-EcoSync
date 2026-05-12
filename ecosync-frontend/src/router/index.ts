import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 将根路径设为介绍页，meta.role 设为可选
    { path: '/', name: 'landing', component: () => import('../views/Landing.vue'), meta: { requiresAuth: false } },
    { path: '/login', name: 'login', component: LoginView, meta: { requiresAuth: false } },

    // --- 消费者/游客 栏目 ---
    { path: '/home', name: 'home', component: () => import('../views/consumer/Home.vue'), meta: { requiresAuth: false, role: 'CONSUMER' } },
    { path: '/category', name: 'Category', component: () => import('../views/consumer/Category.vue'), meta: { requiresAuth: false, role: 'CONSUMER' } },

    // 需登录项
    { path: '/cart', name: 'Cart', component: () => import('../views/consumer/Cart.vue'), meta: { requiresAuth: true, role: 'CONSUMER' } },
    { path: '/order-status', name: 'OrderStatus', component: () => import('../views/consumer/OrderStatus.vue'), meta: { requiresAuth: true, role: 'CONSUMER' } },
    { path: '/profile', name: 'Profile', component: () => import('../views/consumer/Profile.vue'), meta: { requiresAuth: true, role: 'CONSUMER' } },

    // --- ADMIN ---
    { path: '/admin/accounts', name: 'AccountManage', component: () => import('../views/admin/AccountManage.vue'), meta: { requiresAuth: true, role: 'ADMIN' } },
    { path: '/admin/inventory', name: 'Inventory', component: () => import('../views/admin/Inventory.vue'), meta: { requiresAuth: true, role: 'ADMIN' } },
    { path: '/admin/dashboard', name: 'Dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { requiresAuth: true, role: 'ADMIN' } },

    // --- STAFF ---
    { path: '/staff/home', name: 'staffHome', component: () => import('../views/staff/StaffHome.vue'), meta: { requiresAuth: true, role: 'EMPLOYEE' } },
    { path: '/staff/profile', name: 'staffProfile', component: () => import('../views/staff/StaffProfile.vue'), meta: { requiresAuth: true, role: 'EMPLOYEE' } }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const isLogged = !!(token && token !== 'null' && token !== 'undefined')

  // 1. 只有登录页完全不需要导航栏，其它页面都要显示
  // 2. 如果已登录用户强行去 /login，才跳转到对应后台
  if (isLogged && to.name === 'login') {
    const userRole = (localStorage.getItem('role') || '').toUpperCase()
    if (userRole === 'ADMIN') return { name: 'AccountManage' }
    if (userRole === 'EMPLOYEE') return { name: 'staffHome' }
    return { name: 'home' }
  }

  // 3. 基础权限拦截
  if (to.meta.requiresAuth && !isLogged) {
    return { name: 'login' }
  }
})

export default router
