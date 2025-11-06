import { POST, GET, DEL } from '../request'

// 查询筛选风机场下的所有机位号
export const getSiteListByOrgId = (params) => {
  return GET('/site/getSiteListByOrgId', params)
}

export const getHardwareTree = (params) => {
    return GET('/hardware/getHardwareTree', params)
  }
export const getHardwareTreeTemp = (params) => {
  return GET('/hardware/getHardwareTreeTemp', params)
}
  