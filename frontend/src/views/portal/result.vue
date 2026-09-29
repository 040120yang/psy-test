<template>
  <div v-loading="loading" class="page-stack">
    <PageHeader eyebrow="Assessment Report" :title="record?.scaleName || '测评报告'" description="以下结果用于心理健康筛查和健康建议，不能替代专业医疗诊断。" >
      <template #actions><el-button @click="window.print()"><el-icon><Printer /></el-icon> 打印报告</el-button><el-button type="primary" @click="$router.push('/portal/records')">查看全部记录</el-button></template>
    </PageHeader>
    <section v-if="record" class="panel panel-body">
      <div class="result-hero">
        <div class="score-ring" :style="{ '--score': Math.min(Number(record.stdScore),100) }"><div class="score-ring-content"><strong>{{ record.stdScore }}</strong><span>标准分</span></div></div>
        <div><StatusTag :text="record.level" /><h2 style="margin:12px 0 8px">{{ record.scaleName }}测评结果</h2><p class="text-muted" style="line-height:1.9;margin:0">量表编码：{{ record.scaleCode }}　原始粗分：{{ record.rawScore }}　测评时间：{{ record.createTime }}</p><div class="notice mt-20">{{ record.suggestion || '测评已完成，请结合近期情绪、睡眠和生活状态综合判断。' }}</div></div>
      </div>
    </section>
    <section v-if="isRisk" class="panel"><div class="panel-head"><div><div class="panel-title">风险提示与就医建议</div><div class="panel-subtitle">本结果达到中度或以上风险等级</div></div><el-icon :size="28" color="var(--danger)"><Warning /></el-icon></div><div class="panel-body"><el-alert title="建议尽快联系专业医护人员进行进一步评估" description="如出现自伤、轻生念头或无法控制情绪，请立即联系家人陪同就医，或拨打全国心理援助热线 12356。" type="error" :closable="false" show-icon /></div></section>
    <section class="soft-grid cols-2">
      <div class="panel"><div class="panel-head"><div><div class="panel-title">AI 辅助解读</div><div class="panel-subtitle">仅作为健康建议，不构成诊断</div></div><el-button type="primary" plain :loading="aiLoading" @click="aiDiagnosis">生成解读</el-button></div><div class="panel-body"><div v-if="aiResult" style="white-space:pre-wrap;line-height:1.9;color:var(--text-2)">{{ aiResult }}</div><div v-else class="text-muted">点击生成个性化健康建议。系统不会据此自动诊断。</div></div></div>
      <div class="panel"><div class="panel-head"><div><div class="panel-title">结果说明</div><div class="panel-subtitle">如何使用这份报告</div></div></div><div class="panel-body" style="line-height:2;color:var(--text-2);font-size:13px"><div>1. 对照标准分和等级理解当前状态。</div><div>2. 关注建议中的生活方式和就医提示。</div><div>3. 如需专业帮助，可携带报告咨询医生。</div><div>4. 建议定期复测，观察变化趋势。</div></div></div>
    </section>
    <section class="panel"><div class="panel-head"><div><div class="panel-title">答题明细</div><div class="panel-subtitle">共 {{ answers.length }} 道题</div></div></div><div class="panel-body"><el-table :data="answers" stripe><el-table-column type="index" label="#" width="60" align="center" /><el-table-column prop="content" label="题目" min-width="340" /><el-table-column prop="optionValue" label="选项分值" width="110" align="center" /></el-table></div></section>
    <EmptyPanel v-if="!record&&!loading" title="无法读取报告" description="记录可能已被删除或无权访问" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { getRecordDetail } from '@/api/test'
import request from '@/utils/request'
const route=useRoute(); const loading=ref(true); const record=ref(null); const answers=ref([]); const aiLoading=ref(false); const aiResult=ref(''); const window=globalThis.window
const isRisk=computed(()=>record.value?.level?.includes('中度')||record.value?.level?.includes('重度'))
async function aiDiagnosis(){aiLoading.value=true;try{const prompt=`我刚完成了${record.value.scaleName}测评，标准分是${record.value.stdScore}，结果等级是${record.value.level}。请给出原因分析、生活建议和是否需要就医的提示，分点列出。`;const res=await request({url:'/coze/chat',method:'post',data:{message:prompt}});aiResult.value=res.reply||res.data||res.msg||'生成完成'}finally{aiLoading.value=false}}
onMounted(async()=>{try{const res=await getRecordDetail(Number(route.query.recordId));record.value=res.data.record;answers.value=res.data.answers||[]}finally{loading.value=false}})
</script>
