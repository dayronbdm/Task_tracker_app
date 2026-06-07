package com.tasktracker.api.repository;

import com.tasktracker.api.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // JOIN FETCH the category in the same query to avoid the N+1 problem
    // (without this, loading 10 tasks would fire 10 extra queries to get each category)
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category")
    List<Task> findAllWithCategory();

    // same idea but for a single task
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category WHERE t.id = :id")
    Optional<Task> findByIdWithCategory(@Param("id") Long id);

    // Spring Data generates the SQL for this automatically from the method name
    List<Task> findByCategoryId(Long categoryId);
}
