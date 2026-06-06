export interface Category {
  id: number
  name: string
}

export interface Task {
  id: number
  title: string
  description: string
  completed: boolean
  category: Category | null
}

export interface CategoryPayload {
  name: string
}

export interface TaskPayload {
  title: string
  description: string
  categoryId: number | null
  completed: boolean
}

export interface Toast {
  message: string
  type: 'success' | 'error'
}
