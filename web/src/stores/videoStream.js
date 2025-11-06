// 视频流
import { defineStore } from 'pinia'

export const useVideoStore = defineStore('videoStore', {
  state: () => {
    return {
      videoStream: '',
    }
  },
  actions: {
    
    // 设置视频流
    setVideoStream(data) {
        this.videoStream = data
    },
    // 返回视频流
    getVideoStream() {
        return this.videoStream
    }
  },
  // 开启数据缓存
  persist: {
    enabled: true
  }
})
