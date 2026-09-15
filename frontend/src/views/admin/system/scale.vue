<template>
  <div class="scale-page">
    <el-card shadow="never">
      <div slot="header" class="toolbar">
        <div class="filter-bar">
          <el-input v-model="query.scaleName" placeholder="量表名称" clearable style="width:180px" @keyup.enter.native="loadScales" />
          <el-input v-model="query.scaleCode" placeholder="量表编码" clearable style="width:140px" @keyup.enter.native="loadScales" />
          <el-button type="primary" icon="el-icon-search" @click="loadScales">查询</el-button>
        </div>
        <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增量表</el-button>
      </div>

      <el-table :data="scales" border v-loading="loading">
        <el-table-column prop="scaleId" label="ID" width="60" align="center" />
        <el-table-column prop="scaleName" label="量表名称" min-width="130" />
        <el-table-column prop="scaleCode" label="编码" width="90" align="center" />
        <el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="评分制" width="90" align="center">
          <template slot-scope="scope">{{ scope.row.optionType === 5 ? '五级' : '四级' }}</template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
              {{ scope.row.status === '0' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="$router.push({ name: 'AdminSystemQuestion', query: { scaleId: scope.row.scaleId } })">题目管理</el-button>
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
        @current-change="loadScales"
      />
    </el-card>

    <el-dialog :title="form.scaleId ? '编辑量表' : '新增量表'" :visible.sync="dialogVisible" width="560px">
      <el-form ref="scaleForm" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="量表名称" prop="scaleName">
          <el-input v-model="form.scaleName" placeholder="如：焦虑自评量表" />
        </el-form-item>
        <el-form-item label="量表编码" prop="scaleCode">
          <el-input v-model="form.scaleCode" placeholder="如：SAS / SDS / SCL90 / SRSS" :disabled="!!form.scaleId" />
        </el-form-item>
        <el-form-item label="评分制" prop="optionType">
          <el-radio-group v-model="form.optionType">
            <el-radio :label="4">四级评分（1-4分）</el-radio>
            <el-radio :label="5">五级评分（1-5分）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="显示顺序">
          <el-input-number v-model="form.sortNo" :min="0" />
        </el-form-item>
        <el-form-item label="量表说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="量表用途与简介" />
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
import { listScale, addScale, updateScale, delScale } from '@/api/system'

export default {
  name: 'ScaleManage',
  data() {
    return {
      scales: [],
      total: 0,
      loading: false,
      query: { pageNum: 1, pageSize: 10, scaleName: '', scaleCode: '' },
      dialogVisible: false,
      form: {},
      rules: {
        scaleName: [{ required: true, message: '请输入量表名称', trigger: 'blur' }],
        scaleCode: [{ required: true, message: '请输入量表编码', trigger: 'blur' }],
        optionType: [{ required: true, message: '请选择评分制', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadScales()
  },
  methods: {
    loadScales() {
      this.loading = true
      listScale(this.query).then(res => {
        this.scales = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    openDialog(row) {
      this.form = row ? { ...row } : { optionType: 4, status: '0', sortNo: 0 }
      this.dialogVisible = true
    },
    handleSave() {
      this.$refs.scaleForm.validate(valid => {
        if (!valid) return
        const req = this.form.scaleId ? updateScale(this.form) : addScale(this.form)
        req.then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadScales()
        })
      })
    },
    handleDelete(row) {
      this.$confirm(`确认删除量表「${row.scaleName}」及其全部题目吗？`, '提示', { type: 'warning' })
        .then(() => delScale(row.scaleId))
        .then(() => {
          this.$message.success('删除成功')
          this.loadScales()
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
