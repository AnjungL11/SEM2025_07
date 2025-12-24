<template>
    <div class="auth-wrapper">
      <el-card class="auth-card">
        <template #header>
          <div class="card-header">
            <span class="title">用户注册</span>
            <el-button link type="primary" @click="$router.push('/login')">已有账号? 立即登录</el-button>
          </div>
        </template>
  
        <el-form :model="form" label-width="80px" label-position="right" class="auth-form">
          
          <el-form-item label="租户名称" required>
            <el-input v-model="form.tenantName" placeholder="请输入租户名称 (如: 测试企业G07)" />
          </el-form-item>
  
          <el-form-item label="用户名" required>
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
  
          <el-form-item label="邮箱" required>
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
  
          <el-form-item label="密码" required>
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
  
          <el-form-item label="确认密码" required>
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
          </el-form-item>
  
          <div class="submit-btn-wrapper">
            <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">注册</el-button>
          </div>
  
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { ElMessage } from 'element-plus';
  import request from '../utils/request';
  
  const router = useRouter();
  const loading = ref(false);
  
  const form = ref({
    tenantName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  
  const handleRegister = async () => {
    // 简单的表单校验
    if (!form.value.tenantName || !form.value.username || !form.value.email || !form.value.password) {
      ElMessage.warning('请填写完整信息');
      return;
    }
    if (form.value.password !== form.value.confirmPassword) {
      ElMessage.error('两次输入的密码不一致');
      return;
    }
    
    loading.value = true;
    try {
      // 构造请求参数（去掉 confirmPassword，因为后端不需要）
      const payload = {
        tenantName: form.value.tenantName,
        username: form.value.username,
        password: form.value.password,
        email: form.value.email
      };
  
      // 发送 POST 请求到 /auth/register
      await request.post('/auth/register', payload);
      
      ElMessage.success('注册成功，请登录');
      // 注册成功后跳转回登录页
      router.push('/login');
      
    } catch (error) {
      console.error(error);
    } finally {
      loading.value = false;
    }
  };
  </script>
  
  <style scoped>
  /* 样式复用 Login 的 */
  .auth-wrapper { width: 100%; height: 100%; display: flex; justify-content: center; align-items: center; background-color: #f5f7fa; }
  .auth-card { width: 480px; border-radius: 8px; }
  .card-header { display: flex; justify-content: space-between; align-items: center; }
  .title { font-size: 20px; font-weight: bold; color: #333; }
  .auth-form { padding: 20px 20px 0 0; }
  .submit-btn-wrapper { padding-left: 80px; margin-top: 30px; }
  .submit-btn { width: 100%; height: 40px; font-size: 16px; }
  </style>