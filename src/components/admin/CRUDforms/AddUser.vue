<script setup>
import { reactive, ref } from 'vue'
import { useUser } from '@/data/user'
import ButtonCRUD from '../buttons/ButtonCRUD.vue'
import DatePickerFrame from '../frames/DatePickerFrame.vue'
import FrameRU from '../frames/FrameRU.vue'
import ButtonText from '../texts/ButtonText.vue'
import TitleText from '../texts/TitleText.vue'
import CRUDMainForm from './CRUDMainForm.vue'
import AppDialog from '../AppDialog.vue'

const emit = defineEmits(['close', 'add-user'])
const store = useUser()

// Dialog states
const showDialog = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')
const isSuccess = ref(false)

const newUser = reactive({
  firstName: '',
  lastName: '',
  email: '',
  username: '',
  dob: '',
  phone: '',
  password: '',
})

const handleAdd = async () => {
  if (store.loading) return

  // Validate required fields
  if (!newUser.username || !newUser.password || !newUser.firstName || !newUser.lastName || !newUser.email) {
    showErrorDialog('Validation Error', 'Please fill in all required fields (Username, Password, First Name, Last Name, Email)')
    return
  }

  // Validate username length (backend requires min 4 characters)
  if (newUser.username.length < 4) {
    showErrorDialog('Validation Error', 'Username must be at least 4 characters long')
    return
  }

  // Validate password length (backend requires min 8 characters)
  if (newUser.password.length < 8) {
    showErrorDialog('Validation Error', 'Password must be at least 8 characters long')
    return
  }

  const payload = {
    firstName: newUser.firstName,
    lastName: newUser.lastName,
    email: newUser.email,
    username: newUser.username,
    dob: newUser.dob,
    phone: newUser.phone,
    password: newUser.password
    // Note: roles are automatically assigned as USER by backend
  }

  const ok = await store.addUser({ ...payload })
  if (ok) {
    showSuccessDialog('User Added Successfully', `User <strong>${newUser.username}</strong> has been created successfully with USER role!`)
  } else {
    // Get friendly error message from store
    const errorMsg = store.error?.friendlyMessage ||
      store.error?.response?.data?.message ||
      'Unable to create user. Please check your input and try again.'
    showErrorDialog('Failed to Add User', errorMsg)
  }
}

function showSuccessDialog(title, message) {
  dialogTitle.value = title
  dialogMessage.value = message
  isSuccess.value = true
  showDialog.value = true
}

function showErrorDialog(title, message) {
  dialogTitle.value = title
  dialogMessage.value = message
  isSuccess.value = false
  showDialog.value = true
}

function handleDialogConfirm() {
  if (isSuccess.value) {
    // Reset form on success
    Object.keys(newUser).forEach(key => {
      newUser[key] = ''
    })
    emit('close')
  }
  showDialog.value = false
}
</script>

<template>
  <div class="detail-wrapper">
    <CRUDMainForm title="Add User" :data="newUser" @close="$emit('close')">
      <template #title>
        <TitleText><template #text>Add User</template></TitleText>
      </template>

      <template #content>
        <div class="frame-wrapper">
          <FrameRU v-model="newUser.username" placeholder="Username" />
          <FrameRU v-model="newUser.password" placeholder="Password" />
          <FrameRU v-model="newUser.firstName" placeholder="Firstname" />
          <FrameRU v-model="newUser.lastName" placeholder="Lastname" />
          <FrameRU v-model="newUser.email" placeholder="Email" />

          <DatePickerFrame v-model="newUser.dob" placeholder="DOB" />
          <FrameRU v-model="newUser.phone" placeholder="Phone" />

          <div class="role-info">
            <p>📝 <strong>Note:</strong> New users will automatically be assigned the <strong>USER</strong> role.</p>
          </div>

          <ButtonCRUD @click="handleAdd">
            <template #btn-text>
              <ButtonText><template #text>ADD</template></ButtonText>
            </template>
          </ButtonCRUD>
        </div>
      </template>
    </CRUDMainForm>

    <!-- Success/Error Dialog -->
    <AppDialog v-model="showDialog" :title="dialogTitle" :message="dialogMessage" :show-cancel="false"
      @confirm="handleDialogConfirm" />
  </div>
</template>

<style scoped>
.detail-wrapper {
  color: var(--vt-c-main-bg-color);
  width: 100%;
  height: 100%;
  padding: 12px;
  font-family: Montserrat;
}

.frame-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  padding: 5vh 0;
}

.role-info {
  text-align: center;
  background: rgba(var(--vt-c-second-bg-color-rgb), 0.1);
  border: 1px solid rgba(var(--vt-c-second-bg-color-rgb), 0.2);
  border-radius: 8px;
  padding: 12px 20px;
  margin: 8px 0;
  max-width: 400px;
}

.role-info p {
  margin: 0;
  font-size: 14px;
  color: var(--vt-c-second-bg-color);
  font-family: Montserrat;
}
</style>
