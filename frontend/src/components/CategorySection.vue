<template>
  <div class="section-card">
    <!-- Head -->
    <div class="section-head">
      <h2 class="section-title">
        <span class="section-icon">🗂</span>
        Categories
        <span class="section-badge">{{ categories.length }}</span>
      </h2>
      <button class="btn btn--primary" @click="toggleForm">
        {{ showForm ? '✕ Cancel' : '+ Add' }}
      </button>
    </div>

    <!-- Add form -->
    <Transition name="slide">
      <div v-if="showForm" class="add-form">
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
            <button type="submit" class="btn btn--primary" :disabled="submitting" style="flex:1">
              {{ submitting ? 'Adding…' : '+ Add Category' }}
            </button>
          </div>
        </form>
      </div>
    </Transition>

    <!-- List -->
    <ul class="cat-list" role="list">
      <li
        v-for="cat in categories"
        :key="cat.id"
        class="cat-item"
        :style="{ '--cat-color': catColor(cat.id) }"
      >
        <span class="cat-dot"></span>
        <span class="cat-name">{{ cat.name }}</span>
        <span class="cat-count">{{ taskCountFor(cat.id) }} task{{ taskCountFor(cat.id) !== 1 ? 's' : '' }}</span>
        <button
          class="btn btn--icon"
          title="Delete category"
          @click="deleteCategory(cat.id, cat.name)"
        >✕</button>
      </li>

      <li v-if="categories.length === 0" class="cat-empty">
        No categories yet. Add one above.
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { categoriesApi } from '../api/index.js'

const props  = defineProps({ categories: Array, tasks: Array })
const emit   = defineEmits(['refresh', 'toast'])

const showForm   = ref(false)
const newName    = ref('')
const submitting = ref(false)
const nameInput  = ref(null)

const PALETTE = [
  '#6366f1','#8b5cf6','#ec4899','#f59e0b',
  '#10b981','#06b6d4','#f97316','#84cc16',
]
const catColor    = (id) => PALETTE[(id - 1) % PALETTE.length]
const taskCountFor = (catId) => props.tasks.filter(t => t.category?.id === catId).length

async function toggleForm() {
  showForm.value = !showForm.value
  if (showForm.value) {
    await nextTick()
    nameInput.value?.focus()
  }
}

async function addCategory() {
  if (!newName.value.trim()) return
  submitting.value = true
  try {
    await categoriesApi.create({ name: newName.value.trim() })
    newName.value  = ''
    showForm.value = false
    emit('refresh')
    emit('toast', 'Category added successfully', 'success')
  } catch (err) {
    const msg = err.response?.data?.message ?? 'Failed to add category'
    emit('toast', msg, 'error')
  } finally {
    submitting.value = false
  }
}

async function deleteCategory(id, name) {
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
/* ── Add form ── */
.add-form {
  padding: .875rem 1.25rem;
  border-bottom: 1px solid var(--border);
  background: var(--primary-bg);
}

.slide-enter-active, .slide-leave-active { transition: all .22s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-6px); }

/* ── Category list ── */
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
