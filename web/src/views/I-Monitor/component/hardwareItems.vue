<template>
    <div class="func-all">
        <div class="hardware-box">
            <div class="hardware-items" v-if="hardwareType == 1 && sseUrl.length">
                <!-- 实时动态折线图数据 -->
                <div v-for="(item, index) in sseUrl" :key="index" class="webrtc-item">
                    <div>
                        <RealTimeSpeedLineChart :sseUrl="item" :maxDataPoints="100" v-if="monitorType == 1"
                            class="line-chart" />
                        <RealTimeSpeedFluctuateLineChart :sseUrl="item" v-if="monitorType == 2" :smallFlag="true"
                            class="line-chart" />
                        <RealTimeSpeedAverageLineChart :sseUrl="item" v-if="monitorType == 3" class="line-chart" />
                        <HistoryLineChart v-if="monitorType == 4 && item.chartData" :data="item.chartData" :height="120"
                            class="line-chart" :key="item.siteName" :titleFlag="false" />
                        <div class="hardware-large-name">{{ item.siteName }}</div>
                    </div>
                </div>
            </div>
            <div class="hardware-items" v-if="hardwareType == 3 && videoList.length">
                <div v-for="(item, index) in videoList" :key="index" class="webrtc-item">
                    <div>
                        <WebRTCPlayer :rtsp-url="item.url" />
                        <div class="hardware-name">{{ item.siteName }}</div>
                    </div>
                </div>
            </div>
            <div class="hardware-items" v-if="hardwareType == 2 && temperatureList.length">
                <div v-for="(item, index) in temperatureList" :key="index" class="webrtc-item">
                    <div>
                        <WebRTCPlayer :rtsp-url="item.url" />
                        <div class="hardware-name">{{ item.siteName }}</div>
                    </div>
                </div>
            </div>
            <div class="hardware-items" v-if="hardwareType == 4 && audioList.length">
                <div v-for="(item, index) in audioList" :key="index" class="webrtc-item">
                    <div>
                        <LiveAudioVisualizer :roomUrl="item.serverUrl" :token="item.token" />
                        <div class="hardware-name">{{ item.siteName }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang='ts'>
import { getFftChart } from '@/api/dataCenter/index.js'
import { getVideoStreamByType, getLiveByType } from '@/api/overview/index.js'
import WebRTCPlayer from '@/views/commonComponents/WebRTC.vue'
import LiveAudioVisualizer from '@/views/commonComponents/LiveAudioVisualizer.vue'
import RealTimeSpeedLineChart from '@/views/commonComponents/RealTimeSpeedLineChart.vue'
import HistoryLineChart from '@/views/commonComponents/historyLineChart.vue'
import RealTimeSpeedFluctuateLineChart from '@/views/commonComponents/RealTimeSpeedFluctuateLineChart.vue'
import RealTimeSpeedAverageLineChart from '@/views/commonComponents/RealTimeSpeedAverageLineChart.vue'
import { useSiteStore } from '@/stores/site.js'
import { useSysStore } from '@/stores/system.js'
import { storeToRefs } from 'pinia'
import { ref, reactive, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { chart } from 'highcharts'
import $X from '@/utils/$X.js'
interface hardwareType {
    id: number
    hardwareType: number
    label: string
    monitorLocation: string
    versionId: number
}
interface liveType {
    siteName: string
    url?: string
    token?: string
    serverUrl?: string
}
let user = useUserStore()
const emit = defineEmits(['selectHaredware'])
let siteConf = useSiteStore()
let sysConf = useSysStore()
const { siteId } = storeToRefs(sysConf)
const { userInfo } = storeToRefs(user)
const { fanId, hardwareId, hardwareType, monitorType } = storeToRefs(siteConf)
// 所有热成像设备
let temperatureList = ref<liveType[]>([])
// 所有视频设备
let videoList = ref<liveType[]>([])
// 所有音频设备
let audioList = ref<liveType[]>([])
// 所有转速设备
let liveLineList = ref<liveType[]>([])
// 实时折线图
let sseUrl = ref([])
onMounted(() => {
    getMonitorType()
})
watch(() => hardwareId.value, (newHardwareId, oldHardwareId) => {
    if (oldHardwareId != newHardwareId) {
        console.log('更改硬件id')

        // 判断传过来得liveData是否为空
        getMonitorType()
        console.log('当作没有传值')
    }
})
const getMonitorType = () => {
    sseUrl.value = []
    console.log(userInfo.value, 'userInfo.value')
    const params = {
        orgId: siteId.value,
        monitorType: monitorType.value
    }
    const siteList = siteConf.getFunList()
    if (hardwareType.value == 1) {
        siteList.map(site => {
            console.log(site, 'site---风机列表')
            let data = {
                userId: userInfo.value.id,
                siteId: site.id,
                sseType: '',
                siteName: site.label,
                chartData: []
            }
            //转速
            switch (monitorType.value) {
                case 1:
                    data.sseType = 'speed'
                    break;
                case 2:
                    data.sseType = 'speedFluctuate'
                    break;
                case 3:
                    data.sseType = 'speedAverage'
                    break;
                case 4:
                    getFftChart({ siteId: site.id }).then(res => {
                        data.chartData = res.data
                    })

                    break;
            }
            setTimeout(() => {
                sseUrl.value.push(data)
            }, 200)
        })
        console.log(sseUrl.value, ' sseUrl.value')
    } else if (hardwareType.value == 2) {
        getVideoStreamByType(params).then(res => {
            temperatureList.value = res.data.map(item => ({ ...item, url: $X.decryptRTSP(item.url) }))
        })
    } else if (hardwareType.value == 3) {
        getVideoStreamByType(params).then(res => {
            videoList.value = res.data.map(item => ({ ...item, url: $X.decryptRTSP(item.url) }))
        })
    } else if (hardwareType.value == 4) {
        getLiveByType(params).then(res => {
            audioList.value = res.data
        })
    }
}
// 点击某个设备
const gohardwareDetail = (item) => {
    console.log(item, '设备某一项值')
    // 设置当前硬件id
    siteConf.setHardwareId(item.hardwareId)
    siteConf.setFanId(item.siteId)
    // 跳转到某风机下的硬件设备详情
    emit('selectHaredware', item)
}
</script>
<style lang='scss' scoped>
.func-all {
    padding: 20px;
    background: #001525;
    box-shadow: inset 0px 0px 16px 0px rgba(0, 69, 90, 0.24);
    width: 100%;
    box-sizing: border-box;
}

.hardware-box {
    height: 200px;
    padding: 16px 0;
    border-top: 1px solid #2B4450;

    &:first-child {
        border-top: none;
        padding-top: 0
    }

    .hardware-title {
        font-size: 16px;
        color: #00D1FF;
    }

    .hardware-items {
        display: flex;
        padding: 10px 20px;
        flex-wrap: wrap;
    }

    .webrtc-item {
        width: calc(25% - 20px);
        height: 120px;
        margin-right: 20px;
        margin-bottom: 50px;

        &:last-child {
            margin-right: 0
        }
    }

    .hardware-name {
        font-size: 14px;
        color: #B8BDC1;
        text-align: center;
        margin-top: 10px;
    }

}

.hardware-large-name {
    width: 380px;
    font-size: 14px;
    color: #B8BDC1;
    text-align: center;
    margin-top: 10px;
}

.line-chart {
    padding: 0;
    width: 380px;
    height: 130px;
    margin-right: 20px;

    &:last-child {
        margin-right: 0
    }
}

::v-deep .ant-spin-nested-loading {
    width: 97%
}
</style>