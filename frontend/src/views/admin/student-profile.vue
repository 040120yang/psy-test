<template>
  <div v-loading="loading" class="page-stack">
    <PageHeader eyebrow="Student Profile" :title="studentName || '个案档案'" :description="headerDesc">
      <template #actions>
        <el-button type="primary" @click="$router.back()">返回</el-button>
      </template>
    </PageHeader>

    <template v-if="loaded">
      <section class="soft-grid">
        <StatCard label="测评次数" :value="summary.recordCount || 0" icon="Document" color="var(--primary)" />
        <StatCard label="中/重度次数" :value="summary.riskCount || 0" icon="Warning" color="var(--danger)" />
        <StatCard label="随访次数" :value="summary.followCount || 0" icon="Bell" color="var(--accent)" />
        <StatCard label="沟通条数" :value="summary.chatCount || 0" icon="ChatLineSquare" color="var(--success)" />
      </section>

      <section class="panel">
        <div class="panel-head"><div><div class="panel-title">基本信息</div><div class="panel-subtitle">学生档案与心理关注信息</div></div><StatusTag :text="riskTag" /></div>
        <div class="panel-body">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="姓名">{{ student.patientName || student.studentName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="登录账号">{{ student.studentUsername || '-' }}</el-descriptions-item>
            <el-descriptions-item label="负责教师"><template v-if="student.teacherName">{{ student.teacherName }}</template><el-tag v-else size="small" type="warning" effect="plain">待分配</el-tag></el-descriptions-item>
            <el-descriptions-item label="性别">{{ sexText }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ student.age || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ student.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="关注标签"><el-tag v-if="student.tag" effect="plain">{{ student.tag }}</el-tag><span v-else class="text-muted">-</span></el-descriptions-item>
            <el-descriptions-item label="最近测评">{{ summary.lastTestTime ? fmtFull(summary.lastTestTime) : '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="建档时间">{{ student.createTime ? fmtFull(student.createTime) : '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系地址" :span="3">{{ student.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="既往经历/主要困扰" :span="3">{{ student.medicalHistory || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head"><div><div class="panel-title">测评趋势</div><div class="panel-subtitle">按量表分组的历次标准分变化</div></div></div>
        <div class="panel-body"><div v-show="chartReady" ref="trendChart" class="chart" /><div v-if="!chartReady" class="todo-empty">{{ records.length ? '测评记录缺少有效时间或分数，无法绘制趋势' : '暂无测评记录' }}</div></div>
      </section>

      <section class="panel">
        <div class="panel-head"><div><div class="panel-title">测评记录与心理教师评估</div><div class="panel-subtitle">共 {{ records.length }} 条</div></div></div>
        <div class="panel-body">
          <el-table v-if="records.length" :data="records" size="small">
            <el-table-column label="测评时间" width="150"><template #default="{row}">{{ fmtFull(row.createTime) }}</template></el-table-column>
            <el-table-column prop="scaleName" label="量表" min-width="130" />
            <el-table-column prop="stdScore" label="标准分" width="80" align="center" />
            <el-table-column label="等级" width="120"><template #default="{row}"><StatusTag :text="row.level" /></template></el-table-column>
            <el-table-column label="评估结论" min-width="180"><template #default="{row}"><span v-if="row.doctorConclusion">{{ row.doctorConclusion }}</span><el-tag v-else size="small" type="warning" effect="plain">待评估</el-tag></template></el-table-column>
            <el-table-column prop="doctorAdvice" label="关怀建议" min-width="200" show-overflow-tooltip />
          </el-table>
          <div v-else class="todo-empty">暂无测评记录</div>
        </div>
      </section>

      <section class="soft-grid cols-2">
        <div class="panel">
          <div class="panel-head"><div><div class="panel-title">随访记录</div><div class="panel-subtitle">共 {{ followUps.length }} 条</div></div></div>
          <div class="panel-body">
            <el-table v-if="followUps.length" :data="followUps" size="small" max-height="320">
              <el-table-column prop="followDate" label="随访日期" width="110" />
              <el-table-column prop="followType" label="方式" width="70" />
              <el-table-column label="状态" width="100"><template #default="{row}"><StatusTag :text="row.status" /></template></el-table-column>
              <el-table-column prop="doctorNote" label="心理教师记录" min-width="160" show-overflow-tooltip />
            </el-table>
            <div v-else class="todo-empty">暂无随访记录</div>
          </div>
        </div>
        <div class="panel">
          <div class="panel-head"><div><div class="panel-title">沟通记录</div><div class="panel-subtitle">最近 {{ chats.length }} 条</div></div></div>
          <div class="panel-body">
            <div v-if="chats.length" class="profile-chat">
              <div v-for="m in chats" :key="m.id" class="chat-row" :class="{ mine: m.senderRole === 'teacher' }">
                <div class="chat-avatar">{{ m.senderRole === 'teacher' ? '师' : '学' }}</div>
                <div class="chat-body"><div class="chat-meta">{{ m.senderName || (m.senderRole === 'teacher' ? '心理教师' : '学生') }} · {{ m.createTime }}</div><div class="chat-bubble">{{ m.content }}</div></div>
              </div>
            </div>
            <div v-else class="todo-empty">暂无沟通记录</div>
          </div>
        </div>
      </section>
    </template>

    <EmptyPanel v-else-if="!loading" title="无法读取个案档案" description="该学生不存在、未关联账号，或你无权查看（仅负责教师或系统管理员可查看）" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { getStudentProfile } from '@/api/student'

const route = useRoute()
const loading = ref(true)
const loaded = ref(false)
const student = ref({})
const summary = ref({})
const records = ref([])
const followUps = ref([])
const chats = ref([])
const trendChart = ref(null)
const chartReady = ref(false)
let chart = null

const studentName = computed(() => student.value.patientName || student.value.studentName || '')
const headerDesc = computed(() => student.value.studentId
  ? `个案档案：${student.value.studentUsername || ''}${student.value.teacherName ? ' · 负责教师：' + student.value.teacherName : ' · 尚未指派负责教师'}`
  : '')
const sexText = computed(() => ({ '0': '男', '1': '女', '2': '未知' }[student.value.sex] || '未知'))
const riskTag = computed(() => {
  const n = summary.value.riskCount || 0
  if (!n) return '暂无风险测评'
  return `中/重度 ${n} 次`
})

/** 把后端时间（ISO，如 2026-09-15T10:03:11）转成时间戳，无法解析返回 null */
function toTs(v) {
  const t = Date.parse(String(v == null ? '' : v).replace(' ', 'T'))
  return Number.isNaN(t) ? null : t
}

/** x 轴刻度文案：09-15 10:03 */
function fmtAxis(v) {
  if (v instanceof Date) {
    const p = n => String(n).padStart(2, '0')
    return `${p(v.getMonth() + 1)}-${p(v.getDate())} ${p(v.getHours())}:${p(v.getMinutes())}`
  }
  return String(v == null ? '' : v).replace('T', ' ').slice(5, 16)
}

/** 完整时间文案：2026-09-15 10:03:11 */
function fmtFull(v) {
  return String(v == null ? '' : v).replace('T', ' ').slice(0, 19)
}

async function renderChart() {
  // 每次测评作为一个数据点（精确到分钟），不按"日"聚合，
  // 否则同一天的多次测评会互相覆盖，折线只剩孤立点甚至空白。
  const points = records.value
    .map(r => ({ r, ts: toTs(r.createTime) }))
    .filter(x => x.ts !== null && x.r.stdScore !== null && x.r.stdScore !== undefined)
    .sort((a, b) => a.ts - b.ts)

  chartReady.value = points.length > 0
  // 等容器由 v-show 恢复显示后再初始化，否则 ECharts 取到 0 尺寸
  await nextTick()
  if (chart) { chart.dispose(); chart = null }
  if (!points.length || !trendChart.value) return
  chart = echarts.init(trendChart.value)

  const groups = new Map()
  points.forEach(({ r, ts }) => {
    const key = r.scaleName || r.scaleCode || '其他'
    if (!groups.has(key)) groups.set(key, [])
    groups.get(key).push({ ts, score: Number(r.stdScore), level: r.level, timeText: fmtAxis(r.createTime) })
  })

  const palette = ['#2f7d7a', '#f39a4a', '#7c5cb0', '#e5484d', '#3b82f6']
  let index = 0
  const series = []
  groups.forEach((list, name) => {
    const color = palette[index % palette.length]
    index++
    series.push({
      name,
      type: 'line',
      smooth: true,
      showSymbol: true,
      symbolSize: 9,
      emphasis: { focus: 'series' },
      lineStyle: { width: 3, color },
      itemStyle: { color },
      // 点跟随系列颜色，与折线和图例保持一致；风险等级在提示框中给出
      data: list.map(p => ({ value: [p.ts, p.score], level: p.level, timeText: p.timeText }))
    })
  })

  chart.setOption({
    color: palette,
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const arr = Array.isArray(params) ? params : [params]
        if (!arr.length) return ''
        const head = (arr[0].data && arr[0].data.timeText) || fmtAxis(new Date(arr[0].value[0]))
        const rows = arr.map(p => `${p.marker}${p.seriesName}：${p.value[1]} 分（${(p.data && p.data.level) || '-'}）`)
        return head + '<br/>' + rows.join('<br/>')
      }
    },
    legend: { bottom: 0, icon: 'circle' },
    grid: { left: 46, right: 26, top: 26, bottom: 54 },
    xAxis: {
      type: 'time',
      axisLabel: { formatter: '{MM}-{dd} {HH}:{mm}', hideOverlap: true },
      axisLine: { lineStyle: { color: '#dbe3ec' } },
      splitLine: { show: false }
    },
    yAxis: { type: 'value', name: '标准分', splitLine: { lineStyle: { color: '#eef2f7' } } },
    series
  })
}

function resize() { if (chart) chart.resize() }

async function load() {
  loading.value = true
  try {
    const res = await getStudentProfile(Number(route.query.studentId))
    const data = res.data || {}
    student.value = data.student || {}
    summary.value = data.summary || {}
    records.value = data.records || []
    followUps.value = data.followUps || []
    chats.value = data.chats || []
    loaded.value = true
    await nextTick()
    await renderChart()
  } catch (e) {
    loaded.value = false
  } finally {
    loading.value = false
  }
}

onMounted(() => { load(); window.addEventListener('resize', resize) })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); if (chart) chart.dispose() })
</script>
<style scoped>
.chart{height:300px}
.todo-empty{padding:28px 0;text-align:center;color:var(--text-3);font-size:13px}
.profile-chat{max-height:320px;overflow-y:auto;display:flex;flex-direction:column;gap:14px;padding:2px}
.chat-row{display:flex;gap:10px;align-items:flex-start}
.chat-row.mine{flex-direction:row-reverse}
.chat-avatar{flex-shrink:0;width:32px;height:32px;display:grid;place-items:center;border-radius:50%;background:var(--primary-soft);color:var(--primary);font-size:12px;font-weight:700}
.chat-row.mine .chat-avatar{background:#e8eef4;color:var(--text-2)}
.chat-body{max-width:78%}
.chat-row.mine .chat-body{text-align:right}
.chat-meta{font-size:12px;color:var(--text-3);margin-bottom:4px}
.chat-bubble{display:inline-block;text-align:left;padding:9px 12px;border-radius:12px;background:#fff;border:1px solid var(--line);color:var(--text);font-size:13px;line-height:1.8;white-space:pre-wrap;word-break:break-word}
.chat-row.mine .chat-bubble{background:var(--primary-soft);border-color:transparent}
</style>
