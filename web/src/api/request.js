import axiosService from './config'

export const POST = (url, data = {}, config = {}) => {
  return axiosService({
    method: 'post',
    url,
    data,
    ...config
  })
}
export const GET = (url, params = {}, config = {}) => {
  return axiosService({
    method: 'get',
    params: params,
    url,
    ...config
  })
}
export const PUT = (url, data, config = {}) => {
  return axiosService({
    method: 'put',
    url,
    data,
    ...config
  })
}
export const DEL = (url, params = {}, config = {}) => {
  return axiosService({
    method: 'delete',
    params: params,
    url,
    ...config
  })
}
