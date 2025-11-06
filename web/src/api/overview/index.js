import { POST, GET, DEL } from '../request'
// 告警列表
export const getAlarmList = (params) => {
  return GET('/alarm/list', params)
}
// 所有风机状态信息
export const getAllSiteStatus = (params) => {
  return GET('/overview/getSiteStatus', params)
}
// 修改告警状态
export const checkStatus = (data) => {
  return POST('/alarm/check', data)
}
// 获取按风机场所有风机的热成像的列表
export const getVideoStreamByType = (params) => {
  return GET('/video/getVideoStreamByType', params)
}
// 获取按风机场所有风机的音频的列表
export const getLiveByType = (params) => {
  return GET('/audio/liveByType', params)
}
// 获取超限前五分钟数据
export const getLineChart = (params) => {
  return GET('/speed/list/Chart', params)
}
// 获取稳定性前一分钟数据
export const getFluctuateLineChart = (params) => {
  return GET('/speed/fluctuate/Chart', params)
}
// 获取震荡前十分钟数据
export const getAverageLineChart = (params) => {
  return GET('/speed/average/Chart', params)
}
// 频谱导出
export const analysisfftExport = (params) => {
  return GET('/speed/AnalysisExport', params, { responseType: 'blob' })
}
// 震荡导出
export const analysisAverageExport = (params) => {
  return GET('/speed/oscillatesExport', params, { responseType: 'blob' })
}
// 超限导出
export const analysisSpeedExport = (params) => {
  return GET('/speed/speedingExport', params, { responseType: 'blob' })
}
// 稳定性导出
export const analysisFluctuteExport = (params) => {
  return GET('/speed/stabilityExport', params, { responseType: 'blob' })
}
// 温度导出
export const analysisTemperatureExport = (params) => {
  return GET('/ti/temperatureExport', params, { responseType: 'blob' })
}
// 长时温度导出
export const analysisLongTemperatureExport = (params) => {
  return GET('/ti/longTimeTemperatureExport', params, { responseType: 'blob' })
}
