<template>
    <div v-if="visible" :class="['loading-overlay', { 'full-screen': fullScreen }]">
        <img src="@/assets/newImg/loading.gif" alt="Loading" class="loading-gif" />
        <p v-if="text" class="loading-text">{{ text }}</p>
    </div>
</template>

<script setup>
import { defineProps, defineExpose, ref } from 'vue';

const props = defineProps({
    text: String,
    fullScreen: {
        type: Boolean,
        default: false,
    },
});

const visible = ref(false);

const show = () => {
    visible.value = true;
};

const hide = () => {
    visible.value = false;
};

// 允许父组件调用 show 和 hide 方法
defineExpose({ show, hide });
</script>

<style scoped>
.loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
}

.full-screen {
    position: fixed;
    z-index: 9999;
}

.loading-gif {
    width: 50px;
    height: 50px;
}

.loading-text {
    margin-top: 10px;
    color: white;
    font-size: 16px;
}
</style>