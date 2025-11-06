<template>
  <!-- !crumbHide -->
  <a-row align="middle" class="breadCrumbPage" v-if="false">
    <a-col :span="6" class="breadLeft">
      <label>当前位置:</label>

      <a-breadcrumb>
        <!-- <a-breadcrumb-item v-for="menu in breadCrumb" :key="menu.name">
          <RouterLink :to="menu.path"> {{ menu.meta && menu.meta.title }} </RouterLink>
        </a-breadcrumb-item> -->
        <a-breadcrumb-item v-for="menu in breadcrumbData" :key="menu.name">
          <RouterLink :to="menu.path"> {{ menu.meta && menu.meta.title }} </RouterLink>
        </a-breadcrumb-item>
      </a-breadcrumb>
    </a-col>
    <a-col :span="12" class="breadCenter">
      <MenuList :menuList="menuChild" v-if="menu.isShow"></MenuList>
    </a-col>
    <a-col :span="6" class="breadcrumbBtns">
      <a-button size="small" type="link" @click="() => router.go(-1)" class="backBtn" v-show="goBackShow">
        <i class="iconfont fanhui"></i> {{ $t('返回') }}</a-button>
    </a-col>
  </a-row>
</template>
<script setup>
import { useMenuStore } from '@/stores/menu.js'
import { RouterLink } from 'vue-router'
const router = useRouter()
const route = useRoute()
let menu = useMenuStore()
const menuChild = computed(() => menu.menuChild)

const breadcrumbData = ref([])
const getBreadcrumbData = () => {
  breadcrumbData.value = route.matched.filter((item) => {
    return item.meta && item.meta.title
  })
}
watch(
  route,
  () => {
    getBreadcrumbData()
  },
  {
    immediate: true
  }
)

// 返回按钮 显示路径
let goBackList = ['/robot/details', '/UAV/details']
let crumbHideList = ['/overview']
const goBackShow = computed(() => {
  return goBackList.includes(route.path) || goBackList.find((key) => route.path.includes(key))
})
const crumbHide = computed(() => {
  return crumbHideList.includes(route.path) || crumbHideList.find((key) => route.path.includes(key))
})
</script>
<style lang="scss" scoped>
.breadcrumbBtns {
  display: flex;
  justify-content: center;
  height: 22px;
}

.breadCrumbPage {
  padding-bottom: 10px;
}

.backBtn {
  @include font_color('base00D1FF');
  font-size: 14px;
}

:deep(.breadCenter) {
  .ant-menu {
    height: 30px;
    line-height: 30px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-bottom: none;

    .ant-menu-item {
      font-size: 14px;
      text-align: center;
      background-image: url('../../assets/img/public/menuBtn.png');
      background-size: 100% 100%;
      background-repeat: no-repeat;

      +.ant-menu-item {
        margin-left: 10px;
      }

      &::after {
        border-bottom-color: transparent;
        border-bottom-width: 0;
      }
    }

    .ant-menu-item-selected {
      // color: $text-Active-color;
      font-weight: 600;
      background-image: url('../../assets//img/public/menuBtn-active.png');
      background-repeat: no-repeat;
      background-size: 100% 100%;

      a {
        background: linear-gradient(to bottom, #00d1ff, #fff);
        background-clip: text;
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
    }

    .ant-menu-item-active {
      // color: $text-Active-color !important;
    }
  }
}

label {
  margin-left: 30px;
  @include font_color('baseA6ACB1');
}

.breadLeft {
  display: flex;
  align-items: center;
  font-size: 14px;
  background: url('../../assets/img/public/crumb.png');
  background-repeat: no-repeat;
  background-position: 0% 100%;
  background-size: 224px;
  height: 32px;
}
</style>
