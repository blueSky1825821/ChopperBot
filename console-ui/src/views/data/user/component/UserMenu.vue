<template>
  <div class="user-menu">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>账号管理</span>
          <el-button @click="fetchAccounts" type="primary" size="small">刷新</el-button>
        </div>
      </template>

      <!-- 添加账号表单 -->
      <el-form :model="addAccountForm" :rules="addAccountRules" ref="addAccountFormRef" label-width="100px">
        <el-form-item label="平台" prop="platformId">
          <el-select v-model="addAccountForm.platformId" placeholder="请选择平台">
            <el-option label="B站" :value="1"></el-option>
            <el-option label="抖音" :value="2"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input v-model="addAccountForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="addAccount">添加账号</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 账号列表 -->
      <el-table :data="accounts" style="width: 100%" v-loading="loading">
        <el-table-column prop="uid" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="platform" label="平台"></el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button @click="deleteAccount(scope.row.uid)" type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAllUsers, addAccount, deleteAccount } from '@/api/account/accountApi';

export default {
  name: 'UserMenu',
  data() {
    return {
      loading: false,
      accounts: [],
      addAccountForm: {
        platformId: 1,
        username: ''
      },
      addAccountRules: {
        platformId: [
          { required: true, message: '请选择平台', trigger: 'change' }
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ]
      }
    };
  },

  mounted() {
    this.fetchAccounts();
  },

  methods: {
    // 获取账号列表
    async fetchAccounts() {
      this.loading = true;
      try {
        const response = await getAllUsers();
        this.accounts = response.data.data || [];
      } catch (error) {
        this.$message.error('获取账号列表失败: ' + error.message);
      } finally {
        this.loading = false;
      }
    },

    // 添加账号
    addAccount() {
      this.$refs.addAccountFormRef.validate(async (valid) => {
        if (valid) {
          try {
            await addAccount(this.addAccountForm.platformId, this.addAccountForm.username);

            this.$message.success('账号添加成功');
            this.resetForm();
            this.fetchAccounts(); // 刷新列表
          } catch (error) {
            this.$message.error('添加账号失败: ' + error.message);
          }
        }
      });
    },

    // 删除账号
    async deleteAccount(uid) {
      try {
        await deleteAccount(uid);

        this.$message.success('账号删除成功');
        this.fetchAccounts(); // 刷新列表
      } catch (error) {
        this.$message.error('删除账号失败: ' + error.message);
      }
    },

    // 重置表单
    resetForm() {
      this.$refs.addAccountFormRef.resetFields();
    }
  }
};
</script>

<style scoped>
.user-menu {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
