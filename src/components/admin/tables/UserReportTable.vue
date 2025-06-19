<script setup>
import { ref, watch } from 'vue'
import axios from '@/plugins/axios'

const props = defineProps({
  month: {
    type: Object,
    required: true,
  }
})

const reports = ref([])
const loading = ref(false)

const headers = [
  { title: 'Customer ID', key: 'userId' },
  { title: 'Customer Name', key: 'userName' },
  { title: 'Opening Debt', key: 'openingDebt' },
  { title: 'Debt Increase', key: 'debtIncrease' },
  { title: 'Total Paid', key: 'paidAmount' },
  { title: 'Closing Debt', key: 'closingDebt' },
]

async function fetchReportByMonth(monthDate) {
  if (!monthDate || typeof monthDate.year !== 'number' || typeof monthDate.month !== 'number') return

  const year = monthDate.year
  const month = monthDate.month + 1

  console.log(`[UserReport] Fetching for year: ${year}, month: ${month}`)

  loading.value = true
  try {
    const { data } = await axios.get('/monthlyDebtReports', {
      params: { year, month }
    })
    reports.value = data.result || []
  } catch (e) {
    console.error('[UserReport] Load failed:', e)
    reports.value = [] // Clear data on error
  } finally {
    loading.value = false
  }
}

// Automatically fetch data when the month prop changes
watch(() => props.month, fetchReportByMonth, { immediate: true })
</script>

<template>
  <v-container fluid>
    <div v-if="loading">Loading...</div>
    <v-data-table
      v-else
      :headers="headers"
      :items="reports"
      class="elevation-1"
      item-value="id"
      :items-per-page="-1"
      hide-default-footer
    >
      <template #item.openingDebt="{ item }">{{ item.openingDebt?.toLocaleString() }}</template>
      <template #item.debtIncrease="{ item }">{{ item.debtIncrease?.toLocaleString() }}</template>
      <template #item.paidAmount="{ item }">{{ item.paidAmount?.toLocaleString() }}</template>
      <template #item.closingDebt="{ item }">{{ item.closingDebt?.toLocaleString() }}</template>
    </v-data-table>
  </v-container>
</template>

<style scoped>
.v-data-table {
  background-color: var(--vt-c-main-bg-color);
  border-radius: 12px;
  padding: 12px;
  font-family: Montserrat;
  font-size: 15px;
  font-style: normal;
  font-weight: 500;
  line-height: 140%;
  color: var(--vt-c-second-bg-color);
}
</style>
