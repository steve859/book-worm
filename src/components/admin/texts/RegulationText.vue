<template>
  <span class="text">
    <slot name="text"></slot>
  </span>
</template>

<style scoped>
.text {
    color: var(--vt-c-second-bg-color);
    font-family: Montserrat;
    font-size: 15px;
    font-style: normal;
    font-weight: 600;
    line-height: 100%; 
}
</style>
