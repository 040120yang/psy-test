<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="4" v-for="card in cards" :key="card.label">
        <div class="stat-card" :style="{ borderTopColor: card.color }">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="stat-card" style="borderTopColor:#f56c6c">
          <div class="stat-value">{{ todayCount }}</div>
          <div class="stat-label">今日测评</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stat-cards" style="margin-top: 16px;">
      <el-col :span="6">
        <div class="stat-card" style="borderTopColor:#409EFF">
          <div class="stat-value">{{ followTotal }}</div>
          <div class="stat-label">随访总数</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="borderTopColor:#67C23A">
          <div class="stat-value">{{ followDone }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="borderTopColor:#E6A23C">
          <div class="stat-value">{{ followOverdue }}</div>
          <div class="stat-label">逾期未随访</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="borderTopColor:#909399">
          <div class="stat-value">{{ followRate }}%</div>
          <div class="stat-label">完成率</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header">各量表测评次数分布</div>
          <div ref="scaleChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div slot="header">测评结果等级分布</div>
          <div ref="levelChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-row">
      <div slot="header">近 7 天测评趋势</div>
      <div ref="trendChart" class="chart"></div>
    </el-card>
  </div>
</template>

<script>
import { getStats } from '@/api/dashboard'
import * as echarts from 'echarts'

export default {
  name: 'Dashboard',
  data() {
    return {
      cards: [
        { label: '用户总数', value: 0, color: '#409EFF' },
        { label: '测评总次数', value: 0, color: '#67C23A' },
        { label: '量表总数', value: 0, color: '#E6A23C' },
        { label: '患者总数', value: 0, color: '#909399' }
      ],
      todayCount: 0,
      followTotal: 0,
      followDone: 0,
      followOverdue: 0
    }
  },
  computed: {
    followRate() {
      return this.followTotal > 0 ? Math.round(this.followDone * 100 / this.followTotal) : 0
    }
  },
  mounted() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      getStats().then(res => {
        const data = res.data
        this.cards[0].value = data.userCount
        this.cards[1].value = data.recordCount
        this.cards[2].value = data.scaleCount
        this.cards[3].value = data.patientCount
        this.todayCount = data.todayCount
        this.followTotal = data.followTotal || 0
        this.followDone = data.followDone || 0
        this.followOverdue = data.followOverdue || 0
        this.renderScaleChart(data.scaleDist || [])
        this.renderLevelChart(data.levelDist || [])
        this.renderTrendChart(data.weekTrend || [])
      })
    },
    renderScaleChart(dist) {
      const chart = echarts.init(this.$refs.scaleChart)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: dist.map(d => d.name) },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{
          type: 'bar',
          barWidth: 40,
          data: dist.map(d => d.value),
          itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] }
        }]
      })
    },
    renderLevelChart(dist) {
      const chart = echarts.init(this.$refs.levelChart)
      // 按严重程度统一配色：正常=绿 / 轻度=黄 / 中度=橙 / 重度=红，未识别等级兜底灰色
      const levelColor = name => {
        if (name === '正常') return '#67C23A'
        if (name.indexOf('重度睡眠') !== -1) return '#B22222'  //砖红
        if (name.indexOf('重度焦虑') !== -1) return '#8B0000'  //深红
        if (name.indexOf('重度抑郁') !== -1) return '#FF8A80'  //浅红
        if (name.indexOf('重度') !== -1) return '#ff4d4f'     //中红
        if (name.indexOf('中度睡眠') !== -1) return '#FFA940' //亮橙
        if (name.indexOf('中度焦虑') !== -1) return '#E86F0C' //深橙
        if (name.indexOf('中度抑郁') !== -1) return '#FF9800' //通用橙
        if (name.indexOf('中度') !== -1) return '#FA8C16'     
        if (name.indexOf('轻度睡眠') !== -1) return '#FFEB3B' //亮黄
        if (name.indexOf('轻度焦虑') !== -1) return '#FFD700' //中黄
        if (name.indexOf('轻度抑郁') !== -1) return '#F5C518' //温和黄
        if (name.indexOf('轻度') !== -1) return '#E6A23C'
        return '#909399'
      }
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          data: dist.map(d => ({
            name: d.name,
            value: d.value,
            itemStyle: { color: levelColor(d.name) }
          })),
          label: { formatter: '{b}: {c}' }
        }]
      })
    },
    renderTrendChart(trend) {
      const chart = echarts.init(this.$refs.trendChart)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 20, top: 30, bottom: 30 },
        xAxis: { type: 'category', data: trend.map(d => d.day) },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.15 },
          data: trend.map(d => d.value),
          itemStyle: { color: '#67C23A' }
        }]
      })
    }
  }
}
</script>

<style scoped>
.stat-cards {
  margin-bottom: 16px;
}
.stat-card {
  background: #fff;
  border-radius: 6px;
  border-top: 3px solid #409EFF;
  padding: 18px;
  text-align: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
}
.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #333;
}
.stat-label {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}
.chart-row {
  margin-bottom: 16px;
}
.chart {
  height: 300px;
}
</style>
