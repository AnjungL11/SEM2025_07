<template>
  <div class="layout-container">
    <el-container>
      <el-aside width="200px">
        <el-menu :default-active="$route.path" router class="side-menu">
          <el-menu-item index="/chat">智能问答</el-menu-item>
          <el-menu-item index="/knowledge">知识库管理</el-menu-item>
          <el-menu-item v-if="userRole === 'admin'" index="/admin">系统设置</el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="logo">产业知识问答系统</div>
          <div class="user-info">
            <div v-if="!isLogin">
              <el-button link @click="$router.push('/login')">登录</el-button>
              <el-button link @click="$router.push('/register')">注册</el-button>
            </div>
            <el-dropdown v-else @command="handleCommand">
              <span class="username-link">
                {{ username }} ({{ userRole === 'admin' ? '管理员' : '研究员' }})
                <el-icon><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute();

const isLogin = ref(false);
const username = ref('');
const userRole = ref('');

// 初始化用户信息
const initUserInfo = () => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  
  isLogin.value = !!token;
  username.value = localStorage.getItem('username') || '';
  // 关键：确保 role 被正确赋值且没有多余空格
  userRole.value = role ? role.trim() : '';
};

onMounted(() => {
  initUserInfo();
});

// 监听路由变化，确保在登录/登出跳转后状态立即刷新
watch(() => route.path, () => {
  initUserInfo();
});

const handleCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout();
  } else if (command === 'profile') {
    router.push('/profile'); // 这里改为跳转到新的 profile 页面
  }
};

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', { type: 'warning' }).then(() => {
    localStorage.clear(); // 清除所有缓存
    isLogin.value = false;
    ElMessage.success('已安全退出');
    router.push('/login');
  });
};
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ddd; background: #fff; }
.username-link { cursor: pointer; color: #409EFF; font-weight: bold; }
.side-menu { height: calc(100vh - 60px); }
</style>