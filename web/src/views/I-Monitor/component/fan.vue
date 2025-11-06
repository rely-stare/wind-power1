<template>
    <div class="func-box">
        <div class="top-titel-box">
            风机组 ( {{ siteList.length }} )
        </div>
        <div class="site-box">
            <div class="fan-box" v-for="(item, index) in siteList" :key="index" @click="goFanHardware(item)">
                <div class="top-box">
                    <div class="name-box">
                        <span class="name-title" :title="item.siteName"> {{ item.siteName }}</span>
                    </div>
                    <div class="status-box">
                        <span :class="['icon', status.speedStatus[item.speedStatus]]"></span>
                        <span :class="['icon', status.temperatureStatus[item.temperatureStatus]]"></span>
                        <span :class="['icon', status.videoStatus[item.videoStatus]]"></span>
                        <span :class="['icon', status.audioStatus[item.audioStatus]]"></span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang='ts'>
import { getAllSiteStatus } from '@/api/overview/index.js'
import { getHardwareTree } from '@/api/siteMenu/index.js'
import { useSiteStore } from '@/stores/site.js'
import { useSysStore } from '@/stores/system.js'
import { storeToRefs } from 'pinia'
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
interface statusType {
    audioStatus: number,
    siteId: number,
    siteName: string,
    speedStatus: number,
    temperatureStatus: number,
    videoStatus: number
}
let siteConf = useSiteStore()
let sysConf = useSysStore()
const { siteId } = storeToRefs(sysConf)
const { fanId } = storeToRefs(siteConf)
const status = ref({
    'speedStatus': ['icon3', 'icon3-alarm', 'icon3-red'],
    'temperatureStatus': ['icon2', 'icon2-alarm', 'icon2-red'],
    'videoStatus': ['icon1', 'icon1-alarm', 'icon1-red'],
    'audioStatus': ['icon4', 'icon4-alarm', 'icon4-red']
})
let timer = ref()
// const props = defineProps({
//     fanName: {
//         type: String,
//         default: ''
//     },
//     fanDefaultFlag: {
//         type: Boolean,
//         default: false
//     },
// })
const emit = defineEmits(['changeComp'])
onMounted(() => {
    getAllFanList()
    timer.value = setInterval(() => {
        getAllFanList()
    }, 6000)
})
onBeforeUnmount(() => {
    clearInterval(timer.value)
})
const siteList = ref<statusType[]>([])
// 获取所有风机
const getAllFanList = () => {
    getAllSiteStatus({ orgId: siteId.value, siteId: fanId.value }).then(res => {
        siteList.value = res.data
    })
}
// 点击某风机，跳转到该风机下的所有硬件实时页面
const goFanHardware = (item) => {
    siteConf.setFanId(item.siteId)
    console.log('点风机图标')
    emit('changeComp', item.siteId)
}
</script>
<style lang='scss' scoped>
.func-box {
    width: calc(60% - 16px);
    margin-right: 20px;
    background: #001525;
    box-shadow: inset 0px 0px 16px 0px rgba(0, 69, 90, 0.24);

    .top-titel-box {
        background: url("@/assets/newImg/overview_top_bg.png") top left no-repeat;
        font-weight: 500;
        font-size: 16px;
        color: #FFFFFF;
        padding: 13px 0 13px 15px;
        width: 100%;
        height: 58px;
        box-sizing: border-box;
    }

    .site-box {
        display: flex;
        justify-content: baseline;
        padding: 24px;
        width: 100%;
        flex-wrap: wrap;
    }
}

.fan-box {
    width: calc(25% - 20px);
    margin-right: 20px;
    margin-bottom: 20px;
    height: 216px;
    box-shadow: inset 0px 0px 10px 0px rgba(16, 89, 107, 0.84);
    background: #00111E url("@/assets/newImg/fan.png") center bottom no-repeat;
    background-size: 90%;
    cursor: pointer;

    &:last-child {
        margin-right: 0;
    }

    &:hover {
        box-shadow: inset 0px 0px 12px 0px #E29A43;
    }

    .top-box {
        width: 100%;
        height: 32px;
        background: url('@/assets/newImg/fan_top_bg.png') no-repeat;
        display: flex;
        justify-content: space-between;

        .name-box {
            width: 63px;
            height: 32px;
            line-height: 32px;
            font-weight: 500;
            font-size: 14px;
            color: #FFFFFF;
            padding-left: 10px;
            box-sizing: border-box;


            .name-title {
                display: block;
                width: 28px;
                overflow: hidden;
                white-space: nowrap;
            }
        }

        .status-box {
            display: flex;
            height: 32px;
            align-items: center;
        }

        .icon {
            display: block;
            width: 16px;
            height: 18px;
            margin-right: 12px;

            // &:last-child {
            //     margin-right: 0
            // }
        }

        .icon1 {
            background: url('@/assets/newImg/icon1.png') no-repeat;
            background-size: 90%;
        }

        .icon1-alarm {
            background: url('@/assets/newImg/icon1_alarm.png') no-repeat;
            background-size: 90%;
            animation: flash 1s infinite alternate;
        }

        .icon1-red {
            background: url('@/assets/newImg/icon1_red.png') no-repeat;
            background-size: 90%;
        }

        .icon2 {
            background: url('@/assets/newImg/icon2.png') no-repeat;
            background-size: 90%;
        }

        .icon2-alarm {
            background: url('@/assets/newImg/icon2_alarm.png') no-repeat;
            background-size: 90%;
            animation: flash 1s infinite alternate;
        }

        .icon2-red {
            background: url('@/assets/newImg/icon2_red.png') no-repeat;
            background-size: 90%;
        }

        .icon3 {
            background: url('@/assets/newImg/icon3.png') no-repeat;
            background-size: 90%;
        }

        .icon3-alarm {
            background: url('@/assets/newImg/icon3_alarm.png') no-repeat;
            background-size: 90%;
            animation: flash 1s infinite alternate;
        }

        .icon3-red {
            background: url('@/assets/newImg/icon3_red.png') no-repeat;
            background-size: 90%;
        }

        .icon4 {
            background: url('@/assets/newImg/icon4.png') no-repeat;
            background-size: 90%;
        }

        .icon4-alarm {
            background: url('@/assets/newImg/icon4_alarm.png') no-repeat;
            background-size: 90%;
            animation: flash 1s infinite alternate;
        }

        .icon4-red {
            background: url('@/assets/newImg/icon4_red.png') no-repeat;
            background-size: 90%;
        }
    }
}

@keyframes flash {
    0% {
        opacity: 1;
        /* 初始透明度为1（完全可见） */
    }

    40% {
        opacity: 0.4;
    }

    80% {
        opacity: 0.1;
    }

    100% {
        opacity: 0;
        /* 结束透明度为0（完全不可见） */
    }
}
</style>