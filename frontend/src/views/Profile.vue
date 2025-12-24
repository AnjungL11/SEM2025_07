<template>
    <div class="profile-container">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span class="title">个人中心</span>
            <el-tag :type="userInfo.role === 'admin' ? 'danger' : 'success'" effect="dark">
              {{ userInfo.role === 'admin' ? '系统管理员' : '研究员' }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="1" border class="profile-details">
          <el-descriptions-item label="用户名">
            <span class="value">{{ userInfo.username }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="用户角色">
            <span class="value">{{ userInfo.role }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="用户 ID">
            <code class="code-value">{{ userInfo.userId }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="所属租户 ID">
            <code class="code-value">{{ userInfo.tenantId }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="电子邮箱">
            <span class="value">{{ userInfo.email || '未绑定' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="账号创建时间">
            <span class="value">{{ userInfo.createTime }}</span>
          </el-descriptions-item>
        </el-descriptions>
  
        <div class="action-bar">
          <el-button @click="$router.push('/chat')">返回问答</el-button>
          <el-button type="danger" plain @click="handleLogout">退出当前账号</el-button>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup lang="ts">
  import { reactive, onMounted } from 'vue'; // 引入 onMounted 确保挂载时读取
  import { useRouter } from 'vue-router';
  import { ElMessage, ElMessageBox } from 'element-plus';
  
  const router = useRouter();
  
  // 1. 定义响应式对象
  const userInfo = reactive({
    username: '',
    role: '',
    userId: '',
    tenantId: '',
    email: '',
    createTime: ''
  });
  
  // 2. 抽取读取逻辑，确保多次调用
  const loadUserData = () => {
    userInfo.username = localStorage.getItem('username') || '未知用户';
    userInfo.role = localStorage.getItem('role') || 'researcher';
    userInfo.userId = localStorage.getItem('userId') || 'N/A';
    userInfo.tenantId = localStorage.getItem('tenantId') || 'N/A';
    userInfo.email = localStorage.getItem('email') || '';
    userInfo.createTime = localStorage.getItem('createTime') || 'N/A';
  };
  
  // 3. 在组件挂载时强制读取一次最新的 localStorage
  onMounted(() => {
    loadUserData();
  });
  
  const handleLogout = () => {
    ElMessageBox.confirm(
      '您确定要退出登录并清除所有本地缓存吗？',
      '提示',
      { confirmButtonText: '确定退出', cancelButtonText: '取消', type: 'warning' }
    ).then(() => {
      localStorage.clear();
      ElMessage.success('已安全退出登录');
      router.push('/login');
    }).catch(() => {});
  };
  </script>
  
  <style scoped>
  .profile-container {
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding: 40px 20px;
    background-color: #f5f7fa;
    min-height: calc(100vh - 100px);
  }
  .profile-card { width: 100%; max-width: 700px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }
  .card-header { display: flex; justify-content: space-between; align-items: center; }
  .title { font-size: 18px; font-weight: 600; color: #303133; }
  .profile-details { margin-top: 10px; }
  .value { color: #606266; font-weight: 500; }
  .code-value { background-color: #f0f2f5; padding: 2px 6px; border-radius: 4px; color: #cf9236; font-family: monospace; }
  .action-bar { margin-top: 30px; display: flex; justify-content: flex-end; gap: 15px; }
  </style>