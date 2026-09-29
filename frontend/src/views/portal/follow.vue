<template>
  <div class="page-stack">
    <PageHeader eyebrow="Follow-up Care" title="我的随访" description="查看医生安排的随访时间、当前状态和健康记录。" />
    <section class="soft-grid cols-3">
      <StatCard label="随访总数" :value="rows.length" icon="Bell" />
      <StatCard label="待随访" :value="pending" icon="Clock" color="var(--warning)" />
      <StatCard label="已完成" :value="done" icon="CircleCheck" color="var(--success)" />
    </section>
    <section class="panel"><div class="panel-head"><div><div class="panel-title">随访安排</div><div class="panel-subtitle">按随访日期排列</div></div></div><div class="panel-body">
      <div v-if="rows.length" class="timeline-list"><div v-for="item in rows" :key="item.id" class="timeline-item"><h4>{{ item.followDate }} · {{ item.followType || '线上随访' }} <StatusTag :text="item.status" /></h4><p>症状自评：{{ item.symptomScore ?? '未填写' }} / 10</p><p v-if="item.doctorNote">医生记录：{{ item.doctorNote }}</p><p v-if="item.nextFollowDate">下次随访：{{ item.nextFollowDate }}</p></div></div>
      <EmptyPanel v-else title="暂无随访安排" description="如测评结果需要持续关注，医生会为你安排随访" />
    </div></section>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listMyFollows } from '@/api/follow'
const rows=ref([]); const pending=computed(()=>rows.value.filter(i=>i.status!=='已完成').length); const done=computed(()=>rows.value.filter(i=>i.status==='已完成').length)
onMounted(async()=>{const res=await listMyFollows({pageNum:1,pageSize:100});rows.value=res.rows||[]})
</script>
