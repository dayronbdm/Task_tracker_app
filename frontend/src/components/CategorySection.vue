<template>
  <div class="section-card">
    <!-- header row with the title and the add/cancel button -->
    <div class="section-head">
      <h2 class="section-title">
        <span class="section-icon">🗂</span>
        Categories
        <!-- shows the total number of categories as a little badge -->
        <span class="section-badge">{{ categories.length }}</span>
      </h2>
      <!-- clicking this toggles the add form open/closed -->
      <button class="btn btn--primary" @click="toggleForm">
        {{ showForm ? '✕ Cancel' : '+ Add' }}
      </button>
    </div>

    <!-- the add-category form slides in/out using a Vue transition -->
    <Transition name="slide">
      <div v-if="showForm" class="add-form">
        <!-- @submit.prevent stops the page from refreshing on form submit -->
        <form @submit.prevent="addCategory">
          <input
            v-model="newName"
            ref="nameInput"
            class="input"
            placeholder="Category name…"
            maxlength="60"
            required
          />
          <div class="flex gap-2 mt-2">
            <!-- button is disabled while the API call is in progress -->
            <button type="submit" class="btn btn--primary" :disabled="submitting" style="flex:1">
              {{ submitting ? 'Adding…' : '+ Add Category' }}
            </button>
          </div>
        </form>
      </div>
    </Transition>

    <!-- list of all categories - each one shows name, task count, and a delete button -->
    <ul class="cat-list" role="list">
      <li
        v-for="cat in categories"
        :key="cat.id"
        class="cat-item"
        :style="{ '--cat-color': catColor(cat.id) }"  <!-- pass the color as a CSS variable so the dot can use it -->
      >
        <span class="cat-dot"></span>
        <span class="cat-name">{{ cat.name }}</span>
        <!-- pluralize "task" vs "tasks" based on the count -->
        <span class="cat-count">{{ taskCountFor(cat.id) }} task{{ taskCountFor(cat.id) !== 1 ? 's' : '' }}</span>
        <button
          class="btn btn--icon"
          title="Delete category"
          @click="deleteCategory(cat.id, cat.name)"
        >✕</button>
      </li>

      <!-- show a message if there are no categories yet -->
      <li v-if="categories.length === 0" class="cat-empty">
        No categories yet. Add one above.
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
// nextTick lets us wait for Vue to update the DOM before we do something (like focus an input)
import { ref, nextTick } from 'vue'
import { categoriesApi } from '../api/index.ts'
import type { Category, Task } from '../types'
import type { AxiosError } from 'axios'

// props are passed in from the parent (App.vue) - we get the full list of categories and tasks
const props = defineProps<{ categories: Category[]; tasks: Task[] }>()

// we emit events to tell the parent to refresh data or show a toast notification
const emit  = defineEmits<{
  refresh: []
  toast:   [message: string, type: 'success' | 'error']
}>()

// state for the add-category form
const showForm   = ref(false)  // is the form visible?
const newName    = ref('')     // what the user is typing
const submitting = ref(false)  // true while we're waiting for the API
const nameInput  = ref<HTMLInputElement | null>(null)  // reference to the input element so we can focus it

// a fixed set of colors we cycle through for the category dots
const PALETTE = [
  '#6366f1','#8b5cf6','#ec4899','#f59e0b',
  '#10b981','#06b6d4','#f97316','#84cc16',
]
// picks a color from the palette using modulo so it wraps around
const catColor     = (id: number) => PALETTE[(id - 1) % PALETTE.length]
// counts how many tasks belong to a given category
const taskCountFor = (catId: number) => props.tasks.filter(t => t.category?.id === catId).length

// shows or hides the add form and auto-focuses the input when opening
async function toggleForm() {
  showForm.value = !showForm.value
  if (showForm.value) {
    await nextTick()  // wait for Vue to render the input before trying to focus it
    nameInput.value?.focus()
  }
}

// sends the new category to the API
async function addCategory() {
  if (!newName.value.trim()) return  // don't submit if the input is empty
  submitting.value = true
  try {
    await categoriesApi.create({ name: newName.value.trim() })
    newName.value  = ''
    showForm.value = false
    emit('refresh')  // tell parent to re-fetch data
    emit('toast', 'Category added successfully', 'success')
  } catch (err) {
    // try to get the error message from the API response, otherwise use a generic message
    const msg = (err as AxiosError<{ message?: string }>).response?.data?.message ?? 'Failed to add category'
    emit('toast', msg, 'error')
  } finally {
    submitting.value = false  // always reset the loading state
  }
}

// asks the user to confirm before deleting, then calls the API
async function deleteCategory(id: number, name: string) {
  if (!confirm(`Delete "${name}"?\n\nAll tasks in this category will also be deleted.`)) return
  try {
    await categoriesApi.remove(id)
    emit('refresh')
    emit('toast', `"${name}" deleted`, 'success')
  } catch {
    emit('toast', 'Failed to delete category', 'error')
  }
}
</script>

<style scoped>
/* the add-category form that slides in below the header */
.add-form {
  padding: .875rem 1.25rem;
  border-bottom: 1px solid var(--border);
  background: var(--primary-bg);
}

/* slide-down animation for when the form opens and closes */
.slide-enter-active, .slide-leave-active { transition: all .22s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-6px); }

/* scrollable list of categories - max height so it doesn't grow forever */
.cat-list {
  list-style: none;
  max-height: 420px;
  overflow-y: auto;
}

.cat-item {
  display: flex;
  align-items: center;
  gap: .75rem;
  padding: .75rem 1.25rem;
  border-bottom: 1px solid var(--border);
  transition: background var(--transition);
}
.cat-item:last-child { border-bottom: none; }
.cat-item:hover { background: var(--bg); }

.cat-dot {
  flex-shrink: 0;
  width: 10px; height: 10px;
  border-radius: 50%;
  background: var(--cat-color);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--cat-color) 18%, transparent);
}

.cat-name {
  flex: 1;
  font-weight: 600;
  font-size: .875rem;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cat-count {
  font-size: .75rem;
  color: var(--text-muted);
  font-weight: 500;
  white-space: nowrap;
}

.cat-item .btn--icon { opacity: 0; transition: opacity var(--transition); }
.cat-item:hover .btn--icon { opacity: 1; }

.cat-empty {
  padding: 2rem 1.25rem;
  text-align: center;
  color: var(--text-muted);
  font-size: .875rem;
}
</style>
