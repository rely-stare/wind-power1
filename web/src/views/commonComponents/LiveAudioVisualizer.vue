<template>
    <div class="visualization">
        <div class="waveform-container">
            <canvas ref="waveformCanvas"></canvas>
        </div>

        <div class="frequency-container">
            <canvas ref="frequencyCanvas"></canvas>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Room, RemoteTrack } from 'livekit-client'
const props = defineProps < {
    roomUrl: string,
    token: string
} > ()


// Canvas 引用
const waveformCanvas = ref(null)
const frequencyCanvas = ref(null)

// Audio 上下文和节点
let audioContext = null
let analyserNode = null
let frequencyAnalyser = null
let dataArray = null
let frequencyData = null
let animationFrameId = null
let room = null

// 连接房间
const connectToRoom = async () => {
    try {
        // 初始化音频上下文 (必须在用户交互后)
        audioContext = new (window.AudioContext || window.webkitAudioContext)()

        // 创建分析节点
        analyserNode = audioContext.createAnalyser()
        analyserNode.fftSize = 2048

        frequencyAnalyser = audioContext.createAnalyser()
        frequencyAnalyser.fftSize = 256

        const bufferLength = analyserNode.frequencyBinCount
        dataArray = new Uint8Array(bufferLength)

        const frequencyBufferLength = frequencyAnalyser.frequencyBinCount
        frequencyData = new Uint8Array(frequencyBufferLength)

        // 初始化 LiveKit 房间
        room = new Room({
            adaptiveStream: true,
            dynacast: true,
        })

        // 设置事件监听
        setupRoomListeners()

        // 连接房间
        await room.connect(props.roomUrl, props.token, {
            autoSubscribe: true,
        })

        // 开始可视化
        startVisualization()

    } catch (error) {
        console.error('连接失败:', error)
        cleanupAudio()
    }
}

// 设置房间监听器
const setupRoomListeners = () => {
    if (!room) return

    // 远程参与者发布轨道
    room.on('trackPublished', (publication, participant) => {
        if (publication.kind === 'audio') {
            publication.track.once(RemoteTrack.SwitchedOn, (track) => {
                setupAudioStream(track.mediaStream)
            })
        }
    })

    // 远程参与者取消发布轨道
    room.on('trackUnpublished', (publication, participant) => {
        if (publication.kind === 'audio') {
            cleanupAudio()
        }
    })

    // 活动发言者变化
    room.on('activeSpeakersChanged', (speakers) => {
        if (speakers.length > 0) {
            // activeSpeaker.value = speakers[0].identity
        } else {
            // activeSpeaker.value = ''
        }
    })

    // 断开连接
    room.on('disconnected', () => {
        // isConnected.value = false
        // connectionStatus.value = '已断开连接'
        cleanup()
    })
}

// 设置音频流
const setupAudioStream = (stream) => {
    if (!audioContext || !stream) return

    cleanupAudio()

    try {
        // 创建媒体流源
        const source = audioContext.createMediaStreamSource(stream)

        // 连接到分析器
        source.connect(analyserNode)
        source.connect(frequencyAnalyser)

        // 连接到目的地 (避免音频被静音)
        analyserNode.connect(audioContext.destination)

        // connectionStatus.value = '音频流已连接，开始可视化...'

    } catch (error) {
        console.error('设置音频流失败:', error)
        // connectionStatus.value = `音频流错误: ${error.message}`
    }
}

// 开始可视化
const startVisualization = () => {
    if (!waveformCanvas.value || !frequencyCanvas.value) return

    const waveformCtx = waveformCanvas.value.getContext('2d')
    const frequencyCtx = frequencyCanvas.value.getContext('2d')

    // 设置 Canvas 尺寸
    const updateCanvasSize = () => {
        waveformCanvas.value.width = waveformCanvas.value.clientWidth
        waveformCanvas.value.height = waveformCanvas.value.clientHeight

        frequencyCanvas.value.width = frequencyCanvas.value.clientWidth
        frequencyCanvas.value.height = frequencyCanvas.value.clientHeight
    }

    updateCanvasSize()
    window.addEventListener('resize', updateCanvasSize)

    // 绘制波形
    const drawWaveform = () => {
        if (!analyserNode || !waveformCtx) return

        animationFrameId = requestAnimationFrame(drawWaveform)

        analyserNode.getByteTimeDomainData(dataArray)

        waveformCtx.fillStyle = 'rgb(20, 20, 30)'
        waveformCtx.fillRect(0, 0, waveformCanvas.value.width, waveformCanvas.value.height)

        waveformCtx.lineWidth = 2
        waveformCtx.strokeStyle = 'rgb(0, 200, 200)'
        waveformCtx.beginPath()

        const sliceWidth = waveformCanvas.value.width / analyserNode.fftSize
        let x = 0

        for (let i = 0; i < analyserNode.fftSize; i++) {
            const v = dataArray[i] / 128.0
            const y = v * waveformCanvas.value.height / 2

            if (i === 0) {
                waveformCtx.moveTo(x, y)
            } else {
                waveformCtx.lineTo(x, y)
            }

            x += sliceWidth
        }

        waveformCtx.lineTo(waveformCanvas.value.width, waveformCanvas.value.height / 2)
        waveformCtx.stroke()
    }

    // 绘制频谱
    const drawFrequency = () => {
        if (!frequencyAnalyser || !frequencyCtx) return

        frequencyAnalyser.getByteFrequencyData(frequencyData)

        frequencyCtx.fillStyle = 'rgb(20, 20, 30)'
        frequencyCtx.fillRect(0, 0, frequencyCanvas.value.width, frequencyCanvas.value.height)

        const barWidth = (frequencyCanvas.value.width / frequencyAnalyser.frequencyBinCount) * 2.5
        let x = 0

        for (let i = 0; i < frequencyAnalyser.frequencyBinCount; i++) {
            const barHeight = (frequencyData[i] / 255) * frequencyCanvas.value.height

            frequencyCtx.fillStyle = `rgb(${barHeight + 100}, 50, 200)`
            frequencyCtx.fillRect(
                x,
                frequencyCanvas.value.height - barHeight,
                barWidth,
                barHeight
            )

            x += barWidth + 1
        }

        animationFrameId = requestAnimationFrame(drawFrequency)
    }

    drawWaveform()
    drawFrequency()
}

// 断开连接
const disconnect = () => {
    if (room) {
        room.disconnect()
    }
}

// 清理音频资源
const cleanupAudio = () => {
    if (animationFrameId) {
        cancelAnimationFrame(animationFrameId)
        animationFrameId = null
    }

    if (analyserNode) {
        analyserNode.disconnect()
    }

    if (frequencyAnalyser) {
        frequencyAnalyser.disconnect()
    }
}

// 全面清理
const cleanup = () => {
    cleanupAudio()

    if (audioContext && audioContext.state !== 'closed') {
        audioContext.close()
    }

    audioContext = null
    analyserNode = null
    frequencyAnalyser = null
}
// 组件生命周期
onMounted(async () => {
    await connectToRoom()
})

// 组件卸载时清理
onUnmounted(() => {
    disconnect()
    cleanup()
})
</script>

<style scoped>
.waveform-container,
.frequency-container {
    height: 80px !important;
    width: 100%;
    border-radius: 8px;
    overflow: hidden;
}

canvas {
    width: 100%;
    height: 100%;
    display: block;
}
</style>