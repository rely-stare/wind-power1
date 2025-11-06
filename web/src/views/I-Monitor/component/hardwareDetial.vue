<template>
    <div class="func-box">
        <div class="top-titel-box">
            <div class="title">{{ hardwareTypeName + '--' + hardwareName }}</div>
            <div class="operat-box">
                <a-button class="save-btn" @click="saveData">保存数据</a-button>
            </div>
        </div>
        <!-- <span style="color:#fff">{{ hardwareId }}{{ hardwareType }}{{ detailItem }}{{ monitorType }}{{ fanId }}</span> -->
        <div class="hardware-box">
            <div v-if="hardwareType == 1 && detailItem.siteId">
                <!-- 超限 -->
                <RealTimeSpeedLineChart :sseUrl="detailItem" :legendFlag="true" :height="450" class="line-chart"
                    v-if="monitorType == 1" ref="speedChart" />
                <!-- 稳定性 -->
                <RealTimeSpeedFluctuateLineChart :legendFlag="true" :sseUrl="detailItem" :height="450"
                    v-if="monitorType == 2 && detailItem.siteId" class="line-chart" ref="fluctuateChart" />
                <!-- 震荡 -->
                <RealTimeSpeedAverageLineChart :legendFlag="true" :sseUrl="detailItem" :height="450"
                    v-if="monitorType == 3" class="line-chart" ref="averageChart" />
                <!-- 频谱 -->
                <HistoryLineChart :data="chartDataHistoryOne" :height="250"
                    v-if="monitorType == 4 && chartDataHistoryOne.length > 0" class="line-chart" ref="fftChartOne"
                    key="fftOne" />
                <empty v-if="monitorType == 4 && chartDataHistoryOne.length == 0 && chartDataHistoryTwo.length == 0"
                    :style="{ width: '100%', height: 250 + 'px', marginTop: '100px' }"></empty>
                <HistoryLineChart key="fftTwo" :data="chartDataHistoryTwo" :height="250"
                    v-if="monitorType == 4 && chartDataHistoryTwo.length > 0" class="line-chart" ref="fftChartTwo" />
            </div>
            <div v-if="hardwareType == 2 && detailItem.hardwareId && detailItem.url">
                <!-- <span style="color:#fff">{{ detailItem }}</span> -->
                <!-- 当类型是热成像并且hardwareId赋值后在渲染 -->
                <div class="line-video-box">
                    <div class="video">
                        <WebRTCPlayer :rtsp-url="detailItem.url" />
                    </div>
                    <div class="line">
                        <div class="title">最高温度</div>
                        <RealTimeLineChart :sseUrl="detailItem" style="width:400px;height:200px;" ref="tempChart" />
                    </div>
                </div>
                <div class="line-chart">
                    <div class="top-titel-box">
                        <div class="title">近一年最高温度统计</div>
                        <div class="operat-box">
                            <a-button class="save-btn" @click="longTempChart">保存数据</a-button>
                        </div>
                    </div>
                    <HistoryLineChart :data="chartData" :height="300" :pointFlag="true" ref="tempLongChart" />
                </div>
            </div>
            <!-- 当类型是热成像并且url赋值后在渲染 -->
            <WebRTCPlayer :rtsp-url="detailItem.url" v-if="hardwareType == 3 && detailItem.url" />
            <LiveAudioVisualizer :roomUrl="detailItem.serverUrl" :token="detailItem.token"
                v-if="hardwareType == 4 && detailItem.serverUrl" />
        </div>
    </div>
</template>

<script setup lang='ts'>
import WebRTCPlayer from '@/views/commonComponents/WebRTC.vue'
import LiveAudioVisualizer from '@/views/commonComponents/LiveAudioVisualizer.vue'
import RealTimeLineChart from '@/views/commonComponents/RealTimeLineChart.vue'
import RealTimeSpeedLineChart from '@/views/commonComponents/RealTimeSpeedLineChart.vue'
import RealTimeSpeedFluctuateLineChart from '@/views/commonComponents/RealTimeSpeedFluctuateLineChart.vue'
import RealTimeSpeedAverageLineChart from '@/views/commonComponents/RealTimeSpeedAverageLineChart.vue'
import HistoryLineChart from '@/views/commonComponents/historyLineChart.vue'
import empty from '@/views/commonComponents/empty.vue'
import { getVideoStream, getAudioStreamLive, getLongTemperatureChart, getFftChart, getFftChart2, getLastFFTActive } from '@/api/dataCenter/index.js'
import { analysisfftExport, analysisAverageExport, analysisSpeedExport, analysisFluctuteExport, analysisTemperatureExport, analysisLongTemperatureExport } from '@/api/overview/index.js'
import { useSiteStore } from '@/stores/site.js'
import { useSysStore } from '@/stores/system.js'
import { useUserStore } from '@/stores/user.js'
import { useSpeedTimetorStore } from '@/stores/speedTime.js'
import { message } from 'ant-design-vue';
import moment from 'moment'
import { storeToRefs } from 'pinia'
import { ref, reactive, onMounted, computed, watch, nextTick, toRef } from 'vue'
import $X from '@/utils/$X.js'
interface SSEConfig {
    userId: string
    hardwareId: string,
    sseType: string
}
interface detailItemType {
    url?: string
    token?: string
    serverUrl?: string
    userId?: number
    hardwareId?: number
    siteId?: number
    sseType?: string
    chartData?: any[]
    chartData2?: any[]
}
const props = defineProps({
    liveData: {
        type: Object as () => detailItemType,
        default: {}
    }
})
let siteConf = useSiteStore()
let sysConf = useSysStore()
let user = useUserStore()
let speedTimeStore = useSpeedTimetorStore()
const { speedTime, fluctuateTime, averageTime, temperatureTime } = storeToRefs(speedTimeStore)
const { userInfo } = storeToRefs(user)
const { siteId } = storeToRefs(sysConf)
const { fanId, hardwareId, hardwareName, hardwareType, monitorType } = storeToRefs(siteConf)
let detailItem = ref<detailItemType>(props.liveData)
let chartDataHistoryOne = toRef(detailItem.value, 'chartData')
let chartDataHistoryTwo = ref([])
let chartData = ref([])
// 温度
let tempChart = ref()
let tempLongChart = ref()
// 超限
let speedChart = ref()
// 稳定性
let fluctuateChart = ref()
// 震荡
let averageChart = ref()
// 频谱
let fftChartOne = ref()
let fftChartTwo = ref()
// 记录fft数据
let fft = ''
const typeNameMap = {
    1: '转速',
    2: '热成像',
    3: '视频',
    4: '音频'
}
const hardwareTypeName = computed(() => {
    console.log(hardwareType.value, 'hardwareType.value')
    return typeNameMap[hardwareType.value]
})
onMounted(async () => {
    console.log(monitorType.value == 4, monitorType.value, 'monitorType.value == 4')
    // 如果不是从设备列表页点进来，已经有各数据
    if (Object.keys(props.liveData).length === 0) {
        getMonitorType()
    }
    if (hardwareType.value == 2) {
        getLongChart()
    }
    if (hardwareType.value == 1 && monitorType.value == 4) {
        console.log('频谱')
        await getFft()
    }
})
const getFft = async () => {
    let time = await getLastFFTActive({ siteId: fanId.value })
    let startTime = moment(new Date(time.data.activeTime).getTime() - 10 * 60 * 1000).format('YYYY-MM-DD HH:mm:ss')
    let endTime = moment(new Date(time.data.activeTime).getTime()).format('YYYY-MM-DD HH:mm:ss')
    const res2 = await getFftChart2({ siteId: fanId.value, startTime, endTime, hardwareId: hardwareId.value })
    chartDataHistoryTwo.value = [...res2.data]
    // 记录fft导出id
    fft = time.data.id
}
watch(() => hardwareId.value, (newHardwareId, oldHardwareId) => {
    console.log(oldHardwareId, newHardwareId, hardwareType.value, Object.keys(props.liveData).length, 'Object.keys(props.liveData).length')
    if (oldHardwareId != newHardwareId) {
        console.log('更改硬件id')
        // 判断传过来得liveData是否为空
        getMonitorType()
        console.log('当作没有传值')
    }
})
const getMonitorType = async () => {
    detailItem.value = {}
    console.log(monitorType.value, 'detailItem.value-----详情')
    if (hardwareType.value == 1) {
        detailItem.value = {
            userId: userInfo.value.id,
            siteId: fanId.value,
            chartData: [],
            chartData2: []
        }
        //转速
        switch (monitorType.value) {
            case 1:
                detailItem.value.sseType = 'speed'
                break
            case 2:
                detailItem.value.sseType = 'speedFluctuate'
                break
            case 3:
                detailItem.value.sseType = 'speedAverage'
                break
            case 4:
                const res = await getFftChart({ siteId: fanId.value })
                detailItem.value.chartData = [...res.data]
                getFft()
                break
        }
        console.log(detailItem.value, 'detailItem.value-----详情')
    } else if (hardwareType.value == 2) {
        getVideoStream({ hardwareId: hardwareId.value }).then((res) => {
            console.log(res)
            if (res.code == 200) {
                detailItem.value.url = $X.decryptRTSP(res.data)
            } else {
                detailItem.value.url = ''
            }

        })
        getLongChart()
        detailItem.value.sseType = 'temperature'
        detailItem.value.userId = userInfo.value.id
        detailItem.value.hardwareId = hardwareId.value
        console.log(hardwareId.value, detailItem.value, '再次点击')
    } else if (hardwareType.value == 3) {
        getVideoStream({ hardwareId: hardwareId.value }).then((res) => {
            console.log(res)
            if (res.code == 200) {
                detailItem.value = { url: $X.decryptRTSP(res.data) }
            }

        })
        await nextTick()

    } else if (hardwareType.value == 4) {
        getAudioStreamLive({ hardwareId: hardwareId.value, siteId: fanId.value }).then(res => {
            console.log(res, '音频列表')
            if (res.code == 200) {
                detailItem.value = res.data
            }

        })
    }

}
const getLongChart = () => {
    getLongTemperatureChart({ hardwareId: hardwareId.value }).then(res => {
        chartData.value = res.data
    })
}
// 下载excel及图表图片
const saveData = () => {
    // 转速中要区分是哪个图表组件
    if (hardwareType.value == 1) {
        switch (monitorType.value) {
            case 1:
                speedChart.value.downloadChartImage()
                speedExport()
                break
            case 2:
                fluctuateChart.value.downloadChartImage()
                fluctuatExport()
                break
            case 3:
                averageChart.value.downloadChartImage()
                averageExport()
                break
            case 4:
                fftChartOne.value.downloadChartImage()
                fftChartTwo.value.downloadChartImage()
                fftExport()
                break
        }
    } else if (hardwareType.value == 2) {
        // 热成像
        tempChart.value.downloadChartImage()
        temperatureExport()

    }
}
const longTempChart = () => {
    tempLongChart.value.downloadChartImage()
    temperatureLongExport()
}
// 频谱导出,endTime
const fftExport = async () => {
    let res = await analysisfftExport({ id: fft })
    exportExcel(res, '频谱')
}
// 超限导出
const speedExport = async () => {
    let res = await analysisSpeedExport({ siteId: fanId.value, endTime: speedTime.value })
    exportExcel(res, '超限')
}
// 稳定性导出
const fluctuatExport = async () => {
    let res = await analysisFluctuteExport({ siteId: fanId.value, endTime: fluctuateTime.value })
    exportExcel(res, '稳定性')
}
// 震荡导出
const averageExport = async () => {
    let res = await analysisAverageExport({ siteId: fanId.value, endTime: averageTime.value })
    exportExcel(res, '震荡')
}
// 温度导出
const temperatureExport = async () => {
    let res = await analysisTemperatureExport({ siteId: fanId.value, endTime: temperatureTime.value, hardwareId: hardwareId.value })
    exportExcel(res, '温度')
}
// 长时温度导出
const temperatureLongExport = async () => {
    let res = await analysisLongTemperatureExport({ hardwareId: hardwareId.value })
    exportExcel(res, '温度')
}
const exportExcel = (res, fileName) => {
    let blob = new Blob([res], { type: 'application/msword;charset=UTF-8' })
    let objectUrl = URL.createObjectURL(blob)
    let link = document.createElement('a')
    link.href = objectUrl
    link.download = `${fileName}.xlsx`
    document.body.appendChild(link)
    link.click()
    window.URL.revokeObjectURL(link.href)
    message.success({ content: '下载成功', key: 'orgKey', duration: 2 })
}
</script>
<style lang='scss' scoped>
.func-box {
    width: calc(60% - 16px);
    padding: 24px 15px;
    box-sizing: border-box;
    margin-right: 20px;
    background: #001525;
    box-shadow: inset 0px 0px 16px 0px rgba(0, 69, 90, 0.24);

    .top-titel-box {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .title {
            margin: 20px 0;
            font-weight: 500;
            font-size: 16px;
            color: #00D1FF;
        }

        .save-btn {
            background: rgba(80, 172, 248, 0.3);
            border-radius: 2px 2px 2px 2px;
            border: 1px solid rgba(80, 172, 248, 0.96);
            font-weight: 400;
            font-size: 12px;
            color: #FFFFFF;
        }
    }

    .hardware-box {
        width: 100%;

    }
}

.line-video-box {
    display: flex;
    flex: 1 1 calc(50% - 10px)
}

.video {
    width: calc(50% - 20px);
    margin-right: 20px;
    height: 200px;
}

.line {
    width: 50%;
    height: 200px;


    .title {
        font-weight: 500;
        font-size: 16px;
        color: #00D1FF;
    }
}

.line-chart {
    width: 100%;
    height: 300px;
    margin-top: 20px;
}
</style>