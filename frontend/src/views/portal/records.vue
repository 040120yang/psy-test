<template>
  <div class="record-page">
    <el-card shadow="never">
      <div slot="header" class="record-header">
        <span>我的测评记录</span>
      </div>

      <el-table :data="records" border v-loading="loading">
        <el-table-column prop="scaleName" label="量表名称" min-width="140" />
        <el-table-column prop="scaleCode" label="编码" width="90" align="center" />
        <el-table-column prop="rawScore" label="粗分" width="70" align="center" />
        <el-table-column prop="stdScore" label="标准分" width="80" align="center" />
        <el-table-column label="等级" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="levelTagType(scope.row.level)" size="small">{{ scope.row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="测评时间" width="160" align="center" />
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showDetail(scope.row.recordId)">查看报告</el-button>
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
        @current-change="loadRecords"
      />
    </el-card>

    <!-- 报告详情弹窗 -->
    <el-dialog :title="'测评报告 - ' + (detail.record ? detail.record.scaleName : '')" :visible.sync="dialogVisible" width="720px">
      <div v-if="detail.record">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="粗分">{{ detail.record.rawScore }}</el-descriptions-item>
          <el-descriptions-item label="标准分">{{ detail.record.stdScore }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ detail.record.level }}</el-descriptions-item>
          <el-descriptions-item label="测评时间" :span="3">{{ detail.record.createTime }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-advice">
          <b>分析与建议：</b>
          <p>{{ detail.record.suggestion }}</p>
        </div>
        <el-table :data="detail.answers" border size="small" max-height="300">
          <el-table-column type="index" label="题号" width="60" align="center" />
          <el-table-column prop="content" label="题目" min-width="260" />
          <el-table-column label="选项分值" width="90" align="center">
            <template slot-scope="scope">{{ scope.row.optionValue }} 分</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMyRecords, getRecordDetail, delRecord } from '@/api/test'

export default {
  name: 'PortalRecords',
  data() {
    return {
      records: [],
      total: 0,
      loading: false,
      query: { pageNum: 1, pageSize: 10 },
      dialogVisible: false,
      detail: {}
    }
  },
  created() {
    this.loadRecords()
  },
  methods: {
    loadRecords() {
      this.loading = true
      listMyRecords(this.query).then(res => {
        this.records = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    showDetail(recordId) {
      getRecordDetail(recordId).then(res => {
        this.detail = res.data
        this.dialogVisible = true
      })
    },
    handleDelete(row) {
      this.$confirm(`确认删除该测评记录（${row.scaleName}）吗？`, '提示', { type: 'warning' })
        .then(() => delRecord(row.recordId))
        .then(() => {
          this.$message.success('删除成功')
          this.loadRecords()
        })
        .catch(() => {})
    },
    levelTagType(level) {
      if (!level) return 'info'
      if (level.indexOf('正常') !== -1) return 'success'
      if (level.indexOf('轻度') !== -1) return 'warning'
      return 'danger'
    }
  }
}
</script>

<style scoped>
.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
.detail-advice {
  margin: 14px 0;
  background: #f8f9fb;
  padding: 12px;
  border-radius: 6px;
}
.detail-advice p {
  margin: 6px 0 0;
  color: #606266;
  line-height: 1.8;
}
</style>
