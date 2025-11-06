import { POST, GET } from '../request'

export const getLoginInfo = (data, ip) => {
  return POST('/auth/login', data, {
    headers: {
      isToken: false,
      ip: ip
    },
    preventRepeat: true
  })
}
export const loginOut = () => {
  return GET('/auth/logout')
}

// 获取验证码
export const getCaptcha = () => {
  return GET('/auth/captcha', {}, { preventRepeat: true })
}
