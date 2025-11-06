import { DEL, GET, POST } from '../request'
// 菜单
export const getPageInfo = (data) => {
  return POST('/menu/getMenuList', data)
}
export const setMenuInfo = (data) => {
  return POST('/menu/updateMenu', data, { preventRepeat: true })
}
export const setMenuEnable = (data) => {
  return POST(`/menu/enableMenu`, data, { preventRepeat: true })
}
// 用户
export const getUserInfo = (data) => {
  return POST('/user/getUserList', data)
}
export const setUserInfo = (data) => {
  return POST('/user/saveUser', data, { preventRepeat: true })
}
export const delUserInfo = (params) => {
  return GET(`/user/delUser`, params)
}
export const getUser = (params) => {
  return GET(`/user/getUserInfo`, params)
}
export const setUserPwd = (data) => {
  return POST(`/user/modifyPassword`, data, { preventRepeat: true })
}
export const restUser = (params) => {
  return GET(`/user/resetUser`, params)
}
// 角色
export const getRoleInfo = (data) => {
  return POST('/role/getRoleList', data)
}
export const setRoleInfo = (data) => {
  return POST('/role/saveRole', data, { preventRepeat: true })
}
export const delRoleInfo = (params) => {
  return GET(`/role/delRole`, params)
}
// 组织
export const getOrgInfo = (data) => {
  return GET('/org/getOrgList', data)
}
export const getOrgHeader = (params) => {
  return GET('/org/getOrgs', params)
}
export const setOrgInfo = (data) => {
  return POST('/org/saveOrg', data, { preventRepeat: true })
}
export const delOrgInfo = (params) => {
  return DEL(`/org/delOrg?orgId=${params}`)
}

export const getSiteInfo = (data) => {
  return POST('/site/getSiteList', data)
}
// 新建风机
export const setSiteInfo = (data) => {
  return POST('/site/saveSite', data, { preventRepeat: true })
}
export const delSiteInfo = (params) => {
  return DEL(`/site/delSite?id=${params}`)
}
export const getAllSite = () => {
  return GET(`/site/getAllSite`)
}
// 获取单个机组信息
export const getSite = (params) => {
  return GET(`/site/getSite`, params)
}

// 获取日志
export const getLog = (params) => {
  return GET(`/operlog/list`, params)
}
// 获取硬件列表
export const getHardwareList = (params) => {
  return GET(`/hardware/list`, params)
}
// 更新硬件列表
export const getHardwareUpdate = (data) => {
  return POST(`/hardware/update`, data)
}

