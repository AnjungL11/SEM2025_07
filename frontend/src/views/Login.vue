<template>
  <div class="auth-wrapper">
    <el-card class="auth-card">
      <template #header>
        <div class="card-header">
          <span class="title">用户登录</span>
          <el-button link type="primary" @click="$router.push('/register')">没有账号? 去注册</el-button>
        </div>
      </template>

      <el-form :model="form" label-width="70px" class="auth-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="submit-btn-wrapper">
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登录</el-button>
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
const form = ref({ username: '', password: '' });

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }

  loading.value = true;
  try {
    const res: any = await request.post('/auth/login', form.value);
    
    if (res.code === 200) {
      // 解构后端返回的所有字段
      const { token, role, username, userId, tenantId, email, createTime } = res.data; 
      
      // 存入 localStorage
      localStorage.setItem('token', token);
      // 【关键】统一转小写去空格，防止 admin 判断失效
      localStorage.setItem('role', role ? role.trim().toLowerCase() : 'viewer'); 
      localStorage.setItem('username', username);
      localStorage.setItem('userId', userId || '');
      localStorage.setItem('tenantId', tenantId || '');
      localStorage.setItem('email', email || '未绑定');
      localStorage.setItem('createTime', createTime || 'N/A');
      
      ElMessage.success('登录成功');
      router.push('/chat'); 
    } else {
      ElMessage.error(res.msg || '登录失败');
    }
    
  } catch (error) {
    console.error('登录异常:', error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.auth-wrapper { 
  width: 100%; 
  height: 100vh; 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  background-color: #f5f7fa; 
}
.auth-card { width: 400px; border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 20px; font-weight: bold; color: #333; }
.auth-form { padding: 20px 20px 0 0; }
.submit-btn-wrapper { padding-left: 70px; margin-top: 20px; }
.submit-btn { width: 100%; height: 40px; font-size: 16px; }
</style>