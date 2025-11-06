<template>
  <div class="clearfix">
    <a-upload
      v-model:file-list="fileList"
      :maxCount="maxCount"
      list-type="picture-card"
      @preview="handlePreview"
      :customRequest="customRequest"
      :disabled="disabled"
      :beforeUpload="beforeUpload"
      @change="handleChange"
    >
      <div class="icon-wrapper" v-if="fileList.length < maxCount">
        <UploadOutlined />
      </div>
    </a-upload>
    <div style="display: none">
      <a-image-preview-group :preview="{ visible, onVisibleChange: (vis) => (visible = vis) }">
        <a-image :src="previewImage" />
      </a-image-preview-group>
    </div>
  </div>
</template>
<script setup>
import { UploadOutlined } from '@ant-design/icons-vue'
import { setUpload } from '@/api/systemConf/index.js'
import { message } from 'ant-design-vue'
let { maxCount, disabled, fileSize, fieldName, uploadSuccess, initFiles } = defineProps({
  maxCount: {
    type: Number,
    default: 1
  },
  uploadSuccess: {
    type: Function,
    default: () => {}
  },
  fieldName: {
    type: String,
    default: 'file'
  },
  fileSize: {
    type: Number,
    default: 5
  },
  disabled: {
    type: Boolean,
    default: false
  },
  initFiles: {
    type: Array,
    default: () => []
  }
})
onMounted(() => {
  initFileList()
})
const fileList = ref([])
const initFileList = () => {
  initFiles.forEach((item, index) => {
    fileList.value = fileList.value.concat({
      uid: index,
      name: 'img' + index,
      status: 'done',
      url: item
    })
  })
}
const customRequest = (file) => {
  setUpload({ file: file.file }).then((res) => {
    uploadSuccess(fieldName, res.data)
    let item = fileList.value.find((item) => item.uid === file.file.uid)
    if (item) {
      item.status = 'done'
    }
  })
}
let fileType = ['png', 'jpg', 'jpeg']
let errorImg = ref(null)
const beforeUpload = (file) => {
  return new Promise((resolve, reject) => {
    console.log(file, 'file.type')

    if (!fileType.includes(file.type.split('/')[1])) {
      errorImg.value = file
      message.error({ content: `图片格式错误,请重新上传`, key: 'glbKey', duration: 2 })
      reject() // 阻止上传
    }
    if (file.size / 1024 / 1024 > fileSize) {
      errorImg.value = file
      message.error({ content: `图片大小不超过${fileSize}M`, key: 'glbKey', duration: 2 })
      reject() // 阻止上传
    }
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        let realWidth = img.width
        let realHeight = img.height
        console.log(realWidth, realHeight, '88888888888888888888888')
        if (realWidth != 480 || realHeight != 46) {
          console.log('图片尺寸不符合')
        }
      }
      img.src = e.target.result // preview image before upload
    }
    reader.readAsDataURL(file)
    resolve()
  })
}

const handleChange = (file) => {
  if (errorImg.value) {
    fileList.value = fileList.value.filter((item) => item.uid !== errorImg.value.uid)
  }
  fileList.value = fileList.value.filter((item) => item.status !== 'removed')
}
function getBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = (error) => reject(error)
  })
}
const visible = ref(false)
const previewImage = ref('')
const previewTitle = ref('')

const handlePreview = async (file) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj)
  }
  previewImage.value = file.url || file.preview
  visible.value = true
  previewTitle.value = file.name || file.url.substring(file.url.lastIndexOf('/') + 1)
}
</script>
<style scoped lang="scss">
.clearfix {
  //   width: 80px;
  //   height: 80px;
  .ant-upload-wrapper {
    :deep(.ant-upload-select) {
      height: 72px !important;
      width: 72px !important;
      border-radius: 0;
      @include border-color('base33414C');
      .icon-wrapper {
        font-size: 16px;
        @include font_color('baseBFC3C7');
      }
    }

    :deep(.ant-upload-list-item-container) {
      height: 72px !important;
      width: 72px !important;
      .ant-upload-list-item {
        padding: 4px;
        font-size: 14px;
        &::before {
          width: calc(100% - 8px) !important;
          height: calc(100% - 8px) !important;
        }
        .ant-upload-list-item-thumbnail {
          @include font_color('baseBFC3C7');
        }
      }
    }
  }
}
</style>
