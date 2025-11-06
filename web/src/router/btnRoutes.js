const btnRoutes = [
  {
    path: '/robot',
    name: '',
    children: [
      {
        path: 'details/:devName/:id',
        component: '/device/I-Device/robot/details',
        meta: {
          title: '机器人详情',
          menuType: 4,
          sort: 1,
          isHide: true
        }
      }
    ],
    meta: {
      title: '机器人',
      menuType: 4,
      sort: 1,
      isHide: true
    }
  },
  {
    path: '/UAV',
    name: '',
    children: [
      {
        path: 'details/:devName/:id',
        component: '/device/I-Device/UAV/details',
        meta: {
          title: '无人机详情',
          menuType: 4,
          sort: 1,
          isHide: true
        }
      }
    ],
    meta: {
      title: '无人机',
      menuType: 4,
      sort: 1,
      isHide: true
    }
  }
]
export default btnRoutes
