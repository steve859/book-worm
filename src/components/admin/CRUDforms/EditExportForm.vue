<script setup>
import { ref, computed } from 'vue'
import { useExportReceiptFormStore } from '@/data/exportReceipts.js'
import CRUDMainForm from './CRUDMainForm.vue'
import TitleText from '../texts/TitleText.vue'
import BookTableShortSell from '../tables/BookTableShortSell.vue'
import TitleFrame from '../frames/TitleFrame.vue'
import ReceiptFormFrame from '../frames/ReceiptFormFrame.vue'
import ButtonReceipt from '../buttons/ButtonReceipt.vue'
import ButtonText from '../texts/ButtonText.vue'
import BookOutReceiptTable from '../tables/BookOutReceiptTable.vue'
import AppDialog from '../AppDialog.vue'

const props = defineProps({
  exportReceipt: {
    type: Object,
    required: true
  }
})
const emit = defineEmits(['close'])
const selectedBook = ref(null);
const quantity = ref('')

// Tính tổng tiền tự động
const totalAmount = computed(() => {
  if (!props.exportReceipt.books || props.exportReceipt.books.length === 0) {
    return 0
  }
  return props.exportReceipt.books.reduce((total, book) => {
    const price = book.export_price || book.sellPrice || 0
    const quantity = book.quantity || 0
    return total + (price * quantity)
  }, 0)
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')

function showValidationDialog(title, message) {
  dialogTitle.value = title
  dialogMessage.value = message
  dialogVisible.value = true
}

// ĐẢM BẢO props.exportReceipt.books luôn là mảng
if (!props.exportReceipt.books) {
  props.exportReceipt.books = []
}

const store = useExportReceiptFormStore()

const handleSelectBook = (book) => { selectedBook.value = book }

const addBookToReceipt = () => {
  if (!selectedBook.value) {
    showValidationDialog('Invalid Input', 'Please select a book to add.')
    return
  }
  const quant = parseInt(quantity.value)
  if (isNaN(quant) || quant <= 0) {
    showValidationDialog('Invalid Input', 'Please enter a valid quantity greater than 0.')
    return
  }

  if (quant > selectedBook.value.quantity) {
    showValidationDialog(
      'Invalid Input',
      `Not enough stock for "${selectedBook.value.title}". Available: ${selectedBook.value.quantity}.`
    )
    return
  }

  const newBook = {
    ...selectedBook.value,
    quantity: parseInt(quantity.value)
  }
  props.exportReceipt.books.push(newBook)
  selectedBook.value = null
  quantity.value = ''
}

function handleDeleteBook(book) {
  const idx = props.exportReceipt.books.findIndex(b => b.id === book.id)
  if (idx !== -1) props.exportReceipt.books.splice(idx, 1)
}

async function handleSave() {
  if (!props.exportReceipt.books || props.exportReceipt.books.length === 0) {
    showValidationDialog('Invalid Input', 'An invoice must have at least one book.')
    return
  }

  const bookDetails = (props.exportReceipt.books || []).map(b => ({
    bookId: b.bookId || b.id,
    // name:          b.title || b.name,
    // publishedYear: b.published_year || b.publishedYear,
    quantity: b.quantity,
    // authors:       Array.isArray(b.authors) ? b.authors : Object.values(b.authors || {}),
    // categories:    Array.isArray(b.categories) ? b.categories : Object.values(b.categories || {}),
    // importPrice: b.importPrice || b.import_price
  }))

  const payload = {
    userId: props.exportReceipt.userId,
    adminId: props.exportReceipt.admin || 'admin001',   // Sửa đúng tên props!
    paidAmount: 0, // All payments handled via Payment Receipt form
    bookDetails
  }
  console.log('Payload gửi lên:', payload);

  try {
    if (!props.exportReceipt.id && !props.exportReceipt.exportReceiptId) {
      // Add mới
      await store.addExportReceiptForm(payload);
    } else {
      // Update
      await store.updateExportReceiptForm(
        props.exportReceipt.id || props.exportReceipt.exportReceiptId,
        payload
      );
    }
    emit('close');
  } catch (error) {
    console.error('Save failed:', error)
    const errorMessage = error.response?.data?.message || 'Failed to save the export receipt. Please try again.'
    showValidationDialog('Save Failed', errorMessage)
  }
}
</script>

<template>
  <CRUDMainForm @close="emit('close')">
    <template #title>
      <TitleText>
        <template #text>Edit Export Receipt</template>
      </TitleText>
    </template>
    <template #content>
      <div class="scrollable-content">
        <BookTableShortSell @select-book="handleSelectBook"
          :excludedBookIds="exportReceipt.books.map(book => book.id)" />

        <div class="frame-wrapper">
          <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
          <TitleFrame readonly :modelValue="selectedBook?.title || ''" disabled placeholder="Title" />
          <ReceiptFormFrame v-model="quantity" placeholder="Quantity" />
          <ButtonReceipt @click="addBookToReceipt">
            <template #btn-text>
              <ButtonText><template #text>ADD</template></ButtonText>
            </template>
          </ButtonReceipt>
          </div>
        </div>

        <BookOutReceiptTable :books="exportReceipt.books" :customer="exportReceipt.customer"
          @delete-book="handleDeleteBook " />
        <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
          <div style="font-family: Montserrat; color: var(--vt-c-second-bg-color); font-size: 16px; font-weight: 600;">
            Total: {{ totalAmount.toLocaleString() }} VND
          </div>
          <ButtonReceipt @click="handleSave">
            <template #btn-text>
              <ButtonText>
                <template #text>
                  {{ props.exportReceipt.id || props.exportReceipt.exportReceiptId ? 'Save' : 'Create' }}
                </template>
              </ButtonText>
            </template>
          </ButtonReceipt>
        </div>

      </div>
    </template>
  </CRUDMainForm>
  <AppDialog v-model="dialogVisible" :title="dialogTitle" :message="dialogMessage" :show-cancel="false" />
</template>
<style scoped>
.frame-wrapper {
  display: flex;
  flex-direction: row;
  padding-left: 12px;
  gap: 10px;
}

.scrollable-content {
  max-height: calc(100vh - 150px);
  overflow-y: auto;
  padding-right: 12px;
}
</style>
