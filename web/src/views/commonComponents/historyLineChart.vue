<!-- 历史折线图 -->
<template>
  <div class="content">
    <a-spin :spinning="chartLoading">
      <template v-if="emptyShowFlag">
        <div ref="chartRef" class="chart-container" :style="{ width: '100%', height: height + 'px' }"
          v-if="!emptyFlag" />
      </template>
      <template v-else>
        <div ref="chartRef" class="chart-container" :style="{ width: '100%', height: height + 'px' }" />
      </template>
      <empty v-if="emptyFlag && emptyShowFlag" :style="{ width: '100%', height: height + 'px' }" />
    </a-spin>
  </div>


</template>

<script setup lang="ts">
import empty from './empty.vue'
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { useSiteStore } from '@/stores/site.js'
import { storeToRefs } from 'pinia'
import moment from 'moment'
let siteCof = useSiteStore()
const { fanId, hardwareId, hardwareType, monitorType } = storeToRefs(siteCof)
let chartLoading = ref(false)
// 示例数据
// const chartData = ref([
//   {
//     name: '温度',
//     value: [
//       [new Date('2024-03-15 10:00:00').getTime(), 25],
//       [new Date('2024-03-15 10:05:00').getTime(), 26],
//       [new Date('2024-03-15 10:10:00').getTime(), 28],
//       // ...更多数据点
//     ]
//   },
//   {
//     name: '湿度',
//     value: [
//       [new Date('2024-03-15 10:00:00').getTime(), 60],
//       [new Date('2024-03-15 10:05:00').getTime(), 62],
//       [new Date('2024-03-15 10:10:00').getTime(), 65],
//       // ...更多数据点
//     ]
//   }
// ])
interface LineData {
  value: [string | number, number][]
  name: string
}

interface Props {
  data: LineData[]
  height?: number
  title?: string
  yAxisName?: string
  smooth?: boolean
  pointFlag?: boolean
  titleFlag?: boolean
  emptyShowFlag?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  height: 400,
  title: '',
  yAxisName: '',
  smooth: false,
  doubleYaxis: false,
  pointFlag: false,
  titleFlag: true,
  emptyShowFlag: true
})
const colorList = ['#00D1FF', '#3ECAB3']
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null
let emptyFlag = ref(false)
// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  chart = echarts.init(chartRef.value)
  updateChart()

  // 监听窗口变化
  window.addEventListener('resize', handleResize)
}

// 更新图表
const updateChart = () => {

  if (!chart) return
  // if (props.data.length == 0) {
  //   chartLoading.value = false
  // } else {
  chartLoading.value = true
  // }
  let yAxis = []
  if (monitorType.value == 2) {
    yAxis = [{
      axisLine: {
        show: true,
      },
      name: 'GeneratorSpeedDelay',
      type: 'value',
      splitLine: {
        show: false,
        lineStyle: {
          type: 'dashed',
          color: '#DDD'
        }
      },
      axisLabel: {
        color: '#666'
      }
    }, {
      axisLine: {
        show: true,
      },
      name: 'GeneratorSpeedFluctuateDelay',
      type: 'value',
      min: 0,
      max: 400,
      interval: 100,
      splitLine: {
        show: false,
        lineStyle: {
          type: 'dashed',
          color: '#DDD'
        }
      },
      axisLabel: {
        color: '#666'
      }
    }]
  } else {
    yAxis = [{
      axisLine: {
        show: true,
      },
      name: props.yAxisName,
      type: 'value',
      splitLine: {
        show: false,
        lineStyle: {
          type: 'dashed',
          color: '#DDD'
        }
      },
      axisLabel: {
        color: '#666'
      }
    }]
  }
  const series = props.data.map(item => ({
    name: item.name,
    type: props.pointFlag ? 'scatter' : 'line',
    data: item.value,
    smooth: props.smooth,
    showSymbol: props.pointFlag, // 默认不显示数据点
    symbol: 'circle',  // 数据点的形状
    symbolSize: 6,     // 数据点的大小
    emphasis: {        // 鼠标悬停时的效果
      scale: true,
      focus: 'series',
      showSymbol: true
    }
  }))
  const option: EChartsOption = {
    color: colorList,
    title: {
      text: props.title,
      left: 'center',
      textStyle: {
        color: '#ffffff'
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: function (params: any) {
        let result = params[0].axisValue + '<br/>'
        params.forEach((param: any) => {
          result += `${param.seriesName}: ${param.data[1]}<br/>`
        })
        return result
      }
    },
    legend: {
      data: props.data.map(item => item.name),
      bottom: 0,
      textStyle: {
        color: '#ffffff'
      },
      show: props.titleFlag
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: {
        show: true
      },
      axisTick: {
        show: true
      },
      splitLine: {
        show: false
      },
      axisLabel: {
        // formatter: (value: number) => {
        //   const date = new Date(value)
        //   return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
        // }
      }
    },
    yAxis: yAxis,
    series
  }

  chart.setOption(option)
  chartLoading.value = false
}
// 下载图表图片
const downloadChartImage = () => {
  const imageURL = chart.getDataURL({
    type: 'png', // 图片格式，支持 png、jpeg
    pixelRatio: 2, // 图片分辨率比例，默认为 1
    backgroundColor: '#fff', // 图片背景色
  });
  const link = document.createElement('a');
  link.href = imageURL;
  link.download = 'chart.png'; // 下载文件名
  link.click();
}
// 处理窗口大小变化
const handleResize = () => {
  chart?.resize()
}

// 监听数据变化
// watch(
//   () => props.data,
//   (newData) => {
//     console.log(newData, 'newData----------------------')
//     if (Array.isArray(newData) && newData.length > 0) {
//       console.log('传来的data是否为空')
//       const emptyList = newData.every(item => isValueEmpty(item.value))
//       console.log(emptyList, 'emptyList')
//       if (emptyList) {
//         emptyFlag.value = true
//       } else {
//         updateChart()
//       }
//     } else {
//       console.log('传来的data是空')
//       emptyFlag.value = true
//     }

//   },
//   { deep: true }
// )
const checkEmpty = () => {
  if (Array.isArray(props.data) && props.data.length > 0) {
    console.log('传来的data是否为空')
    emptyFlag.value = props.data.every(item => isValueEmpty(item.value))
    // console.log(emptyList, 'emptyList')
    // if (emptyList) {
    //   emptyFlag.value = true
    // } else {
    //   updateChart()
    // }
  } else {
    console.log('传来的data是空')
    emptyFlag.value = true
  }
}
const isValueEmpty = (value) => {
  if (Array.isArray(value)) {
    console.log('判断是否为空', value.length)
    return value.length === 0;
  }
  if (typeof value === 'object' && value !== null) {
    return Object.keys(value).every(key => isValueEmpty(value[key]));
  }
  return false;
};
onMounted(() => {
  checkEmpty()
  initChart()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
defineExpose({
  downloadChartImage
})
</script>
<style scoped>
.content {
  text-align: center;
  width: 100%;
  height: 100%
}

.chart-container {
  width: 100%;
  height: 100%;
  min-height: 120px;
}
</style>