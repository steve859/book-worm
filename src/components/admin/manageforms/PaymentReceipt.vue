<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUser } from '@/data/user'
import { usePaymentReceipts } from '@/data/paymentReceipts'

import CRUDMainForm from '../CRUDforms/CRUDMainForm.vue'
import TitleText from '../texts/TitleText.vue'
import TitleFrame from '../frames/TitleFrame.vue'
import ReceiptFormFrame from '../frames/ReceiptFormFrame.vue'
import PaymentFormTable from '../tables/PaymentFormTable.vue'
import ButtonCRUD from '../buttons/ButtonCRUD.vue'
import ButtonText from '../texts/ButtonText.vue'

import AppDialog from '../AppDialog.vue'

import PaymentReceiptTable from '../tables/PaymentReceiptTable.vue'
import { onMounted } from 'vue'

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')
const dialogButtonCancel = ref(true)

const onConfirm = ref(() => { })

function showDialog(title, message, options = true, confirmCallback = () => { }) {
  dialogTitle.value = title
  dialogMessage.value = message
  dialogVisible.value = true
  dialogButtonCancel.value = options
  onConfirm.value = confirmCallback
}

const router = useRouter()
const userStore = useUser()
onMounted(() => {
  userStore.fetchUsers()
  paymentReceiptsStore.fetchPaymentReceipts()
})
const paymentReceiptsStore = usePaymentReceipts()

const selectedCustomer = ref({})
const paymentAmount = ref('')


const customersWithDebt = computed(() => {
  return userStore.users.map(user => ({
    ...user,
    debtAmount: user.debtAmount != null ? user.debtAmount.toLocaleString() + ' VND' : '0 VND',
  }))
})

function goBack() {
  router.push('/manage')
}

function handlePayment() {
  if (!selectedCustomer.value || !selectedCustomer.value.name) {
    showDialog('Missing Info', 'Please select a customer', false)
    return;
  }

  if (!paymentAmount.value || paymentAmount.value <= 0) {
    alert('Please enter a valid payment amount');
    return;
  }

  const customerName = selectedCustomer.value.name

  showDialog(
      'Confirm Payment',
      `Are you sure you want to process payment for: ${customerName}?`,
      true,
      () => {
        processPayment()
      }
  )
}

async function processPayment() {
  const payload = {
    payerId: selectedCustomer.value.id,               // 👈 lấy từ customer được chọn
    totalAmount: paymentAmount.value // 👈 đảm bảo là số
  }

  const result = await paymentReceiptsStore.addPaymentReceipt(payload)

  if (result.success) {
    showDialog('Payment Success', `Processed payment for: ${selectedCustomer.value.name}`, false, () => { })
    selectedCustomer.value = {}
    paymentAmount.value = ''
  } else {
    showDialog('Error', result.message || 'Failed to process payment.', false)
  }
}


const handleSelectPayment = (customer) => {
  selectedCustomer.value = customer;
};

const handleDeleteReceipt = async (receipt) => {
  const result = await paymentReceiptsStore.deletePaymentReceipt(receipt.id)

  if (result.success) {
    showDialog('Delete Success', `Payment receipt #${receipt.id} deleted successfully.`, false)
  } else {
    showDialog('Delete Failed', result.message || 'Failed to delete payment receipt.', false)
  }
}
</script>

<template>
  <div class="detail-wrapper">
    <CRUDMainForm @close="goBack">
      <template #title>
        <TitleText><template #text>Payment Receipt</template></TitleText>
      </template>
      <template #content>
        <div class="scrollable-content">
          <PaymentFormTable @select-payment="handleSelectPayment" v-model="selectedCustomer"
                            :customers="customersWithDebt" />
          <div class="action-bar">
            <div class="frame-wrapper">
              <TitleFrame readonly placeholder="Name" :modelValue="selectedCustomer?.name || ''" />
              <TitleFrame readonly placeholder="Debt" :modelValue="selectedCustomer?.debtAmount || '0 VND'" />
              <ReceiptFormFrame placeholder="Amount" v-model="paymentAmount" />
              <ButtonCRUD @click="handlePayment">
                <template #btn-text>
                  <ButtonText><template #text>PROCESS PAYMENT</template></ButtonText>
                </template>
              </ButtonCRUD>
            </div>
          </div>
          <PaymentReceiptTable :receipts="paymentReceiptsStore.paymentReceipts" @delete-receipt="handleDeleteReceipt" />
        </div>
      </template>
    </CRUDMainForm>
  </div>
  <AppDialog v-model="dialogVisible" :title="dialogTitle" :showCancel="dialogButtonCancel" :message="dialogMessage"
             @confirm="onConfirm()" />

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

.detail-wrapper {
  color: var(--vt-c-main-bg-color);
  width: 100%;
  padding: 12px;
  font-family: Montserrat;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.customer-select {
  padding: 6px 10px;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid #ccc;
}
</style>
