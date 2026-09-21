<template>
  <div class="admin-oper-log">
    <el-card shadow="never">
      <div slot="header">
        <span style="font-size:18px;font-weight:600;">操作日志</span>
      </div>
      <el-table :data="list" border stripe>
        <el-table-column prop="operId" label="ID" width="60" align="center" />
        <el-table-column prop="title" label="模块" width="120" />
        <el-table-column prop="operName" label="操作人" width="100" />
        <el-table-column prop="operUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operIp" label="IP" width="130" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 0 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="160" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'AdminOperLog',
  data() {
    return { list: [] }
  },
  mounted() { this.load() },
  methods: {
    load() {
      request({ url: '/business/log/oper', method: 'get' }).then(res => {
        this.list = res.rows || []
      })
    }
  }
}
</script>
