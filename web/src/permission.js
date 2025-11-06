import router from './router/index'
import { useUserStore } from '@/stores/user.js'
import { useSysStore } from '@/stores/system.js'
import { useMenuStore } from '@/stores/menu.js' //全局面包屑
import NProgress from 'nprogress' // Progress 进度条
import filterAsyncRouter from '@/utils/filterAsyncRouter'
import 'nprogress/nprogress.css' // Progress 进度条样式
NProgress.configure({ showSpinner: false })
const whiteList = ['/login', '/test'] // 白名单
router.beforeEach(async (to, from) => {
  NProgress.start()

  const hasToken = sessionStorage.getItem('token') || ''
  const userStore = useUserStore()
  if (hasToken) {
    //判断token是否存在 存在即为已经登录

    if (to.path !== '/login') {
      if (userStore.init) {
        // 获取了动态路由 init一定true,就无需再次请求 直接放行
        let newMenu = router.getRoutes()
        // next()
        return true
      } else {
        // init为false,一定没有获取动态路由,就跳转到获取动态路由的方法
        const result = await userStore.setUserMenu() //获取路由
        const accessRoutes = filterAsyncRouter(result)
        accessRoutes.forEach((route) => {
          router.addRoute(route)
        })
        let newMenu = router.getRoutes()
        userStore.init = true //init改为true,路由初始化完成
        // next({ ...to, replace: true })
        return { ...to, replace: true } // hack方法 确保addRoute已完成
      }
    } else {
      NProgress.done()
      // next()
      return '/login'
    }
  } else {
    // 白名单，直接放行
    // if (whiteList.indexOf(to.path) > -1) next()
    if (whiteList.indexOf(to.path) > -1) return
    // 非白名单，去登录
    // else next({ path: '/login' })
    else return '/login'
    NProgress.done()
  }
})
//路由后置钩子 设置面包屑
router.afterEach((to) => {
  NProgress.done()
  // 更新面包屑
  const menu = useMenuStore()
  if (to.meta.menuType != 4) {
    menu.isShow = to.matched[1]?.meta.isShow
    // 判断当前跳转路由
    const hasChildren = to.matched.length > 1 && to.matched[1].children
    if (hasChildren) {
      menu.setMenuChild(JSON.parse(JSON.stringify(hasChildren)))
    }
    // 激活菜单组件
    menu.setMenuSelectedKeys(to.matched.map((key) => key.path).splice(1))
    menu.setMenuOpenKeys(to.matched.map((key) => key.name).splice(1))
  }
})
// 1. 在路由钩子里面判断是否首次进入系统(permission.js)
// 2. init为true说明已经获取过路由,就直接放行,init为false则向后台请求用户路由
// 3. 获取路由
// 4. 解析路由,存储权限
// 5. 使用router的api,addRouter拼接路由
// 6. 存储路由
// 7. init改为true,路由初始化完成
// 8. 放行路由
