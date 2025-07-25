import { api } from '@/plugins/axios'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUser } from '@/data/user'

export const usePaymentReceipts = defineStore('paymentReceipts', () => {
    const paymentReceipts = ref([])
    const userStore = useUser()
    const loading = ref(false)
    const error = ref(null)

    async function fetchPaymentReceipts() {
        loading.value = true
        error.value = null
        try {
            await userStore.fetchUsers()
            const resp = await api.get('/paymentReceipts')
            const list = Array.isArray(resp.data)
                ? resp.data
                : Array.isArray(resp.data.result)
                    ? resp.data.result
                    : []
            paymentReceipts.value = list.map(p => {
                const payer = userStore.users.find(u => u.id === p.payerId)
                return {
                    id: String(p.paymentReceiptId),
                    payerId: p.payerId,
                    totalAmount: p.totalAmount,
                    createAt: p.createAt,
                    adminId: p.adminId,
                    customer: payer.username,
                    _raw: p
                }

            })
        } catch (e) {
            console.error('[PaymentReceiptsStore] fetchPaymentReceipts failed:',
                e.response?.status,
                e.response?.data || e.message
            )
            error.value = e
        } finally {
            loading.value = false
        }
    }

    async function addPaymentReceipt(paymentReceiptData) {
        try {
            loading.value = true
            error.value = null
            await api.post('/paymentReceipts', paymentReceiptData)
            await fetchPaymentReceipts()
            // Refresh user data to update debt amounts in PaymentReceipt form
            try {
                await userStore.fetchUsers()
            } catch (err) {
                console.warn('[PaymentReceipts] Failed to refresh users after payment creation', err)
            }
            return { success: true }
        } catch (e) {
            error.value = e
            const msg = e.response?.data?.message || e.message || 'Unknown error'
            return { success: false, message: msg }
        } finally {
            loading.value = false
        }
    }

    const updatePaymentReceipt = (updatedPaymentReceipt) => {
        const index = paymentReceipts.value.findIndex(u => u.id === updatedPaymentReceipt.id)
        if (index !== -1) {
            paymentReceipts.value[index] = { ...updatedPaymentReceipt }
        }
    }

    const deletePaymentReceipt = async (receiptId) => {
        try {
            loading.value = true
            error.value = null
            await api.delete(`/paymentReceipts/${receiptId}`)
            await fetchPaymentReceipts()
            // Refresh user data to update debt amounts in PaymentReceipt form
            try {
                await userStore.fetchUsers()
            } catch (err) {
                console.warn('[PaymentReceipts] Failed to refresh users after payment deletion', err)
            }
            return { success: true }
        } catch (e) {
            error.value = e
            const msg = e.response?.data?.message || e.message || 'Failed to delete payment receipt'
            return { success: false, message: msg }
        } finally {
            loading.value = false
        }
    }

    return {
        paymentReceipts,
        fetchPaymentReceipts,
        addPaymentReceipt,
        updatePaymentReceipt,
        deletePaymentReceipt,
    }
})