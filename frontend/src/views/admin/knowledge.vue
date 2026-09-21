<template>
  <div class="admin-knowledge">
    <el-card shadow="never">
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span style="font-size:18px;font-weight:600;">知识库管理</span>
        <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增文章</el-button>
      </div>
      <el-table :data="list" border stripe>
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="category" label="分类" width="100" align="center" />
        <el-table-column prop="author" label="作者" width="100" align="center" />
        <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="openEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color:#f56c6c" @click="del(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="edit.id ? '编辑文章' : '新增文章'" :visible.sync="dialogVisible" width="700px">
      <el-form :model="edit" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="edit.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="edit.category">
            <el-option label="科普" value="科普" />
            <el-option label="指南" value="指南" />
            <el-option label="案例" value="案例" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="edit.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="edit.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'AdminKnowledge',
  data() {
    return {
      list: [],
      dialogVisible: false,
      edit: { title: '', category: '科普', summary: '', content: '' }
    }
  },
  mounted() { this.load() },
  methods: {
    load() {
      request({ url: '/business/knowledge/list', method: 'get' }).then(res => {
        this.list = res.rows || []
      })
    },
    openAdd() {
      this.edit = { title: '', category: '科普', summary: '', content: '' }
      this.dialogVisible = true
    },
    openEdit(row) {
      this.edit = { ...row }
      this.dialogVisible = true
    },
    save() {
      const url = this.edit.id ? '/business/knowledge' : '/business/knowledge'
      const method = this.edit.id ? 'put' : 'post'
      request({ url, method, data: this.edit }).then(() => {
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      })
    },
    del(row) {
      this.$confirm('确认删除？', '提示', { type: 'warning' }).then(() => {
        request({ url: '/business/knowledge/' + row.id, method: 'delete' }).then(() => {
          this.$message.success('删除成功')
          this.load()
        })
      })
    }
  }
}
</script>
