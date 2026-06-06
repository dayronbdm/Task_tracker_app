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

    // Eagerly join category so each GET /api/tasks doesn't fire N+1 queries
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category")
    List<Task> findAllWithCategory();

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category WHERE t.id = :id")
    Optional<Task> findByIdWithCategory(@Param("id") Long id);

    List<Task> findByCategoryId(Long categoryId);
}
