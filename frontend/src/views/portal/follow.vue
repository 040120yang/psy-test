<template>
  <div class="follow-page">
    <el-card>
      <div slot="header">
        <span style="font-size: 18px; font-weight: 600;">我的随访</span>
        <el-tag style="margin-left: 12px;" type="warning" v-if="overdueCount">待随访 {{ overdueCount }} 条</el-tag>
      </div>

      <el-table :data="list" border stripe style="width: 100%;">
        <el-table-column prop="followDate" label="随访日期" width="120" />
        <el-table-column prop="scaleName" label="关联量表" width="150" />
        <el-table-column prop="level" label="测评等级" width="120">
          <template slot-scope="scope">
            <el-tag size="small" :color="getLevelColor(scope.row.level)" style="color:#fff;border:0;">{{ scope.row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="followType" label="随访方式" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="symptomScore" label="症状自评" width="100" />
        <el-table-column prop="doctorNote" label="医生记录" show-overflow-tooltip />
        <el-table-column prop="nextFollowDate" label="下次随访" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'PortalFollow',
  data() {
    return { list: [], overdueCount: 0 }
  },
  mounted() { this.load() },
  methods: {
    load() {
      request({ url: '/follow/my', method: 'get' }).then(res => {
        this.list = res.rows || []
        this.overdueCount = this.list.filter(i => i.status === '待随访').length
      })
    },
    getLevelColor(name) {
      if (name === '正常') return '#67C23A'
      if (name && name.indexOf('重度睡眠') !== -1) return '#B22222'
      if (name && name.indexOf('重度焦虑') !== -1) return '#8B0000'
      if (name && name.indexOf('重度抑郁') !== -1) return '#FF8A80'
      if (name && name.indexOf('重度') !== -1) return '#ff4d4f'
      if (name && name.indexOf('中度睡眠') !== -1) return '#FFA940'
      if (name && name.indexOf('中度焦虑') !== -1) return '#E86F0C'
      if (name && name.indexOf('中度抑郁') !== -1) return '#FF9800'
      if (name && name.indexOf('中度') !== -1) return '#FA8C16'
      if (name && name.indexOf('轻度睡眠') !== -1) return '#FFEB3B'
      if (name && name.indexOf('轻度焦虑') !== -1) return '#FFD700'
      if (name && name.indexOf('轻度抑郁') !== -1) return '#F5C518'
      if (name && name.indexOf('轻度') !== -1) return '#E6A23C'
      return '#909399'
    },
    statusTag(status) {
      if (status === '已完成') return 'success'
      if (status === '已逾期') return 'danger'
      return 'warning'
    }
  }
}
</script>

<style scoped>
.follow-page { padding: 0; }
</style>
