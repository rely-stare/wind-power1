<template>
  <div class="page-box">
    <div class="treeBox">
      <a-tree :tree-data="treeData" ellipsis show-icon :fieldNames="{
        title: 'orgName',
        key: 'id',
        children: 'children'
      }" @select="onSelect" v-model:selectedKeys="orgDefalutKeys" v-model:expandedKeys="expandedKeys" class="tree-org">
      </a-tree>
      <div class="site-all">
        <!-- <span style="color:#fff">{{siteList}}{{defaultKey}}</span> -->
        <SetMenuList :items="siteList" @select="changeSiteHandle" :defaultSelectedKey="defaultKey" />
      </div>
    </div>
    <div class="tableBox">
      <a-spin :spinning="tableLoading">
        <a-table :columns="columns" :data-source="pageInfo.list" :scroll="tableHeight" :pagination="false"
          :rowKey="record => record.id">
          <template #index="{ index }">
            {{ index + 1 }}
          </template>
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.dataIndex === 'hardwareType'">
              <td v-if="record.rowSpan > 0" :rowSpan="record.rowSpan" style="display: inline-block;">
                {{ getHardwareType(record.hardwareType) }}
              </td>
            </template>
          </template>
          <template #hardwareCode="{ text, record }" :key="'hardwareCode'">
            <div class="editable-cell">
              <div v-if="editableData[record.id]">
                <a-input v-model:value="editableData[record.id].hardwareCode" class="form-width" placeholder="请输入" />
              </div>
              <div class="editable-row-operations">
                <span v-if="editableData[record.id]">
                  <CheckOutlined @click="saveConfig(editableData[record.id])" class="edit-txt" />
                  <CloseOutlined @click="cancel(record.id)" class="cancel-txt" />
                </span>
                <span v-else>
                  {{ record.hardwareCode }}
                  <EditFilled @click="edit(record.id)" />
                </span>
              </div>
            </div>

          </template>
        </a-table>
      </a-spin>
    </div>
  </div>
</template>
<script setup>
import {
  getOrgInfo,
  getHardwareList,
  getHardwareUpdate
} from '@/api/system/index.js'
import {
  getSiteListByOrgId
} from '@/api/siteMenu/index.js'
import { reactive, ref, onMounted } from 'vue'
import SetMenuList from './components/menuList.vue'
import $X from '@/utils/$X.js'
import { columns } from './columns'
import { EditFilled, CheckOutlined, CloseOutlined } from '@ant-design/icons-vue';
import { cloneDeep } from 'lodash'
import { message } from 'ant-design-vue';
onMounted(() => {
  tableHeight.value = {
    y: document.getElementsByClassName('page-box')[0].clientHeight * 0.75
  }
  window.onresize = function () {
    tableHeight.value = {
      y: document.getElementsByClassName('page-box')[0].clientHeight * 0.75
    }
  }
  getPageInfo()
})
let treeData = ref([])
// 选择树节点
let selectNode = ref(null)
// 展开得节点
let expandedKeys = ref([])
// 风机列表
let siteList = ref([])
// 风机默认选中值
let defaultKey = ref(0)
// 组织默认选中值
let orgDefalutKeys = ref([])
let tableLoading = ref(false)
let pageInfo = ref({
  list: [],
  total: 0
})
// 编辑列信息
let editableData = ref({})
let tableHeight = ref()
let selectKeys = ref([])
// 按 hardwareType 进行分组
let groupedData = ref([]);
// 计算 rowSpan
let rowSpanMap = {};
const getPageInfo = () => {
  getOrgInfo().then((res) => {
    expandedKeys.value = res.data.map((key) => key.id)
    treeData.value = $X.buildTree(res.data, 0)
    selectNode.value = res.data[0].id
    orgDefalutKeys.value = [selectNode.value]
    selectKeys.value = [selectNode.value]
    getSiteList()
  })
}
// 更换风电场
const onSelect = (selectedKeys, { node }) => {
  console.log('selected', selectedKeys, node)
  if (selectNode.value && selectNode.value == node.id) {
    selectNode.value = null
  } else {
    selectNode.value = node.id
    selectKeys.value = [selectKeys.value]
    getSiteList()
  }
}
// 获取某个风场得风机组
const getSiteList = () => {
  getSiteListByOrgId({
    orgId: selectNode.value
  }).then((res) => {
    siteList.value = res.data.map((item) => ({ label: item.siteName, id: item.id }))
    if (res.data.length > 0) {
      defaultKey.value = res.data[0].id
    } else {
      defaultKey.value = 0
    }

    getHardList()
  })
}
// 更换风机
const changeSiteHandle = ({ key }) => {
  editableData.value = {}
  defaultKey.value = key
  getHardList()
}
const edit = (id) => {
  editableData.value[id] = cloneDeep(pageInfo.value.list.filter(item => id === item.id)[0]);
};
const cancel = (id) => {
  delete editableData.value[id];
};
const saveConfig = async (row) => {
  if (!row.hardwareCode) {
    message.warning('请填写完整')
    return
  }
  let res = await getHardwareUpdate({
    id: row.id,
    hardwareCode: row.hardwareCode
  })
  if (res.code == 200) {
    delete editableData.value[row.id];
    message.success('编辑成功！')
    getHardList()
  }
}
const getHardList = async () => {
  let res = await getHardwareList({ siteId: defaultKey.value })
  calculateRowSpan(res.data);
}
const getHardwareType = (type) => {
  const map = {
    2: '热成像',
    3: '视频',
    4: '音频'
  }
  return map[type]
}


const calculateRowSpan = (data) => {
  let countMap = new Map();
  let firstIndexMap = new Map();
  data.forEach((item, index) => {
    if (!countMap.has(item.hardwareType)) {
      countMap.set(item.hardwareType, 1);
      firstIndexMap.set(item.hardwareType, index);
    } else {
      countMap.set(item.hardwareType, countMap.get(item.hardwareType) + 1);
    }
  });

  pageInfo.value.list = data.map((item, index) => ({
    ...item,
    rowSpan: firstIndexMap.get(item.hardwareType) === index ? countMap.get(item.hardwareType) : 0,
  }));
};

</script>
<style lang="scss" scoped>
.page-box {
  display: flex;
  background: #001525;
  padding: 20px;
  padding-left: 0;
  box-sizing: border-box;
  box-shadow: 0px 0px 16px 0px rgba(0, 69, 90, 0.24) inset;
  height: 100%;
}

.treeBox {
  width: 20%;
  display: flex;

}

::v-deep .tree-org {
  width: 190px !important;
  margin-right: 10px;
  border-right: 1px dashed #142736;
}

.ant-tree .tableBox {
  width: calc(100% - 220px) !important;
  margin-left: 20px;
}

.site-all {
  width: 200px;
  margin-right: 20px;
}


.editable-cell {
  display: flex;
  justify-content: center;
}

.edit-txt {
  margin-right: 5px;
  margin-left: 5px;
  color: #31B182;
}

.cancel-txt {
  color: #BC3D4A
}

::v-deep .ant-tree .ant-tree-treenode .ant-tree-node-selected {
  color: #00d1ff !important
}

::v-deep .ant-table tbody tr:not(.ant-table-placeholder):hover .ant-table-cell-row-hover {
  background: #142736 !important;
  color: #a6acb1 !important
}

.tableBox ::v-deep .ant-table {
  margin-top: 0px !important;
}

.form-width {
  width: 120px;
  height: 28px !important;
}
</style>