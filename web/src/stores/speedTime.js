// 机组及硬件
import { defineStore } from 'pinia'
export const useSpeedTimetorStore = defineStore('useSpeedTimetorStore', {
  state: () => {
    return {
      speedTime: '', // 超限最新时间
      fluctuateTime: '', //稳定性最新时间
      averageTime: '', //震荡最新时间
      temperatureTime:'', // 温度最新时间
    }
  },
  actions: {
    
    // 设置超速最新时间
    setSpeedTime(data) {
        this.speedTime = data
    },
    // 返回超速最新时间
    getSpeedTime() {
        return this.speedTime
    },
    // 设置稳定性最新时间
    setFluctuateTime(data) {
      this.fluctuateTime = data
    },
    // 返回稳定性最新时间
    getFluctuateTime() {
      return this.fluctuateTime
    },
    // 设置震荡最新时间
    setAverageTime(data) {
      this.averageTime = data
    },
    // 返回震荡最新时间
    getAverageTime() {
      return this.averageTime
    },
    // 设置温度最新时间
    setTemperatureTime(data) {
      this.temperatureTime = data
    },
    // 获取温度最新时间
    getTemperatureTime() {
      return this.temperatureTime
    },
    
  },
  // 开启数据缓存
  persist: {
    enabled: false
  }
})
