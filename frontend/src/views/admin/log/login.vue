<template>
  <div class="admin-login-log">
    <el-card shadow="never">
      <div slot="header">
        <span style="font-size:18px;font-weight:600;">登录日志</span>
      </div>
      <el-table :data="list" border stripe>
        <el-table-column prop="infoId" label="ID" width="60" align="center" />
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="ipaddr" label="IP地址" width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="提示" min-width="200" />
        <el-table-column prop="loginTime" label="登录时间" width="160" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'AdminLoginLog',
  data() {
    return { list: [] }
  },
  mounted() { this.load() },
  methods: {
    load() {
      request({ url: '/business/log/login', method: 'get' }).then(res => {
        this.list = res.rows || []
      })
    }
  }
}
</script>
