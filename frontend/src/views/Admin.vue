<template>
  <el-tabs type="border-card" class="admin-tabs" v-model="activeTab" @tab-change="handleTabChange">
    <el-tab-pane label="用户权限管理" name="user">
      <el-table :data="userList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="tenantName" label="所属企业">
           <template #default="{ row }">
             <el-tag type="info">{{ row.tenantName || '未知企业' }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="createTime" label="注册时间" width="180">
           <template #default="{ row }">
             {{ row.createTime ? new Date(row.createTime).toLocaleString() : '-' }}
           </template>
        </el-table-column>
        <el-table-column prop="role" label="当前角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="openRoleDialog(row)">修改角色</el-button>
            <el-popconfirm title="确定要注销此用户吗？" @confirm="handleDeleteUser(row)">
               <template #reference>
                 <el-button size="small" type="danger">注销账户</el-button>
               </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>

    <el-tab-pane label="操作审计日志" name="audit">
      <el-table :data="logs" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="timestamp" label="时间" width="180">
          <template #default="{ row }">
             {{ row.timestamp ? new Date(row.timestamp).toLocaleString() : '-' }}
           </template>
        </el-table-column>
        
        <el-table-column prop="username" label="操作人" width="120" show-overflow-tooltip>
           <template #default="{ row }">
              <strong>{{ row.username || 'Unknown' }}</strong>
           </template>
        </el-table-column>
        
        <el-table-column prop="operationType" label="操作类型" width="100">
           <template #default="{ row }">
             <el-tag :type="getLogTagType(row.operationType)">{{ row.operationType }}</el-tag>
           </template>
        </el-table-column>
        
        <el-table-column prop="targetResource" label="目标资源" show-overflow-tooltip/>
        <el-table-column prop="operationDetail" label="详情" show-overflow-tooltip/>
        
        </el-table>
    </el-tab-pane>
  </el-tabs>

  <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="400px">
    <el-form>
      <el-form-item label="用户名">
        <el-input v-model="currentUser.username" disabled />
      </el-form-item>
      <el-form-item label="选择角色">
        <el-select v-model="selectedRole">
          <el-option label="管理员 (Admin)" value="admin" />
          <el-option label="研究员 (Researcher)" value="researcher" />
          <el-option label="访客 (Viewer)" value="viewer" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="roleDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitRoleChange">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const activeTab = ref('user')
const loading = ref(false)
const userList = ref([])
const logs = ref([])
const roleDialogVisible = ref(false)
const currentUser = ref<any>({})
const selectedRole = ref('')

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/user/list')
    if (res.code === 200) {
      userList.value = res.data
    }
  } catch(e) { console.error(e) } 
  finally { loading.value = false }
}

// 获取日志
const fetchLogs = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/audit/logs')
    if (res.code === 200) {
      logs.value = res.data
    }
  } catch(e) { console.error(e) } 
  finally { loading.value = false }
}

const handleTabChange = (tabName: string) => {
  if (tabName === 'user') fetchUsers()
  if (tabName === 'audit') fetchLogs()
}

const openRoleDialog = (row: any) => {
  currentUser.value = row
  selectedRole.value = row.role
  roleDialogVisible.value = true
}

const submitRoleChange = async () => {
  try {
    const res: any = await request.post('/user/updateRole', {
      adminId: localStorage.getItem('userId'),
      targetUserId: currentUser.value.userId,
      newRole: selectedRole.value
    })
    if (res.code === 200) {
      ElMessage.success('角色修改成功')
      roleDialogVisible.value = false
      fetchUsers()
    }
  } catch(e) { ElMessage.error('修改失败') }
}

const handleDeleteUser = async (row: any) => {
  try {
    const res: any = await request.delete('/user/delete', {
      params: { adminId: localStorage.getItem('userId'), targetUserId: row.userId }
    })
    if (res.code === 200) {
      ElMessage.success('用户已注销')
      fetchUsers()
    }
  } catch(e) { ElMessage.error('注销失败') }
}

const getLogTagType = (type: string) => {
  if (type === 'DELETE' || type === 'DELETE_USER') return 'danger'
  if (type === 'UPLOAD') return 'success'
  if (type === 'UPDATE_ROLE') return 'warning'
  return 'info'
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-tabs { min-height: 80vh; }
</style>