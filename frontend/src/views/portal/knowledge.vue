<template>
  <div class="knowledge-page">
    <el-card shadow="never">
      <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
        <span style="font-size:18px;font-weight:600;">心理知识库</span>
        <div style="display:flex;align-items:center;gap:12px;">
          <el-input v-model="keyword" placeholder="搜索文章标题" size="small" style="width:200px;" @keyup.enter.native="load" clearable>
            <el-button slot="append" icon="el-icon-search" @click="load" />
          </el-input>
          <el-radio-group v-model="category" size="small" @change="load">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="科普">科普</el-radio-button>
            <el-radio-button label="指南">指南</el-radio-button>
            <el-radio-button label="案例">案例</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <el-row :gutter="16">
        <el-col :span="8" v-for="k in list" :key="k.id" style="margin-bottom:16px;">
          <el-card shadow="hover" class="article-card" @click="showDetail(k)">
            <h3 style="margin-top:0;">{{ k.title }}</h3>
            <p style="color:#909399;font-size:13px;line-height:1.6;">{{ k.summary }}</p>
            <div style="display:flex;justify-content:space-between;color:#909399;font-size:12px;">
              <span>{{ k.author }}</span>
              <span>浏览 {{ k.viewCount }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog title="文章详情" :visible.sync="detailVisible" width="700px">
      <h2 style="margin-top:0;">{{ current.title }}</h2>
      <p style="color:#909399;font-size:13px;">{{ current.author }} · {{ current.createTime }}</p>
      <div style="line-height:1.9;white-space:pre-wrap;">{{ current.content }}</div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
export default {
  name: 'PortalKnowledge',
  data() {
    return { list: [], category: '', keyword: '', detailVisible: false, current: {} }
  },
  mounted() { this.load() },
  methods: {
    load() {
      request({ url: '/business/knowledge/list', method: 'get', params: { category: this.category } }).then(res => {
        let list = res.rows || []
        if (this.keyword) {
          list = list.filter(k => k.title.includes(this.keyword))
        }
        this.list = list
      })
    },
    showDetail(row) {
      request({ url: '/business/knowledge/' + row.id, method: 'get' }).then(res => {
        this.current = res.data
        this.detailVisible = true
      })
    }
  }
}
</script>

<style scoped>
.article-card { cursor: pointer; }
.article-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
</style>
