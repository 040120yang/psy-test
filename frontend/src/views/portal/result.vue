<template>
  <div class="result-page">
    <el-card shadow="never" v-if="record">
      <div slot="header">
        <span class="result-title">测评报告 - {{ record.scaleName }}</span>
        <el-tag size="small" style="margin-left:8px">{{ record.scaleCode }}</el-tag>
      </div>

      <!-- 结果概览 -->
      <div class="result-overview">
        <div class="score-box" :style="{ borderColor: levelColor }">
          <div class="score-label">标准分</div>
          <div class="score-value" :style="{ color: levelColor }">{{ record.stdScore }}</div>
          <div class="score-level" :style="{ background: levelColor }">{{ record.level }}</div>
        </div>
        <div class="score-meta">
          <p>量表：{{ record.scaleName }}（{{ record.scaleCode }}）</p>
          <p>原始粗分：{{ record.rawScore }}</p>
          <p>测评时间：{{ record.createTime }}</p>
        </div>
      </div>

      <!-- 分析建议 -->
      <div class="advice-box">
        <div class="advice-title">结果分析与建议</div>
        <p class="advice-content">{{ record.suggestion }}</p>
        <el-alert
          title="本报告仅作心理健康状况参考，不能替代专业医疗诊断。如有明显不适，请及时前往正规医疗机构心理科/精神科就诊，或拨打全国心理援助热线 12356。"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top:12px"
        />
      </div>

      <!-- 答题明细 -->
      <div class="detail-title">答题明细</div>
      <el-table :data="answers" border size="small" max-height="360">
        <el-table-column type="index" label="题号" width="60" align="center" />
        <el-table-column prop="content" label="题目" min-width="300" />
        <el-table-column label="选项分值" width="100" align="center">
          <template slot-scope="scope">{{ scope.row.optionValue }} 分</template>
        </el-table-column>
      </el-table>

      <div class="result-actions">
        <el-button @click="$router.push('/portal/scales')">返回量表列表</el-button>
        <el-button type="primary" @click="$router.push('/portal/records')">查看我的测评记录</el-button>
      </div>
    </el-card>
    <el-empty v-else description="正在加载报告..." />
  </div>
</template>

<script>
import { getRecordDetail } from '@/api/test'

const LEVEL_COLORS = {
  '正常': '#67C23A',
  '轻度焦虑': '#FFD700',
  '中度焦虑': '#E86F0C',
  '重度焦虑': '#8B0000',
  '轻度抑郁': '#F5C518',
  '中度抑郁': '#FF9800',
  '重度抑郁': '#FF8A80',
  '轻度症状': '#E6A23C',
  '中度症状': '#F56C6C',
  '重度症状': '#d81e06',
  '轻度睡眠问题': '#FFEB3B',
  '中度睡眠问题': '#FFA940',
  '重度睡眠问题': '#B22222'
}

export default {
  name: 'PortalResult',
  data() {
    return {
      recordId: Number(this.$route.query.recordId),
      record: null,
      answers: []
    }
  },
  computed: {
    levelColor() {
      return LEVEL_COLORS[this.record.level] || '#909399'
    }
  },
  created() {
    this.loadDetail()
  },
  methods: {
    loadDetail() {
      getRecordDetail(this.recordId).then(res => {
        this.record = res.data.record
        this.answers = res.data.answers || []
      })
    }
  }
}
</script>

<style scoped>
.result-title {
  font-size: 16px;
  font-weight: bold;
}
.result-overview {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.score-box {
  width: 160px;
  border: 2px solid #67c23a;
  border-radius: 8px;
  text-align: center;
  padding: 16px 0;
  margin-right: 30px;
}
.score-label {
  color: #909399;
  font-size: 13px;
}
.score-value {
  font-size: 34px;
  font-weight: bold;
  margin: 4px 0;
}
.score-level {
  display: inline-block;
  color: #fff;
  padding: 2px 14px;
  border-radius: 12px;
  font-size: 13px;
}
.score-meta p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}
.advice-box {
  background: #f8f9fb;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
}
.advice-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}
.advice-content {
  margin: 0;
  color: #606266;
  line-height: 1.9;
}
.detail-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}
.result-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
