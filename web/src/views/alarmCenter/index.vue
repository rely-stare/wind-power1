<template>
  <div class="page-box">
    <!-- 左侧菜单区域 -->
    <Menu :defaultKey="defaultAlarm" :hardwareDefaultKey="false">
    </Menu>
    <!-- 右侧内容区域 -->
    <div class="page-search-content">
      <!-- 查询区域 -->
      <div class="search">
        <a-form layout="inline">
          <!-- <a-form-item label="监控信息">
            <a-select v-model:value="state.info" style="width: 120px">
              <a-select-option value="normal">正常</a-select-option>
              <a-select-option value="warning">警告</a-select-option>
              <a-select-option value="error">错误</a-select-option>
            </a-select>
          </a-form-item> -->
          <a-form-item label="触发时间">
            <!-- <a-range-picker v-model:value="dateRange" /> -->
            <a-date-picker v-model:value="state.startTime" placeholder="请选择开始时间" class="form-width"
              valueFormat="YYYY-MM-DD HH:mm:ss" show-time />
            <a-span>-</a-span>
            <a-date-picker v-model:value="state.endTime" placeholder="请选择结束时间" class="form-width"
              valueFormat="YYYY-MM-DD HH:mm:ss" show-time />
          </a-form-item>
          <a-form-item label="查看情况">
            <a-select v-model:value="state.isCheck" class="form-width">
              <a-select-option v-for="(item, index) in statusList" :value="item.value" :key="index">{{ item.label
              }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
        <a-space>
          <a-button ghost class="info" @click="getAlarmInfo({ id: '', pageNum: 1, pageSize: 10 })">搜索</a-button>
          <a-button ghost class="info" @click="reset">重置</a-button>
          <!-- <a-button ghost class="info" @click="exportExcel">导出</a-button> -->
        </a-space>
      </div>
      <div class="content-box">
        <div class="hardcare-box" v-if="hardwareId">
          <div class="title">
            <div class="color-txt">告警详情</div>

            <a-button class="save-btn" @click="saveData" v-if="saveBtnShow">保存数据</a-button>
          </div>

          <HistoryLineChart v-if="showSpeedLineChart" :data="chartData" :height="monitorType == 4 ? 260 : 400"
            ref="speedChart" />
          <HistoryLineChart v-if="showSpeedLineChart && monitorType == 4" :data="chartData2" :height="260"
            ref="fftChart" />
          <!-- 监控-->
          <WebRTCPlayer v-if="showWebRTCPlayer" :rtsp-url="rtspUrl" ref="webRtcRef" />
          <!-- 历史热成像最高温度数据折线图，长时温度统计 -->
          <div v-if="showHistoryLineChart">
            <WebRTCPlayer :rtsp-url="rtspUrl" ref="webRtcRef" class="half-width" />
            <HistoryLineChart :data="chartData" :height="300" :smooth="true" ref="tempChart" />
          </div>
          <!-- 音频 -->
          <Waversurfer v-if="showAudioVisualizer" :fileID="fileID" v-loading="true" style="width:100%;" />
        </div>
        <div class="no-select-hardware" v-else>
          请选择告警数据后查看
        </div>
        <div class="table-box">
          <Table @getPageInfo="getAlarmInfo" :columns="columns" :list="pageInfo.list" :total="pageInfo.total">
            <template #action="{ record }">
              <a-button class="editBtn" @click="showDetail(record)">查看</a-button>
            </template>
          </Table>
        </div>
      </div>


    </div>
  </div>
</template>

<script setup lang="ts">
import { getAlarmList } from '@/api/overview/index.js'
import { getAlarmSpeedData, getAlarmFluctuateData, getAlarmAverageData, getTemperatureData, analysisfftExport, analysisSpeedExport, analysisFluctuateExport, analysisTemperatureExport } from '@/api/alarmCenter/index.js'
import { getVedioList, getAudioList, getVideoStreamHis, getTemperatureChart, getFftChart, getLastFFTActive, getFftChart2 } from '@/api/dataCenter/index.js'
import { useSysStore } from '@/stores/system.js'
import { useSiteStore } from '@/stores/site.js'
import { storeToRefs } from 'pinia'
import Menu from '@/views/commonComponents/menu.vue'
import WebRTCPlayer from '@/views/commonComponents/WebRTC.vue'
import Waversurfer from '@/views/commonComponents/waversurfer.vue'
import HistoryLineChart from '@/views/commonComponents/historyLineChart.vue'
import Table from '@/views/commonComponents/table.vue'
import moment from 'moment'
import { columns } from './columns'
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router';
import { set } from 'nprogress'
import $X from '@/utils/$X.js'
import { useSpeedTimetorStore } from '@/stores/speedTime.js'
import { message } from 'ant-design-vue';
const route = useRoute()
// 系统配置
let sysConf = useSysStore()
let siteCof = useSiteStore()
let speedTimeStore = useSpeedTimetorStore()
let rtspUrl = ref('')
let chartData = ref([])
let chartData2 = ref([])
let fileID = ref()
let speedChart = ref()
let fftChart = ref()
let tempChart = ref()
const { speedTime, fluctuateTime, averageTime, temperatureTime } = storeToRefs(speedTimeStore)
const { siteId } = storeToRefs(sysConf)
const { fanId, hardwareId, hardwareType, monitorType } = storeToRefs(siteCof)
const statusList = ref([{ value: "", label: '全部' }, { value: 0, label: '未查看' }, { value: 1, label: '已查看' }])
const componentName = ref({
  1: 'HistoryLineChart',
  2: 'WebRTCPlayer',
  3: 'Waversurfer',
})
// 分页查询
let state = ref({
  // pageNo: 1,
  // pageSize: 10,
  startTime: "",
  endTime: "",
  hardwareId: '',
  isCheck: ""
})
let pageInfo = ref({
  list: [],
  total: 0,
  pageSize: 10,
  pageNo: 1
})
let routeQuery = ref()
// 计算属性优化
const showWebRTCPlayer = computed(() => {
  return rtspUrl.value && hardwareType.value == 3
})
const showHistoryLineChart = computed(() => {
  return rtspUrl.value && hardwareType.value == 2
})
const showSpeedLineChart = computed(() => {
  return chartData.value.length && hardwareType.value == 1
})
const showAudioVisualizer = computed(() => {
  return hardwareType.value == 4 && fileID.value
})
const saveBtnShow = computed(() => {
  return (showWebRTCPlayer.value || showHistoryLineChart.value || showSpeedLineChart.value || showAudioVisualizer.value)
})
let alarmId = 0

let defaultAlarm = ref(true)
// 可选：监听 rtspUrl 变化
watch(rtspUrl, (newUrl) => {
  if (newUrl) {
    // 处理 URL 变化时的逻辑
    console.log('RTSP URL 已更新:', newUrl)
  }
})
// 监听硬件变化,重置页面状态
watch(() => hardwareId.value, (newItems, oldItems) => {
  if (newItems) {
    state.value.isCheck = ""
    state.value.hardwareId = ''

    rtspUrl.value = ''
    chartData.value = []
    //sseUrl = ''
    //roomUrl = ''
    //roomToken = ''
    console.log(newItems, Number(routeQuery.value.hardwareId), 'newItems')
    if (newItems != Number(routeQuery.value.hardwareId)) {
      pageInfo.value.list = []
      pageInfo.value.total = 0
      pageInfo.value.pageNo = 0
      getAlarmInfo({ pageNum: 1, pageSize: 10 })
    }

  }
})
// 点击查看
const showDetail = (item) => {
  // 记录告警id
  alarmId = item.id
  siteCof.setFanId(Number(item.siteId))
  siteCof.setHardwareId(Number(item.hardwareId))
  siteCof.setHardwareType(item.hardwareType)
  siteCof.setMonitorType(item.monitorType)
  fileID.value = ''
  rtspUrl.value = ''
  let params = {
    hardwareId: item.hardwareId,
    siteId: item.siteId,
    startTime: "",
    endTime: ""
  }
  // 热成像和视频要告警前后10分钟
  let startTime = new Date()
  let endTime = new Date()
  const time = new Date(item.alarmTime)
  startTime = new Date(time.getTime() - 10 * 60 * 1000)
  endTime = new Date(time.getTime() + 10 * 60 * 1000)
  params.startTime = moment(startTime).format('YYYY-MM-DD HH:mm:ss')
  params.endTime = moment(endTime).format('YYYY-MM-DD HH:mm:ss')

  if (item.hardwareType == 1) {

    getSpeedChartData(params, item)

  } else if (item.hardwareType == 2 || item.hardwareType == 3) {
    getVideoStreamHis(params).then((res) => {
      rtspUrl.value = $X.decryptRTSP(res.data)
    })
    if (item.hardwareType == 2) {
      getTemperatureData(params).then(res => {
        chartData.value = res.data
      })
    }
  } else if (item.hardwareType == 4) {
    fileID.value = item.fileID
  }
}
// 自动触发从智能监控跳转过来的那条告警
const autoTriggerView = () => {
  console.log(pageInfo.value.list, 'pageInfo.value.list')
  // 查找 ID 为 1 的记录
  const recordToView = pageInfo.value.list.find(record => record.id == routeQuery.value.id);
  if (recordToView) {
    // 调用查看函数
    showDetail(recordToView);
  } else {
    console.error('未找到 ID 为 1 的记录');
  }
}
const getSpeedChartData = async (params, item) => {
  let startTime = new Date()
  let endTime = new Date()
  const time = new Date(item.alarmTime)
  let res = null
  //转速
  switch (item.monitorType) {
    case 1:
      startTime = new Date(time.getTime() - 5 * 60 * 1000)
      endTime = new Date(time.getTime() + 5 * 60 * 1000)
      params.startTime = moment(startTime).format('YYYY-MM-DD HH:mm:ss')
      params.endTime = moment(endTime).format('YYYY-MM-DD HH:mm:ss')
      res = await getAlarmSpeedData(params)
      break
    case 2:
      startTime = new Date(time.getTime() - 2 * 60 * 1000)
      endTime = new Date(time.getTime() + 2 * 60 * 1000)
      params.startTime = moment(startTime).format('YYYY-MM-DD HH:mm:ss')
      params.endTime = moment(endTime).format('YYYY-MM-DD HH:mm:ss')
      res = await getAlarmFluctuateData(params)
      break
    case 3:

      startTime = new Date(item.alarmTime.getTime() - 10 * 60 * 1000)
      endTime = new Date(item.alarmTime.getTime() + 2 * 60 * 1000)
      params.startTime = moment(startTime).format('YYYY-MM-DD HH:mm:ss')
      params.endTime = moment(endTime).format('YYYY-MM-DD HH:mm:ss')
      res = await getAlarmAverageData(params)
      break
    case 4:
      res = await getFftChart({ siteId: fanId.value, alarmId: routeQuery.value?.id })
      getFft()
      break;
  }
  chartData.value = res.data
  console.log(chartData.value, '')
}
// 获取频谱chart数据
const getFft = async () => {
  let time = await getLastFFTActive({ siteId: fanId.value })
  let startTime = moment(new Date(time.data.activeTime).getTime() - 10 * 60 * 1000).format('YYYY-MM-DD HH:mm:ss')
  let endTime = moment(new Date(time.data.activeTime).getTime()).format('YYYY-MM-DD HH:mm:ss')
  const res2 = await getFftChart2({ siteId: fanId.value, startTime, endTime, hardwareId: hardwareId.value })
  chartData2.value = res2.data
}
// 页面加载完成后执行
const tableHeight = ref()
onMounted(() => {

  getRouteQuery()
})
// 获取路由跳转信息
const getRouteQuery = () => {
  routeQuery.value = route.query
  if (Object.keys(routeQuery.value).length != 0) {
    defaultAlarm.value = false
    siteCof.setFanId(Number(routeQuery.value.siteId))
    siteCof.setHardwareId(Number(routeQuery.value.hardwareId))
    siteCof.setHardwareType(routeQuery.value.hardwareType)
    siteCof.setMonitorType(routeQuery.value.monitorType)
    getAlarmInfo({ id: routeQuery.value.id, pageNum: 1, pageSize: 10 })
  } else {
    getAlarmInfo({ id: "", pageNum: 1, pageSize: 10 })

  }
  console.log(hardwareType.value, monitorType.value, hardwareId.value)
}
// 告警列表
const getAlarmInfo = async ({ id, pageNum, pageSize }) => {
  pageInfo.value.pageNo = pageNum
  const params = {
    isCheck: state.value.isCheck,
    startTime: state.value.startTime,
    endTime: state.value.endTime,
    id: id,
    pageNo: pageNum,
    pageSize: pageSize,
    siteId: fanId.value,
    orgId: siteId.value,
    hardwareId: hardwareId.value

  }
  const res = await getAlarmList(params)
  pageInfo.value.list = res.data.records
  pageInfo.value.total = res.data.total
  if (id) {
    autoTriggerView()
  }
}
// 重置事件
const reset = () => {
  state.value = {
    //pageSize: 10,
    //pageNum: 1,
    startTime: "",
    endTime: "",
    hardwareId: '',
    isCheck: ""
  }
  pageInfo.value.list = []
  pageInfo.value.total = 0
  pageInfo.value.pageNo = 0
  rtspUrl.value = ''
  // getAlarmInfo({ id: "", pageNum: 1, pageSize: 10 })
}
// 下载excel及图表图片
const saveData = () => {
  // 转速中要区分是哪个图表组件
  if (hardwareType.value == 1) {
    switch (monitorType.value) {
      // 震荡没有告警
      case 1:
        speedChart.value.downloadChartImage()
        speedExport()
        break
      case 2:
        speedChart.value.downloadChartImage()
        fluctuatExport()
        break
      case 4:
        speedChart.value.downloadChartImage()
        fftChart.value.downloadChartImage()
        fftExport()
        break
    }
  } else if (hardwareType.value == 2) {
    tempChart.value.downloadChartImage()
    temperatureExport()
  }
}
// 频谱导出,endTime
const fftExport = async () => {
  let res = await analysisfftExport({ alarmId: alarmId })
  exportExcel(res, '频谱')
}
// 超限导出
const speedExport = async () => {
  let res = await analysisSpeedExport({ siteId: fanId.value, alarmId: alarmId })
  exportExcel(res, '超限')
}
// 稳定性导出
const fluctuatExport = async () => {
  let res = await analysisFluctuateExport({ siteId: fanId.value, alarmId: alarmId })
  exportExcel(res, '稳定性')
}
// 温度导出
const temperatureExport = async () => {
  let res = await analysisTemperatureExport({ alarmId: alarmId })
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

<style scoped>
.btn {
  display: flex;
  justify-content: flex-end;
}

.search {
  box-shadow: 0px 0px 16px 0px rgba(0, 69, 90, 0.24) inset;
  padding: 20px;
  border: none
}

.content-box {
  margin-top: 20px;
  display: flex;
  height: 88%;
}

.hardcare-box,
.table-box {
  box-shadow: 0px 0px 16px 0px rgba(0, 69, 90, 0.24) inset;
  padding: 20px;
  gap: 20px;
  width: 45%;
}

.hardcare-box {
  width: calc(55% - 20px);
  margin-right: 20px;
  /* display: flex;
  justify-content: center;
  align-items: center; */
}

.large-width {
  width: 100%;
  margin-right: 0
}

.page-box {
  display: flex;
  height: 100%;
}

.page-search-content {
  margin-left: 20px;
  width: calc(100% - 296px)
}

.no-select-hardware {
  text-align: center;
  color: #fff;
  height: 400px;
  line-height: 400px;
  width: calc(55% - 20px);
  margin-right: 20px;
}

.half-width {
  width: 100%;
  height: 300px;
  margin: 20px 0;
}

.form-width {
  width: 200px;
}

.color-txt {
  color: #fff
}

.title {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.save-btn {
  background: rgba(80, 172, 248, 0.3);
  border-radius: 2px 2px 2px 2px;
  border: 1px solid rgba(80, 172, 248, 0.96);
  font-weight: 400;
  font-size: 12px;
  color: #FFFFFF;
}
</style>
