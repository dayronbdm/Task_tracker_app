import axios, { type AxiosResponse } from 'axios'
import type { Category, Task, CategoryPayload, TaskPayload } from '../types'

const http = axios.create({
  baseURL: 'http://localhost:8081',
  headers: { 'Content-Type': 'application/json' },
})

export const categoriesApi = {
  getAll:  ():                                   Promise<AxiosResponse<Category[]>> => http.get('/api/categories'),
  create:  (payload: CategoryPayload):           Promise<AxiosResponse<Category>>   => http.post('/api/categories', payload),
  update:  (id: number, payload: CategoryPayload): Promise<AxiosResponse<Category>> => http.put(`/api/categories/${id}`, payload),
  remove:  (id: number):                         Promise<AxiosResponse<void>>       => http.delete(`/api/categories/${id}`),
}

export const tasksApi = {
  getAll:  ():                               Promise<AxiosResponse<Task[]>> => http.get('/api/tasks'),
  create:  (payload: TaskPayload):           Promise<AxiosResponse<Task>>   => http.post('/api/tasks', payload),
  update:  (id: number, payload: TaskPayload): Promise<AxiosResponse<Task>> => http.put(`/api/tasks/${id}`, payload),
  remove:  (id: number):                     Promise<AxiosResponse<void>>   => http.delete(`/api/tasks/${id}`),
}
