import { POST, GET, DEL } from '../request'
export const getAlarmList = (params) => {
  return GET('/alarm/list', params)
}

// 导出数据列表
export const exportAlarmList = (data) => {
  return POST('/alarm/export', data, { responseType: 'blob' })
}
// 获取告警速度告警数据
export const getAlarmSpeedData = (params) => {
  return GET('/alarm/getSpeedData', params)
}
// 获取告警稳定性数据
export const getAlarmFluctuateData = (params) => {
  return GET('/alarm/getFluctuateData', params)
}
// 获取告警震荡数据
export const getAlarmAverageData = (params) => {
  return GET('/alarm/getAverageData', params)
}
// 获取温度告警数据
export const getTemperatureData = (params) => {
  return GET('/alarm/getTemperatureData', params)
}
//频谱导出
export const analysisfftExport = (params) => {
  return GET('/alarm/AnalysisExport', params,{ responseType: 'blob' })
}
//超限导出
export const analysisSpeedExport = (params) => {
  return GET('/alarm/speedingExport', params,{ responseType: 'blob' })
}
//稳定性导出
export const analysisFluctuateExport = (params) => {
  return GET('/alarm/stabilityExport', params,{ responseType: 'blob' })
}
//温度导出
export const analysisTemperatureExport = (params) => {
  return GET('/alarm/temperatureExport', params,{ responseType: 'blob' })
}