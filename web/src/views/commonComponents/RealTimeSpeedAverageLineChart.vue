<template>
    <div class="audio-visualizer">
        <a-spin :spinning="chartLoading">
            <div ref="chartRef" class="chart-container" :style="{ width: '100%', height: height + 'px' }" />
        </a-spin>
    </div>

</template>

<script setup lang="ts">
import { getAverageLineChart } from '@/api/overview/index.js'
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import moment from 'moment'
import { useSiteStore } from '@/stores/site.js'
import { storeToRefs } from 'pinia'
import { useSpeedTimetorStore } from '@/stores/speedTime.js'
interface LineData {
    name: string
    value: [string, string][] // each value is a pair of timestamp and value
    color?: string
}
interface SSEConfig {
    userId: number
    siteId: number,
    sseType: string
}
const props = defineProps<{
    sseUrl: SSEConfig,
    height: string,
    legendFlag: boolean
}>()
let siteConf = useSiteStore()
let speedTimeStore = useSpeedTimetorStore()
const { fanId } = storeToRefs(siteConf)
// 图表实例
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null


const chartData = ref<LineData[]>([])
const baseUrl = import.meta.env.VITE_BASE_API
let eventSource = ref()
const colorList = ['#00D1FF', '#3ECAB3']
let chartLoading = ref(false)
let maxPoints = 0
const timeoutDuration = 30000; // 30 秒超时

// Build SSE URL
const buildSseUrl = (config: SSEConfig) => {
    return `${baseUrl}sse/${config.sseType}/${config.userId}/${config.siteId}`
}

// 追加前10分钟数据
const fetchHistoricalData = async (timestamp: string) => {
    try {
        const endTime = new Date(timestamp)
        const startTime = new Date(endTime.getTime() - 10 * 60 * 1000)
        const response = await getAverageLineChart({
            siteId: props.sseUrl.siteId,
            startTime: moment(startTime).format('YYYY-MM-DD HH:mm:ss'),
            endTime: moment(timestamp).format('YYYY-MM-DD HH:mm:ss')
        })

        if (response.code !== 200) {
            throw new Error('Failed to fetch historical data')
        }

        return response.data
    } catch (error) {
        console.error('Error fetching historical data:', error)
        return []
    }
}

// 初始化图表
const initChart = () => {
    if (!chartRef.value) return

    chartInstance = echarts.init(chartRef.value)
    const option: EChartsOption = {
        color: colorList,
        title: {
            // text: '实时数据',
            textStyle: {
                fontSize: 14,
                color: '#333'
            }
        },
        legend: {
            data: ['Flow', 'Rainfall'],
            textStyle: {
                color: '#ffffff'
            }
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
                let result = moment(params[0].axisValue).format('YYYY-MM-DD HH:mm:ss.SSS') + '<br/>'
                params.forEach((param: any) => {
                    result += `${param.seriesName}: ${param.data[1]}<br/>`
                })
                return result
            }
        },
        grid: {
            left: '4%',
            right: '4%',
            bottom: '5%',
            top: '10px',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            splitLine: {
                show: false
            },
            axisLabel: {
                //formatter: '{HH}:{mm}:{ss}',
                color: '#666'
            },
            // minInterval: 1000, // 1 second
            axisPointer: {
                show: true,
                snap: true
            }
        },
        yAxis: [{
            axisLine: {
                show: true,
            },
            name: '',
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
        }],
        dataZoom: [
            {
                type: 'inside', // 内置型数据区域缩放组件
                start: 10, // 左边在10%的位置
                end: 60 // 右边在60%的位置
            }
        ],
        series: chartData.value.map((item, index) => ({
            name: item.name,
            type: 'line',
            showSymbol: false,
            data: item.value,
            lineStyle: {
                width: 1.5
            },
            emphasis: {
                focus: 'series',
                lineStyle: {
                    width: 2.5
                }
            },
            animation: false,
            // smooth: true
        }))
    }

    chartInstance.setOption(option)
}

// 更新图表数据
const updateChart = (newData: LineData[]) => {
    if (!chartInstance) return

    chartInstance.setOption({
        legend: {
            data: newData.map(item => item.name),
            textStyle: {
                color: '#ffffff'
            },
            bottom: 0,
            show: props.legendFlag
        },
        series: newData.map(item => ({
            name: item.name,
            type: 'line',
            showSymbol: false,
            data: item.value,
            lineStyle: {
                width: 1.5,
                // color: item.color
            },
            emphasis: {
                focus: 'series'
            },
            animation: false,
            smooth: true
        }))
    })
}

// 创建 SSE 连接
const setupSSE = () => {
    chartLoading.value = true
    // 拼接得到sse路径
    const url = buildSseUrl(props.sseUrl)
    const eventSource = new EventSource(url)
    // 是否是第一次消息
    let isFirstMessage = true
    // 设置超时，关闭连接
    let timeoutTimer = setTimeout(() => {
        console.log('No data received within timeout period. Closing connection.');
        eventSource.close();
        chartLoading.value = false
    }, timeoutDuration);
    eventSource.onmessage = async (event) => {
        try {
            clearTimeout(timeoutTimer)
            const newData = JSON.parse(event.data)
            chartLoading.value = false
            // 设置最新时间，持续更新
            speedTimeStore.setAverageTime(newData[0][0].value[0][0])
            if (isFirstMessage) {
                // 是第一次把第一次返回的时间当中请求得结束时间获取前几分钟数据
                isFirstMessage = false
                const historicalData = await fetchHistoricalData(
                    newData[0][0].value[0][0]
                )
                maxPoints = historicalData[0][0].value.length
                for (const item of newData) {
                    item.forEach(lisItem => {
                        // 初始化或更新图表数据
                        let historyLine = []
                        historicalData.map(line => historyLine = line.filter(lis => lis.name == lisItem.name))
                        chartData.value.push({
                            name: lisItem.name,
                            value: [...historyLine[0].value, ...lisItem.value],
                        })

                    })
                }
            } else {
                newData.forEach(newItem => {
                    newItem.forEach(lis => {
                        const existingLine = chartData.value.find(
                            line => line.name === lis.name
                        )

                        if (existingLine) {
                            existingLine.value.push(...lis.value)
                        } else {
                            chartData.value.push({
                                name: lis.name,
                                value: lis.value,
                            })
                        }
                    })
                })
                // chartData.value.map(item => {
                //     if (item.value.length > maxPoints) {
                //         item.value = item.value.slice(item.value.length - maxPoints)
                //     }
                // })
                // // 如果超过最大点数，移除旧数据
                // if (chartData.value.length > maxPoints) {
                //     chartData.value = chartData.value.slice(chartData.value.length - maxPoints);
                // }
            }
            // 更新图表数据
            updateChart(chartData.value)
        } catch (error) {
            chartLoading.value = false
            clearTimeout(timeoutTimer)
            console.error('Error processing SSE data:', error)
        }
    }

    eventSource.onerror = (error) => {
        chartLoading.value = false
        clearTimeout(timeoutTimer)
        console.error('SSE Error:', error)
        eventSource.close()
    }
    eventSource.addEventListener('close', () => {
        console.log('Connection closed by server.');
        chartLoading.value = false
        clearTimeout(timeoutTimer)
        eventSource.close();
    });
    return eventSource
}
// 下载图表图片
const downloadChartImage = () => {
    const imageURL = chartInstance.getDataURL({
        type: 'png', // 图片格式，支持 png、jpeg
        pixelRatio: 2, // 图片分辨率比例，默认为 1
        backgroundColor: '#fff', // 图片背景色
    });
    const link = document.createElement('a');
    link.href = imageURL;
    link.download = 'chart.png'; // 下载文件名
    link.click();
}
// 随机生成颜色，暂时没用到
const getRandomColor = () => {
    const colors = [
        '#FF4B4B', '#4B9EFF', '#4BFF6D', '#FFB74B',
        '#9E4BFF', '#4BFFB7', '#FF4B9E', '#B74BFF'
    ]
    return colors[Math.floor(Math.random() * colors.length)]
}

onMounted(() => {
    initChart()
    eventSource.value = setupSSE()
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    eventSource.value.close()
    chartInstance?.dispose()
    window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
    chartInstance?.resize()
}
defineExpose({
    downloadChartImage
})
</script>

<style scoped>
.audio-visualizer {
    width: 100%;
    height: 100%;
    /* height: 400px; */
}

.chart-container {
    width: 100%;
    height: 100%;
    min-height: 120px;
}
</style>