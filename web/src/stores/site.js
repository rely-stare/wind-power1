// 机组及硬件
import { defineStore } from 'pinia'
import eventHub from '@/utils/eventBus'
export const useSiteStore = defineStore('siteStore', {
  state: () => {
    return {
      hardwareId: '', // 硬件id
      hardwareType: '', //硬件类型  1-转速 2-热成像 3-视频 4-音频
      hardwareName: '', //硬件名称
      monitorType:'', // 用来标识当前设备
      fanId:'',  // 风机id
      siteList:[], // 风机列表
      hardwareList:[] // 硬件列表
    }
  },
  actions: {
    
    // 设置风机id
    setFanId(data) {
        this.fanId = data
    },
    // 返回风机id
    getFanId() {
        return this.fanId
    },
    // 设置硬件id
    setHardwareId(data) {
      this.hardwareId = data
    },
    // 返回硬件id
    getHardwareId() {
      return this.hardwareId
    },
    // 设置硬件类型
    setHardwareType(data) {
      this.hardwareType = data
    },
    // 返回硬件类型
    getHardwareType() {
      return this.hardwareType
    },
    // 设置硬件名称
    setHardwareName(data) {
      this.hardwareName = data
    },
    // 获取硬件名称
    getHardwareName() {
      return this.hardwareName
    },
    
    // 设置硬件标识
    setMonitorType(data) {
      this.monitorType = data
    },
    // 获取硬件标识
    getMonitorType() {
      return this.monitorType
    },
    // 设置机组列表
    setFunList(data) {
      this.siteList = data
    },
    // 返回机组列表
    getFunList() {
      return this.siteList
    },
    // 设置硬件列表
    setHardwareList(data){
        this.hardwareList[this.siteId] = data
        eventHub.$emit('hardwareList-updated',{})
    },
    // 返回硬件列表
    getHardwareList(){
        return this.hardwareList[this.siteId]
    }
  },
  // 开启数据缓存
  persist: {
    enabled: false
  }
})
