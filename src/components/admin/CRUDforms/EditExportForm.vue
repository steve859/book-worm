<script setup>
import { ref, computed, onMounted } from 'vue'
import { useExportReceiptFormStore } from '@/data/exportReceipts.js'
import { useUser } from '@/data/user'
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

// Store instances
const store = useExportReceiptFormStore()
const userStore = useUser()

// Constraint related data
const currentCustomer = ref(null)
const originalTotalAmount = ref(0)
const maxDebtLimit = ref(500000) // Default value, can be loaded from parameters

onMounted(() => {
  // Store original amount for comparison
  originalTotalAmount.value = totalAmount.value

  // Load customer data  
  if (props.exportReceipt.userId) {
    currentCustomer.value = userStore.users.find(u => u.id === props.exportReceipt.userId)
  }

  // Store original book quantities for comparison
  props.exportReceipt.originalBooks = props.exportReceipt.books.map(book => ({
    id: book.id,
    quantity: book.quantity
  }))
})

// Tính tổng tiền tự động
const totalAmount = computed(() => {
  if (!props.exportReceipt.books || props.exportReceipt.books.length === 0) {
    return 0
  }
  const total = props.exportReceipt.books.reduce((total, book) => {
    // Prioritize sellPrice, fallback to export_price for backward compatibility
    const price = book.sellPrice || book.export_price || 0
    const quantity = book.quantity || 0
    const bookTotal = price * quantity
    console.log(`[EditExportForm] Book: ${book.title}, Price: ${price}, Qty: ${quantity}, Subtotal: ${bookTotal}`)
    return total + bookTotal
  }, 0)
  console.log(`[EditExportForm] Total calculated: ${total}`)
  return total
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')
const dialogConfirmCallback = ref(null)
const dialogShowCancel = ref(false)

function showValidationDialog(title, message, showCancel = false, confirmCallback = null) {
  dialogTitle.value = title
  dialogMessage.value = message
  dialogVisible.value = true
  dialogShowCancel.value = showCancel
  dialogConfirmCallback.value = confirmCallback
}

function handleDialogConfirm() {
  if (dialogConfirmCallback.value) {
    dialogConfirmCallback.value()
  }
  dialogVisible.value = false
  dialogConfirmCallback.value = null
}

// Computed constraints
const debtIncrease = computed(() => {
  return totalAmount.value - originalTotalAmount.value
})

const projectedDebt = computed(() => {
  if (!currentCustomer.value) return 0
  const currentDebt = currentCustomer.value.debtAmount || 0
  return currentDebt + debtIncrease.value
})

const isExceedingDebtLimit = computed(() => {
  return projectedDebt.value > maxDebtLimit.value
})

// ĐẢM BẢO props.exportReceipt.books luôn là mảng
if (!props.exportReceipt.books) {
  props.exportReceipt.books = []
}

// Normalize existing books to have sellPrice property
props.exportReceipt.books.forEach(book => {
  if (!book.sellPrice && !book.export_price) {
    // If no price is set, calculate from import price
    const importPrice = book.import_price || book.importPrice || 0
    book.sellPrice = Math.round(importPrice * 1.05)
    book.export_price = book.sellPrice // backward compatibility
  } else if (!book.sellPrice && book.export_price) {
    // If only export_price exists, use it as sellPrice
    book.sellPrice = book.export_price
  } else if (book.sellPrice && !book.export_price) {
    // If only sellPrice exists, set export_price for compatibility
    book.export_price = book.sellPrice
  }
})

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

  // Calculate sell price (import price * 1.05 markup)
  const importPrice = selectedBook.value.import_price || selectedBook.value.importPrice || 0
  const sellPrice = Math.round(importPrice * 1.05)

  console.log(`[EditExportForm] Adding book "${selectedBook.value.title}" - Import: ${importPrice}, Sell: ${sellPrice}`)

  const newBook = {
    ...selectedBook.value,
    quantity: parseInt(quantity.value),
    sellPrice: sellPrice,
    // Keep backward compatibility
    export_price: sellPrice
  }
  props.exportReceipt.books.push(newBook)
  selectedBook.value = null
  quantity.value = ''
}

function handleDeleteBook(book) {
  const idx = props.exportReceipt.books.findIndex(b => b.id === book.id)
  if (idx !== -1) props.exportReceipt.books.splice(idx, 1)
}

async function validateConstraints() {
  // Check 1: Empty invoice
  if (!props.exportReceipt.books || props.exportReceipt.books.length === 0) {
    showValidationDialog('Invalid Input', 'An invoice must have at least one book.')
    return false
  }

  // Check 2: Book quantity constraints  
  for (const book of props.exportReceipt.books) {
    // Check if reducing quantity would violate minimum stock
    const originalBook = props.exportReceipt.originalBooks?.find(b => b.id === book.id)
    const originalQty = originalBook?.quantity || 0
    const qtyDiff = book.quantity - originalQty

    if (qtyDiff > 0 && book.availableQuantity && (book.availableQuantity - qtyDiff) < 20) {
      showValidationDialog(
        '⚠️ Stock Constraint Violation',
        `Cannot increase quantity for "<strong>${book.title}</strong>". <br/>
        This would reduce stock below minimum threshold (20 books).<br/>
        Current available: ${book.availableQuantity} books`
      )
      return false
    }
  }

  // Check 3: Customer debt limit
  if (isExceedingDebtLimit.value) {
    showValidationDialog(
      '💳 Debt Limit Exceeded',
      `This change would increase customer debt to <strong>${projectedDebt.value.toLocaleString()} VND</strong>.<br/>
      This exceeds the maximum allowed debt of <strong>${maxDebtLimit.value.toLocaleString()} VND</strong>.<br/><br/>
      Current debt: ${(currentCustomer.value?.debtAmount || 0).toLocaleString()} VND<br/>
      Debt increase: ${debtIncrease.value.toLocaleString()} VND`,
      true,
      () => handleSaveWithWarning()
    )
    return false
  }

  // Check 4: Warn about monthly report impact
  if (Math.abs(debtIncrease.value) > 0) {
    showValidationDialog(
      '📊 Monthly Report Impact',
      `This edit will affect monthly debt reports:<br/><br/>
      • Original amount: <strong>${originalTotalAmount.value.toLocaleString()} VND</strong><br/>
      • New amount: <strong>${totalAmount.value.toLocaleString()} VND</strong><br/>
      • Change: <strong>${debtIncrease.value > 0 ? '+' : ''}${debtIncrease.value.toLocaleString()} VND</strong><br/><br/>
      <strong>Note:</strong> The "Debt Increase" column in monthly reports will be updated to reflect this change.`,
      true,
      () => proceedWithSave()
    )
    return false
  }

  return true
}

async function handleSave() {
  // Validate all constraints first
  const isValid = await validateConstraints()
  if (!isValid) return

  await proceedWithSave()
}

async function handleSaveWithWarning() {
  showValidationDialog(
    '⚠️ Override Debt Limit',
    'Are you sure you want to proceed despite exceeding the debt limit?<br/>This action requires administrator approval.',
    true,
    () => proceedWithSave()
  )
}

async function proceedWithSave() {
  const bookDetails = (props.exportReceipt.books || []).map(b => ({
    bookId: b.bookId || b.id,
    quantity: b.quantity,
  }))

  const payload = {
    userId: props.exportReceipt.userId,
    adminId: props.exportReceipt.admin || 'admin001',
    paidAmount: 0, // All payments handled via Payment Receipt form
    bookDetails
  }
  console.log('[EditExportForm] Saving with payload:', payload);

  try {
    if (!props.exportReceipt.id && !props.exportReceipt.exportReceiptId) {
      // Add new
      await store.addExportReceiptForm(payload);
    } else {
      // Update existing
      await store.updateExportReceiptForm(
        props.exportReceipt.id || props.exportReceipt.exportReceiptId,
        payload
      );
    }

    // Refresh related data to prevent stale state
    console.log('[EditExportForm] Refreshing user data after save...')
    await userStore.fetchUsers()

    showValidationDialog(
      '✅ Invoice Updated Successfully',
      `Invoice has been updated.<br/>
      Customer debt has been ${debtIncrease.value >= 0 ? 'increased' : 'decreased'} by ${Math.abs(debtIncrease.value).toLocaleString()} VND.`,
      false,
      () => emit('close')
    )
  } catch (error) {
    console.error('Save failed:', error)

    // Handle specific constraint violations
    if (error.response?.status === 409) {
      showValidationDialog(
        '🔒 Edit Blocked',
        'This invoice cannot be edited because it has associated payment receipts.<br/>Please remove all payment links before editing.'
      )
    } else if (error.response?.data?.code === 1018) {
      showValidationDialog(
        '💳 Debt Limit Exceeded',
        'The invoice cannot be saved because it would exceed the customer\'s debt limit.'
      )
    } else if (error.response?.data?.code === 1019) {
      showValidationDialog(
        '📦 Insufficient Stock',
        'The invoice cannot be saved because it would reduce book stock below minimum levels.'
      )
    } else {
      const errorMessage = error.response?.data?.message || 'Failed to save the export receipt. Please try again.'
      showValidationDialog('❌ Save Failed', errorMessage)
    }
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
        <!-- Constraint Warnings -->
        <div v-if="isExceedingDebtLimit" class="constraint-warning">
          <v-icon color="warning" size="small">mdi-alert</v-icon>
          <span>Warning: Changes will exceed customer's debt limit ({{ maxDebtLimit.toLocaleString() }} VND)</span>
        </div>

        <div v-if="debtIncrease !== 0" class="info-notice">
          <v-icon color="info" size="small">mdi-information</v-icon>
          <span>Debt Impact: {{ debtIncrease > 0 ? '+' : '' }}{{ debtIncrease.toLocaleString() }} VND</span>
        </div>

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
          @delete-book="handleDeleteBook" />
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
  <AppDialog v-model="dialogVisible" :title="dialogTitle" :message="dialogMessage" :show-cancel="dialogShowCancel"
    @confirm="handleDialogConfirm" @cancel="dialogVisible = false" />
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

.constraint-warning {
  background: rgba(255, 152, 0, 0.1);
  border: 1px solid rgba(255, 152, 0, 0.3);
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: Montserrat;
  font-size: 14px;
  color: var(--vt-c-second-bg-color);
  font-weight: 500;
}

.info-notice {
  background: rgba(33, 150, 243, 0.1);
  border: 1px solid rgba(33, 150, 243, 0.3);
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: Montserrat;
  font-size: 14px;
  color: var(--vt-c-second-bg-color);
}
</style>
