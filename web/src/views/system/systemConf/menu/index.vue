<template>
  <div class="page" ref="page">
    <a-row>
      <a-col :span="24">
        <div class="search" ref="search">
          <a-space>
            <div class="funItem">
              <label>菜单名称：</label>
              <a-input
                v-model:value="fmSearch.title"
                placeholder="请输入菜单名称"
                @pressEnter="setPageInfo"
              />
            </div>
            <div class="funItem">
              <label>状态：</label>
              <a-select
                v-model:value="fmSearch.status"
                @change="setPageInfo"
                placeholder="请选择状态"
              >
                <a-select-option v-for="item in status" :value="item.value" :key="item.value">{{
                  item.key
                }}</a-select-option>
              </a-select>
            </div>
            <div class="funItem">
              <label>所属角色：</label>
              <a-checkbox-group v-model:value="fmSearch.accessTypeList" @change="setPageInfo">
                <a-checkbox v-for="item in accessType" :key="item.value" :value="item.value">{{
                  item.key
                }}</a-checkbox>
              </a-checkbox-group>
            </div>
          </a-space>
          <a-space>
            <a-button type="primary" class="primary" @click="setPageInfo" ghost>搜索</a-button>
            <a-button ghost class="info" @click="reset">重置</a-button>
          </a-space>
        </div>
        <a-table
          :data-source="data"
          rowKey="id"
          :bordered="false"
          :pagination="false"
          :scroll="tableHeight"
          :expand-icon="customExpandIcon"
        >
          <a-table-column key="id" title="菜单名称" data-index="title" :ellipsis="true" />
          <a-table-column
            key="id"
            title="菜单图标"
            data-index="iconPic"
            align="center"
            width="100px"
          >
            <template #default="{ record }">
              <i class="iconfont" :class="record.iconPic"></i>
            </template>
          </a-table-column>
          <a-table-column
            key="id"
            title="菜单地址"
            data-index="path"
            :ellipsis="true"
            align="center"
          >
          </a-table-column>

          <a-table-column key="id" title="所属角色" align="center">
            <template #default="{ record }">
              <span>
                {{ accessType.find((item) => item.value == record.accessType).key }}
              </span>
            </template>
          </a-table-column>
          <!-- <a-table-column key="id" title="菜单类型" align="center">
            <template #default="{ record }">
              <span>
                {{ menuType.find((item) => item.value == record.menuType).key }}
              </span>
            </template>
          </a-table-column> -->
          <!-- <a-table-column key="id" title="重定向地址" data-index="redirect" align="center" :ellipsis="true" /> -->
          <a-table-column key="id" title="状态" align="center">
            <template #default="{ record }">
              <a-switch
                v-model:checked="record.status"
                :checkedValue="1"
                :unCheckedValue="0"
                @change="changeStatus(record)"
              >
              </a-switch>
            </template>
          </a-table-column>
          <!-- <a-table-column key="id" title="菜单组件" data-index="component" align="center" :ellipsis="true">
          </a-table-column> -->
          <!-- <a-table-column key="id" title="是否显示" align="center">
            <template #default="{ record }">
              <span>
                {{ isShow.find((item) => item.value == record.isShow).key }}
              </span>
            </template>
          </a-table-column> -->
          <!-- <a-table-column key="id" title="是否外链" align="center">
            <template #default="{ record }">
              <span>
                {{ isFrame.find((item) => item.value == record.isFrame).key }}
              </span>
            </template>
          </a-table-column> -->
          <!-- <a-table-column key="id" title="是否缓存" align="center">
            <template #default="{ record }">
              <span>
                {{ isCache.find((item) => item.value == record.isCache).key }}
              </span>
            </template>
          </a-table-column> -->
          <a-table-column key="id" title="操作" align="center">
            <template #default="{ record }">
              <span class="editBtn" @click="menuEdit(record)"> 编辑 </span>
            </template>
          </a-table-column>
        </a-table>
      </a-col>
    </a-row>
    <a-drawer
      title="编辑菜单"
      :width="450"
      placement="right"
      :closable="false"
      :open="open"
      @close="onClose"
    >
      <a-form
        :model="formState"
        :rules="rules"
        ref="formRef"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
      >
        <a-form-item ref="orgName" label="父菜单" name="parentId">
          <a-tree-select
            v-model:value="formState.parentId"
            style="width: 100%"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            placeholder="请选择父菜单"
            allow-clear
            tree-default-expand-all
            :tree-data="data"
            tree-node-filter-prop="label"
            :fieldNames="{
              label: 'title',
              value: 'id',
              children: 'children'
            }"
          >
          </a-tree-select>
        </a-form-item>
        <a-form-item label="菜单名称">
          <a-input v-model:value="formState.title" :maxlength="20" />
        </a-form-item>
        <a-form-item label="菜单图标">
          <a-input v-model:value="formState.iconPic" />
        </a-form-item>
        <a-form-item label="重定向地址">
          <a-input v-model:value="formState.redirect" />
        </a-form-item>
        <a-form-item label="组件地址">
          <a-input v-model:value="formState.component" />
        </a-form-item>
        <a-form-item label="是否显示">
          <a-switch v-model:checked="formState.isShow" :checkedValue="1" :unCheckedValue="0" />
        </a-form-item>
        <a-form-item label="是否外链">
          <a-radio-group v-model:value="formState.isFrame">
            <a-radio :value="item.value" :key="item.value" v-for="item in isFrame">{{
              item.key
            }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="是否缓存">
          <a-radio-group v-model:value="formState.isCache">
            <a-radio :value="item.value" v-for="item in isCache" :key="item.value">{{
              item.key
            }}</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button type="primary" class="primary" @click="onSubmit">提交</a-button>
          <a-button @click="onClose" class="info">取消</a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>
<script setup lang="jsx">
import { message, Modal } from 'ant-design-vue'
import { getPageInfo, setMenuInfo, setMenuEnable } from '@/api/system/index.js'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { useDictStore } from '@/stores/dict.js'
import $X from '@/utils/$X.js'
import { onMounted, reactive } from 'vue'
let dict = useDictStore()

const router = useRouter()
const rules = {
  title: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 1, max: 20, message: '1到20个字符', trigger: 'blur' }
  ],
  redirect: [{ required: true, message: '请输入重定向地址', trigger: 'blur' }],
  component: [{ required: true, message: '请输入组件地址', trigger: 'blur' }],
  isShow: [{ required: true, message: '请选择是否显示', trigger: 'blur' }]
}
let fmSearch = reactive({
  title: null,
  accessTypeList: [],
  status: null
})
let isShow = reactive(dict.getDictList('isShow'))
let isFrame = reactive(dict.getDictList('isFrame'))
let isCache = reactive(dict.getDictList('isCache'))
let menuType = reactive(dict.getDictList('menuType'))
let accessType = reactive(dict.getDictList('accessType'))
let status = reactive(dict.getDictList('status'))
const data = ref([])
let open = ref(false)
const formRef = ref()
const onSubmit = () => {
  formRef.value
    .validate()
    .then(() => {
      message.loading({ content: '提交中...', key: 'menuKey' })
      setMenuInfo(formState).then((res) => {
        if (res.code == 200) {
          message.success({ content: '操作成功', key: 'menuKey', duration: 2 })
          setPageInfo()
          onClose()
        }
      })
    })
    .catch((error) => {
      console.log('error', error)
    })
}

const formState = reactive({})
const menuEdit = (record) => {
  open.value = true
  formState.title = record.title
  formState.iconPic = record.iconPic
  formState.path = record.path
  formState.redirect = record.redirect
  formState.isShow = record.isShow
  formState.isFrame = record.isFrame + ''
  formState.isCache = record.isCache + ''
  formState.component = record.component
  formState.parentId = record.parentId
  formState.id = record.id
}
const onClose = () => {
  open.value = false
}
const tableHeight = ref()
// const page = ref(null)

onMounted(() => {
  setPageInfo()
  tableHeight.value = { y: document.getElementsByClassName('page')[0].clientHeight * 0.8 }
  window.onresize = function () {
    tableHeight.value = { y: document.getElementsByClassName('page')[0].clientHeight * 0.8 }
  }
})
// 修改状态
const changeStatus = (record) => {
  Modal.confirm({
    title: '提示',
    icon: h('i'),
    content: h('div', { class: 'delContent' }, [
      h('span', { class: 'iconfont delIcon tixing' }),
      h('span', { class: 'delContentText' }, `确定修改【${record.title}】菜单状态吗？`)
    ]),
    okText: '确认',
    cancelText: '取消',
    closable: true,
    onOk: () => {
      setMenuEnable({
        menuId: record.id,
        status: record.status == 0 ? 0 : 1
      }).then((res) => {
        if (res.code == 200) {
          message.success({ content: '修改成功', key: 'userKey', duration: 2 })
          setPageInfo()
        }
      })
    },
    onCancel: () => {
      record.status = record.status == 1 ? 0 : 1
    }
  })
}
const setPageInfo = () => {
  getPageInfo(fmSearch).then((res) => {
    data.value = $X.buildTree(res.data, null)
  })
}
const customExpandIcon = ({ expanded, onExpand, record }) => {
  // 定义展开和收起图标的样式

  if (record.children && record.children.length > 0) {
    return expanded ? (
      <i class={'iconfont xiangxia tableIcon'} onClick={(e) => onExpand(record, e)}></i>
    ) : (
      <i class={'iconfont xiangyou tableIcon'} onClick={(e) => onExpand(record, e)}></i>
    )
  }
}

// // 跳转到新增页面
// const menuAdd = () => {
//   router.push('/menu/add')
// }
const reset = () => {
  fmSearch.title = ''
  fmSearch.accessTypeList = []
  fmSearch.status = null
  setPageInfo()
}
</script>
<style scoped lang="scss">
.search {
  display: flex;
  justify-content: space-between;
}
.funItem {
  display: flex;
  justify-content: center;
  align-items: center;
  label {
    width: 100px;
  }
}
</style>
