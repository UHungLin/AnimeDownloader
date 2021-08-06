import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import antd from 'ant-design-vue'
import 'ant-design-vue/dist/antd.less'
import './assets/style/main.less'
import global from './global.js'
import api from './api'
const exec = require('child_process').exec

Vue.use(antd)

const { ipcRenderer, remote } = require('electron')
window.ipcRenderer = ipcRenderer
window.remote = remote

router.afterEach (( to, from, next ) => {
  setTimeout(() => {
    let _hmt = _hmt || [];
    (function() {
    const hm = document.createElement('script')
    hm.src = 'https://hm.baidu.com/hm.js?c0936e1408995a0d47191fcc302afea8'
    const s = document.getElementsByTagName('script')[0]
    s.parentNode.insertBefore(hm, s)
    })()
  }, 0)
} )

// let process = exec('..\\animeDownloader-server\\AnimeDownloaderServer.exe')
// process.error.on('data', data => {
//   console.log(data)
// })
// process.stdout.on('data', data => {
//   console.log(data)
// })
// process.stderr.on('data', data => {
//   console.log(data)
// })

// checkUpdate checkLogin 组件中用到了ipcRenderer所以需放到其引入下面
// import checkUpdate from './components/CheckUpdate/index'
import LoginModal from './components/LoginModal/LoginModal'
import UserModal from './components/UserModal'
// Vue.use(checkUpdate)
Vue.component('LoginModal', LoginModal)
Vue.component('UserModal', UserModal)

Vue.config.productionTip = false
Vue.prototype.global = global
Vue.prototype.api = api
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
