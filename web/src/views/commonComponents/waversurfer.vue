<!-- 可视化音频组件 -->
<template>
  <div>
    <a-spin :spinning="tableLoading" style="text-align: center;">
      <div id="waveform" ref="waveform"></div>
    </a-spin>

  </div>
</template>

<script setup>
import WaveSurfer from 'wavesurfer.js'
import Spectrogram from 'wavesurfer.js/dist/plugins/spectrogram.esm.js'
import { getAudioStream } from '@/api/dataCenter/index.js'
import { onMounted, onBeforeUnmount, ref } from 'vue'
const props = defineProps({
  height: {
    type: String,
    default: '60'
  },
  fileID: {
    type: String,
    default: ''
  }
})
let waveform = ref()
let wavesurfer = ref() //wavesurfer实例
let tableLoading = ref(false)
const currentTime = ref(0);
const duration = ref(0);
onMounted(() => {
  console.log('音频组件')
  loadWave()
})
// 可选：监听 rtspUrl 变化
watch(() => props.fileID, (newFileID) => {
  console.log('初始化音频1', newFileID)
  if (newFileID) {
    console.log('初始化音频')
    loadWave()
  }
})
// 监听 currentTime 的变化，以便在拖动进度条时更新波形播放位置
watch(currentTime, (newVal) => {
  if (wavesurfer.value.isPlaying()) {
    wavesurfer.value.pause();
  }
  wavesurfer.value.seekTo(newVal);
});
const loadWave = () => {
  tableLoading.value = true
  console.log('音频流加载', props.fileID)
  getAudioStream(props.fileID).then(res => {
    try {
      console.log('音频流')
      const blob = new Blob([res], { type: 'audio/wav' }); // 确保指定正确的 MIME 类型
      const audioURL = URL.createObjectURL(blob);
      if (wavesurfer.value) {
        wavesurfer.value.destroy();
      }

      wavesurfer.value = WaveSurfer.create({
        container: waveform.value,
        // backend: 'WebAudio',
        waveColor: '#00FF00',
        //波纹宽度
        // Set a bar width
        // barWidth: 2,
        // Optionally, specify the spacing between bars
        // barGap: 1,
        // And the bar radius
        // barRadius: 2,
        //光标的填充颜色，指示播放头的位置
        cursorColor: '#00D1FF',
        //光标后面的波形部分的填充色.
        progressColor: '#7CFC00',
        mediaControls: true,
      });
      wavesurfer.value.registerPlugin(
        Spectrogram.create({
          labels: true,
          height: 260,
          splitChannels: true,
          scale: 'mel', // or 'linear', 'logarithmic', 'bark', 'erb'
          frequencyMax: 8000,
          frequencyMin: 0,
          fftSamples: 1024,
          labelsBackground: 'rgba(0, 0, 0, 0.1)',
        }),
      )
      wavesurfer.value.on('click', () => wavesurfer.value.playPause())
      wavesurfer.value.load(audioURL);
      wavesurfer.value.on('finish', () => {
        URL.revokeObjectURL(audioURL);
      });
      tableLoading.value = false
    } catch (error) {
      console.error('在处理音频流时发生错误:', error);
      // 可以在这里添加额外的错误处理逻辑
    }
  }).catch(err => {
    console.log('错误')
  })

};
onBeforeUnmount(() => {
  if (wavesurfer.value) {
    wavesurfer.value.destroy();
  }
});
</script>

<style scoped>
#waveform {
  height: 80px !important;
  background-color: transparent;
  width: 100%;
}

#waveform audio {
  height: 25px !important;
  margin-top: 10px;
}
</style>
