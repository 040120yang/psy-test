<template>
  <div class="follow-admin">
    <el-card>
      <div slot="header" style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 18px; font-weight: 600;">随访管理</span>
        <div>
          <el-select v-model="status" placeholder="按状态筛选" clearable style="width: 140px; margin-right: 10px;" @change="load">
            <el-option label="待随访" value="待随访" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已逾期" value="已逾期" />
          </el-select>
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增随访任务</el-button>
        </div>
      </div>

      <el-table :data="list" border stripe style="width: 100%;">
        <el-table-column prop="nickname" label="患者" width="120" />
        <el-table-column prop="scaleName" label="关联量表" width="140" />
        <el-table-column prop="level" label="测评等级" width="110">
          <template slot-scope="scope">
            <el-tag size="small" :color="getLevelColor(scope.row.level)" style="color:#fff;border:0;">{{ scope.row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="followDate" label="随访日期" width="110" />
        <el-table-column prop="followType" label="方式" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="symptomScore" label="症状分" width="80" />
        <el-table-column prop="doctorNote" label="医生记录" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="openFinish(scope.row)" v-if="scope.row.status !== '已完成'">完成随访</el-button>
            <el-button size="mini" type="text" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" style="color: #f56c6c;" @click="del(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog :title="edit.id ? '编辑随访' : '新增随访任务'" :visible.sync="editVisible" width="500px">
      <el-form :model="edit" label-width="90px">
        <el-form-item label="患者">
          <el-select v-model="edit.userId" filterable placeholder="选择患者" style="width: 100%;" @change="onPatientChange">
            <el-option v-for="p in patients" :key="p.userId" :label="p.patientName + ' (' + p.username + ')'" :value="p.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联记录">
          <el-select v-model="edit.recordId" filterable placeholder="选择患者的测评记录" style="width: 100%;" @change="onRecordChange">
            <el-option v-for="r in userRecords" :key="r.recordId" :label="r.scaleName + ' - ' + r.level + ' (' + r.createTime + ')'" :value="r.recordId" />
          </el-select>
        </el-form-item>
        <el-form-item label="随访日期">
          <el-date-picker v-model="edit.followDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="随访方式">
          <el-select v-model="edit.followType" style="width: 100%;">
            <el-option label="线上" value="线上" />
            <el-option label="电话" value="电话" />
            <el-option label="线下" value="线下" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </div>
    </el-dialog>

    <!-- 完成随访 -->
    <el-dialog title="完成随访" :visible.sync="finishVisible" width="500px">
      <el-form :model="finish" label-width="120px">
        <el-form-item label="症状自评(0-10)">
          <el-input-number v-model="finish.symptomScore" :min="0" :max="10" />
        </el-form-item>
        <el-form-item label="医生记录">
          <el-input type="textarea" v-model="finish.doctorNote" :rows="4" placeholder="记录本次随访沟通内容与患者状态" />
        </el-form-item>
        <el-form-item label="下次随访日期">
          <el-date-picker v-model="finish.nextFollowDate" type="date" value-format="yyyy-MM-dd" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFinish">完成</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'AdminFollow',
  data() {
    return {
      list: [],
      status: '',
      patients: [],
      userRecords: [],
      editVisible: false,
      edit: { userId: '', recordId: '', followDate: '', followType: '线上' },
      finishVisible: false,
      finish: { id: '', symptomScore: 5, doctorNote: '', nextFollowDate: '' }
    }
  },
  mounted() {
    this.load()
    this.loadPatients()
  },
  methods: {
    load() {
      request({ url: '/follow/admin/list', method: 'get', params: { status: this.status } }).then(res => {
        this.list = res.rows || []
      })
    },
    loadPatients() {
      request({ url: '/patient/list', method: 'get' }).then(res => {
        this.patients = res.rows || []
      })
    },
    onPatientChange(userId) {
      this.edit.recordId = ''
      this.userRecords = []
      if (!userId) return
      request({ url: '/test/record/list', method: 'get', params: { userId } }).then(res => {
        this.userRecords = res.rows || []
      })
    },
    onRecordChange() {
      // 选了记录后可以自动带出量表和等级，这里暂不需要
    },
    openAdd() {
      this.edit = { userId: '', recordId: '', followDate: '', followType: '线上' }
      this.editVisible = true
    },
    openEdit(row) {
      this.edit = { ...row }
      this.editVisible = true
    },
    saveEdit() {
      if (!this.edit.userId) {
        this.$message.warning('请选择患者')
        return
      }
      if (!this.edit.followDate) {
        this.$message.warning('请选择随访日期')
        return
      }
      const data = { ...this.edit }
      if (!data.recordId) data.recordId = null
      if (!data.doctorId) data.doctorId = null
      const url = this.edit.id ? '/follow' : '/follow'
      const method = this.edit.id ? 'put' : 'post'
      request({ url, method, data }).then(() => {
        this.$message.success('保存成功')
        this.editVisible = false
        this.load()
      })
    },
    openFinish(row) {
      this.finish = { id: row.id, symptomScore: row.symptomScore || 5, doctorNote: '', nextFollowDate: '' }
      this.finishVisible = true
    },
    saveFinish() {
      request({ url: '/follow/finish', method: 'put', data: this.finish }).then(() => {
        this.$message.success('随访完成')
        this.finishVisible = false
        this.load()
      })
    },
    del(row) {
      this.$confirm('确认删除该随访任务？', '提示', { type: 'warning' }).then(() => {
        request({ url: '/follow/' + row.id, method: 'delete' }).then(() => {
          this.$message.success('删除成功')
          this.load()
        })
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
.follow-admin { padding: 0; }
</style>
