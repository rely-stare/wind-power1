<template>
    <div class="menu-container">
        <!-- <span style="color:#fff">{{ selectedKeys }}{{ menuItems }}</span> -->
        <a-menu :class="className" :selectedKeys="selectedKeys" :openKeys="openKeys" mode="inline" :theme="theme"
            @select="handleSelect">
            <template v-for="item in menuItems" :key="item.id">
                <!-- 有子菜单的情况 -->
                <template v-if="item.list && item.list.length">
                    <a-sub-menu :key="item.id">
                        <template #title>
                            <span>
                                <component v-if="item.icon" :is="item.icon" />
                                <span>{{ item.label }}</span>
                            </span>
                        </template>
                        <!-- 递归渲染子菜单 -->
                        <template v-for="child in item.list" :key="child.id">
                            <template v-if="child.list && child.list.length">
                                <a-sub-menu :key="child.id">
                                    <template #title>
                                        <span>
                                            <component v-if="child.icon" :is="child.icon" />
                                            <span>{{ child.label }}</span>
                                        </span>
                                    </template>
                                    <a-menu-item v-for="subChild in child.list" :key="subChild.id">
                                        <component v-if="subChild.icon" :is="subChild.icon" />
                                        <span>{{ subChild.label }}</span>
                                    </a-menu-item>
                                </a-sub-menu>
                            </template>
                            <a-menu-item v-else :key="child.id">
                                <component v-if="child.icon" :is="child.icon" />
                                <span>{{ child.label }}</span>
                            </a-menu-item>
                        </template>
                    </a-sub-menu>
                </template>

                <!-- 没有子菜单的情况 -->
                <template v-else>
                    <a-menu-item :key="item.id">
                        <component v-if="item.icon" :is="item.icon" />
                        <span :title="item.label">{{ item.label }}</span>
                    </a-menu-item>
                </template>
            </template>
        </a-menu>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useSiteStore } from '@/stores/site.js'
import { storeToRefs } from 'pinia'
let siteConf = useSiteStore()
const { fanId, hardwareId } = storeToRefs(siteConf)
interface MenuItem {
    id: string
    siteName: string
    label: string
    icon?: any
    list?: MenuItem[]
}

const props = defineProps({
    items: {
        type: Array as () => MenuItem[],
        required: true
    },
    theme: {
        type: String as () => 'light' | 'dark',
        default: 'light'
    },
    defaultSelectedKey: {
        type: Number,
        default: 0
    },
    defaultOpenKeys: {
        type: Array as () => string[],
        default: () => []
    },
    defaultOpenAll: {
        type: Boolean,
        default: true
    },
    className: {
        type: String,
        default: ''
    },
    isSiteFlag: { // 是否是风机
        type: Boolean,
        default: true
    }
})

const emit = defineEmits(['select'])
const openKeys = ref<string[]>(props.defaultOpenKeys)
const menuItems = ref(props.items)
let selectedKeys = computed(() => {
    return [props.defaultSelectedKey]

})
// 获取所有子菜单的key
const getAllSubMenuKeys = (items: MenuItem[]): string[] => {
    const keys: string[] = []
    const getKeys = (menuItems: MenuItem[]) => {
        menuItems.forEach(item => {
            if (item.list && item.list.length) {
                keys.push(item.id)
                getKeys(item.list)
            }
        })
    }
    getKeys(items)
    return keys
}

// 初始化展开的菜单
const initOpenKeys = () => {
    if (props.defaultOpenAll) {
        openKeys.value = getAllSubMenuKeys(props.items)
    }
}

// 选择菜单项处理
const handleSelect = (item: { key: string }) => {
    // selectedKeys.value = [item.key]
    console.log(item, menuItems.value, '点击菜单')
    emit('select', item)
}

// 监听items变化
watch(
    () => props.items,
    (newItems) => {
        menuItems.value = newItems
        if (props.defaultOpenAll) {
            openKeys.value = getAllSubMenuKeys(newItems)
        }
    }
)

// 组件挂载时初始化
onMounted(() => {
    initOpenKeys()
})
</script>

<style lang="scss" scoped>
::v-deep .ant-menu-inline.ant-menu-root .ant-menu-item {
    border-radius: 0 !important;
    margin-top: 0 !important;
}

::v-deep .ant-menu-light .ant-menu-item:hover:not(.ant-menu-item-selected):not(.ant-menu-submenu-selected) {
    color: #fff !important;
}



::v-deep .ant-menu-item {
    line-height: 40px !important;
    height: 40px !important;
    // font-size: 12px !important;
    background: rgba(20, 39, 54, 1);
    border-radius: 0;
}

::v-deep .ant-menu-submenu-title {
    background: rgba(20, 39, 54, 1) !important;
    border-radius: 0 !important;
    line-height: 40px !important;
    height: 40px !important;
    padding: 10px 10px 10px 30px !important
}

::v-deep(.ant-menu-inline .ant-menu-submenu) {
    padding: 0 !important
}

::v-deep .ant-menu {
    width: 100%;
    color: #a6acb1 !important
}

::v-deep .ant-menu-item-selected {
    color: #00d1ff !important
}

::v-deep .ant-menu-submenu-title {
    margin: 0 4px;
}

::v-deep .ant-menu-inline .ant-menu-item {
    padding-left: 10px !important;
    text-align: center;
}

::v-deep .ant-menu-light.ant-menu-inline .ant-menu-item {
    position: relative;
    line-height: 40px !important;
    height: 40px !important;
    border-radius: 0 !important;
    font-size: 14px !important;
}

::v-deep .ant-menu-item:hover {
    background: #142736 !important
}

::v-deep .ant-menu-inline.ant-menu-root .ant-menu-submenu-title>.ant-menu-title-content {
    font-size: 14px !important;
}

::v-deep .ant-menu .ant-menu-item-selected::before {
    background-color: #0E4052 !important;
    width: 0 !important
}

::v-deep .ant-menu .ant-menu-item-selected::after {
    border-right: none !important
}

::v-deep .ant-menu-light .ant-menu-submenu-title {
    color: #B8BDC1
}

::v-deep .ant-menu .ant-menu-submenu-arrow {
    left: 10px !important;
}

::v-deep .ant-menu-item-selected {
    background: #0E4052 !important
}
</style>