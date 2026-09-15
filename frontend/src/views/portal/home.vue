<template>
  <div class="portal-home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>欢迎您，{{ user.nickname || user.username }}！</h2>
        <p>关注心理健康，从一次测评开始。以下是为您准备的测评工作台。</p>
      </div>
      <div class="welcome-hotline">
        <div class="hotline-title">心理援助热线</div>
        <div class="hotline-num">12356</div>
        <div class="hotline-desc">24 小时全国统一心理援助热线</div>
      </div>
    </div>

    <!-- 数据统计 -->
    <div class="section-title">我的数据统计</div>
    <el-row :gutter="16">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">测评总次数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color:#67C23A">{{ stats.normal }}</div>
          <div class="stat-label">结果正常</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value" style="color:#F56C6C">{{ stats.attention }}</div>
          <div class="stat-label">需关注结果</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-value stat-time">{{ stats.lastTime }}</div>
          <div class="stat-label">最近测评时间</div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <div class="section-title">快捷操作</div>
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="quick-card" @click="$router.push('/portal/scales')">
          <div class="quick-icon" style="background:#409eff">
            <i class="el-icon-notebook-2"></i>
          </div>
          <div class="quick-info">
            <div class="quick-name">开始测评</div>
            <div class="quick-desc">选择量表进行心理健康测评</div>
          </div>
          <i class="el-icon-arrow-right quick-arrow"></i>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="quick-card" @click="$router.push('/portal/records')">
          <div class="quick-icon" style="background:#67c23a">
            <i class="el-icon-document"></i>
          </div>
          <div class="quick-info">
            <div class="quick-name">我的测评记录</div>
            <div class="quick-desc">查看历史测评报告与答题明细</div>
          </div>
          <i class="el-icon-arrow-right quick-arrow"></i>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="quick-card" @click="$router.push('/portal/coze')">
          <div class="quick-icon" style="background:#764ba2">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="quick-info">
            <div class="quick-name">AI 心理助手</div>
            <div class="quick-desc">与扣子 AI 心理助手在线对话</div>
          </div>
          <i class="el-icon-arrow-right quick-arrow"></i>
        </div>
      </el-col>
    </el-row>

    <!-- 平台概览 -->
    <div class="section-title">平台概览</div>
    <div class="overview-card">
      <el-row>
        <el-col :span="8">
          <div class="overview-item">
            <div class="ov-label">系统版本</div>
            <div class="ov-value">v1.0.0</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overview-item">
            <div class="ov-label">测评量表</div>
            <div class="ov-value">SAS / SDS / SCL-90 / SRSS</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overview-item">
            <div class="ov-label">心理援助</div>
            <div class="ov-value">全国热线 12356</div>
          </div>
        </el-col>
      </el-row>
      <div class="overview-note">
        本平台测评结果仅作为心理健康状况的参考提示，不能替代专业医疗诊断。如有明显不适，请及时前往正规医疗机构心理科/精神科就诊。
      </div>
    </div>
  </div>
</template>

<script>
import { listMyRecords } from '@/api/test'
import { getUser } from '@/utils/auth'

export default {
  name: 'PortalHome',
  data() {
    return {
      user: getUser() || {},
      stats: { total: 0, normal: 0, attention: 0, lastTime: '--' }
    }
  },
  created() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      listMyRecords({ pageNum: 1, pageSize: 100 }).then(res => {
        const rows = res.rows || []
        const total = res.total || 0
        let normal = 0
        let attention = 0
        rows.forEach(r => {
          if (r.level && r.level.indexOf('正常') !== -1) {
            normal++
          } else if (r.level) {
            attention++
          }
        })
        this.stats.total = total
        this.stats.normal = normal
        this.stats.attention = attention
        if (rows.length) {
          this.stats.lastTime = rows[0].createTime
        }
      })
    }
  }
}
</script>

<style scoped>
.portal-home {
  width: 100%;
}
.welcome-banner {
  background: linear-gradient(120deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  color: #fff;
  padding: 32px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.welcome-text h2 {
  margin: 0 0 8px;
  font-size: 22px;
}
.welcome-text p {
  margin: 0;
  opacity: 0.9;
  font-size: 13px;
}
.welcome-hotline {
  text-align: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 10px 24px;
}
.hotline-title {
  font-size: 12px;
  opacity: 0.9;
}
.hotline-num {
  font-size: 28px;
  font-weight: bold;
  letter-spacing: 2px;
}
.hotline-desc {
  font-size: 11px;
  opacity: 0.85;
}
.section-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin: 20px 0 12px;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px 12px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}
.stat-time {
  font-size: 14px;
  line-height: 28px;
}
.stat-label {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}
.quick-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  transition: box-shadow 0.2s;
}
.quick-card:hover {
  box-shadow: 0 4px 12px rgba(0, 21, 41, 0.12);
}
.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}
.quick-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.quick-desc {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}
.quick-arrow {
  margin-left: auto;
  color: #c0c4cc;
  font-size: 18px;
}
.overview-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.overview-item {
  text-align: center;
  padding: 10px 0;
}
.ov-label {
  color: #909399;
  font-size: 12px;
  margin-bottom: 6px;
}
.ov-value {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}
.overview-note {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #ebeef5;
  color: #909399;
  font-size: 12px;
  line-height: 1.8;
}
</style>
