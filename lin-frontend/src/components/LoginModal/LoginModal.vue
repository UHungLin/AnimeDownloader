<template>
  <div>
    <a-modal
      v-model="visible"
      title="扫码登录Bilibili账号"
      okText="确认登录"
      cancelText="不登录"
      :ok-button-props="{ props: { disabled: statusText === '未扫码' } }"
      :closable="false"
      :maskClosable="false"
      @ok="handleOk"
      @cancel="handleCancel">
      <div class="fc jc ac login">
        <div class="fc jc img-box">
          <div class="fr ac jc img-modal" v-if="!countDown">
            <a-icon class="icon" type="redo" @click="getQrcodeData()" />
          </div>
          <img class="img" :src="qrCodeImg" alt="">
        </div>
        <div class="mt16 status">{{ statusText }}</div>
        <div class="mt16 desc">不登录想下大会员高清视频？想啥呢╮(￣▽￣)╭</div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import base from '../../mixin/base'
import qrCode from 'qrcode'
import { checkLogin } from '../../core/bilibili'
export default {
  mixins: [base],
  data () {
    return {
      visible: false,
      qrCodeImg: null,
      oauthKey: '',
      countDown: 180,
      timer: null,
      qrCodeConfig: {
        margin: 0,
        errorCorrectionLevel: 'H',
        width: 400
      },
      statusText: '未扫码',
      isCheck: true,
      SESSDATA: '',
      uname: '',
      face: ''
    }
  },
  components: {},
  computed: {},
  watch: {},
  mounted () {},
  created () {},
  methods: {
    async openLoginModal () {
      await this.getQrcodeData()
      this.statusText = '未扫码'
      this.visible = true
      this.isCheck = true
      this.checkLoginStatus(this.oauthKey)
    },
    hideLoginModal () {
      this.isCheck = false
      setTimeout(() => {
        clearInterval(this.timer)
        this.timer = null
      }, 3000)
      this.visible = false
    },
    async getQrcodeData () {
      const { body } = await this.got('http://passport.bilibili.com/qrcode/getLoginUrl', { responseType: 'json' })
      this.createQrcode(body.data.url)
      this.oauthKey = body.data.oauthKey
      // 开始倒计时
      this.countDown = 180
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
      this.timer = setInterval(() => {
        if (!this.countDown) {
          clearInterval(this.timer)
          return
        }
        this.countDown -= 1
      }, 1000)
    },
    createQrcode (data) {
      qrCode.toDataURL(data, this.qrCodeConfig)
        .then(res => {
          this.qrCodeImg = res
        })
    },
    checkLoginStatus (oauthKey) {
      const _this = this
      run(oauthKey)
      async function run (oauthKey) {
        if (!_this.isCheck) return
        const { body } = await _this.got('http://passport.bilibili.com/qrcode/getLoginInfo', {
          method: 'POST',
          responseType: 'json',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          form: { oauthKey }
        })
        console.log(body)
        if (!body.status) {
          setTimeout(() => {
            run(oauthKey)
          }, 3000)
          return
        }
        // 获取SESSDATA
        // console.log('body ' + JSON.stringify(body))
        _this.SESSDATA = body.data.url.match(/SESSDATA=(\S*)&bili_jct/)[1]
        // console.log('body.data.uname ' + body.data.uname)
        // _this.uname = body.data.uname
        // _this.face = body.data.face
        // console.log('_this.SESSDATA ' + _this.SESSDATA)
        _this.statusText = '扫码成功'
        _this.isCheck = false
      }
    },
    async handleOk () {
      // console.log('this.SESSDATA ' + this.SESSDATA)
      this.store.set('setting.SESSDATA', this.SESSDATA)
      console.log('uname ' + this.uname)
      console.log('uname ' + this.face)
      this.store.set('uname', this.uname)
      this.store.set('face', this.face)
      // console.log('uname ' + this.uname)
      // 设置登录状态
      const status = await checkLogin()
      this.$store.commit('setLoginStatus', status)
      this.hideLoginModal()
    },
    handleCancel () {
      console.log('handleCancel()')
      this.SESSDATA = ''
      this.uname = ''
      this.face = ''
      this.$store.commit('setShowLoginModal', false)
      this.hideLoginModal()
    },
    async signOut () {
      this.$confirm({
        title: '退出登录',
        content: '确定吗⊙ω⊙确定吗⊙ω⊙确定吗⊙ω⊙',
        cancelText: '取消',
        okText: '确定',
        onOk: () => {
          this.store.set('setting.SESSDATA', '')
          this.$store.commit('setLoginStatus', 0)
        },
        onCancel () {
          console.log('取消')
        }
      })
    }
  }
}
</script>

<style scoped lang="less">
.login{
  .img-box{
    position: relative;
    z-index: 10;
    width: 200px;
    .img-modal{
      position: absolute;
      top: 0px;
      left: 0px;
      right: 0px;
      bottom: 0px;
      z-index: 12;
      background: rgba(255, 255, 255, 0.8);
      .icon{
        font-size: 24px;
      }
    }
    .img{
      display: block;
      position: relative;
      z-index: 11;
      width: 100%;
    }
  }
}
</style>
