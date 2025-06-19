<script setup>
import CRUDMainForm from './CRUDMainForm.vue'
import TitleText from '../texts/TitleText.vue'
import BookInReceiptTable from '../tables/BookInReceiptTable.vue'

const props = defineProps({
  importReceipt: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close'])

// Helper to format currency
const formatCurrency = (value) => {
  if (!value) return '0 VND'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}
</script>

<template>
  <CRUDMainForm @close="emit('close')">
    <template #title>
      <TitleText>
        <template #text>View Import Receipt #{{ importReceipt.id }}</template>
      </TitleText>
    </template>
    <template #content>
      <div class="scrollable-content">
        <div class="receipt-details">
          <p><strong>Receipt ID:</strong> {{ importReceipt.id }}</p>
          <p><strong>Admin:</strong> {{ importReceipt.admin }}</p>
          <p><strong>Date:</strong> {{ importReceipt.date }}</p>
          <p><strong>Total Amount:</strong> {{ formatCurrency(importReceipt.total) }}</p>
        </div>
        
        <BookInReceiptTable 
          :books="importReceipt.books" 
          :show-actions="false" 
        />
      </div>
    </template>
  </CRUDMainForm>
</template>

<style scoped>
.scrollable-content {
  max-height: calc(100vh - 150px);
  overflow-y: auto;
  padding: 20px;
}

.receipt-details {
  margin-bottom: 20px;
  padding: 15px;
  background-color: var(--vt-c-main-bg-color);
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  color: var(--vt-c-second-bg-color);
  font-family: Montserrat;
}

.receipt-details p {
  margin: 8px 0;
  font-size: 16px;
}

.receipt-details strong {
  font-weight: 600;
  margin-right: 8px;
}
</style>
