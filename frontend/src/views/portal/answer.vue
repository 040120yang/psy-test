<template>
  <div v-loading="loading" class="page-stack">
    <PageHeader :eyebrow="scale?.scaleCode || 'Assessment'" :title="scale?.scaleName || '心理测评'" :description="tip" />
    <div v-if="questions.length" class="answer-layout">
      <aside class="panel question-nav">
        <strong>答题进度</strong>
        <el-progress :percentage="progress" :stroke-width="9" class="mt-16" />
        <div class="question-indexes">
          <div v-for="(q,index) in questions" :key="q.questionId" class="question-index" :class="{done:answers[q.questionId],current:index===currentIndex}" @click="currentIndex=index">{{ index+1 }}</div>
        </div>
        <div class="mt-20 text-muted" style="font-size:12px">已完成 {{ answeredCount }} / {{ questions.length }} 题</div>
      </aside>
      <section v-if="current" class="panel question-card">
        <div class="question-number">第 {{ currentIndex+1 }} 题 / 共 {{ questions.length }} 题</div>
        <h2 class="question-title">{{ current.content }}<span v-if="current.reverseFlag===1" class="reverse-tag">反向计分</span></h2>
        <div class="option-list">
          <div v-for="(label,index) in options" :key="label" class="option-item" :class="{active:answers[current.questionId]===index+1}" @click="selectOption(index+1)"><span class="option-index">{{ String.fromCharCode(65+index) }}</span>{{ label }}</div>
        </div>
        <div class="answer-actions"><el-button @click="currentIndex--" :disabled="currentIndex===0">上一题</el-button><el-button v-if="currentIndex<questions.length-1" type="primary" @click="currentIndex++">下一题</el-button><el-button v-else type="primary" :loading="submitting" @click="submit">提交测评</el-button></div>
      </section>
    </div>
    <EmptyPanel v-else-if="!loading" title="未找到题目" description="该量表暂无可用题目" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { getQuestions, listEnabledScales, submitTest } from '@/api/test'
const route=useRoute(); const router=useRouter(); const loading=ref(true); const submitting=ref(false); const scale=ref(null); const questions=ref([]); const answers=ref({}); const currentIndex=ref(0)
const options4=['没有或很少时间','小部分时间','相当多时间','绝大部分或全部时间']; const options5=['没有','很轻','中等','偏重','严重']
const options=computed(()=>scale.value?.optionType===5?options5:options4); const current=computed(()=>questions.value[currentIndex.value]); const answeredCount=computed(()=>Object.values(answers.value).filter(Boolean).length); const progress=computed(()=>questions.value.length?Math.round(answeredCount.value*100/questions.value.length):0); const tip=computed(()=>`请根据最近一周的实际情况作答，每题选择一个最符合的选项（${scale.value?.optionType===5?'五级评分':'四级评分'}）。`)
function selectOption(value){answers.value[current.value.questionId]=value;if(currentIndex.value<questions.value.length-1)setTimeout(()=>currentIndex.value++,220)}
async function submit(){if(answeredCount.value<questions.value.length)return ElMessage.warning(`还有 ${questions.value.length-answeredCount.value} 题未作答`);submitting.value=true;try{const payload={scaleId:Number(route.query.scaleId),answers:questions.value.map(q=>({questionId:q.questionId,optionValue:answers.value[q.questionId]}))};const res=await submitTest(payload);ElMessage.success('测评完成');router.replace({path:'/portal/result',query:{recordId:res.data.record.recordId}})}finally{submitting.value=false}}
onMounted(async()=>{try{const scaleId=Number(route.query.scaleId);const list=await listEnabledScales();scale.value=(list.data||[]).find(item=>item.scaleId===scaleId);if(!scale.value){ElMessage.error('量表不存在或已停用');return router.replace('/portal/scales')}const res=await getQuestions(scaleId);questions.value=res.data||[]}finally{loading.value=false}})
</script>
<style scoped>
.question-number{color:var(--primary);font-size:13px;font-weight:700}.reverse-tag{display:inline-block;margin-left:10px;padding:2px 8px;border-radius:999px;background:#f1f4f8;color:var(--text-3);font-size:11px;vertical-align:middle}.option-index{display:inline-grid;place-items:center;width:26px;height:26px;margin-right:12px;border-radius:8px;background:#f1f4f8;font-size:12px;font-weight:700}.option-item.active .option-index{background:var(--primary);color:#fff}.answer-actions{display:flex;justify-content:flex-end;gap:10px;margin-top:24px}.question-card{padding:26px}
</style>
