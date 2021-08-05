<template>
  <a-modal
    v-model="visible"
    :closable="true"
    :footer="null">
    <div class="user fc ac">
      <img src="../assets/images/user.png" alt="" class="avatar">
      <div class="version mt16">
        {{ `${projectName} - v${version}` }}
      </div>
      <div class="git mt16">项目地址：<span class="text-active">https://github.com/UHungLin/AnimeDownloader</span></div>
      <div class="desc mt16">邮箱：1251994554@qq.com 欢迎交流~</div>
      <div class="desc mt16">希望世上没有蛀牙 peace~</div>
    </div>
  </a-modal>
</template>

<script>
const packageInfo = require('../../package.json')
export default {
  data () {
    return {
      visible: false,
      projectName: '',
      version: ''
    }
  },
  components: {},
  computed: {},
  watch: {},
  mounted () {
    this.projectName = packageInfo.name
    this.version = packageInfo.version
  },
  created () {},
  methods: {
    show () {
      this.visible = true
    }
  }
}
</script>

<style scoped lang="less">
.user{
  .avatar{
    width: 150px;
    border: 1px solid #eeeeee;
    border-radius: 50%;
  }
}
</style>
