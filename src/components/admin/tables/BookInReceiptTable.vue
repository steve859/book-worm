<script setup>
import { ref, computed } from 'vue'
import DeleteIcon from '@/assets/icons-vue/trash.vue'
import AppDialog from '../AppDialog.vue'

const props = defineProps({
  books: {
    type: Array,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['delete-book'])

// Dialog state
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')
const bookToDelete = ref(null)

const openDeleteDialog = (book) => {
  bookToDelete.value = book
  dialogTitle.value = 'Confirm Deletion'
  dialogMessage.value = `Are you sure you want to delete the book <strong>${book.title}</strong> from this receipt?`
  dialogVisible.value = true
}

const confirmDelete = () => {
  if (bookToDelete.value) {
    emit('delete-book', bookToDelete.value)
  }
  dialogVisible.value = false
  bookToDelete.value = null
}

const headers = computed(() => [
  { title: 'ID', key: 'id' },
  { title: 'Title', key: 'title' },
  { title: 'Quantity', key: 'quantity' },
  { title: 'Import Price', key: 'import_price' },
  { title: 'Action', key: 'action', sortable: false, align: 'center' },
])
</script>

<template>
  <div class="table-container">
    <v-data-table
      :headers="headers"
      :items="books"
      class="elevation-1"
      item-value="id"
      :items-per-page="-1"
      hide-default-footer
    >
      <template v-if="props.showActions" #item.action="{ item }">
        <div class="action-icons">
          <v-tooltip text="Delete" location="top">
            <template #activator="{ props: tooltipProps }">
              <div v-bind="tooltipProps" @click="openDeleteDialog(item)" style="cursor: pointer;">
                <DeleteIcon />
              </div>
            </template>
          </v-tooltip>
        </div>
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
