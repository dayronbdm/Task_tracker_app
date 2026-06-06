package com.tasktracker.api.service;

import com.tasktracker.api.entity.Category;
import com.tasktracker.api.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllWithTasks();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id " + id));
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category incoming) {
        Category existing = findById(id);
        existing.setName(incoming.getName());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        // Use the tasks-fetched variant so the orphanRemoval cascade fires correctly
        Category existing = categoryRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id " + id));
        categoryRepository.delete(existing);
    }
}
