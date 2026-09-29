<template>
  <div class="page-stack">
    <section class="hero">
      <h2>你好，{{ user.nickname || user.username }}</h2>
      <p>今天也请留意自己的情绪和睡眠状态。完成一次快速测评，或继续查看过往报告。</p>
      <div class="hero-actions"><el-button type="primary" size="large" @click="$router.push('/portal/scales')">开始心理测评</el-button><el-button size="large" @click="$router.push('/portal/records')">查看我的报告</el-button></div>
      <div class="hotline"><span>心理援助热线</span><strong>12356</strong><small>24 小时全国支持</small></div>
    </section>
    <section class="soft-grid">
      <StatCard label="测评总次数" :value="stats.total" icon="Document" />
      <StatCard label="正常结果" :value="stats.normal" icon="CircleCheck" color="var(--success)" />
      <StatCard label="需关注结果" :value="stats.attention" icon="Warning" color="var(--danger)" />
      <StatCard label="最近测评" :value="stats.lastTime" icon="Clock" color="var(--accent)" />
    </section>
    <section><h2 class="section-title">快捷入口</h2><div class="action-grid mt-16">
      <div class="action-card" @click="$router.push('/portal/scales')"><div class="icon" style="background:#2f7d7a"><el-icon><Notebook /></el-icon></div><h3>心理测评</h3><p>选择 SAS、SDS、SCL-90 或 SRSS 量表</p></div>
      <div class="action-card" @click="$router.push('/portal/records')"><div class="icon" style="background:#4f7cf0"><el-icon><Document /></el-icon></div><h3>我的报告</h3><p>查看标准分、等级建议和答题明细</p></div>
      <div class="action-card" @click="$router.push('/portal/follow')"><div class="icon" style="background:#f59e0b"><el-icon><Bell /></el-icon></div><h3>我的随访</h3><p>查看心理教师安排的随访时间和状态</p></div>
      <div class="action-card" @click="$router.push('/portal/coze')"><div class="icon" style="background:#7c5cb0"><el-icon><MagicStick /></el-icon></div><h3>AI 助手</h3><p>获取心理陪伴和健康知识建议</p></div>
    </div></section>
    <section class="soft-grid cols-2">
      <div class="panel"><div class="panel-head"><div><div class="panel-title">最近测评</div><div class="panel-subtitle">最近完成的测评记录</div></div><el-button text type="primary" @click="$router.push('/portal/records')">全部记录</el-button></div><div class="panel-body"><div v-if="records.length" class="timeline-list"><div v-for="item in records.slice(0,4)" :key="item.recordId" class="timeline-item"><h4>{{ item.scaleName }} · <StatusTag :text="item.level" /></h4><p>标准分 {{ item.stdScore }} · {{ item.createTime }}</p></div></div><EmptyPanel v-else title="还没有测评记录" description="完成第一次测评后，这里会显示结果" /></div></div>
      <div class="panel"><div class="panel-head"><div><div class="panel-title">平台说明</div><div class="panel-subtitle">测评与使用边界</div></div></div><div class="panel-body"><div class="notice">测评结果用于心理健康筛查和辅助参考，不能替代专业心理评估。如出现明显不适、持续失眠或伤害自己的念头，请及时联系家人并前往医院就诊。</div><div class="mt-16" style="line-height:2;color:var(--text-2);font-size:13px"><div>系统版本：V2.0</div><div>可用量表：SAS / SDS / SCL-90 / SRSS</div><div>服务支持：AI 助手、随访提醒、消息中心</div></div></div></div>
    </section>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import StatCard from '@/components/StatCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { getUser } from '@/utils/auth'
import { listMyRecords } from '@/api/test'
const user=ref(getUser()||{}); const records=ref([]); const stats=ref({total:0,normal:0,attention:0,lastTime:'暂无'})
onMounted(async()=>{const res=await listMyRecords({pageNum:1,pageSize:100});records.value=res.rows||[];stats.value.total=records.value.length;stats.value.normal=records.value.filter(r=>r.level==='正常').length;stats.value.attention=records.value.filter(r=>r.level&&r.level!=='正常').length;stats.value.lastTime=records.value[0]?.createTime?.slice(5,10)||'暂无'})
</script>
