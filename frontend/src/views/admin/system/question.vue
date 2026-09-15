<template>
  <div class="question-page">
    <el-card shadow="never">
      <div slot="header" class="toolbar">
        <div class="filter-bar">
          <el-select v-model="query.scaleId" placeholder="选择量表" clearable style="width:200px" @change="loadQuestions">
            <el-option v-for="s in scales" :key="s.scaleId" :label="s.scaleName" :value="s.scaleId" />
          </el-select>
          <el-input v-model="query.content" placeholder="题目内容" clearable style="width:200px" @keyup.enter.native="loadQuestions" />
          <el-button type="primary" icon="el-icon-search" @click="loadQuestions">查询</el-button>
        </div>
        <el-button type="primary" icon="el-icon-plus" @click="openDialog()">新增题目</el-button>
      </div>

      <el-table :data="questions" border v-loading="loading" size="small">
        <el-table-column prop="questionId" label="ID" width="70" align="center" />
        <el-table-column prop="scaleName" label="所属量表" width="140" />
        <el-table-column prop="sortNo" label="题号" width="70" align="center" />
        <el-table-column prop="content" label="题目内容" min-width="300" />
        <el-table-column label="反向计分" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.reverseFlag === 1 ? 'warning' : 'info'" size="small">
              {{ scope.row.reverseFlag === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center">
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
        @current-change="loadQuestions"
      />
    </el-card>

    <el-dialog :title="form.questionId ? '编辑题目' : '新增题目'" :visible.sync="dialogVisible" width="580px">
      <el-form ref="questionForm" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属量表" prop="scaleId">
          <el-select v-model="form.scaleId" style="width:100%" placeholder="请选择量表">
            <el-option v-for="s in scales" :key="s.scaleId" :label="s.scaleName" :value="s.scaleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="题号" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="1" />
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="反向计分">
          <el-switch v-model="reverseFlag" active-text="是" inactive-text="否" />
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
import { listScale, listQuestion, addQuestion, updateQuestion, delQuestion } from '@/api/system'

export default {
  name: 'QuestionManage',
  data() {
    return {
      scales: [],
      questions: [],
      total: 0,
      loading: false,
      query: { pageNum: 1, pageSize: 10, scaleId: Number(this.$route.query.scaleId) || null, content: '' },
      dialogVisible: false,
      form: {},
      reverseFlag: false,
      rules: {
        scaleId: [{ required: true, message: '请选择量表', trigger: 'change' }],
        content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }]
      }
    }
  },
  created() {
    listScale({ pageNum: 1, pageSize: 100 }).then(res => { this.scales = res.rows || [] })
    this.loadQuestions()
  },
  methods: {
    loadQuestions() {
      this.loading = true
      listQuestion(this.query).then(res => {
        this.questions = res.rows || []
        this.total = res.total || 0
      }).finally(() => { this.loading = false })
    },
    openDialog(row) {
      this.form = row ? { ...row } : { scaleId: this.query.scaleId, sortNo: 1 }
      this.reverseFlag = this.form.reverseFlag === 1
      this.dialogVisible = true
    },
    handleSave() {
      this.$refs.questionForm.validate(valid => {
        if (!valid) return
        this.form.reverseFlag = this.reverseFlag ? 1 : 0
        const req = this.form.questionId ? updateQuestion(this.form) : addQuestion(this.form)
        req.then(() => {
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadQuestions()
        })
      })
    },
    handleDelete(row) {
      this.$confirm('确认删除该题目吗？', '提示', { type: 'warning' })
        .then(() => delQuestion(row.questionId))
        .then(() => {
          this.$message.success('删除成功')
          this.loadQuestions()
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
.filter-bar .el-select,
.filter-bar .el-input {
  margin-right: 8px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
