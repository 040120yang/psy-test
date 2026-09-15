<template>
  <div class="patient-page">
    <el-card shadow="never">
      <div slot="header" class="toolbar">
        <div class="filter-bar">
          <el-input v-model="query.patientName" placeholder="患者姓名" clearable style="width:160px" @keyup.enter.native="loadPatients" />
          <el-input v-model="query.phone" placeholder="联系电话" clearable style="width:160px" @keyup.enter.native="loadPatients" />
          <el-button type="primary" icon="el-icon-search" @click="loadPatients">查询</el-button>
        </div>
        <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增患者</el-button>
      </div>

      <el-table :data="patients" border v-loading="loading">
        <el-table-column prop="patientId" label="ID" width="60" align="center" />
        <el-table-column prop="patientName" label="姓名" width="100" />
        <el-table-column label="性别" width="60" align="center">
          <template slot-scope="scope">{{ { 0: '男', 1: '女', 2: '未知' }[scope.row.sex] }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="address" label="联系地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="medicalHistory" label="病史/主诉" min-width="200" show-overflow-tooltip />
        <el-table-column prop="username" label="关联用户" width="110" align="center" />
        <el-table-column prop="createTime" label="建档时间" width="160" align="center" />
        <el-table-column label="操作" width="140" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="openDialog(scope.row)">编辑</el-button>
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
        @current-change="loadPatients"
      />
    </el-card>

    <el-dialog :title="form.patientId ? '编辑患者' : '新增患者'" :visible.sync="dialogVisible" width="560px">
      <el-form ref="patientForm" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="patientName">
          <el-input v-model="form.patientName" placeholder="患者姓名" />
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
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="11位手机号码" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" placeholder="身份证号（选填）" />
        </el-form-item>
        <el-form-item label="联系地址">
          <el-input v-model="form.address" placeholder="联系地址" />
        </el-form-item>
        <el-form-item label="病史/主诉">
          <el-input v-model="form.medicalHistory" type="textarea" :rows="3" placeholder="病史或主诉描述" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPatient, addPatient, updatePatient, delPatient } from '@/api/patient'

export default {
  name: 'AdminPatients',
  data() {
    return {
      patients: [],
      total: 0,
      loading: false,
      query: { pageNum: 1, pageSize: 10, patientName: '', phone: '' },
      dialogVisible: false,
      form: {},
      rules: {
        patientName: [{ required: true, message: '请输入患者姓名', trigger: 'blur' }],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadPatients()
  },
  methods: {
    loadPatients() {
      this.loading = true
      listPatient(this.query).then(res => {
        this.patients = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    openDialog(row) {
      this.form = row ? { ...row } : { sex: '2', age: 30 }
      this.dialogVisible = true
    },
    handleSave() {
      this.$refs.patientForm.validate(valid => {
        if (!valid) return
        const req = this.form.patientId ? updatePatient(this.form) : addPatient(this.form)
        req.then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadPatients()
        })
      })
    },
    handleDelete(row) {
      this.$confirm(`确认删除患者「${row.patientName}」吗？`, '提示', { type: 'warning' })
        .then(() => delPatient(row.patientId))
        .then(() => {
          this.$message.success('删除成功')
          this.loadPatients()
        })
        .catch(() => {})
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
.filter-bar .el-input {
  margin-right: 8px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
