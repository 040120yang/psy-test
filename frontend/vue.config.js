'use strict'
const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  productionSourceMap: false,
  devServer: {
    host: 'localhost',
    port: 8088,
    open: false,
    proxy: {
      '/dev-api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: { '^/dev-api': '' }
      }
    }
  }
})
