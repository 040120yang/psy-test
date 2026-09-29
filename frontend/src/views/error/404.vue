<template>
  <div class="not-found"><div class="code">404</div><h1>页面不存在</h1><p>你访问的页面可能已被移动或删除。</p><el-button type="primary" @click="$router.push('/')">返回首页</el-button></div>
</template>
<style scoped>
.not-found{min-height:100vh;display:grid;place-items:center;align-content:center;text-align:center;background:radial-gradient(circle at 30% 20%,#dcefec,transparent 32%),radial-gradient(circle at 80% 80%,#e5ecff,transparent 34%),#f5f8fa}.code{font-size:110px;font-weight:900;line-height:1;color:var(--primary);opacity:.18}.not-found h1{margin:-10px 0 8px}.not-found p{margin:0 0 22px;color:var(--text-3)}
</style>
