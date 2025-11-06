<template>
  <a-row :gutter="10" class="page-container">
    <a-col :span="6">
      <div class="page-left">
        <div class="page-title">
          <span class="title"> 机组组织</span>
          <p>
            <span class="addBtn" @click="orgAdd">新增</span>
            <span class="editBtn" @click="orgEdit">编辑</span>
            <span class="delBtn" @click="orgDel">删除</span>
          </p>
        </div>
        <a-input v-model:value="fmSearchOrgName.orgName" @pressEnter="getPageInfo" placeholder="组织名称">
          <template #prefix>
            <SearchOutlined @click="getPageInfo" />
          </template>
        </a-input>

        <div class="treeBox">
          <a-tree :tree-data="treeData" ellipsis show-icon :fieldNames="{
            title: 'orgName',
            key: 'id',
            children: 'children'
          }" @select="onSelect" v-model:expandedKeys="expandedKeys">
            <!-- <template #icon="{ orgType }">
              <template v-if="orgType == 1"> <i class="iconfont sheng"></i> </template>
              <template v-else-if="orgType == 2"> <i class="iconfont shi"></i> </template>
              <template v-else-if="orgType == 3"> <i class="iconfont qu"></i> </template>
              <template v-else-if="orgType == 4"> <i class="iconfont xian"></i> </template>
              <template v-else-if="orgType == 5"> <i class="iconfont genbu"></i> </template>
              <template v-else>
                <i class="iconfont biandianzhan"></i>
              </template>
            </template> -->
          </a-tree>
        </div>
      </div>
    </a-col>
    <a-col :span="18">
      <div class="page-right">
        <div class="search">
          <a-space wrap>
            <div class="funItem">
              <label>风机名称：</label>
              <a-input v-model:value="fmSearch.siteName" @pressEnter="getSiteList" placeholder="请输入风机名称" />
            </div>
          </a-space>
          <a-space>
            <a-button type="primary" class="primary" ghost @click="getSiteList">搜索</a-button>
            <a-button ghost class="info" @click="reset">重置</a-button>

          </a-space>
        </div>
        <a-space class="flex-end">
          <a-button type=" primary" class="primary" @click="addSiteForm">新增风机</a-button>
        </a-space>
        <a-table :data-source="pageInfo.list" rowKey="id" :bordered="false" :pagination="false" :scroll="tableHeight">
          <a-table-column key="id" title="序号" :ellipsis="true" align="center">
            <template #default="{ record, index }">
              {{ index + 1 }}
            </template>
          </a-table-column>
          <a-table-column key="id" title="风机名称" width="10%" data-index="siteName" :ellipsis="true" />
          <a-table-column key="id" title="风机型号" data-index="siteModel" :ellipsis="true" align="center" />
          <a-table-column key="id" title="所属组织" align="center" data-index="orgName" :ellipsis="true" />

          <a-table-column key="id" title="操作" align="center" width="210px">
            <template #default="{ record }">
              <span class="editBtn" @click="siteEdit(record)"> 编辑 </span>
              <span class="delBtn" @click="siteDel(record)"> 删除 </span>
            </template>
          </a-table-column>
        </a-table>
        <div class="pagination">
          <a-pagination :current="fmSearch.page" :pageSize="fmSearch.size" :show-quick-jumper="true"
            :total="pageInfo.total" :showSizeChanger="true" :pageSizeOptions="['10', '20', '40', '50']"
            :show-total="(total, range) => `共 ${total} 条`" @change="pageChange" @showSizeChange="sizeChange" />
          <span class="total">本页共 {{ pageInfo.items }} 条</span>
        </div>
      </div>
    </a-col>
    <!-- 新建，编辑组织 -->
    <a-drawer :title="title" placement="right" :closable="false" :width="450" :open="open" @close="onClose">
      <div class="fromBox" v-if="open">
        <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
          <a-form-item ref="remark" label="组织名称" name="orgName">
            <a-input v-model:value="formState.orgName" placeholder="请输入组织名称" :maxlength="20" />
          </a-form-item>
          <!-- 组织编码 -->
          <a-form-item ref="orgCode" label="组织编码" name="orgCode">
            <a-input v-model:value="formState.orgCode" placeholder="请输入组织编码" :maxlength="80" />
          </a-form-item>
        </a-form>
      </div>
      <template #footer>
        <a-space>
          <a-button type="primary" class="primary" @click="onSubmit">提交</a-button>
          <a-button @click="onClose" class="info">取消</a-button>
        </a-space>
      </template>
    </a-drawer>
    <!-- 新增，编辑风机 -->
    <a-drawer :title="siteTitle" placement="right" :closable="false" :width="450" :open="blowerOpen"
      @close="blowerClose">
      <div class="fromBox">
        <a-form ref="addSiteRef" :model="addSiteState" :rules="blowerRules" :label-col="{ span: 3 }"
          :wrapper-col="{ span: 21 }">
          <a-form-item ref="remark" label="风机名称" name="siteName">
            <a-input v-model:value="addSiteState.siteName" placeholder="请输入风机名称" :maxlength="20" />
          </a-form-item>
          <a-form-item ref="orgCode" label="风机型号" name="siteModel">
            <a-input v-model:value="addSiteState.siteModel" placeholder="请输入风机型号" :maxlength="80" />
          </a-form-item>
          <a-form-item ref="orgType" label="所属组织" name="orgId">
            <a-select v-model:value="addSiteState.orgId" placeholder="请选择">
              <a-select-option v-for="item in orgList" :key="item.value" :value="item.value">{{
                item.label
              }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
      </div>
      <template #footer>
        <a-space>
          <a-button type="primary" class="primary" @click="AddSiteOnSubmit">提交</a-button>
          <a-button @click="blowerClose" class="info">取消</a-button>
        </a-space>
      </template>
    </a-drawer>
  </a-row>
</template>
<script setup>
import { SearchOutlined } from '@ant-design/icons-vue'
import {
  getOrgInfo,
  setOrgInfo,
  delOrgInfo,
  getSiteInfo,
  setSiteInfo,
  delSiteInfo
} from '@/api/system/index.js'
import { message, Modal } from 'ant-design-vue'
import { useDictStore } from '@/stores/dict.js'
import { useSysStore } from '@/stores/system.js'
import { getOrgHeader } from '@/api/system/index.js'
import { storeToRefs } from 'pinia'

import $X from '@/utils/$X.js'
import { reactive } from 'vue'

// 初始加载
const tableHeight = ref()
onMounted(() => {
  tableHeight.value = {
    y: document.getElementsByClassName('page-right')[0].clientHeight * 0.75
  }
  window.onresize = function () {
    tableHeight.value = {
      y: document.getElementsByClassName('page-right')[0].clientHeight * 0.75
    }
  }
  getPageInfo()
})

// 字典配置
let dict = useDictStore()
let orgTypeList = dict.getDictList('orgType').slice(0, 2)
let siteModelList = dict.getDictList('siteType')
let orgList = ref([])
// 获取组织信息
let fmSearchOrgName = ref({})
let treeData = ref([])
let treeDataNoSite = ref([])
let expandedKeys = ref([])
const getPageInfo = () => {
  getOrgInfo({ orgName: fmSearchOrgName.value.orgName }).then((res) => {
    orgList.value = res.data.map(item => ({ label: item.orgName, value: item.id }))
    if (fmSearchOrgName.value.orgName) {
      expandedKeys.value = res.data.map((key) => key.id)
    } else {
      // expandedKeys.value = []
    }
    let obj = JSON.parse(JSON.stringify(res.data))
    if (selectNode.value) {
      fmSearch.value.orgIds = [selectNode.value.id]
    } else {
      fmSearch.value.orgIds = obj.map((key) => key.id)
    }
    treeDataNoSite.value = $X.buildTree(
      obj.filter((item) => item.orgType != 2),
      0
    )
    treeData.value = $X.buildTree(res.data, 0)
    getSiteList()
  })
}
const fmSearch = ref({
  page: 1,
  size: 10,
  orgIds: null,
  siteName: null
})
// 获取机组信息
const pageInfo = ref({
  list: [],
  total: 0,
  items: 0
})
const getSiteList = () => {
  getSiteInfo(fmSearch.value).then((res) => {
    pageInfo.value.list = res.data.records
    pageInfo.value.total = res.data.total
    pageInfo.value.items = res.data.records.length
  })
}
// 获取所有机组
let sysConf = useSysStore()
let { siteId } = storeToRefs(sysConf)
const getHeaderOrg = () => {
  getOrgHeader({ orgType: 1 }).then((res) => {
    sysConf.setSiteList(res.data)
    if (!siteId.value) {
      sysConf.setSiteId(res.data[0].id)
    }
  })
}

const pageChange = (page, pageSize) => {
  fmSearch.value.page = page
  getSiteList()
}
const sizeChange = (page, pageSize) => {
  fmSearch.value.size = pageSize
  fmSearch.value.page = 1
  getSiteList()
}
// 选择树节点
let selectNode = ref(null)
const onSelect = (selectedKeys, { node }) => {
  if (selectNode.value && selectNode.value.id == node.id) {
    selectNode.value = null
  } else {
    selectNode.value = node
    fmSearch.value.orgIds = [node.id]
    getSiteList()
  }
}
// dw
let open = ref(false)
let siteOpen = ref(false)
let blowerOpen = ref(false)
let title = ref('')
let formState = ref({
  parentId: 0,
  orgName: '',
  orgCode: '',
  orgType: 1
})
let addSiteState = ref({
  siteName: '',
  siteModel: '',
  orgId: ''
})
let siteTitle = ref('')
let formRef = ref(null)
let addSiteRef = ref(null)
// 发送请求
const onSubmit = () => {
  formRef.value
    .validate()
    .then(() => {
      formState.value.parentId = formState.value.parentId || 0
      message.loading({ content: '提交中...', key: 'orgKey' })

      setOrgInfo(formState.value).then((res) => {
        if (res.code == 200) {
          message.success({ content: '操作成功', key: 'orgKey', duration: 2 })
          if (title.value == '编辑') {
            selectNode.value = res.data
          }
          getHeaderOrg()
          getPageInfo()
          onClose()
        }
      })
    })
    .catch((error) => {
      console.log('error', error)
    })
}
const AddSiteOnSubmit = () => {
  addSiteRef.value
    .validate()
    .then(() => {
      message.loading({ content: '提交中...', key: 'orgKey' })

      setSiteInfo(addSiteState.value).then((res) => {
        if (res.code == 200) {
          message.success({ content: '操作成功', key: 'orgKey', duration: 2 })
          getHeaderOrg()
          getPageInfo()
          blowerClose()
        }
      })
    })
    .catch((error) => {
      console.log('error', error)
    })
}
// 表单规则
const rules = {
  orgName: [
    {
      required: true,
      message: '请输入组织名称',
      trigger: 'blur'
    },
    { min: 1, max: 20, message: '1到20个字符', trigger: 'blur' }
  ],
  orgCode: [
    {
      required: true,
      message: '请输入组织编码',
      trigger: 'blur'
    }
  ],
  // orgType: [
  //   {
  //     required: true,
  //     message: '请选择组织类型',
  //     trigger: 'blur'
  //   }
  // ]
}
const siteRules = {
  siteName: [
    {
      required: true,
      message: '请输入机组名称',
      trigger: 'blur'
    },
    { min: 1, max: 20, message: '1到20个字符', trigger: 'blur' }
  ],
  siteModel: [
    {
      required: true,
      message: '请选择机组类型',
      trigger: 'change'
    }
  ],

  siteType: [
    {
      required: true,
      message: '请选择电压等级',
      trigger: 'change'
    }
  ]
}

// 关闭
const onClose = () => {
  open.value = false
}
const siteClose = () => {
  siteOpen.value = false
}
const blowerClose = () => {
  addSiteRef.value.clearValidate()
  blowerOpen.value = false
}
// 新增
const orgAdd = () => {
  if (selectNode.value?.orgType == 6) {
    message.warning({ content: '机组不可再新增', key: 'orgAddKey', duration: 2 })
    return
  }
  open.value = true
  title.value = '新增'
  formState.value = {
    parentId: 0,
    orgName: '',
    orgCode: '',
    orgType: 1
  }
}
// 编辑
const orgEdit = (item) => {
  if (selectNode.value) {
    console.log(selectNode.value, 'selectNode.value')
    open.value = true
    title.value = '编辑'
    formState.value = {
      id: selectNode.value.id,
      parentId: 0,
      orgName: selectNode.value.orgName,
      orgCode: selectNode.value.orgCode,
      orgType: 1
    }
  } else {
    message.warning({ content: '请先选择组织', key: 'orgEditKey', duration: 2 })
  }
}
// 机组编辑
const siteEdit = (item) => {
  blowerOpen.value = true
  siteTitle.value = '编辑风机'
  addSiteState.value = {
    id: item.id,
    siteName: item.siteName,
    siteModel: item.siteModel,
    orgId: item.orgId
  }
}
// 删除机组
const siteDel = (item) => {
  Modal.confirm({
    title: '提示',
    icon: h('i'),
    content: h('div', { class: 'delContent' }, [
      h('span', { class: 'iconfont delIcon tixing' }),
      h('span', { class: 'delContentText' }, `确定删除【${item.siteName}】机组吗？`)
    ]),
    okText: '确认',
    cancelText: '取消',
    closable: true,
    onOk: () => {
      siteDelConfirm(item.id)
    }
  })
}
// 确认删除机组
const siteDelConfirm = (id) => {
  delSiteInfo(id).then((res) => {
    if (res.code == 200) {
      message.success({ content: '删除成功', key: 'siteDelKey', duration: 2 })
      getPageInfo()
    }
  })
}
// 删除提示
const orgDel = () => {
  if (!selectNode.value) {
    message.warning({ content: '请先选择要删除的组织', key: 'orgDelKey', duration: 2 })
    return
  }
  Modal.confirm({
    title: '提示',
    icon: h('i'),
    content: h('div', { class: 'delContent' }, [
      h('span', { class: 'iconfont delIcon tixing' }),
      h('span', { class: 'delContentText' }, `确定删除【${selectNode.value.orgName}】组织吗？`)
    ]),
    okText: '确认',
    cancelText: '取消',
    closable: true,
    onOk: () => {
      if (selectNode.value.orgType == 6) {
        let { id } = pageInfo.value.list[0]
        siteDelConfirm(id)
      } else {
        orgDelConfirm(selectNode.value.id)
      }
    }
  })
}
// 确认删除
const orgDelConfirm = (id) => {
  delOrgInfo(id).then((res) => {
    if (res.code == 200) {
      message.success({ content: '删除成功', key: 'orgDelKey', duration: 2 })
      selectNode.value = null
      if (id == siteId.value) {
        sysConf.setSiteId(null)
      }
      getHeaderOrg()
      getPageInfo()
    }
  })
}

const reset = () => {
  fmSearch.value = {
    page: 1,
    size: 10,
    orgIds: null,
    siteName: null,
  }
  getPageInfo()
}

const blowerRules = {
  siteName: [
    {
      required: true,
      message: '请输入风机名称',
      trigger: 'blur'
    },
    { min: 1, max: 20, message: '1到20个字符', trigger: 'blur' }
  ],
  siteModel: [
    {
      required: true,
      message: '请输入风机型号',
      trigger: 'blur'
    }
  ],
  orgId: [
    {
      required: true,
      message: '请选择',
      trigger: 'change'
    }
  ]
}
// 新增
const addSiteForm = () => {
  blowerOpen.value = true
  siteTitle.value = '新增风机'
  addSiteState.value = {
    siteName: '',
    siteModel: '',
    orgId: ''
  }
}
</script>
<style lang="scss" scope>
.page-container {
  height: 100%;

  .page-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 22px;

    .title {
      font-size: 16px;
      font-weight: 400;
      @include font_color('base00D1FF');
    }
  }

  .treeBox {
    margin-top: 16px;
    overflow-y: auto;
    height: 100%;

    .ant-tree {
      .ant-tree-node-content-wrapper {
        .ant-tree-switcher {
          display: none;
        }
      }
    }
  }
}

.siteType {
  font-size: 14px;
  @include font_color('baseBFBFBF');
  display: flex;
  justify-content: center;
  align-items: center;
  margin-left: 7px;
}

.fromBox {
  padding: 0 !important;
}

p.title {
  margin: 24px 0 24px 10px;
  font-size: 16px;
  font-weight: 400;
  @include font_color('base00D1FF');
  position: relative;

  // margin-left: 10px;
  &::after {
    content: '';
    position: absolute;
    left: -10px;
    top: 5px;
    width: 4px;
    height: 16px;
    @include background-color('base00D1FF');
  }

  &:first-child {
    margin-top: 0;
  }
}

.page-left,
.page-right {
  padding: 16px;
  background: #001525;
  box-shadow: inset 0px 0px 16px 0px rgba(0, 69, 90, 0.24);
  height: 100%;

}

.flex-end {
  display: flex;
  justify-content: flex-end;
}
</style>
