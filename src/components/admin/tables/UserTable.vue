<template>
  <v-container fluid>
    <div class="table-wrapper">
      <v-data-table :headers="headers" :items="displayItems" class="elevation-1" item-value="id" :items-per-page="-1"
        height="60vh" hide-default-footer>
        <template #item.action="{ item }">

          <div class="action-icons">
            <v-tooltip text="View" location="top">
              <template #activator="{ props }">
                <div v-bind="props" @click="$emit('view-user', item)" style="cursor: pointer;">
                  <ViewIcon />
                </div>
              </template>
            </v-tooltip>
            <v-tooltip text="Edit" location="top">
              <template #activator="{ props }">
                <div v-bind="props" @click="$emit('edit-user', item)" style="cursor: pointer;">
                  <EditIcon />
                </div>
              </template>
            </v-tooltip>
            <v-tooltip text="Delete" location="top">
              <template #activator="{ props }">

                <div v-bind="props" @click="openDeleteDialog(item.id)" style="cursor: pointer;">
                  <DeleteIcon />
                </div>
              </template>
            </v-tooltip>
          </div>
        </template>

        <template #item.role="{ item }">
          <span>{{ item.roleNames?.join(', ') || item.role || '' }}</span>
        </template>
      </v-data-table>
    </div>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="dialog" width="400" class="delete-dialog" persistent>
      <v-card>
        <v-card-title class="text-h6">Confirm Deletion</v-card-title>
        <v-card-text>
          Are you sure you want to delete user
          <strong>{{ userToDeleteId }}</strong>?
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="grey" variant="text" @click="dialog = false">Cancel</v-btn>
          <v-btn color="var(--vt-c-second-bg-color)" variant="tonal" @click="confirmDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Error Dialog -->
    <AppDialog 
      v-model="errorDialog" 
      title="Cannot Delete User"
      :message="errorMessage" 
      :show-cancel="false"
      @confirm="closeErrorDialog" />
  </v-container>
</template>

<script setup>
import EditIcon from '@/assets/icons-vue/edit.vue'
import ViewIcon from '@/assets/icons-vue/receipt.vue'
import DeleteIcon from '@/assets/icons-vue/trash.vue'
import AppDialog from '../AppDialog.vue'
import { useUser } from '@/data/user'
import { ref, watch, computed } from 'vue'
import { storeToRefs } from 'pinia'

const userStore = useUser()
// Use storeToRefs to maintain reactivity when destructuring
const { filteredUsers } = storeToRefs(userStore)

const props = defineProps({
  filteredItems: Array
})

// Use filtered items from parent if provided, otherwise use store's filtered users
const displayItems = computed(() => props.filteredItems || filteredUsers.value)

// Debug: Watch for changes in filteredUsers
watch(displayItems, (newUsers, oldUsers) => {
  console.log('[UserTable] displayItems changed from', oldUsers?.length, 'to', newUsers?.length)
}, { immediate: true })

const emit = defineEmits(['view-user', 'edit-user', 'delete-user'])

const dialog = ref(false)
const userToDeleteId = ref(null)
const errorDialog = ref(false)
const errorMessage = ref('')

function openDeleteDialog(userId) {
  userToDeleteId.value = userId
  dialog.value = true
}

function confirmDelete() {
  if (userToDeleteId.value) {
    emit('delete-user', userToDeleteId.value)
    dialog.value = false
    userToDeleteId.value = null
  }
}

function showDeleteError(message = 'This user cannot be deleted because it is still tied to other data in the system.') {
  errorMessage.value = message
  errorDialog.value = true
}

function closeErrorDialog() {
  errorDialog.value = false
  errorMessage.value = ''
}

// Expose function to parent component
defineExpose({
  showDeleteError
})

const headers = [
  { title: 'ID', key: 'id' },
  { title: 'Name', key: 'name' },
  { title: 'Username', key: 'username' },
  { title: 'Email', key: 'email' },
  { title: 'DOB', key: 'dob' },
  { title: 'Roles', key: 'role' },
  { title: 'Action', key: 'action', sortable: false }
]
</script>

<style scoped>
/* Wrapper cho scroll */
.table-wrapper {}

.v-data-table {
  background-color: var(--vt-c-main-bg-color);
  border-radius: 12px;
  padding: 12px;
  font-family: Montserrat;
  font-size: 15px;
  font-weight: 500;
  line-height: 140%;
  color: var(--vt-c-second-bg-color);
}

.action-icons {
  display: flex;
  gap: 12px;
}

.delete-dialog .v-card {
  width: 25vw;
  border-radius: 20px;
  background: var(--vt-c-main-bg-color);
}

.delete-dialog .v-card-title {
  color: var(--vt-c-second-bg-color);
  font-weight: bold;
  text-align: center;
}

.delete-dialog .v-card-text {
  font-size: 16px;
  color: var(--vt-c-second-bg-color);
}
</style>
