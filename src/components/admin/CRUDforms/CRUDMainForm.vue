<script setup>
import BackButton from '../buttons/BackButton.vue'
const emit = defineEmits(['close'])
</script>

<template>
  <div class="main-form">
    <div class="header">
      <div class="header-left">
        <BackButton @click="emit('close')" />
        <div class="title">
          <slot name="title" />
        </div>
      </div>
      <div class="header-right">
        <slot name="header-actions"></slot>
      </div>
    </div>
    <div class="content-wrapper">
      <div class="form-content">
        <slot name="content" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-wrapper {
  color: var(--vt-c-main-bg-color);
  width: 100%;
  padding: 12px;
  font-family: Montserrat;
}

.top-group {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;       
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
