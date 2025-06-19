<script setup>
import { ref, onMounted } from 'vue'
import { useImportReceiptFormStore } from '@/data/importReceipts.js'
import { useRegulation } from '@/data/regulation.js'
import CRUDMainForm from './CRUDMainForm.vue'
import TitleText from '../texts/TitleText.vue'
import BookTable from '../tables/BookTable.vue'
import BookTableShort from '../tables/BookTableShort.vue'
import TitleFrame from '../frames/TitleFrame.vue'
import ReceiptFormFrame from '../frames/ReceiptFormFrame.vue'
import ButtonReceipt from '../buttons/ButtonReceipt.vue'
import ButtonText from '../texts/ButtonText.vue'
import AppDialog from '../AppDialog.vue'

import BookInReceiptTable from '../tables/BookInReceiptTable.vue'

const props = defineProps({
  importReceipt: {
    type: Object,
    required: true
  }
})
const emit = defineEmits(['close'])
const selectedBook = ref(null);

// Dialog states
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')

const handleSelectBook = (book) => {
  selectedBook.value = book;
  // Automatically bind the import price from the selected book
  importPrice.value = book.import_price || '';
};

const quantity = ref('')
const importPrice = ref('')
const store = useImportReceiptFormStore()
const { regulations, fetchRegulations } = useRegulation()

onMounted(async () => {
  await fetchRegulations()
})

const addBookToReceipt = () => {
  // Validation logic
  if (!selectedBook.value) {
    showValidationDialog('Missing Book Selection', 'Please select a book before adding to receipt.')
    return;
  }

  if (!quantity.value || quantity.value <= 0) {
    showValidationDialog('Invalid Quantity', 'Please enter a valid quantity (greater than 0).')
    return;
  }

  if (!importPrice.value || importPrice.value <= 0) {
    showValidationDialog('Invalid Import Price', 'Please enter a valid import price (greater than 0).')
    return;
  }

  // Business rule validation: minimum import quantity
  if (regulations.value.minImportQuantity && parseInt(quantity.value) < regulations.value.minImportQuantity) {
    showValidationDialog('Quantity Below Minimum', `Quantity must be at least ${regulations.value.minImportQuantity} according to store regulations.`)
    return;
  }

  // Business rule validation: minimum stock before import
  if (regulations.value.minStockBeforeImport && selectedBook.value.quantity >= regulations.value.minStockBeforeImport) {
    showValidationDialog('Stock Above Threshold', `This book has ${selectedBook.value.quantity} in stock. Import is only allowed when stock is below ${regulations.value.minStockBeforeImport}.`)
    return;
  }

  // Check if book already exists in receipt
  const existingBook = props.importReceipt.books.find(b => b.id === selectedBook.value.id)
  if (existingBook) {
    showValidationDialog('Duplicate Book', 'This book is already in the receipt. Please select a different book.')
    return;
  }

  // If all validations pass, add the book
  const newBook = {
    ...selectedBook.value,
    quantity: parseInt(quantity.value),
    import_price: parseInt(importPrice.value),
  };

  props.importReceipt.books.push(newBook);

  // Clear form after successful addition
  selectedBook.value = null;
  quantity.value = '';
  importPrice.value = '';
}

function showValidationDialog(title, message) {
  dialogTitle.value = title
  dialogMessage.value = message
  dialogVisible.value = true
}

function deleteBookInReceipt(book) {
  // importReceipt từ props
  const idx = props.importReceipt.books.findIndex(b => b.id === book.id)
  if (idx !== -1) props.importReceipt.books.splice(idx, 1)
}

async function handleSave() {
  // Validate before saving: Check for empty receipt
  if (!props.importReceipt.books || props.importReceipt.books.length === 0) {
    showValidationDialog('Empty Receipt', 'Please add at least one book to the receipt before saving.')
    return;
  }

  // Frontend validation for total import quantity
  const totalQuantity = props.importReceipt.books.reduce((sum, book) => sum + book.quantity, 0);
  if (regulations.value.minImportQuantity && totalQuantity < regulations.value.minImportQuantity) {
    showValidationDialog(
      'Total Quantity Too Low',
      `The total quantity of all books in this receipt is <strong>${totalQuantity}</strong>, which is below the minimum requirement of <strong>${regulations.value.minImportQuantity}</strong> books.`
    );
    return;
  }

  // Chuyển đổi books -> bookDetails theo đúng yêu cầu backend
  const bookDetails = (props.importReceipt.books || []).map(b => ({
    bookId: b.bookId || b.id,
    name: b.title || b.name,
    publishedYear: b.published_year || b.publishedYear,
    importPrice: b.import_price || b.importPrice,
    quantity: b.quantity,
    authors: Array.isArray(b.authors) ? b.authors : Object.values(b.authors || {}),
    categories: Array.isArray(b.categories) ? b.categories : Object.values(b.categories || {})
  }));

  // Debug payload gửi lên
  const payload = {
    adminId: props.importReceipt.admin || 'admin001',
    bookDetails
  };
  console.log('Payload gửi lên:', payload);

  try {
    if (!props.importReceipt.id && !props.importReceipt.importReceiptId) {
      // Add mới
      await store.createReceipt(payload);
    } else {
      // Update
      await store.updateReceipt(
        props.importReceipt.id || props.importReceipt.importReceiptId,
        payload
      );
    }
    emit('close');
  } catch (error) {
    console.error('Save failed:', error);
    showValidationDialog('Save Failed', 'Failed to save the import receipt. Please try again.');
  }
}
</script>
<template>
  <CRUDMainForm @close="emit('close')">
    <template #title>
      <TitleText>
        <template #text>Edit Import Receipt</template>
      </TitleText>
    </template>
    <template #content>
      <div class="scrollable-content">
        <BookTableShort @select-book="handleSelectBook" :excludedBookIds="importReceipt.books.map(book => book.id)" />

        <div class="frame-wrapper">
          <TitleFrame readonly :modelValue="selectedBook?.title || ''" disabled placeholder="Title" />
          <ReceiptFormFrame v-model="quantity" placeholder="Quantity" />
          <ReceiptFormFrame v-model="importPrice" placeholder="Import Price" />

          <ButtonReceipt @click="addBookToReceipt">
            <template #btn-text>
              <ButtonText><template #text>ADD</template></ButtonText>
            </template>
          </ButtonReceipt>
        </div>

        <BookInReceiptTable :books="importReceipt.books" @delete-book="deleteBookInReceipt" />
        <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
          <ButtonReceipt @click="handleSave">
            <template #btn-text>
              <ButtonText>
                <template #text>
                  {{ props.importReceipt.id || props.importReceipt.importReceiptId ? 'Save' : 'Create' }}
                </template>
              </ButtonText>
            </template>
          </ButtonReceipt>
        </div>
      </div>
    </template>
  </CRUDMainForm>

  <!-- Validation Dialog -->
  <AppDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    :message="dialogMessage"
    :showCancel="false"
  />
</template>
<style scoped>
.scrollable-content {
  max-height: calc(100vh - 150px);
  overflow-y: auto;
  padding-right: 12px;
}

.frame-wrapper {
  display: flex;
  flex-direction: row;
  padding-left: 12px;
  gap: 10px;
}
</style>