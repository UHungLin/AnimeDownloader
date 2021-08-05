import UA from '../assets/data/ua'
const got = require('got')

/**
 *
 * @returns 0: 未登录 1：普通会员 2：大会员
 */
const checkLogin = async () => {
  console.log('运行 checkLogin')
  const SESSDATA = window.remote.getGlobal('store').get('setting.SESSDATA')
  const { body } = await got('https://api.bilibili.com/x/web-interface/nav', {
    headers: {
      'User-Agent': `${UA}`,
      cookie: `SESSDATA=${SESSDATA}`
    },
    responseType: 'json'
  })
  console.log(SESSDATA)
  console.log(body)
  if (body.data.isLogin) {
    window.remote.getGlobal('store').set('uname', body.data.uname)
    window.remote.getGlobal('store').set('face', body.data.face)
  }
  if (!body.data.isLogin) return 0
  if (body.data.isLogin && !body.data.vipStatus) return 1
  if (body.data.isLogin && body.data.vipStatus) return 2
}

export {
  checkLogin
}
