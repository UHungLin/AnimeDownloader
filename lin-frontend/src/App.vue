<template>
  <a-config-provider :locale="zh_CN">
    <div id="app">
      <LayoutHeader />
      <router-view></router-view>
    </div>
  </a-config-provider>
</template>

<script>
import zh_CN from 'ant-design-vue/lib/locale-provider/zh_CN'
import LayoutHeader from './components/LayoutHeader'
export default {
  data () {
    return {
      wallpaper: require('./assets/images/bg.png'),
      zh_CN,
      webSocketObj: null,
      timeout: 60 * 1000, // 60秒一次心跳
      lockReconnect: false, // 是否真正建立连接
      timeoutObj: null, // 外层心跳倒计时
      serverTimeoutObj: null, // 内层心跳检测
      timeoutnum: null // 断开 重连倒计时
    }
  },
  components: {
    LayoutHeader
  },
  computed: {},
  watch: {},
  mounted () {
  },
  created () {
    this.initWebsocket()
  },
  methods: {
    initWebsocket () {
      if (!this.webSocketObj) {
        if ('WebSocket' in window) {
          // console.log('websocket')
          this.webSocketObj = new WebSocket('ws://127.0.0.1:9090')
          this.webSocketObj.onmessage = this.onMessage
          this.webSocketObj.onopen = this.onOpen
          this.webSocketObj.onerror = this.onError
          this.webSocketObj.onclose = this.onClose

          window.onbeforeunload = this.onbeforeunload
        } else {
          this.$message.error('不支持 websocket')
        }
      }
    },
    reconnect () {
      // 重新连接
      if (this.lockReconnect) {
        // 是否真正建立连接
        return
      }
      this.lockReconnect = true
      // 没连接上会一直重连，设置延迟避免请求过多
      this.timeoutnum && clearTimeout(this.timeoutnum)
      // 如果到了这里断开重连的倒计时还有值的话就清除掉
      this.timeoutnum = setTimeout(() => {
        console.log('重新连接')
        this.initWebsocket()
        this.lockReconnect = false
      }, 5000)
    },
    reset () {
      // 重置心跳
      // 清除时间（清除内外两个心跳计时）
      clearTimeout(this.timeoutObj)
      clearTimeout(this.serverTimeoutObj)
      // 重启心跳
      this.start()
    },
    start () {
      // 开启心跳
      this.timeoutObj && clearTimeout(this.timeoutObj)
      // 如果外层心跳倒计时存在的话，清除掉
      this.serverTimeoutObj && clearTimeout(this.serverTimeoutObj)
      // 如果内层心跳检测倒计时存在的话，清除掉
      this.timeoutObj = setTimeout(() => {
        // 重新赋值重新发送 进行心跳检测
        // 这里发送一个心跳，后端收到后，返回一个心跳消息
        if (this.webSocketObj && this.webSocketObj.readyState === 1) {
          // 如果连接正常
          this.webSocketObj.send('2333')
        } else {
          // 否则重连
          this.reconnect()
        }
        this.serverTimeoutObj = setTimeout(() => {
          // 超时关闭
          console.log('超时关闭')
          this.webSocketObj && this.webSocketObj.close && this.webSocketObj.close()
        }, this.timeout)
      }, this.timeout)
    },
    onOpen () {
      if (this.webSocketObj.readyState === 1) {
        console.log('连接成功')
        // this.webSocketObj.send('hello, world')
        this.global.setWs(this.webSocketObj)
      }
    },
    onMessage (evt) {
      console.log('onMessage...')
      this.reset()
      // console.log(JSON.stringify(JSON.parse(evt.data)))
      if (evt.data === '2333') {
        return
      }
      // console.log(JSON.parse(evt.data))
      const data = JSON.parse(evt.data)
      window.ipcRenderer.send('download-progress', data)
    },
    onError () {
      console.log('出现错误关闭连接')
      this.onClose()
      setTimeout(() => {
        this.initWebsocket()
      }, 5000)
      // this.initWebSocket()
    },
    onbeforeunload () {
      this.onClose()
    },
    onClose () {
      console.log('关闭连接')
      this.webSocketObj && this.webSocketObj.close && this.webSocketObj.close()
      this.webSocketObj = null
    }
  }
}
</script>

<style lang="less">
#app {
  position: relative;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  width: 100%;
  height: 100%;
  user-select: none;
  background-image: url('./assets/images/bg.png');
  background-size: 100% 100%;
}
</style>
