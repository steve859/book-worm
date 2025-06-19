<!-- src/components/admin/manageforms/ImportBook.vue -->
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useImportReceiptFormStore } from '@/data/importReceipts'

import TitleText from '../texts/TitleText.vue'
import ReceiptFormTable from '../tables/ReceiptFormTable.vue'
import ButtonCRUD from '../buttons/ButtonCRUD.vue'
import ButtonText from '../texts/ButtonText.vue'
import EditImportForm from '../CRUDforms/EditImportForm.vue'
import ViewImportForm from '../CRUDforms/ViewImportForm.vue'
import SearchFrame from '@/components/admin/frames/SearchFrame.vue'
import BackButton from '../buttons/BackButton.vue'

const router = useRouter()
const store = useImportReceiptFormStore()

const receipts = computed(() => store.receipts)
const editingReceipt = ref(null)
const viewingReceipt = ref(null)
const searchQuery = ref('')

const filteredReceipts = computed(() => {
  if (!searchQuery.value) {
    return receipts.value
  }
  const query = searchQuery.value.toLowerCase()
  return receipts.value.filter(receipt => {
    const totalString = String(receipt.total || '')
    return (
      String(receipt.id).toLowerCase().includes(query) ||
      (receipt.admin || '').toLowerCase().includes(query) ||
      (receipt.date || '').toLowerCase().includes(query) ||
      totalString.includes(query)
    )
  })
})

onMounted(() => {
  store.fetchReceipts()
})

async function handleView(item) {
  const detail = await store.fetchReceiptById(item.id)
  viewingReceipt.value = { ...detail }
}

async function handleEdit(item) {
  const detail = await store.fetchReceiptById(item.id)
  editingReceipt.value = { ...detail }
}

async function deleteReceipt(id) {
  await store.deleteReceipt(id)
}

async function handleAdd() {
  editingReceipt.value = {
    admin: 'admin001',
    date: new Date().toISOString().slice(0, 10),
    total: 0,
    books: []
  }
}

function closeEdit() {
  editingReceipt.value = null
  store.fetchReceipts()
}

function closeView() {
  viewingReceipt.value = null
}

function goBack() {
  router.push('/manage')
}
</script>

<template>
  <div class="main-container">
    <div v-if="!editingReceipt && !viewingReceipt" class="content">
      <div class="top-bar">
        <div class="left">
          <BackButton @click="goBack" />
          <TitleText>
            <template #text>Import Book</template>
          </TitleText>
        </div>
        <SearchFrame v-model="searchQuery" class="right" />
      </div>

      <ReceiptFormTable
        :receipts="filteredReceipts"
        @edit-receipt="handleEdit"
        @delete-receipt="deleteReceipt"
        @view-receipt="handleView"
      />
      
      <ButtonCRUD @click="handleAdd" style="margin-top: 20px;">
        <template #btn-text>
          <ButtonText><template #text>ADD RECEIPT</template></ButtonText>
        </template>
      </ButtonCRUD>
    </div>

    <div v-else-if="editingReceipt" class="detail-wrapper">
      <EditImportForm :importReceipt="editingReceipt" @close="closeEdit" />
    </div>

    <div v-else-if="viewingReceipt" class="detail-wrapper">
      <ViewImportForm :import-receipt="viewingReceipt" @close="closeView" />
    </div>
  </div>
</template>

<style scoped>
.main-container {
  width: 100%;
  height: 100%;
  overflow-y: auto;
}

.content {
  width: 100%;
  height: 100%;
  padding: 20px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 20px;
}

.left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.right {
  flex-shrink: 0;
}

.detail-wrapper {
  color: var(--vt-c-main-bg-color);
  width: 100%;
  height: 100%;
  padding: 12px;
  font-family: Montserrat;
}
</style>
