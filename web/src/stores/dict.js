// 字典表
import { defineStore } from 'pinia'
export const useDictStore = defineStore('dictStoreId', {
  state: () => {
    return {
      dictList: []
    }
  },
  actions: {
    setDict(dictList) {
      this.dictList = dictList
    },
    getDictList(field) {
      return this.dictList[field]
    }
  },
  // 开启数据缓存
  persist: {
    enabled: true
  }
})
