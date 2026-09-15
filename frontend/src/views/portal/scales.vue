<template>
  <div class="scale-list">
    <el-alert
      title="请选择下方量表进行心理测评。测评结果仅作为心理健康状况的参考提示，不能替代专业医疗诊断。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom:16px"
    />
    <el-row :gutter="16">
      <el-col :span="8" v-for="scale in scales" :key="scale.scaleId" style="margin-bottom:16px">
        <el-card shadow="hover" class="scale-card">
          <div class="scale-header">
            <span class="scale-name">{{ scale.scaleName }}</span>
            <el-tag size="small" type="primary">{{ scale.scaleCode }}</el-tag>
          </div>
          <p class="scale-desc">{{ scale.description }}</p>
          <div class="scale-footer">
            <span class="scale-meta">共 {{ scale.questionCount || 20 }} 题 · {{ scale.optionType === 4 ? '四级评分' : '五级评分' }}</span>
            <el-button type="primary" size="small" @click="startTest(scale)">开始测评</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!scales.length" description="暂无可用的测评量表" />
  </div>
</template>

<script>
import { listEnabledScales } from '@/api/test'

export default {
  name: 'PortalScales',
  data() {
    return {
      scales: []
    }
  },
  created() {
    this.loadScales()
  },
  methods: {
    loadScales() {
      listEnabledScales().then(res => {
        this.scales = res.data || []
      })
    },
    startTest(scale) {
      this.$router.push({ path: '/portal/answer', query: { scaleId: scale.scaleId } })
    }
  }
}
</script>

<style scoped>
.scale-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.scale-name {
  font-size: 17px;
  font-weight: bold;
  color: #303133;
}
.scale-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
  min-height: 54px;
  margin: 0 0 12px;
}
.scale-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.scale-meta {
  color: #909399;
  font-size: 12px;
}
</style>
