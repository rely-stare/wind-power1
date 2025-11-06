import { POST, GET, DEL } from '../request'
// 转速数据
export const getSpeedSensorList = (params) => {
  return GET('/speed.data/list', params)
}
// 转速数据导出
export const getSpeedSensorExport = (data) => {
  return POST('/speed.data/export', data, { responseType: 'blob' })
}
// 红外数据
export const getVoiceSensorList = (params) => {
  return GET('/infrared.data/list', params)
}
// 红外数据导出
export const getVoiceSensorExport = (data) => {
  return POST('/infrared.data/export', data, { responseType: 'blob' })
}
// 声音数据
export const getVoiceNvrList = (params) => {
  return GET('/voice.data/list', params)
}
// 声音数据导出
export const getVoiceNvrExport = (data) => {
  return POST('/voice.data/export', data, { responseType: 'blob' })
}
// 视频数据
export const getCameraList = (params) => {
  return GET('/camera.data/list', params)
}
// 视频数据导出
export const getCameraExport = (data) => {
  return POST('/camera.data/export', data, { responseType: 'blob' })
}
// 获取视频列表

export const getVedioList = (params) => {
  return GET('/video/list', params)
}
// 获取视频流
export const getVideoStream = (params) => {
  return GET('video/getVideoStream',params)
}
//获取回放视频流
export const getVideoStreamHis = (params) => {
  return GET('/video/getVideoStreamHis', params)
}
// 热成像数据
export const getTemperatureChart = (params) => {
  return GET('/ti/getTemperatureChart', params)
}
// 获取音频列表
export const getAudioList = (params) => {
  return GET('/audio/list', params)
}
// 获取音频流
export const getAudioStream = (params) => {
  return GET(`/audio/stream/${params}`,{},{responseType:'blob'})
}
// 获取实时音频流

export const getAudioStreamLive = (params) => {
  return GET(`/audio/live`,params)
}
// 获取长时温度
export const getLongTemperatureChart = (params) => {
  return GET(`/ti/getLongTemperatureChart`,params)
}

// 获取频谱
export const getFftChart = (params) => {
  return GET(`/speed/FFT/Chart`,params)
}
// 获取频谱
export const getFftChart2 = (params) => {
  return GET(`/alarm/getSpeedData`,params)
}
// 获取超限阈值
export const getSpeedLimit = (params) => {
  return GET(`/speed/limit`,params)
}
// 获取最新频谱触发记录
export const getLastFFTActive = (params) => {
  return GET(`/speed/getLastFFTActive`,params)
}
// 获取 FFT 触发记录列表
export const getFFTActive = (params) => {
  return GET(`/speed/getFFTActive`,params)
}
// 根据id获取FFT
export const getFTActiveById = (params) => {
  return GET(`/speed/getFTActiveById`,params)
}
// 超限导出
export const speedExport = (params) => {
  return GET(`/dataCenter/speedExport`,params)
}
// 稳定性导出
export const speedStabilityExport = (params) => {
  return GET(`/dataCenter/speedStabilityExport`,params)
}
// 震荡导出
export const speedFluctuationExport = (params) => {
  return GET(`/dataCenter/speedFluctuationExport`,params)
}
// 频谱导出
export const speedSpectrumExport = (params) => {
  return GET(`/dataCenter/speedSpectrumExport`,params)
}
// 视频，热成像下载
export const getVideoDownload = (params) => {
  return GET(`/dataCenter/videoDownload`,params)
}
