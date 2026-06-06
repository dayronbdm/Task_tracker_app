import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8081',
  headers: { 'Content-Type': 'application/json' },
})

export const categoriesApi = {
  getAll:  ()              => http.get('/api/categories'),
  create:  (payload)       => http.post('/api/categories', payload),
  update:  (id, payload)   => http.put(`/api/categories/${id}`, payload),
  remove:  (id)            => http.delete(`/api/categories/${id}`),
}

export const tasksApi = {
  getAll:  ()              => http.get('/api/tasks'),
  create:  (payload)       => http.post('/api/tasks', payload),
  update:  (id, payload)   => http.put(`/api/tasks/${id}`, payload),
  remove:  (id)            => http.delete(`/api/tasks/${id}`),
}
