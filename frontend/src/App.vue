<template>
  <!-- main wrapper div that holds the whole page -->
  <div class="app">

    <!-- top header bar - stays visible even when you scroll down -->
    <header class="app-header">
      <div class="header-inner">
        <!-- app logo/name on the left side -->
        <div class="header-brand">
          <div class="brand-icon">✓</div>
          <div>
            <h1 class="brand-title">Task Tracker</h1>
            <p class="brand-sub">Stay organized. Get things done.</p>
          </div>
        </div>
        <!-- quick stats on the right - pending, done, and categories count -->
        <div class="header-stats">
          <div class="stat-chip">
            <span class="stat-num">{{ pending }}</span>
            <span class="stat-lbl">Pending</span>
          </div>
          <div class="stat-chip stat-chip--green">
            <span class="stat-num">{{ done }}</span>
            <span class="stat-lbl">Done</span>
          </div>
          <div class="stat-chip stat-chip--violet">
            <span class="stat-num">{{ categories.length }}</span>
            <span class="stat-lbl">Categories</span>
          </div>
        </div>
      </div>
    </header>

    <!-- main page content -->
    <main class="app-main">
      <!-- show a spinner while we wait for the API to respond -->
      <div v-if="loading" class="global-loader">
        <div class="spinner"></div>
        <span>Loading…</span>
      </div>

      <!-- once data is loaded, show the two sections side by side -->
      <div v-else class="dashboard">
        <CategorySection
          :categories="categories"
          :tasks="tasks"
          @refresh="refreshAll"
          @toast="showToast"
        />
        <TaskSection
          :categories="categories"
          :tasks="tasks"
          @refresh-tasks="fetchTasks"
          @refresh="refreshAll"
          @toast="showToast"
        />
      </div>
    </main>

    <!-- small popup notification at the bottom right corner, disappears after 3.5 seconds -->
    <Transition name="toast">
      <div v-if="toast" :class="['toast', `toast--${toast.type}`]" role="alert">
        <span class="toast-icon">{{ toast.type === 'success' ? '✓' : '✕' }}</span>
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
// importing the Vue utilities we need
// ref() makes a variable reactive (Vue will re-render when it changes)
// computed() creates a value that updates automatically based on other refs
// onMounted() runs code after the component first appears on screen
import { ref, computed, onMounted } from 'vue'

// our API helper functions that call the Spring Boot backend
import { categoriesApi, tasksApi } from './api/index.ts'

// the child components - one for categories, one for tasks
import CategorySection from './components/CategorySection.vue'
import TaskSection from './components/TaskSection.vue'

// TypeScript interfaces so we know exactly what shape our data has
import type { Category, Task, Toast } from './types'

// reactive state - Vue watches these and re-renders the template when they change
const categories = ref<Category[]>([])
const tasks      = ref<Task[]>([])
const loading    = ref(true)  // true while the first API call is in progress
const toast      = ref<Toast | null>(null)  // null means the toast is hidden
let   toastTimer: ReturnType<typeof setTimeout> | null = null  // so we can cancel the auto-hide timer

// computed values automatically recalculate when tasks changes
const pending = computed(() => tasks.value.filter(t => !t.completed).length)
const done    = computed(() => tasks.value.filter(t =>  t.completed).length)

// fetch all categories from the API and store them
async function fetchCategories() {
  const { data } = await categoriesApi.getAll()
  categories.value = data
}

// fetch all tasks from the API and store them
async function fetchTasks() {
  const { data } = await tasksApi.getAll()
  tasks.value = data
}

// runs both fetches at the same time using Promise.all so it's faster
async function refreshAll() {
  await Promise.all([fetchCategories(), fetchTasks()])
}

// displays a toast notification - if one is already showing we cancel its timer first
// default type is 'success' but can also be 'error'
function showToast(message: string, type: Toast['type'] = 'success') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { message, type }
  // auto-hide the toast after 3.5 seconds
  toastTimer = setTimeout(() => (toast.value = null), 3500)
}

// this runs once when the page loads - fetch the data right away
// if the backend isn't running, show an error instead of crashing the app
onMounted(async () => {
  try {
    await refreshAll()
  } catch {
    showToast('Cannot reach the API — is the backend running on :8081?', 'error')
  } finally {
    loading.value = false  // hide the spinner no matter what
  }
})
</script>

<style>
/* remove default margin/padding from everything - browsers add spacing by default and it breaks layouts */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

/* CSS variables (custom properties) - storing colors and sizes here so I only have to change them in one place */
:root {
  /* main orange/red brand color */
  --primary:        #e8552a;
  --primary-dim:    #c94420;   /* slightly darker orange for hover states */
  --primary-bg:     #fff3ee;   /* very light orange background tint */
  --success:        #10b981;   /* green for completed tasks */
  --success-bg:     #ecfdf5;
  --danger:         #ef4444;   /* red for delete actions */
  --danger-dim:     #dc2626;
  --danger-bg:      #fef2f2;
  --violet:         #8b5cf6;   /* purple used for the categories stat chip */
  --violet-bg:      #f5f3ff;

  /* page and surface colors */
  --bg:             #f1f5f9;   /* light gray page background */
  --surface:        #ffffff;   /* white card background */
  --border:         #e2e8f0;   /* subtle border color */
  --border-focus:   #e8552a;   /* border color when an input is focused */

  /* text colors - going from darkest to most faded */
  --text:           #0f172a;
  --text-soft:      #475569;
  --text-muted:     #94a3b8;

  /* border radius sizes - sm for small elements, lg for cards */
  --r:              10px;
  --r-sm:           6px;
  --r-lg:           16px;

  /* box shadow presets */
  --shadow:         0 1px 3px rgba(0,0,0,.08), 0 1px 2px rgba(0,0,0,.05);
  --shadow-md:      0 4px 12px rgba(0,0,0,.08);
  --shadow-lg:      0 10px 24px rgba(0,0,0,.10);

  /* how fast hover/focus transitions animate */
  --transition:     .18s ease;
}

/* make html and body fill the entire screen height */
html, body { height: 100%; }

/* global font and background settings */
body {
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  font-size: 14px;
  background: var(--bg);
  color: var(--text);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;  /* makes fonts look sharper on Mac */
}

/* custom scrollbar styling so it looks cleaner */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: var(--border); border-radius: 99px; }

/* the root div takes up at least the full screen and stacks header + main vertically */
.app { min-height: 100vh; display: flex; flex-direction: column; }

/* sticky header with a gradient background - position:sticky keeps it on screen while scrolling */
.app-header {
  background: linear-gradient(135deg, #431407 0%, #c2390f 100%);
  color: #fff;
  padding: 0 1.5rem;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 12px rgba(0,0,0,.25);
}
.header-inner {
  max-width: 1440px;
  margin: 0 auto;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}
.header-brand { display: flex; align-items: center; gap: .875rem; }
.brand-icon {
  width: 40px; height: 40px;
  background: var(--primary);
  border-radius: var(--r-sm);
  display: grid; place-items: center;
  font-size: 1.2rem; font-weight: 700;
  box-shadow: 0 0 0 3px rgba(232,85,42,.35);
}
.brand-title { font-size: 1.2rem; font-weight: 700; letter-spacing: -.3px; }
.brand-sub   { font-size: .75rem; color: rgba(255,255,255,.55); margin-top: 1px; }

.header-stats { display: flex; gap: .625rem; }
.stat-chip {
  display: flex; flex-direction: column; align-items: center;
  padding: .35rem .75rem;
  background: rgba(255,255,255,.10);
  border: 1px solid rgba(255,255,255,.12);
  border-radius: var(--r-sm);
  min-width: 62px;
}
.stat-chip--green  { background: rgba(16,185,129,.15); border-color: rgba(16,185,129,.25); }
.stat-chip--violet { background: rgba(139,92,246,.20); border-color: rgba(139,92,246,.3); }
.stat-num { font-size: 1.15rem; font-weight: 700; line-height: 1; }
.stat-lbl { font-size: .65rem; font-weight: 500; color: rgba(255,255,255,.6); margin-top: 2px; text-transform: uppercase; letter-spacing: .04em; }

/* flex:1 makes the main area grow and fill leftover space between header and bottom */
.app-main { flex: 1; padding: 1.75rem 1.5rem; }

/* full-page centered spinner shown while data is loading */
.global-loader {
  display: flex; flex-direction: column; align-items: center; gap: .75rem;
  padding: 4rem 0; color: var(--text-muted);
}
/* the spinning loading circle - only the top border has color so it looks like a spinner */
.spinner {
  width: 32px; height: 32px;
  border: 3px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* two-column grid layout: categories panel on left (fixed 360px), tasks take the rest */
.dashboard {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 1.5rem;
  max-width: 1440px;
  margin: 0 auto;
  align-items: start;
}
/* on small screens, stack the two sections vertically instead */
@media (max-width: 860px) {
  .dashboard { grid-template-columns: 1fr; }
}

/* white card that wraps each section (used by both CategorySection and TaskSection) */
.section-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow);
  overflow: hidden;
}
.section-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--border);
  background: #fafbfc;
}
.section-title {
  display: flex; align-items: center; gap: .5rem;
  font-size: .9rem; font-weight: 700; color: var(--text);
}
.section-badge {
  font-size: .7rem; font-weight: 600;
  padding: .15rem .5rem;
  border-radius: 99px;  /* pill shape */
  background: var(--primary-bg);
  color: var(--primary);
}

/* shared input styles used for text fields, textareas, and selects */
.input {
  width: 100%;
  padding: .5rem .75rem;
  border: 1.5px solid var(--border);
  border-radius: var(--r-sm);
  font-size: .875rem;
  color: var(--text);
  background: var(--surface);
  outline: none;
  transition: border-color var(--transition), box-shadow var(--transition);
}
.input:focus {
  border-color: var(--border-focus);
  box-shadow: 0 0 0 3px rgba(232,85,42,.12);
}

/* base button styles shared by all button variants */
.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: .35rem;
  padding: .475rem .9rem;
  border: none; border-radius: var(--r-sm);
  font-size: .8rem; font-weight: 600; cursor: pointer;
  transition: background var(--transition), transform var(--transition), box-shadow var(--transition);
  white-space: nowrap;
}
/* tiny scale-down effect when you click a button */
.btn:active { transform: scale(.97); }
.btn--primary  { background: var(--primary); color: #fff; }
.btn--primary:hover  { background: var(--primary-dim); box-shadow: 0 2px 8px rgba(232,85,42,.35); }
.btn--danger   { background: var(--danger-bg); color: var(--danger); }
.btn--danger:hover   { background: var(--danger); color: #fff; }
.btn--ghost    { background: transparent; color: var(--text-soft); border: 1.5px solid var(--border); }
.btn--ghost:hover    { background: var(--bg); }
.btn--success  { background: var(--success-bg); color: var(--success); }
.btn--success:hover  { background: var(--success); color: #fff; }
.btn--icon {
  padding: .35rem; background: transparent; color: var(--text-muted); border-radius: var(--r-sm);
}
.btn--icon:hover { background: var(--bg); color: var(--danger); }
.btn[disabled]  { opacity: .55; cursor: not-allowed; pointer-events: none; }

/* toast notification fixed to the bottom-right corner of the screen */
.toast {
  position: fixed; bottom: 1.5rem; right: 1.5rem;
  display: flex; align-items: center; gap: .6rem;
  padding: .75rem 1.25rem;
  border-radius: var(--r);
  font-size: .875rem; font-weight: 500;
  box-shadow: var(--shadow-lg);
  z-index: 9999; max-width: 400px;
}
.toast--success { background: #1c2b1e; color: #6ee7b7; }
.toast--error   { background: #2b1c1c; color: #fca5a5; }
.toast-icon     { font-size: 1rem; font-weight: 700; }

/* Vue's <Transition> component needs these classes to animate enter/leave */
.toast-enter-active, .toast-leave-active { transition: all .3s ease; }
/* slide up and fade in when appearing, slide down and fade out when leaving */
.toast-enter-from  { opacity: 0; transform: translateY(12px) scale(.95); }
.toast-leave-to    { opacity: 0; transform: translateY(12px) scale(.95); }

/* small helper/utility classes I use in multiple places in the template */
.mt-2 { margin-top: .5rem; }
.mt-3 { margin-top: .75rem; }
.flex { display: flex; }
.gap-2 { gap: .5rem; }
</style>
