// src/utils/request.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 修改 baseURL，加上 /api 后缀
const request = axios.create({
  baseURL: 'http://localhost:8080/api', 
  timeout: 120000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    
    // 【修改点】获取 userId and tenantId
    const userId = localStorage.getItem('userId')
    const tenantId = localStorage.getItem('tenantId')

    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 【修改点】注入自定义 Header，供后端审计使用
    if (userId) {
      config.headers['X-User-Id'] = userId
    }
    if (tenantId) {
      config.headers['X-Tenant-Id'] = tenantId
    }

    return config
  },
  (error) => Promise.reject(error)
)

// 2. 修改响应拦截器，适配你的后端结构 { code, msg, data }
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果 code 不是 200，说明后端报错了（比如密码错误）
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求出错')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    
    // 成功时，返回整个 res 对象，方便组件里取 data
    return res
  },
  (error) => {
    let msg = '请求失败'
    // 处理 HTTP 状态码错误
    if (error.response) {
      switch (error.response.status) {
        case 401: msg = '认证失败，请重新登录'; break;
        case 403: msg = '无权访问'; break;
        case 404: msg = '请求地址不存在'; break;
        case 500: msg = '服务器内部错误'; break;
      }
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request