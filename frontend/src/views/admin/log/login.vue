<template>
  <div class="page-stack">
    <PageHeader eyebrow="Audit" title="登录日志" description="查看用户登录成功和失败记录。" />
    <section class="panel"><div class="panel-head"><div><div class="panel-title">登录记录</div><div class="panel-subtitle">最多显示最近 200 条</div></div><el-button @click="load">刷新</el-button></div><div class="panel-body"><el-table v-if="rows.length" :data="rows" stripe><el-table-column prop="userName" label="用户名" min-width="130"/><el-table-column prop="ipaddr" label="IP地址" min-width="150"/><el-table-column label="状态" width="100"><template #default="{row}"><StatusTag :text="row.status===0?'成功':'异常'"/></template></el-table-column><el-table-column prop="msg" label="提示消息" min-width="220"/><el-table-column prop="loginTime" label="登录时间" width="180"/></el-table><EmptyPanel v-else title="暂无登录日志"/></div></section>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyPanel from '@/components/EmptyPanel.vue'
import { listLoginLogs } from '@/api/business'
const rows=ref([]); async function load(){const res=await listLoginLogs();rows.value=res.rows||[]}; onMounted(load)
</script>
