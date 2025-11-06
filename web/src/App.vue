<template>
  <a-config-provider :locale="locale">
    <template #renderEmpty>
      <div class="emptyBox">
        <img src="./assets/img/public/listEmpty.png" class="emptyImg" alt="" />
        <p class="tips">暂无数据</p>
      </div>
    </template>
    <div id="app">
        <RouterView />
    </div>
  </a-config-provider>
</template>
<script setup>
// import ScaleBox from "vue3-scale-box";
import enUS from 'ant-design-vue/es/locale/en_US.js'
import zhCN from 'ant-design-vue/es/locale/zh_CN.js'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn.js'
import DevicePixelRatio from "@/utils/zoom"
import { onMounted } from 'vue'
dayjs.locale('zhCN')
const locale = ref(zhCN)
onMounted(() => {
  // const getScale = () => {
  //   const queries = [
  //       '(min-resolution: 1.25dppx)',
  //       '(min-resolution: 1.5dppx)',
  //       '(min-resolution: 2dppx)'
  //   ];
 
  //   for (const query of queries) {
  //       if (window.matchMedia(query).matches) {
  //         const app = document.getElementById('app')
  //         app.style.transform = `scale(1)`
  //           console.log(`Zoom Level detected by media query: ${parseFloat(query.match(/(\d+\.\d+)dppx/)[1])}`);
  //           return;
  //       }
  //   }
  //   console.log('Zoom Level not detected by media queries');
  // }
  // getScale()
  // const changeAppScale = () => {
  //   const body = document.documentElement
  //   const scale1 = body.clientWidth / 1920
  //   const scale2 = body.clientHeight / 1080
  //   const scale = scale1 < scale2 ? scale1 : scale2
  //   const app = document.getElementById('app')
  //   app.style.transform = `scale(0.5+${scale}) translate(-50%, -50%)`
  // }
  
  // changeAppScale()
  // window.addEventListener('resize', changeAppScale)
  //new DevicePixelRatio().init()
  // 处理缩放导致canvas定位异常
  //const size = window.screen.height / 1080;
  const realZoom = document.body.style.zoom;
  document.styleSheets[document.styleSheets.length - 1].insertRule('canvas {zoom: ' + 1 / realZoom + '}');
  document.styleSheets[document.styleSheets.length - 1].insertRule('canvas {transform: scale(' + realZoom + ')}');
  document.styleSheets[document.styleSheets.length - 1].insertRule('canvas {transform-origin: 0 0}');
  
})

</script>
<style lang="scss" scoped>
.emptyBox {
  width: 100%;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  position: relative;

  .tips {
    position: absolute;
    bottom: 25px;
    font-size: 12px;
  }
}

.emptyImg {
  width: 50px;
  position: absolute;
  top: 40px;
}

#app {
  height: 100vh;
  overflow: hidden;
}
</style>
