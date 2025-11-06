<template>
    <div class="alarm-box">
        <a-spin :spinning="listLoading">
            <a-tabs v-model:activeKey="state.activeKey" @change="changeTab">
                <a-tab-pane key="realTime" tab="实时告警">
                    <div class="table-box">
                        <Table @getPageInfo="getRealTimeTimePageInfo" :columns="columns" :list="realTimeList"
                            :total="state.realTimeTotal">
                            <template #action="{ record }">
                                <a-button :class="['editBtn', record.isCheck == 0 ? 'redBtn' : 'blueBtn']"
                                    @click="showDetial(record)">查看</a-button>
                            </template>
                        </Table>
                    </div>
                </a-tab-pane>
                <a-tab-pane key="historyTime" tab="历史告警">
                    <div class="table-box">
                        <Table @getPageInfo="getHistoryPageInfo" :columns="columns" :list="historyList"
                            :total="state.historyTotal">
                            <template #action="{ record }">
                                <a-button :class="['editBtn', record.isCheck == 0 ? 'redBtn' : 'blueBtn']"
                                    @click="showDetial(record)">查看</a-button>
                            </template>
                        </Table>
                    </div>
                </a-tab-pane>
            </a-tabs>
        </a-spin>
    </div>
</template>

<script setup lang='ts'>
import { getAlarmList, checkStatus } from '@/api/overview/index.js'
import Table from '@/views/commonComponents/table.vue'
import { columns } from '../columns'
import { ref, watch, onMounted } from 'vue'
import { useSiteStore } from '@/stores/site.js'
import { storeToRefs } from 'pinia'
import { useSysStore } from '@/stores/system.js'
import { useRouter } from 'vue-router';
const router = useRouter()
let siteConf = useSiteStore()
let sysConf = useSysStore()
const { siteId } = storeToRefs(sysConf)
const { fanId, hardwareId } = storeToRefs(siteConf)
const realTimeList = ref([])
const historyList = ref([])
let listLoading = ref(false)
onMounted(() => {
    getRealTimeTimePageInfo({ pageNum: 1, pageSize: 10 })
})
const props = defineProps({
    fanName: {
        type: String,
        default: ''
    },
    fanDefaultFlag: {
        type: Boolean,
        default: false
    },
})
let state = ref({
    activeKey: 'realTime',
    historyTotal: 0,
    realTimeTotal: 0
})
// 告警点击查看
const showDetial = (record) => {
    if (record.isCheck == 0) {
        // 实时告警，调接口改状态，并跳转到告警中心
        checkStatus({ id: record.id }).then(res => {
            if (res.code == 200) {

            }
        })
    }
    console.log(record, 'record----------')
    // 跳转到告警中心
    router.push({
        name: 'alarmCenter',
        query: { id: record.id, hardwareId: record.hardwareId, hardwareType: record.hardwareType, monitorType: record.monitorType, siteId: record.siteId }
    })
}
// 实时告警
const getRealTimeTimePageInfo = ({ pageNum, pageSize }) => {
    listLoading.value = true
    getAlarmList({ isCheck: 0, pageNo: pageNum, pageSize: pageSize, siteId: fanId.value, orgId: siteId.value, hardwareId: hardwareId.value }).then(res => {
        realTimeList.value = res.data.records
        state.value.realTimeTotal = res.data.total
        listLoading.value = false
    })
}
// 历史告警
const getHistoryPageInfo = ({ pageNum, pageSize }) => {
    listLoading.value = true
    getAlarmList({ pageNo: pageNum, pageSize: pageSize, siteId: fanId.value, orgId: siteId.value, hardwareId: hardwareId.value }).then(res => {
        historyList.value = res.data.records
        state.value.historyTotal = res.data.total
        listLoading.value = false
    })
}
const changeTab = () => {
    if (state.value.activeKey == 'realTime') {
        getRealTimeTimePageInfo({ pageNum: 1, pageSize: 10 })
    } else {
        getHistoryPageInfo({ pageNum: 1, pageSize: 10 })
    }
}
watch(() => fanId.value, (oldFanId, newFanId) => {
    if (oldFanId != newFanId) {
        state.value.activeKey = 'realTime'
        getRealTimeTimePageInfo({ pageNum: 1, pageSize: 10 })
    }
})
watch(() => hardwareId.value, (oldHardwareIdId, newHardwareIdId) => {
    if (oldHardwareIdId != newHardwareIdId) {
        state.value.activeKey = 'realTime'
        getRealTimeTimePageInfo({ pageNum: 1, pageSize: 10 })
    }
})
</script>
<style lang='scss' scoped>
.alarm-box {
    width: 40%;
    background: #001525;
    box-shadow: inset 0px 0px 16px 0px rgba(0, 69, 90, 0.24);

    ::v-deep .ant-tabs-nav-wrap {
        height: 48px;
        background: linear-gradient(273deg, rgba(0, 38, 54, 0.18) 70%, rgba(24, 68, 84, 0.64) 100%);
    }

    ::v-deep :where(.css-dev-only-do-not-override-1dgrbo1).ant-tabs .ant-tabs-tab-btn {
        color: #9CC0CA;
        font-weight: 400;
        font-size: 16px;
    }

    ::v-deep .ant-tabs-nav-list .ant-tabs-tab {
        width: 100% !important
    }

    .table-box {
        margin: 0 24px 24px 24px;

        .redBtn {
            background: #4E2B2B !important;
            border: 1px solid #D14141 !important;
        }

        .blueBtn {
            background: #013C51 !important;
            border: 1px solid #00A5CA !important;
        }
    }
}

::v-deep .ant-tabs-nav-list .ant-tabs-tab {
    border-width: 0 !important
}
</style>