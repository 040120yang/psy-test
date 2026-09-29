<template>
  <div class="app-logo" :class="{ light }">
    <div class="mark"><span></span><span></span><span></span></div>
    <div class="copy"><strong>区域智能诊疗</strong><small>心理测评与随访系统</small></div>
  </div>
</template>
<script setup>
defineProps({ light: Boolean })
</script>
<style scoped>
.app-logo { display:flex; align-items:center; gap:11px; min-width:190px; }
.mark { display:flex; align-items:flex-end; gap:2px; width:38px; height:38px; padding:8px; border-radius:12px; background:linear-gradient(135deg,var(--primary),#61aaa5); }
.mark span { width:5px; border-radius:4px; background:#fff; }
.mark span:nth-child(1){height:10px}.mark span:nth-child(2){height:20px}.mark span:nth-child(3){height:15px}
.copy strong { display:block; font-size:15px; letter-spacing:.2px; }
.copy small { display:block; margin-top:3px; color:var(--text-3); font-size:10px; }
.light .copy strong { color:#fff; } .light .copy small { color:rgba(255,255,255,.62); }
</style>
