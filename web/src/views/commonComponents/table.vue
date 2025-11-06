<!-- 表格组件 -->
<template>
  <div class="table">
    <a-table :data-source="list" rowKey="id" :bordered="false" :pagination="false" :scroll="tableHeight"
      :columns="columns">
      <template #index="{ index }">
        {{ index + 1 }}
      </template>
      <!-- 操作项插槽 -->
      <template #action="{ record }">
        <slot class="editBtn" name="action" :record="record"></slot>
        <!-- <a-button @click="showDetial(record)">查看</a-button> -->
      </template>
    </a-table>
    <div class="pagination" v-if="props.isPagination">
      <a-pagination :current="pageInfo.pageNum" :pageSize="pageInfo.pageSize" :show-quick-jumper="true" :total="total"
        :showSizeChanger="true" :pageSizeOptions="['10', '20', '40', '50']"
        :show-total="(total, range) => `共 ${total} 条`" @change="pageChange" @showSizeChange="sizeChange" />
      <span class="total">本页共 {{ pageInfo.pageSize }} 条</span>
    </div>
  </div>
</template>
<script setup>
const props = defineProps({
  columns: {
    type: Array,
    default: []
  },
  list: {
    type: Array,
    default: []
  },
  total: {
    type: Number,
    default: 0
  },
  isPagination: {
    type: Boolean,
    default: true
  }
})
const emit = defineEmits(['getPageInfo'])
const pageInfo = ref({
  pageNum: 1,
  pageSize: 10
})
// 页面加载完成后执行
const tableHeight = ref()
onMounted(() => {
  tableHeight.value = {
    y: document.getElementsByClassName('page-box')[0].clientHeight * 0.68
  }
  window.onresize = function () {
    tableHeight.value = {
      y: document.getElementsByClassName('page-box')[0].clientHeight * 0.68
    }
  }
})
const pageChange = (page, pageSize) => {
  pageInfo.value.pageNum = page
  emit('getPageInfo', { pageNum: page, pageSize: pageSize })
}
const sizeChange = (page, pageSize) => {
  pageInfo.value.pageSize = pageSize
  pageInfo.value.pageNum = 1
  emit('getPageInfo', { page: 1, pageSize: pageSize })
}
const showDetial = (record) => {
  console.log('查看', record)
  emit('goDetial', record)
}
</script>
<style lang="scss" scope>
.table .pagination {
  position: fixed;
  bottom: 30px;
  right: 40px;
}

.table .pagination .ant-pagination-item-container .ant-pagination-item-ellipsis {
  color: rgba(0, 209, 255, 0.5) !important;
}


.table .ant-pagination-options {
  display: none !important;
}
</style>
