package com.tasktracker.api.service;

import com.tasktracker.api.entity.Category;
import com.tasktracker.api.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// @Service marks this as a Spring-managed service bean
// @Transactional wraps every method in a database transaction by default
@Service
@Transactional
public class CategoryService {

    // constructor injection - Spring automatically passes the repository in
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // readOnly = true is a small optimization - tells the DB we won't be writing anything
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllWithTasks();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        // orElseThrow returns a 404 response if the category doesn't exist
        return categoryRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id " + id));
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category incoming) {
        // first make sure the category exists, then update its name
        Category existing = findById(id);
        existing.setName(incoming.getName());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        // we use findByIdWithTasks here so that Hibernate knows about the tasks
        // that way the orphanRemoval on the @OneToMany relationship fires correctly
        // and deletes all the tasks in the same transaction
        Category existing = categoryRepository.findByIdWithTasks(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id " + id));
        categoryRepository.delete(existing);
    }
}
