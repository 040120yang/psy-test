<template>
  <div class="page-stack">
    <PageHeader eyebrow="Campus Overview" title="系统概览" description="查看区域心理测评、学生随访和风险分布情况。" />
    <section class="soft-grid">
      <StatCard label="用户总数" :value="stats.userCount||0" icon="User" />
      <StatCard label="测评总次数" :value="stats.recordCount||0" icon="Document" color="var(--accent)" />
      <StatCard label="学生总数" :value="stats.patientCount||0" icon="FirstAidKit" color="var(--success)" />
      <StatCard label="今日测评" :value="stats.todayCount||0" icon="Calendar" color="var(--warning)" />
    </section>
    <section class="soft-grid">
      <StatCard label="随访总数" :value="stats.followTotal||0" icon="Bell" />
      <StatCard label="已完成随访" :value="stats.followDone||0" icon="CircleCheck" color="var(--success)" />
      <StatCard label="逾期未随访" :value="stats.followOverdue||0" icon="Warning" color="var(--danger)" />
      <StatCard label="随访完成率" :value="`${followRate}%`" icon="TrendCharts" color="var(--primary)" />
    </section>
    <section class="soft-grid cols-2">
      <div class="panel"><div class="panel-head"><div><div class="panel-title">风险等级分布</div><div class="panel-subtitle">按测评结果等级统计</div></div></div><div class="panel-body"><div ref="levelChart" class="chart"/></div></div>
      <div class="panel"><div class="panel-head"><div><div class="panel-title">量表使用情况</div><div class="panel-subtitle">各量表累计测评次数</div></div></div><div class="panel-body"><div ref="scaleChart" class="chart"/></div></div>
    </section>
    <section class="panel"><div class="panel-head"><div><div class="panel-title">近 7 天测评趋势</div><div class="panel-subtitle">每日完成测评次数</div></div></div><div class="panel-body"><div ref="trendChart" class="trend-chart"/></div></section>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { getStats } from '@/api/dashboard'
const stats=reactive({}); const levelChart=ref(); const scaleChart=ref(); const trendChart=ref(); const charts=[]
const followRate=computed(()=>stats.followTotal?Math.round((stats.followDone||0)*100/stats.followTotal):0)
const palette=['#36a269','#f4c14f','#f39a4a','#e5484d','#7c5cb0']
function initChart(el,option){const chart=echarts.init(el);chart.setOption(option);charts.push(chart);return chart}
function levelColor(name){const t=name||'';if(t.includes('重度'))return '#e5484d';if(t.includes('中度'))return '#f39a4a';if(t.includes('轻度'))return '#f4c14f';if(t==='正常')return '#36a269';return '#94a3b8'}
async function load(){const res=await getStats();Object.assign(stats,res.data||{});const level=(res.data.levelDist||[]).map((i,index)=>({name:i.name,value:i.value,itemStyle:{color:levelColor(i.name)}}));const scales=res.data.scaleDist||[];const trend=res.data.weekTrend||[];initChart(levelChart.value,{tooltip:{trigger:'item'},legend:{bottom:0,icon:'circle'},series:[{type:'pie',radius:['48%','72%'],center:['50%','44%'],itemStyle:{borderRadius:6,borderColor:'#fff',borderWidth:3},label:{formatter:'{b}\n{c}'},data:level}]});initChart(scaleChart.value,{tooltip:{trigger:'axis'},grid:{left:34,right:16,top:24,bottom:36},xAxis:{type:'category',data:scales.map(i=>i.name),axisLine:{lineStyle:{color:'#dbe3ec'}}},yAxis:{type:'value',minInterval:1,splitLine:{lineStyle:{color:'#eef2f7'}}},series:[{type:'bar',barMaxWidth:38,data:scales.map((i,index)=>({value:i.value,itemStyle:{color:palette[index%palette.length],borderRadius:[8,8,0,0]}}))}]});initChart(trendChart.value,{tooltip:{trigger:'axis'},grid:{left:34,right:20,top:24,bottom:36},xAxis:{type:'category',boundaryGap:false,data:trend.map(i=>i.day)},yAxis:{type:'value',minInterval:1,splitLine:{lineStyle:{color:'#eef2f7'}}},series:[{type:'line',smooth:true,symbolSize:8,data:trend.map(i=>i.value),lineStyle:{width:3,color:'#2f7d7a'},itemStyle:{color:'#2f7d7a'},areaStyle:{color:new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(47,125,122,.28)'},{offset:1,color:'rgba(47,125,122,0)'}])}}]})}
function resize(){charts.forEach(c=>c.resize())}
onMounted(()=>{load();window.addEventListener('resize',resize)})
onBeforeUnmount(()=>{window.removeEventListener('resize',resize);charts.forEach(c=>c.dispose())})
</script>
<style scoped>
.chart{height:330px}.trend-chart{height:290px}
</style>
