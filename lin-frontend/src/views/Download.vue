<template>
  <div :class="['container fr', !taskList || !taskList.length ? 'ac jc' : 'bg-fff']">
    <div class="back-icon">
      <a-icon type="rollback" class="icon" @click="goHome" />
    </div>
    <a-empty v-if="!taskList || !taskList.length" :image="require('../assets/images/no-data.png')">
      <span slot="description" class="text-active" style="font-weight: bold">暂无数据</span>
    </a-empty>
    <template v-else>
        <div class="left">
        <div class="">
          <a-button icon="folder" type="primary" @click="openFolder(current)">打开</a-button>
          <a-button class="ml16" icon="delete" type="primary" @click="delDir(current)">删除</a-button>
          <a-button class="ml16" icon="pause" type="primary" @click="pause(current)">暂停</a-button>
          <a-button class="ml16" icon="resume" type="primary" @click="resume(current)">恢复</a-button>
        </div>
        <br>
        <div v-for="(item, index) in taskList" :key="index" :class="['fr', 'download-item', selected === index ? 'active' : '']" @click="switchItem(index)">
          <div class="img fr ac">
            <img :src="item.coverImg | formatCover" :alt="item.title">
          </div>
          <div class="content fc jsb">
            <div class="ellipsis-1">{{ item.title }}</div>
            <div>状态：<span class="text-active">{{ item.status | formatProgress }}  {{ item.speed }}  {{ item.msg }}</span></div>
            <div>已下载：<span class="text-active">{{ item.downloadSize }} / {{ item.totalSize }}</span></div>
            <div v-if="item.totalSize !== '0.00MB'">
              <a-progress :percent="item.progress" :status="item.status | formatStatus" strokeColor="#32A0FF"></a-progress>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import base from '../mixin/base'
import { quality } from '../assets/data/quality'
// import sleep from '../utlis/sleep'
const fs = require('fs')
// const os = require('os')
export default {
  mixins: [base],
  data () {
    return {
      taskList: [],
      selected: null,
      current: null,
      intervalTaskList: [],
      buttonStatus: true
      // timer: ''
      // lock: true
      // websocketObj: null
      // wsHeartflag: false
    }
  },
  components: {},
  computed: {},
  watch: {},
  filters: {
    formatStatus (status) {
      const mapData = {
        0: 'active',
        1: 'active',
        2: 'active',
        4: 'success',
        5: 'success',
        3: 'exception'
      }
      return mapData[status]
    },
    formatProgress (status) {
      const mapData = {
        0: '等待下载中...',
        1: '下载中...',
        2: '暂停中...',
        3: '下载失败',
        4: '已完成',
        5: '重试中...'
      }
      return `${mapData[status]}`
    },
    formatQuality (id) {
      return quality[id]
    },
    formatCover (img) {
      return `http://127.0.0.1:8965/?img=${img}`
    }
  },
  mounted () {
    this.getTaskList()
    window.ipcRenderer.on('download-progress', this.handleProgress)
  },
  created () {
  },
  destroyed () {
    window.ipcRenderer.removeListener('download-progress', this.handleProgress)
    // clearInterval(this.timer)
  },
  methods: {
    openFolder (videoInfo) {
      window.remote.shell.showItemInFolder(videoInfo.savePath)
    },
    delDir (videoInfo) {
      console.log('删除 ' + JSON.stringify(videoInfo))
      this.$confirm({
        title: '你确定要删除当前任务吗',
        content: '确定吗⊙ω⊙确定吗⊙ω⊙确定吗⊙ω⊙(不会删除已下载完成的文件)',
        cancelText: '取消',
        okText: '删除',
        onOk: () => {
          const taskList = this.store.get('taskList')
          const index = taskList.findIndex(item => item.id === videoInfo.id)
          taskList.splice(index, 1)
          this.store.set('taskList', taskList)
          this.$message.success('删除成功(｡･ω･｡)ﾉ♡')
          this.getTaskList()
          // const requestUrl = 'http://127.0.0.1:8080/lin/anime/cancel'
          this.got.post(this.api.Api.animeCancel, {
            json: {
              id: videoInfo.id,
              responseType: 'json'
            }
          })
        },
        onCancel () {
          console.log('取消')
        }
      })
    },
    pause (videoInfo) {
      if (!this.buttonStatus) {
        return
      }
      this.buttonStatus = false
      const taskList = this.store.get('taskList')
      const index = taskList.findIndex(item => item.id === videoInfo.id)
      if (taskList[index].status === 2 || taskList[index].status === 3 || taskList[index].status === 4) {
        this.buttonStatus = true
        return
      }
      taskList[index].status = 2
      taskList[index].speed = 0
      taskList[index].msg = ''
      // console.log('暂停，更新 ' + taskList[index].id + ' 后储存 taskList')
      // console.log(JSON.stringify(taskList))
      this.store.set('taskList', taskList)
      // this.$message.success('暂停成功')
      // this.getTaskList()
      this.$set(this.taskList, index, {
        // ...this.taskList[index],
        id: taskList[index].id,
        coverImg: taskList[index].coverImg,
        savePath: taskList[index].savePath,
        title: taskList[index].title,
        status: 2,
        progress: taskList[index].progress,
        downloadSize: taskList[index].downloadSize,
        totalSize: taskList[index].totalSize,
        speed: 0,
        msg: ''
      })
      this.buttonStatus = true
      // console.log('更新 this.taskList ' + JSON.stringify(this.taskList))
      // index = this.taskList.findIndex(item => item.id === videoInfo.id)
      // this.taskList[index] = 2
      // const requestUrl = 'http://127.0.0.1:8080/lin/anime/pause'
      this.got.post(this.api.Api.animePause, {
        json: {
          id: videoInfo.id,
          responseType: 'json'
        }
      })
    },
    resume (videoInfo) {
      if (!this.buttonStatus) {
        return
      }
      this.buttonStatus = false
      const taskList = this.store.get('taskList')
      let index = taskList.findIndex(item => item.id === videoInfo.id)
      if (taskList[index].status === 0 || taskList[index].status === 1 || taskList[index].status === 4) {
        this.buttonStatus = true
        return
      }
      taskList[index].status = 0
      // console.log('重启，更新 ' + taskList[index].id + ' 后储存 taskList')
      // console.log(JSON.stringify(taskList))
      this.store.set('taskList', taskList)
      index = this.taskList.findIndex(item => item.id === videoInfo.id)
      this.taskList[index].status = 0
      // console.log('更新 this.taskList ' + JSON.stringify(this.taskList))
      this.$set(this.taskList, index, {
        // ...this.taskList[index],
        id: this.taskList[index].id,
        coverImg: this.taskList[index].coverImg,
        savePath: this.taskList[index].savePath,
        title: this.taskList[index].title,
        status: 0,
        progress: this.taskList[index].progress,
        downloadSize: this.taskList[index].downloadSize,
        totalSize: this.taskList[index].totalSize,
        speed: 0,
        msg: ''
      })
      this.buttonStatus = true
      // const requestUrl = 'http://127.0.0.1:8080/lin/anime/resume'
      this.got.post(this.api.Api.animeResume, {
        json: {
          id: videoInfo.id,
          responseType: 'json'
        }
      })
    },
    getVideoSize (videoInfo) {
      const setting = this.store.get('setting')
      fs.stat(`${setting.downloadPath}/${videoInfo.title}-${videoInfo.id}/${videoInfo.title}.mp4`, (err, info) => {
        if (err) {
          console.log(err)
        } else {
          const taskList = this.store.get('taskList')
          const index = taskList.findIndex(item => item.id === videoInfo.id)
          taskList[index].size = `${(info.size / 1024 / 1024).toFixed(2)}MB`
          this.store.set('taskList', taskList)
          this.getTaskList()
        }
      })
    },
    handleProgress (event, res) {
      const result = res.res
      const data = result.data
      const totalSize = `${(data.totalSize / 1024 / 1024).toFixed(2)}MB`
      const downloadSize = `${(data.currentSize / 1024 / 1024).toFixed(2)}MB`
      const progress = parseInt(data.currentSize / data.totalSize * 100)
      const speed = `${(data.speed / 1024 / 1024).toFixed(2)}MB/s`
      let index = this.taskList.findIndex(item => item.id === data.id)
      if (index === -1) return
      this.$set(this.taskList, index, {
        // ...this.taskList[index],
        id: this.taskList[index].id,
        coverImg: this.taskList[index].coverImg,
        savePath: this.taskList[index].savePath,
        title: data.title,
        status: result.status,
        progress: progress,
        downloadSize: downloadSize,
        totalSize: totalSize,
        speed: speed,
        msg: result.msg
      })
      // 保存数据
      const taskList = this.store.get('taskList')
      index = taskList.findIndex(item => item.id === data.id)
      taskList[index].status = result.status
      taskList[index].progress = progress
      taskList[index].downloadSize = downloadSize
      taskList[index].totalSize = totalSize
      taskList[index].speed = speed
      taskList[index].title = data.title
      taskList[index].msg = result.msg
      this.store.set('taskList', taskList)
      if (result.type === 1) {
        console.log('this.global.ws = ' + this.global.ws)
        this.global.ws.send(result.id)
      }
    },
    openExternal (url) {
      if (url) {
        window.ipcRenderer.send('open-external', url)
      }
    },
    getTaskList () {
      this.taskList = this.store.get('taskList') ? this.store.get('taskList') : []
      console.log(JSON.stringify(this.taskList))
      // this.taskList = [{ id: 1, title: '测试', status: 1, progress: 30, size: '1024000', cover: 'https://i0.hdslb.com/bfs/article/040683786a0945d285c1c1f9b24b9018865f58b0.jpg' }]
      // console.log('hahaha')
      if (this.taskList && this.taskList.length) {
        console.log('taskList[0].id = ' + this.taskList[0].id)
        this.switchItem(0)
      }
    },
    goHome () {
      this.$router.push('/')
    },
    switchItem (index) {
      this.selected = index
      this.current = this.taskList[index]
    }
  }
}
</script>

<style scoped lang="less">
.container{
  box-sizing: border-box;
  padding: 16px;
  position: relative;
  height: calc(100vh - 28px);
  &.bg-fff{
    background: #ffffff;
  }
  .back-icon{
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
  .left{
    flex: 5;
    // flex-direction: row;
    border-top: 1px solid #eeeeee;
    border-right: 1px solid #eeeeee;
    overflow-y: scroll;
    &::-webkit-scrollbar{
      display: none;
    }
    .download-item{
      border-bottom: 1px solid #eeeeee;
      cursor: pointer;
      &.active{
        background: rgba(50, 160, 255, 0.2);
      }
      .img{
        flex: none;
        width: 106px;
        height: 79px;
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
        width: 364px;
        padding: 8px;
      }
    }
  }
  .right{
    position: relative;
    flex: 3;
    .image{
      height: 179px;
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
    .operate{
      position: absolute;
      width: 100%;
      bottom: 0px;
      left: 0px;
    }
  }
}
/deep/.ant-progress-status-success .ant-progress-text{
  color: @primary-color;
}
</style>
