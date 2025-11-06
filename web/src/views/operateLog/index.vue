<template>
  <div class="page page-max-height">
    <div class="search">
      <a-space wrap>
        <div class="funItem">
          <label>操作用户：</label>
          <a-input v-model:value="fmSearch.operName" @pressEnter="getPageInfo" placeholder="请输入用户名称" />
        </div>
        <div class="funItem">
          <label>操作IP：</label>
          <a-input v-model:value="fmSearch.operIp" @pressEnter="getPageInfo" placeholder="请输入操作IP" />
        </div>
        <div class="funItem">
          <label>菜单：</label>
          <a-input v-model:value="fmSearch.title" @pressEnter="getPageInfo" placeholder="请输入菜单名称" />
        </div>
        <div class="funItem">
          <label>操作类型：</label>
          <a-select v-model:value="fmSearch.businessType" placeholder="请选择操作类型" @change="getPageInfo">
            <a-select-option :value="item.value" v-for="item in businessTypes" :key="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </div>
        <div class="funItem">
          <label>开始时间：</label>
          <a-date-picker v-model:value="fmSearch.beginTime" placeholder="请选择开始日期" style="width: 100%"
            valueFormat="YYYY-MM-DD HH:mm:ss" show-time @ok="getPageInfo" />
        </div>
        <div class="funItem">
          <label>结束时间：</label>
          <a-date-picker v-model:value="fmSearch.endTime" placeholder="请选择结束日期" style="width: 100%"
            valueFormat="YYYY-MM-DD HH:mm:ss" show-time @ok="getPageInfo" />
        </div>
        <div class="funItem">
          <label>操作状态：</label>
          <a-checkbox-group v-model:value="status" :options="optionStatus" @change="getPageInfo" />
        </div>
      </a-space>
      <a-space>
        <a-button type="primary" class="primary" ghost @click="getPageInfo">搜索</a-button>
        <a-button ghost class="info" @click="reset">重置</a-button>
      </a-space>
    </div>

    <a-table :data-source="pageInfo.list" rowKey="id" :bordered="false" :pagination="false" :scroll="tableHeight">
      <a-table-column key="id" title="序号" :ellipsis="true" align="center">
        <template #default="{ record, index }">
          {{ index + 1 }}
        </template>
      </a-table-column>
      <a-table-column key="operId" title="操作用户" data-index="operName" :ellipsis="true" align="center" />
      <a-table-column key="id" title="操作时间" data-index="operTime" :ellipsis="true" align="center" />

      <a-table-column key="id" title="操作IP" data-index="operIp" :ellipsis="true" align="center" />

      <a-table-column key="id" title="菜单" data-index="title" :ellipsis="true" align="center" />
      <a-table-column key="id" title="操作类型" data-index="businessType" :ellipsis="true" align="center">
        <template #default="{ record, index }">
          {{businessTypes.find((item) => item.value === record.businessType)?.label}}
        </template></a-table-column>
      <a-table-column key="id" title="操作状态" data-index="status" :ellipsis="true" align="center">
        <template #default="{ record, index }">
          {{optionStatus.find((item) => item.value === record.status)?.label}}
        </template>
      </a-table-column>
    </a-table>
    <div class="pagination">
      <a-pagination :current="fmSearch.pageNum" :pageSize="fmSearch.pageSize" :show-quick-jumper="true"
        :total="pageInfo.total" :showSizeChanger="true" :pageSizeOptions="['10', '20', '40', '50']"
        :show-total="(total, range) => `共 ${total} 条`" @change="pageChange" @showSizeChange="sizeChange" />
      <span class="total">本页共 {{ pageInfo.items }} 条</span>
    </div>
  </div>
</template>
<script setup>
import { getLog } from '@/api/system/index.js'
import { useSysStore } from '@/stores/system.js'
import { ref } from 'vue'
// 系统配置
let sysConf = useSysStore()
let optionStatus = ref([
  { label: '成功', value: 0 },
  { label: '失败', value: 1 }
])
let businessTypes = ref([
  { label: '新增', value: 1 },
  { label: '修改', value: 2 },
  { label: '删除', value: 3 },
  { label: '导出', value: 5 },
  { label: '其他', value: 0 }
])
// 分页查询
let status = ref([])
const fmSearch = ref({
  pageNum: 1,
  pageSize: 10,
  operName: null,
  operIp: null,
  title: null,
  businessType: null,
  beginTime: null,
  endTime: null,
  status: null
})
const pageInfo = ref({
  list: [],
  total: 0,
  items: 0
})
const getPageInfo = () => {
  fmSearch.value.status = status.value.join(',')
  getLog(fmSearch.value).then((res) => {
    console.log('**********', res)
    pageInfo.value.list = res.rows
    pageInfo.value.total = res.total
    pageInfo.value.items = res.rows.length
  })
}
const pageChange = (page, pageSize) => {
  fmSearch.value.pageNum = page
  getPageInfo()
}
const sizeChange = (page, pageSize) => {
  fmSearch.value.pageSize = pageSize
  fmSearch.value.pageNum = 1
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

// 重置事件
const reset = () => {
  fmSearch.value = {
    pageSize: 10,
    pageNum: 1,
    operName: null,
    operIp: null,
    title: null,
    businessType: null,
    beginTime: null,
    endTime: null,
    status: null
  }
  status.value = []
  getPageInfo()
}
</script>
<style scoped>
.pagination {
  position: relative;
  margin-top: 10px;
  bottom: 0;
  right: 0;
}

::v-deep .pagination .ant-pagination-item-container .ant-pagination-item-ellipsis {
  color: rgba(0, 209, 255, 0.5) !important;
}

.page-max-height {
  height: 100% !important;
}
</style>