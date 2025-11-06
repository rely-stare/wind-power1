// 系统配置
import { defineStore } from 'pinia'
import { getUserMenu } from '@/api/systemConf/index.js'
import $X from '@/utils/$X.js'
import btnRoutes from '@/router/btnRoutes.js'
export const useUserStore = defineStore('userStoreId', {
  state: () => {
    return {
      userInfo: {},
      userMenu: [],
      init: false,
      sysShow: true,
      ledgerShow: true,
      largeScreen: true,
      homeShow: true
    }
  },
  actions: {
    setUserInfo(data) {
      this.userInfo = data
    },
    getUserInfo() {
      return this.userInfo
    },
    setUserMenu() {
      return new Promise((resolve, reject) => {
        getUserMenu().then((res) => {
          this.sysShow = res.data.find((item) => item.path == '/system')
          // this.ledgerShow = res.data.find((item) => item.path == '/device') 
          this.largeScreen = res.data.find((item) => item.path == '/largeScreen')
          // this.homeShow = res.data.find((item) => item.path == '/overview')

          let asyncRoutes = $X.buildTree(res.data, null)
          this.userMenu = asyncRoutes.filter((item) => {
            return item.meta.isShow
          })
          let obj = {
            path: '/',
            name: 'home',
            component: '/home/index',
            children: [...asyncRoutes, ...btnRoutes],
            // children: [...btnMenuRoutes],
            redirect: '/I-Visitation',
            meta: {
              title: '首页',
              id: 2
            }
          }
          let newMenu = $X.setMenu([obj])

          resolve(newMenu)
        })
      }).catch((error) => {
        console.log(error)
        this.init = false
        reject(error)
      })
    }
  },
  // 开启数据缓存
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'userStoreId',
        paths: ['userInfo']
      }
    ]
  }
})
