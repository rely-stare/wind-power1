<template>
  <div class="page">
    <div class="grid-box">
      <div>
        <a-row align="middle" justify="space-between" class="confItem-title">
          <a-col>
            <h3>语言</h3>
          </a-col>
          <a-col>
            <a-button
              type="primary"
              ghost
              class="editBtn"
              @click="edit('languageDis')"
              v-if="disabledList.languageDis"
              >编辑</a-button
            >
            <a-button v-else type="primary" ghost class="infoBtn" @click="saveLanguage"
              >保存</a-button
            >
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="12">
            <a-form ref="languageRef" :model="languageState">
              <a-form-item
                ref="language"
                label="语言"
                name="language"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-radio-group
                  v-model:value="languageState.language"
                  :disabled="disabledList.languageDis"
                >
                  <a-radio value="zh_CN">中文</a-radio>
                  <a-radio value="zn">英文</a-radio>
                </a-radio-group>
              </a-form-item>
            </a-form>
          </a-col>
        </a-row>
      </div>
      <div>
        <a-row align="middle" justify="space-between" class="confItem-title">
          <a-col>
            <h3>登录页</h3>
          </a-col>
          <a-col>
            <a-button
              type="primary"
              ghost
              class="editBtn"
              @click="edit('loginDis')"
              v-if="disabledList.loginDis"
              >编辑</a-button
            >
            <a-button v-else type="primary" ghost class="infoBtn" @click="loginSave">保存</a-button>
          </a-col>
        </a-row>
        <a-form ref="loginRef" :model="loginState">
          <a-row>
            <a-col :span="12">
              <a-form-item
                label="Logo"
                name="logo"
                :rules="[
                  {
                    required: true,
                    message: '请上传Logo',
                    trigger: 'change'
                  }
                ]"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <Upload
                  :disabled="disabledList.loginDis"
                  :fieldName="'logo'"
                  :uploadSuccess="uploadSuccess"
                  :initFiles="[loginState.logo]"
                ></Upload>
                <span class="tips tip"
                  >格式支持<span>jpg</span>
                  、<span>jpeg</span>、<span>png</span>,尺寸建议为<span>200*46</span>,大小不超过<span
                    >5M</span
                  ></span
                >
              </a-form-item>
              <a-form-item
                label="密码有效时长"
                name="pwdRememberTime"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
                :rules="[
                  {
                    required: true,
                    message: '请输入密码有效时间',
                    trigger: 'blur'
                  },
                  {
                    validator: (rules, value, callback) => {
                      pwdTime(rules, value, callback)
                    },
                    trigger: 'change'
                  }
                ]"
              >
                <a-input
                  v-model:value="loginState.pwdRememberTime"
                  :disabled="disabledList.loginDis"
                />
                <span class="tips">天</span>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="系统名称"
                name="sysName"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
                :rules="[
                  {
                    required: true,
                    message: '请输入系统名称',
                    trigger: 'blur'
                  }
                ]"
              >
                <a-input
                  v-model:value="loginState.sysName"
                  :disabled="disabledList.loginDis"
                  :maxlength="15"
                />
              </a-form-item>
              <a-form-item
                label="登陆有效时长"
                name="loginTime"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-input
                  v-model:value="loginState.loginTime"
                  :disabled="disabledList.loginDis"
                /><span class="tips">分钟</span>
              </a-form-item>
              <a-form-item
                label="是否启用验证码"
                name="verifySwitch"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-radio-group
                  v-model:value="loginState.verifySwitch"
                  :disabled="disabledList.loginDis"
                >
                  <a-radio value="0">不启用</a-radio>
                  <a-radio value="1">启用</a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div>
        <a-row align="middle" justify="space-between" class="confItem-title">
          <a-col>
            <h3>顶部状态栏</h3>
          </a-col>
          <a-col>
            <a-button
              type="primary"
              ghost
              class="editBtn"
              @click="edit('topStatus')"
              v-if="disabledList.topStatus"
              >编辑</a-button
            >
            <a-button v-else type="primary" ghost class="infoBtn" @click="topSave">保存</a-button>
          </a-col>
        </a-row>
        <a-form ref="topRef" :model="topState">
          <a-row>
            <a-col :span="12">
              <a-form-item
                label="Logo"
                name="topLogo"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
                :rules="[
                  {
                    required: true,
                    message: '请上传Logo',
                    trigger: 'change'
                  }
                ]"
              >
                <Upload
                  :disabled="disabledList.topStatus"
                  :fieldName="'topLogo'"
                  :uploadSuccess="uploadSuccess"
                  :initFiles="[topState.topLogo]"
                ></Upload
                ><span class="tips tip"
                  >格式支持<span>jpg</span>
                  、<span>jpeg</span>、<span>png</span>,尺寸建议为<span>200*46</span>,大小不超过<span
                    >5M</span
                  ></span
                >
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="系统名称"
                name="topSysName"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
                :rules="[
                  {
                    required: true,
                    message: '请输入系统名称',
                    trigger: 'blur'
                  }
                ]"
              >
                <a-input
                  v-model:value="topState.topSysName"
                  :disabled="disabledList.topStatus"
                  :maxlength="15"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div>
        <a-row align="middle" justify="space-between" class="confItem-title">
          <a-col>
            <h3>任务优先级 <span class="tips">数字越大，优先级越高(4级最高,1级最低)</span></h3>
          </a-col>
          <a-col>
            <a-button
              type="primary"
              ghost
              class="editBtn"
              @click="edit('taskDis')"
              v-if="disabledList.taskDis"
              >编辑</a-button
            >
            <a-button v-else type="primary" ghost class="infoBtn" @click="taskSave">保存</a-button>
          </a-col>
        </a-row>
        <a-form ref="taskRef" :model="taskState" :rules="rules">
          <a-row>
            <a-col :span="12">
              <a-form-item
                label="主辅设备联动"
                name="deviceLinkage"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-input-number
                  v-model:value="taskState.deviceLinkage"
                  :disabled="disabledList.taskDis"
                  :step="1"
                  :min="1"
                  :max="4"
                />
              </a-form-item>
              <a-form-item
                label="现场/区域巡视主机控制"
                name="hostControl"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-input-number
                  v-model:value="taskState.hostControl"
                  :disabled="disabledList.taskDis"
                  :step="1"
                  :min="1"
                  :max="4"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="上级系统控制"
                name="superiorControl"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-input-number
                  v-model:value="taskState.superiorControl"
                  :disabled="disabledList.taskDis"
                  :step="1"
                  :min="1"
                  :max="4"
                />
              </a-form-item>
              <a-form-item
                label="站端日常巡视任务"
                name="dailyTask"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-input-number
                  v-model:value="taskState.dailyTask"
                  :disabled="disabledList.taskDis"
                  :step="1"
                  :min="1"
                  :max="4"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
    </div>
    <!-- </a-form> -->
  </div>
</template>
<script setup>
import { setConfigInfo, getConfigAllInfo } from '@/api/systemConf/index.js'
import { useSysStore } from '@/stores/system.js'
import { message } from 'ant-design-vue'
import { storeToRefs } from 'pinia'
import { ref } from 'vue'
// 回显系统配置信息
let sysStore = useSysStore()
let { sysConfig } = storeToRefs(sysStore)
let languageState = ref({})
languageState.value.language = sysConfig.value.language
// 登录信息
let loginState = ref({})
loginState.value = {
  pwdRememberTime: sysConfig.value.pwdRememberTime,
  logo: sysConfig.value.logo,
  sysName: sysConfig.value.sysName,
  loginTime: sysConfig.value.loginTime,
  verifySwitch: sysConfig.value.verifySwitch
}
// 顶部信息
let topState = ref({})
topState.value = {
  topLogo: sysConfig.value.topLogo,
  topSysName: sysConfig.value.topSysName
}
// 任务信息
let taskState = ref({})
taskState.value = {
  deviceLinkage: sysConfig.value.deviceLinkage,
  hostControl: sysConfig.value.hostControl,
  superiorControl: sysConfig.value.superiorControl,
  dailyTask: sysConfig.value.dailyTask
}

let logoList = ref({})
// 定义各个模块是否disabled
const disabledList = ref({
  languageDis: true,
  loginDis: true,
  topStatus: true,
  taskDis: true
})
// 编辑按钮
const edit = (name) => {
  disabledList.value[name] = false
}
const rules = {
  deviceLinkage: [
    {
      required: true,
      message: '请输入设备联动优先级',
      trigger: 'blur'
    },
    {
      validator: (rules, value, callback) => {
        priority(rules, value, callback)
      },
      trigger: 'change'
    }
  ],
  hostControl: [
    {
      required: true,
      message: '请输入主机控制优先级',
      trigger: 'blur'
    },
    {
      validator: (rules, value, callback) => {
        priority(rules, value, callback)
      },
      trigger: 'change'
    }
  ],
  superiorControl: [
    {
      required: true,
      message: '请输入上级系统控制优先级',
      trigger: 'blur'
    },
    {
      validator: (rules, value, callback) => {
        priority(rules, value, callback)
      },
      trigger: 'change'
    }
  ],
  dailyTask: [
    {
      required: true,
      message: '请输入站端日常巡视任务优先级',
      trigger: 'blur'
    },
    {
      validator: (rules, value, callback) => {
        priority(rules, value, callback)
      },
      trigger: 'change'
    }
  ]
}

const pwdTime = (rules, value, callback) => {
  let regex = /^\d+$/
  if (value && !regex.test(value)) {
    callback(new Error('请输入正整数'))
  } else {
    callback()
  }
}
const priority = (rules, value, callback) => {
  let arr = [
    taskState.value.dailyTask + '',
    taskState.value.hostControl + '',
    taskState.value.deviceLinkage + '',
    taskState.value.superiorControl + ''
  ].filter((item) => item + '')
  if (hasDuplicates(arr)) {
    callback(new Error('优先级不可重复'))
  } else {
    callback()
  }
}
const hasDuplicates = (array) => {
  return array.length !== new Set(array).size
}
const languageRef = ref(null)
// 语言保存
const saveLanguage = () => {
  languageRef.value
    .validate()
    .then(() => {
      onSubmit(languageState.value, 'languageDis')
    })
    .catch((err) => {
      console.log(err, 'err')
    })
}
// 登录保存
const loginRef = ref(null)
const loginSave = () => {
  loginState.value.logo = logoList.value.logo || loginState.value.logo
  loginRef.value
    .validate()
    .then(() => {
      onSubmit(loginState.value, 'loginDis')
    })
    .catch((err) => {
      console.log(err, 'err')
    })
}
// 顶部保存
const topRef = ref(null)
const topSave = () => {
  topState.value.topLogo = logoList.value.topLogo || topState.value.topLogo
  topRef.value
    .validate()
    .then(() => {
      onSubmit(topState.value, 'topStatus')
    })
    .catch((err) => {
      console.log(err, 'err')
    })
}
// 任务保存
const taskRef = ref(null)
const taskSave = () => {
  taskRef.value
    .validate()
    .then(() => {
      onSubmit(taskState.value, 'taskDis')
    })
    .catch((err) => {
      console.log(err, 'err')
    })
}
// 表单提交
const onSubmit = (value, fieldName) => {
  message.loading({ content: '提交中...', key: 'userKey' })
  setConfigInfo(value).then((res) => {
    if (res.code == 200) {
      message.success({ content: '操作成功', key: 'userKey', duration: 2 })
      getConfigAllInfo().then((res) => {
        sysStore.setSysConf(res.data)
      })
      disabledList.value[fieldName] = true
    }
  })
}

// 子组件上传成功后的回调
const uploadSuccess = (fieldName, url) => {
  logoList.value[fieldName] = url
}
</script>
<style lang="scss" scoped>
.grid-box {
  display: grid;
  grid-template-columns: 1fr; /* 指定 1 列*/
  grid-template-rows: 1fr; /* 指定 1行 */
  height: 100%;
  > div {
    border-radius: 2px;
    margin: 5px;
    @include background-color('baseFFF008');
    .ant-form-item {
      margin-bottom: 0;
      padding: 12px 0;
      padding-right: 117px;
    }
  }
}
.confItem-title {
  line-height: 48px;
  height: 48px;
  padding: 0 24px;
  font-size: 16px;
  color: #00d1ff;
  @include background-color('baseFFF008');
  h3 {
    font-size: 16px;
  }
}
.tips {
  font-size: 14px;
  @include font_color('baseBFBFBF');
  line-height: 36px;
  white-space: nowrap;
  margin-left: 8px;
  > span {
    @include font_color('base00D1FF');
  }
}
.tip {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
</style>
