<template>
  <div class="page-stack">
    <PageHeader eyebrow="Assessment Records" title="测评记录" description="查询全部心理测评记录，查看报告并填写心理评估与关怀建议。" />
    <section class="panel"><div class="panel-head"><el-input v-model="query.username" clearable placeholder="用户名" style="width:180px"/><el-select v-model="query.scaleId" clearable placeholder="全部量表" style="width:180px"><el-option v-for="s in scales" :key="s.scaleId" :label="s.scaleName" :value="s.scaleId"/></el-select><el-button type="primary" @click="load">查询</el-button><el-button :disabled="!rows.length" @click="exportCsv">导出当前结果</el-button><el-button :disabled="!selected.length" @click="printBatch">批量打印报告（{{ selected.length }}）</el-button></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe @selection-change="onSelect"><el-table-column type="selection" width="46" fixed="left"/><el-table-column prop="username" label="用户" min-width="110"/><el-table-column prop="nickname" label="昵称" min-width="110"/><el-table-column prop="scaleName" label="量表" min-width="150"/><el-table-column prop="stdScore" label="标准分" width="90" align="center"/><el-table-column label="等级" width="130"><template #default="{row}"><StatusTag :text="row.level"/></template></el-table-column><el-table-column prop="createTime" label="测评时间" min-width="170"/><el-table-column label="操作" width="190" fixed="right"><template #default="{row}"><el-button text type="primary" @click="open(row)">查看详情</el-button><el-button text @click="printOne(row.recordId)">打印报告</el-button></template></el-table-column></el-table><EmptyPanel v-else title="暂无测评记录"/><el-pagination v-if="total" v-model:current-page="query.pageNum" layout="total, prev, pager, next" :total="total" @current-change="load"/></div></section>
    <el-dialog v-model="visible" title="测评记录详情" width="900px"><div v-if="detail.record" class="detail-head"><div><h3>{{ detail.record.scaleName }}</h3><p>{{ detail.record.createTime }}</p></div><StatusTag :text="detail.record.level"/></div><el-descriptions v-if="detail.record" :column="3" border><el-descriptions-item label="用户">{{ detail.record.username || detail.record.userName }}</el-descriptions-item><el-descriptions-item label="粗分">{{ detail.record.rawScore }}</el-descriptions-item><el-descriptions-item label="标准分">{{ detail.record.stdScore }}</el-descriptions-item></el-descriptions><div v-if="detail.record" class="notice mt-16">{{ detail.record.suggestion }}</div><el-divider content-position="left">心理教师评估</el-divider><el-form label-position="top"><el-form-item label="评估结论"><el-input v-model="diagnosis.conclusion" type="textarea" :rows="3"/></el-form-item><el-form-item label="关怀建议"><el-input v-model="diagnosis.advice" type="textarea" :rows="3"/></el-form-item><el-button type="primary" @click="saveDiagnosis">保存评估</el-button></el-form><el-divider content-position="left">答题明细</el-divider><el-table :data="detail.answers" max-height="280"><el-table-column type="index" width="55"/><el-table-column prop="content" label="题目"/><el-table-column prop="optionValue" label="分值" width="80"/></el-table><template #footer><el-button v-if="detail.record" @click="printOne(detail.record.recordId)">打印该报告</el-button><el-button type="primary" @click="visible=false">关闭</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listAllRecords, getRecordDetail } from '@/api/test'
import { listScale } from '@/api/system'
import { saveDiagnosis as saveDiagnosisApi } from '@/api/business'
const router=useRouter()
const rows=ref([]); const scales=ref([]); const total=ref(0); const visible=ref(false); const selected=ref([]); const detail=reactive({record:null,answers:[]}); const diagnosis=reactive({recordId:null,conclusion:'',advice:''}); const query=reactive({pageNum:1,pageSize:10,username:'',scaleId:null})
async function load(){const res=await listAllRecords(query);rows.value=res.rows||[];total.value=res.total||rows.value.length;selected.value=[]}
function onSelect(list){selected.value=list}
function printOne(recordId){visible.value=false;router.push({path:'/admin/report-print',query:{recordId}})}
function printBatch(){const ids=selected.value.map(r=>r.recordId).join(',');if(!ids)return;router.push({path:'/admin/report-print',query:{recordIds:ids}})}
function csvCell(v){const s=v==null?'':String(v);return '"'+s.replace(/"/g,'""')+'"'}
async function exportCsv(){
  const res=await listAllRecords({pageNum:1,pageSize:1000,username:query.username,scaleId:query.scaleId})
  const list=res.rows||[]
  if(!list.length)return ElMessage.warning('当前筛选条件下没有可导出的记录')
  const header=['用户名','姓名','量表','量表编码','粗分','标准分','等级','测评时间','评估结论','关怀建议']
  const lines=[header.map(csvCell).join(',')]
  list.forEach(r=>lines.push([r.username,r.nickname,r.scaleName,r.scaleCode,r.rawScore,r.stdScore,r.level,r.createTime,r.doctorConclusion,r.doctorAdvice].map(csvCell).join(',')))
  const blob=new Blob(['\uFEFF'+lines.join('\r\n')],{type:'text/csv;charset=utf-8'})
  const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='测评记录_'+new Date().toISOString().slice(0,10)+'.csv';document.body.appendChild(a);a.click();document.body.removeChild(a);URL.revokeObjectURL(a.href)
  ElMessage.success('已导出 '+list.length+' 条记录')
}
async function open(row){const res=await getRecordDetail(row.recordId);detail.record=res.data.record;detail.answers=res.data.answers||[];diagnosis.recordId=row.recordId;diagnosis.conclusion=detail.record.doctorConclusion||'';diagnosis.advice=detail.record.doctorAdvice||'';visible.value=true}
async function saveDiagnosis(){await saveDiagnosisApi(diagnosis);ElMessage.success('评估建议已保存')}
onMounted(async()=>{await load();const res=await listScale({pageNum:1,pageSize:100});scales.value=res.rows||[]})
</script>
<style scoped>
.panel-head{flex-wrap:wrap}.detail-head{display:flex;align-items:center;justify-content:space-between}.detail-head h3{margin:0}.detail-head p{margin:6px 0 0;color:var(--text-3);font-size:12px}
</style>
