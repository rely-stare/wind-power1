<template>
  <a-layout-sider v-if="menuChild.length > 0 && !menu.isShow">
    <div class="sider">
      <MenuList :menuList="menuChild" :mode="'inline'"></MenuList>
    </div>
  </a-layout-sider>
</template>
<script setup>
import { useMenuStore } from '@/stores/menu.js'
let menu = useMenuStore()
const menuChild = computed(() => menu.menuChild)
</script>
<style lang="scss" scoped>
.ant-layout-sider {
  @include background-color('base12222F');
  width: 240px !important;
  max-width: 240px !important;
  flex: 0 0 240px !important;
  min-width: 200px !important;
}
:deep(.sider) {
  .ant-menu {
    .ant-menu-item {
      border-left: 0.2px solid transparent;
      width: 100%;
      border-radius: 0;
      font-size: 16px;
      height: 48px;
      line-height: 48px;
      padding-left: 24px !important;
      margin: 0;
      &:active {
        background-color: transparent;
      }
    }

    .ant-menu-submenu {
      border-left: 0.2px solid transparent;
      width: 100%;
      border-radius: 0;
      // padding-left: 24px !important;
      .ant-menu-item {
        padding-left: 48px !important;
      }
      .ant-menu-submenu-title {
        padding-left: 24px !important;
        padding-right: 34px !important;
        height: 48px !important;
        // line-height: 48px;
        margin: 0;
        width: 100%;

        .ant-menu-title-content {
          height: 48px;
          line-height: 48px;
          font-size: 16px;
        }
        .ant-menu-submenu-arrow {
          transform: translateY(-2px);
          inset-inline-end: 16px;
          width: 10px !important;
          &::before {
            transform: rotate(-45deg) translateX(2.5px);
            width: 6px;
            height: 1.5px;
            border-radius: 6px;
          }
          &::after {
            transform: rotate(45deg) translateX(-2.5px);
            width: 6px;
            height: 1.5px;
            border-radius: 6px;
          }
        }
      }
    }
  }
  .ant-menu-item-selected {
    background-color: #0E4052;
}
}
</style>
