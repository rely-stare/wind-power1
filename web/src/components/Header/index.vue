<template>
  <a-layout-header class="main-header">
    <a-row align="middle" justify="center">
      <a-col :span="3" class="headerLeft">
        <img :src="sysConfInfo.topLogo" alt="" class="topLog" />
        <a-select ref="select" v-model:value="siteId" class="areaSelect" v-if="!areaShow" @change="changeArea">
          <a-select-option v-for="item in siteList" :key="item.id" :value="item.id">{{
            item.orgName
          }}</a-select-option>
        </a-select>
      </a-col>
      <a-col :span="6" class="header-left">
        <MenuList :menuList="leftRoutes"></MenuList>
      </a-col>
      <a-col :span="6" class="sysTitle">
        <!-- <router-link to="/overview"> {{ sysConfInfo.topSysName }}</router-link> -->
        <a href="javascript:;" @click="goOverview">{{ sysConfInfo.topSysName }}</a>
        <!-- <router-link to="/overview"> {{ sysConfInfo.topSysName }}</router-link> -->
      </a-col>
      <a-col :span="6" class="header-right">
        <MenuList :menuList="rightRoutes"></MenuList>
      </a-col>
      <a-col :span="1">
        <div class="largeScreen" v-if="user.largeScreen">
          <router-link to="/overview"> 大屏展示</router-link>
        </div>
      </a-col>
      <a-col :span="2" class="header-Iocn">
        <a-space :size="15">
          <router-link to="/system" v-if="user.sysShow">
            <a-tooltip placement="top">
              <template #title>系统管理</template>
              <i class="iconfont xitongguanli"></i>
            </a-tooltip>
          </router-link>

          <a-dropdown arrow :trigger="['click']">
            <a-tooltip placement="top">
              <template #title>个人信息</template>
              <i class="iconfont guanliyuan" style="width:100px;height:100px;"> </i>
            </a-tooltip>
            <template #overlay>
              <a-menu>
                <a-menu-item>
                  <i class="iconfont guanliyuan"></i> <span>{{ userInfo.userName }}</span>
                </a-menu-item>
                <a-menu-item @click="upPwd">
                  <i class="iconfont mima"></i> <span>修改密码</span>
                </a-menu-item>
                <a-menu-item @click="quit">
                  <i class="iconfont tuichu"></i> <span>退出</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </a-col>
    </a-row>
    <a-modal v-model:open="pwdModal" width="none" title="修改密码" @cancel="onPwdClose" :centered="true">
      <div class="formBox">
        <a-form :model="formState" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" :rules="rules" ref="formRef">
          <p v-if="!isCanClose" class="pwdTip">提示：密码已过期，请修改密码！！！</p>
          <a-form-item label="原始密码" name="historyPassword">
            <a-input-password v-model:value="formState.historyPassword" />
          </a-form-item>

          <a-form-item label="新密码" name="newPassword">
            <a-input-password v-model:value="formState.newPassword" />
          </a-form-item>
          <a-form-item label="确认密码" name="confirmPassword">
            <a-input-password v-model:value="formState.confirmPassword" />
          </a-form-item>
        </a-form>
      </div>
      <template #footer>
        <a-row justify="center">
          <a-button type="primary" class="primary" @click="onSubmit">提交</a-button>
          <a-button @click="onClose" class="info">取消</a-button>
        </a-row>
      </template>
    </a-modal>
  </a-layout-header>
</template>
<script setup>
import { loginOut } from '@/api/login/index.js'
import { setUserPwd } from '@/api/system/index.js'
import { useSysStore } from '@/stores/system.js'
// import allRoutes from '@/router/allRoutes'
import { useUserStore } from '@/stores/user.js'
import $X from '@/utils/$X.js'
import { message } from 'ant-design-vue'
import { storeToRefs } from 'pinia'
const router = useRouter()
const route = useRoute()
let user = useUserStore()
let allRoutes = ref(user.userMenu)
let leftRoutes = allRoutes.value.slice(0, Math.ceil(allRoutes.value.length / 2))
let rightRoutes = allRoutes.value.slice(Math.ceil(allRoutes.value.length / 2))
let sysConf = useSysStore()

const sysConfInfo = computed(() => sysConf.sysConfig)
let areaShow = ref(true)
watch(
  route,
  () => {
    areaShow.value = route.matched.some((item) => item.path == '/system')
  },
  {
    immediate: true
  }
)
const quit = () => {
  loginOut().then((res) => {
    sessionStorage.removeItem('token')
    sessionStorage.clear()
    location.reload()
    router.push('/login')
  })
}
const rules = {
  historyPassword: [{ required: true, message: '请输入原始密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (rules, value, callback) => {
        handleNewPwd(rules, value, callback)
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
    {
      validator: (rules, value, callback) => {
        handleCfmPwd(rules, value, callback)
      },
      trigger: 'blur'
    }
  ]
}

//确认密码校验一致
const handleCfmPwd = (rules, value, callback) => {
  if (formState.value.newPassword && value && formState.value.newPassword !== value) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
let userInfo = computed(() => user.userInfo)
const handleNewPwd = (rules, value, callback) => {
  let regex =
    /^(?:(?=.*[0-9])(?=.*[a-zA-Z])|(?=.*[0-9])(?=.*[^0-9a-zA-Z])|(?=.*[a-zA-Z])(?=.*[^0-9a-zA-Z])).{8,}$/

  if (value && !regex.test(value)) {
    callback(new Error('最小8位，至少字母、数字、特殊符号两种'))
  } else if (regex.test(value) && value == formState.value.historyPassword) {
    callback(new Error('新密码不能与旧密码相同！'))
  } else if (value && value.includes(userInfo.value.userName)) {
    callback(new Error('新密码不能包含用户名！'))
  } else {
    callback()
  }
}
//修改密码
const { pwdModal, isCanClose, siteList, siteId, showMain } = storeToRefs(sysConf)
const changeArea = () => {
  showMain.value = false
  nextTick(() => (showMain.value = true))

}

let formRef = ref(null)
let formState = ref({
  historyPassword: '',
  newPassword: '',
  confirmPassword: ''
})
// 关闭弹窗
const onPwdClose = () => {
  formRef.value.clearValidate()
  console.log('onPwdClose', isCanClose.value)
  return new Promise((resolve, reject) => {
    if (!isCanClose.value) {
      message.warning({ content: '密码已过期，请先修改密码！', key: 'pwdKey', duration: 0 })
      pwdModal.value = true
      return
    } else {
      reject()
    }
  }).catch(() => { })
}
// 打开弹窗
const upPwd = () => {
  pwdModal.value = true
  formState.value.historyPassword = null
  formState.value.newPassword = null
  formState.value.confirmPassword = null
}
// 提交密码
const onSubmit = () => {
  formRef.value
    .validate()
    .then(() => {
      let obj = {
        historyPassword: $X.Encrypt(formState.value.historyPassword, 'AyYkmAVZ6XsUes0ICjq5TA=='),
        newPassword: $X.Encrypt(formState.value.newPassword, 'AyYkmAVZ6XsUes0ICjq5TA==')
      }
      message.loading({ content: '提交中...', key: 'userKey' })
      setUserPwd(obj).then((res) => {
        if (res.code == 200) {
          message.success({ content: '操作成功', key: 'userKey', duration: 2 })
          isCanClose.value = true
          quit()
          onClose()
        }
      })
    })
    .catch((error) => {
      console.log('error', error)
    })
}
// 关闭弹窗
const onClose = () => {
  if (!isCanClose.value) {
    message.warning({ content: '密码已过期，请先修改密码！', key: 'pwdKey', duration: 2 })
    return
  }
  formRef.value.resetFields()
  pwdModal.value = false
}
// 跳转首页
const goOverview = () => {
  if (user.homeShow) router.push('/I-Monitor')
}
</script>
<style lang="scss" scoped>
.pwdTip {
  @include font_color('baseFFAD30');
  font-size: 14px;
  margin-bottom: 10px;
}

@font-face {
  font-family: 'ALiHanYiZhiNengHeiTi-2';
  src: url('../../assets/typeface/ALiHanYiZhiNengHeiTi-2.ttf');
}

.main-header {
  background-image: url('../../assets/img/public/header.png') !important;
  background-size: 100% 100%;
  height: 81px;
  min-height: 81px;

  .topLog {
    // width: 110px;
    margin-right: 10px;
    max-height: 48px;
  }

  :deep(.areaSelect) {
    .ant-select-selector {
      background-color: transparent !important;
      border: none !important;
      box-shadow: none !important;
      font-size: 14px;
      @include font_color('baseFFF');
      height: 32px;
      width: 132px;
    }

    .ant-select-selection-item {
      font-size: 14px;
      @include font_color('baseFFF');
    }

    .ant-select-arrow {
      @include font_color('baseFFF');
      font-size: 14px;
    }
  }

  .ant-modal-content {
    height: 500px !important;
  }
}

.ant-dropdown {
  .ant-dropdown-menu-item {
    .ant-dropdown-menu-title-content {
      i {
        margin-right: 10px;
      }
    }
  }
}

.largeScreen {
  background-image: url('../../assets/img/public/menuBtn-active.png');
  background-size: 100% 100%;
  background-repeat: no-repeat;
  height: 30x;
  line-height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 28px;
  margin-left: -40px;
  width: 106px;
  font-size: 14px;

  a {
    @include font_color('baseFFF');
  }
}

.ant-space {
  gap: 15px !important;
}

.header-Iocn {
  .iconfont {
    // color: $text-noActive-color;
    font-size: 16px;
    cursor: pointer;
    background: linear-gradient(to bottom, #fff, #00d1ff);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    line-height: 110px;
  }

  text-align: right;
  padding-right: 17px;
  height: 81px;
}

.headerLeft {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 28px;
  padding-left: 15px;
  position: relative;
}

.sysTitle {
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;

  >a {
    // color: #fff;
    font-family: 'ALiHanYiZhiNengHeiTi-2';
    font-weight: 400;
    font-size: 30px;
    background: linear-gradient(to top, #fff 10%, #00d1ff);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

:deep(.header-left),
:deep(.header-right) {
  .ant-menu {
    height: 81px;
    line-height: 110px;

    .ant-menu-item {
      padding: 0 20px;
      font-size: 16px;
      text-align: center;
      @include font_color('baseFFF');
      transition:
        border-color 0.3s,
        padding 0.3s,
        cubic-bezier(0.645, 0.045, 0.355, 1);

      &::after {
        border-bottom-color: transparent;
        border-bottom-width: 0;
      }
    }

    .ant-menu-item-selected {
      background-image: url('../../assets/img/public/menu-active.png') !important;
      background-position: center 115%;
      background-repeat: no-repeat;
      background-size: 100% auto;

      a {
        background: linear-gradient(to bottom, #00d1ff, #fff);
        background-clip: text;
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
    }

    .ant-menu-item-active {
      // color: $text-Active-color !important;
      @include font_color('baseFFF');
    }
  }
}

:deep(.header-left) {
  .ant-menu {
    display: flex;
    justify-content: flex-end;
  }
}

.primary {
  margin-right: 10px;
}
</style>
