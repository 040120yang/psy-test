<template>
  <div class="metric-card">
    <div class="metric-icon"><el-icon :size="21"><component :is="icon" /></el-icon></div>
    <div class="metric-value" :style="{ color }">{{ value }}</div>
    <div class="metric-label">{{ label }}</div>
    <div v-if="note" class="metric-note">{{ note }}</div>
  </div>
</template>
<script setup>
defineProps({ label:String, value:[String,Number], icon:{type:String,default:'DataLine'}, color:{type:String,default:'var(--text)'}, note:String })
</script>
