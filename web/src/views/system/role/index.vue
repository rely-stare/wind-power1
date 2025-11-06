<template>
  <div class="page">
    <div class="search">
      <a-space wrap>
        <div class="funItem">
          <label>角色名称：</label>
          <a-input
            v-model:value="fmSearch.roleName"
            @pressEnter="getPageInfo"
            placeholder="请输入角色名称"
          />
        </div>
      </a-space>
      <a-space>
        <a-button type="primary" class="primary" ghost @click="getPageInfo">搜索</a-button>
        <a-button ghost class="info" @click="reset">重置</a-button>
      </a-space>
    </div>
    <a-space class="btns">
      <a-button type="primary" class="primary" ghost @click="roleAdd">新增</a-button>
    </a-space>
    <a-table
      :data-source="pageInfo.list"
      rowKey="id"
      :bordered="false"
      :pagination="false"
      :scroll="tableHeight"
    >
      <a-table-column key="id" title="序号" :ellipsis="true" align="center" width="5%">
        <template #default="{ record, index }">
          {{ index + 1 }}
        </template>
      </a-table-column>
      <a-table-column
        key="id"
        title="角色名"
        data-index="roleName"
        width="210px"
        :ellipsis="true"
        align="center"
      />
      <a-table-column
        key="id"
        title="关联用户"
        data-index="number"
        :ellipsis="true"
        align="center"
        width="210px"
      />
      <a-table-column
        key="id"
        title="权限说明"
        align="center"
        data-index="remark"
        :ellipsis="true"
      />

      <a-table-column key="id" title="操作" align="center" width="210px">
        <template #default="{ record }">
          <span class="editBtn" @click="roleEdit(record)" v-if="record.roleType !== 1"> 编辑 </span>
          <span class="delBtn" @click="roleDel(record)" v-if="record.roleType !== 1"> 删除 </span>
        </template>
      </a-table-column>
    </a-table>
    <div class="pagination">
      <a-pagination
        :current="fmSearch.page"
        :pageSize="fmSearch.size"
        :show-quick-jumper="true"
        :total="pageInfo.total"
        :showSizeChanger="true"
        :pageSizeOptions="['10', '20', '40', '50']"
        :show-total="(total, range) => `共 ${total} 条`"
        @change="pageChange"
        @showSizeChange="sizeChange"
      />
      <span class="total">本页共 {{ pageInfo.items }} 条</span>
    </div>
    <a-drawer
      :title="title"
      placement="right"
      :closable="false"
      :width="450"
      :open="open"
      @close="onClose"
    >
      <div class="fromBox" v-if="open">
        <a-form
          ref="formRef"
          :model="formState"
          :rules="rules"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 19 }"
        >
          <a-form-item ref="roleName" label="角色名称" name="roleName">
            <a-input
              v-model:value.trim="formState.roleName"
              :maxlength="20"
              placeholder="请输入角色名称"
            />
          </a-form-item>
          <!-- <a-form-item ref="taskControl" label="任务控制权限" name="taskControl">
            <a-select v-model:value="formState.taskControl" placeholder="请选择任务控制权限">
              <a-select-option
                :value="item.value"
                v-for="item in taskControlList"
                :key="item.value"
              >
                {{ item.key }}
              </a-select-option>
            </a-select>
          </a-form-item> -->
          <a-form-item ref="remark" label="权限说明" name="remark">
            <a-textarea
              v-model:value="formState.remark"
              show-count
              :autoSize="true"
              :maxlength="80"
              placeholder="请输入权限说明"
            />
          </a-form-item>

          <a-tabs v-model:activeKey="activeKey">
            <a-tab-pane key="1" tab="菜单&按钮权限">
              <menuComp ref="menuCompKey" :menuIds="formState.menuIds"></menuComp>
            </a-tab-pane>
            <a-tab-pane key="2" tab="数据权限-巡视设备" force-render></a-tab-pane>
            <a-tab-pane key="3" tab="数据权限-电力设备"></a-tab-pane>
          </a-tabs>
        </a-form>
      </div>
      <template #footer>
        <a-space>
          <a-button type="primary" class="primary" @click="onSubmit">提交</a-button>
          <a-button @click="onClose" class="info">取消</a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { getRoleInfo, setRoleInfo, delRoleInfo } from '@/api/system/index.js'
// import { useDictStore } from '@/stores/dict.js'
import { useSysStore } from '@/stores/system.js'
import { message, Modal } from 'ant-design-vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
import menuComp from './component/menuComp.vue'
// 系统配置
let sysConf = useSysStore()
// 字典配置
// let dict = useDictStore()
// let taskControlList = dict.getDictList('taskControl')

//
const activeKey = ref('1')
// 分页查询
const fmSearch = reactive({
  page: 1,
  size: 10
})
const pageInfo = ref({
  list: [],
  total: 0,
  items: 0
})
const getPageInfo = () => {
  getRoleInfo(fmSearch).then((res) => {
    pageInfo.value.list = res.data.list
    pageInfo.value.total = res.data.total
    pageInfo.value.items = res.data.list.length
  })
}
const pageChange = (page, pageSize) => {
  fmSearch.page = page
  getPageInfo()
}
const sizeChange = (page, pageSize) => {
  fmSearch.size = pageSize
  fmSearch.page = 1
  getPageInfo()
}
// 页面加载完成后执行
const tableHeight = ref()
onMounted(() => {
  tableHeight.value = {
    y: document.getElementsByClassName('page')[0].clientHeight * 0.68
  }
  window.onresize = function () {
    tableHeight.value = {
      y: document.getElementsByClassName('page')[0].clientHeight * 0.68
    }
  }
  getPageInfo()
})

// 表单数据
const open = ref(false)
let title = ref('新增')
const formRef = ref()
const menuCompKey = ref()
const formState = reactive({})
const onSubmit = () => {
  const { checkedKeys } = menuCompKey.value
  if (checkedKeys.length === 0) {
    message.error({ content: '菜单权限不能为空', key: 'roleKey1', duration: 2 })
    return
  }
  formState.menuIds = checkedKeys
  formRef.value
    .validate()
    .then(() => {
      message.loading({ content: '提交中...', key: 'roleKey' })
      setRoleInfo(formState).then((res) => {
        if (res.code == 200) {
          message.success({ content: '操作成功', key: 'roleKey', duration: 2 })
          getPageInfo()
          onClose()
        }
      })
    })
    .catch((error) => {
      console.log('error', error)
    })
}
const rules = {
  roleName: [
    {
      required: true,
      message: '请输入角色名称',
      trigger: 'blur'
    },
    { min: 1, max: 20, message: '1到20个字符', trigger: 'blur' }
  ],
  remark: [
    {
      required: true,
      message: '请输入权限说明',
      trigger: 'blur'
    }
  ]
}
const onClose = () => {
  open.value = false
}
// 新增
const roleAdd = () => {
  formState.roleName = null
  open.value = true
  title.value = '新增'
  formState.remark = null
  formState.id = null
  formState.menuIds = []
}

// 删除用户接口
const roleDelConfirm = (roleId) => {
  delRoleInfo({ roleId }).then((res) => {
    if (res.code == 200) {
      message.success({ content: '删除成功', key: 'roleDelKey', duration: 2 })
      getPageInfo()
    }
  })
}
// 编辑
const roleEdit = (record) => {
  open.value = true
  title.value = '编辑'
  formState.id = record.id
  formState.roleName = record.roleName
  formState.remark = record.remark
  formState.menuIds = record.menuIds
}

// 单个删除
const roleDel = (record) => {
  Modal.confirm({
    title: '提示',
    icon: h('i'),
    content: h('div', { class: 'delContent' }, [
      h('span', { class: 'iconfont delIcon tixing' }),
      h('span', { class: 'delContentText' }, `确定删除【${record.roleName}】角色吗？`)
    ]),
    okText: '确认',
    cancelText: '取消',
    closable: true,
    onOk: () => {
      roleDelConfirm(record.id)
    }
  })
}
// 重置事件
const reset = () => {
  fmSearch.page = 1
  fmSearch.size = 10
  fmSearch.roleName = null
  getPageInfo()
}
</script>
<style lang="scss" scoped>
.fromBox {
  .ant-tabs > .ant-tabs-nav .ant-tabs-nav-wrap {
    border-bottom-style: solid;
    border-width: 1px;
    @include border-color('base33414C');
    // border-bottom: 1px solid #33414c !important;
  }
}
</style>
