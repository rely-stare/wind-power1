// 系统配置
import { defineStore } from 'pinia'

export const useSysStore = defineStore('sysStoreId', {
  state: () => {
    return {
      sysConfig: {},
      theme: 'light',
      pwdModal: false,
      isCanClose: true,
      siteList: [],
      siteId: '',
      showMain: true
    }
  },
  actions: {
    // 设置机组id
    setSiteId(data) {
      this.siteId = data
    },
    // 返回机组id
    getSiteId() {
      return this.siteId
    },
    // 设置机组列表
    setSiteList(data) {
      this.siteList = data
    },
    // 返回机组列表
    getSiteList() {
      return this.siteList
    },
    // 获取系统配置
    setSysConf(sysConfig) {
      this.sysConfig = sysConfig
    },
    // 返回系统配置
    getSysConf() {
      return this.sysConfig
    },
    // 更改主题色
    setTheme(data) {
      this.theme = data
      window.document.documentElement.setAttribute('data-theme', this.theme)
    },
    // 修改密码
    changePwd(data, type = true) {
      this.pwdModal = data
      this.isCanClose = type
      console.log('changePwd', this.pwdModal, this.isCanClose)
    }
  },
  // 开启数据缓存
  persist: {
    enabled: true
  }
})
