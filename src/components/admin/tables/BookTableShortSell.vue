<script setup>
import { computed, ref, watch } from 'vue'
import { useBook } from '@/data/book'
import { storeToRefs } from 'pinia'

const bookStore = useBook()
const { items, fullBookDetails } = storeToRefs(bookStore)

const props = defineProps({
  excludedBookIds: {
    type: Array,
    default: () => []
  }
})

watch(items, (n, o) => {
  console.log('[BookTableShortSell] items changed', o?.length, '=>', n?.length)
}, { immediate: true })

const emit = defineEmits(['select-book'])

const filteredItems = computed(() =>
  items.value.filter(item => !props.excludedBookIds.includes(item.id))
)

const onRowClick = (row) => emit('select-book', row)

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Title', key: 'title' },
  { title: 'Authors', key: 'authors' },
  { title: 'Published Year', key: 'publishedYear' },
  { title: 'Categories', key: 'categories' },
  { title: 'Quantity', key: 'quantity' },
  { title: 'Sell Price', key: 'export_price' }
]
</script>

<template>
  <div class="table-wrapper">
    <v-data-table :headers="headers" :items="filteredItems" item-value="id" :items-per-page="-1" fixed-header
      height="40vh" class="elevation-1" hide-default-footer>
      <template #item="{ item }">
        <tr @click="onRowClick(item)" style="cursor: pointer;">
          <td>{{ item.id }}</td>
          <td>{{ item.title }}</td>
          <td>{{ fullBookDetails[item.id]?.authors?.join(', ') }}</td>
          <td>{{ item.publishedYear }}</td>
          <td>{{ fullBookDetails[item.id]?.categories?.join(', ') }}</td>
          <td>{{ item.quantity }}</td>
          <td>{{ item.export_price }}</td>
        </tr>
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

::v-deep(.v-data-table tbody tr:hover) {
  background-color: var(--vt-c-gradient-bg-color) !important;
}
</style>