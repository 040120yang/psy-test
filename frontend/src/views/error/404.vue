<template>
  <div class="error-page">
    <div class="error-content">
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在或已被移除</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound'
}
</script>

<style scoped>
.error-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.error-content {
  text-align: center;
}
.error-content h1 {
  font-size: 80px;
  margin: 0;
  color: #409eff;
}
.error-content p {
  color: #909399;
  margin: 12px 0 24px;
}
</style>
