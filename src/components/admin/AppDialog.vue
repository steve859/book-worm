<template>
  <v-dialog v-model="visible" max-width="400" class="app-dialog" persistent>
    <v-card>
      <v-card-title class="text-h6">{{ title }}</v-card-title>
      <v-card-text>
        <p v-html="message"></p>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn v-if="showCancel" color="grey" variant="text" @click="cancel">Cancel</v-btn>
        <v-btn color="var(--vt-c-second-bg-color)" variant="tonal" @click="confirm">OK</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  title: String,
  message: String,
  showCancel: {
    type: Boolean,
    default: true
  }
})
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = ref(props.modelValue)

watch(() => props.modelValue, val => {
  visible.value = val
})

function confirm() {
  visible.value = false
  emit('update:modelValue', false)
  emit('confirm')
}

function cancel() {
  visible.value = false
  emit('update:modelValue', false)
  emit('cancel')
}
</script>

<style scoped>
.app-dialog .v-card {
  width: 25vw;
  border-radius: 20px;
  background: var(--vt-c-main-bg-color);
  font-family: Montserrat;
}

.app-dialog .v-card-title {
  color: var(--vt-c-second-bg-color);
  font-weight: bold;
  text-align: center;
}

.app-dialog .v-card-text {
  font-size: 16px;
  color: var(--vt-c-second-bg-color);
  text-align: center;
  padding: 0 24px 20px;
}
</style>
