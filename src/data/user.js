// src/data/user.js
import { api } from '@/plugins/axios'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

// Pinia store để quản lý người dùng
export const useUser = defineStore('user', () => {
  const users = ref([])
  const customers = ref([])           // Danh sách customers (không phải admin)
  const searchQuery = ref('')
  const loading = ref(false)
  const error = ref(null)
  const currentUser = ref(null)

  // Computed list đã lọc theo searchQuery
  const filteredUsers = computed(() => {
    const q = searchQuery.value.toLowerCase()
    return users.value.filter(u => {
      const fullName = `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim().toLowerCase()
      return (
          fullName.includes(q) ||
          (u.email ?? '').toLowerCase().includes(q) ||
          u.username.toLowerCase().includes(q)
      )
    })
  })

  /**
   * fetchUsers: Gọi API GET /users với token hợp lệ
   */
  async function fetchUsers() {
    loading.value = true
    error.value = null
    try {
      const resp = await api.get('/users')
      const list = Array.isArray(resp.data)
          ? resp.data
          : Array.isArray(resp.data.result)
              ? resp.data.result
              : []

      users.value = list.map(u => ({
        id: u.id,
        firstName: u.firstName,
        lastName: u.lastName,
        name: `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim(),
        email: u.email ?? '',
        username: u.username,
        dob: u.dob,
        phone: u.phone,
        roles: u.roles || [],
        roleNames: u.roles?.map(r => r.name) || [],
        role: u.roles?.[0]?.name ?? '',
        password: u.password ?? '',
        debtAmount: u.debtAmount,
        _raw: u
      }))
    } catch (e) {
      console.error('[UserStore] fetchUsers failed:',
          e.response?.status,
          e.response?.data || e.message
      )
      error.value = e
    } finally {
      loading.value = false
    }
  }

  /**
   * fetchCustomers: Gọi API GET /users/except-admin
   */
  async function fetchCustomers() {
    loading.value = true
    error.value = null
    try {
      const resp = await api.get('/users/except-admin')
      const list = Array.isArray(resp.data.result)
        ? resp.data.result
        : []
      customers.value = list.map(u => ({
        id: u.id,
        firstName: u.firstName,
        lastName: u.lastName,
        name: `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim(),
        email: u.email ?? '',
        debtAmount: u.debtAmount,
        _raw: u
      }))
    } catch (e) {
      console.error('[UserStore] fetchCustomers failed:', e)
      error.value = e
    } finally {
      loading.value = false
    }
  }

  // Các hàm khác (addUser, updateUserAPI, deleteUserAPI, fetchCurrentUser)...
  async function addUser(userData) {
    loading.value = true; error.value = null
    try {
      userData.password = "11111111"
      await api.post('/users', userData)
      await fetchUsers()
      return true
    } catch (e) {
      error.value = e
      return false
    } finally {
      loading.value = false
    }
  }

  async function updateUserAPI(userId, payload) {
    if (payload.dob instanceof Date) {
      const y = payload.dob.getFullYear()
      const m = String(payload.dob.getMonth() + 1).padStart(2, '0')
      const d = String(payload.dob.getDate()).padStart(2, '0')
      payload.dob = `${y}-${m}-${d}`
    }
    loading.value = true
    try {
      await api.put(`/users/${userId}`, payload)
      await fetchUsers()
    } catch (e) {
      console.error('[UserStore] updateUser failed', e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function deleteUserAPI(userId) {
    loading.value = true; error.value = null
    try {
      await api.delete(`/users/${userId}`)
      await fetchUsers()
    } catch (e) {
      console.error('[UserStore] deleteUserAPI failed:', e)
      error.value = e
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchCurrentUser() {
    loading.value = true; error.value = null
    try {
      const resp = await api.get('/users/myinfo')
      const u = resp.data.result || resp.data
      currentUser.value = {
        id: u.id,
        firstName: u.firstName,
        lastName: u.lastName,
        name: `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim(),
        email: u.email ?? '',
        username: u.username,
        dob: u.dob,
        phone: u.phone,
        roles: u.roles || [],
        roleNames: u.roles?.map(r => r.name) || [],
        role: u.roles?.[0]?.name ?? '',
        debtAmount: u.debtAmount,
        _raw: u
      }
    } catch (e) {
      console.error('[UserStore] fetchCurrentUser failed:', e)
      currentUser.value = null
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    users,
    customers,
    searchQuery,
    filteredUsers,
    loading,
    error,
    currentUser,
    fetchUsers,
    fetchCustomers,   // expose hàm mới
    fetchCurrentUser,
    addUser,
    updateUserAPI,
    deleteUserAPI
  }
})