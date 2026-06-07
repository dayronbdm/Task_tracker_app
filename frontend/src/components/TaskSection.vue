<template>
  <div class="section-card">
    <!-- section header with a badge showing how many tasks match the current filter -->
    <div class="section-head">
      <h2 class="section-title">
        <span class="section-icon">📋</span>
        Tasks
        <span class="section-badge">{{ filteredTasks.length }}</span>
      </h2>
      <!-- toggles the new-task form open/closed -->
      <button class="btn btn--primary" @click="toggleAddForm">
        {{ showAddForm ? '✕ Cancel' : '+ New Task' }}
      </button>
    </div>

    <!-- add-task form that slides in/out -->
    <Transition name="slide">
      <div v-if="showAddForm" class="task-form-wrap">
        <form @submit.prevent="createTask" class="task-form">
          <div class="form-row">
            <div class="form-group" style="flex:2">
              <label class="form-label">Title *</label>
              <input
                v-model="newTask.title"
                ref="addTitleInput"
                class="input"
                placeholder="What needs to be done?"
                maxlength="120"
                required
              />
            </div>
            <div class="form-group" style="flex:1">
              <label class="form-label">Category</label>
              <select v-model="newTask.categoryId" class="input">
                <option :value="null">— None —</option>
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
          </div>
          <div class="form-group mt-2">
            <label class="form-label">Description</label>
            <textarea
              v-model="newTask.description"
              class="input"
              placeholder="Optional details…"
              rows="2"
              style="resize:vertical"
            ></textarea>
          </div>
          <div class="form-footer mt-3">
            <label class="check-label">
              <input type="checkbox" v-model="newTask.completed" class="check-box" />
              Mark as completed
            </label>
            <button type="submit" class="btn btn--primary" :disabled="addingTask">
              {{ addingTask ? 'Adding…' : '+ Add Task' }}
            </button>
          </div>
        </form>
      </div>
    </Transition>

    <!-- filter tabs: All / Active / Completed -->
    <div class="filter-bar">
      <button
        v-for="f in FILTERS"
        :key="f.key"
        :class="['filter-btn', { active: filter === f.key }]"
        @click="filter = f.key"
      >
        {{ f.label }}
        <span class="filter-count">{{ f.count }}</span>
      </button>
    </div>

    <!-- the scrollable list of tasks -->
    <ul class="task-list" role="list">
      <!-- TransitionGroup animates tasks when they are added or removed -->
      <TransitionGroup name="task-item">
        <li
          v-for="task in filteredTasks"
          :key="task.id"
          :class="['task-item', { 'task-item--done': task.completed, 'task-item--editing': editingId === task.id }]"
        >
          <!-- normal view row (shown when the task is not being edited) -->
          <div v-if="editingId !== task.id" class="task-row">
            <!-- circular checkbox button - clicking it toggles the completed state -->
            <button
              :class="['check-circle', { 'check-circle--done': task.completed }]"
              :title="task.completed ? 'Mark incomplete' : 'Mark complete'"
              :aria-label="task.completed ? 'Mark incomplete' : 'Mark complete'"
              @click="toggleComplete(task)"
            >
              <span v-if="task.completed">✓</span>
            </button>

            <!-- title and description text -->
            <div class="task-info">
              <span :class="['task-title', { 'task-title--done': task.completed }]">
                {{ task.title }}
              </span>
              <!-- only show description if the task has one -->
              <span v-if="task.description" class="task-desc">{{ task.description }}</span>
            </div>

            <!-- colored pill showing which category the task belongs to -->
            <span
              v-if="task.category"
              class="cat-badge"
              :style="{
                '--badge-color': catColor(task.category.id),
                '--badge-bg': catColorBg(task.category.id),
              }"
            >{{ task.category.name }}</span>
            <span v-else class="cat-badge cat-badge--none">No category</span>

            <!-- edit and delete buttons, hidden until you hover the row -->
            <div class="task-actions">
              <button class="btn btn--ghost btn--sm" title="Edit task" @click="startEdit(task)">
                ✎ Edit
              </button>
              <button class="btn btn--icon" title="Delete task" @click="deleteTask(task.id, task.title)">
                ✕
              </button>
            </div>
          </div>

          <!-- inline edit panel - replaces the row when editing -->
          <div v-else class="edit-panel">
            <div class="edit-panel-head">Editing task</div>
            <form @submit.prevent="saveEdit(task.id)" class="edit-form">
              <div class="form-row">
                <div class="form-group" style="flex:2">
                  <label class="form-label">Title *</label>
                  <input v-model="editForm.title" class="input" maxlength="120" required />
                </div>
                <div class="form-group" style="flex:1">
                  <label class="form-label">Category</label>
                  <select v-model="editForm.categoryId" class="input">
                    <option :value="null">— None —</option>
                    <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                  </select>
                </div>
              </div>
              <div class="form-group mt-2">
                <label class="form-label">Description</label>
                <textarea
                  v-model="editForm.description"
                  class="input"
                  rows="2"
                  style="resize:vertical"
                ></textarea>
              </div>
              <div class="form-footer mt-3">
                <label class="check-label">
                  <input type="checkbox" v-model="editForm.completed" class="check-box" />
                  Completed
                </label>
                <div class="flex gap-2">
                  <button type="button" class="btn btn--ghost" @click="cancelEdit">Cancel</button>
                  <button type="submit" class="btn btn--success" :disabled="savingEdit">
                    {{ savingEdit ? 'Saving…' : '✓ Save' }}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </li>
      </TransitionGroup>

      <!-- empty state message when no tasks match the current filter -->
      <li v-if="filteredTasks.length === 0" class="task-empty">
        <span class="empty-icon">{{ filter === 'completed' ? '🎉' : '📭' }}</span>
        <p>{{ emptyMessage }}</p>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { tasksApi } from '../api/index.ts'
import type { Category, Task, TaskPayload } from '../types'
import type { AxiosError } from 'axios'

// parent passes in the full list of categories (for the dropdown) and all tasks
const props = defineProps<{ categories: Category[]; tasks: Task[] }>()

// we emit three different events back to the parent
// 'refresh-tasks' only re-fetches tasks (faster), 'refresh' re-fetches everything
const emit  = defineEmits<{
  'refresh-tasks': []
  refresh:         []
  toast:           [message: string, type: 'success' | 'error']
}>()

// the three possible filter states for the tab buttons
type FilterKey = 'all' | 'active' | 'completed'
const filter = ref<FilterKey>('all')  // start with "All" selected

// computed so it updates the counts live as tasks change
const FILTERS = computed(() => [
  { key: 'all'       as FilterKey, label: 'All',       count: props.tasks.length },
  { key: 'active'    as FilterKey, label: 'Active',    count: props.tasks.filter(t => !t.completed).length },
  { key: 'completed' as FilterKey, label: 'Completed', count: props.tasks.filter(t =>  t.completed).length },
])

// returns only the tasks that match the active filter tab
const filteredTasks = computed(() => {
  if (filter.value === 'active')    return props.tasks.filter(t => !t.completed)
  if (filter.value === 'completed') return props.tasks.filter(t =>  t.completed)
  return props.tasks  // 'all' - no filtering needed
})

// different empty-state message depending on which tab is active
const emptyMessage = computed<string>(() => ({
  all:       'No tasks yet. Add your first one above!',
  active:    'No pending tasks — great job!',
  completed: 'Nothing completed yet. Get started!',
}[filter.value]))

// two palettes so each category gets a matching text color and background color
const PALETTE    = ['#6366f1','#8b5cf6','#ec4899','#f59e0b','#10b981','#06b6d4','#f97316','#84cc16']
const PALETTE_BG = ['#eef2ff','#f5f3ff','#fdf2f8','#fffbeb','#ecfdf5','#ecfeff','#fff7ed','#f7fee7']
const catColor   = (id: number) => PALETTE[(id - 1) % PALETTE.length]
const catColorBg = (id: number) => PALETTE_BG[(id - 1) % PALETTE_BG.length]

// state for the new task form
const showAddForm   = ref(false)
const addingTask    = ref(false)
const addTitleInput = ref<HTMLInputElement | null>(null)  // ref to focus the input when the form opens
const newTask = ref<TaskPayload>({ title: '', description: '', categoryId: null, completed: false })

// show/hide the form and auto-focus the title input when it opens
async function toggleAddForm() {
  showAddForm.value = !showAddForm.value
  if (showAddForm.value) {
    await nextTick()  // wait for Vue to render the input first
    addTitleInput.value?.focus()
  }
}

// sends a POST request to create a new task, then resets the form
async function createTask() {
  if (!newTask.value.title.trim()) return  // don't submit blank titles
  addingTask.value = true
  try {
    await tasksApi.create({
      title:       newTask.value.title.trim(),
      description: newTask.value.description.trim(),
      categoryId:  newTask.value.categoryId,
      completed:   newTask.value.completed,
    })
    // reset form fields after successful creation
    newTask.value     = { title: '', description: '', categoryId: null, completed: false }
    showAddForm.value = false
    emit('refresh-tasks')
    emit('toast', 'Task created', 'success')
  } catch (err) {
    // grab the first validation error from the API or fall back to a generic message
    const msg = (err as AxiosError<{ errors?: string[] }>).response?.data?.errors?.[0] ?? 'Failed to create task'
    emit('toast', msg, 'error')
  } finally {
    addingTask.value = false
  }
}

// flips the completed flag by sending a PUT with the opposite value
async function toggleComplete(task: Task) {
  try {
    await tasksApi.update(task.id, {
      title:       task.title,
      description: task.description,
      categoryId:  task.category?.id ?? null,
      completed:   !task.completed,  // flip it
    })
    emit('refresh-tasks')
  } catch {
    emit('toast', 'Failed to update task', 'error')
  }
}

// inline editing state - stores which task is being edited and its current form values
const editingId  = ref<number | null>(null)  // null means nothing is being edited
const savingEdit = ref(false)
const editForm   = ref<TaskPayload>({ title: '', description: '', categoryId: null, completed: false })

// populate the edit form with the current task data and mark it as being edited
function startEdit(task: Task) {
  editingId.value = task.id
  editForm.value  = {
    title:       task.title,
    description: task.description ?? '',  // description might be null in the DB
    categoryId:  task.category?.id ?? null,
    completed:   task.completed,
  }
}

// close the edit panel without saving
function cancelEdit() {
  editingId.value = null
}

// save the edited task by sending a PUT request
async function saveEdit(taskId: number) {
  if (!editForm.value.title.trim()) return
  savingEdit.value = true
  try {
    await tasksApi.update(taskId, {
      title:       editForm.value.title.trim(),
      description: editForm.value.description.trim(),
      categoryId:  editForm.value.categoryId,
      completed:   editForm.value.completed,
    })
    editingId.value = null  // close the edit panel
    emit('refresh-tasks')
    emit('toast', 'Task updated', 'success')
  } catch {
    emit('toast', 'Failed to save changes', 'error')
  } finally {
    savingEdit.value = false
  }
}

// delete a task after a confirmation dialog
async function deleteTask(id: number, title: string) {
  if (!confirm(`Delete task "${title}"?`)) return
  try {
    await tasksApi.remove(id)
    // if the task being deleted is currently open in the edit panel, close it
    if (editingId.value === id) editingId.value = null
    emit('refresh-tasks')
    emit('toast', 'Task deleted', 'success')
  } catch {
    emit('toast', 'Failed to delete task', 'error')
  }
}
</script>

<style scoped>
/* the add-task form container with a light orange background */
.task-form-wrap {
  padding: 1rem 1.25rem;
  background: var(--primary-bg);
  border-bottom: 1px solid var(--border);
}
.task-form { display: flex; flex-direction: column; }
.form-row  { display: flex; gap: .75rem; flex-wrap: wrap; }
/* each form field column: label on top, input below */
.form-group { display: flex; flex-direction: column; gap: .3rem; flex: 1; min-width: 140px; }
.form-label { font-size: .75rem; font-weight: 600; color: var(--text-soft); text-transform: uppercase; letter-spacing: .04em; }
.form-footer { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: .5rem; }
.check-label { display: flex; align-items: center; gap: .4rem; font-size: .875rem; color: var(--text-soft); cursor: pointer; user-select: none; }
.check-box   { width: 15px; height: 15px; accent-color: var(--primary); cursor: pointer; }

/* slide animation - uses max-height trick to animate height from 0 to the content size */
.slide-enter-active, .slide-leave-active { transition: all .22s ease; overflow: hidden; }
.slide-enter-from, .slide-leave-to { opacity: 0; max-height: 0; padding-top: 0; padding-bottom: 0; }
.slide-enter-to, .slide-leave-from { max-height: 320px; }

/* the row of All / Active / Completed filter tab buttons */
.filter-bar {
  display: flex;
  gap: .25rem;
  padding: .625rem 1.25rem;
  border-bottom: 1px solid var(--border);
  background: #fafbfc;
}
.filter-btn {
  display: flex; align-items: center; gap: .35rem;
  padding: .3rem .75rem;
  border: 1.5px solid transparent;
  border-radius: 99px;
  font-size: .8rem; font-weight: 500;
  cursor: pointer;
  background: transparent; color: var(--text-muted);
  transition: all var(--transition);
}
.filter-btn:hover { color: var(--text); background: var(--bg); }
.filter-btn.active {
  background: var(--primary-bg);
  color: var(--primary);
  border-color: rgba(232,85,42,.25);
  font-weight: 600;
}
.filter-count {
  font-size: .7rem; font-weight: 700;
  background: currentColor;
  color: white;
  border-radius: 99px;
  padding: .05rem .4rem;
  line-height: 1.5;
  opacity: .8;
}
.filter-btn.active .filter-count { opacity: 1; }

/* scrollable task list with a max height so it doesn't push everything off screen */
.task-list {
  list-style: none;
  max-height: 560px;
  overflow-y: auto;
}

/* individual task row */
.task-item {
  border-bottom: 1px solid var(--border);
  transition: background var(--transition);
}
.task-item:last-child { border-bottom: none; }
.task-item:hover { background: #fafbfc; }
.task-item--editing { background: #fff5f0; }  /* light orange tint when the edit form is open */

/* the horizontal flex row that holds checkbox, title, badge, and action buttons */
.task-row {
  display: flex;
  align-items: center;
  gap: .75rem;
  padding: .8rem 1.25rem;
}

/* circular toggle button for marking tasks complete/incomplete */
.check-circle {
  flex-shrink: 0;
  width: 22px; height: 22px;
  border-radius: 50%;
  border: 2px solid var(--border);
  background: var(--surface);
  cursor: pointer;
  display: grid; place-items: center;
  font-size: .7rem; font-weight: 700;
  color: transparent;  /* hide the checkmark until the task is done */
  transition: all var(--transition);
}
/* hover: show a green preview of the checkmark */
.check-circle:hover           { border-color: var(--success); color: var(--success); background: var(--success-bg); }
/* done state: filled green circle with white checkmark */
.check-circle--done           { border-color: var(--success); background: var(--success); color: #fff; }
/* hover on a done task: turns red to hint you can un-complete it */
.check-circle--done:hover     { background: var(--danger); border-color: var(--danger); color: #fff; }

.task-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: .15rem;
}
.task-title {
  font-size: .875rem;
  font-weight: 600;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color var(--transition);
}
/* strike through the title and grey it out when the task is completed */
.task-title--done {
  color: var(--text-muted);
  text-decoration: line-through;
}
.task-desc {
  font-size: .775rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cat-badge {
  flex-shrink: 0;
  font-size: .72rem; font-weight: 600;
  padding: .2rem .6rem;
  border-radius: 99px;
  white-space: nowrap;
  color: var(--badge-color);
  background: var(--badge-bg);
}
.cat-badge--none {
  color: var(--text-muted);
  background: var(--bg);
}

/* action buttons are hidden by default and only appear on hover - keeps the list clean */
.task-actions { display: flex; align-items: center; gap: .25rem; flex-shrink: 0; }
.btn--sm { padding: .3rem .6rem; font-size: .77rem; }
.task-item .task-actions .btn--ghost,
.task-item .task-actions .btn--icon { opacity: 0; transition: opacity var(--transition); }
.task-item:hover .task-actions .btn--ghost,
.task-item:hover .task-actions .btn--icon { opacity: 1; }

.edit-panel { padding: .875rem 1.25rem; background: #fff5f0; }
.edit-panel-head {
  font-size: .7rem; font-weight: 700; text-transform: uppercase;
  letter-spacing: .06em; color: var(--primary);
  margin-bottom: .625rem;
}
.edit-form { display: flex; flex-direction: column; }

.task-empty {
  display: flex; flex-direction: column; align-items: center;
  gap: .625rem; padding: 3rem 1.25rem;
  color: var(--text-muted); font-size: .875rem;
  text-align: center;
}
.empty-icon { font-size: 2.5rem; }

/* TransitionGroup animation - tasks slide in from the left and fade out when removed */
.task-item-enter-active { transition: all .2s ease; }
.task-item-leave-active { transition: all .18s ease; position: absolute; width: 100%; }
.task-item-enter-from, .task-item-leave-to { opacity: 0; transform: translateX(-8px); }
</style>
