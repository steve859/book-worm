<script setup>
import { ref } from 'vue'
import { useBook } from '@/data/book'
import TitleText from './texts/TitleText.vue'
import BookReportTable from './tables/BookReportTable.vue'
import UserReportTable from './tables/UserReportTable.vue'
import VueDatePicker from '@vuepic/vue-datepicker'
import '@vuepic/vue-datepicker/dist/main.css'

import BackIcon from '@/assets/icons-vue/back-icon.vue'

const book = useBook()
const bookMonth = ref(new Date())
const userMonth = ref(new Date())

const currentPage = ref(0)

function prevPage() {
  if (currentPage.value > 0) currentPage.value--
}
function nextPage() {
  if (currentPage.value < 1) currentPage.value++
}
</script>

<template>
  <div style="overflow-y: auto; overflow-x: auto;" class="content">
    <div class="top-bar">
      <div class="left">
        <TitleText><template #text>Bookstore Report</template></TitleText>
      </div>
      <!-- Nút chuyển trang -->
      <div class="page-switcher">
        <button
          class="icon-btn"
          @click="prevPage"
          :disabled="currentPage === 0"
          aria-label="Trang trước"
        >
          <BackIcon class="back-icon" />
        </button>
        <span style="margin: 0 8px;">{{ currentPage + 1 }}/2</span>
        <button
          class="icon-btn"
          @click="nextPage"
          :disabled="currentPage === 1"
          aria-label="Trang sau"
        >
          <BackIcon class="back-icon" style="transform: rotate(180deg);" />
        </button>
      </div>

    </div>
    <div class="table-wrappers">
      <!-- Trang 1: BookReportTable -->
      <div v-if="currentPage === 0" class="table-block">
        <div class="month-picker-wrapper">
          <VueDatePicker class="month-picker" v-model="bookMonth" month-picker :format="'MM/yyyy'" :clearable="false" />
        </div>
        <BookReportTable :month="bookMonth" />
      </div>

      <!-- Trang 2: UserReportTable -->
      <div v-else class="table-block">
        <div class="month-picker-wrapper">
          <VueDatePicker class="month-picker" v-model="userMonth" month-picker :format="'MM/yyyy'" :clearable="false" />
        </div>
        <UserReportTable :month="userMonth" />
      </div>
    </div>
  </div>
</template>

<style scoped>
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
}

.left {
  display: flex;
  align-items: center;
}

.table-wrappers {
  display: flex;
  flex-direction: row;
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
