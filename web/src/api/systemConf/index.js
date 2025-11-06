import { GET, POST } from '../request'

export const getConfigInfo = (params) => {
  return GET('/config/getConfig', params, {
    headers: {
      isToken: false
    }
  })
}
export const getConfigAllInfo = (params) => {
  return GET('/config/getAllConfig', params, {})
}
export const getDictInfo = () => {
  return GET('/dictionary/getDictionary')
}

export const setUpload = (data) => {
  return POST('/pic/uploadPic', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
export const setConfigInfo = (data) => {
  return POST('/config/saveConfig', data, {})
}
// 获取用户菜单
export const getUserMenu = () => {
  return GET('/menu/getMenu')
}
// 获取视频流
export const getVideoStream = (params) => {
  return GET('video/getVideoStream',params)
}