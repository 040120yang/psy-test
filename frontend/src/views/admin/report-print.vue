<template>
  <div v-loading="loading" class="print-page">
    <div class="print-toolbar no-print">
      <span class="print-tip">共 {{ sheets.length }} 份报告；在弹出的打印对话框中选择「另存为 PDF」即可导出电子版</span>
      <el-button @click="$router.back()">返回</el-button>
      <el-button type="primary" @click="doPrint"><el-icon><Printer /></el-icon> 打印 / 另存为 PDF</el-button>
    </div>

    <div v-for="sheet in sheets" :key="sheet.record.recordId" class="print-sheet">
      <header class="sheet-head">
        <div>
          <h1>心理测评报告</h1>
          <p>校园心理健康智能测评与随访系统</p>
        </div>
        <div class="sheet-no">报告编号：R{{ sheet.record.recordId }}</div>
      </header>

      <table class="sheet-table">
        <tbody>
          <tr><th>学生姓名</th><td>{{ sheet.record.nickname || sheet.record.username || '-' }}</td><th>登录账号</th><td>{{ sheet.record.username || '-' }}</td></tr>
          <tr><th>测评量表</th><td>{{ sheet.record.scaleName }}（{{ sheet.record.scaleCode }}）</td><th>测评时间</th><td>{{ sheet.record.createTime }}</td></tr>
          <tr><th>原始粗分</th><td>{{ sheet.record.rawScore }}</td><th>标准分</th><td>{{ sheet.record.stdScore }}</td></tr>
          <tr><th>结果等级</th><td colspan="3">{{ sheet.record.level }}</td></tr>
        </tbody>
      </table>

      <h2>系统评估建议</h2>
      <p class="sheet-text">{{ sheet.record.suggestion || '测评已完成，请结合近期情绪、睡眠和生活状态综合判断。' }}</p>

      <h2>心理教师评估结论</h2>
      <p class="sheet-text">{{ sheet.record.doctorConclusion || '（尚未填写）' }}</p>

      <h2>心理教师关怀建议</h2>
      <p class="sheet-text">{{ sheet.record.doctorAdvice || '（尚未填写）' }}</p>

      <h2>答题明细</h2>
      <table class="sheet-table answers">
        <thead><tr><th style="width:60px">题号</th><th>题目</th><th style="width:90px">选项分值</th></tr></thead>
        <tbody>
          <tr v-for="(a, i) in sheet.answers" :key="a.id || i"><td>{{ i + 1 }}</td><td>{{ a.content }}</td><td>{{ a.optionValue }}</td></tr>
        </tbody>
      </table>

      <footer class="sheet-foot">
        <p>本报告仅用于心理健康筛查参考，不能替代专业心理评估与诊断。</p>
        <p>如出现自伤、轻生念头或无法控制情绪，请立即联系家人陪同就医，或拨打全国心理援助热线 12356。</p>
        <p class="sheet-sign">心理教师签字：________________　　日期：________________</p>
      </footer>
    </div>

    <EmptyPanel v-if="!sheets.length && !loading" title="无法读取报告" description="记录可能已被删除或无权访问" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { getRecordDetail } from '@/api/test'

const route = useRoute()
const loading = ref(true)
const sheets = ref([])

const ids = computed(() => {
  const many = route.query.recordIds
  if (many) {
    return String(many).split(',').map(s => Number(s)).filter(n => n > 0)
  }
  const single = Number(route.query.recordId)
  return single > 0 ? [single] : []
})

function doPrint() { window.print() }

onMounted(async () => {
  try {
    const list = []
    for (const id of ids.value) {
      try {
        const res = await getRecordDetail(id)
        const record = (res.data || {}).record
        if (record) list.push({ record, answers: (res.data || {}).answers || [] })
      } catch (e) {
        // 单条读取失败不影响其余报告
      }
    }
    sheets.value = list
  } finally {
    loading.value = false
    if (sheets.value.length) setTimeout(doPrint, 500)
  }
})
</script>
<style scoped>
.print-page{padding:8px 0}
.print-toolbar{display:flex;align-items:center;justify-content:flex-end;gap:12px;margin-bottom:16px}
.print-tip{margin-right:auto;color:var(--text-3);font-size:12px}
.print-sheet{max-width:900px;margin:0 auto;background:#fff;border:1px solid var(--line);border-radius:12px;padding:38px 42px}
.print-sheet+.print-sheet{margin-top:24px}
.sheet-head{display:flex;align-items:flex-start;justify-content:space-between;border-bottom:2px solid #2f7d7a;padding-bottom:14px}
.sheet-head h1{margin:0;font-size:22px;color:#12344c}
.sheet-head p{margin:6px 0 0;color:var(--text-3);font-size:12px}
.sheet-no{color:var(--text-3);font-size:12px}
.print-sheet h2{font-size:15px;margin:26px 0 10px;padding-left:10px;border-left:3px solid #2f7d7a;color:#12344c}
.sheet-table{width:100%;border-collapse:collapse;font-size:13px}
.sheet-table th,.sheet-table td{border:1px solid #dfe6ee;padding:9px 12px;text-align:left;vertical-align:top}
.sheet-table th{background:#f6f9fc;font-weight:700;width:120px;color:var(--text-2)}
.answers th{width:auto}
.sheet-text{margin:0;font-size:13px;line-height:1.9;color:var(--text-2);white-space:pre-wrap}
.sheet-foot{margin-top:30px;padding-top:14px;border-top:1px dashed #dfe6ee;color:var(--text-3);font-size:12px;line-height:1.9}
.sheet-sign{margin-top:18px;color:var(--text-2)}
</style>
<style>
@media print {
  .admin-aside, .admin-header, .no-print, .el-loading-mask { display: none !important; }
  .admin-main { margin-left: 0 !important; }
  .admin-content { padding: 0 !important; max-width: none !important; }
  .print-sheet { border: 0 !important; border-radius: 0 !important; padding: 0 !important; max-width: none !important; page-break-after: always; }
  .print-sheet:last-child { page-break-after: auto; }
  .print-sheet + .print-sheet { margin-top: 0 !important; }
  .print-page { padding: 0 !important; }
  body { background: #fff !important; }
}
</style>
