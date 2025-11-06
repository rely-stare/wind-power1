<template>
  <div class="loginBox">
    <div class="logBox">
      <img :src="config.logo" alt="" />
    </div>
    <p class="loginTitle">{{ config.sysName }}</p>
    <div class="login">
      <a-form :model="formState" name="normal_login" class="login-form" @finish="onFinish">
        <a-form-item name="username" :rules="[{ required: true, message: '请输入用户名!' }]">
          <a-input v-model:value="formState.username" placeholder="请输入用户名">
            <template #prefix>
              <i class="iconfont guanliyuan"></i>
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="password" :rules="[{ required: true, message: '请输入密码!' }]">
          <a-input-password v-model:value="formState.password" placeholder="请输入密码">
            <template #prefix>
              <i class="iconfont mima"></i>
            </template>
          </a-input-password>
        </a-form-item>
        <a-form-item name="captcha" :rules="[{ required: true, message: '请输入验证码!' }]" v-if="config.verifySwitch == 1">
          <div class="codeFm">
            <a-input v-model:value="formState.captcha" placeholder="请输入验证码" class="code">
              <template #prefix>
                <i class="iconfont yanzhengma"></i>
              </template>
            </a-input>
            <img :src="code" alt="" @click="getCode" />
          </div>
        </a-form-item>

        <a-form-item class="loginBtn">
          <a-button :disabled="disabled" type="primary" html-type="submit" class="login-form-button">
            登录
          </a-button>
          <a-button :disabled="disabled" type="primary" @click="reset" class="login-form-button">
            重置
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script setup>
import { getLoginInfo, getCaptcha } from '@/api/login/index.js'
import { getConfigInfo, getConfigAllInfo, getDictInfo } from '@/api/systemConf/index.js'
import { getUser, getOrgHeader } from '@/api/system/index.js'
import { useSysStore } from '@/stores/system.js'
import { useUserStore } from '@/stores/user.js'
import { useDictStore } from '@/stores/dict.js'
import { computed, onMounted } from 'vue'
import $X from '@/utils/$X.js'
const router = useRouter()

let sysConf = useSysStore()
let dict = useDictStore()
let user = useUserStore()

const config = ref({})

const formState = reactive({
  username: '',
  password: '',
  formState: ''
})
const onFinish = (values) => {
  values.password = $X.Encrypt(values.password, 'AyYkmAVZ6XsUes0ICjq5TA==')
  let ip = window.location.hostname.includes('localhost') ? '127.0.0.1' : window.location.hostname
  getLoginInfo(values, ip).then((res) => {
    sessionStorage.setItem('token', res.data)

    getConfigAllInfo().then((res) => {
      sysConf.setSysConf(res.data)
    })
    getUserInfo()
    getDict()
    getSiteList()
    user.setUserMenu()
    router.push('/')
  })
}
let code = ref(null)
const getCode = () => {
  getCaptcha().then((res) => {
    code.value = base64ToImage(res.data)
  })
}
// 获取所有机组
const getSiteList = () => {
  getOrgHeader({ orgType: 1 }).then((res) => {
    sysConf.setSiteList(res.data)
    sysConf.setSiteId(res.data[0].id)
  })
}
const base64ToImage = (base64Str) => {
  let binary = atob(base64Str)
  let len = binary.length
  let buffer = new ArrayBuffer(len)
  let view = new Uint8Array(buffer)

  for (let i = 0; i < len; i++) {
    view[i] = binary.charCodeAt(i)
  }

  let blob = new Blob([view], { type: 'image/png' }) // 或者其他图片类型
  return URL.createObjectURL(blob)
}
const disabled = computed(() => {
  return !(formState.username && formState.password)
})
// 获取用户信息
const getUserInfo = () => {
  getUser().then((res) => {
    user.setUserInfo(res.data)
  })
}
// 获取字典表
const getDict = () => {
  getDictInfo().then((res) => {
    dict.setDict(res.data)
  })
}
// 重置
const reset = () => {
  formState.username = ''
  formState.password = ''
  formState.code = ''
}
onMounted(() => {
  getConfigInfo().then((res) => {
    config.value = res.data
    sysConf.setTheme(res.data.theme)
    if (config.value.verifySwitch == 1) {
      getCode()
    }
  })
})
</script>
<style lang="scss" scoped>
@font-face {
  font-family: 'ALiHanYiZhiNengHeiTi-2';
  src: url('../../assets/typeface/ALiHanYiZhiNengHeiTi-2.ttf');
}

.loginBox {
  position: relative;
  height: 100%;
  width: 100%;
  background-image: url('@/assets/img/public/loginBg.png');
  background-size: 100% 100%;
  background-repeat: no-repeat;
  background-position: center center;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;

  .loginTitle {
    font-size: 40px;
    @include font_color('baseFFF');
    line-height: 56px;
    margin-bottom: 28px;
    font-family: 'ALiHanYiZhiNengHeiTi-2';
  }

  .login {
    padding: 106px 81px 66px;
    width: 603px;
    height: 454px;
    background-image: url('@/assets/img/public/loginBox.png');
    background-size: 100% 100%;
    background-repeat: no-repeat;
    background-position: center center;

    .ant-input-affix-wrapper {
      border-radius: 4px !important;
      height: 48px;
      @include background-color('base293F4C08');
      @include border-color('base293F4C08');
    }

    .ant-input-password {
      @include background-color('base293F4C08');
      @include border-color('base293F4C08');
    }

    .loginBtn {
      display: flex;
      justify-content: center;
      align-items: center;
      flex-wrap: nowrap;

      .ant-btn {
        line-height: 10px;
        height: 48px;
        font-size: 14px;

        &:first-child {
          margin-right: 16px;
          @include background-color('base014454');
          @include font_color('baseFFF');
          @include border-color('base00D1FF');
        }

        &:last-child {
          @include background-color('base2A4854');
          @include font_color('baseFFF');
          @include border-color('baseCCF6FF');
        }

        padding: 13px 48px;
      }
    }

    .codeFm {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .code {
        width: 280px;
      }

      img {
        width: 144px;
        height: 48px;
        display: inline-block;
        margin-left: 17px;
        border-radius: 6px;
        cursor: pointer;
      }
    }
  }

  .logBox {
    position: absolute;
    top: 81px;
    left: 81px;

    img {
      max-height: 48px;
      display: block;
    }
  }
}
</style>
