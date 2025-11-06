<template>
    <div class="audio-visualizer">
        <div ref="waveformRef" id="waveform" class="waveform-container"></div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import WaveSurfer from 'wavesurfer.js'
import Spectrogram from 'wavesurfer.js/dist/plugins/spectrogram.esm.js'
import {
    Room,
    RoomEvent,
    RemoteParticipant,
    RemoteTrackPublication,
    Track,
    RoomOptions
} from 'livekit-client'

const props = defineProps<{
    roomUrl: string
    token: string
}>()

// 状态管理
const waveformRef = ref<HTMLElement | null>(null)
const wavesurfer = ref<WaveSurfer | null>(null)
const room = ref<Room | null>(null)
const isPlaying = ref(false)
const isReady = ref(false)
const connectionState = ref<'connecting' | 'connected' | 'disconnected' | 'error'>('disconnected')
const audioElement = ref<HTMLAudioElement | null>(null)
let animationId = ref()
// 计算属性
const statusMessage = computed(() => {
    switch (connectionState.value) {
        case 'connecting': return '正在连接...'
        case 'connected': return '已连接'
        case 'disconnected': return '未连接'
        case 'error': return '连接错误'
    }
})

// 初始化 WaveSurfer
const initWaveSurfer = () => {
    if (!waveformRef.value) return

    wavesurfer.value = WaveSurfer.create({
        container: waveformRef.value,
        waveColor: '#00FF00',
        cursorColor: '#00D1FF',
        progressColor: '#7CFC00',
        responsive: true,
        normalize: true,
        mediaControls: false,
        interact: true,
        hideScrollbar: false,
    })
    // wavesurfer.value.registerPlugin(
    //     Spectrogram.create({
    //         labels: true,
    //         height: 260,
    //         splitChannels: true,
    //         scale: 'mel', // or 'linear', 'logarithmic', 'bark', 'erb'
    //         frequencyMax: 8000,
    //         frequencyMin: 0,
    //         fftSamples: 1024,
    //         labelsBackground: 'rgba(0, 0, 0, 0.1)',
    //     }),
    // )
    // 监听事件
    wavesurfer.value.on('ready', () => {
        console.log('WaveSurfer ready')
        isReady.value = true
    })

    wavesurfer.value.on('error', (error) => {
        console.error('WaveSurfer error:', error)
        connectionState.value = 'error'
    })

}

// 初始化 LiveKit 房间
const initLiveKitRoom = async () => {
    try {
        connectionState.value = 'connecting'

        room.value = new Room({
            adaptiveStream: true,
            dynacast: true,
            publishDefaults: {
                simulcast: false,
            },
        })

        // 监听房间事件
        room.value.on(RoomEvent.Connected, () => {
            console.log('Connected to room')
            connectionState.value = 'connected'
        })

        room.value.on(RoomEvent.Disconnected, () => {
            console.log('Disconnected from room')
            connectionState.value = 'disconnected'
        })

        room.value.on(RoomEvent.TrackSubscribed, handleTrackSubscribed)
        room.value.on(RoomEvent.TrackUnsubscribed, handleTrackUnsubscribed)

        // 连接到房间
        await room.value.connect(props.roomUrl, props.token)
        console.log('Room connection initialized')

    } catch (error) {
        console.error('Failed to connect to room:', error)
        connectionState.value = 'error'
    }
}

const handleTrackSubscribed = async (
    track: Track,
    publication: RemoteTrackPublication,
    participant: RemoteParticipant
) => {
    if (track.kind === Track.Kind.Audio) {
        try {
            console.log('Audio track subscribed')
            const mediaStream = new MediaStream([track.mediaStreamTrack])

            // 创建音频上下文和分析器
            const audioContext = new (window.AudioContext || window.webkitAudioContext)()
            const analyser = audioContext.createAnalyser()
            const source = audioContext.createMediaStreamSource(mediaStream)

            // 配置分析器
            analyser.fftSize = 2048
            const bufferLength = analyser.frequencyBinCount
            const dataArray = new Float32Array(bufferLength)
            // 连接音频节点
            source.connect(analyser)

            // 重新初始化 WaveSurfer
            if (wavesurfer.value) {
                wavesurfer.value.destroy()
            }

            // 创建波形容器
            const waveformCanvas = document.createElement('canvas')
            waveformCanvas.style.width = '100%'
            waveformCanvas.style.height = '100%'
            waveformRef.value.innerHTML = ''
            waveformRef.value.appendChild(waveformCanvas)

            const ctx = waveformCanvas.getContext('2d')

            // 设置画布尺寸
            const updateCanvasSize = () => {
                const rect = waveformRef.value.getBoundingClientRect()
                waveformCanvas.width = rect.width
                waveformCanvas.height = rect.height
            }

            updateCanvasSize()
            window.addEventListener('resize', updateCanvasSize)

            // // 创建实时更新函数
            // let animationId: number
            const drawWaveform = () => {
                if (!ctx) return

                // 获取音频数据
                analyser.getFloatTimeDomainData(dataArray)

                // 清除画布
                ctx.clearRect(0, 0, waveformCanvas.width, waveformCanvas.height)

                // 设置波形样式
                ctx.lineWidth = 2
                ctx.strokeStyle = '#00FF00'
                ctx.beginPath()

                const sliceWidth = waveformCanvas.width / bufferLength
                let x = 0

                for (let i = 0; i < bufferLength; i++) {
                    const v = dataArray[i] * 0.5
                    const y = (v * waveformCanvas.height) + (waveformCanvas.height / 2)

                    if (i === 0) {
                        ctx.moveTo(x, y)
                    } else {
                        ctx.lineTo(x, y)
                    }

                    x += sliceWidth
                }

                ctx.lineTo(waveformCanvas.width, waveformCanvas.height / 2)
                ctx.stroke()

                // 继续动画循环
                animationId.value = requestAnimationFrame(drawWaveform)
            }

            // 开始波形动画
            drawWaveform()

            // 设置状态
            isReady.value = true
            isPlaying.value = true
            connectionState.value = 'connected'

            // 清理函数
            const cleanup = () => {
                cancelAnimationFrame(animationId.value)
                window.removeEventListener('resize', updateCanvasSize)
                source.disconnect()
                analyser.disconnect()
            }

            // 组件卸载时清理
            onUnmounted(cleanup)

        } catch (error) {
            console.error('Error handling audio track:', error)
            connectionState.value = 'error'
        }
    }
}

// 处理音频轨道取消订阅
const handleTrackUnsubscribed = (
    track: Track,
    publication: RemoteTrackPublication,
    participant: RemoteParticipant
) => {
    if (track.kind === Track.Kind.Audio) {
        console.log('Audio track unsubscribed')
        if (wavesurfer.value) {
            wavesurfer.value.empty()
        }
        if (audioElement.value) {
            audioElement.value.remove()
            audioElement.value = null
        }
        isReady.value = false
    }
}
// 添加音频处理工具函数
const processAudioData = (audioData: Float32Array): number[] => {
    const chunkSize = 100 // 调整此值以改变波形的精度
    const result = []

    for (let i = 0; i < audioData.length; i += chunkSize) {
        const chunk = audioData.slice(i, i + chunkSize)
        const amplitude = Math.max(...chunk.map(Math.abs))
        result.push(amplitude)
    }

    return result
}

// 组件生命周期
onMounted(async () => {
    initWaveSurfer()
    await initLiveKitRoom()
})

onUnmounted(() => {
    if (wavesurfer.value) {
        wavesurfer.value.destroy()
    }
    if (room.value) {
        room.value.disconnect()
    }
    if (audioElement.value) {
        audioElement.value.remove()
    }
    if (animationId.value) {
        cancelAnimationFrame(animationId.value)
    }
})
</script>
<style scoped>
.waveform-container {
    height: 80px !important;
    width: 100%;
    border-radius: 8px;
    overflow: hidden;
    position: relative;
}
</style>