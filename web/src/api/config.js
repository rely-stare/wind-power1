import axios from 'axios'
import { message } from 'ant-design-vue'
import { useSysStore } from '@/stores/system.js'
import router from '@/router'
import qs from 'qs' //参数编译
let pending = [] //用于存储每个ajax请求的取消函数和ajax标识

const axiosConfig = {
  // baseURL: import.meta.env.VITE_BASE_API, //环境地址
  baseURL: '/auth', //环境地址
  timeout: 1000*60, // 超时时间（可以根据不同的环境配置响应时间）
  withCredentials: true // 是否允许携带cookie
}
axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const axiosService = axios.create(axiosConfig)
let key = ''
// 请求拦截
axiosService.interceptors.request.use(
  (config) => {
    const isToken = (config.headers || {}).isToken === false
    if (!isToken) {
      config.headers['token'] = sessionStorage.getItem('token') // 让每个请求携带自定义token 请根据实际情况自行修改
    }

    if (config.preventRepeat) {
      if (config.method == 'post') {
        key = '/auth' + config.url + '?' + JSON.stringify(config.data)
      } else {
        key = '/auth' + config.url + '?' + qs.stringify(config.params)
      }
      console.log('key值就是要联谊', key)
      config.cancelToken = new axios.CancelToken((cancelToken) => {
        // 判断是否存在请求中的当前请求 如果有取消当前请求
        if (existInPending(key)) {
          cancelToken()
        } else {
          pushPending({ key, cancelToken })
        }
      })
    }
    return config
  },
  (error) => errorHandler(error)
)
// 响应拦截
axiosService.interceptors.response.use(
  (res) => {
    const { config, data } = res
    if (config.preventRepeat) {
      removePending(key)
    }
    if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
      return data
    }
    // 错误处理（我们的所有接口都会默认返回一个success用来判断成功还是失败）
    if (data && !String(data.code).includes(200)) {
      return errorHandler(data)
    } else {
      const sysConf = useSysStore()
      switch (data.code) {
        case 2001:
          if (window.location.pathname == '/login') {
            sessionStorage.clear()
            router.replace('/login')
            return errorHandler(data)
          } else {
            sysConf.changePwd(true, false)
            return errorHandler(data)
          }
        case 2002:
          errorHandler(data)
          sessionStorage.clear()
          router.replace('/login')
          break
        case 3001:
          errorHandler(data)
          sessionStorage.clear()
          router.replace('/login')
          break
        case 200:
          return data
      }
    }

    // do something.....

    // return data
  },
  // 错误处理
  (error) => errorHandler(error)
)

// 错误异常
const errorHandler = (error) => {
  message.destroy()
  message.error(error.message)
  return Promise.reject(error)
}

//请求开始前推入pending
export const pushPending = (item) => {
  pending.push(item)
}
//请求完成后取消该请求，从列表删除
export const removePending = (key) => {
  for (let p in pending) {
    if (pending[p].key === key) {
      //当前请求在列表中存在时
      pending[p].cancelToken() //执行取消操作
      pending.splice(p, 1) //把这条记录从列表中移除
    }
  }
}
//请求前判断是否已存在该请求
export const existInPending = (key) => {
  return pending.some((e) => e.key === key)
}

// 默认导出
export default axiosService
