<script setup>
import { ref } from 'vue'
import { useBook } from '@/data/book'
import TitleText from './texts/TitleText.vue'
import ButtonText from './texts/ButtonText.vue'
import BookReportTable from './tables/BookReportTable.vue'
import UserReportTable from './tables/UserReportTable.vue'
import VueDatePicker from '@vuepic/vue-datepicker'
import '@vuepic/vue-datepicker/dist/main.css'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'
import axios from '@/plugins/axios'

import BackIcon from '@/assets/icons-vue/back-icon.vue'
import ExportIcon from '@/assets/icons-vue/export.vue'

const book = useBook()

const initialDate = new Date();
const bookMonth = ref({ month: initialDate.getMonth(), year: initialDate.getFullYear() })
const userMonth = ref({ month: initialDate.getMonth(), year: initialDate.getFullYear() })

const currentPage = ref(0)

function prevPage() {
  if (currentPage.value > 0) currentPage.value--
}
function nextPage() {
  if (currentPage.value < 1) currentPage.value++
}

// Hàm xuất Excel cho Book Report
async function exportBookReport() {
  try {
    const year = bookMonth.value.year
    const month = bookMonth.value.month + 1

    // Lấy dữ liệu từ API
    const { data } = await axios.get('/monthlyInventoryReports', {
      params: { year, month }
    })

    const reports = data.result || []

    if (reports.length === 0) {
      alert('No data to export')
      return
    }

    // Chuẩn bị dữ liệu cho Excel
    const excelData = reports.map(item => ({
      'Book ID': item.bookId,
      'Title': item.bookName,
      'Opening Stock': item.openingStock,
      'Stock Increase': item.stockIncrease,
      'Stock Decrease': item.stockDecrease,
      'Closing Stock': item.closingStock
    }))

    // Tạo workbook và worksheet
    const ws = XLSX.utils.json_to_sheet(excelData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'Book Report')

    // Xuất file
    const fileName = `BookReport_${month}_${year}.xlsx`
    const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
    saveAs(new Blob([wbout], { type: 'application/octet-stream' }), fileName)

  } catch (error) {
    console.error('Export failed:', error)
    alert('Failed to export Excel file')
  }
}

// Hàm xuất Excel cho User Report
async function exportUserReport() {
  try {
    const year = userMonth.value.year
    const month = userMonth.value.month + 1

    // Lấy dữ liệu từ API
    const { data } = await axios.get('/monthlyDebtReports', {
      params: { year, month }
    })

    const reports = data.result || []

    if (reports.length === 0) {
      alert('No data to export')
      return
    }

    // Chuẩn bị dữ liệu cho Excel
    const excelData = reports.map(item => ({
      'Customer ID': item.userId,
      'Customer Name': item.userName,
      'Opening Debt': item.openingDebt,
      'Debt Increase': item.debtIncrease,
      'Total Paid': item.paidAmount,
      'Closing Debt': item.closingDebt
    }))

    // Tạo workbook và worksheet
    const ws = XLSX.utils.json_to_sheet(excelData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'User Report')

    // Xuất file
    const fileName = `UserReport_${month}_${year}.xlsx`
    const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
    saveAs(new Blob([wbout], { type: 'application/octet-stream' }), fileName)

  } catch (error) {
    console.error('Export failed:', error)
    alert('Failed to export Excel file')
  }
}

// Hàm xuất Excel chung (gọi hàm phù hợp dựa trên trang hiện tại)
function exportExcel() {
  if (currentPage.value === 0) {
    exportBookReport()
  } else {
    exportUserReport()
  }
}
</script>

<template>
  <div class="content">
    <!-- Header với title và page switcher -->
    <div class="top-bar">
      <div class="left">
        <TitleText><template #text>Bookstore Report</template></TitleText>
      </div>

      <!-- Nút chuyển trang -->
      <div class="page-switcher">
        <button class="icon-btn" @click="prevPage" :disabled="currentPage === 0" aria-label="Trang trước">
          <BackIcon class="back-icon" />
        </button>
        <span style="margin: 0 8px;">{{ currentPage + 1 }}/2</span>
        <button class="icon-btn" @click="nextPage" :disabled="currentPage === 1" aria-label="Trang sau">
          <BackIcon class="back-icon" style="transform: rotate(180deg);" />
        </button>
      </div>
    </div>

    <!-- Nội dung chính có scroll -->
    <div class="scrollable-content">
      <div class="table-wrappers">
        <!-- Trang 1: BookReportTable -->
        <div v-if="currentPage === 0" class="table-block">
          <div class="month-picker-wrapper">
            <VueDatePicker class="month-picker" v-model="bookMonth" month-picker :format="'MM/yyyy'"
              :clearable="false" />
          </div>
          <BookReportTable :month="bookMonth" />
        </div>

        <!-- Trang 2: UserReportTable -->
        <div v-else class="table-block">
          <div class="month-picker-wrapper">
            <VueDatePicker class="month-picker" v-model="userMonth" month-picker :format="'MM/yyyy'"
              :clearable="false" />
          </div>
          <UserReportTable :month="userMonth" />
        </div>
      </div>
    </div>

    <!-- Footer với button Export Excel -->
    <div class="footer">
      <button class="export-button" @click="exportExcel">
        <ExportIcon class="export-icon" />
        <ButtonText><template #text>Export Excel</template></ButtonText>
      </button>
    </div>
  </div>
</template>

<style scoped>
.content {
  width: 100%;
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-shrink: 0;
  /* Không cho phép shrink */
}

.left {
  display: flex;
  align-items: center;
}

/* Nội dung chính có scroll */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  margin-bottom: 20px;
}

.table-wrappers {
  display: flex;
  flex-direction: row;
  height: 100%;
}

.table-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  gap: 12px;
  border-radius: 8px;
}

.month-picker-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.month-picker {}

:deep(.month-picker .dp__input) {
  background-color: var(--vt-c-main-bg-color);
  font-family: Montserrat;
  font-size: 15px;
  font-style: normal;
  width: 130px;
  text-align: center;
  color: var(--vt-c-second-bg-color);
}

/* Footer với button Export */
.footer {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0;
  flex-shrink: 0;
  /* Không cho phép shrink */
  border-top: 1px solid #e0e0e0;
}

/* Style cho button Export Excel */
.export-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background-color: var(--vt-c-second-bg-color);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: Montserrat;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.export-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.export-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.export-icon {
  width: 20px;
  height: 20px;
  color: var(--vt-c-main-bg-color);
}

.page-switcher {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-switcher button {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  border: 1px solid var(--vt-c-second-bg-color);
  background-color: var(--vt-c-main-bg-color);
  cursor: pointer;
  padding: 0;
  font-size: 18px;
}

.page-switcher button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.back-icon {
  width: 20px;
  height: 20px;
  display: block;
  margin: auto;
  vertical-align: middle;
}

.icon-btn {
  width: 32px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--vt-c-main-bg-color);
  border-radius: 10px;
  border: 1px solid var(--vt-c-second-bg-color);
  padding: 0;
  cursor: pointer;
  transition: background 0.2s, border 0.2s;
  /* Loại bỏ font-size nếu không có text trong button */
  font-size: 0;
}

.icon-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.back-icon {
  width: 20px;
  height: 20px;
  display: block;
  margin: 0;
  /* Đảm bảo không bị ảnh hưởng bởi vertical-align mặc định */
  vertical-align: middle;
}
</style>
