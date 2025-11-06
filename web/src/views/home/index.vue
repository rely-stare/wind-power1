<template>
  <a-layout class="content">
    <Header></Header>
    <a-layout>
      <Sider></Sider>
      <a-layout-content class="main-center">
        <Crumb></Crumb>
        <AppMain v-if="showMain"></AppMain>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
<script setup>
import { useSysStore } from '@/stores/system.js'
import { storeToRefs } from 'pinia'
let sysConf = useSysStore()
const showMain = computed(() => sysConf.showMain)
// const { showMain } = storeToRefs(sysConf)
</script>

<style lang="scss" scoped>
.content {
  .main-center {
    height: 100%;
    // overflow: hidden;
    // overflow-y: auto;
    padding: 15px;
  }
}
</style>
