<!-- sse实时数据折线图 -->
<template>
    <div class="audio-visualizer">
        <a-spin :spinning="chartLoading">
            <div ref="chartRef" class="chart-container" :style="{ width: '100%', height: height + 'px' }" />
        </a-spin>
    </div>
</template>

<script setup lang="ts">
import { getLineChart } from '@/api/overview/index.js'
import { getSpeedLimit } from '@/api/dataCenter/index.js'
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import moment from 'moment'
import { useSiteStore } from '@/stores/site.js'
import { storeToRefs } from 'pinia'
import { useSpeedTimetorStore } from '@/stores/speedTime.js'
const colorList = ['#00D1FF', '#3ECAB3']
interface LineData {
    name: string
    value: [string, string][]
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
const { fanId } = storeToRefs(siteConf)
let speedTimeStore = useSpeedTimetorStore()
// 图表实例
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

// 数据状态
const chartData = ref<LineData[]>([])
const baseUrl = import.meta.env.VITE_BASE_API
let eventSource = ref()
let chartLoading = ref(false)
let maxPoints = 0
let limitData = ref([])
const timeoutDuration = 30000; // 30 秒超时
// 构建 SSE URL
const buildSseUrl = (config: SSEConfig) => {
    console.log('拼接的sse', `${baseUrl}sse/${config.sseType}/${config.userId}/${fanId.value}`)
    return `${baseUrl}sse/${config.sseType}/${config.userId}/${config.siteId}`
}
// 补全时间格式
const fixTimeFormat = (timeStr: string) => {
    let now = new Date();
    let [hours, minutes, seconds] = timeStr.split(':');
    let [sec, ms] = seconds.split('.');

    now.setHours(parseInt(hours, 10));
    now.setMinutes(parseInt(minutes, 10));
    now.setSeconds(parseInt(sec, 10));
    now.setMilliseconds(parseInt(ms, 10));

    return now;
}
// 添加获取历史数据的函数
const fetchHistoricalData = async (timestamp: string) => {
    try {
        // 计算5分钟前的时间
        const endTime = new Date(timestamp)
        const startTime = new Date(endTime.getTime() - 5 * 60 * 1000)
        const response = await getLineChart({
            siteId: props.sseUrl.siteId,
            startTime: moment(startTime).format('YYYY-MM-DD HH:mm:ss'),
            endTime: moment(timestamp).format('YYYY-MM-DD HH:mm:ss')
        })

        if (response.code != 200) {
            throw new Error('Failed to fetch historical data')
        }

        return await response.data[0].value
    } catch (error) {
        console.error('Error fetching historical data:', error)
        return []
    }
}
const extractPoints = (rawData) => {
    let result = [];
    rawData.forEach(item => {
        item.value.forEach(([time, value]) => {
            result.push([time, parseFloat(value)]);
        });
    });
    return result;
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
            data: chartData.value.map(item => item.name),
            textStyle: {
                color: '#666'
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
                let result = moment(params[0].axisValue).format('YYYY-MM-DD HH:mm:ss') + '<br/>'
                params.forEach((param: any) => {
                    result += `${param.seriesName}: ${param.data[1]}<br/>`
                })
                return result
            }
        },
        grid: {
            left: '4%',
            right: '50px',
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
                // formatter: '{HH}:{mm}:{ss}',
                color: '#666'
            },
            // 添加最小间隔
            // minInterval: 1000, // 1秒
            // 添加滚动条
            axisPointer: {
                show: true,
                snap: true
            }
        },
        yAxis: {
            axisLine: {
                show: true,
            },
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
        },
        series: [{
            name: 'Speed',
            type: 'line',
            data: chartData.value,
            showSymbol: false,
            emphasis: {
                focus: 'series',
                lineStyle: {
                    width: 1.5
                }
            },
            animation: false,
            // smooth: true,
            connectNulls: true,
            // 阈值设置
            markLine: {
                symbol: ['none', 'none'],
                data: limitData.value.map(item => ({
                    name: item.threshold,
                    label: {
                        show: true,
                        color: 'red'
                    },
                    yAxis: item.threshold
                })),
                // label: {
                //     show: true,
                //     formatter: '{b}'  // 显示标记线的名称，这里使用默认的 {b} 表示
                // },
                lineStyle: {
                    type: 'dashed',  // 线条类型，可以是 solid, dashed, dotted
                    color: 'red'     // 线条颜色
                }
            }
        }]
    }

    chartInstance.setOption(option)
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
// 更新图表数据
const updateChart = (newData: LineData[]) => {
    if (!chartInstance) return
    chartInstance.setOption({
        legend: {
            data: newData[0].name,
            textStyle: {
                color: '#ffffff'
            },
            bottom: 0,
            show: props.legendFlag
        },
        series: [{
            data: newData
        }]
    })
}

// 修改 SSE 设置函数
const setupSSE = () => {
    chartLoading.value = true
    const url = buildSseUrl(props.sseUrl)
    const eventSource = new EventSource(url)
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
            // 设置最新时间，持续更新
            speedTimeStore.setSpeedTime(newData[0].value[0][0])
            chartLoading.value = false
            // console.log('Received SSE data:', newData)
            let receivedData = JSON.parse(event.data);
            let points = extractPoints(receivedData);
            // 处理第一条消息
            if (isFirstMessage) {
                isFirstMessage = false
                //获取每个规则的历史数据
                const historicalData = await fetchHistoricalData(
                    newData[0].value[0][0]
                )
                // maxPoints = historicalData[0].value.length
                // chartData.value.unshift(...historicalData.value)
                // 把新增点追加到图表数据中
                chartData.value.push(...historicalData, ...points);


            } else {
                chartData.value.push(...points);
                // console.log(chartData.value, 'chartData.value11111111111111')
                // chartData.value.shift()
                // 如果超过最大点数，移除旧数据
                // if (chartData.value.length > maxPoints) {
                //     chartData.value = chartData.value.slice(chartData.value.length - maxPoints);
                // }
                // chartData.value.map(item => {
                //     if (item.value.length > maxPoints) {
                //         item.value = item.value.slice(item.value.length - maxPoints)
                //     }
                // })
            }

            // 更新图表
            updateChart(chartData.value)

        } catch (error) {
            clearTimeout(timeoutTimer)
            chartLoading.value = false
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
// 生成随机颜色（用于新添加的数据线）
const getRandomColor = () => {
    const colors = [
        '#FF4B4B', '#4B9EFF', '#4BFF6D', '#FFB74B',
        '#9E4BFF', '#4BFFB7', '#FF4B9E', '#B74BFF'
    ]
    return colors[Math.floor(Math.random() * colors.length)]
}

// 模拟数据（用于测试）
const mockSSE = () => {
    setInterval(() => {
        const now = new Date().toISOString()

        // 更新每条线的数据
        chartData.value.forEach(line => {
            // 生成不同的模拟数据
            let value
            switch (line.name) {
                case 'd1':
                    value = Math.sin(Date.now() / 1000) * 50 + 50
                    break
                case 'd2':
                    value = Math.cos(Date.now() / 1000) * 30 + 60
                    break
                case 'd3':
                    value = Math.sin(Date.now() / 500) * 20 + 40
                    break
                default:
                    value = Math.random() * 100
            }

            line.value.push([now, value])

            // 限制数据点数量
            if (line.value.length > 100) {
                line.value.shift()
            }
        })

        updateChart(chartData.value)
    }, 1000)
}

// 响应式调整
const handleResize = () => {
    chartInstance?.resize()
}
const getLimit = async () => {
    const res = await getSpeedLimit()
    return res.data
}
onMounted(async () => {
    limitData.value = await getLimit()
    initChart()
    eventSource.value = setupSSE()  // 实际 SSE
    // mockSSE()  // 测试用
    window.addEventListener('resize', handleResize)
})
// 清理函数
onUnmounted(() => {
    eventSource.value.close()
    chartInstance?.dispose()
    window.removeEventListener('resize', handleResize)
})
defineExpose({
    downloadChartImage
})
</script>

<style scoped>
.audio-visualizer {
    width: 100%;
    height: 100%;
    text-align: center;
}

.chart-container {
    width: 100%;
    height: 100%;
    min-height: 120px;
}
</style>