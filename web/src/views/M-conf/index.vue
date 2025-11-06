<template>
  <div class="body-box">
    <img src="@/assets/newImg/config_title.png" alt="" class="title-img">
    <a-row class="content-box">
      <a-col :span="4">
        <div class="sider">
          <div class="site-box">
            <siteMenuList :items="siteList" :defaultSelectedKey="defaultKey" @select="changeSiteHandle"
              :defaultOpenAll="true" :className="'minHeight'" />
          </div>
        </div>
      </a-col>
      <a-col :span="20">
        <div class="page-right">
          <div class="title">信息配置</div>
          <a-spin :spinning="tableLoading">
            <div class="info-box">

              <a-table :columns="columns" :data-source="pageInfo.list" :scroll="tableHeight" :pagination="false"
                :class="['table-box', (imgFlagOne || imgFlagTwo) ? 'calc-width' : '']">
                <template #setting="{ text, record }" :key="'setting'">
                  <div>
                    <div v-if="editableData[record.id]">

                      <a-select v-model:value="editableData[record.id].symbol" class="form-width">
                        <a-select-option v-for="(item, index) in symbolList" :value="item.key" :key="index">{{
                          item.value
                        }}</a-select-option>
                      </a-select>
                      <a-input v-model:value="editableData[record.id].threshold" class="form-width margin10" />
                      <a-input v-model:value="editableData[record.id].unit" class="form-width" />
                    </div>
                    <template v-else>
                      {{ record.symbol + record.threshold + (record.unit || '') }}
                    </template>
                  </div>
                </template>
                <template #operation="{ record }">
                  <div class="editable-row-operations">
                    <span v-if="editableData[record.id]">
                      <span class="edit-txt" @click="saveConfig(editableData[record.id])">Save</span>
                      <span class="edit-txt" @click="cancel(record.id)">Cancel</span>
                    </span>
                    <span v-else>
                      <span @click="edit(record.id)" class="edit-txt">Edit</span>
                    </span>
                  </div>
                </template>
              </a-table>
              <!-- 热成像 -->
              <img src="@/assets/newImg/temperatureBg.png" alt="" class="img-box" v-if="imgFlagOne">
              <img src="@/assets/newImg/audioBg.png" alt="" class="img-box" v-if="imgFlagTwo">


            </div>
          </a-spin>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script setup>
import { getOrgTree, getConfigList, getDictionary, getConfigEdit } from '@/api/configSetting/index.js'
import siteMenuList from './component/siteMenuList.vue'
import Table from '@/views/commonComponents/table.vue'
import { columns } from './columns'
import { cloneDeep } from 'lodash'
import { message } from 'ant-design-vue';
import { ref, reactive, onMounted, computed } from 'vue'
let siteList = ref([])
let tableLoading = ref(false)
let pageInfo = ref({
  list: [],
  total: 0,
  pageNum: 1,
  pageSize: 10

})
let tableHeight = ref(0)
let defaultKey = ref(0)
let symbolList = ref([])
let editableData = ref({});
onMounted(() => {
  tableHeight.value = {
    y: document.getElementsByClassName('body-box')[0].clientHeight * 0.78
  }
  window.onresize = function () {
    tableHeight.value = {
      y: document.getElementsByClassName('body-box')[0].clientHeight * 0.78
    }
  }
  getDictionaryList()
  getSiteList()
})
const imgFlagOne = computed(() => {
  return defaultKey.value == 11 || defaultKey.value == 12
})
const imgFlagTwo = computed(() => {
  return defaultKey.value == 13 || defaultKey.value == 14 || defaultKey.value == 15
})
const getSiteList = async () => {
  let res = await getOrgTree()
  siteList.value = res.data
  defaultKey.value = siteList.value[0].menuCode
  getPageInfo(defaultKey.value)
}
const changeSiteHandle = ({ key }) => {
  editableData.value = {}
  defaultKey.value = key
  getPageInfo(key)
}
const getPageInfo = async (key) => {
  tableLoading.value = true
  let res = await getConfigList({
    menuCode: key
  })
  tableLoading.value = false
  pageInfo.value.list = res.data
}

const edit = (id) => {
  editableData.value[id] = cloneDeep(pageInfo.value.list.filter(item => id === item.id)[0]);
  console.log(id, editableData.value[id], '编辑id')
};
const saveConfig = async ({ id, symbol, threshold, unit }) => {
  if (!symbol || !threshold || !unit) {
    message.warning('请填写完整')
    return
  }
  let res = await getConfigEdit({ id, symbol, threshold, unit })
  if (res.code == 200) {
    delete editableData.value[id];
    message.success(res.message)
    getPageInfo(defaultKey.value)
  } else {
    message.error('保存失败')
  }
}

const cancel = (id) => {
  delete editableData.value[id];
};
const getDictionaryList = async () => {
  let res = await getDictionary({ type: 'symbol' })
  symbolList.value = res.data.symbol
}
</script>
<style lang="scss" scope>
.sider {
  width: calc(100% - 20px);
  background: #001525;
  padding: 20px;
  display: flex;
  box-sizing: border-box;
  height: 100%;
  box-shadow: 0px 0px 16px 0px rgba(0, 69, 90, 0.24) inset;
}

.site-box {
  width: 100%
}

.form-width {
  width: 100px;
}

.margin10 {
  margin: 0 10px;
}

.edit-txt {
  color: #00d1ff;
  cursor: pointer;
  margin-right: 5px;
}

.title {
  font-weight: 500;
  font-size: 16px;
  color: #01C9FD;
  margin-bottom: 14px;
}

.body-box {
  height: 96%;
  display: block;
}

.content-box {
  height: 100%
}

.page-right {
  background: #001525;
  padding: 20px;
  box-sizing: border-box;
  height: 98%;
  box-shadow: 0px 0px 16px 0px rgba(0, 69, 90, 0.24) inset;
}

.title-img {
  margin: 8px 0;
  width: 224px;
  height: 32px;
  display: block;
}

.img-box {
  width: 334px;
  height: 192px;
  margin-top: 10px;
  max-height: 334px;
}

.table-box {
  margin-right: 8px;

}

.calc-width {
  width: calc(100% - 340px);
}

.info-box {
  display: flex;
  align-items: top;
}
</style>
