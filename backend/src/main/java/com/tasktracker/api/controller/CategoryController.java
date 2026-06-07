package com.tasktracker.api.controller;

import com.tasktracker.api.entity.Category;
import com.tasktracker.api.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController means all methods return JSON by default (no need for @ResponseBody on each one)
// @RequestMapping sets the base URL path for all endpoints in this class
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    // injected via constructor - Spring handles this automatically
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /api/categories - returns all categories
    @GetMapping
    public List<Category> getAll() {
        return categoryService.findAll();
    }

    // GET /api/categories/{id} - returns a single category or 404
    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    // POST /api/categories - creates a new category, responds with 201 Created
    // @Valid triggers the @NotBlank validation on the Category fields
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody Category category) {
        return categoryService.create(category);
    }

    // PUT /api/categories/{id} - updates an existing category
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody Category category) {
        return categoryService.update(id, category);
    }

    // DELETE /api/categories/{id} - deletes a category and all its tasks, responds with 204 No Content
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
