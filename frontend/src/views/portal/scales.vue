<template>
  <div class="page-stack">
    <PageHeader eyebrow="Psychological Assessment" title="选择心理测评量表" description="请在安静环境中根据最近一周的实际情况作答。测评结果用于筛查参考，不能替代专业诊断。" />
    <div class="notice"><el-icon><InfoFilled /></el-icon> 共 {{ scales.length }} 个启用量表，完成答题后系统将自动计算标准分并生成分级建议。</div>
    <div v-if="scales.length" class="scale-grid">
      <article v-for="scale in scales" :key="scale.scaleId" class="panel scale-card">
        <div class="scale-code">{{ scale.scaleCode }}</div><h3>{{ scale.scaleName }}</h3><p>{{ scale.description || '用于评估近期心理健康状况的自评量表。' }}</p>
        <div class="scale-meta"><span>约 {{ scale.questionCount || 20 }} 题</span><span>{{ scale.optionType === 5 ? '五级评分' : '四级评分' }}</span><span>自动计分</span></div>
        <el-button type="primary" @click="startTest(scale)">开始测评 <el-icon class="el-icon--right"><ArrowRight /></el-icon></el-button>
      </article>
    </div>
    <EmptyPanel v-else title="暂无可用量表" description="请联系管理员启用测评量表" />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listEnabledScales } from '@/api/test'
const router=useRouter(); const scales=ref([])
function startTest(scale){router.push({path:'/portal/answer',query:{scaleId:scale.scaleId}})}
onMounted(async()=>{const res=await listEnabledScales();scales.value=res.data||[]})
</script>
