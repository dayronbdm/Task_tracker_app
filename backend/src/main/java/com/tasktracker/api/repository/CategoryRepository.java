package com.tasktracker.api.repository;

import com.tasktracker.api.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Spring Data JPA automatically generates the SQL for basic CRUD operations
// we just need to add custom queries for cases where we need to JOIN fetch related data
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // LEFT JOIN FETCH loads the tasks in the same query instead of lazy loading them one by one
    // DISTINCT is needed because the join can produce duplicate category rows
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.tasks")
    List<Category> findAllWithTasks();

    // same idea but for a single category by ID
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.tasks WHERE c.id = :id")
    Optional<Category> findByIdWithTasks(@Param("id") Long id);
}
