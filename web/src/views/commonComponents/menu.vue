<template>
    <div class="sider">
        <div class="site-box">
            <div class="menu-title">机位号</div>
            <!-- fanDefaultFlag区分是否需要默认值 -->
            <siteMenuList :items="siteList" :defaultSelectedKey="menuState.siteSelectKeys" @select="changeSiteHandle"
                :className="'siteClass'" />
        </div>
        <div class="func-box">
            <div class="menu-title">功能总览</div>
            <!-- hardwareDefaultFlag区分是否需要默认值 -->
            <siteMenuList :items="funcList" :defaultOpenAll="true" :defaultSelectedKey="menuState.funcSelectKeys"
                @select="changeFuncHandle" :className="'minHeight'" :isSiteFlag="false" />
        </div>
    </div>
</template>
<script setup>
import { storeToRefs } from 'pinia'
import { useSysStore } from '@/stores/system.js'
import { useSiteStore } from '@/stores/site.js'
import { getSiteListByOrgId, getHardwareTree, getHardwareTreeTemp } from '@/api/siteMenu/index.js'
import { onBeforeUnmount, onMounted } from 'vue'
import siteMenuList from './siteMenuList.vue'

let menuState = ref({
    siteSelectKeys: 0,
    funcSelectKeys: 0
})
let sysConf = useSysStore()
let sitConf = useSiteStore()
// 获取当前风场
const { siteId } = storeToRefs(sysConf)
let { fanId, hardwareList } = storeToRefs(sitConf)
onMounted(() => {
    sitConf.setFanId('')
    sitConf.setHardwareId('')
    sitConf.setHardwareName('')
    sitConf.setHardwareType('')
    getAllSite()
    if (props.isMontorFlag) {
        // 是否是智能监控，智能监控默认把功能总览加载出，不按机位号
        getAllHardware()
    }
})
const props = defineProps({
    isMontorFlag: { //是否是只能监控下的筛选项
        type: Boolean,
        default: false
    },
    defaultKey: {
        type: Boolean, // 机位号是否设置默认值
        default: false
    },
    hardwareDefaultKey: {
        type: Boolean, // 硬件是否设置默认值
        default: false
    }
})
const emit = defineEmits(['changeView'])
let siteList = ref([])
let funcList = ref([])
watch(() => props.defaultSelectedKey, (oldSelectKey, newSelectKey) => {
    if (oldSelectKey != newSelectKey) {

    }
})
// 当风机号发射改变更新funcList
watch(() => fanId.value, (newFanId, oldFanId) => {
    if (newFanId && newFanId != oldFanId) {
        sitConf.setHardwareId('')
        sitConf.setHardwareType('')
        sitConf.setHardwareName('')
        sitConf.setMonitorType('')
        // 如果没有存过，则请求接口获取该风机下的所有硬件列表
        getHardwareTree({ siteId: newFanId }).then(res => {
            funcList.value = res.data
            sitConf.setHardwareList(res.data)
        })
    }
})
// 获取所有机位
const getAllSite = () => {
    getSiteListByOrgId({ orgId: siteId.value }).then(res => {
        // 当要默认值时
        if (props.defaultKey) {
            menuState.value.siteSelectKeys = res.data[0].id
            changeSiteHandle({ key: menuState.value.siteSelectKeys })
        }
        siteList.value = res.data.map((item) => ({ label: item.siteName, id: item.id }))
        sitConf.setFunList(siteList.value)

    })
}
//获取所有设备硬件列表，当智能监控不设置默认机位号时，也要获取到所有设备硬件
const getAllHardware = () => {
    getHardwareTreeTemp().then(res => {
        funcList.value = res.data
        sitConf.setHardwareList(res.data)
    })
}
// 更改风机，按机位号查询硬件设备
const changeSiteHandle = ({ key }) => {
    // 设置当前风机
    sitConf.setFanId(key)
    // 如果没有存过，则请求接口获取该风机下的所有硬件列表
    getHardwareTree({ siteId: key }).then(res => {

        sitConf.setHardwareList(res.data)
        funcList.value = res.data
        console.log('切换机位号，重新加载功能总览', res.data, sitConf.getHardwareList())
        // 当前风机没有硬件，把硬件id重置位0
        if (res.data.length == 0) {
            sitConf.setHardwareId('')
        }
        // 判断是否默认初始化硬件
        if (props.hardwareDefaultKey) {
            menuState.value.funcSelectKeys = res.data[0].list ? res.data[0].list[0].id : res.data[0].id
            const hardwareType = res.data[0].list ? res.data[0].list[0].hardwareType : ''
            // 设置当前硬件
            sitConf.setHardwareId(menuState.value.funcSelectKeys)
            // 设置当前硬件类型
            sitConf.setHardwareType(hardwareType)
        } else {
            //清空硬件id和类型
            // 设置当前硬件
            //sitConf.setHardwareId('')
            // 设置当前硬件类型
            // sitConf.setHardwareType('')
        }
        if (props.isMontorFlag) {
            emit('changeView')
        }
    })

}
// 更改设备
const changeFuncHandle = ({ key }) => {
    sitConf.setHardwareId(key)
    const hardwareList = sitConf.getHardwareList()
    console.log(hardwareList, 'hardwareList---')
    const item = hardwareList
        .flatMap(category => category.list)
        .find(item => item.id === key)
    const hardwareType = item ? item.hardwareType : null
    const hardwareName = item ? item.label : null
    const hardwareMonitorType = item ? item.monitorType : null
    sitConf.setHardwareType(hardwareType)
    sitConf.setHardwareName(hardwareName)
    sitConf.setMonitorType(hardwareMonitorType)
    if (props.isMontorFlag) {
        emit('changeView')
        console.log(hardwareList, key, hardwareType, hardwareName, hardwareMonitorType, '点击某个硬件00000000')
    }
}
onBeforeUnmount(() => {
    clearValue()
})
const clearValue = () => {
    sitConf.setFanId('')
    sitConf.setHardwareId('')
    sitConf.setHardwareType('')
    sitConf.setHardwareName('')
    sitConf.setMonitorType('')
}
</script>
<style lang="scss" scoped>
.sider {
    background: #001525;
    padding: 20px;
    display: flex;
    width: 276px;
    box-sizing: content-box;
    height: 95%;
    box-shadow: 0px 0px 16px 0px rgba(0, 69, 90, 0.24) inset;
}

.site-box {
    width: 100px;
    margin-right: 20px;
    border-right: 1px dashed #142736;
    overflow-y: auto;

    .menu-title {
        width: 100px;
    }
}

.func-box {
    width: 148px;
    overflow-y: auto;

    .menu-title {
        width: 148px;
    }
}

.menu-container {
    margin-top: 36px;
}

.menu-title {
    background: #003B51;
    height: 36px;
    color: #99A0A5;
    font-weight: 500;
    font-size: 14px;
    line-height: 36px;
    text-align: center;
    position: absolute;
    z-index: 100
}

::v-deep :where(.css-dev-only-do-not-override-1dgrbo1).ant-menu-light .ant-menu-item:hover:not(.ant-menu-item-selected):not(.ant-menu-submenu-selected),
:where(.css-dev-only-do-not-override-1dgrbo1).ant-menu-light .ant-menu-submenu-title:hover:not(.ant-menu-item-selected):not(.ant-menu-submenu-selected) {
    color: #fff !important
}
:deep(.ant-menu-light .ant-menu-submenu-title:hover:not(.ant-menu-item-selected):not(.ant-menu-submenu-selected)){
    color: #fff !important
}
:deep(:where(.css-1dgrbol).ant-menu-light .ant-menu-item:hover:not(.ant-menu-item-selected):not(.ant-menu-submenu-selected)){
    color: #fff !important
}
</style>