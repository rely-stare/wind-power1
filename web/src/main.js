import './assets/style/global.scss' //公共样式
import './assets/style/ant-global.scss' //公共样式

// import { useMenuStore } from '@/stores/menu.js' //全局面包屑
import { useSysStore } from '@/stores/system.js' //全局设置主题色

import './assets/iconfont/iconfont.css' //字体样式

import 'amfe-flexible/index.js' //伸缩布局

import i18n from './i18n/index'
import './permission.js' //路由钩子权限
import { createApp } from 'vue'

import store from './stores' //pinia
import App from './App.vue'
import router from './router' // 路由
// import autoFont from './utils/rem.js'
// autoFont()
// window.onresize = function () {
//   autoFont()
// }
const app = createApp(App)
app.use(store)
app.use(i18n)
app.use(router)

const themeColor = useSysStore()
themeColor.setTheme(themeColor.theme)

app.mount('#app')
