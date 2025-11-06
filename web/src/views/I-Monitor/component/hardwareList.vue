<template>
    <div class="func-all">
        <div class="hardware-box">
            <div class="hardware-title">转速监控</div>
            <div class="hardware-items">
                <!-- 实时动态折线图数据 -->
                <div v-for="(item, index) in sseUrl" :key="index" class="line-items">
                    <div @click="gohardwareDetail(speedList[index], item)">
                        <div style="width:100%;height:120px;">
                            <RealTimeSpeedLineChart :sseUrl="item" :legend="false"
                                v-if="speedList[index].monitorType == 1" class="line-chart" />
                            <RealTimeSpeedFluctuateLineChart :sseUrl="item" :legend="false" :smallFlag="true"
                                v-if="speedList[index].monitorType == 2" class="line-chart" />
                            <RealTimeSpeedAverageLineChart :sseUrl="item" :legend="false"
                                v-if="speedList[index].monitorType == 3" class="line-chart" />
                            <HistoryLineChart :data="item.chartData" :titleFlag="false" :height="120"
                                :emptyShowFlag="false" v-if="speedList[index].monitorType == 4"
                                class="line-chart min-chart" />
                        </div>

                        <div class="hardware-name">{{ speedList[index].label }}</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="hardware-box">
            <div class="hardware-title">视频</div>
            <div class="hardware-items">
                <div v-for="(item, index) in videoRtspUrl" :key="index" class="webrtc-item margin-r-100">
                    <div v-if="item" @click="gohardwareDetail(videoList[index], item)">
                        <WebRTCPlayer :rtsp-url="item.url" />
                        <div class="hardware-name">{{ videoList[index].label }}</div>
                    </div>

                    <div style="width:200px;height:120px;background:#eee" v-else></div>

                </div>

            </div>
        </div>
        <div class="hardware-box">
            <div class="hardware-title">热成像</div>
            <div class="hardware-items">
                <div v-for="(item, index) in temperatureRtspUrl" :key="index" class="webrtc-item margin-r-100">
                    <div v-if="item" @click="gohardwareDetail(temperatureList[index], item)">
                        <WebRTCPlayer :rtsp-url="item.url" />
                        <div class="hardware-name">{{ temperatureList[index].label }}</div>
                    </div>
                    <div style="width:200px;height:120px;background:#eee" v-else></div>
                </div>
            </div>
        </div>
        <div class="hardware-box">
            <div class="hardware-title">音频</div>
            <div class="hardware-items">
                <div v-for="(item, index) in audioRoomToken" :key="index" class="webrtc-item webrtc-item-max">
                    <div v-if="item" @click="gohardwareDetail(audioList[index], item)">
                        <LiveAudioVisualizer :roomUrl="item.serverUrl" :token="item.token" />
                        <div class="hardware-name">{{ audioList[index].label }}</div>
                    </div>
                    <div style="width:200px;height:120px;background:#eee" v-else></div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang='ts'>
import { getVideoStream, getAudioStreamLive, getFftChart } from '@/api/dataCenter/index.js'
import WebRTCPlayer from '@/views/commonComponents/WebRTC.vue'
import LiveAudioVisualizer from '@/views/commonComponents/LiveAudioVisualizer.vue'
import HistoryLineChart from '@/views/commonComponents/historyLineChart.vue'
import RealTimeSpeedLineChart from '@/views/commonComponents/RealTimeSpeedLineChart.vue'
import RealTimeSpeedFluctuateLineChart from '@/views/commonComponents/RealTimeSpeedFluctuateLineChart.vue'
import RealTimeSpeedAverageLineChart from '@/views/commonComponents/RealTimeSpeedAverageLineChart.vue'
import { useSiteStore } from '@/stores/site.js'
import { useSysStore } from '@/stores/system.js'
import { useUserStore } from '@/stores/user.js'
import { storeToRefs } from 'pinia'
import { ref, reactive, onMounted, watch, onBeforeUnmount } from 'vue'
import eventHub from '@/utils/eventBus'
import $X from '@/utils/$X.js'
interface hardwareType {
    id: number
    hardwareType: number
    label: string
    monitorLocation: string
    versionId: number
    monitorType: number
}
interface audioType {
    serverUrl: string,
    token: string
}
interface sseUrlType {
    userId: number
    siteId: number
    sseType: string,
    chartData: any[]
}
const props = defineProps({
    hardwareListFlag: {
        type: Boolean,
        default: false
    }
})
const emit = defineEmits(['selectHaredware'])
let siteConf = useSiteStore()
let sysConf = useSysStore()
let user = useUserStore()
const { userInfo } = storeToRefs(user)
const { siteId } = storeToRefs(sysConf)
const { fanId, hardwareId, hardwareList } = storeToRefs(siteConf)
// 所有转速设备
let speedList = ref<hardwareType[]>([])
// 所有热成像设备
let temperatureList = ref<hardwareType[]>([])
// 所有视频设备
let videoList = ref<hardwareType[]>([])
// 所有音频设备
let audioList = ref<hardwareType[]>([])
// 热成像设备实时流
let temperatureRtspUrl = ref([])
// 视频设备实时流
let videoRtspUrl = ref([])
let audioRoomToken = ref<audioType[]>([])
// 实时折线图
let sseUrl = ref<sseUrlType[]>([])
// 频谱折线图数据
let chartData = ref([])
onMounted(() => {
    eventHub.$on('hardwareList-updated', (data) => {
        if (props.hardwareListFlag) {
            getHardwareType()
            initStream()
        }
    });
})
onBeforeUnmount(() => {
    eventHub.$off('hardwareList-updated')

})
const initStream = async () => {
    console.log(speedList.value, 'speedList.value')
    speedList.value.map(item => {
        console.log(item.monitorType, 'item.monitorType')
        let data: sseUrlType = { userId: userInfo.value.id, siteId: fanId.value, sseType: '', chartData: [] }
        switch (item.monitorType) {
            case 1:
                data.sseType = 'speed'
                break
            case 2:
                data.sseType = 'speedFluctuate'
                break
            case 3:
                data.sseType = 'speedAverage'
                break
            case 4:
                getFftChart({ siteId: fanId.value }).then(res => {
                    data.chartData = res.data
                })
                break
        }
        sseUrl.value.push(data)
    })
    console.log(sseUrl, 'sseUrl==========')
    temperatureList.value.map((item, index) => {

        goTemperatureDetail(item, index)
    })
    videoList.value.map((item, index) => {
        goVideoDetail(item, index)
    })
    audioList.value.map((item, index) => {
        console.log('音频列表')
        getAudioStreamLive({ hardwareId: item.id, siteId: siteId.value }).then(res => {
            console.log(res, '音频列表')
            if (res.code == 200) {
                audioRoomToken.value = [...audioRoomToken.value]
                audioRoomToken.value[index] = res.data
            } else {

            }

        })
    })
    console.log(temperatureRtspUrl.value, videoRtspUrl.value, '实时流地址')
}
const goTemperatureDetail = async (item, index) => {
    // 热成像实时流
    let res = await getVideoStream({ hardwareId: item.id })
    if (res.code == 200) {
        temperatureRtspUrl.value = [...temperatureRtspUrl.value]
        temperatureRtspUrl.value[index] = { url: $X.decryptRTSP(res.data), sseType: 'temperature', userId: 1, hardwareId: item.id }
    } else {
        temperatureRtspUrl.value = [...temperatureRtspUrl.value]
        temperatureRtspUrl.value[index] = { url: '' }
    }

}
const goVideoDetail = async (item, index) => {
    // 视频实时流
    let res = await getVideoStream({ hardwareId: item.id })
    console.log(res)
    if (res.code == 200) {
        videoRtspUrl.value = [...videoRtspUrl.value]
        videoRtspUrl.value[index] = { url: $X.decryptRTSP(res.data) }
    } else {
        videoRtspUrl.value = [...videoRtspUrl.value]
        videoRtspUrl.value[index] = { url: '' }
    }



}
const getHardwareType = () => {
    const hardwareList = siteConf.getHardwareList()
    console.log(hardwareList, 'hardwareList.value[fanId.value]')
    // // 热成像所有硬件设备
    // temperatureList.value = hardwareList.filter(item => {
    //     item.list.find(func => func.hardwareType == 2)
    // }
    // )
    // 遍历数据
    hardwareList.forEach(item => {
        item.list.forEach(subItem => {
            if (subItem.hardwareType === 1) {
                speedList.value.push(subItem)
            } else if (subItem.hardwareType === 2) {
                temperatureList.value.push(subItem);
            } else if (subItem.hardwareType === 3) {
                videoList.value.push(subItem);
            } else if (subItem.hardwareType === 4) {
                audioList.value.push(subItem);
            }
        });
    });
    // // 视频所有硬件设备
    // videoList.value = hardwareList.filter(item => item.hardwareType == 3)
    // // 音频所有硬件设备
    // audioList.value = hardwareList.filter(item => item.hardwareType == 4)
    console.log(speedList.value, temperatureList.value, videoList.value, audioList.value, '各硬件设备分类')

}
// 点击某个设备
const gohardwareDetail = (list, item) => {
    console.log(list, '设备某一项值')
    // 设置当前硬件id
    siteConf.setHardwareId(list.id)
    // 当前硬件类型
    siteConf.setHardwareType(list.hardwareType)
    // 当前硬件名称
    siteConf.setHardwareName(list.label)
    // 当前硬件标识
    siteConf.setMonitorType(list.monitorType)
    // 跳转到某风机下的硬件设备详情
    console.log(list, item, '从所有硬件中跳转到某一硬件')
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
        justify-content: evenly;
    }

    .line-items {

        width: 400px;
        height: 160px;
    }

    .webrtc-item {
        width: 200px;
        height: 120px;
        margin-right: 20px;

        &:last-child {
            margin-right: 0
        }
    }

    .margin-r-100 {
        margin-right: 260px;

        &:last-child {
            margin-right: 0
        }
    }

    .webrtc-item-max {
        width: 400px
    }

    .hardware-name {
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
    }

    .min-chart {
        width: 300px;
        margin-right: 0px;
    }
}
</style>