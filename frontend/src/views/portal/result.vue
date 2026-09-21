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

      <!-- 医生诊断结论 -->
      <div v-if="record.doctorConclusion" class="advice-box" style="background:#f5f0ff;border-left:4px solid #9b59b6;">
        <div class="advice-title" style="color:#9b59b6;">医生诊断结论</div>
        <p class="advice-content">{{ record.doctorConclusion }}</p>
      </div>
      <div v-if="record.doctorAdvice" class="advice-box" style="background:#fff8f0;border-left:4px solid #e86f0c;">
        <div class="advice-title" style="color:#e86f0c;">医生处方建议</div>
        <p class="advice-content">{{ record.doctorAdvice }}</p>
      </div>

      <!-- AI智能诊断 -->
      <div class="ai-diagnosis-box">
        <div class="ai-title">
          <i class="el-icon-cpu" style="color:#9b59b6;margin-right:6px"></i>
          AI 智能诊断分析
          <el-button v-if="!aiLoading && !aiResult" type="primary" size="mini" style="margin-left:12px" @click="aiDiagnosis">
            生成AI诊断分析
          </el-button>
          <el-button v-if="aiLoading" type="text" loading>正在生成中，请稍候...</el-button>
        </div>
        <div v-if="aiResult" class="ai-result">
          <p>{{ aiResult }}</p>
        </div>
        <div v-if="!aiResult && !aiLoading" class="ai-tip">
          点击按钮，AI将根据您的测评结果生成个性化的诊断分析与建议
        </div>
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
import request from '@/utils/request'

const getLevelColor = (name) => {
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
}

export default {
  name: 'PortalResult',
  data() {
    return {
      recordId: Number(this.$route.query.recordId),
      record: null,
      answers: [],
      aiLoading: false,
      aiResult: ''
    }
  },
  computed: {
    levelColor() {
      return getLevelColor(this.record.level)
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
    },
    aiDiagnosis() {
      this.aiLoading = true
      const prompt = `我刚完成了${this.record.scaleName}测评，标准分是${this.record.stdScore}，结果等级是${this.record.level}。请根据我的测评结果，给出专业的诊断分析、原因分析和具体的改善建议，分点列出。`
      request({
        url: '/coze/chat',
        method: 'post',
        data: { message: prompt }
      }).then(res => {
        this.aiResult = res.reply || res.data || res.msg || 'AI生成分析完成'
      }).catch(() => {
        this.$message.error('AI生成失败，请稍后重试')
      }).finally(() => {
        this.aiLoading = false
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
.ai-diagnosis-box {
  background: linear-gradient(135deg, #f5f0ff 0%, #e8f4ff 100%);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  border-left: 4px solid #9b59b6;
}
.ai-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}
.ai-tip {
  color: #909399;
  font-size: 13px;
}
.ai-result p {
  margin: 0;
  color: #303133;
  line-height: 1.9;
  white-space: pre-wrap;
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
