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
        <el-table-column label="频道">
          <template #default="scope">
            <el-tag
              v-for="channel in scope.row.typeList"
              :key="channel.uid"
              size="mini"
              style="margin-right: 5px"
            >
              {{ getChannelTypeName(channel.type) }}
            </el-tag>
            <span v-if="!scope.row.typeList || scope.row.typeList.length === 0">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button @click="showChannels(scope.row)" type="info" size="small" plain>查看频道</el-button>
            <el-button @click="openBindDialog(scope.row)" type="primary" size="small">绑定频道</el-button>
            <el-button @click="deleteAccount(scope.row.uid)" type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 绑定频道对话框 -->
    <el-dialog title="绑定频道" :visible.sync="bindDialogVisible" width="30%">
      <el-form :model="bindForm" ref="bindFormRef" label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="bindForm.userId" disabled></el-input>
        </el-form-item>
        <el-form-item label="频道ID" prop="channelId">
          <el-input v-model="bindForm.channelId" placeholder="请输入频道ID"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="bindDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="bindChannel">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 频道列表对话框 -->
    <el-dialog title="频道列表" :visible.sync="channelDialogVisible" width="50%">
      <el-table :data="currentChannels" style="width: 100%">
        <el-table-column prop="type" label="频道类型">
          <template #default="scope">
            {{ getChannelTypeName(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="uid" label="频道ID"></el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button @click="unbindChannel(bindForm.userId, scope.row.uid)" type="danger" size="small">解绑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="channelDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAllUsers, addAccount, deleteAccount, bindChannel, unbindChannel } from '@/api/account/accountApi';

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
      },
      bindDialogVisible: false,
      channelDialogVisible: false,
      bindForm: {
        userId: '',
        channelId: ''
      },
      currentChannels: []
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
        // 确保正确处理响应数据
        if (response && response.data) {
          this.accounts = Array.isArray(response.data) ? response.data : [];
        } else {
          this.accounts = [];
        }
        console.log('获取账号列表成功:', this.accounts);
        this.$message.success('账号列表刷新成功');
      } catch (error) {
        console.error('获取账号列表失败:', error);
        this.$message.error('获取账号列表失败: ' + (error.message || '未知错误'));
        // 即使出错也要确保loading状态被清除
        this.accounts = [];
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
            await this.fetchAccounts(); // 刷新列表
          } catch (error) {
            this.$message.error('添加账号失败: ' + (error.message || '未知错误'));
          }
        }
      });
    },

    // 删除账号
    deleteAccount(uid) {
      this.$confirm('确定要删除该账号吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteAccount(uid);
          this.$message.success('账号删除成功');
          await this.fetchAccounts(); // 刷新列表
        } catch (error) {
          this.$message.error('删除账号失败: ' + (error.message || '未知错误'));
        }
      }).catch(() => {
        // 用户取消删除
      });
    },

    // 打开绑定频道对话框
    openBindDialog(account) {
      this.bindForm.userId = account.uid;
      this.bindForm.channelId = '';
      this.bindDialogVisible = true;
    },

    // 绑定频道
    async bindChannel() {
      if (!this.bindForm.channelId) {
        this.$message.warning('请输入频道ID');
        return;
      }

      try {
        await bindChannel(this.bindForm.userId, this.bindForm.channelId);
        this.$message.success('频道绑定成功');
        this.bindDialogVisible = false;
        await this.fetchAccounts(); // 刷新列表
      } catch (error) {
        this.$message.error('频道绑定失败: ' + (error.message || '未知错误'));
      }
    },

    // 显示频道列表
    showChannels(account) {
      this.bindForm.userId = account.uid;
      this.currentChannels = account.typeList || [];
      this.channelDialogVisible = true;
    },

    // 解绑频道
    async unbindChannel(userId, channelId) {
      try {
        await unbindChannel(userId, channelId);
        this.$message.success('频道解绑成功');
        this.channelDialogVisible = false;
        await this.fetchAccounts(); // 刷新列表
      } catch (error) {
        this.$message.error('频道解绑失败: ' + (error.message || '未知错误'));
      }
    },

    // 获取频道类型名称
    getChannelTypeName(type) {
      const typeMap = {
        '1': 'B站频道',
        '2': '抖音频道'
      };
      return typeMap[type] || `未知频道(${type})`;
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
