<template>
  <div class="container">
    <div class="download-icon">
      <a-badge>
        <a-icon type="download" class="icon" @click="goDownload" />
      </a-badge>
    </div>
    <div class="download-logo fr ac jc">
      <img src="../assets/images/logo.png" alt="">
    </div>
    <div class="download-box">
      <a-input v-model="url" size="large" placeholder="请输入视频地址">
        <a-icon slot="addonAfter" type="arrow-down" class="icon" @click="download" />
      </a-input>
    </div>
    <div class="setting" v-if="$route.path === '/'">
      <a-icon type="setting" class="icon" @click="$refs.settingDrawer.show(store.get('setting'))" />
    </div>
    <div class="user" v-if="$route.path === '/'">
      <a-icon type="user" class="icon" @click="$refs.userModal.show()"/>
    </div>
    <VideoModal ref="videoModal" />
    <SettingDrawer ref="settingDrawer" />
    <UserModal ref="userModal" />
    <LoginModal ref="loginModal" />
  </div>
</template>

<script>
import base from '../mixin/base'
import UA from '../assets/data/ua'
import VideoModal from '../components/VideoModal'
import SettingDrawer from '../components/SettingDrawer'
export default {
  mixins: [base],
  data () {
    return {
      url: '',
      requestLock: true,
      timer: null
    }
  },
  components: {
    VideoModal,
    SettingDrawer
  },
  computed: {},
  watch: {},
  mounted () {},
  created () {},
  methods: {
    test () {
      this.$refs.loginModal.openLoginModal()
      // this.$store.commit('setShowLoginModal', false)
    },
    delayedRequest () {
      // console.log('delayedRequest')
      this.timer = setTimeout(() => {
        console.log('delayedRequest')
        this.requestLock = !this.requestLock
      }, 3000)
    },
    goDownload () {
      this.$router.push('/download')
    },
    async download () {
      // 检测url
      if (!this.url) {
        this.$message.info('请输入视频地址')
        return
      }

      // const requestUrl = 'http://127.0.0.1:8080/lin/anime/parse'
      console.log('this.requestLock = ' + this.requestLock)
      if (!this.requestLock) {
        this.$message.info('解析视频链接中，请勿重复点击...')
        return
      }
      try {
        this.requestLock = false
        this.delayedRequest()
        // const res = await this.got(params.url, params.config)
        const res = await this.got.post(this.api.Api.animeParse, {
          json: {
            search: this.url,
            headers: {
              'User-Agent': `${UA}`,
              cookie: `SESSDATA=${this.store.get('setting.SESSDATA')}`
            },
            responseType: 'json'
          }
        })
        const body = JSON.parse(res.body)
        // console.log(body)
        const status = body.status
        this.requestLock = true
        clearInterval(this.timer)
        if (status === -1) {
          if (res.msg) {
            this.$message.error(res.msg)
          } else {
            this.$message.error('解析失败')
          }
          return
        }

        const videoData = body.data
        console.log('videoData.acceptDescription =' + Object.entries(videoData.acceptDescription).length)
        const videoInfo = {
          title: videoData.title,
          id: videoData.id,
          cId: videoData.cId,
          cover: videoData.preViewUrl,
          type: videoData.type,
          fileType: videoData.fileType,
          qualityOptions: Object.entries(videoData.acceptDescription).length ? Object.entries(videoData.acceptDescription).map(item => ({ label: item[0], value: item[1] })) : [{ label: '默认', value: '1' }],
          totalSizeMap: videoData.totalSizeMap,
          page: videoData.subVideoInfos.map(item => ({ title: item.name, cid: item.cid, url: item.url, bvId: item.bvId })),
          dashVideoMap: videoData.dashVideoMap,
          audioUrl: videoData.audioUrl,
          headers: videoData.headers,
          downloadPath: {}
        }
        // console.log('videoInfo ' + JSON.stringify(videoInfo))
        this.$refs.videoModal.show(videoInfo)
      } catch (error) {
        console.log(error)
        if (error === -1) {
          this.$message.info('不支持当前视频')
        }
      }
    }
  }
}
</script>

<style scoped lang="less">
.container{
  box-sizing: border-box;
  padding: 16px;
  position: relative;
  height: calc(100% - 28px);
  .download-icon{
    position: absolute;
    top: 16px;
    right: 16px;
    z-index: 99;
    cursor: pointer;
    .icon{
      font-size: 36px;
      color: @primary-color;
    }
  }
  .download-logo{
    margin: 130px 0px 50px 0px;
    img{
      transform: scale(.6);
    }
  }
  .download-box{
    padding: 0px 64px;
    /deep/ .ant-input-group-addon{
      background: @primary-color;
      border: none;
    }
    .icon{
      color: #ffffff;
      font-size: 18px;
    }
  }
  .setting{
    position: absolute;
    left: 16px;
    bottom: 16px;
    z-index: 100;
    color: @primary-color;
    font-size: 16px;
  }
  .user{
    position: absolute;
    right: 16px;
    bottom: 16px;
    z-index: 100;
    color: @primary-color;
    font-size: 16px;
  }
}
</style>
