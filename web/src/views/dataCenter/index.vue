<!-- 数据中心 -->
<template>
  <div class="page-box">
    <Menu :defaultKey="true" :hardwareDefaultKey="false"></Menu>
    <div class="page-search-content">
      <div class="search">
        <a-space wrap>
          <!-- <span style="color:#fff">{{ hardwareType }}{{ showTableBox }}</span> -->
          <div class="funItem">
            <label>数据类型：</label>
            <a-select ref="select" v-model:value="dataType" @change="handleChange" :disabled="hardwareType != 2"
              :class="{ 'disabled-select': hardwareType !== 2 }" aria-placeholder="请选择">
              <a-select-option v-for="(item, index) in dataTypeList" :value="item.value" :key="index">{{ item.label
              }}</a-select-option>
            </a-select>
          </div>
          <div class="funItem">
            <label>时间：</label>
            <!-- <a-range-picker v-model:value="dateRange" /> -->
            <a-date-picker v-model:value="fmSearch.startTime" placeholder="请选择开始时间" style="width: 100%"
              valueFormat="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledStartDate" show-time />
            <a-span>-</a-span>
            <a-date-picker v-model:value="fmSearch.endTime" placeholder="请选择结束时间" style="width: 100%"
              valueFormat="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledStartDate" show-time />
          </div>
        </a-space>
        <a-space>
          <a-button type="primary" class="primary" ghost
            @click="getPageInfo({ pageNum: 1, pageSize: 10 })">搜索</a-button>
          <a-button ghost class="info" @click="reset">重置</a-button>
        </a-space>
      </div>
      <div class="content-box" v-if="hardwareId">
        <div :class="['hardcare-box', showTableBox ? '' : 'large-width']">
          <div class="title-box">
            <div class="title">数据详情</div>
            <a-button class="save-btn" @click="saveData" v-if="saveBtnShow">保存数据</a-button>
          </div>
          <div class="chart-player-box">
            <a-spin :spinning="chartLoading">
              <HistoryLineChart ref="speedChart" v-if="showSpeedLineChart" :data="chartData"
                :height="monitorType == 4 ? 260 : 500" />
              <HistoryLineChart ref="fftChart" v-if="showSpeedLineChart && monitorType == 4" :data="chartData2"
                :height="260" />
              <!-- 监控，热成像 -->
              <WebRTCPlayer v-if="showWebRTCPlayer" :rtsp-url="rtspUrl" ref="webRtcRef" />
              <!-- 历史热成像最高温度数据折线图，长时温度统计 -->
              <HistoryLineChart ref="tempChart" v-if="showHistoryLineChart" :data="chartData" :height="300"
                :pointFlag="pointFlag" />
              <!-- 音频 -->
              <Waversurfer v-if="showAudioVisualizer" :fileID="fileID" v-loading="true" style="width:100%;" />
            </a-spin>
          </div>

        </div>
        <div class="table-box" v-if="showTableBox">
          <a-spin :spinning="tableLoading">
            <Table @getPageInfo="getPageInfo" :columns="columns" :list="pageInfo.list" :total="pageInfo.total">
              <template #action="{ record }">
                <a-button class="editBtn" @click="showDetial(record)">查看</a-button>
              </template>
            </Table>
          </a-spin>
        </div>
      </div>

      <div class="no-select-hardware" v-else>
        请筛选后查看
      </div>
    </div>
  </div>
</template>
<script setup>
import { getOrgHeader } from '@/api/system/index.js'
import { getFluctuateLineChart, getLineChart, getAverageLineChart } from '@/api/overview/index.js'
import { getVedioList, getAudioList, getVideoStreamHis, getTemperatureChart, getLongTemperatureChart, getFftChart, getFftChart2, getLastFFTActive, getFFTActive, getFTActiveById, speedSpectrumExport, speedStabilityExport, speedFluctuationExport, speedExport, getVideoDownload } from '@/api/dataCenter/index.js'
import { useSysStore } from '@/stores/system.js'
import { useSiteStore } from '@/stores/site.js'
import { storeToRefs } from 'pinia'
import Menu from '@/views/commonComponents/menu.vue'
import WebRTCPlayer from '@/views/commonComponents/WebRTC.vue'
import Waversurfer from '@/views/commonComponents/waversurfer.vue'
import HistoryLineChart from '../commonComponents/historyLineChart.vue'
import Table from '@/views/commonComponents/table.vue'
import moment from 'moment'
import { columns } from './columns'
import { computed, ref, watch } from 'vue'
import $X from '@/utils/$X.js'
// 系统配置
let sysConf = useSysStore()
let siteCof = useSiteStore()
let dataType = ref('')
let rtspUrl = ref('')
let chartData = ref([])
let chartData2 = ref([])
let fileID = ref()
let speedChart = ref()
let fftChart = ref()
let tempChart = ref()
const dataTypeList = ref([{ label: '视频', value: 'video' }, { label: '最高温度数据', value: 'line' }, { label: '长时温度统计', value: 'maxLine' }])
const { siteId } = storeToRefs(sysConf)
const { fanId, hardwareId, hardwareType, monitorType } = storeToRefs(siteCof)
let pointFlag = ref(false)
// 分页查询
let fmSearch = ref({
  startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
  endTime: moment(new Date() - 1 * 60 * 1000).format('YYYY-MM-DD HH:mm:ss'),
  hardwareId: 0
})
let pageInfo = ref({
  list: [],
  total: 0,
  pageSize: 10,
  pageNo: 1
})
let routerParams = ref()
// 查看详情的触发事件
let time = ref()
// 计算属性优化
const showWebRTCPlayer = computed(() => {
  return rtspUrl.value && (hardwareType.value === 3 || (hardwareType.value === 2 && ['video'].includes(dataType.value)))
})
const showSpeedLineChart = computed(() => {
  return chartData.value.length && hardwareType.value === 1
})
const showHistoryLineChart = computed(() => {
  return hardwareType.value === 2 &&
    ['line', 'maxLine'].includes(dataType.value) && chartData.value.length
})

const showAudioVisualizer = computed(() => {
  return hardwareType.value === 4 && fileID.value
})
const showTableBox = computed(() => {
  return !['line', 'maxLine'].includes(dataType.value) && ![1, 2, 3].includes(monitorType.value)
})
const saveBtnShow = computed(() => {
  return (showWebRTCPlayer.value || showSpeedLineChart.value || showHistoryLineChart.value || showAudioVisualizer.value)
})
let tableLoading = ref(false)
let chartLoading = ref(false)
// 可选：使用 watch 监听相关变量变化
watch(() => hardwareType.value, (newHardwareType) => {
  if (newHardwareType == 2) {
    dataType.value = 'video'
  } else {
    dataType.value = ''
  }
})

// 可选：监听 rtspUrl 变化
watch(rtspUrl, (newUrl) => {
  if (newUrl) {
    // 处理 URL 变化时的逻辑
    console.log('RTSP URL 已更新:', newUrl)
  }
})
// 监听硬件变化,重置页面状态
watch(() => hardwareId.value, (newItems) => {
  if (newItems) {
    fmSearch.value = {
      startTime: moment().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      endTime: moment(new Date().getTime() - 1 * 60 * 1000).format('YYYY-MM-DD HH:mm:ss'),
      hardwareId: 0
    }
    pageInfo.value.list = []
    pageInfo.value.total = 0
    pageInfo.value.pageNo = 0
    rtspUrl.value = ''
    chartData.value = []
    fileID.value = ''
    //sseUrl = ''
    //roomUrl = ''
    //roomToken = ''
    getPageInfo({ pageNum: 1, pageSize: 10 })
  }
})

// 查询页面列表
const getPageInfo = ({ pageNum, pageSize }) => {
  if (hardwareType.value == 1 && monitorType.value != 4) {
    getSpeedLine()
  } else {
    tableLoading.value = true
    pageInfo.value.pageNo = pageNum
    pageInfo.value.pageSize
    fmSearch.value.hardwareId = hardwareId.value
    const params = {
      ...fmSearch.value,
      pageNum,
      pageSize
    }
    if (hardwareType.value == 2) {
      // 热成像
      if (dataType.value == 'line') {
        pointFlag.value = false
        // 热成像历史数据折线图
        getTemperatureChart(params).then(res => {
          chartData.value = res.data
        })

      } else if (dataType.value == 'maxLine') {
        pointFlag.value = true
        getLongTemperatureChart({ hardwareId: hardwareId.value, startTime: fmSearch.value.startTime, endTime: fmSearch.value.endTime }).then(res => {
          chartData.value = res.data
        })
      }
      else {
        getVedioList(params).then((res) => {
          pageInfo.value.list = res.data.records
          pageInfo.value.total = res.data.total
          tableLoading.value = false
        })
      }

    } else if (hardwareType.value == 3) {
      // 监控
      getVedioList(params).then((res) => {
        pageInfo.value.list = res.data.records
        pageInfo.value.total = res.data.total
        tableLoading.value = false
      })
    } else if (hardwareType.value == 4) {
      // 音频
      getAudioList(params).then(res => {
        console.log('不知道')
        pageInfo.value.list = res.data.records
        pageInfo.value.total = res.data.total
        tableLoading.value = false
      })
    } else if (monitorType.value == 4) {
      getFFTActive({ ...params, siteId: fanId.value }).then(res => {
        pageInfo.value.list = res.data.records
        pageInfo.value.total = res.data.total
        tableLoading.value = false
      })
    }

  }

}
const showDetial = (item) => {
  chartLoading.value = true
  fileID.value = ''
  rtspUrl.value = ''
  let params = {
    ...fmSearch.value
  }
  time.value = new Date(item.time)
  const endTime = new Date(time.value.getTime() + 60 * 60 * 1000)
  params.startTime = moment(time.value).format('YYYY-MM-DD HH:mm:ss')
  params.endTime = moment(endTime).format('YYYY-MM-DD HH:mm:ss')
  if (hardwareType.value == 2 || hardwareType.value == 3) {
    getVideoStreamHis(params).then((res) => {
      rtspUrl.value = $X.decryptRTSP(res.data)
      chartLoading.value = false
    })
  } else if (hardwareType.value == 4) {
    fileID.value = item.fileID
    chartLoading.value = false
  } else if (monitorType.value == 4) {
    getFTActiveById({ id: item.id }).then(res => {
      chartData.value = res.data
    })
    getFft()
  }
}
const getFft = async () => {
  let time = await getLastFFTActive({ siteId: fanId.value })
  let startTime = moment(new Date(time.data.activeTime).getTime() - 10 * 60 * 1000).format('YYYY-MM-DD HH:mm:ss')
  let endTime = moment(new Date(time.data.activeTime).getTime()).format('YYYY-MM-DD HH:mm:ss')
  const res2 = await getFftChart2({ siteId: fanId.value, startTime, endTime, hardwareId: hardwareId.value })
  chartData2.value = res2.data
  chartLoading.value = false
}
// 页面加载完成后执行
const tableHeight = ref()
onMounted(() => {
  // routerParams.value = 
})

// 重置事件
const reset = () => {
  fmSearch.value = {
    //pageSize: 10,
    //pageNum: 1,
    startTime: null,
    endTime: null,
    hardwareId: 0
  }
  pointFlag.value = false
  chartData.value = []
  if ((hardwareType.value == 2 && dataType.value == 'vedio') || hardwareType.value == 3 || hardwareType.value == 4) {
    getPageInfo({ pageNum: 1, pageSize: 10 })
  }

}
// 下载excel及图表图片
const saveData = () => {
  const params = {
    siteId: fanId.value,
    startTime: fmSearch.value.startTime,
    endTime: fmSearch.value.endTime
  }
  // 转速中要区分是哪个图表组件
  if (hardwareType.value == 1) {
    switch (monitorType.value) {
      // 震荡没有告警
      case 1:
        speedChart.value.downloadChartImage()
        getSpeedExport(params)
        break
      case 2:
        speedChart.value.downloadChartImage()
        fluctuatExport(params)
        break
      case 3:
        speedChart.value.downloadChartImage()
        averageExport(params)
        break
      case 4:
        speedChart.value.downloadChartImage()
        fftChart.value.downloadChartImage()
        fftExport(params)
        break
    }
  } else if (hardwareType.value == 2) {
    if (dataType.value == 'line' || dataType.value == 'maxLine') {
      tempChart.value.downloadChartImage()
    } else {
      // 热成像下载
      vedioDownload(params)
    }

    // temperatureExport()
  } else if (hardwareType.value == 3) {
    //视频下载
    vedioDownload(params)
  }
}
// 视频导出
const vedioDownload = (params) => {
  const endTime = new Date(time.getTime() + 60 * 60 * 1000)
  params.startTime = moment(time).format('YYYY-MM-DD HH:mm:ss')
  params.endTime = moment(endTime).format('YYYY-MM-DD HH:mm:ss')
  if (hardwareType.value == 2) {
    if (dataType.value == 'vedio') {
      getVideoDownload({ hardwareId: hardwareId.value, startTime: params.startTime, endTime: params.endTime })
    }
  }

}
// 频谱导出,endTime
const fftExport = async (params) => {
  let res = await speedSpectrumExport(params)
  exportExcel(res, '频谱')
}
// 超限导出
const getSpeedExport = async (params) => {
  let res = await speedExport(params)
  exportExcel(res, '超限')
}
// 震荡导出
const averageExport = async (params) => {
  let res = await speedFluctuationExport(params)
  exportExcel(res, '震荡')
}
// 稳定性导出
const fluctuatExport = async (params) => {
  let res = await speedStabilityExport(params)
  exportExcel(res, '稳定性')
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
const getSpeedLine = async () => {
  chartLoading.value = true
  const params = {
    siteId: fanId.value,
    startTime: fmSearch.value.startTime,
    endTime: fmSearch.value.endTime
  }
  let res = []
  //转速
  switch (monitorType.value) {
    case 1:
      res = await getLineChart(params)
      chartData.value = res.data
      chartLoading.value = false
      break
    case 2:
      res = await getFluctuateLineChart(params)
      chartData.value = res.data[0]
      chartLoading.value = false
      break
    case 3:
      res = await getAverageLineChart(params)
      chartData.value = res.data[0]
      chartLoading.value = false
      break
  }
}

</script>
<style lang="scss" scoped>
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
  height: 89%;
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
}

.title {
  font-weight: 500;
  font-size: 16px;
  color: #00D1FF;
  margin-bottom: 20px;
}

.chart {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-player-box {
  width: 100%;
  height: 500px;
  text-align: center;
  line-height: 500px;
}

.title-box {
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
