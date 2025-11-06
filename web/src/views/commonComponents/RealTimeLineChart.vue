<!-- sse实时数据折线图 -->
<template>
    <a-spin class="audio-visualizer" :spinning="chartLoading">
        <div ref="chartRef" class="chart-container" />
    </a-spin>
</template>

<script setup lang="ts">
import { getTemperatureChart } from '@/api/dataCenter/index.js'
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { useSpeedTimetorStore } from '@/stores/speedTime.js'
import moment from 'moment'
interface LineData {
    name: string
    value: [string, string][]
    color?: string
}
interface SSEConfig {
    userId: number
    hardwareId: number,
    sseType: string
}
const props = defineProps<{
    sseUrl: SSEConfig
}>()
// 图表实例
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
const colorList = ['#00D1FF', '#3ECAB3']
let speedTimeStore = useSpeedTimetorStore()
// 数据状态
const chartData = ref<LineData[]>([])
const baseUrl = import.meta.env.VITE_BASE_API
let eventSource = ref()
let chartLoading = ref(false)
let maxPoints = 0
const timeoutDuration = 30000; // 30 秒超时
// 构建 SSE URL
const buildSseUrl = (config: SSEConfig) => {
    console.log('拼接的sse', `${baseUrl}sse/${config.sseType}/${config.userId}/${config.hardwareId}`)
    return `${baseUrl}sse/${config.sseType}/${config.userId}/${config.hardwareId}`
}
// 添加获取历史数据的函数
const fetchHistoricalData = async (timestamp: string, name: string) => {
    try {
        // 计算5分钟前的时间
        const endTime = new Date(timestamp)
        const startTime = new Date(endTime.getTime() - 5 * 60 * 1000)
        const response = await getTemperatureChart({
            hardwareId: props.sseUrl.hardwareId,
            startTime: moment(startTime).format('YYYY-MM-DD HH:mm:ss'),
            endTime: timestamp
        })

        if (response.code != 200) {
            throw new Error('Failed to fetch historical data')
        }
        return await response.data
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
            data: chartData.value.map(item => item.name),
            bottom: 10,
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
                let result = moment(params[0].axisValue).format('YYYY-MM-DD HH:mm:ss') + '<br/>'
                params.forEach((param: any) => {
                    result += `${param.seriesName}: ${param.data[1]}<br/>`
                })
                return result
            }
        },
        grid: {
            left: '0%',
            right: '0%',
            bottom: '10%',
            top: '20px',
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
        series: chartData.value.map((item, index) => ({
            name: item.name,
            type: 'line',
            showSymbol: false,
            data: item.value,
            lineStyle: {
                width: 1.5,
            },
            emphasis: {
                focus: 'series',
                lineStyle: {
                    width: 2.5
                }
            },
            animation: false,
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
            right: 0,
            top: 0,
            textStyle: {
                color: '#ffffff'
            },
        },
        series: newData.map(item => ({
            name: item.name,
            type: 'line',  // 必须指定类型
            showSymbol: false,
            data: item.value,
            emphasis: {
                focus: 'series'
            },
            animation: false,
            smooth: true
        }))
    })
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
            speedTimeStore.setTemperatureTime(newData[0].value[0][0])
            chartLoading.value = false
            // console.log('Received SSE data:', newData)

            // 处理第一条消息
            if (isFirstMessage) {
                isFirstMessage = false
                // 获取每个规则的历史数据
                const historicalData = await fetchHistoricalData(
                    newData[0].value[0][0],
                    newData[0].name
                )
                maxPoints = historicalData[0].value.length
                for (const item of newData) {
                    if (item.value && item.value.length > 0) {
                        // 初始化或更新图表数据
                        const historyLine = historicalData.filter(line => line.name == item.name)[0].value
                        chartData.value.push({
                            name: item.name,
                            value: [...historyLine, ...item.value],
                        })

                    }
                }
            } else {
                // 更新现有数据并保持10分钟窗口
                newData.forEach(newItem => {
                    const existingLine = chartData.value.find(
                        line => line.name === newItem.name
                    )

                    if (existingLine) {
                        if (Array.isArray(newItem.value) && newItem.value.length > 0) {
                            // 添加新数据
                            existingLine.value.push(...newItem.value)
                        }
                    } else {
                        chartData.value.push({
                            name: newItem.name,
                            value: [...newItem.value]
                        })
                    }
                })
                // // 如果超过最大点数，移除旧数据
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
            // console.log(chartData.value, '温度实时数据')
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

// 生命周期钩子
onMounted(() => {
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
    text-align: center;
    width: 100%;
    height: 100%
}

.chart-container {
    width: 100%;
    height: 100%;
    min-height: 200px;
}
</style>