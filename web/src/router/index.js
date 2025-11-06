import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      meta: {
        title: '登录',
        menuType: 2,
        sort: 1,
        icon: '',
        isShow: true,
        menuType: 2,
        menuState: true,
        isFrame: false,
        isCache: false,
        id: 1
      },
      component: () => import('../views/login/index.vue')
    }
  ]
})

export default router
