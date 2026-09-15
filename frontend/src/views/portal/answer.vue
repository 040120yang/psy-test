<template>
  <div class="answer-page">
    <el-card shadow="never">
      <div slot="header" class="answer-header">
        <span>{{ scale ? scale.scaleName : '心理测评' }}</span>
        <span class="answer-progress">已完成 {{ answeredCount }} / {{ questions.length }} 题</span>
      </div>

      <div v-if="scale" class="answer-tip">
        <el-alert
          :title="'请根据最近一周的实际情况作答，每题选择一个最符合您的选项（' + (scale.optionType === 4 ? '四级评分' : '五级评分') + '）。'"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>

      <div v-for="(q, index) in questions" :key="q.questionId" class="question-item">
        <div class="question-title">
          <span class="q-no">{{ index + 1 }}</span>
          <span class="q-content">{{ q.content }}</span>
          <el-tag v-if="q.reverseFlag === 1" size="mini" type="info" style="margin-left:8px">反向计分</el-tag>
        </div>
        <el-radio-group v-model="answers[q.questionId]" class="q-options">
          <el-radio v-for="(opt, i) in options" :key="i" :label="i + 1">{{ opt }}</el-radio>
        </el-radio-group>
      </div>

      <div class="submit-bar">
        <el-button size="medium" @click="$router.push('/portal/scales')">返回量表列表</el-button>
        <el-button type="primary" size="medium" :loading="submitting" @click="handleSubmit">提交测评</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getQuestions, listEnabledScales, submitTest } from '@/api/test'

const OPTIONS_4 = ['没有或很少时间', '小部分时间', '相当多时间', '绝大部分或全部时间']
const OPTIONS_5 = ['没有', '很轻', '中等', '偏重', '严重']

export default {
  name: 'PortalAnswer',
  data() {
    return {
      scaleId: Number(this.$route.query.scaleId),
      scale: null,
      questions: [],
      answers: {},
      submitting: false
    }
  },
  computed: {
    answeredCount() {
      return Object.keys(this.answers).filter(k => this.answers[k] !== undefined && this.answers[k] !== null).length
    },
    options() {
      if (!this.scale) return OPTIONS_4
      return this.scale.optionType === 5 ? OPTIONS_5 : OPTIONS_4
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      listEnabledScales().then(res => {
        const list = res.data || []
        this.scale = list.find(s => s.scaleId === this.scaleId) || null
        if (!this.scale) {
          this.$message.error('量表不存在或已停用')
          this.$router.push('/portal/scales')
          return
        }
        return getQuestions(this.scaleId)
      }).then(res => {
        if (res && res.data) {
          this.questions = res.data || []
        }
      })
    },
    handleSubmit() {
      if (!this.questions.length) return
      if (this.answeredCount < this.questions.length) {
        this.$message.warning(`还有 ${this.questions.length - this.answeredCount} 题未作答，请完成全部题目`)
        return
      }
      const answers = this.questions.map(q => ({
        questionId: q.questionId,
        optionValue: this.answers[q.questionId]
      }))
      this.submitting = true
      submitTest({ scaleId: this.scaleId, answers })
        .then(res => {
          const recordId = res.data.record.recordId
          this.$message.success('测评完成')
          this.$router.replace({ path: '/portal/result', query: { recordId } })
        })
        .finally(() => {
          this.submitting = false
        })
    }
  }
}
</script>

<style scoped>
.answer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
}
.answer-progress {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}
.answer-tip {
  margin-bottom: 16px;
}
.question-item {
  padding: 16px 0;
  border-bottom: 1px dashed #ebeef5;
}
.question-title {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.q-no {
  display: inline-block;
  width: 26px;
  height: 26px;
  line-height: 26px;
  text-align: center;
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  font-size: 13px;
  margin-right: 10px;
  flex-shrink: 0;
}
.q-content {
  font-size: 15px;
  color: #303133;
}
.q-options {
  margin-left: 36px;
  display: flex;
  flex-wrap: wrap;
}
.q-options .el-radio {
  margin-right: 24px;
  margin-bottom: 6px;
}
.submit-bar {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
