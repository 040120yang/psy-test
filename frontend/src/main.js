import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import App from './App.vue'
import router from './router'
import './permission'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

Vue.use(ElementUI, { size: 'medium' })
Vue.config.productionTip = false

NProgress.configure({ showSpinner: false })

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
