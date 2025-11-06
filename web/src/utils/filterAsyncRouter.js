const modules = import.meta.glob('@/views/**/**.vue')
function filterAsyncRouter(asyncRouterMap) {
  return asyncRouterMap.filter((route) => {
    if (route.component) {
      if (route.component === 'Home') {
        // route.component = loadView(route.component)
        delete route['component']
      } else {
        // route.component = loadView(route.component)
        route.component = modules[`/src/views${route.component}.vue`]
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}
export const loadView = (view) => {
  // if (process.env.NODE_ENV === 'development') {
  // return (resolve) => require([`@/views/${view}`], resolve);
  // } else {
  // 使用 import 实现生产环境的路由懒加载
  return () => import(`@/views${view}.vue`)
  // }
}
export default filterAsyncRouter
