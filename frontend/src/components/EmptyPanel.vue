<template><div class="empty-panel"><el-icon :size="42"><Files /></el-icon><h3>{{ title }}</h3><p>{{ description }}</p><slot /></div></template>
<script setup>
defineProps({ title:{type:String,default:'暂无数据'}, description:{type:String,default:'当前没有可展示的内容'} })
</script>
<style scoped>
.empty-panel{display:grid;place-items:center;padding:56px 20px;text-align:center;color:var(--text-3)}.empty-panel h3{margin:14px 0 6px;color:var(--text)}.empty-panel p{margin:0 0 16px;font-size:13px}
</style>
