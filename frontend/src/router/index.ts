import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Layout from '../views/Layout.vue'
import { ElMessage } from 'element-plus'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/chat',
    children: [
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('../views/Chat.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('../views/Knowledge.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('../views/Admin.vue'),
        meta: { 
          requiresAuth: true, 
          requiresAdmin: true // 标记只有 admin 角色可以进入
        }
      },
      {
        path: 'profile', // 子路由路径不加 "/"
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  // 捕获所有未定义的路径，重定向到首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置守卫：处理登录拦截与权限验证
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const rawRole = localStorage.getItem('role')
  // 统一转为小写并去空格，防止后端传回 "Admin " 或 "admin" 导致匹配失败
  const userRole = rawRole ? rawRole.trim().toLowerCase() : ''

  // 1. 检查是否需要登录
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录后操作')
    next('/login')
    return
  }

  // 2. 检查管理员权限
  if (to.meta.requiresAdmin) {
    if (userRole === 'admin') {
      next() // 角色匹配，放行
    } else {
      ElMessage.error('权限不足：您的角色不是管理员，无法访问该界面')
      // 拦截后返回之前的页面，如果没有来源页则去 chat
      next(from.path && from.path !== '/' ? from.path : '/chat')
    }
    return
  }

  // 3. 正常放行
  next()
})

export default router