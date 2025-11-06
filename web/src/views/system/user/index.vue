<template>
  <div class="page">
    <div class="search">
      <a-space wrap>
        <div class="funItem">
          <label>用户名称：</label>
          <a-input v-model:value="fmSearch.userName" @pressEnter="getPageInfo" placeholder="请输入用户名称" />
        </div>
        <div class="funItem">
          <label>姓名：</label>
          <a-input v-model:value="fmSearch.fullName" @pressEnter="getPageInfo" placeholder="请输入姓名" />
        </div>
        <div class="funItem">
          <label>手机号：</label>
          <a-input v-model:value="fmSearch.telephone" @pressEnter="getPageInfo" placeholder="请输入手机号" />
        </div>
        <div class="funItem">
          <label>关联角色：</label>
          <a-select v-model:value="fmSearch.roleId" placeholder="请选择关联角色" @change="getPageInfo">
            <a-select-option :value="item.id" v-for="item in roleList" :key="item.id">
              {{ item.roleName }}
            </a-select-option>
          </a-select>
        </div>
        <div class="funItem">
          <label>启用状态：</label>
          <a-select v-model:value="fmSearch.status" placeholder="请选择启用状态" @change="getPageInfo">
            <a-select-option :value="item.value" v-for="item in status" :key="item.value">
              {{ item.key }}
            </a-select-option>
          </a-select>
        </div>
      </a-space>
      <a-space>
        <a-button type="primary" class="primary" ghost @click="getPageInfo">搜索</a-button>
        <a-button ghost class="info" @click="reset">重置</a-button>
      </a-space>
    </div>
    <a-space class="btns">
      <a-button type="primary" class="primary" ghost @click="userAdd">新增</a-button>
      <a-button type="primary" class="del" danger @click="userDelsAll">批量删除</a-button>
    </a-space>
    <a-table :data-source="pageInfo.list" rowKey="id" :bordered="false" :pagination="false"
      :row-selection="{ selectedRowKeys: tableKey, onChange: onSelectChange }" :scroll="tableHeight">
      <a-table-column key="id" title="序号" :ellipsis="true" align="center">
        <template #default="{ record, index }">
          {{ index + 1 }}
        </template>
      </a-table-column>
      <a-table-column key="id" title="用户名" data-index="userName" :ellipsis="true" align="center" />
      <a-table-column key="id" title="姓名" data-index="fullName" :ellipsis="true" align="center" />
      <a-table-column key="id" title="状态" align="center">
        <template #default="{ record }">
          <a-switch v-model:checked="record.status" @change="changeStatus(record)" :checkedValue="1"
            :unCheckedValue="0">
          </a-switch>
        </template>
      </a-table-column>
      <a-table-column key="id" title="手机号" data-index="telephone" :ellipsis="true" align="center" />
      <a-table-column key="id" title="角色" data-index="roleId" :ellipsis="true" align="center">
        <template #default="{ record }">
          <span>
            {{roleList.find((item) => item.id === record.roleId)?.roleName}}
          </span>
        </template>
      </a-table-column>
      <a-table-column key="id" title="ip地址" data-index="ipAddress" :ellipsis="true" align="center" />

      <a-table-column key="id" title="操作" align="center">
        <template #default="{ record }">
          <span class="editBtn" @click="userEdit(record)"> 编辑 </span>
          <span class="infoBtn" @click="userReset(record)"> 重置 </span>
          <span class="delBtn" @click="userDel(record)"> 删除 </span>
        </template>
      </a-table-column>
    </a-table>
    <div class="pagination">
      <a-pagination :current="fmSearch.page" :pageSize="fmSearch.size" :show-quick-jumper="true" :total="pageInfo.total"
        :showSizeChanger="true" :pageSizeOptions="['10', '20', '40', '50']"
        :show-total="(total, range) => `共 ${total} 条`" @change="pageChange" @showSizeChange="sizeChange" />
      <span class="total">本页共 {{ pageInfo.items }} 条</span>
    </div>
    <a-drawer :title="title" placement="right" :closable="false" :width="450" :open="open" @close="onClose">
      <div class="fromBox">
        <a-form ref="formRef" :model="formState" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
          <a-form-item ref="userName" label="用户名" name="userName">
            <a-input v-model:value.trim="formState.userName" :maxlength="20" placeholder="请输入用户名" />
          </a-form-item>
          <a-form-item ref="fullName" label="姓名" name="fullName">
            <a-input v-model:value.trim="formState.fullName" placeholder="请输入姓名" />
          </a-form-item>
          <a-form-item ref="telephone" label="手机号" name="telephone">
            <a-input v-model:value="formState.telephone" placeholder="请输入手机号" />
          </a-form-item>

          <a-form-item ref="roleId" label="关联角色" name="roleId">
            <a-select v-model:value="formState.roleId" placeholder="请选择关联角色">
              <a-select-option :value="item.id" v-for="item in roleList" :key="item.id">
                {{ item.roleName }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item ref="password" label="密码" name="password" v-if="title === '新增'">
            <!-- <a-input-password v-model:value="formState.password" disabled size="small" /> -->
            <a-input v-model:value="formState.password" disabled />
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
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { getUserInfo, delUserInfo, setUserInfo, getRoleInfo, restUser } from '@/api/system/index.js'
import { useDictStore } from '@/stores/dict.js'
import { useSysStore } from '@/stores/system.js'
import { message, Modal } from 'ant-design-vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
// 系统配置
let sysConf = useSysStore()
// 字典配置
let dict = useDictStore()
let status = reactive(dict.getDictList('status'))

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
  getUserInfo(fmSearch).then((res) => {
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
// 角色列表
let roleList = ref([])
const getRoleList = () => {
  getRoleInfo().then((res) => {
    roleList.value = res.data
    getPageInfo()
  })
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
  getRoleList()
})

// 表单数据
const open = ref(false)
let title = ref('新增')
const formRef = ref()
const formState = reactive({})
const onSubmit = () => {
  formRef.value
    .validate()
    .then(() => {
      message.loading({ content: '提交中...', key: 'userKey' })
      setUserInfo(formState).then((res) => {
        if (res.code == 200) {
          message.success({ content: '操作成功', key: 'userKey', duration: 2 })
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
  userName: [
    {
      required: true,
      message: '请输入用户名',
      trigger: 'blur'
    },
    { min: 1, max: 20, message: '1到20个字符', trigger: 'blur' }
  ],
  fullName: [
    {
      required: true,
      message: '请输入姓名',
      trigger: 'blur'
    },
    { min: 1, max: 20, message: '1到20个字符', trigger: 'blur' }
  ],
  roleId: [
    {
      required: true,
      message: '请选择关联角色',
      trigger: 'change'
    }
  ]
}
const onClose = () => {
  formRef.value.clearValidate()
  open.value = false
}
// 新增
const userAdd = () => {

  open.value = true
  title.value = '新增'
  formState.id = null
  formState.userName = null
  formState.fullName = null
  formState.telephone = null
  formState.roleId = null
  formState.password = sysConf.getSysConf().defaultPassword
}

// 删除用户接口
const userDels = (userIds) => {
  delUserInfo({ userIds }).then((res) => {
    if (res.code == 200) {
      message.success({ content: '删除成功', key: 'userDelKey', duration: 2 })
      getPageInfo()
    }
  })
}
// 编辑
const userEdit = (record) => {
  open.value = true
  title.value = '编辑'
  formState.id = record.id
  formState.userName = record.userName
  formState.fullName = record.fullName
  formState.telephone = record.telephone
  formState.roleId = record.roleId
  formState.password = null
}
// 表格数据选择
let tableKey = ref([])
const onSelectChange = (selectedRowKeys) => {
  tableKey.value = selectedRowKeys
}
// 批量删除
const userDelsAll = () => {
  if (tableKey.value.length === 0) {
    message.error({ content: '请选择要删除的用户', key: 'userDelKey', duration: 2 })
    return
  }
  Modal.confirm({
    title: '提示',
    icon: h(),
    content: h('div', { class: 'delContent' }, [
      h('span', { class: 'iconfont delIcon tixing' }),
      h('span', { class: 'delContentText' }, `确定删除选中用户吗？`)
    ]),
    okText: '确认',
    cancelText: '取消',
    closable: true,
    onOk: () => {
      userDels(tableKey.value.join(','))
    }
  })
}
// 单个删除
const userDel = (record) => {
  Modal.confirm({
    title: '提示',
    icon: h('i'),
    content: h('div', { class: 'delContent' }, [
      h('span', { class: 'iconfont delIcon tixing' }),
      h('span', { class: 'delContentText' }, `确定删除【${record.userName}】用户吗？`)
    ]),
    okText: '确认',
    cancelText: '取消',
    closable: true,
    onOk: () => {
      userDels(record.id)
    }
  })
}
// 修改状态
const changeStatus = (record) => {
  Modal.confirm({
    title: '提示',
    icon: h('i'),
    content: h('div', { class: 'delContent' }, [
      h('span', { class: 'iconfont delIcon tixing' }),
      h('span', { class: 'delContentText' }, `确定修改【${record.userName}】用户状态吗？`)
    ]),
    okText: '确认',
    cancelText: '取消',
    closable: true,
    onOk: () => {
      setUserInfo({
        id: record.id,
        status: record.status == 0 ? 0 : 1,
        userName: record.userName,
        roleId: record.roleId
      }).then((res) => {
        if (res.code == 200) {
          message.success({ content: '修改成功', key: 'userKey', duration: 2 })
          getPageInfo()
        }
      })
    },
    onCancel: () => {
      record.status = record.status == 1 ? 0 : 1
    }
  })
}
// 重置密码
const userReset = (record) => {
  Modal.confirm({
    title: '提示',
    icon: h('i', { class: '' }),
    content: h('div', { class: 'delContent' }, [
      h('span', { class: 'iconfont delIcon tixing' }),
      h('span', { class: 'delContentText' }, `确定重置【${record.userName}】用户密码吗？`)
    ]),
    okText: '确认',
    cancelText: '取消',
    closable: true,
    onOk: () => {
      restUser({
        userId: record.id
      }).then((res) => {
        if (res.code == 200) {
          message.success({ content: '重置成功', key: 'userKey', duration: 2 })
          getPageInfo()
        }
      })
    }
  })
}

// 重置事件
const reset = () => {
  fmSearch.page = 1
  fmSearch.size = 10
  fmSearch.userName = null
  fmSearch.fullName = null
  fmSearch.telephone = null
  fmSearch.roleId = null
  fmSearch.status = null
  getPageInfo()
}
</script>
