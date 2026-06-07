package com.tasktracker.api.service;

import com.tasktracker.api.dto.TaskRequest;
import com.tasktracker.api.entity.Category;
import com.tasktracker.api.entity.Task;
import com.tasktracker.api.repository.CategoryRepository;
import com.tasktracker.api.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class TaskService {

    // we need both repositories because tasks can be linked to categories
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        // use the custom query that JOIN FETCHes the category to avoid N+1 queries
        return taskRepository.findAllWithCategory();
    }

    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return taskRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id " + id));
    }

    public Task create(TaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);  // fill in the fields from the request DTO
        return taskRepository.save(task);
    }

    public Task update(Long id, TaskRequest request) {
        Task task = findById(id);  // throws 404 if the task doesn't exist
        applyRequest(task, request);
        return taskRepository.save(task);
    }

    public void delete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found with id " + id));
        taskRepository.delete(task);
    }

    // shared helper used by both create and update to avoid duplicate code
    private void applyRequest(Task task, TaskRequest request) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.isCompleted());

        // if a categoryId was provided, look up the category and assign it
        // if not, set category to null (task has no category)
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Category not found with id " + request.getCategoryId()));
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }
    }
}
