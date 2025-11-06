<template>
  <div class="page-box">
    <!-- 左侧机号功能筛选项 -->
    <Menu :isMontorFlag="true" :defaultKey="false" :hardwareDefaultKey="false" @changeView="changeComp"></Menu>
    <!-- 右侧内容区域 -->
    <div class="page-content">
      <!-- <span style="color:#fff">{{ liveData }}{{ fanFlag }}{{ hardwareListFlag }}{{ hardwareDetialFlag }}{{
        hardwareItemsFlag }}</span> -->
      <Fan @changeComp="changeComp" v-if="fanFlag" />
      <HardwareList v-if="hardwareListFlag" @selectHaredware="selectHaredware" :hardwareListFlag="hardwareListFlag" />
      <HardwareDetial v-if="hardwareDetialFlag" :liveData="liveData" />
      <HardwareItems v-if="hardwareItemsFlag" />
      <AlarmList v-if="fanFlag || hardwareDetialFlag" ref="alarmList" />
    </div>
  </div>
</template>
<script setup>
import Menu from '@/views/commonComponents/menu.vue'
import Fan from './component/fan.vue'
import AlarmList from './component/alarmList.vue'
import HardwareList from './component/hardwareList.vue'
import HardwareDetial from './component/hardwareDetial.vue'
import HardwareItems from './component/hardwareItems.vue'
import { useSiteStore } from '@/stores/site.js'
import { storeToRefs } from 'pinia'
import { onMounted } from 'vue'

let siteConf = useSiteStore()
let fanFlag = ref(true)
let hardwareDetialFlag = ref(false)
let hardwareItemsFlag = ref(false)
let hardwareListFlag = ref(false)
const { fanId, hardwareId } = storeToRefs(siteConf)
let liveData = ref()
let alarmList = ref()

// 选中某个风机
const changeComp = () => {
  console.log(fanId.value, hardwareId.value, '风机号硬件号')
  fanFlag.value = false
  hardwareDetialFlag.value = false
  hardwareItemsFlag.value = false
  hardwareListFlag.value = false
  // console.log('跳转了吗')
  if (fanId.value) {
    if (hardwareId.value) {
      hardwareDetialFlag.value = true
    } else {
      hardwareListFlag.value = true
    }
  } else {
    if (hardwareId.value) {
      hardwareItemsFlag.value = true
    } else {
      fanFlag.value = false
    }
  }
  liveData.value = {}
}
// 选中某个风机的某个硬件
const selectHaredware = (detialItem) => {
  changeComp()
  // hardwareDetialFlag.value = true
  liveData.value = detialItem
}
// watch(() => fanId.value, (oldFanId, newFanId) => {
//   if (oldFanId != newFanId) {
//     fanFlag.value = true
//     hardwareDetialFlag.value = false
//     hardwareItemsFlag.value = false
//   }
// })
// watch(() => hardwareId.value, (oldHardwareIdId, newHardwareIdId) => {
//   if (oldHardwareIdId != newHardwareIdId) {
//     // 当机位号不为空
//     if (fanId.value) {
//       hardwareDetialFlag.value = true
//     } else {
//       //机位号为空，展示所有机位号的该硬件列表
//       hardwareItemsFlag.value = true
//     }


//   }
// })
</script>
<style lang="scss" scope>
.page-box {
  display: flex;
  height: 100%;
}

.page-content {
  margin-left: 16px;
  width: calc(100% - 296px);
  display: flex;
}
</style>
