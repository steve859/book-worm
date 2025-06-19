<script setup>
import { computed, ref, watch } from 'vue'
import { useBook } from '@/data/book'
import { storeToRefs } from 'pinia'

const bookStore = useBook()
// Use storeToRefs to maintain reactivity when destructuring
const { items, fullBookDetails } = storeToRefs(bookStore)

const props = defineProps({
  excludedBookIds: {
    type: Array,
    default: () => []
  }
})

// Debug: Watch for changes in items
watch(items, (newBooks, oldBooks) => {
  console.log('[BookTableShort] items changed from', oldBooks?.length, 'to', newBooks?.length)
}, { immediate: true })

// Debug: Watch for changes in excludedBookIds
watch(() => props.excludedBookIds, (newExcluded, oldExcluded) => {
  console.log('[BookTableShort] excludedBookIds changed from', oldExcluded?.length, 'to', newExcluded?.length)
}, { immediate: true })

const emit = defineEmits(['select-book'])

// Filter out excluded books
const filteredItems = computed(() => 
  items.value.filter(item => !props.excludedBookIds.includes(item.id))
)

const dialog = ref(false)

const onRowClick = (bookRow) => {
  // Emit the book object from the row directly.
  // This object contains all necessary info including 'import_price'.
  emit('select-book', bookRow);
}

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Title', key: 'title' },
  { title: 'Authors', key: 'authors' },
  { title: 'Published Year', key: 'publishedYear' },
  { title: 'Categories', key: 'categories' },
  { title: 'Quantity', key: 'quantity' },
  { title: 'Price', key: 'import_price' }
]
</script>

<template>
  <div class="table-wrapper">
    <v-data-table 
      :headers="headers" 
      :items="filteredItems" 
      item-value="id" 
      :items-per-page="-1" 
      fixed-header
      height="40vh" 
      class="elevation-1" 
      hide-default-footer
    >
      <template #item="{ item }">
        <tr @click="onRowClick(item)" style="cursor: pointer;">
          <td>{{ item.id }}</td>
          <td>{{ item.title }}</td>
          <td>{{ fullBookDetails[item.id]?.authors?.join(', ') }}</td>
          <td>{{ item.publishedYear }}</td>
          <td>{{ fullBookDetails[item.id]?.categories?.join(', ') }}</td>
          <td>{{ item.quantity }}</td>
          <td>{{ item.import_price }}</td>
        </tr>
      </template>

      <template #item.categories="{ item }">
        <span>{{ fullBookDetails[item.id]?.categories?.join(', ') }}</span>
      </template>

      <template #item.authors="{ item }">
        <span>{{ fullBookDetails[item.id]?.authors?.join(', ') }}</span>
      </template>
    </v-data-table>
  </div>
</template>

<style scoped>
.table-wrapper {}

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

/* Hover effect for clickable rows */
::v-deep(.v-data-table tbody tr:hover) {
  background-color: var(--vt-c-gradient-bg-color) !important;
  cursor: pointer;
}
</style>