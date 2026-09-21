<template>
  <div class="message-page">
    <el-card shadow="never">
      <div slot="header">
        <span style="font-size:18px;font-weight:600;">消息中心</span>
      </div>
      <el-table :data="list" border stripe>
        <el-table-column prop="title" label="标题" min-width="200">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isRead === 0" type="danger" size="mini" style="margin-right:8px;">未读</el-tag>
            {{ scope.row.title }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small">{{ typeText(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" align="center" />
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="消息详情" :visible.sync="detailVisible" width="600px">
      <h3 style="margin-top:0;">{{ current.title }}</h3>
      <p style="color:#909399;font-size:13px;">{{ current.createTime }}</p>
      <p style="line-height:1.8;">{{ current.content }}</p>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'PortalMessage',
  data() {
    return { list: [], detailVisible: false, current: {} }
  },
  mounted() { this.load() },
  methods: {
    load() {
      request({ url: '/business/message/my', method: 'get' }).then(res => {
        this.list = res.rows || []
      })
    },
    typeText(t) {
      return { system: '系统', follow: '随访', result: '测评' }[t] || t
    },
    showDetail(row) {
      this.current = row
      this.detailVisible = true
      if (row.isRead === 0) {
        request({ url: '/business/message/read/' + row.id, method: 'put' }).then(() => {
          row.isRead = 1
        })
      }
    }
  }
}
</script>
