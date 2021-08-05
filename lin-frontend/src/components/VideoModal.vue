<template>
  <a-modal
    v-model="visible"
    :confirm-loading="confirmLoading"
    @ok="handleOk"
    @cancel="handleCancel"
    :closable="false"
    title="当前视频信息"
    okText="下载"
    cancelText="取消">
    <div class="video-modal">
      <div class="video-info fr">
        <div class="image">
          <img :src="videoInfo.cover | formatCover" alt="">
        </div>
        <div class="content fc jsa pl16">
          <div class="text-active ellipsis-2" @click="openExternal(videoInfo.url)">{{ videoInfo.title }}</div>
          <!-- <div>up：<span v-for="(item, index) in videoInfo.up" :key="index" class="text-active mr8" @click="openExternal(`https://space.bilibili.com/${item.mid}`)">{{item.name}}</span></div> -->
          <!-- <div>下载路径: </div> -->
          <!-- <div hidden class="video-type" value="{{ videoInfo.type }}"></div> -->
          <!-- <div hidden class="video-fileType" value="{{ videoInfo.fileType }}"></div> -->
          <!-- <div hidden class="video-headers" value="{{ videoInfo.headers }}"></div> -->
        </div>
      </div>
      <a-form :form="form">
          <a-form-item>
            <span slot="label">
              下载路径:
              <a-tooltip>
                <a-icon type="info-circle" style="font-size: 12px;display: inline-block;margin-left: 4px;" />
              </a-tooltip>
            </span>
            <a-input ref="input1" id="downloadPath" readonly="true" v-decorator="['downloadPath', { rules: [{ required: true, message: '请设置下载地址' }] }]" :placeholder="'请设置下载地址'" class="custom-input" @click="openFolder">
            <a-icon slot="suffix" type="folder" style="color: rgba(0,0,0,.45)" />
            </a-input>
          </a-form-item>
      </a-form>
      <div class="mt16">
        选择清晰度：
        <div class="mt8">
          <a-radio-group v-model="quality" :options="qualityOptions" />
        </div>
      </div>
      <div v-if="videoInfo.page && videoInfo.page.length > 1" class="fr ac jsb mt16">
        <div>这是一个多P视频，请选择</div>
      </div>
      <div v-if="videoInfo.page && videoInfo.page.length >= 1" class="fr ac warp mt16">
        <div v-for="(item, index) in videoInfo.page" :key="index" :class="['video-item', selected.includes(item.cid) ? 'active' : '']" @click="toggle(item.cid)">
          <a-tooltip>
            <template slot="title">
              {{ item.title }}
            </template>
            <span class="ellipsis-1">{{ item.title }}</span>
          </a-tooltip>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script>
import base from '../mixin/base'
import md5 from 'js-md5'
// import randomNum from '../utlis/randomNum'
// import sleep from '../utlis/sleep'
// import filterTitle from '../utlis/filterTitle'
// import UA from '../assets/data/ua'
// import formatSeconed from '../utlis/formatSeconed'
// import { userQuality } from '../assets/data/quality'
export default {
  mixins: [base],
  data () {
    return {
      visible: false,
      confirmLoading: false,
      videoInfo: {
        title: '',
        qualityOptions: [],
        page: []
      },
      selected: [],
      videoOptions: [],
      quality: null,
      form: this.$form.createForm(this),
      formItemLayout: {
        labelCol: { span: 7, offset: 0 },
        wrapperCol: { span: 16, offset: 1 }
      },
      videoInfoList: [],
      taskInfoList: []
    }
  },
  components: {},
  computed: {
    qualityOptions () {
      // const quality = userQuality[this.$store.state.loginStatus]
      // return this.videoInfo.qualityOptions.filter(item => quality.includes(item.value))
      return this.videoInfo.qualityOptions
    }
  },
  watch: {},
  filters: {
    formatCover (img) {
      return `http://127.0.0.1:8965/?img=${img}`
    }
  },
  mounted () {
    window.ipcRenderer.on('dir-dialog-reply', (event, arg) => {
      console.log(arg)
      if (!arg.canceled) {
        this.form.setFieldsValue({
          downloadPath: arg.filePaths[0]
        })
      }
    })
    // this.setDefaultSavePath()
  },
  created () {},
  methods: {
    openExternal (url) {
      if (url) {
        window.ipcRenderer.send('open-external', url)
      }
    },
    show (info) {
      this.visible = true
      this.videoInfo = info
    },
    async handleOk () {
      console.log('---' + this.$refs.input1.value)
      if (!this.$refs.input1.value) {
        this.$message.info('请选择下载路径')
        return
      }
      // 判断是否选择清晰度
      if (!this.quality) {
        this.$message.info('请选择清晰度')
        return
      }
      // const requestUrl = 'http://127.0.0.1:8080/lin/anime/download'
      this.confirmLoading = true
      // 判断是否多p视频
      if (this.videoInfo.page.length >= 1) {
        if (!this.selected.length) {
          this.$message.info('请选择要下载的视频')
          this.confirmLoading = false
          return
        }
        // console.log('---' + this.selected)
        for (let index = 0; index < this.selected.length; index++) {
          // console.log('this.selected = ' + JSON.stringify(this.selected))
          const currentPage = this.selected[index]
          // console.log('currentPage = ' + currentPage)
          // console.log(this.videoInfo.page.find(item => item.cid === currentPage).title)
          const title = this.videoInfo.page.find(item => item.cid === currentPage).title
          // console.log('title = ' + title)
          let bvId = ''
          if (this.videoInfo.type === 'BILIBILI') {
            // woc B站需要大会员才能看的番剧，每一集的bvId都不一样(눈_눈)
            bvId = this.videoInfo.page.find(item => item.cid === currentPage).bvId
            // console.log('bvId = ' + bvId)
          }
          let url = ''
          if (this.videoInfo.type === 'ACFUN' || this.videoInfo.type === 'IMOMOE_LA') {
            // const title = this.videoInfo.page.find(item => item.cid === currentPage).title
            url = this.videoInfo.page.find(item => item.cid === currentPage).url
            console.log('url = ' + url)
          }
          // const cId = currentPage
          const id = md5(title + url) // md5(title + url)
          console.log('id = ' + id)
          const info = {
            id: id,
            title: title,
            type: this.videoInfo.type,
            fileType: this.videoInfo.fileType,
            bId: bvId, // bilibili id
            cId: currentPage,
            quality: this.quality,
            totalSize: 0,
            url: url,
            savePath: this.$refs.input1.value,
            coverImg: this.videoInfo.cover
          }
          const task = {
            id: id,
            title: this.videoInfo.title,
            savePath: this.$refs.input1.value,
            coverImg: this.videoInfo.cover,
            status: 0,
            progress: 0,
            downloadSize: 0,
            totalSize: 0,
            speed: 0,
            msg: ''
          }
          this.videoInfoList.push(info)
          this.taskInfoList.push(task)
        }
        console.log('videoInfoList = ' + this.videoInfoList)
      } else {
        // 单P视频，直接拼接 videoUrl + audioUrl
        // 请求选中清晰度视频下载地址
        const dashVideoMap = this.videoInfo.dashVideoMap
        const totalSizeMap = this.videoInfo.totalSizeMap
        const qn = this.quality
        // console.log('qn = ' + qn)
        const videoUrl = dashVideoMap[qn]
        // console.log('totalSizeMap[qn] = ' + JSON.stringify(totalSizeMap))
        // console.log('totalSizeMap[qn] = ' + JSON.stringify(totalSizeMap)[qn])
        const totalSize = totalSizeMap[qn]
        // console.log('totalSize = ' + totalSize)
        let url = videoUrl
        console.log('url = ' + url)
        if (this.videoInfo.type === 'BILIBILI') {
          url = videoUrl + '#' + this.videoInfo.audioUrl
        }
        // else if (this.videoInfo.type === 'IMOMOE_LA') {
        //   const currentPage = this.selected[0]
        //   url = this.videoInfo.page.find(item => item.cid === currentPage).url
        // }
        // console.log(url)
        const id = md5(this.videoInfo.title + url)
        // console.log('md5 = ' + id)
        // send to server to download
        const info = {
          id: id,
          title: this.videoInfo.title,
          type: this.videoInfo.type,
          fileType: this.videoInfo.fileType,
          bId: this.videoInfo.id,
          cId: this.videoInfo.cId,
          quality: this.quality,
          totalSize: totalSize,
          url: url,
          savePath: this.$refs.input1.value,
          coverImg: this.videoInfo.cover
        }
        // store in client
        const task = {
          id: id,
          title: this.videoInfo.title,
          savePath: this.$refs.input1.value,
          coverImg: this.videoInfo.cover,
          status: 0,
          progress: 0,
          downloadSize: 0,
          totalSize: totalSize,
          speed: 0,
          msg: ''
        }
        this.videoInfoList.push(info) // 提交到服务端下载的任务list
        this.taskInfoList.push(task) // 准备保存在客户端的任务list
      }
      // 保存数据
      const taskList = this.store.get('taskList') ? this.store.get('taskList') : []
      this.taskInfoList.forEach(item => {
        // if (JSON.stringify(taskList).indexOf(JSON.stringify(item)) === -1) {
        //   taskList.unshift(item)
        // videoInfoList.forEach(videoInfo => {
        //   if (videoInfo.id === item.id) {
        //     videoInfoList.remove(videoInfo)
        //   }
        // })
        const exist = taskList.some(task => {
          // 本地已存在正在进行中的任务，不重复提交到服务端
          console.log('task.status = ' + task.status)
          if (task.id === item.id && (task.status !== 4 && task.status !== 3)) {
            return true
          }
        })
        // console.log('exist = ' + exist)
        // console.log(task.status)
        if (!exist) {
          taskList.unshift(item)
        } else {
          const index = this.videoInfoList.findIndex(info => info.id === item.id)
          if (index !== -1) {
            console.log('存在相同数据，删除index = ' + index)
            this.videoInfoList.splice(index, 1)
          }
        }
      })
      // console.log('保存数据 ' + taskList)
      this.store.set('taskList', taskList)
      // 调用下载
      // window.ipcRenderer.send('download-video', videoInfo)
      if (this.videoInfoList.length !== 0) {
        const res = await this.got.post(this.api.Api.animeDownload, {
          json: {
            videoInfoList: this.videoInfoList,
            headers: this.videoInfo.headers,
            // savePath: this.$refs.input1.value,
            responseType: 'json'
          }
        })
        const status = res.status
        if (status === -1) {
          this.$message.error('下载出现错误')
        }
      }
      this.confirmLoading = false
      // sleep(500)
      this.$router.push('/download')
    },
    handleCancel () {
      this.visible = false
      this.confirmLoading = false
      this.quality = null
      this.selected = []
    },
    toggle (page) {
      const index = this.selected.indexOf(page)
      if (index === -1) {
        this.selected.push(page)
      } else {
        this.$delete(this.selected, index)
      }
    },
    saveResponseCookies (cookies) {
      if (cookies && cookies.length) {
        const cookiesString = cookies.join(';')
        const setting = this.store.get('setting')
        this.store.set('setting', {
          ...setting,
          bfe_id: cookiesString
        })
      }
    },
    openFolder () {
      console.log('openFolder')
      window.ipcRenderer.send('open-dir-dialog', 'open')
    }
  }
}
</script>

<style scoped lang="less">
.video-modal{
  height: 260px;
  overflow-y: scroll;
  &::-webkit-scrollbar{
    display: none;
  }
  .video-info{
    height: 71.25px;
    .image{
      flex: none;
      width: 114px;
      overflow: hidden;
      position: relative;
      img{
        display: block;
        width: 100%;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
      }
    }
    .content{
      box-sizing: border-box;
      flex: none;
      width: 358px;
    }
  }
  .video-item{
    display: flex;
    justify-content: center;
    align-items: center;
    box-sizing: border-box;
    width: 100px;
    height: 50px;
    border: 1px solid #eeeeee;
    background: #ffffff;
    margin: 0px 18px 18px 0px;
    padding: 8px;
    cursor: pointer;
    overflow: hidden;
    user-select: none;
    &.active{
      color: #ffffff;
      background: @primary-color;
      border: 1px solid @primary-color;
    }
  }
}
.custom-input{
  /deep/ .ant-input{
    cursor: pointer;
  }
}
</style>
