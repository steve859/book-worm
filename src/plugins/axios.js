// src/plugins/axios.js
import { getErrorMessage } from '@/data/error'
import axios from 'axios'

export const api = axios.create({
  baseURL: 'http://localhost:8080/bookstore',
  timeout: 5000,
  headers: { 'Content-Type': 'application/json' }
})

// Attach token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}, error => Promise.reject(error))

// Handle responses and errors globally
api.interceptors.response.use(
  response => response,
  error => {
    // If server responded with an error
    const resp = error.response
    if (resp && resp.data) {
      const code = resp.data.code
      // Optional: resp.data.messageParams for interpolation
      const vars = resp.data.messageParams || {}
      const friendly = getErrorMessage(code, vars)
      // You can choose to show a toast/alert here or pass it along
      // For example:
      // alert(friendly) // Removed to let components handle their own errors
      error.friendlyMessage = friendly
    } else if (error.request) {
      error.friendlyMessage = 'Network error. Please check your connection.'
    } else {
      error.friendlyMessage = 'An unexpected error occurred.'
    }
    return Promise.reject(error)
  }
)

export default api
