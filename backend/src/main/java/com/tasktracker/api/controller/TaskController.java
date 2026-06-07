package com.tasktracker.api.controller;

import com.tasktracker.api.dto.TaskRequest;
import com.tasktracker.api.entity.Task;
import com.tasktracker.api.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// REST controller for all task-related endpoints
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET /api/tasks - returns all tasks with their categories
    @GetMapping
    public List<Task> getAll() {
        return taskService.findAll();
    }

    // GET /api/tasks/{id} - returns one task or 404
    @GetMapping("/{id}")
    public Task getById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    // POST /api/tasks - creates a new task, responds with 201 Created
    // we use TaskRequest (a DTO) instead of Task directly so the frontend can't set the ID
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@Valid @RequestBody TaskRequest request) {
        return taskService.create(request);
    }

    // PUT /api/tasks/{id} - updates all fields of an existing task
    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    // DELETE /api/tasks/{id} - deletes a task, responds with 204 No Content
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}
