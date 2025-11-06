import { POST, GET, DEL } from '../request'
// 转速数据
export const getOrgTree = (params) => {
  return GET('/config/center/getTree', params)
}
// 获取配置中心列表
export const getConfigList = (params) => {
  return GET('/config/center/getRule', params)
}
// 符号字典
export const getDictionary = (params) => {
  return GET('/dictionary/getDictionary', params)
}
// 编辑配置规则
export const getConfigEdit = (data) => {
  return POST('/config/center/edit', data)
}
