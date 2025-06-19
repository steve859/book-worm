<script setup>
import {defineProps, defineEmits, ref, onMounted} from 'vue'
import {useUser} from '@/data/user'
import EditIcon from '@/assets/icons-vue/edit.vue'
import ViewIcon from '@/assets/icons-vue/receipt.vue'
import DeleteIcon from '@/assets/icons-vue/trash.vue'
import AppDialog from '../AppDialog.vue'

const props = defineProps({ receipts: Array })
const emit = defineEmits(['view-receipt', 'edit-receipt', 'delete-receipt'])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')
const receiptToDelete = ref(null)
const userStore = useUser()

onMounted(() => {
  userStore.fetchUsers()
})

function openDeleteDialog(receipt) {
  receiptToDelete.value = receipt
  dialogTitle.value = 'Confirm Deletion'
  dialogMessage.value = `Are you sure you want to delete the receipt with ID <strong>#${receipt.id}</strong>?`
  dialogVisible.value = true
}

function confirmDelete() {
  if (receiptToDelete.value) {
    emit('delete-receipt', receiptToDelete.value.id)
  }
  dialogVisible.value = false
  receiptToDelete.value = null
}

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Admin', key: 'admin' },
  { title: 'Date', key: 'date' },
  { title: 'Total', key: 'total' },
  { title: 'Action', key: 'action', sortable: false, align: 'center' }
]
</script>

<template>
  <div class="table-container">
    <v-data-table :headers="headers" :items="props.receipts" item-value="id" hide-default-footer>
      <template #item.action="{ item }">
        <div class="action-icons">
          <v-tooltip text="View" location="top">
            <template #activator="{ props: tooltipProps }">
              <div v-bind="tooltipProps" @click="$emit('view-receipt', item)" style="cursor: pointer;">
                <ViewIcon />
              </div>
            </template>
          </v-tooltip>
          <v-tooltip text="Edit" location="top">
            <template #activator="{ props: tooltipProps }">
              <div v-bind="tooltipProps" @click="$emit('edit-receipt', item)" style="cursor: pointer;">
                <EditIcon />
              </div>
            </template>
          </v-tooltip>
          <v-tooltip text="Delete" location="top">
            <template #activator="{ props: tooltipProps }">
              <div v-bind="tooltipProps" @click="openDeleteDialog(item)" style="cursor: pointer;">
                <DeleteIcon />
              </div>
            </template>
          </v-tooltip>
        </div>
      </template>

      <template #item.admin="{ item }">
        <span>{{ userStore.users?.find(u => u.id === item.admin)?.name }}</span>
      </template>
      <template #item.date="{ item }">
        <span>{{ item.date }}</span>
      </template>
      <template #item.total="{ item }">
        <span>{{ item.total }}</span>
      </template>
    </v-data-table>

    <AppDialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      :message="dialogMessage" 
      @confirm="confirmDelete"
      :show-cancel="true" 
    />
  </div>
</template>

<style scoped>
.table-container {
  width: 100%;
}

.v-data-table {
  background-color: var(--vt-c-main-bg-color);
  border-radius: 12px;
  padding: 12px;
  font-family: Montserrat;
  font-size: 15px;
  color: var(--vt-c-second-bg-color);
}

::v-deep(.v-data-table__th) {
  background-color: var(--vt-c-main-bg-color) !important;
  color: var(--vt-c-second-bg-color) !important;
  font-weight: 600 !important;
}

::v-deep(.v-data-table-header__sort-btn) {
  color: var(--vt-c-second-bg-color) !important;
}

.action-icons {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
