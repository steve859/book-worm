<script setup>
import BackIcon from '@/assets/icons-vue/back.vue'
</script>

<template>
  <button class="button">
    <BackIcon />
  </button>
</template>

<style scoped>
.button {
  background-color: var(--vt-c-main-bg-color);
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  transition: background-color 0.2s;
}

.button:hover {
  background-color: #f0f0f0;
}
</style>