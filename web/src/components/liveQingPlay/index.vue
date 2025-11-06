<template>
  <div class="liveQingPlay">
    <LivePlayer
      :videoUrl="videoUrl"
      aspect="fullscreen"
      :poster="poster"
      fluent
      autoplay
      live
      stretch
      :controls="false"
      hide-big-play-button
    ></LivePlayer>
  </div>
</template>
<script setup>
import LivePlayer from '@liveqing/liveplayer-v3' // vue3

let poster = new URL('../../assets/img/public/cameraQusheng.png', import.meta.url).href
const props = defineProps({
  videoUrl: {
    type: String,
    default: ''
  }
})
</script>
<style lang="scss" scoped>
.liveQingPlay {
  position: relative;
  height: 100%;
}
</style>
