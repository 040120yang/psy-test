<template><el-tag class="risk-tag" :class="cls" effect="plain">{{ text }}</el-tag></template>
<script setup>
import { computed } from 'vue'
const props=defineProps({ text:{type:String,default:''} })
const cls=computed(()=>{const t=props.text||'';if(t.includes('重度')||t.includes('逾期')||t.includes('异常'))return 'risk-high';if(t.includes('中度'))return 'risk-mid';if(t.includes('轻度')||t.includes('待')||t.includes('停用'))return 'risk-mild';if(t==='正常'||t.includes('已完成')||t.includes('成功')||t.includes('启用'))return 'risk-normal';return ''})
</script>
