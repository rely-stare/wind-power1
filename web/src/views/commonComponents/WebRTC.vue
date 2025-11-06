<!-- 监控组件 -->
<template>
    <video ref="videoRef" style="width:100%" controls autoplay autobuffer muted preload="auto"></video>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import config from '@/config.js'
// 确保这两个文件在 public 目录下
// import 'public/js/webrtcstreamer.js'
// import 'public/js/adapter.min.js'

const props = defineProps({
    rtspUrl: {
        type: String,
        required: true,
        default: 'rtsp://admin:esvtek2019@192.168.31.64:554/h264/ch2/main/av_stream'
    }
})
const serverUrl = config.webRTCServerUrl
const videoRef = ref(null)
let webRtcServer = null
const recordedChunks = []
// 初始化WebRTC连接
const initWebRTC = () => {
    if (!videoRef.value) return

    webRtcServer = new WebRtcStreamer(
        videoRef.value,
        serverUrl
    )
    console.log(props.rtspUrl, 'props.rtspUrl---------')
    webRtcServer.connect(props.rtspUrl)
}
// 下载录制的视频
const downloadRecording = () => {
    if (recordedChunks.length === 0) return

    const blob = new Blob(recordedChunks, {
        type: 'video/webm'
    })

    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `rtsp_recording_${new Date().toISOString()}.webm`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)

    // 清理录制的数据
    recordedChunks = []
}

// 组件挂载时初始化
onMounted(() => {
    initWebRTC()
})

// 组件卸载前断开连接
onBeforeUnmount(() => {
    if (webRtcServer) {
        webRtcServer.disconnect()
        console.log('断连')
        webRtcServer = null
    }
})
defineExpose({
    downloadRecording
})
</script>

<style scoped>
video {
    width: 100%;
    height: 97%;
    background: #000;
}
</style>