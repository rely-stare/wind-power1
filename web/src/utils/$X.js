import CryptoJS from 'crypto-js'
const AES_KEY = "AyYkmAVZ6XsUes0ICjq5TA=="; // 必须与后端一致
const decryptRTSP = (encryptedText) => {
  let key = CryptoJS.enc.Utf8.parse(AES_KEY)
    const bytes = CryptoJS.AES.decrypt({ ciphertext: CryptoJS.enc.Base64.parse(encryptedText) },
    key,
    { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 });
    return bytes.toString(CryptoJS.enc.Utf8);
}
// crypto-js aes 解密
const Decrypt = (cipherText, keyInBase64Str) => {
  let key = CryptoJS.enc.Base64.parse(keyInBase64Str)
  // 返回的是一个Word Array Object，其实就是Java里的字节数组
  let decrypted = CryptoJS.AES.decrypt(cipherText, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })

  return decrypted.toString(CryptoJS.enc.Utf8)
}
//加密方法
const Encrypt = (plainText, keyInBase64Str) => {
  let key = CryptoJS.enc.Base64.parse(keyInBase64Str)
  let encrypted = CryptoJS.AES.encrypt(plainText, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  // 这里的encrypted不是字符串，而是一个CipherParams对象
  return encrypted.ciphertext.toString(CryptoJS.enc.Base64)
}
// 扁平数据 转换成树形结构

const buildTree = (data, parentId) => {
  let tree = []
  for (let i = 0; i < data.length; i++) {
    if (data[i].parentId === parentId) {
      let children = buildTree(data, data[i].id)
      if (children.length > 0) {
        data[i].children = children
      }
      tree.push(data[i])
    }
  }
  return tree
}
const setMenu = (arrList) => {
  let newArr = []
  arrList.map((item) => {
    if (
      item.redirect &&
      item.children &&
      item.children.length > 0 &&
      !item.children.some((key) => key.path == item.redirect)
    ) {
      // if (item.some((key) => key.meta.fullPath == item.redirect)) {
      //   newArr.push(item);
      // } else {
      item.redirect = item.children[0].path
      newArr.push(item)
      // }
    } else {
      newArr.push(item)
    }
  })
  return newArr
}
// 导出数据表
const exportExcel = (res, fileName, callback) => {
  let blob = new Blob([res], { type: 'application/msword;charset=UTF-8' })
  let objectUrl = URL.createObjectURL(blob)
  let link = document.createElement('a')
  link.href = objectUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  window.URL.revokeObjectURL(link.href)
  callback && callback()
}
export default {
  decryptRTSP,
  Decrypt,
  Encrypt,
  // changeTree,
  buildTree,
  setMenu,
  exportExcel
}
