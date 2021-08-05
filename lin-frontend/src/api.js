const baseURL = 'http://127.0.0.1:23333'

const APILIST = {
  animeParse: '/lin/anime/parse',
  animeDownload: '/lin/anime/download',
  animeCancel: '/lin/anime/cancel',
  animePause: '/lin/anime/pause',
  animeResume: '/lin/anime/resume',
  animeClose: '/lin/anime/shutdown'
}

const Api = function () {
  const api = {}
  for (const k in APILIST) {
    api[k] = baseURL + APILIST[k]
  }
  return api
}

export default {
  Api: Api()
}
