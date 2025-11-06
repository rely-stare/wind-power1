<template>
  <a-menu :openKeys="menuState.openKeys" :selectedKeys="menuState.selectedKeys" :mode="mode">
    <template v-for="menu in menuList">
      <a-sub-menu
        v-if="menu.children && menu.children.length && mode === 'inline'"
        :key="menu.name"
      >
        <template #icon v-if="menu.meta && menu.meta.iconPic">
          <i class="iconfont" :class="menu.meta.iconPic"></i>
        </template>
        <template #title>
          <span>
            {{ menu.meta && menu.meta.title }}
            <router-link :to="menu.path"></router-link>
          </span>
        </template>
        <MenuList :menuList="menu.children" :mode="mode" />
      </a-sub-menu>
      <a-menu-item :key="menu.path" v-else-if="menu.meta.isShow">
        <template #icon v-if="menu.meta && menu.meta.iconPic">
          <i class="iconfont" :class="menu.meta.iconPic"></i>
        </template>
        <span>
          <router-link :to="menu.path">{{ menu.meta && menu.meta.title }}</router-link>
        </span>
      </a-menu-item>
    </template></a-menu
  >
</template>
<script setup name="MenuList">
import { createFromIconfontCN } from '@ant-design/icons-vue'
import { useMenuStore } from '@/stores/menu.js'
// import MenuList from './index.vue'
const menuState = useMenuStore()
const props = defineProps({
  menuList: {
    type: Array,
    default: () => []
  },
  mode: {
    type: String,
    default: 'horizontal'
  }
})
onMounted(() => {})
const IconFont = createFromIconfontCN({
  scriptUrl: '//at.alicdn.com/t/font_2572336_4hg62uu7hxd.js'
})
</script>
