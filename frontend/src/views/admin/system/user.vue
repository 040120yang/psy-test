<template>
  <div class="user-page">
    <el-card shadow="never">
      <div slot="header" class="toolbar">
        <div class="filter-bar">
          <el-input v-model="query.username" placeholder="用户名" clearable style="width:160px" @keyup.enter.native="loadUsers" />
          <el-input v-model="query.nickname" placeholder="昵称" clearable style="width:160px" @keyup.enter.native="loadUsers" />
          <el-select v-model="query.roleId" placeholder="角色" clearable style="width:140px" @change="loadUsers">
            <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
          <el-button type="primary" icon="el-icon-search" @click="loadUsers">查询</el-button>
        </div>
        <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增用户</el-button>
      </div>

      <el-table :data="users" border v-loading="loading">
        <el-table-column prop="userId" label="ID" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="roleName" label="角色" width="120" align="center" />
        <el-table-column label="性别" width="70" align="center">
          <template slot-scope="scope">{{ { 0: '男', 1: '女', 2: '未知' }[scope.row.sex] }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="openDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="openResetPwd(scope.row)">重置密码</el-button>
            <el-button type="text" size="small" style="color:#f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page.sync="query.pageNum"
        @current-change="loadUsers"
      />
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog :title="form.userId ? '编辑用户' : '新增用户'" :visible.sync="dialogVisible" width="520px">
      <el-form ref="userForm" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.userId" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="form.userId ? '留空则不修改密码' : '初始密码'" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" style="width:100%">
            <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio label="0">男</el-radio>
            <el-radio label="1">女</el-radio>
            <el-radio label="2">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="1" :max="120" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11位手机号码" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog title="重置密码" :visible.sync="pwdDialogVisible" width="420px">
      <el-form label-width="90px">
        <el-form-item label="用户名">
          <el-input :value="resetUser.username" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPwd">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUser, listRoles, addUser, updateUser, delUser, resetUserPwd } from '@/api/system'

export default {
  name: 'User',
  data() {
    return {
      users: [],
      roles: [],
      total: 0,
      loading: false,
      query: { pageNum: 1, pageSize: 10, username: '', nickname: '', roleId: null },
      dialogVisible: false,
      pwdDialogVisible: false,
      form: {},
      resetUser: {},
      newPassword: '',
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }]
      }
    }
  },
  created() {
    listRoles().then(res => { this.roles = res.data || [] })
    this.loadUsers()
  },
  methods: {
    loadUsers() {
      this.loading = true
      listUser(this.query).then(res => {
        this.users = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    openDialog(row) {
      this.form = row ? { ...row, password: '' } : { sex: '2', status: '0', age: 20 }
      this.dialogVisible = true
    },
    handleSave() {
      this.$refs.userForm.validate(valid => {
        if (!valid) return
        const req = this.form.userId ? updateUser(this.form) : addUser(this.form)
        req.then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadUsers()
        })
      })
    },
    handleDelete(row) {
      this.$confirm(`确认删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
        .then(() => delUser(row.userId))
        .then(() => {
          this.$message.success('删除成功')
          this.loadUsers()
        })
        .catch(() => {})
    },
    openResetPwd(row) {
      this.resetUser = row
      this.newPassword = ''
      this.pwdDialogVisible = true
    },
    handleResetPwd() {
      if (!this.newPassword) {
        this.$message.warning('请输入新密码')
        return
      }
      resetUserPwd({ userId: this.resetUser.userId, password: this.newPassword }).then(() => {
        this.$message.success('密码已重置')
        this.pwdDialogVisible = false
      })
    }
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.filter-bar {
  display: flex;
  align-items: center;
}
.filter-bar .el-input,
.filter-bar .el-select {
  margin-right: 8px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
