export const handleCfmIp = (rules, value, callback) => {
  let ipRegex =
    /^(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
  if (value && !ipRegex.test(value)) {
    callback(new Error('ip格式不正确'))
  } else {
    callback()
  }
}
export const checkMacAddress = (rules, value, callback) => {
  let ipRegex = /^(([A-Fa-f0-9]{2}-){5}[A-Fa-f0-9]{2})|(([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2})$/
  let regexp = new RegExp(ipRegex)
  if (value && !regexp.test(value)) {
    callback(new Error('Mac地址格式不正确'))
  } else {
    callback()
  }
}
export const handleCfmProt = (rules, value, callback) => {
  let protRegex = /^\d{1,5}$/

  if (value && !protRegex.test(value)) {
    callback(new Error('端口格式不正确'))
  } else {
    if (Number(value) && Number(value) <= 65535) {
      callback()
    } else {
      callback(new Error('端口格式不正确'))
    }
  }
}
