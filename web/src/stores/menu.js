import { defineStore } from 'pinia'
export const useMenuStore = defineStore('menuStoreId', {
  state: () => {
    return {
      openKeys: ['/overview'], //展开的菜单
      selectedKeys: ['/overview'], //选中的菜单
      menuChild: [], //选种一级菜单的子菜单
      menuChildActive: true, //激活左侧菜单栏
      isShow: ''
    }
  },
  actions: {
    setMenuSelectedKeys(data) {
      this.selectedKeys = data
    },
    setMenuOpenKeys(data) {
      this.openKeys = data
    },
    setMenuChild(data) {
      this.menuChild = data
    },
  },
  // 开启数据缓存
  persist: {
    enabled: true
  }
})
