<template>
  <div>
    <a-row class="menuSearch">
      <!-- <a-col :span="8" class="search">
        <div class="funItem">
          <a-select
            ref="select"
            v-model:value="fmSearch.accessType"
            placeholder="请选择角色属性"
            @change="handleChange"
          >
            <a-select-option value=""> 全部 </a-select-option>
            <a-select-option :value="item.value" v-for="item in accessTypes" :key="item.value">
              {{ item.key }}
            </a-select-option>
          </a-select>
        </div>
      </a-col> -->
      <a-col :span="8" class="search">
        <a-input
          v-model:value="fmSearch.title"
          @pressEnter="getMenuList"
          placeholder="菜单、按钮"
          class="inpt"
        />
      </a-col>
      <a-col :span="8" class="search">
        <a-button type="primary" class="primary" @click="setEXpand">{{
          expandedKeys.length ? '收起' : '展开'
        }}</a-button>
        <a-button
          type="primary"
          class="primary"
          @click="checkIdAlls"
          >{{ checkedKeys.length !== expandedKeyList.length ? '全选' : '取消全选' }}</a-button
        >
      </a-col>
    </a-row>
    <div class="treeBox">
      <a-tree
        v-model:checkedKeys="checkedKeys"
        v-model:expandedKeys="expandedKeys"
        :tree-data="treeData"
        checkable
        show-icon
        @check="handleCheck"
        :fieldNames="{
          title: 'title',
          key: 'id',
          children: 'children'
        }"
      >
        <!-- <template #switcherIcon="{ expanded }">
          <i class="iconfont xiangxia1" v-if="expanded"></i>
          <i class="iconfont xiangxia" v-else></i>
        </template> -->
        <template #icon="{ menuType }">
          <template v-if="menuType !== 3"> <i class="iconfont caidan"></i> </template>
          <template v-else>
            <i class="iconfont anniu"></i>
          </template>
        </template>
      </a-tree>
    </div>
  </div>
</template>
<script setup>
import { useDictStore } from '@/stores/dict.js'
import { getPageInfo } from '@/api/system/index.js'
import $X from '@/utils/$X.js'
// 接收父组件传过来的menuIds
const props = defineProps({
  menuIds: {
    type: Array,
    default: []
  }
})

// 字典配置
let dict = useDictStore()
let accessTypes = dict.getDictList('accessType')

onMounted(() => {
  if (props.menuIds.length > 0) {
    checkedKeys.value = props.menuIds
  }
  getMenuList()
})
let expandedKeys = ref([])
// 展开按钮
const setEXpand = () => {
  if (expandedKeys.value.length === 0) {
    expandedKeys.value = expandedKeyList.value
  } else {
    expandedKeys.value = []
  }
}
// 全选按钮
const checkIdAlls = () => {
  if (checkedKeys.value.length !== expandedKeyList.value.length) {
    checkedKeys.value = expandedKeyList.value
  } else {
    checkedKeys.value = []
  }
}
// 搜索条件
const fmSearch = ref({
  title: null
})
// 监听
watch(
  () => fmSearch.value.accessType,
  (newValue, oldValue) => {
    getMenuList()
  }
)
// 获取菜单列表
let treeData = ref([])
let expandedKeyList = ref([])

const getMenuList = () => {
  fmSearch.value.accessTypeList = fmSearch.value.accessType ? [fmSearch.value.accessType] : null
  getPageInfo(fmSearch.value).then((res) => {
    // if (checkedKeys.value.length) {
    //   fmSearch.value.accessType =
    //     res.data.find((item) => item.id == checkedKeys.value[0]).accessType + ''
    // }
    expandedKeyList.value = res.data.map((item) => item.id)
    treeData.value = $X.buildTree(res.data, null)
  })
}
// 切换菜单属性
const handleChange = () => {
  checkedKeys.value = []

  // getMenuList()
}
// 选中菜单
const checkedKeys = ref([])
const handleCheck = (checkedKeys, { node }) => {
  // if (checkedKeys.includes(node.id) && node.accessType != fmSearch.value.accessType) {
  //   // fmSearch.value.accessType = node.accessType + ''
  // } else if (checkedKeys.length === 0) {
  //   fmSearch.value.accessType = null
  // }
}
// 导出选中的菜单
defineExpose({ checkedKeys })
</script>
<style lang="scss" scoped>
// 一个y轴滚动的区域
.treeBox {
  margin-top: 20px;
  overflow-y: auto;
  height: 51 0px;
  .ant-tree {
    .ant-tree-node-content-wrapper {
      .ant-tree-switcher {
        display: none;
      }
    }
  }
}

.menuSearch {
}
.search {
  display: flex;
  justify-content: center;
  align-items: center;
  border: none;
  padding: 0;
  margin: 0;
  &:nth-of-type(1) {
    justify-content: flex-start;
  }
  &:nth-of-type(2) {
    .primary:nth-of-type(1) {
      margin-right: 10px;
    }
  }
  :deep(.funItem) {
    .ant-select {
      .ant-select-selector {
        border-radius: 4px;
        min-width: 100px;
        width: 160px;
        line-height: 30px;
        font-size: 13px;
        height: 32px;
      }
      .ant-select-selection-placeholder {
        line-height: 30px;
        font-size: 13px;
      }
      .ant-select-selection-item {
        line-height: 30px;
      }
      .ant-select-arrow {
        font-size: 13px;
      }
    }
  }

  .ant-input {
    height: 32px !important;
    font-size: 13px;
    width: 160px;
    border-radius: 4px;
    &::placeholder {
      font-size: 13px;
    }
  }
}
</style>
